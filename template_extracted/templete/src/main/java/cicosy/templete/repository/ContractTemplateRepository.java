package cicosy.templete.repository;

import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {

    List<ContractTemplate> findByActive(boolean active);
    
    List<ContractTemplate> findByContractType(ContractType contractType);
    
    List<ContractTemplate> findByCreatedBy(User createdBy);
    
    List<ContractTemplate> findByNameContainingIgnoreCase(String name);
}