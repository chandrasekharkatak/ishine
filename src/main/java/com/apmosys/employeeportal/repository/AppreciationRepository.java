package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.dto.AppreciationDetailsDTO;
import com.apmosys.employeeportal.model.Appreciation;

@Repository
public interface AppreciationRepository extends JpaRepository<Appreciation, Long> {
	
	
	@Query(nativeQuery = true)
	List<Object[]> viewAppreciationInfo(Long appreciationEventId,Long appreciationTo);
	
	
	@Query(nativeQuery = true)
	List<Object[]> getAppreciationByCategories(Long appreciationEventId, String appreciateType);

	@Query(nativeQuery = true)
	List<Object[]> getAppreciateEmployeeByCurrentUser(Long appreciationBy, Long appreciationEventId );
	
	@Query(nativeQuery = true,value = "SELECT * FROM (\r\n"
			+ "  SELECT \r\n"
			+ "   CASE \r\n"
			+ "    WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\r\n"
			+ "    ELSE CONCAT('A-', e.employeement_id)\r\n"
			+ "  END AS formatted_employeement_id,\r\n"
			+ "    e.name,\r\n"
			+ "    d.name AS department,\r\n"
			+ "\r\n"
			+ "    \r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId\r\n"
			+ "      AND appreciation_to = e.emp_id \r\n"
			+ "      AND appriate_type = 'You are my Star') AS starCount,\r\n"
			+ "      \r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId \r\n"
			+ "      AND appreciation_to = e.emp_id \r\n"
			+ "      AND appriate_type = 'You are Gem of a Person') AS gemCount,\r\n"
			+ "\r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId \r\n"
			+ "      AND appreciation_to = e.emp_id \r\n"
			+ "      AND appriate_type = 'You are a Problem Solver') AS problemSolverCount,\r\n"
			+ "\r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId \r\n"
			+ "      AND appreciation_to = e.emp_id\r\n"
			+ "      AND appriate_type = 'You are Supportive') AS supportiveCount,\r\n"
			+ "\r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId \r\n"
			+ "      AND appreciation_to = e.emp_id \r\n"
			+ "      AND appriate_type = 'You are Reliable') AS reliableCount,\r\n"
			+ "\r\n"
			+ "    (SELECT COUNT(*) FROM appreciation \r\n"
			+ "      WHERE appreciation_event_id = :appreciationEventId \r\n"
			+ "      AND appreciation_to = e.emp_id \r\n"
			+ "      AND appriate_type = 'You are a Motivator') AS motivatorCount\r\n"
			+ "\r\n"
			+ "  FROM employee e \r\n"
			+ "  INNER JOIN employee em ON e.manager_id = em.emp_id \r\n"
			+ "  INNER JOIN job_role j ON j.job_role_id = e.job_role_id \r\n"
			+ "  INNER JOIN department d ON d.dept_id = j.dept_id \r\n"
			+ "  LEFT JOIN employee rm ON e.reporting_manager_id = rm.emp_id \r\n"
			+ "  LEFT JOIN designation de ON de.designation_id = e.designation_id\r\n"
			+ ") AS appreciationData\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "WHERE \r\n"
			+ "  starCount > 0 OR \r\n"
			+ "  gemCount > 0 OR \r\n"
			+ "  problemSolverCount > 0 OR \r\n"
			+ "  supportiveCount > 0 OR \r\n"
			+ "  reliableCount > 0 OR \r\n"
			+ "  motivatorCount > 0\r\n"
			+ "")
	List<Object[]> getAllEmployeeAppreciationListByCategory(Long appreciationEventId);
	
