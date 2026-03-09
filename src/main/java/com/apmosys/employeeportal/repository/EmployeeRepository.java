package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeJobRoleDept;
import com.apmosys.employeeportal.dto.EmployeeMailDTO;
import com.apmosys.employeeportal.dto.EmployeeProjection;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeListByProjectIdDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	public Employee findByEmail(String email);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByEmpId(Long empId);

	@Cacheable(value = "Employee")
	@Query(nativeQuery = true)
	public List<EmployeeProjection> getAllEmployees();
	
	@Query(nativeQuery = true)
	public List<Object[]>  getAllEmployees360(Long empId);
	
	@Query("SELECT e.empId, e.name FROM Employee e WHERE e.empId IN :ids")
	List<Object[]> getEmployeeNamesByEmpIds(@Param("ids") Set<Long> ids);
	
	
	@Query("Select e.empId from Employee e where e.employeementId = :employeementId and name = :employeeName ")
	Optional<Long> findByEmploymentIdAndEmployeeName(@Param("employeementId") Long employeementId,@Param("employeeName") String employeeName );
	
	@Query(nativeQuery = true, value = "SELECT e.employeement_id, \n"
			+ " e.date_of_joining, e.email, \n"
			+ " e.employmentstatus, \n"
			+ " e.job_role_id, e.manager_id, e.name, \n"
			+ " jr.dept_id, jr.name as jobrolename, \n"
			+ " d.name as departmentname,e.emp_id, e2.name as manager, e.experience, \n"
			+ "e.billable,e.total_experience,  jr.name as JobRolename,e.billable_type,des.designation_name,e.reporting_manager_id, e5.name AS reportingManger, jr.employee_role, d.hod_id, e7.name AS hodName, d.name AS hodDepartmentName , e.is_apmosys_product \n"
			+ "FROM employee e \n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "LEFT JOIN employee e3 on e.updated_by = e3.emp_id \n"
			+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id \n"
			+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id \n"
			+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id \n"
			+ "LEFT JOIN employee e5 ON e.reporting_manager_id = e5.emp_id \n"
			+ "LEFT JOIN designation des ON des.designation_id = e.designation_id \n"
			+ " where e.employmentstatus!='InActive'  \n"
			+ "order by e.name")
	public List<Object[]> getAllEmployeesForPerformanceForHr();

	@Query(nativeQuery = true, value = "SELECT e.employeement_id, \n"
			+ " e.date_of_joining, e.email, \n"
			+ " e.employmentstatus, \n"
			+ " e.job_role_id, e.manager_id, e.name, \n"
			+ " jr.dept_id, jr.name as jobrolename, \n"
			+ " d.name as departmentname,e.emp_id, e2.name as manager, e.experience, \n"
			+ "e.billable,e.total_experience,  jr.name as JobRolename,e.billable_type,des.designation_name,e.reporting_manager_id, e5.name AS reportingManger, jr.employee_role, d.hod_id, e7.name AS hodName, d.name AS hodDepartmentName,e.is_apmosys_product  \n"
			+ "FROM employee e \n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id \n"
			+ "LEFT JOIN employee e3 on e.updated_by = e3.emp_id \n"
			+ "LEFT JOIN employee e4 on e.created_by = e4.emp_id \n"
			+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id \n"
			+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id \n"
			+ "LEFT JOIN employee e5 ON e.reporting_manager_id = e5.emp_id \n"
			+ "LEFT JOIN designation des ON des.designation_id = e.designation_id \n"
			+ " where e.employmentstatus!='InActive' AND  d.hod_id=:empId  \n"
			+ "or (\n"
			+ "    CASE \n"
			+ "        WHEN e.approvals_to = 'Manager' THEN e.manager_id = :empId \n"
			+ "  WHEN e.approvals_to = 'Reporting Manager' THEN e.reporting_manager_id = :empId \n"
			+ "        ELSE (d.hod_id = :empId)\n"
			+ "    END\n"
			+ ")"
			+ "order by e.name")
	public List<Object[]> getAllEmployeesForPerformance(Long empId);

	// @Query(nativeQuery = true)
	// public List<Object[]> getEmployeesByRole();

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByRole(Long jobRoleId);

//	@Query(nativeQuery = true)
//	public List<Object[]> getAllEmployeesByDepartmentIds(List<Long> deptIds);
	
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.empId,e.name,jr.name,d.deptId,d.name,jr.jobRoleId,e.billableType, \n" +
			"CASE \n" +
			"    WHEN e.isConsultant = 'true' THEN CONCAT('CS-', e.employeementId) \n" +
			"    ELSE CONCAT('A-', e.employeementId) \n" +
			"END)  \n" +
			"FROM Employee e \n" +
			"INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n" +
			"INNER JOIN Department d ON d.deptId = jr.deptId \n" +
			"WHERE d.deptId IN :deptIds AND e.employmentstatus not like 'InActive' and e.empId not in (1,2,3,4,5,6)")
		public List<EmployeeDTO> getAllEmployeesByDepartmentIds(List<Long> deptIds);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.empId,e.name,jr.name,d.deptId,d.name,jr.jobRoleId,e.billableType, \n" +
			"CASE \n" +
			"    WHEN e.isConsultant = 'true' THEN CONCAT('CS-', e.employeementId) \n" +
			"    ELSE CONCAT('A-', e.employeementId) \n" +
			"END,ee.createdOn,ee.updatedOn,cb.name AS created_by_name ,p.projectName )  \n" +
			"FROM Employee e \n" +
			"INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n" +
			"INNER JOIN Department d ON d.deptId = jr.deptId \n" +
			"LEFT JOIN EmployeeexcludedFromLeave ee ON ee.empId=e.empId  \n"+
			"LEFT JOIN Employee cb ON cb.empId = ee.createdBy "+
			"LEFT JOIN EmployeeTeamMap etm on etm.empId=e.empId \n"+
			"INNER JOIN Team t on t.teamId=etm.teamId \n"+
			"INNER JOIN Project p on p.projectId=t.projectId  \n "+
			"WHERE d.deptId IN :deptIds AND (ee.isExcluded IS NULL or ee.isExcluded = 0)"
			+ "AND  p.active = 'true' and t.isActive = 'Y'"
			+ "AND e.employmentstatus not like 'InActive' and e.empId not in (1,2,3,4,5,6)"
			+ "AND (:empId IS NULL OR CAST(e.employeementId AS string) LIKE CONCAT('%', :empId, '%'))\n"
			+ " AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))\n"
			+ " AND (:jobRoleId IS NULL OR LOWER(jr.name) LIKE LOWER(CONCAT('%', :jobRoleId, '%')))\n"
			+ " AND (:deptName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :deptName, '%')))\n"
			+ " AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
			+ " AND (:billableType IS NULL OR e.billableType = :billableType)")
		public Page<EmployeeDTO> getAllEmployeesByDepartmentIdsForLeaveExclusion(List<Long> deptIds, String empId,
			    String name,String jobRoleId,String deptName,String projectName,String billableType,Pageable pageable);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.empId,e.name,jr.name,d.deptId,d.name,jr.jobRoleId,"
			+ "e.billableType, \n" +
			"CASE \n" +
			"    WHEN e.isConsultant = 'true' THEN CONCAT('CS-', e.employeementId) \n" +
			"    ELSE CONCAT('A-', e.employeementId) \n" +
			"END,ee.createdOn,ee.updatedOn,cb.name AS created_by_name ,p.projectName)  \n" +
			"FROM Employee e \n" +
			"INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n" +
			"INNER JOIN Department d ON d.deptId = jr.deptId \n" +
			"INNER JOIN EmployeeexcludedFromLeave ee ON ee.empId=e.empId AND ee.isExcluded=1 \n"+
			"LEFT JOIN Employee cb ON cb.empId = ee.createdBy "+
			"LEFT JOIN EmployeeTeamMap etm on etm.empId=e.empId \n"
			+ "INNER JOIN Team t on t.teamId=etm.teamId \n"
			+ "INNER JOIN Project p on p.projectId=t.projectId "+
			"WHERE d.deptId IN :deptIds "
			+ " AND  p.active = 'true' and t.isActive = 'Y' "
			+ " AND e.employmentstatus not like 'InActive' and e.empId not in (1,2,3,4,5,6) \n"
			+ " AND (:empId IS NULL OR CAST(e.employeementId AS string) LIKE CONCAT('%', :empId, '%'))\n"
			+ " AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))\n"
			+ " AND (:jobRoleId IS NULL OR LOWER(jr.name) LIKE LOWER(CONCAT('%', :jobRoleId, '%')))\n"
			+ " AND (:deptName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :deptName, '%')))\n"
			+ " AND (:projectName IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :projectName, '%')))\n"
			+ " AND (:billableType IS NULL OR e.billableType = :billableType)")
		public Page<EmployeeDTO> getAllEmployeesByDepartmentIdsForLeaveInclusion(List<Long> deptIds,String empId,
				String name,String jobRoleId,String deptName,String projectName,String billableType,Pageable pageable);



	
