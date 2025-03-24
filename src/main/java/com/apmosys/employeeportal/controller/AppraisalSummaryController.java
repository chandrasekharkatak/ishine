package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.AppraisalSummaryDto;
import com.apmosys.employeeportal.dto.SummaryDto;
import com.apmosys.employeeportal.service.AppraisalSummaryService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appraisal-summaries")
public class AppraisalSummaryController {

    @Autowired
    private AppraisalSummaryService appraisalSummaryService;

    @GetMapping("/employee/{empId}/quarter/{quarterId}")
    public ServiceResponse getAppraisalSummary(@PathVariable Long empId, @PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            SummaryDto summaryDto = appraisalSummaryService.getAppraisalSummary(empId, quarterId);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(summaryDto);
            response.setServiceMessage("Appraisal Summary Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Appraisal Summary");
        }
        return response;
    }

    @GetMapping("/calculate/{employeeId}")
    public ServiceResponse calculateAppraisalSummary(@PathVariable Long employeeId) {
        return appraisalSummaryService.calculateAndCreateAppraisalSummary(employeeId);
    }
    
    @GetMapping("/{id}")
    public ServiceResponse getAppraisalSummaryById(@PathVariable Long id) {
        return appraisalSummaryService.getAppraisalSummaryById(id);
    }

    @GetMapping
    public ServiceResponse getAllAppraisalSummaries() {
        return appraisalSummaryService.getAllAppraisalSummaries();
    }
    
    @GetMapping("/employee/{employeeId}")
    public ServiceResponse getAppraisalSummariesByEmployeeId(@PathVariable Long employeeId) {
        return appraisalSummaryService.getAppraisalSummaryByEmployeeId(employeeId);
    }
}
