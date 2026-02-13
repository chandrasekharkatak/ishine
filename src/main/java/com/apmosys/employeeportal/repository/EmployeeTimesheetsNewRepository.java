package com.apmosys.employeeportal.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.EmployeeTimesheetsNewDTO;
import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.Timesheet;

public interface EmployeeTimesheetsNewRepository extends JpaRepository<EmployeeTimesheetsNew, Long> {

	// ========== NEW METHODS FOR HIERARCHICAL STRUCTURE ==========

	@Query(value = "SELECT et.emp_id FROM employee_timesheets_new et WHERE et.date = :date", nativeQuery = true)
	List<Long> findEmpIdsByDate(@Param("date") LocalDate date);

	@Query(value = "SELECT et.emp_id, et.timesheet_id, et.date, dtm.day_type, " +
			"ROUND(CAST(et.total_working_minutes AS DECIMAL(10,2))/60, 2) AS totalTime, " +
			"sm.status, et.description " +
			"FROM employee_timesheets_new et " +
			"INNER JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id " +
			"INNER JOIN status_master_new sm ON et.status = sm.status_id " +
			"WHERE et.date BETWEEN :start AND :end " +
			"ORDER BY et.emp_id, et.date DESC", nativeQuery = true)
	List<Object[]> findAllByDateRangeNative(@Param("start") LocalDate start, @Param("end") LocalDate end);
	/**
	 * Find EmployeeTimesheet by employee ID and date.
	 * Returns new entity type.
	 */
	@Query("SELECT e FROM EmployeeTimesheetsNew e WHERE e.empId = :empId AND e.date = :date")
	java.util.Optional<EmployeeTimesheetsNew> findByEmpIdAndDateNew(@Param("empId") Long empId,
			@Param("date") LocalDate date);

	/**
	 * Find all EmployeeTimesheets by employee ID and date range.
	 * Returns new entity type.
	 */
	@Query("SELECT e FROM EmployeeTimesheetsNew e WHERE e.empId = :empId AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
	List<EmployeeTimesheetsNew> findAllByEmpIdAndDateBetweenOrderByDateDescNew(@Param("empId") Long empId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query(
		    "SELECT new com.apmosys.employeeportal.dto.EmployeeTimesheetsNewDTO(" +
		    " e.timesheetId, " +
		    " e.createdBy, " +
		    " e.createdOn, " +
		    " e.updatedBy, " +
		    " e.isNightShift, " +
		    " e.updatedOn, " +
		    " e.date, " +
		    " e.dayTypeId, " +
		    " e.empId, " +
		    " e.status, " +
		    " e.workCheckIn, " +
		    " e.workCheckOut, " +
		    " e.totalWorkingMinutes, " +
		    " e.leaveTypeMasterId, " +
		    " e.description, " +
		    " e.currentManagerId, " +
		    " d.dayType " +
		    ") " +
		    "FROM EmployeeTimesheetsNew e, DayTypeMasterNew d " +
		    "WHERE e.dayTypeId = d.dayTypeId " +
		    "AND e.empId = :empId " +
		    "AND e.date BETWEEN :startDate AND :endDate " +
		    "ORDER BY e.date DESC"
		)
	List<EmployeeTimesheetsNewDTO>fetchTimesheetDataWithDateType(@Param("empId") Long empId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);
	// ========== OLD METHODS (Backward Compatibility) ==========

	public List<Timesheet> findAllByEmpIdAndDateBetweenOrderByDateDesc(Long empId, LocalDate start, LocalDate end);

	// @Query(nativeQuery = true)
	// public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String
	// status);

	// ========== BACKUP: Original queries renamed with _old suffix ==========
	// @Query(nativeQuery = true)
	// public List<Object[]> getMyReporteesTimesheetRequests_old(Long
	// managerId,String status,LocalDate dateOfJoining,Boolean clientFlag);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getMyReporteesApprovedTimesheetRequests2_old(Long
	// managerId,String status,LocalDate start, LocalDate end,List<Long> empIds);
	//
	// @Query(nativeQuery = true)
	// public Long countMyReporteesTimesheetRequests_old(Long managerId, LocalDate
	// dateOfJoining);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getMyReporteesApprovedTimesheets_old(Long managerId,
	// LocalDate start, LocalDate end);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getLast7DaysTimesheetsByEmpId_old(Long empId,LocalDate
	// date);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getTimesheetsForHomePageByEmpId_old(Long empId,
	// LocalDate start, LocalDate end);

	// ========== UPDATED: New queries using _new tables (using named queries)
	// ==========
	// @Query(nativeQuery = true)
	// public List<Object[]> getMyReporteesTimesheetRequests(Long managerId, String
	// status, LocalDate dateOfJoining,
	// Boolean clientFlag);

	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesApprovedTimesheetRequests2(Long managerId, String status, LocalDate start,
			LocalDate end, List<Long> empIds);

	@Query(nativeQuery = true)
	public Long countMyReporteesTimesheetRequests(Long managerId, LocalDate dateOfJoining);

	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesApprovedTimesheets(Long managerId, LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getLast7DaysTimesheetsByEmpId(Long empId, LocalDate date);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetsForHomePageByEmpId(Long empId, LocalDate start, LocalDate end);

	public Optional<Timesheet> findByEmpIdAndDate(Long empId, LocalDate dateToday);

	List<Timesheet> findByEmpIdAndTimesheetIdIn(Long empId, List<Long> timesheetIds);

	List<Timesheet> findByTimesheetIdIn(List<Long> timesheetIds);

	// ========== BACKUP: Original queries renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "Select emp_Id,date,status "
			+ "from employee_timesheets "
			+ "where emp_Id=:empId and date=:localDate")
	List<Object[]> getTimesheetDataByEmpIdAndDateOLD(Long empId, LocalDate localDate);

	// @Query(nativeQuery = true)
	// public List<Object[]> getAllTimesheetDataOLD();
	//
	 @Query(nativeQuery = true, value ="SELECT e.emp_id,count(*) filled_eod FROM employee_timesheets_new et "+
			" INNER JOIN employee e ON e.emp_id = et.emp_id "+
			 "WHERE date >= :start and date <= :end and e.employmentstatus != 'InActive' "+
			 " group by e.emp_id")
	 public List<Object[]> getFilledTimesheetPerEmployeeCount(LocalDate start,LocalDate end);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getLast9DaysFilledTimesheetReportOLD(LocalDate start,
	// LocalDate end);

	// ========== UPDATED: New queries using _new tables ==========
	@Query(nativeQuery = true, value = "SELECT et.emp_id, et.date, sm.status " +
			"FROM employee_timesheets_new et " +
			"LEFT JOIN status_master_new sm ON et.status = sm.status_id " +
			"WHERE et.emp_id = :empId AND et.date = :localDate")
	List<Object[]> getTimesheetDataByEmpIdAndDate(Long empId, LocalDate localDate);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTimesheetData();

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysPendingTimesheetReport(LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysFilledTimesheetReport(LocalDate start, LocalDate end);

	// ========== BACKUP: Original queries renamed with _old suffix ==========
	// @Query(nativeQuery = true)
	// public List<Object[]> getAllMyTeamTimesheetsOLD(Long createdBy, LocalDate
	// start, LocalDate end);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getAllMyTimesheetsOLD(Long empId, LocalDate start,
	// LocalDate end);
	//
	// @Query(nativeQuery = true)
	// public List<Timesheet> findTimesheetOnLeaveDateOLD(Long empId, String start,
	// String end);

	// ========== UPDATED: New queries using _new tables (using named queries)
	// ==========
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamTimesheets(Long createdBy, LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTimesheets(Long empId, LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Timesheet> findTimesheetOnLeaveDate(Long empId, String start, String end);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT t FROM Timesheet t WHERE t.empId = :empId AND t.date >= :startDate AND t.date <= :endDate")
	List<Timesheet> findTimesheetsForRejectionOLD(@Param("empId") Long empId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	// ========== UPDATED: New query using _new tables (JPQL - using new entity)
	// ==========
	@Query("SELECT t FROM EmployeeTimesheetsNew t WHERE t.empId = :empId AND t.date >= :startDate AND t.date <= :endDate")
	List<EmployeeTimesheetsNew> findTimesheetsForRejection(@Param("empId") Long empId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
	// @Query(nativeQuery = true)
	// public Optional<Timesheet> findExistingTimesheetOnLeaveDate(Long empId,
	// LocalDate fromDate, LocalDate toDate);

	// @Query(nativeQuery = true)
	// public List<Timesheet> findTimesheetOnLeaveDate(Long empId, LocalDate start,
	// LocalDate end);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" t.dayType, " +
			" t.description, " +
			" t.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.commonProperty.createdOn, " +
			" t.commonProperty.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.timesheetStatusUpdatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM Timesheet t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.timesheetStatusUpdatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"WHERE t.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND ((:date IS NULL OR t.date = :date) " +
			"OR (:dateStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m-%d') LIKE CONCAT('%', :dateStr, '%'))) "
			+
			"AND (:dayType IS NULL OR LOWER(t.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(t.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND ((:createdOn IS NULL OR DATE(t.commonProperty.createdOn) = :createdOn) " +
			"OR (:createdOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.commonProperty.createdOn, '%Y-%m-%d') LIKE CONCAT('%', :createdOnStr, '%'))) "
			+
			"AND ((:updatedOn IS NULL OR DATE(t.commonProperty.updatedOn) = :updatedOn) " +
			"OR (:updatedOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.commonProperty.updatedOn, '%Y-%m-%d') LIKE CONCAT('%', :updatedOnStr, '%'))) "
			+
			"AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> findAllLeaveTimesheetsWithoutLeaveApplicationOLD(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("dateStr") String dateStr,
			@Param("createdOnStr") String createdOnStr,
			@Param("updatedOnStr") String updatedOnStr,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" dtm.dayType, " +
			" COALESCE(etam.description, ''), " +
			" sm.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.createdOn, " +
			" t.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.updatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM EmployeeTimesheetsNew t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.updatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"LEFT JOIN DayTypeMasterNew dtm ON dtm.dayTypeId = t.dayTypeId " +
			"LEFT JOIN StatusMasterNew sm ON sm.statusId = t.status " +
			"LEFT JOIN EmployeeTimesheetActivitiesMappingNew etam ON etam.id.timesheetId = t.timesheetId " +
			"WHERE dtm.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND ((:date IS NULL OR t.date = :date) " +
			"OR (:dateStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m-%d') LIKE CONCAT('%', :dateStr, '%'))) "
			+
			"AND (:dayType IS NULL OR LOWER(dtm.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(sm.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND ((:createdOn IS NULL OR DATE(t.createdOn) = :createdOn) " +
			"OR (:createdOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.createdOn, '%Y-%m-%d') LIKE CONCAT('%', :createdOnStr, '%'))) "
			+
			"AND ((:updatedOn IS NULL OR DATE(t.updatedOn) = :updatedOn) " +
			"OR (:updatedOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.updatedOn, '%Y-%m-%d') LIKE CONCAT('%', :updatedOnStr, '%'))) "
			+
			"AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> findAllLeaveTimesheetsWithoutLeaveApplication(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("dateStr") String dateStr,
			@Param("createdOnStr") String createdOnStr,
			@Param("updatedOnStr") String updatedOnStr,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" t.dayType, " +
			" t.description, " +
			" t.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.commonProperty.createdOn, " +
			" t.commonProperty.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.timesheetStatusUpdatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM Timesheet t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.timesheetStatusUpdatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"WHERE t.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND d.deptId IN :deptIds " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND (:date IS NULL OR t.date = :date) " +
			"AND (:dayType IS NULL OR LOWER(t.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(t.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND (:createdOn IS NULL OR DATE(t.commonProperty.createdOn) = :createdOn) " +
			"AND (:updatedOn IS NULL OR DATE(t.commonProperty.updatedOn) = :updatedOn) " +
			"AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWiseOLD(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("deptIds") List<Long> deptIds,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" dtm.dayType, " +
			" COALESCE(etam.description, ''), " +
			" sm.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.createdOn, " +
			" t.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.updatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM EmployeeTimesheetsNew t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.updatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"LEFT JOIN DayTypeMasterNew dtm ON dtm.dayTypeId = t.dayTypeId " +
			"LEFT JOIN StatusMasterNew sm ON sm.statusId = t.status " +
			"LEFT JOIN EmployeeTimesheetActivitiesMappingNew etam ON etam.id.timesheetId = t.timesheetId " +
			"WHERE dtm.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND d.deptId IN :deptIds " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND (:date IS NULL OR t.date = :date) " +
			"AND (:dayType IS NULL OR LOWER(dtm.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(sm.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND (:createdOn IS NULL OR DATE(t.createdOn) = :createdOn) " +
			"AND (:updatedOn IS NULL OR DATE(t.updatedOn) = :updatedOn) " +
			"AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("deptIds") List<Long> deptIds,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" t.dayType, " +
			" t.description, " +
			" t.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.commonProperty.createdOn, " +
			" t.commonProperty.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.timesheetStatusUpdatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM Timesheet t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.timesheetStatusUpdatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"WHERE t.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND d.deptId = :deptId " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND ((:date IS NULL OR t.date = :date) " +
			"OR (:dateStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m-%d') LIKE CONCAT('%', :dateStr, '%'))) "
			+
			"AND (:dayType IS NULL OR LOWER(t.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(t.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND ((:createdOn IS NULL OR DATE(t.commonProperty.createdOn) = :createdOn) " +
			"     OR (:createdOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.commonProperty.createdOn, '%Y-%m-%d') LIKE CONCAT('%', :createdOnStr, '%'))) "
			+
			"AND ((:updatedOn IS NULL OR DATE(t.commonProperty.updatedOn) = :updatedOn) " +
			"     OR (:updatedOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.commonProperty.updatedOn, '%Y-%m-%d') LIKE CONCAT('%', :updatedOnStr, '%'))) "
			+ "AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWiseOLD(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("deptId") Long deptId,
			@Param("dateStr") String dateStr,
			@Param("createdOnStr") String createdOnStr,
			@Param("updatedOnStr") String updatedOnStr,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(" +
			" e.employeementId, " +
			" e.name, " +
			" t.date, " +
			" dtm.dayType, " +
			" COALESCE(etam.description, ''), " +
			" sm.status, " +
			" l.leaveType, " +
			" mgr.name, " +
			" d.name, " +
			" t.createdOn, " +
			" t.updatedOn, " +
			" s.name, " +
			" e.isConsultant, " +
			" e.isApprenticeship, " +
			" e.managerId, " +
			" t.updatedBy, " +
			" t.empId, " +
			" e.isApmosysProduct) " +
			"FROM EmployeeTimesheetsNew t " +
			"JOIN Employee e ON t.empId = e.empId " +
			"LEFT JOIN Employee s ON t.updatedBy = s.empId " +
			"LEFT JOIN Employee mgr ON e.managerId = mgr.empId " +
			"LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"LEFT JOIN Department d ON jr.deptId = d.deptId " +
			"LEFT JOIN LeaveTypeMaster l ON l.leaveTypeMasterId = t.leaveTypeMasterId " +
			"LEFT JOIN DayTypeMasterNew dtm ON dtm.dayTypeId = t.dayTypeId " +
			"LEFT JOIN StatusMasterNew sm ON sm.statusId = t.status " +
			"LEFT JOIN EmployeeTimesheetActivitiesMappingNew etam ON etam.id.timesheetId = t.timesheetId " +
			"WHERE dtm.dayType <> 'Week Off' " +
			"AND t.leaveTypeMasterId IS NULL " +
			"AND d.deptId = :deptId " +
			"AND t.date BETWEEN :start AND :end " +
			"AND (:empName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :empName, '%'))) " +
			"AND ((:empId IS NULL OR e.employeementId = :empId) " +
			"OR (:empIdStr IS NOT NULL AND CAST(e.employeementId AS string) LIKE :empIdStr)) " +
			"AND ((:date IS NULL OR t.date = :date) " +
			"OR (:dateStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.date, '%Y-%m-%d') LIKE CONCAT('%', :dateStr, '%'))) "
			+
			"AND (:dayType IS NULL OR LOWER(dtm.dayType) LIKE LOWER(CONCAT('%', :dayType, '%'))) " +
			"AND (:status IS NULL OR LOWER(sm.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
			"AND (:managerName IS NULL OR LOWER(mgr.name) LIKE LOWER(CONCAT('%', :managerName, '%'))) " +
			"AND (:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
			"AND ((:createdOn IS NULL OR DATE(t.createdOn) = :createdOn) " +
			"OR (:createdOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.createdOn, '%Y-%m-%d') LIKE CONCAT('%', :createdOnStr, '%'))) "
			+
			"AND ((:updatedOn IS NULL OR DATE(t.updatedOn) = :updatedOn) " +
			"OR (:updatedOnStr IS NOT NULL AND FUNCTION('DATE_FORMAT', t.updatedOn, '%Y-%m-%d') LIKE CONCAT('%', :updatedOnStr, '%'))) "
			+
			"AND (:updatedBy IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :updatedBy, '%')))")
	Page<TimesheetDTO> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end,
			@Param("empName") String empName,
			@Param("empId") Long empId,
			@Param("date") LocalDate date,
			@Param("dayType") String dayType,
			@Param("status") String status,
			@Param("managerName") String managerName,
			@Param("departmentName") String departmentName,
			@Param("createdOn") java.sql.Date createdOn,
			@Param("updatedOn") java.sql.Date updatedOn,
			@Param("updatedBy") String updatedBy,
			@Param("deptId") Long deptId,
			@Param("dateStr") String dateStr,
			@Param("createdOnStr") String createdOnStr,
			@Param("updatedOnStr") String updatedOnStr,
			@Param("empIdStr") String empIdStr,
			Pageable pageable);

	// ========== BACKUP: Original queries renamed with _old suffix ==========
	// @Query(nativeQuery = true)
	// public List<Object[]> getInactiveActivitiesByTimesheetIdOLD(Long
	// timesheetId);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getMyTeamsFilledEodCountByManagerIdOLD(LocalDate start,
	// LocalDate end, Long managerId);
	//
	// @Query(nativeQuery = true)
	// public List<Object[]> getTimesheetFilledByMemberOLD(Long empId, LocalDate
	// date);

	// ========== UPDATED: New queries using _new tables (using named queries)
	// ==========
	@Query(nativeQuery = true)
	public List<Object[]> getInactiveActivitiesByTimesheetId(Long timesheetId);

	@Query(nativeQuery = true)
	public List<Object[]> getMyTeamsFilledEodCountByManagerId(LocalDate start, LocalDate end, Long managerId);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetFilledByMember(Long empId, LocalDate date);

	// @Query(value = "SELECT et.emp_id, e.manager_id FROM employee e " +
	// "INNER JOIN employee_timesheets et ON e.emp_id = et.emp_id", nativeQuery =
	// true)
	// List<Object[]> findEmployeesAndTheirManagers();
	//
	// @Modifying
	// @Transactional
	// @Query(value = "UPDATE employee_timesheets SET current_manager_id =
	// :managerId WHERE emp_id = :empId", nativeQuery = true)
	// void updateCurrentManagerId(Long empId, Long managerId);

	// @Query(value = "SELECT DISTINCT emp_id FROM employee_timesheets", nativeQuery
	// = true)
	// List<Long> findDistinctEmpIds();
	//
	// @Query(value = "SELECT * FROM employee_timesheets WHERE emp_id = :empId ORDER
	// BY created_on DESC", nativeQuery = true)
	// List<Timesheet> findTimesheetsByEmpIdOrderByCreatedOn(Long empId);
	//
	// @Modifying
	// @Transactional
	// @Query(value = "UPDATE employee_timesheets SET current_manager_id =
	// :managerId WHERE timesheet_id = :timesheetId", nativeQuery = true)
	// void updateCurrentManagerId(Long timesheetId, Long managerId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT \n"
			+ "    et.timesheet_id,\n"
			+ "    et.date,\n"
			+ "    et.day_type,\n"
			+ "    e.employeement_id,\n"
			+ "    e.name AS employeeName,\n"
			+ "    et.total_time,\n"
			+ "    et.status,\n"
			+ "    em.name AS created_by,\n"
			+ "    et.created_on,\n"
			+ "    et.emp_id,\n"
			+ "    et.remarks,\n"
			+ "    et.office_in_time,\n"
			+ "    et.office_out_time,\n"
			+ "    et.is_night_shift,\n"
			+ "    ltm.leave_type,\n"
			+ "    map.timesheet_id AS activity_timesheet_id,\n"
			+ "    ac.activity,\n"
			+ "    ac.eta,\n"
			+ "    map.description AS activity_description,\n"
			+ "    p.project_name,\n"
			+ "    c.client_name,\n"
			+ "    cl.client_location,\n"
			+ "    t.team_name,\n"
			+ "    e2.name AS manager,\n"
			+ "    ac.activity_id,\n"
			+ "    p.project_id,\n"
			+ "    map.timesheet_activity_map_id,\n"
			+ "    c.client_id,\n"
			+ "    cl.client_location_id,\n"
			+ "    t.team_id,\n"
			+ "    e.is_consultant,\n"
			+ "    e.is_apprenticeship,\n"
			+ "    d.name As departmentName,e2.emp_id AS managerEmpId,e.emp_id AS employeeEmpId\n"
			+ "FROM employee_timesheets et\n"
			+ "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee em ON et.created_by = em.emp_id\n"
			+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id\n"
			+ "LEFT JOIN employee_timesheet_activities_mapping map ON map.timesheet_id = et.timesheet_id\n"
			+ "LEFT JOIN activities ac ON ac.activity_id = map.activity_id\n"
			+ "LEFT JOIN teams t ON t.team_id = ac.team_id\n"
			+ "LEFT JOIN projects p ON p.project_id = t.project_id\n"
			+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "LEFT JOIN client_locations cl ON cl.client_location_id = map.client_location_id\n"
			+ "LEFT JOIN employee e2 ON e2.emp_id = e.manager_id\n"
			+ "WHERE et.date BETWEEN :startDate AND :endDate\n "
			+ "ORDER BY et.created_on DESC", nativeQuery = true)
	List<Object[]> getAllEmployeeTimesheetsBetweenDatesOLD(@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT \n"
			+ "    et.timesheet_id,\n"
			+ "    et.date,\n"
			+ "    dtm.day_type,\n"
			+ "    e.employeement_id,\n"
			+ "    e.name AS employeeName,\n"
			+ "    ROUND(et.total_activities_minutes / 60, 2) AS total_time,\n"
			+ "    sm.status,\n"
			+ "    em.name AS created_by,\n"
			+ "    et.created_on,\n"
			+ "    et.emp_id,\n"
			+ "    etam.description AS activity_description,\n"
			+ "    et.office_in_time,\n"
			+ "    et.office_out_time,\n"
			+ "    pts.is_night_shift,\n"
			+ "    ltm.leave_type,\n"
			+ "    etam.timesheet_id AS activity_timesheet_id,\n"
			+ "    ac.activity,\n"
			+ "    ac.eta,\n"
			+ "    p.project_name,\n"
			+ "    c.client_name,\n"
			+ "    cl.client_location,\n"
			+ "    t.team_name,\n"
			+ "    e2.name AS manager,\n"
			+ "    ac.activity_id,\n"
			+ "    pts.project_id,\n"
			+ "    c.client_id,\n"
			+ "    cl.client_location_id,\n"
			+ "    t.team_id,\n"
			+ "    e.is_consultant,\n"
			+ "    e.is_apprenticeship,\n"
			+ "    d.name As departmentName,e2.emp_id AS managerEmpId,e.emp_id AS employeeEmpId\n"
			+ "FROM employee_timesheets_new et\n"
			+ "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee em ON et.created_by = em.emp_id\n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id\n"
			+ "LEFT JOIN employee_timesheet_activities_mapping_new etam ON etam.timesheet_id = et.timesheet_id\n"
			+ "LEFT JOIN activities ac ON ac.activity_id = etam.activity_id\n"
			+ "LEFT JOIN teams t ON t.team_id = ac.team_id\n"
			+ "LEFT JOIN projects p ON p.project_id = t.project_id\n"
			+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "LEFT JOIN client_locations cl ON cl.client_location_id = etam.client_location_id\n"
			+ "LEFT JOIN employee e2 ON e2.emp_id = e.manager_id\n"
			+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "WHERE et.date BETWEEN :startDate AND :endDate\n "
			+ "ORDER BY et.created_on DESC", nativeQuery = true)
	List<Object[]> getAllEmployeeTimesheetsBetweenDates(@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT \n"
			+ "    et.timesheet_id,\n"
			+ "    et.date,\n"
			+ "    et.day_type,\n"
			+ "    e.employeement_id,\n"
			+ "    e.name AS employeeName,\n"
			+ "    et.total_time,\n"
			+ "    et.status,\n"
			+ "    em.name AS created_by,\n"
			+ "    et.created_on,\n"
			+ "    et.emp_id,\n"
			+ "    et.remarks,\n"
			+ "    et.office_in_time,\n"
			+ "    et.office_out_time,\n"
			+ "    et.is_night_shift,\n"
			+ "    ltm.leave_type,\n"
			+ "    map.timesheet_id AS activity_timesheet_id,\n"
			+ "    ac.activity,\n"
			+ "    ac.eta,\n"
			+ "    map.description AS activity_description,\n"
			+ "    p.project_name,\n"
			+ "    c.client_name,\n"
			+ "    cl.client_location,\n"
			+ "    t.team_name,\n"
			+ "    e2.name AS manager,\n"
			+ "    ac.activity_id,\n"
			+ "    p.project_id,\n"
			+ "    map.timesheet_activity_map_id,\n"
			+ "    c.client_id,\n"
			+ "    cl.client_location_id,\n"
			+ "    t.team_id,\n"
			+ "    e.is_consultant,\n"
			+ "    e.is_apprenticeship,\n"
			+ "    d.name As departmentName,e2.emp_id AS managerEmpId,e.emp_id AS employeeEmpId\n"
			+ "FROM employee_timesheets et\n"
			+ "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee em ON et.created_by = em.emp_id\n"
			+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id\n"
			+ "LEFT JOIN employee_timesheet_activities_mapping map ON map.timesheet_id = et.timesheet_id\n"
			+ "LEFT JOIN activities ac ON ac.activity_id = map.activity_id\n"
			+ "LEFT JOIN teams t ON t.team_id = ac.team_id\n"
			+ "LEFT JOIN projects p ON p.project_id = t.project_id\n"
			+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "LEFT JOIN client_locations cl ON cl.client_location_id = map.client_location_id\n"
			+ "LEFT JOIN employee e2 ON e2.emp_id = e.manager_id\n"
			+ "where d.dept_id = :deptId AND et.date BETWEEN :startDate AND :endDate\n"
			+ "ORDER BY et.created_on DESC ", nativeQuery = true)
	List<Object[]> getTimesheetsByDepartmentAndDateRangeOLD(@Param("deptId") Long deptId,
			@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT \n"
			+ "    et.timesheet_id,\n"
			+ "    et.date,\n"
			+ "    dtm.day_type,\n"
			+ "    e.employeement_id,\n"
			+ "    e.name AS employeeName,\n"
			+ "    ROUND(et.total_activities_minutes / 60, 2) AS total_time,\n"
			+ "    sm.status,\n"
			+ "    em.name AS created_by,\n"
			+ "    et.created_on,\n"
			+ "    et.emp_id,\n"
			+ "    etam.description AS activity_description,\n"
			+ "    et.office_in_time,\n"
			+ "    et.office_out_time,\n"
			+ "    pts.is_night_shift,\n"
			+ "    ltm.leave_type,\n"
			+ "    etam.timesheet_id AS activity_timesheet_id,\n"
			+ "    ac.activity,\n"
			+ "    ac.eta,\n"
			+ "    p.project_name,\n"
			+ "    c.client_name,\n"
			+ "    cl.client_location,\n"
			+ "    t.team_name,\n"
			+ "    e2.name AS manager,\n"
			+ "    ac.activity_id,\n"
			+ "    pts.project_id,\n"
			+ "    c.client_id,\n"
			+ "    cl.client_location_id,\n"
			+ "    t.team_id,\n"
			+ "    e.is_consultant,\n"
			+ "    e.is_apprenticeship,\n"
			+ "    d.name As departmentName,e2.emp_id AS managerEmpId,e.emp_id AS employeeEmpId\n"
			+ "FROM employee_timesheets_new et\n"
			+ "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee em ON et.created_by = em.emp_id\n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "LEFT JOIN leave_type_master ltm ON ltm.leave_type_master_id = et.leave_type_master_id\n"
			+ "LEFT JOIN employee_timesheet_activities_mapping_new etam ON etam.timesheet_id = et.timesheet_id\n"
			+ "LEFT JOIN activities ac ON ac.activity_id = etam.activity_id\n"
			+ "LEFT JOIN teams t ON t.team_id = ac.team_id\n"
			+ "LEFT JOIN projects p ON p.project_id = t.project_id\n"
			+ "LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "LEFT JOIN client_locations cl ON cl.client_location_id = etam.client_location_id\n"
			+ "LEFT JOIN employee e2 ON e2.emp_id = e.manager_id\n"
			+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "where d.dept_id = :deptId AND et.date BETWEEN :startDate AND :endDate\n"
			+ "ORDER BY et.created_on DESC ", nativeQuery = true)
	List<Object[]> getTimesheetsByDepartmentAndDateRange(@Param("deptId") Long deptId,
			@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT " +
			"ets.timesheet_id, " +
			"ets.date AS timesheet_date, " +
			"ets.day_type, " +
			"ets.office_in_time, " +
			"ets.office_out_time, " +
			"ets.total_working_hours, " +
			"ets.description AS timesheet_description, " +
			"etam.completion_time, " +
			"etam.description AS activity_description, " +
			"a.activity_id, " +
			"a.activity, " +
			"t.team_id, " +
			"t.team_name, " +
			"t.team_lead_name, " +
			"p.project_id, " +
			"p.project_name, " +
			"p.client_id, " +
			"cl.client_location_id, " +
			"cl.client_location AS project_location, " +
			"c.client_name " +
			"FROM employee_timesheets ets " +
			"JOIN ( " +
			"   SELECT timesheet_id " +
			"   FROM employee_timesheets " +
			"   WHERE emp_id = :empId " +
			"     AND day_type NOT IN ('Public Holiday', 'Week Off', 'Leave') " +
			"   ORDER BY date DESC " +
			"   LIMIT 1 " +
			") latest_ts ON ets.timesheet_id = latest_ts.timesheet_id " +
			"JOIN employee_timesheet_activities_mapping etam ON ets.timesheet_id = etam.timesheet_id " +
			"JOIN activities a ON etam.activity_id = a.activity_id " +
			"JOIN teams t ON a.team_id = t.team_id " +
			"JOIN projects p ON t.project_id = p.project_id " +
			"LEFT JOIN client_locations cl ON cl.client_id = p.client_id " +
			"LEFT JOIN clients c ON p.client_id = c.client_id", nativeQuery = true)
	List<Object[]> getLastFilledTimesheetOLD(@Param("empId") Long empId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT " +
			"ets.timesheet_id, " +
			"ets.date AS timesheet_date, " +
			"dtm.day_type, " +
			"ets.office_in_time, " +
			"ets.office_out_time, " +
			"TIME_FORMAT(SEC_TO_TIME(ets.total_working_minutes * 60), '%H:%i') AS total_working_hours, " +
			"CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time, " +
			"etam.description AS activity_description, " +
			"a.activity_id, " +
			"a.activity, " +
			"t.team_id, " +
			"t.team_name, " +
			"t.team_lead_name, " +
			"pts.project_id, " +
			"p.project_name, " +
			"p.client_id, " +
			"cl.client_location_id, " +
			"cl.client_location AS project_location, " +
			"c.client_name " +
			"FROM employee_timesheets_new ets " +
			"JOIN ( " +
			"   SELECT et.timesheet_id " +
			"   FROM employee_timesheets_new et " +
			"   LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id " +
			"   WHERE et.emp_id = :empId " +
			"     AND dtm.day_type NOT IN ('Public Holiday', 'Week Off', 'Leave') " +
			"   ORDER BY et.date DESC " +
			"   LIMIT 1 " +
			") latest_ts ON ets.timesheet_id = latest_ts.timesheet_id " +
			"JOIN employee_timesheet_activities_mapping_new etam ON ets.timesheet_id = etam.timesheet_id " +
			"JOIN activities a ON etam.activity_id = a.activity_id " +
			"JOIN teams t ON a.team_id = t.team_id " +
			"JOIN projects p ON t.project_id = p.project_id " +
			"LEFT JOIN day_type_master_new dtm ON ets.day_type_id = dtm.day_type_id " +
			"LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = ets.timesheet_id AND pts.project_id = etam.project_id "
			+
			"LEFT JOIN client_locations cl ON cl.client_id = p.client_id " +
			"LEFT JOIN clients c ON p.client_id = c.client_id", nativeQuery = true)
	List<Object[]> getLastFilledTimesheet(@Param("empId") Long empId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT DISTINCT et.emp_id, etm.active, etm.team_id, et.date " +
			"FROM ( " +
			"    SELECT emp_id, MAX(date) AS max_date " +
			"    FROM employee_timesheets " +
			"    WHERE day_type = 'Working' " +
			"    GROUP BY emp_id " +
			") latest " +
			"INNER JOIN employee_timesheets et " +
			"    ON et.emp_id = latest.emp_id AND et.date = latest.max_date " +
			"INNER JOIN employee_timesheet_activities_mapping etam " +
			"    ON etam.timesheet_id = et.timesheet_id " +
			"INNER JOIN activities a " +
			"    ON a.activity_id = etam.activity_id " +
			"INNER JOIN ( " +
			"    SELECT emp_id, team_id, active FROM ( " +
			"        SELECT emp_id, team_id, active, " +
			"               ROW_NUMBER() OVER (PARTITION BY emp_id, team_id ORDER BY active DESC) AS rn " +
			"        FROM employee_team_mapping " +
			"    ) ranked " +
			"    WHERE rn = 1 " +
			") etm " +
			"    ON etm.team_id = a.team_id AND etm.emp_id = et.emp_id " +
			"WHERE et.emp_id = :empId", nativeQuery = true)
	List<Object[]> checkEmployeeActiveOrNotOLD(@Param("empId") Long empId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT DISTINCT et.emp_id, etm.active, etm.team_id, et.date " +
			"FROM ( " +
			"    SELECT et2.emp_id, MAX(et2.date) AS max_date " +
			"    FROM employee_timesheets_new et2 " +
			"    LEFT JOIN day_type_master_new dtm ON et2.day_type_id = dtm.day_type_id " +
			"    WHERE dtm.day_type = 'Working' " +
			"    GROUP BY et2.emp_id " +
			") latest " +
			"INNER JOIN employee_timesheets_new et " +
			"    ON et.emp_id = latest.emp_id AND et.date = latest.max_date " +
			"INNER JOIN employee_timesheet_activities_mapping_new etam " +
			"    ON etam.timesheet_id = et.timesheet_id " +
			"INNER JOIN activities a " +
			"    ON a.activity_id = etam.activity_id " +
			"INNER JOIN ( " +
			"    SELECT emp_id, team_id, active FROM ( " +
			"        SELECT emp_id, team_id, active, " +
			"               ROW_NUMBER() OVER (PARTITION BY emp_id, team_id ORDER BY active DESC) AS rn " +
			"        FROM employee_team_mapping " +
			"    ) ranked " +
			"    WHERE rn = 1 " +
			") etm " +
			"    ON etm.team_id = a.team_id AND etm.emp_id = et.emp_id " +
			"WHERE et.emp_id = :empId", nativeQuery = true)
	List<Object[]> checkEmployeeActiveOrNot(@Param("empId") Long empId);

	// added by sakti for duplicate timesheet check
	// Optional<Timesheet> findByEmpIdAndDate(Long empId, Date date);

	@Query(value = "select new com.apmosys.employeeportal.dto.ProjectDTO(p.projectId, p.projectName )  \n" +
			"from Project p  \n" +
			"inner join Team t on t.projectId = p.projectId \n" +
			"inner join EmployeeTeamMap etm on etm.teamId = t.teamId \n" +
			"where etm.empId = :empId and etm.active = 1 and t.isActive = 'Y' and p.active = 'true'")
	public List<ProjectDTO> getActiveProjectsByEmpId(Long empId);

	@Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END \n"
			+ "FROM Project p \n"
			+ "INNER JOIN Team t ON t.projectId = p.projectId \n"
			+ "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
			+ "WHERE etm.empId = :empId AND etm.active = 1 AND t.isActive = 'Y' \n"
			+ "AND p.active = 'true' AND p.poProjectType = 'TNM'")
	public Boolean isInTNMProject(Long empId);

	@Query(value = "select new com.apmosys.employeeportal.dto.ProjectClientSideIdDTO(p.projectId, p.projectName, ecsm.clientSideId )  \n"
			+
			"from Project p  \n" +
			"inner join Team t on t.projectId = p.projectId \n" +
			"inner join EmployeeTeamMap etm on etm.teamId = t.teamId \n" +
			"left join EmployeeClientSideIdMapping ecsm on ecsm.projectId = p.projectId AND ecsm.active = TRUE AND ecsm.empId = etm.empId \n"
			+
			"where etm.empId = :empId and etm.active = 1 and p.active = 'true' and t.isActive = 'Y'")
	public List<ProjectClientSideIdDTO> getActiveProjectsAndClientSideIdByEmpId(Long empId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS DISTINCT e.name, p.project_name, t.team_name,   \n"
			+ "CASE WHEN po_project_type IS NOT NULL THEN po_project_type   \n"
			+ "ELSE internal_project_type END AS project_type,   \n"
			+ "et.date, day_type, et.total_time,   \n"
			+ "GROUP_CONCAT(distinct a.activity ORDER BY a.activity SEPARATOR ',') AS activity,  \n"
			+ "GROUP_CONCAT(distinct etam.description ORDER BY etam.description SEPARATOR ',') AS description,  \n"
			+ "GROUP_CONCAT(DISTINCT pm.name ORDER BY pm.name SEPARATOR ',') AS Project_Manager_name,\n"
			+ "et.timesheet_id, et.status,  \n"
			+ "client_in_time, client_out_time,  \n"
			+ "CASE WHEN final_flag = 0 THEN doc_id END AS doc_id_1,  \n"
			+ "CASE WHEN final_flag = 1 THEN doc_id END AS doc_id_2  \n"
			+ "FROM employee_timesheets et   \n"
			+ "LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id   \n"
			+ "LEFT JOIN activities a ON a.activity_id = etam.activity_id   \n"
			+ "LEFT JOIN teams t ON a.team_id = t.team_id  AND t.is_active != 'N' \n"
			+ "LEFT JOIN projects p ON t.project_id = p.project_id  AND p.active = 'true' \n"
			+ "LEFT JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id and pmm.active = 1\n"
			+ "LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id   \n"
			+ "LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id  AND etm.active != 0        \n"
			+ "LEFT JOIN employee e ON et.emp_id = e.emp_id   \n"
			+ "LEFT JOIN timesheet_document_details tdd ON tdd.emp_id = et.emp_id \n"
			+ "and tdd.timesheet_id = et.timesheet_id\n"
			+ "WHERE e.employmentstatus != 'InActive'   \n"
			+ "AND et.emp_id = :empId\n"
			// + " AND p.has_client_side_id = 1\n"
			+ "AND (et.date BETWEEN :fromDate AND :toDate)  \n"
			+ "GROUP BY e.name, p.project_name, t.team_name, et.date, day_type, et.total_time,   \n"
			+ "et.timesheet_id, et.status, client_in_time, client_out_time \n"
			+ "ORDER BY et.date asc \n"
			+ "  LIMIT :offset, :pageSize", nativeQuery = true)
	List<Object[]> findByEmpIdAndDateBetweenOLD(@Param("empId") Long empId,
			@Param("fromDate") String fromDate,
			@Param("toDate") String toDate,
			@Param("offset") int offset,
			@Param("pageSize") int pageSize);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT SQL_CALC_FOUND_ROWS DISTINCT e.name, p.project_name, t.team_name,   \n"
			+ "CASE WHEN po_project_type IS NOT NULL THEN po_project_type   \n"
			+ "ELSE internal_project_type END AS project_type,   \n"
			+ "et.date, dtm.day_type, ROUND(et.total_activities_minutes / 60, 2) AS total_time,   \n"
			+ "GROUP_CONCAT(distinct a.activity ORDER BY a.activity SEPARATOR ',') AS activity,  \n"
			+ "GROUP_CONCAT(distinct etam.description ORDER BY etam.description SEPARATOR ',') AS description,  \n"
			+ "GROUP_CONCAT(DISTINCT pm.name ORDER BY pm.name SEPARATOR ',') AS Project_Manager_name,\n"
			+ "et.timesheet_id, sm.status,  \n"
			+ "pts.client_in_time, pts.client_out_time,  \n"
			+ "CASE WHEN tdd.final_flag = 0 THEN tdd.doc_id END AS doc_id_1,  \n"
			+ "CASE WHEN tdd.final_flag = 1 THEN tdd.doc_id END AS doc_id_2  \n"
			+ "FROM employee_timesheets_new et   \n"
			+ "LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id   \n"
			+ "LEFT JOIN activities a ON a.activity_id = etam.activity_id   \n"
			+ "LEFT JOIN teams t ON a.team_id = t.team_id  AND t.is_active != 'N' \n"
			+ "LEFT JOIN projects p ON t.project_id = p.project_id  AND p.active = 'true' \n"
			+ "LEFT JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id and pmm.active = 1\n"
			+ "LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id   \n"
			+ "LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id  AND etm.active != 0        \n"
			+ "LEFT JOIN employee e ON et.emp_id = e.emp_id   \n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id   \n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id   \n"
			+ "LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id   \n"
			+ "LEFT JOIN timesheet_document_details_new tdd ON tdd.emp_id = et.emp_id AND tdd.timesheet_id = et.timesheet_id   \n"
			+ "WHERE e.employmentstatus != 'InActive'   \n"
			+ "AND et.emp_id = :empId\n"
			// + " AND p.has_client_side_id = 1\n"
			+ "AND (et.date BETWEEN :fromDate AND :toDate)  \n"
			+ "GROUP BY e.name, p.project_name, t.team_name, et.date, dtm.day_type, ROUND(et.total_activities_minutes / 60, 2),   \n"
			+ "et.timesheet_id, sm.status, pts.client_in_time, pts.client_out_time \n"
			+ "ORDER BY et.date asc \n"
			+ "  LIMIT :offset, :pageSize", nativeQuery = true)
	List<Object[]> findByEmpIdAndDateBetween(@Param("empId") Long empId,
			@Param("fromDate") String fromDate,
			@Param("toDate") String toDate,
			@Param("offset") int offset,
			@Param("pageSize") int pageSize);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "WITH\n"
			+ "    Employees_With_ClientID AS (\n"
			+ "        SELECT DISTINCT ecsm.emp_id\n"
			+ "        FROM employee_client_side_id_mapping_new ecsm\n"
			+ "        WHERE ecsm.client_side_id IS NOT NULL AND ecsm.client_side_id != '' AND ecsm.active = 1\n"
			+ "    ),\n"
			+ "    Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (\n"
			+ "                SELECT 1 FROM employee u\n"
			+ "                inner JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                inner JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            inner JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    )\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT auth.emp_id)\n"
			+ "FROM\n"
			+ "    Authorized_Employees auth\n"
			+ "INNER JOIN\n"
			+ "    Employees_With_ClientID crit ON auth.emp_id = crit.emp_id", nativeQuery = true)
	public List<Object[]> getTotalVmsFilledCountOLD(@Param("emp_id") Long emp_id);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "WITH\n"
			+ "    Employees_With_ClientID AS (\n"
			+ "        SELECT DISTINCT ecsm.emp_id\n"
			+ "        FROM employee_client_side_id_mapping_new ecsm\n"
			+ "        WHERE ecsm.client_side_id IS NOT NULL AND ecsm.client_side_id != '' AND ecsm.active = 1\n"
			+ "    ),\n"
			+ "    Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (\n"
			+ "                SELECT 1 FROM employee u\n"
			+ "                inner JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                inner JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            inner JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    )\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT auth.emp_id)\n"
			+ "FROM\n"
			+ "    Authorized_Employees auth\n"
			+ "INNER JOIN\n"
			+ "    Employees_With_ClientID crit ON auth.emp_id = crit.emp_id", nativeQuery = true)
	public List<Object[]> getTotalVmsFilledCount(@Param("emp_id") Long emp_id);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT COUNT(DISTINCT emp_id) AS employee_count_with_client_id " +
			"FROM employee_timesheets " +
			"WHERE MONTH(created_on) = MONTH(CURRENT_DATE()) " +
			"AND YEAR(created_on) = YEAR(CURRENT_DATE()) " +
			"AND day_type = 'Working'", nativeQuery = true)
	public List<Object[]> totalIshineFilledCountOLD();

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT COUNT(DISTINCT et.emp_id) AS employee_count_with_client_id " +
			"FROM employee_timesheets_new et " +
			"LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id " +
			"WHERE MONTH(et.created_on) = MONTH(CURRENT_DATE()) " +
			"AND YEAR(et.created_on) = YEAR(CURRENT_DATE()) " +
			"AND dtm.day_type = 'Working'", nativeQuery = true)
	public List<Object[]> totalIshineFilledCount();

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "WITH\n"
			+ " Employees_Without_ClientID AS (\n"
			+ "       select distinct e.emp_id from employee e inner join employee_timesheets et\n"
			+ "				on e.emp_id = et.emp_id \n"
			+ "				where e.emp_id not in\n"
			+ "				(\n"
			+ "				SELECT DISTINCT emp_id AS employee_count_with_client_id\n"
			+ "				FROM employee_timesheets\n"
			+ "				WHERE client_side_id IS NOT NULL AND client_side_id != ''\n"
			+ "				)\n"
			+ "				and client_side_id IS NOT NULL AND client_side_id != ''\n"
			+ "                ),\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (\n"
			+ "                SELECT 1 FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT auth.emp_id)\n"
			+ "FROM\n"
			+ "    Authorized_Employees auth\n"
			+ "INNER JOIN\n"
			+ "    Employees_Without_ClientID crit ON auth.emp_id = crit.emp_id", nativeQuery = true)
	public List<Object[]> totalvmsNotFilledOLD(@Param("emp_id") Long emp_id);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "WITH\n"
			+ " Employees_Without_ClientID AS (\n"
			+ "       select distinct e.emp_id from employee e inner join employee_timesheets_new et\n"
			+ "				on e.emp_id = et.emp_id \n"
			+ "				where e.emp_id not in\n"
			+ "				(\n"
			+ "				SELECT DISTINCT ecsm.emp_id\n"
			+ "				FROM employee_client_side_id_mapping_new ecsm\n"
			+ "				WHERE ecsm.client_side_id IS NOT NULL AND ecsm.client_side_id != '' AND ecsm.active = 1\n"
			+ "				)\n"
			+ "				AND EXISTS (\n"
			+ "				    SELECT 1 FROM employee_client_side_id_mapping_new ecsm2\n"
			+ "				    WHERE ecsm2.emp_id = e.emp_id AND ecsm2.client_side_id IS NOT NULL AND ecsm2.client_side_id != '' AND ecsm2.active = 1\n"
			+ "				)\n"
			+ "                ),\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (\n"
			+ "                SELECT 1 FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "\n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT auth.emp_id)\n"
			+ "FROM\n"
			+ "    Authorized_Employees auth\n"
			+ "INNER JOIN\n"
			+ "    Employees_Without_ClientID crit ON auth.emp_id = crit.emp_id", nativeQuery = true)
	public List<Object[]> totalvmsNotFilled(@Param("emp_id") Long emp_id);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "\n"
			+ "WITH RECURSIVE\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id \n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (  \n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                INNER JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    ),\n"
			+ "\n"
			+ " Date_Generator (dt) AS (\n"
			+ "        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT COUNT(*) AS expected_fill_count\n"
			+ "        FROM Date_Generator\n"
			+ "        WHERE dt NOT IN (\n"
			+ "            SELECT date_of_holiday\n"
			+ "            FROM holiday\n"
			+ "            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
			+ "              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT etm.emp_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        WHERE etm.active != 0\n"
			+ "          AND t.is_active != 'N'\n"
			+ "          AND p.active != 'false'\n"
			+ "          AND p.has_client_side_id = true\n"
			+ "          AND e.employmentstatus != 'InActive'\n"
			+ "          AND date(etm.start_date) < curdate()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            emp_id,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'PENDING' THEN 1 END) AS Client_pending_count,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'APPROVED' THEN 1 END) AS Client_Approved_count,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'REJECTED' THEN 1 END) AS Client_Rejected_count\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "         WHERE MONTH(tdd.created_on) = MONTH(CURRENT_DATE()) AND YEAR(tdd.created_on) = YEAR(CURRENT_DATE())\n"
			+ "        GROUP BY tdd.emp_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Final_Counts AS (\n"
			+ "        SELECT\n"
			+ "            SUM(IFNULL(ds.Client_Approved_count, 0)) as totalApproved,\n"
			+ "            SUM(IFNULL(ds.Client_pending_count, 0)) as totalPending,\n"
			+ "            SUM(IFNULL(ds.Client_Rejected_count, 0)) as totalRejected,\n"
			+ "            (SELECT COUNT(*) FROM Base_Project_Employees) as totalUniqueEmployees\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    fc.totalApproved AS totalClientSideApprovedCount,\n"
			+ "    fc.totalPending AS totalClientSidePendingCount,\n"
			+ "    ( (fc.totalUniqueEmployees * wds.expected_fill_count) - (fc.totalApproved + fc.totalPending) ) AS eod_not_filled,\n"
			+ "    (fc.totalApproved + fc.totalPending + fc.totalRejected) as totalSubmitted,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalApproved * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_approved_percentage,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalRejected * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_rejected_percentage\n"
			+ "FROM\n"
			+ "    Final_Counts fc,\n"
			+ "    WorkingDays_Summary wds", nativeQuery = true)
	public List<Object[]> totalIshineNotFilledCountOLD(@Param("emp_id") Long emp_id);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "\n"
			+ "WITH RECURSIVE\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id \n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (  \n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                INNER JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    ),\n"
			+ "\n"
			+ " Date_Generator (dt) AS (\n"
			+ "        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT COUNT(*) AS expected_fill_count\n"
			+ "        FROM Date_Generator\n"
			+ "        WHERE dt NOT IN (\n"
			+ "            SELECT date_of_holiday\n"
			+ "            FROM holiday\n"
			+ "            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
			+ "              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT etm.emp_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        WHERE etm.active != 0\n"
			+ "          AND t.is_active != 'N'\n"
			+ "          AND p.active != 'false'\n"
			+ "          AND p.has_client_side_id = true\n"
			+ "          AND e.employmentstatus != 'InActive'\n"
			+ "          AND date(etm.start_date) < curdate()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            tdd.emp_id,\n"
			+ "            COUNT(CASE WHEN csm.status = 'pending' THEN 1 END) AS Client_pending_count,\n"
			+ "            COUNT(CASE WHEN csm.status = 'approved' THEN 1 END) AS Client_Approved_count,\n"
			+ "            COUNT(CASE WHEN csm.status = 'rejected' THEN 1 END) AS Client_Rejected_count\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status = csm.status_id\n"
			+ "         WHERE MONTH(tdd.created_on) = MONTH(CURRENT_DATE()) AND YEAR(tdd.created_on) = YEAR(CURRENT_DATE())\n"
			+ "        GROUP BY tdd.emp_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Final_Counts AS (\n"
			+ "        SELECT\n"
			+ "            SUM(IFNULL(ds.Client_Approved_count, 0)) as totalApproved,\n"
			+ "            SUM(IFNULL(ds.Client_pending_count, 0)) as totalPending,\n"
			+ "            SUM(IFNULL(ds.Client_Rejected_count, 0)) as totalRejected,\n"
			+ "            (SELECT COUNT(*) FROM Base_Project_Employees) as totalUniqueEmployees\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    fc.totalApproved AS totalClientSideApprovedCount,\n"
			+ "    fc.totalPending AS totalClientSidePendingCount,\n"
			+ "    ( (fc.totalUniqueEmployees * wds.expected_fill_count) - (fc.totalApproved + fc.totalPending) ) AS eod_not_filled,\n"
			+ "    (fc.totalApproved + fc.totalPending + fc.totalRejected) as totalSubmitted,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalApproved * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_approved_percentage,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalRejected * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_rejected_percentage\n"
			+ "FROM\n"
			+ "    Final_Counts fc,\n"
			+ "    WorkingDays_Summary wds", nativeQuery = true)
	public List<Object[]> totalIshineNotFilledCount(@Param("emp_id") Long emp_id);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT et.date FROM Timesheet et " +
			"INNER JOIN TimesheetDocumentDetails tdd ON et.timesheetId = tdd.timesheetId " +
			"WHERE et.empId = :empId " +
			"AND et.projectId = :projectId " +
			"AND et.clientSideId IS NOT NULL " +
			"AND tdd.active IS TRUE " +
			"AND tdd.finalFlag IS TRUE " +
			"AND (tdd.rmApprovalStatus = 'Approved' OR tdd.rmApprovalStatus = 'Pending' OR tdd.hrApprovalStatus != 'Rejected')")
	Set<LocalDate> findDatesByEmpIdAndProjectIdOLD(@Param("empId") Long empId,
			@Param("projectId") Integer projectId);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT et.date FROM EmployeeTimesheetsNew et " +
			"INNER JOIN TimesheetDocumentDetailsNew tdd ON et.timesheetId = tdd.timesheetId " +
			"WHERE et.empId = :empId " +
			"AND EXISTS (SELECT 1 FROM ProjectTimesheetStatusNew pts WHERE pts.id.timesheetId = et.timesheetId AND pts.id.projectId = :projectId) "
			+
			"AND EXISTS (SELECT 1 FROM EmployeeClientSideIdMapping ecsm WHERE ecsm.empId = et.empId AND ecsm.projectId = :projectId AND ecsm.active = 1) "
			+
			"AND tdd.active IS TRUE " +
			"AND tdd.finalFlag IS TRUE " +
			"AND EXISTS (SELECT 1 FROM ClientStatusMasterNew csm WHERE csm.statusId = tdd.clientApprovalStatusId AND (csm.status = 'Approved' OR csm.status = 'Pending')) "
			+
			"AND NOT EXISTS (SELECT 1 FROM ClientStatusMasterNew csm2 WHERE csm2.statusId = tdd.clientApprovalStatusId AND csm2.status = 'Rejected')")
	Set<LocalDate> findDatesByEmpIdAndProjectId(@Param("empId") Long empId,
			@Param("projectId") Integer projectId);

	// @Query(value=" WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),
	// DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
	// + " CASE\n"
	// + " WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
	// + " IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(),
	// LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
	// + " ELSE CURDATE()\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " \n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + " \n"
	// + " Project_Managers AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name
	// SEPARATOR ', ') AS project_manager_name\n"
	// + " FROM project_manager_mapping pm\n"
	// + " left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " \n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " WHERE p.has_client_side_id = TRUE\n"
	// + " ),\n"
	// + " \n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = 3
	// AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts',
	// 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = 3))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = 3)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = 3)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = 3 OR etm_inner.team_lead_id = 3))\n"
	// + " )),\n"
	// + " \n"
	// + " Base_Project_Employees AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// etm.employee_team_map_id,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
	// + " s.name AS spoc, tl.name AS teamLead,\n"
	// + " e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
	// + " CASE\n"
	// + " WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id)\n"
	// + " ELSE CONCAT('A-',e.employeement_id)\n"
	// + " END AS employement_id\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN clients c ON c.client_id = p.client_id\n"
	// + " INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.has_client_side_id = 1\n"
	// + " AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Timesheet_Base_Data AS (\n"
	// + " SELECT DISTINCT\n"
	// + " et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,\n"
	// + " et.date, et.day_type, et.client_in_time, et.client_out_time,
	// et.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
	// + " LEFT JOIN teams t ON a.team_id = t.team_id\n"
	// + " LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and
	// etm.team_id = t.team_id\n"
	// + " WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT
	// to_date FROM Date_Parameters)\n"
	// + " ),\n"
	// + " \n"
	// + " Employee_Document_Summary_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
	// + " DATE(tbd.date) AS timesheet_date,\n"
	// + " csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
	// + " tbd.shadow_emp_id, tdd.timesheet_id\n"
	// + " FROM timesheet_document_details tdd\n"
	// + " INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id
	// and tbd.emp_id = tdd.emp_id\n"
	// + " WHERE tdd.active = TRUE\n"
	// + " ),\n"
	// + " Expected_Client_Side_Base_DSR AS (\n"
	// + " SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id,
	// adir.dt\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(bpe.start_date) AND
	// COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM employee_timesheets et1 WHERE et1.emp_id = bpe.emp_id AND
	// adir.dt = et1.date\n"
	// + " AND UPPER(et1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY',
	// 'WEEK OFF')\n"
	// + " )\n"
	// + " ),\n"
	// + " \n"
	// + " Actual_Client_Side_Submissions AS (\n"
	// + " SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date
	// AS dt\n"
	// + " FROM Employee_Document_Summary_Details\n"
	// + " WHERE (UPPER(client_approval_status) = 'APPROVED' OR
	// UPPER(client_approval_status) = 'PENDING')\n"
	// + " AND timesheet_date < CURDATE()\n"
	// + " ),\n"
	// + " \n"
	// + " Combined_Expected_Client_Side_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Expected_Client_Side_Base_DSR\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Actual_Client_Side_Submissions\n"
	// + " ),\n"
	// + " \n"
	// + " WorkingDays_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT
	// dt) AS expected_fill_count\n"
	// + " FROM Combined_Expected_Client_Side_DSR\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + " \n"
	// + " Employee_Document_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND
	// final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND
	// NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE timesheet_id =
	// edsd.timesheet_id AND UPPER(client_approval_status) = 'APPROVED') THEN
	// timesheet_id END) AS pending_days\n"
	// + " FROM Employee_Document_Summary_Details edsd\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + " \n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT distinct\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " adir.dt AS timesheet_date,\n"
	// + " pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
	// + " CASE\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A' \n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
	// + " AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id !=
	// ts_data_relevant.employee_team_map_id)\n"
	// + " ) THEN 'O'\n"
	// + " WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
	// + " WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
	// + " WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN employee_timesheets global_ts ON bpe.emp_id =
	// global_ts.emp_id\n"
	// + " AND adir.dt = global_ts.date\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id =
	// ts_data_relevant.emp_id\n"
	// + " AND adir.dt = ts_data_relevant.date\n"
	// + " AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id =
	// ts_data_all_employee.emp_id\n"
	// + " AND adir.dt = ts_data_all_employee.date\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_approved ON
	// ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
	// + " AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
	// + " AND doc_approved.final_flag = 1\n"
	// + " AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_pending ON
	// ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
	// + " AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
	// + " AND NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE
	// timesheet_id = doc_pending.timesheet_id AND UPPER(client_approval_status) =
	// 'APPROVED')\n"
	// + " AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Calculated_Status AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS
	// client_side_not_filled_count,\n"
	// + " COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN
	// 'Defaulter'\n"
	// + " WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0,
	// COALESCE(wds.expected_fill_count, 0) - \n"
	// + " (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1
	// THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id =
	// wds.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id =
	// eds.employee_team_map_id\n"
	// + " )\n"
	// + " SELECT SQL_CALC_FOUND_ROWS distinct\n"
	// + " bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name,
	// bpe.team_id,\n"
	// + " CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN
	// CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS
	// name,\n"
	// + " bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,
	// bpe.project_id,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,
	// bpe.reporting_manager_id,\n"
	// + " MONTHNAME(dp.from_date) AS month_name,\n"
	// + " ecs.expectedTimesheetFillCount,\n"
	// + " ecs.client_side_not_filled_count,\n"
	// + " ecs.clientSidePendingCount,\n"
	// + " ecs.clientSideApprovedCount,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS
	// `1_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS
	// `2_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS
	// `3_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS
	// `4_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS
	// `5_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS
	// `6_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS
	// `7_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS
	// `8_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS
	// `9_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS
	// `10_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS
	// `11_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS
	// `12_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS
	// `13_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS
	// `14_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS
	// `15_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS
	// `16_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS
	// `17_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS
	// `18_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS
	// `19_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS
	// `20_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS
	// `21_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS
	// `22_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS
	// `23_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS
	// `24_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS
	// `25_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS
	// `26_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS
	// `27_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS
	// `28_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS
	// `29_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS
	// `30_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS
	// `31_client_out_time`,\n"
	// + " bpe.employement_id\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " JOIN Date_Parameters dp ON 1=1\n"
	// + " LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + " LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
	// + " LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
	// + " inner join emp_primary_project_mapping eppm on bpe.project_id =
	// eppm.primary_project_id and bpe.emp_id = eppm.emp_id and eppm.is_mapped =
	// 'Y'\n"
	// + " WHERE bpe.emp_id in (:emp_id)\n"
	// + " GROUP BY\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
	// + " bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date,
	// bpe.end_date,\n"
	// + " bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
	// + " ecs.expectedTimesheetFillCount, ecs.client_side_not_filled_count,
	// ecs.clientSidePendingCount, ecs.clientSideApprovedCount " , nativeQuery =
	// true)
	// public List<Object[]> getEmployeeTimesheetAsCalender(
	// @Param("emp_id") Integer empId,
	// @Param("month") Integer month,
	// @Param("year") Integer year);

	// @Query(value="WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// date(etm.end_date) as end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " inner join employee e on etm.emp_id = e.emp_id\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = 3
	// AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts',
	// 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = 3))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = 3)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = 3)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = 3 OR etm_inner.team_lead_id = 3))\n"
	// + " )),\n"
	// + " Authorized_Project_IDs AS (\n"
	// + " SELECT DISTINCT p.project_id FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
	// + " ),\n"
	// + " Employee_Timesheets_With_Activities AS (\n"
	// + " SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status,
	// et.office_in_time, et.office_out_time,\n"
	// + " a.team_id AS activity_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " ),\n"
	// + " Base_Report_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// e.billable,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, p.po_no,\n"
	// + " s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
	// + " e.reporting_manager_id, ecsm.client_side_id,\n"
	// + " CASE WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS
	// employement_id,\n"
	// + " d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email,
	// e.employmentstatus, p.active as projectActive\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
	// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Project_Managers_Aggregated AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name
	// SEPARATOR ', ') AS Project_Manager_Names\n"
	// + " FROM project_manager_mapping pm\n"
	// + " LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
	// + " etwa_team.office_in_time, etwa_team.office_out_time,
	// brd.employee_team_map_id,\n"
	// + " CASE\n"
	// + " WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
	// + " WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id =
	// brd.team_id THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Approved' THEN 'AP'\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Pending' THEN 'PE'\n"
	// + " WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS
	// NULL THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id =
	// brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND
	// o.activity_team_id != brd.team_id\n"
	// + " ) THEN 'O'\n"
	// + " WHEN adir.dt < brd.start_date THEN 'O'\n"
	// + " WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM
	// Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date
	// = adir.dt) THEN 'A' \n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
	// + " ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND
	// brd.team_id = etwa_team.activity_team_id\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
	// + " ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
	// + " AND etwa_general.activity_team_id IS NULL\n"
	// + " ),\n"
	// + " Expected_Working_Days_Detail AS (\n"
	// + " SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS
	// expected_working_day_date, brd.employee_team_map_id\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(brd.start_date) AND
	// COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
	// + " WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
	// + " AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type)
	// LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Timesheet_Filled AS (\n"
	// + " SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS
	// dt, brd.employee_team_map_id\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND
	// etwa.activity_team_id = brd.team_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS
	// dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id
	// FROM Actual_Timesheet_Filled\n"
	// + " ),\n"
	// + " Expected_Ishine_Working_Days AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, employee_team_map_id,
	// COUNT(DISTINCT dt) AS expected_ishine_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Ishine_Timesheet_Summary AS (\n"
	// + " SELECT distinct etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS
	// filled_ishine_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_pending_Days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_approved_Days\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
	// + " GROUP BY etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Calculated_Status AS (\n"
	// + " SELECT distinct\n"
	// + " brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) AS client_side_not_filled_count,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) >= 2 THEN 'Defaulter'\n"
	// + " WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0,
	// COALESCE(eiwd.expected_ishine_days, 0)\n"
	// + " - (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " )\n"
	// + " SELECT SQL_CALC_FOUND_ROWS distinct\n"
	// + " brd.emp_id, brd.client_side_id, brd.start_date, brd.team_name,
	// brd.team_id,\n"
	// + " brd.name, brd.spoc, brd.billable_type, brd.employee_role, brd.dept_name,
	// brd.project_id,\n"
	// + " brd.project_name, pma.Project_Manager_Names, brd.po_no,
	// brd.client_name,\n"
	// + " brd.reporting_manager_id,\n"
	// + " (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS
	// expected_ishine_timesheet_days,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days,
	// 0)) ) AS not_filled_ishine_timesheet_days,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS
	// `1_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS
	// `2_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS
	// `3_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS
	// `4_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS
	// `5_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS
	// `6_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS
	// `7_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS
	// `8_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS
	// `9_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS
	// `10_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS
	// `11_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS
	// `12_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS
	// `13_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS
	// `14_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS
	// `15_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS
	// `16_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS
	// `17_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS
	// `18_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS
	// `19_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS
	// `20_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS
	// `21_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS
	// `22_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS
	// `23_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS
	// `24_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS
	// `25_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS
	// `26_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS
	// `27_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS
	// `28_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS
	// `29_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS
	// `30_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS
	// `31_office_out_time`,\n"
	// + " brd.employement_id\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id =
	// pma.project_id\n"
	// + " LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + " inner join emp_primary_project_mapping eppm on brd.project_id =
	// eppm.primary_project_id and brd.emp_id = eppm.emp_id and eppm.is_mapped =
	// 'Y'\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " AND brd.emp_id in (:emp_id)\n"
	// + " GROUP BY\n"
	// + " brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id,
	// brd.name,\n"
	// + " pma.Project_Manager_Names, expected_ishine_timesheet_days,
	// not_filled_ishine_timesheet_days,\n"
	// + " ishine_pending_Days, ishine_approved_Days,brd.employement_id" ,
	// nativeQuery = true)
	// public List<Object[]> getEmployeeTimesheetAsCalenderForAllEmp(
	// @Param("emp_id") Integer empId,
	// @Param("month") Integer month,
	// @Param("year") Integer year);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "SELECT et.timesheet_id,et.date,et.day_type,e.name employeeName,et.description ,et.status, \n"
			+ "em.name created_by,em.emp_id as createdById,et.created_on,e.employeement_id,et.total_time , e.email,et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, et.current_manager_id,e.is_consultant,e.is_apprenticeship,e.emp_id, \n"
			+ "et.client_in_time, et.client_out_time, et.client_side_id, et.total_client_working_hours,\n"
			+ "et.project_id, et.client_approval_status, et.has_client_side_id, et.is_shadow_timesheet , et.shadow_emp_id\n"
			+ "        FROM employee_timesheets et\n"
			+ "        INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
			+ "        INNER JOIN teams t ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN projects p ON p.project_id = t.project_id\n"
			+ "        INNER join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
			+ "        INNER join activities a on etam.activity_id = a.activity_id and a.team_id = t.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "        INNER JOIN employee em ON  et.created_by = em.emp_id\n"
			+ "        WHERE day_type LIKE '%Working%'\n"
			+ "        and et.status = 'Pending'\n"
			+ "         and et.emp_id = :empId and t.team_id = :teamId")
	public List<Object[]> getPendingTimesheetsByEmpAndTeamOLD(Long empId, Long teamId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "SELECT et.timesheet_id,et.date,dtm.day_type,e.name employeeName,etam.description ,sm.status, \n"
			+ "em.name created_by,em.emp_id as createdById,et.created_on,e.employeement_id,ROUND(et.total_activities_minutes / 60, 2) AS total_time , e.email,et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, pts.is_night_shift, e.manager_id AS current_manager_id,e.is_consultant,e.is_apprenticeship,e.emp_id, \n"
			+ "pts.client_in_time, pts.client_out_time, ecsm.client_side_id, CAST(pts.total_client_working_minutes AS DECIMAL(10,2))/60 AS total_client_working_hours,\n"
			+ "pts.project_id, csm.status AS client_approval_status, CASE WHEN ecsm.client_side_id IS NOT NULL THEN 1 ELSE 0 END AS has_client_side_id, CASE WHEN pts.shadow_emp_id IS NOT NULL THEN 1 ELSE 0 END AS is_shadow_timesheet , pts.shadow_emp_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
			+ "        INNER JOIN teams t ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN projects p ON p.project_id = t.project_id\n"
			+ "        INNER join employee_timesheet_activities_mapping_new etam on et.timesheet_id = etam.timesheet_id\n"
			+ "        INNER join activities a on etam.activity_id = a.activity_id and a.team_id = t.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "        INNER JOIN employee em ON  et.created_by = em.emp_id\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN client_status_master_new csm ON pts.client_approval_status = csm.status_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON ecsm.emp_id = et.emp_id AND ecsm.project_id = pts.project_id AND ecsm.active = 1\n"
			+ "        WHERE dtm.day_type LIKE '%Working%'\n"
			+ "        and sm.status = 'Pending'\n"
			+ "         and et.emp_id = :empId and t.team_id = :teamId")
	public List<Object[]> getPendingTimesheetsByEmpAndTeam(Long empId, Long teamId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "SELECT DISTINCT et.timesheet_id,et.date,et.day_type,e.name employeeName,et.description ,et.status,\n"
			+ "				 				em.name created_by,em.emp_id as createdById,et.created_on,e.employeement_id,et.total_time , e.email,et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, et.current_manager_id,e.is_consultant,e.is_apprenticeship,e.emp_id,\n"
			+ "				 			et.client_in_time, et.client_out_time, et.client_side_id, et.total_client_working_hours, \n"
			+ "				 				et.project_id, et.client_approval_status, et.has_client_side_id, et.is_shadow_timesheet , et.shadow_emp_id, e.is_apmosys_product \n"
			+ "				 			       FROM employee_timesheets et\n"
			+ "				 				        INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
			+ "				 				       INNER JOIN teams t ON t.team_id = etm.team_id\n"
			+ "				 				        INNER JOIN projects p ON p.project_id = t.project_id\n"
			+ "				 				        INNER join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
			+ "				 				        INNER join activities a on etam.activity_id = a.activity_id and a.team_id = t.team_id\n"
			+ "				 				        INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "				 				       INNER JOIN employee em ON  et.created_by = em.emp_id\n"
			+ "				 				        WHERE day_type LIKE '%Working%'\n"
			+ "				 				        and et.status = 'Pending'\n"
			+ "				 				        and et.emp_id = :empId  and t.team_id = :teamId \n"
			+ "                                        and et.date between \n"
			+ "                                        COALESCE(NULLIF(:fromDate, ''), DATE_FORMAT(CURDATE(), '%Y-%m-01')) and\n"
			+ "										COALESCE(NULLIF(:toDate, ''), CURDATE())\n"
			+ "	                                       AND ( \n"
			+ "	                                             (:clientFlag IS NULL) \n"
			+ "	                                          OR (:clientFlag = TRUE  AND et.has_client_side_id = 1) \n"
			+ "	                                          OR (:clientFlag = FALSE AND (et.has_client_side_id = 0 OR et.has_client_side_id IS NULL)) \n"
			+ "	                                         ) \n"
			+ "                                        Order by et.date desc")
	public List<Object[]> getMyTimesheetRequestsOLD(Long empId, Long teamId, String fromDate, String toDate,
			Boolean clientFlag);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "SELECT DISTINCT et.timesheet_id,et.date,dtm.day_type,e.name employeeName,etam.description ,sm.status,\n"
			+ "				 				em.name created_by,em.emp_id as createdById,et.created_on,e.employeement_id,ROUND(et.total_activities_minutes / 60, 2) AS total_time , e.email,et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, pts.is_night_shift, e.manager_id AS current_manager_id,e.is_consultant,e.is_apprenticeship,e.emp_id,\n"
			+ "				 			pts.client_in_time, pts.client_out_time, ecsm.client_side_id, CAST(pts.total_client_working_minutes AS DECIMAL(10,2))/60 AS total_client_working_hours, \n"
			+ "				 				pts.project_id, csm.status AS client_approval_status, CASE WHEN ecsm.client_side_id IS NOT NULL THEN 1 ELSE 0 END AS has_client_side_id, CASE WHEN pts.shadow_emp_id IS NOT NULL THEN 1 ELSE 0 END AS is_shadow_timesheet , pts.shadow_emp_id, e.is_apmosys_product \n"
			+ "				 			       FROM employee_timesheets_new et\n"
			+ "				 				        INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
			+ "				 				       INNER JOIN teams t ON t.team_id = etm.team_id\n"
			+ "				 				        INNER JOIN projects p ON p.project_id = t.project_id\n"
			+ "				 				        INNER join employee_timesheet_activities_mapping_new etam on et.timesheet_id = etam.timesheet_id\n"
			+ "				 				        INNER join activities a on etam.activity_id = a.activity_id and a.team_id = t.team_id\n"
			+ "				 				        INNER JOIN employee e ON e.emp_id = et.emp_id\n"
			+ "				 				       INNER JOIN employee em ON  et.created_by = em.emp_id\n"
			+ "				 				        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "				 				        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "				 				        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "				 				        LEFT JOIN client_status_master_new csm ON pts.client_approval_status = csm.status_id\n"
			+ "				 				        LEFT JOIN employee_client_side_id_mapping_new ecsm ON ecsm.emp_id = et.emp_id AND ecsm.project_id = pts.project_id AND ecsm.active = 1\n"
			+ "				 				        WHERE dtm.day_type LIKE '%Working%'\n"
			+ "				 				        and sm.status = 'Pending'\n"
			+ "				 				        and et.emp_id = :empId  and t.team_id = :teamId \n"
			+ "                                        and et.date between \n"
			+ "                                        COALESCE(NULLIF(:fromDate, ''), DATE_FORMAT(CURDATE(), '%Y-%m-01')) and\n"
			+ "										COALESCE(NULLIF(:toDate, ''), CURDATE())\n"
			+ "	                                       AND ( \n"
			+ "	                                             (:clientFlag IS NULL) \n"
			+ "	                                          OR (:clientFlag = TRUE  AND ecsm.client_side_id IS NOT NULL) \n"
			+ "	                                          OR (:clientFlag = FALSE AND ecsm.client_side_id IS NULL) \n"
			+ "	                                         ) \n"
			+ "                                        Order by et.date desc")
	public List<Object[]> getMyTimesheetRequests(Long empId, Long teamId, String fromDate, String toDate,
			Boolean clientFlag);

	// @Query(value= " WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),
	// DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
	// + " CASE\n"
	// + " WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
	// + " IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(),
	// LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
	// + " ELSE CURDATE()\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " \n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + "\n"
	// + " Project_Managers AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name
	// SEPARATOR ', ') AS project_manager_name\n"
	// + " FROM project_manager_mapping pm\n"
	// + " left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " \n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,
	// \n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm \n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " WHERE p.has_client_side_id = TRUE\n"
	// + " ),\n"
	// + "\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + "\n"
	// + " Base_Project_Employees AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// etm.employee_team_map_id,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
	// + " s.name AS spoc, tl.name AS teamLead, \n"
	// + " e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
	// + " CASE\n"
	// + " WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id)\n"
	// + " ELSE CONCAT('A-',e.employeement_id)\n"
	// + " END AS employement_id\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN clients c ON c.client_id = p.client_id\n"
	// + " INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.has_client_side_id = 1\n"
	// + " AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Timesheet_Base_Data AS (\n"
	// + " SELECT DISTINCT\n"
	// + " et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,\n"
	// + " et.date, et.day_type, et.client_in_time, et.client_out_time,
	// et.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
	// + " LEFT JOIN teams t ON a.team_id = t.team_id\n"
	// + " LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and
	// etm.team_id = t.team_id\n"
	// + " WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT
	// to_date FROM Date_Parameters)\n"
	// + " -- AND et.date BETWEEN DATE(etm.start_date) AND
	// COALESCE(date(etm.end_date), '2099-12-31')\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
	// + " DATE(tbd.date) AS timesheet_date,\n"
	// + " csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
	// + " tbd.shadow_emp_id, tdd.timesheet_id\n"
	// + " FROM timesheet_document_details tdd\n"
	// + " INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id
	// and tbd.emp_id = tdd.emp_id\n"
	// + " WHERE tdd.active = TRUE\n"
	// + " ),\n"
	// + " Expected_Client_Side_Base_DSR AS (\n"
	// + " SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id,
	// adir.dt\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(bpe.start_date) AND
	// COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM employee_timesheets et1 WHERE et1.emp_id = bpe.emp_id AND
	// adir.dt = et1.date\n"
	// + " AND UPPER(et1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY',
	// 'WEEK OFF')\n"
	// + " )\n"
	// + " ),\n"
	// + " \n"
	// + " Actual_Client_Side_Submissions AS (\n"
	// + " SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date
	// AS dt\n"
	// + " FROM Employee_Document_Summary_Details\n"
	// + " WHERE (UPPER(client_approval_status) = 'APPROVED' OR
	// UPPER(client_approval_status) = 'PENDING')\n"
	// + " AND timesheet_date < CURDATE()\n"
	// + " ),\n"
	// + " \n"
	// + " Combined_Expected_Client_Side_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Expected_Client_Side_Base_DSR\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Actual_Client_Side_Submissions\n"
	// + " ),\n"
	// + " \n"
	// + " WorkingDays_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT
	// dt) AS expected_fill_count\n"
	// + " FROM Combined_Expected_Client_Side_DSR\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND
	// final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND
	// NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE timesheet_id =
	// edsd.timesheet_id AND UPPER(client_approval_status) = 'APPROVED') THEN
	// timesheet_id END) AS pending_days\n"
	// + " FROM Employee_Document_Summary_Details edsd\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + "\n"
	// + "Daily_Status_Details AS (\n"
	// + " SELECT distinct\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " adir.dt AS timesheet_date,\n"
	// + " pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
	// + " CASE\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A' \n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
	// + " AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id !=
	// ts_data_relevant.employee_team_map_id)\n"
	// + " ) THEN 'O'\n"
	// + " WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
	// + " WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
	// + " WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN employee_timesheets global_ts ON bpe.emp_id =
	// global_ts.emp_id\n"
	// + " AND adir.dt = global_ts.date\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id =
	// ts_data_relevant.emp_id\n"
	// + " AND adir.dt = ts_data_relevant.date\n"
	// + " AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id =
	// ts_data_all_employee.emp_id\n"
	// + " AND adir.dt = ts_data_all_employee.date\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_approved ON
	// ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
	// + " AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
	// + " AND doc_approved.final_flag = 1\n"
	// + " AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_pending ON
	// ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
	// + " AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
	// + " AND NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE
	// timesheet_id = doc_pending.timesheet_id AND UPPER(client_approval_status) =
	// 'APPROVED')\n"
	// + " AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
	// + ")\n"
	// + "SELECT distinct \n"
	// + " bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name,
	// bpe.team_id,\n"
	// + " CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN
	// CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS
	// name,\n"
	// + " bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,
	// bpe.project_id,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,
	// bpe.reporting_manager_id,\n"
	// + " MONTHNAME(dp.from_date) AS month_name,\n"
	// + " COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS
	// client_side_not_filled_count,\n"
	// + " COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS
	// `1_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS
	// `2_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS
	// `3_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS
	// `4_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS
	// `5_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS
	// `6_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS
	// `7_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS
	// `8_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS
	// `9_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS
	// `10_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS
	// `11_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS
	// `12_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS
	// `13_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS
	// `14_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS
	// `15_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS
	// `16_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS
	// `17_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS
	// `18_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS
	// `19_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS
	// `20_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS
	// `21_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS
	// `22_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS
	// `23_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS
	// `24_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS
	// `25_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS
	// `26_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS
	// `27_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS
	// `28_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS
	// `29_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS
	// `30_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS
	// `31_client_out_time`,\n"
	// + " bpe.employement_id,\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('CA', 'CN', 'P') THEN 1 ELSE 0 END) AS
	// 'Present',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS
	// 'Holiday',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
	// + " 0 AS 'Comp_Off',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS
	// 'NA_Count',\n"
	// + " 0 AS 'Half_Day',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN
	// ('CA','CN','P','WO','AH','CH','L','A','O') THEN 1 ELSE 0 END) AS
	// total_days,\n"
	// + " (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)) as
	// client_filled_Days,\n"
	// + " bpe.employmentstatus,bpe.end_date, \n"
	// + " SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS
	// 'Ready_for_invoicing',\n"
	// + " bpe.active\n"
	// + "FROM Base_Project_Employees bpe\n"
	// + "JOIN Date_Parameters dp ON 1=1\n"
	// + "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + "LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id =
	// wds.employee_team_map_id\n"
	// + "LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id =
	// eds.employee_team_map_id\n"
	// + "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
	// + "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
	// + "where bpe.project_id in (:project_id)\n"
	// + "GROUP BY\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
	// + " bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date,
	// bpe.end_date,\n"
	// + " bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
	// + " expectedTimesheetFillCount, client_side_not_filled_count,
	// clientSidePendingCount, clientSideApprovedCount\n"
	// + "ORDER BY\n"
	// + " bpe.project_name, name ", nativeQuery = true)
	// public List<Object[]> getEmployeeTimesheetAsCalenderByProjectId(
	// @Param("project_id") Integer projectId,
	// @Param("month") Integer month,
	// @Param("year") Integer year,
	// @Param("emp_id") Long empId);

	// @Query(value="WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,
	// \n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm \n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + " Authorized_Project_IDs AS (\n"
	// + " SELECT DISTINCT p.project_id FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
	// + " ),\n"
	// + " Employee_Timesheets_With_Activities AS (\n"
	// + " SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status,
	// et.office_in_time, et.office_out_time,\n"
	// + " a.team_id AS activity_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " ),\n"
	// + " Base_Report_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// e.billable,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, p.po_no,\n"
	// + " s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
	// + " e.reporting_manager_id, ecsm.client_side_id,\n"
	// + " CASE WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS
	// employement_id,\n"
	// + " d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email,
	// e.employmentstatus, p.active as projectActive\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
	// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Project_Managers_Aggregated AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name
	// SEPARATOR ', ') AS Project_Manager_Names\n"
	// + " FROM project_manager_mapping pm\n"
	// + " LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
	// + " etwa_team.office_in_time, etwa_team.office_out_time,
	// brd.employee_team_map_id,\n"
	// + " CASE\n"
	// + " WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA' \n"
	// + " WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id =
	// brd.team_id THEN\n"
	// + " CASE \n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Approved' THEN 'AP'\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Pending' THEN 'PE'\n"
	// + " WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
	// + " ELSE 'NA' \n"
	// + " END\n"
	// + " WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS
	// NULL THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id =
	// brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND
	// o.activity_team_id != brd.team_id\n"
	// + " ) THEN 'O' \n"
	// + " WHEN adir.dt < brd.start_date THEN 'O' \n"
	// + " WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM
	// Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date
	// = adir.dt) THEN 'A' -- Absent / Not filled\n"
	// + " ELSE 'NA' \n"
	// + " END AS daily_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_team \n"
	// + " ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND
	// brd.team_id = etwa_team.activity_team_id\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
	// + " ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date \n"
	// + " AND etwa_general.activity_team_id IS NULL \n"
	// + " ), \n"
	// + " Expected_Working_Days_Detail AS (\n"
	// + " SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS
	// expected_working_day_date, brd.employee_team_map_id\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(brd.start_date) AND
	// COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
	// + " WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
	// + " AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type)
	// LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Timesheet_Filled AS (\n"
	// + " SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS
	// dt, brd.employee_team_map_id\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND
	// etwa.activity_team_id = brd.team_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS
	// dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id
	// FROM Actual_Timesheet_Filled\n"
	// + " ),\n"
	// + " Expected_Ishine_Working_Days AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, employee_team_map_id,
	// COUNT(DISTINCT dt) AS expected_ishine_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Ishine_Timesheet_Summary AS (\n"
	// + " SELECT distinct etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS
	// filled_ishine_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_pending_Days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_approved_Days\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
	// + " GROUP BY etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id\n"
	// + " )\n"
	// + " SELECT distinct\n"
	// + "
	// brd.emp_id,brd.client_side_id,brd.start_date,brd.team_name,brd.team_id,\n"
	// + " brd.name,brd.spoc, brd.billable_type,brd.employee_role, brd.dept_name,
	// brd.project_id,\n"
	// + " brd.project_name,pma.Project_Manager_Names,brd.po_no,brd.client_name,\n"
	// + " brd.reporting_manager_id,\n"
	// + " (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS
	// expected_ishine_timesheet_days,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days,
	// 0)) ) AS not_filled_ishine_timesheet_days,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS
	// `1_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS
	// `2_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS
	// `3_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS
	// `4_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS
	// `5_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS
	// `6_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS
	// `7_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS
	// `8_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS
	// `9_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS
	// `10_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS
	// `11_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS
	// `12_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS
	// `13_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS
	// `14_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS
	// `15_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS
	// `16_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS
	// `17_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS
	// `18_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS
	// `19_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS
	// `20_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS
	// `21_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS
	// `22_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS
	// `23_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS
	// `24_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS
	// `25_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS
	// `26_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS
	// `27_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS
	// `28_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS
	// `29_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS
	// `30_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS
	// `31_office_out_time`,\n"
	// + " brd.employement_id,\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END)
	// AS 'Present',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS
	// 'Holiday',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
	// + " 0 AS 'Comp_Off',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS
	// 'NA_Count',\n"
	// + " 0 AS 'Half_Day',\n"
	// + " (SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END)
	// + SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) + SUM(CASE WHEN
	// dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) + SUM(CASE WHEN
	// dsd.daily_status = 'L' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN
	// ('A','O','NA') THEN 1 ELSE 0 END)) AS total_days,\n"
	// + " (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0)) as ishine_filled_days,\n"
	// + " brd.employmentstatus, brd.end_date,\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS
	// 'Ready_for_invoicing',\n"
	// + " brd.active\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id =
	// pma.project_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " and brd.project_id in (:project_id)\n"
	// + " GROUP BY\n"
	// + " brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id,
	// brd.name,\n"
	// + " pma.Project_Manager_Names, expected_ishine_timesheet_days,
	// not_filled_ishine_timesheet_days,\n"
	// + " ishine_pending_Days,
	// ishine_approved_Days,brd.employement_id,brd.employmentstatus,
	// brd.end_date,brd.active\n"
	// + " ORDER BY\n"
	// + " brd.name" , nativeQuery = true)
	// public List<Object[]> getEmployeeTimesheetAsCalenderByProjectIdForAllEmp(
	// @Param("project_id") Integer projectId,
	// @Param("month") Integer month,
	// @Param("year") Integer year,
	// @Param("emp_id") Long emp_id);

	// @Query(value="WITH recursive\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),
	// DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
	// + " CASE\n"
	// + " WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
	// + " IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(),
	// LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
	// + " ELSE CURDATE()\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + "\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + "\n"
	// + " Project_Managers AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name
	// SEPARATOR ', ') AS project_manager_name\n"
	// + " FROM project_manager_mapping pm\n"
	// + " left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + "\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " WHERE p.has_client_side_id = TRUE\n"
	// + " ),\n"
	// + "\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + "\n"
	// + " Base_Project_Employees AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// etm.employee_team_map_id,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
	// + " s.name AS spoc, tl.name AS teamLead,\n"
	// + " e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
	// + " CASE\n"
	// + " WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id)\n"
	// + " ELSE CONCAT('A-',e.employeement_id)\n"
	// + " END AS employement_id\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN clients c ON c.client_id = p.client_id\n"
	// + " INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.has_client_side_id = true\n"
	// + " AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Timesheet_Base_Data AS (\n"
	// + " SELECT DISTINCT\n"
	// + " et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,\n"
	// + " et.date, et.day_type, et.client_in_time, et.client_out_time,
	// et.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
	// + " LEFT JOIN teams t ON a.team_id = t.team_id\n"
	// + " LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and
	// etm.team_id = t.team_id\n"
	// + " WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT
	// to_date FROM Date_Parameters)\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
	// + " DATE(tbd.date) AS timesheet_date,\n"
	// + " csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
	// + " tbd.shadow_emp_id, tdd.timesheet_id\n"
	// + " FROM timesheet_document_details tdd\n"
	// + " INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id
	// and tbd.emp_id = tdd.emp_id\n"
	// + " WHERE tdd.active = TRUE\n"
	// + " ),\n"
	// + " Expected_Client_Side_Base_DSR AS (\n"
	// + " SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id,
	// adir.dt\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(bpe.start_date) AND
	// COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM employee_timesheets et1 WHERE et1.emp_id = bpe.emp_id AND
	// adir.dt = et1.date\n"
	// + " AND UPPER(et1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY',
	// 'WEEK OFF')\n"
	// + " )\n"
	// + " ),\n"
	// + "\n"
	// + " Actual_Client_Side_Submissions AS (\n"
	// + " SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date
	// AS dt\n"
	// + " FROM Employee_Document_Summary_Details\n"
	// + " WHERE (UPPER(client_approval_status) = 'APPROVED' OR
	// UPPER(client_approval_status) = 'PENDING')\n"
	// + " AND timesheet_date < CURDATE()\n"
	// + " ),\n"
	// + "\n"
	// + " Combined_Expected_Client_Side_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Expected_Client_Side_Base_DSR\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Actual_Client_Side_Submissions\n"
	// + " ),\n"
	// + "\n"
	// + " WorkingDays_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT
	// dt) AS expected_fill_count\n"
	// + " FROM Combined_Expected_Client_Side_DSR\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND
	// final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND
	// NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE timesheet_id =
	// edsd.timesheet_id AND UPPER(client_approval_status) = 'APPROVED') THEN
	// timesheet_id END) AS pending_days\n"
	// + " FROM Employee_Document_Summary_Details edsd\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT distinct\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " adir.dt AS timesheet_date,\n"
	// + " pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
	// + " CASE\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (adir.dt <= bpe.end_date OR bpe.end_date IS NULL) THEN 'A'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
	// + " AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id !=
	// ts_data_relevant.employee_team_map_id)\n"
	// + " ) THEN 'O'\n"
	// + " WHEN adir.dt < DATE(bpe.start_date) THEN 'O'\n"
	// + " WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
	// + " WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
	// + " WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN employee_timesheets global_ts ON bpe.emp_id =
	// global_ts.emp_id\n"
	// + " AND adir.dt = global_ts.date\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id =
	// ts_data_relevant.emp_id\n"
	// + " AND adir.dt = ts_data_relevant.date\n"
	// + " AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id =
	// ts_data_all_employee.emp_id\n"
	// + " AND adir.dt = ts_data_all_employee.date\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_approved ON
	// ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
	// + " AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
	// + " AND doc_approved.final_flag = 1\n"
	// + " AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_pending ON
	// ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
	// + " AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
	// + " AND NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE
	// timesheet_id = doc_pending.timesheet_id AND UPPER(client_approval_status) =
	// 'APPROVED')\n"
	// + " AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
	// + "),\n"
	// + "Employee_Calculated_Status AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS
	// client_side_not_filled_count,\n"
	// + " COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN
	// 'Defaulter'\n"
	// + " WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0,
	// COALESCE(wds.expected_fill_count, 0) \n"
	// + " - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1
	// THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id =
	// wds.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id =
	// eds.employee_team_map_id\n"
	// + "),\n"
	// + "final_select as \n"
	// + "(\n"
	// + " SELECT distinct COUNT(DISTINCT ecs.emp_id) AS
	// total_no_of_applicable_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Approved' THEN ecs.emp_id
	// END) AS total_approved_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Pending' THEN ecs.emp_id
	// END) AS total_pending_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Defaulter' THEN
	// ecs.emp_id END) AS total_defaulter_employees\n"
	// + "FROM Base_Project_Employees bpe\n"
	// + "JOIN Date_Parameters dp ON 1=1\n"
	// + "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + "LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
	// + "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
	// + " )\n"
	// + " SELECT \n"
	// + " sum(total_no_of_applicable_employees) AS
	// total_no_of_applicable_employees,\n"
	// + " sum(total_approved_employees) as total_approved_employees\n"
	// + " ,sum(total_pending_employees) as total_pending_employees\n"
	// + " ,sum(total_defaulter_employees) as total_defaulter_employees\n"
	// + " from final_select", nativeQuery = true)
	// public List<Object[]> getTimesheetDashboardCountForEmployee(@Param("month")
	// Integer month, @Param("year") Integer year,@Param("emp_id") Long emp_id);

	// @Query(value= " WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " inner join employee e on etm.emp_id = e.emp_id\n"
	// + " where (:billableType = 'All' or e.billable_type = :billableType)\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + " Authorized_Project_IDs AS (\n"
	// + " SELECT DISTINCT p.project_id FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
	// + " ),\n"
	// + " Employee_Timesheets_With_Activities AS (\n"
	// + " SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status,
	// et.office_in_time, et.office_out_time,\n"
	// + " a.team_id AS activity_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " ),\n"
	// + " Base_Report_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// e.billable,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, p.po_no,\n"
	// + " s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
	// + " e.reporting_manager_id, ecsm.client_side_id,\n"
	// + " CASE WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS
	// employement_id,\n"
	// + " d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email,
	// e.employmentstatus, p.active as projectActive\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
	// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " and (:billableType = 'All' or e.billable_type = :billableType)\n"
	// + " AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND
	// UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND
	// UPPER(e.employmentstatus) != 'INACTIVE')) \n"
	// + " ),\n"
	// + " Project_Managers_Aggregated AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name
	// SEPARATOR ', ') AS Project_Manager_Names\n"
	// + " FROM project_manager_mapping pm\n"
	// + " LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
	// + " etwa_team.office_in_time, etwa_team.office_out_time,
	// brd.employee_team_map_id,\n"
	// + " CASE\n"
	// + " WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
	// + " WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id =
	// brd.team_id THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Approved' THEN 'AP'\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Pending' THEN 'PE'\n"
	// + " WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS
	// NULL THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id =
	// brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND
	// o.activity_team_id != brd.team_id\n"
	// + " ) THEN 'O'\n"
	// + " WHEN adir.dt < brd.start_date THEN 'O'\n"
	// + " WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM
	// Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date
	// = adir.dt) THEN 'A' -- Absent / Not filled\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
	// + " ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND
	// brd.team_id = etwa_team.activity_team_id\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
	// + " ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
	// + " AND etwa_general.activity_team_id IS NULL\n"
	// + " ),\n"
	// + " Expected_Working_Days_Detail AS (\n"
	// + " SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS
	// expected_working_day_date, brd.employee_team_map_id\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(brd.start_date) AND
	// COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
	// + " WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
	// + " AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type)
	// LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Timesheet_Filled AS (\n"
	// + " SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS
	// dt, brd.employee_team_map_id\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND
	// etwa.activity_team_id = brd.team_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS
	// dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id
	// FROM Actual_Timesheet_Filled\n"
	// + " ),\n"
	// + " Expected_Ishine_Working_Days AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, employee_team_map_id,
	// COUNT(DISTINCT dt) AS expected_ishine_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Ishine_Timesheet_Summary AS (\n"
	// + " SELECT distinct etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS
	// filled_ishine_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_pending_Days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_approved_Days\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
	// + " GROUP BY etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Calculated_Status AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) AS client_side_not_filled_count,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) >= 2 THEN 'Defaulter'\n"
	// + " WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0,
	// COALESCE(eiwd.expected_ishine_days, 0)\n"
	// + " - (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " ),\n"
	// + " final_select as\n"
	// + " (\n"
	// + " SELECT\n"
	// + " distinct COUNT(DISTINCT ecs.emp_id) AS
	// total_no_of_applicable_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Approved' THEN ecs.emp_id
	// END) AS total_approved_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Pending' THEN ecs.emp_id
	// END) AS total_pending_employees,\n"
	// + " COUNT(distinct CASE WHEN ecs.employee_status = 'Defaulter' THEN
	// ecs.emp_id END) AS total_defaulter_employees\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id =
	// pma.project_id\n"
	// + " LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " )\n"
	// + " SELECT \n"
	// + " sum(DISTINCT total_no_of_applicable_employees) AS
	// total_no_of_applicable_employees,\n"
	// + " sum(total_approved_employees) as total_approved_employees\n"
	// + " ,sum(total_pending_employees) as total_pending_employees\n"
	// + " ,sum(total_defaulter_employees) as total_defaulter_employees\n"
	// + " from final_select ", nativeQuery = true)
	// public List<Object[]>
	// getTimesheetDashboardCountForAllEmployee(@Param("month") Integer month,
	// @Param("year") Integer year,@Param("emp_id") Long
	// emp_id,@Param("billableType") String billableType,@Param("employeeActive")
	// String employeeActive);

	// @Query(value="WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS
	// UNSIGNED) = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range(dt) AS (\n"
	// + " SELECT from_date FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
	// + " WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " WHERE p.has_client_side_id = TRUE\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id =:emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + " Authorized_Project_IDs AS (\n"
	// + " SELECT DISTINCT p.project_id FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
	// + " WHERE p.has_client_side_id = true\n"
	// + " ),\n"
	// + " Employee_Timesheets_With_Activities AS (\n"
	// + " SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status,\n"
	// + " a.team_id AS activity_team_id, et.timesheet_id\n"
	// + " FROM employee_timesheets et\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " ),\n"
	// + " Base_Report_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// e.billable,\n"
	// + " p.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, p.po_no,\n"
	// + " s.name spoc, tl.name teamLead,
	// etm.employee_team_map_id,p.internal_project_type,p.po_project_type,\n"
	// + " e.reporting_manager_id, ecsm.client_side_id,p.clientrm,\n"
	// + " CASE WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS
	// employement_id,\n"
	// + " d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email,
	// e.employmentstatus\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
	// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Project_Manager_Summary AS (\n"
	// + " SELECT p.project_id,\n"
	// + " GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') as
	// Project_Manager\n"
	// + " FROM projects p\n"
	// + " LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
	// + " LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
	// + " GROUP BY p.project_id\n"
	// + " ),\n"
	// + " Expected_Client_Side_Base_DSR_Dates AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id,\n"
	// + " brd.project_id,\n"
	// + " brd.team_id,\n"
	// + " brd.employee_team_map_id,\n"
	// + " adir.dt\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND adir.dt >= DATE(brd.start_date)\n"
	// + " AND (brd.end_date IS NULL OR adir.dt <= brd.end_date)\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
	// + " WHERE etwa_nested.emp_id = brd.emp_id\n"
	// + " AND etwa_nested.date = adir.dt\n"
	// + " AND etwa_nested.activity_team_id IS NULL -- Exclude timesheets
	// specifically linked to another team/project activity\n"
	// + " AND (upper(etwa_nested.day_type) LIKE '%LEAVE%'\n"
	// + " OR upper(etwa_nested.day_type) LIKE '%CLIENT%HOLIDAY%'\n"
	// + " OR upper(etwa_nested.day_type) LIKE '%PUBLIC%HOLIDAY%'\n"
	// + " OR upper(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Client_Side_Submissions AS (\n"
	// + " SELECT DISTINCT\n"
	// + " et.emp_id,\n"
	// + " brd.project_id,\n"
	// + " brd.team_id,\n"
	// + " brd.employee_team_map_id,\n"
	// + " et.date AS dt,\n"
	// + " tdd.client_approval_status\n"
	// + " FROM employee_timesheets et\n"
	// + " INNER JOIN timesheet_document_details tdd ON et.timesheet_id =
	// tdd.timesheet_id\n"
	// + " INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
	// + " AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT
	// to_date FROM Date_Parameters))\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " WHERE tdd.active = TRUE\n"
	// + " AND (upper(tdd.client_approval_status) = 'APPROVED' OR
	// upper(tdd.client_approval_status) = 'PENDING')\n"
	// + " AND et.date < CURDATE()\n"
	// + " AND (a.team_id = brd.team_id OR a.team_id IS NULL) -- Link to the
	// specific team or general timesheet\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM
	// Expected_Client_Side_Base_DSR_Dates\n"
	// + " UNION\n"
	// + " SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM
	// Actual_Client_Side_Submissions\n"
	// + " ),\n"
	// + " Expected_DSR_Counts AS (\n"
	// + " SELECT emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS
	// total_expected_dsr_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Apmosys_Timesheet_Filled_Days AS (\n"
	// + " SELECT\n"
	// + " et.emp_id, brd.project_id, brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT et.date) AS filled_working_days\n"
	// + " FROM Employee_Timesheets_With_Activities et\n"
	// + " INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
	// + " AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT
	// to_date FROM Date_Parameters))\n"
	// + " AND (et.activity_team_id = brd.team_id OR et.activity_team_id IS NULL) --
	// Link to specific team or general\n"
	// + " WHERE (et.day_type LIKE '%Working%' OR upper(et.day_type) LIKE
	// '%LEAVE%')\n"
	// + " GROUP BY et.emp_id, brd.project_id, brd.employee_team_map_id\n"
	// + " ),\n"
	// + " Document_Summary AS (\n"
	// + " SELECT\n"
	// + " tdd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'APPROVED'
	// AND tdd.final_flag = 1 THEN tdd.timesheet_id END) AS
	// Client_Approved_count,\n"
	// + " COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'PENDING'
	// AND tdd.timesheet_id\n"
	// + " NOT IN (SELECT timesheet_id FROM timesheet_document_details WHERE
	// upper(client_approval_status) = 'APPROVED') THEN tdd.timesheet_id END) AS
	// Client_pending_count\n"
	// + " FROM timesheet_document_details tdd\n"
	// + " INNER JOIN employee_timesheets et ON tdd.timesheet_id =
	// et.timesheet_id\n"
	// + " INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
	// + " AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT
	// to_date FROM Date_Parameters))\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " WHERE DATE(et.date) BETWEEN (SELECT from_date FROM Date_Parameters) AND
	// (SELECT to_date FROM Date_Parameters)\n"
	// + " AND tdd.active = TRUE\n"
	// + " AND (a.team_id = brd.team_id OR a.team_id IS NULL) -- Link to the
	// specific team or general timesheet\n"
	// + " GROUP BY tdd.emp_id, brd.project_id, brd.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Final_Summary AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.project_name, pms.Project_Manager,
	// brd.po_no,\n"
	// + " COALESCE(brd.po_project_type, brd.internal_project_type) AS project_type,
	// brd.client_name,\n"
	// + " brd.apmosysrm, brd.apmosys_rm_email, brd.clientrm,\n"
	// + " COALESCE(edc.total_expected_dsr_days, 0) AS expected_dsr_count,\n"
	// + " COALESCE(atfd.filled_working_days, 0) AS
	// ishine_timesheet_filled_count,\n"
	// + " GREATEST(0, COALESCE(edc.total_expected_dsr_days, 0) -
	// (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count,
	// 0))) AS ClientSideNotFilledTimesheets_count,\n"
	// + " COALESCE(ds.Client_pending_count, 0) AS
	// ClientSidePendingTimesheet_count,\n"
	// + " COALESCE(ds.Client_Approved_count, 0) AS Client_Approved_count,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(edc.total_expected_dsr_days, 0) -
	// (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count,
	// 0))) >= 2 THEN 'Defaulter'\n"
	// + " WHEN COALESCE(ds.Client_pending_count, 0) > 0 or GREATEST(0,
	// COALESCE(edc.total_expected_dsr_days, 0) -
	// (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count,
	// 0))) >= 1 THEN 'Pending' "
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status,\n"
	// + " brd.active\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Project_Manager_Summary pms ON brd.project_id =
	// pms.project_id\n"
	// + " LEFT JOIN Expected_DSR_Counts edc ON brd.emp_id = edc.emp_id AND
	// brd.project_id = edc.project_id AND brd.employee_team_map_id =
	// edc.employee_team_map_id\n"
	// + " LEFT JOIN Apmosys_Timesheet_Filled_Days atfd ON brd.emp_id = atfd.emp_id
	// AND brd.project_id = atfd.project_id AND brd.employee_team_map_id =
	// atfd.employee_team_map_id\n"
	// + " LEFT JOIN Document_Summary ds ON brd.emp_id = ds.emp_id AND
	// brd.project_id = ds.project_id AND brd.employee_team_map_id =
	// ds.employee_team_map_id\n"
	// + " ),\n"
	// + " Project_Level_Summary AS (\n"
	// + " SELECT project_id,\n"
	// + " CASE\n"
	// + " WHEN SUM(CASE WHEN employee_status = 'Defaulter' THEN 1 ELSE 0 END) > 0
	// THEN 'Defaulter'\n"
	// + " WHEN SUM(CASE WHEN employee_status = 'Pending' THEN 1 ELSE 0 END) > 0
	// THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS project_status\n"
	// + " FROM Employee_Final_Summary\n"
	// + " GROUP BY project_id\n"
	// + " )\n"
	// + "SELECT\n"
	// + " COUNT(DISTINCT pls.project_id) AS total_no_of_applicable_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Approved' THEN 1 ELSE 0 END) AS
	// total_approved_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Pending' THEN 1 ELSE 0 END) AS
	// total_pending_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Defaulter' THEN 1 ELSE 0 END) AS
	// total_defaulter_projects\n"
	// + "FROM\n"
	// + " Project_Level_Summary pls", nativeQuery = true)
	// public List<Object[]> getTimesheetDashboardCountForProject(@Param("month")
	// Integer month, @Param("year") Integer year,@Param("emp_id") Long emp_id);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "WITH RankedTimeSheets AS ( " +
			"SELECT et.emp_id, et.office_in_time, et.office_out_time, p.project_id, c.client_id, cl.client_location_id, "
			+
			"t.team_name, a.activity, a.activity_id, etam.description, ecsm.client_side_id, t.team_id, et.client_approval_status, "
			+
			"RANK() OVER (PARTITION BY et.emp_id ORDER BY et.date DESC) as rnk, " +
			"CASE WHEN e.is_apmosys_product = 'true' " +
			"THEN CONCAT('AP-', e.employeement_id) " +
			"ELSE CONCAT('A-', e.employeement_id) END AS employement_id,"
			+ "et.total_time, e.timesheet_lock_updated_on, is_timesheet_lock_check_enable " +
			"FROM employee_timesheets et " +
			"INNER JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id " +
			"INNER JOIN activities a ON etam.activity_id = a.activity_id " +
			"LEFT JOIN employee e ON et.emp_id = e.emp_id " +
			"LEFT JOIN teams t ON a.team_id = t.team_id " +
			"LEFT JOIN projects p ON p.project_id = t.project_id " +
			"LEFT JOIN clients c ON p.client_id = c.client_id " +
			"LEFT JOIN client_locations cl ON cl.client_id = c.client_id " +
			"LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = et.emp_id AND ecsm.project_id = p.project_id "
			+
			"WHERE UPPER(et.day_type) LIKE '%WORKING%' " +
			") " +
			"SELECT DISTINCT employement_id, office_in_time, office_out_time, project_id, client_id, client_location_id, "
			+
			"team_name, activity, activity_id, description, team_id, client_approval_status, total_time, timesheet_lock_updated_on, is_timesheet_lock_check_enable "
			+
			"FROM RankedTimeSheets " +
			"WHERE rnk = 1 AND emp_id = :emp_id", nativeQuery = true)
	List<Object[]> getLastTimesheetFiledByEmpIdOLD(@Param("emp_id") Long emp_id);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "WITH RankedTimeSheets AS ( " +
			"SELECT et.emp_id, et.office_in_time, et.office_out_time, pts.project_id, c.client_id, cl.client_location_id, "
			+
			"t.team_name, a.activity, a.activity_id, etam.description, ecsm.client_side_id, t.team_id, csm.status AS client_approval_status, "
			+
			"RANK() OVER (PARTITION BY et.emp_id ORDER BY et.date DESC) as rnk, " +
			"CASE WHEN e.is_apmosys_product = 'true' " +
			"THEN CONCAT('AP-', e.employeement_id) " +
			"ELSE CONCAT('A-', e.employeement_id) END AS employement_id,"
			+ "ROUND(et.total_activities_minutes / 60, 2) AS total_time, e.timesheet_lock_updated_on, is_timesheet_lock_check_enable "
			+
			"FROM employee_timesheets_new et " +
			"INNER JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id " +
			"INNER JOIN activities a ON etam.activity_id = a.activity_id " +
			"LEFT JOIN employee e ON et.emp_id = e.emp_id " +
			"LEFT JOIN teams t ON a.team_id = t.team_id " +
			"LEFT JOIN projects p ON p.project_id = t.project_id " +
			"LEFT JOIN clients c ON p.client_id = c.client_id " +
			"LEFT JOIN client_locations cl ON cl.client_id = c.client_id " +
			"LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id " +
			"LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id "
			+
			"LEFT JOIN client_status_master_new csm ON pts.client_approval_status = csm.status_id " +
			"LEFT JOIN employee_client_side_id_mapping_new ecsm ON ecsm.emp_id = et.emp_id AND ecsm.project_id = pts.project_id AND ecsm.active = 1 "
			+
			"WHERE UPPER(dtm.day_type) LIKE '%WORKING%' " +
			") " +
			"SELECT DISTINCT employement_id, office_in_time, office_out_time, project_id, client_id, client_location_id, "
			+
			"team_name, activity, activity_id, description, team_id, client_approval_status, total_time, timesheet_lock_updated_on, is_timesheet_lock_check_enable "
			+
			"FROM RankedTimeSheets " +
			"WHERE rnk = 1 AND emp_id = :emp_id", nativeQuery = true)
	List<Object[]> getLastTimesheetFiledByEmpId(@Param("emp_id") Long emp_id);

	// @Query(value= "WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS
	// UNSIGNED) = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range(dt) AS (\n"
	// + " SELECT from_date FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
	// + " WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id )\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id )\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + " User_Is_SuperAdmin_Or_Special_Dept AS (\n"
	// + " SELECT EXISTS (\n"
	// + " SELECT 1\n"
	// + " FROM employee u\n"
	// + " JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
	// + " JOIN department d ON jr.dept_id = d.dept_id\n"
	// + " WHERE u.emp_id = :emp_id\n"
	// + " AND (jr.employee_role = 'SuperAdmin'\n"
	// + " OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
	// + " ) AS is_special_user\n"
	// + " ),\n"
	// + " Base_Project_Employees AS (\n"
	// + " SELECT DISTINCT\n"
	// + " e.emp_id,\n"
	// + " p.project_id,\n"
	// + " t.team_id,\n"
	// + " date(etm.start_date) AS etm_start_date,\n"
	// + " etm.end_date AS etm_end_date,\n"
	// + " d1.dept_id AS employee_dept_id,\n"
	// + " COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
	// + " etm.employee_team_map_id,p.active\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN clients c ON c.client_id = p.client_id\n"
	// + " INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
	// + " LEFT JOIN job_role j1 ON j1.job_role_id = e.job_role_id\n"
	// + " LEFT JOIN department d1 ON d1.dept_id = j1.dept_id\n"
	// + " WHERE 1=1\n"
	// + " AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " AND (:billableType = 'All' OR p.po_project_type = :billableType)\n"
	// + " AND (:projectActive = 'All' OR p.active = :projectActive)\n"
	// + " ),\n"
	// + " Employee_Timesheet_Statuses AS (\n"
	// + " SELECT\n"
	// + " et.emp_id,\n"
	// + " et.date,\n"
	// + " et.timesheet_id,\n"
	// + " UPPER(et.day_type) AS day_type_upper,\n"
	// + " et.status,\n"
	// + " COALESCE(a.team_id, 0) AS activity_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " ),\n"
	// + " Expected_Working_Days_Detail AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id,\n"
	// + " bpe.project_id,\n"
	// + " bpe.team_id,\n"
	// + " bpe.employee_team_map_id,\n"
	// + " adir.dt AS expected_working_day_date\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND adir.dt >= DATE(bpe.etm_start_date)\n"
	// + " AND (bpe.etm_end_date IS NULL OR adir.dt <= DATE(bpe.etm_end_date))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheet_Statuses ets1\n"
	// + " WHERE ets1.emp_id = bpe.emp_id\n"
	// + " AND ets1.date = adir.dt\n"
	// + " AND (ets1.day_type_upper LIKE '%LEAVE%'\n"
	// + " OR ets1.day_type_upper LIKE '%CLIENT%HOLIDAY%'\n"
	// + " OR ets1.day_type_upper LIKE '%PUBLIC%HOLIDAY%'\n"
	// + " OR ets1.day_type_upper LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Timesheet_Filled AS (\n"
	// + " SELECT DISTINCT\n"
	// + " bpe.emp_id,\n"
	// + " bpe.project_id,\n"
	// + " bpe.team_id,\n"
	// + " bpe.employee_team_map_id,\n"
	// + " ets.date AS dt\n"
	// + " FROM Employee_Timesheet_Statuses ets\n"
	// + " INNER JOIN Base_Project_Employees bpe ON bpe.emp_id = ets.emp_id AND
	// bpe.team_id = ets.activity_team_id\n"
	// + " WHERE ets.date BETWEEN (SELECT from_date FROM Date_Parameters)\n"
	// + " AND (SELECT to_date FROM Date_Parameters)\n"
	// + " AND ets.day_type_upper IN ('WORKING', 'NON-WORKING')\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT emp_id, project_id, team_id, employee_team_map_id,
	// expected_working_day_date as dt FROM Expected_Working_Days_Detail\n"
	// + " UNION\n"
	// + " SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM
	// Actual_Timesheet_Filled\n"
	// + " ),\n"
	// + " Expected_Ishine_Working_Days AS (\n"
	// + " SELECT\n"
	// + " emp_id,\n"
	// + " project_id,\n"
	// + " team_id,\n"
	// + " employee_team_map_id,\n"
	// + " COUNT(DISTINCT dt) AS expected_ishine_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Ishine_Timesheet_Summary AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id,\n"
	// + " bpe.project_id,\n"
	// + " bpe.team_id,\n"
	// + " bpe.employee_team_map_id,\n"
	// + " COUNT(DISTINCT\n"
	// + " CASE\n"
	// + " WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING') AND
	// ets.activity_team_id = bpe.team_id THEN ets.date\n"
	// + " ELSE NULL\n"
	// + " END\n"
	// + " ) AS filled_ishine_days,\n"
	// + " COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING')
	// AND ets.status = 'Pending' AND ets.activity_team_id = bpe.team_id THEN
	// ets.date END) AS ishine_pending_Days,\n"
	// + " COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING')
	// AND ets.status = 'Approved' AND ets.activity_team_id = bpe.team_id THEN
	// ets.date END) AS ishine_approved_Days\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " INNER JOIN Employee_Timesheet_Statuses ets ON bpe.emp_id = ets.emp_id\n"
	// + " AND ets.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT
	// to_date FROM Date_Parameters)\n"
	// + " GROUP BY bpe.emp_id, bpe.project_id, bpe.team_id,
	// bpe.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Final_Summary AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_dept_id, bpe.project_type,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS
	// expected_ishine_timesheet_days,\n"
	// + " COALESCE(its.filled_ishine_days, 0) AS filled_ishine_timesheet_days,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days,
	// 0)) ) AS not_filled_ishine_timesheet_days,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -\n"
	// + " (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
	// + " WHEN COALESCE(its.ishine_pending_Days, 0) > 0 or GREATEST(0,
	// COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days,
	// 0)\n"
	// + " + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON bpe.emp_id = eiwd.emp_id
	// AND bpe.project_id = eiwd.project_id AND bpe.team_id = eiwd.team_id AND
	// bpe.employee_team_map_id = eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON bpe.emp_id = its.emp_id AND
	// bpe.project_id = its.project_id AND bpe.team_id = its.team_id AND
	// bpe.employee_team_map_id = its.employee_team_map_id\n"
	// + " ),\n"
	// + " Project_Level_Summary AS (\n"
	// + " SELECT\n"
	// + " efs.project_id,\n"
	// + " efs.project_type,\n"
	// + " CASE\n"
	// + " WHEN SUM(CASE WHEN efs.employee_status = 'Defaulter' THEN 1 ELSE 0 END) >
	// 0 THEN 'Defaulter'\n"
	// + " WHEN SUM(CASE WHEN efs.employee_status = 'Pending' THEN 1 ELSE 0 END) > 0
	// THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS project_status\n"
	// + " FROM Employee_Final_Summary efs\n"
	// + " JOIN User_Is_SuperAdmin_Or_Special_Dept uis ON 1=1\n"
	// + " WHERE (\n"
	// + " uis.is_special_user = TRUE\n"
	// + " OR efs.employee_dept_id IN (SELECT dept_id FROM department WHERE hod_id =
	// :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM Authorized_Employees ae WHERE ae.emp_id =
	// :emp_id AND ae.emp_id = efs.emp_id)\n"
	// + " )\n"
	// + " GROUP BY efs.project_id, efs.project_type\n"
	// + " )\n"
	// + "SELECT\n"
	// + " COUNT(DISTINCT pls.project_id) AS total_no_of_applicable_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Approved' THEN 1 ELSE 0 END) AS
	// total_approved_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Pending' THEN 1 ELSE 0 END) AS
	// total_pending_projects,\n"
	// + " SUM(CASE WHEN pls.project_status = 'Defaulter' THEN 1 ELSE 0 END) AS
	// total_defaulter_projects\n"
	// + "FROM\n"
	// + " Project_Level_Summary pls\n"
	// + "WHERE\n"
	// + " (:billableType = 'All' OR pls.project_type = :billableType) ",
	// nativeQuery = true)
	// public List<Object[]>
	// getAllEmpTimesheetDashboardCountForProject(@Param("month") Integer month,
	// @Param("year") Integer year,@Param("emp_id") Long emp_id
	// ,@Param("billableType") String billableType,@Param("projectActive") String
	// projectActive);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "\n"
			+ "WITH RECURSIVE\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id \n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (  \n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                INNER JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    ),\n"
			+ "\n"
			+ " Date_Generator (dt) AS (\n"
			+ "        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT COUNT(*) AS expected_fill_count\n"
			+ "        FROM Date_Generator\n"
			+ "        WHERE dt NOT IN (\n"
			+ "            SELECT date_of_holiday\n"
			+ "            FROM holiday\n"
			+ "            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
			+ "              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT etm.emp_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        WHERE etm.active != 0\n"
			+ "          AND t.is_active != 'N'\n"
			+ "          AND p.active != 'false'\n"
			+ "          AND e.employmentstatus != 'InActive'\n"
			+ "          AND date(etm.start_date) < curdate()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            tdd.emp_id,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'PENDING' THEN 1 END) AS Client_pending_count,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'APPROVED' THEN 1 END) AS Client_Approved_count,\n"
			+ "            COUNT(CASE WHEN UPPER(csm.status) = 'REJECTED' THEN 1 END) AS Client_Rejected_count\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status = csm.status_id\n"
			+ "         WHERE MONTH(tdd.created_on) = MONTH(CURRENT_DATE()) AND YEAR(tdd.created_on) = YEAR(CURRENT_DATE())\n"
			+ "        GROUP BY tdd.emp_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Final_Counts AS (\n"
			+ "        SELECT\n"
			+ "            SUM(IFNULL(ds.Client_Approved_count, 0)) as totalApproved,\n"
			+ "            SUM(IFNULL(ds.Client_pending_count, 0)) as totalPending,\n"
			+ "            SUM(IFNULL(ds.Client_Rejected_count, 0)) as totalRejected,\n"
			+ "            (SELECT COUNT(*) FROM Base_Project_Employees) as totalUniqueEmployees\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    fc.totalApproved AS totalClientSideApprovedCount,\n"
			+ "    fc.totalPending AS totalClientSidePendingCount,\n"
			+ "    ( (fc.totalUniqueEmployees * wds.expected_fill_count) - (fc.totalApproved + fc.totalPending) ) AS eod_not_filled,\n"
			+ "    (fc.totalApproved + fc.totalPending + fc.totalRejected) as totalSubmitted,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalApproved * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_approved_percentage,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalRejected * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_rejected_percentage\n"
			+ "FROM\n"
			+ "    Final_Counts fc,\n"
			+ "    WorkingDays_Summary wds", nativeQuery = true)
	public List<Object[]> totalIshineNotFilledCountForAllEmpDashOLD(@Param("emp_id") Long emp_id);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "\n"
			+ "WITH RECURSIVE\n"
			+ "  Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id FROM (\n"
			+ "            SELECT e.emp_id \n"
			+ "            FROM employee e\n"
			+ "            WHERE EXISTS (  \n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                INNER JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                INNER JOIN department d ON jr.dept_id = d.dept_id"
			+ "                WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT e.emp_id\n"
			+ "            FROM employee e\n"
			+ "            INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "\n"
			+ "            UNION\n"
			+ "            \n"
			+ "            SELECT etm.emp_id\n"
			+ "            FROM employee_team_mapping etm\n"
			+ "            WHERE etm.team_id IN (\n"
			+ "                SELECT t.team_id FROM teams t\n"
			+ "                WHERE t.project_id IN (\n"
			+ "                    SELECT DISTINCT p.project_id FROM projects p\n"
			+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
			+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
			+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
			+ "                    WHERE pm.project_manager_id = :emp_id\n"
			+ "                      OR pom.project_overhead_id = :emp_id\n"
			+ "                      OR t2.spoc_id = :emp_id\n"
			+ "                      OR t2.team_lead_id = :emp_id\n"
			+ "                      OR etm2.emp_id = :emp_id\n"
			+ "                )\n"
			+ "            )\n"
			+ "        ) AS employee_list\n"
			+ "    ),\n"
			+ "\n"
			+ " Date_Generator (dt) AS (\n"
			+ "        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT COUNT(*) AS expected_fill_count\n"
			+ "        FROM Date_Generator\n"
			+ "        WHERE dt NOT IN (\n"
			+ "            SELECT date_of_holiday\n"
			+ "            FROM holiday\n"
			+ "            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
			+ "              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT etm.emp_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        WHERE etm.active != 0\n"
			+ "          AND t.is_active != 'N'\n"
			+ "          AND p.active != 'false'\n"
			+ "          AND e.employmentstatus != 'InActive'\n"
			+ "          AND date(etm.start_date) < curdate()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            tdd.emp_id,\n"
			+ "            COUNT(CASE WHEN csm.status = 'pending' THEN 1 END) AS Client_pending_count,\n"
			+ "            COUNT(CASE WHEN csm.status = 'approved' THEN 1 END) AS Client_Approved_count,\n"
			+ "            COUNT(CASE WHEN csm.status = 'rejected' THEN 1 END) AS Client_Rejected_count\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "         WHERE MONTH(tdd.created_on) = MONTH(CURRENT_DATE()) AND YEAR(tdd.created_on) = YEAR(CURRENT_DATE())\n"
			+ "        GROUP BY tdd.emp_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Final_Counts AS (\n"
			+ "        SELECT\n"
			+ "            SUM(IFNULL(ds.Client_Approved_count, 0)) as totalApproved,\n"
			+ "            SUM(IFNULL(ds.Client_pending_count, 0)) as totalPending,\n"
			+ "            SUM(IFNULL(ds.Client_Rejected_count, 0)) as totalRejected,\n"
			+ "            (SELECT COUNT(*) FROM Base_Project_Employees) as totalUniqueEmployees\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    fc.totalApproved AS totalClientSideApprovedCount,\n"
			+ "    fc.totalPending AS totalClientSidePendingCount,\n"
			+ "    ( (fc.totalUniqueEmployees * wds.expected_fill_count) - (fc.totalApproved + fc.totalPending) ) AS eod_not_filled,\n"
			+ "    (fc.totalApproved + fc.totalPending + fc.totalRejected) as totalSubmitted,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalApproved * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_approved_percentage,\n"
			+ "    CASE\n"
			+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
			+ "        THEN (fc.totalRejected * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
			+ "        ELSE 0\n"
			+ "    END AS document_rejected_percentage\n"
			+ "FROM\n"
			+ "    Final_Counts fc,\n"
			+ "    WorkingDays_Summary wds", nativeQuery = true)
	public List<Object[]> totalIshineNotFilledCountForAllEmpDash(@Param("emp_id") Long emp_id);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT et.date \n"
			+ "FROM Timesheet et \n"
			+ "WHERE et.empId = :empId \n"
			+ "AND et.projectId = :projectId \n"
			+ "AND et.date BETWEEN :startDate AND :endDate \n"
			+ "AND et.dayType in ('Non-working','Working')")
	Set<LocalDate> allTimesheetFilledDatesForDateRangeOLD(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("projectId") Integer projectId, @Param("empId") Long empId);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT et.date \n"
			+ "FROM EmployeeTimesheetsNew et \n"
			+ "LEFT JOIN DayTypeMasterNew dtm ON dtm.dayTypeId = et.dayTypeId \n"
			+ "WHERE et.empId = :empId \n"
			+ "AND EXISTS (SELECT 1 FROM ProjectTimesheetStatusNew pts WHERE pts.id.timesheetId = et.timesheetId AND pts.id.projectId = :projectId) \n"
			+ "AND et.date BETWEEN :startDate AND :endDate \n"
			+ "AND dtm.dayType in ('Non-working','Working')")
	Set<LocalDate> allTimesheetFilledDatesForDateRange(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("projectId") Integer projectId, @Param("empId") Long empId);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    \n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "    \n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id, \n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm \n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ "\n"
			+ " Authorized_Employees AS (\n"
			+ "	SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "		EXISTS (SELECT 1 FROM employee u \n"
			+ "				JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "				JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "				WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "				OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "				WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ "	)\n"
			+ "),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead, \n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "							WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "							ELSE CONCAT('A-',e.employeement_id)\n"
			+ "						END AS employement_id,\n"
			+ "				p.active as projectActive\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id\n"
			+ "			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1\n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "        --  AND et.date BETWEEN DATE(etm.start_date) AND COALESCE(date(etm.end_date), '2099-12-31')\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    \n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details\n"
			+ "        WHERE (UPPER(client_approval_status) = 'APPROVED' OR UPPER(client_approval_status) = 'PENDING')\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "    \n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "    \n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "Daily_Status_Details AS (\n"
			+ "    SELECT distinct\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        adir.dt AS timesheet_date,\n"
			+ "        pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
			+ "        CASE\n"
			+ "            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (adir.dt <= bpe.end_date or adir.dt >= bpe.start_date) THEN 'A'	\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "                 ) THEN 'O'\n"
			+ "            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "            ELSE 'NA'\n"
			+ "        END AS daily_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "                                            AND adir.dt = global_ts.date\n"
			+ "    LEFT JOIN day_type_master_new dtm ON global_ts.day_type_id = dtm.day_type_id\n"
			+ "    LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = global_ts.timesheet_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "                                                             AND doc_approved.final_flag = 1\n"
			+ "                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd3 LEFT JOIN client_status_master_new csm3 ON tdd3.client_approval_status_id = csm3.status_id WHERE tdd3.timesheet_id = doc_pending.timesheet_id AND UPPER(csm3.status) = 'APPROVED')\n"
			+ "                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ ")\n"
			+ "SELECT distinct \n"
			+ "    bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name, bpe.team_id,\n"
			+ "    CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS name,\n"
			+ "    bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name, bpe.reporting_manager_id,\n"
			+ "    MONTHNAME(dp.from_date) AS month_name,\n"
			+ "    COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "    GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "    COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "    COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`,\n"
			+ "    bpe.employement_id,\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA', 'CN', 'P') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "    0 AS 'Comp_Off',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "    0 AS 'Half_Day',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA','CN','P','WO','AH','CH','L','A','O') THEN 1 ELSE 0 END) AS total_days,\n"
			+ "    (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)) as ishine_filled_days,\n"
			+ "    bpe.employmentstatus,bpe.end_date, \n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "    bpe.active, bpe.projectActive\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "GROUP BY\n"
			+ "    bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "    name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
			+ "    bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date, bpe.end_date,\n"
			+ "    bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
			+ "    expectedTimesheetFillCount, client_side_not_filled_count, clientSidePendingCount, clientSideApprovedCount\n"
			+ "ORDER BY\n"
			+ "    bpe.project_name, name ", nativeQuery = true)
	public List<Object[]> getEmployeeSummaryReportClientSideApplicable(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId);

	// @Query(value= " WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS
	// from_date,\n"
	// + " CASE\n"
	// + " WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
	// + " THEN CURDATE()\n"
	// + " ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'),
	// '%Y-%m-%d'))\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " inner join employee e on etm.emp_id = e.emp_id\n"
	// + " where (:billableType = 'All' or e.billable_type = :billableType)\n"
	// + " ),\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id\n"
	// + " FROM employee e\n"
	// + " WHERE (\n"
	// + " EXISTS (\n"
	// + " SELECT 1\n"
	// + " FROM employee u\n"
	// + " JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
	// + " JOIN department d ON jr.dept_id = d.dept_id\n"
	// + " WHERE u.emp_id = :emp_id\n"
	// + " AND (jr.employee_role IN ('SuperAdmin')\n"
	// + " OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
	// + " )\n"
	// + " OR\n"
	// + " e.job_role_id IN (\n"
	// + " SELECT jr.job_role_id\n"
	// + " FROM job_role jr\n"
	// + " WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id =
	// :emp_id)\n"
	// + " )\n"
	// + " OR\n"
	// + " EXISTS (\n"
	// + " SELECT 1\n"
	// + " FROM employee_team_mapping etm_inner\n"
	// + " inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
	// + " inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
	// + " inner JOIN project_manager_mapping pm_inner ON p_inner.project_id =
	// pm_inner.project_id\n"
	// + " WHERE etm_inner.emp_id = e.emp_id\n"
	// + " AND pm_inner.project_manager_id = :emp_id \n"
	// + " )\n"
	// + " OR\n"
	// + " EXISTS (\n"
	// + " SELECT 1\n"
	// + " FROM employee_team_mapping etm_inner\n"
	// + " inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
	// + " inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
	// + " inner JOIN project_overhead_mapping pom_inner ON p_inner.project_id =
	// pom_inner.project_id\n"
	// + " WHERE etm_inner.emp_id = e.emp_id\n"
	// + " AND pom_inner.project_overhead_id = :emp_id \n"
	// + " )\n"
	// + " OR\n"
	// + " EXISTS (\n"
	// + " SELECT 1\n"
	// + " FROM employee_team_mapping etm_inner\n"
	// + " inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
	// + " inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
	// + " WHERE etm_inner.emp_id = e.emp_id\n"
	// + " AND (t_inner.spoc_id = :emp_id or t_inner.team_lead_id = :emp_id)\n"
	// + " )\n"
	// + " )), \n"
	// + " Authorized_Project_IDs AS (\n"
	// + " SELECT DISTINCT p.project_id FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
	// + " ),\n"
	// + " Employee_Timesheets_With_Activities AS (\n"
	// + " SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status,
	// et.office_in_time, et.office_out_time,\n"
	// + " a.team_id AS activity_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
	// + " ),\n"
	// + " Base_Report_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// e.billable,\n"
	// + " etm.active, p.project_id, p.project_name,p.active as projectActive,\n"
	// + " c.client_id, c.client_name, p.po_no,\n"
	// + " s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
	// + " e.reporting_manager_id, ecsm.client_side_id,\n"
	// + " CASE WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS
	// employement_id,\n"
	// + " d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email,
	// e.employmentstatus\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
	// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " and (:billableType = 'All' or e.billable_type = :billableType)\n"
	// + " AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND
	// UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND
	// UPPER(e.employmentstatus) != 'INACTIVE')) \n"
	// + " ),\n"
	// + " Project_Managers_Aggregated AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name
	// SEPARATOR ', ') AS Project_Manager_Names\n"
	// + " FROM project_manager_mapping pm\n"
	// + " LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + " Daily_Status_Details AS (\n"
	// + " SELECT\n"
	// + " brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
	// + " etwa_team.office_in_time, etwa_team.office_out_time,
	// brd.employee_team_map_id,\n"
	// + " CASE\n"
	// + " WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
	// + " WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id =
	// brd.team_id THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Approved' THEN 'AP'\n"
	// + " WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status =
	// 'Pending' THEN 'PE'\n"
	// + " WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS
	// NULL THEN\n"
	// + " CASE\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
	// + " WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
	// + " WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " ELSE 'NA'\n"
	// + " END\n"
	// + " WHEN EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id =
	// brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND
	// o.activity_team_id != brd.team_id\n"
	// + " ) THEN 'O'\n"
	// + " WHEN adir.dt < brd.start_date THEN 'O'\n"
	// + " WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM
	// Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date
	// = adir.dt) THEN 'A' -- Absent / Not filled\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
	// + " ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND
	// brd.team_id = etwa_team.activity_team_id\n"
	// + " LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
	// + " ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
	// + " AND etwa_general.activity_team_id IS NULL\n"
	// + " ),\n"
	// + " Expected_Working_Days_Detail AS (\n"
	// + " SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS
	// expected_working_day_date, brd.employee_team_map_id\n"
	// + " FROM Base_Report_Details brd\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(brd.start_date) AND
	// COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
	// + " WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
	// + " AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type)
	// LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
	// + " )\n"
	// + " ),\n"
	// + " Actual_Timesheet_Filled AS (\n"
	// + " SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS
	// dt, brd.employee_team_map_id\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND
	// etwa.activity_team_id = brd.team_id\n"
	// + " WHERE brd.project_id IN (SELECT project_id FROM
	// Authorized_Project_IDs)\n"
	// + " ),\n"
	// + " Combined_Expected_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS
	// dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id
	// FROM Actual_Timesheet_Filled\n"
	// + " ),\n"
	// + " Expected_Ishine_Working_Days AS (\n"
	// + " SELECT distinct emp_id, project_id, team_id, employee_team_map_id,
	// COUNT(DISTINCT dt) AS expected_ishine_days\n"
	// + " FROM Combined_Expected_DSR\n"
	// + " GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
	// + " ),\n"
	// + " Ishine_Timesheet_Summary AS (\n"
	// + " SELECT distinct etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS
	// filled_ishine_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_pending_Days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING',
	// 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id =
	// brd.team_id THEN etwa.date END) AS ishine_approved_Days\n"
	// + " FROM Employee_Timesheets_With_Activities etwa\n"
	// + " JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
	// + " GROUP BY etwa.emp_id, brd.project_id, brd.team_id,
	// brd.employee_team_map_id\n"
	// + " ),\n"
	// + " Employee_Calculated_Status AS (\n"
	// + " SELECT distinct\n"
	// + " brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) AS client_side_not_filled_count,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days,
	// 0))) >= 2 THEN 'Defaulter'\n"
	// + " WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0,
	// COALESCE(eiwd.expected_ishine_days, 0)\n"
	// + " - (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Report_Details brd\n"
	// + " LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + " LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + " )\n"
	// + "SELECT SQL_CALC_FOUND_ROWS distinct\n"
	// + " brd.emp_id, brd.client_side_id, brd.start_date, brd.team_name,
	// brd.team_id,\n"
	// + " brd.name, brd.spoc, brd.billable_type, brd.employee_role, brd.dept_name,
	// brd.project_id,\n"
	// + " brd.project_name, pma.Project_Manager_Names, brd.po_no,
	// brd.client_name,\n"
	// + " brd.reporting_manager_id,\n"
	// + " (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
	// + " COALESCE(eiwd.expected_ishine_days, 0) AS
	// expected_ishine_timesheet_days,\n"
	// + " GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -
	// (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days,
	// 0)) ) AS not_filled_ishine_timesheet_days,\n"
	// + " COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
	// + " COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS
	// `1_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS
	// `2_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS
	// `3_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS
	// `4_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS
	// `5_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS
	// `6_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS
	// `7_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS
	// `8_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS
	// `9_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS
	// `10_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS
	// `11_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS
	// `12_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS
	// `13_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS
	// `14_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS
	// `15_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS
	// `16_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS
	// `17_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS
	// `18_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS
	// `19_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS
	// `20_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS
	// `21_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS
	// `22_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS
	// `23_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS
	// `24_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS
	// `25_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS
	// `26_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS
	// `27_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS
	// `28_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS
	// `29_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS
	// `30_office_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS
	// `31_office_out_time`,\n"
	// + " brd.employement_id,\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END)
	// AS 'Present',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS
	// 'Holiday',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
	// + " 0 AS 'Comp_Off',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS
	// 'NA_Count',\n"
	// + " 0 AS 'Half_Day',\n"
	// + " (SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END)
	// + SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) + SUM(CASE WHEN
	// dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) + SUM(CASE WHEN
	// dsd.daily_status = 'L' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN
	// ('A','O','NA') THEN 1 ELSE 0 END)) AS total_days,\n"
	// + " (COALESCE(its.ishine_approved_Days, 0) +
	// COALESCE(its.ishine_pending_Days, 0)) as ishine_filled_days,\n"
	// + " brd.employmentstatus, brd.end_date,\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS
	// 'Ready_for_invoicing',\n"
	// + " brd.active,brd.projectActive\n"
	// + "FROM Base_Report_Details brd\n"
	// + "LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + "LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id =
	// eiwd.employee_team_map_id\n"
	// + "LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id =
	// its.employee_team_map_id\n"
	// + "LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id =
	// pma.project_id\n"
	// + "LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + "WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
	// + " AND (:status = 'All' OR ecs.employee_status = :status)\n"
	// + " AND (:employmentId IS NULL OR LOWER(brd.employement_id) LIKE CONCAT('%',
	// :employmentId, '%'))\n"
	// + " AND (:clientsideId IS NULL OR LOWER(brd.client_side_id) LIKE CONCAT('%',
	// :clientsideId, '%'))\n"
	// + " AND (:employeeName IS NULL OR LOWER(brd.name) LIKE CONCAT('%',
	// :employeeName, '%'))\n"
	// + " AND (:billableType2 IS NULL OR LOWER(brd.billable_type) =
	// :billableType2)\n"
	// + " AND (:projectName IS NULL OR LOWER(brd.project_name) LIKE CONCAT('%',
	// :projectName, '%'))\n"
	// + " AND (:poNo IS NULL OR LOWER(brd.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
	// + " AND (:department IS NULL OR LOWER(brd.dept_name) LIKE CONCAT('%',
	// :department, '%'))\n"
	// + " AND (:clientName IS NULL OR LOWER(brd.client_name) LIKE CONCAT('%',
	// :clientName, '%'))\n"
	// + " AND (:projectManagers IS NULL OR LOWER(pma.Project_Manager_Names) LIKE
	// CONCAT('%', :projectManagers, '%'))\n"
	// + " AND (:teamName IS NULL OR LOWER(brd.team_name) LIKE CONCAT('%',
	// :teamName, '%'))\n"
	// + "GROUP BY\n"
	// + " brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id,
	// brd.name,\n"
	// + " pma.Project_Manager_Names, expected_ishine_timesheet_days,
	// not_filled_ishine_timesheet_days,\n"
	// + " ishine_pending_Days,
	// ishine_approved_Days,brd.employement_id,brd.employmentstatus,
	// brd.end_date,brd.active,brd.projectActive\n"
	// +" ORDER BY CASE WHEN :sortDirection = 'asc' THEN\n"
	// + " CASE\n"
	// + " WHEN :sortBy = 'employement_id' THEN employement_id\n"
	// + " WHEN :sortBy = 'employeeName' THEN brd.name\n"
	// + " WHEN :sortBy = 'employmentStatus' THEN brd.employmentstatus\n"
	// + " WHEN :sortBy = 'projectStatus' THEN brd.active\n"
	// + " WHEN :sortBy = 'departmentName' THEN dept_name\n"
	// + " WHEN :sortBy = 'billable_type' THEN brd.billable_type\n"
	// + " WHEN :sortBy = 'clientName' THEN brd.client_name\n"
	// + " WHEN :sortBy = 'po_no' THEN po_no\n"
	// + " WHEN :sortBy = 'project_name' THEN brd.project_name\n"
	// + " WHEN :sortBy = 'projectManagerName' THEN pma.Project_Manager_Names\n"
	// + " WHEN :sortBy = 'team' THEN team_name\n"
	// + " WHEN :sortBy = 'startDate' THEN brd.start_date\n"
	// + " WHEN :sortBy = 'endDate' THEN brd.end_date\n"
	// + " WHEN :sortBy = 'expectedTimesheetFillCount' THEN
	// ecs.expectedTimesheetFillCount\n"
	// + " ELSE brd.name\n"
	// + " END\n"
	// + " END ASC,\n"
	// + " CASE WHEN :sortDirection = 'desc' THEN\n"
	// + " CASE\n"
	// + " WHEN :sortBy = 'employement_id' THEN employement_id\n"
	// + " WHEN :sortBy = 'employeeName' THEN brd.name\n"
	// + " WHEN :sortBy = 'employmentStatus' THEN brd.employmentstatus\n"
	// + " WHEN :sortBy = 'projectStatus' THEN brd.active\n"
	// + " WHEN :sortBy = 'departmentName' THEN dept_name\n"
	// + " WHEN :sortBy = 'billable_type' THEN brd.billable_type\n"
	// + " WHEN :sortBy = 'clientName' THEN brd.client_name\n"
	// + " WHEN :sortBy = 'po_no' THEN po_no\n"
	// + " WHEN :sortBy = 'project_name' THEN brd.project_name\n"
	// + " WHEN :sortBy = 'projectManagerName' THEN pma.Project_Manager_Names\n"
	// + " WHEN :sortBy = 'team' THEN team_name\n"
	// + " WHEN :sortBy = 'startDate' THEN brd.start_date\n"
	// + " WHEN :sortBy = 'endDate' THEN brd.end_date\n"
	// + " WHEN :sortBy = 'expectedTimesheetFillCount' THEN
	// ecs.expectedTimesheetFillCount\n"
	// + " ELSE brd.name\n"
	// + " END\n"
	// + " END DESC\n"
	// + " LIMIT :offset, :pageSize" , nativeQuery = true)
	// public List<Object[]> getEmployeeSummaryReportAllEMP(
	// @Param("month") Integer month,
	// @Param("year") Integer year,
	// @Param("emp_id") Long emp_id,
	// @Param("billableType") String billableType,
	// @Param("status") String status,
	// @Param("employeeActive") String employeeActive,
	// String employmentId,String clientsideId, String employeeName, String
	// billableType2, String projectName, String poNo,
	// String projectManagers, String clientName, String teamName,String department
	// ,int offset,int pageSize,
	// String sortBy,String sortDirection);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id, \n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm \n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    ),\n"
			+ "    Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
			+ "            OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id = e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
			+ "            OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id = e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
			+ "            OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id = e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id = :emp_id))\n"
			+ "    )),\n"
			+ "    Authorized_Project_IDs AS (\n"
			+ "        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			// + " WHERE :billableType = 'All' OR p.po_project_type = :billableType\n"
			+ "AND (\n"
			+ "    'All' IN (:billableType)\n"
			+ "    OR p.po_project_type IN (:billableType)\n"
			+ ")\n"
			+ "    ),\n"
			+ "    Employee_Timesheets_With_Activities AS (\n"
			+ "        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "                        a.team_id AS activity_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "    ),\n"
			+ "    Base_Report_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, p.po_no,\n"
			+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus, p.active as projectActive\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ "        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ "    ),\n"
			+ "    Project_Managers_Aggregated AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "Daily_Status_Details AS (\n"
			+ "        SELECT\n"
			+ "            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "            CASE\n"
			+ "                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA' \n"
			+ "                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "                    CASE \n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "                        ELSE 'NA' \n"
			+ "                    END\n"
			+ "                WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "                    CASE\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "                        ELSE 'NA'\n"
			+ "                    END\n"
			+ "                WHEN EXISTS (\n"
			+ "                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "                ) THEN 'O' \n"
			+ "                WHEN adir.dt < brd.start_date THEN 'O' \n"
			+ "                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "                ELSE 'NA' \n"
			+ "            END AS daily_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_team \n"
			+ "            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date \n"
			+ "               AND etwa_general.activity_team_id IS NULL \n"
			+ "    ), \n"
			+ "    Expected_Working_Days_Detail AS (\n"
			+ "        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Actual_Timesheet_Filled AS (\n"
			+ "        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "    ),\n"
			+ "    Combined_Expected_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "    ),\n"
			+ "    Expected_Ishine_Working_Days AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "        FROM Combined_Expected_DSR\n"
			+ "        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "    Ishine_Timesheet_Summary AS (\n"
			+ "        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS filled_ishine_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id THEN etwa.date END) AS ishine_approved_Days\n"
			+ "        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "    )\n"
			+ "SELECT distinct\n"
			+ "     brd.emp_id,brd.client_side_id,brd.start_date,brd.team_name,brd.team_id,\n"
			+ "     brd.name,brd.spoc, brd.billable_type,brd.employee_role, brd.dept_name, brd.project_id,\n"
			+ "     brd.project_name,pma.Project_Manager_Names,brd.po_no,brd.client_name,\n"
			+ "     brd.reporting_manager_id,\n"
			+ "    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
			+ "    COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "    GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "    COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "    COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS `1_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS `2_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS `3_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS `4_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS `5_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS `6_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS `7_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS `8_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS `9_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS `10_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS `11_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS `12_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS `13_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS `14_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS `15_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS `16_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS `17_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS `18_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS `19_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS `20_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS `21_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS `22_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS `23_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS `24_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS `25_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS `26_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS `27_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS `28_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS `29_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS `30_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS `31_office_out_time`,\n"
			+ "    brd.employement_id,\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "	   0 AS 'Comp_Off', \n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "    0 AS 'Half_Day',\n"
			+ "    (SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('A','O','NA') THEN 1 ELSE 0 END)) AS total_days,\n"
			+ "    (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0)) as ishine_filled_days,\n"
			+ "    brd.employmentstatus, brd.end_date,\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "    brd.active, brd.projectActive\n"
			+ "FROM Base_Report_Details brd\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "GROUP BY\n"
			+ "     brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id, brd.name,\n"
			+ "     pma.Project_Manager_Names, expected_ishine_timesheet_days, not_filled_ishine_timesheet_days,\n"
			+ "     ishine_pending_Days, ishine_approved_Days,brd.employement_id,brd.employmentstatus, brd.end_date,brd.active\n"
			+ "ORDER BY\n"
			+ "    brd.name ", nativeQuery = true)
	public List<Object[]> getEmployeeSummaryReportAll(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id,
			@Param("billableType") List<String> billableTypes);

	// @Query(value= " WITH RECURSIVE\n"
	// + " Date_Parameters AS (\n"
	// + " SELECT\n"
	// + " COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),
	// DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
	// + " CASE\n"
	// + " WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
	// + " IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(),
	// LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
	// + " ELSE CURDATE()\n"
	// + " END AS to_date\n"
	// + " ),\n"
	// + "\n"
	// + " All_Dates_In_Range AS (\n"
	// + " SELECT from_date AS dt FROM Date_Parameters\n"
	// + " UNION ALL\n"
	// + " SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range,
	// Date_Parameters WHERE dt < Date_Parameters.to_date\n"
	// + " ),\n"
	// + "\n"
	// + " Project_Managers AS (\n"
	// + " SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name
	// SEPARATOR ', ') AS project_manager_name\n"
	// + " FROM project_manager_mapping pm\n"
	// + " left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
	// + " GROUP BY pm.project_id\n"
	// + " ),\n"
	// + "\n"
	// + " auth_emp AS (\n"
	// + " SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
	// + " etm.employee_team_map_id, date(etm.start_date) as start_date,
	// etm.end_date\n"
	// + " FROM employee_team_mapping etm\n"
	// + " INNER JOIN teams t ON etm.team_id = t.team_id\n"
	// + " INNER JOIN projects p ON t.project_id = p.project_id\n"
	// + " WHERE p.has_client_side_id = TRUE\n"
	// + " ),\n"
	// + "\n"
	// + " Authorized_Employees AS (\n"
	// + " SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
	// + " EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id =
	// jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id =
	// :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR',
	// 'Accounts', 'Resource Management Group')))\n"
	// + " OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE
	// jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_manager_mapping
	// pm_inner ON p_inner.project_id = pm_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pm_inner.project_manager_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp p_inner JOIN project_overhead_mapping
	// pom_inner ON p_inner.project_id = pom_inner.project_id WHERE p_inner.emp_id =
	// e.emp_id AND pom_inner.project_overhead_id = :emp_id)\n"
	// + " OR EXISTS (SELECT 1 FROM auth_emp etm_inner WHERE etm_inner.emp_id =
	// e.emp_id AND (etm_inner.spoc_id = :emp_id OR etm_inner.team_lead_id =
	// :emp_id))\n"
	// + " )),\n"
	// + "\n"
	// + " Base_Project_Employees AS (\n"
	// + " SELECT DISTINCT\n"
	// + " etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role,
	// e.billable_type,\n"
	// + " date(etm.start_date) as start_date, date(etm.end_date) as end_date,
	// etm.employee_team_map_id,\n"
	// + " etm.active, p.project_id, p.project_name,\n"
	// + " c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
	// + " s.name AS spoc, tl.name AS teamLead,\n"
	// + " e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
	// + " CASE\n"
	// + " WHEN e.is_apmosys_product = 'true' THEN
	// CONCAT('AP-',e.employeement_id)\n"
	// + " ELSE CONCAT('A-',e.employeement_id)\n"
	// + " END AS employement_id\n"
	// + " FROM projects p\n"
	// + " INNER JOIN teams t ON p.project_id = t.project_id\n"
	// + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	// + " INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	// + " INNER JOIN clients c ON c.client_id = p.client_id\n"
	// + " INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
	// + " LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
	// + " LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
	// + " LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
	// + " LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
	// + " LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id =
	// ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
	// + " WHERE p.has_client_side_id = 1\n"
	// + " AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
	// + " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM
	// Date_Parameters))\n"
	// + " ),\n"
	// + " Timesheet_Base_Data AS (\n"
	// + " SELECT DISTINCT\n"
	// + " et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,\n"
	// + " et.date, et.day_type, et.client_in_time, et.client_out_time,
	// et.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
	// + " FROM employee_timesheets et\n"
	// + " LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id =
	// etam.timesheet_id\n"
	// + " LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
	// + " LEFT JOIN teams t ON a.team_id = t.team_id\n"
	// + " LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and
	// etm.team_id = t.team_id\n"
	// + " WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT
	// to_date FROM Date_Parameters)\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary_Details AS (\n"
	// + " SELECT DISTINCT\n"
	// + " tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
	// + " DATE(tbd.date) AS timesheet_date,\n"
	// + " csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
	// + " tbd.shadow_emp_id, tdd.timesheet_id\n"
	// + " FROM timesheet_document_details tdd\n"
	// + " INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id
	// and tbd.emp_id = tdd.emp_id\n"
	// + " WHERE tdd.active = TRUE\n"
	// + " ),\n"
	// + " Expected_Client_Side_Base_DSR AS (\n"
	// + " SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id,
	// adir.dt\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " WHERE adir.dt BETWEEN DATE(bpe.start_date) AND
	// COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
	// + " AND NOT EXISTS (\n"
	// + " SELECT 1 FROM employee_timesheets et1 WHERE et1.emp_id = bpe.emp_id AND
	// adir.dt = et1.date\n"
	// + " AND UPPER(et1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY',
	// 'WEEK OFF')\n"
	// + " )\n"
	// + " ),\n"
	// + "\n"
	// + " Actual_Client_Side_Submissions AS (\n"
	// + " SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date
	// AS dt\n"
	// + " FROM Employee_Document_Summary_Details\n"
	// + " WHERE (UPPER(client_approval_status) = 'APPROVED' OR
	// UPPER(client_approval_status) = 'PENDING')\n"
	// + " AND timesheet_date < CURDATE()\n"
	// + " ),\n"
	// + "\n"
	// + " Combined_Expected_Client_Side_DSR AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Expected_Client_Side_Base_DSR\n"
	// + " UNION\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM
	// Actual_Client_Side_Submissions\n"
	// + " ),\n"
	// + "\n"
	// + " WorkingDays_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT
	// dt) AS expected_fill_count\n"
	// + " FROM Combined_Expected_Client_Side_DSR\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + "\n"
	// + " Employee_Document_Summary AS (\n"
	// + " SELECT distinct emp_id, project_id, employee_team_map_id,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND
	// final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
	// + " COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND
	// NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE timesheet_id =
	// edsd.timesheet_id AND UPPER(client_approval_status) = 'APPROVED') THEN
	// timesheet_id END) AS pending_days\n"
	// + " FROM Employee_Document_Summary_Details edsd\n"
	// + " GROUP BY emp_id, project_id, employee_team_map_id\n"
	// + " ),\n"
	// + "\n"
	// + "Daily_Status_Details AS (\n"
	// + " SELECT distinct\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " adir.dt AS timesheet_date,\n"
	// + " pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
	// + " CASE\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
	// + " WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets et WHERE
	// et.emp_id = bpe.emp_id)\n"
	// + " AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A' \n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
	// + " WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
	// + " WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
	// + " AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id !=
	// ts_data_relevant.employee_team_map_id)\n"
	// + " ) THEN 'O'\n"
	// + " WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
	// + " WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
	// + " WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
	// + " ELSE 'NA'\n"
	// + " END AS daily_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " CROSS JOIN All_Dates_In_Range adir\n"
	// + " LEFT JOIN employee_timesheets global_ts ON bpe.emp_id =
	// global_ts.emp_id\n"
	// + " AND adir.dt = global_ts.date\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id =
	// ts_data_relevant.emp_id\n"
	// + " AND adir.dt = ts_data_relevant.date\n"
	// + " AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
	// + " LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id =
	// ts_data_all_employee.emp_id\n"
	// + " AND adir.dt = ts_data_all_employee.date\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_approved ON
	// ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
	// + " AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
	// + " AND doc_approved.final_flag = 1\n"
	// + " AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary_Details doc_pending ON
	// ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
	// + " AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
	// + " AND NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE
	// timesheet_id = doc_pending.timesheet_id AND UPPER(client_approval_status) =
	// 'APPROVED')\n"
	// + " AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
	// + "),\n"
	// + "Employee_Calculated_Status AS (\n"
	// + " SELECT\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
	// + " GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS
	// client_side_not_filled_count,\n"
	// + " COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
	// + " COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
	// + " CASE\n"
	// + " WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) -
	// (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN
	// 'Defaulter'\n"
	// + " WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0,
	// COALESCE(wds.expected_fill_count, 0) - \n"
	// + " (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1
	// THEN 'Pending'\n"
	// + " ELSE 'Approved'\n"
	// + " END AS employee_status\n"
	// + " FROM Base_Project_Employees bpe\n"
	// + " LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id =
	// wds.employee_team_map_id\n"
	// + " LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id =
	// eds.employee_team_map_id\n"
	// + ")\n"
	// + "SELECT SQL_CALC_FOUND_ROWS distinct\n"
	// + " bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name,
	// bpe.team_id,\n"
	// + " CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN
	// CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS
	// name,\n"
	// + " bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,
	// bpe.project_id,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,
	// bpe.reporting_manager_id,\n"
	// + " MONTHNAME(dp.from_date) AS month_name,\n"
	// + " ecs.expectedTimesheetFillCount,\n"
	// + " ecs.client_side_not_filled_count,\n"
	// + " ecs.clientSidePendingCount,\n"
	// + " ecs.clientSideApprovedCount,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status
	// END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN
	// dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS
	// `1_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status
	// END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN
	// dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS
	// `2_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status
	// END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN
	// dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS
	// `3_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status
	// END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN
	// dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS
	// `4_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status
	// END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN
	// dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS
	// `5_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status
	// END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN
	// dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS
	// `6_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status
	// END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN
	// dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS
	// `7_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status
	// END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN
	// dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS
	// `8_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status
	// END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN
	// dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS
	// `9_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status
	// END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN
	// dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS
	// `10_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status
	// END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN
	// dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS
	// `11_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status
	// END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN
	// dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS
	// `12_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status
	// END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN
	// dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS
	// `13_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status
	// END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN
	// dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS
	// `14_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status
	// END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN
	// dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS
	// `15_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status
	// END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN
	// dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS
	// `16_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status
	// END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN
	// dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS
	// `17_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status
	// END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN
	// dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS
	// `18_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status
	// END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN
	// dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS
	// `19_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status
	// END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN
	// dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS
	// `20_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status
	// END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN
	// dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS
	// `21_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status
	// END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN
	// dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS
	// `22_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status
	// END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN
	// dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS
	// `23_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status
	// END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN
	// dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS
	// `24_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status
	// END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN
	// dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS
	// `25_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status
	// END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN
	// dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS
	// `26_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status
	// END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN
	// dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS
	// `27_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status
	// END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN
	// dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS
	// `28_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status
	// END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN
	// dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS
	// `29_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status
	// END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN
	// dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS
	// `30_client_out_time`,\n"
	// + " IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status
	// END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN
	// dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN
	// DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS
	// `31_client_out_time`,\n"
	// + " bpe.employement_id,\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('CA', 'CN', 'P') THEN 1 ELSE 0 END) AS
	// 'Present',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS
	// 'Holiday',\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
	// + " 0 AS 'Comp_Off',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS
	// 'NA_Count',\n"
	// + " 0 AS 'Half_Day',\n"
	// + " SUM(CASE WHEN dsd.daily_status IN
	// ('CA','CN','P','WO','AH','CH','L','A','O') THEN 1 ELSE 0 END) AS
	// total_days,\n"
	// + " (COALESCE(ecs.clientSideApprovedCount, 0) +
	// COALESCE(ecs.clientSidePendingCount, 0)) as ishine_filled_days,\n"
	// + " bpe.employmentstatus,bpe.end_date,\n"
	// + " SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS
	// 'Ready_for_invoicing',\n"
	// + " bpe.active,ecs.employee_status\n"
	// + "FROM Base_Project_Employees bpe\n"
	// + "JOIN Date_Parameters dp ON 1=1\n"
	// + "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id =
	// dsd.employee_team_map_id\n"
	// + "LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id =
	// ecs.employee_team_map_id\n"
	// + "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
	// + "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
	// + "WHERE (:status = 'All' OR ecs.employee_status = :status)\n"
	// + " AND (:employmentId IS NULL OR LOWER(bpe.employement_id) LIKE CONCAT('%',
	// :employmentId, '%'))\n"
	// + " AND (:clientsideId IS NULL OR LOWER(bpe.client_side_id) LIKE CONCAT('%',
	// :clientsideId, '%'))\n"
	// + " AND (:employeeName IS NULL OR LOWER(bpe.name) LIKE CONCAT('%',
	// :employeeName, '%'))\n"
	// + " AND (:billableType2 IS NULL OR LOWER(bpe.billable_type) =
	// :billableType2)\n"
	// + " AND (:projectName IS NULL OR LOWER(bpe.project_name) LIKE CONCAT('%',
	// :projectName, '%'))\n"
	// + " AND (:poNo IS NULL OR LOWER(bpe.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
	// + " AND (:department IS NULL OR LOWER(bpe.dept_name) LIKE CONCAT('%',
	// :department, '%'))\n"
	// + " AND (:clientName IS NULL OR LOWER(bpe.client_name) LIKE CONCAT('%',
	// :clientName, '%'))\n"
	// + " AND (:projectManagers IS NULL OR LOWER(pm.project_manager_name) LIKE
	// CONCAT('%', :projectManagers, '%'))\n"
	// + " AND (:teamName IS NULL OR LOWER(bpe.team_name) LIKE CONCAT('%',
	// :teamName, '%'))\n"
	// + " AND (:employmentStatus IS NULL OR LOWER(bpe.employmentstatus) LIKE
	// CONCAT('%', :employmentStatus, '%'))\n"
	// + " AND (:projectStatus IS NULL OR LOWER(bpe.active) LIKE CONCAT('%',
	// :projectStatus, '%'))\n"
	// + "GROUP BY\n"
	// + " bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
	// + " name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
	// + " bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
	// + " bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date,
	// bpe.end_date,\n"
	// + " bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
	// + " ecs.expectedTimesheetFillCount, ecs.client_side_not_filled_count,
	// ecs.clientSidePendingCount, ecs.clientSideApprovedCount\n"
	// +" ORDER BY CASE WHEN :sortDirection = 'asc' THEN\n"
	// + " CASE\n"
	// + " WHEN :sortBy = 'employement_id' THEN employement_id\n"
	// + " WHEN :sortBy = 'clientSideId' THEN bpe.client_side_id\n"
	// + " WHEN :sortBy = 'employeeName' THEN bpe.name\n"
	// + " WHEN :sortBy = 'employmentStatus' THEN bpe.employmentstatus\n"
	// + " WHEN :sortBy = 'projectStatus' THEN bpe.active\n"
	// + " WHEN :sortBy = 'departmentName' THEN dept_name\n"
	// + " WHEN :sortBy = 'billable_type' THEN bpe.billable_type\n"
	// + " WHEN :sortBy = 'clientName' THEN bpe.client_name\n"
	// + " WHEN :sortBy = 'po_no' THEN po_no\n"
	// + " WHEN :sortBy = 'project_name' THEN bpe.project_name\n"
	// + " WHEN :sortBy = 'projectManagerName' THEN pm.project_manager_name\n"
	// + " WHEN :sortBy = 'team' THEN team_name\n"
	// + " WHEN :sortBy = 'startDate' THEN bpe.start_date\n"
	// + " WHEN :sortBy = 'endDate' THEN bpe.end_date\n"
	// + " WHEN :sortBy = 'expectedTimesheetFillCount' THEN
	// ecs.expectedTimesheetFillCount\n"
	// + " ELSE bpe.name\n"
	// + " END\n"
	// + " END ASC,\n"
	// + " CASE WHEN :sortDirection = 'desc' THEN\n"
	// + " CASE\n"
	// + " WHEN :sortBy = 'employement_id' THEN employement_id\n"
	// + " WHEN :sortBy = 'clientSideId' THEN bpe.client_side_id\n"
	// + " WHEN :sortBy = 'employeeName' THEN bpe.name\n"
	// + " WHEN :sortBy = 'employmentStatus' THEN bpe.employmentstatus\n"
	// + " WHEN :sortBy = 'projectStatus' THEN bpe.active\n"
	// + " WHEN :sortBy = 'departmentName' THEN dept_name\n"
	// + " WHEN :sortBy = 'billable_type' THEN bpe.billable_type\n"
	// + " WHEN :sortBy = 'clientName' THEN bpe.client_name\n"
	// + " WHEN :sortBy = 'po_no' THEN po_no\n"
	// + " WHEN :sortBy = 'project_name' THEN bpe.project_name\n"
	// + " WHEN :sortBy = 'projectManagerName' THEN pm.project_manager_name\n"
	// + " WHEN :sortBy = 'team' THEN team_name\n"
	// + " WHEN :sortBy = 'startDate' THEN bpe.start_date\n"
	// + " WHEN :sortBy = 'endDate' THEN bpe.end_date\n"
	// + " WHEN :sortBy = 'expectedTimesheetFillCount' THEN
	// ecs.expectedTimesheetFillCount\n"
	// + " ELSE bpe.name\n"
	// + " END\n"
	// + " END DESC\n"
	// + " LIMIT :offset, :pageSize", nativeQuery = true)
	// public List<Object[]> getEmployeeViewForClientAttendanceStatus(
	// @Param("month") Integer month,
	// @Param("year") Integer year,
	// @Param("emp_id") Long empId,
	// @Param("status") String status,String employmentId,String clientsideId,
	// String employeeName,
	// String billableType2, String projectName, String poNo,
	// String projectManagers, String clientName, String teamName,String department,
	// String employmentStatus,String projectStatus,
	// int offset,int pageSize,
	// String sortBy,String sortDirection);

	@Query(value = "WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ "\n"
			+ " Authorized_Employees AS (\n"
			+ "	SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "		EXISTS (SELECT 1 FROM employee u \n"
			+ "				JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "				JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "				WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "				OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "				WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ "	)\n"
			+ "),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead,\n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "                                        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "                                        ELSE CONCAT('A-',e.employeement_id)\n"
			+ "                                    END AS employement_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id\n"
			+ "			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "			AND (\n"
			+ "			ae.emp_id IS NOT NULL \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "				AND jr.dept_id = user_jr.dept_id\n"
			+ "			)\n"
			+ "		)\n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters)) AND (\n"
			+ "						:clientSideFilter = 'ALL'\n"
			+ "						OR (:clientSideFilter = 'true' AND p.client_flag = 1)\n"
			+ "						OR (:clientSideFilter = 'false' AND (p.client_flag = 0 OR p.client_flag IS NULL))\n"
			+ "						)\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters)) AND adir.dt >= (SELECT from_date FROM Date_Parameters)\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details tdd\n"
			+ "        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE upper(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "    Employee_Calculated_Status AS (\n"
			+ "        SELECT\n"
			+ "            bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "            COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "            GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "            COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "            COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "                WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0, COALESCE(wds.expected_fill_count, 0) - \n"
			+ "                (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "        LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT bpe.emp_id) AS total_no_of_applicable_employees,\n"
			+ "    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Approved' THEN bpe.emp_id END) AS total_approved_employees,\n"
			+ "    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Pending' THEN bpe.emp_id END) AS total_pending_employees,\n"
			+ "    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Defaulter' THEN bpe.emp_id END) AS total_defaulter_employees,\n"
			+ "    COUNT(DISTINCT CASE WHEN ecs.employee_status in ('Defaulter','Pending') THEN bpe.emp_id END) AS Defaulter_employees\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "INNER JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id", nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForEmployee(@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id,
			@Param("clientSideFilter") String clientSideFilter);

	@Query(value = "WITH RECURSIVE\n"
			+ "			    Date_Parameters AS (\n"
			+ "			        SELECT\n"
			+ "			            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "			            CASE\n"
			+ "			                WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
			+ "			                    THEN CURDATE()\n"
			+ "			                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "			            END AS to_date\n"
			+ "			    ),\n"
			+ "			    All_Dates_In_Range(dt) AS (\n"
			+ "			        SELECT from_date FROM Date_Parameters\n"
			+ "			        UNION ALL\n"
			+ "			        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
			+ "			        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
			+ "			    ),\n"
			+ "			    auth_emp AS (\n"
			+ "			        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id, \n"
			+ "			               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "			        FROM employee_team_mapping etm \n"
			+ "			        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "			        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "			        WHERE p.has_client_side_id = TRUE\n"
			+ "			    ),\n"
			+ "			 Authorized_Employees AS (\n"
			+ "				SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "					EXISTS (SELECT 1 FROM employee u \n"
			+ "							JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "							JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "							WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "							OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "							WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ "				)\n"
			+ "			),\n"
			+ "			    Employee_Timesheets_With_Activities AS (\n"
			+ "			        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status,\n"
			+ "			                        a.team_id AS activity_team_id, et.timesheet_id\n"
			+ "			        FROM employee_timesheets_new et\n"
			+ "			        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "			        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "			        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "			        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "			        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "			    ),\n"
			+ "			    Base_Report_Details AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "			            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "			            p.active, p.project_id, p.project_name, p.po_start_date, p.po_end_date, \n"
			+ "			            c.client_id, c.client_name, p.po_no,\n"
			+ "			            s.name spoc, tl.name teamLead, etm.employee_team_map_id,p.internal_project_type,p.po_project_type,\n"
			+ "			            e.reporting_manager_id, ecsm.client_side_id,p.clientrm,\n"
			+ "			            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "			            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus\n"
			+ "			        FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "			        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "			        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "			        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "			        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "						LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "						LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "						JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "						JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id		\n"
			+ "						LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "						LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "			        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "			        WHERE p.has_client_side_id = 1 \n"
			+ "						AND (\n"
			+ "						ae.emp_id IS NOT NULL \n"
			+ "						OR \n"
			+ "						(\n"
			+ "							(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "							AND jr.dept_id = user_jr.dept_id\n"
			+ "						)\n"
			+ "					)\n"
			+ "			 AND (\n"
			+ "					e.date_of_relieving IS NULL \n"
			+ "					OR YEAR(e.date_of_relieving) > :year \n"
			+ "					OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "				) \n"
			+ "			 AND e.emp_id not between 1 and 6 \n"
			+ "			        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "			        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			    Project_Manager_Summary AS (\n"
			+ "			        SELECT p.project_id,\n"
			+ "			               GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') as Project_Manager\n"
			+ "			        FROM projects p\n"
			+ "			        LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "			        LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "			        GROUP BY p.project_id\n"
			+ "			    ),\n"
			+ "			    Expected_Client_Side_Base_DSR_Dates AS (\n"
			+ "			        SELECT\n"
			+ "			            brd.emp_id,\n"
			+ "			            brd.project_id,\n"
			+ "			            brd.team_id,\n"
			+ "			            brd.employee_team_map_id,\n"
			+ "			            adir.dt\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
			+ "			          AND adir.dt >= DATE(brd.start_date)\n"
			+ "			 AND adir.dt >= (SELECT from_date FROM Date_Parameters)\n"
			+ "			          AND (brd.end_date IS NULL OR adir.dt <= brd.end_date)\n"
			+ "			            AND NOT EXISTS (\n"
			+ "			               SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "			               WHERE etwa_nested.emp_id = brd.emp_id\n"
			+ "			               AND etwa_nested.date = adir.dt\n"
			+ "			               AND etwa_nested.activity_team_id IS NULL \n"
			+ "			               AND (upper(etwa_nested.day_type) LIKE '%LEAVE%'\n"
			+ "			               OR upper(etwa_nested.day_type) LIKE '%CLIENT%HOLIDAY%'\n"
			+ "			               OR upper(etwa_nested.day_type) LIKE '%PUBLIC%HOLIDAY%'\n"
			+ "			               OR upper(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "			           )\n"
			+ "			    ),\n"
			+ "		    Actual_Client_Side_Submissions AS (\n"
			+ "		        SELECT DISTINCT\n"
			+ "		            et.emp_id,\n"
			+ "		            brd.project_id,\n"
			+ "		            brd.team_id,\n"
			+ "		            brd.employee_team_map_id,\n"
			+ "		            et.date AS dt,\n"
			+ "		            csm.status AS client_approval_status\n"
			+ "		        FROM employee_timesheets_new et\n"
			+ "		        INNER JOIN timesheet_document_details_new tdd ON et.timesheet_id = tdd.timesheet_id\n"
			+ "		        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "		        INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
			+ "		                                            AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT to_date FROM Date_Parameters))\n"
			+ "		        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "		        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "		        WHERE tdd.active = TRUE and \n"
			+ "		          ((upper(csm.status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(csm.status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE upper(csm2.status) = 'APPROVED'))) AND  et.date <= (SELECT to_date FROM Date_Parameters) AND et.date >= (SELECT from_date FROM Date_Parameters) \n"
			+ "		          AND (a.team_id = brd.team_id OR a.team_id IS NULL) -- Link to the specific team or general timesheet\n"
			+ "		    ),\n"
			+ "			    Combined_Expected_DSR AS (\n"
			+ "			        SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR_Dates\n"
			+ "			        UNION\n"
			+ "			        SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "			    ),\n"
			+ "			    Expected_DSR_Counts AS (\n"
			+ "			        SELECT emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS total_expected_dsr_days\n"
			+ "			        FROM Combined_Expected_DSR\n"
			+ "			        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "			    ),\n"
			+ "			    Apmosys_Timesheet_Filled_Days AS (\n"
			+ "			        SELECT\n"
			+ "			            et.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "			            COUNT(DISTINCT et.date) AS filled_working_days\n"
			+ "			        FROM Employee_Timesheets_With_Activities et\n"
			+ "			        INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
			+ "			                                          AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT to_date FROM Date_Parameters))\n"
			+ "			                                          AND (et.activity_team_id = brd.team_id OR et.activity_team_id IS NULL) -- Link to specific team or general\n"
			+ "			        WHERE (et.day_type LIKE '%Working%' OR upper(et.day_type) LIKE '%LEAVE%')\n"
			+ "			        GROUP BY et.emp_id, brd.project_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "			    Document_Summary AS (\n"
			+ "			        SELECT\n"
			+ "			            tdd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "			            COUNT(DISTINCT CASE WHEN upper(csm.status) = 'APPROVED' AND tdd.final_flag = 1 THEN tdd.timesheet_id END) AS Client_Approved_count,\n"
			+ "			            COUNT(DISTINCT CASE WHEN upper(csm.status) = 'PENDING' AND tdd.timesheet_id\n"
			+ "			                                                            NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE upper(csm2.status) = 'APPROVED') THEN tdd.timesheet_id END) AS Client_pending_count\n"
			+ "			        FROM timesheet_document_details_new tdd\n"
			+ "			        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "			        INNER JOIN employee_timesheets_new et ON tdd.timesheet_id = et.timesheet_id\n"
			+ "			        INNER JOIN Base_Report_Details brd ON et.emp_id = brd.emp_id\n"
			+ "			                                            AND et.date BETWEEN brd.start_date AND COALESCE(brd.end_date, (SELECT to_date FROM Date_Parameters))\n"
			+ "			        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "			        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "			        WHERE DATE(et.date) BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "			          AND tdd.active = TRUE\n"
			+ "			          AND (a.team_id = brd.team_id OR a.team_id IS NULL) -- Link to the specific team or general timesheet\n"
			+ "			        GROUP BY tdd.emp_id, brd.project_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "			    Employee_Final_Summary AS (\n"
			+ "			        SELECT\n"
			+ "			            brd.emp_id, brd.project_id, brd.project_name, pms.Project_Manager, brd.po_no,\n"
			+ "			            COALESCE(brd.po_project_type, brd.internal_project_type) AS project_type, brd.client_name,\n"
			+ "			            brd.apmosysrm, brd.apmosys_rm_email, brd.clientrm, brd.po_start_date project_start_date, brd.po_end_date project_end_date,\n"
			+ "			            COALESCE(edc.total_expected_dsr_days, 0) AS expected_dsr_count,\n"
			+ "			            COALESCE(atfd.filled_working_days, 0) AS ishine_timesheet_filled_count,\n"
			+ "			            GREATEST(0, COALESCE(edc.total_expected_dsr_days, 0) - (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))) AS ClientSideNotFilledTimesheets_count,\n"
			+ "			            COALESCE(ds.Client_pending_count, 0) AS ClientSidePendingTimesheet_count,\n"
			+ "			            COALESCE(ds.Client_Approved_count, 0) AS Client_Approved_count,\n"
			+ "			            CASE\n"
			+ "			                WHEN GREATEST(0, COALESCE(edc.total_expected_dsr_days, 0) - (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))) >= 2 THEN 'Defaulter' -- You might need to refine the definition of 'Defaulter' based on specific rules for client-side timesheets.\n"
			+ "			 WHEN COALESCE(ds.Client_pending_count, 0) > 0 or GREATEST(0, COALESCE(edc.total_expected_dsr_days, 0) - (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))) >= 1 THEN 'Pending' \n"
			+ "			                ELSE 'Approved'\n"
			+ "			            END AS employee_status,\n"
			+ "			            brd.active\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        LEFT JOIN Project_Manager_Summary pms ON brd.project_id = pms.project_id\n"
			+ "			        LEFT JOIN Expected_DSR_Counts edc ON brd.emp_id = edc.emp_id AND brd.project_id = edc.project_id AND brd.employee_team_map_id = edc.employee_team_map_id\n"
			+ "			        LEFT JOIN Apmosys_Timesheet_Filled_Days atfd ON brd.emp_id = atfd.emp_id AND brd.project_id = atfd.project_id AND brd.employee_team_map_id = atfd.employee_team_map_id\n"
			+ "			        LEFT JOIN Document_Summary ds ON brd.emp_id = ds.emp_id AND brd.project_id = ds.project_id AND brd.employee_team_map_id = ds.employee_team_map_id\n"
			+ "			    ),\n"
			+ "			    Project_Level_Summary AS (\n"
			+ "			        SELECT project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
			+ "			               apmosysrm, apmosys_rm_email, clientrm, project_start_date, project_end_date,\n"
			+ "			               SUM(expected_dsr_count) AS total_expected_fill_count,\n"
			+ "			               SUM(ishine_timesheet_filled_count) AS total_ishine_filled,\n"
			+ "			               SUM(ClientSideNotFilledTimesheets_count) AS total_client_side_not_filled,\n"
			+ "			               SUM(ClientSidePendingTimesheet_count) AS total_client_side_pending,\n"
			+ "			               SUM(Client_Approved_count) AS total_client_approved,\n"
			+ "			               CASE\n"
			+ "			                   WHEN SUM(CASE WHEN employee_status = 'Defaulter' THEN 1 ELSE 0 END) > 0 THEN 'Defaulter'\n"
			+ "			                   WHEN SUM(CASE WHEN employee_status = 'Pending' THEN 1 ELSE 0 END) > 0 THEN 'Pending'\n"
			+ "			                   ELSE 'Approved'\n"
			+ "			               END AS project_status,\n"
			+ "			               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(Client_Approved_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSideApproved_Percent,\n"
			+ "			               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSidePendingTimesheet_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSidePending_Percent,\n"
			+ "			               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSideNotFilledTimesheets_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS NotFilled_Percent,\n"
			+ "			                    COUNT(emp_id) AS total_employees_in_project, active\n"
			+ "			        FROM Employee_Final_Summary\n"
			+ "			        GROUP BY project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
			+ "			                 apmosysrm, apmosys_rm_email, clientrm, active\n"
			+ "			\n"
			+ "			    )\n"
			+ "				SELECT\n"
			+ "			    COUNT(DISTINCT pls.project_id) AS total_no_of_applicable_projects,\n"
			+ "			    SUM(CASE WHEN pls.project_status = 'Approved' THEN 1 ELSE 0 END) AS total_approved_projects,\n"
			+ "			    SUM(CASE WHEN pls.project_status = 'Pending' THEN 1 ELSE 0 END) AS total_pending_projects,\n"
			+ "			    SUM(CASE WHEN pls.project_status = 'Defaulter' THEN 1 ELSE 0 END) AS total_defaulter_projects\n"
			+ "			FROM\n"
			+ "			    Project_Level_Summary pls", nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForProject(@Param("month") Integer month,
			@Param("year") Integer year, @Param("emp_id") Long emp_id);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    \n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "    \n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id, \n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm \n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ "\n"
			+ " Authorized_Employees AS (\n"
			+ "	SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "		EXISTS (SELECT 1 FROM employee u \n"
			+ "				JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "				JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "				WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "				OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "				WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ "	)\n"
			+ "),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead, \n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "							WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "							ELSE CONCAT('A-',e.employeement_id)\n"
			+ "						END AS employement_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id\n"
			+ "			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "			AND (\n"
			+ "			ae.emp_id IS NOT NULL \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "				AND jr.dept_id = user_jr.dept_id\n"
			+ "			)\n"
			+ "		)\n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "        --  AND et.date BETWEEN DATE(etm.start_date) AND COALESCE(date(etm.end_date), '2099-12-31')\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    \n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details tdd\n"
			+ "        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE upper(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "    \n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "    \n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "Daily_Status_Details AS (\n"
			+ "    SELECT distinct\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        adir.dt AS timesheet_date,\n"
			+ "        pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
			+ "        CASE\n"
			+ "            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
			+ "			WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A'				\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "            WHEN UPPER(dtm.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "                 ) THEN 'O'\n"
			+ "            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "            ELSE 'NA'\n"
			+ "        END AS daily_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "                                            AND adir.dt = global_ts.date\n"
			+ "    LEFT JOIN day_type_master_new dtm ON global_ts.day_type_id = dtm.day_type_id\n"
			+ "    LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = global_ts.timesheet_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "                                                             AND doc_approved.final_flag = 1\n"
			+ "                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd3 LEFT JOIN client_status_master_new csm3 ON tdd3.client_approval_status_id = csm3.status_id WHERE tdd3.timesheet_id = doc_pending.timesheet_id AND UPPER(csm3.status) = 'APPROVED')\n"
			+ "                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ ")\n"
			+ "SELECT distinct \n"
			+ "    bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name, bpe.team_id,\n"
			+ "    CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS name,\n"
			+ "    bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name, bpe.reporting_manager_id,\n"
			+ "    MONTHNAME(dp.from_date) AS month_name,\n"
			+ "    COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "    GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "    COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "    COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`,\n"
			+ "    bpe.employement_id,\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA', 'CN', 'P') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "   0 AS 'Comp_Off',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "    0 AS 'Half_Day',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA','CN','P','WO','AH','CH','L','A','O') THEN 1 ELSE 0 END) AS total_days,\n"
			+ "    (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)) as client_filled_Days,\n"
			+ "    bpe.employmentstatus,bpe.end_date, \n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "    bpe.active\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "where bpe.project_id in (:project_id)\n"
			+ "GROUP BY\n"
			+ "    bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "    name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
			+ "    bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date, bpe.end_date,\n"
			+ "    bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
			+ "    expectedTimesheetFillCount, client_side_not_filled_count, clientSidePendingCount, clientSideApprovedCount\n"
			+ "ORDER BY\n"
			+ "    bpe.project_name, name ", nativeQuery = true)
	public List<Object[]> getEmployeeTimesheetAsCalenderByProjectId(
			@Param("project_id") Integer projectId,
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId);

	@Query(value = " WITH RECURSIVE\n"
			+ "			    Date_Parameters AS (\n"
			+ "			        SELECT\n"
			+ "			            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "			            CASE\n"
			+ "			                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "			                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "			                ELSE CURDATE()\n"
			+ "			            END AS to_date\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    All_Dates_In_Range AS (\n"
			+ "			        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "			        UNION ALL\n"
			+ "			        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Project_Managers AS (\n"
			+ "			        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "			        FROM project_manager_mapping pm\n"
			+ "			        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "			        GROUP BY pm.project_id\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    auth_emp AS (\n"
			+ "			        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "			               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "			        FROM employee_team_mapping etm\n"
			+ "			        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "			        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "			        WHERE p.has_client_side_id = TRUE\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Authorized_Employees AS (\n"
			+ "			        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "			            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = 3 AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "			            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = 3))\n"
			+ "			    )),\n"
			+ "			    Base_Project_Employees AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "			            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "			            etm.active, p.project_id, p.project_name,\n"
			+ "			            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "			            s.name AS spoc, tl.name AS teamLead,\n"
			+ "			            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "			             CASE\n"
			+ "			                                        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "			                                        ELSE CONCAT('A-',e.employeement_id)\n"
			+ "			                                    END AS employement_id\n"
			+ "			        FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "			        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "			        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "			        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "			        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "			        WHERE p.has_client_side_id = 1 \n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "			        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "			        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			        Timesheet_Base_Data AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "			            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "			        FROM employee_timesheets_new et\n"
			+ "			        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "			        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "			        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "			        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "			        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "			        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id\n"
			+ "			        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Employee_Document_Summary_Details AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "			            DATE(tbd.date) AS timesheet_date,\n"
			+ "			            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "			            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "			        FROM timesheet_document_details_new tdd\n"
			+ "			        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "			        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "			        WHERE tdd.active = TRUE\n"
			+ "			    ),\n"
			+ "			        Expected_Client_Side_Base_DSR AS (\n"
			+ "			        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "			        FROM Base_Project_Employees bpe\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters)) and adir.dt >= (select from_date from Date_Parameters)\n"
			+ "			        AND NOT EXISTS (\n"
			+ "			            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "			            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "			            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "			            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "			        )\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Actual_Client_Side_Submissions AS (\n"
			+ "			        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "			        FROM Employee_Document_Summary_Details tdd\n"
			+ "			        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE upper(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "			          AND timesheet_date < CURDATE()\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Combined_Expected_Client_Side_DSR AS (\n"
			+ "			        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "			        UNION\n"
			+ "			        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    WorkingDays_Summary AS (\n"
			+ "			        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "			        FROM Combined_Expected_Client_Side_DSR\n"
			+ "			        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Employee_Document_Summary AS (\n"
			+ "			        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "			        FROM Employee_Document_Summary_Details edsd\n"
			+ "			        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			Daily_Status_Details AS (\n"
			+ "			    SELECT distinct\n"
			+ "			        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "			        adir.dt AS timesheet_date,\n"
			+ "			        pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
			+ "			        CASE\n"
			+ "			            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "			                 AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
			+ "						WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "			                 AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A'				\n"
			+ "			            WHEN UPPER(dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "			            WHEN UPPER(dtm.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "			            WHEN UPPER(dtm.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "			            WHEN UPPER(dtm.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "			            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "			                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "			                 ) THEN 'O'\n"
			+ "			            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "			            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "			            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "			            ELSE 'NA'\n"
			+ "			        END AS daily_status\n"
			+ "			    FROM Base_Project_Employees bpe\n"
			+ "			    CROSS JOIN All_Dates_In_Range adir\n"
			+ "			    LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "			                                            AND adir.dt = global_ts.date\n"
			+ "			    LEFT JOIN day_type_master_new dtm ON global_ts.day_type_id = dtm.day_type_id\n"
			+ "			    LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = global_ts.timesheet_id\n"
			+ "			    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "			                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "			                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "			    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "			                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "			    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "			                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "			                                                             AND doc_approved.final_flag = 1\n"
			+ "			                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "			    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "			                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "			                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd3 LEFT JOIN client_status_master_new csm3 ON tdd3.client_approval_status_id = csm3.status_id WHERE tdd3.timesheet_id = doc_pending.timesheet_id AND UPPER(csm3.status) = 'APPROVED')\n"
			+ "			                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ "			),\n"
			+ "			Employee_Calculated_Status AS (\n"
			+ "			    SELECT\n"
			+ "			        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "			        COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "			        GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "			        COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "			        COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "			        CASE\n"
			+ "			            WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "			            WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0, COALESCE(wds.expected_fill_count, 0) - \n"
			+ "			            (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1 THEN 'Pending'\n"
			+ "			            ELSE 'Approved'\n"
			+ "			        END AS employee_status\n"
			+ "			    FROM Base_Project_Employees bpe\n"
			+ "			    LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "			    LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ "			)\n"
			+ "			SELECT SQL_CALC_FOUND_ROWS distinct\n"
			+ "			    bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name, bpe.team_id,\n"
			+ "			    CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS name,\n"
			+ "			    bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
			+ "			    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name, bpe.reporting_manager_id,\n"
			+ "			    MONTHNAME(dp.from_date) AS month_name,\n"
			+ "			    ecs.expectedTimesheetFillCount,\n"
			+ "			    ecs.client_side_not_filled_count,\n"
			+ "			    ecs.clientSidePendingCount,\n"
			+ "			    ecs.clientSideApprovedCount,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`,\n"
			+ "			    bpe.employement_id\n"
			+ "			FROM Base_Project_Employees bpe\n"
			+ "			JOIN Date_Parameters dp ON 1=1\n"
			+ "			LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "			LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "			LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "			LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "			WHERE bpe.emp_id in (:emp_id)\n"
			+ "			and bpe.project_id in (:project_id)\n"
			+ "			GROUP BY\n"
			+ "			    bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "			    name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
			+ "			    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
			+ "			    bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date, bpe.end_date,\n"
			+ "			    bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
			+ "			    ecs.expectedTimesheetFillCount, ecs.client_side_not_filled_count, ecs.clientSidePendingCount, ecs.clientSideApprovedCount ", nativeQuery = true)
	public List<Object[]> getEmployeeTimesheetAsCalender(
			@Param("emp_id") Long empId,
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("project_id") Integer projectId);

	@Query(value = "WITH RECURSIVE\n"
			+ "			    Date_Parameters AS (\n"
			+ "			        SELECT\n"
			+ "			            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "			            CASE\n"
			+ "			                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "			                    THEN CURDATE()\n"
			+ "			                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "			            END AS to_date\n"
			+ "			    ),\n"
			+ "			    All_Dates_In_Range AS (\n"
			+ "			        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "			        UNION ALL\n"
			+ "			        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "			    ),\n"
			+ "			    auth_emp AS (\n"
			+ "			        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "			               etm.employee_team_map_id, date(etm.start_date) as start_date, date(etm.end_date) as end_date\n"
			+ "			        FROM employee_team_mapping etm\n"
			+ "			        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "			        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "			        inner join employee e on etm.emp_id = e.emp_id\n"
			+ "			    ),\n"
			+ "			    Authorized_Employees AS (\n"
			+ "			        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "			            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = 3 AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "			            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = 3))\n"
			+ "			    )),\n"
			+ "			    Authorized_Project_IDs AS (\n"
			+ "			        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "			    ),\n"
			+ "			    Employee_Timesheets_With_Activities AS (\n"
			+ "			        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "			                        a.team_id AS activity_team_id\n"
			+ "			        FROM employee_timesheets_new et\n"
			+ "			        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "			        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "			        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "			        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "			        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "			    ),\n"
			+ "			    Base_Report_Details AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "			            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "			            etm.active, p.project_id, p.project_name,\n"
			+ "			            c.client_id, c.client_name, p.po_no,\n"
			+ "			            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "			            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "			            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "			            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus, p.active as projectActive\n"
			+ "			        FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "			        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ "			        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "			        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "			        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "			        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ " AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "			        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "			        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			    Project_Managers_Aggregated AS (\n"
			+ "			        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "			        FROM project_manager_mapping pm\n"
			+ "			        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "			        GROUP BY pm.project_id\n"
			+ "			    ),\n"
			+ "	Daily_Status_Details AS (\n"
			+ "			        SELECT\n"
			+ "			            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "			            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "			            CASE\n"
			+ "							WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "							WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "			                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "			                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "			                WHEN EXISTS (\n"
			+ "			                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "			                ) THEN 'O'\n"
			+ "			                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "			                ELSE 'NA'\n"
			+ "			            END AS daily_status\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "			            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "			            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "			               AND etwa_general.activity_team_id IS NULL\n"
			+ "			    ),\n"
			+ "			    Expected_Working_Days_Detail AS (\n"
			+ "			        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "			        AND NOT EXISTS (\n"
			+ "			            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "			            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "			            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "			        )\n"
			+ "			    ),\n"
			+ "			    Actual_Timesheet_Filled AS (\n"
			+ "			        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "			        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs) AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			    Combined_Expected_DSR AS (\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "			        UNION\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "			    ),\n"
			+ "			    Expected_Ishine_Working_Days AS (\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "			        FROM Combined_Expected_DSR\n"
			+ "			        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "			    ),\n"
			+ "	Ishine_Timesheet_Summary AS (\n"
			+ "			        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "			        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "			    Employee_Calculated_Status AS (\n"
			+ "			        SELECT distinct\n"
			+ "			            brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "			            COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
			+ "			            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) AS client_side_not_filled_count,\n"
			+ "			            COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
			+ "			            COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
			+ "			            CASE\n"
			+ "			                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "			                WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0)\n"
			+ "			                        - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "			                ELSE 'Approved'\n"
			+ "			            END AS employee_status\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "			        LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "			    )\n"
			+ "			SELECT  SQL_CALC_FOUND_ROWS distinct\n"
			+ "			    brd.emp_id, brd.client_side_id, brd.start_date, brd.team_name, brd.team_id,\n"
			+ "			    brd.name, brd.spoc, brd.billable_type, brd.employee_role, brd.dept_name, brd.project_id,\n"
			+ "			    brd.project_name, pma.Project_Manager_Names, brd.po_no, brd.client_name,\n"
			+ "			    brd.reporting_manager_id,\n"
			+ "			    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
			+ "			    COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "			    GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "			    COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "			    COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS `1_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS `2_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS `3_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS `4_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS `5_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS `6_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS `7_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS `8_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS `9_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS `10_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS `11_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS `12_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS `13_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS `14_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS `15_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS `16_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS `17_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS `18_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS `19_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS `20_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS `21_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS `22_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS `23_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS `24_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS `25_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS `26_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS `27_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS `28_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS `29_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS `30_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS `31_office_out_time`,\n"
			+ "			    brd.employement_id\n"
			+ "			FROM Base_Report_Details brd\n"
			+ "			LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "			LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "			LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "			LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "			LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "			WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "			AND brd.emp_id in (:emp_id)\n"
			+ "			AND brd.project_id in (:project_id)\n"
			+ "			GROUP BY\n"
			+ "			    brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id, brd.name,\n"
			+ "			    pma.Project_Manager_Names, expected_ishine_timesheet_days, not_filled_ishine_timesheet_days,\n"
			+ "			    ishine_pending_Days, ishine_approved_Days,brd.employement_id", nativeQuery = true)
	public List<Object[]> getEmployeeTimesheetAsCalenderForAllEmp(
			@Param("emp_id") Long empId,
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("project_id") Integer projectId);

	@Query(value = "WITH RECURSIVE\n"
			+ "			    Date_Parameters AS (\n"
			+ "			        SELECT\n"
			+ "			            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "			            CASE\n"
			+ "			                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "			                    THEN CURDATE()\n"
			+ "			                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "			            END AS to_date\n"
			+ "			    ),\n"
			+ "			    All_Dates_In_Range AS (\n"
			+ "			        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "			        UNION ALL\n"
			+ "			        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "			    ),\n"
			+ "			    auth_emp AS (\n"
			+ "			        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id, \n"
			+ "			               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "			        FROM employee_team_mapping etm \n"
			+ "			        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "			        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "			    ),\n"
			+ "			    Authorized_Employees AS (\n"
			+ "			        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "			            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "			            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "			    )),\n"
			+ "			    Authorized_Project_IDs AS (\n"
			+ "			        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "			    ),\n"
			+ "			    Employee_Timesheets_With_Activities AS (\n"
			+ "			        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "			                        a.team_id AS activity_team_id\n"
			+ "			        FROM employee_timesheets_new et\n"
			+ "			        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "			        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "			        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "			        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "			        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "			    ),\n"
			+ "			    Base_Report_Details AS (\n"
			+ "			        SELECT DISTINCT\n"
			+ "			            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "			            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "			            etm.active, p.project_id, p.project_name,\n"
			+ "			            c.client_id, c.client_name, p.po_no,\n"
			+ "			            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "			            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "			            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "			            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus, p.active as projectActive\n"
			+ "			        FROM projects p\n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "			        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ "			        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "			        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "			        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "			        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ " AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "			        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "			        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			    Project_Managers_Aggregated AS (\n"
			+ "			        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "			        FROM project_manager_mapping pm\n"
			+ "			        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "			        GROUP BY pm.project_id\n"
			+ "			    ),\n"
			+ "	Daily_Status_Details AS (\n"
			+ "			        SELECT\n"
			+ "			            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "			            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "			            CASE\n"
			+ "							WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "							WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "			                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "			                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "			                WHEN EXISTS (\n"
			+ "			                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "			                ) THEN 'O'\n"
			+ "			                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "			                ELSE 'NA'\n"
			+ "			            END AS daily_status\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "			            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "			            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "			               AND etwa_general.activity_team_id IS NULL\n"
			+ "			    ), \n"
			+ "			    Expected_Working_Days_Detail AS (\n"
			+ "			        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "			        AND NOT EXISTS (\n"
			+ "			            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "			            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "			            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "			        )\n"
			+ "			    ),\n"
			+ "			    Actual_Timesheet_Filled AS (\n"
			+ "			        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "			        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs) AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "			    ),\n"
			+ "			    Combined_Expected_DSR AS (\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "			        UNION\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "			    ),\n"
			+ "			    Expected_Ishine_Working_Days AS (\n"
			+ "			        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "			        FROM Combined_Expected_DSR\n"
			+ "			        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "			    ),\n"
			+ "	Ishine_Timesheet_Summary AS (\n"
			+ "			        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "			        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "			    )\n"
			+ "			SELECT distinct\n"
			+ "			     brd.emp_id,brd.client_side_id,brd.start_date,brd.team_name,brd.team_id,\n"
			+ "			     brd.name,brd.spoc, brd.billable_type,brd.employee_role, brd.dept_name, brd.project_id,\n"
			+ "			     brd.project_name,pma.Project_Manager_Names,brd.po_no,brd.client_name,\n"
			+ "			     brd.reporting_manager_id,\n"
			+ "			    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
			+ "			    COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "			    GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "			    COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "			    COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS `1_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS `2_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS `3_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS `4_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS `5_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS `6_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS `7_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS `8_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS `9_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS `10_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS `11_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS `12_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS `13_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS `14_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS `15_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS `16_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS `17_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS `18_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS `19_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS `20_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS `21_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS `22_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS `23_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS `24_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS `25_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS `26_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS `27_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS `28_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS `29_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS `30_office_out_time`,\n"
			+ "			    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS `31_office_out_time`,\n"
			+ "			    brd.employement_id,\n"
			+ "			    SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "			    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "			    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "			    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "				0 AS 'Comp_Off',\n"
			+ "			    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "			    0 AS 'Half_Day',\n"
			+ "			    (SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('A','O','NA') THEN 1 ELSE 0 END)) AS total_days,\n"
			+ "			    (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0)) as ishine_filled_days,\n"
			+ "			    brd.employmentstatus, brd.end_date,\n"
			+ "			    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "			    brd.active\n"
			+ "			FROM Base_Report_Details brd\n"
			+ "			LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "			LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "			LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "			LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "			WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "            and brd.project_id in (:project_id)\n"
			+ "			GROUP BY\n"
			+ "			     brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id, brd.name,\n"
			+ "			     pma.Project_Manager_Names, expected_ishine_timesheet_days, not_filled_ishine_timesheet_days,\n"
			+ "			     ishine_pending_Days, ishine_approved_Days,brd.employement_id,brd.employmentstatus, brd.end_date,brd.active\n"
			+ "			ORDER BY\n"
			+ "			    brd.name", nativeQuery = true)
	public List<Object[]> getEmployeeTimesheetAsCalenderByProjectIdForAllEmp(
			@Param("project_id") Integer projectId,
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id);

	@Query(value = "WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range(dt) AS (\n"
			+ "        SELECT from_date FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
			+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    ),\n"
			+ "    Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "    )),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            e.emp_id,\n"
			+ "            p.project_id,\n"
			+ "            t.team_id,\n"
			+ "            date(etm.start_date) AS etm_start_date,\n"
			+ "            etm.end_date AS etm_end_date,\n"
			+ "            d1.dept_id AS employee_dept_id,\n"
			+ "            COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
			+ "            etm.employee_team_map_id,p.active\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN job_role j1 ON j1.job_role_id = e.job_role_id\n"
			+ "        LEFT JOIN department d1 ON d1.dept_id = j1.dept_id\n"
			+ "        WHERE 1=1\n"
			+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "          AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
			// + " AND (:billableType = 'All' OR p.po_project_type = :billableType)\n"
			+ "AND (\n"
			+ "    'All' IN (:billableType)\n"
			+ "    OR p.po_project_type IN (:billableType)\n"
			+ ")\n"
			+ "          AND (:projectActive = 'All' OR p.active = :projectActive) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ "    ),\n"
			+ "    Employee_Timesheet_Statuses AS (\n"
			+ "        SELECT\n"
			+ "            et.emp_id,\n"
			+ "            et.date,\n"
			+ "            et.timesheet_id,\n"
			+ "            UPPER(dtm.day_type) AS day_type_upper,\n"
			+ "            sm.status,\n"
			+ "            COALESCE(a.team_id, 0) AS activity_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "    ),\n"
			+ "    Expected_Working_Days_Detail AS (\n"
			+ "        SELECT\n"
			+ "            bpe.emp_id,\n"
			+ "            bpe.project_id,\n"
			+ "            bpe.team_id,\n"
			+ "            bpe.employee_team_map_id,\n"
			+ "            adir.dt AS expected_working_day_date\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
			+ "         AND adir.dt >= DATE(bpe.etm_start_date)\n"
			+ "         AND (bpe.etm_end_date IS NULL OR adir.dt <= DATE(bpe.etm_end_date))\n"
			+ "         AND NOT EXISTS (\n"
			+ "                            SELECT 1 FROM Employee_Timesheet_Statuses ets1\n"
			+ "                            WHERE ets1.emp_id = bpe.emp_id\n"
			+ "                            AND ets1.date = adir.dt\n"
			+ "                            AND (ets1.day_type_upper LIKE '%LEAVE%'\n"
			+ "                            OR ets1.day_type_upper LIKE '%CLIENT%HOLIDAY%'\n"
			+ "                            OR ets1.day_type_upper LIKE '%PUBLIC%HOLIDAY%'\n"
			+ "                            OR ets1.day_type_upper LIKE '%WEEK%OFF%')\n"
			+ "                        )\n"
			+ "    ),\n"
			+ "    Actual_Timesheet_Filled AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            bpe.emp_id,\n"
			+ "            bpe.project_id,\n"
			+ "            bpe.team_id,\n"
			+ "            bpe.employee_team_map_id,\n"
			+ "            ets.date AS dt\n"
			+ "        FROM Employee_Timesheet_Statuses ets\n"
			+ "        INNER JOIN Base_Project_Employees bpe ON bpe.emp_id = ets.emp_id AND bpe.team_id = ets.activity_team_id\n"
			+ "        WHERE ets.date BETWEEN (SELECT from_date FROM Date_Parameters)\n"
			+ "                        AND (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND ets.day_type_upper IN ('WORKING', 'NON-WORKING') AND ets.date BETWEEN DATE(bpe.etm_start_date) AND COALESCE(DATE(bpe.etm_end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "    ),\n"
			+ "    Combined_Expected_DSR AS (\n"
			+ "        SELECT emp_id, project_id, team_id, employee_team_map_id, expected_working_day_date as dt FROM Expected_Working_Days_Detail\n"
			+ "        UNION\n"
			+ "        SELECT emp_id, project_id, team_id, employee_team_map_id, dt FROM Actual_Timesheet_Filled\n"
			+ "    ),\n"
			+ "    Expected_Ishine_Working_Days AS (\n"
			+ "        SELECT\n"
			+ "            emp_id,\n"
			+ "            project_id,\n"
			+ "            team_id,\n"
			+ "            employee_team_map_id,\n"
			+ "            COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "        FROM Combined_Expected_DSR\n"
			+ "        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "  Ishine_Timesheet_Summary AS (\n"
			+ "					        SELECT\n"
			+ "					            bpe.emp_id,\n"
			+ "					            bpe.project_id,\n"
			+ "					            bpe.team_id,\n"
			+ "					            bpe.employee_team_map_id,\n"
			+ "					            COUNT(DISTINCT\n"
			+ "					                CASE\n"
			+ "					                    WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING') AND ets.activity_team_id = bpe.team_id AND ets.date BETWEEN DATE(bpe.etm_start_date) AND COALESCE(DATE(bpe.etm_end_date), (SELECT to_date FROM Date_Parameters)) THEN ets.date\n"
			+ "					                    ELSE NULL\n"
			+ "					                END\n"
			+ "					            ) AS filled_ishine_days,\n"
			+ "					            COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING') AND ets.status = 'Pending' AND ets.activity_team_id = bpe.team_id AND ets.date BETWEEN DATE(bpe.etm_start_date) AND COALESCE(DATE(bpe.etm_end_date), (SELECT to_date FROM Date_Parameters)) THEN ets.date END) AS ishine_pending_Days,\n"
			+ "					            COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING') AND ets.status = 'Approved' AND ets.activity_team_id = bpe.team_id AND ets.date BETWEEN DATE(bpe.etm_start_date) AND COALESCE(DATE(bpe.etm_end_date), (SELECT to_date FROM Date_Parameters)) THEN ets.date END) AS ishine_approved_Days\n"
			+ "					        FROM Base_Project_Employees bpe\n"
			+ "					        INNER JOIN Employee_Timesheet_Statuses ets ON bpe.emp_id = ets.emp_id\n"
			+ "					            AND ets.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "					        GROUP BY bpe.emp_id, bpe.project_id, bpe.team_id, bpe.employee_team_map_id\n"
			+ "					    ),\n"
			+ "    Employee_Final_Summary AS (\n"
			+ "        SELECT\n"
			+ "            bpe.emp_id, bpe.project_id, bpe.employee_dept_id, bpe.project_type,\n"
			+ "            COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "            COALESCE(its.filled_ishine_days, 0) AS filled_ishine_timesheet_days,\n"
			+ "            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "            COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "            COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -\n"
			+ "                (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "               WHEN COALESCE(its.ishine_pending_Days, 0) > 0 or GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0)\n"
			+ "							+ COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Expected_Ishine_Working_Days eiwd ON bpe.emp_id = eiwd.emp_id AND bpe.project_id = eiwd.project_id AND bpe.team_id = eiwd.team_id AND bpe.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "        LEFT JOIN Ishine_Timesheet_Summary its ON bpe.emp_id = its.emp_id AND bpe.project_id = its.project_id AND bpe.team_id = its.team_id AND bpe.employee_team_map_id = its.employee_team_map_id\n"
			+ "    ),\n"
			+ "    Project_Level_Summary AS (\n"
			+ "        SELECT\n"
			+ "            efs.project_id,\n"
			+ "            efs.project_type,\n"
			+ "            CASE\n"
			+ "                WHEN SUM(CASE WHEN efs.employee_status = 'Defaulter' THEN 1 ELSE 0 END) > 0 THEN 'Defaulter'\n"
			+ "                WHEN SUM(CASE WHEN efs.employee_status = 'Pending' THEN 1 ELSE 0 END) > 0 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS project_status\n"
			+ "        FROM Employee_Final_Summary efs\n"
			+ "        GROUP BY efs.project_id, efs.project_type\n"
			+ "    )\n"
			+ "SELECT\n"
			+ "    COUNT(DISTINCT pls.project_id) AS total_no_of_applicable_projects,\n"
			+ "    SUM(CASE WHEN pls.project_status = 'Approved' THEN 1 ELSE 0 END) AS total_approved_projects,\n"
			+ "    SUM(CASE WHEN pls.project_status = 'Pending' THEN 1 ELSE 0 END) AS total_pending_projects,\n"
			+ "    SUM(CASE WHEN pls.project_status = 'Defaulter' THEN 1 ELSE 0 END) AS total_defaulter_projects\n"
			+ "FROM\n"
			+ "    Project_Level_Summary pls\n"
			+ "WHERE\n"
			+ "   (\n"
			+ "    'All' IN (:billableType)\n"
			+ "    OR pls.project_type IN (:billableType)\n"
			+ ") ", nativeQuery = true)
	public List<Object[]> getAllEmpTimesheetDashboardCountForProject(@Param("month") Integer month,
			@Param("year") Integer year, @Param("emp_id") Long emp_id, @Param("billableType") List<String> billableType,
			@Param("projectActive") String projectActive);

	@Query(value = " WITH RECURSIVE\n"
			+ "						    Date_Parameters AS (\n"
			+ "						        SELECT\n"
			+ "						            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "						            CASE\n"
			+ "						                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "						                    THEN CURDATE()\n"
			+ "						                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "						            END AS to_date\n"
			+ "						    ),\n"
			+ "						    All_Dates_In_Range AS (\n"
			+ "						        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "						        UNION ALL\n"
			+ "						        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "						    ),\n"
			+ " Employees_With_Target_Project_Type AS (\n"
			+ "    SELECT DISTINCT etm.emp_id\n"
			+ "    FROM employee_team_mapping etm\n"
			+ "    INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "    INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    JOIN Date_Parameters dp ON 1=1\n"
			+ "    WHERE \n"
			+ "        etm.start_date <= dp.to_date\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= dp.from_date)\n"
			+ "        AND (\n"
			+ "             'All' IN (:billableType) \n"
			+ "             \n"
			+ "             OR p.po_project_type IN (:billableType) \n"
			+ "             OR p.internal_project_type IN (:billableType) \n"
			+ "\n"
			+ "             OR (\n"
			+ "                 'TNM(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'TNM' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )             OR (\n"
			+ "                 'Fixed Cost(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'Fixed Cost' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )\n"
			+ "        )\n"
			+ "),\n "
			+ "						    auth_emp AS (\n"
			+ "						        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "						               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "						        FROM employee_team_mapping etm\n"
			+ "						        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "						        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "						        inner join employee e on etm.emp_id = e.emp_id\n"
			+ "						    ),\n"
			+ "						    Authorized_Employees AS (\n"
			+ "						        SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "						            EXISTS (SELECT 1 FROM employee u JOIN job_role jr ON u.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "						            OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "						    )),\n"
			+ "						    Authorized_Project_IDs AS (\n"
			+ "						        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "						        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "						        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "						        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "						    ),\n"
			+ "						    Employee_Timesheets_With_Activities AS (\n"
			+ "						        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "						                        a.team_id AS activity_team_id\n"
			+ "						        FROM employee_timesheets_new et\n"
			+ "						        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "						        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "						        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "						        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "						        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "						    ),\n"
			+ "						    Base_Report_Details AS (\n"
			+ "						        SELECT DISTINCT\n"
			+ "						            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "						            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "						            etm.active, p.project_id, p.project_name,\n"
			+ "						            c.client_id, c.client_name, p.po_no,\n"
			+ "						            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "						            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "						            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "						            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus, p.active as projectActive\n"
			+ "						        FROM projects p\n"
			+ "						        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "						        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "						        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "						        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ "								INNER JOIN Employees_With_Target_Project_Type target_emps ON e.emp_id = target_emps.emp_id \n"
			+ "						        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "						        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "						        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "						        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "						        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "						        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "						        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ "			  AND (\n"
			+ "					e.date_of_relieving IS NULL \n"
			+ "					OR YEAR(e.date_of_relieving) > :year \n"
			+ "					OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "				) \n"
			+ "			 AND e.emp_id not between 1 and 6 \n"
			+ "						        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "						        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ "			 AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND UPPER(e.employmentstatus) != 'INACTIVE')) \n"
			+ "						    ),\n"
			+ "						    Project_Managers_Aggregated AS (\n"
			+ "						        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "						        FROM project_manager_mapping pm\n"
			+ "						        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "						        GROUP BY pm.project_id\n"
			+ "						    ),\n"
			+ "				Daily_Status_Details AS (\n"
			+ "						        SELECT\n"
			+ "						            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "						            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "						            CASE\n"
			+ "										WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "						                    CASE\n"
			+ "						                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "						                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "						                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "						                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "						                        ELSE 'NA'\n"
			+ "						                    END\n"
			+ "										WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "						                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "						                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "						                    CASE\n"
			+ "						                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "						                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "						                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "						                        ELSE 'NA'\n"
			+ "						                    END\n"
			+ "						                WHEN EXISTS (\n"
			+ "						                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "						                ) THEN 'O'\n"
			+ "						                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "						                ELSE 'NA'\n"
			+ "						            END AS daily_status\n"
			+ "						        FROM Base_Report_Details brd\n"
			+ "						        CROSS JOIN All_Dates_In_Range adir\n"
			+ "						        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "						            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "						        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "						            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "						               AND etwa_general.activity_team_id IS NULL\n"
			+ "						    ),\n"
			+ "						    Expected_Working_Days_Detail AS (\n"
			+ "						        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "						        FROM Base_Report_Details brd\n"
			+ "						        CROSS JOIN All_Dates_In_Range adir\n"
			+ "						        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "						        AND NOT EXISTS (\n"
			+ "						            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "						            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "						            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "						        )\n"
			+ "						    ),\n"
			+ "						    Actual_Timesheet_Filled AS (\n"
			+ "						        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "						        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "						        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "						        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs) AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "						    ),\n"
			+ "						    Combined_Expected_DSR AS (\n"
			+ "						        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "						        UNION\n"
			+ "						        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "						    ),\n"
			+ "						    Expected_Ishine_Working_Days AS (\n"
			+ "						        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "						        FROM Combined_Expected_DSR\n"
			+ "						        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "						    ),\n"
			+ "			Ishine_Timesheet_Summary AS (\n"
			+ "						        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "						               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "						               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "						               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "						        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "						        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "						        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "						    ),\n"
			+ "						    Employee_Calculated_Status AS (\n"
			+ "						        SELECT\n"
			+ "						            brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "						            COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
			+ "						            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) AS client_side_not_filled_count,\n"
			+ "						            COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
			+ "						            COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
			+ "						            CASE\n"
			+ "						                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "						                WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0)\n"
			+ "						                        - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "						                ELSE 'Approved'\n"
			+ "						            END AS employee_status\n"
			+ "						        FROM Base_Report_Details brd\n"
			+ "						        LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "						        LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "						    ),\n"
			+ "						    final_select as\n"
			+ "						    (\n"
			+ "						SELECT\n"
			+ "							distinct COUNT(DISTINCT ecs.emp_id) AS total_no_of_applicable_employees,\n"
			+ "						    COUNT(distinct CASE WHEN ecs.employee_status = 'Approved' THEN ecs.emp_id  END) AS total_approved_employees,\n"
			+ "						    COUNT(distinct CASE WHEN ecs.employee_status = 'Pending' THEN ecs.emp_id  END) AS total_pending_employees,\n"
			+ "						    COUNT(distinct CASE WHEN ecs.employee_status = 'Defaulter' THEN ecs.emp_id  END) AS total_defaulter_employees,\n"
			+ "                            COUNT(distinct CASE WHEN ecs.employee_status in ('Defaulter','Pending') THEN ecs.emp_id  END) as defaulter_employees\n"
			+ "						FROM Base_Report_Details brd\n"
			+ "						LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "						LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "						LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "						LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "						LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "						WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "						    )\n"
			+ "						     SELECT \n"
			+ "						    sum(DISTINCT total_no_of_applicable_employees) AS total_no_of_applicable_employees,\n"
			+ "						    sum(total_approved_employees) as total_approved_employees\n"
			+ "						    ,sum(total_pending_employees) as total_pending_employees\n"
			+ "						    ,sum(total_defaulter_employees) as total_defaulter_employees\n"
			+ "                            ,sum(defaulter_employees) as defaulter_employees\n"
			+ "						    from final_select ", nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForAllEmployee(@Param("month") Integer month,
			@Param("year") Integer year, @Param("emp_id") Long emp_id,
			@Param("billableType") List<String> billableTypes, @Param("employeeActive") String employeeActive);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "Employees_With_Target_Project_Type AS (\n"
			+ "    SELECT DISTINCT etm.emp_id\n"
			+ "    FROM employee_team_mapping etm\n"
			+ "    INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "    INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    JOIN Date_Parameters dp ON 1=1\n"
			+ "    WHERE \n"
			+ "        etm.start_date <= dp.to_date\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= dp.from_date)\n"
			+ "        AND (\n"
			+ "             'All' IN (:billableType) \n"
			+ "             \n"
			+ "             OR p.po_project_type IN (:billableType) \n"
			+ "             OR p.internal_project_type IN (:billableType) \n"
			+ "\n"
			+ "             OR (\n"
			+ "                 'TNM(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'TNM' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )             OR (\n"
			+ "                 'Fixed Cost(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'Fixed Cost' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )\n"
			+ "        )\n"
			+ "),\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        inner join employee e on etm.emp_id = e.emp_id\n"
			+ "    ),\n"
			+ " Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id\n"
			+ "        FROM employee e\n"
			+ "        WHERE (\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id\n"
			+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            e.job_role_id IN (\n"
			+ "                SELECT jr.job_role_id\n"
			+ "                FROM job_role jr\n"
			+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "            )\n"
			+ "            OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "    )), \n"
			+ "    Authorized_Project_IDs AS (\n"
			+ "        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "    ),\n"
			+ "    Employee_Timesheets_With_Activities AS (\n"
			+ "        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "                        a.team_id AS activity_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "    ),\n"
			+ "    Base_Report_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "            etm.active, p.project_id, p.project_name,p.active as projectActive,\n"
			+ "            c.client_id, c.client_name, p.po_no,\n"
			+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ " INNER JOIN Employees_With_Target_Project_Type target_emps ON e.emp_id = target_emps.emp_id"
			+ "        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ " 	  AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND UPPER(e.employmentstatus) != 'INACTIVE')) \n"
			+ "    ),\n"
			+ "    Project_Managers_Aggregated AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ " Daily_Status_Details AS (\n"
			+ "			        SELECT\n"
			+ "			            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "			            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "			            CASE\n"
			+ "							WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "			                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "							WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "			                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "			                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "			                    CASE\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "			                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "			                        ELSE 'NA'\n"
			+ "			                    END\n"
			+ "			                WHEN EXISTS (\n"
			+ "			                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "			                ) THEN 'O'\n"
			+ "			                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "			                ELSE 'NA'\n"
			+ "			            END AS daily_status\n"
			+ "			        FROM Base_Report_Details brd\n"
			+ "			        CROSS JOIN All_Dates_In_Range adir\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "			            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "			        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "			            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "			               AND etwa_general.activity_team_id IS NULL\n"
			+ "			    ),\n"
			+ "    Expected_Working_Days_Detail AS (\n"
			+ "        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Actual_Timesheet_Filled AS (\n"
			+ "        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs) AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "    ),\n"
			+ "    Combined_Expected_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "    ),\n"
			+ "    Expected_Ishine_Working_Days AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "        FROM Combined_Expected_DSR\n"
			+ "        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "    ),\n"
			+ " Ishine_Timesheet_Summary AS (\n"
			+ "			        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "			        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "    Employee_Calculated_Status AS (\n"
			+ "        SELECT distinct\n"
			+ "            brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "            COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
			+ "            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) AS client_side_not_filled_count,\n"
			+ "            COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
			+ "            COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "                WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0)\n"
			+ "                        - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "        LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "    )\n"
			+ "SELECT DISTINCT brd.emp_id\n"
			+ "FROM Base_Report_Details brd\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ " AND (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ " AND (:employmentId IS NULL OR LOWER(brd.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(brd.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(brd.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(brd.billable_type) = :billableType2)\n"
			+ " AND (:projectName IS NULL OR LOWER(brd.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(brd.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(brd.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(brd.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pma.Project_Manager_Names) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(brd.team_name) LIKE CONCAT('%', :teamName, '%'))\n"
			+ " AND (:projectStatus IS NULL OR LOWER(brd.active) LIKE CONCAT('%', :projectStatus, '%'))\n"
			+ "    LIMIT :offset, :pageSize", nativeQuery = true)
	List<Long> getPaginatedEmployeeIds(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id,
			@Param("billableType") List<String> billableType,
			@Param("status") String status,
			@Param("employeeActive") String employeeActive,
			String employmentId, String clientsideId, String employeeName, String billableType2, String projectName,
			String poNo,
			String projectManagers, String clientName, String teamName, String department, String projectStatus,
			int offset, int pageSize);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ " Employees_With_Target_Project_Type AS (\n"
			+ "    SELECT DISTINCT etm.emp_id\n"
			+ "    FROM employee_team_mapping etm\n"
			+ "    INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "    INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    JOIN Date_Parameters dp ON 1=1\n"
			+ "    WHERE \n"
			+ "        etm.start_date <= dp.to_date\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= dp.from_date)\n"
			+ "        AND (\n"
			+ "             'All' IN (:billableType) \n"
			+ "             \n"
			+ "             OR p.po_project_type IN (:billableType) \n"
			+ "             OR p.internal_project_type IN (:billableType) \n"
			+ "\n"
			+ "             OR (\n"
			+ "                 'TNM(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'TNM' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )             OR (\n"
			+ "                 'Fixed Cost(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'Fixed Cost' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )\n"
			+ "        )\n"
			+ "),\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        inner join employee e on etm.emp_id = e.emp_id\n"
			+ "    ),\n"
			+ " Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id\n"
			+ "        FROM employee e\n"
			+ "        WHERE (\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id\n"
			+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            e.job_role_id IN (\n"
			+ "                SELECT jr.job_role_id\n"
			+ "                FROM job_role jr\n"
			+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "            )\n"
			+ "            OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "    )), \n"
			+ "    Authorized_Project_IDs AS (\n"
			+ "        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "    ),\n"
			+ "    Employee_Timesheets_With_Activities AS (\n"
			+ "        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "                        a.team_id AS activity_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "    ),\n"
			+ "    Base_Report_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "            etm.active, p.project_id, p.project_name,p.active as projectActive,\n"
			+ "            c.client_id, c.client_name, p.po_no,\n"
			+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ " INNER JOIN Employees_With_Target_Project_Type target_emps ON e.emp_id = target_emps.emp_id"
			+ "        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ " 	  AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND UPPER(e.employmentstatus) != 'INACTIVE')) \n"
			+ "    ),\n"
			+ "    Project_Managers_Aggregated AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "    Daily_Status_Details AS (\n"
			+ "        SELECT\n"
			+ "            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "            CASE\n"
			+ "                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "                    CASE\n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "                        ELSE 'NA'\n"
			+ "                    END\n"
			+ "                WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "                    CASE\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "                        ELSE 'NA'\n"
			+ "                    END\n"
			+ "                WHEN EXISTS (\n"
			+ "                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "                ) THEN 'O'\n"
			+ "                WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "                ELSE 'NA'\n"
			+ "            END AS daily_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "               AND etwa_general.activity_team_id IS NULL\n"
			+ "    ),\n"
			+ "    Expected_Working_Days_Detail AS (\n"
			+ "        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Actual_Timesheet_Filled AS (\n"
			+ "        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ " AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) \n"
			+ "    ),\n"
			+ "    Combined_Expected_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "    ),\n"
			+ "    Expected_Ishine_Working_Days AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "        FROM Combined_Expected_DSR\n"
			+ "        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "  Ishine_Timesheet_Summary AS (\n"
			+ "			        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "			        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "    Employee_Calculated_Status AS (\n"
			+ "        SELECT distinct\n"
			+ "            brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "            COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
			+ "            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) AS client_side_not_filled_count,\n"
			+ "            COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
			+ "            COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "                WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0)\n"
			+ "                        - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "        LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "    )\n"
			+ "SELECT  SQL_CALC_FOUND_ROWS distinct\n"
			+ "    brd.emp_id, brd.client_side_id, brd.start_date, brd.team_name, brd.team_id,\n"
			+ "    brd.name, brd.spoc, brd.billable_type, brd.employee_role, brd.dept_name, brd.project_id,\n"
			+ "    brd.project_name, pma.Project_Manager_Names, brd.po_no, brd.client_name,\n"
			+ "    brd.reporting_manager_id,\n"
			+ "    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
			+ "    COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "    GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "    COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "    COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_in_time END) AS `1_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.office_out_time END) AS `1_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_in_time END) AS `2_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.office_out_time END) AS `2_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_in_time END) AS `3_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.office_out_time END) AS `3_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_in_time END) AS `4_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.office_out_time END) AS `4_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_in_time END) AS `5_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.office_out_time END) AS `5_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_in_time END) AS `6_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.office_out_time END) AS `6_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_in_time END) AS `7_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.office_out_time END) AS `7_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_in_time END) AS `8_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.office_out_time END) AS `8_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_in_time END) AS `9_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.office_out_time END) AS `9_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_in_time END) AS `10_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.office_out_time END) AS `10_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_in_time END) AS `11_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.office_out_time END) AS `11_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_in_time END) AS `12_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.office_out_time END) AS `12_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_in_time END) AS `13_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.office_out_time END) AS `13_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_in_time END) AS `14_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.office_out_time END) AS `14_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_in_time END) AS `15_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.office_out_time END) AS `15_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_in_time END) AS `16_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.office_out_time END) AS `16_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_in_time END) AS `17_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.office_out_time END) AS `17_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_in_time END) AS `18_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.office_out_time END) AS `18_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_in_time END) AS `19_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.office_out_time END) AS `19_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_in_time END) AS `20_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.office_out_time END) AS `20_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_in_time END) AS `21_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.office_out_time END) AS `21_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_in_time END) AS `22_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.office_out_time END) AS `22_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_in_time END) AS `23_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.office_out_time END) AS `23_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_in_time END) AS `24_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.office_out_time END) AS `24_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_in_time END) AS `25_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.office_out_time END) AS `25_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_in_time END) AS `26_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.office_out_time END) AS `26_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_in_time END) AS `27_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.office_out_time END) AS `27_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_in_time END) AS `28_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.office_out_time END) AS `28_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_in_time END) AS `29_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.office_out_time END) AS `29_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_in_time END) AS `30_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.office_out_time END) AS `30_office_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_in_time END) AS `31_office_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.office_out_time END) AS `31_office_out_time`,\n"
			+ "    brd.employement_id,\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "    0 AS 'Comp_Off',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "    0 AS 'Half_Day',\n"
			+ "    (SUM(CASE WHEN dsd.daily_status IN ('AP', 'PE', 'NW') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) + SUM(CASE WHEN dsd.daily_status IN ('A','O','NA') THEN 1 ELSE 0 END)) AS total_days,\n"
			+ "    (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0)) as ishine_filled_days,\n"
			+ "    brd.employmentstatus, brd.end_date,\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "    brd.active,brd.projectActive\n"
			+ "FROM Base_Report_Details brd\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ "AND brd.emp_id IN (:employeeIds)\n"
			+ "AND (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ " AND (:employmentId IS NULL OR LOWER(brd.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(brd.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(brd.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(brd.billable_type) = :billableType2)\n"
			+ " AND (:projectName IS NULL OR LOWER(brd.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(brd.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(brd.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(brd.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pma.Project_Manager_Names) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(brd.team_name) LIKE CONCAT('%', :teamName, '%'))\n"
			+ " AND (:projectStatus IS NULL OR LOWER(brd.active) LIKE CONCAT('%', :projectStatus, '%'))\n"
			+ "GROUP BY\n"
			+ "    brd.emp_id, brd.employee_team_map_id, brd.project_id, brd.team_id, brd.name,\n"
			+ "    pma.Project_Manager_Names, expected_ishine_timesheet_days, not_filled_ishine_timesheet_days,\n"
			+ "    ishine_pending_Days, ishine_approved_Days,brd.employement_id,brd.employmentstatus, brd.end_date,brd.active,brd.projectActive\n"
			+ " ORDER BY CASE WHEN :sortDirection = 'asc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'employeeName' THEN brd.name\n"
			+ "            WHEN :sortBy = 'employmentStatus' THEN brd.employmentstatus\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN brd.active\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'billable_type' THEN brd.billable_type\n"
			+ "            WHEN :sortBy = 'clientName' THEN brd.client_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_name' THEN brd.project_name\n"
			+ "            WHEN :sortBy = 'projectManagerName' THEN pma.Project_Manager_Names\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'startDate' THEN brd.start_date\n"
			+ "            WHEN :sortBy = 'endDate' THEN brd.end_date\n"
			+ "            WHEN :sortBy = 'expectedTimesheetFillCount' THEN ecs.expectedTimesheetFillCount\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN brd.active\n"
			+ "            ELSE brd.name\n"
			+ "        END\n"
			+ "    END ASC,\n"
			+ "    CASE WHEN :sortDirection = 'desc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'employeeName' THEN brd.name\n"
			+ "            WHEN :sortBy = 'employmentStatus' THEN brd.employmentstatus\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN brd.active\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'billable_type' THEN brd.billable_type\n"
			+ "            WHEN :sortBy = 'clientName' THEN brd.client_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_name' THEN brd.project_name\n"
			+ "            WHEN :sortBy = 'projectManagerName' THEN pma.Project_Manager_Names\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'startDate' THEN brd.start_date\n"
			+ "            WHEN :sortBy = 'endDate' THEN brd.end_date\n"
			+ "            WHEN :sortBy = 'expectedTimesheetFillCount' THEN ecs.expectedTimesheetFillCount\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN brd.active\n"
			+ "            ELSE brd.name\n"
			+ "        END\n"
			+ "    END DESC", nativeQuery = true)
	// ========== BACKUP: Original query renamed with _old suffix ==========
	// Note: getEmployeeSummaryReportAllEMP_old would be the backup version
	// ========== UPDATED: New query using _new tables - CTE
	// Employee_Timesheets_With_Activities fixed ==========
	public List<Object[]> getEmployeeSummaryReportAllEMP(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id,
			@Param("billableType") List<String> billableType,
			@Param("status") String status,
			@Param("employeeActive") String employeeActive,
			String employmentId, String clientsideId, String employeeName, String billableType2, String projectName,
			String poNo,
			String projectManagers, String clientName, String teamName, String department, String projectStatus,
			String sortBy, String sortDirection,
			@Param("employeeIds") List<Long> employeeIds);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "Employees_With_Target_Project_Type AS (\n"
			+ "    SELECT DISTINCT etm.emp_id\n"
			+ "    FROM employee_team_mapping etm\n"
			+ "    INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "    INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "    JOIN Date_Parameters dp ON 1=1\n"
			+ "    WHERE \n"
			+ "        etm.start_date <= dp.to_date\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= dp.from_date)\n"
			+ "        AND (\n"
			+ "             'All' IN (:billableType) \n"
			+ "             \n"
			+ "             OR p.po_project_type IN (:billableType) \n"
			+ "             OR p.internal_project_type IN (:billableType) \n"
			+ "\n"
			+ "             OR (\n"
			+ "                 'TNM(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'TNM' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )             OR (\n"
			+ "                 'Fixed Cost(Shadow)' IN (:billableType) \n"
			+ "                 AND p.po_project_type = 'Fixed Cost' \n"
			+ "                 AND etm.is_shadow = 1\n"
			+ "             )\n"
			+ "        )\n"
			+ "),\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        inner join employee e on etm.emp_id = e.emp_id\n"
			+ "    ),\n"
			+ " Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id\n"
			+ "        FROM employee e\n"
			+ "        WHERE (\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id\n"
			+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            e.job_role_id IN (\n"
			+ "                SELECT jr.job_role_id\n"
			+ "                FROM job_role jr\n"
			+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "            )\n"
			+ "            OR EXISTS (SELECT 1 FROM employee_team_mapping etm INNER JOIN teams t ON etm.team_id = t.team_id INNER JOIN job_role emp_jr ON e.job_role_id = emp_jr.job_role_id INNER JOIN employee user_e ON user_e.emp_id = :emp_id INNER JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id LEFT JOIN project_manager_mapping pmm ON t.project_id = pmm.project_id AND pmm.project_manager_id = :emp_id LEFT JOIN project_overhead_mapping pom ON t.project_id = pom.project_id AND pom.project_overhead_id = :emp_id WHERE etm.emp_id = e.emp_id AND (pmm.project_manager_id IS NOT NULL OR pom.project_overhead_id IS NOT NULL) AND emp_jr.dept_id = user_jr.dept_id)\n"
			+ "    )), \n"
			+ "    Authorized_Project_IDs AS (\n"
			+ "        SELECT DISTINCT p.project_id FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON etm.emp_id = ae.emp_id\n"
			+ "    ),\n"
			+ "    Employee_Timesheets_With_Activities AS (\n"
			+ "        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
			+ "                        a.team_id AS activity_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
			+ "    ),\n"
			+ "    Base_Report_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, e.billable,\n"
			+ "            etm.active, p.project_id, p.project_name,p.active as projectActive,\n"
			+ "            c.client_id, c.client_name, p.po_no,\n"
			+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "            e.reporting_manager_id, ecsm.client_side_id,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "            d.name dept_name, e.email, e.mobile_no, p.apmosysrm, p.apmosys_rm_email, e.employmentstatus\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON ae.emp_id = e.emp_id\n"
			+ " 	   INNER JOIN Employees_With_Target_Project_Type target_emps ON e.emp_id = target_emps.emp_id"
			+ "        LEFT JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "        LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "        LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.project_id IN (SELECT project_id FROM Authorized_Project_IDs) \n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND etm.start_date <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR etm.end_date >= (SELECT from_date FROM Date_Parameters))\n"
			+ " 	  AND (:employeeActive = 'All' OR (:employeeActive = 'InActive' AND UPPER(e.employmentstatus) = 'INACTIVE') OR (:employeeActive != 'InActive' AND UPPER(e.employmentstatus) != 'INACTIVE')) \n"
			+ "    ),\n"
			+ "    Project_Managers_Aggregated AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "    Daily_Status_Details AS (\n"
			+ "        SELECT\n"
			+ "            brd.emp_id, brd.project_id, brd.team_id, adir.dt AS timesheet_date,\n"
			+ "            etwa_team.office_in_time, etwa_team.office_out_time, brd.employee_team_map_id,\n"
			+ "            CASE\n"
			+ "                WHEN (brd.end_date IS NOT NULL AND adir.dt > brd.end_date) THEN 'NA'\n"
			+ "                WHEN etwa_team.emp_id IS NOT NULL AND etwa_team.activity_team_id = brd.team_id THEN\n"
			+ "                    CASE\n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Approved' THEN 'AP'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) LIKE '%WORKING%' AND etwa_team.status = 'Pending' THEN 'PE'\n"
			+ "                        WHEN UPPER(etwa_team.day_type) = 'NON-WORKING' THEN 'NW'\n"
			+ "                        ELSE 'NA'\n"
			+ "                    END\n"
			+ "                WHEN etwa_general.emp_id IS NOT NULL AND etwa_general.activity_team_id IS NULL THEN\n"
			+ "                    CASE\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'PUBLIC HOLIDAY' THEN 'AH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) = 'CLIENT HOLIDAY' THEN 'CH'\n"
			+ "                        WHEN UPPER(etwa_general.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "                        ELSE 'NA'\n"
			+ "                    END\n"
			+ "                WHEN EXISTS (\n"
			+ "                    SELECT 1 FROM Employee_Timesheets_With_Activities o WHERE o.emp_id = brd.emp_id AND o.date = adir.dt AND o.activity_team_id IS NOT NULL AND o.activity_team_id != brd.team_id\n"
			+ "                ) THEN 'O'\n"
			+ "                WHEN adir.dt < brd.start_date THEN 'O'\n"
			+ "                WHEN adir.dt <= CURDATE() AND NOT EXISTS (SELECT 1 FROM Employee_Timesheets_With_Activities a WHERE a.emp_id = brd.emp_id AND a.date = adir.dt) THEN 'A' -- Absent / Not filled\n"
			+ "                ELSE 'NA'\n"
			+ "            END AS daily_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_team\n"
			+ "            ON brd.emp_id = etwa_team.emp_id AND adir.dt = etwa_team.date AND brd.team_id = etwa_team.activity_team_id\n"
			+ "        LEFT JOIN Employee_Timesheets_With_Activities etwa_general\n"
			+ "            ON brd.emp_id = etwa_general.emp_id AND adir.dt = etwa_general.date\n"
			+ "               AND etwa_general.activity_team_id IS NULL\n"
			+ "    ),\n"
			+ "    Expected_Working_Days_Detail AS (\n"
			+ "        SELECT DISTINCT brd.emp_id, brd.project_id, brd.team_id, adir.dt AS expected_working_day_date, brd.employee_team_map_id\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters))\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM Employee_Timesheets_With_Activities etwa_nested\n"
			+ "            WHERE etwa_nested.emp_id = brd.emp_id AND etwa_nested.date = adir.dt\n"
			+ "            AND (etwa_nested.day_type LIKE '%Leave%' OR UPPER(etwa_nested.day_type) LIKE '%HOLIDAY%' OR UPPER(etwa_nested.day_type) LIKE '%WEEK%OFF%')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Actual_Timesheet_Filled AS (\n"
			+ "        SELECT DISTINCT etwa.emp_id, brd.project_id, brd.team_id, etwa.date AS dt, brd.employee_team_map_id\n"
			+ "        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "        INNER JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id AND etwa.activity_team_id = brd.team_id\n"
			+ "        WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ " AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) \n"
			+ "    ),\n"
			+ "    Combined_Expected_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, expected_working_day_date AS dt, employee_team_map_id FROM Expected_Working_Days_Detail\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, team_id, dt, employee_team_map_id FROM Actual_Timesheet_Filled\n"
			+ "    ),\n"
			+ "    Expected_Ishine_Working_Days AS (\n"
			+ "        SELECT distinct emp_id, project_id, team_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "        FROM Combined_Expected_DSR\n"
			+ "        GROUP BY emp_id, project_id, team_id, employee_team_map_id\n"
			+ "    ),\n"
			+ " Ishine_Timesheet_Summary AS (\n"
			+ "			        SELECT distinct etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS filled_ishine_days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Pending' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_pending_Days,\n"
			+ "			               COUNT(DISTINCT CASE WHEN UPPER(etwa.day_type) IN ('WORKING', 'NON-WORKING') AND etwa.status = 'Approved' AND etwa.activity_team_id = brd.team_id AND etwa.date BETWEEN DATE(brd.start_date) AND COALESCE(DATE(brd.end_date), (SELECT to_date FROM Date_Parameters)) THEN etwa.date END) AS ishine_approved_Days\n"
			+ "			        FROM Employee_Timesheets_With_Activities etwa\n"
			+ "			        JOIN Base_Report_Details brd ON etwa.emp_id = brd.emp_id\n"
			+ "			        GROUP BY etwa.emp_id, brd.project_id, brd.team_id, brd.employee_team_map_id\n"
			+ "			    ),\n"
			+ "    Employee_Calculated_Status AS (\n"
			+ "        SELECT distinct\n"
			+ "            brd.emp_id, brd.project_id, brd.employee_team_map_id,\n"
			+ "            COALESCE(eiwd.expected_ishine_days, 0) AS expectedTimesheetFillCount,\n"
			+ "            GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) AS client_side_not_filled_count,\n"
			+ "            COALESCE(its.ishine_pending_Days, 0) AS clientSidePendingCount,\n"
			+ "            COALESCE(its.ishine_approved_Days, 0) AS clientSideApprovedCount,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "                WHEN COALESCE(its.ishine_pending_Days, 0) > 0 OR GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0)\n"
			+ "                        - (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 1 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Report_Details brd\n"
			+ "        LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "        LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "    )\n"
			+ "SELECT COUNT(DISTINCT brd.emp_id)\n"
			+ "FROM Base_Report_Details brd\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON brd.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.employee_team_map_id = eiwd.employee_team_map_id\n"
			+ "LEFT JOIN Ishine_Timesheet_Summary its ON brd.employee_team_map_id = its.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers_Aggregated pma ON brd.project_id = pma.project_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON brd.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "WHERE brd.project_id IN (SELECT project_id FROM Authorized_Project_IDs)\n"
			+ " AND (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ " AND (:employmentId IS NULL OR LOWER(brd.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(brd.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(brd.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(brd.billable_type) = :billableType2)\n"
			+ " AND (:projectName IS NULL OR LOWER(brd.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(brd.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(brd.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(brd.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pma.Project_Manager_Names) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(brd.team_name) LIKE CONCAT('%', :teamName, '%'))"
			+ " AND (:projectStatus IS NULL OR LOWER(brd.active) LIKE CONCAT('%', :projectStatus, '%'))", nativeQuery = true)
	Integer getTotalEmployeeCount(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long emp_id,
			@Param("billableType") List<String> billableType,
			@Param("status") String status,
			@Param("employeeActive") String employeeActive,
			String employmentId, String clientsideId, String employeeName, String billableType2, String projectName,
			String poNo,
			String projectManagers, String clientName, String teamName, String department, String projectStatus);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ "\n"
			+ " Authorized_Employees AS (\n"
			+ "	SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "		EXISTS (SELECT 1 FROM employee u \n"
			+ "				JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "				JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "				WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "				OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "				WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
			+ ")),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead,\n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "                                        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "                                        ELSE CONCAT('A-',e.employeement_id)\n"
			+ "                                    END AS employement_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id\n"
			+ "			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "			AND (\n"
			+ "			ae.emp_id IS NOT NULL \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "				AND jr.dept_id = user_jr.dept_id\n"
			+ "			)\n"
			+ "		)\n"
			+ "  AND (\n"
			+ "		e.date_of_relieving IS NULL \n"
			+ "		OR YEAR(e.date_of_relieving) > :year \n"
			+ "		OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ "	) \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters)) AND (\n"
			+ "						:clientSideFilter = 'ALL'\n"
			+ "						OR (:clientSideFilter = 'true' AND p.client_flag = 1)\n"
			+ "						OR (:clientSideFilter = 'false' AND (p.client_flag = 0 OR p.client_flag IS NULL))\n"
			+ "						)\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters)) AND adir.dt >= (SELECT from_date FROM Date_Parameters)\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details tdd\n"
			+ "        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT tdd2.timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status = csm2.status_id WHERE upper(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "Daily_Status_Details AS (\n"
			+ "    SELECT distinct\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        adir.dt AS timesheet_date,\n"
			+ "        ts_data_relevant.client_in_time, ts_data_relevant.client_out_time, ts_data_relevant.shadow_emp_id,\n"
			+ "        CASE\n"
			+ "            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
			+ "			WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A'				\n"
			+ "            WHEN UPPER(global_dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "            WHEN UPPER(global_dtm.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "            WHEN UPPER(global_dtm.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "            WHEN UPPER(global_dtm.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "                 ) THEN 'O'\n"
			+ "            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "            ELSE 'NA'\n"
			+ "        END AS daily_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "                                            AND adir.dt = global_ts.date\n"
			+ "    LEFT JOIN day_type_master_new global_dtm ON global_ts.day_type_id = global_dtm.day_type_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "                                                             AND doc_approved.final_flag = 1\n"
			+ "                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status = csm2.status_id WHERE tdd2.timesheet_id = doc_pending.timesheet_id AND UPPER(csm2.status) = 'APPROVED')\n"
			+ "                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ "),\n"
			+ "Employee_Calculated_Status AS (\n"
			+ "    SELECT\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "        GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "        COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "        COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "        CASE\n"
			+ "            WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "            WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0, COALESCE(wds.expected_fill_count, 0) - \n"
			+ "            (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1 THEN 'Pending'\n"
			+ "            ELSE 'Approved'\n"
			+ "        END AS employee_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ ")\n"
			+ "SELECT SQL_CALC_FOUND_ROWS distinct\n"
			+ "    bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name, bpe.team_id,\n"
			+ "    CASE WHEN bpe.billable_type = 'Shadow' AND s_emp.name IS NOT NULL THEN CONCAT(bpe.name, ' (Shadow for ', s_emp.name, ')') ELSE bpe.name END AS name,\n"
			+ "    bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name, bpe.reporting_manager_id,\n"
			+ "    MONTHNAME(dp.from_date) AS month_name,\n"
			+ "    ecs.expectedTimesheetFillCount,\n"
			+ "    ecs.client_side_not_filled_count,\n"
			+ "    ecs.clientSidePendingCount,\n"
			+ "    ecs.clientSideApprovedCount,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.daily_status END), 'NA') AS `1`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.daily_status END), 'NA') AS `2`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.daily_status END), 'NA') AS `3`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.daily_status END), 'NA') AS `4`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.daily_status END), 'NA') AS `5`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.daily_status END), 'NA') AS `6`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.daily_status END), 'NA') AS `7`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.daily_status END), 'NA') AS `8`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.daily_status END), 'NA') AS `9`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
			+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`, MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`,\n"
			+ "    bpe.employement_id,\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA', 'CN', 'P') THEN 1 ELSE 0 END) AS 'Present',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'WO' THEN 1 ELSE 0 END) AS 'WeekOff',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('AH', 'CH') THEN 1 ELSE 0 END) AS 'Holiday',\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'L' THEN 1 ELSE 0 END) AS 'Leave',\n"
			+ "    0 AS 'Comp_Off',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('A','O') THEN 1 ELSE 0 END) AS 'NA_Count',\n"
			+ "    0 AS 'Half_Day',\n"
			+ "    SUM(CASE WHEN dsd.daily_status IN ('CA','CN','P','WO','AH','CH','L','A','O') THEN 1 ELSE 0 END) AS total_days,\n"
			+ "    (COALESCE(ecs.clientSideApprovedCount, 0) + COALESCE(ecs.clientSidePendingCount, 0)) as ishine_filled_days,\n"
			+ "    bpe.employmentstatus,bpe.end_date,\n"
			+ "    SUM(CASE WHEN dsd.daily_status = 'CA' THEN 1 ELSE 0 END) AS 'Ready_for_invoicing',\n"
			+ "    bpe.active,ecs.employee_status\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "WHERE (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ "AND bpe.emp_id IN (:employeeIds)\n"
			+ " AND (:employmentId IS NULL OR LOWER(bpe.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(bpe.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(bpe.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(bpe.billable_type) LIKE CONCAT('%',:billableType2,'%'))\n"
			+ " AND (:projectName IS NULL OR LOWER(bpe.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(bpe.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(bpe.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(bpe.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pm.project_manager_name) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(bpe.team_name) LIKE CONCAT('%', :teamName, '%'))\n"
			+ " AND (:employmentStatus IS NULL OR LOWER(bpe.employmentstatus) LIKE CONCAT('%', :employmentStatus, '%'))\n"
			+ " AND (:projectStatus IS NULL OR LOWER(bpe.active) LIKE CONCAT('%', :projectStatus, '%'))\n"
			+ "GROUP BY\n"
			+ "    bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "    name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
			+ "    bpe.project_name, pm.project_manager_name, bpe.po_no, bpe.client_name,\n"
			+ "    bpe.reporting_manager_id, bpe.client_side_id, bpe.start_date, bpe.end_date,\n"
			+ "    bpe.team_name, bpe.team_id, bpe.employmentstatus, month_name,\n"
			+ "    ecs.expectedTimesheetFillCount, ecs.client_side_not_filled_count, ecs.clientSidePendingCount, ecs.clientSideApprovedCount\n"
			+ "     ORDER BY CASE WHEN :sortDirection = 'asc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'clientSideId' THEN bpe.client_side_id\n"
			+ "            WHEN :sortBy = 'employeeName' THEN bpe.name\n"
			+ "            WHEN :sortBy = 'employmentStatus' THEN bpe.employmentstatus\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN bpe.active\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'billable_type' THEN bpe.billable_type\n"
			+ "            WHEN :sortBy = 'clientName' THEN bpe.client_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_name' THEN bpe.project_name\n"
			+ "            WHEN :sortBy = 'projectManagerName' THEN pm.project_manager_name\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'startDate' THEN bpe.start_date\n"
			+ "            WHEN :sortBy = 'endDate' THEN bpe.end_date\n"
			+ "            WHEN :sortBy = 'expectedTimesheetFillCount' THEN ecs.expectedTimesheetFillCount\n"
			+ "            ELSE bpe.name\n"
			+ "        END\n"
			+ "    END ASC,\n"
			+ "    CASE WHEN :sortDirection = 'desc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'clientSideId' THEN bpe.client_side_id\n"
			+ "            WHEN :sortBy = 'employeeName' THEN bpe.name\n"
			+ "            WHEN :sortBy = 'employmentStatus' THEN bpe.employmentstatus\n"
			+ "            WHEN :sortBy = 'projectStatus' THEN bpe.active\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'billable_type' THEN bpe.billable_type\n"
			+ "            WHEN :sortBy = 'clientName' THEN bpe.client_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_name' THEN bpe.project_name\n"
			+ "            WHEN :sortBy = 'projectManagerName' THEN pm.project_manager_name\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'startDate' THEN bpe.start_date\n"
			+ "            WHEN :sortBy = 'endDate' THEN bpe.end_date\n"
			+ "            WHEN :sortBy = 'expectedTimesheetFillCount' THEN ecs.expectedTimesheetFillCount\n"
			+ "            ELSE bpe.name\n"
			+ "        END\n"
			+ "    END DESC", nativeQuery = true)
	public List<Object[]> getEmployeeViewForClientAttendanceStatus(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId,
			@Param("status") String status,
			@Param("clientSideFilter") String clientSideFilter, String employmentId, String clientsideId,
			String employeeName,
			String billableType2, String projectName, String poNo,
			String projectManagers, String clientName, String teamName, String department,
			String employmentStatus, String projectStatus,
			String sortBy, String sortDirection, @Param("employeeIds") List<Long> employeeIds);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ "\n"
			+ "    Authorized_Employees AS (\n"
			+ "SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "	EXISTS (SELECT 1 FROM employee u \n"
			+ "			JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "			JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "			WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "			OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "			WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ ")),\n"
			+ "\n"
			+ "   Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead,\n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "                                        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "                                        ELSE CONCAT('A-',e.employeement_id)\n"
			+ "                                    END AS employement_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "		LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "		LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "		JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "		JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id		\n"
			+ "		LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "		LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "		AND (\n"
			+ "		ae.emp_id IS NOT NULL \n"
			+ "		OR \n"
			+ "		(\n"
			+ "			(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "			AND jr.dept_id = user_jr.dept_id\n"
			+ "		)\n"
			+ "	)\n"
			+ "  AND (\n"
			+ "	e.date_of_relieving IS NULL \n"
			+ "	OR YEAR(e.date_of_relieving) > :year \n"
			+ "	OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ ") \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters)) AND (\n"
			+ "					:clientSideFilter = 'ALL'\n"
			+ "					OR (:clientSideFilter = 'true' AND p.client_flag = 1)\n"
			+ "					OR (:clientSideFilter = 'false' AND (p.client_flag = 0 OR p.client_flag IS NULL))\n"
			+ "					)\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters)) AND adir.dt >= (SELECT from_date FROM Date_Parameters)\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details tdd\n"
			+ "        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE UPPER(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "Daily_Status_Details AS (\n"
			+ "    SELECT distinct\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        adir.dt AS timesheet_date,\n"
			+ "        pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
			+ "        CASE\n"
			+ "            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
			+ "			WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A'				\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "                 ) THEN 'O'\n"
			+ "            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "            ELSE 'NA'\n"
			+ "        END AS daily_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    LEFT JOIN employee_timesheets global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "                                            AND adir.dt = global_ts.date\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "                                                             AND doc_approved.final_flag = 1\n"
			+ "                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd3 LEFT JOIN client_status_master_new csm3 ON tdd3.client_approval_status_id = csm3.status_id WHERE tdd3.timesheet_id = doc_pending.timesheet_id AND UPPER(csm3.status) = 'APPROVED')\n"
			+ "                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ "),\n"
			+ "Employee_Calculated_Status AS (\n"
			+ "    SELECT\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "        GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "        COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "        COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "        CASE\n"
			+ "            WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "            WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0, COALESCE(wds.expected_fill_count, 0) - \n"
			+ "            (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1 THEN 'Pending'\n"
			+ "            ELSE 'Approved'\n"
			+ "        END AS employee_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ ")\n"
			+ "SELECT COUNT(DISTINCT bpe.emp_id)\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "WHERE (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ " AND (:employmentId IS NULL OR LOWER(bpe.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(bpe.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(bpe.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(bpe.billable_type) LIKE CONCAT('%', :billableType2,'%'))\n"
			+ " AND (:projectName IS NULL OR LOWER(bpe.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(bpe.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(bpe.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(bpe.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pm.project_manager_name) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(bpe.team_name) LIKE CONCAT('%', :teamName, '%'))\n"
			+ " AND (:employmentStatus IS NULL OR LOWER(bpe.employmentstatus) LIKE CONCAT('%', :employmentStatus, '%'))\n"
			+ " AND (:projectStatus IS NULL OR LOWER(bpe.active) LIKE CONCAT('%', :projectStatus, '%'))", nativeQuery = true)
	Integer getTotalEmployeeCountForClientApplicable(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId,
			@Param("status") String status,
			@Param("clientSideFilter") String clientSideFilter, String employmentId, String clientsideId,
			String employeeName,
			String billableType2, String projectName, String poNo,
			String projectManagers, String clientName, String teamName, String department,
			String employmentStatus, String projectStatus);

	@Query(value = " WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "                    IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "                ELSE CURDATE()\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Project_Managers AS (\n"
			+ "        SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "        FROM project_manager_mapping pm\n"
			+ "        left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "        GROUP BY pm.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    auth_emp AS (\n"
			+ "        SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "               etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "        WHERE p.has_client_side_id = TRUE\n"
			+ "    ),\n"
			+ " Authorized_Employees AS (\n"
			+ "SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "	EXISTS (SELECT 1 FROM employee u \n"
			+ "			JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "			JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "			WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "			OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "			WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)) \n"
			+ ")),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "            etm.active, p.project_id, p.project_name,\n"
			+ "            c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "            s.name AS spoc, tl.name AS teamLead,\n"
			+ "            e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "             CASE\n"
			+ "                                        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "                                        ELSE CONCAT('A-',e.employeement_id)\n"
			+ "                                    END AS employement_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "		LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "		LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "		JOIN employee user_e ON user_e.emp_id = :emp_id\n"
			+ "		JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id		\n"
			+ "		LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
			+ "		LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "		AND (\n"
			+ "		ae.emp_id IS NOT NULL \n"
			+ "		OR \n"
			+ "		(\n"
			+ "			(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "			AND jr.dept_id = user_jr.dept_id\n"
			+ "		)\n"
			+ "	)\n"
			+ "  AND (\n"
			+ "	e.date_of_relieving IS NULL \n"
			+ "	OR YEAR(e.date_of_relieving) > :year \n"
			+ "	OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ ") \n"
			+ " AND e.emp_id not between 1 and 6 \n"
			+ "        AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters)) AND (\n"
			+ "					:clientSideFilter = 'ALL'\n"
			+ "					OR (:clientSideFilter = 'true' AND p.client_flag = 1)\n"
			+ "					OR (:clientSideFilter = 'false' AND (p.client_flag = 0 OR p.client_flag IS NULL))\n"
			+ "					)\n"
			+ "    ),\n"
			+ "        Timesheet_Base_Data AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,\n"
			+ "            et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
			+ "        LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id\n"
			+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
			+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary_Details AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            tbd.emp_id, tbd.project_id, tbd.employee_team_map_id,\n"
			+ "            DATE(tbd.date) AS timesheet_date,\n"
			+ "            csm.status AS client_approval_status, tdd.final_flag, tdd.active,\n"
			+ "            tbd.shadow_emp_id, tdd.timesheet_id\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id and tbd.emp_id = tdd.emp_id\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "    ),\n"
			+ "        Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT distinct bpe.emp_id, bpe.employee_team_map_id, bpe.project_id, adir.dt\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt BETWEEN DATE(bpe.start_date) AND COALESCE(DATE(bpe.end_date), (SELECT to_date FROM Date_Parameters)) AND adir.dt >= (SELECT from_date FROM Date_Parameters)\n"
			+ "        AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets_new et1\n"
			+ "            LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "            WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date\n"
			+ "            AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT emp_id, project_id, employee_team_map_id, timesheet_date AS dt\n"
			+ "        FROM Employee_Document_Summary_Details tdd\n"
			+ "        WHERE ((upper(tdd.client_approval_status) = 'APPROVED' AND tdd.final_flag = 1) OR (upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id NOT IN (SELECT timesheet_id FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE UPPER(csm2.status) = 'APPROVED'))) AND  tdd.timesheet_date <= (SELECT to_date FROM Date_Parameters) AND tdd.timesheet_date >= (SELECT from_date FROM Date_Parameters)\n"
			+ "          AND timesheet_date < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Combined_Expected_Client_Side_DSR AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "\n"
			+ "    WorkingDays_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id, COUNT(DISTINCT dt) AS expected_fill_count\n"
			+ "        FROM Combined_Expected_Client_Side_DSR\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT distinct emp_id, project_id, employee_team_map_id,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN timesheet_id END) AS approved_days,\n"
			+ "               COUNT(DISTINCT CASE WHEN UPPER(client_approval_status) = 'PENDING' AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED') THEN timesheet_id END) AS pending_days\n"
			+ "        FROM Employee_Document_Summary_Details edsd\n"
			+ "        GROUP BY emp_id, project_id, employee_team_map_id\n"
			+ "    ),\n"
			+ "\n"
			+ "Daily_Status_Details AS (\n"
			+ "    SELECT distinct\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        adir.dt AS timesheet_date,\n"
			+ "        pts.client_in_time, pts.client_out_time, pts.shadow_emp_id,\n"
			+ "        CASE\n"
			+ "            WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (adir.dt <= bpe.end_date and adir.dt >= bpe.start_date) THEN 'A'\n"
			+ "			WHEN adir.dt NOT IN (SELECT date FROM employee_timesheets_new et WHERE et.emp_id = bpe.emp_id)\n"
			+ "                 AND (bpe.end_date is null and adir.dt >= bpe.start_date) THEN 'A'				\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%PUBLIC HOLIDAY%' THEN 'AH'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%CLIENT HOLIDAY%' THEN 'CH'\n"
			+ "            WHEN UPPER(global_ts.day_type) LIKE '%LEAVE%' THEN 'L'\n"
			+ "            WHEN (ts_data_all_employee.timesheet_id IS NOT NULL\n"
			+ "                  AND (ts_data_relevant.timesheet_id IS NULL OR bpe.employee_team_map_id != ts_data_relevant.employee_team_map_id)\n"
			+ "                 ) THEN 'O'\n"
			+ "            WHEN doc_approved.timesheet_id IS NOT NULL THEN 'CA'\n"
			+ "            WHEN doc_pending.timesheet_id IS NOT NULL THEN 'CN'\n"
			+ "            WHEN ts_data_relevant.timesheet_id IS NOT NULL THEN 'P'\n"
			+ "            ELSE 'NA'\n"
			+ "        END AS daily_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    LEFT JOIN employee_timesheets global_ts ON bpe.emp_id = global_ts.emp_id\n"
			+ "                                            AND adir.dt = global_ts.date\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_relevant ON bpe.emp_id = ts_data_relevant.emp_id\n"
			+ "                                                  AND adir.dt = ts_data_relevant.date\n"
			+ "                                                  AND bpe.employee_team_map_id = ts_data_relevant.employee_team_map_id\n"
			+ "    LEFT JOIN Timesheet_Base_Data ts_data_all_employee ON bpe.emp_id = ts_data_all_employee.emp_id\n"
			+ "                                                      AND adir.dt = ts_data_all_employee.date\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_approved ON ts_data_relevant.timesheet_id = doc_approved.timesheet_id\n"
			+ "                                                             AND UPPER(doc_approved.client_approval_status) = 'APPROVED'\n"
			+ "                                                             AND doc_approved.final_flag = 1\n"
			+ "                                                             AND bpe.employee_team_map_id = doc_approved.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary_Details doc_pending ON ts_data_relevant.timesheet_id = doc_pending.timesheet_id\n"
			+ "                                                            AND UPPER(doc_pending.client_approval_status) = 'PENDING'\n"
			+ "                                                            AND NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd3 LEFT JOIN client_status_master_new csm3 ON tdd3.client_approval_status_id = csm3.status_id WHERE tdd3.timesheet_id = doc_pending.timesheet_id AND UPPER(csm3.status) = 'APPROVED')\n"
			+ "                                                            AND bpe.employee_team_map_id = doc_pending.employee_team_map_id\n"
			+ "),\n"
			+ "Employee_Calculated_Status AS (\n"
			+ "    SELECT\n"
			+ "        bpe.emp_id, bpe.project_id, bpe.employee_team_map_id,\n"
			+ "        COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "        GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS client_side_not_filled_count,\n"
			+ "        COALESCE(eds.pending_days, 0) AS clientSidePendingCount,\n"
			+ "        COALESCE(eds.approved_days, 0) AS clientSideApprovedCount,\n"
			+ "        CASE\n"
			+ "            WHEN GREATEST(0, COALESCE(wds.expected_fill_count, 0) - (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "            WHEN COALESCE(eds.pending_days, 0) > 0 or GREATEST(0, COALESCE(wds.expected_fill_count, 0) - \n"
			+ "            (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 1 THEN 'Pending'\n"
			+ "            ELSE 'Approved'\n"
			+ "        END AS employee_status\n"
			+ "    FROM Base_Project_Employees bpe\n"
			+ "    LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id\n"
			+ "    LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n"
			+ ")\n"
			+ "SELECT DISTINCT bpe.emp_id\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Daily_Status_Details dsd ON bpe.employee_team_map_id = dsd.employee_team_map_id\n"
			+ "LEFT JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "LEFT JOIN employee s_emp ON dsd.shadow_emp_id = s_emp.emp_id\n"
			+ "WHERE (\n"
			+ "			(:status IN ('All')) \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				:status IN ('Total_defaulter') \n"
			+ "				AND ecs.employee_status IN ('Defaulter', 'Pending')\n"
			+ "			)\n"
			+ "			OR \n"
			+ "			ecs.employee_status IN (:status)\n"
			+ "			)\n"
			+ " AND (:employmentId IS NULL OR LOWER(bpe.employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ " AND (:clientsideId IS NULL OR LOWER(bpe.client_side_id) LIKE CONCAT('%', :clientsideId, '%'))\n"
			+ " AND (:employeeName IS NULL OR LOWER(bpe.name) LIKE CONCAT('%', :employeeName, '%'))\n"
			+ " AND (:billableType2 IS NULL OR LOWER(bpe.billable_type) LIKE CONCAT('%', :billableType2,'%'))\n"
			+ " AND (:projectName IS NULL OR LOWER(bpe.project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ " AND (:poNo IS NULL OR LOWER(bpe.po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ " AND (:department IS NULL OR LOWER(bpe.dept_name) LIKE CONCAT('%', :department, '%'))\n"
			+ " AND (:clientName IS NULL OR LOWER(bpe.client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ " AND (:projectManagers IS NULL OR LOWER(pm.project_manager_name) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ " AND (:teamName IS NULL OR LOWER(bpe.team_name) LIKE CONCAT('%', :teamName, '%'))\n"
			+ " AND (:employmentStatus IS NULL OR LOWER(bpe.employmentstatus) LIKE CONCAT('%', :employmentStatus, '%'))\n"
			+ " AND (:projectStatus IS NULL OR LOWER(bpe.active) LIKE CONCAT('%', :projectStatus, '%'))\n"
			+ "    LIMIT :offset, :pageSize", nativeQuery = true)
	List<Long> getPaginatedEmployeeIdsForClientAttendance(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId,
			@Param("status") String status,
			@Param("clientSideFilter") String clientSideFilter, String employmentId, String clientsideId,
			String employeeName,
			String billableType2, String projectName, String poNo,
			String projectManagers, String clientName, String teamName, String department,
			String employmentStatus, String projectStatus,
			int offset, int pageSize);

	@Query(value = "WITH RECURSIVE\n"
			+ "Date_Parameters AS (\n"
			+ "	SELECT\n"
			+ "		COALESCE(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "		CASE\n"
			+ "			WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
			+ "				IF(:year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()), CURDATE(), LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d')))\n"
			+ "			ELSE CURDATE()\n"
			+ "		END AS to_date\n"
			+ "),\n"
			+ "\n"
			+ "All_Dates_In_Range AS (\n"
			+ "	SELECT from_date AS dt FROM Date_Parameters\n"
			+ "	UNION ALL\n"
			+ "	SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
			+ "),\n"
			+ "\n"
			+ "Project_Managers AS (\n"
			+ "	SELECT pm.project_id, GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
			+ "	FROM project_manager_mapping pm\n"
			+ "	left JOIN employee e ON e.emp_id = pm.project_manager_id\n"
			+ "	GROUP BY pm.project_id\n"
			+ "),\n"
			+ "\n"
			+ "auth_emp AS (\n"
			+ "	SELECT etm.emp_id, p.project_id, t.spoc_id, t.team_lead_id, t.team_id,\n"
			+ "		   etm.employee_team_map_id, date(etm.start_date) as start_date, etm.end_date\n"
			+ "	FROM employee_team_mapping etm\n"
			+ "	INNER JOIN teams t ON etm.team_id = t.team_id\n"
			+ "	INNER JOIN projects p ON t.project_id = p.project_id\n"
			+ "),\n"
			+ "\n"
			+ " Authorized_Employees AS (\n"
			+ "	SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
			+ "		EXISTS (SELECT 1 FROM employee u \n"
			+ "				JOIN job_role jr ON u.job_role_id = jr.job_role_id \n"
			+ "				JOIN department d ON jr.dept_id = d.dept_id \n"
			+ "				WHERE u.emp_id = 3 AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
			+ "				OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr \n"
			+ "				WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = 3)) \n"
			+ "	)\n"
			+ "),\n"
			+ "Base_Project_Employees AS (\n"
			+ "	SELECT DISTINCT\n"
			+ "		etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "		date(etm.start_date) as start_date, date(etm.end_date) as end_date, etm.employee_team_map_id,\n"
			+ "		etm.active, p.project_id, p.project_name,\n"
			+ "		c.client_id, c.client_name, ecsm.client_side_id, p.po_no,\n"
			+ "		s.name AS spoc, tl.name AS teamLead,\n"
			+ "		e.reporting_manager_id, e.employmentstatus, d.name AS dept_name,\n"
			+ "		 CASE\n"
			+ "									WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "									ELSE CONCAT('A-',e.employeement_id)\n"
			+ "								END AS employement_id\n"
			+ "	FROM projects p\n"
			+ "	INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "	INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "	INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "	INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
			+ "			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			JOIN employee user_e ON user_e.emp_id = 3\n"
			+ "			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id\n"
			+ "			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = 3\n"
			+ "			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = 3\n"
			+ "        LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1\n"
			+ "        WHERE p.has_client_side_id = 1 \n"
			+ "			AND (\n"
			+ "			ae.emp_id IS NOT NULL \n"
			+ "			OR \n"
			+ "			(\n"
			+ "				(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
			+ "				AND jr.dept_id = user_jr.dept_id\n"
			+ "			)\n"
			+ "		)\n"
			+ " AND (\n"
			+ "e.date_of_relieving IS NULL \n"
			+ "OR YEAR(e.date_of_relieving) > :year \n"
			+ "OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)\n"
			+ ") \n"
			+ "AND e.emp_id not between 1 and 6 \n"
			+ "	AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "	AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
			+ ")\n"
			+ "SELECT SQL_CALC_FOUND_ROWS distinct\n"
			+ "bpe.emp_id,\n"
			+ "bpe.name,\n"
			+ "bpe.project_id,\n"
			+ "bpe.project_name,bpe.po_no,\n"
			+ "bpe.employement_id\n"
			+ "FROM Base_Project_Employees bpe\n"
			+ "JOIN Date_Parameters dp ON 1=1\n"
			+ "LEFT JOIN Project_Managers pm ON bpe.project_id = pm.project_id\n"
			+ "WHERE 1=1 \n"
			+ "AND bpe.emp_id = :emp_id", nativeQuery = true)
	List<Object[]> getProjectByMonthRangeAndEmpId(
			@Param("month") Integer month,
			@Param("year") Integer year,
			@Param("emp_id") Long empId);

	@Query(value = "select p.hasClientSideId from Project p where p.projectId = :projectId")
	Boolean checkProjectIsClientApplicable(Integer projectId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT et FROM Timesheet et\n" +
			"LEFT JOIN TimesheetDocumentDetails tdd on tdd.timesheetId = et.timesheetId \n" +
			"WHERE et.empId = :empId \n" +
			"AND et.date BETWEEN :fromDate AND :toDate and et.status='Rejected'")
	List<Timesheet> getRejectedTimesheetIdByEmpAndDateRangeOLD(
			@Param("empId") Long empId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	// ========== UPDATED: New query using _new tables (JPQL - using new entities)
	// ==========
	@Query("SELECT et FROM EmployeeTimesheetsNew et\n" +
			"LEFT JOIN TimesheetDocumentDetailsNew tdd on tdd.timesheetId = et.timesheetId \n" +
			"WHERE et.empId = :empId \n" +
			"AND et.date BETWEEN :fromDate AND :toDate " +
			"AND EXISTS (SELECT 1 FROM StatusMasterNew sm WHERE sm.statusId = et.status AND sm.status = 'Rejected')")
	List<EmployeeTimesheetsNew> getRejectedTimesheetIdByEmpAndDateRange(
			@Param("empId") Long empId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	// @Query(value= "select * from timesheetDocumentDetails tdd where
	// tdd.timesheetId = :timesheet_id")
	// List<TimesheetDocumentDetails>
	// getTimeSheetDocsByTimeSheetId(@Param("timesheet_id") Long timesheet_id);

	@Query(value = "SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(e.empId, e.name)\n"
			+ "FROM Employee e\n"
			+ "WHERE \n"
			+ "    (\n"
			+ "        e.managerId = :managerId \n"
			+ "        AND (e.approvalsTo = 'Manager' OR e.approvalsTo IS NULL)\n"
			+ "    )\n"
			+ "    OR\n"
			+ "    (\n"
			+ "        e.reportingManagerId = :managerId \n"
			+ "        AND e.approvalsTo = 'Reporting Manager'\n"
			+ "    )")
	List<TimesheetDTO> getMyReportees(Long managerId);

	@Query("SELECT pts.id.projectId FROM ProjectTimesheetStatusNew pts WHERE pts.id.timesheetId = :timesheetId")
	Integer findProjectIdByTimesheetId(@Param("timesheetId") Long timesheetId);

	/*
	 * Repository / Service Method: getMyReporteesTimesheetRequests
	 *
	 * This API fetches timesheet requests for the manager's reportees with
	 * pagination, sorting, and searching.
	 *
	 * -------------------- SEARCHABLE FIELDS --------------------
	 * The search parameter (:search) performs case-insensitive partial matching on:
	 * 1. Employee Name -> e.name
	 * 2. Employment ID -> e.employeementId (with prefix 'AP-' or 'A-' depending on
	 * isApmosysProduct)
	 * 3. Day Type -> dtmn.dayType
	 * 4. Date -> etn.date
	 * 5. Project Name -> p.projectName
	 * 6. Client Name -> c.clientName
	 * 7. Client Location -> cl.clientLocation
	 * 8. PO Number -> ptsn.poNo
	 * 9. Shadow Employee Name -> es.name
	 * 10. Team Name -> t.teamName
	 * 11. Activity -> a.activity
	 *
	 * -------------------- SORTABLE FIELDS ---------------------
	 * Controlled by sortBy and sortDir parameters:
	 * 1. employeeName -> e.name
	 * 2. employmentId -> e.employeementId
	 * 3. dayType -> dtmn.dayType
	 * 4. date -> etn.date
	 * 5. appliedOn -> etn.createdOn
	 *
	 * Note:
	 * - Default sorting is etn.date DESC if sortBy or sortDir is null/empty.
	 * - Sorting is implemented via CASE-based ORDER BY in JPQL due to multiple
	 * JOINs.
	 * - Pagination is applied via Pageable (page, size).
	 *
	 * -------------------- COUNT QUERY PURPOSE -----------------
	 * - countQuery is used by Spring Data JPA to calculate the total number of
	 * records
	 * that match the filtering conditions (search, clientFilter, managerId).
	 * - It is required for proper pagination metadata (total pages, total
	 * elements).
	 * - Without countQuery, the Pageable object cannot determine the total number
	 * of pages.
	 */

	// @Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO(\n"
	// 		+ "		            etn.timesheetId, etn.empId,\n"
	// 		+ "		            CASE\n"
	// 		+ "		                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
	// 		+ "		                ELSE CONCAT('A-', e.employeementId)\n"
	// 		+ "		            END,\n"
	// 		+ "		            e.name, dtmn.dayType, etn.date, etn.isNightShift, etn.workCheckIn, etn.workCheckOut, \n"
	// 		+ "		            COUNT(DISTINCT ptsn.id.projectId), COUNT(DISTINCT etlm.locationMappingId), ab.name, etn.createdOn, \n"
	// 		+ "		            wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
	// 		+ "		            ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation,\n"
	// 		+ "		            ptsn.poNo, es.name, ptsn.status, ptsn.totalClientWorkingMinutes,\n"
	// 		+ "		            ptsn.description, a.activity, etamn.description,\n"
	// 		+ "		            etamn.durationMinutes, t.teamName,\n"
	// 		+ "		            tddn.docId, tddn.docName, tddn.finalFlag,\n"
	// 		+ "		            tddn.bulkApprovedDocId, dmtmn.mimeType\n"
	// 		+ "		        )\n"
	// 		+ "		        FROM EmployeeTimesheetsNew etn\n"
	// 		+ "		        INNER JOIN Employee e ON etn.empId = e.empId\n"
	// 		+ "		        INNER JOIN Employee ab ON ab.empId = etn.createdBy\n"
	// 		+ "		        INNER JOIN DayTypeMasterNew dtmn ON dtmn.dayTypeId = etn.dayTypeId\n"
	// 		+ "		        INNER JOIN EmployeeTimesheetLocationMapping etlm ON etlm.timesheetId = etn.timesheetId\n"
	// 		+ "		        INNER JOIN ProjectTimesheetStatusNew ptsn ON ptsn.id.locationMappingId = etlm.locationMappingId\n"
	// 		+ "		        INNER JOIN Project p ON p.projectId = ptsn.id.projectId\n"
	// 		+ "		        INNER JOIN Client c ON c.clientId = p.clientId\n"
	// 		+ "		        INNER JOIN ClientLocation cl ON cl.clientLocationId = ptsn.clientLocationId\n"
	// 		+ "		        INNER JOIN EmployeeTimesheetActivitiesMappingNew etamn ON etamn.timesheetId = etn.timesheetId\n"
	// 		+ "		        INNER JOIN Activity a ON a.activityId = etamn.activityId\n"
	// 		+ "		        INNER JOIN Team t ON t.teamId = a.teamId\n"
	// 		+ "		        INNER JOIN WorkLocationTypeMaster wltm ON wltm.workLocationTypeId = etlm.locationTypeId\n"
	// 		+ "		        LEFT JOIN TimesheetDocumentDetailsNew tddn ON tddn.timesheetId = etn.timesheetId\n"
	// 		+ "		        LEFT JOIN Employee es ON ptsn.shadowEmpId = es.empId\n"
	// 		+ "		        LEFT JOIN DocMimeTypeMasterNew dmtmn ON dmtmn.mimeTypeId = tddn.mimeTypeId\n"
	// 		+ "		        WHERE\n"
	// 		+ "		            (CASE\n"
	// 		+ "		                WHEN e.approvalsTo = 'Reporting Manager' THEN e.reportingManagerId\n"
	// 		+ "		                ELSE e.managerId\n"
	// 		+ "		            END) = :managerId\n"
	// 		+ "		            AND ptsn.status = 1\n"
	// 		+ "		            AND (\n"
	// 		+ "		                :clientFilter IS NULL\n"
	// 		+ "		                OR (:clientFilter = TRUE AND p.hasClientSideId = TRUE)\n"
	// 		+ "		                OR (:clientFilter = FALSE AND (p.hasClientSideId = FALSE OR p.hasClientSideId IS NULL))\n"
	// 		+ "		            )\n"
	// 		+ "		            AND (\n"
	// 		+ "					    :employmentId IS NULL\n"
	// 		+ "					    OR LOWER(\n"
	// 		+ "					        CASE\n"
	// 		+ "					            WHEN e.isApmosysProduct = 'true'\n"
	// 		+ "					                THEN CONCAT('AP-', e.employeementId)\n"
	// 		+ "					            ELSE CONCAT('A-', e.employeementId)\n"
	// 		+ "					        END\n"
	// 		+ "					    ) LIKE LOWER(CONCAT('%', :employmentId, '%'))\n"
	// 		+ "					)\n"
	// 		+ "					AND (:employeeName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%')))\n"
	// 		+ "					AND (:dayType IS NULL OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :dayType, '%')))\n"
	// 		+ "					AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
	// 		+ "					AND (:clientName IS NULL OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :clientName, '%')))\n"
	// 		+ "					AND (:clientLocation IS NULL OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :clientLocation, '%')))\n"
	// 		+ "					AND (:poNo IS NULL OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :poNo, '%')))\n"
	// 		+ "					AND (:shadowEmpName IS NULL OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :shadowEmpName, '%')))\n"
	// 		+ "					AND (:teamName IS NULL OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :teamName, '%')))\n"
	// 		+ "					AND (:activity IS NULL OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :activity, '%')))\n"
	// 		+ "					AND (\n"
	// 		+ "						    :date IS NULL\n"
	// 		+ "						    OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y')\n"
	// 		+ "						       LIKE CONCAT(:date, '%')\n"
	// 		+ "						)\n"
	// 		+ "					AND (\n"
	// 		+ "						    :search IS NULL OR :search = ''\n"
	// 		+ "						    OR LOWER(\n"
	// 		+ "						        CASE\n"
	// 		+ "						            WHEN e.isApmosysProduct = 'true'\n"
	// 		+ "						                THEN CONCAT('AP-', e.employeementId)\n"
	// 		+ "						            ELSE CONCAT('A-', e.employeementId)\n"
	// 		+ "						        END\n"
	// 		+ "						    ) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						\n"
	// 		+ "						    OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :search, '%'))\n"
	// 		+ "						    OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE CONCAT('%', :search, '%')\n"
	// 		+ "						)\n"
	// 		+ "				GROUP BY etn.timesheetId, etn.empId, e.employeementId, e.name, dtmn.dayType, etn.date,\n"
	// 		+ "			         etn.isNightShift, etn.workCheckIn, etn.workCheckOut, ab.name, etn.createdOn,\n"
	// 		+ "			         wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
	// 		+ "			         ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation, ptsn.poNo,\n"
	// 		+ "			         es.name, ptsn.status, ptsn.totalClientWorkingMinutes, ptsn.description,\n"
	// 		+ "			         a.activity, etamn.description, etamn.durationMinutes, t.teamName,\n"
	// 		+ "			         tddn.docId, tddn.docName, tddn.finalFlag, tddn.bulkApprovedDocId, dmtmn.mimeType\n"
	// 		+ "		        ORDER BY\n"
	// 		+ "		            CASE WHEN :sortBy = 'employeeName' AND :sortDir = 'ASC'  THEN e.name END ASC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'employeeName' AND :sortDir = 'DESC' THEN e.name END DESC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'employmentId' AND :sortDir = 'ASC'  THEN e.employeementId END ASC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'employmentId' AND :sortDir = 'DESC' THEN e.employeementId END DESC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'dayType' AND :sortDir = 'ASC'  THEN dtmn.dayType END ASC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'dayType' AND :sortDir = 'DESC' THEN dtmn.dayType END DESC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'date' AND :sortDir = 'ASC'  THEN etn.date END ASC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'date' AND :sortDir = 'DESC' THEN etn.date END DESC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'appliedOn' AND :sortDir = 'ASC'  THEN etn.createdOn END ASC,\n"
	// 		+ "		            CASE WHEN :sortBy = 'appliedOn' AND :sortDir = 'DESC' THEN etn.createdOn END DESC,\n"
	// 		+ "		            etn.createdOn DESC", countQuery = "\n"
	// 				+ "		        SELECT COUNT(DISTINCT etn)\n"
	// 				+ "		        FROM EmployeeTimesheetsNew etn\n"
	// 				+ "		        INNER JOIN Employee e ON etn.empId = e.empId\n"
	// 				+ "		        INNER JOIN DayTypeMasterNew dtmn ON dtmn.dayTypeId = etn.dayTypeId\n"
	// 				+ "		        INNER JOIN EmployeeTimesheetLocationMapping etlm ON etlm.timesheetId = etn.timesheetId\n"
	// 				+ "		        INNER JOIN ProjectTimesheetStatusNew ptsn ON ptsn.id.locationMappingId = etlm.locationMappingId\n"
	// 				+ "		        INNER JOIN Project p ON p.projectId = ptsn.id.projectId\n"
	// 				+ "		        INNER JOIN Client c ON c.clientId = p.clientId\n"
	// 				+ "		        WHERE etn.currentManagerId = :managerId\n"
	// 				+ "		            AND ptsn.status = 1\n"
	// 				+ "		            AND (\n"
	// 				+ "		                :clientFilter IS NULL\n"
	// 				+ "		                OR (:clientFilter = TRUE AND p.hasClientSideId = TRUE)\n"
	// 				+ "		                OR (:clientFilter = FALSE AND (p.hasClientSideId = FALSE OR p.hasClientSideId IS NULL))\n"
	// 				+ "		            )\n"
	// 				+ "		            AND (\n"
	// 				+ "		                :employeeName IS NULL OR :search = ''\n"
	// 				+ "		                OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%'))\n"
	// 				+ "		            )")
	// Page<GetReporteesTimesheetReqFlatDTO> getMyReporteesTimesheetRequests(
	// 		@Param("managerId") Long managerId,
	// 		@Param("clientFilter") Boolean clientFilter,

	// 		@Param("employmentId") String employmentId,
	// 		@Param("employeeName") String employeeName,
	// 		@Param("dayType") String dayType,
	// 		@Param("projectName") String projectName,
	// 		@Param("clientName") String clientName,
	// 		@Param("clientLocation") String clientLocation,
	// 		@Param("poNo") String poNo,
	// 		@Param("shadowEmpName") String shadowEmpName,
	// 		@Param("teamName") String teamName,
	// 		@Param("activity") String activity,
	// 		@Param("date") String date,

	// 		@Param("search") String search,

	// 		@Param("sortBy") String sortBy,
	// 		@Param("sortDir") String sortDir,
	// 		Pageable pageable);


//	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO(\n"
//        + "    etn.timesheetId, etn.empId,\n"
//        + "    CASE\n"
//        + "        WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//        + "        ELSE CONCAT('A-', e.employeementId)\n"
//        + "    END,\n"
//        + "    e.name, dtmn.dayType, etn.date, etn.isNightShift, etn.workCheckIn, etn.workCheckOut,\n"
//        + "    COUNT(DISTINCT ptsn.id.projectId), COUNT(DISTINCT etlm.locationMappingId), ab.name, etn.createdOn,\n"
//        + "    wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
//        + "    ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation,\n"
//        + "    ptsn.poNo, es.name, ptsn.status, ptsn.totalClientWorkingMinutes,\n"
//        + "    ptsn.description, a.activity, etamn.description,\n"
//        + "    etamn.durationMinutes, t.teamName,\n"
//        + "    tddn.docId, tddn.docName, tddn.finalFlag,\n"
//        + "    tddn.bulkApprovedDocId, dmtmn.mimeType\n"
//        + ")\n"
//        + "FROM EmployeeTimesheetsNew etn\n"
//        + "INNER JOIN Employee e ON etn.empId = e.empId\n"
//        + "INNER JOIN Employee ab ON ab.empId = etn.createdBy\n"
//        + "INNER JOIN DayTypeMasterNew dtmn ON dtmn.dayTypeId = etn.dayTypeId\n"
//        + "INNER JOIN EmployeeTimesheetLocationMapping etlm ON etlm.timesheetId = etn.timesheetId\n"
//        + "INNER JOIN ProjectTimesheetStatusNew ptsn ON ptsn.id.locationMappingId = etlm.locationMappingId\n"
//        + "INNER JOIN Project p ON p.projectId = ptsn.id.projectId\n"
//        + "INNER JOIN Client c ON c.clientId = p.clientId\n"
//        + "INNER JOIN ClientLocation cl ON cl.clientLocationId = ptsn.clientLocationId\n"
//        + "INNER JOIN EmployeeTimesheetActivitiesMappingNew etamn ON etamn.timesheetId = etn.timesheetId\n"
//        + "INNER JOIN Activity a ON a.activityId = etamn.activityId\n"
//        + "INNER JOIN Team t ON t.teamId = a.teamId\n"
//        + "INNER JOIN WorkLocationTypeMaster wltm ON wltm.workLocationTypeId = etlm.locationTypeId\n"
//        + "LEFT JOIN TimesheetDocumentDetailsNew tddn ON tddn.timesheetId = etn.timesheetId\n"
//        + "LEFT JOIN Employee es ON ptsn.shadowEmpId = es.empId\n"
//        + "LEFT JOIN DocMimeTypeMasterNew dmtmn ON dmtmn.mimeTypeId = tddn.mimeTypeId\n"
//        + "WHERE\n"
//        + "    (CASE\n"
//        + "        WHEN e.approvalsTo = 'Reporting Manager' THEN e.reportingManagerId\n"
//        + "        ELSE e.managerId\n"
//        + "    END) = :managerId\n"
//        + "    AND ptsn.status = 1\n"
//        + "    AND (\n"
//        + "        :clientFilter IS NULL\n"
//        + "        OR (:clientFilter = TRUE AND p.hasClientSideId = TRUE)\n"
//        + "        OR (:clientFilter = FALSE AND (p.hasClientSideId = FALSE OR p.hasClientSideId IS NULL))\n"
//        + "    )\n"
//        + "    AND (\n"
//        + "        :employmentId IS NULL\n"
//        + "        OR LOWER(\n"
//        + "            CASE\n"
//        + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//        + "                ELSE CONCAT('A-', e.employeementId)\n"
//        + "            END\n"
//        + "        ) LIKE LOWER(CONCAT('%', :employmentId, '%'))\n"
//        + "    )\n"
//        + "    AND (:employeeName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%')))\n"
//        + "    AND (:dayType IS NULL OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :dayType, '%')))\n"
//        + "    AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
//        + "    AND (:clientName IS NULL OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :clientName, '%')))\n"
//        + "    AND (:clientLocation IS NULL OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :clientLocation, '%')))\n"
//        + "    AND (:poNo IS NULL OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :poNo, '%')))\n"
//        + "    AND (:shadowEmpName IS NULL OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :shadowEmpName, '%')))\n"
//        + "    AND (:teamName IS NULL OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :teamName, '%')))\n"
//        + "    AND (:activity IS NULL OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :activity, '%')))\n"
//        + "    AND (\n"
//        + "        :date IS NULL\n"
//        + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE CONCAT(:date, '%')\n"
//        + "    )\n"
//        + "    AND (\n"
//        + "        :search IS NULL OR :search = ''\n"
//        + "        OR LOWER(\n"
//        + "            CASE\n"
//        + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//        + "                ELSE CONCAT('A-', e.employeementId)\n"
//        + "            END\n"
//        + "        ) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE LOWER(CONCAT('%', :search, '%'))\n"
//        + "    )\n"
//        + "GROUP BY etn.timesheetId, etn.empId, e.employeementId, e.name, dtmn.dayType, etn.date,\n"
//        + "         etn.isNightShift, etn.workCheckIn, etn.workCheckOut, ab.name, etn.createdOn,\n"
//        + "         wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
//        + "         ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation, ptsn.poNo,\n"
//        + "         es.name, ptsn.status, ptsn.totalClientWorkingMinutes, ptsn.description,\n"
//        + "         a.activity, etamn.description, etamn.durationMinutes, t.teamName,\n"
//        + "         tddn.docId, tddn.docName, tddn.finalFlag, tddn.bulkApprovedDocId, dmtmn.mimeType",
//        countQuery =
//        "SELECT COUNT(DISTINCT etn.timesheetId) "
//        		+ "FROM EmployeeTimesheetsNew etn\n"
//                + "INNER JOIN Employee e ON etn.empId = e.empId\n"
//                + "INNER JOIN Employee ab ON ab.empId = etn.createdBy\n"
//                + "INNER JOIN DayTypeMasterNew dtmn ON dtmn.dayTypeId = etn.dayTypeId\n"
//                + "INNER JOIN EmployeeTimesheetLocationMapping etlm ON etlm.timesheetId = etn.timesheetId\n"
//                + "INNER JOIN ProjectTimesheetStatusNew ptsn ON ptsn.id.locationMappingId = etlm.locationMappingId\n"
//                + "INNER JOIN Project p ON p.projectId = ptsn.id.projectId\n"
//                + "INNER JOIN Client c ON c.clientId = p.clientId\n"
//                + "INNER JOIN ClientLocation cl ON cl.clientLocationId = ptsn.clientLocationId\n"
//                + "INNER JOIN EmployeeTimesheetActivitiesMappingNew etamn ON etamn.timesheetId = etn.timesheetId\n"
//                + "INNER JOIN Activity a ON a.activityId = etamn.activityId\n"
//                + "INNER JOIN Team t ON t.teamId = a.teamId\n"
//                + "INNER JOIN WorkLocationTypeMaster wltm ON wltm.workLocationTypeId = etlm.locationTypeId\n"
//                + "LEFT JOIN TimesheetDocumentDetailsNew tddn ON tddn.timesheetId = etn.timesheetId\n"
//                + "LEFT JOIN Employee es ON ptsn.shadowEmpId = es.empId\n"
//                + "LEFT JOIN DocMimeTypeMasterNew dmtmn ON dmtmn.mimeTypeId = tddn.mimeTypeId\n"
//                + "WHERE\n"
//                + "    (CASE\n"
//                + "        WHEN e.approvalsTo = 'Reporting Manager' THEN e.reportingManagerId\n"
//                + "        ELSE e.managerId\n"
//                + "    END) = :managerId\n"
//                + "    AND ptsn.status = 1\n"
//                + "    AND (\n"
//                + "        :clientFilter IS NULL\n"
//                + "        OR (:clientFilter = TRUE AND p.hasClientSideId = TRUE)\n"
//                + "        OR (:clientFilter = FALSE AND (p.hasClientSideId = FALSE OR p.hasClientSideId IS NULL))\n"
//                + "    )\n"
//                + "    AND (\n"
//                + "        :employmentId IS NULL\n"
//                + "        OR LOWER(\n"
//                + "            CASE\n"
//                + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//                + "                ELSE CONCAT('A-', e.employeementId)\n"
//                + "            END\n"
//                + "        ) LIKE LOWER(CONCAT('%', :employmentId, '%'))\n"
//                + "    )\n"
//                + "    AND (:employeeName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%')))\n"
//                + "    AND (:dayType IS NULL OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :dayType, '%')))\n"
//                + "    AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
//                + "    AND (:clientName IS NULL OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :clientName, '%')))\n"
//                + "    AND (:clientLocation IS NULL OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :clientLocation, '%')))\n"
//                + "    AND (:poNo IS NULL OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :poNo, '%')))\n"
//                + "    AND (:shadowEmpName IS NULL OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :shadowEmpName, '%')))\n"
//                + "    AND (:teamName IS NULL OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :teamName, '%')))\n"
//                + "    AND (:activity IS NULL OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :activity, '%')))\n"
//                + "    AND (\n"
//                + "        :date IS NULL\n"
//                + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE CONCAT(:date, '%')\n"
//                + "    )\n"
//                + "    AND (\n"
//                + "        :search IS NULL OR :search = ''\n"
//                + "        OR LOWER(\n"
//                + "            CASE\n"
//                + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//                + "                ELSE CONCAT('A-', e.employeementId)\n"
//                + "            END\n"
//                + "        ) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE LOWER(CONCAT('%', :search, '%'))\n"
//                + "    )\n"
//                + "GROUP BY etn.timesheetId"
//        )
//Page<GetReporteesTimesheetReqFlatDTO> getMyReporteesTimesheetRequests(
//        @Param("managerId") Long managerId,
//        @Param("clientFilter") Boolean clientFilter,
//
//        @Param("employmentId") String employmentId,
//        @Param("employeeName") String employeeName,
//        @Param("dayType") String dayType,
//        @Param("projectName") String projectName,
//        @Param("clientName") String clientName,
//        @Param("clientLocation") String clientLocation,
//        @Param("poNo") String poNo,
//        @Param("shadowEmpName") String shadowEmpName,
//        @Param("teamName") String teamName,
//        @Param("activity") String activity,
//        @Param("date") String date,
//
//        @Param("search") String search,
//
//        Pageable pageable
//);


	@Query(value = "WITH RECURSIVE \n" +
			"All_Dates_In_Range AS ( \n" +
			"    SELECT CAST(:fromDate AS DATE) AS dt \n" +
			"    UNION ALL \n" +
			"    SELECT DATE_ADD(dt, INTERVAL 1 DAY) \n" +
			"    FROM All_Dates_In_Range \n" +
			"    WHERE dt < CAST(:toDate AS DATE) \n" +
			"), \n" +
			"Base_Project_Employees AS ( \n" +
			"    SELECT DISTINCT \n" +
			"        etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, \n" +
			"        e.billable_type, DATE(etm.start_date) AS start_date, \n" +
			"        DATE(etm.end_date) AS end_date, etm.employee_team_map_id, etm.active, \n" +
			"        p.project_id, p.project_name, c.client_id, c.client_name, \n" +
			"        ecsm.client_side_id, p.po_no, s.name AS spoc, tl.name AS team_lead, \n" +
			"        e.reporting_manager_id, e.employmentstatus, d.name AS dept_name, \n" +
			"        CASE WHEN e.is_apmosys_product = 'true' \n" +
			"             THEN CONCAT('AP-', e.employeement_id) \n" +
			"             ELSE CONCAT('A-', e.employeement_id) END AS employement_id \n" +
			"    FROM projects p \n" +
			"    JOIN teams t ON p.project_id = t.project_id \n" +
			"    JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n" +
			"    JOIN employee e ON e.emp_id = etm.emp_id \n" +
			"    JOIN clients c ON c.client_id = p.client_id \n" +
			"    LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id \n" +
			"    LEFT JOIN employee s ON s.emp_id = t.spoc_id \n" +
			"    LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id \n" +
			"    LEFT JOIN department d ON d.dept_id = jr.dept_id \n" +
			"    LEFT JOIN project_manager_mapping pmm_check \n" +
			"           ON p.project_id = pmm_check.project_id \n" +
			"          AND pmm_check.project_manager_id = :emp_id \n" +
			"    LEFT JOIN project_overhead_mapping pom_check \n" +
			"           ON p.project_id = pom_check.project_id \n" +
			"          AND pom_check.project_overhead_id = :emp_id \n" +
			"    LEFT JOIN employee_client_side_id_mapping_new ecsm \n" +
			"           ON e.emp_id = ecsm.emp_id \n" +
			"          AND ecsm.project_id = t.project_id \n" +
			"          AND ecsm.active = 1 \n" +
			"    WHERE p.has_client_side_id = 1 \n" +
			"      AND ( e.emp_id IN (:authorizedEmpIds) \n" +
			"            OR ( (pmm_check.project_manager_id IS NOT NULL \n" +
			"                  OR pom_check.project_overhead_id IS NOT NULL) \n" +
			"                 AND jr.dept_id = ( \n" +
			"                     SELECT ujr.dept_id \n" +
			"                     FROM employee ue \n" +
			"                     JOIN job_role ujr ON ue.job_role_id = ujr.job_role_id \n" +
			"                     WHERE ue.emp_id = :emp_id ) ) ) \n" +
			"      AND ( e.date_of_relieving IS NULL \n" +
			"            OR YEAR(e.date_of_relieving) > :year \n" +
			"            OR ( YEAR(e.date_of_relieving) = :year \n" +
			"                 AND MONTH(e.date_of_relieving) >= :month ) ) \n" +
			"      AND e.emp_id NOT BETWEEN 1 AND 6 \n" +
			"      AND DATE(etm.start_date) <= :toDate \n" +
			"      AND ( etm.end_date IS NULL OR DATE(etm.end_date) >= :fromDate ) \n" +
			"      AND ( :clientSideFilter = 'ALL' \n" +
			"            OR ( :clientSideFilter = 'true' AND p.client_flag = 1 ) \n" +
			"            OR ( :clientSideFilter = 'false' \n" +
			"                 AND (p.client_flag = 0 OR p.client_flag IS NULL) ) ) \n" +
			"), \n" +
			"Timesheet_Base_Data AS ( --\n" + //
			"  SELECT \n" + //
			"    DISTINCT et.timesheet_id, \n" + //
			"    et.emp_id, \n" + //
			"    etam.project_id, \n" + //
			"    etm.employee_team_map_id, \n" + //
			"    et.date, \n" + //
			"    dtm.day_type, \n" + //
			"    t.team_id team_id, \n" + //
			"    a.team_id as a_team_id\n" + //
			"  FROM \n" + //
			"    employee_timesheets_new et \n" + //
			"    LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id \n" + //
			"    LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id \n" + //
			"    LEFT JOIN activities a ON etam.activity_id = a.activity_id \n" + //
			"    LEFT JOIN teams t ON a.team_id = t.team_id \n" + //
			"    LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id \n" + //
			"    AND pts.project_id = etam.project_id\n" + //
			"    LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id \n" + //
			"    AND etm.team_id = t.team_id \n" + //
			"    AND et.date >= DATE(etm.start_date) \n" + //
			"    AND (\n" + //
			"      etm.end_date IS NULL \n" + //
			"      OR et.date <= DATE(etm.end_date)\n" + //
			"    ) \n" + //
			"  WHERE \n" + //
			"    et.date BETWEEN '2026-01-01'\n" + //
			"    AND '2026-01-19'\n" + //
			"), \n" +
			"Employee_Document_Summary_Details AS (\n" +
			"  SELECT \n" +
			"    DISTINCT tbd.emp_id, \n" +
			"    tbd.project_id, \n" +
			"    tbd.employee_team_map_id, \n" +
			"    DATE(tbd.date) AS timesheet_date, \n" +
			"    csm.status AS client_approval_status, \n" +
			"    tdd.final_flag, \n" +
			"    tdd.active,\n" +
			"    tdd.timesheet_id,\n" +
			"    tdd.bulk_approved_doc_id AS bulk_approved_doc_id\n" +
			"  FROM \n" +
			"    timesheet_document_details_new tdd \n" +
			"    LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id \n" +
			"    INNER JOIN Timesheet_Base_Data tbd ON tdd.timesheet_id = tbd.timesheet_id \n" +
			"    AND tdd.project_id = tbd.project_id\n" +
			"  WHERE \n" +
			"    tdd.active = TRUE\n" +
			"), \n" +
			"Expected_Client_Side_Base_DSR AS (\n" +
			"  SELECT \n" +
			"    DISTINCT bpe.emp_id, \n" +
			"    bpe.employee_team_map_id, \n" +
			"    bpe.project_id, \n" +
			"    adir.dt \n" +
			"  FROM \n" +
			"    Base_Project_Employees bpe CROSS \n" +
			"    JOIN All_Dates_In_Range adir \n" +
			"  WHERE\n" +
			"    adir.dt BETWEEN DATE(bpe.start_date)\n" +
			"               AND COALESCE(DATE(bpe.end_date), :toDate)\n" +
			"    AND adir.dt >= :fromDate\n" +
			"\n" +
			"    AND NOT EXISTS (\n" +
			"      SELECT \n" +
			"        1 \n" +
			"      FROM \n" +
			"        employee_timesheets_new et1 \n" +
			"        LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id \n" +
			"      WHERE \n" +
			"        et1.emp_id = bpe.emp_id \n" +
			"        AND adir.dt = et1.date \n" +
			"        AND UPPER(dtm1.day_type) IN (\n" +
			"          'LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', \n" +
			"          'WEEK OFF', 'ApMoSys Holiday'\n" +
			"        )\n" +
			"    )\n" +
			"), \n" +
			"Actual_Client_Side_Submissions AS (\n" +
			"  SELECT \n" +
			"    DISTINCT edsd.emp_id, \n" +
			"    edsd.project_id, \n" +
			"    edsd.employee_team_map_id, \n" +
			"    edsd.client_approval_status AS client_approval_status,\n" +
			"    edsd.timesheet_date AS dt \n" +
			"  FROM \n" +
			"    Employee_Document_Summary_Details edsd \n" +
			"  WHERE \n" +
			"    (\n" +
			"      (\n" +
			"        UPPER(edsd.client_approval_status) = 'APPROVED' \n" +
			"        AND edsd.final_flag = 1\n" +
			"      ) \n" +
			"      OR (\n" +
			"        UPPER(edsd.client_approval_status) = 'PENDING' \n" +
			"        AND NOT EXISTS (\n" +
			"          SELECT \n" +
			"            1 \n" +
			"          FROM \n" +
			"            timesheet_document_details_new tdd2 \n" +
			"            LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id \n"
			+
			"          WHERE \n" +
			"            tdd2.timesheet_id = edsd.timesheet_id \n" +
			"            AND tdd2.project_id = edsd.project_id\n" +
			"            AND UPPER(csm2.status) = 'APPROVED'\n" +
			"        )\n" +
			"      )\n" +
			"    ) \n" +
			"    AND edsd.timesheet_date BETWEEN :fromDate AND :toDate \n" +
			"    AND edsd.timesheet_date < CURDATE()\n" +
			"), \n" +
			"\n" +
			"Combined_Expected_Client_Side_DSR AS (\n" +
			"  SELECT \n" +
			"    DISTINCT emp_id, \n" +
			"    project_id, \n" +
			"    employee_team_map_id, \n" +
			"    dt \n" +
			"  FROM \n" +
			"    Expected_Client_Side_Base_DSR \n" +
			"  UNION \n" +
			"  SELECT \n" +
			"    DISTINCT emp_id, \n" +
			"    project_id, \n" +
			"    employee_team_map_id, \n" +
			"    dt \n" +
			"  FROM \n" +
			"    Actual_Client_Side_Submissions\n" +
			"), \n" +
			"WorkingDays_Summary AS (\n" +
			"  SELECT \n" +
			"    DISTINCT emp_id, \n" +
			"    project_id, \n" +
			"    employee_team_map_id, \n" +
			"    COUNT(DISTINCT dt) AS expected_fill_count \n" +
			"  FROM \n" +
			"    Combined_Expected_Client_Side_DSR \n" +
			"  GROUP BY \n" +
			"    emp_id, \n" +
			"    project_id, \n" +
			"    employee_team_map_id\n" +
			"), \n" +
			"Employee_Document_Summary AS (\n" +
			"  SELECT \n" +
			"    DISTINCT edsd.emp_id, \n" +
			"    edsd.project_id, \n" +
			"    edsd.employee_team_map_id, \n" +
			"    COUNT(\n" +
			"      DISTINCT CASE WHEN UPPER(edsd.client_approval_status) = 'APPROVED' \n" +
			"      AND edsd.final_flag = 1 AND edsd.bulk_approved_doc_id IS NULL THEN edsd.timesheet_id END\n" +
			"    ) AS approved_days, \n" +
			"    COUNT(\n" +
			"      DISTINCT CASE WHEN UPPER(edsd.client_approval_status) = 'PENDING' \n" +
			"      AND edsd.final_flag = 0 AND edsd.bulk_approved_doc_id IS NULL\n" +
			"      THEN edsd.timesheet_id END\n" +
			"     ) AS pending_days \n" +
			"  FROM \n" +
			"    Employee_Document_Summary_Details edsd \n" +
			"  GROUP BY \n" +
			"    edsd.emp_id, \n" +
			"    edsd.project_id, \n" +
			"    edsd.employee_team_map_id\n" +
			"), \n" +

			"Employee_Calculated_Status AS (\n" + //
			"  SELECT \n" + //
			"    bpe.emp_id, \n" + //
			"    bpe.project_id, \n" + //
			"    bpe.employee_team_map_id, \n" + //
			"    COALESCE(wds.expected_fill_count, 0) AS expectedTimesheetFillCount, \n" + //
			"    GREATEST(\n" + //
			"      0, \n" + //
			"      COALESCE(wds.expected_fill_count, 0) - (\n" + //
			"        COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)\n" + //
			"      )\n" + //
			"    ) AS client_side_not_filled_count, \n" + //
			"    COALESCE(eds.pending_days, 0) AS clientSidePendingCount, \n" + //
			"    COALESCE(eds.approved_days, 0) AS clientSideApprovedCount, \n" + //
			"    CASE \n" + //
			"      WHEN GREATEST(\n" + //
			"        0, \n" + //
			"        COALESCE(wds.expected_fill_count, 0) - (\n" + //
			"          COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)\n" + //
			"        )\n" + //
			"      ) >= 2 THEN 'Defaulter' \n" + //
			"      WHEN COALESCE(eds.pending_days, 0) > 0 \n" + //
			"        OR GREATEST(\n" + //
			"          0, \n" + //
			"          COALESCE(wds.expected_fill_count, 0) - (\n" + //
			"            COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0)\n" + //
			"          )\n" + //
			"        ) >= 1 THEN 'Pending' \n" + //
			"      ELSE 'Approved' \n" + //
			"    END AS employee_status \n" + //
			"  FROM \n" + //
			"    Base_Project_Employees bpe \n" + //
			"    LEFT JOIN WorkingDays_Summary wds ON bpe.employee_team_map_id = wds.employee_team_map_id \n" + //
			"    LEFT JOIN Employee_Document_Summary eds ON bpe.employee_team_map_id = eds.employee_team_map_id\n" + //
			") ,\n" +
			"Ishine_Not_Filled_New AS ( \n" +
			" SELECT bpe.emp_id \n" +
			" FROM Base_Project_Employees bpe \n" +
			" WHERE DATEDIFF(:toDate, :fromDate) > ( \n" +
			" SELECT COUNT(*) FROM employee_timesheets_new ets \n" +
			" WHERE ets.emp_id = bpe.emp_id \n" +
			" AND ets.date BETWEEN :fromDate AND :toDate ) \n" +
			") \n" +
			"SELECT \n" +
			"    COUNT(DISTINCT bpe.emp_id) AS total_no_of_applicable_employees, \n" +
			"    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Approved' THEN bpe.emp_id END) AS total_approved_employees, \n"
			+
			"    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Pending' THEN bpe.emp_id END) AS total_pending_employees, \n"
			+
			"    COUNT(DISTINCT CASE WHEN ecs.employee_status = 'Defaulter' THEN inf.emp_id END) AS total_ishine_defaulter_employees, \n"
			+
			"    COUNT(DISTINCT CASE WHEN ecs.employee_status IN ('Defaulter','Pending') THEN inf.emp_id END) AS Defaulter_employees \n"
			+
			"FROM Base_Project_Employees bpe \n" +
			"JOIN Employee_Calculated_Status ecs ON bpe.employee_team_map_id = ecs.employee_team_map_id \n"+
			"LEFT JOIN Ishine_Not_Filled_New inf ON bpe.emp_id= inf.emp_id" , nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForEmployeeNew(@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate,
			@Param("emp_id") Long emp_id,
			@Param("clientSideFilter") String clientSideFilter,
			@Param("authorizedEmpIds") List<Long> authorizedEmpIds,
			@Param("month") Integer month,
			@Param("year") Integer year);

			// Not used and is incorrect
	// @Query(value = "SELECT count(emp_id) AS defaulter_employee\n" +
	// 			"FROM (\n" + 
	// 			"    SELECT e.emp_id, e.email\n" +
	// 			"    FROM employee e\n" + 
	// 			"    left JOIN employee_timesheets_new t\n" + 
	// 			"        ON t.emp_id = e.emp_id\n" + 
	// 			"       AND t.date BETWEEN :fromDate AND :toDate\n" + 
	// 			"\tINNER join project_timesheet_status_new p on p.timesheet_id = t.timesheet_id\n" + 
	// 			"    WHERE p.client_side_id is not null\n" + 
	// 			"    and (e.date_of_relieving is null or e.date_of_relieving  >= :fromDate) \n" +
	// 			"    GROUP BY e.emp_id, e.email\n" + 
	// 			"    HAVING COUNT(DISTINCT t.date) \n" +
	// 			"           < (DATEDIFF(:toDate, :fromDate))\n" + 
	// 			") AS defaulters", nativeQuery = true)
	// public Long countIshineNotFilled(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

	@Query(value = "SELECT etn.timesheet_id,etn.date,dayname(etn.date) ,\n"+
	"TIME_FORMAT(SEC_TO_TIME(etn.total_working_minutes * 60), '%H:%i') AS total_working_hours,\n"+
	"smn.status, dtn.day_type,\n"+
	"COALESCE(etn.description, ptsn.description, 'N/A') AS description, a.activity,p.project_name \n"+
	"FROM employee_timesheets_new etn\n"+
	"LEFT join project_timesheet_status_new ptsn on etn.timesheet_id = ptsn.timesheet_id\n"+
	"LEFT JOIN projects p on p.project_id = ptsn.project_id \n"+
	"LEFT join employee_timesheet_activities_mapping_new etamn on etamn.project_id = ptsn.project_id and etamn.timesheet_id = etn.timesheet_id\n"+
	"LEFT JOIN activities a ON etamn.activity_id = a.activity_id\n"+
	"INNER JOIN day_type_master_new dtn on dtn.day_type_id = etn.day_type_id\n"+
	"INNER JOIN status_master_new smn on smn.status_id = etn.status\n"+
	"where etn.emp_id = :empId and etn.date between :start and :end",nativeQuery = true )
	List<Object[]> getNewTimesheetDetails(@Param("empId") Long empId, @Param("start") LocalDate start, @Param("end") LocalDate end);

	
	
//	@Query( value ="select sm.status , count(distinct e.timesheet_id) \n"
//			+ "from employee_timesheets_new e \n"
//			+ "inner join project_timesheet_status_new pts on e.timesheet_id = pts.timesheet_id \n"
//			+ "inner join employee emp on emp.emp_id = e.emp_id\n "
//			+ "inner join projects p on p.project_id = pts.project_id\n "
//			+ "left join status_master_new sm on e.status = sm.status_id\n "
//			+ "where (CASE\n"
//			+ "                WHEN emp.approvals_to = 'Reporting Manager' THEN emp.reporting_manager_id\n"
//			+ "                ELSE emp.manager_id\n"
//			+ "            END)= :managerId "
//			+ "AND (\n "
//			+ "	 :clientFilter IS NULL\n "
//			+ "	 OR (:clientFilter = TRUE AND p.has_client_side_id = TRUE)\n "
//			+ "	 OR (:clientFilter = FALSE AND (p.has_client_side_id = FALSE OR p.has_client_side_id IS NULL))\n "
//			+ " )\n"
//			+ " group by sm.status",nativeQuery = true
//			) 
//			List<Object[]> getTimesheetStatusCountsByCurrentManagerId(
//			        @Param("managerId") Long managerId,
//			        @Param("clientFilter") Boolean clientFilter
//
//			);
	
	@Query (value = "WITH Base_List_Data AS(\n"
			+ "SELECT \n"
			+ "    etn.timesheet_id, \n"
			+ "    etn.emp_id, \n"
			+ "    CASE\n"
			+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
			+ "        ELSE CONCAT('A-', e.employeement_id)\n"
			+ "    END as employeement_id, \n"
			+ "    e.name employee_name,  \n"
			+ "    dtm.day_type, \n"
			+ "    etn.date, \n"
			+ "    etn.is_night_shift, \n"
			+ "    etn.work_in_time, \n"
			+ "    etn.work_out_time,\n"
			+ "    COUNT(DISTINCT ptsn.project_id), \n"
			+ "    COUNT(DISTINCT etlm.location_mapping_id), \n"
			+ "    ab.name as created_by_name, \n"
			+ "    etn.created_on,\n"
			+ "    wltm.code, \n"
			+ "    etlm.location_in_time, \n"
			+ "    etlm.location_out_time, \n"
			+ "    etlm.location_mapping_id,\n"
			+ "    ptsn.project_id, \n"
			+ "    p.project_name, \n"
			+ "    c.client_name, \n"
			+ "    cl.client_location,\n"
			+ "    ptsn.po_no, \n"
			+ "    es.name as shadow_employee_name, \n"
			+ "    ptsn.status, \n"
			+ "    ptsn.total_client_working_minutes,\n"
			+ "    ptsn.description project_timesheet_description, \n"
			+ "    a.activity, \n"
			+ "    etamn.description etam_description,\n"
			+ "    etamn.duration_minutes, \n"
			+ "    t.team_name,\n"
			+ "    tddn.doc_id, \n"
			+ "    tddn.doc_name, \n"
			+ "    tddn.final_flag,\n"
			+ "    tddn.bulk_approved_doc_id, \n"
			+ "    dmtmn.mime_type\n"
			+ "FROM \n"
			+ "    employee_timesheets_new etn \n"
			+ "INNER JOIN \n"
			+ "    employee e ON etn.emp_id = e.emp_id\n"
			+ "INNER JOIN \n"
			+ "    employee ab ON etn.created_by = ab.emp_id\n"
			+ "INNER JOIN \n"
			+ "	employee_team_mapping etm ON etm.emp_id = e.emp_id\n"
			+ "INNER JOIN \n"
			+ "    teams t ON t.team_id = etm.team_id\n"
			+ "INNER JOIN \n"
			+ "    projects p ON t.project_id = p.project_id\n"
			+ "INNER JOIN \n"
			+ "    project_timesheet_status_new ptsn ON ptsn.timesheet_id = etn.timesheet_id\n"
			+ "INNER JOIN \n"
			+ "    employee_timesheet_location_mapping etlm ON etn.timesheet_id = etlm.timesheet_id\n"
			+ "LEFT JOIN \n"
			+ "    employee_timesheet_activities_mapping_new etamn ON etamn.timesheet_id = etn.timesheet_id\n"
			+ "LEFT JOIN \n"
			+ "    activities a ON a.activity_id = etamn.activity_id\n"
			+ "LEFT JOIN \n"
			+ "    clients c ON ptsn.client_side_id = c.client_id\n"
			+ "LEFT JOIN \n"
			+ "    client_locations cl ON cl.client_location_id = ptsn.client_location_id\n"
			+ "LEFT JOIN \n"
			+ "    work_location_type_master wltm ON wltm.work_location_type_id = etlm.location_type_id\n"
			+ "LEFT JOIN \n"
			+ "    day_type_master_new dtm ON dtm.day_type_id = etn.day_type_id\n"
			+ "LEFT JOIN \n"
			+ "    timesheet_document_details_new tddn ON tddn.timesheet_id = etn.timesheet_id\n"
			+ "LEFT JOIN \n"
			+ "    employee es ON ptsn.shadow_emp_id = es.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    doc_mime_type_master_new dmtmn ON dmtmn.mime_type_id = tddn.mime_type_id\n"
			+ "WHERE \n"
			+ "  (CASE\n"
			+ "		 WHEN e.approvals_to = 'Reporting Manager' THEN e.reporting_manager_id\n"
			+ "		 ELSE e.manager_id\n"
			+ "	 END) = :managerId\n"
			+ "	 AND (\n"
			+ "		 :clientFilter IS NULL\n"
			+ "		 OR (:clientFilter = TRUE AND p.has_client_side_id = TRUE)\n"
			+ "		 OR (:clientFilter = FALSE AND (p.has_client_side_id = FALSE OR p.has_client_side_id IS NULL))\n"
			+ "	 )\n"
			+ " --      AND etn.status = :status\n"
			+ "GROUP BY \n"
			+ "    etn.timesheet_id,\n"
			+ "   etn.emp_id,\n"
			+ "    e.is_apmosys_product,\n"
			+ "    e.employeement_id,\n"
			+ "    e.name,\n"
			+ "    dtm.day_type,\n"
			+ "    etn.date,\n"
			+ "    etn.is_night_shift,\n"
			+ "    etn.work_in_time,\n"
			+ "    etn.work_out_time,\n"
			+ "    ab.name,\n"
			+ "    etn.created_on,\n"
			+ "    wltm.code,\n"
			+ "    etlm.location_in_time,\n"
			+ "    etlm.location_out_time,\n"
			+ "    etlm.location_mapping_id,\n"
			+ "    ptsn.project_id,\n"
			+ "    p.project_name,\n"
			+ "    c.client_name,\n"
			+ "    cl.client_location,\n"
			+ "    ptsn.po_no,\n"
			+ "    es.name,\n"
			+ "    ptsn.status,\n"
			+ "    ptsn.total_client_working_minutes,\n"
			+ "    ptsn.description,\n"
			+ "    a.activity,\n"
			+ "    etamn.description,\n"
			+ "    etamn.duration_minutes,\n"
			+ "    t.team_name,\n"
			+ "    tddn.doc_id,\n"
			+ "    tddn.doc_name,\n"
			+ "    tddn.final_flag,\n"
			+ "    tddn.bulk_approved_doc_id,\n"
			+ "    dmtmn.mime_type\n"
			+ "\n"
			+ ")\n"
			+ "select count(distinct bld.timesheet_id) from Base_List_Data bld  \n"
			+ "INNER JOIN employee_timesheets_new etn on bld.timesheet_id = etn.timesheet_id\n"
			+ "group by etn.status",nativeQuery = true
			)
	List<Object[]> getTimesheetStatusCountsByCurrentManagerId(
	        @Param("managerId") Long managerId,
	        @Param("clientFilter") Boolean clientFilter
//			@Param("status") Integer status
	);


			@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO(\n"
			        + "    etn.timesheetId, etn.empId,\n"
			        + "    CASE\n"
			        + "        WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
			        + "        ELSE CONCAT('A-', e.employeementId)\n"
			        + "    END,\n"
			        + "    e.name, dtmn.dayType, etn.date, etn.isNightShift, etn.workCheckIn, etn.workCheckOut,\n"
			        + "    COUNT(DISTINCT ptsn.id.projectId), COUNT(DISTINCT etlm.locationMappingId), ab.name, etn.createdOn,\n"
			        + "    wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
			        + "    ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation,\n"
			        + "    ptsn.poNo, es.name, ptsn.status, ptsn.totalClientWorkingMinutes,\n"
			        + "    ptsn.description, a.activity, etamn.description,\n"
			        + "    etamn.durationMinutes, t.teamName,\n"
			        + "    tddn.docId, tddn.docName, tddn.finalFlag,\n"
			        + "    tddn.bulkApprovedDocId, dmtmn.mimeType\n"
			        + ")\n"
			        + "FROM EmployeeTimesheetsNew etn\n"
			        + "INNER JOIN Employee e ON etn.empId = e.empId\n"
			        + "INNER JOIN Employee ab ON ab.empId = etn.createdBy\n"
			        + "INNER JOIN DayTypeMasterNew dtmn ON dtmn.dayTypeId = etn.dayTypeId\n"
			        + "INNER JOIN EmployeeTimesheetLocationMapping etlm ON etlm.timesheetId = etn.timesheetId\n"
			        + "INNER JOIN ProjectTimesheetStatusNew ptsn ON ptsn.id.locationMappingId = etlm.locationMappingId\n"
			        + "INNER JOIN Project p ON p.projectId = ptsn.id.projectId\n"
			        + "INNER JOIN Client c ON c.clientId = p.clientId\n"
			        + "INNER JOIN ClientLocation cl ON cl.clientLocationId = ptsn.clientLocationId\n"
			        + "INNER JOIN EmployeeTimesheetActivitiesMappingNew etamn ON etamn.timesheetId = etn.timesheetId\n"
			        + "INNER JOIN Activity a ON a.activityId = etamn.activityId\n"
			        + "INNER JOIN Team t ON t.teamId = a.teamId\n"
			        + "INNER JOIN WorkLocationTypeMaster wltm ON wltm.workLocationTypeId = etlm.locationTypeId\n"
			        + "LEFT JOIN TimesheetDocumentDetailsNew tddn ON tddn.timesheetId = etn.timesheetId\n"
			        + "LEFT JOIN Employee es ON ptsn.shadowEmpId = es.empId\n"
			        + "LEFT JOIN DocMimeTypeMasterNew dmtmn ON dmtmn.mimeTypeId = tddn.mimeTypeId\n"
			        + "WHERE\n"
			        + "    (CASE\n"
			        + "        WHEN e.approvalsTo = 'Reporting Manager' THEN e.reportingManagerId\n"
			        + "        ELSE e.managerId\n"
			        + "    END) = :managerId\n"
			        + "    AND ptsn.status = 1\n"
//			        + "    AND e.employmentstatus !='InActive'\n"
			        + "    AND (\n"
			        + "        :clientFilter IS NULL\n"
			        + "        OR (:clientFilter = TRUE AND p.hasClientSideId = TRUE)\n"
			        + "        OR (:clientFilter = FALSE AND (p.hasClientSideId = FALSE OR p.hasClientSideId IS NULL))\n"
			        + "    )\n"
			        + "    AND (\n"
			        + "        :employmentId IS NULL\n"
			        + "        OR LOWER(\n"
			        + "            CASE\n"
			        + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
			        + "                ELSE CONCAT('A-', e.employeementId)\n"
			        + "            END\n"
			        + "        ) LIKE LOWER(CONCAT('%', :employmentId, '%'))\n"
			        + "    )\n"
			        + "    AND (:employeeName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%')))\n"
			        + "    AND (:dayType IS NULL OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :dayType, '%')))\n"
			        + "    AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
			        + "    AND (:clientName IS NULL OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :clientName, '%')))\n"
			        + "    AND (:clientLocation IS NULL OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :clientLocation, '%')))\n"
			        + "    AND (:poNo IS NULL OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :poNo, '%')))\n"
			        + "    AND (:shadowEmpName IS NULL OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :shadowEmpName, '%')))\n"
			        + "    AND (:teamName IS NULL OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :teamName, '%')))\n"
			        + "    AND (:activity IS NULL OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :activity, '%')))\n"
			        + "    AND (\n"
			        + "        :date IS NULL\n"
			        + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE CONCAT(:date, '%')\n"
			        + "    )\n"
			        + "    AND ( \n"
			        + "        :workCheckIn IS NULL \n"
			        + "        OR FUNCTION('DATE_FORMAT', etn.workCheckIn, '%h:%i %p') \n"
			        + "           LIKE CONCAT('%', :workCheckIn, '%') \n"
			        + "    ) \n"
			        + "    AND ( \n"
			        + "        :workCheckOut IS NULL \n"
			        + "        OR FUNCTION('DATE_FORMAT', etn.workCheckOut, '%h:%i %p') \n"
			        + "           LIKE CONCAT('%', :workCheckOut, '%') \n"
			        + "    ) \n"
			        + "    AND ( \n"
			        + "        :appliedBy IS NULL \n"
			        + "        OR LOWER(ab.name) LIKE LOWER(CONCAT('%', :appliedBy, '%')) \n"
			        + "    ) \n"
			        + "    AND ( \n"
			        + "        :appliedOn IS NULL \n"
			        + "        OR LOWER(FUNCTION('DATE_FORMAT', etn.createdOn, '%h:%i %p')) \n"
			        + "           LIKE LOWER(CONCAT('%', :appliedOn, '%')) \n"
			        + "    ) \n"
			        + "    AND (\n"
			        + "        :search IS NULL OR :search = ''\n"
			        + "        OR LOWER(\n"
			        + "            CASE\n"
			        + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
			        + "                ELSE CONCAT('A-', e.employeementId)\n"
			        + "            END\n"
			        + "        ) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE LOWER(CONCAT('%', :search, '%'))\n"
			        + "    )\n"
			        + "GROUP BY etn.timesheetId, etn.empId, e.employeementId, e.name, dtmn.dayType, etn.date,\n"
			        + "         etn.isNightShift, etn.workCheckIn, etn.workCheckOut, ab.name, etn.createdOn,\n"
			        + "         wltm.code, etlm.locationInTime, etlm.locationOutTime, etlm.locationMappingId,\n"
			        + "         ptsn.id.projectId, p.projectName, c.clientName, cl.clientLocation, ptsn.poNo,\n"
			        + "         es.name, ptsn.status, ptsn.totalClientWorkingMinutes, ptsn.description,\n"
			        + "         a.activity, etamn.description, etamn.durationMinutes, t.teamName,\n"
			        + "         tddn.docId, tddn.docName, tddn.finalFlag, tddn.bulkApprovedDocId, dmtmn.mimeType"
			        + " HAVING ( \n"
			        + "        :locationCount IS NULL \n"
			        + "        OR COUNT(DISTINCT etlm.locationMappingId) = :locationCount \n"
			        + "    ) \n"
			        + "    AND ( \n"
			        + "        :projectCount IS NULL \n"
			        + "        OR COUNT(DISTINCT ptsn.id.projectId) = :projectCount \n"
			        + "    ) \n"
			        )
			Page<GetReporteesTimesheetReqFlatDTO> getMyReporteesTimesheetRequests(
			        @Param("managerId") Long managerId,
			        @Param("clientFilter") Boolean clientFilter,
			        @Param("employmentId") String employmentId,
			        @Param("employeeName") String employeeName,
			        @Param("dayType") String dayType,
			        @Param("projectName") String projectName,
			        @Param("clientName") String clientName,
			        @Param("clientLocation") String clientLocation,
			        @Param("poNo") String poNo,
			        @Param("shadowEmpName") String shadowEmpName,
			        @Param("teamName") String teamName,
			        @Param("activity") String activity,
			        @Param("date") String date,
			        @Param("search") String search,
			        @Param("workCheckIn") String workCheckIn,
			        @Param("workCheckOut") String workCheckOut,
			        @Param("locationCount") Long locationCount,
			        @Param("projectCount") Long projectCount,
			        @Param("appliedBy") String appliedBy,
			        @Param("appliedOn") String appliedOn,
			        Pageable pageable
			);	
//			
			
//			@Query(value = "SELECT \n"
//					+ "    etn.timesheet_id, etn.emp_id, \n"
//					+ "    CASE\n"
//					+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
//					+ "        ELSE CONCAT('A-', e.employeement_id)\n"
//					+ "    END as employeement_id, \n"
//					+ "    e.name,  dtm.day_type, etn.date, etn.is_night_shift, etn.work_in_time, \n"
//					+ "    etn.work_out_time,COUNT(DISTINCT ptsn.project_id), COUNT(DISTINCT etlm.location_mapping_id), \n"
//					+ "    ab.name, etn.created_on,wltm.code, etlm.location_in_time, etlm.location_out_time, \n"
//					+ "    etlm.location_mapping_id,ptsn.project_id, p.project_name, c.client_name, cl.client_location,\n"
//					+ "    ptsn.po_no, es.name, ptsn.status, ptsn.total_client_working_minutes,ptsn.description, \n"
//					+ "    a.activity, etamn.description,etamn.duration_minutes, t.team_name,tddn.doc_id, tddn.doc_name, \n"
//					+ "    tddn.final_flag,tddn.bulk_approved_doc_id, dmtmn.mime_type\n"
//					+ "FROM employee_timesheets_new etn \n"
//					+ "INNER JOIN employee e ON etn.emp_id = e.emp_id\n"
//					+ "INNER JOIN employee ab ON etn.created_by = ab.emp_id\n"
//					+ "INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id \n"
//					+ "INNER JOIN teams t ON t.team_id = etm.team_id\n"
//					+ "INNER JOIN projects p ON t.project_id = p.project_id\n"
//					+ "LEFT JOIN project_timesheet_status_new ptsn ON ptsn.timesheet_id = etn.timesheet_id\n"
//					+ "LEFT JOIN employee_timesheet_location_mapping etlm ON etn.timesheet_id = etlm.timesheet_id\n"
//					+ "LEFT JOIN employee_timesheet_activities_mapping_new etamn ON etamn.timesheet_id = etn.timesheet_id\n"
//					+ "LEFT JOIN activities a ON a.activity_id = etamn.activity_id and a.team_id = t.team_id and a.team_id = etm.team_id \n"
//					+ "LEFT JOIN clients c ON ptsn.client_side_id = c.client_id\n"
//					+ "LEFT JOIN client_locations cl ON cl.client_location_id = ptsn.client_location_id\n"
//					+ "LEFT JOIN work_location_type_master wltm ON wltm.work_location_type_id = etlm.location_type_id\n"
//					+ "LEFT JOIN day_type_master_new dtm ON dtm.day_type_id = etn.day_type_id\n"
//					+ "LEFT JOIN timesheet_document_details_new tddn ON tddn.timesheet_id = etn.timesheet_id\n"
//					+ "LEFT JOIN employee es ON ptsn.shadow_emp_id = es.emp_id\n"
//					+ "LEFT JOIN doc_mime_type_master_new dmtmn ON dmtmn.mime_type_id = tddn.mime_type_id\n"
//					+ "WHERE \n"
//					+ "		(\n"
//					+ "        CASE\n"
//					+ "			 WHEN e.approvals_to = 'Reporting Manager' THEN e.reporting_manager_id\n"
//					+ "			 ELSE e.manager_id\n"
//					+ "		END\n"
//					+ "		) = :managerId\n"
//					+ "	 AND (\n"
//					+ "		 :clientFilter IS NULL\n"
//					+ "		 OR (:clientFilter = TRUE AND p.has_client_side_id = TRUE)\n"
//					+ "		 OR (:clientFilter = FALSE AND (p.has_client_side_id = FALSE OR p.has_client_side_id IS NULL))\n"
//					+ "	 )\n"
//					+ "AND (\n"
//					+ " :employmentId IS NULL\n"
//					+ " OR LOWER(\n"
//					+ "     CASE\n"
//					+ "         WHEN e.is_apmosys_product = 'true'\n"
//					+ "         THEN CONCAT('AP-', e.employeement_id)\n"
//					+ "         ELSE CONCAT('A-', e.employeement_id)\n"
//					+ "     END\n"
//					+ " ) LIKE LOWER(CONCAT('%', :employmentId, '%'))\n"
//					+ ")\n"
//					+ "\n"
//					+ "AND (:employeeName IS NULL \n"
//					+ "     OR LOWER(e.name) LIKE LOWER(CONCAT('%', :employeeName, '%')))\n"
//					+ "\n"
//					+ "AND (:dayType IS NULL \n"
//					+ "     OR LOWER(dtm.day_type) LIKE LOWER(CONCAT('%', :dayType, '%')))\n"
//					+ "\n"
//					+ "AND (:projectName IS NULL \n"
//					+ "     OR LOWER(p.project_name) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
//					+ "\n"
//					+ "AND (:clientName IS NULL \n"
//					+ "     OR LOWER(c.client_name) LIKE LOWER(CONCAT('%', :clientName, '%')))\n"
//					+ "\n"
//					+ "AND (:clientLocation IS NULL \n"
//					+ "     OR LOWER(cl.client_location) LIKE LOWER(CONCAT('%', :clientLocation, '%')))\n"
//					+ "\n"
//					+ "AND (:poNo IS NULL \n"
//					+ "     OR LOWER(ptsn.po_no) LIKE LOWER(CONCAT('%', :poNo, '%')))\n"
//					+ "\n"
//					+ "AND (:shadowEmpName IS NULL \n"
//					+ "     OR LOWER(COALESCE(es.name, '')) \n"
//					+ "     LIKE LOWER(CONCAT('%', :shadowEmpName, '%')))\n"
//					+ "\n"
//					+ "AND (:teamName IS NULL \n"
//					+ "     OR LOWER(t.team_name) LIKE LOWER(CONCAT('%', :teamName, '%')))\n"
//					+ "\n"
//					+ "AND (:activity IS NULL \n"
//					+ "     OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :activity, '%')))\n"
//					+ "\n"
//					+ " AND (\n"
//					+ "	 :date IS NULL\n"
//					+ "	 OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE CONCAT(:date, '%')\n"
//					+ " )\n"
//			        + "    AND (\n"
//			        + "        :search IS NULL OR :search = ''\n"
//			        + "        OR LOWER(\n"
//			        + "            CASE\n"
//			        + "                WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)\n"
//			        + "                ELSE CONCAT('A-', e.employeementId)\n"
//			        + "            END\n"
//			        + "        ) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(dtmn.dayType) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(cl.clientLocation) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(ptsn.poNo) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(COALESCE(es.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR LOWER(a.activity) LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "        OR FUNCTION('DATE_FORMAT', etn.date, '%d/%m/%Y') LIKE LOWER(CONCAT('%', :search, '%'))\n"
//			        + "    )\n"
////					+ " 	AND etn.status = :status\n"
//					+ "GROUP BY \n"
//					+ "    etn.timesheet_id,etn.emp_id,e.is_apmosys_product,e.employeement_id,e.name,dtm.day_type,etn.date,\n"
//					+ "    etn.is_night_shift,etn.work_in_time,etn.work_out_time,ab.name,etn.created_on,wltm.code,etlm.location_in_time,\n"
//					+ "    etlm.location_out_time,etlm.location_mapping_id,ptsn.project_id ,c.client_name,cl.client_location,ptsn.po_no,\n"
//					+ "    es.name,ptsn.status,ptsn.total_client_working_minutes,ptsn.description,a.activity,etamn.description,\n"
//					+ "    etamn.duration_minutes,t.team_name,tddn.doc_id,tddn.doc_name,tddn.final_flag,tddn.bulk_approved_doc_id,\n"
//					+ "    dmtmn.mime_type\n", nativeQuery = true
//					)
//			Page<GetReporteesTimesheetReqFlatDTO> getMyReporteesTimesheetRequests(
//			        @Param("managerId") Long managerId,
//			        @Param("clientFilter") Boolean clientFilter,
//			        @Param("employmentId") String employmentId,
//			        @Param("employeeName") String employeeName,
//			        @Param("dayType") String dayType,
//			        @Param("projectName") String projectName,
//			        @Param("clientName") String clientName,
//			        @Param("clientLocation") String clientLocation,
//			        @Param("poNo") String poNo,
//			        @Param("shadowEmpName") String shadowEmpName,
//			        @Param("teamName") String teamName,
//			        @Param("activity") String activity,
//			        @Param("date") String date,
//			        @Param("search") String search,
////                    @Param(status) Integer Status ,
//			        Pageable pageable
//			);
//	
	
	
}
