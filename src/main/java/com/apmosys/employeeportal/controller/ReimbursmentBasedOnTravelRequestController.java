package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.service.TravelBasedReimbursementRequestService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ReimbursmentBasedOnTravelRequestController {
	
	@Autowired
     private TravelBasedReimbursementRequestService travelBasedService;
	
	@RequestMapping(value = "/submitReimbursmentBasedOnTravelRequest", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> createBioMaxRequest(@RequestBody List<TravelBasedReimbursementRequestDTO> reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.submitReimbursmentBasedOnTravelRequest(reimbursementRequestDTO));
	}
	

}
