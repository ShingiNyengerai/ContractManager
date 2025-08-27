package cicosy.templete.repository;

import cicosy.templete.domain.Contract;
import cicosy.templete.domain.ContractPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractPerformanceRepository extends JpaRepository<ContractPerformance, Long> {

    ContractPerformance findByContract(Contract contract);
    
    @Query("SELECT p FROM ContractPerformance p WHERE p.performanceIndex < :threshold")
    List<ContractPerformance> findUnderperformingContracts(BigDecimal threshold);
    
    @Query("SELECT p FROM ContractPerformance p WHERE p.performanceIndex > :threshold")
    List<ContractPerformance> findOverperformingContracts(BigDecimal threshold);
    
    List<ContractPerformance> findByLastEvaluationDateBefore(LocalDate date);
}