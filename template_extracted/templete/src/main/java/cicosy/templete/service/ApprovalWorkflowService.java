package cicosy.templete.service;

import cicosy.templete.domain.ApprovalWorkflow;
import cicosy.templete.domain.User;

import java.util.List;

public interface ApprovalWorkflowService {

    ApprovalWorkflow findById(Long id);
    
    List<ApprovalWorkflow> findAll();
    
    List<ApprovalWorkflow> findByCompleted(boolean completed);
    
    List<ApprovalWorkflow> findByNameContaining(String name);
    
    List<ApprovalWorkflow> findByApprover(User approver);
    
    List<ApprovalWorkflow> findPendingWorkflows();
    
    ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow);
    
    ApprovalWorkflow updateWorkflow(ApprovalWorkflow workflow);
    
    void deleteWorkflow(Long id);
    
    void addApprover(Long workflowId, Long userId);
    
    void removeApprover(Long workflowId, Long userId);
    
    void advanceWorkflow(Long workflowId);
    
    void completeWorkflow(Long workflowId);
    
    void resetWorkflow(Long workflowId);
    
    boolean isUserApproverInWorkflow(Long workflowId, Long userId);
    
    User getCurrentApprover(Long workflowId);
    
    int getWorkflowProgress(Long workflowId);
}