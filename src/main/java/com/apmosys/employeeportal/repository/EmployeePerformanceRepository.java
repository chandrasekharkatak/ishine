package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeePerformance;

@Repository
public interface EmployeePerformanceRepository extends JpaRepository<EmployeePerformance, Long> {

	@Query(nativeQuery = true, value="SELECT ep.employee_performance_id, ep.quarter_id, \n"
			+ "       qc.financial_year, qc.quarter_cycle, ep.emp_id, \n"
			+ "       rt.review_label, erp.rating_value, ep.final_rating, \n"
			+ "       ep.hod_approval_date, ep.hod_id, ep.hod_remarks, \n"
			+ "       rt.review_type_id \n"
			+ "FROM employee_performance ep \n"
			+ "INNER JOIN quater_cycle qc ON qc.quarter_id = ep.quarter_id \n"
			+ "INNER JOIN review_type rt ON rt.quarter_id = qc.quarter_id \n"
			+ "INNER JOIN employee_rating_performance erp ON erp.review_type_id = rt.review_type_id")
	List<Object[]> getEmployeePerformanceHOD();
    
	@Query(nativeQuery = true , value="SELECT  \n"
			+ "			    ep.emp_id,\n"
			+ "			    ep.completion_status,\n"
			+ "			    ep.employee_performance_id,\n"
			+ "			    ep.quarter_id,\n"
			+ "			    rt.review_label,\n"
			+ "			    rt.review_field_type,\n"
			+ "			    rt.condition,\n"
			+ "			    erp.rating_value,\n"
			+ "			    erp.review_type_id,\n"
			+ "			    ep.hod_remarks,\n"
			+ "			    ep.final_rating,\n"
			+ "			    rt.dept_id,\n"
			+ "             erp.performance_rating_id,\n"
			+ "              ep.hr_remarks,\n"
			+ "             ep.hr_review_status,\n"
			+ "             ep.reject_status,\n"
			+ "             ep.manager_remarks,\n"
			+ "             erp.criteria_remark\n"
			+ "			FROM employee_performance ep\n"
			+ "			INNER JOIN employee_rating_performance erp \n"
			+ "			    ON ep.quarter_id = erp.quarter_id \n"
			+ "			    AND ep.emp_id = erp.emp_id\n"
			+ "			INNER JOIN review_type rt \n"
			+ "			    ON ep.quarter_id = rt.quarter_id and\n"
			+ "				erp.review_type_id=rt.review_type_id\n"
			+ "			 where ep.emp_id = :empId and ep.quarter_id = :quarterId")
	List<Object[]> hrAndHODEmployeePerformanceView(@Param("empId") Long empId,@Param("quarterId") Long quarterId);

	@Query(nativeQuery = true , value="SELECT * from employee_performance ep where ep.employee_performance_id= :employeePerformanceId ")
	EmployeePerformance findByPerformanceId(@Param("employeePerformanceId")Long employeePerformanceId);

	/** Find latest employee_performance row for given emp and quarter (for manager/HOD submit). */
	@Query(nativeQuery = true, value = "SELECT * FROM employee_performance ep WHERE ep.emp_id = :empId AND ep.quarter_id = :quarterId ORDER BY ep.employee_performance_id DESC LIMIT 1")
	List<EmployeePerformance> findLatestByEmpIdAndQuarterId(@Param("empId") Long empId, @Param("quarterId") Long quarterId);

