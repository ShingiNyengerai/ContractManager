package cicosy.templete.service.impl;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.repository.ContractTemplateRepository;
import cicosy.templete.service.ContractTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the ContractTypeService interface
 */
@Service
public class ContractTypeServiceImpl implements ContractTypeService {

    private final ContractRepository contractRepository;
    private final ContractTemplateRepository contractTemplateRepository;

    @Autowired
    public ContractTypeServiceImpl(ContractRepository contractRepository,
                                  ContractTemplateRepository contractTemplateRepository) {
        this.contractRepository = contractRepository;
        this.contractTemplateRepository = contractTemplateRepository;
    }

    @Override
    public List<ContractType> getAllContractTypes() {
        return Arrays.asList(ContractType.values());
    }

    @Override
    public ContractType getContractTypeByName(String name) {
        try {
            return ContractType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<Contract> findContractsByType(ContractType contractType) {
        return contractRepository.findByContractType(contractType);
    }

    @Override
    public List<ContractTemplate> findTemplatesByType(ContractType contractType) {
        return contractTemplateRepository.findByContractType(contractType);
    }

    @Override
    public Map<ContractType, Long> countContractsByType() {
        Map<ContractType, Long> countMap = new HashMap<>();
        List<Contract> allContracts = contractRepository.findAll();
        
        for (Contract contract : allContracts) {
            ContractType type = contract.getContractType();
            countMap.put(type, countMap.getOrDefault(type, 0L) + 1);
        }
        
        return countMap;
    }

    @Override
    public Map<ContractType, Long> countTemplatesByType() {
        Map<ContractType, Long> countMap = new HashMap<>();
        List<ContractTemplate> allTemplates = contractTemplateRepository.findAll();
        
        for (ContractTemplate template : allTemplates) {
            if (template.getContractType() != null) {
                ContractType type = template.getContractType();
                countMap.put(type, countMap.getOrDefault(type, 0L) + 1);
            }
        }
        
        return countMap;
    }

    @Override
    public ContractType getMostCommonContractType() {
        Map<ContractType, Long> typeCounts = countContractsByType();
        return typeCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    @Override
    public boolean isContractTypeInUse(ContractType contractType) {
        return !contractRepository.findByContractType(contractType).isEmpty() || 
               !contractTemplateRepository.findByContractType(contractType).isEmpty();
    }

    @Override
    public List<ContractType> getContractTypesWithActiveContracts() {
        return contractRepository.findAll().stream()
            .filter(Contract::isActive)
            .map(Contract::getContractType)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<ContractType> getContractTypesWithTemplates() {
        return contractTemplateRepository.findAll().stream()
            .map(ContractTemplate::getContractType)
            .filter(type -> type != null)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<ContractType> getContractTypesWithoutTemplates() {
        List<ContractType> allTypes = getAllContractTypes();
        List<ContractType> typesWithTemplates = getContractTypesWithTemplates();
        
        return allTypes.stream()
                .filter(type -> !typesWithTemplates.contains(type))
                .collect(Collectors.toList());
    }
}