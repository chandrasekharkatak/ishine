package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.TimesheetActivityMap;

public interface TimesheetActivityMapRepository extends JpaRepository<TimesheetActivityMap, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> activitiesByTimesheetId(Long timesheetId);

@Query(nativeQuery = true, value = "SELECT tim.description, tim.emp_id, tim.created_on, tim.day_type, map.timesheet_id, a.team_id, p.project_name, c1.client_name, " +
	     "t.team_name, c.client_location, map.completion_time " +
	     "FROM db_emp_portal.employee_timesheet_activities_mapping AS map " +
	     "JOIN db_emp_portal.client_locations AS c ON map.client_location_id = c.client_id " +
	     "JOIN db_emp_portal.activities AS a ON map.activity_id = a.activity_id " +
	     "LEFT JOIN db_emp_portal.teams AS t ON a.team_id = t.team_id " +
	     "LEFT JOIN db_emp_portal.projects AS p ON t.project_id = p.project_id " +
	     "LEFT JOIN db_emp_portal.clients AS c1 ON p.client_id = c1.client_id " +
	     "LEFT JOIN db_emp_portal.employee_timesheets AS tim ON map.timesheet_id = tim.timesheet_id " +
	     "WHERE tim.emp_id = ? AND DATE_FORMAT(tim.created_on, '%Y-%m-%d') = ?")
	List<Object[]> activitiesByTimesheetIdforBiomax(@Param("emp_id") String empId, @Param("date") String date);

	@Transactional
	public void deleteByTimesheetId(Long timesheetId);

	public List<TimesheetActivityMap> findByTimesheetId(Long timesheetId);
	

	public Long countByActivityId(Long activityId);

	@Query(nativeQuery = true)
	public List<TimesheetActivityMap> getTimesheetActivityByTimesheetId(Long timesheetId);

}
