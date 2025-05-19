package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	
	@PostMapping("/fetchReimbursementDataforApproval")
	public ServiceResponse fetchReimbursementDataforApproval(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchReimbursementDataforApproval(reimbursementObj.getEmpId());
		
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
	public ServiceResponse approveOrRejectReimbursement(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.approveOrRejectReimbursement(reimbursementObj);
		
	}
	
	@PostMapping("/uploadFileReimbursement")
	public ServiceResponse uploadFile(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy) {
		ServiceResponse serviceResponse = reimbursementService.uploadFile(file, displayName, uploadedBy);
		return serviceResponse;
	}
	
	@PostMapping("/fetchTotalReimbursementData")
	public ServiceResponse fetchTotalReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchTotalReimbursementData();
		
	}
}
