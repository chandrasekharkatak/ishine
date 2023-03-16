package com.apmosys.employeeportal.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.LeaveBalanceLog;

public interface LeaveBalanceLogRepository extends JpaRepository<LeaveBalanceLog, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getLeaveLogsByEmpId(Long empId);

	List<LeaveBalanceLog> findByEmpIdAndLeaveTypeMasterId(Long empId, short s);

//	@Query(nativeQuery = true)
//	List<LeaveBalanceLog> findCompOffLogByEmpId(Long empId, short leaveTypeMasterId, String message);

	@Query(nativeQuery = true)
	LeaveBalanceLog findCompOffLogByEmpId(Long empId, short leaveTypeMasterId, String message, LocalDateTime updatedOn);

}
