package cicosy.templete.dto;

import java.time.LocalDate;

public class ContractAmendmentDTO {
    private Long id;
    private Long contractId;
    private String title;
    private String description;
    private String changes;
    private boolean approved;
    private String approvedByUsername;
    private LocalDate approvedDate;
    private LocalDate amendmentDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getApprovedByUsername() { return approvedByUsername; }
    public void setApprovedByUsername(String approvedByUsername) { this.approvedByUsername = approvedByUsername; }

    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }

    public LocalDate getAmendmentDate() { return amendmentDate; }
    public void setAmendmentDate(LocalDate amendmentDate) { this.amendmentDate = amendmentDate; }
}

