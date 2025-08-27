package cicosy.templete.service;

import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.domain.User;

import java.util.List;

public interface ContractTemplateService {

    ContractTemplate findById(Long id);
    
    List<ContractTemplate> findAll();
    
    List<ContractTemplate> findByActive(boolean active);
    
    List<ContractTemplate> findByContractType(ContractType contractType);
    
    List<ContractTemplate> findByCreatedBy(User createdBy);
    
    List<ContractTemplate> findByNameContaining(String name);
    
    ContractTemplate createTemplate(ContractTemplate template);
    
    ContractTemplate updateTemplate(ContractTemplate template);
    
    void deleteTemplate(Long id);
    
    void activateTemplate(Long id);
    
    void deactivateTemplate(Long id);
    
    ContractTemplate cloneTemplate(Long id, String newName);
    
    ContractTemplate applyTemplateToContract(Long templateId, Long contractId);
    
    List<ContractTemplate> findRecentTemplates(int limit);
    
    List<ContractTemplate> findPopularTemplates(int limit);
}