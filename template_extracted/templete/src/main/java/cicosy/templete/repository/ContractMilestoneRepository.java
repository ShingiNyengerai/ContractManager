package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractMilestoneRepository extends JpaRepository<ContractMilestone, Long> {

    List<ContractMilestone> findByContract(Contract contract);
    
    List<ContractMilestone> findByContractAndCompleted(Contract contract, boolean completed);
    
    List<ContractMilestone> findByDueDateBefore(LocalDate date);
    
    List<ContractMilestone> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT m FROM ContractMilestone m WHERE m.dueDate <= :date AND m.completed = false")
    List<ContractMilestone> findOverdueMilestones(LocalDate date);
    
    @Query("SELECT m FROM ContractMilestone m WHERE m.dueDate BETWEEN :startDate AND :endDate AND m.completed = false")
    List<ContractMilestone> findUpcomingMilestones(LocalDate startDate, LocalDate endDate);
}