package com.ebanca.controller.admin;

import com.ebanca.model.Loan;
import com.ebanca.model.User;
import com.ebanca.service.LoanService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/loans")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoanController {

    private final LoanService loanService;
    private final UserService userService;

    public AdminLoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping
    public String listLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Loan> loans = loanService.findAll(pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        return "admin/loans/list";
    }

    @GetMapping("/{id}")
    public String viewLoan(@PathVariable Long id, Model model) {
        Loan loan = loanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        model.addAttribute("loan", loan);
        return "admin/loans/view";
    }

    @GetMapping("/user/{userId}")
    public String getUserLoans(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        Page<Loan> loans = loanService.findByUser(user, pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        
        return "admin/loans/user-loans";
    }

    @GetMapping("/status/{status}")
    public String getLoansByStatus(
            @PathVariable Loan.LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        Page<Loan> loans = loanService.findByStatus(status, pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("loanStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        
        return "admin/loans/status-loans";
    }

    @GetMapping("/date-range")
    public String getLoansByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        Page<Loan> loans = loanService.findByDateRange(startDate, endDate, pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        
        return "admin/loans/date-range-loans";
    }

    @GetMapping("/amount-range")
    public String getLoansByAmountRange(
            @RequestParam double minAmount,
            @RequestParam double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        Page<Loan> loans = loanService.findByAmountRange(
            BigDecimal.valueOf(minAmount), 
            BigDecimal.valueOf(maxAmount), 
            pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        
        return "admin/loans/amount-range-loans";
    }

    @GetMapping("/search")
    public String searchLoans(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        Page<Loan> loans = loanService.searchLoans(query, pageRequest);
        
        model.addAttribute("loans", loans);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loans.getTotalPages());
        model.addAttribute("totalItems", loans.getTotalElements());
        model.addAttribute("query", query);
        
        return "admin/loans/list";
    }

    @PostMapping("/{id}/approve")
    public String approveLoan(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.approveLoan(id);
            redirectAttributes.addFlashAttribute("success", "Loan approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving loan: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectLoan(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.rejectLoan(id, reason);
            redirectAttributes.addFlashAttribute("success", "Loan rejected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting loan: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }

    @PostMapping("/{id}/process-payment")
    public String processLoanPayment(
            @PathVariable Long id,
            @RequestParam double amount,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.processLoanPayment(id, BigDecimal.valueOf(amount));
            redirectAttributes.addFlashAttribute("success", "Loan payment processed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing loan payment: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }

    @PostMapping("/{id}/mark-paid")
    public String markLoanAsPaid(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.markLoanAsPaid(id);
            redirectAttributes.addFlashAttribute("success", "Loan marked as paid successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error marking loan as paid: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }

    @PostMapping("/{id}/mark-defaulted")
    public String markLoanAsDefaulted(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.markLoanAsDefaulted(id);
            redirectAttributes.addFlashAttribute("success", "Loan marked as defaulted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error marking loan as defaulted: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + id;
    }

    @GetMapping("/export")
    public String exportLoans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Loan.LoanStatus status,
            @RequestParam(required = false) Long userId,
            Model model) {
        
        List<Loan> loans = loanService.exportLoans(startDate, endDate, status, userId);
        model.addAttribute("loans", loans);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("userId", userId);
        
        return "admin/loans/export";
    }
} 