package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
import com.apmosys.employeeportal.model.EmployeeRewards;

@Repository
public interface EmployeeRewardsRepository extends JpaRepository <EmployeeRewards, Long>  {
	
	@Query(nativeQuery = true,value = "SELECT er.reward_id,er.is_active,er.id,er.rewarded_to,e.name AS rewarded_to_name, \n"
			+ "er.reward_type AS rewardtypeId,er.reward_type_name,d.dept_id, \n"
			+ "d.name AS department_name,er.ofmonthyear,rc.category_name FROM \n"
			+ "employee_rewards er LEFT JOIN employee e ON e.emp_id = er.rewarded_to INNER JOIN \n"
			+ "job_role j ON e.job_role_id = j.job_role_id INNER JOIN department d ON d.dept_id = j.dept_id \n"
			+ "INNER JOIN rewards_category rc ON rc.reward_category_id = er.reward_category_id\n"
			+ "WHERE  er.is_active !=0 AND er.reward_category_id = :rewardCategoryid")
	 List<Object[]> fetchEmployeesForHomepage(Long rewardCategoryid);
	 
	 @Query(value = "SELECT \n"
				+ "    er.reward_type_name,\n"
				+ "    er.rewarded_to,\n"
				+ "    er.created_on, \n"
				+ "    er.remark, \n"
				+ "    e.updated_by \n"
				+ "FROM \n"
				+ "    employee_rewards er \n"
				+ "INNER JOIN \n"
				+ "    employee e ON e.emp_id = er.rewarded_to \n"
				+ "where er.rewarded_to =: empId", 
				nativeQuery = true)
		 List<EmployeeRewardsDTO> getRewardByEmpId(Long empId);
		
		@Query(nativeQuery = true)
		List<Object[]> getRewardByEmpIdWithDateRange(Long empId, String ofMonthYear);

		@Query(nativeQuery = true,value="SELECT e.name,er.reward_type_name, er.rewarded_to, er.created_on, er.remark, er.created_by,rc.reward_category_id,rc.category_name,e.emp_id \n"
				+ "				FROM employee_rewards er \n"
				+ "				INNER JOIN employee e ON e.emp_id = er.rewarded_to\n"
				+ "                inner join rewards_category rc on er.reward_category_id = rc.reward_category_id\n"
				+ "				where er.rewarded_to = :empId and er.ofmonthyear = :ofMonthYear")
		List<Object[]> getRewardDetailsByEmpId(Long empId, String ofMonthYear);
		
		@Query(nativeQuery = true, value="select t.team_id,t.team_name \n"
				+ "from teams t \n"
				+ "inner join employee_team_mapping etm on etm.team_id = t.team_id\n"
				+ "where t.is_active != 'N' and etm.active != 0 and etm.emp_id = :empId")
		List<Object[]> getTeamsByEmpId(Long empId);
	
	//  @Query(nativeQuery = true , value = "SELECT er.reward_id,rc.reward_category_id, rc.category_name, er.id ,r.reward_name,r.reward_type,er.reward_type_name,er.rewarded_to,\n"
	// 	 		+ "			   e.name AS rewarded_to_name,e.manager_id,er.ofmonthyear,er.remark\n"
	// 	 		+ "			FROM \n"
	// 	 		+ "			  employee_rewards er\n"
	// 	 		+ "			LEFT JOIN \n"
	// 	 		+ "			  employee e ON e.emp_id = er.rewarded_to\n"
	// 	 		+ "			INNER JOIN \n"
	// 	 		+ "			   reward_config r ON r.id = er.id\n"
	// 	 		+ "			INNER JOIN \n"
	// 	 		+ "			    rewards_category rc ON r.catagory_id = rc.reward_category_id where er.reward_id = :employeerewardId ;")
	// 	 List<Object[]> getAllEmployeeRewardById(Long employeerewardId);

	// 	 @Query(nativeQuery = true)
	// 	 List<Object[]> showAllEmployeeRewards();
 
	//  @Modifying
	//  @Transactional
	//  @Query("UPDATE EmployeeRewards er SET er.isActive = 1 WHERE er.ofmonthyear IN :monthyears")
	//  int bulkEnableRewards(@Param("monthyears") List<String> monthyears);
     
	 
	//  @Modifying
	//  @Transactional
	//  @Query("UPDATE EmployeeRewards er SET er.isActive = 0")
	//  int bulkDisableRewards();

	
//	@Query(nativeQuery = true,value = "SELECT er.reward_id,er.is_active,er.id,er.rewarded_to,e.name AS rewarded_to_name,\n"
//			+ "er.reward_type AS rewardtypeId,r.catagory_id,er.reward_type_name,d.dept_id,\n"
//			+ "d.name AS department_name,e.profile_image_name,er.ofmonthyear,rc.category_name FROM \n"
//			+ "employee_rewards er LEFT JOIN employee e ON e.emp_id = er.rewarded_to INNER JOIN \n"
//			+ "job_role j ON e.job_role_id = j.job_role_id INNER JOIN department d ON d.dept_id = j.dept_id\n"
//			+ "INNER JOIN reward_config r ON r.id = er.id INNER JOIN rewards_category rc ON r.catagory_id = rc.reward_category_id \n"
//			+ "WHERE  er.is_active !=0")
//	 List<Object[]> fetchEmployeesHomepagecurrentmonth();
	
