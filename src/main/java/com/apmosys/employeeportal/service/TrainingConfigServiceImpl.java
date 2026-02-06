package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.FileValidationException;
import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingHistoryDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.TrainingConsent;
import com.apmosys.employeeportal.model.TrainingContent;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.model.TrainingSkip;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TrainingConsentRepository;
import com.apmosys.employeeportal.repository.TrainingContentRepository;
import com.apmosys.employeeportal.repository.TrainingMasterRepository;
import com.apmosys.employeeportal.repository.TrainingSkipRepository;
import com.apmosys.employeeportal.serviceInterface.TrainingConfigService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.TrainingFileValidator;

/**
 * Service implementation for Training Configuration operations (HR/Admin)
 * Handles CRUD operations for training configuration and content management
 */
@Service
public class TrainingConfigServiceImpl implements TrainingConfigService {

	@Autowired
	private TrainingMasterRepository trainingMasterRepository;
	
	@Autowired
	private TrainingContentRepository trainingContentRepository;
	
	@Autowired
	private TrainingConsentRepository trainingConsentRepository;
	
	@Autowired
	private TrainingSkipRepository trainingSkipRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private StringToDateTimeParser stringToDateTimeParser;
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;
	
	@Value("${file.training.max.size.allowed}")
	private String maxFileSize;

	// ==================== HR Configuration APIs ====================
	@Override
	public ServiceResponse getAllTrainings(String activeStatus, String mandatoryFlag) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get All Trainings");
		apiLogInfo.setApiUrl("/api/training/getAllTrainings");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			List<TrainingMaster> trainings;
			
			if (activeStatus != null && mandatoryFlag != null) {
				trainings = trainingMasterRepository.findByMandatoryFlagAndActiveStatus(mandatoryFlag, activeStatus);
			} else if (activeStatus != null) {
				trainings = trainingMasterRepository.findByActiveStatus(activeStatus);
			} else {
				trainings = trainingMasterRepository.findAll();
			}
			
