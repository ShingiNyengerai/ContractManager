package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing ContractType operations
 */
public interface ContractTypeService {

    /**
     * Get all available contract types
     * 
     * @return List of all ContractType enum values
     */
    List<ContractType> getAllContractTypes();
    
    /**
     * Get contract type by name
     * 
     * @param name The name of the contract type
     * @return The ContractType enum value if found, null otherwise
     */
    ContractType getContractTypeByName(String name);
    
    /**
     * Find contracts by contract type
     * 
     * @param contractType The contract type to search for
     * @return List of contracts with the specified contract type
     */
    List<Contract> findContractsByType(ContractType contractType);
    
    /**
     * Find templates by contract type
     * 
     * @param contractType The contract type to search for
     * @return List of contract templates with the specified contract type
     */
    List<ContractTemplate> findTemplatesByType(ContractType contractType);
    
    /**
     * Count contracts by contract type
     * 
     * @return Map with contract types as keys and counts as values
     */
    Map<ContractType, Long> countContractsByType();
    
    /**
     * Count templates by contract type
     * 
     * @return Map with contract types as keys and counts as values
     */
    Map<ContractType, Long> countTemplatesByType();
    
    /**
     * Get the most common contract type based on existing contracts
     * 
     * @return The most frequently used ContractType
     */
    ContractType getMostCommonContractType();
    
    /**
     * Check if a contract type is in use
     * 
     * @param contractType The contract type to check
     * @return true if the contract type is used by any contract or template, false otherwise
     */
    boolean isContractTypeInUse(ContractType contractType);
    
    /**
     * Get contract types that have active contracts
     * 
     * @return List of contract types that have at least one active contract
     */
    List<ContractType> getContractTypesWithActiveContracts();
    
    /**
     * Get contract types that have templates
     * 
     * @return List of contract types that have at least one template
     */
    List<ContractType> getContractTypesWithTemplates();
    
    /**
     * Get contract types without templates
     * 
     * @return List of contract types that don't have any templates
     */
    List<ContractType> getContractTypesWithoutTemplates();
}