package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeeKpi;
import com.apmosys.employeeportal.model.EmployeeKpiMapping;

@Repository
public interface EmployeeKpiMappingRepository extends JpaRepository<EmployeeKpiMapping, Long>{

	Optional<EmployeeKpiMapping> findByEmpIdAndQuarterId(Long empId, Long quarterId);

	EmployeeKpiMapping save(EmployeeKpiMapping kpiMapping);

}
