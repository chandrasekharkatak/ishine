package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.service.EmployeeGoalService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/EmployeeGoals")
public class EmployeeGoalController {

    @Autowired
    private EmployeeGoalService employeeGoalService;

    @GetMapping("/getAllEmployeeGoals")
    public ServiceResponse getAllEmployeeGoals() {
        return employeeGoalService.getAllEmployeeGoals();
    }
    

    @GetMapping("/getEmployeeGoalById/{id}")
    public ServiceResponse getEmployeeGoalById(@PathVariable Long id) {
        return employeeGoalService.getEmployeeGoalById(id);
    }

   
}



