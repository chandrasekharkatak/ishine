package com.apmosys.employeeportal.controller;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import com.apmosys.employeeportal.dto.QuarterDto;
import com.apmosys.employeeportal.service.QuaterService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.model.*;


@RestController
@RequestMapping("/api/quarter")
public class AppraisalStatusController {
	
	@Autowired
	private QuaterService quarterService;

	@PostMapping("/createQuarter")
	
	public ResponseEntity<ServiceResponse> createQuarter(@RequestBody QuarterDto quarterDto) {
		try {
	    return new ResponseEntity<>(quarterService.saveQuarter(quarterDto),HttpStatus.OK);
	}catch(Exception e)
		{
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
