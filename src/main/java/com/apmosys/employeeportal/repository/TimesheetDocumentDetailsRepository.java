package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.dto.TimesheetIdAndEmpIdDTO;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;

public interface TimesheetDocumentDetailsRepository extends JpaRepository<TimesheetDocumentDetails, Long>{

	TimesheetDocumentDetails findByDocIdAndActive(Long docId, Boolean active);
	TimesheetDocumentDetails findByDocIdAndFinalFlag(Long docId, Boolean finalFlag);

	TimesheetDocumentDetails findTopByTimesheetIdAndActive(Long timesheetId,Boolean active);
	
	@Query("SELECT t FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId and t.active = true")
	TimesheetDocumentDetails findByTimesheetId(Long timesheetId);
	
	@Query("SELECT t.docId FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId and t.active = true")
	Long findDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);
	
	@Query("SELECT tdd FROM TimesheetDocumentDetails tdd \n" +
		       "INNER JOIN Timesheet et on et.timesheetId = tdd.timesheetId \n" +
		       "WHERE et.empId = :empId \n" +
		       " AND et.dayType in ('Working','Non-working')\n" +
		       "AND et.date BETWEEN :fromDate AND :toDate and tdd.active = true")
		List<TimesheetDocumentDetails> getDocsByEmpAndDateRange(
		    @Param("empId") Long empId,
		    @Param("fromDate") LocalDate fromDate,
		    @Param("toDate") LocalDate toDate
		);

	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO(" +
            "t.docId, t.docName, t.timesheetId, t.empId, t.active, " +
            "t.clientApprovalStatus, t.rmApprovalStatus, t.hrApprovalStatus, t.finalFlag) " +
            "FROM TimesheetDocumentDetails t " +
            "WHERE t.timesheetId = :timesheetId AND t.active = true")
	List<TimesheetDocumentDetailsDTO> findAllDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);
	
	
	  @Query("SELECT d FROM TimesheetDocumentDetails d " +
			"JOIN Timesheet t ON d.timesheetId = t.timesheetId " +
			"WHERE t.empId = :empId AND t.date = :date")
	List<TimesheetDocumentDetails> findDocumentsByEmpIdAndDate(
			@Param("empId") Long empId,
			@Param("date") LocalDate date);
	  
	  
		@Query(value = " WITH RECURSIVE \n"
				+ "		  		Date_Parameters AS (\n"
				+ "		  		    SELECT\n"
				+ "		  		        STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
				+ "		  		        CASE\n"
				+ "		  		            WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
				+ "		  		            THEN CURDATE()\n"
				+ "		  		            ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
				+ "		  		        END AS to_date\n"
				+ "		  		),\n"
				+ "		  		All_Dates_In_Range AS (\n"
				+ "		  		    SELECT from_date AS dt FROM Date_Parameters\n"
				+ "		  		    UNION ALL\n"
				+ "		  		    SELECT DATE_ADD(dt, INTERVAL 1 DAY)\n"
				+ "		  		    FROM All_Dates_In_Range, Date_Parameters WHERE dt < Date_Parameters.to_date\n"
				+ "		  		),\n"
				+ "		  		Project_Managers AS (\n"
				+ "		  		    SELECT pm.project_id,\n"
				+ "		  		    GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS project_manager_name\n"
				+ "		  		    FROM project_manager_mapping pm LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
				+ "		  		    GROUP BY pm.project_id\n"
				+ "		  		),\n"
				+ "		  		 Authorized_Employees AS (\n"
				+ "		  			SELECT DISTINCT e.emp_id FROM employee e WHERE (\n"
				+ "		  						EXISTS (SELECT 1 FROM employee u\n"
				+ "		  						JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
				+ "		  						JOIN department d ON jr.dept_id = d.dept_id\n"
				+ "		  						WHERE u.emp_id = :emp_id AND (jr.employee_role IN ('SuperAdmin') OR d.name IN ('HR', 'Accounts', 'Resource Management Group')))\n"
				+ "		  						OR e.job_role_id IN (SELECT jr.job_role_id FROM job_role jr\n"
				+ "		  						WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id))\n"
				+ "		  		)),\n"
				+ "		  		Base_Project_Employees AS (\n"
				+ "		  			SELECT DISTINCT\n"
				+ "		  				etm.team_id,t.team_name,etm.emp_id,e.name,etm.employee_role,case when p.po_project_type = 'TNM' AND (etm.is_shadow = 0 OR etm.is_shadow IS NULL) then 'TNM' when p.po_project_type = 'Fixed Cost' AND (etm.is_shadow = 0 OR etm.is_shadow IS NULL) then 'Fixed Cost' when p.po_project_type = 'TNM' and etm.is_shadow = 1 then 'TNM(Shadow)' when p.po_project_type = 'Fixed Cost' and etm.is_shadow = 1 then 'Fixed Cost(Shadow)' when p.po_project_type = 'Monitoring' then 'Fixed Cost' when p.po_project_type is null then internal_project_type end as billable_type,\n"
				+ "		  				date(etm.start_date) as start_date,date(etm.end_date) as end_date,etm.employee_team_map_id,\n"
				+ "		  		        etm.active,p.project_id,p.project_name,c.client_id,c.client_name,ecsm.client_side_id,p.po_no,\n"
				+ "		  				s.name AS spoc,tl.name AS teamLead, \n"
				+ "		  		        e.reporting_manager_id,e.employmentstatus,d.name AS dept_name,\n"
				+ "		  				CASE\n"
				+ "		  					 WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
				+ "		  		             ELSE CONCAT('A-',e.employeement_id)\n"
				+ "		  				END AS employement_id\n"
				+ "		  			FROM projects p\n"
				+ "		  			INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "		  			INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "		  			INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "		  			INNER JOIN clients c ON c.client_id = p.client_id\n"
				+ "		  			left JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
				+ "		  			LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
				+ "		  			LEFT JOIN employee s ON s.emp_id = t.spoc_id\n"
				+ "		  			LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
				+ "		  			LEFT JOIN department d ON d.dept_id = jr.dept_id\n"
				+ "		  			JOIN employee user_e ON user_e.emp_id = :emp_id\n"
				+ "		  			JOIN job_role user_jr ON user_e.job_role_id = user_jr.job_role_id		\n"
				+ "		  			LEFT JOIN project_manager_mapping pmm_check ON p.project_id = pmm_check.project_id AND pmm_check.project_manager_id = :emp_id\n"
				+ "		  			LEFT JOIN project_overhead_mapping pom_check ON p.project_id = pom_check.project_id AND pom_check.project_overhead_id = :emp_id\n"
				+ "		  			LEFT JOIN employee_client_side_id_mapping ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id\n"
				+ "		  			WHERE p.has_client_side_id = 1 \n"
				+ "		  			AND ( ae.emp_id IS NOT NULL \n"
				+ "		  			OR (\n"
				+ "		  				(pmm_check.project_manager_id IS NOT NULL OR pom_check.project_overhead_id IS NOT NULL)\n"
				+ "		  				 AND jr.dept_id = user_jr.dept_id))\n"
				+ "		  			AND (\n"
				+ "		  					e.date_of_relieving IS NULL OR YEAR(e.date_of_relieving) > :year \n"
				+ "		  					OR (YEAR(e.date_of_relieving) = :year AND MONTH(e.date_of_relieving) >= :month)) \n"
				+ "		  			AND e.emp_id not between 1 and 6 AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
				+ "		  			AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
				+ "		  		),\n"
				+ "		  		Timesheet_Base_Data AS (\n"
				+ "		  		    SELECT DISTINCT et.timesheet_id,et.emp_id,t.project_id,etm.employee_team_map_id,et.date,et.day_type\n"
				+ "		  		    FROM employee_timesheets et\n"
				+ "		  		    JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id\n"
				+ "		  		    JOIN activities a ON etam.activity_id = a.activity_id\n"
				+ "		  		    JOIN teams t ON a.team_id = t.team_id\n"
				+ "		  		    JOIN employee_team_mapping etm ON etm.emp_id = et.emp_id AND etm.team_id = t.team_id\n"
				+ "		  				 AND et.date BETWEEN DATE(etm.start_date) AND COALESCE(DATE(etm.end_date), '2099-12-31')\n"
				+ "		  		    WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
				+ "		  		),\n"
				+ "		  		Last_Working_Calendar_Day AS (\n"
				+ "		  		    SELECT MAX(adir.dt) AS last_working_day FROM All_Dates_In_Range adir \n"
				+ "		  		    WHERE NOT EXISTS (\n"
				+ "		  							  SELECT 1 FROM employee_timesheets et WHERE et.date = adir.dt\n"
				+ "		  							  AND UPPER(et.day_type) IN ('LEAVE','CLIENT HOLIDAY','PUBLIC HOLIDAY','WEEK OFF'))\n"
				+ "		  		)	,\n"
				+ "		  		Last_Working_Calendar_Per_Stint AS (\n"
				+ "		  		    SELECT bpe.employee_team_map_id,bpe.emp_id,bpe.project_id,MAX(adir.dt) AS last_working_calendar_day\n"
				+ "		  		    FROM Base_Project_Employees bpe\n"
				+ "		  		    JOIN All_Dates_In_Range adir\n"
				+ "		  				 ON adir.dt BETWEEN\n"
				+ "		  		         GREATEST(bpe.start_date, (SELECT from_date FROM Date_Parameters))\n"
				+ "		  		         AND\n"
				+ "		  		         LEAST(COALESCE(bpe.end_date, (SELECT to_date FROM Date_Parameters)),\n"
				+ "		  		         (SELECT to_date FROM Date_Parameters))\n"
				+ "		  		    WHERE NOT EXISTS (\n"
				+ "		  							 SELECT 1 FROM employee_timesheets et WHERE et.emp_id = bpe.emp_id AND et.date = adir.dt\n"
				+ "		  							 AND UPPER(et.day_type) IN ('LEAVE','CLIENT HOLIDAY','PUBLIC HOLIDAY','WEEK OFF'))\n"
				+ "		  		    GROUP BY bpe.employee_team_map_id, bpe.emp_id, bpe.project_id\n"
				+ "		  		),\n"
				+ "	Last_Timesheet_Per_Stint AS (\n"
				+ "  SELECT\n"
				+ "    tbd.employee_team_map_id,\n"
				+ "    MAX(tbd.date) AS last_timesheet_date,\n"
				+ "    SUBSTRING_INDEX(\n"
				+ "      GROUP_CONCAT(tbd.timesheet_id ORDER BY tbd.date DESC),\n"
				+ "      ',', 1\n"
				+ "    ) AS last_timesheet_id\n"
				+ "  FROM Timesheet_Base_Data tbd\n"
				+ "  WHERE UPPER(tbd.day_type) NOT IN (\n"
				+ "    'LEAVE','CLIENT HOLIDAY','PUBLIC HOLIDAY','WEEK OFF'\n"
				+ "  )\n"
				+ "  GROUP BY tbd.employee_team_map_id\n"
				+ "),\n"
				+ "	Last_Working_Day_Doc AS (\n"
				+ "  SELECT\n"
				+ "    lts.employee_team_map_id,\n"
				+ "    tdd.doc_id,\n"
				+ "    tdd.doc_name,\n"
				+ "    tdd.doc_mime_type,\n"
				+ "    tdd.final_flag,\n"
				+ "    tdd.doc_data\n"
				+ "  FROM Last_Timesheet_Per_Stint lts\n"
				+ "  JOIN timesheet_document_details tdd\n"
				+ "    ON tdd.timesheet_id = lts.last_timesheet_id\n"
				+ "   AND COALESCE(tdd.final_flag, 0) = 1\n"
				+ ")\n"
				+ "		  		SELECT DISTINCT bpe.name,bpe.project_id,bpe.project_name,bpe.team_id,bpe.employement_id,lwdd.doc_id,\n"
				+ "		  		lwdd.doc_mime_type,lwdd.doc_name,lwdd.final_flag ,lwdd.doc_data \n"
				+ "		  		FROM Base_Project_Employees bpe\n"
				+ "		  		LEFT JOIN Project_Managers pm ON pm.project_id = bpe.project_id\n"
				+ "		  		LEFT JOIN Last_Working_Day_Doc lwdd ON lwdd.employee_team_map_id = bpe.employee_team_map_id\n"
				+ "		  		WHERE bpe.project_id IN (:project_id)\n"
				+ "                AND bpe.emp_id in (:selectedEmpId)\n"
				+ "		  		ORDER BY bpe.project_name, bpe.name ", nativeQuery = true)
		        List<Object[]> getDocumentsBySelectedEmpId(
				    @Param("project_id") Integer projectId,
				    @Param("month") Integer month,
				    @Param("year") Integer year,
				    @Param("emp_id") Long empId,
				    @Param("selectedEmpId") Long selectedEmpId
				);
				

		@Query( "SELECT new com.apmosys.employeeportal.dto.TimesheetIdAndEmpIdDTO(t.timesheetId, t.empId) from Timesheet t \n"+
				"where t.hasClientSideId = 1 \n"+
				"and t.projectId = :projectId \n"+
				"and t.dayType IN ('Working', 'Non-working') \n"+
				"and (t.status = 'Rejected' \n"+
				"or not exists (\n" + 
					"select 1 from TimesheetDocumentDetails tdd1 where \n" + 
					"tdd1.timesheetId = t.timesheetId and tdd1.finalFlag = 1 and tdd1.clientApprovalStatus = 'Approved'\n" +
					") \n" +
				") \n" +
				"and t.date between :fromDate and :toDate \n"+
				"and t.empId IN :empIds")
		List<TimesheetIdAndEmpIdDTO> getDocsByEmpIdsAndDate(
				@Param("empIds") List<Long> empIds,
				@Param("fromDate") LocalDate fromDate,
				@Param("toDate") LocalDate toDate,
				@Param("projectId") Integer projectId
			);

		@Query("SELECT tdd from TimesheetDocumentDetails tdd where tdd.timesheetId IN :timesheetIds and tdd.finalFlag = 1 and tdd.clientApprovalStatus = 'Approved'")
		List<TimesheetDocumentDetails> getDocsByTimesheetIdsAndFinalFlag(
				@Param("timesheetIds") List<Long> timesheetIds
			);
}
