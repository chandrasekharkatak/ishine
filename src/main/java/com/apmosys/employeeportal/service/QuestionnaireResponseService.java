package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QuestionnaireResponseDTO;
import com.apmosys.employeeportal.model.QuestionnaireResponse;
import com.apmosys.employeeportal.repository.QuestionnaireResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionnaireResponseService {

    @Autowired
    private QuestionnaireResponseRepository repository;

    public QuestionnaireResponseDTO saveResponse(QuestionnaireResponseDTO dto) {
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setEmpId(dto.getEmpId());
        response.setQuestionId(dto.getQuestionId());
        response.setQuestionTitle(dto.getQuestionTitle());
        response.setResponse(dto.getResponse());
        response.setQuarter(dto.getQuarter());
        response.setRemarks(dto.getRemarks());
        response.setScore(dto.getScore());

        response = repository.save(response);
        dto.setResponseId(response.getResponseId());
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
