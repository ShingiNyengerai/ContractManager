package cicosy.templete.controller;

import cicosy.templete.domain.ApprovalWorkflow;
import cicosy.templete.service.ApprovalWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contracts/workflows")
public class ApprovalWorkflowController {
	private final ApprovalWorkflowService workflowService;

	@Autowired
	public ApprovalWorkflowController(ApprovalWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("workflows", workflowService.findAll());
		return "contracts/workflows";
	}

	@GetMapping("/view/{id}")
	public String view(@PathVariable Long id, Model model) {
		model.addAttribute("workflow", workflowService.findById(id));
		return "contracts/workflow-view";
	}

	@PostMapping("/create")
	public String create(@ModelAttribute ApprovalWorkflow workflow) {
		workflowService.createWorkflow(workflow);
		return "redirect:/contracts/workflows/list";
	}

	@PostMapping("/{id}/advance")
	public String advance(@PathVariable Long id) {
		workflowService.advanceWorkflow(id);
		return "redirect:/contracts/workflows/view/" + id;
	}

	@PostMapping("/{id}/reset")
	public String reset(@PathVariable Long id) {
		workflowService.resetWorkflow(id);
		return "redirect:/contracts/workflows/view/" + id;
	}
}