	/** Approval details for popup: manager/HOD/HR name, employment id, role-wise ratings, remarks. Latest performance row per emp and quarter. */
	@Query(nativeQuery = true, value = "SELECT ep.manager_id, ep.manager_remarks, ep.final_rating, ep.manager_rating, ep.hod_rating, ep.hr_rating, "
			+ "e_m.name AS manager_name, e_m.employeement_id AS manager_employment_id, "
			+ "ep.hod_id, ep.hod_remarks, e_h.name AS hod_name, e_h.employeement_id AS hod_employment_id, "
			+ "ep.hr_id, ep.hr_remarks, e_hr.name AS hr_name, e_hr.employeement_id AS hr_employment_id "
			+ "FROM employee_performance ep "
			+ "LEFT JOIN employee e_m ON ep.manager_id = e_m.emp_id "
			+ "LEFT JOIN employee e_h ON ep.hod_id = e_h.emp_id "
			+ "LEFT JOIN employee e_hr ON ep.hr_id = e_hr.emp_id "
			+ "WHERE ep.emp_id = :empId AND ep.quarter_id = :quarterId "
			+ "ORDER BY ep.employee_performance_id DESC LIMIT 1")
	List<Object[]> findApprovalDetailsByEmpIdAndQuarterId(@Param("empId") Long empId, @Param("quarterId") Long quarterId);

	/** Approval audit history rows for popup (all records in quarter, latest first). */
	@Query(nativeQuery = true, value = "SELECT ep.employee_performance_id, ep.final_rating, ep.manager_rating, ep.hod_rating, ep.hr_rating, "
			+ "ep.manager_id, ep.manager_remarks, ep.manager_review_date, e_m.name AS manager_name, e_m.employeement_id AS manager_employment_id, "
			+ "ep.hod_id, ep.hod_remarks, ep.hod_approval_date, ep.hod_rejected_date, e_h.name AS hod_name, e_h.employeement_id AS hod_employment_id, "
			+ "ep.hr_id, ep.hr_remarks, ep.hr_review_date, ep.hr_review_status, e_hr.name AS hr_name, e_hr.employeement_id AS hr_employment_id "
			+ "FROM employee_performance ep "
			+ "LEFT JOIN employee e_m ON ep.manager_id = e_m.emp_id "
			+ "LEFT JOIN employee e_h ON ep.hod_id = e_h.emp_id "
			+ "LEFT JOIN employee e_hr ON ep.hr_id = e_hr.emp_id "
			+ "WHERE ep.emp_id = :empId AND ep.quarter_id = :quarterId "
			+ "ORDER BY ep.employee_performance_id DESC")
	List<Object[]> findApprovalAuditHistoryByEmpIdAndQuarterId(@Param("empId") Long empId, @Param("quarterId") Long quarterId);

	/** Approval audit history rows across all years (for year-wise summary in popup). */
	@Query(nativeQuery = true, value = "SELECT ep.employee_performance_id, ep.final_rating, ep.manager_rating, ep.hod_rating, ep.hr_rating, "
			+ "ep.quarter_id, qc.financial_year, qc.quarter_cycle, "
			+ "ep.manager_id, ep.manager_remarks, ep.manager_review_date, e_m.name AS manager_name, e_m.employeement_id AS manager_employment_id, "
			+ "ep.hod_id, ep.hod_remarks, ep.hod_approval_date, ep.hod_rejected_date, e_h.name AS hod_name, e_h.employeement_id AS hod_employment_id, "
			+ "ep.hr_id, ep.hr_remarks, ep.hr_review_date, ep.hr_review_status, e_hr.name AS hr_name, e_hr.employeement_id AS hr_employment_id "
			+ "FROM employee_performance ep "
			+ "LEFT JOIN quater_cycle qc ON ep.quarter_id = qc.quarter_id "
			+ "LEFT JOIN employee e_m ON ep.manager_id = e_m.emp_id "
			+ "LEFT JOIN employee e_h ON ep.hod_id = e_h.emp_id "
			+ "LEFT JOIN employee e_hr ON ep.hr_id = e_hr.emp_id "
			+ "WHERE ep.emp_id = :empId "
			+ "ORDER BY ep.employee_performance_id DESC")
	List<Object[]> findApprovalAuditHistoryByEmpId(@Param("empId") Long empId);

