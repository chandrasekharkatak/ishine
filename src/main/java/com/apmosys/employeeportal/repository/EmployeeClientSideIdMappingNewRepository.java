package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeClientSideIdMapId;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMappingNew;

public interface EmployeeClientSideIdMappingNewRepository extends JpaRepository<EmployeeClientSideIdMappingNew, EmployeeClientSideIdMapId>{

}
