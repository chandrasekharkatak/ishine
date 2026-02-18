package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.EmployeeLeave;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {

	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyLeaveApplicationsByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamsPendingLeaveApplicationsByManagerId(Integer managerId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveApplicationsByEmpIdAndManagerApprovalStatus(Long empId,String status);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveApplicationsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveApplicationsByEmpIdAndFromDate(Long empId,LocalDate localFromDate,LocalDate localToDate);
	
	@Query(nativeQuery = true)
	public List<Long> findTeamIdsByEmpId(Long empId);

    @Query(nativeQuery = true)
    public List<Object[]> getAllLeaveApplicationsByTeamId(List<Long> teamIds);
    
	public List<EmployeeLeave> findAllByEmpIdAndLeaveTypeMasterIdAndLeaveStatusId(Long empId,Short leaveTypeMasterId,Short leaveStatusId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamLeaveHistoryView(Long managerId, LocalDate fromDate, LocalDate toDate);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAppliedLeaveApplicationsByEmpIdAndDateRange(Long empId, String fromDate, String toDate, Short leaveTypeMasterId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamCompOffHistoryView(Long managerId, LocalDate fromDate, LocalDate toDate);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamCompOffHistoryViewByEmpId(Long empId, LocalDate fromDate, LocalDate toDate);

	@Query(nativeQuery = true)
	public String countAllMyTeamsPendingLeaveApplicationsByManagerId(Integer managerId);

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
	
	// @Query(nativeQuery = true)
	// public List<Object[]> findLeaveTypeFromEmpIdAndDateNew(Long empId, String date);

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
	
	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentPendingLeaveHistory(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentLeaveHistoryAndNotIn(Long empId, LocalDate fromDate, LocalDate toDate,
			List<String> jobRoles);
	
	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentPendingLeaveHistoryStatusNotIn(Long deptId, List<String> jobRoles);
	
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveApplicationsByLeaveId(Long leaveId);
	@Query(value = "SELECT * FROM employee_leave " +
            "WHERE leave_type_master_id = 2 AND leave_status_id = 2 AND manager_approval_status = 'Approved' ", 
    nativeQuery = true)
public List<EmployeeLeave> findByEmployeeforApproved();
	
	@Query(value = "SELECT * FROM employee_leave " +
            "WHERE leave_type_master_id = 2 AND leave_status_id = 1 AND manager_approval_status = 'Pending' ", 
    nativeQuery = true)
public List<EmployeeLeave> findByEmployeeforPending();

@Query(value = " FROM EmployeeLeave WHERE current_date() between fromDate AND toDate")
public List<EmployeeLeave> findEmployeeIsOnLeaveToday();

//used in travel desk to fetch the whethere the reporting manager is on leave or not
@Query(nativeQuery = true, value = 
"SELECT emp.name " +
"FROM employee_leave el " +
"INNER JOIN leave_type_master ltm ON ltm.leave_type_master_id = el.leave_type_master_id " +
"INNER JOIN leave_status ls ON ls.leave_status_id = el.leave_status_id " +
"INNER JOIN employee e ON e.emp_id = el.emp_id " +
"INNER JOIN employee emp ON emp.emp_id = el.emp_id " +
"INNER JOIN employee em ON em.emp_id = el.manager_id " +
"LEFT JOIN employee e2 ON e2.emp_id = el.level2approver_id " +
"LEFT JOIN employee e3 ON e3.emp_id = el.level3approver_id " +
"WHERE el.emp_id = :empId " +
"AND CURDATE() BETWEEN el.from_date AND el.to_date " +
"AND ls.status != 'Rejected'")
List<Object[]> reportingManagerIsOnLeave(@Param("empId") Long empId);


@Query("SELECT e FROM EmployeeLeave e WHERE e.empId = :empId AND " +
	       "((e.fromDate BETWEEN :startOfMonth AND :endOfMonth) OR " +
	       " (e.toDate BETWEEN :startOfMonth AND :endOfMonth))")
	List<EmployeeLeave> findLeavesInCurrentMonth(
	    @Param("empId") Long empId,
	    @Param("startOfMonth") LocalDate startOfMonth,
	    @Param("endOfMonth") LocalDate endOfMonth
	);

@Query("SELECT COUNT(e) > 0 FROM EmployeeLeave e " +
	       "WHERE e.empId = :empId " +
	       "AND e.leaveStatusId IN (1,2,4) " +
	       "AND e.fromDate <= :toDate " +
	       "AND e.toDate >= :fromDate")
	boolean existsOverlappingLeave(@Param("empId") Long empId,
	                               @Param("fromDate") LocalDate fromDate,
	                               @Param("toDate") LocalDate toDate);
//@Query(nativeQuery = true)
//public List<Object[]> getAllMyTeamsApprovedLeaveApplicationsByManagerId(Integer managerId);


@Query(value = " SELECT \n"
		+ " el.leave_id, \n"
		+ " ltm.leave_type,el.from_date, \n"
		+ " el.to_date, \n"
		+ " el.no_of_days,ls.status, \n"
		+ " emp.name AS createdByName, \n"
		+ " el.created_on,el.reason, el.created_by, el.leave_type_master_id, emp1.name AS employeeName, \n"
		+ " emp1.email, emp1.employeement_id, em.name AS approverName, em.emp_id, em.email AS approverMail, \n"
		+ " el.manager_approval_status, el.level2approver_id, e2.name AS level2ApproverName, \n"
		+ " e2.email AS level2ApproverEmail, el.level2approval_status, el.level3approver_id, \n"
		+ " e3.name AS level3ApproverName, el.level3approval_status, e3.email AS level3ApproverEmail, \n"
		+ " el.current_approval_level, el.final_approval_level, emp1.emp_id AS leaveEmpId, el.manager_id, \n"
		+ " GROUP_CONCAT(DISTINCT t.team_name ORDER BY t.team_name ASC SEPARATOR ', ') AS team_names, \n"
		+ " GROUP_CONCAT(DISTINCT c.client_name ORDER BY c.client_name ASC SEPARATOR ', ') AS client_names \n"
		+ "FROM employee_leave el \n"
		+ "INNER JOIN employee emp ON emp.emp_id = el.created_by \n"
		+ "INNER JOIN employee emp1 ON emp1.emp_id = el.emp_id \n"
		+ "INNER JOIN leave_type_master ltm ON ltm.leave_type_master_id = el.leave_type_master_id \n"
		+ "INNER JOIN leave_status ls ON ls.leave_status_id = el.leave_status_id \n"
		+ "INNER JOIN employee em ON em.emp_id = el.manager_id \n"
		+ "LEFT JOIN employee_team_mapping etm ON etm.emp_id = emp.emp_id \n"
		+ "LEFT JOIN teams t ON etm.team_id = t.team_id \n"
		+ "LEFT JOIN projects p ON p.project_id = t.project_id \n"
		+ "LEFT JOIN clients c ON c.client_id = p.client_id \n"
		+ "LEFT JOIN employee e2 ON e2.emp_id = el.level2approver_id \n"
		+ "LEFT JOIN employee e3 ON e3.emp_id = el.level3approver_id \n"
		+ "WHERE el.leave_status_id = 2 \n"
		+ "    AND ( \n"
		+ "        (el.manager_id = :managerId AND el.final_approval_level IS NULL)  \n"
		+ "        OR (el.manager_id = :managerId AND el.final_approval_level = 1) \n"
		+ "        OR (el.level2approver_id = :managerId AND el.final_approval_level = 2) \n"
		+ "        OR (el.level3approver_id = :managerId AND el.final_approval_level = 3) \n"
		+ "    ) \n"
		+ "GROUP BY el.leave_id ORDER BY el.created_on DESC ", nativeQuery = true)
List<Object[]> getAllMyTeamsApprovedLeaveApplicationsByManagerId(Integer managerId);


@Query(nativeQuery = true)
public List<Object[]> getAllTeamLeaveHistoryViewHirarchy(List<Long> empIds, LocalDate fromDate, LocalDate toDate);
		
@Query(value = "WITH RECURSIVE emp_hierarchy AS (" +
            "    SELECT " +
            "        e.emp_id, " +
            "        e.manager_id, " +
            "        e.reporting_manager_id, " +
            "        CAST(e.emp_id AS CHAR(1000)) AS path " +
            "    FROM employee e " +
            "    WHERE e.emp_id = :managerId " +
            "    UNION ALL " +
            "    SELECT " +
            "        e.emp_id, " +
            "        e.manager_id, " +
            "        e.reporting_manager_id, " +
            "        CONCAT(eh.path, ',', e.emp_id) " +
            "    FROM employee e " +
            "    INNER JOIN emp_hierarchy eh " +
            "        ON (e.manager_id = eh.emp_id OR e.reporting_manager_id = eh.emp_id ) " +
            "    WHERE " +
            "         FIND_IN_SET(e.emp_id, eh.path) = 0 " +
            " AND e.employmentstatus not like 'InActive' " +
            ") " +
            "SELECT DISTINCT eh.emp_id " +
            "FROM emp_hierarchy eh", nativeQuery = true)
public List<Long> fetchEmployeeIdsByHirarchy( @Param("managerId") Long managerId);

@Query(nativeQuery = true)
public List<Object[]> getAllMyTeamsPendingLeaveApplicationsByManagerIdInHirarchy(List<Long> empIds,Integer managerId);


@Query(nativeQuery = true)
public List<Object[]> getAllTeamCompOffHistoryViewHirarchy(List<Long> empIds, LocalDate fromDate, LocalDate toDate);

@Query("SELECT el FROM EmployeeLeave el WHERE el.empId = :empId " +
	       "AND el.leaveTypeMasterId = :leaveTypeMasterId " +
	       "AND ((FUNCTION('YEAR', el.fromDate) = :year AND FUNCTION('MONTH', el.fromDate) = :month) " +
	       "OR (FUNCTION('YEAR', el.toDate) = :year AND FUNCTION('MONTH', el.toDate) = :month))")
	List<EmployeeLeave> findByEmpIdAndLeaveTypeAndMonth(
	        @Param("empId") Long empId,
	        @Param("leaveTypeMasterId") Short leaveTypeMasterId,
	        @Param("year") int year,
	        @Param("month") int month);


@Query(
	    "select l " +
	    "from EmployeeLeave l " +
	    "where l.empId = :empId " +
	    "and l.leaveStatusId not in (3, 5) " +
	    "and (l.fromDate = :timesheetDate or l.toDate = :timesheetDate)"
	)
	List<EmployeeLeave> findActiveLeavesByEmpIdAndDate(
	        @Param("empId") Long empId,
	        @Param("timesheetDate") LocalDate timesheetDate);



}


