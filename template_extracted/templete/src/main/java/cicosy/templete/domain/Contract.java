package cicosy.templete.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Contract title is required")
    private String title;

    @Column(unique = true)
    private String referenceNumber;

    @NotNull(message = "Contract type is required")
    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    // Fixed: Removed clientName field since it's not in your HTML form
    // If needed, add it back with proper form field

    @NotBlank(message = "Counterparty name is required")
    private String counterpartyName;

    @Email(message = "Counterparty email should be valid")
    private String counterpartyEmail;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    // Fixed: Changed field name from contractValue to value to match Thymeleaf binding
    @Column(name = "contract_value", precision = 15, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "USD";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractClause> clauses = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractMilestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractAmendment> amendments = new ArrayList<>();

    private boolean active = true;

    private LocalDate signedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContractTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_workflow_id")
    private ApprovalWorkflow approvalWorkflow;

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContractPerformance performance;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RenewalNotification> renewalNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplianceAlert> complianceAlerts = new ArrayList<>();

    private LocalDate createdDate;
    private LocalDate lastModifiedDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        lastModifiedDate = LocalDate.now();
        if (referenceNumber == null || referenceNumber.isEmpty()) {
            referenceNumber = generateReferenceNumber();
        }
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDate.now();
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
    }

    private String generateReferenceNumber() {
        return "CONTRACT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    // Constructors
    public Contract() {
        this.value = BigDecimal.ZERO;
        this.currency = "USD";
        this.active = true;
        this.status = Status.DRAFT;
    }

    public Contract(String title, String referenceNumber, ContractType contractType, User owner,
                    String counterpartyName, LocalDate startDate,
                    LocalDate endDate, BigDecimal value) {
        this();
        this.title = title;
        this.referenceNumber = referenceNumber;
        this.contractType = contractType;
        this.owner = owner;
        this.counterpartyName = counterpartyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.value = value != null ? value : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public String getCounterpartyEmail() {
        return counterpartyEmail;
    }

    public void setCounterpartyEmail(String counterpartyEmail) {
        this.counterpartyEmail = counterpartyEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Fixed: Direct getter/setter for value field to match Thymeleaf binding
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value != null ? value : BigDecimal.ZERO;
    }

    // Convenience method for double conversion (if needed)
    public double getValueAsDouble() {
        return value != null ? value.doubleValue() : 0.0;
    }

    public void setValueAsDouble(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    // Keep contractValue methods for backward compatibility
    public BigDecimal getContractValue() {
        return value;
    }

    public void setContractValue(BigDecimal contractValue) {
        this.value = contractValue != null ? contractValue : BigDecimal.ZERO;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency : "USD";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ContractClause> getClauses() {
        return clauses;
    }

    public void setClauses(List<ContractClause> clauses) {
        this.clauses = clauses;
    }

    public void addClause(ContractClause clause) {
        clauses.add(clause);
        clause.setContract(this);
    }

    public void removeClause(ContractClause clause) {
        clauses.remove(clause);
        clause.setContract(null);
    }

    public List<ContractMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<ContractMilestone> milestones) {
        this.milestones = milestones;
    }

    public void addMilestone(ContractMilestone milestone) {
        milestones.add(milestone);
        milestone.setContract(this);
    }

    public void removeMilestone(ContractMilestone milestone) {
        milestones.remove(milestone);
        milestone.setContract(null);
    }

    public List<ContractAmendment> getAmendments() {
        return amendments;
    }

    public void setAmendments(List<ContractAmendment> amendments) {
        this.amendments = amendments;
    }

    public void addAmendment(ContractAmendment amendment) {
        amendments.add(amendment);
        amendment.setContract(this);
    }

    public void removeAmendment(ContractAmendment amendment) {
        amendments.remove(amendment);
        amendment.setContract(null);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }

    public ContractTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ContractTemplate template) {
        this.template = template;
    }

    public ApprovalWorkflow getApprovalWorkflow() {
        return approvalWorkflow;
    }

    public void setApprovalWorkflow(ApprovalWorkflow approvalWorkflow) {
        this.approvalWorkflow = approvalWorkflow;
    }

    public ContractPerformance getPerformance() {
        return performance;
    }

    public void setPerformance(ContractPerformance performance) {
        this.performance = performance;
        if (performance != null) {
            performance.setContract(this);
        }
    }

    public List<RenewalNotification> getRenewalNotifications() {
        return renewalNotifications;
    }

    public void setRenewalNotifications(List<RenewalNotification> renewalNotifications) {
        this.renewalNotifications = renewalNotifications;
    }

    public void addRenewalNotification(RenewalNotification notification) {
        renewalNotifications.add(notification);
        notification.setContract(this);
    }

    public void removeRenewalNotification(RenewalNotification notification) {
        renewalNotifications.remove(notification);
        notification.setContract(null);
    }

    public List<ComplianceAlert> getComplianceAlerts() {
        return complianceAlerts;
    }

    public void setComplianceAlerts(List<ComplianceAlert> complianceAlerts) {
        this.complianceAlerts = complianceAlerts;
    }

    public void addComplianceAlert(ComplianceAlert alert) {
        complianceAlerts.add(alert);
        alert.setContract(this);
    }

    public void removeComplianceAlert(ComplianceAlert alert) {
        complianceAlerts.remove(alert);
        alert.setContract(null);
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Business logic methods
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean isDraft() {
        return Status.DRAFT.equals(status);
    }

    public boolean isPendingApproval() {
        return Status.PENDING_APPROVAL.equals(status);
    }

    public boolean isActiveStatus() {
        return Status.ACTIVE.equals(status);
    }

    public long getDaysRemaining() {
        if (endDate == null || endDate.isBefore(LocalDate.now())) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public enum Status {
        DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, TERMINATED, RENEWED
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", contractType=" + contractType +
                ", status=" + status +
                ", counterpartyName='" + counterpartyName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return id != null && id.equals(contract.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}