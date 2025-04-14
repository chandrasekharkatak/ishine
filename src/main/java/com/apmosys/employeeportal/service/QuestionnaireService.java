package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.QuestionDTO;
import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Question;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QuestionnaireRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

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
    	String quarter = quarterCycleRepository.findquartercyclebyID(quarterId);//we do not need this
    	String department = departmentRepository.findNameByDeptId(departmentId);//we do not need this
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setQuestionTitle(dto.getQuestionTitle());
        questionnaire.setQuestionDescription(dto.getQuestionDescription());
        questionnaire.setCreatedBy(dto.getCreatedBy());
        questionnaire.setQuarterId(dto.getQuarterId());
        questionnaire.setDepartmentName(dto.getDepartmentName());//we do not need this
        questionnaire.setManagerRating(dto.getManagerRating());
        questionnaire.setManagerRemark(dto.getManagerRemark());
 
        questionnaire.setQuarterId(quarterId);
        questionnaire.setQuarter(quarter);//we do not need this
//        questionnaire.setDepartment(department);
        questionnaire.setDepartmentId(departmentId);
//        use convertto dto and convertto entity
        
        // Handle questions if they exist
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
        	//check the database of deleting 
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
        questionnaire.setDepartmentId(dto.getDepartmentId());
        
        questionnaire.setDepartmentName(dto.getDepartmentName());
 
        if (dto.getQuestions() != null) {
       
            questionnaire.getQuestions().clear();
        
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
        dto.setManagerRating(questionnaire.getManagerRating());
        dto.setManagerRemark(questionnaire.getManagerRemark());
        
        // Convert questions to DTOs
        if (questionnaire.getQuestions() != null) {
            List<QuestionDTO> questionDTOs = questionnaire.getQuestions().stream()
                .map(q -> new QuestionDTO(q.getId(), q.getQuestionText(),q.getResponse()))
                .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }
        
        return dto;
    }

	public List<Questionnaire> getQuestionnairesByDepartmentName(String departmentName) {
		return questionnaireRepository.findByDepartmentName(departmentName);
	}

	public List<Questionnaire> getQuestionnairesByDepartmentAndQuarter(Long departmentId, Long quarterId) {
		return questionnaireRepository.findByQuarterAndDepartment(departmentId , quarterId);
	}
	public List<Questionnaire> getQuestionnairesByDepartmentId(Long departmentId) {
		return questionnaireRepository.findbyDepartmentId(departmentId);
	}
	
	
}