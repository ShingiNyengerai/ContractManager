package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.RenewalNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RenewalNotificationRepository extends JpaRepository<RenewalNotification, Long> {

    List<RenewalNotification> findByContract(Contract contract);
    
    List<RenewalNotification> findBySent(boolean sent);
    
    List<RenewalNotification> findByNotificationDateBefore(LocalDate date);
    
    List<RenewalNotification> findByNotificationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT n FROM RenewalNotification n WHERE n.notificationDate <= :currentDate AND n.sent = false")
    List<RenewalNotification> findPendingNotifications(LocalDate currentDate);
}