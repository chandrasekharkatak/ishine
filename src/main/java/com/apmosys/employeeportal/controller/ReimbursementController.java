package com.apmosys.employeeportal.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ReimbursementController {

	@PostMapping("/fetchReimbursementData")
	public ServiceResponse fetchReimbursementData() {
		return null;
		
	}
	
	@PostMapping("/saveReimbursementData")
	public ServiceResponse saveReimbursementData() {
		return null;
		
	}
	
	@PostMapping("/updateReimbursementData")
	public ServiceResponse updateReimbursementData() {
		return null;
		
	}
	
	@PostMapping("/revokeReimbursement")
	public ServiceResponse revokeReimbursement() {
		return null;
		
	}
	
	@PostMapping("/approveOrRejectReimbursement")
	public ServiceResponse approveOrRejectReimbursement() {
		return null;
		
	}
}
