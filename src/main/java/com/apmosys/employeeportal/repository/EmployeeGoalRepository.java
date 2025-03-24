package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeGoals;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoals, Long> {

	List<EmployeeGoals> findByEmpId(Long empId);
	
	@Query(nativeQuery = true,value = "select COUNT(eg.emp_id) from employee_goals eg where emp_id = :empId and quarter_id = :quarterId and goal_status = :goalStatus")
	public long countByEmpIdAndQuarterAndGoalStatus(Long empId, Long quarterId,String goalStatus);

}
