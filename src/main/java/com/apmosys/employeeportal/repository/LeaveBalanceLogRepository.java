package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.LeaveBalanceLog;

public interface LeaveBalanceLogRepository extends JpaRepository<LeaveBalanceLog, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getLeaveLogsByEmpId(Long empId);

}
