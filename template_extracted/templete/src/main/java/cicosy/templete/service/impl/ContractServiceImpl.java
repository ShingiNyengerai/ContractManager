package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Override
    public Contract findById(Long id) {
        return contractRepository.findById(id).orElse(null);
    }

    @Override
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Override
    public List<Contract> findByOwner(User owner) {
        return contractRepository.findByOwner(owner);
    }

    @Override
    public List<Contract> findByContractType(ContractType contractType) {
        return contractRepository.findByContractType(contractType);
    }

    @Override
    public List<Contract> findByClientName(String clientName) {
        // Note: You might need to add this field back if you use client names
        return contractRepository.findByCounterpartyNameContainingIgnoreCase(clientName);
    }

    @Override
    public List<Contract> findByTitle(String title) {
        return contractRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Contract> findByReferenceNumber(String referenceNumber) {
        return contractRepository.findByReferenceNumberContainingIgnoreCase(referenceNumber);
    }

    @Override
    public List<Contract> findActiveContracts() {
        return contractRepository.findByActive(true);
    }

    @Override
    public List<Contract> findExpiredContracts() {
        return contractRepository.findByEndDateBefore(LocalDate.now());
    }

    @Override
    public List<Contract> findExpiringContracts(int daysToExpiry) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysToExpiry);
        return contractRepository.findExpiringContracts(expiryDate);
    }

    @Override
    public List<Contract> findContractsWithUpcomingMilestones(int daysAhead) {
        LocalDate dueDate = LocalDate.now().plusDays(daysAhead);
        return contractRepository.findContractsWithUpcomingMilestones(dueDate);
    }

    @Override
    public List<Contract> findByContractValueRange(BigDecimal minValue, BigDecimal maxValue) {
        return contractRepository.findByContractValueRange(minValue, maxValue);
    }

    @Override
    @Transactional
    public Contract createContract(Contract contract) {
        // Set creation date if not set
        if (contract.getCreatedDate() == null) {
            // This will be handled by @PrePersist
        }

        // Ensure default values
        if (contract.getValue() == null) {
            contract.setValue(BigDecimal.ZERO);
        }

        if (contract.getStatus() == null) {
            contract.setStatus(Contract.Status.DRAFT);
        }

        return contractRepository.save(contract);
    }

    @Override
    @Transactional
    public Contract updateContract(Contract contract) {
        Contract existingContract = contractRepository.findById(contract.getId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + contract.getId()));

        // Update fields
        existingContract.setTitle(contract.getTitle());
        existingContract.setContractType(contract.getContractType());
        existingContract.setStartDate(contract.getStartDate());
        existingContract.setEndDate(contract.getEndDate());
        existingContract.setCounterpartyName(contract.getCounterpartyName());
        existingContract.setCounterpartyEmail(contract.getCounterpartyEmail());
        existingContract.setValue(contract.getValue());
        existingContract.setStatus(contract.getStatus());
        existingContract.setContent(contract.getContent());
        existingContract.setTermsAndConditions(contract.getTermsAndConditions());
        existingContract.setNotes(contract.getNotes());
        existingContract.setActive(contract.isActive());
        existingContract.setTemplate(contract.getTemplate());

        return contractRepository.save(existingContract);
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateContract(Long id) {
        Contract contract = findById(id);
        if (contract != null) {
            contract.setActive(true);
            contract.setStatus(Contract.Status.ACTIVE);
            contractRepository.save(contract);
        }
    }

    @Override
    @Transactional
    public void deactivateContract(Long id) {
        Contract contract = findById(id);
        if (contract != null) {
            contract.setActive(false);
            contractRepository.save(contract);
        }
    }

    @Override
    @Transactional
    public void signContract(Long id, LocalDate signedDate) {
        Contract contract = findById(id);
        if (contract != null) {
            contract.setSignedDate(signedDate);
            contract.setStatus(Contract.Status.ACTIVE);
            contractRepository.save(contract);
        }
    }

    @Override
    public BigDecimal calculateAverageContractValue() {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .map(Contract::getValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(contracts.size()));
    }

    @Override
    public BigDecimal calculateTotalContractValue() {
        return contractRepository.findAll().stream()
                .map(Contract::getValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<ContractType, Long> countContractsByType() {
        return contractRepository.findAll().stream()
                .collect(Collectors.groupingBy(Contract::getContractType, Collectors.counting()));
    }

    @Override
    public Map<Contract.Status, Long> countContractsByStatus() {
        return contractRepository.findAll().stream()
                .collect(Collectors.groupingBy(Contract::getStatus, Collectors.counting()));
    }

    @Override
    public Map<String, Long> countContractsByOwner() {
        return contractRepository.findAll().stream()
                .filter(contract -> contract.getOwner() != null)
                .collect(Collectors.groupingBy(contract -> contract.getOwner().getUsername(), Collectors.counting()));
    }

    @Override
    public Map<String, Long> countContractsByMonth() {
        return contractRepository.findAll().stream()
                .filter(contract -> contract.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        contract -> contract.getCreatedDate().getYear() + "-" +
                                String.format("%02d", contract.getCreatedDate().getMonthValue()),
                        Collectors.counting()));
    }

    @Override
    public BigDecimal calculateTotalContractValueForUser(User currentUser) {
        return contractRepository.findByOwner(currentUser).stream()
                .map(Contract::getValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateAverageContractValueForUser(User currentUser) {
        List<Contract> contracts = contractRepository.findByOwner(currentUser);
        if (contracts.isEmpty()) return BigDecimal.ZERO;

        return contracts.stream()
                .map(Contract::getValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(contracts.size()));
    }

    @Override
    public Map<ContractType, Long> countContractsByTypeForUser(User currentUser) {
        return contractRepository.findByOwner(currentUser).stream()
                .collect(Collectors.groupingBy(Contract::getContractType, Collectors.counting()));
    }

    @Override
    public Map<Contract.Status, Long> countContractsByStatusForUser(User currentUser) {
        return contractRepository.findByOwner(currentUser).stream()
                .collect(Collectors.groupingBy(Contract::getStatus, Collectors.counting()));
    }

    @Override
    public Map<String, Long> countContractsByMonthForUser(User currentUser) {
        return contractRepository.findByOwner(currentUser).stream()
                .filter(contract -> contract.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        contract -> contract.getCreatedDate().getYear() + "-" +
                                String.format("%02d", contract.getCreatedDate().getMonthValue()),
                        Collectors.counting()));
    }

    @Override
    public List<Contract> findByFilters(LocalDate startDate, LocalDate endDate, String contractType, String contractStatus, User owner) {
        // Implementation would depend on creating custom repository methods or using Specifications
        // For now, returning all contracts - you should implement proper filtering
        return contractRepository.findAll().stream()
                .filter(contract -> {
                    boolean matches = true;

                    if (startDate != null && contract.getStartDate() != null) {
                        matches &= contract.getStartDate().isAfter(startDate) || contract.getStartDate().equals(startDate);
                    }

                    if (endDate != null && contract.getEndDate() != null) {
                        matches &= contract.getEndDate().isBefore(endDate) || contract.getEndDate().equals(endDate);
                    }

                    if (contractType != null && !contractType.isEmpty()) {
                        matches &= contract.getContractType().toString().equals(contractType);
                    }

                    if (contractStatus != null && !contractStatus.isEmpty()) {
                        matches &= contract.getStatus().toString().equals(contractStatus);
                    }

                    if (owner != null) {
                        matches &= contract.getOwner() != null && contract.getOwner().equals(owner);
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<ContractType, Long> countContractsByTypeInList(List<Contract> filteredContracts) {
        return filteredContracts.stream()
                .collect(Collectors.groupingBy(Contract::getContractType, Collectors.counting()));
    }

    @Override
    public Map<Contract.Status, Long> countContractsByStatusInList(List<Contract> filteredContracts) {
        return filteredContracts.stream()
                .collect(Collectors.groupingBy(Contract::getStatus, Collectors.counting()));
    }

    @Override
    public Map<String, Long> countContractsByMonthInRange(LocalDate startDate, LocalDate endDate, List<Contract> filteredContracts) {
        return filteredContracts.stream()
                .filter(contract -> contract.getCreatedDate() != null)
                .filter(contract -> {
                    LocalDate createdDate = contract.getCreatedDate();
                    return (startDate == null || createdDate.isAfter(startDate) || createdDate.equals(startDate)) &&
                            (endDate == null || createdDate.isBefore(endDate) || createdDate.equals(endDate));
                })
                .collect(Collectors.groupingBy(
                        contract -> contract.getCreatedDate().getYear() + "-" +
                                String.format("%02d", contract.getCreatedDate().getMonthValue()),
                        Collectors.counting()));
    }

    @Override
    public ContractType[] getAllContractTypes() {
        return ContractType.values();
    }

    @Override
    @Transactional
    public Contract save(Contract contract) {
        return contractRepository.save(contract);
    }
}