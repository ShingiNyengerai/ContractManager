package cicosy.templete.service.impl;

import cicosy.templete.domain.ApprovalWorkflow;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ApprovalWorkflowRepository;
import cicosy.templete.repository.UserRepository;
import cicosy.templete.service.ApprovalWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {
	private final ApprovalWorkflowRepository workflowRepository;
	private final UserRepository userRepository;

	@Autowired
	public ApprovalWorkflowServiceImpl(ApprovalWorkflowRepository workflowRepository, UserRepository userRepository) {
		this.workflowRepository = workflowRepository;
		this.userRepository = userRepository;
	}

	@Override
	public ApprovalWorkflow findById(Long id) {
		return workflowRepository.findById(id).orElse(null);
	}

	@Override
	public List<ApprovalWorkflow> findAll() {
		return workflowRepository.findAll();
	}

	@Override
	public List<ApprovalWorkflow> findByCompleted(boolean completed) {
		return workflowRepository.findByCompleted(completed);
	}

	@Override
	public List<ApprovalWorkflow> findByNameContaining(String name) {
		return workflowRepository.findByNameContainingIgnoreCase(name);
	}

	@Override
	public List<ApprovalWorkflow> findByApprover(User approver) {
		return workflowRepository.findByApprover(approver);
	}

	@Override
	public List<ApprovalWorkflow> findPendingWorkflows() {
		return workflowRepository.findPendingWorkflows();
	}

	@Override
	public ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow) {
		workflow.setCurrentStep(0);
		workflow.setCompleted(false);
		return workflowRepository.save(workflow);
	}

	@Override
	public ApprovalWorkflow updateWorkflow(ApprovalWorkflow workflow) {
		return workflowRepository.save(workflow);
	}

	@Override
	public void deleteWorkflow(Long id) {
		workflowRepository.deleteById(id);
	}

	@Override
	public void addApprover(Long workflowId, Long userId) {
		ApprovalWorkflow workflow = findById(workflowId);
		User user = userRepository.findById(userId).orElse(null);
		if (workflow != null && user != null) {
			workflow.getApprovers().add(user);
			workflowRepository.save(workflow);
		}
	}

	@Override
	public void removeApprover(Long workflowId, Long userId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow != null) {
			workflow.getApprovers().removeIf(u -> u.getId().equals(userId));
			workflowRepository.save(workflow);
		}
	}

	@Override
	public void advanceWorkflow(Long workflowId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow == null || workflow.isCompleted()) return;
		int next = workflow.getCurrentStep() + 1;
		if (next >= workflow.getNumberOfSteps()) {
			completeWorkflow(workflowId);
		} else {
			workflow.setCurrentStep(next);
			workflowRepository.save(workflow);
		}
	}

	@Override
	public void completeWorkflow(Long workflowId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow != null) {
			workflow.setCurrentStep(workflow.getNumberOfSteps());
			workflow.setCompleted(true);
			workflowRepository.save(workflow);
		}
	}

	@Override
	public void resetWorkflow(Long workflowId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow != null) {
			workflow.setCurrentStep(0);
			workflow.setCompleted(false);
			workflowRepository.save(workflow);
		}
	}

	@Override
	public boolean isUserApproverInWorkflow(Long workflowId, Long userId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow == null) return false;
		return workflow.getApprovers().stream().anyMatch(u -> u.getId().equals(userId));
	}

	@Override
	public User getCurrentApprover(Long workflowId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow == null) return null;
		int idx = workflow.getCurrentStep();
		if (workflow.getApprovers() == null || workflow.getApprovers().isEmpty() || idx < 0 || idx >= workflow.getApprovers().size()) return null;
		return workflow.getApprovers().get(idx);
	}

	@Override
	public int getWorkflowProgress(Long workflowId) {
		ApprovalWorkflow workflow = findById(workflowId);
		if (workflow == null || workflow.getNumberOfSteps() == 0) return 0;
		return (int) Math.round((workflow.getCurrentStep() * 100.0) / workflow.getNumberOfSteps());
	}
}