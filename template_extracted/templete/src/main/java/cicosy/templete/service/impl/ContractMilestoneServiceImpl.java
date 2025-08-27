package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractMilestone;
import cicosy.templete.repository.ContractMilestoneRepository;
import cicosy.templete.service.ContractMilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContractMilestoneServiceImpl implements ContractMilestoneService {

    private final ContractMilestoneRepository milestoneRepository;

    @Autowired
    public ContractMilestoneServiceImpl(ContractMilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
    }

    @Override
    public ContractMilestone findById(Long id) {
        return milestoneRepository.findById(id).orElse(null);
    }

    @Override
    public List<ContractMilestone> findAll() {
        return milestoneRepository.findAll();
    }

    @Override
    public List<ContractMilestone> findByContract(Contract contract) {
        if (contract == null) return List.of();
        return milestoneRepository.findByContract(contract);
    }

    @Override
    public List<ContractMilestone> findByContractAndCompleted(Contract contract, boolean completed) {
        if (contract == null) return List.of();
        return milestoneRepository.findByContractAndCompleted(contract, completed);
    }

    @Override
    public List<ContractMilestone> findByDueDateBefore(LocalDate date) {
        return milestoneRepository.findByDueDateBefore(date);
    }

    @Override
    public List<ContractMilestone> findByDueDateBetween(LocalDate startDate, LocalDate endDate) {
        return milestoneRepository.findByDueDateBetween(startDate, endDate);
    }

    @Override
    public List<ContractMilestone> findOverdueMilestones() {
        return milestoneRepository.findOverdueMilestones(LocalDate.now());
    }

    @Override
    public List<ContractMilestone> findUpcomingMilestones(int daysAhead) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(daysAhead);
        return milestoneRepository.findUpcomingMilestones(start, end);
    }

    @Override
    public ContractMilestone createMilestone(ContractMilestone milestone) {
        return milestoneRepository.save(milestone);
    }

    @Override
    public ContractMilestone updateMilestone(ContractMilestone milestone) {
        return milestoneRepository.save(milestone);
    }

    @Override
    public void deleteMilestone(Long id) {
        milestoneRepository.deleteById(id);
    }

    @Override
    public void completeMilestone(Long id) {
        ContractMilestone milestone = findById(id);
        if (milestone != null) {
            milestone.setCompleted(true);
            milestoneRepository.save(milestone);
        }
    }

    @Override
    public void uncompleteMilestone(Long id) {
        ContractMilestone milestone = findById(id);
        if (milestone != null) {
            milestone.setCompleted(false);
            milestoneRepository.save(milestone);
        }
    }

    @Override
    public void updateMilestoneDueDate(Long id, LocalDate newDueDate) {
        ContractMilestone milestone = findById(id);
        if (milestone != null) {
            milestone.setDueDate(newDueDate);
            milestoneRepository.save(milestone);
        }
    }

    @Override
    public double calculateCompletionPercentage(Contract contract) {
        if (contract == null) return 0.0;
        List<ContractMilestone> milestones = milestoneRepository.findByContract(contract);
        if (milestones == null || milestones.isEmpty()) return 0.0;
        long total = milestones.size();
        long completed = milestones.stream().filter(ContractMilestone::isCompleted).count();
        return (completed * 100.0) / total;
    }
}