	@Query(nativeQuery = true , value="SELECT  \n"
			+ "    d.name AS department_name,\n"
			+ "    COUNT(DISTINCT e.emp_id) AS eligible_employees, \n"
			+ "    COUNT(em.emp_id) AS filled_employees, \n"
			+ "    (COUNT(DISTINCT e.emp_id) - COUNT(em.emp_id)) AS unfilled_employees,\n"
			+ "    e7.name AS hod_name\n"
			+ "FROM employee e\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id \n"
			+ "LEFT JOIN employee_performance em ON e.emp_id = em.emp_id\n"
			+ "WHERE e.employmentstatus = 'Confirmed'\n"
			+ "AND e.date_of_joining <= DATE_FORMAT(NOW(), '%Y-12-31') - INTERVAL 1 YEAR\n"
			+ "GROUP BY d.name, e7.name")
	List<Object[]> DepartmentbyEmployeecontquery();

	@Query(nativeQuery = true , value="SELECT e.emp_id,e.name,e.employeement_id FROM employee e")
	List<Object[]> getAllEmployeeForTeamMember();

	@Query(
		    nativeQuery = true,
		    value = "SELECT e.emp_id, e.name, e.employeement_id " +
		            "FROM employee e " +
		            "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id " +
		            "INNER JOIN department d ON d.dept_id = jr.dept_id " +
		            "WHERE d.dept_id IN (:deptIds) and e.employmentstatus != \"InActive\";"
		)
	List<Object[]> getAllEmployeeForTeamMemberByDepartment(@Param("deptIds") List<Long> deptIds);

	@Query(
		    nativeQuery = true,
		    value = "SELECT e.emp_id,e.name,e.employeement_id from employee_team_mapping etm \n"
		    		+ "INNER JOIN employee e ON e.emp_id=etm.emp_id \n"
		    		+ "WHERE etm.team_id IN (:teamIds) and etm.active=1 "
		)	
	List<Object[]> getAllTeamMembers(@Param("teamIds") List<Long> teamIds);

	@Query(
		    nativeQuery = true,
		    value = "SELECT e.emp_id,e.name,e.employeement_id FROM employee e \n"
		    		+ "WHERE e.reporting_manager_id=:empId OR e.manager_id=:empId and e.employmentstatus != \"InActive\";"
		)	
	List<Object[]> getAllEmployeeReportByEmpId(Long empId);

	@Query(
		    nativeQuery = true,
		    value = "SELECT e.emp_id,e.name,e.employeement_id FROM employee e \n"
		    		+ "INNER JOIN project_insight_response pir ON pir.emp_id=e.emp_id \n"
		    		+ "WHERE pir.process_to=:empId "
		    		+ "GROUP BY e.emp_id,e.name,e.employeement_id"
		)
	List<Object[]> getEmployeeUnderReviewByEmpId(Long empId);
	
