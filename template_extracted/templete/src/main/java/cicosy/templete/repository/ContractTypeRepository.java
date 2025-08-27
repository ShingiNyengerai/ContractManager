package cicosy.templete.repository;

import cicosy.templete.domain.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ContractType operations
 */
@Repository
public interface ContractTypeRepository {

    /**
     * Find contract types that have active contracts
     * 
     * @return List of contract types with active contracts
     */
    @Query("SELECT DISTINCT c.contractType FROM Contract c WHERE c.active = true")
    List<ContractType> findContractTypesWithActiveContracts();
    
    /**
     * Find contract types that have templates
     * 
     * @return List of contract types with templates
     */
    @Query("SELECT DISTINCT t.contractType FROM ContractTemplate t")
    List<ContractType> findContractTypesWithTemplates();
    
    /**
     * Count contracts by contract type
     * 
     * @return List of contract types with their counts
     */
    @Query("SELECT c.contractType, COUNT(c) FROM Contract c GROUP BY c.contractType")
    List<Object[]> countContractsByType();
    
    /**
     * Count templates by contract type
     * 
     * @return List of contract types with their template counts
     */
    @Query("SELECT t.contractType, COUNT(t) FROM ContractTemplate t GROUP BY t.contractType")
    List<Object[]> countTemplatesByType();
    
    /**
     * Find the most common contract type based on existing contracts
     * 
     * @return The most frequently used ContractType
     */
    @Query("SELECT c.contractType FROM Contract c GROUP BY c.contractType ORDER BY COUNT(c) DESC")
    List<ContractType> findMostCommonContractTypes();
}