			List<TrainingMasterDTO> dtoList = trainings.stream().map(training -> {
				TrainingMasterDTO dto = convertToTrainingMasterDTO(training);
				return dto;
			}).collect(Collectors.toList());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			apiLogInfo.setApiResponse("Trainings fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addTrainingContent(TrainingContentDTO contentDTO, Long createdBy) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Add Training Content");
		apiLogInfo.setApiUrl("/api/training/addTrainingContent");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(contentDTO.getTrainingId())
				  .append(", Content Type: ").append(contentDTO.getContentType())
				  .append(", CreatedBy: ").append(createdBy);
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(contentDTO.getTrainingId());
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			
			// Validate content type and required fields
			if ("LINK".equals(contentDTO.getContentType())) {
				if (contentDTO.getExternalLinkUrl() == null || contentDTO.getExternalLinkUrl().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("External link URL is required for LINK content type");
					apiLogInfo.setApiResponse("External link URL is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
			} else {
				if (contentDTO.getContentPath() == null || contentDTO.getContentPath().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Content path is required for file content types");
					apiLogInfo.setApiResponse("Content path is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
			}
			
			// Check for overlapping active content
			List<TrainingContent> overlappingContent = trainingContentRepository.findOverlappingActiveContent(
				contentDTO.getTrainingId(),
				contentDTO.getEffectiveFrom(),
				contentDTO.getEffectiveTo()
			);
			
			// Deactivate overlapping content
			for (TrainingContent overlap : overlappingContent) {
				overlap.setActiveStatus("false");
				trainingContentRepository.save(overlap);
			}
			
			TrainingContent content = new TrainingContent();
			content.setTrainingMaster(training);
			content.setContentType(contentDTO.getContentType());
			content.setContentName(contentDTO.getContentName());
			content.setContentPath(contentDTO.getContentPath());
			content.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
			content.setEffectiveFrom(contentDTO.getEffectiveFrom());
			content.setEffectiveTo(contentDTO.getEffectiveTo());
			content.setFileSizeBytes(contentDTO.getFileSizeBytes());
			content.setMimeType(contentDTO.getMimeType());
			content.setActiveStatus(contentDTO.getActiveStatus() != null ? contentDTO.getActiveStatus() : "true");
			content.setCreatedBy(createdBy);
			
			TrainingContent savedContent = trainingContentRepository.save(content);
			
			if (savedContent.getContentId() != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Content added successfully");
				response.setServiceMessage(savedContent.getContentId().toString());
				apiLogInfo.setApiResponse("Content added successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to add content");
				apiLogInfo.setApiResponse("Failed to add content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long createdBy, MultipartFile file) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Create Training With Content");
		apiLogInfo.setApiUrl("/api/training/createTrainingWithContent");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training Name: ").append(trainingDTO.getTrainingName())
				  .append(", Content Type: ").append(contentDTO.getContentType())
				  .append(", CreatedBy: ").append(createdBy);
		
		try {
			// Validation: Check for duplicate training name
			Optional<TrainingMaster> existingTraining = trainingMasterRepository.findByTrainingNameIgnoreCase(trainingDTO.getTrainingName().trim());
			if (existingTraining.isPresent()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training name already exists. Please use a different name.");
				apiLogInfo.setApiResponse("Duplicate training name");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Validation: Training effectiveTo date is required
			if (trainingDTO.getEffectiveTo() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training effective to date is required");
				apiLogInfo.setApiResponse("Training effective to date is null");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Validation: Content effectiveTo date is required
			if (contentDTO.getEffectiveTo() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective to date is required");
				apiLogInfo.setApiResponse("Content effective to date is null");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Validation: Training date range (effectiveFrom < effectiveTo)
			if (trainingDTO.getEffectiveFrom().after(trainingDTO.getEffectiveTo())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training effective from date must be before effective to date");
				apiLogInfo.setApiResponse("Invalid training date range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Validation: Content date range (effectiveFrom < effectiveTo)
			if (contentDTO.getEffectiveFrom().after(contentDTO.getEffectiveTo())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective from date must be before effective to date");
				apiLogInfo.setApiResponse("Invalid content date range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Validation: Content dates should be within training dates
			if (contentDTO.getEffectiveFrom().before(trainingDTO.getEffectiveFrom()) ||
				contentDTO.getEffectiveFrom().after(trainingDTO.getEffectiveTo())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective from date must be within training effective dates");
				apiLogInfo.setApiResponse("Content date out of training range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			if (contentDTO.getEffectiveTo().after(trainingDTO.getEffectiveTo())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective to date must be within training effective dates");
				apiLogInfo.setApiResponse("Content date out of training range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			if (!"LINK".equals(contentDTO.getContentType())) {
					    TrainingFileValidator.validateFile(
					            file,
					            contentDTO.getContentType(),
					            maxFileSize
					    );
					
				
			}
			 
			
			// Step 1: Create Training
			TrainingMaster training = new TrainingMaster();
			training.setTrainingName(trainingDTO.getTrainingName().trim());
			training.setTrainingType(trainingDTO.getTrainingType());
			training.setMandatoryFlag(trainingDTO.getMandatoryFlag() != null ? trainingDTO.getMandatoryFlag() : "false");
			training.setEffectiveFrom(trainingDTO.getEffectiveFrom());
			training.setEffectiveTo(trainingDTO.getEffectiveTo());
//			training.setFrequencyPerYear(trainingDTO.getFrequencyPerYear() != null ? trainingDTO.getFrequencyPerYear() : 2);
			training.setLockEnabled(trainingDTO.getLockEnabled() != null ? trainingDTO.getLockEnabled() : "false");
			training.setMinViewTimeMinutes(trainingDTO.getMinViewTimeMinutes());
			training.setConsentRequired(trainingDTO.getConsentRequired() != null ? trainingDTO.getConsentRequired() : "true");
			// If lock is enabled, skip must be false (lock means hard mandatory)
			if ("true".equals(trainingDTO.getLockEnabled())) {
				training.setSkipAllowed("false");
				training.setMandatoryFlag("true");
			} else {
				training.setSkipAllowed(trainingDTO.getSkipAllowed() != null ? trainingDTO.getSkipAllowed() : "true");
			}
			// Deadline is mandatory - always enabled
			training.setDeadlineEnabled("true");
			// Set default pattern to YEARLY if not provided
			training.setDeadlinePattern(trainingDTO.getDeadlinePattern() != null && !trainingDTO.getDeadlinePattern().isEmpty() 
				? trainingDTO.getDeadlinePattern() : "YEARLY");
			training.setCustomDeadlineMonths(trainingDTO.getCustomDeadlineMonths());
			training.setActiveStatus(trainingDTO.getActiveStatus() != null ? trainingDTO.getActiveStatus() : "true");
			training.setCreatedBy(createdBy);
			
			TrainingMaster savedTraining = trainingMasterRepository.save(training);
			
			if (savedTraining.getTrainingId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to create training");
				apiLogInfo.setApiResponse("Failed to create training");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Step 2: Create Content
			String filePath=null;
			// Handle file upload if present
			if (file != null && !file.isEmpty() && !"LINK".equals(contentDTO.getContentType())) {
				try {
				    filePath =generateTrainingFilePath( savedTraining.getTrainingId(), file); 
					contentDTO.setContentPath(filePath);
					contentDTO.setFileSizeBytes(file.getSize());
					contentDTO.setMimeType(file.getContentType());
				} catch (Exception e) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Error saving file: " + e.getMessage());
					apiLogInfo.setApiResponse("Error saving file");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
			}
			
			// Validate content type and required fields
			if ("LINK".equals(contentDTO.getContentType())) {
				if (contentDTO.getExternalLinkUrl() == null || contentDTO.getExternalLinkUrl().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("External link URL is required for LINK content type");
					apiLogInfo.setApiResponse("External link URL is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
			} else {
				if (contentDTO.getContentPath() == null || contentDTO.getContentPath().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("File is required for file content types");
					apiLogInfo.setApiResponse("File is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
			}
			
			// Check for overlapping active content (should be none for new training, but check anyway)
			List<TrainingContent> overlappingContent = trainingContentRepository.findOverlappingActiveContent(
				savedTraining.getTrainingId(),
				contentDTO.getEffectiveFrom(),
				contentDTO.getEffectiveTo()
			);
			
			// Deactivate overlapping content
			for (TrainingContent overlap : overlappingContent) {
				overlap.setActiveStatus("false");
				trainingContentRepository.save(overlap);
			}
			
			TrainingContent content = new TrainingContent();
			content.setTrainingMaster(savedTraining);
			content.setContentType(contentDTO.getContentType());
			content.setContentName(contentDTO.getContentName());
			content.setContentPath(contentDTO.getContentPath());
			content.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
			content.setEffectiveFrom(contentDTO.getEffectiveFrom());
			content.setEffectiveTo(contentDTO.getEffectiveTo());
			content.setFileSizeBytes(contentDTO.getFileSizeBytes());
			content.setMimeType(contentDTO.getMimeType());
			content.setActiveStatus(contentDTO.getActiveStatus() != null ? contentDTO.getActiveStatus() : "true");
			content.setCreatedBy(createdBy);
			
			TrainingContent savedContent = trainingContentRepository.save(content);
			
			if (file != null && !file.isEmpty() && !"LINK".equals(contentDTO.getContentType())) {
			    saveTrainingFile(filePath, file);
			}
			
			if (savedContent.getContentId() != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(savedTraining); // Return training with ID
				response.setServiceMessage("Training and content created successfully");
				apiLogInfo.setApiResponse("Training and content created successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training created but failed to add content");
				apiLogInfo.setApiResponse("Training created but failed to add content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		} 
		
		catch (FileValidationException e) {
              e.printStackTrace();
		    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		    response.setServiceResponse(e.getMessage());
		    response.setServiceError(e.getMessage());

		    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		    apiLogInfo.setApiResponse(e.getMessage());
		    apiLogInfo.setLogLevel("WARN");
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long updatedBy, MultipartFile file) {

	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Update Training With Content");
	    apiLogInfo.setApiUrl("/api/training/updateTrainingWithContent");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Training ID: ").append(trainingDTO.getTrainingId())
	            .append(", Content Type: ").append(contentDTO.getContentType())
	            .append(", UpdatedBy: ").append(updatedBy);

	    String filePath = null; 

	    try {

	        Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingDTO.getTrainingId());

	        if (trainingOpt.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Training not found");
	            return response;
	        }

	        TrainingMaster training = trainingOpt.get();

	        /* ================= TRAINING UPDATE (UNCHANGED) ================= */

	        if (trainingDTO.getTrainingName() != null && !trainingDTO.getTrainingName().trim().isEmpty()) {

	            Optional<TrainingMaster> existingTraining =
	                    trainingMasterRepository.findByTrainingNameAndNotTrainingId(
	                            trainingDTO.getTrainingName().trim(),
	                            trainingDTO.getTrainingId());

	            if (existingTraining.isPresent()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Training name already exists.");
	                return response;
	            }
	        }

	        Date trainingEffectiveFrom =
	                trainingDTO.getEffectiveFrom() != null
	                        ? trainingDTO.getEffectiveFrom()
	                        : training.getEffectiveFrom();

	        Date trainingEffectiveTo =
	                trainingDTO.getEffectiveTo() != null
	                        ? trainingDTO.getEffectiveTo()
	                        : training.getEffectiveTo();

	        if (trainingEffectiveFrom.after(trainingEffectiveTo)) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Training effective from date must be before effective to date");
	            return response;
	        }

	        if (trainingDTO.getTrainingName() != null)
	            training.setTrainingName(trainingDTO.getTrainingName().trim());

	        if (trainingDTO.getTrainingType() != null)
	            training.setTrainingType(trainingDTO.getTrainingType());

	        if (trainingDTO.getMandatoryFlag() != null)
	            training.setMandatoryFlag(trainingDTO.getMandatoryFlag());

	        if (trainingDTO.getEffectiveFrom() != null)
	            training.setEffectiveFrom(trainingDTO.getEffectiveFrom());

	        if (trainingDTO.getEffectiveTo() != null)
	            training.setEffectiveTo(trainingDTO.getEffectiveTo());

	        if (trainingDTO.getLockEnabled() != null)
	            training.setLockEnabled(trainingDTO.getLockEnabled());

	        if ("true".equals(trainingDTO.getLockEnabled())) {
	            training.setSkipAllowed("false");
	            training.setMandatoryFlag("true");


	        }
	        else if (trainingDTO.getSkipAllowed() != null)
	            training.setSkipAllowed(trainingDTO.getSkipAllowed());

	        training.setDeadlineEnabled("true");

	        if (trainingDTO.getDeadlinePattern() != null)
	            training.setDeadlinePattern(trainingDTO.getDeadlinePattern());

	        if (trainingDTO.getCustomDeadlineMonths() != null)
	            training.setCustomDeadlineMonths(trainingDTO.getCustomDeadlineMonths());

	        if (trainingDTO.getActiveStatus() != null)
	            training.setActiveStatus(trainingDTO.getActiveStatus());

	        training.setUpdatedBy(updatedBy);

	        TrainingMaster updatedTraining = trainingMasterRepository.save(training);

	        /* ================= CONTENT VALIDATION ================= */

	        if (!"LINK".equals(contentDTO.getContentType())) {

	            if (file != null && !file.isEmpty()) {

	                TrainingFileValidator.validateFile(
	                        file,
	                        contentDTO.getContentType(),
	                        maxFileSize
	                );

	                filePath = generateTrainingFilePath(trainingDTO.getTrainingId(), file);

	                contentDTO.setContentPath(filePath);
	                contentDTO.setFileSizeBytes(file.getSize());
	                contentDTO.setMimeType(file.getContentType());
	            }
	        }

	        Optional<TrainingContent> existingContentOpt =
                    trainingContentRepository.findByContentId(contentDTO.getContentId());
	        
	        if (existingContentOpt.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Content not found for update");
                return response;
            }
                TrainingContent existing=null;
                if(existingContentOpt.isPresent()) {
                	existing=existingContentOpt.get();	
                }
                
                
	        /* ===== Existing Content Fetch If No File Uploaded ===== */
                
             if(existing.getContentType().equals("LINK")){
	        		
	        		existing.setContentPath(null);	
		        	existing.setFileSizeBytes(0L);
		        	existing.setMimeType(null);
		        	
		        	//TODO delete file  if it was not link eirlier
		        	
		        	
	         }

             else if (!"LINK".equals(contentDTO.getContentType())
	                && (file == null || file.isEmpty())
	                && contentDTO.getContentId() != null) {
                    contentDTO.setContentPath(existing.getContentPath());
	                contentDTO.setFileSizeBytes(existing.getFileSizeBytes());
	                contentDTO.setMimeType(existing.getMimeType());
	            
	        }else if(!"LINK".equals(contentDTO.getContentType())
	        		&& (file != null || !file.isEmpty() )
	        		&& ! existing.getContentType().equals("LINK")) {
	        	
	        	//TODO Delete old file
	        }
	        	
	        /* ================= CONTENT SAVE ================= */

	        TrainingContent content;

	        if (contentDTO.getContentId() != null) {

	            content = existing;
                content.setContentType(contentDTO.getContentType());
	            content.setContentName(contentDTO.getContentName());
	            content.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
	            content.setEffectiveFrom(contentDTO.getEffectiveFrom());
	            content.setEffectiveTo(contentDTO.getEffectiveTo());

	            if (contentDTO.getContentPath() != null) {
	                content.setContentPath(contentDTO.getContentPath());
	                content.setFileSizeBytes(contentDTO.getFileSizeBytes());
	                content.setMimeType(contentDTO.getMimeType());
	            }

	            content.setUpdatedBy(updatedBy);
	            content.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
	        }
	        else {

	            content = new TrainingContent();
	            content.setTrainingMaster(updatedTraining);
	            content.setContentType(contentDTO.getContentType());
	            content.setContentName(contentDTO.getContentName());
	            content.setContentPath(contentDTO.getContentPath());
	            content.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
	            content.setEffectiveFrom(contentDTO.getEffectiveFrom());
	            content.setEffectiveTo(contentDTO.getEffectiveTo());
	            content.setFileSizeBytes(contentDTO.getFileSizeBytes());
	            content.setMimeType(contentDTO.getMimeType());
	            content.setActiveStatus("true");
	            content.setCreatedBy(updatedBy);
	        }

	        TrainingContent savedContent = trainingContentRepository.save(content);
	        
	        

	        /* SAVE FILE AFTER DB SAVE */
	        if (filePath != null) {
	            saveTrainingFile(filePath, file);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Training and content updated successfully");
	        response.setServiceMessage(savedContent.getContentId().toString());

	    }

	    catch (FileValidationException e) {

	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(e.getMessage());
	        response.setServiceError(e.getMessage());
	    }

	    catch (Exception e) {

	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

	@Override
	@Transactional
	public ServiceResponse updateTrainingContent(TrainingContentDTO contentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Training Content");
		apiLogInfo.setApiUrl("/api/training/updateTrainingContent");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(contentDTO.getContentId());
			
			if (contentOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content not found");
				return response;
			}
			
			TrainingContent content = contentOpt.get();
			
			if (contentDTO.getContentName() != null) {
				content.setContentName(contentDTO.getContentName());
			}
			if (contentDTO.getEffectiveFrom() != null) {
				content.setEffectiveFrom(contentDTO.getEffectiveFrom());
			}
			if (contentDTO.getEffectiveTo() != null) {
				content.setEffectiveTo(contentDTO.getEffectiveTo());
			}
			if (contentDTO.getActiveStatus() != null) {
				content.setActiveStatus(contentDTO.getActiveStatus());
			}
			content.setUpdatedBy(contentDTO.getUpdatedBy());
			content.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
			
			// Check for overlaps if dates changed
			if (contentDTO.getEffectiveFrom() != null || contentDTO.getEffectiveTo() != null) {
				List<TrainingContent> overlappingContent = trainingContentRepository.findOverlappingActiveContent(
					content.getTrainingMaster().getTrainingId(),
					content.getEffectiveFrom(),
					content.getEffectiveTo()
				);
				
				// Remove self from overlap list
				overlappingContent.removeIf(c -> c.getContentId().equals(content.getContentId()));
				
				// Deactivate overlapping content
				for (TrainingContent overlap : overlappingContent) {
					overlap.setActiveStatus("false");
					trainingContentRepository.save(overlap);
				}
			}
			
			trainingContentRepository.save(content);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Content updated successfully");
			
		} 
		catch (FileValidationException e) {
            e.printStackTrace();
		    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		    response.setServiceResponse(e.getMessage());
		    response.setServiceError(e.getMessage());

		    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		    apiLogInfo.setApiResponse(e.getMessage());
		    apiLogInfo.setLogLevel("WARN");
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong."+e.getMessage());
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getTrainingContent(Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			List<TrainingContent> contents = trainingContentRepository.findByTrainingMaster_TrainingId(trainingId);
			LocalDate today = LocalDate.now();
			
			List<TrainingContentDTO> dtoList = contents.stream().map(content -> {
				TrainingContentDTO dto = convertToTrainingContentDTO(content);
				
				// Check if currently active
				boolean isActive = "true".equals(content.getActiveStatus()) &&
								   content.getEffectiveFrom().toLocalDate().isBefore(today) &&
								   (content.getEffectiveTo() == null || content.getEffectiveTo().toLocalDate().isAfter(today));
				dto.setIsCurrentlyActive(isActive);
				
				return dto;
			}).collect(Collectors.toList());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		return response;
	}

	@Override
	@Transactional
	public ServiceResponse deactivateTraining(Integer trainingId, Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Deactivate Training");
		apiLogInfo.setApiUrl("/api/training/deactivateTraining");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
			
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			training.setActiveStatus("false");
			training.setUpdatedBy(updatedBy);
			training.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
			
			trainingMasterRepository.save(training);
			
			// Count employees with pending training (for response message)
			// This would require a query to count pending trainings
			// For now, just return success
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training deactivated successfully");
			apiLogInfo.setApiResponse("Training deactivated successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse downloadContent(Integer contentId) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Download Training Content");
	    apiLogInfo.setApiUrl("/api/training/downloadContent");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(contentId);

	        if (contentOpt.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Content not found");
	            return response;
	        }

	        TrainingContent content = contentOpt.get();

	        // Block external links
	        if ("LINK".equalsIgnoreCase(content.getContentType())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("External link content cannot be downloaded");
	            return response;
	        }

	        if (content.getContentPath() == null || content.getContentPath().isBlank()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Content file path not found");
	            return response;
	        }

	        // Resolve paths safely
	        Path basePath = Paths.get(trainingFileLocation)
	                             .toAbsolutePath()
	                             .normalize();

	        Path filePath = basePath
	                .resolve(content.getContentPath())
	                .normalize();

	        // Path traversal protection
	        if (!filePath.startsWith(basePath)) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid file path");
	            return response;
	        }

	        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("File not found on server");
	            return response;
	        }

	        Resource resource = new FileSystemResource(filePath);

	        // ---------- Correct filename extraction ----------
	        String storedFileName = filePath.getFileName().toString();
	        String downloadFileName = storedFileName;

	        // Remove "content_<uuid>_" prefix only
	        if (storedFileName.startsWith("content_")) {
	            int index = storedFileName.indexOf("_", "content_".length());
	            if (index > 0 && index + 1 < storedFileName.length()) {
	                downloadFileName = storedFileName.substring(index + 1);
	            }
	        }

	        // Override with content name if present (keep extension)
	        if (content.getContentName() != null && !content.getContentName().isBlank()) {
	            String extension = "";
	            int dotIndex = downloadFileName.lastIndexOf('.');
	            if (dotIndex > -1) {
	                extension = downloadFileName.substring(dotIndex);
	            }
	            downloadFileName = content.getContentName() + extension;
	        }

	        // MIME type handling
	        String mimeType = content.getMimeType();
	        if (mimeType == null || mimeType.isBlank()) {
	            mimeType = Files.probeContentType(filePath);
	        }
	        if (mimeType == null) {
	            mimeType = "application/octet-stream";
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(resource);
	        response.setServiceResponse1(downloadFileName);
	        response.setServiceResponse2(mimeType);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Content downloaded successfully");

	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getEmployeeTrainingHistory(Long empId, Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Employee Training History");
		apiLogInfo.setApiUrl("/api/training/getEmployeeTrainingHistory");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			List<TrainingHistoryDTO> historyList = new ArrayList<>();
			
			// Get all consents for employee
			List<TrainingConsent> consents;
			if (trainingId != null) {
				consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
			} else {
				// Get all consents for employee (all trainings) - need to get all trainings first
				// For now, get all active trainings and then get consents for each
				List<TrainingMaster> allTrainings = trainingMasterRepository.findAll();
				consents = new ArrayList<>();
				for (TrainingMaster training : allTrainings) {
					consents.addAll(trainingConsentRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId()));
				}
			}
			
			// Get all skips for employee
			List<TrainingSkip> skips;
			if (trainingId != null) {
				skips = trainingSkipRepository.findByEmpIdAndTrainingId(empId, trainingId);
			} else {
				// Get all skips for employee (all trainings) - need to get all trainings first
				List<TrainingMaster> allTrainings = trainingMasterRepository.findAll();
				skips = new ArrayList<>();
				for (TrainingMaster training : allTrainings) {
					skips.addAll(trainingSkipRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId()));
				}
			}
			
			// Combine consents into history
			for (TrainingConsent consent : consents) {
				TrainingHistoryDTO dto = new TrainingHistoryDTO();
				dto.setTrainingId(consent.getTrainingMaster().getTrainingId());
				dto.setTrainingName(consent.getTrainingMaster().getTrainingName());
				dto.setTrainingType(consent.getTrainingMaster().getTrainingType());
				dto.setCompletedOn(formatTimestampToString(consent.getConsentTimestamp()));
				dto.setStatus("COMPLETED");
				dto.setCycleNumber(consent.getCompletionCycleNumber());
				dto.setContentId(consent.getTrainingContent().getContentId());
				dto.setContentName(consent.getTrainingContent().getContentName());
				dto.setContentType(consent.getTrainingContent().getContentType());
				
				// Count completions
				Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
				Long completionCount = trainingConsentRepository.countCompletionsInLast12Months(
					empId, 
					consent.getTrainingMaster().getTrainingId(), 
					fromDate
				);
				dto.setCompletionCount(completionCount != null ? completionCount.intValue() : 0);
				
				historyList.add(dto);
			}
			
			// Add skips
			for (TrainingSkip skip : skips) {
				TrainingHistoryDTO dto = new TrainingHistoryDTO();
				dto.setTrainingId(skip.getTrainingMaster().getTrainingId());
				dto.setTrainingName(skip.getTrainingMaster().getTrainingName());
				dto.setTrainingType(skip.getTrainingMaster().getTrainingType());
				dto.setStatus("SKIPPED");
				dto.setCycleNumber(skip.getCycleNumber());
				dto.setSkipCount(skip.getSkipCount());
				historyList.add(dto);
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(historyList);
			apiLogInfo.setApiResponse("Training history fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getComplianceReport(Integer trainingId, Long departmentId, String status) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Compliance Report");
		apiLogInfo.setApiUrl("/api/training/getComplianceReport");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			ComplianceReportDTO dto = new ComplianceReportDTO();
			dto.setTrainingId(trainingId);
			
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			dto.setTrainingName(training.getTrainingName());
			
			// Get current active content for this training
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(trainingId, PageRequest.of(0, 1))
			        .stream()
			        .findFirst();
			if (activeContentOpt.isEmpty()) {
				// No active content means no one can complete, so all are pending
				Long totalAssigned = employeeRepository.getTotalEmployeeCount();
				dto.setTotalAssigned(totalAssigned != null ? totalAssigned : 0L);
				dto.setCompleted(0L);
				dto.setPending(totalAssigned != null ? totalAssigned : 0L);
				dto.setSkipped(0L);
				dto.setCompliancePercentage(0.0);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
				apiLogInfo.setApiResponse("Compliance report generated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			TrainingContent activeContent = activeContentOpt.get();
			Integer activeContentId = activeContent.getContentId();
			
			// Get total assigned: All active employees (Phase-1: mandatory trainings assigned to all)
			Long totalAssigned = employeeRepository.getTotalEmployeeCount();
			if (totalAssigned == null) {
				totalAssigned = 0L;
			}
			dto.setTotalAssigned(totalAssigned);
			
			// Get all active employee IDs
			List<Long> activeEmployeeIds = employeeRepository.findAllActiveEmployees();
			
			// Calculate current cycle for compliance (use cycle 1 for Phase-1 basic calculation)
			// For more accurate reporting, we'd need to check each employee's cycle, but for Phase-1, 
			// we'll use a simplified approach: check if they have consent for the current active content
			// in any cycle (since consent is per content)
			
			long completedCount = 0L;
			long skippedCount = 0L;
			
			// For each active employee, check if they have consent for the current active content
			for (Long empId : activeEmployeeIds) {
				// Check if employee has consent for this training's current active content (any cycle)
				List<TrainingConsent> consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
				boolean hasConsentForActiveContent = consents.stream()
					.anyMatch(c -> c.getTrainingContent().getContentId().equals(activeContentId));
				
				if (hasConsentForActiveContent) {
					completedCount++;
				} else {
					// Check if they have skipped
					List<TrainingSkip> skips = trainingSkipRepository.findByEmpIdAndTrainingId(empId, trainingId);
					if (!skips.isEmpty()) {
						skippedCount++;
					}
				}
			}
			
			dto.setCompleted(completedCount);
			dto.setSkipped(skippedCount);
			dto.setPending(totalAssigned - completedCount - skippedCount);
			
			// Calculate compliance percentage
			double compliancePercentage = 0.0;
			if (totalAssigned > 0) {
				compliancePercentage = (completedCount * 100.0) / totalAssigned;
			}
			dto.setCompliancePercentage(compliancePercentage);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dto);
			apiLogInfo.setApiResponse("Compliance report generated successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	// ==================== Helper Methods ====================
	
	/**
	 * Count completions in last 12 months for an employee and training
	 * Used by reporting methods
	 */
	private int countCompletionsInLast12Months(Long empId, Integer trainingId) {
		Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
		Long count = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, fromDate);
		return count != null ? count.intValue() : 0;
	}
	

	/**
	 * Convert TrainingMaster entity to DTO
	 */
	private TrainingMasterDTO convertToTrainingMasterDTO(TrainingMaster training) {
		TrainingMasterDTO dto = new TrainingMasterDTO();
		dto.setTrainingId(training.getTrainingId());
		dto.setTrainingName(training.getTrainingName());
		dto.setTrainingType(training.getTrainingType());
		dto.setMandatoryFlag(training.getMandatoryFlag());
		dto.setEffectiveFrom(training.getEffectiveFrom());
		dto.setEffectiveTo(training.getEffectiveTo());
//		dto.setFrequencyPerYear(training.getFrequencyPerYear());
		dto.setLockEnabled(training.getLockEnabled());
		dto.setMinViewTimeMinutes(training.getMinViewTimeMinutes());
		dto.setConsentRequired(training.getConsentRequired());
		dto.setSkipAllowed(training.getSkipAllowed());
		dto.setDeadlineEnabled(training.getDeadlineEnabled());
		dto.setDeadlinePattern(training.getDeadlinePattern());
		dto.setCustomDeadlineMonths(training.getCustomDeadlineMonths());
		dto.setActiveStatus(training.getActiveStatus());
		dto.setCreatedBy(training.getCreatedBy());
		
		if (training.getCreatedOn() != null) {
			dto.setCreatedOn(formatTimestampToString(training.getCreatedOn()));
		}
		if (training.getUpdatedOn() != null) {
			dto.setUpdatedOn(formatTimestampToString(training.getUpdatedOn()));
		}
		
		// Get employee names
		if (training.getCreatedBy() != null) {
			Optional<Employee> empOpt = employeeRepository.findById(training.getCreatedBy());
			if (empOpt.isPresent()) {
				dto.setCreatedByName(empOpt.get().getName());
			}
		}
		if (training.getUpdatedBy() != null) {
			Optional<Employee> empOpt = employeeRepository.findById(training.getUpdatedBy());
			if (empOpt.isPresent()) {
				dto.setUpdatedByName(empOpt.get().getName());
			}
		}
		
		return dto;
	}
	
	private String generateTrainingFilePath(Integer trainingId, MultipartFile file) {

	    String originalFilename = file.getOriginalFilename();
	    String safeOriginalName = originalFilename != null
	            ? originalFilename.replaceAll("\\s+", "_")
	            : "file";

	    String newFileName = "content_" + UUID.randomUUID() + "_" + safeOriginalName;

	    return trainingId + "/" + newFileName;
	}
	
	private String saveTrainingFile(String relativePath, MultipartFile file) throws IOException {

	    // Base directory + relative path
	    Path targetFile = Paths.get(trainingFileLocation)
	            .resolve(relativePath)
	            .toAbsolutePath()
	            .normalize();

	    // Create directory if not exists
	    if (!Files.exists(targetFile.getParent())) {
	        Files.createDirectories(targetFile.getParent());
	    }

	    // Save file using passed filename
	    try (InputStream in = file.getInputStream()) {
	        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
	    }

	    // Return same relative path
	    return relativePath;
	}

	

	private TrainingContentDTO convertToTrainingContentDTO(TrainingContent content) {
		TrainingContentDTO dto = new TrainingContentDTO();
		dto.setContentId(content.getContentId());
		dto.setTrainingId(content.getTrainingMaster().getTrainingId());
		dto.setTrainingName(content.getTrainingMaster().getTrainingName());
		dto.setContentType(content.getContentType());
		dto.setContentName(content.getContentName());
		dto.setContentPath(content.getContentPath());
		dto.setExternalLinkUrl(content.getExternalLinkUrl());
		dto.setEffectiveFrom(content.getEffectiveFrom());
		dto.setEffectiveTo(content.getEffectiveTo());
		dto.setFileSizeBytes(content.getFileSizeBytes());
		dto.setMimeType(content.getMimeType());
		dto.setActiveStatus(content.getActiveStatus());
		dto.setCreatedBy(content.getCreatedBy());
		
		if (content.getCreatedOn() != null) {
			dto.setCreatedOn(formatTimestampToString(content.getCreatedOn()));
		}
		if (content.getUpdatedOn() != null) {
			dto.setUpdatedOn(formatTimestampToString(content.getUpdatedOn()));
		}
		
		// Get employee names
		if (content.getCreatedBy() != null) {
			Optional<Employee> empOpt = employeeRepository.findById(content.getCreatedBy());
			if (empOpt.isPresent()) {
				dto.setCreatedByName(empOpt.get().getName());
			}
		}
		if (content.getUpdatedBy() != null) {
			Optional<Employee> empOpt = employeeRepository.findById(content.getUpdatedBy());
			if (empOpt.isPresent()) {
				dto.setUpdatedByName(empOpt.get().getName());
			}
		}
		
		return dto;
	}
	
	/**
	 * Helper method to format Timestamp to String
	 * Format: "dd-MM-yyyy HH:mm:ss"
	 */
	private String formatTimestampToString(Timestamp timestamp) {
		if (timestamp == null) {
			return null;
		}
		try {
			LocalDateTime localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			return localDateTime.format(formatter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
