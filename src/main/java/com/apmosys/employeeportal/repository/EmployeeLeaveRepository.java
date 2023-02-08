package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
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
	public List<Object[]> getAllTeamLeaveHistoryView(Long managerId, LocalDate fromDate, LocalDate toDate);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAppliedLeaveApplicationsByEmpIdAndDateRange(Long empId, String fromDate, String toDate, Short leaveTypeMasterId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamCompOffHistoryView(Long managerId, LocalDate fromDate, LocalDate toDate);

	@Query(nativeQuery = true)
	public Long countAllMyTeamsPendingLeaveApplicationsByManagerId(Integer managerId);

	public List<EmployeeLeave> findByLeaveTypeMasterId(Short oldLeaveTypeMasterId);

	@Query(nativeQuery = true)
	public List<Object[]> countMyPendingLeaveApplicationsByLeaveType(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> countMyApprovedLeaveApplicationsByLeaveType(Long empId);

	public List<EmployeeLeave> findByFromDate(LocalDate dateToday);

	@Query(nativeQuery = true)
	public List<Object[]> getLeaveReport();
	
	@Query(nativeQuery = true)
	public List<Object[]> getLast8DaysLeaveReport(LocalDate fromDate, LocalDate toDate);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamApplicationsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> countMyRejectedLeaveApplicationsByLeaveType(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveApplicationByFromDate(String fromDate);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentLeaveHistory(Long deptId, LocalDate fromDate, LocalDate toDate);

	@Query(nativeQuery = true)
	public List<Object[]> findLeaveTypeFromEmpIdAndDate(Long empId, String date);

}
