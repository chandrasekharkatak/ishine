package com.apmosys.employeeportal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TimesheetApprovalAllocationLogs;


@Repository
public interface TimesheetApprovalAllocationLogsRepository extends JpaRepository<TimesheetApprovalAllocationLogs, Long> {

}
