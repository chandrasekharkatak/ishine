package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.AppraisalSummaryDto;
import com.apmosys.employeeportal.service.AppraisalSummaryService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appraisal-summaries")
public class AppraisalSummaryController {

    @Autowired
    private AppraisalSummaryService appraisalSummaryService;

//    @PostMapping
//    public ServiceResponse createAppraisalSummary(@RequestBody AppraisalSummaryDto appraisalSummaryDto) {
//        return appraisalSummaryService.calculateAndCreateAppraisalSummary();
//    }

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