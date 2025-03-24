package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QuestionDTO;
import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Question;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionnaireService {
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    @Autowired
    private QuarterCycleRepository quarterCycleRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    
    public Questionnaire createQuestionnaireTemplate(QuestionnaireDTO dto,Long quarterId,Long departmentId) {
    	String quarter = quarterCycleRepository.findquartercyclebyID(quarterId);
    	String department = departmentRepository.findNameByDeptId(departmentId);
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setQuestionTitle(dto.getQuestionTitle());
        questionnaire.setQuestionDescription(dto.getQuestionDescription());
        questionnaire.setCreatedBy(dto.getCreatedBy());
        questionnaire.setQuarterId(quarterId);
        questionnaire.setQuarter(quarter);
        questionnaire.setDepartment(department);
        questionnaire.setDepartmentId(departmentId);
        
        
        // Handle questions if they exist
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (QuestionDTO questionDTO : dto.getQuestions()) {
                Question question = new Question();
                question.setQuestionText(questionDTO.getQuestionText());
                question.setResponse(questionDTO.getResponse());
                questionnaire.addQuestion(question);
            }
        }
        
        
        return questionnaireRepository.save(questionnaire);
    }
   
    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }
  
    public Questionnaire getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questionnaire not found"));
    }

   
    public List<Questionnaire> getQuestionsByQuarter(Long quarterId) {
        return questionnaireRepository.findByQuarterId(quarterId);
    }
    public List<Questionnaire> getQuestionsByQuarterAndDepartment(Long quarterId,Long departmentId) {
        return questionnaireRepository.findByQuarterAndDepartment(quarterId,departmentId);
    }
    
    public Questionnaire updateQuestionnaire(Long id, QuestionnaireDTO dto) {
        Questionnaire questionnaire = getQuestionnaireById(id);
        questionnaire.setQuestionTitle(dto.getQuestionTitle());
        questionnaire.setQuestionDescription(dto.getQuestionDescription());
        questionnaire.setCreatedBy(dto.getCreatedBy());
        questionnaire.setQuarterId(dto.getQuarterId());
        
        // Handle questions if they exist
        if (dto.getQuestions() != null) {
            // Clear existing questions
            questionnaire.getQuestions().clear();
            
            // Add new questions
            for (QuestionDTO questionDTO : dto.getQuestions()) {
                Question question = new Question();
                question.setQuestionText(questionDTO.getQuestionText());
                question.setResponse(questionDTO.getResponse());
                questionnaire.addQuestion(question);
            }
        }
        
        return questionnaireRepository.save(questionnaire);
    }
  
    public void deleteQuestionnaire(Long id) {
        questionnaireRepository.deleteById(id);
    }
    
    // Convert entity to DTO
    public QuestionnaireDTO convertToDTO(Questionnaire questionnaire) {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setQuestionId(questionnaire.getQuestionId());
        dto.setQuestionTitle(questionnaire.getQuestionTitle());
        dto.setQuestionDescription(questionnaire.getQuestionDescription());
        dto.setCreatedBy(questionnaire.getCreatedBy());
        dto.setQuarterId(questionnaire.getQuarterId());
        dto.setQuarter(questionnaire.getQuarter());
        dto.setDepartment(questionnaire.getDepartment());
        dto.setDepartmentId(questionnaire.getDepartmentId());
        
        // Convert questions to DTOs
        if (questionnaire.getQuestions() != null) {
            List<QuestionDTO> questionDTOs = questionnaire.getQuestions().stream()
                .map(q -> new QuestionDTO(q.getId(), q.getQuestionText(),q.getResponse()))
                .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }
        
        return dto;
    }
}