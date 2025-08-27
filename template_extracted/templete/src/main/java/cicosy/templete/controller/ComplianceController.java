package cicosy.templete.controller;

import cicosy.templete.domain.ComplianceAlert;
import cicosy.templete.domain.Contract;
import cicosy.templete.domain.User;
import cicosy.templete.service.ComplianceAlertService;
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
@RequestMapping("/contracts/compliance")
public class ComplianceController {

    private final ComplianceAlertService complianceService;
    private final ContractService contractService;
    private final UserService userService;

    @Autowired
    public ComplianceController(ComplianceAlertService complianceService,
                              ContractService contractService,
                              UserService userService) {
        this.complianceService = complianceService;
        this.contractService = contractService;
        this.userService = userService;
    }

    @GetMapping("/monitor")
    public String complianceMonitor(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        
        List<ComplianceAlert> alerts;
        
        // Different views based on user role
        if (currentUser.getRole() == User.Role.MANAGER) {
            // Admin sees all compliance alerts
            alerts = complianceService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            // Regular users see only their contract alerts
            List<Contract> userContracts = contractService.findByOwner(currentUser);
            alerts = userContracts.stream()
                .flatMap(c -> complianceService.findByContract(c).stream())
                .toList();
            model.addAttribute("isAdmin", false);
        }
        
        model.addAttribute("alerts", alerts);
        model.addAttribute("overdueAlerts", complianceService.findOverdueAlerts());
        model.addAttribute("upcomingAlerts", complianceService.findUpcomingAlerts(30)); // Next 30 days
        model.addAttribute("criticalAlerts", complianceService.findBySeverity(ComplianceAlert.AlertSeverity.CRITICAL));
        
        return "contracts/compliance-monitor";
    }

    @GetMapping("/list/{contractId}")
    public String listComplianceAlerts(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to view this contract's compliance alerts
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        List<ComplianceAlert> alerts = complianceService.findByContract(contract);
        
        model.addAttribute("contract", contract);
        model.addAttribute("alerts", alerts);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.MANAGER);
        
        return "contracts/compliance-list";
    }

    @GetMapping("/create/{contractId}")
    public String showCreateForm(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add compliance alerts to this contract
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        model.addAttribute("contract", contract);
        model.addAttribute("alert", new ComplianceAlert());
        model.addAttribute("severities", ComplianceAlert.AlertSeverity.values());
        
        return "contracts/compliance-form";
    }

    @PostMapping("/create/{contractId}")
    public String createComplianceAlert(@PathVariable("contractId") Long contractId,
                                     @Valid @ModelAttribute("alert") ComplianceAlert alert,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to add compliance alerts to this contract
        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            model.addAttribute("severities", ComplianceAlert.AlertSeverity.values());
            return "contracts/compliance-form";
        }
        
        alert.setContract(contract);
        complianceService.createAlert(alert);
        
        redirectAttributes.addFlashAttribute("success", "Compliance alert created successfully");
        return "redirect:/contracts/compliance/list/" + contractId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ComplianceAlert alert = complianceService.findById(id);
        
        if (alert == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = alert.getContract();
        
        // Check if user has permission to edit this compliance alert
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        model.addAttribute("alert", alert);
        model.addAttribute("contract", contract);
        model.addAttribute("severities", ComplianceAlert.AlertSeverity.values());
        
        return "contracts/compliance-form";
    }

    @PostMapping("/edit/{id}")
    public String updateComplianceAlert(@PathVariable("id") Long id,
                                     @Valid @ModelAttribute("alert") ComplianceAlert alert,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ComplianceAlert existingAlert = complianceService.findById(id);
        
        if (existingAlert == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = existingAlert.getContract();
        
        // Check if user has permission to edit this compliance alert
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            model.addAttribute("severities", ComplianceAlert.AlertSeverity.values());
            return "contracts/compliance-form";
        }
        
        // Update existing alert with new values
        existingAlert.setTitle(alert.getTitle());
        existingAlert.setDescription(alert.getDescription());
        existingAlert.setDueDate(alert.getDueDate());
        existingAlert.setSeverity(alert.getSeverity());
        
        complianceService.updateAlert(existingAlert);
        
        redirectAttributes.addFlashAttribute("success", "Compliance alert updated successfully");
        return "redirect:/contracts/compliance/list/" + contract.getId();
    }

    @PostMapping("/resolve/{id}")
    public String resolveComplianceAlert(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ComplianceAlert alert = complianceService.findById(id);
        
        if (alert == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = alert.getContract();
        
        // Check if user has permission to resolve this compliance alert
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        complianceService.resolveAlert(id, currentUser);
        
        redirectAttributes.addFlashAttribute("success", "Compliance alert marked as resolved");
        return "redirect:/contracts/compliance/list/" + contract.getId();
    }

    @PostMapping("/unresolve/{id}")
    public String unresolveComplianceAlert(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ComplianceAlert alert = complianceService.findById(id);
        
        if (alert == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = alert.getContract();
        
        // Check if user has permission to unresolve this compliance alert
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        complianceService.unresolveAlert(id);
        
        redirectAttributes.addFlashAttribute("success", "Compliance alert marked as unresolved");
        return "redirect:/contracts/compliance/list/" + contract.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteComplianceAlert(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ComplianceAlert alert = complianceService.findById(id);
        
        if (alert == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }
        
        Contract contract = alert.getContract();
        Long contractId = contract.getId();
        
        // Check if user has permission to delete this compliance alert
        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        complianceService.deleteAlert(id);
        
        redirectAttributes.addFlashAttribute("success", "Compliance alert deleted successfully");
        return "redirect:/contracts/compliance/list/" + contractId;
    }
}