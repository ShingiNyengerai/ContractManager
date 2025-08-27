package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractAmendment;
import cicosy.templete.domain.User;

import java.time.LocalDate;
import java.util.List;

public interface ContractAmendmentService {

    ContractAmendment findById(Long id);
    
    List<ContractAmendment> findAll();
    
    List<ContractAmendment> findByContract(Contract contract);
    
    List<ContractAmendment> findByContractAndApproved(Contract contract, boolean approved);
    
    List<ContractAmendment> findByApprovedBy(User approvedBy);
    
    List<ContractAmendment> findByAmendmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ContractAmendment> findByTitleContaining(String title);
    
    ContractAmendment createAmendment(ContractAmendment amendment);
    
    ContractAmendment updateAmendment(ContractAmendment amendment);
    
    void deleteAmendment(Long id);
    
    void approveAmendment(Long id, User approver);
    
    void rejectAmendment(Long id);
    
    void applyAmendmentToContract(Long amendmentId);
}