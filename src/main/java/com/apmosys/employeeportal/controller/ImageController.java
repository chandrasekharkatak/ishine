package com.apmosys.employeeportal.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.EventPhotoDTO;
import com.apmosys.employeeportal.service.ImageService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ImageController {
		
	@Autowired
	ImageService imageService;
		
	@RequestMapping(value="/uploadMultipleImages" , method = RequestMethod.POST)
	public ServiceResponse uploadMultipleImages(HttpServletRequest request, @RequestParam("image")List<MultipartFile> images,
			@RequestParam("eventName")String eventName, @RequestParam("uploadedBy")Long uploadedBy) {
		ServiceResponse serviceResponse = imageService.uploadMultipleImages(images,eventName,uploadedBy);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/getAllEventPhotos", method = RequestMethod.GET)
	public ServiceResponse getAllEventPhotos() {

		ServiceResponse response = imageService.getAllEventPhotos();
		return response;
	}
	
	@RequestMapping(value="/deleteEventPhoto" , method = RequestMethod.POST)
	public ServiceResponse deleteActivity(@RequestBody EventPhotoDTO eventPhotoDTO) {		
		
		ServiceResponse response =	imageService.deleteEventPhoto(eventPhotoDTO);		
		return response;
	}
	
	@RequestMapping(value="/uploadEmployeeDocument" , method = RequestMethod.POST)
	public ServiceResponse uploadImage(HttpServletRequest request, @RequestParam("image")List<MultipartFile> images,
			@RequestParam("uploadedBy")Long uploadedBy, @RequestParam("employeementId")Long employeementId,
			@RequestParam("empId")Long empId) {		
		
		ServiceResponse response =	imageService.uploadEmployeeDocument(images,uploadedBy,employeementId,empId);		
		return response;
	}
	
	@RequestMapping(value="/saveEmployeeDocuments" , method = RequestMethod.POST)
	public ServiceResponse saveEmployeeDocuments(@RequestBody EmployeeDTO employeeDTO) {		
		
		ServiceResponse response =	imageService.saveEmployeeDocuments(employeeDTO);		
		return response;
	}
	
	@RequestMapping(value="/getEmployeeDocuments" , method = RequestMethod.POST)
	public ServiceResponse getEmployeeDocuments(@RequestBody EmployeeDTO employeeDTO) {		
		
		ServiceResponse response =	imageService.getEmployeeDocuments(employeeDTO);		
		return response;
	}
	
	/*
	 upload employee document - part of data migration.
	 */
	
	@RequestMapping(value = "/uploadDocumentByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse uploadDocumentByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = imageService.uploadDocumentByList(employee);
		}
		return response;
	}
}
