package com.apmosys.employeeportal.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class TravelDeskController {
	
	@PostMapping("/fetchTravelData")
	public ServiceResponse fetchTravelData() {
		return null;
		
	}
	
	@PostMapping("/saveTravelData")
	public ServiceResponse saveTravelData() {
		return null;
		
	}
	
	@PostMapping("/updateTravelData")
	public ServiceResponse updateTravelData() {
		return null;
		
	}
	
	@PostMapping("/revokeTravel")
	public ServiceResponse revokeTravel() {
		return null;
		
	}
	
	@PostMapping("/approveOrRejectReimbursement")
	public ServiceResponse approveOrRejectTravel() {
		return null;
		
	}
}
