package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractMilestone;

import java.time.LocalDate;
import java.util.List;

public interface ContractMilestoneService {

    ContractMilestone findById(Long id);
    
    List<ContractMilestone> findAll();
    
    List<ContractMilestone> findByContract(Contract contract);
    
    List<ContractMilestone> findByContractAndCompleted(Contract contract, boolean completed);
    
    List<ContractMilestone> findByDueDateBefore(LocalDate date);
    
    List<ContractMilestone> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ContractMilestone> findOverdueMilestones();
    
    List<ContractMilestone> findUpcomingMilestones(int daysAhead);
    
    ContractMilestone createMilestone(ContractMilestone milestone);
    
    ContractMilestone updateMilestone(ContractMilestone milestone);
    
    void deleteMilestone(Long id);
    
    void completeMilestone(Long id);
    
    void uncompleteMilestone(Long id);
    
    void updateMilestoneDueDate(Long id, LocalDate newDueDate);
    
    double calculateCompletionPercentage(Contract contract);
}