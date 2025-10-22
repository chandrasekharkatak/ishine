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
import com.apmosys.employeeportal.dto.NewsletterDTO;
import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.service.NewsletterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/newsletters")
public class NewsletterController {

	@Autowired
	private NewsletterService newsletterService;
	
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value = "/uploadNewsletter", method = RequestMethod.POST)
	public ServiceResponse uploadNewsletter(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy) {
		ServiceResponse serviceResponse = newsletterService.uploadNewsletter(file, displayName, uploadedBy);
		return serviceResponse;
	}

	@RequestMapping(method = RequestMethod.GET)
	@JobRoleAccess(featureIds = {43})
	public ServiceResponse getAllNewsletters() {
		ServiceResponse response = newsletterService.getAllNewsletters();
		return response;
	}
	
//	added by anurag by typeId
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value="/getDocumentByType", method = RequestMethod.POST)
	public ServiceResponse getAllNewslettersByTypeId(@RequestBody NewsletterDTO newsletterDto ) {
		ServiceResponse response = newsletterService.getAllNewslettersByTypeId(newsletterDto);
		return response;
	}
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value = "/{documentId}", method = RequestMethod.DELETE)
	public ServiceResponse deleteNewsletter(@PathVariable Long documentId) {
		ServiceResponse response = newsletterService.deleteNewsletter(documentId);
		return response;
	}
	
	@JobRoleAccess(featureIds = {43,24})
	@RequestMapping(value = "/download/{documentId}", method = RequestMethod.GET)
	public ResponseEntity<Resource> downloadDocument(@PathVariable String documentId) throws IOException {

		Resource file = newsletterService.getTemplateFile(Long.parseLong(documentId));
		Path path = file.getFile().toPath();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value = "/setNewsletterReadResponseByEmpId", method = RequestMethod.POST)
	public ServiceResponse setNewsletterReadResponseByEmpId(@RequestBody NewsletterDTO newsletterDTO) {

		ServiceResponse response = newsletterService.setNewsletterReadResponseByEmpId(newsletterDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value = "/getAllReadNewslettersByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllReadNewslettersByEmpId(@RequestBody NewsletterDTO newsletterDTO) {
		ServiceResponse response = newsletterService.getAllReadNewslettersByEmpId(newsletterDTO);

		return response;
		
	}
	
//	checkDocumentName
	@JobRoleAccess(featureIds = {43})
	@RequestMapping(value = "/checkDocumentName/{documentName}", method = RequestMethod.GET)
	public ServiceResponse checkDocumentName(@PathVariable("documentName") String documentName) {
		ServiceResponse response = newsletterService.checkDocumentName(documentName);

		return response;
		
	}
	
}
