package com.apmosys.employeeportal.controller;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.service.FileUploadService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;
//
//    @PostMapping("/file")
//    public ServiceResponse handleFileUpload(@RequestParam("file") MultipartFile file) {
//    	ServiceResponse response = new ServiceResponse();
//        try {
//        	response = fileUploadService.uploadFile(file);
////            return ResponseEntity.ok("File uploaded and processed successfully.");
//            return response;
//        } catch (IOException e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
//        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//        	response.setServiceResponse("Something went wrong");
//        	return response;
//        }
//    }
    
    @PostMapping("/billableFile")
    public ServiceResponse uploadBillableFile(@RequestParam("file") MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
    	ServiceResponse response = new ServiceResponse();
    	if (file.isEmpty()) {
    		response.setServiceResponse("Please upload a file.");
    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    		return response;
        }

        response = fileUploadService.saveExcelData(file);

//        return ResponseEntity.ok("File uploaded and data saved successfully.");
        return response;
    }
    
    // as suggest by vini
    
    @PostMapping("/saveExcelDataForManagerMapping")
    public ServiceResponse saveExcelDataForManagerMapping(@RequestParam("file") MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
    	ServiceResponse response = new ServiceResponse();
    	if (file.isEmpty()) {
    		response.setServiceResponse("Please upload a file.");
    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    		return response;
        }

        response = fileUploadService.saveExcelDataForManagerMapping(file);
        return response;
    }
}