	@Query(nativeQuery = true,value="SELECT DISTINCT\n"
			+ "    e.employeement_id,\n"
			+ "    e.date_of_joining,\n"
			+ "    e.email,\n"
			+ "    e.employmentstatus,\n"
			+ "    e.name,\n"
			+ "    d.name AS departmentname,\n"
			+ "    e2.name AS manager,\n"
			+ "    e.billable,\n"
			+ "    e.total_experience,\n"
			+ "    e.billable_type,\n"
			+ "    e5.name AS reportingManger,\n"
			+ "    e7.name AS hodName,\n"
			+ "    ep.completion_status,\n"
			+ "    qc.quarter_cycle,\n"
			+ "    qc.financial_year,\n"
			+ "    ep.hod_remarks,\n"
			+ "    ep.final_rating,\n"
			+ "    ep.hr_remarks,\n"
			+ "    ep.hr_review_status\n"
			+ "FROM \n"
			+ "    employee e\n"
			+ "INNER JOIN \n"
			+ "    job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "INNER JOIN \n"
			+ "    department d ON d.dept_id = jr.dept_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e3 ON e.updated_by = e3.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e4 ON e.created_by = e4.emp_id\n"
			+ "INNER JOIN \n"
			+ "    employee e2 ON e.manager_id = e2.emp_id\n"
			+ "INNER JOIN \n"
			+ "    employee e7 ON d.hod_id = e7.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e5 ON e.reporting_manager_id = e5.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    designation des ON des.designation_id = e.designation_id\n"
			+ "LEFT JOIN \n"
			+ "    employee_performance ep ON e.emp_id = ep.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee_rating_performance erp ON ep.quarter_id = erp.quarter_id AND ep.emp_id = erp.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    quater_cycle qc ON ep.quarter_id = qc.quarter_id\n"
			+ "WHERE \n"
			+ "    e.employmentstatus != 'InActive' \n"
			+ "    AND (\n"
			+ "        d.hod_id = :empId\n"
			+ "        OR (\n"
			+ "            CASE \n"
			+ "                WHEN e.approvals_to = 'Manager' THEN e.manager_id = :empId\n"
			+ "                WHEN e.approvals_to = 'Reporting Manager' THEN e.reporting_manager_id = :empId\n"
			+ "                ELSE (d.hod_id = :empId)\n"
			+ "            END\n"
			+ "        )\n"
			+ "    )\n"
			+ "ORDER BY \n"
			+ "    e.name")
	List<Object[]> ExcelExportQueryForPerformnaceHODManager(Long empId);

	
	@Query(nativeQuery = true , value="SELECT  \n"
			+ "    d.name AS department_name,\n"
			+ "    COUNT(DISTINCT e.emp_id) AS eligible_employees, \n"
			+ "    COUNT(em.emp_id) AS filled_employees, \n"
			+ "    (COUNT(DISTINCT e.emp_id) - COUNT(em.emp_id)) AS unfilled_employees,\n"
			+ "    e7.name AS hod_name\n"
			+ "FROM employee e\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id \n"
			+ "LEFT JOIN employee_performance em ON e.emp_id = em.emp_id\n"
			+ "WHERE e.employmentstatus = 'Confirmed'\n"
			+"AND d.dept_id = :deptId\n"
			+ "AND e.date_of_joining <= DATE_FORMAT(NOW(), '%Y-12-31') - INTERVAL 1 YEAR\n"
			+ "GROUP BY d.name, e7.name")
	List<Object[]> DepartmentbyEmployeecontqueryForEachDepartment(Long deptId);
	@Query(nativeQuery = true,value = "select DISTINCT e.emp_id,e.email,e.name from employee e inner join department d on d.hod_id = e.emp_id and e.employmentstatus != 'InActive'")
	List<Object[]> findAllActiveHODs();
	
	@Query(nativeQuery = true,value = "SELECT ep.emp_id, ep.completion_status, qc.financial_year, qc.quarter_cycle, \n"
			+ "       e.name AS employee_name, d.name AS department_name,e.employeement_id \n"
			+ "FROM employee_performance ep\n"
			+ "inner JOIN quater_cycle qc ON ep.quarter_id = qc.quarter_id\n"
			+ "left JOIN employee e ON ep.emp_id = e.emp_id\n"
			+ "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "where d.hod_id = :hodId\n"
			+ "AND ep.completion_status = 'Ongoing'\n"
			+ "AND qc.is_active = 1 \n"
			+ "AND qc.is_enable = 1")
	List<Object[]> findAllOngoingReviewedEmployeesUnderHOD(@Param("hodId") Long hodId);
	
