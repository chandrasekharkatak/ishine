package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.service.QuestionnaireService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @PostMapping("/createQuestionnaireTemplate/quarter/{quarterId}/department/{departmentId}")
    public ServiceResponse createQuestionnaire(@RequestBody QuestionnaireDTO questionnaireDTO,@PathVariable Long quarterId,@PathVariable Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Questionnaire savedQuestionnaire = questionnaireService.createQuestionnaireTemplate(questionnaireDTO,quarterId,departmentId);
            response.setServiceResponse(questionnaireService.convertToDTO(savedQuestionnaire));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Questionnaire created successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @GetMapping("/getAllQuestionnaires")
    public ServiceResponse getAllQuestionnaires() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Questionnaire> questionnaires = questionnaireService.getAllQuestionnaires();
            List<QuestionnaireDTO> dtos = questionnaires.stream()
                    .map(questionnaire -> questionnaireService.convertToDTO(questionnaire))
                    .collect(Collectors.toList());
            response.setServiceResponse(dtos);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched all questionnaires successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @GetMapping("/getQuestionnaireById/{id}")
    public ServiceResponse getQuestionnaireById(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            Questionnaire questionnaire = questionnaireService.getQuestionnaireById(id);
            response.setServiceResponse(questionnaireService.convertToDTO(questionnaire));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched questionnaire successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @GetMapping("/getQuestionnaireByQuarter/{quarterId}")
    public ServiceResponse getQuestionnairesByQuarter(@PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Questionnaire> questionnaires = questionnaireService.getQuestionsByQuarter(quarterId);
            List<QuestionnaireDTO> dtos = questionnaires.stream()
                    .map(questionnaire -> questionnaireService.convertToDTO(questionnaire))
                    .collect(Collectors.toList());
            response.setServiceResponse(dtos);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched questionnaires by quarter successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }
    @GetMapping("/department/name/{departmentName}")
    public ServiceResponse getQuestionnairesByDepartmentName(@PathVariable String departmentName) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Questionnaire> questionnaires = questionnaireService.getQuestionnairesByDepartmentName(departmentName);
            List<QuestionnaireDTO> dtos = questionnaires.stream()
                    .map(questionnaire -> questionnaireService.convertToDTO(questionnaire))
                    .collect(Collectors.toList());
            response.setServiceResponse(dtos);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched questionnaires by department name successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }
    @GetMapping("/department/{departmentId}/quarter/{quarterId}")
    public ServiceResponse getQuestionnairesByDepartmentAndQuarter(@PathVariable Long departmentId, @PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Questionnaire> questionnaires = questionnaireService.getQuestionnairesByDepartmentAndQuarter(departmentId, quarterId);
            List<QuestionnaireDTO> dtos = questionnaires.stream()
                    .map(questionnaire -> questionnaireService.convertToDTO(questionnaire))
                    .collect(Collectors.toList());
            response.setServiceResponse(dtos);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched questionnaires by department and quarter successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }


    @PutMapping("/updateQuestionnaire/{id}")
    public ServiceResponse updateQuestionnaire(@PathVariable Long id, @RequestBody QuestionnaireDTO questionnaireDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Questionnaire updatedQuestionnaire = questionnaireService.updateQuestionnaire(id, questionnaireDTO);
            response.setServiceResponse(questionnaireService.convertToDTO(updatedQuestionnaire));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Questionnaire updated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @DeleteMapping("/deleteQuestionnaire/{id}")
    public ServiceResponse deleteQuestionnaire(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            questionnaireService.deleteQuestionnaire(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Questionnaire deleted successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }
}