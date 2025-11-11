package com.apmosys.employeeportal.controller;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeSessionDTO;
import com.apmosys.employeeportal.service.EmployeeSessionService;
import com.apmosys.employeeportal.utility.ServiceResponse;


@RestController
@RequestMapping("/api/employees/details")
public class EmployeeSessionController {

    private final EmployeeSessionService employeeSessionService;

    @Autowired
    public EmployeeSessionController(EmployeeSessionService employeeSessionService) {
        this.employeeSessionService = employeeSessionService;
    }
    @GetMapping("/session/{empId}")
    public ServiceResponse getEmployeeDetailsBySessionEmpId(@PathVariable Long empId) {  
    		return employeeSessionService.getEmployeeDetailsBySessionEmpId(empId);
    	}
}