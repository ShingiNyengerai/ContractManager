package cicosy.templete.controller;

import cicosy.templete.domain.*;
import cicosy.templete.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

@Controller
@RequestMapping("/contracts/wizard")
public class ContractWizardController {
	private final ContractService contractService;
	private final ContractClauseService clauseService;
	private final ContractMilestoneService milestoneService;
	private final ApprovalWorkflowService workflowService;
	private final UserService userService;

	@Autowired
	public ContractWizardController(ContractService contractService,
									 ContractClauseService clauseService,
									 ContractMilestoneService milestoneService,
									 ApprovalWorkflowService workflowService,
									 UserService userService) {
		this.contractService = contractService;
		this.clauseService = clauseService;
		this.milestoneService = milestoneService;
		this.workflowService = workflowService;
		this.userService = userService;
	}

	@GetMapping("/details")
	public String stepDetails(@RequestParam(required = false) Long contractId, Model model) {
		Contract contract = contractId != null ? contractService.findById(contractId) : new Contract();
		if (contract.getStartDate() == null) contract.setStartDate(LocalDate.now());
		model.addAttribute("contract", contract);
		model.addAttribute("contractTypes", contractService.getAllContractTypes());
		return "contracts/wizard-details";
	}

	@PostMapping("/details")
	public String saveDetails(@ModelAttribute Contract contract) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = userService.findByUsername(auth.getName());
		if (contract.getOwner() == null) contract.setOwner(currentUser);
		Contract saved = contractService.save(contract);
		return "redirect:/contracts/wizard/clauses?contractId=" + saved.getId();
	}

	@GetMapping("/clauses")
	public String stepClauses(@RequestParam Long contractId, Model model) {
		Contract contract = contractService.findById(contractId);
		model.addAttribute("contract", contract);
		model.addAttribute("clauses", clauseService.findByContractOrderByOrderIndex(contract));
		return "contracts/wizard-clauses";
	}

	@PostMapping("/clauses/add")
	public String addClause(@RequestParam Long contractId, @ModelAttribute ContractClause clause) {
		clauseService.addClauseToContract(contractId, clause);
		return "redirect:/contracts/wizard/clauses?contractId=" + contractId;
	}

	@PostMapping("/clauses/delete")
	public String deleteClause(@RequestParam Long contractId, @RequestParam Long clauseId) {
		clauseService.removeClauseFromContract(contractId, clauseId);
		return "redirect:/contracts/wizard/clauses?contractId=" + contractId;
	}

	@PostMapping("/clauses/next")
	public String nextFromClauses(@RequestParam Long contractId) {
		return "redirect:/contracts/wizard/milestones?contractId=" + contractId;
	}

	@GetMapping("/milestones")
	public String stepMilestones(@RequestParam Long contractId, Model model) {
		Contract contract = contractService.findById(contractId);
		model.addAttribute("contract", contract);
		model.addAttribute("milestones", milestoneService.findByContract(contract));
		return "contracts/wizard-milestones";
	}

	@PostMapping("/milestones/add")
	public String addMilestone(@RequestParam Long contractId, @ModelAttribute ContractMilestone milestone) {
		Contract contract = contractService.findById(contractId);
		milestone.setContract(contract);
		milestoneService.createMilestone(milestone);
		return "redirect:/contracts/wizard/milestones?contractId=" + contractId;
	}

	@PostMapping("/milestones/next")
	public String nextFromMilestones(@RequestParam Long contractId) {
		return "redirect:/contracts/wizard/approvals?contractId=" + contractId;
	}

	@GetMapping("/approvals")
	public String stepApprovals(@RequestParam Long contractId, Model model) {
		Contract contract = contractService.findById(contractId);
		model.addAttribute("contract", contract);
		model.addAttribute("workflows", workflowService.findAll());
		return "contracts/wizard-approvals";
	}

	@PostMapping("/approvals/apply")
	public String applyWorkflow(@RequestParam Long contractId, @RequestParam Long workflowId) {
		Contract contract = contractService.findById(contractId);
		ApprovalWorkflow wf = workflowService.findById(workflowId);
		contract.setApprovalWorkflow(wf);
		contractService.save(contract);
		return "redirect:/contracts/view/" + contractId;
	}
}