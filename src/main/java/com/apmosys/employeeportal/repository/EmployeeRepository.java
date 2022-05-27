package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

	public Employee findByEmailAndPassword(String email,String password);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeByEmpId(Long empId);
	

}
