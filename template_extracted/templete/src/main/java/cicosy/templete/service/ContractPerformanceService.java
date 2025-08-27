package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractPerformance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractPerformanceService {

    /**
     * Find a contract performance by ID
     */
    ContractPerformance findById(Long id);
    
    /**
     * Find all contract performances
     */
    List<ContractPerformance> findAll();
    
    /**
     * Find contract performance by contract
     */
    ContractPerformance findByContract(Contract contract);
    
    /**
     * Find contracts with performance index below threshold
     */
    List<ContractPerformance> findUnderperformingContracts(BigDecimal threshold);
    
    /**
     * Find contracts with performance index above threshold
     */
    List<ContractPerformance> findOverperformingContracts(BigDecimal threshold);
    
    /**
     * Find contracts that haven't been evaluated since the given date
     */
    List<ContractPerformance> findByLastEvaluationDateBefore(LocalDate date);
    
    /**
     * Create a new contract performance record
     */
    ContractPerformance createPerformance(ContractPerformance performance);
    
    /**
     * Update an existing contract performance record
     */
    ContractPerformance updatePerformance(ContractPerformance performance);
    
    /**
     * Delete a contract performance record
     */
    void deletePerformance(Long id);
    
    /**
     * Update actual cost and recalculate performance index
     */
    ContractPerformance updateActualCost(Long id, BigDecimal actualCost);
    
    /**
     * Update planned cost and recalculate performance index
     */
    ContractPerformance updatePlannedCost(Long id, BigDecimal plannedCost);
    
    /**
     * Update completed milestones count and recalculate performance index
     */
    ContractPerformance updateCompletedMilestones(Long id, Integer completedMilestones);
    
    /**
     * Update total milestones count and recalculate performance index
     */
    ContractPerformance updateTotalMilestones(Long id, Integer totalMilestones);
    
    /**
     * Add performance notes
     */
    ContractPerformance addPerformanceNotes(Long id, String notes);
    
    /**
     * Evaluate contract performance and update performance index
     */
    ContractPerformance evaluatePerformance(Long id);
    
    /**
     * Calculate milestone completion percentage
     */
    double calculateMilestoneCompletionPercentage(Long id);
    
    /**
     * Calculate cost variance (planned cost - actual cost)
     */
    BigDecimal calculateCostVariance(Long id);
    
    /**
     * Calculate cost performance index (planned cost / actual cost)
     */
    BigDecimal calculateCostPerformanceIndex(Long id);
    
    /**
     * Find contracts that need evaluation (last evaluation date older than specified days)
     */
    List<ContractPerformance> findContractsNeedingEvaluation(int days);
    
    /**
     * Generate performance report for a contract
     */
    String generatePerformanceReport(Long contractId);

    Object calculateAverageMilestoneCompletionPercentage(List<Contract> contracts);

    Object findContractsWithDelayedMilestones(List<Contract> contracts);

    Object findContractsWithCostVariance(List<Contract> contracts, double v);

    Object findDelayedMilestones(Contract contract);

    Object findCompletedMilestones(Contract contract);

    Object findUpcomingMilestones(Contract contract);
}