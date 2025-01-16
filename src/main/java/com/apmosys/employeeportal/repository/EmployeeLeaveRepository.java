package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

	public EmployeeLeave findByLeaveId(Long leaveId);

	public List<EmployeeLeave> findByFromDateAfterAndLeaveStatusId(LocalDate leaveFromDate, short s);

	public List<EmployeeLeave> findAllByEmpIdAndLeaveTypeMasterId(Long empId, Short leaveTypeMasterId);

	@Query(nativeQuery = true)
	public List<EmployeeLeave> findLeaveApplicationByCreatedOnDate(Long empId, short leaveTypeMasterId, Timestamp createdOn);

	@Query(nativeQuery = true)
	public EmployeeLeave findLeaveApplicationByCreatedOnDate(Long empId, short leaveTypeMasterId,
			LocalDate createdOn);

	@Query(nativeQuery = true)
	public List<Object[]> findOverlappedTeamMemberLeave(Long empId, String toDate, String fromDate);

	@Query(nativeQuery = true)
	public List<EmployeeLeave> findRecentLeavesByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public EmployeeLeave findEmployeeLeaveByToDate(LocalDate toDate,Long empId);

	@Query(nativeQuery = true)
	List<EmployeeLeave> findLeaveByFromDate(String fromDate, Long empId);

	@Query(nativeQuery = true)
	public Optional<List<EmployeeLeave>> findLeaveByFromDateAndToDate(String fromDate, String toDate, Long empId);

	@Query(nativeQuery = true)
	public Optional<List<EmployeeLeave>> findLeaveByManagerId(Long managerId);

	@Query(nativeQuery = true)
	public Optional<List<Object[]>> findClLeavesInBetweenDates(LocalDate toDate , String leaveTypeCode);

	@Query(nativeQuery = true )
	public Optional<List<EmployeeLeave>> findLeavesByEmpId(Long empId);

	@Query(nativeQuery = true )
	public List<Object[]> findHodsData(Long empId);

	@Query(nativeQuery = true )
	public List<EmployeeLeave> findOldPendingLeaves();

	@Query(nativeQuery = true)
	public List<Object[]> getOverLapsLeaveForManager(String fromDate, String toDate, Integer managerId);

	@Query(nativeQuery = true )
	public List<EmployeeLeave> findLeavesByEmpIdAndStatus(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentPendingLeaveHistory(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentLeaveHistoryAndNotIn(Long deptId, LocalDate fromDate, LocalDate toDate,
			List<String> jobRoles);
	
	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentPendingLeaveHistoryStatusNotIn(Long deptId, List<String> jobRoles);
	
	@Query(value = "SELECT * FROM employee_leave " +
            "WHERE leave_type_master_id = 2 AND leave_status_id = 2 AND manager_approval_status = 'Approved' ", 
    nativeQuery = true)
public List<EmployeeLeave> findByEmployeeforApproved();
	
	@Query(value = "SELECT * FROM employee_leave " +
            "WHERE leave_type_master_id = 2 AND leave_status_id = 1 AND manager_approval_status = 'Pending' ", 
    nativeQuery = true)
public List<EmployeeLeave> findByEmployeeforPending();
	
//	@Query(value = "SELECT * FROM db_emp_portal.employee_leave el " +
//            "INNER JOIN db_emp_portal.employee_leaves_mapping elm ON el.emp_id = elm.emp_id " +
//            "WHERE el.leave_type_master_id = 4 AND el.leave_status_id = 1 AND elm.manager_approval_status = 'Pending' AND el.from_date BETWEEN '2025-01-01' AND '2025-01-31'", 
//    nativeQuery = true)
//public List<EmployeeLeave> findByEmployeeforPending();



}
