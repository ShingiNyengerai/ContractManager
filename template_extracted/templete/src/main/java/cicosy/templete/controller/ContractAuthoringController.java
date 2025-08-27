package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractClause;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractAuthoringService;
import cicosy.templete.service.ContractService;
import cicosy.templete.service.ContractTemplateService;
import cicosy.templete.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/contracts/authoring")
public class ContractAuthoringController {
	private final ContractAuthoringService authoringService;
	private final ContractTemplateService templateService;
	private final ContractService contractService;
	private final UserService userService;

	@Autowired
	public ContractAuthoringController(ContractAuthoringService authoringService,
										 ContractTemplateService templateService,
										 ContractService contractService,
										 UserService userService) {
		this.authoringService = authoringService;
		this.templateService = templateService;
		this.contractService = contractService;
		this.userService = userService;
	}

	@GetMapping("/wizard")
	public String authoringWizard(Model model) {
		model.addAttribute("templates", templateService.findByActive(true));
		return "contracts/authoring-wizard";
	}

	@PostMapping("/start/{templateId}")
	public String startFromTemplate(@PathVariable Long templateId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = userService.findByUsername(auth.getName());
		Contract contract = authoringService.startFromTemplate(templateId, currentUser.getId());
		return "redirect:/contracts/edit/" + contract.getId();
	}

	@GetMapping("/{contractId}/clauses")
	public String listClauses(@PathVariable Long contractId, Model model) {
		List<ContractClause> clauses = authoringService.listContractClauses(contractId);
		model.addAttribute("clauses", clauses);
		model.addAttribute("contractId", contractId);
		return "contracts/authoring-clauses";
	}

	@PostMapping("/{contractId}/clauses")
	public String addClause(@PathVariable Long contractId, @ModelAttribute ContractClause clause) {
		authoringService.addClause(contractId, clause);
		return "redirect:/contracts/authoring/" + contractId + "/clauses";
	}

	@PostMapping("/{contractId}/clauses/{clauseId}/delete")
	public String deleteClause(@PathVariable Long contractId, @PathVariable Long clauseId) {
		authoringService.removeClause(contractId, clauseId);
		return "redirect:/contracts/authoring/" + contractId + "/clauses";
	}
}