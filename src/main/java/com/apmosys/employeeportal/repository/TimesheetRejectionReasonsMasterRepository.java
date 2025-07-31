package com.apmosys.employeeportal.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;

public interface TimesheetRejectionReasonsMasterRepository extends JpaRepository<TimesheetRejectionReasonsMaster, Long> {

}
