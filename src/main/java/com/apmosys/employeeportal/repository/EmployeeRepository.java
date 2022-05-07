package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{

}
