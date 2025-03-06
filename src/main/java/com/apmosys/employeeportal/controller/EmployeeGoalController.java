package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.service.EmployeeGoalService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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



