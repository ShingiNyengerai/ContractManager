package cicosy.templete.service;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractTemplate;
import cicosy.templete.domain.ContractType;
import cicosy.templete.repository.ContractRepository;
import cicosy.templete.repository.ContractTemplateRepository;
import cicosy.templete.service.impl.ContractTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContractTypeServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractTemplateRepository contractTemplateRepository;

    private ContractTypeService contractTypeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        contractTypeService = new ContractTypeServiceImpl(contractRepository, contractTemplateRepository);
    }

    @Test
    public void testGetAllContractTypes() {
        List<ContractType> types = contractTypeService.getAllContractTypes();
        
        assertNotNull(types);
        assertEquals(ContractType.values().length, types.size());
        assertTrue(types.contains(ContractType.SERVICE_AGREEMENT));
        assertTrue(types.contains(ContractType.EMPLOYMENT_CONTRACT));
    }

    @Test
    public void testGetContractTypeByName() {
        ContractType type = contractTypeService.getContractTypeByName("SERVICE_AGREEMENT");
        assertEquals(ContractType.SERVICE_AGREEMENT, type);
        
        type = contractTypeService.getContractTypeByName("service_agreement");
        assertEquals(ContractType.SERVICE_AGREEMENT, type);
        
        type = contractTypeService.getContractTypeByName("INVALID_TYPE");
        assertNull(type);
    }

    @Test
    public void testFindContractsByType() {
        Contract contract1 = new Contract();
        Contract contract2 = new Contract();
        List<Contract> expectedContracts = Arrays.asList(contract1, contract2);
        
        when(contractRepository.findByContractType(ContractType.SERVICE_AGREEMENT))
            .thenReturn(expectedContracts);
        
        List<Contract> contracts = contractTypeService.findContractsByType(ContractType.SERVICE_AGREEMENT);
        
        assertNotNull(contracts);
        assertEquals(2, contracts.size());
        verify(contractRepository).findByContractType(ContractType.SERVICE_AGREEMENT);
    }

    @Test
    public void testFindTemplatesByType() {
        ContractTemplate template1 = new ContractTemplate();
        ContractTemplate template2 = new ContractTemplate();
        List<ContractTemplate> expectedTemplates = Arrays.asList(template1, template2);
        
        when(contractTemplateRepository.findByContractType(ContractType.SERVICE_AGREEMENT))
            .thenReturn(expectedTemplates);
        
        List<ContractTemplate> templates = contractTypeService.findTemplatesByType(ContractType.SERVICE_AGREEMENT);
        
        assertNotNull(templates);
        assertEquals(2, templates.size());
        verify(contractTemplateRepository).findByContractType(ContractType.SERVICE_AGREEMENT);
    }

    @Test
    public void testIsContractTypeInUse() {
        when(contractRepository.findByContractType(ContractType.SERVICE_AGREEMENT))
            .thenReturn(Arrays.asList(new Contract()));
        when(contractRepository.findByContractType(ContractType.EMPLOYMENT_CONTRACT))
            .thenReturn(List.of());
        when(contractTemplateRepository.findByContractType(ContractType.EMPLOYMENT_CONTRACT))
            .thenReturn(Arrays.asList(new ContractTemplate()));
        
        assertTrue(contractTypeService.isContractTypeInUse(ContractType.SERVICE_AGREEMENT));
        assertTrue(contractTypeService.isContractTypeInUse(ContractType.EMPLOYMENT_CONTRACT));
        
        when(contractRepository.findByContractType(ContractType.OTHER))
            .thenReturn(List.of());
        when(contractTemplateRepository.findByContractType(ContractType.OTHER))
            .thenReturn(List.of());
        
        assertFalse(contractTypeService.isContractTypeInUse(ContractType.OTHER));
    }
}