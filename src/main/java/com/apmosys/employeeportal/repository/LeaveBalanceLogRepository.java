package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
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

	@Query(nativeQuery = true)
	List<LeaveBalanceLog> findLogToReconsile(Long empId, short leaveTypeMasterId);
	@Query(nativeQuery = true,value="SELECT * FROM leave_balance_log WHERE emp_id = :empId AND   created_on=CURDATE()")
	List<LeaveBalanceLog> findLogValidation(Long empId);

}
