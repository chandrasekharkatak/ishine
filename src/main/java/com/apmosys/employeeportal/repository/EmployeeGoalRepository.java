package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeGoals;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoals, Long> {

	List<EmployeeGoals> findByEmpId(Long empId);
}
