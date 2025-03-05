package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.service.QuestionnaireService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @PostMapping("/createQuestionnaire")
    public ServiceResponse createQuestionnaire(@RequestBody QuestionnaireDTO questionnaireDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Questionnaire createdQuestionnaire = questionnaireService.createQuestionnaire(questionnaireDTO);
            response.setServiceResponse(createdQuestionnaire);
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
            response.setServiceResponse(questionnaires);
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
            response.setServiceResponse(questionnaire);
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
    public ServiceResponse getQuestionsByQuarter(@PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Questionnaire> questionnaires = questionnaireService.getQuestionsByQuarter(quarterId);
            response.setServiceResponse(questionnaires);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Fetched questionnaires by quarter successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @PutMapping("/{id}")
    public ServiceResponse updateQuestionnaire(@PathVariable Long id, @RequestBody QuestionnaireDTO questionnaireDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Questionnaire updatedQuestionnaire = questionnaireService.updateQuestionnaire(id, questionnaireDTO);
            response.setServiceResponse(updatedQuestionnaire);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Questionnaire updated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }

    @DeleteMapping("/{id}")
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
