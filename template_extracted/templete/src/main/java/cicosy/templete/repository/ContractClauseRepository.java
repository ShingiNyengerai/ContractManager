package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractClauseRepository extends JpaRepository<ContractClause, Long> {

    List<ContractClause> findByContract(Contract contract);
    
    List<ContractClause> findByContractAndMandatory(Contract contract, boolean mandatory);
    
    List<ContractClause> findByTitleContainingIgnoreCase(String title);
    
    List<ContractClause> findByContentContainingIgnoreCase(String content);
    
    List<ContractClause> findBySection(String section);
    
    List<ContractClause> findByContractOrderByOrderIndexAsc(Contract contract);
}