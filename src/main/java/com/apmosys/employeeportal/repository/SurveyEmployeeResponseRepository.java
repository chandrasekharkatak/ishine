package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.SurveyEmployeeResponse;

public interface SurveyEmployeeResponseRepository extends JpaRepository<SurveyEmployeeResponse, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getSurveyResponseByEmpIdAndSurveyId(Long empId, Long surveyId);

	@Query(nativeQuery = true)
	public List<Object[]> getSurveyAllResponsesBySurveyId(Long surveyId);

	public SurveyEmployeeResponse findByEmpIdAndSurveyQuestionId(Long empId, Long surveyQuestionId);

	@Query(value = "SELECT q.question ,q.options, \n" + 
				"r.response, q.correct_answer, eqrsm.marks_obtained,eqrsm.pass_status,q.option_type, s.cut_off_questions \n" + 
				"FROM survey_employee_response r \n" + 
				"INNER JOIN survey_questions q ON r.survey_question_id = q.survey_question_id \n" + 
				"INNER JOIN surveys s ON q.survey_id = s.survey_id \n" +
				"INNER JOIN employee_quiz_response_status_mapping eqrsm ON eqrsm.response_id = r.survey_employee_response_id\n" +
				"INNER JOIN employee e ON e.emp_id = r.emp_id \n" +
				"WHERE r.emp_id = :empId AND q.survey_id = :quizId order by e.name", nativeQuery = true)
	public List<Object[]> getAllQuizResponsesByQuizIdAndEmpId(@Param("empId") Long empId, @Param("quizId") Long quizId);

	@Query(value = "SELECT e.employeement_id , e.name , r.created_on , q.survey_question_id, q.question ,q.options, \n" + 
				"r.response,e.is_consultant,e.is_apprenticeship,e.is_apmosys_product,eqrsm.marks_obtained,eqrsm.pass_status \n" + 
				"FROM survey_employee_response r \n" + 
				"INNER JOIN survey_questions q ON r.survey_question_id = q.survey_question_id \n" + 
				"INNER JOIN employee_quiz_response_status_mapping eqrsm ON eqrsm.response_id = r.survey_employee_response_id\n" +
				"INNER JOIN employee e ON e.emp_id = r.emp_id \n" +
				"WHERE q.survey_id = :quizId order by e.name", nativeQuery = true)
	public List<Object[]> getAllQuizResponsesByQuizId(@Param("quizId") Long quizId);
}
