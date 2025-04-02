package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeKpiDto;
import com.apmosys.employeeportal.model.EmployeeKpi;
import com.apmosys.employeeportal.service.EmployeeKpiService;

@RestController
@RequestMapping("/api/kpi")
public class AssignKpiController {
	
	
	@Autowired
	private EmployeeKpiService employeeKpiService;
	
	   @PostMapping("/assign")
	    public ResponseEntity<EmployeeKpi> assignKpiToEmployee(@RequestBody EmployeeKpiDto employeeKpiDto)
	    {
	        
	        EmployeeKpi employeeKpi = employeeKpiService.assignKpiToEmployee(employeeKpiDto);
	        
	        return new ResponseEntity<>(employeeKpi, HttpStatus.CREATED);
	    }
	
}
