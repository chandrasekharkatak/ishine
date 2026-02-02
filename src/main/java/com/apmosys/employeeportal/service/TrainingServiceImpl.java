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
import java.time.YearMonth;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.LockStatusDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PendingTrainingDTO;
import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingFrequencyDTO;
import com.apmosys.employeeportal.dto.TrainingHistoryDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
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
import com.apmosys.employeeportal.serviceInterface.TrainingService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class TrainingServiceImpl implements TrainingService {

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
	public ServiceResponse createTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long createdBy, org.springframework.web.multipart.MultipartFile file) {
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
			Optional<TrainingMaster> existingTraining = trainingMasterRepository.findByTrainingName(trainingDTO.getTrainingName().trim());
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
			
			// Step 1: Create Training
			TrainingMaster training = new TrainingMaster();
			training.setTrainingName(trainingDTO.getTrainingName().trim());
			training.setTrainingType(trainingDTO.getTrainingType());
			training.setMandatoryFlag(trainingDTO.getMandatoryFlag() != null ? trainingDTO.getMandatoryFlag() : "false");
			training.setEffectiveFrom(trainingDTO.getEffectiveFrom());
			training.setEffectiveTo(trainingDTO.getEffectiveTo());
			training.setFrequencyPerYear(trainingDTO.getFrequencyPerYear() != null ? trainingDTO.getFrequencyPerYear() : 2);
			training.setLockEnabled(trainingDTO.getLockEnabled() != null ? trainingDTO.getLockEnabled() : "false");
			training.setMinViewTimeMinutes(trainingDTO.getMinViewTimeMinutes());
			training.setConsentRequired(trainingDTO.getConsentRequired() != null ? trainingDTO.getConsentRequired() : "true");
			training.setSkipAllowed(trainingDTO.getSkipAllowed() != null ? trainingDTO.getSkipAllowed() : "true");
			training.setDeadlineEnabled(trainingDTO.getDeadlineEnabled() != null ? trainingDTO.getDeadlineEnabled() : "false");
			training.setDeadlinePattern(trainingDTO.getDeadlinePattern());
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
			// Handle file upload if present
			if (file != null && !file.isEmpty()) {
				try {
					String filePath = saveTrainingFile(savedTraining.getTrainingId(), file);
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
	@Transactional
	public ServiceResponse updateTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Training With Content");
		apiLogInfo.setApiUrl("/api/training/updateTrainingWithContent");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(trainingDTO.getTrainingId())
				  .append(", Content Type: ").append(contentDTO.getContentType())
				  .append(", UpdatedBy: ").append(updatedBy);
		
		try {
			// Step 1: Update Training
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingDTO.getTrainingId());
			
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
			
			// Validation: Check for duplicate training name (excluding current training)
			if (trainingDTO.getTrainingName() != null && !trainingDTO.getTrainingName().trim().isEmpty()) {
				Optional<TrainingMaster> existingTraining = trainingMasterRepository.findByTrainingNameAndNotTrainingId(
					trainingDTO.getTrainingName().trim(), trainingDTO.getTrainingId());
				if (existingTraining.isPresent()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Training name already exists. Please use a different name.");
					apiLogInfo.setApiResponse("Duplicate training name");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
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
			// Check if dates are being updated, if so validate them
			Date trainingEffectiveFrom = trainingDTO.getEffectiveFrom() != null ? trainingDTO.getEffectiveFrom() : training.getEffectiveFrom();
			Date trainingEffectiveTo = trainingDTO.getEffectiveTo() != null ? trainingDTO.getEffectiveTo() : training.getEffectiveTo();
			
			// Validate training date range if dates are provided
			if (trainingDTO.getEffectiveFrom() != null || trainingDTO.getEffectiveTo() != null) {
				// If updating effectiveTo, ensure it's not null
				if (trainingDTO.getEffectiveTo() == null && training.getEffectiveTo() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Training effective to date is required");
					apiLogInfo.setApiResponse("Training effective to date is null");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
				
				if (trainingEffectiveFrom.after(trainingEffectiveTo)) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Training effective from date must be before effective to date");
					apiLogInfo.setApiResponse("Invalid training date range");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}
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
			// Both content dates are mandatory, so validate against training dates
			if (contentDTO.getEffectiveFrom().before(trainingEffectiveFrom) ||
				contentDTO.getEffectiveFrom().after(trainingEffectiveTo)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective from date must be within training effective dates");
				apiLogInfo.setApiResponse("Content date out of training range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			if (contentDTO.getEffectiveTo().after(trainingEffectiveTo)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content effective to date must be within training effective dates");
				apiLogInfo.setApiResponse("Content date out of training range");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			if (trainingDTO.getTrainingName() != null) {
				training.setTrainingName(trainingDTO.getTrainingName().trim());
			}
			if (trainingDTO.getTrainingType() != null) {
				training.setTrainingType(trainingDTO.getTrainingType());
			}
			if (trainingDTO.getMandatoryFlag() != null) {
				training.setMandatoryFlag(trainingDTO.getMandatoryFlag());
			}
			if (trainingDTO.getEffectiveFrom() != null) {
				training.setEffectiveFrom(trainingDTO.getEffectiveFrom());
			}
			if (trainingDTO.getEffectiveTo() != null) {
				training.setEffectiveTo(trainingDTO.getEffectiveTo());
			}
			if (trainingDTO.getFrequencyPerYear() != null) {
				training.setFrequencyPerYear(trainingDTO.getFrequencyPerYear());
			}
			if (trainingDTO.getLockEnabled() != null) {
				training.setLockEnabled(trainingDTO.getLockEnabled());
			}
			if (trainingDTO.getMinViewTimeMinutes() != null) {
				training.setMinViewTimeMinutes(trainingDTO.getMinViewTimeMinutes());
			}
			if (trainingDTO.getConsentRequired() != null) {
				training.setConsentRequired(trainingDTO.getConsentRequired());
			}
			if (trainingDTO.getSkipAllowed() != null) {
				training.setSkipAllowed(trainingDTO.getSkipAllowed());
			}
			if (trainingDTO.getDeadlineEnabled() != null) {
				training.setDeadlineEnabled(trainingDTO.getDeadlineEnabled());
			}
			if (trainingDTO.getDeadlinePattern() != null) {
				training.setDeadlinePattern(trainingDTO.getDeadlinePattern());
			}
			if (trainingDTO.getCustomDeadlineMonths() != null) {
				training.setCustomDeadlineMonths(trainingDTO.getCustomDeadlineMonths());
			}
			if (trainingDTO.getActiveStatus() != null) {
				training.setActiveStatus(trainingDTO.getActiveStatus());
			}
			training.setUpdatedBy(updatedBy);
			
			TrainingMaster updatedTraining = trainingMasterRepository.save(training);
			
			// Step 2: Add/Update Content
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
				trainingDTO.getTrainingId(),
				contentDTO.getEffectiveFrom(),
				contentDTO.getEffectiveTo()
			);
			
			// Deactivate overlapping content
			for (TrainingContent overlap : overlappingContent) {
				overlap.setActiveStatus("false");
				trainingContentRepository.save(overlap);
			}
			
			TrainingContent content = new TrainingContent();
			content.setTrainingMaster(updatedTraining);
			content.setContentType(contentDTO.getContentType());
			content.setContentName(contentDTO.getContentName());
			content.setContentPath(contentDTO.getContentPath());
			content.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
			content.setEffectiveFrom(contentDTO.getEffectiveFrom());
			content.setEffectiveTo(contentDTO.getEffectiveTo());
			content.setFileSizeBytes(contentDTO.getFileSizeBytes());
			content.setMimeType(contentDTO.getMimeType());
			content.setActiveStatus(contentDTO.getActiveStatus() != null ? contentDTO.getActiveStatus() : "true");
			content.setCreatedBy(updatedBy); // Use updatedBy for new content
			
			TrainingContent savedContent = trainingContentRepository.save(content);
			
			if (savedContent.getContentId() != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Training and content updated successfully");
				response.setServiceMessage(savedContent.getContentId().toString());
				apiLogInfo.setApiResponse("Training and content updated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training updated but failed to add content");
				apiLogInfo.setApiResponse("Training updated but failed to add content");
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
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
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

	// ==================== Employee Training APIs ====================
	
	@Override
	public ServiceResponse getPendingTraining(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Pending Training");
		apiLogInfo.setApiUrl("/api/training/getPendingTraining");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			TrainingMaster pendingTraining = findPendingMandatoryTraining(empId);
			
			if (pendingTraining == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(null); // No pending training
				apiLogInfo.setApiResponse("No pending training");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Get current active content
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(pendingTraining.getTrainingId());
			
			if (activeContentOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(null); // No active content
				apiLogInfo.setApiResponse("No active content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			TrainingContent activeContent = activeContentOpt.get();
			
			// Calculate current cycle
			int currentCycle = calculateCurrentCycle(empId, pendingTraining.getTrainingId());
			
			// Calculate deadline
			LocalDate cycleDeadline = null;
			boolean isDeadlineCrossed = false;
			if ("true".equals(pendingTraining.getDeadlineEnabled()) && pendingTraining.getDeadlinePattern() != null) {
				cycleDeadline = calculateCycleDeadline(pendingTraining, currentCycle);
				if (cycleDeadline != null) {
					isDeadlineCrossed = LocalDate.now().isAfter(cycleDeadline);
				}
			}
			
			PendingTrainingDTO dto = new PendingTrainingDTO();
			dto.setTrainingId(pendingTraining.getTrainingId());
			dto.setTrainingName(pendingTraining.getTrainingName());
			dto.setTrainingType(pendingTraining.getTrainingType());
			dto.setMinViewTimeMinutes(pendingTraining.getMinViewTimeMinutes());
			dto.setConsentRequired(pendingTraining.getConsentRequired());
			dto.setSkipAllowed(pendingTraining.getSkipAllowed());
			dto.setDeadlineEnabled(pendingTraining.getDeadlineEnabled());
			dto.setDeadlinePattern(pendingTraining.getDeadlinePattern());
			dto.setCustomDeadlineMonths(pendingTraining.getCustomDeadlineMonths());
			dto.setCurrentCycleNumber(currentCycle);
			if (cycleDeadline != null) {
				dto.setCurrentCycleDeadline(Date.valueOf(cycleDeadline));
			}
			dto.setIsDeadlineCrossed(isDeadlineCrossed);
			
			TrainingContentDTO contentDTO = convertToTrainingContentDTO(activeContent);
			dto.setContent(contentDTO);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dto);
			apiLogInfo.setApiResponse("Pending training fetched successfully");
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
	@Transactional
	public ServiceResponse submitConsent(TrainingConsentDTO consentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Submit Training Consent");
		apiLogInfo.setApiUrl("/api/training/submitConsent");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(consentDTO.getTrainingId())
				  .append(", Content ID: ").append(consentDTO.getContentId())
				  .append(", Emp ID: ").append(consentDTO.getEmpId())
				  .append(", Cycle: ").append(consentDTO.getCompletionCycleNumber());
		
		try {
			// Calculate cycle number if not provided
			Integer cycleNumber = consentDTO.getCompletionCycleNumber();
			if (cycleNumber == null || cycleNumber == 0) {
				cycleNumber = calculateCurrentCycle(consentDTO.getEmpId(), consentDTO.getTrainingId());
				consentDTO.setCompletionCycleNumber(cycleNumber);
			}
			
			// Validate that content is current active content
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(consentDTO.getTrainingId());
			
			if (activeContentOpt.isEmpty() || !activeContentOpt.get().getContentId().equals(consentDTO.getContentId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Content is not the current active content for this training");
				apiLogInfo.setApiResponse("Invalid content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			// Check if consent already exists
			Optional<TrainingConsent> existingConsent = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(
				consentDTO.getEmpId(),
				consentDTO.getTrainingId(),
				consentDTO.getContentId(),
				cycleNumber
			);
			
			if (existingConsent.isPresent()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Consent already submitted");
				apiLogInfo.setApiResponse("Consent already exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(consentDTO.getTrainingId());
			Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(consentDTO.getContentId());
			
			if (trainingOpt.isEmpty() || contentOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training or content not found");
				apiLogInfo.setApiResponse("Training/content not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			
			TrainingConsent consent = new TrainingConsent();
			consent.setTrainingMaster(trainingOpt.get());
			consent.setTrainingContent(contentOpt.get());
			consent.setEmpId(consentDTO.getEmpId());
			consent.setCompletionCycleNumber(cycleNumber);
			consent.setCreatedBy(consentDTO.getEmpId());
			
			TrainingConsent savedConsent = trainingConsentRepository.save(consent);
			
			// Delete skip record if exists (use the same cycle number)
			Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
				consentDTO.getEmpId(),
				consentDTO.getTrainingId(),
				cycleNumber
			);
			if (skipOpt.isPresent()) {
				trainingSkipRepository.delete(skipOpt.get());
			}
			
			// Check lock status
			LockStatusDTO lockStatus = getLockStatusInternal(consentDTO.getEmpId());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Consent submitted successfully");
			response.setServiceMessage(lockStatus.getIsLocked().toString());
			apiLogInfo.setApiResponse("Consent submitted successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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
	@Transactional
	public ServiceResponse skipTraining(TrainingSkipDTO skipDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Skip Training");
		apiLogInfo.setApiUrl("/api/training/skipTraining");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(skipDTO.getTrainingId());
			
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			
			// Validate skip allowed
			if (!"true".equals(training.getSkipAllowed())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Skip is not allowed for this training");
				return response;
			}
			
			// Calculate cycle number if not provided
			Integer cycleNumber = skipDTO.getCycleNumber();
			if (cycleNumber == null || cycleNumber == 0) {
				cycleNumber = calculateCurrentCycle(skipDTO.getEmpId(), skipDTO.getTrainingId());
			}
			
			// Check deadline
			if ("true".equals(training.getDeadlineEnabled()) && training.getDeadlinePattern() != null) {
				LocalDate cycleDeadline = calculateCycleDeadline(training, cycleNumber);
				
				if (cycleDeadline != null && LocalDate.now().isAfter(cycleDeadline)) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Deadline has passed. Skip is not allowed");
					return response;
				}
			}
			
			// Check if skip record exists
			Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
				skipDTO.getEmpId(),
				skipDTO.getTrainingId(),
				cycleNumber
			);
			
			TrainingSkip skip;
			if (skipOpt.isPresent()) {
				// Update existing record
				skip = skipOpt.get();
				skip.setSkipCount(skip.getSkipCount() + 1);
				// last_skipped_on will be auto-updated by database
			} else {
				// Create new record
				skip = new TrainingSkip();
				skip.setTrainingMaster(training);
				skip.setEmpId(skipDTO.getEmpId());
				skip.setCycleNumber(cycleNumber);
				skip.setSkipCount(1);
			}
			
			TrainingSkip savedSkip = trainingSkipRepository.save(skip);
			
			// Check lock status
			LockStatusDTO lockStatus = getLockStatusInternal(skipDTO.getEmpId());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training skipped successfully");
			apiLogInfo.setApiResponse("Training skipped successfully");
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
	public ServiceResponse getLockStatus(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Lock Status");
		apiLogInfo.setApiUrl("/api/training/getLockStatus");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			LockStatusDTO lockStatus = getLockStatusInternal(empId);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(lockStatus);
			apiLogInfo.setApiResponse("Lock status fetched successfully");
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
	public ServiceResponse getUserTrainings(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get User Trainings");
		apiLogInfo.setApiUrl("/api/training/getUserTrainings");
		apiLogInfo.setLogLevel("INFO");
		
		try {
			// Get all active trainings (both mandatory and non-mandatory) with effective dates
			List<TrainingMaster> allActiveTrainings = trainingMasterRepository
				.findActiveTrainingsWithEffectiveDates("true");
			
			List<com.apmosys.employeeportal.dto.UserTrainingDTO> userTrainings = new ArrayList<>();
			
			for (TrainingMaster training : allActiveTrainings) {
				com.apmosys.employeeportal.dto.UserTrainingDTO userTraining = new com.apmosys.employeeportal.dto.UserTrainingDTO();
				userTraining.setTrainingId(training.getTrainingId());
				userTraining.setTrainingName(training.getTrainingName());
				userTraining.setTrainingType(training.getTrainingType());
				userTraining.setLockEnabled("true".equals(training.getLockEnabled()));
				userTraining.setMandatoryFlag(training.getMandatoryFlag());
				userTraining.setMinViewTimeMinutes(training.getMinViewTimeMinutes());
				userTraining.setConsentRequired(training.getConsentRequired());
				userTraining.setSkipAllowed(training.getSkipAllowed());
				userTraining.setRequiredFrequency(training.getFrequencyPerYear());
				
				// Calculate completion count
				int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId());
				userTraining.setCompletionCount(completionCount);
				
				// Get current active content
				Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(training.getTrainingId());
				if (activeContentOpt.isEmpty()) {
					continue; // Skip trainings without active content
				}
				
				TrainingContent activeContent = activeContentOpt.get();
				TrainingContentDTO contentDTO = convertToTrainingContentDTO(activeContent);
				userTraining.setContent(contentDTO);
				
				// Calculate current cycle
				int currentCycle = calculateCurrentCycle(empId, training.getTrainingId());
				userTraining.setCurrentCycleNumber(currentCycle);
				
				// Check if consent exists for current active content in current cycle
				Optional<TrainingConsent> consentOpt = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(
					empId,
					training.getTrainingId(),
					activeContent.getContentId(),
					currentCycle
				);
				
				// Check if skip exists for current cycle
				Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
					empId,
					training.getTrainingId(),
					currentCycle
				);
				
				// Determine status
				String status;
				if (consentOpt.isPresent()) {
					status = "COMPLETED";
					// Get last completed date (most recent consent)
					List<TrainingConsent> allConsents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId());
					if (!allConsents.isEmpty()) {
						// List is already sorted DESC by consentTimestamp, so first element is most recent
						TrainingConsent mostRecentConsent = allConsents.get(0);
						if (mostRecentConsent != null && mostRecentConsent.getConsentTimestamp() != null) {
							java.sql.Date lastCompleted = Date.valueOf(
							        mostRecentConsent.getConsentTimestamp()
							                .toLocalDateTime()
							                .toLocalDate()
							);


							userTraining.setLastCompletedOn(lastCompleted);
						}
					}
				} else if (skipOpt.isPresent()) {
					status = "SKIPPED";
					TrainingSkip skip = skipOpt.get();
					if (skip != null) {
						userTraining.setSkipCount(skip.getSkipCount());
					}
				} else {
					status = "PENDING";
				}
				userTraining.setStatus(status);
				
				// Calculate deadline
				LocalDate cycleDeadline = null;
				boolean isDeadlineCrossed = false;
				if ("true".equals(training.getDeadlineEnabled()) && training.getDeadlinePattern() != null) {
					cycleDeadline = calculateCycleDeadline(training, currentCycle);
					if (cycleDeadline != null) {
						isDeadlineCrossed = LocalDate.now().isAfter(cycleDeadline);
						userTraining.setDeadline(Date.valueOf(cycleDeadline));
						userTraining.setIsDeadlineCrossed(isDeadlineCrossed);
					}
				}
				
				userTrainings.add(userTraining);
			}
			
			// Sort by priority: 
			// 1. Deadline crossed + mandatory + lock enabled first
			// 2. Then mandatory + lock enabled
			// 3. Then deadline crossed
			// 4. Then by deadline date (earliest first)
			// 5. Then mandatory flag (mandatory first)
			// 6. Then by training name
			userTrainings.sort((t1, t2) -> {
				// Priority 1: Deadline crossed + mandatory + lock enabled
				boolean t1Priority1 = t1.getIsDeadlineCrossed() != null && t1.getIsDeadlineCrossed() && 
					"true".equals(t1.getMandatoryFlag()) && t1.getLockEnabled() != null && t1.getLockEnabled();
				boolean t2Priority1 = t2.getIsDeadlineCrossed() != null && t2.getIsDeadlineCrossed() && 
					"true".equals(t2.getMandatoryFlag()) && t2.getLockEnabled() != null && t2.getLockEnabled();
				if (t1Priority1 && !t2Priority1) return -1;
				if (t2Priority1 && !t1Priority1) return 1;
				
				// Priority 2: Mandatory + lock enabled
				boolean t1Priority2 = "true".equals(t1.getMandatoryFlag()) && t1.getLockEnabled() != null && t1.getLockEnabled();
				boolean t2Priority2 = "true".equals(t2.getMandatoryFlag()) && t2.getLockEnabled() != null && t2.getLockEnabled();
				if (t1Priority2 && !t2Priority2) return -1;
				if (t2Priority2 && !t1Priority2) return 1;
				
				// Priority 3: Deadline crossed
				if (t1.getIsDeadlineCrossed() != null && t1.getIsDeadlineCrossed() && 
					(t2.getIsDeadlineCrossed() == null || !t2.getIsDeadlineCrossed())) {
					return -1;
				}
				if (t2.getIsDeadlineCrossed() != null && t2.getIsDeadlineCrossed() && 
					(t1.getIsDeadlineCrossed() == null || !t1.getIsDeadlineCrossed())) {
					return 1;
				}
				
				// Priority 4: Deadline date (earliest first)
				if (t1.getDeadline() != null && t2.getDeadline() != null) {
					return t1.getDeadline().compareTo(t2.getDeadline());
				}
				if (t1.getDeadline() != null) return -1;
				if (t2.getDeadline() != null) return 1;
				
				// Priority 5: Mandatory flag (mandatory first)
				if ("true".equals(t1.getMandatoryFlag()) && !"true".equals(t2.getMandatoryFlag())) {
					return -1;
				}
				if ("true".equals(t2.getMandatoryFlag()) && !"true".equals(t1.getMandatoryFlag())) {
					return 1;
				}
				
				// Priority 6: Training name
				return t1.getTrainingName().compareTo(t2.getTrainingName());
			});
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(userTrainings);
			apiLogInfo.setApiResponse("User trainings fetched successfully");
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
	public ServiceResponse checkTrainingFrequency(Long empId, Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
			
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			
			// Calculate completion count in last 12 months
			Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
			Long completionCount = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, fromDate);
			
			TrainingFrequencyDTO dto = new TrainingFrequencyDTO();
			dto.setTrainingId(trainingId);
			dto.setTrainingName(training.getTrainingName());
			dto.setCompletionCount(completionCount != null ? completionCount.intValue() : 0);
			dto.setRequiredFrequency(training.getFrequencyPerYear());
			dto.setNeedsAssignment(completionCount < training.getFrequencyPerYear());
			
			// Get last completed date
			List<TrainingConsent> consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
			if (!consents.isEmpty()) {
				dto.setLastCompletedOn(consents.get(0).getConsentTimestamp().toLocalDateTime().toString());
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dto);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		return response;
	}

	@Override
	public ServiceResponse getEmployeeTrainingHistory(Long empId, Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			List<TrainingHistoryDTO> historyList = new ArrayList<>();
			
			// Get all consents for employee
			List<TrainingConsent> consents;
			if (trainingId != null) {
				consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
			} else {
				// Get all trainings - need to query differently
				// For now, get by trainingId only
				consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
			}
			
			// Get all skips
			List<TrainingSkip> skips;
			if (trainingId != null) {
				Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
				if (trainingOpt.isPresent()) {
					skips = trainingSkipRepository.findByEmpIdAndTrainingId(empId, trainingId);
				} else {
					skips = new ArrayList<>();
				}
			} else {
				skips = new ArrayList<>(); // Need to implement getAllSkipsByEmpId if needed
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
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
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
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(trainingId);
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

	@Override
	public ServiceResponse checkTrainingRequirements(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			// This is for cron job - check if employee needs training
			// For Phase-1, just return success
			// Actual implementation would check all employees if empId is null
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training requirements checked");
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		return response;
	}

	// ==================== Helper Methods ====================
	
	private TrainingMaster findPendingMandatoryTraining(Long empId) {
		List<TrainingMaster> mandatoryTrainings = trainingMasterRepository
			.findActiveMandatoryTrainings("true", "true");
		
		TrainingMaster pendingTraining = null;
		LocalDate earliestDeadline = null;
		
		for (TrainingMaster training : mandatoryTrainings) {
			int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId());
			
			if (completionCount < training.getFrequencyPerYear()) {
				Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(training.getTrainingId());
				
				if (activeContentOpt.isEmpty()) {
					continue;
				}
				
				TrainingContent activeContent = activeContentOpt.get();
				int currentCycle = completionCount + 1;
				
				Optional<TrainingConsent> consentOpt = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(
					empId,
					training.getTrainingId(),
					activeContent.getContentId(),
					currentCycle
				);
				
				if (consentOpt.isPresent()) {
					continue;
				}
				
				LocalDate cycleDeadline = null;
				if ("true".equals(training.getDeadlineEnabled()) && training.getDeadlinePattern() != null) {
					cycleDeadline = calculateCycleDeadline(training, currentCycle);
					
					if (cycleDeadline != null && LocalDate.now().isAfter(cycleDeadline)) {
						return training;
					}
				}
				
				if (cycleDeadline != null) {
					if (earliestDeadline == null || cycleDeadline.isBefore(earliestDeadline)) {
						earliestDeadline = cycleDeadline;
						pendingTraining = training;
					}
				} else if (pendingTraining == null) {
					pendingTraining = training;
				}
			}
		}
		
		return pendingTraining;
	}
	
	private int countCompletionsInLast12Months(Long empId, Integer trainingId) {
		Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
		Long count = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, fromDate);
		return count != null ? count.intValue() : 0;
	}
	
	private int calculateCurrentCycle(Long empId, Integer trainingId) {
		Integer maxCycle = trainingConsentRepository.findMaxCycleNumber(empId, trainingId);
		return maxCycle != null ? maxCycle + 1 : 1;
	}
	
	private LocalDate calculateCycleDeadline(TrainingMaster training, int cycleNumber) {
		if (training.getDeadlinePattern() == null) {
			return null;
		}
		
		int currentYear = LocalDate.now().getYear();
		String pattern = training.getDeadlinePattern();
		
		switch (pattern) {
			case "MID_YEAR":
				if (cycleNumber == 1) {
					return LocalDate.of(currentYear, 6, 30);
				} else {
					return LocalDate.of(currentYear, 12, 31);
				}
				
			case "YEAR_END":
				if (cycleNumber == 1) {
					return LocalDate.of(currentYear, 6, 30);
				} else {
					return LocalDate.of(currentYear, 12, 31);
				}
				
			case "QUARTERLY":
				int[] quarterMonths = {3, 6, 9, 12};
				int quarterMonthIndex = (cycleNumber - 1) % 4;
				int quarterMonth = quarterMonths[quarterMonthIndex];
				int quarterLastDay = YearMonth.of(currentYear, quarterMonth).lengthOfMonth();
				return LocalDate.of(currentYear, quarterMonth, quarterLastDay);
				
			case "CUSTOM":
				if (training.getCustomDeadlineMonths() != null) {
					String[] months = training.getCustomDeadlineMonths().split(",");
					int customMonthIndex = (cycleNumber - 1) % months.length;
					int customMonth = Integer.parseInt(months[customMonthIndex].trim());
					int customLastDay = YearMonth.of(currentYear, customMonth).lengthOfMonth();
					return LocalDate.of(currentYear, customMonth, customLastDay);
				}
				return null;
				
			default:
				return null;
		}
	}
	
	private LockStatusDTO getLockStatusInternal(Long empId) {
		LockStatusDTO lockStatus = new LockStatusDTO();
		lockStatus.setIsLocked(false);
		
		TrainingMaster pendingTraining = findPendingMandatoryTraining(empId);
		
		if (pendingTraining == null) {
			return lockStatus;
		}
		
		if (!"true".equals(pendingTraining.getActiveStatus())) {
			return lockStatus;
		}
		
		if (!"true".equals(pendingTraining.getLockEnabled())) {
			return lockStatus;
		}
		
		Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(pendingTraining.getTrainingId());
		if (activeContentOpt.isEmpty()) {
			return lockStatus;
		}
		
		int currentCycle = calculateCurrentCycle(empId, pendingTraining.getTrainingId());
		
		Optional<TrainingConsent> consentOpt = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(
			empId,
			pendingTraining.getTrainingId(),
			activeContentOpt.get().getContentId(),
			currentCycle
		);
		
		if (consentOpt.isPresent()) {
			return lockStatus; // Not locked
		}
		
		lockStatus.setIsLocked(true);
		lockStatus.setLockedTrainingId(pendingTraining.getTrainingId());
		lockStatus.setLockedTrainingName(pendingTraining.getTrainingName());
		lockStatus.setLockReason("Mandatory training pending");
		lockStatus.setCurrentCycleNumber(currentCycle);
		
		// Check skip allowed
		lockStatus.setCanSkip("true".equals(pendingTraining.getSkipAllowed()));
		
		// Check deadline
		if ("true".equals(pendingTraining.getDeadlineEnabled()) && pendingTraining.getDeadlinePattern() != null) {
			LocalDate cycleDeadline = calculateCycleDeadline(pendingTraining, currentCycle);
			if (cycleDeadline != null) {
				lockStatus.setDeadlineCrossed(LocalDate.now().isAfter(cycleDeadline));
			}
		}
		
		return lockStatus;
	}
	
	private TrainingMasterDTO convertToTrainingMasterDTO(TrainingMaster training) {
		TrainingMasterDTO dto = new TrainingMasterDTO();
		dto.setTrainingId(training.getTrainingId());
		dto.setTrainingName(training.getTrainingName());
		dto.setTrainingType(training.getTrainingType());
		dto.setMandatoryFlag(training.getMandatoryFlag());
		dto.setEffectiveFrom(training.getEffectiveFrom());
		dto.setEffectiveTo(training.getEffectiveTo());
		dto.setFrequencyPerYear(training.getFrequencyPerYear());
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
	
	// Helper method to save training file
	private String saveTrainingFile(Integer trainingId, MultipartFile file) throws IOException {

	    // Base directory: {trainingFileLocation}/{trainingId}/
	    Path trainingDir = Paths.get(trainingFileLocation)
	                            .resolve(trainingId.toString())
	                            .toAbsolutePath()
	                            .normalize();

	    if (!Files.exists(trainingDir)) {
	        Files.createDirectories(trainingDir);
	    }

	    // Safer unique filename
	    String originalFilename = file.getOriginalFilename();
	    String safeOriginalName = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "file";
	    String newFileName = "content_" + UUID.randomUUID() + "_" + safeOriginalName;

	    Path targetFile = trainingDir.resolve(newFileName).normalize();

	    // Stream-based copy (safe for large files)
	    try (InputStream in = file.getInputStream()) {
	        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
	    }

	    // Store clean relative path in DB
	    // Example: "12/content_uuid_policy.pdf"
	    return trainingId + "/" + newFileName;
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
