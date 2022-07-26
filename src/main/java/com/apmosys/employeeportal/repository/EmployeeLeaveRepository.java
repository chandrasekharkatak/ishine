package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeLeave;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {

	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyLeaveApplicationsByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamsPendingLeaveApplicationsByManagerId(Integer managerId);
	
	public List<EmployeeLeave> findAllByEmpIdAndLeaveTypeMasterIdAndLeaveStatusId(Long empId,Short leaveTypeMasterId,Short leaveStatusId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamLeaveHistoryView(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getApprovedLeaveApplicationsByEmpIdAndDateRange(Long empId, String fromDate, String toDate);

}
