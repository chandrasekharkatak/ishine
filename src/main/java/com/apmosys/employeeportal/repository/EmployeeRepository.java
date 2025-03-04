package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDto;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Timesheet;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	public Employee findByEmail(String email);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployees();
	
	
	@Query(nativeQuery = true, value = "SELECT e.employeement_id, \n"
			+ " e.date_of_joining, e.email, \n"
			+ " e.employmentstatus, \n"
			+ " e.job_role_id, e.manager_id, e.name, \n"
			+ " jr.dept_id, jr.name as jobrolename, \n"
			+ " d.name as departmentname,e.emp_id, e2.name as manager, e.experience, \n"
			+ "e.billable,e.total_experience,  jr.name as JobRolename,e.billable_type,des.designation_name,e.reporting_manager_id, e5.name AS reportingManger, jr.employee_role, d.hod_id, e7.name AS hodName, d.name AS hodDepartmentName   \n"
			+ "FROM employee e \n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "LEFT JOIN employee e3 on e.updated_by = e3.emp_id \n"
			+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id \n"
			+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id \n"
			+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id \n"
			+ "LEFT JOIN employee e5 ON e.reporting_manager_id = e5.emp_id \n"
			+ "LEFT JOIN designation des ON des.designation_id = e.designation_id \n"
			+ "order by e.name")
	public List<Object[]> getAllEmployeesForPerformance();

