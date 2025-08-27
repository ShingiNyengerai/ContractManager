package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.RenewalNotification;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.repository.RenewalNotificationRepository;
import cicosy.templete.service.RenewalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RenewalNotificationServiceImpl implements RenewalNotificationService {

    private final RenewalNotificationRepository renewalRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public RenewalNotificationServiceImpl(RenewalNotificationRepository renewalRepository,
                                          ContractRepository contractRepository) {
        this.renewalRepository = renewalRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public RenewalNotification findById(Long id) {
        Optional<RenewalNotification> renewal = renewalRepository.findById(id);
        return renewal.orElse(null);
    }

    @Override
    public List<RenewalNotification> findAll() {
        return renewalRepository.findAll();
    }

    @Override
    public List<RenewalNotification> findByContract(Contract contract) {
        return renewalRepository.findByContract(contract);
    }

    @Override
    public List<RenewalNotification> findBySent(boolean sent) {
        return renewalRepository.findBySent(sent);
    }

    @Override
    public List<RenewalNotification> findByNotificationDateBefore(LocalDate date) {
        return renewalRepository.findByNotificationDateBefore(date);
    }

    @Override
    public List<RenewalNotification> findByNotificationDateBetween(LocalDate startDate, LocalDate endDate) {
        return renewalRepository.findByNotificationDateBetween(startDate, endDate);
    }

    @Override
    public List<RenewalNotification> findPendingNotifications() {
        return renewalRepository.findPendingNotifications(LocalDate.now());
    }

    @Override
    public RenewalNotification createNotification(RenewalNotification notification) {
        return renewalRepository.save(notification);
    }

    @Override
    public RenewalNotification updateNotification(RenewalNotification notification) {
        return renewalRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        renewalRepository.deleteById(id);
    }

    @Override
    public void markAsSent(Long id) {
        Optional<RenewalNotification> optional = renewalRepository.findById(id);
        if (optional.isPresent()) {
            RenewalNotification renewal = optional.get();
            renewal.setSent(true);
            renewal.setSentDate(LocalDate.now());
            renewalRepository.save(renewal);
        }
    }

    @Override
    public void markAsUnsent(Long id) {
        Optional<RenewalNotification> optional = renewalRepository.findById(id);
        if (optional.isPresent()) {
            RenewalNotification renewal = optional.get();
            renewal.setSent(false);
            renewalRepository.save(renewal);
        }
    }

    @Override
    public void updateNotificationDate(Long id, LocalDate newDate) {
        Optional<RenewalNotification> optional = renewalRepository.findById(id);
        if (optional.isPresent()) {
            RenewalNotification renewal = optional.get();
            renewal.setNotificationDate(newDate);
            renewalRepository.save(renewal);
        }
    }

    @Override
    public void updateMessage(Long id, String newMessage) {
        Optional<RenewalNotification> optional = renewalRepository.findById(id);
        if (optional.isPresent()) {
            RenewalNotification renewal = optional.get();
            renewal.setMessage(newMessage);
            renewalRepository.save(renewal);
        }
    }

    @Override
    public void generateRenewalNotifications(Long contractId, Integer[] daysBeforeExpiry) {
        Optional<Contract> optionalContract = contractRepository.findById(contractId);
        if (optionalContract.isEmpty() || daysBeforeExpiry == null) return;
        Contract contract = optionalContract.get();
        LocalDate renewalDate = contract.getEndDate();
        if (renewalDate == null) return;
        for (Integer days : daysBeforeExpiry) {
            if (days == null) continue;
            LocalDate notificationDate = renewalDate.minusDays(days);
            RenewalNotification rn = new RenewalNotification();
            rn.setContract(contract);
            rn.setDaysBeforeExpiry(days);
            rn.setMessage("Reminder: Contract '" + contract.getTitle() + "' expires in " + days + " day(s).");
            rn.setNotificationDate(notificationDate);
            rn.setSent(false);
            renewalRepository.save(rn);
        }
    }

    @Override
    public int countPendingNotificationsByContract(Long contractId) {
        Optional<Contract> optionalContract = contractRepository.findById(contractId);
        if (optionalContract.isEmpty()) return 0;
        Contract contract = optionalContract.get();
        LocalDate today = LocalDate.now();
        return (int) renewalRepository.findByContract(contract).stream()
                .filter(r -> !r.isSent() && !r.getNotificationDate().isAfter(today))
                .count();
    }

    @Override
    public List<RenewalNotification> findNotificationsDueToday() {
        LocalDate today = LocalDate.now();
        return renewalRepository.findByNotificationDateBetween(today, today).stream()
                .filter(r -> !r.isSent())
                .collect(Collectors.toList());
    }

    @Override
    public void sendNotification(Long id) {
        // For now, sending a notification simply marks it as sent with a sent date
        markAsSent(id);
    }

    @Override
    public List<RenewalNotification> findUpcomingRenewals(int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        return renewalRepository.findByNotificationDateBetween(start, end).stream()
                .filter(r -> !r.isSent())
                .collect(Collectors.toList());
    }

    @Override
    public List<RenewalNotification> findOverdueRenewals() {
        LocalDate today = LocalDate.now();
        return renewalRepository.findByNotificationDateBefore(today).stream()
                .filter(r -> !r.isSent())
                .collect(Collectors.toList());
    }

    @Override
    public Object toCalendarEvents(List<RenewalNotification> renewals) {
        return null;
    }
}