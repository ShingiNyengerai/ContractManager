package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractAmendment;
import cicosy.templete.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractAmendmentRepository extends JpaRepository<ContractAmendment, Long> {

    List<ContractAmendment> findByContract(Contract contract);
    
    List<ContractAmendment> findByContractAndApproved(Contract contract, boolean approved);
    
    List<ContractAmendment> findByApprovedBy(User approvedBy);
    
    List<ContractAmendment> findByAmendmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ContractAmendment> findByTitleContainingIgnoreCase(String title);
}