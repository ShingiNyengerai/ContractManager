package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractClause;

import java.util.List;

public interface ContractClauseService {

    /**
     * Find a contract clause by ID
     */
    ContractClause findById(Long id);
    
    /**
     * Find all contract clauses
     */
    List<ContractClause> findAll();
    
    /**
     * Find clauses by contract
     */
    List<ContractClause> findByContract(Contract contract);
    
    /**
     * Find clauses by contract and mandatory flag
     */
    List<ContractClause> findByContractAndMandatory(Contract contract, boolean mandatory);
    
    /**
     * Find clauses by title containing text (case-insensitive)
     */
    List<ContractClause> findByTitleContaining(String title);
    
    /**
     * Find clauses by content containing text (case-insensitive)
     */
    List<ContractClause> findByContentContaining(String content);
    
    /**
     * Find clauses by section
     */
    List<ContractClause> findBySection(String section);
    
    /**
     * Find clauses by contract ordered by order index
     */
    List<ContractClause> findByContractOrderByOrderIndex(Contract contract);
    
    /**
     * Create a new contract clause
     */
    ContractClause createClause(ContractClause clause);
    
    /**
     * Update an existing contract clause
     */
    ContractClause updateClause(ContractClause clause);
    
    /**
     * Delete a contract clause
     */
    void deleteClause(Long id);
    
    /**
     * Reorder clauses for a contract
     */
    void reorderClauses(Contract contract, List<Long> clauseIds);
    
    /**
     * Move a clause to a new position
     */
    void moveClause(Long clauseId, int newPosition);
    
    /**
     * Toggle the mandatory flag for a clause
     */
    void toggleMandatory(Long clauseId);
    
    /**
     * Add a clause to a contract
     */
    ContractClause addClauseToContract(Long contractId, ContractClause clause);
    
    /**
     * Remove a clause from a contract
     */
    void removeClauseFromContract(Long contractId, Long clauseId);
    
    /**
     * Import clauses from a template to a contract
     */
    List<ContractClause> importClausesFromTemplate(Long contractId, Long templateId);
    
    /**
     * Export clauses from a contract to a template
     */
    void exportClausesToTemplate(Long contractId, Long templateId);
    
    /**
     * Find clauses by keywords in content
     */
    List<ContractClause> findByKeywords(String keywords);
    
    /**
     * Clone a clause
     */
    ContractClause cloneClause(Long clauseId, Long targetContractId);
}