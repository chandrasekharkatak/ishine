package com.apmosys.employeeportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import com.apmosys.employeeportal.dto.QuarterDto;
import com.apmosys.employeeportal.service.QuaterService;
import com.apmosys.employeeportal.model.*;


@RestController
@RequestMapping("/employee")
public class AppraisalStatusController {
	
	@Autowired
	private QuaterService quarterservice;

	@PostMapping("/createQuarter")
	public ResponseEntity<QuarterModel> createQuarter(@RequestBody QuarterDto quarterDto)
	{
		
		try {
			QuarterModel savedQuarter = quarterservice.saveQuarter(quarterDto);
		    return new ResponseEntity<>(savedQuarter, HttpStatus.CREATED);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
