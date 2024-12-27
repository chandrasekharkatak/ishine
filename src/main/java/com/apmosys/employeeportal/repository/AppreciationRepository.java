package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    
    @Query(nativeQuery = true)
    List<Object[]> getMyAppreciationDetails(@Param("startDate") String startDate, 
                                   @Param("endDate") String endDate, 
                                   @Param("employeementId") Long employmentId);


    @Query(nativeQuery = true)
	List<Object[]> getTeamAppreciationDetails(@Param("startDate") String startDate, 
	                                          @Param("endDate") String endDate, 
	                                          @Param("currentUserEmpId") Long currentUserEmpId,
	                                          @Param("currentUserEmployeementId") Long currentUserEmployeementId);
	
	
	
	
	@Query( nativeQuery=true)
	public Long findEmployeementIdByEmpId(@Param("empId")Long empId);
    


}
