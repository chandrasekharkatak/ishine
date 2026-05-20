//package com.apmosys.employeeportal.repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.apmosys.employeeportal.model.TimesheetActivityMap;
//
//public interface TimesheetActivityMapRepository extends JpaRepository<TimesheetActivityMap, Long> {
//
//	@Query(nativeQuery = true)
//	public List<Object[]> activitiesByTimesheetId(Long timesheetId);
//
////@Query(nativeQuery = true, value = "SELECT tim.description, tim.emp_id, tim.created_on, tim.day_type, map.timesheet_id, a.team_id, p.project_name, c1.client_name, " +
////	     "t.team_name, c.client_location, map.completion_time " +
////	     "FROM db_emp_portal.employee_timesheet_activities_mapping AS map " +
////	     "JOIN db_emp_portal.client_locations AS c ON map.client_location_id = c.client_id " +
////	     "JOIN db_emp_portal.activities AS a ON map.activity_id = a.activity_id " +
////	     "LEFT JOIN db_emp_portal.teams AS t ON a.team_id = t.team_id " +
////	     "LEFT JOIN db_emp_portal.projects AS p ON t.project_id = p.project_id " +
////	     "LEFT JOIN db_emp_portal.clients AS c1 ON p.client_id = c1.client_id " +
////	     "LEFT JOIN db_emp_portal.employee_timesheets AS tim ON map.timesheet_id = tim.timesheet_id " +
////	     "WHERE tim.emp_id = ? AND DATE_FORMAT(tim.created_on, '%Y-%m-%d') = ?")
//// @Query(nativeQuery = true, value = "select et.timesheet_id,et.emp_id,et.date,a.team_id,t.team_name,p.client_id,c.client_name,\n"
//// 		+ "cl.client_location,a.activity,a.eta ,etam.completion_time, emp.name\n"
//// 		+ "from employee_timesheets et \n"
//// 		+ "inner join employee as emp on et.emp_id=emp.emp_id \n"
//// 		+ "inner join employee_timesheet_activities_mapping etam on etam.timesheet_id = et.timesheet_id\n"
//// 		+ "inner join activities a on etam.activity_id = a.activity_id\n"
//// 		+ "inner join teams t on t.team_id = a.team_id\n"
//// 		+ "inner join projects p on p.project_id = t.project_id\n"
//// 		+ "inner join clients c on c.client_id = p.client_id\n"
//// 		+ "inner join client_locations cl on cl.client_id = p.client_id\n"
//// 		+ "where et.emp_id IN (?) and DATE_FORMAT(et.date, '%Y-%m-%d')= ? group by etam.timesheet_id")
//// 	List<Object[]> activitiesByTimesheetIdforBiomax(@Param("emp_id") Long empId, @Param("date") String date);
//
//	@Transactional
//	public void deleteByTimesheetId(Long timesheetId);
//
//	public List<TimesheetActivityMap> findByTimesheetId(Long timesheetId);
//
////	@Query(value =
////	        "SELECT etam.timesheet_id, " +
////	        "ac.activity, " +
////	        "ac.eta, " +
////	        "etam.description, " +
////	        "CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time, " +
////	        "p.project_name, " +
////	        "c.client_name, " +
////	        "cl.client_location, " +
////	        "t.team_name, " +
////	        "e.name as employee, " +
////	        "e2.name as manager, " +
////	        "ac.activity_id, " +
////	        "p.project_id, " +
////	        "etam.id, " +
////	        "c.client_id, " +
////	        "cl.client_location_id, " +
////	        "t.team_id, " +
////	        "e.is_consultant, " +
////	        "e.is_apprenticeship " +
////	        "FROM employee_timesheet_activities_mapping_new etam " +
////	        "INNER JOIN activities ac ON ac.activity_id = etam.activity_id " +
////	        "INNER JOIN teams t ON t.team_id = ac.team_id " +
////	        "INNER JOIN projects p ON p.project_id = etam.project_id " +
////	        "INNER JOIN clients c ON c.client_id = p.client_id " +
////	        "INNER JOIN employee_timesheets_new et ON et.timesheet_id = etam.timesheet_id " +
////	        "INNER JOIN employee e ON e.emp_id = et.emp_id " +
////	        "INNER JOIN employee e2 ON e2.emp_id = et.current_manager_id " +
////	        "INNER JOIN project_timesheet_status_new pts " +
////	        "ON pts.timesheet_id = etam.timesheet_id " +
////	        "AND pts.project_id = etam.project_id " +
////	        "INNER JOIN client_locations cl " +
////	        "ON cl.client_location_id = pts.client_location_id " +
////	        "WHERE etam.timesheet_id IN (:timesheetIds)",
////	        nativeQuery = true)
////	List<Object[]> activitiesByTimesheetIds(
////	        @Param("timesheetIds") List<Long> timesheetIds
////	);
//
//
//	public Long countByActivityId(Long activityId);
//
//	@Query(nativeQuery = true)
//	public List<TimesheetActivityMap> getTimesheetActivityByTimesheetId(Long timesheetId);
//
//
//}
