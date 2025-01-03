package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeRewardsDTO;
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
	
	@Query(value = "SELECT " +
            "    er.reward_type_name, " +
            "    er.rewarded_to, " +
            "    er.created_on, " +
            "    er.remark, " +
            "    e.updated_by, " +
            "    e.name " +
            "FROM " +
            "    employee_rewards er " +
            "INNER JOIN " +
            "    employee e ON e.emp_id = er.rewarded_to " +
            "WHERE er.rewarded_to = :empId " +
            "  AND (:fromDate IS NULL OR er.from_date >= :fromDate) " +
            "  AND (:toDate IS NULL OR er.to_date <= :toDate)", 
    nativeQuery = true)
	List<Object[]> getRewardByEmpIdWithDateRange(@Param("empId") Long empId, 
                                                    @Param("fromDate") LocalDate fromDate, 
                                                    @Param("toDate") LocalDate toDate);
	
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
	
	@Query(value = "SELECT " +
            "    e.name AS employee_name, " +
            "    a.reward_type_name, " +
            "    a.rewarded_to, " +
            "    a.created_on, " +
            "    cb.name AS created_by_name " +
            "FROM " +
            "    employee_rewards a " +
            "JOIN " +
            "    employee_team_mapping etm ON a.rewarded_to = etm.emp_id " +
            "JOIN " +
            "    employee e ON e.emp_id = etm.emp_id " +
            "JOIN " +
            "    employee cb ON a.created_by = cb.emp_id " + // Joining again for created_by details
            "WHERE a.rewarded_to IN ( " +
            "    SELECT emp_id " +
            "    FROM employee_team_mapping " +
            "    WHERE team_id IN ( " +
            "        SELECT etm.team_id " +
            "        FROM employee_team_mapping etm " +
            "        JOIN teams t ON t.team_id = etm.team_id " +
            "        JOIN projects p ON t.project_id = p.project_id " +
            "        WHERE etm.emp_id = :empId " +
            "          AND etm.active != 0 " +
            "          AND t.is_active != 'N' " +
            "          AND p.active != 'false' " +
            "    ) " +
            ") " +
            "AND a.reward_type IN ( " +
            "    SELECT reward_type " +
            "    FROM employee_rewards " +
            "    WHERE rewarded_to = :empId)", nativeQuery = true)
List<Object[]> getRewardByTeamAndDateRange(@Param("empId") Long empId);

	
	

	
}