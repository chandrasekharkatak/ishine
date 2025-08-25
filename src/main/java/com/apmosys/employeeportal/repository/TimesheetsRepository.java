package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
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
				"where etm.empId = :empId and etm.active = 1 and t.isActive = 'Y' and p.active = 'true'")
		public List<ProjectDTO> getActiveProjectsByEmpId(Long empId);
		
		@Query(value ="select new com.apmosys.employeeportal.dto.ProjectClientSideIdDTO(CAST(p.projectId as long), p.projectName, ecsm.clientSideId )  \n"+
				"from Project p  \n"+
				"inner join Team t on t.projectId = p.projectId \n"+
				"inner join EmployeeTeamMap etm on etm.teamId = t.teamId \n"+
				"left join EmployeeClientSideIdMapping ecsm on ecsm.projectId = p.projectId AND ecsm.active = TRUE AND ecsm.empId = etm.empId \n"+
				"where etm.empId = :empId and etm.active = 1 and p.active = 'true' and t.isActive = 'Y'")
		public List<ProjectClientSideIdDTO> getActiveProjectsAndClientSideIdByEmpId(Long empId);
		
		@Query(value = "SELECT DISTINCT e.name, p.project_name, t.team_name,  \n" +
		        "       CASE WHEN po_project_type IS NOT NULL THEN po_project_type  \n" +
		        "            ELSE internal_project_type END AS project_type,  \n" +
		        "       et.date, day_type, et.total_time,  \n" +
		        "       GROUP_CONCAT(distinct a.activity ORDER BY a.activity SEPARATOR ',') AS activity, \n" +
		        "       GROUP_CONCAT(distinct etam.description ORDER BY etam.description SEPARATOR ',') AS description, \n" +
		        "       pm.name AS Project_Manager_name, et.timesheet_id, et.status, \n" +
		        "       client_in_time, client_out_time, \n" +
		        "       CASE WHEN final_flag = 0 THEN doc_id END AS doc_id_1, \n" +
		        "       CASE WHEN final_flag = 1 THEN doc_id END AS doc_id_2 \n" +
		        "  FROM employee_timesheets et  \n" +
		        "  INNER JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id  \n" +
		        "  INNER JOIN activities a ON a.activity_id = etam.activity_id  \n" +
		        "  INNER JOIN teams t ON a.team_id = t.team_id  \n" +
		        "  LEFT JOIN projects p ON t.project_id = p.project_id  \n" +
		        "  LEFT JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id  \n" +
		        "  LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id  \n" +
		        "  LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id  \n" +
		        "  LEFT JOIN employee e ON et.emp_id = e.emp_id  \n" +
		        "  LEFT JOIN timesheet_document_details tdd ON tdd.emp_id = et.emp_id AND DATE(tdd.created_on) = DATE(et.date) \n" +
		        " WHERE e.employmentstatus != 'InActive'  \n" +
		        "   AND p.active = 'true'  \n" +
		        "   AND etm.active != 0  \n" +
		        "   AND t.is_active != 'N'  \n" +
		        "   AND et.emp_id = :empId  \n" +  
		        "   AND (et.date BETWEEN :fromDate AND :toDate) \n" +
		        "GROUP BY e.name, p.project_name, t.team_name, et.date, day_type, et.total_time,  \n" +
		        "         pm.name, et.timesheet_id, et.status, client_in_time, client_out_time",
		       nativeQuery = true)
		List<Object[]> findByEmpIdAndDateBetween(@Param("empId") Long empId,
		                                         @Param("fromDate") String fromDate,
		                                         @Param("toDate") String toDate);

		
		@Query(value = "SELECT COUNT(DISTINCT emp_id) FROM employee_timesheets WHERE client_side_id IS NOT NULL AND client_side_id != ''", nativeQuery = true)
		public List<Object[]> getTotalVmsFilledCount(String clientApprovalStatus);

		@Query(value = "SELECT COUNT(DISTINCT emp_id) AS employee_count_with_client_id " +
	               "FROM employee_timesheets " +
	               "WHERE MONTH(created_on) = MONTH(CURRENT_DATE()) " +
	               "AND YEAR(created_on) = YEAR(CURRENT_DATE()) " +
	               "AND day_type = 'Working'", nativeQuery = true)
		public List<Object[]> totalIshineFilledCount();
		
		@Query(value = "select count(distinct e.emp_id) from employee e inner join employee_timesheets et\n"
				+ "on e.emp_id = et.emp_id \n"
				+ "where e.emp_id not in\n"
				+ "(\n"
				+ "SELECT DISTINCT emp_id AS employee_count_with_client_id\n"
				+ "FROM employee_timesheets\n"
				+ "WHERE client_side_id IS NOT NULL AND client_side_id != ''\n"
				+ ")\n"
				+ "and client_side_id IS NOT NULL AND client_side_id != ''", nativeQuery = true)
		public List<Object[]> totalvmsNotFilled(String clientApprovalStatus);

		@Query(value = "WITH RECURSIVE\n"
				+ "    Date_Generator (dt) AS (\n"
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
				+ " Base_Project_Employees AS (\n"
				+ "        SELECT DISTINCT etm.emp_id\n"
				+ "        FROM projects p\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
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
				+ "            COUNT(CASE WHEN client_approval_status = 'pending' THEN 1 END) AS Client_pending_count,\n"
				+ "            COUNT(CASE WHEN client_approval_status = 'approved' THEN 1 END) AS Client_Approved_count,\n"
				+ "            COUNT(CASE WHEN client_approval_status = 'rejected' THEN 1 END) AS Client_Rejected_count -- ADDED\n"
				+ "        FROM timesheet_document_details\n"
				+ "         WHERE MONTH(created_on) = MONTH(CURRENT_DATE()) AND YEAR(created_on) = YEAR(CURRENT_DATE())\n"
				+ "        GROUP BY emp_id\n"
				+ "    ),\n"
				+ " Final_Counts AS (\n"
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
				+ "\n"
				+ "    ( (fc.totalUniqueEmployees * wds.expected_fill_count) - (fc.totalApproved + fc.totalPending) ) AS eod_not_filled,\n"
				+ "\n"
				+ "    (fc.totalApproved + fc.totalPending + fc.totalRejected) as totalSubmitted,\n"
				+ "\n"
				+ "    CASE\n"
				+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
				+ "        THEN (fc.totalApproved * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
				+ "        ELSE 0\n"
				+ "    END AS document_approved_percentage,\n"
				+ "\n"
				+ "    CASE\n"
				+ "        WHEN (fc.totalApproved + fc.totalPending + fc.totalRejected) > 0\n"
				+ "        THEN (fc.totalRejected * 100.0 / (fc.totalApproved + fc.totalPending + fc.totalRejected))\n"
				+ "        ELSE 0\n"
				+ "    END AS document_rejected_percentage\n"
				+ "FROM\n"
				+ "    Final_Counts fc,\n"
				+ "    WorkingDays_Summary wds ", nativeQuery = true)
		public List<Object[]> totalIshineNotFilledCount();
		
		@Query("SELECT et.date FROM Timesheet et " +
			       "INNER JOIN TimesheetDocumentDetails tdd ON et.timesheetId = tdd.timesheetId " +
			       "WHERE et.empId = :empId " +
			       "AND et.projectId = :projectId " +
			       "AND tdd.active = true " +
			       "AND tdd.finalFlag = true "+
			       "AND (tdd.rmApprovalStatus = 'Approved' OR tdd.rmApprovalStatus = 'Pending' OR tdd.hrApprovalStatus != 'Rejected')")
			Set<LocalDate> findDatesByEmpIdAndProjectId(@Param("empId") Long empId,
			                                             @Param("projectId") Integer projectId);


		@Query(value="WITH RECURSIVE\n"
				+ "    Date_Parameters AS (\n"
				+ "        SELECT\n"
				+ "            COALESCE(\n"
				+ "                STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),\n"
				+ "                DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
				+ "            ) AS from_date,\n"
				+ "            CASE\n"
				+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
				+ "                    IF(\n"
				+ "                        :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()),\n"
				+ "                        CURDATE(),\n"
				+ "                        LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
				+ "                    )\n"
				+ "                ELSE\n"
				+ "                    CURDATE()\n"
				+ "            END AS to_date\n"
				+ "    ),\n"
				+ "\n"
				+ "    All_Dates_In_Range AS (\n"
				+ "        SELECT DATE_ADD(dp.from_date, INTERVAL a.a + b.a DAY) AS dt\n"
				+ "        FROM Date_Parameters dp,\n"
				+ "             (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,\n"
				+ "             (SELECT 0 AS a UNION ALL SELECT 10 UNION ALL SELECT 20 UNION ALL SELECT 30) b\n"
				+ "        WHERE DATE_ADD(dp.from_date, INTERVAL a.a + b.a DAY) <= dp.to_date\n"
				+ "    ),\n"
				+ "    Valid_Working_Dates AS (\n"
				+ "        SELECT dt FROM All_Dates_In_Range WHERE dt NOT IN (SELECT DISTINCT date_of_holiday FROM holiday)\n"
				+ "    ),\n"
				+ "    WorkingDays_Summary AS (\n"
				+ "        SELECT COUNT(*) AS expected_fill_count FROM Valid_Working_Dates\n"
				+ "    ),\n"
				+ "\n"
				+ "    Base_Project_Employees AS (\n"
				+ "        SELECT DISTINCT\n"
				+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
				+ "            etm.start_date,\n"
				+ "            etm.active, p.project_id, p.project_name,\n"
				+ "            c.client_id, c.client_name,ecsm.client_side_id,p.po_no,\n"
				+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
				+ "            e.reporting_manager_id,\n"
				+ "             CASE\n"
				+ "                WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
				+ "                ELSE CONCAT('A-',e.employeement_id)\n"
				+ "            END AS employement_id,\n"
				+ "            d.name dept_name,\n"
				+ "            e1.name Project_manager_name,\n"
				+ "            e.email,\n"
				+ "            e.mobile_no\n"
				+ "        FROM projects p\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
				+ "        LEFT JOIN employee tl on tl.emp_id = t.team_lead_id\n"
				+ "        LEFT JOIN employee s on s.emp_id = t.spoc_id\n"
				+ "        LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
				+ "        LEFT JOIN department d on d.dept_id = jr.dept_id\n"
				+ "        left JOIN employee_client_side_id_mapping ecsm on e.emp_id = ecsm.emp_id and ecsm.project_id = t.project_id\n"
				+ "        LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ "        LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ "        WHERE etm.active != 0 AND t.is_active != 'N'\n"
				+ "        AND p.active != 'false' AND p.has_client_side_id = true\n"
				+ "        AND e.employmentstatus != 'InActive' AND date(etm.start_date) < curdate()\n"
				+ "    ),\n"
				+ "    Daily_Status_Details AS (\n"
				+ "        SELECT\n"
				+ "            bpe.emp_id,\n"
				+ "            adir.dt AS timesheet_date,\n"
				+ "            ts.client_in_time,\n"
				+ "            ts.client_out_time,\n"
				+ "           CASE\n"
				+ "            WHEN adir.dt < DATE(bpe.start_date) AND \n"
				+ "                 (DAYOFWEEK(adir.dt) = 1 OR \n"
				+ "                  (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
				+ "            THEN 'W'\n"
				+ "            WHEN adir.dt < DATE(bpe.start_date) THEN 'O'\n"
				+ "            WHEN EXISTS (SELECT 1 FROM timesheet_document_details tdd WHERE tdd.created_by = bpe.emp_id AND DATE(tdd.created_on) = adir.dt AND tdd.client_approval_status = 'approved' AND tdd.active = true) THEN 'DA'\n"
				+ "            WHEN EXISTS (SELECT 1 FROM timesheet_document_details tdd WHERE tdd.created_by = bpe.emp_id AND DATE(tdd.created_on) = adir.dt AND tdd.client_approval_status = 'pending' AND tdd.active = true) THEN 'DP'\n"
				+ "            WHEN ts.day_type like '%Leave%' THEN 'A'\n"
				+ "            WHEN ts.day_type like '%Non-Working%' THEN 'NW'\n"
				+ "            WHEN ts.day_type like '%Public Holiday%' THEN 'AH'\n"
				+ "            WHEN h.holiday_type like '%Week Off%' THEN 'W'\n"
				+ "            WHEN ts.day_type like '%Holiday%' THEN 'H'\n"
				+ "            WHEN ts.day_type like '%Client Holiday%' THEN 'CH'\n"
				+ "            WHEN ts.emp_id IS NOT NULL THEN 'P'\n"
				+ "            WHEN adir.dt IN (SELECT dt FROM Valid_Working_Dates) THEN 'A'\n"
				+ "            ELSE 'NA'\n"
				+ "        END AS daily_status\n"
				+ "        FROM Base_Project_Employees bpe\n"
				+ "        CROSS JOIN All_Dates_In_Range adir\n"
				+ "        LEFT JOIN holiday h ON adir.dt = h.date_of_holiday\n"
				+ "        LEFT JOIN employee_timesheets ts ON bpe.emp_id = ts.emp_id AND adir.dt = ts.date\n"
				+ "        WHERE bpe.emp_id = :empId \n"
				+ "    ),\n"
				+ "\n"
				+ "    Timesheet_Counts_In_Range AS (\n"
				+ "        SELECT emp_id, COUNT(*) as filled_count FROM employee_timesheets ts\n"
				+ "        JOIN Date_Parameters dp ON ts.date BETWEEN dp.from_date AND dp.to_date WHERE ts.day_type LIKE '%Working%' GROUP BY emp_id\n"
				+ "    ),\n"
				+ "    Document_Summary AS (\n"
				+ "        SELECT emp_id,\n"
				+ "            COUNT(DISTINCT CASE WHEN client_approval_status = 'pending' and final_flag = 1 THEN DATE(tdd.created_on) END) AS Client_pending_count,\n"
				+ "            COUNT(DISTINCT CASE WHEN client_approval_status = 'approved' and final_flag  = 1 THEN DATE(tdd.created_on) END) AS Client_Approved_count\n"
				+ "        FROM timesheet_document_details tdd \n"
				+ "        JOIN Date_Parameters dp ON DATE(tdd.created_on) BETWEEN dp.from_date AND dp.to_date \n"
				+ "        WHERE active = true \n"
				+ "        GROUP BY emp_id\n"
				+ "    )\n"
				+ "\n"
				+ "SELECT\n"
				+ "    bpe.emp_id,bpe.client_side_id,bpe.start_date,bpe.team_name,bpe.team_id,\n"
				+ "    bpe.name,bpe.spoc, bpe.billable_type,bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
				+ "    bpe.project_name,bpe.Project_manager_name,bpe.po_no,bpe.client_name,\n"
				+ "    bpe.reporting_manager_id,\n"
				+ "    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name, \n"
				+ "    wds.expected_fill_count AS expectedTimesheetFillCount,\n"
				+ "    IFNULL(tcir.filled_count, 0) AS apmosysTimesheetFilledCount,\n"
				+ "    (wds.expected_fill_count - (IFNULL(ds.Client_pending_count, 0) + IFNULL(ds.Client_Approved_count, 0))) as client_side_not_filled_count,\n"
				+ "    IFNULL(ds.Client_pending_count, 0) AS clientSidePendingCount,\n"
				+ "    IFNULL(ds.Client_Approved_count, 0) AS clientSideApprovedCount,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`\n"
				+ ",bpe.employement_id\n"
				+ "FROM\n"
				+ "    Base_Project_Employees bpe\n"
				+ "LEFT JOIN\n"
				+ "    Daily_Status_Details dsd ON bpe.emp_id = dsd.emp_id\n"
				+ "CROSS JOIN\n"
				+ "    WorkingDays_Summary wds\n"
				+ "LEFT JOIN\n"
				+ "    Timesheet_Counts_In_Range tcir ON bpe.emp_id = tcir.emp_id\n"
				+ "LEFT JOIN\n"
				+ "    Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
				+ "WHERE\n"
				+ "     bpe.emp_id = :empId \n"
				+ "GROUP BY\n"
				+ "    bpe.emp_id,bpe.client_side_id,bpe.start_date,bpe.team_name,bpe.team_id,\n"
				+ "    bpe.name,bpe.spoc, bpe.billable_type,bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
				+ "    bpe.project_name,bpe.Project_manager_name,bpe.po_no,bpe.client_name,\n"
				+ "    bpe.reporting_manager_id,\n"
				+ "    wds.expected_fill_count, tcir.filled_count, ds.Client_pending_count, ds.Client_Approved_count,bpe.employement_id\n"
				+ "ORDER BY\n"
				+ "    bpe.name " , nativeQuery = true)
		public List<Object[]> getEmployeeTimesheetAsCalender(
				  @Param("empId") Integer empId,
				  @Param("month") Integer month,
				  @Param("year") Integer year);
		
		@Query(nativeQuery=true,value="SELECT et.timesheet_id,et.date,et.day_type,e.name employeeName,et.description ,et.status, \n"
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
		public List<Object[]> getPendingTimesheetsByEmpAndTeam(Long empId, Long teamId);

		@Query(nativeQuery=true,value="SELECT DISTINCT et.timesheet_id,et.date,et.day_type,e.name employeeName,et.description ,et.status,\n"
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
				+ "                                        Order by et.date desc")
		public List<Object[]> getMyTimesheetRequests(Long empId,Long teamId,String fromDate, String toDate);

		@Query(value="WITH RECURSIVE\n"
				+ "    Date_Parameters AS (\n"
				+ "        SELECT\n"
				+ "            COALESCE(\n"
				+ "                STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'),\n"
				+ "                DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
				+ "            ) AS from_date,\n"
				+ "            CASE\n"
				+ "                WHEN :year IS NOT NULL AND :month IS NOT NULL THEN\n"
				+ "                    IF(\n"
				+ "                        :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE()),\n"
				+ "                        CURDATE(),\n"
				+ "                        LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
				+ "                    )\n"
				+ "                ELSE\n"
				+ "                    CURDATE()\n"
				+ "            END AS to_date\n"
				+ "    ),\n"
				+ "\n"
				+ "    All_Dates_In_Range AS (\n"
				+ "        SELECT DATE_ADD(dp.from_date, INTERVAL a.a + b.a DAY) AS dt\n"
				+ "        FROM Date_Parameters dp,\n"
				+ "             (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,\n"
				+ "             (SELECT 0 AS a UNION ALL SELECT 10 UNION ALL SELECT 20 UNION ALL SELECT 30) b\n"
				+ "        WHERE DATE_ADD(dp.from_date, INTERVAL a.a + b.a DAY) <= dp.to_date\n"
				+ "    ),\n"
				+ "    \n"
				+ "\n"
				+ "    Base_Project_Employees AS (\n"
				+ "        SELECT DISTINCT\n"
				+ "            etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type,\n"
				+ "            etm.start_date,\n"
				+ "            etm.active, p.project_id, p.project_name,\n"
				+ "            c.client_id, c.client_name,ecsm.client_side_id,p.po_no,\n"
				+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
				+ "            e.reporting_manager_id,\n"
				+ "             CASE\n"
				+ "                WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
				+ "                ELSE CONCAT('A-',e.employeement_id)\n"
				+ "            END AS employement_id,\n"
				+ "            d.name dept_name,\n"
				+ "            e1.name Project_manager_name,\n"
				+ "            e.email,\n"
				+ "            e.mobile_no\n"
				+ "        FROM projects p\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
				+ "        LEFT JOIN employee tl on tl.emp_id = t.team_lead_id\n"
				+ "        LEFT JOIN employee s on s.emp_id = t.spoc_id\n"
				+ "        LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
				+ "        LEFT JOIN department d on d.dept_id = jr.dept_id\n"
				+ "        LEFT JOIN employee_client_side_id_mapping ecsm on e.emp_id = ecsm.emp_id and ecsm.project_id = t.project_id\n"
				+ "        LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ "        LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ "        WHERE etm.active != 0 AND t.is_active != 'N'\n"
				+ "        AND p.active != 'false' AND p.has_client_side_id = true\n"
				+ "        AND e.employmentstatus != 'InActive' AND date(etm.start_date) < curdate()\n"
				+ "        AND p.project_id in (:projectId)\n"
				+ "    ),\n"
				+ "\n"
				+ "    Dynamic_Expected_Days AS (\n"
				+ "        SELECT DISTINCT bpe.emp_id, adir.dt\n"
				+ "        FROM Base_Project_Employees bpe\n"
				+ "        CROSS JOIN All_Dates_In_Range adir\n"
				+ "        WHERE\n"
				+ "            adir.dt <= CURDATE()\n"
				+ "            AND NOT EXISTS (\n"
				+ "                SELECT 1 FROM employee_timesheets et\n"
				+ "                WHERE et.emp_id = bpe.emp_id AND et.date = adir.dt\n"
				+ "                  AND (et.day_type LIKE '%Leave%' OR et.day_type LIKE '%Client Holiday%')\n"
				+ "            )\n"
				+ "            AND (\n"
				+ "                (adir.dt NOT IN (SELECT date_of_holiday FROM holiday))\n"
				+ "                OR\n"
				+ "                EXISTS (\n"
				+ "                    SELECT 1 FROM employee_timesheets et\n"
				+ "                    WHERE et.emp_id = bpe.emp_id AND et.date = adir.dt AND et.day_type LIKE '%Working%'\n"
				+ "                )\n"
				+ "            )\n"
				+ "    ),\n"
				+ "\n"
				+ "    Expected_Days_Summary AS (\n"
				+ "        SELECT emp_id, COUNT(dt) as expected_fill_count\n"
				+ "        FROM Dynamic_Expected_Days\n"
				+ "        GROUP BY emp_id\n"
				+ "    ),\n"
				+ "\n"
				+ "    Daily_Status_Details AS (\n"
				+ "        SELECT\n"
				+ "            bpe.emp_id, adir.dt AS timesheet_date,\n"
				+ "            ts.client_in_time, ts.client_out_time,\n"
				+ "            CASE\n"
				+ "                WHEN adir.dt < DATE(bpe.start_date) THEN 'O'\n"
				+ "                WHEN EXISTS (SELECT 1 FROM timesheet_document_details tdd WHERE tdd.created_by = bpe.emp_id AND DATE(tdd.created_on) = adir.dt AND tdd.client_approval_status = 'approved' AND tdd.active = true) THEN 'DA'\n"
				+ "                WHEN EXISTS (SELECT 1 FROM timesheet_document_details tdd WHERE tdd.created_by = bpe.emp_id AND DATE(tdd.created_on) = adir.dt AND tdd.client_approval_status = 'pending' AND tdd.active = true) THEN 'DP'\n"
				+ "                WHEN ts.day_type LIKE '%Leave%' THEN 'A'\n"
				+ "                WHEN ts.day_type LIKE '%Non-Working%' THEN 'NW'\n"
				+ "                WHEN ts.day_type LIKE '%Holiday%' THEN 'H'\n"
				+ "     		   WHEN ts.day_type LIKE '%Week Off%' THEN 'WO' \n"
				+ "                WHEN ts.emp_id IS NOT NULL THEN 'P'\n"
				+ "                WHEN adir.dt NOT IN (SELECT date_of_holiday FROM holiday) THEN 'A'\n"
				+ "                ELSE 'NA'\n"
				+ "            END AS daily_status\n"
				+ "        FROM Base_Project_Employees bpe\n"
				+ "        CROSS JOIN All_Dates_In_Range adir\n"
				+ "        LEFT JOIN employee_timesheets ts ON bpe.emp_id = ts.emp_id AND adir.dt = ts.date\n"
				+ "    ),\n"
				+ "\n"
				+ "    Timesheet_Counts_In_Range AS (\n"
				+ "        SELECT emp_id, COUNT(DISTINCT date) as filled_count\n"
				+ "        FROM employee_timesheets ts\n"
				+ "        JOIN Date_Parameters dp ON ts.date BETWEEN dp.from_date AND dp.to_date\n"
				+ "        WHERE ts.day_type LIKE '%Working%'\n"
				+ "        GROUP BY emp_id\n"
				+ "    ),\n"
				+ "    \n"
				+ "    Document_Summary AS (\n"
				+ "        SELECT emp_id,\n"
				+ "            COUNT(DISTINCT CASE WHEN client_approval_status = 'pending' AND tdd.final_flag = 1 THEN DATE(tdd.created_on) END) AS Client_pending_count,\n"
				+ "            COUNT(DISTINCT CASE WHEN client_approval_status = 'approved' AND tdd.final_flag = 1 THEN DATE(tdd.created_on) END) AS Client_Approved_count\n"
				+ "        FROM timesheet_document_details tdd\n"
				+ "        JOIN Date_Parameters dp ON DATE(tdd.created_on) BETWEEN dp.from_date AND dp.to_date\n"
				+ "        WHERE active = true\n"
				+ "        GROUP BY emp_id\n"
				+ "    )\n"
				+ "\n"
				+ "SELECT\n"
				+ "    bpe.emp_id,bpe.client_side_id,bpe.start_date,bpe.team_name,bpe.team_id,\n"
				+ "    bpe.name,bpe.spoc, bpe.billable_type,bpe.employee_role, bpe.dept_name, bpe.project_id,\n"
				+ "    bpe.project_name,bpe.Project_manager_name,bpe.po_no,bpe.client_name,\n"
				+ "    bpe.reporting_manager_id,\n"
				+ "    (SELECT MONTHNAME(from_date) FROM Date_Parameters) AS month_name,\n"
				+ "    \n"
				+ "    IFNULL(eds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
				+ "    IFNULL(tcir.filled_count, 0) AS apmosysTimesheetFilledCount,\n"
				+ "    GREATEST(0, IFNULL(eds.expected_fill_count, 0) - (IFNULL(ds.Client_pending_count, 0) + IFNULL(ds.Client_Approved_count, 0))) as client_side_not_filled_count,\n"
				+ "    IFNULL(ds.Client_pending_count, 0) AS clientSidePendingCount,\n"
				+ "    IFNULL(ds.Client_Approved_count, 0) AS clientSideApprovedCount,\n"
				+ "\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 1  THEN dsd.daily_status END), 'NA') AS `1`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_in_time END) AS `1_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 1 THEN dsd.client_out_time END) AS `1_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 2  THEN dsd.daily_status END), 'NA') AS `2`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_in_time END) AS `2_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 2 THEN dsd.client_out_time END) AS `2_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 3  THEN dsd.daily_status END), 'NA') AS `3`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_in_time END) AS `3_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 3 THEN dsd.client_out_time END) AS `3_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 4  THEN dsd.daily_status END), 'NA') AS `4`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_in_time END) AS `4_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 4 THEN dsd.client_out_time END) AS `4_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 5  THEN dsd.daily_status END), 'NA') AS `5`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_in_time END) AS `5_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 5 THEN dsd.client_out_time END) AS `5_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 6  THEN dsd.daily_status END), 'NA') AS `6`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_in_time END) AS `6_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 6 THEN dsd.client_out_time END) AS `6_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 7  THEN dsd.daily_status END), 'NA') AS `7`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_in_time END) AS `7_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 7 THEN dsd.client_out_time END) AS `7_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 8  THEN dsd.daily_status END), 'NA') AS `8`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_in_time END) AS `8_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 8 THEN dsd.client_out_time END) AS `8_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 9  THEN dsd.daily_status END), 'NA') AS `9`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_in_time END) AS `9_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 9 THEN dsd.client_out_time END) AS `9_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.daily_status END), 'NA') AS `10`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_in_time END) AS `10_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 10 THEN dsd.client_out_time END) AS `10_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.daily_status END), 'NA') AS `11`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_in_time END) AS `11_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 11 THEN dsd.client_out_time END) AS `11_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.daily_status END), 'NA') AS `12`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_in_time END) AS `12_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 12 THEN dsd.client_out_time END) AS `12_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.daily_status END), 'NA') AS `13`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_in_time END) AS `13_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 13 THEN dsd.client_out_time END) AS `13_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.daily_status END), 'NA') AS `14`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_in_time END) AS `14_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 14 THEN dsd.client_out_time END) AS `14_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.daily_status END), 'NA') AS `15`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_in_time END) AS `15_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 15 THEN dsd.client_out_time END) AS `15_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.daily_status END), 'NA') AS `16`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_in_time END) AS `16_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 16 THEN dsd.client_out_time END) AS `16_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.daily_status END), 'NA') AS `17`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_in_time END) AS `17_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 17 THEN dsd.client_out_time END) AS `17_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.daily_status END), 'NA') AS `18`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_in_time END) AS `18_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 18 THEN dsd.client_out_time END) AS `18_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.daily_status END), 'NA') AS `19`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_in_time END) AS `19_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 19 THEN dsd.client_out_time END) AS `19_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.daily_status END), 'NA') AS `20`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_in_time END) AS `20_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 20 THEN dsd.client_out_time END) AS `20_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.daily_status END), 'NA') AS `21`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_in_time END) AS `21_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 21 THEN dsd.client_out_time END) AS `21_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.daily_status END), 'NA') AS `22`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_in_time END) AS `22_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 22 THEN dsd.client_out_time END) AS `22_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.daily_status END), 'NA') AS `23`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_in_time END) AS `23_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 23 THEN dsd.client_out_time END) AS `23_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.daily_status END), 'NA') AS `24`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_in_time END) AS `24_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 24 THEN dsd.client_out_time END) AS `24_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.daily_status END), 'NA') AS `25`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_in_time END) AS `25_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 25 THEN dsd.client_out_time END) AS `25_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.daily_status END), 'NA') AS `26`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_in_time END) AS `26_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 26 THEN dsd.client_out_time END) AS `26_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.daily_status END), 'NA') AS `27`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_in_time END) AS `27_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 27 THEN dsd.client_out_time END) AS `27_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.daily_status END), 'NA') AS `28`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_in_time END) AS `28_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 28 THEN dsd.client_out_time END) AS `28_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.daily_status END), 'NA') AS `29`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_in_time END) AS `29_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 29 THEN dsd.client_out_time END) AS `29_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.daily_status END), 'NA') AS `30`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_in_time END) AS `30_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 30 THEN dsd.client_out_time END) AS `30_client_out_time`,\n"
				+ "    IFNULL(MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.daily_status END), 'NA') AS `31`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_in_time END) AS `31_client_in_time`,\n"
				+ "    MAX(CASE WHEN DAY(dsd.timesheet_date) = 31 THEN dsd.client_out_time END) AS `31_client_out_time`,\n"
				+ "    bpe.employement_id\n"
				+ "FROM\n"
				+ "    Base_Project_Employees bpe\n"
				+ "LEFT JOIN\n"
				+ "    Daily_Status_Details dsd ON bpe.emp_id = dsd.emp_id\n"
				+ "LEFT JOIN\n"
				+ "    Expected_Days_Summary eds ON bpe.emp_id = eds.emp_id\n"
				+ "LEFT JOIN\n"
				+ "    Timesheet_Counts_In_Range tcir ON bpe.emp_id = tcir.emp_id\n"
				+ "LEFT JOIN\n"
				+ "    Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
				+ "    where bpe.project_id in (:projectId)\n"
				+ "GROUP BY\n"
				+ "    bpe.emp_id, bpe.client_side_id, bpe.start_date, bpe.team_name, bpe.team_id,\n"
				+ "    bpe.name, bpe.spoc, bpe.billable_type, bpe.employee_role, bpe.dept_name,\n"
				+ "    bpe.project_id, bpe.project_name, bpe.Project_manager_name, bpe.po_no,\n"
				+ "    bpe.client_name, bpe.reporting_manager_id, bpe.employement_id,\n"
				+ "    eds.expected_fill_count, tcir.filled_count, ds.Client_pending_count, ds.Client_Approved_count\n"
				+ "ORDER BY\n"
				+ "    bpe.name", nativeQuery = true)
		public List<Object[]> getEmployeeTimesheetAsCalenderByProjectId(
				  @Param("projectId") Integer projectId,
				  @Param("month") Integer month,
				  @Param("year") Integer year);
		
	@Query(value="WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
			+ "                THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range(dt) AS (\n"
			+ "        SELECT from_date FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
			+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "    Base_Employees AS (\n"
			+ "        SELECT\n"
			+ "            DISTINCT e.emp_id, e.name, p.project_id, p.project_name, p.po_no,\n"
			+ "            CASE WHEN p.po_project_type IS NOT NULL THEN p.po_project_type ELSE p.internal_project_type END AS project_type,\n"
			+ "            c.client_name, t.team_id, t.team_name, tl.name AS team_lead_name, e.billable, e.billable_type, e.mobile_no, e.email,\n"
			+ "            p.apmosysrm, p.apmosys_rm_email, p.clientrm,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id) ELSE CONCAT('A-',e.employeement_id) END AS employement_id,\n"
			+ "            d1.name AS dept_name,\n"
			+ "            GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') as Project_Manager\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.project_id = p.project_id\n"
			+ "        LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        LEFT JOIN job_role j1 ON j1.job_role_id = e.job_role_id\n"
			+ "        LEFT JOIN department d1 ON d1.dept_id = j1.dept_id\n"
			+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
			+ "          AND p.has_client_side_id = true AND e.employmentstatus != 'InActive'\n"
			+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        GROUP BY e.emp_id, e.name, p.project_id, p.project_name, p.po_no, project_type, c.client_name, t.team_id, t.team_name,\n"
			+ "                 team_lead_name, e.billable, e.billable_type, e.mobile_no, e.email, p.apmosysrm, p.apmosys_rm_email, p.clientrm,\n"
			+ "                 employement_id, dept_name\n"
			+ "    ),\n"
			+ "    Employee_Actual_Working_Days AS (\n"
			+ "        SELECT be.emp_id, adir.dt\n"
			+ "        FROM Base_Employees be\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE\n"
			+ "            NOT EXISTS (\n"
			+ "                SELECT 1 FROM employee_timesheets et\n"
			+ "                WHERE et.emp_id = be.emp_id AND et.date = adir.dt\n"
			+ "                  AND (et.day_type LIKE '%Leave%' OR et.day_type LIKE '%Client Holiday%')\n"
			+ "            )\n"
			+ "            AND (\n"
			+ "                (\n"
			+ "                    adir.dt NOT IN (SELECT date_of_holiday FROM holiday WHERE MONTH(date_of_holiday) = :month AND YEAR(date_of_holiday) = :year)\n"
			+ "                    AND DAYOFWEEK(adir.dt) != 1 \n"
			+ "                    AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) > 7 AND DAY(dt) <= 14)) \n"
			+ "                    AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(dt) > 21 AND DAY(dt) <= 28)) \n"
			+ "                )\n"
			+ "                OR\n"
			+ "                EXISTS (\n"
			+ "                    SELECT 1 FROM employee_timesheets et\n"
			+ "                    WHERE et.emp_id = be.emp_id AND et.date = adir.dt AND et.day_type LIKE '%Working%'\n"
			+ "                )\n"
			+ "            )\n"
			+ "    ),\n"
			+ "    Missing_Days AS (\n"
			+ "        SELECT awd.emp_id, awd.dt\n"
			+ "        FROM Employee_Actual_Working_Days awd\n"
			+ "        WHERE awd.dt < CURDATE()\n"
			+ "          AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets et\n"
			+ "            WHERE et.emp_id = awd.emp_id\n"
			+ "              AND et.date = awd.dt\n"
			+ "              AND et.day_type LIKE '%Working%'\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Defaulter_List AS (\n"
			+ "        WITH Missing_Day_Groups AS (\n"
			+ "            SELECT\n"
			+ "                emp_id, dt,\n"
			+ "                DATE_SUB(dt, INTERVAL ROW_NUMBER() OVER (PARTITION BY emp_id ORDER BY dt) DAY) as grp\n"
			+ "            FROM Missing_Days\n"
			+ "        ),\n"
			+ "        Streak_Counts AS (\n"
			+ "            SELECT emp_id, COUNT(*) as streak_length\n"
			+ "            FROM Missing_Day_Groups GROUP BY emp_id, grp\n"
			+ "        )\n"
			+ "        SELECT DISTINCT emp_id FROM Streak_Counts WHERE streak_length > 2\n"
			+ "    ),\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            emp_id,\n"
			+ "            COUNT(DISTINCT CASE WHEN upper(client_approval_status) = 'APPROVED' and final_flag = 1 THEN DATE(created_on) END) AS approved_days,\n"
			+ "            COUNT(DISTINCT CASE WHEN upper(client_approval_status) = 'PENDING' and final_flag = 1 THEN DATE(created_on) END) AS pending_days\n"
			+ "        FROM timesheet_document_details tdd\n"
			+ "        JOIN Date_Parameters dp ON DATE(tdd.created_on) BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE active = true\n"
			+ "        GROUP BY emp_id\n"
			+ "    ),\n"
			+ "    Final_Report_Data AS (\n"
			+ "        SELECT\n"
			+ "            CASE\n"
			+ "                WHEN dl.emp_id IS NOT NULL THEN 'Defaulter'\n"
			+ "                WHEN IFNULL(eed.expected_days_passed, 0) > (IFNULL(eds.approved_days, 0) + IFNULL(eds.pending_days, 0)) OR IFNULL(eds.pending_days, 0) > 0 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM\n"
			+ "            Base_Employees e\n"
			+ "        LEFT JOIN (\n"
			+ "            SELECT emp_id, COUNT(dt) as expected_days_passed\n"
			+ "            FROM Employee_Actual_Working_Days\n"
			+ "            WHERE dt <= CURDATE() \n"
			+ "            GROUP BY emp_id\n"
			+ "        ) eed ON e.emp_id = eed.emp_id\n"
			+ "        LEFT JOIN Employee_Document_Summary eds ON e.emp_id = eds.emp_id\n"
			+ "        LEFT JOIN Defaulter_List dl ON e.emp_id = dl.emp_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT\n"
			+ "    COUNT(*) AS total_applicable_employees,\n"
			+ "    COUNT(CASE WHEN employee_status = 'Approved' THEN 1 END) AS approved_count,\n"
			+ "    COUNT(CASE WHEN employee_status = 'Defaulter' THEN 1 END) AS defaulter_count,\n"
			+ "    COUNT(CASE WHEN employee_status = 'Pending' THEN 1 END) AS client_side_pending_count\n"
			+ "FROM\n"
			+ "    Final_Report_Data", nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForEmployee(@Param("month") Integer month, @Param("year") Integer year);
	
	@Query(value="WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "               CASE\n"
			+ "                   WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
			+ "                   THEN CURDATE()\n"
			+ "                   ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "               END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range(dt) AS (\n"
			+ "        SELECT from_date FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
			+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "    Base_Working_Days AS (\n"
			+ "        SELECT dt\n"
			+ "        FROM All_Dates_In_Range\n"
			+ "        WHERE dt NOT IN (\n"
			+ "            SELECT date_of_holiday FROM holiday\n"
			+ "            WHERE MONTH(date_of_holiday) = 08 AND YEAR(date_of_holiday) = 2025\n"
			+ "        )\n"
			+ "        AND DAYOFWEEK(dt) != 1\n"
			+ "        AND NOT (DAYOFWEEK(dt) = 7 AND (DAY(dt) > 7 AND DAY(dt) <= 14))\n"
			+ "        AND NOT (DAYOFWEEK(dt) = 7 AND (DAY(dt) > 21 AND DAY(dt) <= 28))\n"
			+ "    ),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id, p.project_id\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
			+ "          AND p.has_client_side_id = true AND e.employmentstatus != 'InActive'\n"
			+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "    Employee_Actual_Working_Days AS (\n"
			+ "        SELECT be.emp_id, bwd.dt\n"
			+ "        FROM Base_Project_Employees be\n"
			+ "        CROSS JOIN Base_Working_Days bwd\n"
			+ "        WHERE NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets et\n"
			+ "            WHERE et.emp_id = be.emp_id AND et.date = bwd.dt AND (et.day_type LIKE '%Leave%' OR et.day_type LIKE '%Client Holiday%')\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Missing_Days AS (\n"
			+ "        SELECT awd.emp_id, awd.dt\n"
			+ "        FROM Employee_Actual_Working_Days awd\n"
			+ "        WHERE awd.dt < CURDATE()\n"
			+ "          AND NOT EXISTS (\n"
			+ "            SELECT 1 FROM employee_timesheets et\n"
			+ "            WHERE et.emp_id = awd.emp_id AND et.date = awd.dt AND et.day_type LIKE '%Working%'\n"
			+ "        )\n"
			+ "    ),\n"
			+ "    Defaulter_Employees AS (\n"
			+ "        SELECT DISTINCT emp_id\n"
			+ "        FROM (\n"
			+ "            SELECT emp_id, COUNT(*) as streak_length\n"
			+ "            FROM (\n"
			+ "                SELECT emp_id, DATE_SUB(dt, INTERVAL ROW_NUMBER() OVER (PARTITION BY emp_id ORDER BY dt) DAY) as grp\n"
			+ "                FROM Missing_Days\n"
			+ "            ) AS streaks\n"
			+ "            GROUP BY emp_id, grp\n"
			+ "        ) AS streak_counts\n"
			+ "        WHERE streak_length > 2\n"
			+ "    ),\n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT emp_id,\n"
			+ "               COUNT(CASE WHEN client_approval_status = 'pending' and final_flag = 1 THEN 1 END) AS Client_pending_count,\n"
			+ "               COUNT(CASE WHEN client_approval_status = 'approved' and final_flag = 1 THEN 1 END) AS Client_Approved_count\n"
			+ "        FROM timesheet_document_details\n"
			+ "        WHERE DATE(created_on) BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters) AND active = true\n"
			+ "        GROUP BY emp_id\n"
			+ "    ),\n"
			+ "    Employee_Final_Summary AS (\n"
			+ "        SELECT\n"
			+ "            bpe.emp_id, bpe.project_id,\n"
			+ "            CASE\n"
			+ "                WHEN de.emp_id IS NOT NULL THEN 'Defaulter'\n"
			+ "                WHEN IFNULL(ds.Client_pending_count, 0) > 0 OR (IFNULL(ds.Client_Approved_count, 0) + IFNULL(ds.Client_pending_count, 0)) < IFNULL(eed.expected_days_passed, 0) THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Project_Employees bpe\n"
			+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "        LEFT JOIN Defaulter_Employees de ON bpe.emp_id = de.emp_id\n"
			+ "        LEFT JOIN (\n"
			+ "            SELECT emp_id, COUNT(dt) as expected_days_passed\n"
			+ "            FROM Employee_Actual_Working_Days\n"
			+ "            WHERE dt < CURDATE()\n"
			+ "            GROUP BY emp_id\n"
			+ "        ) eed ON bpe.emp_id = eed.emp_id\n"
			+ "    ),\n"
			+ "    Project_Attributes AS (\n"
			+ "        SELECT\n"
			+ "            project_id,\n"
			+ "            MAX(CASE WHEN employee_status = 'Defaulter' THEN 1 ELSE 0 END) AS has_defaulter,\n"
			+ "            MAX(CASE WHEN employee_status = 'Pending' THEN 1 ELSE 0 END) AS has_pending\n"
			+ "        FROM Employee_Final_Summary\n"
			+ "        GROUP BY project_id\n"
			+ "    )\n"
			+ "SELECT\n"
			+ "    (SELECT COUNT(DISTINCT project_id) FROM Base_Project_Employees) AS total_no_of_applicable_projects,\n"
			+ "    SUM(CASE WHEN pa.has_defaulter = 0 AND pa.has_pending = 0 THEN 1 ELSE 0 END) AS total_approved_projects,\n"
			+ "    SUM(pa.has_pending) AS total_pending_projects,\n"
			+ "    SUM(pa.has_defaulter) AS total_defaulter_projects\n"
			+ "FROM\n"
			+ "    Project_Attributes pa", nativeQuery = true)
	public List<Object[]> getTimesheetDashboardCountForProject(@Param("month") Integer month, @Param("year") Integer year);
	
	
	
	
	
	
	
	
	
	
	@Query(
			  value = "WITH RankedTimeSheets AS ( " +
			          "SELECT et.emp_id, et.office_in_time, et.office_out_time, p.project_id, c.client_id, cl.client_location_id, " +
			          "t.team_name, a.activity, a.activity_id, etam.description, ecsm.client_side_id, t.team_id, et.client_approval_status, " +
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
			          "LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = et.emp_id AND ecsm.project_id = p.project_id " +
			          "WHERE UPPER(et.day_type) LIKE '%WORKING%' " +
			          ") " +
			          "SELECT DISTINCT employement_id, office_in_time, office_out_time, project_id, client_id, client_location_id, " +
			          "team_name, activity, activity_id, description, team_id, client_approval_status, total_time, timesheet_lock_updated_on, is_timesheet_lock_check_enable " +
			          "FROM RankedTimeSheets " +
			          "WHERE rnk = 1 AND emp_id = :emp_id",
			  nativeQuery = true
			)
			List<Object[]> getLastTimesheetFiledByEmpId(@Param("emp_id") Long emp_id);
	
	
	
	
}
