package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TimesheetActionAuditNew;

public interface TimesheetActionAuditNewRepository extends JpaRepository<TimesheetActionAuditNew, Long>{

	
}
