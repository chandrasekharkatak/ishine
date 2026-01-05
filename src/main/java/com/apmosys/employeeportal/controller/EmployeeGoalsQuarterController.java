package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeGoalsQuarterDto;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.service.EmployeeGoalService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/goalQuarter")
public class EmployeeGoalsQuarterController {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
    private EmployeeGoalService employeeGoalService;
	
//    @GetMapping("/employee/{empId}/quarter/{quarterId}")
//    public ServiceResponse getEmployeeGoalsByEmpIdAndQuarterId(
//            @PathVariable Long empId, 
//            @PathVariable Long quarterId) {
//        return employeeGoalService.getEmployeeGoalsByEmpIdAndQuarterId(empId, quarterId);
//    }
}
