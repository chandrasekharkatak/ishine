package com.apmosys.employeeportal.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Encrypted;
import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.CreateTimesheetRequestDTONew;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.service.helper.TimesheetEncryptionHelper;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api/timesheetNew")
public class TimesheetControllerNew {
    
    @Autowired
    TimesheetServiceNew timesheetServiceNew;
    
    @Autowired
    TimesheetEncryptionHelper timesheetEncryptionHelper;
    
    /**
     * CREATE - Create new timesheet with multiple projects
     * POST /api/timesheetNew/create
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Encrypted
    public ServiceResponse createTimesheet(
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "doc1", required = false) MultipartFile doc1,
            @RequestPart(value = "doc2", required = false) MultipartFile doc2) throws Exception {
        
        // Decrypt and parse encrypted DTO
        CreateTimesheetRequestDTONew dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNew(encryptedDto);
        
        return timesheetServiceNew.createTimesheet(dto, doc1, doc2);
    }
    
    /**
     * READ - Get timesheet by ID
     * GET /api/timesheetNew/get/{id}
     */
    @JobRoleAccess(featureIds = {15, 16, 24})
    @GetMapping("/get")
    public ServiceResponse getTimesheetById(@RequestParam Long timesheetId) {
        return timesheetServiceNew.getTimesheetById(timesheetId);
    }
    
    /**
     * READ - Get all timesheets by employee ID and date range
     * GET /api/timesheetNew/getByEmployee
     */
    @JobRoleAccess(featureIds = {15, 16, 24})
    @GetMapping("/getByEmployee")
    public ServiceResponse getTimesheetsByEmployee(
            @RequestParam Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return timesheetServiceNew.getTimesheetsByEmployee(empId, startDate, endDate);
    }
    
    /**
     * UPDATE - Update existing timesheet
     * PUT /api/timesheetNew/update/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Encrypted
    public ServiceResponse updateTimesheet(
            @RequestParam Long timesheetId,
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "doc1", required = false) MultipartFile doc1,
            @RequestPart(value = "doc2", required = false) MultipartFile doc2) throws Exception {
        
        // Decrypt and parse encrypted DTO
        CreateTimesheetRequestDTONew dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNew(encryptedDto);
        
        return timesheetServiceNew.updateTimesheet(timesheetId, dto, doc1, doc2);
    }
    
    /**
     * DELETE - Delete timesheet by ID
     * DELETE /api/timesheetNew/delete/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @DeleteMapping("/delete")
    public ServiceResponse deleteTimesheet(@RequestParam Long timesheetId) {
        return timesheetServiceNew.deleteTimesheet(timesheetId);
    }
}