//	@Query(nativeQuery = true)	
//	public List<Object[]> getEmployeesByRole();	

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByRole(Long jobRoleId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesByDepartmentIds(List<Long> deptIds);
	
//	@Query(nativeQuery = true)
//	public List<Object[]> getAllEmployeesByDepartmentId(Long departmentId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamView(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamMemberView(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllHolidayByEmpWorkLocation(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesBirthDayToday();

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesBirthDayTomorrow();
	
	public Long countByJobRoleId(Long jobRoleId);

	public Employee findByEmployeementId(Long employeementId);

	public List<Employee> findByMobileNo(Long employeementId);

	public List<Employee> findByAadhar(Long aadhar);

	public List<Employee> findByPanNumber(String panNumber);

	@Query(value = "FROM Employee e WHERE e.mobileNo = :mobileNo AND e.empId != :empId")
	public List<Employee> findByMobileNoAndEmpId(Long mobileNo, Long empId);

	@Query(value = "FROM Employee e WHERE e.aadhar = :aadhar AND e.empId != :empId")
	public List<Employee> findByAadharAndEmpId(Long aadhar, Long empId);

	@Query(value = "FROM Employee e WHERE e.panNumber = :panNumber AND e.empId != :empId")
	public List<Employee> findByPanNumberAndEmpId(String panNumber, Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInfoOnLogin(String email);

	public boolean existsByEmail(String email);

	public Employee findByName(String leaveStatusUpdatedByName);
//	List<Employee> findByName(String name);
	@Query("SELECT e FROM Employee e WHERE LOWER(e.name) = :name")
	Employee findByNameIgnoreCase(@Param("name") String name);
	
	
	


	public List<Employee> findByJobRoleId(Long oldJobRoleId);

	@Query(nativeQuery = true)
	public List<Object[]> getHierarchyByEmpId(Long empId);
	
	

	public Long countByEmpId(Long empId);
	
	public Long countByManagerId(Long managerId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeData(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInProbationAndNotice();

	@Query(nativeQuery = true)
	public List<Object[]> getManagerEmail(Long empId);

	public boolean existsByEmployeementId(Long employeementId);

	public boolean existsByManagerId(Long managerId);

	public boolean existsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	List<Object[]> getEmployees();
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllManagers();
	
//	@Query(value = "select * from employee e where e.employmentstatus like 'inActive' and e.emp_id = :empId")
//	public List<Object[]> getHistoryOfInActiveEmployee();
	
	@Query(nativeQuery = true)
	public List<Object[]> findEmployeeWorkingHistory(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeDetailForCron();

	public List<Employee> findByManagerId(Long empId);
	
	@Query(nativeQuery = true , value="SELECT e.employeement_id,e.name AS employname,d.name AS departmentName,e.is_apprenticeship,e.is_consultant FROM employee e JOIN job_role jr ON e.job_role_id = jr.job_role_id JOIN department d ON jr.dept_id = d.dept_id where e.manager_id= :managerId AND e.employmentstatus!='InActive'")
	public List<Object[]> findReporteesOfManager(Long managerId);
	
	public Employee findByEmpId(Long empid);	
	
	@Query(nativeQuery = true)	
	public List<Employee> getAllActiveEmployees();	
		
	@Query(nativeQuery = true)	
	public List<Object[]> checkMultipleAppreciation(Long appreciationEventId, Long appreciationTo, Long appreciationBy);

	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteeInfo(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyManagerInfo(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getDepartmentByEmployeementId(long employeementId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByDateOfRelieving();

	@Query(nativeQuery = true)
	public List<Object[]> getEmailForMailConsent();
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeProfileCompletion(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeeInfoForPoPortal();

	public List<Employee> findByEmploymentstatus(String employmentStatus);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByDepartmentId(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getElapsedEmpInProbationAndNotice();
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmploymentStatusAndInvalidAccessAttemptByEmpId(Long empId);

	public Long countByDesignationId(Long designationId);

	public List<Employee> findByDesignationId(Long designationId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByRoleIds(List<Long> jobRoleIds);

	@Query(nativeQuery = true)
	public List<Object[]> getManagerByDepartment(Long deptId);

	public List<Employee> findByManagerIdAndGender(Long managerId, Object object);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByManager(Long managerId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeDetailForDSRCron(LocalDate startDate, LocalDate endDate);

	public List<Employee> findByEmploymentstatusIsNot(String string);

	@Query(nativeQuery = true)
	public List<Object[]> findHodByEmpId(Long empId);

	@Query(nativeQuery = true , value = " select count(*) from employee e where (e.manager_id = :empId or e.reporting_manager_id= :empId) and e.employmentstatus != 'InActive'")
	public Long countReportiesByManagerId(Long empId);
	
	@Query(nativeQuery = true )
	public Long countReportiesByReportingManagerId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> findManagerListByRole();

	@Query(nativeQuery = true , value = "Select * from employee e where e.emp_id = :empId")
	public Employee findNameByEmpId(Long empId);

	@Query(nativeQuery = true , value = "Select * from employee e where e.pip_id = :pipId")
	public Employee findEmployeeByPipId(Long pipId);

	@Query(nativeQuery = true , value = "select e.emp_id, e.email as employeeEmail , p.created_on as createdOn ,em.email as managerEmail,p.extend_days,e.name,em.name as managerName from employee e\n"
			+ "inner join pip p on p.pip_id=e.pip_id\n"
			+ "inner join employee em ON em.emp_id=e.manager_id\n"
			+ " where e.pip_flag=1")
	public List<Object[]> findPipUserWithStatus();
	

	@Query(nativeQuery = true , value = "select e.emp_id,e.employeement_id,e.name as employeeName , e.email,e.billable,e.billable_type, d.name as departmentName,em.name as managerName,hd.name as hodName,emp_proj_client.project_name, emp_proj_client.client_name  from employee e \n"
			+ "			 inner join employee em ON em.emp_id=e.manager_id\n"
			+ "			 Inner join job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "			 INNER JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "			 INNER JOIN employee hd ON d.hod_id=hd.emp_id\n"
			+ "             LEFT JOIN (SELECT etm.emp_id, GROUP_CONCAT(DISTINCT pr.project_name) AS project_name, GROUP_CONCAT(DISTINCT cl.client_name) AS client_name \n"
			+ "FROM employee_team_mapping etm \n"
			+ "LEFT JOIN teams t ON t.team_id = etm.team_id\n"
			+ "LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
			+ "LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
			+ "where etm.active !=0\n"
			+ "GROUP BY etm.emp_id) emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
			+ "where e.employmentstatus != 'InActive'")
	public List<Object[]> getEmployeesWithBillableType();

	@Query(nativeQuery = true , value = "select e.email from employee e \n"
			+ "inner join job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "where jr.name like '%VP%' and e.employmentstatus != 'InActive'")
	public List<Object[]> findAllVPsEmail();

//	@Query(nativeQuery = true , value = "select e.employeement_id,e.email,e.name , em.name as managerName,d.name as departmentName, hd.name as hodName,e.billable,e.billable_type,"
//			+ "e.mobile_no,e.mothers_name,e.approvals_to,e.marital_status,emp_proj_client.project_name,"
//			+ " emp_proj_client.client_name,e.gender,e.employmentstatus,e.total_experience,e.date_of_birth,e.date_of_joining,e.work_location,e.experience,e.emp_id,emp_proj_client.team_name,e.is_consultant,e.is_apprenticeship from employee e\n"
//			+ "inner join job_role jr ON jr.job_role_id=e.job_role_id\n"
//			+ "inner join department d ON d.dept_id=jr.dept_id\n"
//			+ "Inner join employee em ON em.emp_id=e.manager_id\n"
//			+ "INNER JOIN employee hd ON hd.emp_id=d.hod_id\n"
//			+ "LEFT JOIN (SELECT etm.emp_id, GROUP_CONCAT(DISTINCT pr.project_name) AS project_name, GROUP_CONCAT(DISTINCT cl.client_name) AS client_name,GROUP_CONCAT(DISTINCT t.team_name) AS team_name \n"
//			+ "FROM employee_team_mapping etm \n"
//			+ "LEFT JOIN teams t ON t.team_id = etm.team_id \n"
//			+ "LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
//			+ "LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
//			+ "WHERE etm.active != 0 "
//			+ "AND t.is_active != 'N' \n"
//			+ "AND pr.active != 'false'\n"
//			+ "GROUP BY etm.emp_id) emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
//			+ "where e.employmentstatus != 'InActive'")
//	public List<Object[]> getBillableEmpWithDepartment();
	
	@Query(nativeQuery = true , value = "SELECT e.employeement_id, e.email, e.name, em.name AS managerName, d.name AS departmentName, \n"
			+ "       hd.name AS hodName, e.billable, e.billable_type, e.mobile_no, e.mothers_name, \n"
			+ "       e.approvals_to, e.marital_status, emp_proj_client.project_name, emp_proj_client.client_name, \n"
			+ "       e.gender, e.employmentstatus, e.total_experience, e.date_of_birth, e.date_of_joining, \n"
			+ "       e.work_location, e.experience, e.emp_id, emp_proj_client.team_name, e.is_consultant, \n"
			+ "       e.is_apprenticeship,\n"
			+ "       emp_proj_client.project_id \n"
			+ "FROM employee e\n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "INNER JOIN employee em ON em.emp_id = e.manager_id\n"
			+ "INNER JOIN employee hd ON hd.emp_id = d.hod_id\n"
			+ "LEFT JOIN (\n"
			+ "    SELECT etm.emp_id, \n"
			+ "           GROUP_CONCAT(DISTINCT pr.project_id) AS project_id,\n"
			+ "           GROUP_CONCAT(DISTINCT pr.project_name) AS project_name, \n"
			+ "           GROUP_CONCAT(DISTINCT cl.client_name) AS client_name, \n"
			+ "           GROUP_CONCAT(DISTINCT t.team_name) AS team_name \n"
			+ "    FROM employee_team_mapping etm \n"
			+ "    LEFT JOIN teams t ON t.team_id = etm.team_id \n"
			+ "    LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
			+ "    LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
			+ "    WHERE etm.active != 0 \n"
			+ "      AND t.is_active != 'N' \n"
			+ "      AND pr.active != 'false'\n"
			+ "    GROUP BY etm.emp_id\n"
			+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
			+ "WHERE e.employmentstatus != 'InActive'")
	public List<Object[]> getBillableEmpWithDepartment();
	
	
	
	
	
	//	report data change , requirement given by pratima ma'am
	
	@Query(nativeQuery = true , value="select d.name as department , e.billable_type , count(e.emp_id) as countEmployees from employee e \n"
			+ "inner join job_role jr ON jr.job_role_id=e.job_role_id\n"
			+ "inner join department d on d.dept_id=jr.dept_id \n"
			+ "where e.employmentstatus != \"InActive\" GROUP BY \n"
			+ "d.name, e.billable_type\n"
			+ "ORDER BY \n"
			+ "d.name, e.billable_type")
	
	public List<Object[]> getEmployeesBillableDataDepartmentWise();
	
	@Query(nativeQuery = true, value = "SELECT d.name AS department_name, e.email AS hod_email\n"
			+ "FROM department d\n"
			+ "JOIN employee e ON d.hod_id = e.emp_id;")
		public List<Object[]> getHodDepartmentEmail();
		
		@Query(nativeQuery = true , value = "SELECT jr.job_role_id,jr.name,jr.employee_role, d.name as departmentName,d.dept_id FROM employee e \n"
				+ "INNER JOIN job_role jr ON jr.job_role_id=e.job_role_id \n"
				+ "INNER JOIN department d ON d.dept_id=jr.dept_id \n"
				+ "WHERE (e.manager_id = :empId OR e.reporting_manager_id= :empId) AND e.employmentstatus != 'InActive' GROUP BY d.name")
	public List<Object[]> findDepartmentsByReporties(Long empId);
				
				
	@Query(nativeQuery = true, value =
		    "select et.description,p.project_id, a.activity_id,\n"
		    + "    e.name, et.date, et.day_type,\n"
		    + "    et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, \n"
		    + "    et.status, et.created_on,\n"
		    + "    a.activity,\n"
		    + "    p.project_name,t.team_name,et.remarks   \n"
		    + "from employee_timesheets et \n"
		    + "INNER JOIN employee e ON et.created_by = e.emp_id\n"
		    + "inner join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
		    + "inner join activities a on a.activity_id = etam.activity_id\n"
		    + "INNER JOIN teams t ON t.team_id = a.team_id \n"
		    + " INNER JOIN projects p ON p.project_id = t.project_id\n"
		    + "WHERE et.status = :status "
		    + "and  et.emp_id=:empId")
		List<Object[]> getTimesheetDataByEmpId(@Param("status") String status,@Param("empId")long empId);
		
		@Query(nativeQuery = true, value =
				"select et.description,et.emp_id, a.activity_id,\n"
					    + "    e.name, et.date, et.day_type,\n"
					    + "    et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, \n"
					    + "    et.status, et.created_on,\n"
					    + "    a.activity,\n"
					    + "    p.project_name,t.team_name,et.remarks   \n"
					    + "from employee_timesheets et \n"
					    + "INNER JOIN employee e ON et.created_by = e.emp_id\n"
					    + "inner join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
					    + "inner join activities a on a.activity_id = etam.activity_id\n"
					    + "INNER JOIN teams t ON t.team_id = a.team_id \n"
					    + " INNER JOIN projects p ON p.project_id = t.project_id\n"
					    + "WHERE et.status = :status "
					    + "and  p.project_id=:projectId "
					    + "and et.current_manager_id=:managerId")
			List<Object[]> getTimesheetDataByProjectId(@Param("status") String status,@Param("projectId")long projectId,@Param("managerId")long managerId);
			
			@Query(nativeQuery = true, value =
					"select et.description,et.emp_id, a.activity_id,\n"
						    + "    e.name, et.date, et.day_type,\n"
						    + "    et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, \n"
						    + "    et.status, et.created_on,\n"
						    + "    a.activity,\n"
						    + "    p.project_name,et.remarks   \n"
						    + "from employee_timesheets et \n"
						    + "INNER JOIN employee e ON et.created_by = e.emp_id\n"
						    + "inner join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
						    + "inner join activities a on a.activity_id = etam.activity_id\n"
						    + "INNER JOIN teams t ON t.team_id = a.team_id \n"
						    + " INNER JOIN projects p ON p.project_id = t.project_id\n"
						    + "WHERE et.status = :status "
						    + "and t.team_name=:teamName "
						    + "and et.current_manager_id=:managerId")
				List<Object[]> getTimesheetDataByTeamName(@Param("status") String status,@Param("teamName")String teamName,@Param("managerId")long managerId);
				
				
		@Query(nativeQuery = true, value = "SELECT et.description, p.project_id, a.activity_id, " +
				            "e.name, et.date, et.day_type, " +
				            "et.office_in_time, et.office_out_time, et.total_working_hours, et.is_night_shift, " +
				            "et.status, et.created_on, " +
				            "a.activity, " +
				            "p.project_name, t.team_name,et.remarks,et.timesheet_id ,e.employeement_id,et.total_time " +
				            "FROM employee_timesheets et " +
				            "LEFT JOIN employee e ON et.created_by = e.emp_id " +
				            "LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id " +
				            "LEFT JOIN activities a ON a.activity_id = etam.activity_id " +
				            "LEFT JOIN teams t ON t.team_id = a.team_id " +
				            "LEFT JOIN projects p ON p.project_id = t.project_id " +
				            "WHERE et.status = :status " +
				            "AND (:empId = 0 OR et.emp_id = :empId) " +
				            "AND (:projectId = 0 OR p.project_id = :projectId) " +
				            "AND (:teamName IS NULL OR t.team_name = :teamName) " +
				            "AND (:managerId = 0 OR et.current_manager_id = :managerId) " +
				            "AND ((:startDate IS NULL OR :endDate IS NULL) OR (et.date BETWEEN :startDate AND :endDate))")
				    List<Object[]> getDynamicTimesheetData(
				            @Param("status") String status,
				            @Param("empId") long empId,
				            @Param("projectId") long projectId,
				            @Param("teamName") String teamName,
				            @Param("managerId") Long managerId,
				            @Param("startDate") LocalDate startDate,
				            @Param("endDate") LocalDate endDate
				    );


				    @Query(nativeQuery = true, value = "SELECT et.timesheet_id,et.emp_id,e.name,et.date,et.day_type,\n"
				    		+ "et.office_in_time,et.office_out_time,et.total_time,\n"
				    		+ "et.status,et.remarks,e.employeement_id,e.created_on,et.current_manager_id\n"
				    		+ "FROM db_emp_portal.employee_timesheets et  \n"
				    		+ "left join employee e \n"
				    		+ "ON et.created_by = e.emp_id " +
				            "WHERE et.status = :status " +
				            "AND (:empId = 0 OR et.emp_id = :empId) " +
				            "AND (:managerId = 0 OR et.current_manager_id = :managerId) " +
				            "AND ((:startDate IS NULL OR :endDate IS NULL) OR (et.date BETWEEN :startDate AND :endDate))")
				    List<Object[]> getTimesheetData(
				            @Param("status") String status,
				            @Param("empId") long empId,
				            @Param("managerId") Long managerId,
				            @Param("startDate") LocalDate startDate,
				            @Param("endDate") LocalDate endDate
				    );
				    
				    @Query(nativeQuery = true, value = "select a.activity_id,etam.description,etam.timesheet_id,etam.completion_time,\n"
				    		+ "t.team_id,t.team_name,p.project_id,p.project_name\n"
				    		+ "from employee_timesheet_activities_mapping etam\n"
				    		+ "LEFT JOIN activities a \n"
				    		+ "ON a.activity_id = etam.activity_id\n"
				    		+ "LEFT JOIN teams t \n"
				    		+ "ON t.team_id = a.team_id \n"
				    		+ "LEFT JOIN projects p \n"
				    		+ "ON p.project_id = t.project_id \n"
				    		+ "where timesheet_id =:timeSheetId")
				    List<Object[]> getActivityData(@Param("timeSheetId") long timeSheetId);
	
	
	
	
	@Query(nativeQuery = true, value = "SELECT employeement_id,name FROM employee where employeement_id in :empList ")
	public List<Object[]> getDataByEmpId(@Param("empList") Set empList );

	@Query(nativeQuery = true)
	public List<Object[]> removeStaleMappingOfInactiveEmployees(); 
	
	@Query(nativeQuery = true)
	public List<Object[]> getReporteesListByManagerId(Long empId );
	
	@Query(nativeQuery = true)
	public List<Object[]> getReporteesListByReportingManagerId(Long empId );
	
	@Query(nativeQuery = true)
	public List<Object[]> getTeamProjectMappingsByEmpId(Long empId );
	
	@Query(nativeQuery = true, value = "select d.designation_name from employee e\n"
			+ "Inner join designation d on d.designation_id=e.designation_id\n"
			+ "where e.emp_id=:empId")
	public Optional<Object[]> getDesignationByEmpId(Long empId);
	
	
	
	@Transactional
	@Modifying
	@Query("UPDATE Employee e SET e.isRetain = 'No' WHERE e.empId = :empId")
	void updateIsRetain(@Param("empId") Long empId);


}
