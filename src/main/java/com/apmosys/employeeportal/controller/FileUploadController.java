package com.apmosys.employeeportal.controller;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.dto.FileResponseDTO;
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
    

//    @PostMapping("/uploadImage")
//    public ResponseEntity<ServiceResponse> uploadImage(@RequestParam("file") MultipartFile file) {
//        ServiceResponse response = fileUploadService.storeFile(file);
//        return ResponseEntity.status(response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
//                             .body(response);
//    }
//
//    @PostMapping("/uploadVideo")
//    public ResponseEntity<ServiceResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
//        ServiceResponse response = fileUploadService.storeFile(file);
//        return ResponseEntity.status(response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
//                             .body(response);
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
    
    @PostMapping("/designationBulkUpload")
    public ServiceResponse designationBulkUpload(@RequestParam("file") MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
    	ServiceResponse response = new ServiceResponse();
    	if (file.isEmpty()) {
    		response.setServiceResponse("Please upload a file.");
    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    		return response;
        }

        response = fileUploadService.designationBulkUpload(file);
        return response;
    }
    
    @PostMapping("/confirmationDateBulkUpload")
    public ServiceResponse confirmationDateBulkUpload(@RequestParam("file") MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
        ServiceResponse response = new ServiceResponse();
        if (file.isEmpty()) {
            response.setServiceResponse("Please upload a file.");
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            return response;
        }

        response = fileUploadService.confirmationDateBulkUpload(file);
        return response;
    }

    @PostMapping("/project-insight-file-bulk-upload")
    public ResponseEntity<?> projectInsightBulkUpload(@RequestBody List<MultipartFile> files, @RequestParam(value = "projectName") String projectName) throws EncryptedDocumentException, InvalidFormatException {
        try{
            if (files.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload at least one file.");
            }

            List<String> response = fileUploadService.projectInsightBulkUpload(files, projectName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Error occurred while uploading files."));
        }
    }

    
    @GetMapping("/view-project-insight-file")
    public ResponseEntity<?> viewProjectInsightFile(@RequestParam(value = "fileName") String files, @RequestParam(value = "projectName") String projectName) throws Exception {
        try{
            byte[] response = fileUploadService.viewProjectInsightFile(files, projectName);
            String contentType = Files.probeContentType(Paths.get(files));
            FileResponseDTO fileResponseDTO = new FileResponseDTO();
            fileResponseDTO.setData(response);
            fileResponseDTO.setContentType(contentType);
            fileResponseDTO.setStatus("success");
            return ResponseEntity.ok().body(fileResponseDTO);
        } catch (Exception e) {
            // e.printStackTrace();
            // FileResponseDTO fileResponseDTO = new FileResponseDTO();
            // fileResponseDTO.setStatus("error");
            // return ResponseEntity
            // .status(HttpStatus.INTERNAL_SERVER_ERROR)
            // .body(fileResponseDTO);
            throw e;
            
        }
    }

    @DeleteMapping("/project-insight-files")
    public ResponseEntity<?> deleteMultipleProjectInsightFiles(@RequestBody List<String> fileNames, @RequestParam(value = "projectName") String projectName ) throws BadRequestException {
        try {
            Map<String, Boolean> deletionResults = fileUploadService.deleteMultipleFiles(fileNames, projectName);
            
            boolean allDeleted = deletionResults.values().stream().allMatch(Boolean::booleanValue);
            
            if (allDeleted) {
                return ResponseEntity.ok().body(Map.of(
                    "message", "All files deleted successfully",
                    "results", deletionResults
                ));
            } else {                
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(Map.of(
                    "message", "Some files could not be deleted",
                    "results", deletionResults
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Error deleting files: " + e.getMessage())
            );
        }
    }
    
    @GetMapping("/download-project-insight-file")
    public ResponseEntity<?> downloadProjectInsightFile(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "projectName") String projectName) {
        try {
            byte[] fileBytes = fileUploadService.downloadFile(fileName, projectName);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Error downloading file"
            ));
        }
    }

}