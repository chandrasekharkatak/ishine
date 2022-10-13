package com.apmosys.employeeportal.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.service.AppreciationService;
import com.apmosys.employeeportal.service.UploadPolicyService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class UploadPolicyController {
	
	@Autowired
    private UploadPolicyService uploadPolicyService;
	
	@RequestMapping(value="/uploadPolicies" , method = RequestMethod.POST)
	public ServiceResponse uploadPolicies(HttpServletRequest request, @RequestParam("file")List<MultipartFile> files,
			@RequestParam("policyName")String policyName, @RequestParam("uploadedBy")Long uploadedBy) {
		ServiceResponse serviceResponse = uploadPolicyService.uploadPolicies(files,policyName,uploadedBy);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/getAllDocument", method = RequestMethod.GET)
	public ServiceResponse getAllDocument() {
		ServiceResponse response = uploadPolicyService.getAllDocuments();
		return response;
	}
	
	@RequestMapping(value="/deletePolicyDocument" , method = RequestMethod.POST)
	public ServiceResponse deletePolicyDocument(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		System.out.println("hii");
		ServiceResponse response = uploadPolicyService.deleteDocument(uploadPolicyDTO);
		return response;
	}
}
