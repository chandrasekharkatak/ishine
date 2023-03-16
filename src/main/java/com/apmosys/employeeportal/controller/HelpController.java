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

import com.apmosys.employeeportal.dto.HelpDTO;
import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.service.HelpService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class HelpController {

	@Autowired
	private HelpService helpService;

	@RequestMapping(value = "/uploadHelpDocument", method = RequestMethod.POST)
	public ServiceResponse uploadHelpDocument(HttpServletRequest request, @RequestParam("file") List<MultipartFile> files,
			@RequestParam("helpDocumentName") String helpDocumentName, @RequestParam("uploadedBy") Long uploadedBy) {
		ServiceResponse serviceResponse = helpService.uploadHelpDocument(files, helpDocumentName, uploadedBy);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/getAllHelpDocument", method = RequestMethod.GET)
	public ServiceResponse getAllDocument() {
		ServiceResponse response = helpService.getAllHelpDocument();
		return response;
	}
	
	@RequestMapping(value = "/deleteHelpDocument", method = RequestMethod.POST)
	public ServiceResponse deleteHelpDocument(@RequestBody HelpDTO helpDTO) {
		ServiceResponse response = helpService.deleteHelpDocument(helpDTO);
		return response;
	}
	
	@RequestMapping(value = "/downloadHelpDocument/{helpDocId}", method = RequestMethod.GET)
	public ResponseEntity<Resource> downloadDocument(@PathVariable String helpDocId) throws IOException {

		Resource file = helpService.getTemplateFile(Long.parseLong(helpDocId));
		Path path = file.getFile().toPath();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	
}
