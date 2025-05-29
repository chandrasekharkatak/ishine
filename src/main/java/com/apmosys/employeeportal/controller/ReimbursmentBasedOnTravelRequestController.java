package com.apmosys.employeeportal.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.service.TravelBasedReimbursementRequestService;
import com.apmosys.employeeportal.service.TravelDeskService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ReimbursmentBasedOnTravelRequestController {
	
	@Autowired
     private TravelBasedReimbursementRequestService travelBasedService;
	@Autowired
	private TravelDeskService travelDeskService;
	
	@RequestMapping(value = "/submitReimbursmentBasedOnTravelRequest", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> submitReimbursmentBasedOnTravelRequest(@RequestBody List<TravelBasedReimbursementRequestDTO> reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.submitReimbursmentBasedOnTravelRequest(reimbursementRequestDTO));
	}
	
	
	@PostMapping("/uploadFiletravelBased")
	public ServiceResponse uploadFile(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy,
			@RequestParam("invoiceNo") String invoiceNo) {
		
		ServiceResponse serviceResponse = travelBasedService.uploadFile(file, displayName, uploadedBy,invoiceNo);
		return serviceResponse;
	}

	@RequestMapping(value = "/checkInvoiceNumberPresentorNot", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> checkInvoiceNumberPresentorNot(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.checkInvoiceNumberPresentorNot(reimbursementRequestDTO));
	}
	
	@RequestMapping(value = "/previewDocument", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> previewDocument(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.previewDocument(reimbursementRequestDTO));
	}
	
	@RequestMapping(value = "/getAllInvoices", method = RequestMethod.GET)
	public ResponseEntity<ServiceResponse> getAllInvoices() {
		return ResponseEntity.ok(travelBasedService.getAllInvoices());
	}
	
	@RequestMapping(value = "/updateInvoicesDetailsByAccountsTeam", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> updateInvoicesDetailsByAccountsTeam(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.updateInvoicesDetailsByAccountsTeam(reimbursementRequestDTO));
	}
	
	@RequestMapping(value = "/getAllInvoicesByEmpId", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> getAllInvoicesByEmpId(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.getAllInvoicesByEmpId(reimbursementRequestDTO));
	}
	
	@RequestMapping(value = "/updateReimbursmentBasedOnTravelRequest", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> updateReimbursmentBasedOnTravelRequest(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.updateReimbursmentBasedOnTravelRequest(reimbursementRequestDTO));
	}
	
	@PostMapping("/updateUploadedFile")
	public ServiceResponse uploadFile(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy,
			@RequestParam("docId") Long docId) {
		
		ServiceResponse serviceResponse = travelBasedService.updateUploadedFile(file, displayName, uploadedBy,docId);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/markAsPaid", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> markAsPaid(@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(travelBasedService.markAsPaid(reimbursementRequestDTO));
	}
}
