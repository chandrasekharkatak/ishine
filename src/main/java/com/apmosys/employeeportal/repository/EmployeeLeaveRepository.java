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

	@Query(nativeQuery = true, value = "SELECT * FROM employee_leave el WHERE el.manager_id = :managerId AND el.leave_status_id=1")
	public Optional<List<EmployeeLeave>> findLeaveByManagerId(Long managerId);

	@Query(nativeQuery = true, value = "select el.to_date,el.leave_id,ltm.leave_type_code,el.manager_id,ltm.leave_type from employee_leave el \n"
			+ "inner join leave_type_master ltm on ltm.leave_type_master_id=el.leave_type_master_id \n"
			+ "where ltm.leave_type_code= :leaveTypeCode and el.to_date= :toDate ")
	public Optional<List<Object[]>> findClLeavesInBetweenDates(LocalDate toDate , String leaveTypeCode);

	@Query(nativeQuery = true , value = "select * from employee_leave el where el.emp_id= :empId")
	public Optional<List<EmployeeLeave>> findLeavesByEmpId(Long empId);

	@Query(nativeQuery = true , value = "select em.name as managerName ,em.email as managerEmail,eh.emp_id ,eh.email as hodMail,eh.name as hodName\n"
			+ "from employee e \n"
			+ "inner join employee em ON e.manager_id=em.emp_id\n"
			+ "inner join job_role jr ON jr.job_role_id=em.job_role_id\n"
			+ "inner join department d ON d.dept_id=jr.dept_id\n"
			+ "inner join employee eh ON eh.emp_id=d.hod_id\n"
			+ "where e.emp_id= :empId")
	public List<Object[]> findHodsData(Long empId);

	@Query(nativeQuery = true , value = "select * from employee_leave el where el.leave_status_id=1")
	public List<EmployeeLeave> findOldPendingLeaves();

	@Query(nativeQuery = true , value = "select el.leave_id,e.employeement_id ,el.emp_id as employeeId,e.name as employeeName, el.from_date , el.to_date,el.created_on ,el.manager_id as ManagerId, e.name as createdByName , em.name as ManagerName,ls.status as status from employee_leave el \n"
			+ "inner join employee e ON e.emp_id=el.emp_id\n"
			+ "inner join leave_status ls on ls.leave_status_id=el.leave_status_id\n "
			+ "inner join employee em ON em.emp_id=el.manager_id "
			+ "where el.manager_id=:managerId and ((el.from_date  between :fromDate and :toDate) or (el.to_date between :fromDate and :toDate))")
	public List<Object[]> getOverLapsLeaveForManager(String fromDate, String toDate, Integer managerId);

	@Query(nativeQuery = true , value = "SELECT * FROM employee_leave el WHERE el.leave_status_id=1 AND el.emp_id = :empId AND el.manager_approval_status='Pending'")
	public List<EmployeeLeave> findLeavesByEmpIdAndStatus(Long empId);

}
