package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeRewardForHomeDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
import com.apmosys.employeeportal.model.EmployeeRewards;

@Repository
public interface EmployeeRewardsRepository extends JpaRepository <EmployeeRewards, Long>  {
	
	@Query(nativeQuery = true)
	 List<Object[]> fetchEmployeesForHomepage();
	 
	 @Query(value = "SELECT \n"
				+ "    er.reward_type_name,\n"
				+ "    er.rewarded_to,\n"
				+ "    er.created_on, \n"
				+ "    er.remark, \n"
				+ "    e.updated_by, \n"
				+ "FROM \n"
				+ "    employee_rewards er \n"
				+ "INNER JOIN \n"
				+ "    employee e ON e.emp_id = er.rewarded_to \n"
				+ "where er.rewarded_to =: empId", 
				nativeQuery = true)
		 List<EmployeeRewardsDTO> getRewardByEmpId(Long empId);
		
		@Query(value = "SELECT \n" +
	            "e.name, er.reward_type_name, er.rewarded_to, er.created_on, er.remark, e.updated_by, t.team_name \n" +
	            "FROM employee_rewards er \n" +
	            "INNER JOIN employee e ON e.emp_id = er.rewarded_to \n" +
	            "INNER JOIN employee_team_mapping etm ON etm.emp_id = er.rewarded_to \n" +
	            "INNER JOIN teams t ON t.team_id = etm.team_id \n" +
	            "WHERE er.rewarded_to = :empId \n" +
	            "AND (:fromDate IS NULL OR er.from_date >= :fromDate) \n" +
	            "AND (:toDate IS NULL OR er.to_date <= :toDate)", nativeQuery = true)
		List<Object[]> getRewardByEmpIdWithDateRange(@Param("empId") Long empId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


	
	 @Query(nativeQuery = true , value = "SELECT er.reward_id,rc.reward_category_id, rc.category_name, er.id ,r.reward_name,r.reward_type,er.reward_type_name,er.rewarded_to,\n"
		 		+ "			   e.name AS rewarded_to_name,e.manager_id,er.ofmonthyear,er.remark\n"
		 		+ "			FROM \n"
		 		+ "			  employee_rewards er\n"
		 		+ "			LEFT JOIN \n"
		 		+ "			  employee e ON e.emp_id = er.rewarded_to\n"
		 		+ "			INNER JOIN \n"
		 		+ "			   reward_config r ON r.id = er.id\n"
		 		+ "			INNER JOIN \n"
		 		+ "			    rewards_category rc ON r.catagory_id = rc.reward_category_id where er.reward_id = :employeerewardId ;")
		 List<Object[]> getAllEmployeeRewardById(Long employeerewardId);

		 @Query(nativeQuery = true)
		 List<Object[]> showAllEmployeeRewards();
 
	 @Modifying
	 @Transactional
	 @Query("UPDATE EmployeeRewards er SET er.isActive = 1 WHERE er.ofmonthyear IN :monthyears")
	 int bulkEnableRewards(@Param("monthyears") List<String> monthyears);
     
	 
	 @Modifying
	 @Transactional
	 @Query("UPDATE EmployeeRewards er SET er.isActive = 0")
	 int bulkDisableRewards();

	
}