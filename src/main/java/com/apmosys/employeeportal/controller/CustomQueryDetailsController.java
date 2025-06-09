package com.apmosys.employeeportal.controller;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.CustomQueryDetailsDTO;
import com.apmosys.employeeportal.service.CustomQueryDetailsService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class CustomQueryDetailsController {
	@Autowired
	CustomQueryDetailsService customQueryDetailsService;
	
	  @RequestMapping(value = "/saveCustomQueryDetails", method = RequestMethod.POST)
	    public ServiceResponse saveCustomQueryDetails(@RequestBody CustomQueryDetailsDTO customQueryDetailsDTO) {
		  ServiceResponse response = customQueryDetailsService.saveCustomQueryDetails(customQueryDetailsDTO);
		  return response;
	    }
	  
	  @RequestMapping(value = "/getCustomQueries", method = RequestMethod.GET)
	  public ServiceResponse getCustomQueries() {
	      ServiceResponse response = customQueryDetailsService.getCustomQueries();
	      return response;
	  }
	  
	  @PostMapping("/employeeBulkUpload")
	  public ServiceResponse bulkUpload(@RequestParam("file") MultipartFile file,@RequestParam("uploadedBy") Long uploadedBy) throws EncryptedDocumentException, InvalidFormatException {
	        ServiceResponse response = new ServiceResponse();
	        
	        if (file.isEmpty()) {
	            response.setServiceResponse("Please upload a file.");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        try {
	        	response = customQueryDetailsService.bulkUpload(file,uploadedBy);
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Error occurred while processing the file.");
	        }

	        return response;
	    }

}
