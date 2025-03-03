package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.serviceInterface.BioMaxRequestService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/biomaxRequest")
public class BioMaxRequestController {
	@Autowired
	private BioMaxRequestService bioMaxRequestService;
	
	  @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> createBioMaxRequest(@RequestBody BioMaxRequestDTO biomaxRequestDTO) {
		return ResponseEntity.ok(bioMaxRequestService.createBioMaxRequest(biomaxRequestDTO));
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<ServiceResponse> UpdateBioMaxRequest(@PathVariable("id") Long id,@RequestBody BioMaxRequestDTO biomaxRequestDTO) {
		return ResponseEntity.ok(bioMaxRequestService.updateBioMaxRequest(id, biomaxRequestDTO));
	}
	
	@GetMapping("/getByEmployeeId/{empid}")
	public ResponseEntity<ServiceResponse> getByEmployeeIdBioMaxRequest(@PathVariable("empid") Long empid) {
		return ResponseEntity.ok(bioMaxRequestService.getByEmployeeId(empid));
	}
	@GetMapping("/getByReportingManagerEmployeeId/{empid}")
	public ResponseEntity<ServiceResponse> getByReportingManagerEmployeeIdBioMaxRequest(@PathVariable("empid") Long empid) {
		return ResponseEntity.ok(bioMaxRequestService.getByRepostingManager(empid));
	}
	
	@DeleteMapping("/deleteBioMaxRequest/{id}")
	public ResponseEntity<ServiceResponse> deleteBioMaxRequest(@PathVariable("id") Long id) {
		return ResponseEntity.ok(bioMaxRequestService.deletedRequest(id));
	}
	@GetMapping("/getById/{id}")
	public ResponseEntity<ServiceResponse> getById(@PathVariable("id") Long id) {
		return ResponseEntity.ok(bioMaxRequestService.getById(id));
	}
	
	@GetMapping("/getBioMaxRequestType")
	public ResponseEntity<ServiceResponse> getBioMaxRequestType() {
		return ResponseEntity.ok(bioMaxRequestService.getBioMaxRequestType());
	}
	
	@GetMapping("/getBioMaxRequestTypeCronJon")
	public ResponseEntity<ServiceResponse> getBioMaxRequestTypeCronJon() {
		return ResponseEntity.ok(bioMaxRequestService.getBioMaxRequestTypeCronJon());
	}
	
	@PostMapping("/leaveDeductRoleBackForEmloyee")
	public ResponseEntity<ServiceResponse> leaveDeductRoleBackForEmloyee(@RequestBody BioMaxRequestDTO biomaxRequestDTO){
		return ResponseEntity.ok(bioMaxRequestService.leaveDeductRoleBackForEmloyee(biomaxRequestDTO));
	}
}
