package cicosy.templete.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "contract_amendments")
public class ContractAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Amendment title is required")
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Amendment date is required")
    private LocalDate amendmentDate;

    @Column(length = 5000)
    private String changes;

    private boolean approved = false;

    private LocalDate approvedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    // Constructors
    public ContractAmendment() {
    }

    public ContractAmendment(String title, String description, LocalDate amendmentDate, String changes) {
        this.title = title;
        this.description = description;
        this.amendmentDate = amendmentDate;
        this.changes = changes;
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

    public LocalDate getAmendmentDate() {
        return amendmentDate;
    }

    public void setAmendmentDate(LocalDate amendmentDate) {
        this.amendmentDate = amendmentDate;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
        if (approved) {
            this.approvedDate = LocalDate.now();
        } else {
            this.approvedDate = null;
        }
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
}