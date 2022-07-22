package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{
	
	public Employee findByEmail(String email);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployees();

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByRole();

	@Query(nativeQuery = true)
	public List<Object[]> getAllEmployeesByDepartmentIds(List<Long> deptIds);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamView(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeamMemberView(Long managerId);
    
	@Query(nativeQuery = true)
	public List<Object[]> getAllHolidayByEmpWorkLocation(Long empId);
	

}
