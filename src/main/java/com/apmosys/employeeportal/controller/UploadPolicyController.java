package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.service.UploadPolicyService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class UploadPolicyController {

	@Autowired
	private UploadPolicyService uploadPolicyService;

	@RequestMapping(value = "/uploadPolicies", method = RequestMethod.POST)
	public ServiceResponse uploadPolicies(HttpServletRequest request, @RequestParam("file") List<MultipartFile> files,
			@RequestParam("policyName") String policyName, @RequestParam("uploadedBy") Long uploadedBy, @RequestParam("readEnabled") String readEnabled) {
		ServiceResponse serviceResponse = uploadPolicyService.uploadPolicies(files, policyName, uploadedBy, readEnabled);
		return serviceResponse;
	}

	@JobRoleAccess(featureIds = {28,55,29,61})
	@RequestMapping(value = "/getAllDocument", method = RequestMethod.GET)
	public ServiceResponse getAllDocument() {
		ServiceResponse response = uploadPolicyService.getAllDocuments();
		return response;
	}

	@RequestMapping(value = "/deletePolicyDocument", method = RequestMethod.POST)
	public ServiceResponse deletePolicyDocument(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = uploadPolicyService.deleteDocument(uploadPolicyDTO);
		return response;
	}

	@RequestMapping(value = "/downloadDocument/{policyID}", method = RequestMethod.GET)
	public ResponseEntity<Resource> downloadDocument(@PathVariable String policyID) throws IOException {

		Resource file = uploadPolicyService.getTemplateFile(Long.parseLong(policyID));
		Path path = file.getFile().toPath();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	
	@RequestMapping(value = "/changepolicyEnabledMode", method = RequestMethod.POST)
	public ServiceResponse changepolicyEnabledMode(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = uploadPolicyService.changepolicyEnabledMode(uploadPolicyDTO);
		return response;

	}
	@RequestMapping(value = "/setPolicyReadResponseByEmpId", method = RequestMethod.POST)
	public ServiceResponse setPolicyReadResponseByEmpId(@RequestBody UploadPolicyDTO uploadPolicyDTO) {

		ServiceResponse response = uploadPolicyService.setPolicyReadResponseByEmpId(uploadPolicyDTO);
		return response;
	}
	
	@RequestMapping(value = "/showPolicyReadResponseByPolicyID", method = RequestMethod.POST)
	public ServiceResponse showPolicyReadResponseByPolicyID(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = uploadPolicyService.showPolicyReadResponseByPolicyID(uploadPolicyDTO);

		return response;
		
	}
	
	@RequestMapping(value = "/getReadPoliciesByEmpId", method = RequestMethod.POST)
	public ServiceResponse getReadPoliciesByEmpId(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = uploadPolicyService.getReadPoliciesByEmpId(uploadPolicyDTO);

		return response;
		
	}
	
	@RequestMapping(value = "/isAllPolicyRead", method = RequestMethod.POST)
	public ServiceResponse isAllPolicyRead(@RequestBody UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = uploadPolicyService.isAllPolicyRead(uploadPolicyDTO);

		return response;
		
	}

	

}
