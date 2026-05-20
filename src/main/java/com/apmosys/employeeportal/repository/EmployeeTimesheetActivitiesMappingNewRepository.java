// package com.apmosys.employeeportal.repository;

// import java.util.List;

// import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import javax.transaction.Transactional;

// public interface EmployeeTimesheetActivitiesMappingNewRepository extends JpaRepository<EmployeeTimesheetActivitiesMappingNew, Long> {
//     @Modifying
//     @Transactional
//     @Query("DELETE FROM EmployeeTimesheetActivitiesMappingNew a WHERE a.timesheetId = :timesheetId")
//     void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);


//     @Query(value =
//     "SELECT etam.timesheet_id, " +
//     "ac.activity, " +
//     "ac.eta, " +
//     "etam.description, " +
//     "CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time, " +
//     "p.project_name, " +
//     "c.client_name, " +
//     "cl.client_location, " +
//     "t.team_name, " +
//     "e.name as employee, " +
//     "e2.name as manager, " +
//     "ac.activity_id, " +
//     "p.project_id, " +
//     "etam.id, " +
//     "c.client_id, " +
//     "cl.client_location_id, " +
//     "t.team_id, " +
//     "e.is_consultant, " +
//     "e.is_apprenticeship " +
//     "FROM employee_timesheet_activities_mapping_new etam " +
//     "INNER JOIN activities ac ON ac.activity_id = etam.activity_id " +
//     "INNER JOIN teams t ON t.team_id = ac.team_id " +
//     "INNER JOIN projects p ON p.project_id = etam.project_id " +
//     "INNER JOIN clients c ON c.client_id = p.client_id " +
//     "INNER JOIN employee_timesheets_new et ON et.timesheet_id = etam.timesheet_id " +
//     "INNER JOIN employee e ON e.emp_id = et.emp_id " +
//     "INNER JOIN employee e2 ON e2.emp_id = et.current_manager_id " +
//     "INNER JOIN project_timesheet_status_new pts " +
//     "ON pts.timesheet_id = etam.timesheet_id " +
//     "AND pts.project_id = etam.project_id " +
//     "INNER JOIN client_locations cl " +
//     "ON cl.client_location_id = pts.client_location_id " +
//     "WHERE etam.timesheet_id IN (:timesheetIds)",
//     nativeQuery = true)
//    List<Object[]> activitiesByTimesheetIds(
//     @Param("timesheetIds") List<Long> timesheetIds
// );
// }
