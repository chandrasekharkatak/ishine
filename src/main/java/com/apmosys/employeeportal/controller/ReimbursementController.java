package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.service.ReimbursementService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ReimbursementController {
	
	@Autowired
	private ReimbursementService reimbursementService ;
	

	@PostMapping("/fetchReimbursementData")
	public ServiceResponse fetchReimbursementData() {
		return null;
		
	}
	
	@PostMapping("/saveReimbursementData")
	public ServiceResponse saveReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.saveTravelData(reimbursementObj);
		
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
