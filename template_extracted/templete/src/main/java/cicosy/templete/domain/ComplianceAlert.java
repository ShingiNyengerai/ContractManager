package cicosy.templete.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "compliance_alerts")
public class ComplianceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Alert title is required")
    private String title;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private boolean resolved = false;

    private LocalDate resolvedDate;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity = AlertSeverity.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    private LocalDate createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }

    // Constructors
    public ComplianceAlert() {
    }

    public ComplianceAlert(String title, String description, LocalDate dueDate, AlertSeverity severity) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.severity = severity;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
        if (resolved) {
            this.resolvedDate = LocalDate.now();
        } else {
            this.resolvedDate = null;
        }
    }

    public LocalDate getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDate resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}