package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.apmosys.employeeportal.dto.EmployeeDTO;	
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
}