	@Query(nativeQuery = true , value="    SELECT DISTINCT\n"
			+ "    e.employeement_id,\n"
			+ "    e.date_of_joining,\n"
			+ "    e.email,\n"
			+ "    e.employmentstatus,\n"
			+ "    e.name,\n"
			+ "    d.name AS departmentname,\n"
			+ "    e2.name AS manager,\n"
			+ "    e.billable,\n"
			+ "    e.total_experience,\n"
			+ "    e.billable_type,\n"
			+ "    e5.name AS reportingManger,\n"
			+ "    e7.name AS hodName,\n"
			+ "    ep.completion_status,\n"
			+ "    qc.quarter_cycle,\n"
			+ "    qc.financial_year,\n"
			+ "    ep.hod_remarks,\n"
			+ "    ep.final_rating,\n"
			+ "    ep.hr_remarks,\n"
			+ "    ep.hr_review_status\n"
			+ "FROM \n"
			+ "    employee e\n"
			+ "INNER JOIN \n"
			+ "    job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "INNER JOIN \n"
			+ "    department d ON d.dept_id = jr.dept_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e3 ON e.updated_by = e3.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e4 ON e.created_by = e4.emp_id\n"
			+ "INNER JOIN \n"
			+ "    employee e2 ON e.manager_id = e2.emp_id\n"
			+ "INNER JOIN \n"
			+ "    employee e7 ON d.hod_id = e7.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee e5 ON e.reporting_manager_id = e5.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    designation des ON des.designation_id = e.designation_id\n"
			+ "LEFT JOIN \n"
			+ "    employee_performance ep ON e.emp_id = ep.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    employee_rating_performance erp ON ep.quarter_id = erp.quarter_id AND ep.emp_id = erp.emp_id\n"
			+ "LEFT JOIN \n"
			+ "    quater_cycle qc ON ep.quarter_id = qc.quarter_id\n"
			+ "WHERE \n"
			+ "    e.employmentstatus != 'InActive'\n"
			+ "ORDER BY \n"
			+ "    e.name")
	List<Object[]> ExcelExportQueryForPerformnaceHr();
	
	@Query(nativeQuery = true,value = "SELECT DISTINCT e.emp_id, e.name, e.email, e.employmentstatus, e.job_role_id\n"
			+ "FROM employee e\n"
			+ "LEFT JOIN employee e2 ON e.emp_id = e2.manager_id\n"
			+ "LEFT JOIN employee e3 ON e.emp_id = e3.reporting_manager_id\n"
			+ "WHERE e.employmentstatus != 'InActive'\n"
			+ "AND (e2.manager_id IS NOT NULL OR e3.reporting_manager_id IS NOT NULL)")
	List<Object[]> getAllActivemanagersAndReportingManagers();
	
	@Query(nativeQuery = true,value = "select quarter_id,financial_year,quarter_cycle from quater_cycle where is_active = 1 and is_enable=1")
	List<Object[]> getAllActiveEnabledQuarterCycles();
	
	@Query(nativeQuery = true,value = "SELECT e.emp_id, \n"
			+ "       e.employeement_id, \n"
			+ "       e.name, \n"
			+ "       d.name AS department_name, \n"
			+ "       e.approvals_to, \n"
			+ "       CASE \n"
			+ "         WHEN e.approvals_to = 'Manager' THEN e.manager_id \n"
			+ "         ELSE e.reporting_manager_id \n"
			+ "       END AS reviewer_id\n"
			+ "FROM employee e\n"
			+ "JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "WHERE e.employmentstatus = 'Confirmed'\n"
			+ "  AND e.date_of_joining <= :lastDate \n"
			+ "  AND NOT EXISTS (\n"
			+ "    SELECT 1 \n"
			+ "    FROM employee_performance ep \n"
			+ "    WHERE ep.emp_id = e.emp_id \n"
			+ "      AND ep.quarter_id IN :quarterIds \n"
			+ "  );\n"
			+ "")
	List<Object[]> getEmployeesWithoutPerformance(@Param("lastDate") LocalDate lastDate, @Param("quarterIds") List<Long> quarterIds);

	
	
	@Query(nativeQuery = true,value = "select email,name from employee where emp_id = :empId")
	Object[] findEmailAndNameByEmpId(@Param("empId") Long empId);
	
