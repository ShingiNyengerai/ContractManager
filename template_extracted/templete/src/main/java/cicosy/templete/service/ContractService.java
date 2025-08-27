package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ContractService {

    Contract findById(Long id);

    List<Contract> findAll();

    List<Contract> findByOwner(User owner);

    List<Contract> findByContractType(ContractType contractType);

    List<Contract> findByClientName(String clientName);

    List<Contract> findByTitle(String title);

    List<Contract> findByReferenceNumber(String referenceNumber);

    List<Contract> findActiveContracts();

    List<Contract> findExpiredContracts();

    List<Contract> findExpiringContracts(int daysToExpiry);

    List<Contract> findContractsWithUpcomingMilestones(int daysAhead);

    List<Contract> findByContractValueRange(BigDecimal minValue, BigDecimal maxValue);

    Contract createContract(Contract contract);

    Contract updateContract(Contract contract);

    void deleteContract(Long id);

    void activateContract(Long id);

    void deactivateContract(Long id);

    void signContract(Long id, LocalDate signedDate);

    // Fixed return types - changed Object to proper types
    BigDecimal calculateAverageContractValue();

    BigDecimal calculateTotalContractValue();

    Map<ContractType, Long> countContractsByType();

    Map<Contract.Status, Long> countContractsByStatus();

    Map<String, Long> countContractsByOwner();

    Map<String, Long> countContractsByMonth();

    BigDecimal calculateTotalContractValueForUser(User currentUser);

    BigDecimal calculateAverageContractValueForUser(User currentUser);

    Map<ContractType, Long> countContractsByTypeForUser(User currentUser);

    Map<Contract.Status, Long> countContractsByStatusForUser(User currentUser);

    Map<String, Long> countContractsByMonthForUser(User currentUser);

    // Fixed parameter type - changed Object o to User owner
    List<Contract> findByFilters(LocalDate startDate, LocalDate endDate, String contractType, String contractStatus, User owner);

    Map<ContractType, Long> countContractsByTypeInList(List<Contract> filteredContracts);

    Map<Contract.Status, Long> countContractsByStatusInList(List<Contract> filteredContracts);

    Map<String, Long> countContractsByMonthInRange(LocalDate startDate, LocalDate endDate, List<Contract> filteredContracts);

    // Fixed return type
    ContractType[] getAllContractTypes();

    // Core save method
    Contract save(Contract contract);
}