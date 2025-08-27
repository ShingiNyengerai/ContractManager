package cicosy.templete.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContractMilestoneDTO {
    private Long id;
    private Long contractId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private BigDecimal paymentAmount;
    private boolean completed;
    private LocalDate completedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
}

