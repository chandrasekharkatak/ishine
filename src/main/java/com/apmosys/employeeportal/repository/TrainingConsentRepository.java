package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.TrainingResponseDTO;
import com.apmosys.employeeportal.model.TrainingConsent;

@Repository
public interface TrainingConsentRepository extends JpaRepository<TrainingConsent, Long> {
	
	Optional<TrainingConsent> findByConsentId(Long consentId);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.completionCycleNumber = :cycleNumber")
	Optional<TrainingConsent> findByEmpIdAndTrainingIdAndCycleNumber(@Param("empId") Long empId,
																	  @Param("trainingId") Integer trainingId,
																	  @Param("cycleNumber") Integer cycleNumber);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND (:contentId IS NULL OR tc.trainingContent.contentId = :contentId) " +
		   "AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       	   "	(:quizId IS NOT NULL AND tc.quizId = :quizId)) " +
		   "AND tc.completionCycleNumber = :cycleNumber")
	Optional<TrainingConsent> findByEmpIdAndTrainingIdAndContentIdAndQuizIdAndCycleNumber(
			@Param("empId") Long empId,
			@Param("trainingId") Integer trainingId,
			@Param("contentId") Integer contentId,
			@Param("quizId") Long quizId,
			@Param("cycleNumber") Integer cycleNumber);
	
	
	
	@Query(
		    "SELECT CASE WHEN COUNT(tc) > 0 THEN true ELSE false END " +
		    "FROM TrainingConsent tc " +
		    "WHERE tc.empId = :empId " +
		    "AND tc.trainingMaster.trainingId = :trainingId " +
			"AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       	   "(:quizId IS NOT NULL AND tc.quizId = :quizId)) \n"+ 
		    "AND tc.trainingContent.contentId = :contentId"
		)
		boolean existsByEmpIdAndTrainingIdAndContentIdAndQuizId(
		        @Param("empId") Long empId,
		        @Param("trainingId") Integer trainingId,
		        @Param("contentId") Integer contentId,
				@Param("quizId") Long quizId
		);

	
	@Query("SELECT COUNT(DISTINCT tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       "(:quizId IS NOT NULL AND tc.quizId = :quizId)) "+
		   "AND tc.consentTimestamp >= :fromDate")
	Long countCompletionsInLast12Months(@Param("empId") Long empId,
										 @Param("trainingId") Integer trainingId,
										 @Param("quizId") Long quizId,
										 @Param("fromDate") Timestamp fromDate);
	
	@Query("SELECT MAX(tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId")
	Integer findMaxCycleNumber(@Param("empId") Long empId, @Param("trainingId") Integer trainingId);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
	// 	   "AND((:quizId IS NULL AND tc.quizId IS NULL) OR " +
    //    "(:quizId IS NOT NULL AND tc.quizId = :quizId)) " +
		   "ORDER BY tc.consentTimestamp DESC")	
	List<TrainingConsent> findByEmpIdAndTrainingIdAndQuizId(@Param("empId") Long empId, 
																	@Param("trainingId") Integer trainingId);

	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "ORDER BY tc.consentTimestamp DESC")
	List<TrainingConsent> findByEmpIdAndTrainingId(@Param("empId") Long empId, 
																	@Param("trainingId") Integer trainingId);
	@Query("select tc from TrainingConsent tc\n" +
				"where tc.trainingContent.activeStatus = 'true' and tc.trainingMaster.trainingId IN :trainingIds and tc.empId = :empId")
	List<TrainingConsent> findByEmpIdAndTrainingIdsIn(@Param("empId") Long empId, @Param("trainingIds") List<Integer> trainingId);

	@Query("select new com.apmosys.employeeportal.dto.TrainingResponseDTO(" +
    "CONCAT((case when e.isApmosysProduct = 'true' then 'AP-' else 'A-' end), e.employeementId), " +
	"tm.trainingId, " +
	"tm.trainingName, " +
    "MAX(tc.completionCycleNumber), " +
    "e.name, " +
    "tm.consentRequired, " +
    "MAX(tc.consentTimestamp)) " +
    "from TrainingMaster tm, Employee e " +
    "LEFT JOIN TrainingConsent tc ON e.empId = tc.empId AND tc.trainingMaster.trainingId = tm.trainingId " +
    "WHERE tm.trainingId = :trainingId " +
    "AND e.empId NOT IN (1,2,3,4,5,6) AND UPPER(e.employmentstatus) != 'INACTIVE' " +
    "AND ((:type = 'usersAttended' AND tc.empId IS NOT NULL) " +
    "OR (:type = 'usersNotAttended' AND tc.empId IS NULL)) " +
    "GROUP BY e.empId, e.isApmosysProduct, e.employeementId, tm.trainingId, tm.trainingName, tm.consentRequired, e.name")
