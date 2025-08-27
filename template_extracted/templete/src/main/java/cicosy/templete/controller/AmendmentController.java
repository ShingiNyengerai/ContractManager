package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractAmendment;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractAmendmentService;
import cicosy.templete.service.ContractService;
import cicosy.templete.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts/amendments")
public class AmendmentController {

    private final ContractAmendmentService amendmentService;
    private final ContractService contractService;
    private final UserService userService;

    @Autowired
    public AmendmentController(ContractAmendmentService amendmentService,
                              ContractService contractService,
                              UserService userService) {
        this.amendmentService = amendmentService;
        this.contractService = contractService;
        this.userService = userService;
    }

    @GetMapping("/list/{contractId}")
    public String listAmendments(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to view this contract's amendments
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        List<ContractAmendment> amendments = amendmentService.findByContract(contract);
        
        model.addAttribute("contract", contract);
        model.addAttribute("amendments", amendments);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
        
        return "contracts/amendment-management";
    }

    @GetMapping("/create/{contractId}")
    public String showCreateForm(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add amendments to this contract
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        model.addAttribute("contract", contract);
        model.addAttribute("amendment", new ContractAmendment());
        
        return "contracts/amendment-form";
    }

    @PostMapping("/create/{contractId}")
    public String createAmendment(@PathVariable("contractId") Long contractId,
                                @Valid @ModelAttribute("amendment") ContractAmendment amendment,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add amendments to this contract
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/amendment-form";
        }
        
        amendment.setContract(contract);
        amendment.setAmendmentDate(LocalDate.now());
        amendmentService.createAmendment(amendment);
        
        redirectAttributes.addFlashAttribute("success", "Amendment created successfully");
        return "redirect:/contracts/amendments/list/" + contractId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractAmendment amendment = amendmentService.findById(id);

        if (amendment == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }

        Contract contract = amendment.getContract();

        // Check if user has permission to edit this amendment
        if (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        // If amendment is already approved, it cannot be edited
        if (amendment.isApproved()) {
            redirectAttributes.addFlashAttribute("error", "Approved amendments cannot be edited");
            return "redirect:/contracts/amendments/list/" + contract.getId();
        }

        model.addAttribute("amendment", amendment);
        model.addAttribute("contract", contract);

        return "contracts/amendment-form";
    }


    @PostMapping("/edit/{id}")
    public String updateAmendment(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("amendment") ContractAmendment amendment,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractAmendment existingAmendment = amendmentService.findById(id);
        
        if (existingAmendment == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = existingAmendment.getContract();
        
        // Check if user has permission to edit this amendment
        if (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        // If amendment is already approved, it cannot be edited
        if (existingAmendment.isApproved()) {
            redirectAttributes.addFlashAttribute("error", "Approved amendments cannot be edited");
            return "redirect:/contracts/amendments/list/" + contract.getId();
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/amendment-form";
        }
        
        // Update existing amendment with new values
        existingAmendment.setTitle(amendment.getTitle());
        existingAmendment.setDescription(amendment.getDescription());
        existingAmendment.setChanges(amendment.getChanges());
        
        amendmentService.updateAmendment(existingAmendment);
        
        redirectAttributes.addFlashAttribute("success", "Amendment updated successfully");
        return "redirect:/contracts/amendments/list/" + contract.getId();
    }

    @PostMapping("/approve/{id}")
    public String approveAmendment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractAmendment amendment = amendmentService.findById(id);
        
        if (amendment == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = amendment.getContract();

        // Enforce current workflow approver
        if (contract.getApprovalWorkflow() == null) {
            return "redirect:/contracts/amendments/list/" + contract.getId() + "?error=no_workflow";
        }

        var workflow = contract.getApprovalWorkflow();
        var currentApprover = workflow.getApprovers() != null && workflow.getApprovers().size() > workflow.getCurrentStep()
            ? workflow.getApprovers().get(workflow.getCurrentStep()) : null;

        if (currentApprover == null || !currentApprover.getId().equals(currentUser.getId())) {
            return "redirect:/contracts/amendments/list/" + contract.getId() + "?error=not_current_approver";
        }

        // Advance workflow step; only mark approved when workflow completes
        int next = (workflow.getCurrentStep() == null ? 0 : workflow.getCurrentStep()) + 1;
        if (workflow.getNumberOfSteps() != null && next >= workflow.getNumberOfSteps()) {
            workflow.setCurrentStep(workflow.getNumberOfSteps());
            workflow.setCompleted(true);
            amendment.setApproved(true);
            amendment.setApprovedBy(currentUser);
        } else {
            workflow.setCurrentStep(next);
        }
        contract.setApprovalWorkflow(workflow);
        contractService.save(contract);
        amendmentService.updateAmendment(amendment);

        redirectAttributes.addFlashAttribute("success", "Approval recorded");
        return "redirect:/contracts/amendments/list/" + contract.getId();
    }

    @PostMapping("/reject/{id}")
    public String rejectAmendment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractAmendment amendment = amendmentService.findById(id);
        
        if (amendment == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = amendment.getContract();

        // Enforce current workflow approver on rejection, reset workflow
        if (contract.getApprovalWorkflow() == null) {
            return "redirect:/contracts/amendments/list/" + contract.getId() + "?error=no_workflow";
        }
        var workflow = contract.getApprovalWorkflow();
        var currentApprover = workflow.getApprovers() != null && workflow.getApprovers().size() > workflow.getCurrentStep()
            ? workflow.getApprovers().get(workflow.getCurrentStep()) : null;
        if (currentApprover == null || !currentApprover.getId().equals(currentUser.getId())) {
            return "redirect:/contracts/amendments/list/" + contract.getId() + "?error=not_current_approver";
        }

        amendment.setApproved(false);
        amendment.setApprovedBy(null);
        workflow.setCurrentStep(0);
        workflow.setCompleted(false);
        contract.setApprovalWorkflow(workflow);
        contractService.save(contract);
        amendmentService.updateAmendment(amendment);

        redirectAttributes.addFlashAttribute("success", "Rejection recorded and workflow reset");
        return "redirect:/contracts/amendments/list/" + contract.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteAmendment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractAmendment amendment = amendmentService.findById(id);
        
        if (amendment == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = amendment.getContract();
        Long contractId = contract.getId();
        
        // Check if user has permission to delete this amendment
        if (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        // If amendment is already approved, it cannot be deleted
        if (amendment.isApproved()) {
            redirectAttributes.addFlashAttribute("error", "Approved amendments cannot be deleted");
            return "redirect:/contracts/amendments/list/" + contractId;
        }
        
        amendmentService.deleteAmendment(id);
        
        redirectAttributes.addFlashAttribute("success", "Amendment deleted successfully");
        return "redirect:/contracts/amendments/list/" + contractId;
    }
}