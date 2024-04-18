package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;

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

	@Query(nativeQuery = true , value = " select count(*) from employee e where e.manager_id = :empId or e.reporting_manager_id= :empId and e.employmentstatus != 'InActive'")
	public Long countReportiesByManagerId(Long empId);

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
	
}
