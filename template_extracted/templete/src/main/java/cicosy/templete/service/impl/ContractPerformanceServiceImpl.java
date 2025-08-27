package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractMilestone;
import cicosy.templete.domain.ContractPerformance;
import cicosy.templete.repository.ContractMilestoneRepository;
import cicosy.templete.repository.ContractPerformanceRepository;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.service.ContractPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractPerformanceServiceImpl implements ContractPerformanceService {

    private final ContractPerformanceRepository performanceRepository;
    private final ContractMilestoneRepository milestoneRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public ContractPerformanceServiceImpl(ContractPerformanceRepository performanceRepository,
                                          ContractMilestoneRepository milestoneRepository,
                                          ContractRepository contractRepository) {
        this.performanceRepository = performanceRepository;
        this.milestoneRepository = milestoneRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public ContractPerformance findById(Long id) {
        return performanceRepository.findById(id).orElse(null);
    }

    @Override
    public List<ContractPerformance> findAll() {
        return performanceRepository.findAll();
    }

    @Override
    public ContractPerformance findByContract(Contract contract) {
        if (contract == null) return null;
        return performanceRepository.findByContract(contract);
    }

    @Override
    public List<ContractPerformance> findUnderperformingContracts(BigDecimal threshold) {
        return performanceRepository.findUnderperformingContracts(threshold);
    }

    @Override
    public List<ContractPerformance> findOverperformingContracts(BigDecimal threshold) {
        return performanceRepository.findOverperformingContracts(threshold);
    }

    @Override
    public List<ContractPerformance> findByLastEvaluationDateBefore(LocalDate date) {
        return performanceRepository.findByLastEvaluationDateBefore(date);
    }

    @Override
    public ContractPerformance createPerformance(ContractPerformance performance) {
        return performanceRepository.save(performance);
    }

    @Override
    public ContractPerformance updatePerformance(ContractPerformance performance) {
        return performanceRepository.save(performance);
    }

    @Override
    public void deletePerformance(Long id) {
        performanceRepository.deleteById(id);
    }

    @Override
    public ContractPerformance updateActualCost(Long id, BigDecimal actualCost) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        perf.setActualCost(actualCost == null ? BigDecimal.ZERO : actualCost);
        return performanceRepository.save(perf);
    }

    @Override
    public ContractPerformance updatePlannedCost(Long id, BigDecimal plannedCost) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        perf.setPlannedCost(plannedCost == null ? BigDecimal.ZERO : plannedCost);
        return performanceRepository.save(perf);
    }

    @Override
    public ContractPerformance updateCompletedMilestones(Long id, Integer completedMilestones) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        perf.setCompletedMilestones(completedMilestones == null ? 0 : completedMilestones);
        return performanceRepository.save(perf);
    }

    @Override
    public ContractPerformance updateTotalMilestones(Long id, Integer totalMilestones) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        perf.setTotalMilestones(totalMilestones == null ? 0 : totalMilestones);
        return performanceRepository.save(perf);
    }

    @Override
    public ContractPerformance addPerformanceNotes(Long id, String notes) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        perf.setPerformanceNotes(notes);
        return performanceRepository.save(perf);
    }

    @Override
    public ContractPerformance evaluatePerformance(Long id) {
        ContractPerformance perf = findById(id);
        if (perf == null) return null;
        BigDecimal planned = defaultZero(perf.getPlannedCost());
        BigDecimal actual = defaultNonZero(perf.getActualCost());
        BigDecimal index = planned.divide(actual, 2, RoundingMode.HALF_UP);
        perf.setPerformanceIndex(index);
        perf.setLastEvaluationDate(LocalDate.now());
        return performanceRepository.save(perf);
    }

    @Override
    public double calculateMilestoneCompletionPercentage(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return 0.0;
        List<ContractMilestone> milestones = milestoneRepository.findByContract(contract);
        if (milestones == null || milestones.isEmpty()) return 0.0;
        long total = milestones.size();
        long completed = milestones.stream().filter(ContractMilestone::isCompleted).count();
        return (completed * 100.0) / total;
    }

    @Override
    public BigDecimal calculateCostVariance(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return BigDecimal.ZERO;
        ContractPerformance perf = performanceRepository.findByContract(contract);
        if (perf == null) return BigDecimal.ZERO;
        BigDecimal planned = defaultZero(perf.getPlannedCost());
        BigDecimal actual = defaultZero(perf.getActualCost());
        return planned.subtract(actual);
    }

    @Override
    public BigDecimal calculateCostPerformanceIndex(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return BigDecimal.ZERO;
        ContractPerformance perf = performanceRepository.findByContract(contract);
        if (perf == null) return BigDecimal.ZERO;
        BigDecimal planned = defaultZero(perf.getPlannedCost());
        BigDecimal actual = defaultNonZero(perf.getActualCost());
        return planned.divide(actual, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<ContractPerformance> findContractsNeedingEvaluation(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        return performanceRepository.findByLastEvaluationDateBefore(cutoff);
    }

    @Override
    public String generatePerformanceReport(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return "Contract not found";
        ContractPerformance perf = performanceRepository.findByContract(contract);
        if (perf == null) return "No performance data available";
        double milestonePct = calculateMilestoneCompletionPercentage(contractId);
        BigDecimal variance = calculateCostVariance(contractId);
        BigDecimal cpi = calculateCostPerformanceIndex(contractId);
        return String.format(
                "Performance Report for Contract #%d:%n - Milestone Completion: %.2f%%%n - Cost Variance: %s%n - Cost Performance Index: %s",
                contract.getId(), milestonePct, variance.toPlainString(), cpi.toPlainString()
        );
    }

    @Override
    public Object calculateAverageMilestoneCompletionPercentage(List<Contract> contracts) {
        if (contracts == null || contracts.isEmpty()) return 0.0;
        double avg = contracts.stream()
                .mapToDouble(c -> calculateMilestoneCompletionPercentage(c.getId()))
                .average()
                .orElse(0.0);
        return avg;
    }

    @Override
    public Object findContractsWithDelayedMilestones(List<Contract> contracts) {
        if (contracts == null || contracts.isEmpty()) return List.of();
        LocalDate today = LocalDate.now();
        Set<Long> ids = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
        // Check overdue milestones per contract
        List<Contract> delayed = new ArrayList<>();
        for (Contract c : contracts) {
            List<ContractMilestone> open = milestoneRepository.findByContractAndCompleted(c, false);
            boolean hasOverdue = open.stream().anyMatch(m -> m.getDueDate() != null && !m.getDueDate().isAfter(today));
            if (hasOverdue) delayed.add(c);
        }
        // Ensure only provided contracts are returned
        return delayed.stream().filter(c -> ids.contains(c.getId())).collect(Collectors.toList());
    }

    @Override
    public Object findContractsWithCostVariance(List<Contract> contracts, double thresholdPercent) {
        if (contracts == null || contracts.isEmpty()) return List.of();
        List<Contract> out = new ArrayList<>();
        for (Contract c : contracts) {
            ContractPerformance perf = performanceRepository.findByContract(c);
            if (perf == null) continue;
            BigDecimal planned = perf.getPlannedCost();
            BigDecimal actual = perf.getActualCost();
            if (planned == null || planned.compareTo(BigDecimal.ZERO) == 0 || actual == null) continue;
            BigDecimal diff = actual.subtract(planned).abs();
            double pct = diff.divide(planned, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
            if (pct >= thresholdPercent) out.add(c);
        }
        return out;
    }

    @Override
    public Object findDelayedMilestones(Contract contract) {
        if (contract == null) return List.of();
        LocalDate today = LocalDate.now();
        List<ContractMilestone> open = milestoneRepository.findByContractAndCompleted(contract, false);
        return open.stream()
                .filter(m -> m.getDueDate() != null && !m.getDueDate().isAfter(today))
                .collect(Collectors.toList());
    }

    @Override
    public Object findCompletedMilestones(Contract contract) {
        if (contract == null) return List.of();
        return milestoneRepository.findByContractAndCompleted(contract, true);
    }

    @Override
    public Object findUpcomingMilestones(Contract contract) {
        if (contract == null) return List.of();
        LocalDate today = LocalDate.now();
        LocalDate in30 = today.plusDays(30);
        List<ContractMilestone> open = milestoneRepository.findByContractAndCompleted(contract, false);
        return open.stream()
                .filter(m -> m.getDueDate() != null && (m.getDueDate().isAfter(today) || m.getDueDate().isEqual(today))
                        && m.getDueDate().isBefore(in30.plusDays(1)))
                .collect(Collectors.toList());
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    private BigDecimal defaultNonZero(BigDecimal v) {
        return (v == null || v.compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ONE : v;
    }
}