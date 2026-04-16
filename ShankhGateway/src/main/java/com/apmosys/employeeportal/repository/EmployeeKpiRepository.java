package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeKpiRepository extends JpaRepository<EmployeeKpi, Long> {
    
    List<EmployeeKpi> findByEmpId(Long empId);
    
    List<EmployeeKpi> findByTemplateId(Long templateId);
    
    List<EmployeeKpi> findByEmpIdAndTemplateId(Long empId, Long templateId);
}