	// @Query(value = "SELECT \n"
	// 		+ "    er.reward_type_name,\n"
	// 		+ "    er.rewarded_to,\n"
	// 		+ "    er.created_on, \n"
	// 		+ "    er.remark, \n"
	// 		+ "    e.updated_by, \n"
	// 		+ "FROM \n"
	// 		+ "    employee_rewards er \n"
	// 		+ "INNER JOIN \n"
	// 		+ "    employee e ON e.emp_id = er.rewarded_to \n"
	// 		+ "where er.rewarded_to =: empId", 
	// 		nativeQuery = true)
	//  List<EmployeeRewardsDTO> getRewardByEmpId(Long empId);
	
	// @Query(value = "SELECT \n" +
    //         "e.name, er.reward_type_name, er.rewarded_to, er.created_on, er.remark, e.updated_by, t.team_name \n" +
    //         "FROM employee_rewards er \n" +
    //         "INNER JOIN employee e ON e.emp_id = er.rewarded_to \n" +
    //         "INNER JOIN employee_team_mapping etm ON etm.emp_id = er.rewarded_to \n" +
    //         "INNER JOIN teams t ON t.team_id = etm.team_id \n" +
    //         "WHERE er.rewarded_to = :empId \n" +
    //         "AND (:fromDate IS NULL OR er.from_date >= :fromDate) \n" +
    //         "AND (:toDate IS NULL OR er.to_date <= :toDate)", nativeQuery = true)
	// List<Object[]> getRewardByEmpIdWithDateRange(@Param("empId") Long empId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
	
//	@Query(value = "SELECT " +
//            "    e.name, " +
//            "    a.reward_type_name, " +
//            "    a.rewarded_to, " +
//            "    a.created_on, " +
//            "    a.created_by, " +
//            "    e.emp_id " +
//            "FROM " +
//            "    employee_rewards a " +
//            "JOIN " +
//            "    employee_team_mapping etm " +
//            "    ON a.rewarded_to = etm.emp_id " +
//            "JOIN " +
//            "    employee e " +
//            "    ON e.emp_id = etm.emp_id " +
//            "WHERE a.rewarded_to IN ( " +
//            "    SELECT emp_id " +
//            "    FROM employee_team_mapping " +
//            "    WHERE team_id IN ( " +
//            "        SELECT etm.team_id " +
//            "        FROM employee_team_mapping etm " +
//            "        JOIN teams t ON t.team_id = etm.team_id " +
//            "        JOIN projects p ON t.project_id = p.project_id " +
//            "        WHERE etm.emp_id = :empId " +
//            "          AND etm.active != 0 " +
//            "          AND t.is_active != 'N' " +
//            "          AND p.active != 'false' " +
//            "    ) " +
//            ") " +
//            "AND a.reward_type IN ( " +
//            "    SELECT reward_type " +
//            "    FROM employee_rewards " +
//            "    WHERE rewarded_to = :empId)", nativeQuery = true)
//	List<Object[]> getRewardByTeamAndDateRange( @Param("empId") Long empId
//														 );
	
	@Query(value = "SELECT e.name,er.reward_type_name, er.rewarded_to, er.created_on, er.remark, er.created_by,rc.reward_category_id,rc.category_name,e.emp_id \n"
			+ "			FROM employee_rewards er \n"
			+ "			INNER JOIN employee e ON e.emp_id = er.rewarded_to\n"
			+ "			inner join employee_team_mapping etm on etm.emp_id = e.emp_id\n"
			+ "			inner join teams t on t.team_id = etm.team_id\n"
			+ "            inner join rewards_category rc on rc.reward_category_id = er.reward_category_id\n"
			+ "			where  etm.active != 0 and er.ofmonthyear = :ofMonthYear and t.team_id = :teamId ", nativeQuery = true)
List<Object[]> getRewardByTeamAndDateRange(String ofMonthYear,Long teamId);


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
 @Query("UPDATE EmployeeRewards er SET er.isActive = 0")
 int bulkDisableRewards();
 
 @Modifying
 @Transactional
 @Query("UPDATE EmployeeRewards er SET er.isActive = 1 WHERE er.ofmonthyear IN :monthyears")
 int bulkEnableRewards(@Param("monthyears") List<String> monthyears);
	
}