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
	public ResponseEntity<ServiceResponse> createBioMaxRequest(@RequestBody List<TravelBasedReimbursementRequestDTO> reimbursementRequestDTO) {
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

}
