package com.apmosys.employeeportal.repository;

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
			+ "             erp.performance_rating_id\n"
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
		            "WHERE d.dept_id IN (:deptIds)"
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
		    		+ "WHERE e.reporting_manager_id=:empId OR e.manager_id=:empId "
		)	
	List<Object[]> getAllEmployeeReportByEmpId(Long empId);

	@Query(
		    nativeQuery = true,
		    value = "SELECT e.emp_id,e.name,e.employeement_id FROM employee e \n"
		    		+ "INNER JOIN project_insight_response pir ON pir.emp_id=e.emp_id \n"
		    		+ "WHERE pir.process_to=:empId"
		)
	List<Object[]> getEmployeeUnderReviewByEmpId(Long empId);

	
	
	
}
