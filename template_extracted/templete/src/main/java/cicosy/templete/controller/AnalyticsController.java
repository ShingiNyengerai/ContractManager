package cicosy.templete.controller;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.User;
import cicosy.templete.service.ContractPerformanceService;
import cicosy.templete.service.ContractService;
import cicosy.templete.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contracts/analytics")
public class AnalyticsController {

    private final ContractService contractService;
    private final ContractPerformanceService performanceService;
    private final UserService userService;

    @Autowired
    public AnalyticsController(ContractService contractService,
                             ContractPerformanceService performanceService,
                             UserService userService) {
        this.contractService = contractService;
        this.performanceService = performanceService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String analyticsDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        
        List<Contract> contracts;
        Map<String, Object> analyticsData = new HashMap<>();
        
        // Different views based on user role
        if (currentUser.getRole() == User.Role.CONTRACT_MANAGER) {
            // Admin sees analytics for all contracts
            contracts = contractService.findAll();
            model.addAttribute("isAdmin", true);
            
            // Admin-specific analytics
            analyticsData.put("totalContractValue", contractService.calculateTotalContractValue());
            analyticsData.put("averageContractValue", contractService.calculateAverageContractValue());
            analyticsData.put("contractsByType", contractService.countContractsByType());
            analyticsData.put("contractsByStatus", contractService.countContractsByStatus());
            analyticsData.put("contractsByMonth", contractService.countContractsByMonth());
            analyticsData.put("contractsByOwner", contractService.countContractsByOwner());
        } else {
            // Regular users see analytics for their contracts only
            contracts = contractService.findByOwner(currentUser);
            model.addAttribute("isAdmin", false);
            
            // User-specific analytics
            analyticsData.put("totalContractValue", contractService.calculateTotalContractValueForUser(currentUser));
            analyticsData.put("averageContractValue", contractService.calculateAverageContractValueForUser(currentUser));
            analyticsData.put("contractsByType", contractService.countContractsByTypeForUser(currentUser));
            analyticsData.put("contractsByStatus", contractService.countContractsByStatusForUser(currentUser));
            analyticsData.put("contractsByMonth", contractService.countContractsByMonthForUser(currentUser));
        }
        
        // Common analytics for both admin and regular users
        analyticsData.put("contractsCount", contracts.size());
        analyticsData.put("activeContractsCount", contracts.stream()
                .filter(Contract::isActive)
                .count());
        analyticsData.put("expiringContracts", contractService.findExpiringContracts(30)); // Next 30 days
        
        // Performance analytics
        analyticsData.put("averageMilestoneCompletion", performanceService.calculateAverageMilestoneCompletionPercentage(contracts));
        analyticsData.put("contractsWithDelayedMilestones", performanceService.findContractsWithDelayedMilestones(contracts));
        analyticsData.put("contractsWithCostVariance", performanceService.findContractsWithCostVariance(contracts, 10.0)); // 10% variance
        
        model.addAttribute("analyticsData", analyticsData);
        model.addAttribute("contracts", contracts);
        
        return "contracts/contract-analytics";
    }

    @GetMapping("/performance/{contractId}")
    public String contractPerformance(@PathVariable("contractId") Long contractId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        Contract contract = contractService.findById(contractId);
        
        // Check if user has permission to view this contract's performance
        if (contract == null || (currentUser.getRole() != User.Role.CONTRACT_MANAGER && !contract.getOwner().equals(currentUser))) {
            return "redirect:/contracts/list?error=unauthorized";
        }
        
        Map<String, Object> performanceData = new HashMap<>();
        
        // Contract performance metrics
        performanceData.put("milestoneCompletionPercentage", performanceService.calculateMilestoneCompletionPercentage(contract.getId()));
        performanceData.put("costVariance", performanceService.calculateCostVariance(contract.getId()));
        performanceData.put("delayedMilestones", performanceService.findDelayedMilestones(contract));
        performanceData.put("completedMilestones", performanceService.findCompletedMilestones(contract));
        performanceData.put("upcomingMilestones", performanceService.findUpcomingMilestones(contract));
        performanceData.put("performanceReport", performanceService.generatePerformanceReport(contract.getId()));
        
        model.addAttribute("contract", contract);
        model.addAttribute("performanceData", performanceData);
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.CONTRACT_MANAGER);
        
        return "contracts/contract-performance";
    }

    @GetMapping("/reports")
    public String contractReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) String contractStatus,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        
        // Default date range if not provided (last 12 months)
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<Contract> filteredContracts;
        
        // Different views based on user role
        if (currentUser.getRole() == User.Role.CONTRACT_MANAGER) {
            // Admin sees reports for all contracts with filters
            filteredContracts = contractService.findByFilters(startDate, endDate, contractType, contractStatus, null);
            model.addAttribute("isAdmin", true);
        } else {
            // Regular users see reports for their contracts only with filters
            filteredContracts = contractService.findByFilters(startDate, endDate, contractType, contractStatus, currentUser);
            model.addAttribute("isAdmin", false);
        }
        
        // Generate report data
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("contractsCount", filteredContracts.size());
        reportData.put("totalValue", filteredContracts.stream()
                .map(Contract::getContractValue)
                .filter(bd -> bd != null)
                .mapToDouble(bd -> bd.doubleValue())
                .sum());
        reportData.put("contractsByType", contractService.countContractsByTypeInList(filteredContracts));
        reportData.put("contractsByStatus", contractService.countContractsByStatusInList(filteredContracts));
        reportData.put("contractsByMonth", contractService.countContractsByMonthInRange(startDate, endDate, filteredContracts));
        reportData.put("averageMilestoneCompletion", performanceService.calculateAverageMilestoneCompletionPercentage(filteredContracts));
        
        model.addAttribute("contracts", filteredContracts);
        model.addAttribute("reportData", reportData);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("contractType", contractType);
        model.addAttribute("contractStatus", contractStatus);
        model.addAttribute("contractTypes", contractService.getAllContractTypes());
        // TODO: Add contract statuses when Contract.Status enum is implemented
        // model.addAttribute("contractStatuses", Contract.Status.values());

        return "contracts/contract-reports";
    }
}