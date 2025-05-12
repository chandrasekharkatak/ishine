package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Timesheet;

@Repository
public interface TimesheetsRepository extends JpaRepository<Timesheet, Long> {

	public List<Timesheet> findAllByEmpIdAndDateBetweenOrderByDateDesc(Long empId,LocalDate start, LocalDate end);

//	@Query(nativeQuery = true)
//	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status);
	
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO (et.timesheetId, et.date, et.dayType, ec.name, et.description, et.status, \n"
			+ "eh.name, eh.empId, et.createdOn, ec.employeementId, et.totalTime, ec.email, et.officeInTime, et.officeOutTime, et.totalWorkingHours, et.isNightShift, et.currentManagerId, ee.isConsultant, ee.isApprenticeship, ee.empId )\n"
			+ "FROM Timesheet et \n"
			+ "INNER JOIN Employee ec ON ec.empId = et.empId \n"
			+ "INNER JOIN Employee eh ON eh.createdBy = et.empId \n"
			+ "INNER JOIN Employee ee ON ee.empId = et.empId \n"
			+ "WHERE et.status = :status AND et.currentManagerId = :managerId Order by et.date desc")
			public List<TimesheetDTO> getMyReporteesTimesheetRequests(
			    @Param("managerId") Long managerId, 
			    @Param("status") String status
			   );
	
//	@Query(nativeQuery = true)
//	public Long countMyReporteesTimesheetRequests(Long managerId);

	@Query(nativeQuery = true)
	public Long countMyReporteesTimesheetRequests(Long managerId, LocalDate dateOfJoining);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesApprovedTimesheets(Long managerId, LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getLast7DaysTimesheetsByEmpId(Long empId,LocalDate date);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetsForHomePageByEmpId(Long empId, LocalDate start, LocalDate end);

	public Timesheet findByEmpIdAndDate(Long empId, LocalDate dateToday);
	
	List<Timesheet> findByEmpIdAndTimesheetIdIn(Long empId,List<Long> timesheetIds);

	List<Timesheet> findByTimesheetIdIn(List<Long> timesheetIds);

	@Query(nativeQuery = true, value =
			"Select emp_Id,date,status "
			+"from employee_timesheets "
			+"where emp_Id=:empId and date=:localDate")
	List<Object[]>  getTimesheetDataByEmpIdAndDate( Long empId,LocalDate localDate);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllTimesheetData();

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysPendingTimesheetReport(LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysFilledTimesheetReport(LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamTimesheets(Long createdBy, LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTimesheets(Long empId, LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Timesheet> findTimesheetOnLeaveDate(Long empId, String start, String end);
	
//	@Query(nativeQuery = true)
//	public Optional<Timesheet> findExistingTimesheetOnLeaveDate(Long empId, LocalDate fromDate, LocalDate toDate);

//	@Query(nativeQuery = true)
//	public List<Timesheet> findTimesheetOnLeaveDate(Long empId, LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveTimesheetsWithoutLeaveApplication(LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getInactiveActivitiesByTimesheetId(Long timesheetId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyTeamsFilledEodCountByManagerId(LocalDate start, LocalDate end, Long managerId);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetFilledByMember(Long empId, LocalDate date);
	
//	@Query(value = "SELECT et.emp_id, e.manager_id FROM employee e " +
//            "INNER JOIN employee_timesheets et ON e.emp_id = et.emp_id", nativeQuery = true)
//List<Object[]> findEmployeesAndTheirManagers();
//
//@Modifying
//@Transactional
//@Query(value = "UPDATE employee_timesheets SET current_manager_id = :managerId WHERE emp_id = :empId", nativeQuery = true)
//void updateCurrentManagerId(Long empId, Long managerId);
	
//	@Query(value = "SELECT DISTINCT emp_id FROM employee_timesheets", nativeQuery = true)
//    List<Long> findDistinctEmpIds();
//	
//	@Query(value = "SELECT * FROM employee_timesheets WHERE emp_id = :empId ORDER BY created_on DESC", nativeQuery = true)
//	List<Timesheet> findTimesheetsByEmpIdOrderByCreatedOn(Long empId);
//	
//	@Modifying
//	@Transactional
//	@Query(value = "UPDATE employee_timesheets SET current_manager_id = :managerId WHERE timesheet_id = :timesheetId", nativeQuery = true)
//	void updateCurrentManagerId(Long timesheetId, Long managerId);

	
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
			+ "ORDER BY et.created_on DESC" ,nativeQuery = true)
	List<Object[]> getAllEmployeeTimesheetsBetweenDates(@Param("startDate") String startDate, @Param("endDate") String endDate);
	
	
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
			+ "ORDER BY et.created_on DESC ",nativeQuery = true)
	 List<Object[]> getTimesheetsByDepartmentAndDateRange(@Param("deptId") Long deptId,
	            @Param("startDate") String startDate,
	            @Param("endDate") String endDate);
	
}
