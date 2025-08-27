package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.RenewalNotification;

import java.time.LocalDate;
import java.util.List;

public interface RenewalNotificationService {

    RenewalNotification findById(Long id);
    
    List<RenewalNotification> findAll();
    
    List<RenewalNotification> findByContract(Contract contract);
    
    List<RenewalNotification> findBySent(boolean sent);
    
    List<RenewalNotification> findByNotificationDateBefore(LocalDate date);
    
    List<RenewalNotification> findByNotificationDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<RenewalNotification> findPendingNotifications();
    
    RenewalNotification createNotification(RenewalNotification notification);
    
    RenewalNotification updateNotification(RenewalNotification notification);
    
    void deleteNotification(Long id);
    
    void markAsSent(Long id);
    
    void markAsUnsent(Long id);
    
    void updateNotificationDate(Long id, LocalDate newDate);
    
    void updateMessage(Long id, String newMessage);
    
    void generateRenewalNotifications(Long contractId, Integer[] daysBeforeExpiry);
    
    int countPendingNotificationsByContract(Long contractId);
    
    List<RenewalNotification> findNotificationsDueToday();
    
    void sendNotification(Long id);
    
    List<RenewalNotification> findUpcomingRenewals(int days);
    
    List<RenewalNotification> findOverdueRenewals();

    Object toCalendarEvents(List<RenewalNotification> renewals);
}