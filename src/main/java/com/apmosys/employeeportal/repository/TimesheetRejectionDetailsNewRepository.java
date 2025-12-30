package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TimesheetRejectionDetailsId;
import com.apmosys.employeeportal.model.TimesheetRejectionDetailsNew;

public interface TimesheetRejectionDetailsNewRepository extends JpaRepository<TimesheetRejectionDetailsNew, TimesheetRejectionDetailsId> {

}
