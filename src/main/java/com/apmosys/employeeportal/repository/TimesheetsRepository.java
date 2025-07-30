package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Timesheet;

@Repository
public interface TimesheetsRepository extends JpaRepository<Timesheet, Long> {

	public List<Timesheet> findAllByEmpIdAndDateBetweenOrderByDateDesc(Long empId,LocalDate start, LocalDate end);

//	@Query(nativeQuery = true)
//	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status,LocalDate dateOfJoining);
	
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
	public List<Object[]> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise(LocalDate start, LocalDate end,List<Long> deptIds);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise(LocalDate start, LocalDate end,Long deptId);
	
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
	 
	 
	 
	      
	 @Query(
			    value = "SELECT " +
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
			    "LEFT JOIN clients c ON p.client_id = c.client_id",
			    nativeQuery = true
			)

	      List<Object[]> getLastFilledTimesheet(@Param("empId") Long empId);
	      
	      
	      
	      
	      
	      
	     
	      
	      @Query(
	    		    value = "SELECT DISTINCT et.emp_id, etm.active, etm.team_id, et.date " +
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
	    		            "WHERE et.emp_id = :empId",
	    		    nativeQuery = true
	    		)
   
	    		List<Object[]> checkEmployeeActiveOrNot(@Param("empId") Long empId);

//	            added by sakti for duplicate timesheet check
//	    		Optional<Timesheet> findByEmpIdAndDate(Long empId, Date date);
	      
	      
		@Query(value ="select new com.apmosys.employeeportal.dto.ProjectDTO(p.projectId, p.projectName )  \n"+
				"from Project p  \n"+
				"inner join Team t on t.projectId = p.projectId \n"+
				"inner join EmployeeTeamMap etm on etm.teamId = t.teamId \n"+
				"where etm.empId = :empId and etm.active = 1")
		public List<ProjectDTO> getActiveProjectsByEmpId(Long empId);
		
		@Query(value ="select new com.apmosys.employeeportal.dto.ProjectClientSideIdDTO(CAST(p.projectId as long), p.projectName, ecsm.clientSideId )  \n"+
				"from Project p  \n"+
				"inner join Team t on t.projectId = p.projectId \n"+
				"inner join EmployeeTeamMap etm on etm.teamId = t.teamId \n"+
				"left join EmployeeClientSideIdMapping ecsm on ecsm.projectId = p.projectId AND ecsm.active = TRUE \n"+
				"where etm.empId = :empId and etm.active = 1")
		public List<ProjectClientSideIdDTO> getActiveProjectsAndClientSideIdByEmpId(Long empId);
		
		@Query(value = "SELECT DISTINCT e.name employee_name, p.project_name, t.team_name, " +
	               "CASE WHEN po_project_type IS NOT NULL THEN po_project_type " +
	               "ELSE internal_project_type END AS project_type, " +
	               "et.date, day_type, et.total_time, " +
	               "a.activity, etam.description, pm.name Project_Manager_name " +
	               "FROM employee_timesheets et " +
	               "INNER JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id " +
	               "INNER JOIN activities a ON a.activity_id = etam.activity_id " +
	               "INNER JOIN teams t ON a.team_id = t.team_id " +
	               "LEFT JOIN projects p ON t.project_id = p.project_id " +
	               "LEFT JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id " +
	               "LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id " +
	               "LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id " +
	               "LEFT JOIN employee e ON et.emp_id = e.emp_id " +
	               "WHERE e.employmentstatus != 'InActive' AND p.active = 'true' " +
	               "AND etm.active != 0 AND t.is_active != 'N' " +
	               "AND et.emp_id IN (:empId) " +
	               "AND (et.date BETWEEN :fromDate AND :toDate)",
	       nativeQuery = true)
		public List<Object[]> findByEmpIdAndDateBetween(@Param("empId") Long empId,
	                                         @Param("fromDate") String fromDate,
	                                         @Param("toDate") String toDate);

//		@Query(value ="")
//		public List<TimesheetDTO> getTotalVmsFilledCount(String clientApprovalStatus);
//
//		@Query(value ="")
//		public List<TimesheetDTO> totalIshineFilledCount(String status);
	
}
