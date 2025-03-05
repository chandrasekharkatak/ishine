package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.QuestionnaireResponseDTO;
import com.apmosys.employeeportal.service.QuestionnaireResponseService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaire-responses")
public class QuestionnaireResponseController {

    @Autowired
    private QuestionnaireResponseService service;

    @PostMapping("/save")
    public ServiceResponse saveResponse(@RequestBody QuestionnaireResponseDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            QuestionnaireResponseDTO savedDto = service.saveResponse(dto);
            response.setServiceResponse(savedDto);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Response saved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    @GetMapping("/employee/{empId}")
    public ServiceResponse getResponsesByEmpId(@PathVariable Long empId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<QuestionnaireResponseDTO> responses = service.getResponsesByEmpId(empId);
            response.setServiceResponse(responses);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Responses fetched successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    @GetMapping("/goal/{goalId}")
    public ServiceResponse getResponsesByGoalId(@PathVariable Long goalId) {
        ServiceResponse response = new ServiceResponse();
        try 
        {
            List<QuestionnaireResponseDTO> responses = service.getResponsesByGoalId(goalId);
            response.setServiceResponse(responses);
             response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Responses fetched successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/{responseId}")
    public ServiceResponse deleteResponse(@PathVariable Long responseId) {
        ServiceResponse response = new ServiceResponse();
        try 
        {
            service.deleteResponse(responseId);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Response deleted successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}
