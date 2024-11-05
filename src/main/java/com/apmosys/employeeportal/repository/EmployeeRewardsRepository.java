package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeRewardForHomeDTO;

import com.apmosys.employeeportal.model.EmployeeRewards;

@Repository
public interface EmployeeRewardsRepository extends JpaRepository <EmployeeRewards, Long>  {
	
	@Query(value = "SELECT \n"
			+ "    er.reward_id,\n"
			+ "    er.is_active,\n"
			+ "    er.id, \n"
			+ "    er.rewarded_to, \n"
			+ "    e.name, \n"
			+ "    er.reward_type as rewardtypeId, \n"
			+ "    r.catagory_id,          \n"
			+ "    er.reward_type_name, \n"
			+ "    d.dept_id, \n"
			+ "    d.name AS department_name, \n"
			+ "    e.profile_image_name \n"
			+ "FROM \n"
			+ "    employee_rewards er \n"
			+ "LEFT JOIN \n"
			+ "    employee e ON e.emp_id = er.rewarded_to \n"
			+ "INNER JOIN \n"
			+ "    job_role j ON e.job_role_id = j.job_role_id \n"
			+ "INNER JOIN \n"
			+ "    department d ON d.dept_id = j.dept_id \n"
			+ "INNER JOIN \n"
			+ "    reward_config r ON r.id = er.id     \n"
			+ "WHERE \n"
			+ "    er.is_active != 0", 
			nativeQuery = true)
	 List<List<Object>> fetchEmployeesForHomepage();
	
}