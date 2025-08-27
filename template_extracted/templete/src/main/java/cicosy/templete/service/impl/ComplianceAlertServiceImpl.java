package cicosy.templete.service.impl;

import cicosy.templete.domain.ComplianceAlert;
import cicosy.templete.domain.ComplianceAlert.AlertSeverity;
import cicosy.templete.domain.Contract;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ComplianceAlertRepository;
import cicosy.templete.service.ComplianceAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ComplianceAlertServiceImpl implements ComplianceAlertService {

    private final ComplianceAlertRepository alertRepository;

    @Autowired
    public ComplianceAlertServiceImpl(ComplianceAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public ComplianceAlert findById(Long id) {
        Optional<ComplianceAlert> alert = alertRepository.findById(id);
        return alert.orElse(null);
    }

    @Override
    public List<ComplianceAlert> findAll() {
        return alertRepository.findAll();
    }

    @Override
    public List<ComplianceAlert> findByContract(Contract contract) {
        return alertRepository.findByContract(contract);
    }

    @Override
    public List<ComplianceAlert> findByResolved(boolean resolved) {
        return alertRepository.findByResolved(resolved);
    }

    @Override
    public List<ComplianceAlert> findByResolvedBy(User resolvedBy) {
        return alertRepository.findByResolvedBy(resolvedBy);
    }

    @Override
    public List<ComplianceAlert> findBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    @Override
    public List<ComplianceAlert> findByDueDateBefore(LocalDate date) {
        return alertRepository.findByDueDateBefore(date);
    }

    @Override
    public List<ComplianceAlert> findByDueDateBetween(LocalDate startDate, LocalDate endDate) {
        return alertRepository.findByDueDateBetween(startDate, endDate);
    }

    @Override
    public List<ComplianceAlert> findOverdueAlerts() {
        return alertRepository.findOverdueAlerts(LocalDate.now());
    }

    @Override
    public List<ComplianceAlert> findUpcomingAlerts(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        return alertRepository.findUpcomingAlerts(startDate, endDate);
    }

    @Override
    public ComplianceAlert createAlert(ComplianceAlert alert) {
        return alertRepository.save(alert);
    }

    @Override
    public ComplianceAlert updateAlert(ComplianceAlert alert) {
        return alertRepository.save(alert);
    }

    @Override
    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    @Override
    public void resolveAlert(Long id, User resolvedBy) {
        Optional<ComplianceAlert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            ComplianceAlert alert = optionalAlert.get();
            alert.setResolved(true);
            alert.setResolvedBy(resolvedBy);
            alert.setResolvedDate(LocalDate.now());
            alertRepository.save(alert);
        }
    }

    @Override
    public void unresolveAlert(Long id) {
        Optional<ComplianceAlert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            ComplianceAlert alert = optionalAlert.get();
            alert.setResolved(false);
            alert.setResolvedBy(null);
            alert.setResolvedDate(null);
            alertRepository.save(alert);
        }
    }

    @Override
    public void escalateAlert(Long id) {
        Optional<ComplianceAlert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            ComplianceAlert alert = optionalAlert.get();
            AlertSeverity currentSeverity = alert.getSeverity();
            
            switch (currentSeverity) {
                case LOW:
                    alert.setSeverity(AlertSeverity.MEDIUM);
                    break;
                case MEDIUM:
                    alert.setSeverity(AlertSeverity.HIGH);
                    break;
                case HIGH:
                    alert.setSeverity(AlertSeverity.CRITICAL);
                    break;
                case CRITICAL:
                    // Already at highest level
                    break;
            }
            alertRepository.save(alert);
        }
    }

    @Override
    public void deescalateAlert(Long id) {
        Optional<ComplianceAlert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            ComplianceAlert alert = optionalAlert.get();
            AlertSeverity currentSeverity = alert.getSeverity();
            
            switch (currentSeverity) {
                case CRITICAL:
                    alert.setSeverity(AlertSeverity.HIGH);
                    break;
                case HIGH:
                    alert.setSeverity(AlertSeverity.MEDIUM);
                    break;
                case MEDIUM:
                    alert.setSeverity(AlertSeverity.LOW);
                    break;
                case LOW:
                    // Already at lowest level
                    break;
            }
            alertRepository.save(alert);
        }
    }

    @Override
    public void updateDueDate(Long id, LocalDate newDueDate) {
        Optional<ComplianceAlert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            ComplianceAlert alert = optionalAlert.get();
            alert.setDueDate(newDueDate);
            alertRepository.save(alert);
        }
    }

    @Override
    public int countUnresolvedAlertsByContract(Long contractId) {
        // Using a simple approach since we don't have a specific repository method
        return (int) alertRepository.findAll().stream()
                .filter(alert -> alert.getContract().getId().equals(contractId) && !alert.isResolved())
                .count();
    }

    @Override
    public int countOverdueAlertsByContract(Long contractId) {
        LocalDate today = LocalDate.now();
        return (int) alertRepository.findAll().stream()
                .filter(alert -> alert.getContract().getId().equals(contractId) 
                        && alert.getDueDate().isBefore(today) && !alert.isResolved())
                .count();
    }
}