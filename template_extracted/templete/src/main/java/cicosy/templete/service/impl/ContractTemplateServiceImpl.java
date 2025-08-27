package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.repository.ContractTemplateRepository;
import cicosy.templete.service.ContractTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractTemplateServiceImpl implements ContractTemplateService {

    private final ContractTemplateRepository contractTemplateRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public ContractTemplateServiceImpl(ContractTemplateRepository contractTemplateRepository,
                                      ContractRepository contractRepository) {
        this.contractTemplateRepository = contractTemplateRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public ContractTemplate findById(Long id) {
        Optional<ContractTemplate> template = contractTemplateRepository.findById(id);
        return template.orElse(null);
    }

    @Override
    public List<ContractTemplate> findAll() {
        return contractTemplateRepository.findAll();
    }

    @Override
    public List<ContractTemplate> findByActive(boolean active) {
        return contractTemplateRepository.findByActive(active);
    }

    @Override
    public List<ContractTemplate> findByContractType(ContractType contractType) {
        return contractTemplateRepository.findByContractType(contractType);
    }

    @Override
    public List<ContractTemplate> findByCreatedBy(User createdBy) {
        return contractTemplateRepository.findByCreatedBy(createdBy);
    }

    @Override
    public List<ContractTemplate> findByNameContaining(String name) {
        return contractTemplateRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public ContractTemplate createTemplate(ContractTemplate template) {
        // createdDate is set automatically by @PrePersist
        return contractTemplateRepository.save(template);
    }

    @Override
    public ContractTemplate updateTemplate(ContractTemplate template) {
        // lastModifiedDate is set automatically by @PreUpdate
        return contractTemplateRepository.save(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        contractTemplateRepository.deleteById(id);
    }

    @Override
    public void activateTemplate(Long id) {
        Optional<ContractTemplate> optionalTemplate = contractTemplateRepository.findById(id);
        if (optionalTemplate.isPresent()) {
            ContractTemplate template = optionalTemplate.get();
            template.setActive(true);
            contractTemplateRepository.save(template);
        }
    }

    @Override
    public void deactivateTemplate(Long id) {
        Optional<ContractTemplate> optionalTemplate = contractTemplateRepository.findById(id);
        if (optionalTemplate.isPresent()) {
            ContractTemplate template = optionalTemplate.get();
            template.setActive(false);
            contractTemplateRepository.save(template);
        }
    }

    @Override
    public ContractTemplate cloneTemplate(Long id, String newName) {
        Optional<ContractTemplate> optionalTemplate = contractTemplateRepository.findById(id);
        if (optionalTemplate.isPresent()) {
            ContractTemplate originalTemplate = optionalTemplate.get();
            ContractTemplate clonedTemplate = new ContractTemplate();
            
            // Copy properties from original template
            clonedTemplate.setName(newName);
            clonedTemplate.setDescription(originalTemplate.getDescription());
            clonedTemplate.setContractType(originalTemplate.getContractType());
            clonedTemplate.setContent(originalTemplate.getContent());
            clonedTemplate.setActive(originalTemplate.isActive());
            clonedTemplate.setCreatedBy(originalTemplate.getCreatedBy());
            
            return contractTemplateRepository.save(clonedTemplate);
        }
        return null;
    }

    @Override
    public ContractTemplate applyTemplateToContract(Long templateId, Long contractId) {
        Optional<ContractTemplate> optionalTemplate = contractTemplateRepository.findById(templateId);
        Optional<Contract> optionalContract = contractRepository.findById(contractId);
        
        if (optionalTemplate.isPresent() && optionalContract.isPresent()) {
            ContractTemplate template = optionalTemplate.get();
            Contract contract = optionalContract.get();
            
            // Apply template to contract
            contract.setTemplate(template);
            contractRepository.save(contract);
            
            return template;
        }
        return null;
    }

    @Override
    public List<ContractTemplate> findRecentTemplates(int limit) {
        // Assuming we want to sort by creation date
        return contractTemplateRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"))
        ).getContent();
    }

    @Override
    public List<ContractTemplate> findPopularTemplates(int limit) {
        // This is a simplified implementation
        // In a real application, you might want to count how many contracts use each template
        // and sort by that count
        return findRecentTemplates(limit); // For now, just return recent templates
    }
}