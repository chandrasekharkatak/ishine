package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.BusinessValidationException;
import com.apmosys.employeeportal.Exception.FileValidationException;
import com.apmosys.employeeportal.Exception.GlobalException;
import com.apmosys.employeeportal.Exception.ResourceNotFoundException;
import com.apmosys.employeeportal.Exception.TrainingException;
import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.LockStatusDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingHistoryDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingResponseDTO;
import com.apmosys.employeeportal.exception.BadRequestException;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeQuizResponseStatusMapping;
import com.apmosys.employeeportal.model.TrainingConsent;
import com.apmosys.employeeportal.model.TrainingContent;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.model.TrainingSkip;
import com.apmosys.employeeportal.model.TrainingTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeQuizResponseStatusMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TrainingConsentRepository;
import com.apmosys.employeeportal.repository.TrainingContentRepository;
import com.apmosys.employeeportal.repository.TrainingMasterRepository;
import com.apmosys.employeeportal.repository.TrainingSkipRepository;
import com.apmosys.employeeportal.repository.TrainingTypeMasterRepository;
import com.apmosys.employeeportal.serviceInterface.TrainingConfigService;
import com.apmosys.employeeportal.serviceInterface.TrainingUserService;
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
	
	@Autowired
	private  TrainingFileValidator trainingFileValidator;

	@Autowired
	private EmployeeQuizResponseStatusMappingRepository employeeQuizResponseStatusMappingRepository;

	@Autowired
	private TrainingUserService trainingUserService;

	@Autowired
	private TrainingTypeMasterRepository trainingTypeMasterRepository;
	@Autowired
	MailService mailService;
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;

	@Value("${file.training.max.size.allowed}")
	private String maxFileSize;

	@Value("${training.job.role.exclude}")
	private String trainingJobRoleExclude;

	@Autowired
	private TrainingUserServiceImpl trainingUserServiceImpl;

	private Map<Integer, String> trainingContentMap = new HashMap<>();

	public String getTrainingContentMap(Integer contentId) {
		return trainingContentMap.get(contentId);
	}

	public void setTrainingContentMap(Integer contentId, String slidesPath) {
		this.trainingContentMap.put(contentId, slidesPath);
	}


	// ==================== HR Configuration APIs ====================
	@Override
	public ServiceResponse getAllTrainings(String activeStatus, String mandatoryFlag) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get All Trainings");
		apiLogInfo.setApiUrl("/api/training/getAllTrainings");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Active Status: ").append(activeStatus).append(", Mandatory Flag: ").append(mandatoryFlag);

		try {
			List<TrainingMasterDTO> trainings;

			if (activeStatus != null && mandatoryFlag != null) {
				trainings = trainingMasterRepository.findByMandatoryFlagAndActiveStatus(mandatoryFlag, activeStatus);
			} else  {
				trainings = trainingMasterRepository.findByActiveStatus(activeStatus == null ? null : activeStatus);
			} 

			// List<TrainingMasterDTO> dtoList = trainings.stream().map(training -> {
			// 	TrainingMasterDTO dto = convertToTrainingMasterDTO(training);
			// 	return dto;
			// }).collect(Collectors.toList());

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(trainings);
			apiLogInfo.setApiResponse("Trainings fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
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
		logBuilder.append("Training ID: ").append(contentDTO.getTrainingId()).append(", Content Type: ")
				.append(contentDTO.getContentType()).append(", CreatedBy: ").append(createdBy);

		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository
					.findByTrainingId(contentDTO.getTrainingId());
			if (trainingOpt.isEmpty()) {
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training not found");
			}

			TrainingMaster training = trainingOpt.get();

			// Validate content type and required fields
			if ("LINK".equals(contentDTO.getContentType())) {
				if (contentDTO.getExternalLinkUrl() == null || contentDTO.getExternalLinkUrl().isEmpty()) {
					apiLogInfo.setApiResponse("External link URL is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					throw new TrainingException("External link URL is required for LINK content type");
				}
			} else {
				if (contentDTO.getContentPath() == null || contentDTO.getContentPath().isEmpty()) {
					apiLogInfo.setApiResponse("Content path is required");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					throw new TrainingException("Content path is required for file content types");
				}
			}

			// Check for overlapping active content
			List<TrainingContent> overlappingContent = trainingContentRepository.findOverlappingActiveContent(
					contentDTO.getTrainingId(), contentDTO.getEffectiveFrom(), contentDTO.getEffectiveTo());

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
				apiLogInfo.setApiResponse("Failed to add content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("Failed to save training content");
			}

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private void validateTrainingDTO(TrainingMasterDTO dto) {
		if (dto == null)
			throw new RuntimeException("Training DTO is required");

		if (isBlank(dto.getTrainingName()))
			throw new RuntimeException("Training name is required");

		if (dto.getEffectiveFrom() == null || dto.getEffectiveTo() == null)
			throw new RuntimeException("Training effective dates are required");
	}

	private void validateContentDTO(TrainingContentDTO dto) {
		if (dto == null)
			throw new RuntimeException("Content DTO is required");

		if (isBlank(dto.getContentType()))
			throw new RuntimeException("Content type is required");

		if (isBlank(dto.getContentName()))
			throw new RuntimeException("Content name is required");
	}

	private void validateDateLogic(TrainingMasterDTO training, TrainingContentDTO content) {

		if (training.getEffectiveFrom().isAfter(training.getEffectiveTo()))
			throw new RuntimeException("Invalid training date range");

		if (content.getEffectiveFrom().isAfter(content.getEffectiveTo()))
			throw new RuntimeException("Invalid content date range");

		if (content.getEffectiveFrom().isBefore(training.getEffectiveFrom())
				|| content.getEffectiveTo().isAfter(training.getEffectiveTo()))
			throw new RuntimeException("Content dates must be within training dates");
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO,
			MultipartFile file) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Create Training With Content");
		apiLogInfo.setApiUrl("/api/training/createTrainingWithContent");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training Name: ").append(trainingDTO != null ? trainingDTO.getTrainingName() : "null")
				.append(", Content Type: ").append(contentDTO != null ? contentDTO.getContentType() : "null");

		try {
			validateTrainingDTO(trainingDTO);
			validateContentDTO(contentDTO);

			/*
			 * ============================= 2️⃣ DUPLICATE CHECK
			 * ==============================
			 */

			Optional<TrainingMaster> existing = trainingMasterRepository
					.findByTrainingNameIgnoreCase(trainingDTO.getTrainingName().trim());

			if (existing.isPresent()) {
				apiLogInfo.setApiResponse("Training name already exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("Training name already exists.");
			}

			/*
			 * ============================= 3️⃣ DATE VALIDATIONS
			 * ==============================
			 */

			validateDateLogic(trainingDTO, contentDTO);

			/*
			 * ============================= 4️⃣ FILE VALIDATION
			 * ==============================
			 */

			if (!"LINK".equals(contentDTO.getContentType())) {

				if (file == null || file.isEmpty()) {
					throw new TrainingException("File is required for " + contentDTO.getContentType());
				}

				trainingFileValidator.validateFile(file, contentDTO.getContentType(), maxFileSize);
			}

			/*
			 * ============================= 5️⃣ SAVE TRAINING
			 * ==============================
			 */

			TrainingMaster training = new TrainingMaster();

	        training.setTrainingName(trainingDTO.getTrainingName().trim());
	        training.setTrainingType(trainingDTO.getTrainingType());
	        training.setMandatoryFlag(
	                trainingDTO.getMandatoryFlag() != null ?
	                        trainingDTO.getMandatoryFlag() : "false");

	        training.setEffectiveFrom(trainingDTO.getEffectiveFrom());
	        training.setEffectiveTo(trainingDTO.getEffectiveTo());

	        training.setLockEnabled(
	                trainingDTO.getLockEnabled() != null ?
	                        trainingDTO.getLockEnabled() : "false");

	        training.setMinViewTimeMinutes(
	                trainingDTO.getMinViewTimeMinutes());

	        training.setConsentRequired(
	                trainingDTO.getConsentRequired() != null ?
	                        trainingDTO.getConsentRequired() : "true");

	        if ("true".equals(training.getLockEnabled())) {
	            training.setSkipAllowed("false");
	            training.setMandatoryFlag("true");
	        } else {
	            training.setSkipAllowed(
	                    trainingDTO.getSkipAllowed() != null ?
	                            trainingDTO.getSkipAllowed() : "true");
	        }

	        training.setDeadlineEnabled("true");

	        training.setDeadlinePattern(
	                trainingDTO.getDeadlinePattern() != null &&
	                        !trainingDTO.getDeadlinePattern().isEmpty()
	                        ? trainingDTO.getDeadlinePattern()
	                        : "YEARLY");

	        training.setCustomDeadlineMonths(
	                trainingDTO.getCustomDeadlineMonths());

	        training.setActiveStatus(
	                trainingDTO.getActiveStatus() != null ?
	                        trainingDTO.getActiveStatus() : "true");

	        training.setCreatedBy(trainingDTO.getCreatedBy());

	        TrainingMaster savedTraining =
	                trainingMasterRepository.save(training);


			/*
			 * ============================= 6️⃣ HANDLE FILE ==============================
			 */

			String filePath = null;

			if (!"LINK".equals(contentDTO.getContentType())) {
				
				 	filePath = generateTrainingFilePath(savedTraining.getTrainingId(), file);

				contentDTO.setContentPath(filePath);
				contentDTO.setFileSizeBytes(file.getSize());
				contentDTO.setMimeType(file.getContentType());
			}

			/*
			 * ============================= 7️⃣ SAVE CONTENT ==============================
			 */


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
	        content.setActiveStatus(
	                contentDTO.getActiveStatus() != null ?
	                        contentDTO.getActiveStatus() : "true");
	        content.setCreatedBy(trainingDTO.getCreatedBy());

	        TrainingContent savedContent =
	                trainingContentRepository.save(content);

			/*
			 * ============================= 8️⃣ SAVE FILE AFTER DB SUCCESS
			 * ==============================
			 */

			if (filePath != null) {
				saveTrainingFile(filePath, file);

			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(savedTraining);
			response.setServiceMessage("Training and content created successfully");
			apiLogInfo.setApiResponse("Training and content created successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTrainingWithContent(
	        TrainingMasterDTO trainingDTO,
	        TrainingContentDTO contentDTO,
	        MultipartFile file) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Update Training With Content");
	    apiLogInfo.setApiUrl("/api/training/updateTrainingWithContent");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Training ID: ").append(trainingDTO != null ? trainingDTO.getTrainingId() : "null")
	    		.append(", Content ID: ").append(contentDTO != null ? contentDTO.getContentId() : "null");

	    try {
	        if (trainingDTO == null || trainingDTO.getTrainingId() == null){
				apiLogInfo.setApiResponse("Training ID is required for update");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
	            throw new TrainingException("Training ID is required for update");
			}

	        if (trainingDTO.getUpdatedBy() == null){
				apiLogInfo.setApiResponse("Updated by is required");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
	            throw new TrainingException("Updated by is required");
			}

	        if (contentDTO == null){
				apiLogInfo.setApiResponse("Content DTO is required");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
	            throw new TrainingException("Content DTO is required");
			}

	        TrainingMaster training = trainingMasterRepository
	                .findByTrainingId(trainingDTO.getTrainingId())
	                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));

	        if (trainingDTO.getTrainingName() != null
	                && !trainingDTO.getTrainingName().trim().isEmpty()) {

	            Optional<TrainingMaster> existing =
	                    trainingMasterRepository.findByTrainingNameAndNotTrainingId(
	                            trainingDTO.getTrainingName().trim(),
	                            trainingDTO.getTrainingId());

	            if (existing.isPresent())
	                throw new TrainingException("Training name already exists.");
	        }

	        /* ===========================
	           4️⃣ DATE VALIDATION
	        =========================== */

	        LocalDate effectiveFrom = trainingDTO.getEffectiveFrom() != null
	                ? trainingDTO.getEffectiveFrom()
	                : training.getEffectiveFrom();

	        LocalDate effectiveTo = trainingDTO.getEffectiveTo() != null
	                ? trainingDTO.getEffectiveTo()
	                : training.getEffectiveTo();

	        if (effectiveFrom.isAfter(effectiveTo))
	            throw new TrainingException("Training effective from must be before effective to");

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

	        if ("true".equals(training.getLockEnabled())) {
	            training.setSkipAllowed("false");
	            training.setMandatoryFlag("true");
	        } else if (trainingDTO.getSkipAllowed() != null) {
	            training.setSkipAllowed(trainingDTO.getSkipAllowed());
	        }

	        if (trainingDTO.getDeadlinePattern() != null)
	            training.setDeadlinePattern(trainingDTO.getDeadlinePattern());

	        if (trainingDTO.getCustomDeadlineMonths() != null)
	            training.setCustomDeadlineMonths(trainingDTO.getCustomDeadlineMonths());

	        if (trainingDTO.getActiveStatus() != null)
	            training.setActiveStatus(trainingDTO.getActiveStatus());

			training.setMinViewTimeMinutes(trainingDTO.getMinViewTimeMinutes());
			training.setConsentRequired(trainingDTO.getConsentRequired());

	        training.setUpdatedBy(trainingDTO.getUpdatedBy());
	        training.setUpdatedOn(LocalDateTime.now());
	        TrainingMaster updatedTraining =
	                trainingMasterRepository.save(training);

	        /* ===========================
	           6️⃣ FETCH EXISTING CONTENT
	        =========================== */

	        TrainingContent existingContent = trainingContentRepository
	                .findByContentId(contentDTO.getContentId())
	                .orElseThrow(() -> new ResourceNotFoundException("Content not found for update"));

			TrainingContent newTrainingContent = new TrainingContent();

		     String newFilePath=null;

	        /* ===========================
	           7️⃣ FILE VALIDATION
	        =========================== */

	        if (!"LINK".equalsIgnoreCase(contentDTO.getContentType())) {

	            if (file != null && !file.isEmpty()) {

	                trainingFileValidator.validateFile(
	                        file,
	                        contentDTO.getContentType(),
	                        maxFileSize
	                );

	                newFilePath = generateTrainingFilePath(
	                        trainingDTO.getTrainingId(),
	                        file
	                );

	              
	            }
	        }  
	        /* ===========================
	        8️⃣ HANDLE FILE SWITCH CASES
	     =========================== */
		     String oldFilePath=existingContent.getContentPath();
			 TrainingContent savedContent = null;


		  // CASE 1: Incoming LINK
		     if ("LINK".equalsIgnoreCase(contentDTO.getContentType())) {

		         newTrainingContent.setContentPath(null);
		         newTrainingContent.setFileSizeBytes(0L);
		         newTrainingContent.setMimeType(null);
		     }

		     // CASE 2: Incoming FILE with new upload
		     else if (file != null && !file.isEmpty()) {

		         newFilePath = generateTrainingFilePath(
		                 updatedTraining.getTrainingId(), file);

		         newTrainingContent.setContentPath(newFilePath);
		         newTrainingContent.setFileSizeBytes(file.getSize());
		         newTrainingContent.setMimeType(file.getContentType());
				 newTrainingContent.setContentType(contentDTO.getContentType());
				 newTrainingContent.setContentName(contentDTO.getContentName());
				 newTrainingContent.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
				 newTrainingContent.setEffectiveFrom(contentDTO.getEffectiveFrom());
				 newTrainingContent.setEffectiveTo(contentDTO.getEffectiveTo());
				 newTrainingContent.setActiveStatus("true");
				 newTrainingContent.setCreatedBy(trainingDTO.getUpdatedBy());
				 newTrainingContent.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				 newTrainingContent.setTrainingMaster(updatedTraining);
				savedContent = trainingContentRepository.save(newTrainingContent);
				existingContent.setActiveStatus("false");
				existingContent.setUpdatedBy(trainingDTO.getUpdatedBy());
				existingContent.setUpdatedOn(new Timestamp(System.currentTimeMillis()));			
		     } else {
				existingContent.setEffectiveFrom(contentDTO.getEffectiveFrom());
				existingContent.setEffectiveTo(contentDTO.getEffectiveTo());
				existingContent.setContentName(contentDTO.getContentName());
				existingContent.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
				
				existingContent.setUpdatedBy(trainingDTO.getUpdatedBy());
				existingContent.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
				savedContent = trainingContentRepository.save(existingContent);
			}

	  
	        /* ===========================
	        🔟 SAVE FILE AFTER DB SAVE
	         =========================== */
	  // Save new file
	     if (newFilePath != null) {
	         saveTrainingFile(newFilePath, file);
	     }

	     // Delete old file if switching FILE → LINK
	     if ("LINK".equalsIgnoreCase(contentDTO.getContentType())
	             && oldFilePath != null
	             && !oldFilePath.isBlank()) {

	         safeDeleteTrainingFile(oldFilePath);

			 String oldSlidesPath = existingContent.getSlidesPath();
			if (oldSlidesPath != null) {
				safeDeleteDirectory(oldSlidesPath);
			}
	     }

	     // Delete old file if replacing FILE → FILE
	     if (newFilePath != null
	             && oldFilePath != null
	             && !oldFilePath.isBlank()
	             && !oldFilePath.equals(newFilePath)) {

	         safeDeleteTrainingFile(oldFilePath);
			 String oldSlidesPath = existingContent.getSlidesPath();
			if (oldSlidesPath != null) {
				safeDeleteDirectory(oldSlidesPath);
			}
	     }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Training and content updated successfully");
	        response.setServiceMessage(savedContent.getContentId().toString());
	        apiLogInfo.setApiResponse("Training and content updated successfully");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	private void safeDeleteTrainingFile(String relativePath) {
	    try {
	        Path filePath = Paths.get(trainingFileLocation)
	                .resolve(relativePath)
	                .toAbsolutePath()
	                .normalize();

	        Files.deleteIfExists(filePath);

	    } catch (Exception e) {
	        // Log only — do NOT break transaction
	        System.err.println("Failed to delete old file: " + e.getMessage());
	    }
	}

	@Override
	@Transactional
	public ServiceResponse updateTrainingContent(TrainingContentDTO contentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Training Content");
		apiLogInfo.setApiUrl("/api/training/updateTrainingContent");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Content ID: ").append(contentDTO != null ? contentDTO.getContentId() : "null");

		try {
			Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(contentDTO.getContentId());

			if (contentOpt.isEmpty()) {
				apiLogInfo.setApiResponse("Content not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Content not found");
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
						content.getTrainingMaster().getTrainingId(), content.getEffectiveFrom(),
						content.getEffectiveTo());

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
			apiLogInfo.setApiResponse("Content updated successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (FileValidationException e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("WARN");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}


	private int countSlides(String slidesPath) {
		try {
			File slidesDir = new File(slidesPath);
			if (!slidesDir.exists() || !slidesDir.isDirectory()) {
				return 0;
			}
			
			// Count PNG files that start with "slide-" or "page-"
			File[] slides = slidesDir.listFiles((dir, name) -> 
				(name.startsWith("slide-") || name.startsWith("page-")) && 
				name.endsWith(".png"));
			
			return slides != null ? slides.length : 0;
		} catch (Exception e) {
			throw new RuntimeException("Error counting slides", e);
		}
	}

	private void safeDeleteDirectory(String path) {
		try {
			Path dirPath = Paths.get(path);
			if (Files.exists(dirPath)) {
				// Delete all files in directory first
				Files.walk(dirPath)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error deleting directory", e);
		}
	}

	@Override
	public ServiceResponse getTrainingContent(Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Training Content");
		apiLogInfo.setApiUrl("/api/training/getTrainingContent");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(trainingId);

		try {
			LocalDate today = LocalDate.now();
			TrainingContent content = trainingContentRepository.findByTrainingMaster_TrainingId(trainingId, today);

			List<TrainingContentDTO> dtoList = new ArrayList<>();

			if (content != null) {
				TrainingContentDTO dto = convertToTrainingContentDTO(content);

				// Check if currently active
				boolean isActive = "true".equals(content.getActiveStatus())
						&& content.getEffectiveFrom().isBefore(today.plusDays(1))
						&& (content.getEffectiveTo() == null || content.getEffectiveTo().isAfter(today.minusDays(1)));
				dto.setIsCurrentlyActive(isActive);

				dtoList.add(dto);
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			apiLogInfo.setApiResponse("Training content fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
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
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest("Training ID: " + trainingId + ", Updated By: " + updatedBy);
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training not found");
			}

			TrainingMaster training = trainingOpt.get();
			training.setActiveStatus("false");
			training.setUpdatedBy(updatedBy);
			training.setUpdatedOn(LocalDateTime.now());

			trainingMasterRepository.save(training);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training deactivated successfully");
			apiLogInfo.setApiResponse("Training deactivated successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest("Training ID: " + trainingId + ", Updated By: " + updatedBy);
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest("Training ID: " + trainingId + ", Updated By: " + updatedBy);
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

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Content ID: ").append(contentId);

		try {
			Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(contentId);

			if (contentOpt.isEmpty()) {
				apiLogInfo.setApiResponse("Content not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Content not found");
			}

			TrainingContent content = contentOpt.get();

			// Block external links
			if ("LINK".equalsIgnoreCase(content.getContentType())) {
				apiLogInfo.setApiResponse("External link content cannot be downloaded");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("External link content cannot be downloaded");
			}

			if (content.getContentPath() == null || content.getContentPath().isBlank()) {
				apiLogInfo.setApiResponse("Content file path not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Content file path not found");
			}

			// Resolve paths safely
			Path basePath = Paths.get(trainingFileLocation).toAbsolutePath().normalize();

			Path filePath = basePath.resolve(content.getContentPath()).normalize();

			// Path traversal protection
			if (!filePath.startsWith(basePath)) {
				apiLogInfo.setApiResponse("Invalid file path");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Invalid file path");
			}

			if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
				apiLogInfo.setApiResponse("File not found on server");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("File not found on server");
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
				try {
				mimeType = Files.probeContentType(filePath);
				} catch (IOException ex) {
					mimeType = null;
			}
			}
			if (mimeType == null) mimeType = "application/octet-stream";

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(resource);
			response.setServiceResponse1(downloadFileName);
			response.setServiceResponse2(mimeType);

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Content downloaded successfully");

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	// @Override
	// public ServiceResponse getEmployeeTrainingHistory(Long empId, Integer trainingId) {
	// 	ServiceResponse response = new ServiceResponse();
	// 	LogDTO apiLogInfo = new LogDTO();
	// 	apiLogInfo.setSubFeatureName("Get Employee Training History");
	// 	apiLogInfo.setApiUrl("/api/training/getEmployeeTrainingHistory");
	// 	apiLogInfo.setLogLevel("INFO");

	// 	StringBuilder logBuilder = new StringBuilder();
	// 	logBuilder.append("Emp ID: ").append(empId).append(", Training ID: ").append(trainingId);

	// 	try {
	// 		List<TrainingHistoryDTO> historyList = new ArrayList<>();

	// 		// Get all consents for employee
	// 		List<TrainingConsent> consents;
	// 		if (trainingId != null) {
	// 			consents = trainingConsentRepository.findByEmpIdAndTrainingId(empId, trainingId);
	// 		} else {
	// 			// Get all consents for employee (all trainings) - need to get all trainings
	// 			// first
	// 			// For now, get all active trainings and then get consents for each
	// 			List<TrainingMaster> allTrainings = trainingMasterRepository.findAll();
	// 			consents = new ArrayList<>();
	// 			for (TrainingMaster training : allTrainings) {
	// 				consents.addAll(
	// 						trainingConsentRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId()));
	// 			}
	// 		}

	// 		// Get all skips for employee
	// 		List<TrainingSkip> skips;
	// 		if (trainingId != null) {
	// 			skips = trainingSkipRepository.findByEmpIdAndTrainingId(empId, trainingId);
	// 		} else {
	// 			// Get all skips for employee (all trainings) - need to get all trainings first
	// 			List<TrainingMaster> allTrainings = trainingMasterRepository.findAll();
	// 			skips = new ArrayList<>();
	// 			for (TrainingMaster training : allTrainings) {
	// 				skips.addAll(trainingSkipRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId()));
	// 			}
	// 		}

	// 		// Combine consents into history
	// 		for (TrainingConsent consent : consents) {
	// 			TrainingHistoryDTO dto = new TrainingHistoryDTO();
	// 			dto.setTrainingId(consent.getTrainingMaster().getTrainingId());
	// 			dto.setTrainingName(consent.getTrainingMaster().getTrainingName());
	// 			dto.setTrainingType(consent.getTrainingMaster().getTrainingType());
	// 			dto.setCompletedOn(formatTimestampToString(consent.getConsentTimestamp()));
	// 			dto.setStatus("COMPLETED");
	// 			dto.setCycleNumber(consent.getCompletionCycleNumber());
	// 			dto.setContentId(consent.getTrainingContent().getContentId());
	// 			dto.setContentName(consent.getTrainingContent().getContentName());
	// 			dto.setContentType(consent.getTrainingContent().getContentType());

	// 			// Count completions
	// 			Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
	// 			Long completionCount = trainingConsentRepository.countCompletionsInLast12Months(empId,
	// 					consent.getTrainingMaster().getTrainingId(), fromDate);
	// 			dto.setCompletionCount(completionCount != null ? completionCount.intValue() : 0);

	// 			historyList.add(dto);
	// 		}

	// 		// Add skips
	// 		for (TrainingSkip skip : skips) {
	// 			TrainingHistoryDTO dto = new TrainingHistoryDTO();
	// 			dto.setTrainingId(skip.getTrainingMaster().getTrainingId());
	// 			dto.setTrainingName(skip.getTrainingMaster().getTrainingName());
	// 			dto.setTrainingType(skip.getTrainingMaster().getTrainingType());
	// 			dto.setStatus("SKIPPED");
	// 			dto.setCycleNumber(skip.getCycleNumber());
	// 			dto.setSkipCount(skip.getSkipCount());
	// 			historyList.add(dto);
	// 		}

	// 		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	// 		response.setServiceResponse(historyList);
	// 		apiLogInfo.setApiResponse("Training history fetched successfully");
	// 		apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	// 		apiLogInfo.setLogLevel("ERROR");
	// 		apiLogInfo.setApiRequest(logBuilder.toString());
	// 		logService.logMyInfo(httpRequest, apiLogInfo);
	// 		throw e;
	// 	}

	// 	apiLogInfo.setApiRequest(logBuilder.toString());
	// 	logService.logMyInfo(httpRequest, apiLogInfo);
	// 	return response;
	// }

	@Override
	public ServiceResponse getComplianceReport(Integer trainingId, Long departmentId, String status) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Compliance Report");
		apiLogInfo.setApiUrl("/api/training/getComplianceReport");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(trainingId).append(", Dept ID: ").append(departmentId).append(", Status: ").append(status);

		try {
			ComplianceReportDTO dto = new ComplianceReportDTO();
			dto.setTrainingId(trainingId);

			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
			if (trainingOpt.isEmpty()) {
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training not found");
			}

			TrainingMaster training = trainingOpt.get();
			dto.setTrainingName(training.getTrainingName());

			// Get current active content for this training
			Optional<TrainingContent> activeContentOpt = trainingContentRepository
					.findCurrentActiveContent(trainingId, PageRequest.of(0, 1)).stream().findFirst();
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
				apiLogInfo.setApiResponse("Compliance report generated (no active content)");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}

			TrainingContent activeContent = activeContentOpt.get();
			Integer activeContentId = activeContent.getContentId();

			// Get total assigned: All active employees (Phase-1: mandatory trainings
			// assigned to all)
			Long totalAssigned = employeeRepository.getTotalEmployeeCount();
			if (totalAssigned == null) {
				totalAssigned = 0L;
			}
			dto.setTotalAssigned(totalAssigned);

			// Get all active employee IDs
			List<Long> activeEmployeeIds = employeeRepository.findAllActiveEmployees();

			// Calculate current cycle for compliance (use cycle 1 for Phase-1 basic
			// calculation)
			// For more accurate reporting, we'd need to check each employee's cycle, but
			// for Phase-1,
			// we'll use a simplified approach: check if they have consent for the current
			// active content
			// in any cycle (since consent is per content)

			long completedCount = 0L;
			long skippedCount = 0L;

			// For each active employee, check if they have consent for the current active
			// content
			for (Long empId : activeEmployeeIds) {
				// Check if employee has consent for this training's current active content (any
				// cycle)
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
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private String generateTrainingFilePath(Integer trainingId, MultipartFile file) {

		String originalFilename = file.getOriginalFilename();
		String safeOriginalName = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "file";

		String newFileName = "content_" + UUID.randomUUID() + "_" + safeOriginalName;

		return trainingId + "/" + newFileName;
	}

	private String saveTrainingFile(String relativePath, MultipartFile file) {

	    try {

	        Path targetFile = Paths.get(trainingFileLocation)
	                .resolve(relativePath)
	                .toAbsolutePath()
	                .normalize();

	        // Security check (VERY IMPORTANT)
	        Path basePath = Paths.get(trainingFileLocation)
	                .toAbsolutePath()
	                .normalize();

	        if (!targetFile.startsWith(basePath)) {
	            throw new RuntimeException("Invalid file path detected.");
	        }

	        // Create directory if not exists
	        if (!Files.exists(targetFile.getParent())) {
	            Files.createDirectories(targetFile.getParent());
	        }

	        // Save file
	        try (InputStream in = file.getInputStream()) {
	            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
	        }

	        return relativePath;

	    } catch (IOException e) {

	        // Wrap low-level exception
	        throw new RuntimeException(
	                "Failed to store training file. Please try again.", e);
	    }
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
	 * Helper method to format Timestamp to String Format: "dd-MM-yyyy HH:mm:ss"
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

	@Override
	public ServiceResponse changeQuizResponse(Long empId, Long quizId, String responseStatus, Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Change Quiz Response");
		apiLogInfo.setApiUrl("/api/training/changeQuizResponse");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employee ID: ").append(empId).append(", Quiz ID: ")
			.append(quizId).append(", Response Status: ").append(responseStatus);
		try {

			EmployeeQuizResponseStatusMapping employeeQuizResponseStatusMapping = employeeQuizResponseStatusMappingRepository.findByEmployeeIdAndQuizId(empId, quizId);

			if (employeeQuizResponseStatusMapping == null) {
				apiLogInfo.setApiResponse("Quiz Response Not Found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Quiz Response Not Found for empId: " + empId + ", quizId: " + quizId);
			}

			employeeQuizResponseStatusMapping.setPassStatus(responseStatus);
			employeeQuizResponseStatusMapping.setUpdatedBy(updatedBy);
			employeeQuizResponseStatusMappingRepository.save(employeeQuizResponseStatusMapping);

			response.setStatusCode(HttpStatus.OK.value());
			response.setServiceMessage("Quiz Response Changed Successfully");
			apiLogInfo.setApiResponse(responseStatus);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}	

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getTrainingResponses(Integer trainingId){
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Get Training Responses");
		apiLogInfo.setApiUrl("/api/training/getTrainingResponses");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(trainingId);
		try {
			TrainingMaster existingTraining = trainingMasterRepository.findById(trainingId).orElse(null);
			if (existingTraining == null) {
				apiLogInfo.setApiResponse("Training Not Found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training Not Found for trainingId: " + trainingId);
			}

			List<TrainingResponseDTO> trainingResponses = trainingConsentRepository.findByTrainingId(trainingId);
			response.setStatusCode(HttpStatus.OK.value());
			response.setServiceResponse(trainingResponses);
			apiLogInfo.setApiResponse(trainingResponses.toString());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
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

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employee ID: ").append(empId);

		Employee employee = employeeRepository.findById(empId).orElse(null);
		if (employee == null) {
			apiLogInfo.setApiResponse("Employee Not Found");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw new ResourceNotFoundException("Employee Not Found for empId: " + empId);
		}

			List<Long> jobRoleIds = Arrays.stream(trainingJobRoleExclude.split(","))
						.map(String::trim)
						.map(Long::parseLong)
						.collect(Collectors.toList());

			LockStatusDTO lockStatus = new LockStatusDTO();
					
			if (!jobRoleIds.contains(employee.getJobRoleId())) {
				try {

					ServiceResponse lockResponse = trainingUserService.getLockStatus(empId);
					if (lockResponse != null && lockResponse.getServiceStatus() != null && 
						lockResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) &&
						lockResponse.getServiceResponse() != null) {
						
						lockStatus = (LockStatusDTO) lockResponse.getServiceResponse();
						
						
						if (lockStatus.getIsLocked() == null) {
							lockStatus.setIsLocked(false);
						}
						if (lockStatus.getHasMandatoryTrainingPending() == null) {
							lockStatus.setHasMandatoryTrainingPending(false);
						}
						if (lockStatus.getIsHardLock() == null) {
							lockStatus.setIsHardLock(false);
						}
						if (lockStatus.getDeadlineCrossed() == null) {
							lockStatus.setDeadlineCrossed(false);
						}
						
						// Set training lock status for routing and navigation decisions
						// This includes:
						// - hasMandatoryTrainingPending: true if mandatory training exists (for routing)
						// - isLocked: true if lock enabled (for blocking navigation)
						// - isHardLock: true if lock enabled AND deadline crossed (hardest lock)
						// - deadlineCrossed: true if deadline has passed
						
						// Log lock status for debugging and monitoring
						if (lockStatus.getIsHardLock() != null && lockStatus.getIsHardLock()) {
							System.out.println("Training Lock Status - HARD LOCK: Employee " + empId + 
								" has deadline-crossed mandatory training with lock enabled. Training: " + 
								lockStatus.getLockedTrainingName());
						} else if (lockStatus.getIsLocked() != null && lockStatus.getIsLocked()) {
							System.out.println("Training Lock Status - LOCKED: Employee " + empId + 
								" has mandatory training with lock enabled. Training: " + 
								lockStatus.getLockedTrainingName() + 
								", Deadline Crossed: " + lockStatus.getDeadlineCrossed());
						} else if (lockStatus.getHasMandatoryTrainingPending() != null && lockStatus.getHasMandatoryTrainingPending()) {
							System.out.println("Training Lock Status - MANDATORY PENDING: Employee " + empId + 
								" has mandatory training pending (no lock). Training: " + 
								lockStatus.getLockedTrainingName());
						}
						apiLogInfo.setApiResponse(lockStatus.toString());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						logService.logMyInfo(httpRequest, apiLogInfo);
						response.setServiceResponse(lockStatus);
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setStatusCode(HttpStatus.OK.value());
						return response;
					} else {
						// If lock check returns failure or null, initialize empty lock status
						LockStatusDTO emptyLockStatus = new LockStatusDTO();
						emptyLockStatus.setIsLocked(false);
						emptyLockStatus.setHasMandatoryTrainingPending(false);
						emptyLockStatus.setIsHardLock(false);
						emptyLockStatus.setDeadlineCrossed(false);
						lockStatus = emptyLockStatus;
						// System.out.println("Training Lock Status - No lock status returned for employee " + empId);
						apiLogInfo.setApiResponse("Training Lock Status - No lock status returned for employee " + empId);
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						apiLogInfo.setApiRequest(logBuilder.toString());
						logService.logMyInfo(httpRequest, apiLogInfo);
						response.setServiceResponse(lockStatus);
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setStatusCode(HttpStatus.OK.value());
						return response;
					}
				} catch (Exception e) {
					// If lock check fails, initialize empty lock status to prevent NPE
					// Log error but don't fail login - training lock check should not block login
					e.printStackTrace();
					LockStatusDTO emptyLockStatus = new LockStatusDTO();
					emptyLockStatus.setIsLocked(false);
					emptyLockStatus.setHasMandatoryTrainingPending(false);
					emptyLockStatus.setIsHardLock(false);
					emptyLockStatus.setDeadlineCrossed(false);
					lockStatus = emptyLockStatus;
					
					// System.err.println("Error checking training lock on login for employee " + empId + ": " + e.getMessage());\
					apiLogInfo.setApiResponse("Error checking training lock on login for employee " + empId + ": " + e.getMessage());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					response.setServiceResponse(lockStatus);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setStatusCode(HttpStatus.OK.value());
					return response;
				}
			} else {
				// If empId is null, initialize empty lock status
				LockStatusDTO emptyLockStatus = new LockStatusDTO();
				emptyLockStatus.setIsLocked(false);
				emptyLockStatus.setHasMandatoryTrainingPending(false);
				emptyLockStatus.setIsHardLock(false);
				emptyLockStatus.setDeadlineCrossed(false);
				lockStatus = emptyLockStatus;
				// System.err.println("Warning: Employee ID is null, cannot check training lock status");
				apiLogInfo.setApiResponse("Warning: Employee ID is null, cannot check training lock status");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				response.setServiceResponse(lockStatus);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setStatusCode(HttpStatus.OK.value());
			}
			return response;
	}

	@Override
	public ServiceResponse addTrainingType(TrainingMasterDTO trainingDTO)
	{
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/training/addTrainingType");
		apiLogInfo.setLogLevel("INFO");
		 try {
	            if (trainingDTO.getTrainingType() == null || trainingDTO.getTrainingType().trim().isEmpty()) {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            	response.setServiceResponse("Training type cannot be empty");
	            	apiLogInfo.setApiResponse("Training type cannot be empty");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                return response;
	            }

	            // Check if training type already exists
	            if (trainingTypeMasterRepository.existsByTrainingTypeIgnoreCase(trainingDTO.getTrainingType().trim())) {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            	apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Training type '" + trainingDTO.getTrainingType() + "' already exists");
	                return response;
	            }

	            // Create new training type
	            TrainingTypeMaster trainingType = new TrainingTypeMaster();
	            trainingType.setTrainingType(trainingDTO.getTrainingType().trim());
	            trainingType.setCreatedBy(trainingDTO.getCreatedBy());
	            trainingType.setCreatedOn(LocalDateTime.now());
	            trainingType.setIsActive(true);

	            // Save to database
	            TrainingTypeMaster saved = trainingTypeMasterRepository.save(trainingType);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Training type added successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            
	        } catch (Exception e) {
	        	e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Error adding training type");
				response.setServiceError(e.getMessage());
	        }
	        
	        return response;
	}
	
	@Override
    public ServiceResponse getAllTrainingTypes() {
        ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/training/getAllTrainingTypes");
		apiLogInfo.setLogLevel("INFO");
        try {
            List<TrainingTypeMaster> types = trainingTypeMasterRepository.findByIsActiveTrueOrderByTrainingTypeAsc();            
            System.out.println(types.toString());
            if(!types.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Training type retrieved successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(types);
            }
            else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No training type found");
			}
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Error retrieving training types: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
}
