package com.apmosys.employeeportal.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DocumentDTO;
import com.apmosys.employeeportal.service.TypeDocumentService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class TypeDocumentController {
	
	@Autowired
	TypeDocumentService typeDocumentService;
	
	
	@RequestMapping(value = "/addTypeDocument", method= RequestMethod.POST)
	public ServiceResponse addTypeDocument(@RequestBody DocumentDTO documentDto) {
		
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.addTypeDocument(documentDto);
		
		return response;
	}

//	check if already exist or not
	
	@RequestMapping(value= "/checkTypeName/{typeName}", method = RequestMethod.POST)
	public ServiceResponse checkTypeName(@PathVariable("typeName") String typeName) {
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.checkTypeName(typeName);
		
		return response;
	}
	
	@RequestMapping(value = "/getAllTypeName", method= RequestMethod.GET)
	public ServiceResponse getAllTypeName() {

		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.getAllTypeName();
		
		return response;
	}
	
	@RequestMapping(value = "/uploadDocument", method = RequestMethod.POST)
	public ServiceResponse uploadDocument(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy,
			@RequestParam("typeId") Long typeId,
			@RequestParam("readEnabled") String readEnabled) throws IOException {
		
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.uploadDocument(file,displayName,uploadedBy,typeId,readEnabled);
		
		return response;
	}
	
//	@RequestMapping(method = RequestMethod.GET)
//	public ServiceResponse findAllDocument() {
//		ServiceResponse response = typeDocumentService.findAllDocument();
//		return response;
//	}
	
	
	@RequestMapping(value = "/deleteType" , method=RequestMethod.POST)
	public ServiceResponse deleteType(@RequestBody DocumentDTO documentDto) {
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.deleteType(documentDto);
		return response;
	}
	
	@RequestMapping(value = "/getTypeById" , method=RequestMethod.POST)
	public ServiceResponse getTypeById(@RequestBody DocumentDTO documentDto) {
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.getTypeById(documentDto);
		return response;
	}
	
	@RequestMapping(value ="/updateType" , method = RequestMethod.POST)
	public ServiceResponse updateType(@RequestBody DocumentDTO documentDto) {
		ServiceResponse response = new ServiceResponse();
		response = typeDocumentService.updateType(documentDto);
		return response;
	}
	
}