	@Query(nativeQuery = true)
	Long countRecievedAppreciationBYcurrentUser(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationByCurrentUser(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_my_Star(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_Gem_of_a_Person(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_A_Problem_Solver(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_Supportive(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_Reliable(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countMyAppreciationType_You_are_a_Motivator(Long appreciationTo);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_my_Star(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_Gem_of_a_Person(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_A_Problem_Solver(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_Supportive(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_Reliable(Long appreciationBy);
	
	@Query(nativeQuery = true)
	Long countSentAppreciationType_You_are_a_Motivator(Long appreciationBy);
	
	@Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_my_star(Long appreciationEventId);
   
    @Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_Gem_of_a_Person(Long appreciationEventId);
   
    @Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_A_Problem_Solver(Long appreciationEventId);
   
    @Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_Supportive(Long appreciationEventId);
   
    @Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_Reliable(Long appreciationEventId);
   
    @Query(nativeQuery = true)
    public Long countTotalAppreciationByIDandType_You_are_a_Motivator(Long appreciationEventId);
    
    @Query(value = "SELECT a.appreciation_date, ae.appreciation_event_name, a.appreciation_by, eb.name AS appreciated_byName, a.appreciation_to, et.name AS appreciated_toName, a.appriate_type, a.comment, eb.emp_id AS appreciated_by_id, et.emp_id AS appreciated_to_id " +
            "FROM appreciation a " +
            "JOIN appreciation_event ae ON a.appreciation_event_id = ae.appreciation_eventid " +
            "JOIN employee eb ON a.appreciation_by = eb.emp_id " +
            "JOIN employee et ON a.appreciation_to = et.emp_id " +
            "WHERE a.appreciation_to = :employeementId " +
            "AND (COALESCE(:startDate, '') = '' OR COALESCE(:endDate, '') = '' OR DATE(a.appreciation_date) BETWEEN :startDate AND :endDate)"+
            "ORDER BY a.appreciation_date DESC",
    nativeQuery = true)
List<Object[]> getMyAppreciationDetails(@Param("startDate") String startDate, 
                                   @Param("endDate") String endDate, 
                                   @Param("employeementId") Long employmentId);


@Query(value="SELECT \r\n"
		+ "    a.appreciation_date, \r\n"
		+ "    ae.appreciation_event_name, \r\n"
		+ "    a.appreciation_by, \r\n"
		+ "    eb.name AS appreciated_byName, \r\n"
		+ "    a.appreciation_to, \r\n"
		+ "    et.name AS appreciated_toName, \r\n"
		+ "    a.appriate_type, \r\n"
		+ "    a.comment, eb.emp_id AS appreciated_by_id, et.emp_id AS appreciated_to_id  \r\n"
		+ "FROM appreciation a \r\n"
		+ "JOIN appreciation_event ae ON a.appreciation_event_id = ae.appreciation_eventid \r\n"
		+ "JOIN employee eb ON a.appreciation_by = eb.emp_id \r\n"
		+ "JOIN employee et ON a.appreciation_to = et.emp_id \r\n"
		+ "WHERE a.appreciation_to IN ( \r\n"
		+ "    SELECT e.emp_id \r\n"
		+ "    FROM employee e \r\n"
		+ "    WHERE e.emp_id IN ( \r\n"
		+ "        SELECT etm.emp_id \r\n"
		+ "        FROM employee_team_mapping etm \r\n"
		+ "        WHERE etm.team_id IN ( \r\n"
		+ "            SELECT etm2.team_id \r\n"
		+ "            FROM employee_team_mapping etm2 \r\n"
		+ "            WHERE etm2.emp_id = :currentUserEmpId\r\n"
		+ "        ) \r\n"
		+ "    ) \r\n"
		+ ") \r\n"
		+ "AND (COALESCE(:startDate, '') = '' OR COALESCE(:endDate, '') = '' OR DATE(a.appreciation_date) BETWEEN :startDate AND :endDate)"+
           "ORDER BY a.appreciation_date DESC",nativeQuery = true)
	List<Object[]> getTeamAppreciationDetails(@Param("startDate") String startDate, 
	                                          @Param("endDate") String endDate, 
	                                          @Param("currentUserEmpId") Long currentUserEmpId);
	
	
	
	
	@Query( nativeQuery=true, value="SELECT e.employeement_id FROM employee e where e.emp_id = :empId ;")
	public Long findEmployeementIdByEmpId(@Param("empId")Long empId);
    
	
	@Query("SELECT new com.apmosys.employeeportal.dto.AppreciationDetailsDTO( " +
		       "emp.empId, a.appreciateType, a.appreciationDate, emp.name, e.fromDate, e.toDate) " +
		       "FROM com.apmosys.employeeportal.model.Appreciation a " +
		       "JOIN com.apmosys.employeeportal.model.AppreciationEvent e ON a.appreciationEventId = e.appreciationEventid " +
		       "JOIN com.apmosys.employeeportal.model.Employee emp ON a.appreciationBy = emp.employeementId " +
		       "WHERE a.appreciationTo = :empId")
		List<AppreciationDetailsDTO> getAppreciationDetailsByEmpId(@Param("empId") Long empId);
	
	@Query(
			"SELECT DISTINCT new com.apmosys.employeeportal.dto.AppreciationDetailsDTO( " +
			"e.empId, a.appreciateType, a.appreciationDate, e.name, ae.fromDate, ae.toDate) " +
			"FROM com.apmosys.employeeportal.model.Appreciation a " +
			"JOIN com.apmosys.employeeportal.model.AppreciationEvent ae ON a.appreciationEventId = ae.appreciationEventid " +
			"JOIN com.apmosys.employeeportal.model.Employee e ON e.employeementId=a.appreciationTo "+
			"JOIN com.apmosys.employeeportal.model.EmployeeTeamMap etm ON e.empId = etm.empId " + 
			"WHERE a.appreciationTo in (SELECT employeementId FROM Employee WHERE empId  IN (SELECT empId FROM EmployeeTeamMap WHERE teamId IN " +
			"(SELECT teamId FROM EmployeeTeamMap WHERE empId  IN( SELECT empId FROM Employee e3 WHERE  e3.employeementId = :empId) ))) " +
			"AND a.appreciateType IN (SELECT appreciateType FROM Appreciation WHERE appreciationTo = :empId) "
			)
		List<AppreciationDetailsDTO> getTeamAppreciationDetailsByEmpId(@Param("empId") Long empId);
	
	@Query("SELECT e.fromDate AS fromDate, e.toDate AS toDate " +
		       "FROM Appreciation a " +
		       "JOIN AppreciationEvent e ON a.appreciationEventId = e.appreciationEventid " +
		       "WHERE a.appreciationTo = :empId")
		List<Object[]> getAllDateRangesByEmpId(@Param("empId") Long empId);

	@Query("SELECT new com.apmosys.employeeportal.dto.AppreciationDetailsDTO( " +
		       "emp.empId, a.appreciateType, a.appreciationDate, emp.name, e.fromDate, e.toDate) " +
		       "FROM com.apmosys.employeeportal.model.Appreciation a " +
		       "JOIN com.apmosys.employeeportal.model.AppreciationEvent e ON a.appreciationEventId = e.appreciationEventid " +
		       "JOIN com.apmosys.employeeportal.model.Employee emp ON a.appreciationBy = emp.employeementId " +
		       "WHERE a.appreciationTo = :empId " +
		       "AND (e.fromDate >= :fromDate OR :fromDate IS NULL) " +
		       "AND (e.toDate <= :toDate OR :toDate IS NULL)")
		List<AppreciationDetailsDTO> getAppreciationDetailsByEmpIdAndDateRange(@Param("empId") Long empId,
		                                                                        @Param("fromDate") String fromDate,
		                                                                        @Param("toDate") String toDate);
	
	
	
	@Query("Select new com.apmosys.employeeportal.dto.AppreciationDTO ( CASE WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId) ELSE CONCAT('A-', e.employeementId) END,e.name,d.name,ae.appreciationEventName,a.appreciateType )from Appreciation a \r\n"
			+ "inner join  AppreciationEvent ae on ae.appreciationEventid = a.appreciationEventId\r\n"
			+ "inner join Employee e on e.empId = a.appreciationTo\r\n"
			+ "inner join JobRole jr on jr.jobRoleId = e.jobRoleId\r\n"
			+ "inner join Department d on d.deptId = jr.deptId\r\n"
			+ "where a.appreciationBy = :appreciationBy")
	List<AppreciationDTO> appreciationByCurrentUserToEmployees(@Param("appreciationBy") Long appreciationBy);
	
	
	@Query("Select new com.apmosys.employeeportal.dto.AppreciationDTO (CASE WHEN e.isApmosysProduct = 'true' THEN CONCAT('AP-', e.employeementId) ELSE CONCAT('A-', e.employeementId) END,e.name,d.name,ae.appreciationEventName,a.appreciateType )from Appreciation a \r\n"
			+ "inner join  AppreciationEvent ae on ae.appreciationEventid = a.appreciationEventId\r\n"
			+ "inner join Employee e on e.empId = a.appreciationBy\r\n"
			+ "inner join JobRole jr on jr.jobRoleId = e.jobRoleId\r\n"
			+ "inner join Department d on d.deptId = jr.deptId\r\n"
			+ "where a.appreciationTo = :appreciationTo")
	List<AppreciationDTO> appreciationToCurrentUserToEmployees(@Param("appreciationTo") Long appreciationTo);

}
