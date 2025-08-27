package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractMilestone;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractMilestoneService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts/milestones")
public class MilestoneController {

    private final ContractMilestoneService milestoneService;
    private final ContractService contractService;
    private final UserService userService;

    @Autowired
    public MilestoneController(ContractMilestoneService milestoneService,
                              ContractService contractService,
                              UserService userService) {
        this.milestoneService = milestoneService;
        this.contractService = contractService;
        this.userService = userService;
    }

    @GetMapping("/list/{contractId}")
    public String listMilestones(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to view this contract's milestones
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        List<ContractMilestone> milestones = milestoneService.findByContract(contract);
        
        model.addAttribute("contract", contract);
        model.addAttribute("milestones", milestones);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.MANAGER);
        model.addAttribute("completionPercentage", milestoneService.calculateCompletionPercentage(contract));
        
        return "contracts/milestone-tracking";
    }

    @GetMapping("/create/{contractId}")
    public String showCreateForm(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add milestones to this contract
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        model.addAttribute("contract", contract);
        model.addAttribute("milestone", new ContractMilestone());
        
        return "contracts/milestone-form";
    }

    @PostMapping("/create/{contractId}")
    public String createMilestone(@PathVariable("contractId") Long contractId,
                                @Valid @ModelAttribute("milestone") ContractMilestone milestone,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add milestones to this contract
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/milestone-form";
        }
        
        milestone.setContract(contract);
        milestoneService.createMilestone(milestone);
        
        redirectAttributes.addFlashAttribute("success", "Milestone created successfully");
        return "redirect:/contracts/milestones/list/" + contractId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractMilestone milestone = milestoneService.findById(id);
        
        if (milestone == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = milestone.getContract();
        
        // Check if user has permission to edit this milestone
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        model.addAttribute("milestone", milestone);
        model.addAttribute("contract", contract);
        
        return "contracts/milestone-form";
    }

    @PostMapping("/edit/{id}")
    public String updateMilestone(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("milestone") ContractMilestone milestone,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractMilestone existingMilestone = milestoneService.findById(id);
        
        if (existingMilestone == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = existingMilestone.getContract();
        
        // Check if user has permission to edit this milestone
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/milestone-form";
        }
        
        // Update existing milestone with new values
        existingMilestone.setTitle(milestone.getTitle());
        existingMilestone.setDescription(milestone.getDescription());
        existingMilestone.setDueDate(milestone.getDueDate());
        existingMilestone.setPaymentAmount(milestone.getPaymentAmount());
        
        milestoneService.updateMilestone(existingMilestone);
        
        redirectAttributes.addFlashAttribute("success", "Milestone updated successfully");
        return "redirect:/contracts/milestones/list/" + contract.getId();
    }

    @PostMapping("/complete/{id}")
    public String completeMilestone(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractMilestone milestone = milestoneService.findById(id);
        
        if (milestone == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = milestone.getContract();
        
        // Check if user has permission to complete this milestone
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        milestoneService.completeMilestone(id);
        
        redirectAttributes.addFlashAttribute("success", "Milestone marked as completed");
        return "redirect:/contracts/milestones/list/" + contract.getId();
    }

    @PostMapping("/uncomplete/{id}")
    public String uncompleteMilestone(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractMilestone milestone = milestoneService.findById(id);
        
        if (milestone == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = milestone.getContract();
        
        // Check if user has permission to uncomplete this milestone
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        milestoneService.uncompleteMilestone(id);
        
        redirectAttributes.addFlashAttribute("success", "Milestone marked as incomplete");
        return "redirect:/contracts/milestones/list/" + contract.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteMilestone(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractMilestone milestone = milestoneService.findById(id);
        
        if (milestone == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = milestone.getContract();
        Long contractId = contract.getId();
        
        // Check if user has permission to delete this milestone
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        milestoneService.deleteMilestone(id);
        
        redirectAttributes.addFlashAttribute("success", "Milestone deleted successfully");
        return "redirect:/contracts/milestones/list/" + contractId;
    }

    @GetMapping("/calendar")
    public String milestoneCalendar(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        
        List<ContractMilestone> milestones;
        
        // Different views based on user role
        if (currentUser.getRole() == User.Role.MANAGER) {
            // Admin sees all milestones
            milestones = milestoneService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            // Regular users see only their contract milestones
            List<Contract> userContracts = contractService.findByOwner(currentUser);
            milestones = userContracts.stream()
                .flatMap(c -> milestoneService.findByContract(c).stream())
                .toList();
            model.addAttribute("isAdmin", false);
        }
        
        model.addAttribute("milestones", milestones);
        model.addAttribute("overdueMilestones", milestoneService.findOverdueMilestones());
        model.addAttribute("upcomingMilestones", milestoneService.findUpcomingMilestones(30)); // Next 30 days
        
        return "contracts/milestone-calendar";
    }
}