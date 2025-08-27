package cicosy.templete.dto;

import java.time.LocalDate;

public class RenewalNotificationDTO {
    private Long id;
    private Long contractId;
    private String title;
    private String description;
    private LocalDate notificationDate;
    private LocalDate renewalDate;
    private boolean sent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getNotificationDate() { return notificationDate; }
    public void setNotificationDate(LocalDate notificationDate) { this.notificationDate = notificationDate; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
}

