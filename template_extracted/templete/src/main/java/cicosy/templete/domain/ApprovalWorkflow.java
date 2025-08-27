package cicosy.templete.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approval_workflows")
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Workflow name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    private Integer numberOfSteps;

    private Integer currentStep = 0;

    private boolean completed = false;

    private LocalDate completedDate;

    @OneToMany(mappedBy = "approvalWorkflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contract> contracts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "workflow_approvers",
        joinColumns = @JoinColumn(name = "workflow_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> approvers = new ArrayList<>();

    private LocalDate createdDate;
    private LocalDate lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        lastModifiedDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDate.now();
    }

    // Constructors
    public ApprovalWorkflow() {
    }

    public ApprovalWorkflow(String name, String description, Integer numberOfSteps) {
        this.name = name;
        this.description = description;
        this.numberOfSteps = numberOfSteps;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumberOfSteps() {
        return numberOfSteps;
    }

    public void setNumberOfSteps(Integer numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
        if (currentStep >= numberOfSteps) {
            this.completed = true;
            this.completedDate = LocalDate.now();
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.completedDate = LocalDate.now();
        } else {
            this.completedDate = null;
        }
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public List<User> getApprovers() {
        return approvers;
    }

    public void setApprovers(List<User> approvers) {
        this.approvers = approvers;
    }

    public void addApprover(User approver) {
        approvers.add(approver);
    }

    public void removeApprover(User approver) {
        approvers.remove(approver);
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }
}