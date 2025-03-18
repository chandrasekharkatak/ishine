package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.service.EmployeeGoalService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/EmployeeGoals")
public class EmployeeGoalController {

    @Autowired
    private EmployeeGoalService employeeGoalService;

    @PostMapping("/assign")
    public ServiceResponse assignGoalToEmployee(
            @RequestParam Long empId,
            @RequestParam Long templateId,
            @RequestParam String expectedCompletionDate) {

        ServiceResponse response = new ServiceResponse();
        try {
            LocalDate completionDate = LocalDate.parse(expectedCompletionDate);
            EmployeeGoalDTO assignedGoal = employeeGoalService.assignGoalToEmployee(empId, templateId, completionDate);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(assignedGoal);
            response.setServiceMessage("Goal assigned successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error assigning goal.");
        }
        return response;
    }
    
    @PostMapping("/assign-bulk")
    public ServiceResponse assignGoalsToMultipleEmployees(
            @RequestParam List<Long> empIds,
            @RequestParam Long templateId,
            @RequestParam String expectedCompletionDate) {

        ServiceResponse response = new ServiceResponse();
        try {
            LocalDate completionDate = LocalDate.parse(expectedCompletionDate);
            List<EmployeeGoalDTO> assignedGoals = employeeGoalService.assignGoalToMultipleEmployees(empIds, templateId, completionDate);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(assignedGoals);
            response.setServiceMessage("Goals assigned successfully to " + empIds.size() + " employees.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error assigning goals.");
        }
        return response;
    }
    @GetMapping
    public ServiceResponse getAllEmployeeGoals() {
        return employeeGoalService.getAllEmployeeGoals();
    }
    @GetMapping("/{goalId}")
    public ServiceResponse getEmployeeGoalById(@PathVariable Long goalId) {
        return employeeGoalService.getEmployeeGoalById(goalId);
    }
    @GetMapping("/employee/{empId}")
    public ServiceResponse getGoalsByEmployeeId(@PathVariable Long empId) {
        return employeeGoalService.getGoalsByEmployeeId(empId);
    }

   
}



