package cicosy.templete.repository;

import cicosy.templete.domain.ApprovalWorkflow;
import cicosy.templete.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {

    List<ApprovalWorkflow> findByCompleted(boolean completed);
    
    List<ApprovalWorkflow> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT w FROM ApprovalWorkflow w JOIN w.approvers a WHERE a = :approver")
    List<ApprovalWorkflow> findByApprover(User approver);
    
    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.currentStep < w.numberOfSteps")
    List<ApprovalWorkflow> findPendingWorkflows();
}