	@Query(nativeQuery = true, value = "SELECT \n"
			+ "    ep.emp_id, \n"
			+ "    e.employeement_id, \n"
			+ "    e.name, \n"
			+ "    d.name AS department_name, \n"
			+ "    ep.hr_remarks, \n"
			+ "    ep.quarter_id, \n"
			+ "    q.financial_year, \n"
			+ "    q.quarter_cycle,\n"
			+ "    e.approvals_to,\n"
			+ "    CASE \n"
			+ "        WHEN e.approvals_to = 'Manager' THEN e.manager_id\n"
			+ "        ELSE e.reporting_manager_id\n"
			+ "    END AS reviewer_id,\n"
			+ "    r.name AS reviewer_name,\n"
			+ "    r.email AS reviewer_email\n"
			+ "FROM employee_performance ep\n"
			+ "JOIN employee e ON ep.emp_id = e.emp_id\n"
			+ "JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "JOIN quater_cycle q ON ep.quarter_id = q.quarter_id\n"
			+ "JOIN employee r ON (\n"
			+ "    (e.approvals_to = 'Manager' AND e.manager_id = r.emp_id)\n"
			+ " OR (e.approvals_to = 'Reporting Manager' AND e.reporting_manager_id = r.emp_id)\n"
			+ ")\n"
			+ "WHERE ep.completion_status = 'Rejected'\n"
			+ "  AND q.is_active = 1\n"
			+ "  AND q.is_enable = 1;\n"
			+ "")
	List<Object[]> findAllRejectedReviewsAndTheirManagers();
	
	@Query(value="SELECT completion_status,emp_id FROM employee_performance epm inner join quater_cycle qc on epm.quarter_id = qc.quarter_id where qc.is_enable =1 and qc.is_active=1",nativeQuery = true)
	List<Object[]>currentStatusForPerformanceTableView();

	/** Final rating per emp_id from employee_performance.final_rating for active quarters only (matches review page). */
	@Query(nativeQuery = true, value = "SELECT ep.emp_id, ROUND(AVG(CAST(ep.final_rating AS DECIMAL(5,2))), 2) "
			+ "FROM employee_performance ep "
			+ "WHERE ep.quarter_id IN (SELECT quarter_id FROM quater_cycle WHERE is_enable = 1 AND is_active = 1) "
			+ "AND ep.final_rating IS NOT NULL AND ep.final_rating != '' "
			+ "GROUP BY ep.emp_id")
	List<Object[]> findFinalRatingByEmpId();

	/**
	 * Approval status per emp_id for the latest active quarter only (max quarter_id among enabled+active).
	 * Avoids mixing HOD/manager flags from older cycles — needed so HR bulk select matches “HOD approved this cycle”.
	 */
	@Query(nativeQuery = true, value = "SELECT ep.emp_id, "
			+ "CASE WHEN ep.manager_review_date IS NOT NULL THEN 'Submitted' ELSE 'Pending' END, "
			+ "CASE WHEN ep.hod_approval_date IS NOT NULL THEN 'Submitted' ELSE 'Pending' END, "
			+ "COALESCE(CASE WHEN ep.hr_review_status IN ('Accepted','Rejected') THEN ep.hr_review_status ELSE NULL END, 'Pending') "
			+ "FROM employee_performance ep "
			+ "WHERE ep.quarter_id = (SELECT MAX(qc.quarter_id) FROM quater_cycle qc WHERE qc.is_enable = 1 AND qc.is_active = 1)")
	List<Object[]> findApprovalStatusByEmpId();

	@Query(nativeQuery = true , value = "select d.name from employee e "
			+ "inner join job_role jr on e.job_role_id = jr.job_role_id "
			+ "inner join department d on d.dept_id = jr.dept_id "
			+ "where e.emp_id = :empId "	
			)
	
	String getCurrentUserDepartment(@Param("empId") Long empId);
}
