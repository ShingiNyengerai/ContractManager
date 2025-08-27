package cicosy.templete.repository;

import cicosy.templete.domain.ComplianceAlert;
import cicosy.templete.domain.ComplianceAlert.AlertSeverity;
import cicosy.templete.domain.Contract;
import cicosy.templete.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComplianceAlertRepository extends JpaRepository<ComplianceAlert, Long> {

    List<ComplianceAlert> findByContract(Contract contract);
    
    List<ComplianceAlert> findByResolved(boolean resolved);
    
    List<ComplianceAlert> findByResolvedBy(User resolvedBy);
    
    List<ComplianceAlert> findBySeverity(AlertSeverity severity);
    
    List<ComplianceAlert> findByDueDateBefore(LocalDate date);
    
    List<ComplianceAlert> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM ComplianceAlert a WHERE a.dueDate <= :currentDate AND a.resolved = false")
    List<ComplianceAlert> findOverdueAlerts(LocalDate currentDate);
    
    @Query("SELECT a FROM ComplianceAlert a WHERE a.dueDate BETWEEN :startDate AND :endDate AND a.resolved = false")
    List<ComplianceAlert> findUpcomingAlerts(LocalDate startDate, LocalDate endDate);
}