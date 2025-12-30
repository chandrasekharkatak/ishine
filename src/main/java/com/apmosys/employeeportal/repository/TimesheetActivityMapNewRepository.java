package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.TimesheetActivityMapId;

public interface TimesheetActivityMapNewRepository extends JpaRepository<EmployeeTimesheetActivitiesMappingNew, TimesheetActivityMapId>  {

}
