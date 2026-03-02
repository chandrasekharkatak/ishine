package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingRequestDTO;
import com.apmosys.employeeportal.serviceInterface.TrainingConfigService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller for Training Configuration operations (HR/Admin)
 * Handles CRUD operations for training configuration and content management
 */
@RestController
@RequestMapping(path = "/api/training")
public class TrainingConfigController {

	@Autowired
	private TrainingConfigService trainingConfigService;
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;
	
	@Autowired
    private ObjectMapper objectMapper;

	// ==================== HR Configuration APIs ====================
	// NOTE: Using Feature ID 3 for quick testing. Change to 64 for production after setting up feature mapping.
	@JobRoleAccess(featureIds = {3}) // Training Config - View All Trainings
	@GetMapping(value = "/getAllTrainings")
	public ServiceResponse getAllTrainings(
			@RequestParam(required = false) String activeStatus,
			@RequestParam(required = false) String mandatoryFlag) {
		try {
			return trainingConfigService.getAllTrainings(activeStatus, mandatoryFlag);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@JobRoleAccess(featureIds = {3})
    @PostMapping(
        value = "/createTrainingWithContent",    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
    public ServiceResponse createTrainingWithContent(
            @RequestPart("trainingDTO") TrainingMasterDTO trainingDTO,
            @RequestPart("contentDTO") TrainingContentDTO contentDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return trainingConfigService.createTrainingWithContent(
                trainingDTO,
                contentDTO,
                file
        );
    }

	@JobRoleAccess(featureIds = {3})
	@PostMapping(
	        value = "/updateTrainingWithContent",    consumes = MediaType.MULTIPART_FORM_DATA_VALUE

	       	)
	public ServiceResponse updateTrainingWithContent(
	        @RequestPart("trainingDTO") TrainingMasterDTO trainingDTO,
	        @RequestPart("contentDTO") TrainingContentDTO contentDTO,
	        @RequestPart(value = "file", required = false) MultipartFile file) {

	    return trainingConfigService.updateTrainingWithContent(
	            trainingDTO,
	            contentDTO,
	            file
	    );
	}
	@JobRoleAccess(featureIds = {3}) // Training Config - Add Content
	@PostMapping(value = "/addTrainingContent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse addTrainingContent(
			@RequestPart("trainingId") Integer trainingId,
			@RequestPart("contentType") String contentType,
			@RequestPart("contentName") String contentName,
			@RequestPart("effectiveFrom") String effectiveFromStr,
			@RequestPart(value = "effectiveTo", required = false) String effectiveToStr,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart(value = "externalLinkUrl", required = false) String externalLinkUrl,
			@RequestPart("createdBy") Long createdBy) throws Exception {

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Add Training Content");
		apiLogInfo.setApiUrl("/api/training/addTrainingContent");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			TrainingContentDTO contentDTO = new TrainingContentDTO();
			contentDTO.setTrainingId(trainingId);
			contentDTO.setContentType(contentType);
			contentDTO.setContentName(contentName);
			contentDTO.setEffectiveFrom(LocalDate.parse(effectiveFromStr));
			if (effectiveToStr != null && !effectiveToStr.isEmpty()) {
				contentDTO.setEffectiveTo(LocalDate.parse(effectiveToStr));
			}
			contentDTO.setExternalLinkUrl(externalLinkUrl);
			
			// Handle file upload
			if (file != null && !file.isEmpty()) {
				String filePath = saveTrainingFile(trainingId, file, createdBy);
				contentDTO.setContentPath(filePath);
				contentDTO.setFileSizeBytes(file.getSize());
				contentDTO.setMimeType(file.getContentType());
			}
			
			return trainingConfigService.addTrainingContent(contentDTO, createdBy);
			
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiUrl("/api/training/addTrainingContent");
			throw e;
		}
	}

	@JobRoleAccess(featureIds = {3}) // Training Config - Update Content
	@PostMapping(value = "/updateTrainingContent")
	public ServiceResponse updateTrainingContent(@RequestBody TrainingContentDTO contentDTO) {
		try {
			return trainingConfigService.updateTrainingContent(contentDTO);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@JobRoleAccess(featureIds = {3}) // Training Config - View Content
	@GetMapping(value = "/getTrainingContent/{trainingId}")
	public ServiceResponse getTrainingContent(@PathVariable Integer trainingId) {
		try {
			return trainingConfigService.getTrainingContent(trainingId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@JobRoleAccess(featureIds = {3}) // Training Config - Deactivate Training
	@PostMapping(value = "/deactivateTraining")
	public ServiceResponse deactivateTraining(@RequestBody TrainingMasterDTO trainingDTO) {
		try {
			if (trainingDTO == null || trainingDTO.getTrainingId() == null || trainingDTO.getUpdatedBy() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training ID and Updated By are required");
				return response;
			}
			return trainingConfigService.deactivateTraining(trainingDTO.getTrainingId(), trainingDTO.getUpdatedBy());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// Download Content - Accessible to both HR and employees (no JobRoleAccess restriction for viewing)
	@GetMapping(value = "/downloadContent/{contentId}")
	public ResponseEntity<Resource> downloadContent(@PathVariable Integer contentId) {
		try {
			System.out.println("Download request received for contentId: " + contentId);
			ServiceResponse response = trainingConfigService.downloadContent(contentId);
			
			if (response.getServiceStatus() != null && response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				Resource resource = (Resource) response.getServiceResponse();
				String fileName = (String) response.getServiceResponse1();
				String mimeType = (String) response.getServiceResponse2();
				
				if (resource == null) {
					System.err.println("Resource is null for contentId: " + contentId);
					return ResponseEntity.notFound().build();
				}
				
				// Check if resource exists
				try {
					if (!resource.exists()) {
						System.err.println("Resource doesn't exist for contentId: " + contentId);
						return ResponseEntity.notFound().build();
					}
				} catch (Exception e) {
					System.err.println("Error checking resource existence: " + e.getMessage());
				}
				
				// Ensure proper MIME type
				if (mimeType == null || mimeType.isEmpty()) {
					try {
						if (resource instanceof FileSystemResource) {
							mimeType = Files.probeContentType(((FileSystemResource) resource).getFile().toPath());
						}
					} catch (Exception e) {
						// Ignore
					}
					if (mimeType == null || mimeType.isEmpty()) {
						mimeType = "application/octet-stream";
					}
				}
				
				// URL encode filename for proper handling
				String encodedFileName = fileName;
				try {
					encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
				} catch (Exception e) {
					// If encoding fails, use original filename
				}
				
				long contentLength = -1;
				try {
					contentLength = resource.contentLength();
				} catch (Exception e) {
					// If can't get length, continue without it
				}
				
				System.out.println("Sending file: " + fileName + ", MIME: " + mimeType + ", Size: " + (contentLength > 0 ? contentLength : "unknown"));
				
				ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(mimeType))
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
						.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
						.header(HttpHeaders.PRAGMA, "no-cache")
						.header(HttpHeaders.EXPIRES, "0");
				
				if (contentLength > 0) {
					builder.contentLength(contentLength);
				}
				
				return builder.body(resource);
			} else {
				// Log the error for debugging
				String errorMsg = response.getServiceResponse() != null ? response.getServiceResponse().toString() : "Unknown error";
				System.err.println("Download failed for contentId " + contentId + ": " + errorMsg);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Download exception for contentId " + contentId + ": " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// ==================== Reporting APIs ====================
	
	// @JobRoleAccess(featureIds = {3}) // Training Config - View Training History
	// @PostMapping(value = "/getEmployeeTrainingHistory")
	// public ServiceResponse getEmployeeTrainingHistory(@RequestBody TrainingRequestDTO request) {
	// 	try {
	// 		if (request == null || request.getEmpId() == null) {
	// 			ServiceResponse response = new ServiceResponse();
	// 			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	// 			response.setServiceResponse("Employee ID is required");
	// 			return response;
	// 		}
	// 		return trainingConfigService.getEmployeeTrainingHistory(request.getEmpId(), request.getTrainingId());
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		throw e;
	// 	}
	// }

	@JobRoleAccess(featureIds = {3}) // Training Config - Compliance Report
	@PostMapping(value = "/getComplianceReport")
	public ServiceResponse getComplianceReport(@RequestBody ComplianceReportDTO request) {
		try {
			if (request == null || request.getTrainingId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training ID is required");
				return response;
			}
			return trainingConfigService.getComplianceReport(
				request.getTrainingId(),
				null, // departmentId - to be added to DTO if needed
				null  // status - to be added to DTO if needed
			);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// ==================== Helper Methods ====================
	
	/**
	 * Save training file to disk
	 * Note: File saving is actually handled in service layer, but keeping this for backward compatibility
	 */
	private String saveTrainingFile(Integer trainingId, MultipartFile file, Long createdBy) throws IOException {
		// Create directory structure: {trainingFileLocation}/{trainingId}/
		Path trainingDir = Paths.get(trainingFileLocation, trainingId.toString());
		if (!Files.exists(trainingDir)) {
			Files.createDirectories(trainingDir);
		}
		
		// Generate unique filename: content_{timestamp}_{originalFilename}
		String timestamp = String.valueOf(System.currentTimeMillis());
		String originalFilename = file.getOriginalFilename();
		String newFileName = "content_" + timestamp + "_" + originalFilename;
		
		Path filePath = trainingDir.resolve(newFileName);
		Files.write(filePath, file.getBytes());
		
		// Return relative path for database storage
		// Store as "/{trainingId}/{filename}" - download will resolve correctly
		return "/" + trainingId + "/" + newFileName;
	}

	@JobRoleAccess(featureIds = {3}) // Training Config - Change Quiz Response
	@PutMapping(value = "/changeQuizResponse")
	private ServiceResponse changeQuizResponse(@RequestParam("empId") Long empId, @RequestParam("quizId") Long quizId, @RequestParam("responseStatus") String responseStatus, @RequestParam("updatedBy") Long updatedBy) {
		try {
			if (empId == null || quizId == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID and Quiz ID are required");
				return response;
			}
			return trainingConfigService.changeQuizResponse(empId, quizId, responseStatus, updatedBy);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	
}
