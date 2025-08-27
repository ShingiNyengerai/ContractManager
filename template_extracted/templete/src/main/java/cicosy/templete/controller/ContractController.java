package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractService;
import cicosy.templete.service.ContractTemplateService;
import cicosy.templete.service.ContractTypeService;
import cicosy.templete.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractTemplateService contractTemplateService;
    private final ContractTypeService contractTypeService;
    private final UserService userService;

    @Autowired
    public ContractController(ContractService contractService,
                              ContractTemplateService contractTemplateService,
                              ContractTypeService contractTypeService,
                              UserService userService) {
        this.contractService = contractService;
        this.contractTemplateService = contractTemplateService;
        this.contractTypeService = contractTypeService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String contractDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        List<Contract> contracts;
        List<ContractTemplate> templates;

        // Different views based on user role
        if (currentUser.getRole() == User.Role.CONTRACT_MANAGER) {
            // Admin sees all contracts and templates
            contracts = contractService.findAll();
            templates = contractTemplateService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            // Regular users see only their contracts
            contracts = contractService.findByOwner(currentUser);
            templates = contractTemplateService.findByActive(true);
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("contracts", contracts);
        model.addAttribute("templates", templates);
        model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
        model.addAttribute("contractCount", contracts.size());
        model.addAttribute("activeContractCount", contracts.stream().filter(Contract::isActive).count());

        return "contracts/dashboard";
    }

    // FIXED: Changed mapping from /Create to /create for consistency
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        Contract contract = new Contract();
        contract.setOwner(currentUser);
        contract.setStartDate(LocalDate.now());
        contract.setActive(true);
        contract.setStatus(Contract.Status.DRAFT);
        // Set default currency and contract value
        contract.setCurrency("USD");
        contract.setContractValue(BigDecimal.ZERO);

        model.addAttribute("contract", contract);
        model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
        model.addAttribute("templates", contractTemplateService.findByActive(true));
        model.addAttribute("statusValues", Contract.Status.values());

        // FIXED: Return consistent view path
        return "/create";
    }

    // FIXED: Changed mapping from /Create to /create
    @PostMapping("/create")
    public String createContract(@Valid @ModelAttribute Contract contract,
                                 BindingResult result,
                                 @RequestParam(value = "action", required = false) String action,
                                 @RequestParam(value = "templateId", required = false) Long templateId,
                                 Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        if (result.hasErrors()) {
            model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
            model.addAttribute("templates", contractTemplateService.findByActive(true));
            model.addAttribute("statusValues", Contract.Status.values());
            // FIXED: Return consistent view path
            return "/create";
        }

        // Set the current user as owner
        contract.setOwner(currentUser);

        // Set template if provided
        if (templateId != null && templateId > 0) {
            ContractTemplate template = contractTemplateService.findById(templateId);
            if (template != null) {
                contract.setTemplate(template);
            }
        }

        // Handle different save actions using enum
        if ("save_draft".equals(action)) {
            contract.setStatus(Contract.Status.DRAFT);
        } else {
            contract.setStatus(Contract.Status.PENDING_APPROVAL);
        }

        // Ensure contract value is not null
        if (contract.getContractValue() == null) {
            contract.setContractValue(BigDecimal.ZERO);
        }

        // Ensure currency is set
        if (contract.getCurrency() == null || contract.getCurrency().isEmpty()) {
            contract.setCurrency("USD");
        }

        contractService.save(contract);
        return "redirect:/contracts/list?created=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(id);

        // Check if user has permission to edit this contract
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
        model.addAttribute("templates", contractTemplateService.findByActive(true));
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
        model.addAttribute("statusValues", Contract.Status.values());

        return "/create";
    }

    @PostMapping("/update/{id}")
    public String updateContract(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute Contract contract,
                                 BindingResult result,
                                 @RequestParam(value = "action", required = false) String action,
                                 @RequestParam(value = "templateId", required = false) Long templateId,
                                 Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract existingContract = contractService.findById(id);

        // Check if user has permission to edit this contract
        if (existingContract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !existingContract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        if (result.hasErrors()) {
            model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
            model.addAttribute("templates", contractTemplateService.findByActive(true));
            model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
            model.addAttribute("statusValues", Contract.Status.values());
            // FIXED: Return consistent view path
            return "/create";
        }

        // Preserve the original owner and ID
        contract.setOwner(existingContract.getOwner());
        contract.setId(id);

        // Set template if provided
        if (templateId != null && templateId > 0) {
            ContractTemplate template = contractTemplateService.findById(templateId);
            if (template != null) {
                contract.setTemplate(template);
            }
        }

        // Handle different save actions using enum
        if ("save_draft".equals(action)) {
            contract.setStatus(Contract.Status.DRAFT);
        } else if (existingContract.getStatus() == Contract.Status.DRAFT) {
            contract.setStatus(Contract.Status.PENDING_APPROVAL);
        } else {
            contract.setStatus(existingContract.getStatus());
        }

        // Ensure contract value is not null
        if (contract.getContractValue() == null) {
            contract.setContractValue(BigDecimal.ZERO);
        }

        // Ensure currency is set
        if (contract.getCurrency() == null || contract.getCurrency().isEmpty()) {
            contract.setCurrency("USD");
        }

        contractService.save(contract);
        return "redirect:/contracts/view/" + id + "?updated=true";
    }

    @GetMapping("/template-data/{id}")
    @ResponseBody
    public ContractTemplate getTemplateData(@PathVariable("id") Long id) {
        return contractTemplateService.findById(id);
    }

    @GetMapping("/list")
    public String listContracts(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        List<Contract> contracts;

        if (currentUser.getRole() == User.Role.CONTRACT_MANAGER) {
            contracts = contractService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            contracts = contractService.findByOwner(currentUser);
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("contracts", contracts);
        return "contracts/list";
    }

    @GetMapping("/templates")
    public String listTemplates(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        List<ContractTemplate> templates;

        if (currentUser.getRole() == User.Role.CONTRACT_MANAGER) {
            templates = contractTemplateService.findAll();
            model.addAttribute("isAdmin", true);
        } else {
            templates = contractTemplateService.findByActive(true);
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("templates", templates);
        model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
        return "contracts/templates";
    }

    @GetMapping("/view/{id}")
    public String viewContract(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(id);

        // Check if user has permission to view this contract
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
        return "contracts/view";
    }

    @GetMapping("/template/{id}")
    public String viewTemplate(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        ContractTemplate template = contractTemplateService.findById(id);

        // Check if user has permission to view this template
        if (template == null || (!template.isActive() && currentUser.getRole() != User.Role.CONTRACT_MANAGER)) {
            return "redirect:/contracts/templates?error=unauthorized";
        }

        model.addAttribute("template", template);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
        return "contracts/template-view";
    }

    // Admin-only operations
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('CONTRACT_MANAGER')")
    public String adminStats(Model model) {
        model.addAttribute("contractsByType", contractTypeService.countContractsByType());
        model.addAttribute("templatesByType", contractTypeService.countTemplatesByType());
        model.addAttribute("mostCommonType", contractTypeService.getMostCommonContractType());
        model.addAttribute("contractTypes", contractTypeService.getAllContractTypes());
        return "contracts/admin/stats";
    }
}