package cicosy.templete.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contract_performances")
public class ContractPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal actualCost;

    private BigDecimal plannedCost;

    private Integer completedMilestones = 0;

    private Integer totalMilestones = 0;

    private BigDecimal performanceIndex;

    @Column(length = 1000)
    private String performanceNotes;

    private LocalDate lastEvaluationDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

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
    public ContractPerformance() {
    }

    public ContractPerformance(BigDecimal plannedCost, Integer totalMilestones) {
        this.plannedCost = plannedCost;
        this.totalMilestones = totalMilestones;
        this.actualCost = BigDecimal.ZERO;
        this.completedMilestones = 0;
        this.performanceIndex = BigDecimal.ONE;
        this.lastEvaluationDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
        calculatePerformanceIndex();
    }

    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    public void setPlannedCost(BigDecimal plannedCost) {
        this.plannedCost = plannedCost;
        calculatePerformanceIndex();
    }

    public Integer getCompletedMilestones() {
        return completedMilestones;
    }

    public void setCompletedMilestones(Integer completedMilestones) {
        this.completedMilestones = completedMilestones;
        calculatePerformanceIndex();
    }

    public Integer getTotalMilestones() {
        return totalMilestones;
    }

    public void setTotalMilestones(Integer totalMilestones) {
        this.totalMilestones = totalMilestones;
        calculatePerformanceIndex();
    }

    public BigDecimal getPerformanceIndex() {
        return performanceIndex;
    }

    public void setPerformanceIndex(BigDecimal performanceIndex) {
        this.performanceIndex = performanceIndex;
    }

    public String getPerformanceNotes() {
        return performanceNotes;
    }

    public void setPerformanceNotes(String performanceNotes) {
        this.performanceNotes = performanceNotes;
    }

    public LocalDate getLastEvaluationDate() {
        return lastEvaluationDate;
    }

    public void setLastEvaluationDate(LocalDate lastEvaluationDate) {
        this.lastEvaluationDate = lastEvaluationDate;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    // Helper methods
    private void calculatePerformanceIndex() {
        if (plannedCost != null && actualCost != null && !plannedCost.equals(BigDecimal.ZERO)) {
            this.performanceIndex = plannedCost.divide(actualCost, 2, java.math.RoundingMode.HALF_UP);
        }
        this.lastEvaluationDate = LocalDate.now();
    }

    public double getMilestoneCompletionPercentage() {
        if (totalMilestones == 0) {
            return 0.0;
        }
        return (completedMilestones * 100.0) / totalMilestones;
    }
}