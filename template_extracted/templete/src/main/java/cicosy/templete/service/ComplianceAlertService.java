package cicosy.templete.service;

import cicosy.templete.domain.ComplianceAlert;
import cicosy.templete.domain.ComplianceAlert.AlertSeverity;
import cicosy.templete.domain.Contract;
import cicosy.templete.domain.User;

import java.time.LocalDate;
import java.util.List;

public interface ComplianceAlertService {

    ComplianceAlert findById(Long id);
    
    List<ComplianceAlert> findAll();
    
    List<ComplianceAlert> findByContract(Contract contract);
    
    List<ComplianceAlert> findByResolved(boolean resolved);
    
    List<ComplianceAlert> findByResolvedBy(User resolvedBy);
    
    List<ComplianceAlert> findBySeverity(AlertSeverity severity);
    
    List<ComplianceAlert> findByDueDateBefore(LocalDate date);
    
    List<ComplianceAlert> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ComplianceAlert> findOverdueAlerts();
    
    List<ComplianceAlert> findUpcomingAlerts(int daysAhead);
    
    ComplianceAlert createAlert(ComplianceAlert alert);
    
    ComplianceAlert updateAlert(ComplianceAlert alert);
    
    void deleteAlert(Long id);
    
    void resolveAlert(Long id, User resolvedBy);
    
    void unresolveAlert(Long id);
    
    void escalateAlert(Long id);
    
    void deescalateAlert(Long id);
    
    void updateDueDate(Long id, LocalDate newDueDate);
    
    int countUnresolvedAlertsByContract(Long contractId);
    
    int countOverdueAlertsByContract(Long contractId);
}