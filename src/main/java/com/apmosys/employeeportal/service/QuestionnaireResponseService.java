package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QuestionnaireResponseDTO;
import com.apmosys.employeeportal.model.QuaterCycle;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.model.QuestionnaireResponse;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QuestionnaireResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class QuestionnaireResponseService {

    @Autowired
    private QuestionnaireResponseRepository repository;
    
    @Autowired
    private QuarterCycleRepository quarterCycleRepository;
    
    @Transactional
    public QuestionnaireResponseDTO saveResponse(QuestionnaireResponseDTO dto, Long quarterId) {
        String quarter = quarterCycleRepository.findquartercyclebyID(quarterId);
        if (quarter == null) {
            throw new IllegalArgumentException("Quarter not found");
        }

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setEmpId(dto.getEmpId());
        response.setQuestionId(dto.getQuestionId());
        response.setQuestionTitle(dto.getQuestionTitle());
        response.setResponse(dto.getResponse());
        response.setQuarter(quarter);
        response.setRemarks(dto.getRemarks());
        response.setScore(dto.getScore());

        response = repository.save(response);
        dto.setResponseId(response.getResponseId());
        
        System.out.println("+++++++++++++++++++++++++++++++++++++++"+quarter);
        return dto;
    }

    public List<QuestionnaireResponseDTO> getResponsesByEmpId(Long empId) {
        return repository.findByEmpId(empId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<QuestionnaireResponseDTO> getResponsesByGoalId(Long goalId) {
        return repository.findByGoalId(goalId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void deleteResponse(Long responseId) {
        repository.deleteById(responseId);
    }

    private QuestionnaireResponseDTO convertToDTO(QuestionnaireResponse response) {
        QuestionnaireResponseDTO dto = new QuestionnaireResponseDTO();
        dto.setResponseId(response.getResponseId());
        dto.setEmpId(response.getEmpId());
        dto.setQuestionId(response.getQuestionId());
        dto.setQuestionTitle(response.getQuestionTitle());
        dto.setResponse(response.getResponse());
        dto.setQuarter(response.getQuarter());
        dto.setRemarks(response.getRemarks());
        dto.setScore(response.getScore());
        return dto;
    }
}
