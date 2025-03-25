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
	public ServiceResponse fetchReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchReimbursementData(reimbursementObj.getEmpId());
		
	}
	
	@PostMapping("/saveReimbursementData")
	public ServiceResponse saveReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.saveReimbursementData(reimbursementObj);
		
	}
	
	@PostMapping("/updateReimbursementData")
	public ServiceResponse updateReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.updateReimbursementData(reimbursementObj);
		
	}
	
	@PostMapping("/revokeReimbursement")
	public ServiceResponse revokeReimbursement(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.revokeReimbursement(reimbursementObj.getRequestId());
		
	}
	
	@PostMapping("/approveOrRejectReimbursement")
	public ServiceResponse approveOrRejectReimbursement() {
		return null;
		
	}
}