//	@Query(nativeQuery = true)
//	public List<Object[]> getAllEmployeesByDepartmentId(Long departmentId);

	/**
	 * Team view helper: fetch employees for the given departments using the same
	 * column order as Employee.getAllTeamView named query.
	 */
	@Query(value = "SELECT e.emp_id, e.name, e.email, jr.name AS jobrolename, e.mobile_no, em.name AS manager, " +
			       "e.employeement_id, e.invalid_access_attempt, e.is_timesheet_lock_check_enable, e.employmentstatus, " +
			       "e.date_of_relieving, e.pip_flag, p.pip_id, e.is_consultant, e.is_apprenticeship, e.is_apmosys_product " +
			       "FROM employee e " +
			       "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id " +
			       "INNER JOIN department d ON d.dept_id = jr.dept_id " +
			       "LEFT JOIN pip p ON p.pip_id = e.pip_id " +
			       "INNER JOIN employee em ON em.emp_id = e.manager_id " +
			       "WHERE e.employmentstatus NOT LIKE 'InActive' " +
			       "  AND d.dept_id IN (:deptIds)",
	       nativeQuery = true)
	List<Object[]> getAllTeamViewByDepartmentIds(@Param("deptIds") List<Long> deptIds);

	/**
	 * Fetches employees for given department IDs with same SELECT as Employee.getAllTeamMemberView
	 * so that TeamsService.getAllTeamMemberView DTO mapping (26 columns) works for 3-A branch.
	 */
	@Query(value = "SELECT DISTINCT " +
	               "e.emp_id, e.name, e.email, jr.name AS jobrolename, e.mobile_no, e.employeement_id, e.employmentstatus, " +
	               "e.manager_id, em.name AS managerName, em.email AS managerEmail, " +
	               "d.hod_id, eh.name AS hodName, eh.email AS hodEmail, d.dept_id, " +
	               "e.is_timesheet_lock_check_enable, e.reporting_manager_id, e.approvals_to, " +
	               "rm.name AS reportingManager, rm.email AS reportingManagerEmail, " +
	               "e.date_of_joining, e.probation_period, e.is_consultant, e.is_apprenticeship, e.is_apmosys_product, " +
	               "GROUP_CONCAT(DISTINCT ecsm.client_side_id ORDER BY ecsm.client_side_id SEPARATOR ', ') AS client_side_ids, " +
	               "CASE " +
	               "  WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id) " +
	               "  WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) " +
	               "  ELSE CONCAT('A-', e.employeement_id) " +
	               "END " +
	               "FROM employee e " +
	               "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id " +
	               "INNER JOIN department d ON d.dept_id = jr.dept_id " +
	               "INNER JOIN employee em ON e.manager_id = em.emp_id " +
	               "INNER JOIN employee eh ON d.hod_id = eh.emp_id " +
	               "LEFT JOIN employee rm ON e.reporting_manager_id = rm.emp_id " +
	               "LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.active = TRUE " +
	               "WHERE e.employmentstatus != 'InActive' AND d.dept_id IN (:deptIds) " +
	               "GROUP BY " +
	               "  e.emp_id, e.name, e.email, jr.name, e.mobile_no, e.employeement_id, e.employmentstatus, " +
	               "  e.manager_id, em.name, em.email, d.hod_id, eh.name, eh.email, d.dept_id, " +
	               "  e.is_timesheet_lock_check_enable, e.reporting_manager_id, e.approvals_to, " +
	               "  rm.name, rm.email, e.date_of_joining, e.probation_period, e.is_consultant, e.is_apprenticeship, e.is_apmosys_product " +
	               "ORDER BY e.name",
	       nativeQuery = true)
	List<Object[]> getAllTeamMemberViewByDepartmentIds(@Param("deptIds") List<Long> deptIds);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamView(Long empId);

	@Query(name = "Employee.getAllTeamMemberView",nativeQuery = true)
	public List<Object[]> getAllTeamMemberView(@Param("emp_id") Long empId);

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

	@Query(value = "FROM Employee e WHERE e.employeementId = :employeementId")
	public Optional<Employee> findByemployeementIdForBioMax(Long employeementId);

	// @Query(value = "FROM Employee e WHERE e.aadhar = :aadhar AND e.empId !=
	// :empId")
	public List<Employee> findByAadharAndEmpId(Long aadhar, Long empId);

	@Query(value = "FROM Employee e WHERE e.panNumber = :panNumber AND e.empId != :empId")
	public List<Employee> findByPanNumberAndEmpId(String panNumber, Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInfoOnLogin(String email);

	public boolean existsByEmail(String email);

	public Employee findByName(String leaveStatusUpdatedByName);

	// List<Employee> findByName(String name);
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
	
//	@Query("SELECT e.employeementId, e.name, hod.email, e.probationPeriod, e.noticePeriod, e.dateOfJoining \n"
//			+ "FROM Employee e \n"
//			+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
//			+ "INNER JOIN Department d ON jr.deptId = d.deptId \n"
//			+ "INNER JOIN Employee hod ON d.hodId = hod.empId \n"
//			+ "WHERE e.employmentstatus NOT LIKE 'InActive' AND DATEDIFF(CURDATE(), e.dateOfJoining) < e.probationPeriod")
//	public List<EmployeeDTO> getEmployeeInProbation();
	
	@Query(nativeQuery = true)
	public List<Employee> getEmployeeInProbation();
	
	@Query(nativeQuery = true)
	public List<Employee> getEmployeeInProbationExtended();

	@Query(nativeQuery = true)
	public List<Object[]> getManagerEmail(Long empId);

	public boolean existsByEmployeementId(Long employeementId);

	public boolean existsByManagerId(Long managerId);

	public boolean existsByEmpId(Long empId);

	@Query(nativeQuery = true)
	List<Object[]> getEmployees();

	@Query(nativeQuery = true)
	public List<Object[]> getAllManagers();

	// @Query(value = "select * from employee e where e.employmentstatus like
	// 'inActive' and e.emp_id = :empId")
	// public List<Object[]> getHistoryOfInActiveEmployee();

	@Query(nativeQuery = true)
	public List<Object[]> findEmployeeWorkingHistory(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeDetailForCron();
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeDetailForCronExludingSomeEmployees();


	public List<Employee> findByManagerId(Long empId);
	
	@Query(nativeQuery = true , value="SELECT e.employeement_id,e.name AS employname,d.name AS departmentName,"
			+ "e.is_apprenticeship,e.is_consultant "
			+ "FROM employee e "
			+ "JOIN job_role jr ON e.job_role_id = jr.job_role_id "
			+ "JOIN department d ON jr.dept_id = d.dept_id "
			+ "where e.manager_id= :managerId AND e.employmentstatus!='InActive'")
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
	
	@Query(nativeQuery = true,value ="SELECT d.name as department FROM employee e \n"
			+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
			+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
			+ "WHERE e.emp_id =:empId")
	public String getDepartmentByEmpId(long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByDateOfRelieving();

	@Query(nativeQuery = true)
	public List<Object[]> getEmailForMailConsent();

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeProfileCompletion(Long empId);

	@Query(value="SELECT new com.apmosys.employeeportal.dto.PoPortalDTO(e.employeementId, e.name, d.deptId, e.employmentstatus, "
			+ "e.email, e.mobileNo, e.jobRoleId, d.hodId, e.empId, \n"
			+ "case when e.empId in (select hodId from Department) then 'Y' else 'N'\n"
			+ "end as isHead,e.isApmosysProduct) \n" +
			"FROM Employee e  \n" +
			"INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId  \n" +
			"INNER JOIN Department d ON d.deptId = jr.deptId")
	public List<PoPortalDTO> getAllEmployeeInfoForPoPortal();

	public List<Employee> findByEmploymentstatus(String employmentStatus);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByDepartmentId(Long deptId);

	@Query(nativeQuery = true)
	public List<Object[]> getElapsedEmpInProbationAndNotice();

	@Query(nativeQuery = true)
	public List<Object[]> getEmploymentStatusAndInvalidAccessAttemptByEmpId(Long empId);

	public Long countByDesignationId(Long designationId);

	public List<Employee> findByDesignationId(Long designationId);

//	@Query(nativeQuery = true)
//	public List<Object[]> getEmployeesByRoleIds(List<Long> jobRoleIds);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.empId,e.name,jr.name, d.deptId, e.employeementId,e.billableType, \n " +
			"CASE \n " +
			"  WHEN e.isConsultant = 'true' THEN CONCAT('CS-', e.employeementId) \n " +
			"  ELSE CONCAT('A-', e.employeementId) \n " +
			"END) \n " +
			"FROM Employee e \n " +
			"INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n " +
			"INNER JOIN Department d ON d.deptId = jr.deptId \n " +
			"where e.employmentstatus not like 'InActive' AND jr.jobRoleId IN :jobRoleIds and e.empId not in (1,2,3,4,5,6)")
		public List<EmployeeDTO> getEmployeesByRoleIds(List<Long> jobRoleIds);

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

	@Query(nativeQuery = true, value = " select count(*) from employee e where (e.manager_id = :empId or e.reporting_manager_id= :empId) and e.employmentstatus != 'InActive'")
	public Long countReportiesByManagerId(Long empId);

	@Query(nativeQuery = true)
	public Long countReportiesByReportingManagerId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> findManagerListByRole();

	@Query(nativeQuery = true, value = "Select * from employee e where e.emp_id = :empId")
	public Employee findNameByEmpId(Long empId);

	@Query(nativeQuery = true, value = "Select * from employee e where e.pip_id = :pipId")
	public Employee findEmployeeByPipId(Long pipId);

	@Query(nativeQuery = true, value = "select e.emp_id, e.email as employeeEmail , p.created_on as createdOn ,em.email as managerEmail,p.extend_days,e.name,em.name as managerName from employee e\n"
			+ "inner join pip p on p.pip_id=e.pip_id\n"
			+ "inner join employee em ON em.emp_id=e.manager_id\n"
			+ " where e.pip_flag=1")
	public List<Object[]> findPipUserWithStatus();

	@Query(nativeQuery = true, value = "select e.emp_id,e.employeement_id,e.name as employeeName , e.email,e.billable,e.billable_type, d.name as departmentName,em.name as managerName,hd.name as hodName,emp_proj_client.project_name, emp_proj_client.client_name  from employee e \n"
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
			+ "where e.employmentstatus != 'InActive' AND d.dept_id = :deptId ")
	public List<Object[]> getEmployeesWithBillableType(Long deptId);

	@Query(nativeQuery = true, value = "select e.email, d.dept_id from employee e \n"
			+ "inner join job_role jr ON jr.job_role_id = e.job_role_id \n"
			+ "inner join department d on d.dept_id = jr.dept_id \n"
			+ "where jr.name like '%VP%' and jr.employee_role='HOD' and e.employmentstatus != 'InActive'")
	public List<Object[]> findAllVPsEmail();

	// @Query(nativeQuery = true , value = "select e.employeement_id,e.email,e.name
	// , em.name as managerName,d.name as departmentName, hd.name as
	// hodName,e.billable,e.billable_type,"
	// +
	// "e.mobile_no,e.mothers_name,e.approvals_to,e.marital_status,emp_proj_client.project_name,"
	// + "
	// emp_proj_client.client_name,e.gender,e.employmentstatus,e.total_experience,e.date_of_birth,e.date_of_joining,e.work_location,e.experience,e.emp_id,emp_proj_client.team_name,e.is_consultant,e.is_apprenticeship
	// from employee e\n"
	// + "inner join job_role jr ON jr.job_role_id=e.job_role_id\n"
	// + "inner join department d ON d.dept_id=jr.dept_id\n"
	// + "Inner join employee em ON em.emp_id=e.manager_id\n"
	// + "INNER JOIN employee hd ON hd.emp_id=d.hod_id\n"
	// + "LEFT JOIN (SELECT etm.emp_id, GROUP_CONCAT(DISTINCT pr.project_name) AS
	// project_name, GROUP_CONCAT(DISTINCT cl.client_name) AS
	// client_name,GROUP_CONCAT(DISTINCT t.team_name) AS team_name \n"
	// + "FROM employee_team_mapping etm \n"
	// + "LEFT JOIN teams t ON t.team_id = etm.team_id \n"
	// + "LEFT JOIN projects pr ON pr.project_id = t.project_id \n"
	// + "LEFT JOIN clients cl ON cl.client_id = pr.client_id \n"
	// + "WHERE etm.active != 0 "
	// + "AND t.is_active != 'N' \n"
	// + "AND pr.active != 'false'\n"
	// + "GROUP BY etm.emp_id) emp_proj_client ON emp_proj_client.emp_id = e.emp_id
	// \n"
	// + "where e.employmentstatus != 'InActive'")
	// public List<Object[]> getBillableEmpWithDepartment();

	@Query(nativeQuery = true, value = "SELECT e.employeement_id, e.email, e.name, em.name AS managerName, d.name AS departmentName, \n"
			+ "       hd.name AS hodName, e.billable, e.billable_type, e.mobile_no, e.mothers_name, \n"
			+ "       e.approvals_to, e.marital_status, emp_proj_client.project_name, emp_proj_client.client_name, \n"
			+ "       e.gender, e.employmentstatus, e.total_experience, e.date_of_birth, e.date_of_joining, \n"
			+ "       e.work_location, e.experience, e.emp_id, emp_proj_client.team_name, e.is_consultant, \n"
			+ "       e.is_apprenticeship,\n"
			+ "       emp_proj_client.project_id, e.manager_id \n"
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

	// report data change , requirement given by pratima ma'am

	@Query(nativeQuery = true, value = "select d.name as department , e.billable_type , count(e.emp_id) as countEmployees from employee e \n"
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

	@Query(nativeQuery = true, value = "SELECT jr.job_role_id,jr.name,jr.employee_role, d.name as departmentName,d.dept_id FROM employee e \n"
			+ "INNER JOIN job_role jr ON jr.job_role_id=e.job_role_id \n"
			+ "INNER JOIN department d ON d.dept_id=jr.dept_id \n"
			+ "WHERE (e.manager_id = :empId OR e.reporting_manager_id= :empId) AND e.employmentstatus != 'InActive' GROUP BY d.name")
	public List<Object[]> findDepartmentsByReporties(Long empId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "select et.description,p.project_id, a.activity_id,\n"
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
	List<Object[]> getTimesheetDataByEmpId_old(@Param("status") String status, @Param("empId") long empId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "select et.description,p.project_id, a.activity_id,\n"
			+ "    e.name, et.date, dtm.day_type,\n"
			+ "    et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, et.is_night_shift, \n"
			+ "    sm.status, et.created_on,\n"
			+ "    a.activity,\n"
			+ "    p.project_name,t.team_name,et.remarks   \n"
			+ "from employee_timesheets_new et \n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "INNER JOIN employee e ON et.created_by = e.emp_id\n"
			+ "inner join employee_timesheet_activities_mapping_new etam on et.timesheet_id = etam.timesheet_id\n"
			+ "inner join activities a on a.activity_id = etam.activity_id\n"
			+ "INNER JOIN teams t ON t.team_id = a.team_id \n"
			+ " INNER JOIN projects p ON p.project_id = t.project_id AND p.project_id = etam.project_id\n"
			+ "WHERE sm.status = :status "
			+ "and  et.emp_id=:empId")
	List<Object[]> getTimesheetDataByEmpId(@Param("status") String status, @Param("empId") long empId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "select et.description,et.emp_id, a.activity_id,\n"
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
	List<Object[]> getTimesheetDataByProjectId_old(@Param("status") String status, @Param("projectId") long projectId,
			@Param("managerId") long managerId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "select et.description,et.emp_id, a.activity_id,\n"
			+ "    e.name, et.date, dtm.day_type,\n"
			+ "    et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, et.is_night_shift, \n"
			+ "    sm.status, et.created_on,\n"
			+ "    a.activity,\n"
			+ "    p.project_name,t.team_name,et.remarks   \n"
			+ "from employee_timesheets_new et \n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "INNER JOIN employee e ON et.created_by = e.emp_id\n"
			+ "inner join employee_timesheet_activities_mapping_new etam on et.timesheet_id = etam.timesheet_id\n"
			+ "inner join activities a on a.activity_id = etam.activity_id\n"
			+ "INNER JOIN teams t ON t.team_id = a.team_id \n"
			+ " INNER JOIN projects p ON p.project_id = t.project_id AND p.project_id = etam.project_id\n"
			+ "WHERE sm.status = :status "
			+ "and  p.project_id=:projectId "
			+ "and et.current_manager_id=:managerId")
	List<Object[]> getTimesheetDataByProjectId(@Param("status") String status, @Param("projectId") long projectId,
			@Param("managerId") long managerId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "select et.description,et.emp_id, a.activity_id,\n"
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
	List<Object[]> getTimesheetDataByTeamName_old(@Param("status") String status, @Param("teamName") String teamName,
			@Param("managerId") long managerId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "select et.description,et.emp_id, a.activity_id,\n"
			+ "    e.name, et.date, dtm.day_type,\n"
			+ "    et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, et.is_night_shift, \n"
			+ "    sm.status, et.created_on,\n"
			+ "    a.activity,\n"
			+ "    p.project_name,et.remarks   \n"
			+ "from employee_timesheets_new et \n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "INNER JOIN employee e ON et.created_by = e.emp_id\n"
			+ "inner join employee_timesheet_activities_mapping_new etam on et.timesheet_id = etam.timesheet_id\n"
			+ "inner join activities a on a.activity_id = etam.activity_id\n"
			+ "INNER JOIN teams t ON t.team_id = a.team_id \n"
			+ " INNER JOIN projects p ON p.project_id = t.project_id AND p.project_id = etam.project_id\n"
			+ "WHERE sm.status = :status "
			+ "and t.team_name=:teamName "
			+ "and et.current_manager_id=:managerId")
	List<Object[]> getTimesheetDataByTeamName(@Param("status") String status, @Param("teamName") String teamName,
			@Param("managerId") long managerId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
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
	List<Object[]> getDynamicTimesheetData_old(
			@Param("status") String status,
			@Param("empId") long empId,
			@Param("projectId") long projectId,
			@Param("teamName") String teamName,
			@Param("managerId") Long managerId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "SELECT et.description, p.project_id, a.activity_id, " +
			"e.name, et.date, dtm.day_type, " +
			"et.office_in_time, et.office_out_time, TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours, et.is_night_shift, " +
			"sm.status, et.created_on, " +
			"a.activity, " +
			"p.project_name, t.team_name,et.remarks,et.timesheet_id ,e.employeement_id,ROUND(et.total_activities_minutes / 60, 2) AS total_time " +
			"FROM employee_timesheets_new et " +
			"LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id " +
			"LEFT JOIN status_master_new sm ON et.status = sm.status_id " +
			"LEFT JOIN employee e ON et.created_by = e.emp_id " +
			"LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id " +
			"LEFT JOIN activities a ON a.activity_id = etam.activity_id " +
			"LEFT JOIN teams t ON t.team_id = a.team_id " +
			"LEFT JOIN projects p ON p.project_id = t.project_id AND p.project_id = etam.project_id " +
			"WHERE sm.status = :status " +
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
			@Param("endDate") LocalDate endDate);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(nativeQuery = true, value = "SELECT et.timesheet_id,et.emp_id,e.name,et.date,et.day_type,\n"
			+ "et.office_in_time,et.office_out_time,et.total_time,\n"
			+ "et.status,et.remarks,e.employeement_id,et.created_on,et.current_manager_id\n"
			+ "FROM employee_timesheets et  \n"
			+ "inner join employee_timesheet_activities_mapping etam on etam.timesheet_id = et.timesheet_id \n"
			+ "inner join activities a on a.activity_id = etam.activity_id \n"
			+ "inner join teams t on t.team_id = a.team_id \n"
			+ "inner join projects p on p.project_id = t.project_id \n"
			+ "inner join employee e ON et.emp_id = e.emp_id \n"
			+ "WHERE et.status = :status "
			+ " AND (:projectId = 0 OR p.project_id = :projectId) \n "
			+ " AND (:teamName = 0 OR t.team_id = :teamName) \n "
			+ "AND (:empId = 0 OR et.emp_id = :empId) "
			+ "AND et.date >= CURDATE() - INTERVAL 3 MONTH\n"
			+ "AND ((:startDate IS NULL OR :endDate IS NULL) OR (et.date BETWEEN :startDate AND :endDate))"
			+ "ORDER BY et.date DESC\n")
	List<Object[]> getTimesheetData_old(
			@Param("status") String status,
			@Param("empId") long empId,
			// @Param("managerId") Long managerId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("projectId") long projectId,
			@Param("teamName") long teamName);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "SELECT et.timesheet_id,et.emp_id,e.name,et.date,dtm.day_type,\n"
			+ "et.office_in_time,et.office_out_time,ROUND(et.total_activities_minutes / 60, 2) AS total_time,\n"
			+ "sm.status,et.remarks,e.employeement_id,et.created_on,et.current_manager_id\n"
			+ "FROM employee_timesheets_new et  \n"
			+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "inner join employee_timesheet_activities_mapping_new etam on etam.timesheet_id = et.timesheet_id \n"
			+ "inner join activities a on a.activity_id = etam.activity_id \n"
			+ "inner join teams t on t.team_id = a.team_id \n"
			+ "inner join projects p on p.project_id = t.project_id AND p.project_id = etam.project_id \n"
			+ "inner join employee e ON et.emp_id = e.emp_id \n"
			+ "WHERE sm.status = :status "
			+ " AND (:projectId = 0 OR p.project_id = :projectId) \n "
			+ " AND (:teamName = 0 OR t.team_id = :teamName) \n "
			+ "AND (:empId = 0 OR et.emp_id = :empId) "
			+ "AND et.date >= CURDATE() - INTERVAL 3 MONTH\n"
			+ "AND ((:startDate IS NULL OR :endDate IS NULL) OR (et.date BETWEEN :startDate AND :endDate))"
			+ "ORDER BY et.date DESC\n")
	List<Object[]> getTimesheetData(
			@Param("status") String status,
			@Param("empId") long empId,
			// @Param("managerId") Long managerId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("projectId") long projectId,
			@Param("teamName") long teamName);

	// ========== BACKUP: Original query renamed with _old suffix ==========
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
	List<Object[]> getActivityData_old(@Param("timeSheetId") long timeSheetId);

	// ========== UPDATED: New query using _new tables ==========
	@Query(nativeQuery = true, value = "select a.activity_id,etam.description,etam.timesheet_id,CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time,\n"
			+ "t.team_id,t.team_name,p.project_id,p.project_name\n"
			+ "from employee_timesheet_activities_mapping_new etam\n"
			+ "LEFT JOIN activities a \n"
			+ "ON a.activity_id = etam.activity_id\n"
			+ "LEFT JOIN teams t \n"
			+ "ON t.team_id = a.team_id \n"
			+ "LEFT JOIN projects p \n"
			+ "ON p.project_id = t.project_id AND p.project_id = etam.project_id \n"
			+ "where etam.timesheet_id =:timeSheetId")
	List<Object[]> getActivityData(@Param("timeSheetId") long timeSheetId);

	@Query(nativeQuery = true, value = "SELECT employeement_id,name FROM employee where employeement_id in :empList ")
	public List<Object[]> getDataByEmpId(@Param("empList") Set empList);

	@Query(nativeQuery = true)
	public List<Object[]> removeStaleMappingOfInactiveEmployees();

	@Query(nativeQuery = true)
	public List<Object[]> getReporteesListByManagerId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getReporteesListByReportingManagerId(Long empId);

	@Query(nativeQuery = true, value = "select d.designation_name from employee e\n"
			+ "Inner join designation d on d.designation_id=e.designation_id\n"
			+ "where e.emp_id=:empId")
	public Optional<Object[]> getDesignationByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesReportByProjectType();

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesReportByProjectTypeInConsolidated();

	@Transactional
	@Modifying
	@Query("UPDATE Employee e SET e.isRetain = 'No' WHERE e.empId = :empId")
	void updateIsRetain(@Param("empId") Long empId);

	@Query(nativeQuery = true, value = "select e.emp_id from employee e \n"
			+ " inner join user_session u on e.emp_id = u.emp_id")
	public Long findByEmpId();

	List<Employee> findByJobRoleIdIn(List<Long> jobRoleIds);

	@Query("SELECT e FROM Employee e " +
			"WHERE e.jobRoleId IN (" +
			"    SELECT j.jobRoleId FROM JobRole j " +
			"    WHERE j.deptId = (" +
			"        SELECT j2.deptId FROM JobRole j2 " +
			"        WHERE j2.jobRoleId = (" +
			"            SELECT e2.jobRoleId FROM Employee e2 " +
			"            WHERE e2.empId = (" +
			"                SELECT us.empId FROM UserSession us " +
			"                WHERE us.sessionKey = :sessionKey" +
			"            )" +
			"        )" +
			"    )" +
			")")
	List<Employee> findEmployeesByUserSessionDepartment(@Param("sessionKey") String sessionKey);

	@Query(nativeQuery = true, value = "SELECT \n"
			+ "    e.emp_id,\n"
			+ "    e.name,\n"
			+ "    e.employeement_id,\n"
			+ "    COALESCE(eg.NoofGoals, 0) AS NoofGoals\n"
			+ "FROM \n"
			+ "    employee e\n"
			+ "INNER JOIN \n"
			+ "    job_role j ON j.job_role_id = e.job_role_id\n"
			+ "INNER JOIN \n"
			+ "    department d ON j.dept_id = d.dept_id\n"
			+ "LEFT JOIN \n"
			+ "    (SELECT emp_id, COUNT(*) AS NoofGoals \n"
			+ "     FROM employee_goals \n"
			+ "     GROUP BY emp_id) eg ON e.emp_id = eg.emp_id\n"
			+ "WHERE \n"
			+ "    d.hod_id = :hodId")
	List<Object[]> findEmployeesInSameDepartmentAsCurrentUser(Long hodId);

	@Query(value = "SELECT eg.* " +
			"FROM employee_goals eg " +
			"JOIN quater_cycle qc ON eg.quarter = qc.quarter_cycle " +
			"WHERE eg.emp_id = :employeeId " +
			"AND qc.quarter_id = :quarterId", nativeQuery = true)
	List<Object[]> findEmployeeGoalsByEmpIdAndQuarterId(@Param("employeeId") Long employeeId,
			@Param("quarterId") Long quarterId);

	@Query(value = "SELECT jb.employee_role \n"
			+ "FROM employee e \n"
			+ "INNER JOIN job_role jb ON jb.job_role_id = e.job_role_id \n"
			+ "WHERE e.emp_id=:employeeId \n"
			+ "LIMIT 1 \n", nativeQuery = true)
	String getJobRoleByEmployeeId(Long employeeId);

	@Query(value = "SELECT e.empId, e.name, e.email, jr.name as jobrolename, e.mobileNo, em.name as manager, e.employeementId, \n"
			+ "e.invalidAccessAttempt, e.isTimesheetLockCheckEnable, e.employmentstatus, e.dateOfRelieving, e.pipFlag,p.pipId,e.isConsultant,e.isApprenticeship from Employee e \n"
			+ "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n"
			+ "LEFT JOIN PIP p ON p.pipId=e.pipId \n"
			+ "INNER JOIN Employee em ON em.empId =:empId \n"
			+ "WHERE e.employmentstatus not like 'InActive' AND ((e.managerId =:empId AND (e.approvalsTo = 'Manager' OR e.approvalsTo IS NULL)) OR (e.reportingManagerId =:empId AND e.approvalsTo = 'Reporting Manager'))")
	List<Object> findexample(@Param("empId") Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getTeamProjectMappingsByEmpId(Long empId );
	
	@Query(value="select new com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO( \n" +
			"e.empId,e.employeementId,e.name,e.jobRoleId,j.name ,d.deptId,d.name, e.isConsultant) \n" +
			"from Employee e \n" +
			"inner join JobRole j on j.jobRoleId = e.jobRoleId \n" +
			"inner join Department d on d.deptId = j.deptId \n" +
			"where e.empId =:empId " )
	public List<EmployeeDetailsForTeamMemberDTO> getEmployeeDetailsForTeam(Long empId );

    @Query(nativeQuery = true,value = "SELECT \n"
    		+ "    (SELECT COUNT(*) FROM employee_rewards WHERE rewarded_to = e.emp_id) AS rewardCount,\n"
    		+ "    (SELECT COUNT(*) FROM appreciation WHERE appreciation_to = e.emp_id) AS appreciationCount\n"
    		+ "FROM employee e\n"
    		+ "WHERE e.emp_id = :employeeId")
    List<Object[]> getRewardsAndAppreciationCount(@Param("employeeId") Long employeeId);
	
//    @Query(nativeQuery = true)
//    public String findHodMail(Long empId);
    
    
    @Query(value="select distinct h.email \n" +
			"from Employee e \n" +
			"inner join JobRole j on j.jobRoleId = e.jobRoleId \n" +
			"inner join Department d on d.deptId = j.deptId \n" +
			"inner join Employee h on h.empId = d.hodId \n" +
			"where e.empId =:empId ")
    public String findHodMail(Long empId);
  
    @Query(nativeQuery = true)
    public Optional<List<Object[]>> getAllEmployeesWorkAnniversaryToday();
    
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE employee SET billable_type = :billableType, billable = :billable, updated_by = :updatedBy, updated_on = :updatedOn WHERE emp_id = :empId", nativeQuery = true)
    int updateBillableInfo(@Param("empId") Long empId, @Param("billableType") String billableType, @Param("billable") String billable, @Param("updatedBy") Long updatedBy,
    	    @Param("updatedOn") String updatedOn);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE employee SET billable_type = :billableType, billable = :billable, updated_by = :updatedBy, updated_on = :updatedOn WHERE emp_id IN (:empIds)", nativeQuery = true)
    int updateBillableTypeForMultiple(@Param("empIds") List<Long> empIds,
                                      @Param("billableType") String billableType,
                                      @Param("billable") String billable,@Param("updatedBy") Long updatedBy,@Param("updatedOn") String updatedOn);
    
    @Query(value ="select DISTINCT emp_id from employee where employmentstatus !='Inactive'",nativeQuery=true)
    List<Long>findAllActiveEmployees();
    
//    @Query(value="SELECT new com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO(e.empId, e.name,  \n " + 
//			"CASE  \n " + 
//			"  WHEN isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)  \n " + 
//			"  ELSE CONCAT('A-', e.employeementId)  \n " + 
//			"END)  \n " + 
//			"FROM Employee e where e.employmentstatus != 'InActive' and e.empId not between 1 and 6")
//    public List<GetEmployeeByNameAndEmpldDTO> getEmployeeByNameAndEmpld();
    
    @Query(value=" SELECT new com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO(e.empId, e.name,  \n"
    		+ "            CASE  \n"
    		+ "              WHEN isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)  \n"
    		+ "              ELSE CONCAT('A-', e.employeementId)  \n"
    		+ "            END)  \n"
    		+ "            FROM Employee e  \n"
    		+ "            WHERE e.employmentstatus != 'InActive'  \n"
    		+ "            AND e.empId NOT BETWEEN 1 AND 6  \n"
    		+ "            ORDER BY e.name ASC")
    public List<GetEmployeeByNameAndEmpldDTO> getEmployeeByNameAndEmpld();
    
    
   
	
    @Query(value="select billable_type from employee where emp_id = :empId",nativeQuery=true)
    String findBillableTypeByEmpId(@Param("empId") Long empId);
    
    
    @Query(value = "select e.employeement_id,e.name,d.name as departmentName,d.hod_id from employee e inner join job_role jr on  jr.job_role_id = e.job_role_id inner join department d on d.dept_id = jr.dept_id where e.emp_id = :empId",nativeQuery=true)
    Object findEmployeeDepartmentDetails(@Param("empId") Long empId);
    
    @Query(value="SELECT e.name FROM employee e WHERE e.emp_id = :empId",nativeQuery=true)
    String findEmployeeNameById(@Param("empId") Long empId);
    
    @Query(value="SELECT e.email FROM employee e WHERE e.emp_id = :hodId",nativeQuery=true)
    String findHodEmailById(@Param("hodId") Long hodId);
    
    @Query(value ="select count(*) from Employee e where e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 and 6")
    Long getTotalEmployeeCount();
    
    @Query(value = "select count(*) from employee e \n"
    		+ "inner join job_role jr on jr.job_role_id = e.job_role_id \n"
    		+ "inner join department d on jr.dept_id = d.dept_id \n"
    		+ "LEFT JOIN (\n"
    		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
    		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
    		+ "			from employee e \n"
    		+ "			left join employee_leave el \n"
    		+ "				on el.emp_id = e.emp_id \n"
    		+ "				and leave_status_id in (1,2) \n"
    		+ "				and manager_approval_status = 'Approved' \n"
    		+ "				and leave_type_master_id = 5 \n"
    		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
    		+ "		) eld on eld.emp_id = e.emp_id"
    		+ " where e.employmentstatus != 'InActive' and d.dept_id IN :deptIds and e.emp_id NOT BETWEEN 1 AND 6 \n"
    		+ "AND ((:hideMaternityLeaveEmps = true) \n"
    		+ "			or \n"
    		+ "		(:hideMaternityLeaveEmps != true and eld.On_Maternity_Leave = 'No')\n"
    		+ "	)",nativeQuery = true)
    Long getTotalEmployeeCountInDepartments(@Param("deptIds") List<Long>deptIds,@Param("hideMaternityLeaveEmps") Boolean hideMaternityLeaveEmps);
    
    @Query("SELECT e FROM Employee e WHERE e.empId IN :empIds")
    List<Employee> findByEmpIdIn(@Param("empIds") Set<Long> empIds);
    @Query("SELECT e FROM Employee e WHERE e.empId IN :empIds")
    List<Employee> findByEmpIdIn(@Param("empIds") List<Long> empIds);
    
    
    @Query(value ="select new com.apmosys.employeeportal.dto.EmployeeDTO( e.empId,e.employeementId,e.email,e.employmentstatus,e.mobileNo,e.managerId,em.name,jr.name,d.name ,e.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) from Employee e  \n"
    		+ "inner join JobRole jr on jr.jobRoleId = e.jobRoleId \n"
    		+ "inner join Department d on d.deptId = jr.deptId \n"
    		+ "LEFT JOIN Employee em ON em.empId = e.managerId \n"
    		+ "WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n"
    		+ "                  JOIN Team t ON t.teamId = etm.teamId \n"
    		+ "                  JOIN Project p ON p.projectId = t.projectId \n"
    		+ "                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n"
    		+ " and e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 AND 6")
    List<EmployeeDTO> findAllEmployeesWithoutAnyProject();
    
    
    @Query("SELECT NEW com.apmosys.employeeportal.dto.EmployeeDTO(e.empId, e.employeementId, e.email, e.employmentstatus, e.mobileNo, e.managerId, em.name, jr.name, d.name, e.name, e.billableType,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) " +
    	       "FROM Employee e, JobRole jr, Department d " +
    	       "LEFT JOIN Employee em ON e.managerId = em.empId " +
    	       "WHERE e.jobRoleId = jr.jobRoleId AND jr.deptId = d.deptId " + 
    	       "AND e.billableType IS NULL " +
    	       "AND e.empId NOT BETWEEN 1 AND 6 " +
    	       "AND e.employmentstatus <> 'InActive'")
    	List<EmployeeDTO> findAllEmployeesWithoutAnyBillable();
    
    
    @Query("SELECT NEW com.apmosys.employeeportal.dto.EmployeeDTO( e.empId, e.employeementId, e.email, e.employmentstatus, e.mobileNo, e.managerId, em.name, jr.name, d.name, e.name, e.billableType,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) " +
            "FROM Employee e, JobRole jr, Department d " +
            "LEFT JOIN Employee em ON e.managerId = em.empId " + 
            "WHERE e.jobRoleId = jr.jobRoleId AND jr.deptId = d.deptId " + 
            "AND e.billableType IS NULL " +
            "AND e.empId NOT BETWEEN 1 AND 6 " +
            "AND e.employmentstatus != 'InActive' " +
            "AND d.deptId IN :deptIds")
     List<EmployeeDTO> findAllEmployeesWithoutAnyBillableInDeptIds(@Param("deptIds") List<Long> deptIds);

     @Query("SELECT NEW com.apmosys.employeeportal.dto.EmployeeDTO(e.empId, e.employeementId, e.email, e.employmentstatus, e.mobileNo, e.managerId, em.name, jr.name, d.name, e.name, e.billableType,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) " +
            "FROM Employee e, JobRole jr, Department d " +
            "LEFT JOIN Employee em ON e.managerId = em.empId " + 
            "WHERE e.jobRoleId = jr.jobRoleId AND jr.deptId = d.deptId " + 
            "AND e.billableType IS NULL " +
            "AND e.empId NOT BETWEEN 1 AND 6 " +
            "AND e.employmentstatus != 'InActive' " +
            "AND d.deptId = :deptId")
     List<EmployeeDTO> findAllEmployeesWithoutBillableInDeptId(@Param("deptId") Long deptId);
    
    @Query(value ="select e.emp_id,e.employeement_id,e.email,e.employmentstatus,e.mobile_no,e.manager_id,em.name as managerName,jr.name as jobrole,d.name as departmentName,e.name,e.billable_type,e.is_apmosys_product,e.is_apprenticeship from employee e \n"
    		+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
    		+ "inner join department d on d.dept_id = jr.dept_id\n"
    		+ "LEFT JOIN \n"
    		+ "    employee em ON em.emp_id = e.manager_id\n"
    		+ "LEFT JOIN (\n"
    		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
    		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
    		+ "			from employee e \n"
    		+ "			left join employee_leave el \n"
    		+ "				on el.emp_id = e.emp_id \n"
    		+ "				and leave_status_id in (1,2) \n"
    		+ "				and manager_approval_status = 'Approved' \n"
    		+ "				and leave_type_master_id = 5 \n"
    		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
    		+ "		) eld on eld.emp_id = e.emp_id \n"
    		+ "WHERE NOT EXISTS (SELECT 1\n"
    		+ "    FROM employee_team_mapping etm\n"
    		+ "    JOIN teams t ON t.team_id = etm.team_id\n"
    		+ "    JOIN projects p ON p.project_id = t.project_id\n"
    		+ "    WHERE etm.emp_id = e.emp_id \n"
    		+ "      AND etm.active != 0\n"
    		+ "      AND t.is_active = 'Y'\n"
    		+ "      AND p.active = 'true') and e.employmentstatus != 'InActive' and d.dept_id IN :departmentIds AND e.emp_id NOT BETWEEN 1 AND 6 \n"
    		+ "		 AND ((:hideMaternityLeaveEmps = true) \n"
    		+ "			or \n"
    		+ "		(:hideMaternityLeaveEmps != true and eld.On_Maternity_Leave = 'No')\n"
    		+ "	)",nativeQuery = true)
    List<Object[]> findAllEmployeesWithoutAnyProjectDepartmentWise(@Param("departmentIds") List<Long> departmentIds,@Param("hideMaternityLeaveEmps")Boolean hideMaternityLeaveEmps);
    
    @Query(value ="select new com.apmosys.employeeportal.dto.EmployeeDTO( e.empId,e.employeementId,e.email,e.employmentstatus,e.mobileNo,e.managerId,em.name,jr.name ,d.name,e.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) from Employee e  \n"
    		+ "inner join JobRole jr on jr.jobRoleId = e.jobRoleId \n"
    		+ "inner join Department d on d.deptId = jr.deptId \n"
    		+ "LEFT JOIN Employee em ON em.empId = e.managerId \n"
    		+ "WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n"
    		+ "                  JOIN Team t ON t.teamId = etm.teamId \n"
    		+ "                  JOIN Project p ON p.projectId = t.projectId \n"
    		+ "                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n"
    		+ "and e.employmentstatus != 'InActive' and d.deptId IN :deptIds") 
    List<EmployeeDTO> findAllEmployeesWithoutProjectInDeptIds(@Param("deptIds") List<Long> deptIds);
    
    
    @Query(value ="select new com.apmosys.employeeportal.dto.EmployeeDTO( e.empId,e.employeementId,e.email,e.employmentstatus,e.mobileNo,e.managerId,em.name,jr.name ,d.name,e.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) from Employee e  \n"
    		+ "inner join JobRole jr on jr.jobRoleId = e.jobRoleId \n"
    		+ "inner join Department d on d.deptId = jr.deptId \n"
    		+ "LEFT JOIN Employee em ON em.empId = e.managerId \n"
    		+ "WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n"
    		+ "                  JOIN Team t ON t.teamId = etm.teamId \n"
    		+ "                  JOIN Project p ON p.projectId = t.projectId \n"
    		+ "                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n"
    		+ "and e.employmentstatus != 'InActive' and d.deptId = :deptId ")
    List<EmployeeDTO> findAllEmployeesWithoutProjectInDeptId(@Param("deptId") Long deptId);
    
    
    
    @Query(nativeQuery = true ,value ="select p.project_id,p.project_name,t.team_id,t.team_name,t.dept_ids,rr.resource_overview_id, rr.department,rr.count,rr.experience, rr.role , po.po_id from projects p \n"
    		+ "    		inner join teams t on t.project_id = p.project_id  \n"
    		+ "    		left join resource_requirement rr on rr.project_id = p.project_id\n"
    		+ "         left join po_details po on p.project_id = po.project_id \n"
    		+ "         left join po_requirement_mapping prm on prm.po_id = po.po_id \n"
    		+ "    		where p.po_project_id  IS NULL \n"
    		+ "    		and p.internal_project_type = 'Bench' \n"
    		+ "    		and p.active = 'true' and t.is_active = 'Y' ")
    List<Object[]> getAllInternalBenchprojectsAndTeamDetailsForDepartmenFilter();
    
    
    @Query(nativeQuery = true,value ="select p.project_id,p.project_name,t.team_id,t.team_name,t.dept_ids, \n"
    		+ "rr.resource_overview_id, rr.department,rr.count,rr.experience, rr.role , po.po_id\n"
    		+ "from projects p \n"
    		+ "inner join teams t on t.project_id = p.project_id  \n"
    		+ "left join resource_requirement rr on rr.project_id = p.project_id\n"
    		+ "left join po_details po on p.project_id = po.project_id \n"
    		+ "left join po_requirement_mapping prm on prm.po_id = po.po_id \n"
    		+ "where p.internal_project_type = 'InternalRNDProducts' or p.internal_project_type IS NULL ")
    List<Object[]> getAllProjectsThatAreNotBench();
    
    
//    @Modifying
//    @Transactional
//    @Query(nativeQuery = true,value ="update employee set billable = :billable,billable_type = :billableType where emp_id = :empId") 
//    void updateBillableFields(@Param("empId") Long empId, @Param("billable") String billable, @Param("billableType") String billableType);
       
    
    @Modifying
    @Transactional
    @Query(value ="update Employee set billable =:billable,billableType =:billableType where empId =:empId") 
    void updateBillableFields(@Param("empId") Long empId, @Param("billable") String billable, @Param("billableType") String billableType);

    
    @Query(nativeQuery = true, value="SELECT \n"
    		+ "    e.employeement_id, e.aadhar, e.about_me, e.address, e.bank_account_no, e.bankifsccode,\n"
    		+ "    e.bank_name, e.blood_group, e.city, e.country, e.created_by, e.created_on, e.date_of_birth,\n"
    		+ "    e.date_of_joining, e.email, e.emergency_contact_mobile, e.emergency_contact_person,\n"
    		+ "    e.employmentstatus, e.esic_number, e.father_name, e.gender, e.graduation_type, e.pursuing,\n"
    		+ "    e.job_role_id, e.landline, e.manager_id, e.marital_status, e.mobile_no, e.mother_tongue, e.name,\n"
    		+ "    e.notice_period, e.alternate_mobile_no, e.pan_number, e.passport_number,\n"
    		+ "    e.permanent_address, e.pf_account_number, e.pincode, e.place_of_birth, e.passing_grade,\n"
    		+ "    e.previous_pf_account_number, e.relation, e.state, e.uan,\n"
    		+ "    e.views_on_organisation, e.year_of_passing,\n"
    		+ "    jr.dept_id, jr.name AS jobrolename,\n"
    		+ "    d.name AS departmentname, e.work_location, e.probation_period, e.emp_id, e2.name AS manager, e.experience,\n"
    		+ "    e.billable, e.child1, e.child2, e.child3, e.mothers_name, e.spouse, e.total_experience, e.date_of_resign,\n"
    		+ "    e.invalid_access_attempt, e.date_of_relieving, jr.name AS JobRolename, e3.name AS updatedByName, \n"
    		+ "    e4.name AS createdByName, e.updated_on, e.is_timesheet_lock_check_enable, e.employment_release_status, \n"
    		+ "    e.pip_flag, p.pip_id, e.billable_type,\n"
    		+ "    emp_proj_client.project_name, emp_proj_client.client_name, emp_proj_client.team_name, \n"
    		+ "    des.designation_name, e.is_consultant, e.is_apprenticeship, e.reporting_manager_id, \n"
    		+ "    e5.name AS reportingManger, jr.employee_role, e.refered_type, e.refered_name, \n"
    		+ "    e.employee_confirmation_date, d.hod_id, e7.name AS hodName, d.name AS hodDepartmentName,\n"
    		+ "    emp_proj_client.project_id, e.updated_by,e.is_apmosys_product\n"
    		+ "\n"
    		+ "FROM employee e\n"
    		+ "\n"
    		+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
    		+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
    		+ "\n"
    		+ "LEFT JOIN employee e3 ON e.updated_by = e3.emp_id\n"
    		+ "LEFT JOIN employee e4 ON e.created_by = e4.emp_id\n"
    		+ "LEFT JOIN pip p ON p.pip_id = e.pip_id\n"
    		+ "\n"
    		+ "LEFT JOIN (\n"
    		+ "    SELECT \n"
    		+ "        etm.emp_id,\n"
    		+ "        GROUP_CONCAT(DISTINCT pr.project_id ORDER BY pr.project_id SEPARATOR ',') AS project_id,\n"
    		+ "        GROUP_CONCAT(DISTINCT pr.project_name ORDER BY pr.project_id SEPARATOR ',') AS project_name,\n"
    		+ "        GROUP_CONCAT(DISTINCT cl.client_name) AS client_name,\n"
    		+ "        GROUP_CONCAT(DISTINCT t.team_name) AS team_name\n"
    		+ "    FROM employee_team_mapping etm\n"
    		+ "    LEFT JOIN teams t ON t.team_id = etm.team_id\n"
    		+ "    LEFT JOIN projects pr ON pr.project_id = t.project_id\n"
    		+ "    LEFT JOIN clients cl ON cl.client_id = pr.client_id\n"
    		+ "    WHERE etm.active != 0 \n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND pr.active != 'false'\n"
    		+ "    GROUP BY etm.emp_id\n"
    		+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id\n"
    		+ "\n"
    		+ "INNER JOIN employee e2 ON e.manager_id = e2.emp_id\n"
    		+ "INNER JOIN employee e7 ON d.hod_id = e7.emp_id\n"
    		+ "LEFT JOIN employee e5 ON e.reporting_manager_id = e5.emp_id\n"
    		+ "LEFT JOIN designation des ON des.designation_id = e.designation_id\n"
    		+ "\n"
    		+ "WHERE d.dept_id IN (:deptIds)\n"
    		+ "\n"
    		+ "ORDER BY e.name;")
    public List<Object[]> getAllEmployeesBasedOnUserLogined(@Param("deptIds") List<Integer> deptIds);
    
    @Query("SELECT e FROM Employee e WHERE e.empId = :empId AND LOWER(e.employmentstatus) != LOWER(:statusToExclude)")
    Optional<Employee> findByIdAndStatusNot(@Param("empId") Long empId, @Param("statusToExclude") String statusToExclude);
    
    @Query(nativeQuery = true,value = "select d.name from employee e inner join job_role j on e.job_role_id = j.job_role_id inner join department d on d.dept_id = j.dept_id where e.emp_id = :empId")
    public String getDepartment(@Param("empId") Long empId );
    
    @Query(nativeQuery = true,value ="select email from employee where emp_id = :empId")
    public String getMailByEmpId(@Param("empId") Long empId );
    
    @Query(nativeQuery = true,value = "select d.dept_id from employee e inner join job_role j on e.job_role_id = j.job_role_id inner join department d on d.dept_id = j.dept_id where e.emp_id = :empId")
    public Object[] getDepartmentRow(Long empId );
    
    @Query(nativeQuery = true,value = "select d.hod_id from employee e inner join job_role j on e.job_role_id = j.job_role_id inner join department d on d.dept_id = j.dept_id where e.emp_id = :empId")
    public Long getDepartmentHod(@Param("empId") Long empId );
    
    
    @Query(nativeQuery = true,value = "SELECT e.* \n"
    		+ "FROM employee e \n"
    		+ "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id \n"
    		+ "INNER JOIN department d ON jr.dept_id = d.dept_id \n"
    		+ "INNER JOIN employee hod ON d.hod_id = hod.emp_id \n"
    		+ "WHERE e.employmentstatus NOT LIKE 'InActive' AND DATEDIFF(CURDATE(), e.date_of_joining) = e.probation_period and e.is_confirmed_clicked = 1 and e.employmentstatus = 'Probation'")
    public List<Employee> getEmployeeProbationAndIsClicked();
    
    
    @Query(nativeQuery = true,value = "select j.employee_role from employee e inner join job_role j on j.job_role_id = e.job_role_id where e.emp_id = :empId")
    public String getEmployeeRoleByEmpId(@Param("empId") Long empId);
    
//    @Query("UPDATE employee SET long_overdue_notified = :status WHERE emp_id = :empId")
//    public void updateLongOverdueNotified1(@Param("empId") Long empId, @Param("status") boolean status);



	// ========== BACKUP: Original query renamed with _old suffix ==========
	// @Query(value = "select new com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse(p.poNo, p.poProjectId, p.projectId, p.projectName, e.empId, e.employeementId, e.name, e.billableType, d.name, jr.name " +
	// 		",(select count(Ts) from Timesheet Ts where Ts.date between :startDate and :endDate and Ts.empId = e.empId) " +
	// 		",etm.active) " +
	// 		"from Employee e " +
	// 		"left join EmployeeTeamMap etm on etm.empId = e.empId " +
	// 		"left join Team t on t.teamId = etm.teamId " +
	// 		"left join JobRole jr on jr.jobRoleId = e.jobRoleId " +
	// 		"left join Project p on p.projectId = t.projectId " +
	// 		"left join Department d on d.deptId = jr.deptId " +
	// 		"where ((e.employmentstatus != 'InActive') OR e.dateOfRelieving between :startDate and :endDate) and ((:listType = 'Billable' AND e.billableType in ('TNM','Fixed Cost')) " +
	// 		"and e.empId not between 1 and 6 " +
	// 		"or (:listType = 'Non-Billable' and e.billableType in('InternalRNDProducts','Bench','Shadow')))")
	// List<EmployeeTimesheetProjectResponse> findEmployeeAndTimesheetDetailsWithoutPagination_old(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,@Param("listType") String listType);

	// ========== UPDATED: New query using _new entities ==========
	// @Query(value = "select new com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse(p.poNo, p.poProjectId, p.projectId, p.projectName, e.empId, e.employeementId, e.name, e.billableType, d.name, jr.name " +
	// 		",(select count(etn) from EmployeeTimesheetsNew etn where etn.date between :startDate and :endDate and etn.empId = e.empId) " +
	// 		",etm.active) " +
	// 		"from Employee e " +
	// 		"left join EmployeeTeamMap etm on etm.empId = e.empId " +
	// 		"left join Team t on t.teamId = etm.teamId " +
	// 		"left join JobRole jr on jr.jobRoleId = e.jobRoleId " +
	// 		"left join Project p on p.projectId = t.projectId " +
	// 		"left join Department d on d.deptId = jr.deptId " +
	// 		"where ((e.employmentstatus != 'InActive') OR e.dateOfRelieving between :startDate and :endDate) and ((:listType = 'Billable' AND e.billableType in ('TNM','Fixed Cost')) " +
	// 		"and e.empId not between 1 and 6 " +
	// 		"or (:listType = 'Non-Billable' and e.billableType in('InternalRNDProducts','Bench','Shadow')))")
	// List<EmployeeTimesheetProjectResponse> findEmployeeAndTimesheetDetailsWithoutPagination(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,@Param("listType") String listType);

	// @Query(value = "select new com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse(p.poNo, p.poProjectId, p.projectId, p.projectName, e.empId, e.employeementId, e.name, e.billableType, d.name, jr.name " +
	// 		",(select count(Ts) from Timesheet Ts where Ts.date between :startDate and :endDate and Ts.empId = e.empId) " +
	// 		",etm.active) " +
	// 		"from Employee e " +
	// 		"left join EmployeeTeamMap etm on etm.empId = e.empId " +
	// 		"left join Team t on t.teamId = etm.teamId " +
	// 		"left join JobRole jr on jr.jobRoleId = e.jobRoleId " +
	// 		"left join Project p on p.projectId = t.projectId " +
	// 		"left join Department d on d.deptId = jr.deptId " +
	// 		"where ((e.employmentstatus != 'InActive') OR e.dateOfRelieving between :startDate and :endDate) and ((:listType = 'Billable' AND e.billableType in ('TNM','Fixed Cost')) " +
	// 		"and e.empId not between 1 and 6 " +
	// 		"or (:listType = 'Non-Billable' and e.billableType in('InternalRNDProducts','Bench','Shadow')))")
	// List<EmployeeTimesheetProjectResponse> findEmployeeAndTimesheetDetailsWithoutPagination(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,@Param("listType") String listType);

    
    @Query(value = "WITH AllPoProjectTypes AS (\n"
    		+ "    SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
    		+ "),\n"
    		+ "AllBillableTypes AS (\n"
    		+ "    SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
    		+ "),\n"
    		+ "AllCombinations AS (\n"
    		+ "    SELECT apt.po_project_type, abt.billable_type\n"
    		+ "    FROM AllPoProjectTypes apt\n"
    		+ "    CROSS JOIN AllBillableTypes abt\n"
    		+ "),\n"
    		+ "MainAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    		+ "        e.billable_type,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '7' DAY THEN e.emp_id ELSE NULL END) AS total_emp_last_7_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '30' DAY THEN e.emp_id ELSE NULL END) AS total_emp_last_30_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '90' DAY THEN e.emp_id ELSE NULL END) AS total_emp_last_90_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '180' DAY THEN e.emp_id ELSE NULL END) AS total_emp_last_180_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '1' YEAR THEN e.emp_id ELSE NULL END) AS total_emp_last_1_year\n"
    		+ "    FROM projects p\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE\n"
    		+ "      AND p.end_date >= CURRENT_DATE - INTERVAL '1' YEAR \n"
    		+ "      AND d.dept_id IN (:deptId)\n"
    		+ "    GROUP BY\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END,\n"
    		+ "        e.billable_type\n"
    		+ "),\n"
    		+ "OverallAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    		+ "        COUNT(DISTINCT e.emp_id) AS overall_employee\n"
    		+ "    FROM projects p\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE \n"
    		+ "      AND d.dept_id IN (:deptId)\n"
    		+ "    GROUP BY\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END\n"
    		+ "),\n"
    		+ "OverallPerBillableAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    		+ "        e.billable_type,\n"
    		+ "        COUNT(DISTINCT e.emp_id) AS overall_emp_per_billable_type\n"
    		+ "    FROM projects p\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE \n"
    		+ "      AND d.dept_id IN (:deptId) \n"
    		+ "    GROUP BY\n"
    		+ "        CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END,\n"
    		+ "        e.billable_type\n"
    		+ ")\n"
    		+ "SELECT\n"
    		+ "    ac.po_project_type,\n"
    		+ "    ac.billable_type,\n"
    		+ "    \n"
    		+ "    COALESCE(oa.overall_employee, 0) AS total_emp,\n"
    		+ "    COALESCE(opba.overall_emp_per_billable_type, 0) AS overall_emp_per_billable_type,\n"
    		+ "\n"
    		+ "    COALESCE(ma.total_emp_last_7_days, 0) AS total_emp_last_7_days,\n"
    		+ "    COALESCE(ma.total_emp_last_30_days, 0) AS total_emp_last_30_days,\n"
    		+ "    COALESCE(ma.total_emp_last_90_days, 0) AS total_emp_last_90_days,\n"
    		+ "    COALESCE(ma.total_emp_last_180_days, 0) AS total_emp_last_180_days,\n"
    		+ "    COALESCE(ma.total_emp_last_1_year, 0) AS total_emp_last_1_year\n"
    		+ "\n"
    		+ "FROM AllCombinations ac\n"
    		+ "LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
    		+ "LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
    		+ "LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
    		+ "WHERE ac.po_project_type = :po_project_typee AND ac.billable_type = 'TNM'\n"
    		+ "ORDER BY\n"
    		+ "    ac.po_project_type, ac.billable_type", 
           nativeQuery = true)
    List<Object[]> fetchInactivePOCounts(@Param("po_project_typee") String po_project_typee,  @Param("deptId") List<Long> deptId);
    
    @Query(value = "SELECT\n"
    		+ "    COUNT(DISTINCT CASE WHEN e.billable_type = 'TNM' THEN e.emp_id ELSE NULL END) AS tnm\n"
    		+ "FROM projects p\n"
    		+ "INNER JOIN teams t ON p.project_id = t.project_id\n"
    		+ "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
    		+ "INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
    		+ "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
    		+ "INNER JOIN department d ON jr.dept_id = d.dept_id\n"
    		+ "LEFT JOIN (\n"
    		+ "            SELECT DISTINCT el_e.emp_id AS emp_id,\n"
    		+ "                            CASE WHEN el.emp_id IS NULL THEN 'No' ELSE 'Yes' END AS On_Maternity_Leave\n"
    		+ "            FROM employee el_e\n"
    		+ "            LEFT JOIN employee_leave el\n"
    		+ "                ON el.emp_id = el_e.emp_id\n"
    		+ "                AND leave_status_id IN (1,2)\n"
    		+ "                AND manager_approval_status = 'Approved'\n"
    		+ "                AND leave_type_master_id = 5\n"
    		+ "                AND CURDATE() BETWEEN DATE(el.from_date) AND DATE(el.to_date)\n"
    		+ "        ) eld ON eld.emp_id = e.emp_id\n"
    		+ "WHERE p.active = 'true'\n"
    		+ "    AND t.is_active = 'Y'\n"
    		+ "    AND e.employmentstatus != 'InActive'\n"
    		+ "    AND etm.active != 0\n"
    		+ "    AND (d.dept_id IN (:deptIds)) \n"
    		+ "    AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "    AND ((:leave_filter = TRUE)\n"
    		+ "            OR\n"
    		+ "        (:leave_filter != TRUE AND eld.On_Maternity_Leave = 'No')\n"
    		+ "    )\n"
    		+ "    AND (\n"
    		+ "        (:poProjectType IS NULL AND COALESCE(p.po_project_type, 'Internal') = 'Internal')\n"
    		+ "        OR (:poProjectType = COALESCE(p.po_project_type, 'Internal'))\n"
    		+ "    )\n"
    		+ "	AND date(p.end_date) between date:fromDate and date:toDate \n"
    		+ " GROUP BY COALESCE(p.po_project_type, 'Internal') ", nativeQuery = true)
    	Integer fetchInactivePOCountsNew(@Param("poProjectType") String po_project_type,
    	                                        @Param("deptIds") List<Long> deptIds,
    	                                        @Param("fromDate") String fromDate,
    	                                        @Param("toDate") String toDate,
    	                                        @Param("leave_filter") boolean maternityleaveFilter);
    
    @Query(value = "WITH AllPoProjectTypes AS (\n"
    		+ "    SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
    		+ "),\n"
    		+ "AllBillableTypes AS (\n"
    		+ "    SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
    		+ "),\n"
    		+ "AllCombinations AS (\n"
    		+ "    SELECT apt.po_project_type, abt.billable_type\n"
    		+ "    FROM AllPoProjectTypes apt\n"
    		+ "    CROSS JOIN AllBillableTypes abt\n"
    		+ "),\n"
    		+ "MainAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    		+ "        e.billable_type,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '7' DAY THEN p.po_project_id END) AS total_projects_last_7_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '30' DAY THEN p.po_project_id END) AS total_projects_last_30_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '90' DAY THEN p.po_project_id END) AS total_projects_last_90_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '180' DAY THEN p.po_project_id END) AS total_projects_last_180_days,\n"
    		+ "        COUNT(DISTINCT CASE WHEN p.end_date >= CURRENT_DATE - INTERVAL '1' YEAR THEN p.po_project_id END) AS total_projects_last_1_year\n"
    		+ "    FROM projects p\n"
    		+ "    JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE\n"
    		+ "      AND p.end_date >= CURRENT_DATE - INTERVAL '1' YEAR\n"
    		+ "      AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptId))\n"
    		+ "    GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    		+ "),\n"
    		+ "OverallAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    		+ "        COUNT(DISTINCT p.po_project_id) AS overall_pos\n"
    		+ "    FROM projects p\n"
    		+ "    JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE\n"
    		+ "      AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptId))\n"
    		+ "    GROUP BY COALESCE(p.po_project_type, 'Internal')\n"
    		+ "),\n"
    		+ "OverallPerBillableAgg AS (\n"
    		+ "    SELECT\n"
    		+ "        COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    		+ "        e.billable_type,\n"
    		+ "        COUNT(DISTINCT p.po_project_id) AS overall_projects_per_billable_type\n"
    		+ "    FROM projects p\n"
    		+ "    JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    JOIN employee e ON etm.emp_id = e.emp_id\n"
    		+ "    JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    		+ "    JOIN department d ON d.dept_id = j.dept_id\n"
    		+ "    WHERE etm.active != 0\n"
    		+ "      AND t.is_active != 'N'\n"
    		+ "      AND p.active != 'false'\n"
    		+ "      AND e.emp_id NOT BETWEEN 1 AND 6\n"
    		+ "      AND p.po_project_type = :po_project_typee\n"
    		+ "      AND p.end_date < CURRENT_DATE\n"
    		+ "      AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptId))\n"
    		+ "    GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    		+ ")\n"
    		+ "SELECT DISTINCT \n"
    		+ "    ac.po_project_type,\n"
    		+ "    ac.billable_type,\n"
    		+ "    COALESCE(oa.overall_pos, 0) AS total_expired_pos,\n"
    		+ "    COALESCE(opba.overall_projects_per_billable_type, 0) AS overall_projects_per_billable_type,\n"
    		+ "    COALESCE(ma.total_projects_last_7_days, 0) AS total_projects_last_7_days,\n"
    		+ "    COALESCE(ma.total_projects_last_30_days, 0) AS total_projects_last_30_days,\n"
    		+ "    COALESCE(ma.total_projects_last_90_days, 0) AS total_projects_last_90_days,\n"
    		+ "    COALESCE(ma.total_projects_last_180_days, 0) AS total_projects_last_180_days,\n"
    		+ "    COALESCE(ma.total_projects_last_1_year, 0) AS total_projects_last_1_year\n"
    		+ "FROM AllCombinations ac\n"
    		+ "LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
    		+ "LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
    		+ "LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
    		+ "WHERE ac.po_project_type = :po_project_typee AND ac.billable_type = 'TNM'\n"
    		+ "ORDER BY ac.po_project_type, ac.billable_type;\n"
    		+ "", 
           nativeQuery = true)
    List<Object[]> fetchInactivePOCountsForProject(@Param("po_project_typee") String po_project_typee, @Param("deptId") List<Long> deptId);
    
    
    // @Query(nativeQuery = true,value = "WITH AllPoProjectTypes AS (\n"
    // 		+ "    		     SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllBillableTypes AS (\n"
    // 		+ "    		     SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllCombinations AS (\n"
    // 		+ "    		     SELECT apt.po_project_type, abt.billable_type\n"
    // 		+ "    		     FROM AllPoProjectTypes apt\n"
    // 		+ "    		     CROSS JOIN AllBillableTypes abt\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 MainAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    // 		+ "    		         e.billable_type,count(distinct e.emp_id) total_emp\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     INNER JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     INNER JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		         AND p.po_project_type = :po_project_typee\n"
    // 		+ "    		       AND p.po_end_date >= CURRENT_DATE\n"
    // 		+ "					  and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		         AND d.dept_id IN (:deptId)\n"
    // 		+ "                   \n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    // 		+ "    		         COUNT(DISTINCT e.emp_id) AS overall_employee\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     INNER JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     INNER JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		          AND p.po_project_type = :po_project_typee\n"
    // 		+ "    		       AND p.po_end_date >= CURRENT_DATE \n"
    // 		+ "				    and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "   		          AND d.dept_id IN (:deptId)\n"
    // 		+ "    		     GROUP BY\n"
    // 		+ "    		         CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallPerBillableAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type,\n"
    // 		+ "    		         e.billable_type,\n"
    // 		+ "    		         COUNT(DISTINCT e.emp_id) AS overall_emp_per_billable_type\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     INNER JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     INNER JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		         AND p.po_project_type = :po_project_typee\n"
    // 		+ "				    and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		       AND p.po_end_date >= CURRENT_DATE \n"
    // 		+ "    		         AND d.dept_id IN (:deptId) \n"
    // 		+ "    		     GROUP BY\n"
    // 		+ "    		         CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END,\n"
    // 		+ "    		         e.billable_type\n"
    // 		+ "    		 )\n"
    // 		+ "    		 SELECT\n"
    // 		+ "    		     ac.po_project_type,\n"
    // 		+ "    		     ac.billable_type,\n"
    // 		+ "    		     \n"
    // 		+ "    		     COALESCE(oa.overall_employee, 0) AS total_emp,\n"
    // 		+ "    		     COALESCE(opba.overall_emp_per_billable_type, 0) AS overall_emp_per_billable_type\n"
    // 		+ "    		 FROM AllCombinations ac\n"
    // 		+ "    		 LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
    // 		+ "    		 LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
    // 		+ "    		 LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
    // 		+ "    		 WHERE 1=1\n"
    // 		+ "               and ac.po_project_type = :po_project_typee \n"
    // 		+ "             AND ac.billable_type = 'TNM'\n"
    // 		+ "    		 ORDER BY\n"
    // 		+ "    		     ac.po_project_type, ac.billable_type")
    // List<Object[]> fetchactivePOCountsNew(@Param("po_project_typee") String po_project_typee,
    //         @Param("deptId") List<Long> deptId,
    //         @Param("fromDate") String fromDate,
    //         @Param("toDate") String toDate);
    
    
    
    
    // @Query(value = "WITH AllPoProjectTypes AS (\n"
    // 		+ "    		     SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllBillableTypes AS (\n"
    // 		+ "    		     SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllCombinations AS (\n"
    // 		+ "    		     SELECT apt.po_project_type, abt.billable_type\n"
    // 		+ "    		     FROM AllPoProjectTypes apt\n"
    // 		+ "    		     CROSS JOIN AllBillableTypes abt\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 MainAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         e.billable_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) as total_projects\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "                     AND p.po_end_date >= CURRENT_DATE\n"
    // 		+ "				 	 and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_pos\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "                     AND p.po_end_date >= CURRENT_DATE\n"
    // 		+ "				 	 and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ " 		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal')\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallPerBillableAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         e.billable_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_projects_per_billable_type\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "    		         AND p.po_end_date >= CURRENT_DATE\n"
    // 		+ "					  and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		        AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    // 		+ "    		 )\n"
    // 		+ "    		 SELECT DISTINCT \n"
    // 		+ "    		     ac.po_project_type,\n"
    // 		+ "    		     ac.billable_type,\n"
    // 		+ "    		     COALESCE(oa.overall_pos, 0) AS total_expired_pos,\n"
    // 		+ "    		     COALESCE(opba.overall_projects_per_billable_type, 0) AS overall_projects_per_billable_type,\n"
    // 		+ "    		     COALESCE(ma.total_projects, 0) AS total_projects\n"
    // 		+ "    		 FROM AllCombinations ac\n"
    // 		+ "    		 LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
    // 		+ "    		 LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
    // 		+ "    		 LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
    // 		+ "    		 WHERE 1=1\n"
    // 		+ "              and ac.po_project_type = :po_project_typee \n"
    // 		+ "             AND ac.billable_type = 'TNM'\n"
    // 		+ "    		 ORDER BY ac.po_project_type, ac.billable_type;" , nativeQuery = true)
    // List<Object[]> fetchactivePOCountsForProjectNew(@Param("po_project_typee") String po_project_typee,@Param("deptIds") List<Long> deptIds,
    //         @Param("fromDate") String fromDate,
    //         @Param("toDate") String toDate);
    
    
    // @Query(value = "WITH AllPoProjectTypes AS (\n"
    // 		+ "    		     SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllBillableTypes AS (\n"
    // 		+ "    		     SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 AllCombinations AS (\n"
    // 		+ "    		     SELECT apt.po_project_type, abt.billable_type\n"
    // 		+ "    		     FROM AllPoProjectTypes apt\n"
    // 		+ "    		     CROSS JOIN AllBillableTypes abt\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 MainAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         e.billable_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) as total_projects\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "                     AND p.po_end_date < CURRENT_DATE\n"
    // 		+ "				 	 and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_pos\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "                     AND p.po_end_date < CURRENT_DATE\n"
    // 		+ "				 	 and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ " 		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal')\n"
    // 		+ "    		 ),\n"
    // 		+ "    		 OverallPerBillableAgg AS (\n"
    // 		+ "    		     SELECT\n"
    // 		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
    // 		+ "    		         e.billable_type,\n"
    // 		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_projects_per_billable_type\n"
    // 		+ "    		     FROM projects p\n"
    // 		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
    // 		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    // 		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
    // 		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
    // 		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
    // 		+ "    		     WHERE etm.active != 0\n"
    // 		+ "    		       AND t.is_active != 'N'\n"
    // 		+ "    		       AND p.active != 'false'\n"
    // 		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
    // 		+ "    		        AND p.po_project_type = :po_project_typee\n"
    // 		+ "    		         AND p.po_end_date < CURRENT_DATE\n"
    // 		+ "					  and DATE(p.po_end_date) between :fromDate and :toDate\n"
    // 		+ "    		        AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
    // 		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
    // 		+ "    		 )\n"
    // 		+ "    		 SELECT DISTINCT \n"
    // 		+ "    		     ac.po_project_type,\n"
    // 		+ "    		     ac.billable_type,\n"
    // 		+ "    		     COALESCE(oa.overall_pos, 0) AS total_expired_pos,\n"
    // 		+ "    		     COALESCE(opba.overall_projects_per_billable_type, 0) AS overall_projects_per_billable_type,\n"
    // 		+ "    		     COALESCE(ma.total_projects, 0) AS total_projects\n"
    // 		+ "    		 FROM AllCombinations ac\n"
    // 		+ "    		 LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
    // 		+ "    		 LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
    // 		+ "    		 LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
    // 		+ "    		 WHERE 1=1\n"
    // 		+ "              and ac.po_project_type = :po_project_typee \n"
    // 		+ "             AND ac.billable_type = 'TNM'\n"
    // 		+ "    		 ORDER BY ac.po_project_type, ac.billable_type;" , nativeQuery = true)
    // List<Object[]> fetchInactivePOCountsForProjectNew(@Param("po_project_typee") String po_project_typee,@Param("deptIds") List<Long> deptIds,
    //         @Param("fromDate") String fromDate,
    //         @Param("toDate") String toDate);


    @Query(value="SELECT e.employeement_id, e.name, \n"
    		+ " e.billable, e.billable_type, \n"
    		+ " emp_proj_client.project_name, emp_proj_client.start_date, emp_proj_client.end_date,\n"
    		+ " emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, \n"
    		+ " d.name as departmentName, emp_proj_client.po_project_type, \n"
    		+ " j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id,\n"
    		+ " emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, \n"
    		+ " emp_proj_client.effective_end_date FROM employee e\n"
    		+ " INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
    		+ " INNER JOIN department d ON d.dept_id = j.dept_id\n"
    		+ " INNER JOIN employee m ON e.manager_id = m.emp_id\n"
    		+ " LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id\n"
    		+ " LEFT JOIN (     \n"
    		+ " SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
    		+ " GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
    		+ " GROUP_CONCAT(DISTINCT p.start_date ORDER BY p.project_id) AS start_date, \n"
    		+ " GROUP_CONCAT(DISTINCT p.end_date ORDER BY p.project_id) AS end_date,  \n"
    		+ " GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no,  \n"
    		+ " GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type,\n"
    		+ " GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
    		+ " GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location,\n"
    		+ " GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,  \n"
    		+ " GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id, \n"
    		+ " GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id, \n"
    		+ " GROUP_CONCAT(DISTINCT p.clientrm ORDER BY p.project_id) AS clientrm, \n"
    		+ " GROUP_CONCAT(DISTINCT p.apmosysrm ORDER BY p.project_id) AS apmosysrm,\n"
    		+ " GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date, \n"
    		+ " GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date \n"
    		+ " FROM employee_team_mapping etm     \n"
    		+ " LEFT JOIN teams t ON t.team_id = etm.team_id \n"
    		+ " LEFT JOIN projects p ON p.project_id = t.project_id \n"
    		+ " LEFT JOIN clients c ON c.client_id = p.client_id\n"
    		+ " LEFT JOIN client_locations cl ON cl.client_id = c.client_id \n"
    		+ " WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
    		+ " GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id\n"
    		+ " WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
    		+ " and emp_proj_client.po_project_type = :poProjectType\n"
    		+ " AND emp_proj_client.end_date < CURRENT_DATE\n"
    		+ "       AND (:days IS NULL OR emp_proj_client.end_date >= CURRENT_DATE - INTERVAL :days DAY)\n"
    		+ " and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
    		+ " AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptId))",nativeQuery = true)
	public List<Object[]> fetchInActivePOListOfEmployee(
		        @Param("poProjectType") String poProjectType,
		        @Param("days") Integer days,
		        @Param("deptId") List<Long> deptId);
	
	// @Query(value = "SELECT e.employeement_id, e.name, \n"
	// 		+ "    		 e.billable, e.billable_type, \n"
	// 		+ "    		 emp_proj_client.project_name, emp_proj_client.po_start_date, emp_proj_client.po_end_date,\n"
	// 		+ "    		 emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, \n"
	// 		+ "    		 d.name as departmentName, emp_proj_client.po_project_type, \n"
	// 		+ "    		 j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id,\n"
	// 		+ "    		 emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, \n"
	// 		+ "    		 emp_proj_client.effective_end_date FROM employee e\n"
	// 		+ "    		 INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
	// 		+ "    		 INNER JOIN department d ON d.dept_id = j.dept_id\n"
	// 		+ "    		 INNER JOIN employee m ON e.manager_id = m.emp_id\n"
	// 		+ "    		 LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id\n"
	// 		+ "    		 LEFT JOIN (     \n"
	// 		+ "    		 SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_start_date ORDER BY p.project_id) AS po_start_date, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_end_date ORDER BY p.project_id) AS po_end_date,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.clientrm ORDER BY p.project_id) AS clientrm, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.apmosysrm ORDER BY p.project_id) AS apmosysrm,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date \n"
	// 		+ "    		 FROM employee_team_mapping etm     \n"
	// 		+ "    		 LEFT JOIN teams t ON t.team_id = etm.team_id \n"
	// 		+ "    		 LEFT JOIN projects p ON p.project_id = t.project_id \n"
	// 		+ "    		 LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// 		+ "    		 LEFT JOIN client_locations cl ON cl.client_id = p.client_id\n"
	// 		+ "    		 WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
	// 		+ "    		 GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id\n"
	// 		+ "    		 WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
	// 		+ "    		 and emp_proj_client.po_project_type = :poProjectType\n"
	// 		+ "    		 AND emp_proj_client.po_end_date < CURRENT_DATE\n"
	// 		+ "    		 AND date(emp_proj_client.po_end_date) between :fromDate and :toDate\n"
	// 		+ "    		 and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
	// 		+ "    		 AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))", nativeQuery = true)
	// public List<Object[]> fetchInactivePOListOfEmployeeNew(@Param("poProjectType") String poProjectType,
	// 		@Param("deptIds") List<Long> deptIds,
	// 		@Param("fromDate") String fromDate,
	// 		@Param("toDate") String toDate);

	// @Query(value = "SELECT e.employeement_id, e.name, \n"
	// 		+ "    		 e.billable, e.billable_type, \n"
	// 		+ "    		 emp_proj_client.project_name, emp_proj_client.po_start_date, emp_proj_client.po_end_date,\n"
	// 		+ "    		 emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, \n"
	// 		+ "    		 d.name as departmentName, emp_proj_client.po_project_type, \n"
	// 		+ "    		 j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id,\n"
	// 		+ "    		 emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, \n"
	// 		+ "    		 emp_proj_client.effective_end_date FROM employee e\n"
	// 		+ "    		 INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
	// 		+ "    		 INNER JOIN department d ON d.dept_id = j.dept_id\n"
	// 		+ "    		 INNER JOIN employee m ON e.manager_id = m.emp_id\n"
	// 		+ "    		 LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id\n"
	// 		+ "    		 LEFT JOIN (     \n"
	// 		+ "    		 SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_start_date ORDER BY p.project_id) AS po_start_date, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_end_date ORDER BY p.project_id) AS po_end_date,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,  \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.clientrm ORDER BY p.project_id) AS clientrm, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT p.apmosysrm ORDER BY p.project_id) AS apmosysrm,\n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date, \n"
	// 		+ "    		 GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date \n"
	// 		+ "    		 FROM employee_team_mapping etm     \n"
	// 		+ "    		 LEFT JOIN teams t ON t.team_id = etm.team_id \n"
	// 		+ "    		 LEFT JOIN projects p ON p.project_id = t.project_id \n"
	// 		+ "    		 LEFT JOIN clients c ON c.client_id = p.client_id\n"
	// 		+ "    		 LEFT JOIN client_locations cl ON cl.client_id = p.client_id\n"
	// 		+ "    		 WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
	// 		+ "    		 GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id\n"
	// 		+ "    		 WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
	// 		+ "    		 and emp_proj_client.po_project_type = :poProjectType\n"
	// 		+ "    		 AND emp_proj_client.po_end_date >= CURRENT_DATE\n"
	// 		+ "    		 AND date(emp_proj_client.po_end_date) between :fromDate and :toDate\n"
	// 		+ "    		 and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
	// 		+ "    		 AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))", nativeQuery = true)
	// public List<Object[]> fetchActivePOListOfEmployeeNew(@Param("poProjectType") String poProjectType,
	// 		@Param("deptIds") List<Long> deptIds,
	// 		@Param("fromDate") String fromDate,
	// 		@Param("toDate") String toDate);
	
	// @Query(value = "SELECT DISTINCT p.project_name, \n"
	// 		+ "    p.po_no, p.po_project_type, p.po_start_date, p.po_end_date,\n"
	// 		+ "    p.clientrm, p.apmosysrm, c.client_name, p.client_location \n"
	// 		+ "FROM projects p \n"
	// 		+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
	// 		+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
	// 		+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	// 		+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
	// 		+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
	// 		+ "INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	// 		+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
	// 		+ "WHERE etm.active != 0 \n"
	// 		+ "    AND t.is_active != 'N' \n"
	// 		+ "    AND p.active != 'false' \n"
	// 		+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
	// 		+ "    AND d.dept_id IN (:deptIds)\n"
	// 		+ "    AND p.po_project_type = :poProjectType \n"
	// 		+ "    AND p.po_end_date < CURRENT_DATE\n"
	// 		+ "    AND DATE(p.po_end_date) BETWEEN :fromDate AND :toDate ", nativeQuery = true)
	// public List<Object[]> fetchInActivePOListOfProjectNew(
	// 		@Param("poProjectType") String poProjectType,
	// 		@Param("deptIds") List<Long> deptIds,
	// 		@Param("fromDate") String fromDate,
	// 		@Param("toDate") String toDate);
	
	
	
	// @Query(value = "SELECT DISTINCT p.project_name, \n"
	// 		+ "    p.po_no, p.po_project_type, p.po_start_date, p.po_end_date,\n"
	// 		+ "    p.clientrm, p.apmosysrm, c.client_name, p.client_location \n"
	// 		+ "FROM projects p \n"
	// 		+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
	// 		+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
	// 		+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	// 		+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
	// 		+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
	// 		+ "INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	// 		+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
	// 		+ "WHERE etm.active != 0 \n"
	// 		+ "    AND t.is_active != 'N' \n"
	// 		+ "    AND p.active != 'false' \n"
	// 		+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
	// 		+ "    AND d.dept_id IN (:deptIds)\n"
	// 		+ "    AND p.po_project_type = :poProjectType \n"
	// 		+ "    AND p.po_end_date >= CURRENT_DATE\n"
	// 		+ "    AND DATE(p.po_end_date) BETWEEN :fromDate AND :toDate ", nativeQuery = true)
	// public List<Object[]> fetchActivePOListOfProjectNew(
	// 		@Param("poProjectType") String poProjectType,
	// 		@Param("deptIds") List<Long> deptIds,
	// 		@Param("fromDate") String fromDate,
	// 		@Param("toDate") String toDate);
	
	
	// @Query(value="SELECT distinct p.project_name, \n"
	// 		+ " ppd.po_no, p.po_project_type, p.start_date, p.end_date,\n"
	// 		+ " GROUP_CONCAT(DISTINCT ppd.client_rm SEPARATOR ', ') AS clientrm, \n"
	// 		+ " GROUP_CONCAT(DISTINCT ppd.apmosys_rm SEPARATOR ', ') AS apmosysrm, \n"
	// 		+ " c.client_name, p.client_location FROM projects p INNER JOIN teams t ON t.project_id = p.project_id \n"
	// 		+ " INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
	// 		+ " INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	// 		+ " INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
	// 		+ " INNER JOIN department d ON d.dept_id = j.dept_id\n"
	// 		+ " INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	// 		+ " LEFT  JOIN clients c on p.client_id = c.client_id \n"
	// 		+ " LEFT JOIN project_po_details ppd \n"
	// 		+ " ON ppd.project_id = p.project_id \n"
	// 		+ " AND (ppd.active = TRUE OR ppd.active IS NULL) \n"
	// 		+ " WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6  \n"
	// 		+ " AND d.dept_id IN (:deptId)\n"
	// 		+ " AND po_project_type = :poProjectType \n"
	// 		+ " AND p.end_date < CURRENT_DATE\n"
	// 		+ " AND (:days IS NULL OR p.end_date >= CURRENT_DATE - INTERVAL :days DAY) \n"
	// 		+ " GROUP BY p.project_name, p.po_project_type, p.start_date, p.end_date, c.client_name, p.client_location"
	// 		,nativeQuery = true)
	// public List<Object[]> fetchInActivePOListOfProject(
	//         @Param("poProjectType") String poProjectType,
	//         @Param("days") Integer days,
	//         @Param("deptId") List<Long> deptId);
	
	
	
	@Query("Select e from Employee e where e.employeementId = :empId AND e.isApmosysProduct = 'true'")
	Employee findByEmployeementIdForApmosysProduct(@Param("empId") Long empId);
	
	
	@Query("Select e from Employee e where e.employeementId = :empId AND (e.isApmosysProduct IS NULL OR e.isApmosysProduct = 'false') ")
	Employee findByEmployeementIdForOthers(@Param("empId") Long empId);
	
	@Query("Select e from Employee e where e.employeementId = :empId AND e.isConsultant = 'true'")
	Employee findByEmployeementIdForConsultant(@Param("empId") Long empId);
	
	@Query("Select e from Employee e where e.employeementId = :empId AND e.isApprenticeship = 'true'")
	Employee findByEmployeementIdForApprentice(@Param("empId") Long empId);
	
	@Query("Select e from Employee e where e.employeementId = :empId AND (e.isApmosysProduct IS NULL OR e.isApmosysProduct = 'false') AND (e.isConsultant IS NULL OR e.isConsultant = 'false') AND (e.isApprenticeship IS NULL OR e.isApprenticeship = 'false') ")
	Employee findByEmployeementIdForOthers_Create_And_Update(@Param("empId") Long empId);
	
	
	
	@Query("SELECT e.email FROM Employee e WHERE e.empId = :empId")
    String findEmailByEmpId(Long empId);
	@Query("SELECT CASE " +
	       " WHEN e.isConsultant = 'true' THEN CONCAT('CS-', e.employeementId) " +
	       " WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId) " +
	       " ELSE CONCAT('A-', e.employeementId) " +
	       "END " +
	       "FROM Employee e WHERE e.empId = :empId")
	public String fetchEmploymentIdByEmpId(Long empId);
	
	@Query("SELECT new com.apmosys.employeeportal.dto.GetEmployeeListByProjectIdDTO(e.empId, e.name) " +
		       "FROM com.apmosys.employeeportal.model.Employee e " +
		       "INNER JOIN com.apmosys.employeeportal.model.EmployeeTeamMap etm ON etm.empId = e.empId " +
		       "INNER JOIN com.apmosys.employeeportal.model.Team t ON t.teamId = etm.teamId " +
		       "INNER JOIN com.apmosys.employeeportal.model.Project p ON p.projectId = t.projectId " +
		       "WHERE p.projectId = :projectId AND etm.active != 0 AND etm.empId != :currentUser")
	public List<GetEmployeeListByProjectIdDTO> getEmployeeListByProjectId(Integer projectId,Long currentUser);
	
	/**
	 * Date-aware variant: returns employees mapped to the given project and
	 * active on the specified date window (startOfDay/endOfDay).
	 *
	 * Conditions:
	 * - etm.startDate <= :endOfDay
	 * - etm.endDate IS NULL OR etm.endDate >= :startOfDay
	 * - etm.active != 0
	 * - exclude currentUser
	 */
	@Query("SELECT new com.apmosys.employeeportal.dto.GetEmployeeListByProjectIdDTO(e.empId, e.name) " +
		       "FROM com.apmosys.employeeportal.model.Employee e " +
		       "INNER JOIN com.apmosys.employeeportal.model.EmployeeTeamMap etm ON etm.empId = e.empId " +
		       "INNER JOIN com.apmosys.employeeportal.model.Team t ON t.teamId = etm.teamId " +
		       "INNER JOIN com.apmosys.employeeportal.model.Project p ON p.projectId = t.projectId " +
		       "WHERE p.projectId = :projectId " +
		       "  AND etm.active in (0,1) " +
		       "  AND etm.empId != :currentUser " +
		       "  AND etm.startDate <= :endOfDay " +
		       "  AND (etm.endDate IS NULL OR etm.endDate >= :startOfDay)")
	public List<GetEmployeeListByProjectIdDTO> getEmployeeListByProjectIdForDate(
			Integer projectId,
			Long currentUser,
			LocalDateTime startOfDay,
			LocalDateTime endOfDay);

	/**
	 * Helper query for timesheet flows:
	 * Given a candidate list of employee IDs, returns only those who have at least
	 * one active EmployeeTeamMap record on the specified date window.
	 *
	 * This keeps all complex \"who is my team\" logic in the existing named query
	 * (Employee.getAllTeamMemberView), and moves date-window filtering into a
	 * small, focused query that is easy to maintain.
	 */
	@Query("SELECT DISTINCT e.empId " +
	       "FROM EmployeeTeamMap etm " +
	       "JOIN Employee e ON e.empId = etm.empId " +
	       "JOIN Team t ON t.teamId = etm.teamId " +
	       "JOIN Project p ON p.projectId = t.projectId " +
	       "WHERE e.empId IN :empIds " +
	       "  AND p.active = 'true' AND t.isActive = 'Y' AND etm.active != 2 " +
	       "  AND etm.startDate <= :endOfDay " +
	       "  AND (etm.endDate IS NULL OR etm.endDate >= :startOfDay)")
	List<Long> findTeamMemberIdsActiveOnDate(
			@Param("empIds") List<Long> empIds,
			@Param("startOfDay") LocalDateTime startOfDay,
			@Param("endOfDay") LocalDateTime endOfDay);
	
	@Query(value = " WITH RECURSIVE\n"
			+ "       Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id\n"
			+ "        FROM employee e\n"
			+ "        WHERE (\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id\n"
			+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            e.job_role_id IN (\n"
			+ "                SELECT jr.job_role_id\n"
			+ "                FROM job_role jr\n"
			+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "                inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "                inner JOIN project_manager_mapping pm_inner ON p_inner.project_id = pm_inner.project_id\n"
			+ "                WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                   AND p_inner.has_client_side_id = TRUE\n"
			+ "                  AND  pm_inner.project_manager_id = :emp_id and pm_inner.active = 1\n"
			+ "                    )\n"
			+ "                     OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "                inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "                 inner JOIN project_overhead_mapping pom_inner ON p_inner.project_id = pom_inner.project_id\n"
			+ "                WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                  AND p_inner.has_client_side_id = TRUE\n"
			+ "                  AND  pom_inner.project_overhead_id = :emp_id and pom_inner.active = 1\n"
			+ "                    )\n"
			+ "                     OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "             inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "              WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                  AND p_inner.has_client_side_id = TRUE\n"
			+ "                  AND  (t_inner.spoc_id = :emp_id or t_inner.team_lead_id = :emp_id)\n"
			+ "                    )\n"
			+ "    )),\n"
			+ "\n"
			+ "    Date_Parameters AS (\n"
			+ "        SELECT\n"
			+ "            STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "            CASE\n"
			+ "                WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "                    THEN CURDATE()\n"
			+ "                ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "            END AS to_date\n"
			+ "    ),\n"
			+ "    All_Dates_In_Range AS (\n"
			+ "        SELECT from_date AS dt FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY)\n"
			+ "        FROM All_Dates_In_Range, Date_Parameters\n"
			+ "        WHERE dt <= Date_Parameters.to_date\n"
			+ "    ),\n"
			+ "\n"
			+ "    Base_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            e.emp_id, e.name, p.project_id, p.project_name, p.po_no,\n"
			+ "            COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
			+ "            c.client_name, t.team_id, t.team_name,\n"
			+ "            tl.name AS team_lead_name, e.billable, e.billable_type,\n"
			+ "            e.mobile_no, e.email, p.apmosysrm, p.apmosys_rm_email, p.clientrm,\n"
			+ "            CASE WHEN e.is_apmosys_product = 'true'\n"
			+ "                 THEN CONCAT('AP-', e.employeement_id)\n"
			+ "                 ELSE CONCAT('A-', e.employeement_id) END AS employement_id,\n"
			+ "            d1.name AS dept_name,\n"
			+ "            etm.start_date AS employee_project_start_date,\n"
			+ "            GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "        LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        LEFT JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "        LEFT JOIN job_role j1 ON j1.job_role_id = e.job_role_id\n"
			+ "        LEFT JOIN department d1 ON d1.dept_id = j1.dept_id\n"
			+ "        WHERE etm.active != 0\n"
			+ "          AND t.is_active != 'N'\n"
			+ "          AND p.active != 'false'\n"
			+ "          AND p.has_client_side_id = TRUE\n"
			+ "          AND e.employmentstatus != 'InActive'\n"
			+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "        GROUP BY e.emp_id, e.name, p.project_id, p.project_name, p.po_no, project_type,\n"
			+ "                 c.client_name, t.team_id, t.team_name, team_lead_name, e.billable,\n"
			+ "                 e.billable_type, e.mobile_no, e.email, p.apmosysrm, p.apmosys_rm_email,\n"
			+ "                 p.clientrm, employement_id, dept_name, employee_project_start_date\n"
			+ "    ),\n"
			+ "    Expected_Client_Side_Base_DSR AS (\n"
			+ "        SELECT\n"
			+ "            be.emp_id,\n"
			+ "            be.project_id,\n"
			+ "            adir.dt\n"
			+ "        FROM Base_Employees be\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
			+ "          AND adir.dt >= DATE(be.employee_project_start_date)\n"
//			+ "           AND NOT EXISTS (\n"
//			+ "               SELECT 1 FROM holiday h WHERE h.date_of_holiday = adir.dt\n"
//			+ "           ) \n"
			+ "			  AND NOT EXISTS (\n"
			+ "               SELECT 1 FROM employee_timesheets_new et1\n"
			+ "               LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id\n"
			+ "               WHERE et1.emp_id = be.emp_id AND adir.dt = et1.date\n"
			+ "               AND (UPPER(dtm1.day_type) LIKE '%LEAVE%' OR UPPER(dtm1.day_type) LIKE '%CLIENT%HOLIDAY%' OR UPPER(dtm1.day_type) LIKE '%PUBLIC%HOLIDAY%' OR UPPER(dtm1.day_type) LIKE '%WEEK%OFF%')\n"
			+ "           )"
			+ "    ),\n"
			+ "\n"
			+ "    Actual_Client_Side_Submissions AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            et.emp_id,\n"
			+ "            pts.project_id,\n"
			+ "            et.date AS dt\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        INNER JOIN timesheet_document_details_new tdd ON et.timesheet_id = tdd.timesheet_id\n"
			+ "        INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "          AND (UPPER(csm.status) = 'APPROVED' OR UPPER(csm.status) = 'PENDING')\n"
			+ "          AND et.date < CURDATE()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Expected_Client_Side_DSR AS (\n"
			+ "        SELECT emp_id, project_id, dt FROM Expected_Client_Side_Base_DSR\n"
			+ "        UNION\n"
			+ "        SELECT emp_id, project_id, dt FROM Actual_Client_Side_Submissions\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Actual_Working_Days AS (\n"
			+ "        SELECT DISTINCT be.emp_id, be.project_id, adir.dt\n"
			+ "        FROM Base_Employees be\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        LEFT JOIN holiday h ON h.date_of_holiday = adir.dt\n"
			+ "        WHERE adir.dt < CURDATE()\n"
			+ "          AND adir.dt >= DATE(be.employee_project_start_date)\n"
			+ "          AND (\n"
			+ "              (h.date_of_holiday IS NULL AND DAYOFWEEK(adir.dt) NOT IN (1)\n"
			+ "               AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
			+ "              OR EXISTS (\n"
			+ "                  SELECT 1 FROM employee_timesheets_new et\n"
			+ "                  JOIN timesheet_document_details_new tdd ON et.timesheet_id = tdd.timesheet_id\n"
			+ "                  INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "                  WHERE et.emp_id = be.emp_id\n"
			+ "                    AND pts.project_id = be.project_id\n"
			+ "                    AND et.date = adir.dt\n"
			+ "                    AND tdd.active = TRUE\n"
			+ "              )\n"
			+ "          )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Missing_Days AS (\n"
			+ "        SELECT awd.emp_id, awd.project_id, awd.dt\n"
			+ "        FROM Employee_Actual_Working_Days awd\n"
			+ "        WHERE awd.dt < CURDATE()\n"
			+ "          AND NOT EXISTS (\n"
			+ "              SELECT 1\n"
			+ "              FROM employee_timesheets_new et\n"
			+ "              LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "              INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "              WHERE et.emp_id = awd.emp_id\n"
			+ "                AND pts.project_id = awd.project_id\n"
			+ "                AND et.date = awd.dt\n"
			+ "                AND dtm.day_type LIKE '%Working%'\n"
			+ "          )\n"
			+ "    ),\n"
			+ "\n"
			+ "    Defaulter_List AS (\n"
			+ "        SELECT emp_id, project_id\n"
			+ "        FROM Missing_Days\n"
			+ "        GROUP BY emp_id, project_id\n"
			+ "        HAVING COUNT(*) >= 2\n"
			+ "    ),\n"
			+ "\n"
			+ "    Apmosys_Timesheet_Summary AS (\n"
			+ "        SELECT et.emp_id, pts.project_id, COUNT(DISTINCT et.date) AS filled_working_days\n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE (dtm.day_type LIKE '%Working%' OR UPPER(dtm.day_type) LIKE '%LEAVE%')\n"
			+ "        GROUP BY et.emp_id, pts.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Employee_Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            et.emp_id,\n"
			+ "            pts.project_id,\n"
			+ "            COUNT(DISTINCT CASE WHEN UPPER(csm.status) = 'APPROVED' AND tdd.final_flag = 1 THEN tdd.timesheet_id END) AS approved_days,\n"
			+ "            COUNT(DISTINCT CASE WHEN UPPER(csm.status) = 'PENDING' AND tdd.timesheet_id\n"
			+ "                                    NOT IN (SELECT timesheet_id FROM timesheet_document_details_new tdd2\n"
			+ "                                            INNER JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id\n"
			+ "                                            WHERE UPPER(csm2.status) = 'APPROVED') THEN tdd.timesheet_id END) AS pending_days\n"
			+ "        FROM timesheet_document_details_new tdd\n"
			+ "        INNER JOIN employee_timesheets_new et ON tdd.timesheet_id = et.timesheet_id\n"
			+ "        INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id\n"
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id\n"
			+ "        JOIN Date_Parameters dp ON DATE(et.date) BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE tdd.active = TRUE\n"
			+ "        GROUP BY et.emp_id, pts.project_id\n"
			+ "    ),\n"
			+ "\n"
			+ "    Final_Report_Data AS (\n"
			+ "        SELECT\n"
			+ "            e.emp_id, e.name, e.project_id, e.project_name, e.po_no, e.project_type,\n"
			+ "            e.client_name, e.team_id, e.team_name, e.team_lead_name,\n"
			+ "            e.billable, e.billable_type, e.mobile_no, e.email,\n"
			+ "            e.apmosysrm, e.apmosys_rm_email, e.clientrm,\n"
			+ "            e.employement_id, e.dept_name, e.Project_Manager,\n"
			+ "            COALESCE(eed.expected_days_passed, 0) AS expected_fill_count,\n"
			+ "            COALESCE(ecsdr.total_expected_dsr_days, 0) AS expected_client_side_dsr, \n"
			+ "            COALESCE(ats.filled_working_days, 0) AS ishine_timesheet_filled_count,\n"
			+ "            GREATEST(0, COALESCE(ecsdr.total_expected_dsr_days, 0) -\n"
			+ "                (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) AS ClientSideNotFilledTimesheets_count,\n"
			+ "            COALESCE(eds.pending_days, 0) AS ClientSidePendingTimesheet_count,\n"
			+ "            COALESCE(eds.approved_days, 0) AS Client_Approved_count,\n"
			+ "            CASE\n"
			+ "                WHEN GREATEST(0, COALESCE(ecsdr.total_expected_dsr_days, 0) - \n"
			+ "                (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "                WHEN COALESCE(ecsdr.total_expected_dsr_days, 0) > \n"
			+ "                     (COALESCE(eds.approved_days, 0) + COALESCE(eds.pending_days, 0))\n"
			+ "                     OR COALESCE(eds.pending_days, 0) > 0 THEN 'Pending'\n"
			+ "                ELSE 'Approved'\n"
			+ "            END AS employee_status\n"
			+ "        FROM Base_Employees e\n"
			+ "        LEFT JOIN (\n"
			+ "            SELECT emp_id, project_id, COUNT(dt) AS expected_days_passed\n"
			+ "            FROM Employee_Actual_Working_Days\n"
			+ "            WHERE dt <= CURDATE()\n"
			+ "            GROUP BY emp_id, project_id\n"
			+ "        ) eed ON e.emp_id = eed.emp_id AND e.project_id = eed.project_id\n"
			+ "        LEFT JOIN ( -- New join for the combined expected DSR\n"
			+ "            SELECT emp_id, project_id, COUNT(dt) AS total_expected_dsr_days\n"
			+ "            FROM Expected_Client_Side_DSR\n"
			+ "            GROUP BY emp_id, project_id\n"
			+ "        ) ecsdr ON e.emp_id = ecsdr.emp_id AND e.project_id = ecsdr.project_id\n"
			+ "        LEFT JOIN Employee_Document_Summary eds ON e.emp_id = eds.emp_id AND e.project_id = eds.project_id\n"
			+ "        LEFT JOIN Defaulter_List dl ON e.emp_id = dl.emp_id AND e.project_id = dl.project_id\n"
			+ "        LEFT JOIN Apmosys_Timesheet_Summary ats ON e.emp_id = ats.emp_id AND e.project_id = ats.project_id\n"
			+ "    )\n"
			+ "\n"
			+ "SELECT SQL_CALC_FOUND_ROWS\n"
			+ "    name, emp_id, project_name, po_no, client_name, team_name, project_type,\n"
			+ "    team_lead_name, billable, billable_type, mobile_no, email,\n"
			+ "    expected_client_side_dsr,\n"
			+ "    ClientSideNotFilledTimesheets_count, ClientSidePendingTimesheet_count,\n"
			+ "    Client_Approved_count, apmosysrm, apmosys_rm_email, clientrm,\n"
			+ "    employement_id, dept_name, Project_Manager, project_id, employee_status AS employee_status\n"
			+ "FROM Final_Report_Data\n"
			+ "WHERE (:status = 'All' OR employee_status = :status)\n"
			+ "			        AND (:employmentId IS NULL OR LOWER(employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ "			        AND (:name IS NULL OR LOWER(name) LIKE CONCAT('%', :name, '%'))\n"
			+ "			        AND (:billable IS NULL OR LOWER(billable) = :billable)\n"
			+ "			        AND (:billableType IS NULL OR LOWER(billable_type) = :billableType)\n"
			+ "			        AND (:mobileNo IS NULL OR mobile_no LIKE CONCAT('%', :mobileNo, '%'))\n"
			+ "			        AND (:email IS NULL OR LOWER(email) LIKE CONCAT('%', :email, '%'))\n"
			+ "			        AND (:departmentName IS NULL OR LOWER(dept_name) LIKE CONCAT('%', :departmentName, '%'))\n"
			+ "			        AND (:expectedFillCount IS NULL OR expected_client_side_dsr = :expectedFillCount)\n"
			+ "			        AND (:clientSideAttendancePendingCount IS NULL OR ClientSidePendingTimesheet_count = :clientSideAttendancePendingCount)\n"
			+ "			        AND (:clientSideAttendanceApprovedCount IS NULL OR Client_Approved_count = :clientSideAttendanceApprovedCount)\n"
			+ "			        AND (:clientSideAttendanceNotFilledCount IS NULL OR ClientSideNotFilledTimesheets_count = :clientSideAttendanceNotFilledCount)\n"
			+ "			        AND (:projectName IS NULL OR LOWER(project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ "			        AND (:poNo IS NULL OR LOWER(po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ "			        AND (:projectType IS NULL OR LOWER(project_type) LIKE CONCAT('%', :projectType, '%'))\n"
			+ "			        AND (:projectManagers IS NULL OR LOWER(Project_Manager) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ "			        AND (:clientName IS NULL OR LOWER(client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ "			        AND (:apmosysRm IS NULL OR LOWER(apmosysrm) LIKE CONCAT('%', :apmosysRm, '%'))\n"
			+ "			        AND (:apmosysRmEmail IS NULL OR LOWER(apmosys_rm_email) LIKE CONCAT('%', :apmosysRmEmail, '%'))\n"
			+ "			        AND (:clientRm IS NULL OR LOWER(clientrm) LIKE CONCAT('%', :clientRm, '%'))\n"
			+ "			        AND (:team IS NULL OR LOWER(team_name) LIKE CONCAT('%', :team, '%'))\n"
			+ "			        AND (:teamLeadName IS NULL OR LOWER(team_lead_name) LIKE CONCAT('%', :teamLeadName, '%'))\n"
			+ "			ORDER BY\n"
			+ "			    CASE WHEN :sortDirection = 'asc' THEN\n"
			+ "			        CASE\n"
			+ "			            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "			            WHEN :sortBy = 'name' THEN name\n"
			+ "			            WHEN :sortBy = 'billable' THEN billable\n"
			+ "			            WHEN :sortBy = 'billable_type' THEN billable_type\n"
			+ "			            WHEN :sortBy = 'mobile_no' THEN mobile_no\n"
			+ "			            WHEN :sortBy = 'email' THEN email\n"
			+ "			            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "			            WHEN :sortBy = 'expected_client_side_dsr' THEN expected_client_side_dsr\n"
			+ "			            WHEN :sortBy = 'ClientSideNotFilledTimesheets_count' THEN ClientSideNotFilledTimesheets_count\n"
			+ "			            WHEN :sortBy = 'Client_Approved_count' THEN Client_Approved_count\n"
			+ "			            WHEN :sortBy = 'ClientSideNotFilledTimesheets_count' THEN ClientSideNotFilledTimesheets_count\n"
			+ "			            WHEN :sortBy = 'project_name' THEN project_name\n"
			+ "			            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "			            WHEN :sortBy = 'project_type' THEN project_type\n"
			+ "			            WHEN :sortBy = 'Project_Manager' THEN project_manager\n"
			+ "			            WHEN :sortBy = 'client_name' THEN client_name\n"
			+ "			            WHEN :sortBy = 'apmosysrm' THEN apmosysrm\n"
			+ "			            WHEN :sortBy = 'apmosys_rm_email' THEN apmosys_rm_email\n"
			+ "			            WHEN :sortBy = 'clientrm' THEN clientrm\n"
			+ "			            WHEN :sortBy = 'team' THEN team_name\n"
			+ "			            WHEN :sortBy = 'team_lead_name' THEN team_lead_name\n"
			+ "			            ELSE name\n"
			+ "			        END\n"
			+ "			    END ASC,\n"
			+ "			    CASE WHEN :sortDirection = 'desc' THEN\n"
			+ "			        CASE\n"
			+ "			            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "			            WHEN :sortBy = 'name' THEN name\n"
			+ "			            WHEN :sortBy = 'billable' THEN billable\n"
			+ "			            WHEN :sortBy = 'billable_type' THEN billable_type\n"
			+ "			            WHEN :sortBy = 'mobile_no' THEN mobile_no\n"
			+ "			            WHEN :sortBy = 'email' THEN email\n"
			+ "			            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "			            WHEN :sortBy = 'expected_client_side_dsr' THEN expected_client_side_dsr\n"
			+ "			            WHEN :sortBy = 'ClientSideNotFilledTimesheets_count' THEN ClientSideNotFilledTimesheets_count\n"
			+ "			            WHEN :sortBy = 'Client_Approved_count' THEN client_approved_count\n"
			+ "			            WHEN :sortBy = 'ClientSideNotFilledTimesheets_count' THEN ClientSideNotFilledTimesheets_count\n"
			+ "			            WHEN :sortBy = 'project_name' THEN project_name\n"
			+ "			            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "			            WHEN :sortBy = 'project_type' THEN project_type\n"
			+ "			            WHEN :sortBy = 'Project_Manager' THEN project_manager\n"
			+ "			            WHEN :sortBy = 'client_name' THEN client_name\n"
			+ "			            WHEN :sortBy = 'apmosysrm' THEN apmosysrm\n"
			+ "			            WHEN :sortBy = 'apmosys_rm_email' THEN apmosys_rm_email\n"
			+ "			            WHEN :sortBy = 'clientrm' THEN clientrm\n"
			+ "			            WHEN :sortBy = 'team' THEN team_name\n"
			+ "			            WHEN :sortBy = 'team_lead_name' THEN team_lead_name\n"
			+ "			            ELSE name\n"
			+ "			        END\n"
			+ "			    END DESC\n"
			+ "				LIMIT :offset, :pageSize "
						,nativeQuery = true)
	public List<Object[]> getEmployeeViewForClientAttendanceStatus(@Param("status") String status, @Param("month") Integer month, @Param("year") Integer year,@Param("emp_id") Long emp_id,
			String employmentId,String name,String billable,String billableType,Long mobileNo,String email,String departmentName,Integer expectedFillCount,Integer clientSideAttendancePendingCount,
			Integer clientSideAttendanceApprovedCount,Integer clientSideAttendanceNotFilledCount,String projectName,String poNo,String projectType,String projectManagers,String clientName,
			String apmosysRm,String apmosysRmEmail,String clientRm,String team,String teamLeadName,String sortBy,String sortDirection,int offset,int pageSize);
	
	
	 @Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.reportingManagerId, d.hodId, e.managerId)\n"
		 		+ "FROM Employee e, JobRole jr, Department d\n"
		 		+ "WHERE e.jobRoleId = jr.jobRoleId\n"
		 		+ "  AND jr.deptId = d.deptId\n"
		 		+ "  AND e.empId = :empId")
		    Optional<EmployeeDTO> findEmployeeReportingManagerIdAndHODIdDetailsByEmpId(@Param("empId") Long empId);

	 
	 @Modifying
	 @Query("UPDATE Employee e SET e.billable = :billable, e.billableType = :billableType WHERE e.empId IN :empIds")
	 public int updateBillableAndTypeForEmpIds(@Param("billable") String billable,
	                                    @Param("billableType") String billableType,
	                                    @Param("empIds") List<Long> empIds);


		@Query(nativeQuery = true,value = "select sum(no_of_days) from employee_leave where emp_id = :empId")
		public Float getNOOfDays(@Param("empId") Long empId);
		
		
		    	
		// @Query(value = "SELECT distinct \n"
		// 		+ "    e.emp_id, \n"
		// 		+ "    e.name, \n"
		// 		+ "    emp_proj_client.project_name, \n"
		// 		+ "    emp_proj_client.start_date, \n"
		// 		+ "    emp_proj_client.end_date, \n"
		// 		+ "    emp_proj_client.po_no, \n"
		// 		+ "    emp_proj_client.client_name, \n"
		// 		+ "    emp_proj_client.client_location, \n"
		// 		+ "    d.name AS departmentName, \n"
		// 		+ "    emp_proj_client.po_project_type, \n"
		// 		+ "    CASE\n"
		// 		+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
		// 		+ "        WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id)\n"
		// 		+ "        ELSE CONCAT('A-', e.employeement_id)\n"
		// 		+ "    END AS prefixed_employeementId \n"
		// 		+ "FROM employee e\n"
		// 		+ "INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
		// 		+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
		// 		+ "INNER JOIN employee m ON e.manager_id = m.emp_id \n"
		// 		+ "LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id and eppm.is_mapped = 'Y'\n"
		// 		+ "INNER JOIN (     \n"
		// 		+ "	SELECT etm.emp_id,  \n"
		// 		+ "GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
		// 		+ "	GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
		// 		+ "	GROUP_CONCAT(DISTINCT p.start_date ORDER BY p.project_id) AS start_date, \n"
		// 		+ "	GROUP_CONCAT(DISTINCT p.end_date ORDER BY p.project_id) AS end_date,  \n"
		// 		+ "	GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no,  \n"
		// 		+ "	GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type,\n"
		// 		+ "	GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
		// 		+ "	GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location "
		// 		+ "	FROM employee_team_mapping etm     \n"
		// 		+ "	INNER JOIN teams t ON t.team_id = etm.team_id \n"
		// 		+ "	INNER JOIN projects p ON p.project_id = t.project_id \n"
		// 		+ "	LEFT JOIN clients c ON c.client_id = p.client_id\n"
		// 		+ "LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id\n"
		// 		+ "LEFT JOIN client_locations cl ON cl.po_id = ppd.po_id  AND etm.po_id = ppd.po_id\n"
		// 		+ "	WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
		// 		+ "	GROUP BY etm.emp_id \n"
		// 		+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
		// 		+ "LEFT JOIN (\n"
		// 		+ "	select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
		// 		+ "		  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
		// 		+ "	from employee e \n"
		// 		+ "	left join employee_leave el \n"
		// 		+ "		on el.emp_id = e.emp_id \n"
		// 		+ "		and leave_status_id in (1,2) \n"
		// 		+ "		and manager_approval_status = 'Approved' \n"
		// 		+ "		and leave_type_master_id = 5 \n"
		// 		+ "		and curdate() between date(el.from_date) and date(el.to_date) \n"
		// 		+ "		) eld on eld.emp_id = e.emp_id\n"
		// 		+ "WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
		// 		+ "and emp_proj_client.po_project_type = :poProjectType\n"
		// 		+ "AND emp_proj_client.end_date < CURRENT_DATE\n"
		// 		+ "AND date(emp_proj_client.end_date) between date:fromDate and date:toDate\n"
		// 		+ "and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
		// 		+ "AND ((:leave_filter = true) \n"
		// 		+ "			or \n"
		// 		+ "		(:leave_filter != true and eld.On_Maternity_Leave = 'No')\n"
		// 		+ "	)\n"
		// 		+ "AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))", nativeQuery = true)
		//     	public List<Object[]> fetchInactivePOListOfEmployeeNew(
		//     			@Param("poProjectType") String poProjectType,
		//     			@Param("deptIds") List<Long> deptIds,
		//     			@Param("fromDate") String fromDate,
		//     			@Param("toDate") String toDate,
		//     			@Param("leave_filter") boolean maternityleaveFilter);
		    	
		    	@Query(value = "WITH AllPoProjectTypes AS (\n"
		        		+ "    		     SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
		        		+ "    		 ),\n"
		        		+ "    		 AllBillableTypes AS (\n"
		        		+ "    		     SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
		        		+ "    		 ),\n"
		        		+ "    		 AllCombinations AS (\n"
		        		+ "    		     SELECT apt.po_project_type, abt.billable_type\n"
		        		+ "    		     FROM AllPoProjectTypes apt\n"
		        		+ "    		     CROSS JOIN AllBillableTypes abt\n"
		        		+ "    		 ),\n"
		        		+ "    		 MainAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         e.billable_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) as total_projects\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "                     AND p.end_date < CURRENT_DATE\n"
		        		+ "				 	 and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ "    		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
		        		+ "    		 ),\n"
		        		+ "    		 OverallAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_pos\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "                     AND p.end_date < CURRENT_DATE\n"
		        		+ "				 	 and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ " 		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal')\n"
		        		+ "    		 ),\n"
		        		+ "    		 OverallPerBillableAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         e.billable_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_projects_per_billable_type\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "    		         AND p.end_date < CURRENT_DATE\n"
		        		+ "					  and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ "    		        AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
		        		+ "    		 )\n"
		        		+ "    		 SELECT DISTINCT \n"
		        		+ "    		     ac.po_project_type,\n"
		        		+ "    		     ac.billable_type,\n"
		        		+ "    		     COALESCE(oa.overall_pos, 0) AS total_expired_pos,\n"
		        		+ "    		     COALESCE(opba.overall_projects_per_billable_type, 0) AS overall_projects_per_billable_type,\n"
		        		+ "    		     COALESCE(ma.total_projects, 0) AS total_projects\n"
		        		+ "    		 FROM AllCombinations ac\n"
		        		+ "    		 LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
		        		+ "    		 LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
		        		+ "    		 LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
		        		+ "    		 WHERE 1=1\n"
		        		+ "              and ac.po_project_type = :po_project_typee \n"
		        		+ "             AND ac.billable_type = 'TNM'\n"
		        		+ "    		 ORDER BY ac.po_project_type, ac.billable_type;" , nativeQuery = true)
		        List<Object[]> fetchInactivePOCountsForProjectNew(@Param("po_project_typee") String po_project_typee,@Param("deptIds") List<Long> deptIds,
		                @Param("fromDate") String fromDate,
		                @Param("toDate") String toDate);
		        
		        @Query(value = "SELECT DISTINCT p.project_name, \n"
		    			+ " GROUP_CONCAT( DISTINCT ppd.po_no SEPARATOR ', ' ) AS po_no,\n"
						+ " p.po_project_type, ppd.po_start_date, ppd.po_end_date,\n"
		    			+ " GROUP_CONCAT( DISTINCT ppd.client_rm SEPARATOR ', ') AS clientrm, \n"
						+ " GROUP_CONCAT( DISTINCT ppd.apmosys_rm SEPARATOR ', ') AS apmosysrm, \n"
						+ " c.client_name, p.client_location \n"
		    			+ "FROM projects p \n"
						+ "LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id \n"
						+ "AND ( ( :fromDate IS NOT NULL  AND :toDate IS NOT NULL AND DATE(ppd.po_start_date) <= date(:toDate) \n"
						+ " AND DATE(ppd.po_end_date) >= DATE(:fromDate) ) "
						+ " or ( :fromDate IS NULL  AND :toDate IS NULL  AND ppd.active = true \n"
						+ " AND Date(ppd.po_end_date) = (SELECT distinct MAX(Date(ppd2.po_end_date)) from project_po_details ppd2 where ppd2.project_id = p.project_id \n"
						+ " and ppd2.active = true )) \n"
						+ ") \n"
		    			+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
		    			+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		    			+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
		    			+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		    			+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
		    			+ "LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
		    			+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
		    			+ "WHERE etm.active != 0 \n"
		    			+ "    AND t.is_active != 'N' \n"
		    			+ "    AND p.active != 'false' \n"
		    			+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		    			+ "    AND d.dept_id IN (:deptIds)\n"
		    			+ "    AND p.po_project_type = :poProjectType \n"
		    			+ "    AND p.end_date < CURRENT_DATE\n"
		    			+ "    AND ( (:fromDate is NULL or DATE(p.end_date) >=  date(:fromDate) ) AND ( :toDate is Null or  date(p.end_date)  <= date(:toDate) ) ) \n"
						+ " AND e.employmentstatus != 'InActive' \n"
						+ "GROUP BY\n" + 
						"    p.project_name,\n" + 
						"    p.po_project_type,\n" +
						"    ppd.po_start_date,\n" + 
						"    ppd.po_end_date,\n" + 
						"    c.client_name,\n" +
						"    p.client_location", nativeQuery = true)
		    	public List<Object[]> fetchInActivePOListOfProjectNew(
		    			@Param("poProjectType") String poProjectType,
		    			@Param("deptIds") List<Long> deptIds,
		    			@Param("fromDate") String fromDate,
		    			@Param("toDate") String toDate);

		    	@Query(value = "SELECT e.employeement_id, e.name, \n"
		    			+"	e.billable, e.billable_type,  \n"
		    			+"	emp_proj_client.project_name, emp_proj_client.start_date, emp_proj_client.end_date, \n"
		    			+"	emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location,  \n"
		    			+"	d.name as departmentName, emp_proj_client.po_project_type,  \n"
		    			+"	j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id, \n"
		    			+"	emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date,  \n"
		    			+"	emp_proj_client.effective_end_date FROM employee e \n"
		    			+"	INNER JOIN job_role j ON j.job_role_id = e.job_role_id \n"
		    			+"	INNER JOIN department d ON d.dept_id = j.dept_id \n"
		    			+"	INNER JOIN employee m ON e.manager_id = m.emp_id \n"
		    			+"	LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id and eppm.is_mapped = 'Y' \n"
		    			+"	LEFT JOIN (      \n"
		    			+"	SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,   \n"
		    			+"	GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id,  \n"
		    			+"	GROUP_CONCAT(DISTINCT p.start_date ORDER BY p.project_id) AS start_date,  \n"
		    			+"	GROUP_CONCAT(DISTINCT p.end_date ORDER BY p.project_id) AS end_date,   \n"
		    			+"	GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no,   \n"
		    			+"	GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type, \n"
		    			+"	GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name, \n"
		    			+"	GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location, \n"
		    			+"	GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,   \n"
		    			+"	GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id,  \n"
		    			+"	GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id,  \n"
		    			+"	GROUP_CONCAT(DISTINCT p.clientrm ORDER BY p.project_id) AS clientrm,  \n"
		    			+"	GROUP_CONCAT(DISTINCT p.apmosysrm ORDER BY p.project_id) AS apmosysrm, \n"
		    			+"	GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date,  \n"
		    			+"	GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date  \n"
		    			+"	FROM employee_team_mapping etm      \n"
		    			+"	LEFT JOIN teams t ON t.team_id = etm.team_id  \n"
		    			+"	LEFT JOIN projects p ON p.project_id = t.project_id  \n"
		    			+"	LEFT JOIN clients c ON c.client_id = p.client_id \n"
		    			+" LEFT JOIN client_locations cl ON cl.client_id = c.client_id \n"
		    			+"	LEFT JOIN ( \n"
		    			+"				select distinct e.emp_id as emp_id, e.name as name, e.email as email \n"
		    			+"					  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave \n"
		    			+"				from employee e  \n"
		    			+"				left join employee_leave el  \n"
		    			+"					on el.emp_id = e.emp_id  \n"
		    			+"					and leave_status_id in (1,2)  \n"
		    			+"					and manager_approval_status = 'Approved'  \n"
		    			+"					and leave_type_master_id = 5  \n"
		    			+"					and curdate() between date(el.from_date) and date(el.to_date)  \n"
		    			+"			) eld on eld.emp_id = etm.emp_id \n"
		    			+"	WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'   \n"
		    			+"	GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
		    			+"	WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL  \n"
		    			+"	and emp_proj_client.po_project_type = :poProjectType \n"
		    			+"	AND emp_proj_client.end_date >= CURRENT_DATE \n"
		    			+"	AND date(emp_proj_client.end_date) between :fromDate and :toDate \n"
		    			+"	and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM') \n"
		    			+"	AND ((:leave_filter = true)  \n"
		    			+"				or  \n"
		    			+"			(:leave_filter != true and eld.On_Maternity_Leave = 'No') \n"
		    			+"		) \n"
		    			+"	AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))", nativeQuery = true)
		    	public List<Object[]> fetchActivePOListOfEmployeeNew(@Param("poProjectType") String poProjectType,
		    			@Param("deptIds") List<Long> deptIds,
		    			@Param("fromDate") String fromDate,
		    			@Param("toDate") String toDate,
		    			@Param("leave_filter") boolean maternityleaveFilter);
		    	
		    	// @Query(value = "SELECT DISTINCT p.project_name, \n"
		    	// 		+ "    p.po_no, p.po_project_type, p.start_date, p.end_date,\n"
		    	// 		+ "    p.clientrm, p.apmosysrm, c.client_name, p.client_location \n"
		    	// 		+ "FROM projects p \n"
		    	// 		+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
		    	// 		+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		    	// 		+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
		    	// 		+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		    	// 		+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
		    	// 		+ "INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
		    	// 		+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
		    	// 		+ "WHERE etm.active != 0 \n"
		    	// 		+ "    AND t.is_active != 'N' \n"
		    	// 		+ "    AND p.active != 'false' \n"
		    	// 		+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		    	// 		+ "    AND d.dept_id IN (:deptIds)\n"
		    	// 		+ "    AND p.po_project_type = :poProjectType \n"
		    	// 		+ "    AND p.end_date >= CURRENT_DATE\n"
		    	// 		+ "    AND DATE(p.end_date) BETWEEN :fromDate AND :toDate ", nativeQuery = true)
		    	// public List<Object[]> fetchActivePOListOfProjectNew(
		    	// 		@Param("poProjectType") String poProjectType,
		    	// 		@Param("deptIds") List<Long> deptIds,
		    	// 		@Param("fromDate") String fromDate,
		    	// 		@Param("toDate") String toDate);
		    	
		    	@Query(value = "WITH AllPoProjectTypes AS (\n"
		        		+ "    		     SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring'\n"
		        		+ "    		 ),\n"
		        		+ "    		 AllBillableTypes AS (\n"
		        		+ "    		     SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts'\n"
		        		+ "    		 ),\n"
		        		+ "    		 AllCombinations AS (\n"
		        		+ "    		     SELECT apt.po_project_type, abt.billable_type\n"
		        		+ "    		     FROM AllPoProjectTypes apt\n"
		        		+ "    		     CROSS JOIN AllBillableTypes abt\n"
		        		+ "    		 ),\n"
		        		+ "    		 MainAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         e.billable_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) as total_projects\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "                     AND p.end_date >= CURRENT_DATE\n"
		        		+ "				 	 and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ "    		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
		        		+ "    		 ),\n"
		        		+ "    		 OverallAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_pos\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "                     AND p.end_date >= CURRENT_DATE\n"
		        		+ "				 	 and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ " 		       AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal')\n"
		        		+ "    		 ),\n"
		        		+ "    		 OverallPerBillableAgg AS (\n"
		        		+ "    		     SELECT\n"
		        		+ "    		         COALESCE(p.po_project_type, 'Internal') AS po_project_type,\n"
		        		+ "    		         e.billable_type,\n"
		        		+ "    		         COUNT(DISTINCT p.po_project_id) AS overall_projects_per_billable_type\n"
		        		+ "    		     FROM projects p\n"
		        		+ "    		     JOIN teams t ON t.project_id = p.project_id\n"
		        		+ "    		     JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
		        		+ "    		     JOIN employee e ON etm.emp_id = e.emp_id\n"
		        		+ "    		     JOIN job_role j ON e.job_role_id = j.job_role_id\n"
		        		+ "    		     JOIN department d ON d.dept_id = j.dept_id\n"
		        		+ "    		     WHERE etm.active != 0\n"
		        		+ "    		       AND t.is_active != 'N'\n"
		        		+ "    		       AND p.active != 'false'\n"
		        		+ "    		       AND e.emp_id NOT BETWEEN 1 AND 6\n"
		        		+ "    		        AND p.po_project_type = :po_project_typee\n"
		        		+ "    		         AND p.end_date >= CURRENT_DATE\n"
		        		+ "					  and DATE(p.end_date) between :fromDate and :toDate\n"
		        		+ "    		        AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))\n"
		        		+ "    		     GROUP BY COALESCE(p.po_project_type, 'Internal'), e.billable_type\n"
		        		+ "    		 )\n"
		        		+ "    		 SELECT DISTINCT \n"
		        		+ "    		     ac.po_project_type,\n"
		        		+ "    		     ac.billable_type,\n"
		        		+ "    		     COALESCE(oa.overall_pos, 0) AS total_expired_pos,\n"
		        		+ "    		     COALESCE(opba.overall_projects_per_billable_type, 0) AS overall_projects_per_billable_type,\n"
		        		+ "    		     COALESCE(ma.total_projects, 0) AS total_projects\n"
		        		+ "    		 FROM AllCombinations ac\n"
		        		+ "    		 LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type\n"
		        		+ "    		 LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type\n"
		        		+ "    		 LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type\n"
		        		+ "    		 WHERE 1=1\n"
		        		+ "              and ac.po_project_type = :po_project_typee \n"
		        		+ "             AND ac.billable_type = 'TNM'\n"
		        		+ "    		 ORDER BY ac.po_project_type, ac.billable_type;" , nativeQuery = true)
		        List<Object[]> fetchactivePOCountsForProjectNew(@Param("po_project_typee") String po_project_typee,@Param("deptIds") List<Long> deptIds,
		                @Param("fromDate") String fromDate,
		                @Param("toDate") String toDate);
		        
		        @Query(nativeQuery = true,value = 
		        		"WITH AllPoProjectTypes AS ( \n"
		        		+"	 SELECT 'Internal' AS po_project_type UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'Monitoring' \n"
		        		+" ), \n"
		        		+" AllBillableTypes AS ( \n"
		        		+"	 SELECT 'Bench' AS billable_type UNION ALL SELECT 'Shadow' UNION ALL SELECT 'TNM' UNION ALL SELECT 'Fixed Cost' UNION ALL SELECT 'InternalRNDProducts' \n"
		        		+" ), \n"
		        		+" AllCombinations AS ( \n"
		        		+"	 SELECT apt.po_project_type, abt.billable_type \n"
		        		+"	 FROM AllPoProjectTypes apt \n"
		        		+"	 CROSS JOIN AllBillableTypes abt \n"
		        		+" ), \n"
		        		+" MainAgg AS ( \n"
		        		+"	 SELECT \n"
		        		+"		 CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type, \n"
		        		+"		 e.billable_type,count(distinct e.emp_id) total_emp \n"
		        		+"	 FROM projects p \n"
		        		+"	 INNER JOIN teams t ON t.project_id = p.project_id \n"
		        		+"	 INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		        		+"	 INNER JOIN employee e ON etm.emp_id = e.emp_id \n"
		        		+"	 INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		        		+"	 INNER JOIN department d ON d.dept_id = j.dept_id \n"
		        		+"	 LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id \n"
		        		+"     LEFT JOIN ( \n"
		        		+"			select distinct e.emp_id as emp_id, e.name as name, e.email as email \n"
		        		+"				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave \n"
		        		+"			from employee e  \n"
		        		+"			left join employee_leave el  \n"
		        		+"				on el.emp_id = e.emp_id  \n"
		        		+"				and leave_status_id in (1,2)  \n"
		        		+"				and manager_approval_status = 'Approved'  \n"
		        		+"				and leave_type_master_id = 5  \n"
		        		+"				and curdate() between date(el.from_date) and date(el.to_date)  \n"
		        		+"		) eld on eld.emp_id = e.emp_id \n"
		        		+"	 WHERE etm.active != 0 \n"
		        		+"	   AND t.is_active != 'N' \n"
		        		+"	   AND p.active != 'false' \n"
		        		+"	   AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		        		+"		 AND p.po_project_type = :po_project_typee \n"
		        		+"	   AND p.end_date >= CURRENT_DATE \n"
		        		+"		  and DATE(p.end_date) between :fromDate and :toDate \n"
		        		+"		 AND d.dept_id IN (:deptId) \n"
		        		+"         AND ((:leave_filter = true)  \n"
		        		+"			or  \n"
		        		+"		(:leave_filter != true and eld.On_Maternity_Leave = 'No') \n"
		        		+"	) \n"
		        		+"	    \n"
		        		+" ), \n"
		        		+" OverallAgg AS ( \n"
		        		+"	 SELECT \n"
		        		+"		 CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type, \n"
		        		+"		 COUNT(DISTINCT e.emp_id) AS overall_employee \n"
		        		+"	 FROM projects p \n"
		        		+"	 INNER JOIN teams t ON t.project_id = p.project_id \n"
		        		+"	 INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		        		+"	 INNER JOIN employee e ON etm.emp_id = e.emp_id \n"
		        		+"	 INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		        		+"	 INNER JOIN department d ON d.dept_id = j.dept_id \n"
		        		+"     LEFT JOIN ( \n"
		        		+"			select distinct e.emp_id as emp_id, e.name as name, e.email as email \n"
		        		+"				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave \n"
		        		+"			from employee e  \n"
		        		+"			left join employee_leave el  \n"
		        		+"				on el.emp_id = e.emp_id  \n"
		        		+"				and leave_status_id in (1,2)  \n"
		        		+"				and manager_approval_status = 'Approved'  \n"
		        		+"				and leave_type_master_id = 5  \n"
		        		+"				and curdate() between date(el.from_date) and date(el.to_date)  \n"
		        		+"		) eld on eld.emp_id = e.emp_id \n"
		        		+"	 WHERE etm.active != 0 \n"
		        		+"	   AND t.is_active != 'N' \n"
		        		+"	   AND p.active != 'false' \n"
		        		+"	   AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		        		+"		  AND p.po_project_type = :po_project_typee \n"
		        		+"	   AND p.end_date >= CURRENT_DATE  \n"
		        		+"		and DATE(p.end_date) between :fromDate and :toDate \n"
		        		+"	  AND d.dept_id IN (:deptId) \n"
		        		+"      AND ((:leave_filter = true)  \n"
		        		+"			or  \n"
		        		+"		(:leave_filter != true and eld.On_Maternity_Leave = 'No') \n"
		        		+"	) \n"
		        		+"	 GROUP BY \n"
		        		+"		 CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END \n"
		        		+" ), \n"
		        		+" OverallPerBillableAgg AS ( \n"
		        		+"	 SELECT \n"
		        		+"		 CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END AS po_project_type, \n"
		        		+"		 e.billable_type, \n"
		        		+"		 COUNT(DISTINCT e.emp_id) AS overall_emp_per_billable_type \n"
		        		+"	 FROM projects p \n"
		        		+"	 INNER JOIN teams t ON t.project_id = p.project_id \n"
		        		+"	 INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		        		+"	 INNER JOIN employee e ON etm.emp_id = e.emp_id \n"
		        		+"	 INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		        		+"	 INNER JOIN department d ON d.dept_id = j.dept_id \n"
		        		+"     LEFT JOIN ( \n"
		        		+"			select distinct e.emp_id as emp_id, e.name as name, e.email as email \n"
		        		+"				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave \n"
		        		+"			from employee e  \n"
		        		+"			left join employee_leave el  \n"
		        		+"				on el.emp_id = e.emp_id  \n"
		        		+"				and leave_status_id in (1,2)  \n"
		        		+"				and manager_approval_status = 'Approved'  \n"
		        		+"				and leave_type_master_id = 5  \n"
		        		+"				and curdate() between date(el.from_date) and date(el.to_date)  \n"
		        		+"		) eld on eld.emp_id = e.emp_id \n"
		        		+"	 WHERE etm.active != 0 \n"
		        		+"	   AND t.is_active != 'N' \n"
		        		+"	   AND p.active != 'false' \n"
		        		+"	   AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		        		+"		 AND p.po_project_type = :po_project_typee \n"
		        		+"		and DATE(p.end_date) between :fromDate and :toDate \n"
		        		+"	   AND p.end_date >= CURRENT_DATE  \n"
		        		+"		 AND d.dept_id IN (:deptId)  \n"
		        		+"         AND ((:leave_filter = true)  \n"
		        		+"			or  \n"
		        		+"		(:leave_filter != true and eld.On_Maternity_Leave = 'No') \n"
		        		+"	) \n"
		        		+"	 GROUP BY \n"
		        		+"		 CASE WHEN p.po_project_type IS NULL THEN 'Internal' ELSE p.po_project_type END, \n"
		        		+"		 e.billable_type \n"
		        		+" ) \n"
		        		+" SELECT \n"
		        		+"	 ac.po_project_type, \n"
		        		+"	 ac.billable_type, \n"
		        		+"	  \n"
		        		+"	 COALESCE(oa.overall_employee, 0) AS total_emp, \n"
		        		+"	 COALESCE(opba.overall_emp_per_billable_type, 0) AS overall_emp_per_billable_type \n"
		        		+" FROM AllCombinations ac \n"
		        		+" LEFT JOIN MainAgg ma ON ac.po_project_type = ma.po_project_type AND ac.billable_type = ma.billable_type \n"
		        		+" LEFT JOIN OverallAgg oa ON ac.po_project_type = oa.po_project_type \n"
		        		+" LEFT JOIN OverallPerBillableAgg opba ON ac.po_project_type = opba.po_project_type AND ac.billable_type = opba.billable_type \n"
		        		+" WHERE 1=1 \n"
		        		+"   and ac.po_project_type = :po_project_typee  \n"
		        		+" AND ac.billable_type = 'TNM' \n"
		        		+" ORDER BY \n"
		        		+"	 ac.po_project_type, ac.billable_type")
		        List<Object[]> fetchactivePOCountsNew(@Param("po_project_typee") String po_project_typee,
		                @Param("deptId") List<Long> deptId,
		                @Param("fromDate") String fromDate,
		                @Param("toDate") String toDate,
		                @Param("leave_filter") boolean maternityleaveFilter);

		        @Query(value = "WITH Authorized_Employees AS (\n"
		        		+ "    SELECT e.emp_id\n"
		        		+ "    FROM employee e\n"
		        		+ "    WHERE EXISTS (\n"
		        		+ "        SELECT 1\n"
		        		+ "        FROM employee u\n"
		        		+ "        JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
		        		+ "        JOIN department d ON jr.dept_id = d.dept_id\n"
		        		+ "        WHERE u.emp_id = :emp_id\n"
		        		+ "          AND (jr.employee_role = 'SuperAdmin'\n"
		        		+ "               OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
		        		+ "    )\n"
		        		+ "\n"
		        		+ "    UNION\n"
		        		+ "\n"
		        		+ "    SELECT e.emp_id\n"
		        		+ "    FROM employee e\n"
		        		+ "    JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
		        		+ "    JOIN department d ON jr.dept_id = d.dept_id\n"
		        		+ "    WHERE d.hod_id = :emp_id\n"
		        		+ "\n"
		        		+ "    UNION\n"
		        		+ "\n"
		        		+ "    SELECT DISTINCT etm.emp_id\n"
		        		+ "    FROM employee_team_mapping etm\n"
		        		+ "    JOIN teams t ON etm.team_id = t.team_id\n"
		        		+ "    JOIN projects p ON t.project_id = p.project_id\n"
		        		+ "    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		        		+ "    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
		        		+ "    WHERE :emp_id = pm.project_manager_id\n"
		        		+ "       OR :emp_id = pom.project_overhead_id\n"
		        		+ "       OR :emp_id = t.spoc_id\n"
		        		+ "       OR :emp_id = t.team_lead_id\n"
		        		+ ")\n"
		        		+ "SELECT e.emp_id,\n"
		        		+ "       e.name,\n"
		        		+ "       CASE\n"
		        		+ "           WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
		        		+ "           ELSE CONCAT('A-', e.employeement_id)\n"
		        		+ "       END AS employeementId\n"
		        		+ "FROM employee e\n"
		        		+ "JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
		        		+ "WHERE e.employmentstatus != 'InActive'\n"
		        		+ "  AND e.emp_id > 6 ",
		                nativeQuery = true)
		        List<Object[]> getEmployeeByNameAndEmpidForTimesheet(@Param("emp_id") Long empId);	
		        
		        @Query(value = " WITH Authorized_Employees AS (\n"
		        		+ "    SELECT e.emp_id\n"
		        		+ "    FROM employee e\n"
		        		+ "    WHERE EXISTS (\n"
		        		+ "        SELECT 1\n"
		        		+ "        FROM employee u\n"
		        		+ "        JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
		        		+ "        JOIN department d ON jr.dept_id = d.dept_id\n"
		        		+ "        WHERE u.emp_id = :empId\n"
		        		+ "          AND (jr.employee_role = 'SuperAdmin' \n"
		        		+ "               OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
		        		+ "    )\n"
		        		+ "\n"
		        		+ "    UNION ALL\n"
		        		+ "\n"
		        		+ "    SELECT e.emp_id\n"
		        		+ "    FROM employee e\n"
		        		+ "    JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
		        		+ "    JOIN department d ON jr.dept_id = d.dept_id\n"
		        		+ "    WHERE d.hod_id = :empId\n"
		        		+ "\n"
		        		+ "    UNION ALL\n"
		        		+ "\n"
		        		+ "    SELECT DISTINCT etm.emp_id\n"
		        		+ "    FROM employee_team_mapping etm\n"
		        		+ "    JOIN teams t ON etm.team_id = t.team_id\n"
		        		+ "    JOIN projects p ON t.project_id = p.project_id\n"
		        		+ "    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		        		+ "    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
		        		+ "    WHERE :empId IN (pm.project_manager_id, pom.project_overhead_id, t.spoc_id, t.team_lead_id)\n"
		        		+ ")\n"
		        		+ "SELECT DISTINCT  \n"
		        		+ "    e.emp_id,\n"
		        		+ "    e.name,\n"
		        		+ "    CASE \n"
		        		+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
		        		+ "        ELSE CONCAT('A-', e.employeement_id)\n"
		        		+ "    END AS employeementId\n"
		        		+ "FROM employee e\n"
		        		+ "JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
		        		+ "JOIN employee_team_mapping etm ON e.emp_id = etm.emp_id AND etm.active = 1\n"
		        		+ "JOIN teams t ON etm.team_id = t.team_id AND t.is_active = 'Y'\n"
		        		+ "JOIN projects p ON t.project_id = p.project_id \n"
		        		+ "    AND p.has_client_side_id = 1 \n"
		        		+ "    AND p.active = 'true'\n"
		        		+ "WHERE e.employmentstatus != 'InActive' \n"
		        		+ "  AND e.emp_id > 6 \n"
		        		+ " ",nativeQuery = true)
		        List<Object[]> getEmployeeByNameAndEmpidForTimesheetClientDashboard( Long empId);	

	@Query("SELECT CASE WHEN e.employmentstatus != 'InActive' THEN true ELSE false END " +
		       "FROM Employee e WHERE e.employeementId = :empId")
	public Boolean isActiveEmployee(@Param("empId") Long empId);
	
	

	@Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO( " +
		       "e.empId, e.name, e.email, e.employmentstatus, e.employeementId, " +
		       "FUNCTION('DATE_FORMAT', e.dateOfJoining, '%Y-%m-%d'), " +
		       "d.name, COALESCE(e.isApmosysProduct, null)) " +
		       "FROM Employee e " +
		       "JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
		       "JOIN Department d ON d.deptId = jr.deptId " +
		       "WHERE e.employmentstatus <> 'InActive' " +
		       "ORDER BY e.name ASC")
		List<EmployeeDTO> findAllEmployeeForPortalConfig();



	@Query("SELECT  e.jobRoleId " +
		       "FROM Employee e WHERE e.empId = :empId")
	public Long getJobRoleId(@Param("empId") Long empId);

	@Query(value = " WITH RECURSIVE\n"
			+ "Authorized_Employees AS (\n"
			+ "        SELECT DISTINCT e.emp_id\n"
			+ "        FROM employee e\n"
			+ "        WHERE (\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee u\n"
			+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "                WHERE u.emp_id = :emp_id\n"
			+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            e.job_role_id IN (\n"
			+ "                SELECT jr.job_role_id\n"
			+ "                FROM job_role jr\n"
			+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
			+ "            )\n"
			+ "            OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "                inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "                inner JOIN project_manager_mapping pm_inner ON p_inner.project_id = pm_inner.project_id\n"
			+ "                WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                  AND  pm_inner.project_manager_id = :emp_id and pm_inner.active = 1\n"
			+ "                    )\n"
			+ "                     OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "                inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "                 inner JOIN project_overhead_mapping pom_inner ON p_inner.project_id = pom_inner.project_id\n"
			+ "                WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                  AND  pom_inner.project_overhead_id = :emp_id and pom_inner.active = 1\n"
			+ "                    )\n"
			+ "                     OR\n"
			+ "            EXISTS (\n"
			+ "                SELECT 1\n"
			+ "                FROM employee_team_mapping etm_inner\n"
			+ "                inner JOIN teams t_inner ON etm_inner.team_id = t_inner.team_id\n"
			+ "             inner JOIN projects p_inner ON t_inner.project_id = p_inner.project_id\n"
			+ "              WHERE etm_inner.emp_id = e.emp_id\n"
			+ "                  AND etm_inner.active = 1 \n"
			+ "                  AND t_inner.is_active = 'Y' \n"
			+ "                  AND p_inner.active = 'true' \n"
			+ "                  AND  (t_inner.spoc_id = :emp_id or t_inner.team_lead_id = :emp_id)\n"
			+ "                    )\n"
			+ "    )),\n"
			+ "		 User_Is_SuperAdmin_Or_Special_Dept AS (\n"
			+ "			 SELECT EXISTS (\n"
			+ "				 SELECT 1\n"
			+ "				 FROM employee u\n"
			+ "				 JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
			+ "				 JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "				 WHERE u.emp_id = :emp_id\n"
			+ "				   AND (jr.employee_role IN ('SuperAdmin')\n"
			+ "				   OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
			+ "			 ) AS is_special_user\n"
			+ "		 ),\n"
			+ "		 \n"
			+ "		 Date_Parameters AS (\n"
			+ "			 SELECT\n"
			+ "				 STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
			+ "				 CASE\n"
			+ "					 WHEN :year = YEAR(CURDATE()) AND :month = MONTH(CURDATE())\n"
			+ "						 THEN CURDATE()\n"
			+ "					 ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
			+ "				 END AS to_date\n"
			+ "		 ),\n"
			+ "		 All_Dates_In_Range AS (\n"
			+ "			 SELECT from_date AS dt FROM Date_Parameters\n"
			+ "			 UNION ALL\n"
			+ "			 SELECT DATE_ADD(dt, INTERVAL 1 DAY)\n"
			+ "			 FROM All_Dates_In_Range, Date_Parameters\n"
			+ "			 WHERE dt < Date_Parameters.to_date\n"
			+ "		 ),\n"
			+ "		 \n"
			+ "		 Distinct_Employee_Project_Instances AS (\n"
			+ "			 SELECT DISTINCT\n"
			+ "				 e.emp_id,\n"
			+ "				 p.project_id\n"
			+ "			 FROM projects p\n"
			+ "			 INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "			 INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "			 INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "			 INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
			+ "             INNER JOIN emp_primary_project_mapping eppm ON etm.emp_id = eppm.emp_id AND p.project_id = eppm.primary_project_id\n"
			+ "			 WHERE etm.active != 0\n"
			+ "			   AND t.is_active != 'N'\n"
			+ "               and eppm.is_mapped = 'Y'\n"
			+ "			   AND p.active != 'false'\n"
			+ "			   AND e.employmentstatus != 'InActive'\n"
			+ "			   AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
			+ "		 ),\n"
			+ "		 \n"
			+ "		 Base_Report_Details AS (\n"
			+ "			 SELECT\n"
			+ "				 depi.emp_id,\n"
			+ "				 e.name AS employee_name,\n"
			+ "				 depi.project_id,\n"
			+ "				 p.project_name,\n"
			+ "				 GROUP_CONCAT(DISTINCT ppd.po_no SEPARATOR ', ') AS po_no,\n"
			+ "				 COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
			+ "				 c.client_name,\n"
			+ "				 t.team_id,\n"
			+ "				 t.team_name,\n"
			+ "				 tl.name AS team_lead_name,\n"
			+ "				 e.billable,\n"
			+ "				 e.billable_type,\n"
			+ "				 e.mobile_no,\n"
			+ "				 e.email,\n"
			+ "				 GROUP_CONCAT(DISTINCT ppd.apmosys_rm SEPARATOR ', ') AS apmosys_rm,\n"
			+ "				 GROUP_CONCAT(DISTINCT ppd.apmosys_rm_email SEPARATOR ', ') AS apmosys_rm_email,\n"
			+ "				 CASE WHEN e.is_apmosys_product = 'true'\n"
			+ "					  THEN CONCAT('AP-', e.employeement_id)\n"
			+ "					  ELSE CONCAT('A-', e.employeement_id) END AS employement_id,\n"
			+ "				 d1.name AS dept_name,\n"
			+ "				 d1.dept_id AS employee_dept_id, \n"
			+ "				 etm.start_date AS start_date, etm.end_date\n"
			+ "			 FROM Distinct_Employee_Project_Instances depi\n"
			+ "			 INNER JOIN employee e ON depi.emp_id = e.emp_id\n"
			+ "			 INNER JOIN projects p ON depi.project_id = p.project_id\n"
			+ "			 LEFT JOIN project_po_details ppd \n"
			+ "			 ON ppd.project_id = p.project_id \n"
		   	+ "		     AND DATE(ppd.po_start_date) <= (SELECT to_date FROM Date_Parameters)"
		    + "			 AND ("
		    + "			 ppd.po_end_date IS NULL"
		    + "			 OR DATE(ppd.po_end_date) >= (SELECT from_date FROM Date_Parameters)"
		    + ")"
			+ "			 INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "			 INNER JOIN employee_team_mapping etm ON depi.emp_id = etm.emp_id\n"
			+ "												  AND depi.project_id IN (SELECT team_project.project_id FROM teams AS team_project WHERE team_project.team_id = etm.team_id)\n"
			+ "			 INNER JOIN teams t ON etm.team_id = t.team_id AND t.project_id = depi.project_id\n"
			+ "             INNER JOIN emp_primary_project_mapping eppm ON etm.emp_id = eppm.emp_id AND p.project_id = eppm.primary_project_id\n"
			+ "			 LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id\n"
			+ "			 LEFT JOIN job_role j1 ON j1.job_role_id = e.job_role_id\n"
			+ "			 LEFT JOIN department d1 ON d1.dept_id = j1.dept_id\n"
			+ "			 WHERE etm.active != 0 AND t.is_active = 'Y' AND p.active = 'true' and eppm.is_mapped = 'Y'\n"
			+ " GROUP BY "
+ " depi.emp_id, "
+ " e.name, "
+ " depi.project_id, "
+ " p.project_name, "
+ " p.po_project_type, "
+ " p.internal_project_type, "
+ " c.client_name, "
+ " t.team_id, "
+ " t.team_name, "
+ " tl.name, "
+ " e.billable, "
+ " e.billable_type, "
+ " e.mobile_no, "
+ " e.email, "
+ " e.is_apmosys_product, "
+ " e.employeement_id, "
+ " d1.name, "
+ " d1.dept_id, "
+ " etm.start_date, "
+ " etm.end_date "
			+ "		 ),\n"
			+ "		 \n"
			+ "		 Project_Managers_Aggregated AS (\n"
			+ "			 SELECT\n"
			+ "				 pm.project_id,\n"
			+ "				 GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS Project_Manager_Names\n"
			+ "			 FROM project_manager_mapping pm\n"
			+ "			 JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "             where pm.active = 1\n"
			+ "			 GROUP BY pm.project_id\n"
			+ "		 ),\n"
			+ "         Employee_Timesheet_Statuses AS (\n"
			+ "        SELECT\n"
			+ "            et.emp_id,\n"
			+ "            et.date,\n"
			+ "            et.timesheet_id,\n"
			+ "            UPPER(dtm.day_type) AS day_type_upper,\n"
			+ "            sm.status,\n"
			+ "            COALESCE(a.team_id, 0) AS activity_team_id \n"
			+ "        FROM employee_timesheets_new et\n"
			+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
			+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
			+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
			+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
			+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "    ),\n"
			+ "Expected_Working_Days_Detail AS (\n"
			+ "    SELECT\n"
			+ "        bpe.emp_id,\n"
			+ "        bpe.project_id,\n"
			+ "        adir.dt AS expected_working_day_date\n"
			+ "    FROM Base_Report_Details bpe\n"
			+ "    CROSS JOIN All_Dates_In_Range adir\n"
			+ "    WHERE adir.dt <= (SELECT to_date FROM Date_Parameters)\n"
			+ "     AND adir.dt >= DATE(bpe.start_date)\n"
			+ "	  AND NOT EXISTS (\n"
			+ "						SELECT 1 FROM Employee_Timesheet_Statuses ets1\n"
			+ "                        WHERE ets1.emp_id = bpe.emp_id\n"
			+ "						AND ets1.date = adir.dt\n"
			+ "                        AND (ets1.day_type_upper LIKE '%LEAVE%'\n"
			+ "						OR ets1.day_type_upper LIKE '%CLIENT%HOLIDAY%'\n"
			+ "						OR ets1.day_type_upper LIKE '%PUBLIC%HOLIDAY%'\n"
			+ "						OR ets1.day_type_upper LIKE '%WEEK%OFF%')\n"
			+ "					)\n"
			+ "),\n"
			+ "actual_timesheet_filled AS\n"
			+ "(\n"
			+ " SELECT DISTINCT\n"
			+ "		bpe.emp_id,\n"
			+ "		bpe.project_id,\n"
			+ "		ets.date AS dt\n"
			+ "	FROM Employee_Timesheet_Statuses ets\n"
			+ "    INNER JOIN Base_Report_Details bpe ON bpe.emp_id = ets.emp_id\n"
			+ "	WHERE ets.date BETWEEN (SELECT from_date FROM Date_Parameters)\n"
			+ "					AND (SELECT to_date FROM Date_Parameters)\n"
			+ "    AND ets.activity_team_id = bpe.team_id \n"
			+ "),\n"
			+ "Combined_Expected_DSR AS (\n"
			+ "				        SELECT emp_id, project_id, expected_working_day_date as dt FROM Expected_Working_Days_Detail\n"
			+ "				        UNION\n"
			+ "				        SELECT emp_id, project_id, dt FROM actual_timesheet_filled\n"
			+ "				    ),\n"
			+ "		    		    Expected_Ishine_Working_Days AS (\n"
			+ "						SELECT\n"
			+ "							emp_id,\n"
			+ "							project_id,\n"
			+ "							COUNT(DISTINCT dt) AS expected_ishine_days\n"
			+ "						FROM Combined_Expected_DSR\n"
			+ "						GROUP BY emp_id, project_id\n"
			+ "					),\n"
			+ "		    		   Ishine_Timesheet_Summary AS (\n"
			+ "						SELECT\n"
			+ "							bpe.emp_id,\n"
			+ "							bpe.project_id,\n"
			+ "							COUNT(DISTINCT\n"
			+ "								CASE\n"
			+ "									WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING', 'LEAVE') AND ets.activity_team_id = bpe.team_id THEN ets.date\n"
			+ "									WHEN ets.day_type_upper IN ('WEEK OFF','COMP OFF', 'PUBLIC HOLIDAY', 'HOLIDAY', 'CLIENT HOLIDAY') THEN ets.date\n"
			+ "									ELSE NULL\n"
			+ "								END\n"
			+ "							) AS filled_ishine_days,\n"
			+ "							COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING', 'LEAVE') AND ets.status = 'Pending' AND ets.activity_team_id = bpe.team_id THEN ets.date END) AS ishine_pending_Days,\n"
			+ "							COUNT(DISTINCT CASE WHEN ets.day_type_upper IN ('WORKING', 'NON-WORKING', 'LEAVE') AND ets.status = 'Approved' AND ets.activity_team_id = bpe.team_id THEN ets.date END) AS ishine_approved_Days\n"
			+ "						FROM Base_Report_Details bpe\n"
			+ "						INNER JOIN Employee_Timesheet_Statuses ets ON bpe.emp_id = ets.emp_id\n"
			+ "						WHERE\n"
			+ "							(\n"
			+ "								(ets.day_type_upper IN ('WORKING', 'NON-WORKING', 'LEAVE') AND ets.activity_team_id = bpe.team_id)\n"
			+ "								OR\n"
			+ "								ets.day_type_upper IN ('WEEK OFF','COMP OFF', 'PUBLIC HOLIDAY', 'HOLIDAY', 'CLIENT HOLIDAY')\n"
			+ "							)\n"
			+ "						GROUP BY bpe.emp_id, bpe.project_id\n"
			+ "    ),\n"
			+ "		 Final_Report_Data AS (\n"
			+ "			 SELECT\n"
			+ "				 brd.emp_id, brd.employee_name AS name, brd.project_id, brd.project_name, brd.po_no, brd.project_type,\n"
			+ "				 brd.client_name, brd.team_id, brd.team_name, brd.team_lead_name,\n"
			+ "				 brd.billable, brd.billable_type, brd.mobile_no, brd.email,\n"
			+ "				 brd.apmosysrm, brd.apmosys_rm_email,\n"
			+ "				 brd.employement_id, brd.dept_name, brd.employee_dept_id,\n"
			+ "				 pm.Project_Manager_Names AS Project_Manager,\n"
			+ "				 COALESCE(eiwd.expected_ishine_days, 0) AS expected_ishine_timesheet_days,\n"
			+ "				 COALESCE(its.filled_ishine_days, 0) AS filled_ishine_timesheet_days,\n"
			+ "				 GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) - (COALESCE(its.ishine_pending_Days, 0) + COALESCE(its.ishine_approved_Days, 0)) ) AS not_filled_ishine_timesheet_days,\n"
			+ "				 COALESCE(its.ishine_pending_Days, 0) AS ishine_pending_Days,\n"
			+ "				 COALESCE(its.ishine_approved_Days, 0) AS ishine_approved_Days,\n"
			+ "				 CASE\n"
			+ "					 WHEN GREATEST(0, COALESCE(eiwd.expected_ishine_days, 0) -\n"
			+ "					 (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))) >= 2 THEN 'Defaulter'\n"
			+ "					 WHEN COALESCE(eiwd.expected_ishine_days, 0) >\n"
			+ "						  (COALESCE(its.ishine_approved_Days, 0) + COALESCE(its.ishine_pending_Days, 0))\n"
			+ "						  OR COALESCE(its.ishine_pending_Days, 0) > 0 THEN 'Pending'\n"
			+ "					 ELSE 'Approved'\n"
			+ "				 END AS employee_status\n"
			+ "			 FROM Base_Report_Details brd\n"
			+ "			 LEFT JOIN Project_Managers_Aggregated pm ON brd.project_id = pm.project_id\n"
			+ "			 LEFT JOIN Expected_Ishine_Working_Days eiwd ON brd.emp_id = eiwd.emp_id AND brd.project_id = eiwd.project_id\n"
			+ "			 LEFT JOIN Ishine_Timesheet_Summary its ON brd.emp_id = its.emp_id \n"
			+ "		 )\n"
			+ "		 select SQL_CALC_FOUND_ROWS name, emp_id, project_name, po_no, client_name, team_name, project_type,\n"
			+ "			 team_lead_name, billable, billable_type, mobile_no, email,\n"
			+ "			 expected_ishine_timesheet_days,\n"
			+ "			 not_filled_ishine_timesheet_days,\n"
			+ "			 ishine_pending_Days,\n"
			+ "			 ishine_approved_Days,\n"
			+ "			 apmosysrm, apmosys_rm_email,\n"
			+ "			 employement_id, dept_name, Project_Manager, project_id, employee_status\n"
			+ "		 FROM Final_Report_Data frd, User_Is_SuperAdmin_Or_Special_Dept uis\n"
			+ "			 WHERE (:status = 'All' OR employee_status = :status)\n"
			+ "             and (:billableType = 'All' OR billable_type = :billableType)\n"
			+ "			   AND (\n"
			+ "			        uis.is_special_user = TRUE \n"
			+ "			        OR frd.employee_dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id) \n"
			+ "			       )\n"
			+ "AND (:employmentId IS NULL OR LOWER(employement_id) LIKE CONCAT('%', :employmentId, '%'))\n"
			+ "AND (:name IS NULL OR LOWER(name) LIKE CONCAT('%', :name, '%'))\n"
			+ "AND (:billable IS NULL OR LOWER(billable) = :billable)\n"
			+ "AND (:billableType2 IS NULL OR LOWER(billable_type) = :billableType2)\n"
			+ "AND (:mobileNo IS NULL OR mobile_no LIKE CONCAT('%', :mobileNo, '%'))\n"
			+ "AND (:email IS NULL OR LOWER(email) LIKE CONCAT('%', :email, '%'))\n"
			+ "AND (:departmentName IS NULL OR LOWER(dept_name) LIKE CONCAT('%', :departmentName, '%'))\n"
			+ "AND (:expectedFillCount IS NULL OR expected_ishine_timesheet_days = :expectedFillCount)\n"
			+ "AND (:clientSideAttendancePendingCount IS NULL OR not_filled_ishine_timesheet_days = :clientSideAttendancePendingCount)\n"
			+ "AND (:clientSideAttendanceApprovedCount IS NULL OR ishine_pending_Days = :clientSideAttendanceApprovedCount)\n"
			+ "AND (:clientSideAttendanceNotFilledCount IS NULL OR ishine_approved_Days = :clientSideAttendanceNotFilledCount)\n"
			+ "AND (:projectName IS NULL OR LOWER(project_name) LIKE CONCAT('%', :projectName, '%'))\n"
			+ "AND (:poNo IS NULL OR LOWER(po_no) LIKE CONCAT('%', :poNo, '%'))\n"
			+ "AND (:projectType IS NULL OR LOWER(project_type) LIKE CONCAT('%', :projectType, '%'))\n"
			+ "AND (:projectManagers IS NULL OR LOWER(Project_Manager) LIKE CONCAT('%', :projectManagers, '%'))\n"
			+ "AND (:clientName IS NULL OR LOWER(client_name) LIKE CONCAT('%', :clientName, '%'))\n"
			+ "AND (:apmosysRm IS NULL OR LOWER(apmosysrm) LIKE CONCAT('%', :apmosysRm, '%'))\n"
			+ "AND (:apmosysRmEmail IS NULL OR LOWER(apmosys_rm_email) LIKE CONCAT('%', :apmosysRmEmail, '%'))\n"
			+ "AND (:clientRm IS NULL)\n"
			+ "AND (:team IS NULL OR LOWER(team_name) LIKE CONCAT('%', :team, '%'))\n"
			+ "AND (:teamLeadName IS NULL OR LOWER(team_lead_name) LIKE CONCAT('%', :teamLeadName, '%'))\n"
			+ "ORDER BY\n"
			+ "    CASE WHEN :sortDirection = 'asc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'name' THEN name\n"
			+ "            WHEN :sortBy = 'billable' THEN billable\n"
			+ "            WHEN :sortBy = 'billable_type' THEN billable_type\n"
			+ "            WHEN :sortBy = 'mobile_no' THEN mobile_no\n"
			+ "            WHEN :sortBy = 'email' THEN email\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'expected_ishine_timesheet_days' THEN expected_ishine_timesheet_days\n"
			+ "            WHEN :sortBy = 'ishine_pending_Days' THEN ishine_pending_Days\n"
			+ "            WHEN :sortBy = 'ishine_approved_Days' THEN ishine_approved_Days\n"
			+ "            WHEN :sortBy = 'not_filled_ishine_timesheet_days' THEN not_filled_ishine_timesheet_days\n"
			+ "            WHEN :sortBy = 'project_name' THEN project_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_type' THEN project_type\n"
			+ "            WHEN :sortBy = 'Project_Manager' THEN project_manager\n"
			+ "            WHEN :sortBy = 'client_name' THEN client_name\n"
			+ "            WHEN :sortBy = 'apmosysrm' THEN apmosysrm\n"
			+ "            WHEN :sortBy = 'apmosys_rm_email' THEN apmosys_rm_email\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'team_lead_name' THEN team_lead_name\n"
			+ "            ELSE name\n"
			+ "        END\n"
			+ "    END ASC,\n"
			+ "    CASE WHEN :sortDirection = 'desc' THEN\n"
			+ "        CASE\n"
			+ "            WHEN :sortBy = 'employement_id' THEN employement_id\n"
			+ "            WHEN :sortBy = 'name' THEN name\n"
			+ "            WHEN :sortBy = 'billable' THEN billable\n"
			+ "            WHEN :sortBy = 'billable_type' THEN billable_type\n"
			+ "            WHEN :sortBy = 'mobile_no' THEN mobile_no\n"
			+ "            WHEN :sortBy = 'email' THEN email\n"
			+ "            WHEN :sortBy = 'departmentName' THEN dept_name\n"
			+ "            WHEN :sortBy = 'expected_ishine_timesheet_days' THEN expected_ishine_timesheet_days\n"
			+ "            WHEN :sortBy = 'ishine_pending_Days' THEN ishine_pending_Days\n"
			+ "            WHEN :sortBy = 'ishine_approved_Days' THEN ishine_approved_Days\n"
			+ "            WHEN :sortBy = 'not_filled_ishine_timesheet_days' THEN not_filled_ishine_timesheet_days\n"
			+ "            WHEN :sortBy = 'project_name' THEN project_name\n"
			+ "            WHEN :sortBy = 'po_no' THEN po_no\n"
			+ "            WHEN :sortBy = 'project_type' THEN project_type\n"
			+ "            WHEN :sortBy = 'Project_Manager' THEN project_manager\n"
			+ "            WHEN :sortBy = 'client_name' THEN client_name\n"
			+ "            WHEN :sortBy = 'apmosysrm' THEN apmosysrm\n"
			+ "            WHEN :sortBy = 'apmosys_rm_email' THEN apmosys_rm_email\n"
			+ "            WHEN :sortBy = 'team' THEN team_name\n"
			+ "            WHEN :sortBy = 'team_lead_name' THEN team_lead_name\n"
			+ "            ELSE name\n"
			+ "        END\n"
			+ "    END DESC\n"
			+ "          LIMIT :offset, :pageSize",nativeQuery = true)
		public List<Object[]> getEmployeeViewForAllEmpAttendanceStatus(@Param("status") String status, @Param("month") Integer month, @Param("year") Integer year,@Param("emp_id") Long emp_id,@Param("billableType") List<String> billableTypes ,
				String employmentId,String name,String billable,String billableType2,Long mobileNo,String email,String departmentName,Integer expectedFillCount,Integer clientSideAttendancePendingCount,
				Integer clientSideAttendanceApprovedCount,Integer clientSideAttendanceNotFilledCount,String projectName,String poNo,String projectType,String projectManagers,String clientName,
				String apmosysRm,String apmosysRmEmail,String clientRm,String team,String teamLeadName,String sortBy,String sortDirection,int offset, int pageSize);

	@Query("SELECT e.isTimesheetLockCheckEnable from Employee e WHERE e.empId = :empId")
	public String getIsLockEnabled(@Param("empId") Long empId);
	
	
	@Query("Select count(*) from Employee e\n"
			+ "INNER JOIN JobRole jr on jr.jobRoleId = e.jobRoleId\n"
			+ "INNER JOIN Department d on d.deptId = jr.deptId\n"
			+ "WHERE e.employmentstatus != 'InActive' and d.deptId IN (:deptIds) and e.empId NOT BETWEEN 1 AND 6")
	Long empCountDepartmentsWise(List<Long> deptIds);

	@Query("SELECT jr.employeeRole, jr.name, d.name " +
			"FROM Employee e " +
			"INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
			"INNER JOIN Department d on d.deptId = jr.deptId " +
			"WHERE e.empId = :empId")
	public List<Object[]> getJrAndJrNameAndDeptNameByEmpId(@Param("empId") Long empId);

	@Query(value = "SELECT COUNT(DISTINCT e.empId) "
			+ "FROM Employee e  \n"
			+ "INNER join JobRole jr on jr.jobRoleId = e.jobRoleId \n"
			+ "INNER join Department d on d.deptId = jr.deptId \n"
			+ "LEFT JOIN Employee em ON em.empId = e.managerId \n"
			+ "WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n"
			+ "                  JOIN Team t ON t.teamId = etm.teamId \n"
			+ "                  JOIN Project p ON p.projectId = t.projectId \n"
			+ "                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n"
			+ " and e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 AND 6 ")
	Long getAllEmployeesCountNotMappedToAnyProject();

	@Query(value = "SELECT COUNT(DISTINCT e.empId) from Employee e  \n"
			+ "INNER join JobRole jr on jr.jobRoleId = e.jobRoleId \n"
			+ "INNER join Department d on d.deptId = jr.deptId \n"
			+ "LEFT JOIN Employee em ON em.empId = e.managerId \n"
			+ "WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n"
			+ "                  JOIN Team t ON t.teamId = etm.teamId \n"
			+ "                  JOIN Project p ON p.projectId = t.projectId \n"
			+ "                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n"
			+ "and e.employmentstatus != 'InActive' and d.deptId IN :deptIds and e.empId NOT BETWEEN 1 AND 6 ")
	Long getAllEmployeesNotMappedToAnyProjectCountByDeptIds(@Param("deptIds") List<Long> deptIds);

	@Query("SELECT jr.deptId "
			+ "FROM Employee e "
			+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId "
			+ "WHERE e.empId = :empId")
	public Optional<Long> getJobRoleIdByEmpId(@Param("empId") Long empId);

	@Query("SELECT COUNT(DISTINCT e.empId) \n"
			+ "FROM Employee e \n"
			+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
			+ "INNER JOIN Department d ON jr.deptId = d.deptId \n"
			+ "LEFT JOIN Employee em ON e.managerId = em.empId \n"
			+ "WHERE 1=1 \n"
			+ "AND e.billableType IS NULL \n"
			+ "AND e.empId NOT BETWEEN 1 AND 6 \n"
			+ "AND e.employmentstatus !='InActive'")
	public Long getAllEmployeesCountWithoutAnyBillable();

	@Query("SELECT COUNT(DISTINCT e.empId)\n"
			+ "FROM Employee e \n"
			+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
			+ "INNER JOIN Department d ON jr.deptId = d.deptId \n"
			+ "LEFT JOIN Employee em ON e.managerId = em.empId \n"
			+ "WHERE 1=1 \n"
			+ "AND e.billableType IS NULL \n"
			+ "AND e.empId NOT BETWEEN 1 AND 6 \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND d.deptId IN :deptIds")
	public Long getAllEmployeesCountWithoutAnyBillableByDeptIds(@Param("deptIds") List<Long> deptIds);

	@Query(value ="select count(distinct e.empId) from Employee e where e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 and 6")
    Optional<Long> getAllEmployeeCount();
	
	@Query(value = "SELECT\n"
			+ "    COUNT(DISTINCT CASE WHEN e.billable_type = 'TNM' THEN e.emp_id ELSE NULL END) AS tnm\n"
			+ "FROM projects p\n"
			+ "INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d ON jr.dept_id = d.dept_id\n"
			+ "LEFT JOIN (\n"
			+ "            SELECT DISTINCT el_e.emp_id AS emp_id,\n"
			+ "                            CASE WHEN el.emp_id IS NULL THEN 'No' ELSE 'Yes' END AS On_Maternity_Leave\n"
			+ "            FROM employee el_e\n"
			+ "            LEFT JOIN employee_leave el\n"
			+ "                ON el.emp_id = el_e.emp_id\n"
			+ "                AND leave_status_id IN (1,2)\n"
			+ "                AND manager_approval_status = 'Approved'\n"
			+ "                AND leave_type_master_id = 5\n"
			+ "                AND CURDATE() BETWEEN DATE(el.from_date) AND DATE(el.to_date)\n"
			+ "        ) eld ON eld.emp_id = e.emp_id\n"
			+ "WHERE p.active = 'true'\n"
			+ "    AND t.is_active = 'Y'\n"
			+ "    AND e.employmentstatus != 'InActive'\n"
			+ "    AND etm.active != 0\n"
			+ "    AND (d.dept_id IN (:deptIds)) \n"
			+ "    AND e.emp_id NOT BETWEEN 1 AND 6\n"
			+ "    AND ((:leave_filter = TRUE)\n"
			+ "            OR\n"
			+ "        (:leave_filter != TRUE AND eld.On_Maternity_Leave = 'No')\n"
			+ "    )\n"
			+ "    AND (\n"
			+ "        :statusFlag IS NULL \n"
			+ "        OR (:statusFlag = 'Active' AND STR_TO_DATE(p.end_date, '%Y-%m-%d') >= CURRENT_DATE)\n"
			+ "        OR (:statusFlag = 'Inactive' AND STR_TO_DATE(p.end_date, '%Y-%m-%d') < CURRENT_DATE)\n"
			+ "    )\n"
			+ "    AND (\n"
			+ "        (:poProjectType IS NULL AND COALESCE(p.po_project_type, 'Internal') = 'Internal')\n"
			+ "        OR (:poProjectType = COALESCE(p.po_project_type, 'Internal'))\n"
			+ "    )\n"
			+ " GROUP BY COALESCE(p.po_project_type, 'Internal')", nativeQuery = true)
    	Integer fetchInactiveTotalPOCount(
    			@Param("poProjectType") String po_project_type,
    			@Param("deptIds") List<Long> deptIds,
    			@Param("leave_filter") boolean maternityleaveFilter,
    			@Param("statusFlag") String statusFlag);
	
	@Query("SELECT e.workLocation from Employee e where e.empId=:empId")
	String getEmployeeWorkLocation(@Param("empId")Long empId);
	@Query(value = "Select name from Employee where empId = :empId")
	public String findNameByEmpID(@Param("empId") Long empId);

	@Query(value = "select j.employeeRole from JobRole j inner join Employee e on j.jobRoleId = e.jobRoleId where e.empId = :empId")
	public String findemployeerole(@Param("empId") Long empId);

	@Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.empId, e.name )"
			+ "FROM Employee e")
	public List<EmployeeDTO> getAllEmployeeAsApiSource();
	
	@Query("SELECT e.name from Employee e where e.empId=:empId")
	String getEmployeeName(@Param("empId")Long empId);
	
	@Query("SELECT e.employeementId from Employee e where e.empId=:empId")
	Long getEmployeeEmployeementId(@Param("empId")Long empId);

	@Query(nativeQuery = true , value = " select count(*) from employee e where (e.manager_id = :empId) and e.employmentstatus != 'InActive' And e.emp_id NOT BETWEEN 1 AND 6")
	public Long countReportiesByManagerId1(Long empId);

	
	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query(value = "SELECT count(*)\n"
		    + "FROM employee_timesheets et\n"
		    + "INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
		    + "INNER JOIN teams t ON t.team_id = etm.team_id\n"
		    + "INNER JOIN projects p ON p.project_id = t.project_id\n"
		    + "INNER JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id\n"
		    + "INNER JOIN activities a ON etam.activity_id = a.activity_id AND a.team_id = t.team_id\n"
		    + "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
		    + "WHERE et.day_type LIKE '%Working%'\n"
		    + "AND (et.status = 'Pending' OR et.status IS NULL)\n"
		    + "AND et.created_on <= :checkDate\n"
		    + "AND et.emp_id = :empId and etm.active = 1 and t.is_active = 'Y'",
		    nativeQuery = true)
		Long countPendingTimesheetsByEmployeeAndDate_old(
		    @Param("empId") Long empId,
		    @Param("checkDate") LocalDate checkDate
		);

	// ========== UPDATED: New query using _new tables ==========
	@Query(value = "SELECT count(*)\n"
		    + "FROM employee_timesheets_new et\n"
		    + "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
		    + "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
		    + "INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id\n"
		    + "INNER JOIN teams t ON t.team_id = etm.team_id\n"
		    + "INNER JOIN projects p ON p.project_id = t.project_id\n"
		    + "INNER JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
		    + "INNER JOIN activities a ON etam.activity_id = a.activity_id AND a.team_id = t.team_id AND p.project_id = etam.project_id\n"
		    + "INNER JOIN employee e ON e.emp_id = et.emp_id\n"
		    + "WHERE dtm.day_type LIKE '%Working%'\n"
		    + "AND (sm.status = 'Pending' OR sm.status IS NULL)\n"
		    + "AND et.created_on <= :checkDate\n"
		    + "AND et.emp_id = :empId and etm.active = 1 and t.is_active = 'Y'",
		    nativeQuery = true)
		Long countPendingTimesheetsByEmployeeAndDate(
		    @Param("empId") Long empId,
		    @Param("checkDate") LocalDate checkDate
		);

		// ========== BACKUP: Original query renamed with _old suffix ==========
		@Query(value = "SELECT DISTINCT p.project_name\n"
				+ "FROM employee_timesheets et \n"
				+ "INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id \n"
				+ "INNER JOIN teams t ON t.team_id = etm.team_id \n"
				+ "INNER JOIN projects p ON p.project_id = t.project_id \n"
				+ "INNER JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id \n"
				+ "INNER JOIN activities a ON etam.activity_id = a.activity_id AND a.team_id = t.team_id \n"
				+ "INNER JOIN employee e ON e.emp_id = et.emp_id \n"
				+ "WHERE et.day_type LIKE '%Working%' \n"
				+ "AND (et.status = 'Pending' OR et.status IS NULL) \n"
				+ "AND et.created_on <= :checkDate \n"
				+ "AND et.emp_id = :empId and etm.active = 1 and t.is_active = 'Y'",
		        nativeQuery = true)
		List<String> findPendingTimesheetProjectNames_old(
		        @Param("empId") Long empId,
		        @Param("checkDate") LocalDate checkDate
		);

		// ========== UPDATED: New query using _new tables ==========
		@Query(value = "SELECT DISTINCT p.project_name\n"
				+ "FROM employee_timesheets_new et \n"
				+ "LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
				+ "LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
				+ "INNER JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id \n"
				+ "INNER JOIN teams t ON t.team_id = etm.team_id \n"
				+ "INNER JOIN projects p ON p.project_id = t.project_id \n"
				+ "INNER JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id \n"
				+ "INNER JOIN activities a ON etam.activity_id = a.activity_id AND a.team_id = t.team_id AND p.project_id = etam.project_id \n"
				+ "INNER JOIN employee e ON e.emp_id = et.emp_id \n"
				+ "WHERE dtm.day_type LIKE '%Working%' \n"
				+ "AND (sm.status = 'Pending' OR sm.status IS NULL) \n"
				+ "AND et.created_on <= :checkDate \n"
				+ "AND et.emp_id = :empId and etm.active = 1 and t.is_active = 'Y'",
		        nativeQuery = true)
		List<String> findPendingTimesheetProjectNames(
		        @Param("empId") Long empId,
		        @Param("checkDate") LocalDate checkDate
		);

// 		@Query(value = "SELECT \n"
//     " DISTINCT e.emp_id \n"
//   FROM 
//     employee e 
//   WHERE 
//     (
//       EXISTS (
//         SELECT 
//           1 
//         FROM 
//           employee u 
//           JOIN job_role jr ON u.job_role_id = jr.job_role_id 
//           JOIN department d ON jr.dept_id = d.dept_id 
//         WHERE 
//           u.emp_id = : emp_id 
//           AND (
//             jr.employee_role IN ('SuperAdmin') 
//             OR d.name IN (
//               'HR', 'Accounts', 'Resource Management Group'
//             )
//           )
//       ) 
//       OR e.job_role_id IN (
//         SELECT 
//           jr.job_role_id 
//         FROM 
//           job_role jr 
//         WHERE 
//           jr.dept_id IN (
//             SELECT 
//               dept_id 
//             FROM 
//               department 
//             WHERE 
//               hod_id = : emp_id
//           )
//       )
//     ")", nativeQuery = true)
// 	List<Long> getAllAuthorizeEmployeeId(@Param("empId") Long empId);

	@Query(
		value =
			"SELECT DISTINCT e.emp_id \n" +
			"FROM employee e \n" +
			"WHERE ( \n" +
			"    EXISTS ( \n" +
			"        SELECT 1 \n" +
			"        FROM employee u \n" +
			"        JOIN job_role jr ON u.job_role_id = jr.job_role_id \n" +
			"        JOIN department d ON jr.dept_id = d.dept_id \n" +
			"        WHERE u.emp_id = :empId \n" +
			"          AND ( \n" +
			"              jr.employee_role IN ('SuperAdmin') \n" +
			"              OR d.name IN ('HR', 'Accounts', 'Resource Management Group') \n" +
			"          ) \n" +
			"    ) \n" +
			"    OR e.job_role_id IN ( \n" +
			"        SELECT jr.job_role_id \n" +
			"        FROM job_role jr \n" +
			"        WHERE jr.dept_id IN ( \n" +
			"            SELECT dept_id \n" +
			"            FROM department \n" +
			"            WHERE hod_id = :empId \n" +
			"        ) \n" +
			"    ) \n" +
			")",
		nativeQuery = true
	)
	List<Long> getAllAuthorizeEmployeeId(@Param("empId") Long empId);

	@Query(value = "SELECT * FROM employee WHERE employmentstatus != 'InActive'", nativeQuery = true)
	List<Employee> findAllActiveEmployeesObject();

		@Query(value = "SELECT new com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO(e.empId, e.name,  \n " +
				"CASE  \n " +
				"  WHEN isConsultant = 'true' THEN CONCAT('CS-', e.employeementId)  \n " +
				"  WHEN isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId)  \n " +
				"  ELSE CONCAT('A-', e.employeementId)  \n " +
				"END)  \n " +
				"FROM Employee e where e.employmentstatus != 'InActive' and e.empId = :empId  and e.empId not between 1 and 6")
		public List<GetEmployeeByNameAndEmpldDTO> getEmployeeNameAndEmploymentIdByEmpIdIn(List<Long> empId);

		// @Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.name, d.deptId, d.name, \n"
		// 		+ "e.isConsultant, e.isApmosysProduct, e.employmentstatus, e.empId, e.employeementId) \n"
		// 		+ "FROM Employee e \n"
		// 		+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
		// 		+ "INNER JOIN Department d ON jr.deptId = d.deptId \n"
		// 		+ "WHERE 1=1 \n"
		// 		+ "AND e.empId NOT BETWEEN 1 AND 6 \n"
		// 		+ "AND e.employmentstatus != 'InActive' \n"
		// 		+ "AND e.empId IN :empIds")
		// public List<EmployeeDTO> getEmployeeDetailsByEmpIds(@Param("empIds") List<Long> empIds);


		@Query(nativeQuery = true, value = " SELECT e.emp_id, \n"
				+ " CASE WHEN e.is_consultant = TRUE THEN CONCAT('CS-', e.employeement_id) \n"
				+ " ELSE CONCAT('A-', e.employeement_id) END AS employmentId, \n"
				+ " e.name, \n"
				+ " CONCAT(FLOOR(e.total_experience), '.', \n"
				+ " LPAD(SUBSTRING_INDEX(e.total_experience, '.', -1), 2, '0')) AS previousExperience, \n"
				+ " CONCAT(FLOOR(TIMESTAMPDIFF(MONTH, e.date_of_joining, CURDATE()) / 12),'.', \n"
				+ " LPAD(MOD(TIMESTAMPDIFF(MONTH, e.date_of_joining, CURDATE()), 12), 2, '0')) AS currentExperience, \n"
				+ " CONCAT(FLOOR("
				+ "         (FLOOR(e.total_experience) * 12 + CAST(LPAD(SUBSTRING_INDEX(e.total_experience, '.', -1), 2, '0') AS UNSIGNED)) + \n"
				+ "         TIMESTAMPDIFF(MONTH, e.date_of_joining, CURDATE())) DIV 12,'.', \n"
				+ "         LPAD(((FLOOR(e.total_experience) * 12 + CAST(LPAD(SUBSTRING_INDEX(e.total_experience, '.', -1), 2, '0') AS UNSIGNED)) + \n"
				+ "             TIMESTAMPDIFF(MONTH, e.date_of_joining, CURDATE()) \n"
				+ "         ) MOD 12, 2,'0' \n"
				+ " )) AS totalExperience, \n"
				+ " e.billable_type, jr.name AS jobRole, d.name AS DepartmentName, e.employmentstatus \n"
				+ " FROM employee e  \n"
				+ " INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
				+ " INNER JOIN department d ON d.dept_id = jr.dept_id \n"
				+ " WHERE e.emp_id IN :empIds")
		public List<Object[]> getEmployeeInformationIn(List<Long> empIds);

		@Query(nativeQuery = true, value = " SELECT DISTINCT e.emp_id, \n"
				+ " CASE WHEN e.is_consultant = TRUE THEN CONCAT('CS-', e.employeement_id) \n"
				+ " ELSE CONCAT('A-', e.employeement_id) END AS employmentId, \n"
				+ " e.name, e.billable_type, jr.name AS jobRole, d.name AS DepartmentName, d.dept_id, eppm.primary_project_id \n"
				+ " FROM employee e  \n"
				+ " INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n"
				+ " INNER JOIN department d ON d.dept_id = jr.dept_id \n"
				+ " LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id and eppm.is_mapped = 'Y' \n"
				+ " WHERE e.emp_id NOT IN (1,2,3,4,5,6) and e.employmentstatus!='InActive' \n"
				+ " order by e.name ")
		public List<Object[]> getAllActiveEmployeeInformation();

		    
		@Query(value = "select distinct e.empId, e.name,e.email, em.email, h.email \n" +
				"from Employee e \n" +
				"inner join JobRole j on j.jobRoleId = e.jobRoleId \n" +
				"inner join Department d on d.deptId = j.deptId \n" +
				"inner join Employee h on h.empId = d.hodId \n" +
				"LEFT join Employee em on em.empId = e.managerId \n" +
				"where e.empId IN :empIds ")
		public List<Object[]> findMailIdsForProjectMappingByEmpIds(List<Long> empIds);

		@Query(value = "select DISTINCT new com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO( \n" +
				"e.empId,e.employeementId,e.name,e.jobRoleId,j.name ,d.deptId,d.name, e.isConsultant) \n" +
				"from Employee e \n" +
				"inner join JobRole j on j.jobRoleId = e.jobRoleId \n" +
				"inner join Department d on d.deptId = j.deptId \n" +
				"where e.empId IN :empIds ")
		public List<EmployeeDetailsForTeamMemberDTO> getEmployeeDetailsAndDeptIdForTeam(List<Long> empIds);
		
		@Query(value = "select DISTINCT new com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO( \n"
				+ "e.empId, e.employeementId, e.name, e.jobRoleId, j.name, etm.empTeamDepartmentId, d.name, e.isConsultant) \n"
				+ "from Employee e " 
				+ "left join JobRole j on j.jobRoleId = e.jobRoleId \n" 
				+ "left join EmployeeTeamMap etm on etm.empId = e.empId and etm.active != 0 "
				+ "left join Team t on t.teamId = etm.teamId " + "left join JobRole jr on jr.jobRoleId = e.jobRoleId "
				+ "left join Department d on d.deptId = etm.empTeamDepartmentId \n"
				+ "where e.empId IN :empIds and t.teamId =:teamId ")
		public List<EmployeeDetailsForTeamMemberDTO> getEmployeeDetailsAndEtmDeptIdForTeamByTeamId(List<Long> empIds,
				Long teamId);
		
	// -----------------------------------------------Below this are the mew inner joined queries-----------------------------------------
		@Query(value = "select new com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse(ppd.poNo, p.poProjectId, p.projectId, p.projectName, e.empId, e.employeementId, e.name, e.billableType, d.name, jr.name " +
		",(select count(etn) from EmployeeTimesheetsNew etn where etn.date between :startDate and :endDate and etn.empId = e.empId) " +
		",etm.active) " +
		"from Employee e " +
		"left join EmployeeTeamMap etm on etm.empId = e.empId " +
		"left join Team t on t.teamId = etm.teamId " +
		"left join JobRole jr on jr.jobRoleId = e.jobRoleId " +
		"left join Project p on p.projectId = t.projectId " +
		"left join Department d on d.deptId = jr.deptId " +
		"left join ProjectPoDetails ppd " +
  		"on ppd.projectId = p.projectId " +
  		"and (ppd.poStartDate <= CURRENT_TIMESTAMP and ppd.poEndDate >= CURRENT_TIMESTAMP)" +
		"where ((e.employmentstatus != 'InActive') OR e.dateOfRelieving between :startDate and :endDate) and ((:listType = 'Billable' AND e.billableType in ('TNM','Fixed Cost')) " +
		"and e.empId not between 1 and 6 " +
		"or (:listType = 'Non-Billable' and e.billableType in('InternalRNDProducts','Bench','Shadow')))")
List<EmployeeTimesheetProjectResponse> findEmployeeAndTimesheetDetailsWithoutPagination(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,@Param("listType") String listType);


// @Query(value="SELECT e.employeement_id, e.name, \n"
// + " e.billable, e.billable_type, \n"
// + " emp_proj_client.project_name, emp_proj_client.start_date, emp_proj_client.end_date,\n"
// + " emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, \n"
// + " d.name as departmentName, emp_proj_client.po_project_type, \n"
// + " j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id,\n"
// + " emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, \n"
// + " emp_proj_client.effective_end_date FROM employee e\n"
// + " INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
// + " INNER JOIN department d ON d.dept_id = j.dept_id\n"
// + " INNER JOIN employee m ON e.manager_id = m.emp_id\n"
// + " LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id\n"
// + " LEFT JOIN (     \n"
// + " SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
// + " GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
// + " GROUP_CONCAT(DISTINCT p.start_date ORDER BY p.project_id) AS start_date, \n"
// + " GROUP_CONCAT(DISTINCT p.end_date ORDER BY p.project_id) AS end_date,  \n"
// + " GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY p.project_id) AS po_no,  \n"
// + " GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type,\n"
// + " GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
// + " GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location,\n"
// + " GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,  \n"
// + " GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id, \n"
// + " GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id, \n"
// + " GROUP_CONCAT(DISTINCT ppd.client_rm ORDER BY p.project_id) AS clientrm, \n"
// + " GROUP_CONCAT(DISTINCT ppd.apmosys_rm ORDER BY p.project_id) AS apmosysrm,\n"
// + " GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date, \n"
// + " GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date \n"
// + " FROM employee_team_mapping etm     \n"
// + " LEFT JOIN teams t ON t.team_id = etm.team_id \n"
// + " LEFT JOIN projects p ON p.project_id = t.project_id \n"
// + " LEFT JOIN project_po_details ppd  \n" 
// + " ON ppd.project_id = p.project_id  \n"
// + " LEFT JOIN clients c ON c.client_id = p.client_id\n"
// + " LEFT JOIN client_locations cl ON cl.client_id = p.client_id\n"
// + " WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
// + " GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id\n"
// + " WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
// + " and emp_proj_client.po_project_type = :poProjectType\n"
// + " AND emp_proj_client.end_date < CURRENT_DATE\n"
// + "       AND (:days IS NULL OR emp_proj_client.end_date >= CURRENT_DATE - INTERVAL :days DAY)\n"
// + " and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
// + " AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptId))",nativeQuery = true)
// public List<Object[]> fetchInActivePOListOfEmployee(
// 	@Param("poProjectType") String poProjectType,
// 	@Param("days") Integer days,
// 	@Param("deptId") List<Long> deptId);

	@Query(value="SELECT distinct p.project_name, \n"
	+ " ppd.po_no,\n"
	+" p.po_project_type, p.start_date, p.end_date,\n"
	+ " GROUP_CONCAT(DISTINCT ppd.client_rm SEPARATOR ', ') AS clientrm, \n"
	+ " GROUP_CONCAT(DISTINCT ppd.apmosys_rm SEPARATOR ', ') AS apmosysrm, \n"
	+ " c.client_name, p.client_location FROM projects p INNER JOIN teams t ON t.project_id = p.project_id \n"
	+ " INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
	+ " INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	+ " INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
	+ " INNER JOIN department d ON d.dept_id = j.dept_id\n"
	+ " INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	+ " LEFT  JOIN clients c on p.client_id = c.client_id \n"
	+ " LEFT JOIN project_po_details ppd \n"
	+ " ON ppd.project_id = p.project_id \n"
	+ " AND (:days IS NULL \n"
	+ " OR (ppd.po_start_date <= p.end_date \n"
	+"  AND ppd.po_end_date >= CURRENT_DATE - INTERVAL :days DAY)) \n"
	+ " WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6  \n"
	+ " AND d.dept_id IN (:deptId)\n"
	+ " AND po_project_type = :poProjectType \n"
	+ " AND p.end_date < CURRENT_DATE\n"
	+ " AND (:days IS NULL OR p.end_date >= CURRENT_DATE - INTERVAL :days DAY) \n"
	+ " GROUP BY p.project_name, p.po_project_type, p.start_date, p.end_date, c.client_name, p.client_location"
	,nativeQuery = true)
public List<Object[]> fetchInActivePOListOfProject(
	@Param("poProjectType") String poProjectType,
	@Param("days") Integer days,
	@Param("deptId") List<Long> deptId);


// 	@Query(value = "SELECT DISTINCT p.project_name, \n"
// 	+ "GROUP_CONCAT(DISTINCT ppd.po_no SEPARATOR ', ') AS po_no,\n"
// 	+ "p.po_project_type, p.start_date, p.end_date,\n"
// 	+ "GROUP_CONCAT(DISTINCT ppd.client_rm SEPARATOR ', ') AS clientrm,\n"
// 	+ "GROUP_CONCAT(DISTINCT ppd.apmosys_rm SEPARATOR ', ') AS apmosysrm,\n"
// 	+ "c.client_name, p.client_location \n"
// 	+ "FROM projects p \n"
// 	+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
// 	+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
// 	+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
// 	+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
// 	+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
// 	+ "INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
// 	+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
// 	+ "LEFT JOIN project_po_details ppd \n"
// 	+ "ON ppd.project_id = p.project_id \n"
// 	+ "AND (ppd.po_start_date <= :toDate \n"
// 	+ "OR ppd.po_end_date   >= :fromDate) \n"
// 	+ "WHERE etm.active != 0 \n"
// 	+ "    AND t.is_active != 'N' \n"
// 	+ "    AND p.active != 'false' \n"
// 	+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
// 	+ "    AND d.dept_id IN (:deptIds)\n"
// 	+ "    AND p.po_project_type = :poProjectType \n"
// 	+ "    AND p.end_date < CURRENT_DATE\n"
// 	+ "    AND DATE(p.end_date) BETWEEN :fromDate AND :toDate \n"
// 	+ "	GROUP BY p.project_name, p.po_project_type, p.start_date, p.end_date, c.client_name, p.client_location", nativeQuery = true)
// public List<Object[]> fetchInActivePOListOfProjectNew(
// 	@Param("poProjectType") String poProjectType,
// 	@Param("deptIds") List<Long> deptIds,
// 	@Param("fromDate") String fromDate,
// 	@Param("toDate") String toDate);


	@Query(value = "SELECT distinct \n"
	+ "    e.emp_id, \n"
	+ "    e.name, \n"
	+ "    emp_proj_client.project_name, \n"
	+ "    emp_proj_client.po_start_date, \n"
	+ "    emp_proj_client.po_end_date, \n"
	+ "    emp_proj_client.po_no, \n"
	+ "    emp_proj_client.client_name, \n"
	+ "    emp_proj_client.client_location, \n"
	+ "    d.name AS departmentName, \n"
	+ "    emp_proj_client.po_project_type, \n"
	+ "    CASE\n"
	+ "        WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
	+ "        WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id)\n"
	+ "        ELSE CONCAT('A-', e.employeement_id)\n"
	+ "    END AS prefixed_employeementId \n"
	+ "FROM employee e\n"
	+ "INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
	+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
	+ "INNER JOIN employee m ON e.manager_id = m.emp_id \n"
	+ "LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id and eppm.is_mapped = 'Y'\n"
	+ "INNER JOIN (     \n"
	+ "	SELECT etm.emp_id,  \n"
	+ "GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,  \n"
	+ "	GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, \n"
	+ "	GROUP_CONCAT(DISTINCT ppd.po_start_date ORDER BY p.project_id) AS po_start_date, \n"
	+ "	GROUP_CONCAT(DISTINCT ppd.po_end_date ORDER BY p.project_id) AS po_end_date,  \n"
	+ "	GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY p.project_id) AS po_no,  \n"
	+ "	 p.po_project_type  AS po_project_type,\n"
	+ "	GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name,\n"
	+ "	GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location, "
	+ "	p.start_date AS start_date, \n"
	+ "	p.end_date  AS end_date  \n"
	+ "	FROM employee_team_mapping etm     \n"
	+ "	INNER JOIN teams t ON t.team_id = etm.team_id \n"
	+ "	INNER JOIN projects p ON p.project_id = t.project_id \n"
	+ " LEFT JOIN project_po_details ppd \n"
	+ " ON ppd.project_id = p.project_id \n"
	+ " AND ( ( :fromDate IS NOT NULL  AND :toDate IS NOT NULL AND DATE(ppd.po_start_date) <= DATE(:toDate) \n"
	+ " AND DATE(ppd.po_end_date)   >= DATE(:fromDate) ) \n"
	+ "or ( :fromDate IS NULL  AND :toDate IS NULL  AND ppd.active = true \n"
	+ " AND Date(ppd.po_end_date) = (SELECT distinct MAX(Date(ppd2.po_end_date)) from project_po_details ppd2 where ppd2.project_id = p.project_id \n"
	+ " and ppd2.active = true )) ) \n"
	+ "	LEFT JOIN clients c ON c.client_id = p.client_id\n"
	+ "	LEFT JOIN client_locations cl ON cl.client_id = p.client_id\n"
	+ "	WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'  \n"
	+ "	GROUP BY etm.emp_id , p.start_date, p.end_date, p.po_project_type \n"
	+ ") emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
	+ "LEFT JOIN (\n"
	+ "	select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
	+ "		  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
	+ "	from employee e \n"
	+ "	left join employee_leave el \n"
	+ "		on el.emp_id = e.emp_id \n"
	+ "		and leave_status_id in (1,2) \n"
	+ "		and manager_approval_status = 'Approved' \n"
	+ "		and leave_type_master_id = 5 \n"
	+ "		and curdate() between date(el.from_date) and date(el.to_date) \n"
	+ "		) eld on eld.emp_id = e.emp_id\n"
	+ "WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL \n"
	+ "and emp_proj_client.po_project_type = :poProjectType\n"
	+ "AND emp_proj_client.end_date < CURRENT_DATE\n"
	+ "AND ( :fromDate is Null or  date(emp_proj_client.end_date) >= date(:fromDate)) and ( :toDate IS NULL OR date(emp_proj_client.end_date) <= date(:toDate) )\n"
	+ "and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM')\n"
	+ "AND ((:leave_filter = true) \n"
	+ "			or \n"
	+ "		(:leave_filter != true and eld.On_Maternity_Leave = 'No')\n"
	+ "	)\n"
	+ "AND ( d.dept_id IN (:deptIds))", nativeQuery = true)
	public List<Object[]> fetchInactivePOListOfEmployeeNew(
			@Param("poProjectType") String poProjectType,
			@Param("deptIds") List<Long> deptIds,
			@Param("fromDate") String fromDate,
			@Param("toDate") String toDate,
			@Param("leave_filter") boolean maternityleaveFilter);


			// @Query(value = "SELECT e.employeement_id, e.name, \n"
		    // 			+"	e.billable, e.billable_type,  \n"
		    // 			+"	emp_proj_client.project_name, emp_proj_client.start_date, emp_proj_client.end_date, \n"
		    // 			+"	emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location,  \n"
		    // 			+"	d.name as departmentName, emp_proj_client.po_project_type,  \n"
		    // 			+"	j.name as jobrole, emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id, \n"
		    // 			+"	emp_proj_client.clientrm, emp_proj_client.apmosysrm, emp_proj_client.effective_start_date,  \n"
		    // 			+"	emp_proj_client.effective_end_date FROM employee e \n"
		    // 			+"	INNER JOIN job_role j ON j.job_role_id = e.job_role_id \n"
		    // 			+"	INNER JOIN department d ON d.dept_id = j.dept_id \n"
		    // 			+"	INNER JOIN employee m ON e.manager_id = m.emp_id \n"
		    // 			+"	LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id and eppm.is_mapped = 'Y' \n"
		    // 			+"	LEFT JOIN (      \n"
		    // 			+"	SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name,   \n"
		    // 			+"	GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT p.start_date ORDER BY p.project_id) AS start_date,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT p.end_date ORDER BY p.project_id) AS end_date,   \n"
		    // 			+"	GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY p.project_id) AS po_no,   \n"
		    // 			+"	GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type, \n"
		    // 			+"	GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name, \n"
		    // 			+"	GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location, \n"
		    // 			+"	GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name,   \n"
		    // 			+"	GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT ppd.client_rm ORDER BY p.project_id) AS clientrm,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT ppd.apmosys_rm ORDER BY p.project_id) AS apmosysrm, \n"
		    // 			+"	GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date,  \n"
		    // 			+"	GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date  \n"
		    // 			+"	FROM employee_team_mapping etm      \n"
		    // 			+"	LEFT JOIN teams t ON t.team_id = etm.team_id  \n"
		    // 			+"	LEFT JOIN projects p ON p.project_id = t.project_id  \n"
			// 			+"  LEFT JOIN project_po_details ppd \n"
			// 			+" 	ON ppd.project_id = p.project_id \n"
			// 			+"  AND ppd.po_start_date <= :toDate \n" 
			// 			+"	AND ppd.po_end_date   >= :fromDate \n"
		    // 			+"	LEFT JOIN clients c ON c.client_id = p.client_id \n"
		    // 			+"	LEFT JOIN client_locations cl ON cl.client_id = p.client_id \n"
		    // 			+"	LEFT JOIN ( \n"
		    // 			+"				select distinct e.emp_id as emp_id, e.name as name, e.email as email \n"
		    // 			+"					  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave \n"
		    // 			+"				from employee e  \n"
		    // 			+"				left join employee_leave el  \n"
		    // 			+"					on el.emp_id = e.emp_id  \n"
		    // 			+"					and leave_status_id in (1,2)  \n"
		    // 			+"					and manager_approval_status = 'Approved'  \n"
		    // 			+"					and leave_type_master_id = 5  \n"
		    // 			+"					and curdate() between date(el.from_date) and date(el.to_date)  \n"
		    // 			+"			) eld on eld.emp_id = etm.emp_id \n"
		    // 			+"	WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'   \n"
		    // 			+"	GROUP BY etm.emp_id ) emp_proj_client ON emp_proj_client.emp_id = e.emp_id \n"
		    // 			+"	WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL  \n"
		    // 			+"	and emp_proj_client.po_project_type = :poProjectType \n"
		    // 			+"	AND emp_proj_client.end_date >= CURRENT_DATE \n"
		    // 			+"	AND date(emp_proj_client.end_date) between :fromDate and :toDate \n"
		    // 			+"	and e.emp_id not between 1 and 6  AND e.billable_type IN ('TNM') \n"
		    // 			+"	AND ((:leave_filter = true)  \n"
		    // 			+"				or  \n"
		    // 			+"			(:leave_filter != true and eld.On_Maternity_Leave = 'No') \n"
		    // 			+"		) \n"
		    // 			+"	AND (j.employee_role = 'SuperAdmin' OR d.dept_id IN (:deptIds))", nativeQuery = true)
		    // 	public List<Object[]> fetchActivePOListOfEmployeeNew(@Param("poProjectType") String poProjectType,
		    // 			@Param("deptIds") List<Long> deptIds,
		    // 			@Param("fromDate") String fromDate,
		    // 			@Param("toDate") String toDate,
		    // 			@Param("leave_filter") boolean maternityleaveFilter);

				@Query(value = "SELECT DISTINCT p.project_name, \n"
		    			+ "GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY p.project_id SEPARATOR ', ') AS po_no, \n"
						+ "p.po_project_type, p.start_date, p.end_date,\n"
		    			+ "GROUP_CONCAT(DISTINCT ppd.client_rm ORDER BY p.project_id SEPARATOR ', ') AS clientrm, \n"
						+ "GROUP_CONCAT(DISTINCT ppd.apmosys_rm ORDER BY p.project_id SEPARATOR ', ') AS apmosysrm, \n"
						+ "c.client_name, p.client_location \n"
		    			+ "FROM projects p \n"
		    			+ "INNER JOIN teams t ON t.project_id = p.project_id \n"
		    			+ "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
		    			+ "INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
		    			+ "INNER JOIN job_role j ON e.job_role_id = j.job_role_id \n"
		    			+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
		    			+ "INNER JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
		    			+ "LEFT JOIN clients c ON p.client_id = c.client_id \n"
						+ "LEFT JOIN project_po_details ppd \n"
						+ "ON ppd.project_id = p.project_id \n"
						+ "AND DATE(ppd.po_start_date ) <= DATE(:toDate ) \n"
						+ "AND DATE(ppd.po_end_date)  >= DATE(:fromDate) \n"
		    			+ "WHERE etm.active != 0 \n"
		    			+ "    AND t.is_active != 'N' \n"
		    			+ "    AND p.active != 'false' \n"
		    			+ "    AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		    			+ "    AND d.dept_id IN (:deptIds)\n"
		    			+ "    AND p.po_project_type = :poProjectType \n"
		    			+ "    AND p.end_date >= CURRENT_DATE\n"
		    			+ "    AND DATE(p.end_date) BETWEEN :fromDate AND :toDate \n"
						+ "GROUP BY p.project_name, p.po_project_type, p.start_date, p.end_date, c.client_name, p.client_location", nativeQuery = true)
		    	public List<Object[]> fetchActivePOListOfProjectNew(
		    			@Param("poProjectType") String poProjectType,
		    			@Param("deptIds") List<Long> deptIds,
		    			@Param("fromDate") String fromDate,
		    			@Param("toDate") String toDate);
		    	
		    	
		    	@Query(value="SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END\n"
		    			+ "    FROM Employee e\n"
		    			+ "    WHERE e.empId = :empId\n"
		    			+ "      AND e.name = :name")
		    	boolean existsByEmpIdAndEmployeeName(
		    	        @Param("empId") Long empId,
		    	        @Param("name") String name
		    	);   	

	


	
	
	
	@Query("select distinct jr.deptId " +
		       "from JobRole jr " +
		       "where jr.jobRoleId in :roleIds")
		List<Long> findDeptIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
	
	 List<Long> findJobRoleIdByEmpId(Long empId);
	 
	 @Query("SELECT new com.apmosys.employeeportal.dto.EmployeeJobRoleDept(" +
		        "jr.deptId, " +
		        "jr.jobRoleId) " +
		        "FROM Employee e " +
		        "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
		        "WHERE e.empId = :empId")
		List<EmployeeJobRoleDept> findRoleDeptByEmpId(@Param("empId") Long empId);

	/**
	 * Finds project IDs where the employee is a common team member
	 * (part of employee_team_mapping for projects where the employee is on a team)
	 */
	@Query(value = "SELECT DISTINCT p1.project_id " +
	               "FROM projects p1 " +
	               "INNER JOIN teams t1 ON t1.project_id = p1.project_id " +
	               "INNER JOIN employee_team_mapping etm1 ON etm1.team_id = t1.team_id " +
	               "WHERE etm1.emp_id = :empId " +
	               "  AND p1.active = 'true' " +
	               "  AND t1.is_active = 'Y' " +
	               "  AND etm1.active = 1",
	       nativeQuery = true)
	List<Long> findProjectIdsWhereEmpIsOnTeam(@Param("empId") Long empId);

	/**
	 * Finds project IDs where the employee is a project manager
	 */
	@Query(value = "SELECT DISTINCT p2.project_id " +
	               "FROM projects p2 " +
	               "INNER JOIN teams t2 ON t2.project_id = p2.project_id " +
	               "INNER JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id " +
	               "INNER JOIN project_manager_mapping pm2 ON p2.project_id = pm2.project_id " +
	               "WHERE pm2.project_manager_id = :empId " +
	               "  AND p2.active = 'true' " +
	               "  AND t2.is_active = 'Y' " +
	               "  AND etm2.active != 0",
	       nativeQuery = true)
	List<Long> findProjectIdsWhereEmpIsProjectManager(@Param("empId") Long empId);

	/**
	 * Finds project IDs where the employee is an overhead
	 */
	@Query(value = "SELECT DISTINCT p3.project_id " +
	               "FROM projects p3 " +
	               "INNER JOIN teams t3 ON t3.project_id = p3.project_id " +
	               "INNER JOIN employee_team_mapping etm3 ON etm3.team_id = t3.team_id " +
	               "INNER JOIN project_overhead_mapping pom3 ON p3.project_id = pom3.project_id " +
	               "WHERE pom3.project_overhead_id = :empId " +
	               "  AND p3.active = 'true' " +
	               "  AND t3.is_active = 'Y' " +
	               "  AND etm3.active != 0",
	       nativeQuery = true)
	List<Long> findProjectIdsWhereEmpIsOverhead(@Param("empId") Long empId);

	/**
	 * Finds project IDs where the employee is a team lead OR SPOC
	 */
	@Query(value = "SELECT DISTINCT p.project_id " +
	               "FROM projects p " +
	               "INNER JOIN teams t ON t.project_id = p.project_id " +
	               "INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id " +
	               "WHERE (t.team_lead_id = :empId OR t.spoc_id = :empId) " +
	               "  AND p.active = 'true' " +
	               "  AND t.is_active = 'Y' " +
	               "  AND etm.active != 0",
	       nativeQuery = true)
	List<Long> findProjectIdsWhereEmpIsTeamLeadOrSpoc(@Param("empId") Long empId);

	// ========== Date-aware versions for timesheet filtering ==========
	
	/**
	 * Fetches all team members (employees) for given project IDs.
	 * SELECT statement is the same as Employee.getAllTeamMemberView (old jpa-named-query)
	 * so that TeamsService.getAllTeamMemberView DTO mapping works unchanged.
	 */
	@Query(value = "SELECT DISTINCT " +
	               "e.emp_id, e.name, e.email, jr.name AS jobrolename, e.mobile_no, e.employeement_id, e.employmentstatus, " +
	               "e.manager_id, em.name AS managerName, em.email AS managerEmail, " +
	               "d.hod_id, eh.name AS hodName, eh.email AS hodEmail, d.dept_id, " +
	               "e.is_timesheet_lock_check_enable, e.reporting_manager_id, e.approvals_to, " +
	               "rm.name AS reportingManager, rm.email AS reportingManagerEmail, " +
	               "e.date_of_joining, e.probation_period, e.is_consultant, e.is_apprenticeship, e.is_apmosys_product, " +
	               "GROUP_CONCAT(DISTINCT ecsm.client_side_id ORDER BY ecsm.client_side_id SEPARATOR ', ') AS client_side_ids, " +
	               "CASE " +
	               "  WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id) " +
	               "  WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) " +
	               "  ELSE CONCAT('A-', e.employeement_id) " +
	               "END " +
	               "FROM employee e " +
	               "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id " +
	               "INNER JOIN department d ON d.dept_id = jr.dept_id " +
	               "INNER JOIN employee em ON e.manager_id = em.emp_id " +
	               "INNER JOIN employee eh ON d.hod_id = eh.emp_id " +
	               "LEFT JOIN employee rm ON e.reporting_manager_id = rm.emp_id " +
	               "LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.active = TRUE " +
	               "INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id " +
	               "INNER JOIN teams t ON t.team_id = etm.team_id " +
	               "INNER JOIN projects p ON p.project_id = t.project_id " +
	               "WHERE e.employmentstatus != 'InActive' " +
	               "  AND p.project_id IN (:projectIds) " +
	               "  AND p.active = 'true' AND t.is_active = 'Y' AND etm.active = 1 " +
	               "  AND e.emp_id != :excludeEmpId " +
	               "GROUP BY " +
	               "  e.emp_id, e.name, e.email, jr.name, e.mobile_no, e.employeement_id, e.employmentstatus, " +
	               "  e.manager_id, em.name, em.email, d.hod_id, eh.name, eh.email, d.dept_id, " +
	               "  e.is_timesheet_lock_check_enable, e.reporting_manager_id, e.approvals_to, " +
	               "  rm.name, rm.email, e.date_of_joining, e.probation_period, e.is_consultant, e.is_apprenticeship, e.is_apmosys_product " +
	               "ORDER BY e.name",
	       nativeQuery = true)
	List<Object[]> getAllTeamMemberViewByProjectIds(@Param("projectIds") List<Long> projectIds, @Param("excludeEmpId") Long excludeEmpId);

	@Query(
		    value = "SELECT EXISTS ( " +
		            "SELECT 1 " +
		            "FROM employee_team_mapping etm " +
		            "INNER JOIN teams t ON t.team_id = etm.team_id " +
		            "INNER JOIN projects p ON p.project_id = t.project_id " +
		            "WHERE etm.emp_id = :empId " +
		            "AND etm.active = 1 " +
		            "AND p.po_project_type = 'TNM' " +
		            "AND p.active = 'true' " +
		            ")",
		    nativeQuery = true
		)
		Integer checkActiveTNMProject(@Param("empId") Long empId);

	@Query("SELECT new com.apmosys.employeeportal.dto.EmployeeDTO(e.name, d.deptId, d.name, \n"
			+ "e.isConsultant, e.isApmosysProduct, e.employmentstatus, e.empId, e.employeementId) \n"
			+ "FROM Employee e \n"
			+ "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
			+ "INNER JOIN Department d ON jr.deptId = d.deptId \n"
			+ "WHERE 1=1 \n"
			+ "AND e.empId NOT BETWEEN 1 AND 6 \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND e.empId IN :empIds")
	public List<EmployeeDTO> getEmployeeDetailsByEmpIds(@Param("empIds") List<Long> empIds);
	
	@Query("SELECT new com.apmosys.employeeportal.dto.EmployeeMailDTO(" +
       "e.empId, " +
       "e.name, " +
       "e.employeementId, " +
       "rr.role) " +
       "FROM Employee e " +
       " JOIN EmployeeTeamMap etm ON etm.empId = e.empId AND etm.active = 1" +
       " JOIN RoleDetails rr ON rr.roleId = etm.roleId " +
       "WHERE e.empId IN :empIds")
	List<EmployeeMailDTO> getEmployeeMailDetails(@Param("empIds") Set<Long> empIds);	
}
