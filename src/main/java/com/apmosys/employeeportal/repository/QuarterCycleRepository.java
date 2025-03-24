package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.QuarterCycleDTO;
import com.apmosys.employeeportal.model.QuaterCycle;

@Repository
public interface QuarterCycleRepository extends JpaRepository<QuaterCycle, Long> {
    
	
	@Query(nativeQuery=true,value="select * from quater_cycle where financial_year = :financialYear and is_active =1")
	List<QuaterCycle> findByFinancialYear(String financialYear);
	
	@Query(nativeQuery=true,value="select qc.quarter_id,qc.financial_year,qc.quarter_cycle,qc.created_on,qc.created_by,e.name as createdByName,qc.updated_on,qc.updated_by,er.name as updatedByName,qc.is_active,qc.is_enable from quater_cycle qc inner join employee e on qc.created_by=e.emp_id left join employee er on qc.updated_by=er.emp_id where qc.is_active= 1")
	List<Object[]> findAllQuartercycles();
	
	
	@Query(nativeQuery=true,value="select qc.quarter_id,qc.financial_year,qc.quarter_cycle,qc.created_on,qc.created_by,e.name as createdByName,qc.updated_on,qc.updated_by,er.name as updatedByName,qc.is_active,qc.is_enable from quater_cycle qc inner join employee e on qc.created_by=e.emp_id left join employee er on qc.updated_by=er.emp_id where quarter_id = :quarterId")
	List<Object[]> findQuarterCycleById(@Param("quarterId") Long quarterId);
	
	@Query(nativeQuery=true,value="select qc.quarter_cycle from quater_cycle qc inner join employee e on qc.created_by=e.emp_id left join employee er on qc.updated_by=er.emp_id where quarter_id = :quarterId")
	public String findquartercyclebyID(Long quarterId);
	
	


	
}
