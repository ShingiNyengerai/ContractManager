package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByOwner(User owner);

    List<Contract> findByContractType(ContractType contractType);

    // FIXED: Changed to counterpartyName since clientName field doesn't exist
    List<Contract> findByCounterpartyNameContainingIgnoreCase(String counterpartyName);

    List<Contract> findByTitleContainingIgnoreCase(String title);

    List<Contract> findByReferenceNumberContainingIgnoreCase(String referenceNumber);

    List<Contract> findByActive(boolean active);

    List<Contract> findByEndDateBefore(LocalDate date);

    List<Contract> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT c FROM Contract c WHERE c.endDate <= :expiryDate AND c.active = true")
    List<Contract> findExpiringContracts(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT c FROM Contract c JOIN c.milestones m WHERE m.dueDate <= :dueDate AND m.completed = false")
    List<Contract> findContractsWithUpcomingMilestones(@Param("dueDate") LocalDate dueDate);

    // FIXED: Changed from contractValue to value to match the actual entity field
    @Query("SELECT c FROM Contract c WHERE c.value >= :minValue AND c.value <= :maxValue")
    List<Contract> findByContractValueRange(@Param("minValue") BigDecimal minValue, @Param("maxValue") BigDecimal maxValue);

    // Additional helpful queries for better contract management
    List<Contract> findByStatus(Contract.Status status);

    List<Contract> findByOwnerAndStatus(User owner, Contract.Status status);

    @Query("SELECT c FROM Contract c WHERE c.owner = :owner AND c.status IN :statuses")
    List<Contract> findByOwnerAndStatusIn(@Param("owner") User owner, @Param("statuses") List<Contract.Status> statuses);

    // Find contracts by date range
    @Query("SELECT c FROM Contract c WHERE c.startDate >= :startDate AND c.startDate <= :endDate")
    List<Contract> findByStartDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find contracts created within a date range
    @Query("SELECT c FROM Contract c WHERE c.createdDate >= :startDate AND c.createdDate <= :endDate")
    List<Contract> findByCreatedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}