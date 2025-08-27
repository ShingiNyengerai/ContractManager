package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractClause;
import cicosy.templete.domain.ContractTemplate;

import java.util.List;

public interface ContractAuthoringService {
	Contract startFromTemplate(Long templateId, Long ownerUserId);
	Contract applyTemplate(Long contractId, Long templateId);
	List<ContractClause> listAvailableClauses(Long templateId);
	List<ContractClause> listContractClauses(Long contractId);
	ContractClause addClause(Long contractId, ContractClause clause);
	void removeClause(Long contractId, Long clauseId);
	ContractClause cloneClauseToContract(Long clauseId, Long targetContractId);
}