List<TrainingResponseDTO> findByTrainingId(@Param("trainingId") Integer trainingId, 
                                           @Param("type") String type);
	
	@Query(value = "with \n" +
				"CURRENT_CYCLE as (\n" +
				"  select training_id, training_name, (\n" +
				"    case \n" +
				"      when deadline_pattern like 'YEARLY' then 1\n" +
				"            when deadline_pattern IN ('MID_YEAR', 'YEAR_END') then (\n" +
				"        case \n" +
				"          when month(current_date()) <= 6 then 1\n" +
				"          else 2\n" +
				"        end\n" +
				"            )\n" +
				"            when deadline_pattern like 'QUARTERLY' then (\n" +
				"        case \n" +
				"          when month(current_date()) <= 3 then 1\n" +
				"                    when month(current_date()) <= 6 then 2\n" +
				"                    when month(current_date()) <= 9 then 3\n" +
				"                    else 4\n" +
				"        end\n" +
				"            )\n" +
				"    end\n" +
				"    ) as currentcycleNumber from training_master \n" +
				"    where active_status = 'true' \n" +
				"    and effective_from <= current_date()\n" +
				"    and (effective_to is null or effective_to >= current_date())\n" +
				"),\n" +
				"ACTIVE_QUIZ_ID as (\n" +
				"  select tqm.training_id , tqm.survey_id from training_quiz_mapping tqm\n" +
				"    inner join CURRENT_CYCLE cc on cc.training_id = tqm.training_id\n" +
				"    where tqm.active_status = 'true'\n" +
				"),\n" +
				"ACTIVE_CONTENT_ID as (\n" +
				"  select tc.training_id , tc.content_id  from training_content tc\n" +
				"    inner join CURRENT_CYCLE cc on cc.training_id = tc.training_id\n" +
				"    where tc.active_status = 'true'\n" +
				"),\n" +
				"NOT_ATTENDED as (\n" +
				"  select e.name,e.email, tm.training_id from employee e \n" +
				"    cross join training_master tm\n" +
				"    where \n" +
				"  UPPER(e.employmentstatus) != 'INACTIVE'\n" +
				"  and e.emp_id not between 1 and 6\n" +
				"    and not exists (\n" +
				"    select 1\n" +
				"        from training_consent tc\n" +
				"        inner join CURRENT_CYCLE cc on cc.training_id = tc.training_id\n" +
				"        inner join ACTIVE_CONTENT_ID aci on aci.training_id = tc.training_id and aci.content_id = tc.content_id\n" +
				"        left join ACTIVE_QUIZ_ID aqi on aqi.training_id = tc.training_id \n" +
				"        where tc.emp_id = e.emp_id and tc.training_id = tm.training_id\n" +
				"        and (\n" +
				"      case \n" +
				"        when aqi.survey_id is null then tc.quiz_id is null\n" +
				"        else aqi.survey_id = tc.quiz_id\n" +
				"            end\n" +
				"        )\n" +
				"    )\n" +
				")\n" +
				"\n" +
				"select na.email, cc.training_name, na.name from CURRENT_CYCLE cc\n" +
				"INNER join NOT_ATTENDED na on na.training_id = cc.training_id",nativeQuery=true)
	List<Object[]> findEmpForUnattendedQuiz();

    @Query("SELECT " +
    "COUNT(DISTINCT CASE WHEN tc.empId IS NOT NULL THEN e.empId END) as completedCount, " +
    "COUNT(DISTINCT CASE WHEN tc.empId IS NULL THEN e.empId END) as notCompletedCount, " +
    "COUNT(DISTINCT e.empId) as totalCount " +
    "FROM Employee e " +
    "LEFT JOIN TrainingConsent tc ON e.empId = tc.empId " +
    "AND tc.trainingMaster.trainingId = :trainingId " +
	"WHERE e.empId not in (1,2,3,4,5,6) and UPPER(e.employmentstatus) != 'INACTIVE'")
	List<Object[]> getTrainingCounts(@Param("trainingId") Integer trainingId);
	
}