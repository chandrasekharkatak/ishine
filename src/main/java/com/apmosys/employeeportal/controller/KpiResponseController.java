package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.KpiResponseDTO;
import com.apmosys.employeeportal.service.KpiResponseService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpi-responses")
public class KpiResponseController {

    @Autowired
    private KpiResponseService service;

    @PostMapping("/save")
    public ServiceResponse saveResponse(@RequestBody KpiResponseDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
//        	System.out.println("EMP Id in controller ==="+empId);
            KpiResponseDTO savedDto = service.saveResponse(dto);
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
            List<KpiResponseDTO> responses = service.getResponsesByEmpId(empId);
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

    @GetMapping("/kpi/{kpiId}")
    public ServiceResponse getResponsesByKpiId(@PathVariable Long kpiId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiResponseDTO> responses = service.getResponsesByKpiId(kpiId);
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

    @GetMapping("/quarter/{quarterId}")
    public ServiceResponse getResponsesByQuarterId(@PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiResponseDTO> responses = service.getResponsesByQuarterId(quarterId);
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
        try {
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