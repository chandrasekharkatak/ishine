package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.TrainingIdResponsePassStatusDTO;
import com.apmosys.employeeportal.model.EmployeeQuizResponseStatusMapping;

@Repository
public interface EmployeeQuizResponseStatusMappingRepository extends JpaRepository<EmployeeQuizResponseStatusMapping, Long> {

    EmployeeQuizResponseStatusMapping findByEmployeeIdAndQuizId(Long employeeId, Long quizId);

    @Query("SELECT e FROM EmployeeQuizResponseStatusMapping e WHERE e.passStatus = :passStatus")
    List<EmployeeQuizResponseStatusMapping> findByPassStatus(@Param("passStatus") String passStatus);
 
    @Query("SELECT tqm.trainingMaster.trainingId FROM TrainingQuizMapping tqm   \n"+ 
            "INNER JOIN Survey s on s.surveyId = tqm.survey.surveyId \n"+
            "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON tqm.survey.surveyId = eqrsm.quizId \n"+
            "WHERE s.isActive = 'true' AND tqm.activeStatus = 'true' AND tqm.trainingMaster.trainingId IN :trainingIds")
    List<Integer> getTrainingIdsHavingQuiz(@Param("trainingIds") List<Integer> trainingIds);

    @Query("SELECT new com.apmosys.employeeportal.dto.TrainingIdResponsePassStatusDTO(tqm.trainingMaster.trainingId, eqrsm.passStatus, eqrsm.quizId, eqrsm.marksObtained)  FROM TrainingQuizMapping tqm   \n"+ 
            "INNER JOIN Survey s on s.surveyId = tqm.survey.surveyId \n"+
            "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON tqm.survey.surveyId = eqrsm.quizId \n"+
            "WHERE s.isActive = 'true' AND tqm.activeStatus = 'true' AND tqm.trainingMaster.trainingId IN :trainingIds \n"+
            "AND eqrsm.employeeId = :empId")
    List<TrainingIdResponsePassStatusDTO> getTrainingIdsHavingQuizAndEmpId(@Param("trainingIds") List<Integer> trainingIds, @Param("empId") Long empId);

}
