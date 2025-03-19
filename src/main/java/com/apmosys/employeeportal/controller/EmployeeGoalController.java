package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.BulkGoalAssignmentRequest;
import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.dto.GoalAssignmentRequest;
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
    public ServiceResponse assignGoalToEmployee(@RequestBody GoalAssignmentRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            LocalDate completionDate = LocalDate.parse(request.getExpectedCompletionDate());
            EmployeeGoalDTO assignedGoal = employeeGoalService.assignGoalToEmployee(
                request.getEmpId(), 
                request.getTemplateId(), 
                completionDate
            );

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
    public ServiceResponse assignGoalsToMultipleEmployees(@RequestBody BulkGoalAssignmentRequest request) {
        ServiceResponse response = new ServiceResponse();
        try {
            LocalDate completionDate = LocalDate.parse(request.getExpectedCompletionDate());
            List<EmployeeGoalDTO> assignedGoals = employeeGoalService.assignGoalToMultipleEmployees(
                request.getEmpIds(), 
                request.getTemplateId(), 
                completionDate
            );

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(assignedGoals);
            response.setServiceMessage("Goals assigned successfully to " + request.getEmpIds().size() + " employees.");
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



