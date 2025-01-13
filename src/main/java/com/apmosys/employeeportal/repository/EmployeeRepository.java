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

//	@Query(value = "FROM Employee e WHERE e.mobileNo = :mobileNo AND e.empId != :empId")
	public List<Employee> findByMobileNoAndEmpId(Long mobileNo, Long empId);
	


//	@Query(value = "FROM Employee e WHERE e.aadhar = :aadhar AND e.empId != :empId")
	public List<Employee> findByAadharAndEmpId(Long aadhar, Long empId);
	 



//	@Query(value = "FROM Employee e WHERE e.panNumber = :panNumber AND e.empId != :empId")
	public List<Employee> findByPanNumberAndEmpId(String panNumber, Long empId);



	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInfoOnLogin(String email);
	
	
	@Transactional
	@Modifying
	void updateIsRetain(@Param("empId") Long empId);

	public boolean existsByEmail(String email);

	public Employee findByName(String leaveStatusUpdatedByName);
//	List<Employee> findByName(String name);
	
	
//	@Query("SELECT e FROM Employee e WHERE LOWER(e.name) = :name")
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

	@Query(nativeQuery = true )
	public Long countReportiesByManagerId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> findManagerListByRole();

	@Query(nativeQuery = true )
	public Employee findNameByEmpId(Long empId);

	@Query(nativeQuery = true)
	public Employee findEmployeeByPipId(Long pipId);

	@Query(nativeQuery = true )
	public List<Object[]> findPipUserWithStatus();
	

	@Query(nativeQuery = true )
	public List<Object[]> getEmployeesWithBillableType();

	@Query(nativeQuery = true )
	public List<Object[]> findAllVPsEmail();

	@Query(nativeQuery = true )
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
	
	@Query(nativeQuery = true)
		public List<Object[]> getHodDepartmentEmail();
		
		@Query(nativeQuery = true )
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
	
	@Query(nativeQuery = true)
	public List<Object[]> getDataByEmpId(@Param("empList") Set empList );

}
