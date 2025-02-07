package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
	
	@Query(nativeQuery = true)
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
    
    @Query(value = "SELECT a.appreciation_date, ae.appreciation_event_name, a.appreciation_by, eb.name AS appreciated_byName, a.appreciation_to, et.name AS appreciated_toName, a.appriate_type, a.comment " +
            "FROM appreciation a " +
            "JOIN appreciation_event ae ON a.appreciation_event_id = ae.appreciation_eventid " +
            "JOIN employee eb ON a.appreciation_by = eb.employeement_id " +
            "JOIN employee et ON a.appreciation_to = et.employeement_id " +
            "WHERE (a.appreciation_by = :employeementId OR a.appreciation_to = :employeementId) " +
            "AND DATE(a.appreciation_date) BETWEEN :startDate AND :endDate", 
    nativeQuery = true)
List<Object[]> getMyAppreciationDetails(@Param("startDate") String startDate, 
                                   @Param("endDate") String endDate, 
                                   @Param("employeementId") Long employmentId);


@Query(value="SELECT a.appreciation_date, ae.appreciation_event_name, a.appreciation_by, eb.name AS appreciated_byName, " +
	       "a.appreciation_to, et.name AS appreciated_toName, a.appriate_type, a.comment " +
	       "FROM appreciation a " +
	       "JOIN appreciation_event ae ON a.appreciation_event_id = ae.appreciation_eventid " +
	       "JOIN employee eb ON a.appreciation_by = eb.employeement_id " +
	       "JOIN employee et ON a.appreciation_to = et.employeement_id " +
	       "WHERE a.appreciation_to IN ( " +
	       "   SELECT e.employeement_id FROM employee e WHERE e.emp_id IN ( " +
	       "       SELECT etm.emp_id FROM employee_team_mapping etm WHERE etm.team_id IN ( " +
	       "           SELECT etm2.team_id FROM employee_team_mapping etm2 WHERE etm2.emp_id = :currentUserEmpId " +
	       "       ) " +
	       "   ) " +
	       ") " +
	       "AND a.appreciation_to != :currentUserEmployeementId " + // Exclude the current user from being appreciated
	       "AND a.appreciation_by != :currentUserEmployeementId " + // Exclude the current user from appreciating others
	       "AND DATE(a.appreciation_date) BETWEEN :startDate AND :endDate",nativeQuery = true)
	List<Object[]> getTeamAppreciationDetails(@Param("startDate") String startDate, 
	                                          @Param("endDate") String endDate, 
	                                          @Param("currentUserEmpId") Long currentUserEmpId,
	                                          @Param("currentUserEmployeementId") Long currentUserEmployeementId);
	
	
	
	
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
			"SELECT new com.apmosys.employeeportal.dto.AppreciationDetailsDTO( " +
			"e.empId, a.appreciateType, a.appreciationDate, e.name, ae.fromDate, ae.toDate) " +
			"FROM com.apmosys.employeeportal.model.Appreciation a " +
			"JOIN com.apmosys.employeeportal.model.AppreciationEvent ae ON a.appreciationEventId = ae.appreciationEventid " +
			"JOIN com.apmosys.employeeportal.model.Employee e ON e.employeementId=a.appreciationTo "+
			"JOIN com.apmosys.employeeportal.model.EmployeeTeamMap etm ON e.empId = etm.empId " + 
			"WHERE a.appreciationTo in (SELECT empId FROM EmployeeTeamMap WHERE teamId IN " +
			"(SELECT teamId FROM EmployeeTeamMap WHERE empId = :empId)) " +
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

}
