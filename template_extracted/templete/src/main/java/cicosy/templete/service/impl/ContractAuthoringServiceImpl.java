package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractClause;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.repository.UserRepository;
import cicosy.templete.service.ContractAuthoringService;
import cicosy.templete.service.ContractClauseService;
import cicosy.templete.service.ContractTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContractAuthoringServiceImpl implements ContractAuthoringService {
	private final ContractRepository contractRepository;
	private final ContractTemplateService templateService;
	private final ContractClauseService clauseService;
	private final UserRepository userRepository;

	@Autowired
	public ContractAuthoringServiceImpl(ContractRepository contractRepository,
										 ContractTemplateService templateService,
										 ContractClauseService clauseService,
										 UserRepository userRepository) {
		this.contractRepository = contractRepository;
		this.templateService = templateService;
		this.clauseService = clauseService;
		this.userRepository = userRepository;
	}

	@Override
	public Contract startFromTemplate(Long templateId, Long ownerUserId) {
		ContractTemplate template = templateService.findById(templateId);
		User owner = userRepository.findById(ownerUserId).orElse(null);
		if (template == null || owner == null) return null;
		Contract contract = new Contract();
		contract.setOwner(owner);
		contract.setTemplate(template);
		contract.setTitle(template.getName());
		contract.setDescription(template.getDescription());
		contract.setContent(template.getContent());
		// No defaultTermsAndConditions on template; leave null or set from template content if desired
		contract.setStartDate(LocalDate.now());
		return contractRepository.save(contract);
	}

	@Override
	public Contract applyTemplate(Long contractId, Long templateId) {
		Contract contract = contractRepository.findById(contractId).orElse(null);
		ContractTemplate template = templateService.findById(templateId);
		if (contract == null || template == null) return null;
		contract.setTemplate(template);
		contract.setContent(template.getContent());
		// No defaultTermsAndConditions on template; leave unchanged
		return contractRepository.save(contract);
	}

	@Override
	public List<ContractClause> listAvailableClauses(Long templateId) {
		// Clauses are not directly stored on templates in current model
		return List.of();
	}

	@Override
	public List<ContractClause> listContractClauses(Long contractId) {
		Contract contract = contractRepository.findById(contractId).orElse(null);
		if (contract == null) return List.of();
		return clauseService.findByContractOrderByOrderIndex(contract);
	}

	@Override
	public ContractClause addClause(Long contractId, ContractClause clause) {
		return clauseService.addClauseToContract(contractId, clause);
	}

	@Override
	public void removeClause(Long contractId, Long clauseId) {
		clauseService.removeClauseFromContract(contractId, clauseId);
	}

	@Override
	public ContractClause cloneClauseToContract(Long clauseId, Long targetContractId) {
		return clauseService.cloneClause(clauseId, targetContractId);
	}
}