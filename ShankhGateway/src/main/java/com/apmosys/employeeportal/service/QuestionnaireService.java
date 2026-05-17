package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.QuestionDTO;
import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Question;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QuestionnaireRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
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
        questionnaire.setDepartment(department);
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
    
    public ServiceResponse updateQuestionnaire(Long id, QuestionnaireDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<Questionnaire> existingQuestionnaire = questionnaireRepository.findById(id);
            
            if (existingQuestionnaire.isPresent()) {
                Questionnaire questionnaire = existingQuestionnaire.get();
                
                // Update fields only if they are not null
                if (dto.getQuestionTitle() != null) {
                    questionnaire.setQuestionTitle(dto.getQuestionTitle());
                }
                if (dto.getQuestionDescription() != null) {
                    questionnaire.setQuestionDescription(dto.getQuestionDescription());
                }
                if (dto.getCreatedBy() != null) {
                    questionnaire.setCreatedBy(dto.getCreatedBy());
                }
                if (dto.getQuarterId() != null) {
                    questionnaire.setQuarterId(dto.getQuarterId());
                }
                
                // Validate department if changed
                if (dto.getDepartmentId() != null) {
                    Department department = departmentRepository.findByDeptId(dto.getDepartmentId());
                    if (department == null) {
                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                        response.setServiceMessage("Invalid Department ID");
                        return response;
                    }
                    questionnaire.setDepartmentId(dto.getDepartmentId());
                    questionnaire.setDepartmentName(department.getName());
                }
                
                // Update questions if provided
                if (dto.getQuestions() != null) {
                    questionnaire.getQuestions().clear();
                    
                    for (QuestionDTO questionDTO : dto.getQuestions()) {
                        Question question = new Question();
                        question.setQuestionText(questionDTO.getQuestionText());
                        question.setResponse(questionDTO.getResponse());
                        questionnaire.addQuestion(question);
                    }
                }
                
                Questionnaire updatedQuestionnaire = questionnaireRepository.save(questionnaire);
                // Convert to DTO if you have a conversion method
                QuestionnaireDTO updatedDto = convertToDTO(updatedQuestionnaire);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(updatedDto);
                response.setServiceMessage("Questionnaire Updated Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Questionnaire Not Found");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Updating Questionnaire");
        }
        return response;
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
        dto.setDepartmentName(questionnaire.getDepartmentName());
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