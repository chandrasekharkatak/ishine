package com.apmosys.employeeportal.controller;

import java.io.File;
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
}
