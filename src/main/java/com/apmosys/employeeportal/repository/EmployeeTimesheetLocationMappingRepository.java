package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;

import java.util.List;

public interface EmployeeTimesheetLocationMappingRepository
        extends JpaRepository<EmployeeTimesheetLocationMapping, Long> {

    List<EmployeeTimesheetLocationMapping> findByTimesheetId(Long timesheetId);
}

