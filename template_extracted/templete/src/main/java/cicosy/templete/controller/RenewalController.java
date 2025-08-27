package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.RenewalNotification;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractService;
import cicosy.templete.service.RenewalNotificationService;
import cicosy.templete.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/contracts/Renew")
public class RenewalController {

    private final RenewalNotificationService renewalService;
    private final ContractService contractService;
    private final UserService userService;

    @Autowired
    public RenewalController(RenewalNotificationService renewalService,
                             ContractService contractService,
                             UserService userService) {
        this.renewalService = renewalService;
        this.contractService = contractService;
        this.userService = userService;
    }

    // FIXED: Changed mapping to "" so it matches /contracts/Renew
    @GetMapping("")
    public String renewalCalendar(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        List<RenewalNotification> renewals;

        if (currentUser.getRole() == User.Role.MANAGER) {
            renewals = renewalService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            List<Contract> userContracts = contractService.findByOwner(currentUser);
            renewals = userContracts.stream()
                    .flatMap(c -> renewalService.findByContract(c).stream())
                    .toList();
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("renewals", renewals);
        model.addAttribute("upcomingRenewals", renewalService.findUpcomingRenewals(90));
        model.addAttribute("overdueRenewals", renewalService.findOverdueRenewals());

        // ADDED: sentCount for summary card
        long sentCount = renewals.stream().filter(RenewalNotification::isSent).count();
        model.addAttribute("sentCount", sentCount);

        // ADDED: renewalEvents for FullCalendar
        model.addAttribute("renewalEvents", renewalService.toCalendarEvents(renewals));

        return "contracts/Renew";
    }

    @GetMapping("/list/{contractId}")
    public String listRenewalNotifications(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);

        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        List<RenewalNotification> renewals = renewalService.findByContract(contract);

        model.addAttribute("contract", contract);
        model.addAttribute("renewals", renewals);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.MANAGER);

        return "contracts/renewal-list";
    }

    @GetMapping("/create/{contractId}")
    public String showCreateForm(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);

        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("renewal", new RenewalNotification());

        return "contracts/renewal-form";
    }

    @PostMapping("/create/{contractId}")
    public String createRenewalNotification(@PathVariable("contractId") Long contractId,
                                            @Valid @ModelAttribute("renewal") RenewalNotification renewal,
                                            BindingResult bindingResult,
                                            RedirectAttributes redirectAttributes,
                                            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);

        if (contract == null || (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/renewal-form";
        }

        renewal.setContract(contract);
        renewalService.createNotification(renewal);

        redirectAttributes.addFlashAttribute("success", "Renewal notification created successfully");
        return "redirect:/contracts/renewals/list/" + contractId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        RenewalNotification renewal = renewalService.findById(id);

        if (renewal == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }

        Contract contract = renewal.getContract();

        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        model.addAttribute("renewal", renewal);
        model.addAttribute("contract", contract);

        return "contracts/renewal-form";
    }

    @PostMapping("/edit/{id}")
    public String updateRenewalNotification(@PathVariable("id") Long id,
                                            @Valid @ModelAttribute("renewal") RenewalNotification renewal,
                                            BindingResult bindingResult,
                                            RedirectAttributes redirectAttributes,
                                            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        RenewalNotification existingRenewal = renewalService.findById(id);

        if (existingRenewal == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }

        Contract contract = existingRenewal.getContract();

        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("contract", contract);
            return "contracts/renewal-form";
        }

        existingRenewal.setTitle(renewal.getTitle());
        existingRenewal.setDescription(renewal.getDescription());
        existingRenewal.setNotificationDate(renewal.getNotificationDate());
        existingRenewal.setRenewalDate(renewal.getRenewalDate());

        renewalService.updateNotification(existingRenewal);

        redirectAttributes.addFlashAttribute("success", "Renewal notification updated successfully");
        return "redirect:/contracts/renewals/list/" + contract.getId();
    }

    @PostMapping("/send/{id}")
    public String sendRenewalNotification(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        RenewalNotification renewal = renewalService.findById(id);

        if (renewal == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }

        Contract contract = renewal.getContract();

        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        renewalService.sendNotification(id);

        redirectAttributes.addFlashAttribute("success", "Renewal notification sent successfully");
        return "redirect:/contracts/renewals/list/" + contract.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteRenewalNotification(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        RenewalNotification renewal = renewalService.findById(id);

        if (renewal == null) {
            return "redirect:/contracts/dashboard?error=not_found";
        }

        Contract contract = renewal.getContract();
        Long contractId = contract.getId();

        if (currentUser.getRole() != User.Role.MANAGER && !contract.getOwner().equals(currentUser)) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        renewalService.deleteNotification(id);

        redirectAttributes.addFlashAttribute("success", "Renewal notification deleted successfully");
        return "redirect:/contracts/renewals/list/" + contractId;
    }
}
