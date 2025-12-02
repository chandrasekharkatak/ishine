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

import com.apmosys.employeeportal.JobRoleAccess;
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
		
	@JobRoleAccess(featureIds = {23})
	@RequestMapping(value="/uploadMultipleImages" , method = RequestMethod.POST)
	public ServiceResponse uploadMultipleImages(HttpServletRequest request, 
			@RequestParam("image")List<MultipartFile> images,
			@RequestParam("eventName")String eventName,
			@RequestParam("eventCaption")String eventCaption,
			@RequestParam("isExternalLink")String isExternalLink,
			@RequestParam("externalLink")String externalLink,
			@RequestParam("uploadedBy")Long uploadedBy) {
		ServiceResponse serviceResponse = imageService.uploadMultipleImages(images,eventName,eventCaption,isExternalLink,externalLink,uploadedBy);
		return serviceResponse;
	}
	
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value = "/getFirstEventPhotoForHome", method = RequestMethod.GET)
	public ServiceResponse getFirstEventPhotoForHome() {

		ServiceResponse response = imageService.getFirstEventPhotoForHome();
		return response;
	}
	
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value = "/getAllEventPhotosForHome", method = RequestMethod.GET)
	public ServiceResponse getAllEventPhotosForHome() {

		ServiceResponse response = imageService.getAllEventPhotosForHome();
		return response;
	}
	
	@JobRoleAccess(featureIds = {23,24})
	@RequestMapping(value = "/getAllEventPhotos", method = RequestMethod.GET)
	public ServiceResponse getAllEventPhotos() {

		ServiceResponse response = imageService.getAllEventPhotos();
		return response;
	}
	
	@JobRoleAccess(featureIds = {23})
	@RequestMapping(value="/deleteEventPhoto" , method = RequestMethod.POST)
	public ServiceResponse deleteActivity(@RequestBody EventPhotoDTO eventPhotoDTO) {		
		
		ServiceResponse response =	imageService.deleteEventPhoto(eventPhotoDTO);		
		return response;
	}
	
	@JobRoleAccess(featureIds = {23})
	@RequestMapping(value="/updatePhotoOrder" , method = RequestMethod.POST)
	public ServiceResponse updatePhotoOrder(@RequestBody EventPhotoDTO eventPhotoDTO) {		
		
		ServiceResponse response =	imageService.updatePhotoOrder(eventPhotoDTO);		
		return response;
	}
	
	@JobRoleAccess(featureIds = {23})
	@RequestMapping(value="/updatePhotoDetails" , method = RequestMethod.POST)
	public ServiceResponse updatePhotoDetails(@RequestBody EventPhotoDTO eventPhotoDTO) {		
		
		ServiceResponse response =	imageService.updatePhotoDetails(eventPhotoDTO);		
		return response;
	}
	
	// Documents
	
	@JobRoleAccess(featureIds = {8,23,25,29})
	@RequestMapping(value="/uploadEmployeeDocument" , method = RequestMethod.POST)
	public ServiceResponse uploadImage(HttpServletRequest request, @RequestParam("image")MultipartFile images,
			@RequestParam("uploadedBy")Long uploadedBy, @RequestParam("employeementId")Long employeementId,
			@RequestParam("empId")Long empId) {		
		
		ServiceResponse response =	imageService.uploadEmployeeDocument(images,uploadedBy,employeementId,empId);		
		return response;
	}
	
	@JobRoleAccess(featureIds = {8,23,25,29})
	@RequestMapping(value="/saveEmployeeDocuments" , method = RequestMethod.POST)
	public ServiceResponse saveEmployeeDocuments(@RequestBody EmployeeDTO employeeDTO) {		
		
		ServiceResponse response =	imageService.saveEmployeeDocuments(employeeDTO);		
		return response;
	}
	
	@JobRoleAccess(featureIds = {3,8,23,25,29})
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
