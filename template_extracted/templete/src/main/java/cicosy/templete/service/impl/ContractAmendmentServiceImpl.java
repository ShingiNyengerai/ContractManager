package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractAmendment;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ContractAmendmentRepository;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.service.ContractAmendmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContractAmendmentServiceImpl implements ContractAmendmentService {

    private final ContractAmendmentRepository amendmentRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public ContractAmendmentServiceImpl(ContractAmendmentRepository amendmentRepository,
                                        ContractRepository contractRepository) {
        this.amendmentRepository = amendmentRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public ContractAmendment findById(Long id) {
        Optional<ContractAmendment> opt = amendmentRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<ContractAmendment> findAll() {
        return amendmentRepository.findAll();
    }

    @Override
    public List<ContractAmendment> findByContract(Contract contract) {
        return amendmentRepository.findByContract(contract);
    }

    @Override
    public List<ContractAmendment> findByContractAndApproved(Contract contract, boolean approved) {
        return amendmentRepository.findByContractAndApproved(contract, approved);
    }

    @Override
    public List<ContractAmendment> findByApprovedBy(User approvedBy) {
        return amendmentRepository.findByApprovedBy(approvedBy);
    }

    @Override
    public List<ContractAmendment> findByAmendmentDateBetween(LocalDate startDate, LocalDate endDate) {
        return amendmentRepository.findByAmendmentDateBetween(startDate, endDate);
    }

    @Override
    public List<ContractAmendment> findByTitleContaining(String title) {
        return amendmentRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    @Transactional
    public ContractAmendment createAmendment(ContractAmendment amendment) {
        // Ensure new amendments are not pre-approved by default
        if (amendment.getAmendmentDate() == null) {
            amendment.setAmendmentDate(LocalDate.now());
        }
        amendment.setApproved(false);
        amendment.setApprovedBy(null);
        return amendmentRepository.save(amendment);
    }

    @Override
    @Transactional
    public ContractAmendment updateAmendment(ContractAmendment amendment) {
        return amendmentRepository.save(amendment);
    }

    @Override
    @Transactional
    public void deleteAmendment(Long id) {
        amendmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void approveAmendment(Long id, User approver) {
        ContractAmendment amendment = findById(id);
        if (amendment != null) {
            amendment.setApproved(true);
            amendment.setApprovedBy(approver);
            amendmentRepository.save(amendment);
        }
    }

    @Override
    @Transactional
    public void rejectAmendment(Long id) {
        ContractAmendment amendment = findById(id);
        if (amendment != null) {
            amendment.setApproved(false);
            amendment.setApprovedBy(null);
            amendmentRepository.save(amendment);
        }
    }

    @Override
    @Transactional
    public void applyAmendmentToContract(Long amendmentId) {
        ContractAmendment amendment = findById(amendmentId);
        if (amendment == null) {
            return;
        }
        Contract contract = amendment.getContract();
        if (contract == null) {
            return;
        }
        // Only apply if approved
        if (!amendment.isApproved()) {
            return;
        }
        // Minimal example: append amendment changes to the contract description for auditability
        StringBuilder desc = new StringBuilder(contract.getDescription() == null ? "" : contract.getDescription());
        String prefix = desc.length() > 0 ? "\n\n" : "";
        String changes = amendment.getChanges();
        if (changes != null && !changes.isBlank()) {
            desc.append(prefix)
                .append("[Amendment on ")
                .append(amendment.getAmendmentDate() != null ? amendment.getAmendmentDate() : LocalDate.now())
                .append("]: ")
                .append(changes);
            contract.setDescription(desc.toString());
        }
        // Persist the contract update
        contractRepository.save(contract);
    }
}