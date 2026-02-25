package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.BusinessValidationException;
import com.apmosys.employeeportal.Exception.FileValidationException;
import com.apmosys.employeeportal.Exception.GlobalException;
import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingHistoryDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.exception.BadRequestException;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeQuizResponseStatusMapping;
import com.apmosys.employeeportal.model.TrainingConsent;
import com.apmosys.employeeportal.model.TrainingContent;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.model.TrainingSkip;
import com.apmosys.employeeportal.repository.EmployeeQuizResponseStatusMappingRepository;
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
	
	@Autowired
	private  TrainingFileValidator trainingFileValidator;

	@Autowired
	private EmployeeQuizResponseStatusMappingRepository employeeQuizResponseStatusMappingRepository;

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
			List<TrainingMasterDTO> trainings;

			if (activeStatus != null && mandatoryFlag != null) {
				trainings = trainingMasterRepository.findByMandatoryFlagAndActiveStatus(mandatoryFlag, activeStatus, List.of("fail", null));
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
		logBuilder.append("Training ID: ").append(contentDTO.getTrainingId()).append(", Content Type: ")
				.append(contentDTO.getContentType()).append(", CreatedBy: ").append(createdBy);

		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository
					.findByTrainingId(contentDTO.getTrainingId());
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
	
		try {
			
			/*
			 * ============================= 1️⃣ BASIC VALIDATION
			 * ==============================
			 */

			validateTrainingDTO(trainingDTO);
			validateContentDTO(contentDTO);

			/*
			 * ============================= 2️⃣ DUPLICATE CHECK
			 * ==============================
			 */

			Optional<TrainingMaster> existing = trainingMasterRepository
					.findByTrainingNameIgnoreCase(trainingDTO.getTrainingName().trim());

			if (existing.isPresent()) {
				throw new RuntimeException("Training name already exists.");
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
					throw new RuntimeException("File is required for " + contentDTO.getContentType());
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

		} catch (Exception e) {

			throw new RuntimeException("Failed to create training : "+ e.getMessage());
			// e.printStackTrace();
			// response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			// response.setServiceResponse("Something Went Wrong.");
			// response.setServiceError(e.getMessage());
			// apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			// apiLogInfo.setLogLevel("ERROR");
		}

		return response;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTrainingWithContent(
	        TrainingMasterDTO trainingDTO,
	        TrainingContentDTO contentDTO,
	        MultipartFile file) {

	    ServiceResponse response = new ServiceResponse();

	    try {
	    	
	    	

	        /* ===========================
	           1️⃣ BASIC VALIDATION
	        =========================== */

	        if (trainingDTO == null || trainingDTO.getTrainingId() == null)
	            throw new RuntimeException("Training ID is required for update");

	        if (trainingDTO.getUpdatedBy() == null)
	            throw new RuntimeException("Updated by is required");

	        if (contentDTO == null)
	            throw new RuntimeException("Content DTO is required");

	        /* ===========================
	           2️⃣ FETCH TRAINING
	        =========================== */

	        TrainingMaster training = trainingMasterRepository
	                .findByTrainingId(trainingDTO.getTrainingId())
	                .orElseThrow(() -> new RuntimeException("Training not found"));

	        /* ===========================
	           3️⃣ DUPLICATE NAME CHECK
	        =========================== */

	        if (trainingDTO.getTrainingName() != null
	                && !trainingDTO.getTrainingName().trim().isEmpty()) {

	            Optional<TrainingMaster> existing =
	                    trainingMasterRepository.findByTrainingNameAndNotTrainingId(
	                            trainingDTO.getTrainingName().trim(),
	                            trainingDTO.getTrainingId());

	            if (existing.isPresent())
	                throw new RuntimeException("Training name already exists.");
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
	            throw new RuntimeException(
	                    "Training effective from must be before effective to");

	        /* ===========================
	           5️⃣ UPDATE TRAINING FIELDS
	        =========================== */

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
	                .orElseThrow(() ->
	                        new RuntimeException("Content not found for update"));

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
		     }

	        /* ===========================
	           9️⃣ UPDATE CONTENT FIELDS
	        =========================== */

	        newTrainingContent.setContentType(contentDTO.getContentType());
	        newTrainingContent.setContentName(contentDTO.getContentName());
	        newTrainingContent.setExternalLinkUrl(contentDTO.getExternalLinkUrl());
	        newTrainingContent.setEffectiveFrom(contentDTO.getEffectiveFrom());
	        newTrainingContent.setEffectiveTo(contentDTO.getEffectiveTo());
			newTrainingContent.setActiveStatus("true");
			newTrainingContent.setCreatedBy(trainingDTO.getUpdatedBy());
			newTrainingContent.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			newTrainingContent.setTrainingMaster(updatedTraining);


			existingContent.setActiveStatus("false");
			existingContent.setUpdatedBy(trainingDTO.getUpdatedBy());
			existingContent.setUpdatedOn(new Timestamp(System.currentTimeMillis()));

	        // newTrainingContent.setUpdatedBy(trainingDTO.getUpdatedBy());
	        // newTrainingContent.setUpdatedOn(new Timestamp(System.currentTimeMillis()));

	        TrainingContent savedContent =
	                trainingContentRepository.save(newTrainingContent);
				trainingContentRepository.save(existingContent);

	  
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
	     }

	     // Delete old file if replacing FILE → FILE
	     if (newFilePath != null
	             && oldFilePath != null
	             && !oldFilePath.isBlank()
	             && !oldFilePath.equals(newFilePath)) {

	         safeDeleteTrainingFile(oldFilePath);
	     }
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Training and content updated successfully");
	        response.setServiceMessage(savedContent.getContentId().toString());

	    } catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Training and content update failed");
			response.setServiceMessage(e.getMessage());
	        // throw new RuntimeException(e.getMessage(), e);
	    }

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

		} catch (FileValidationException e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("WARN");
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong." + e.getMessage());
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
				boolean isActive = "true".equals(content.getActiveStatus())
						&& content.getEffectiveFrom().isBefore(today)
						&& (content.getEffectiveTo() == null || content.getEffectiveTo().isAfter(today));
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
			training.setUpdatedOn(LocalDateTime.now());

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
			Path basePath = Paths.get(trainingFileLocation).toAbsolutePath().normalize();

			Path filePath = basePath.resolve(content.getContentPath()).normalize();

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
				// Get all consents for employee (all trainings) - need to get all trainings
				// first
				// For now, get all active trainings and then get consents for each
				List<TrainingMaster> allTrainings = trainingMasterRepository.findAll();
				consents = new ArrayList<>();
				for (TrainingMaster training : allTrainings) {
					consents.addAll(
							trainingConsentRepository.findByEmpIdAndTrainingId(empId, training.getTrainingId()));
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
				Long completionCount = trainingConsentRepository.countCompletionsInLast12Months(empId,
						consent.getTrainingMaster().getTrainingId(), fromDate);
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
				apiLogInfo.setApiResponse("Compliance report generated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
	 * Count completions in last 12 months for an employee and training Used by
	 * reporting methods
	 */
	private int countCompletionsInLast12Months(Long empId, Integer trainingId) {
		Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
		Long count = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, fromDate);
		return count != null ? count.intValue() : 0;
	}

	/**
	 * Convert TrainingMaster entity to DTO
	 */
// 	private TrainingMasterDTO convertToTrainingMasterDTO(TrainingMaster training) {
// 		TrainingMasterDTO dto = new TrainingMasterDTO();
// 		dto.setTrainingId(training.getTrainingId());
// 		dto.setTrainingName(training.getTrainingName());
// 		dto.setTrainingType(training.getTrainingType());
// 		dto.setMandatoryFlag(training.getMandatoryFlag());
// 		dto.setEffectiveFrom(training.getEffectiveFrom());
// 		dto.setEffectiveTo(training.getEffectiveTo());
// //		dto.setFrequencyPerYear(training.getFrequencyPerYear());
// 		dto.setLockEnabled(training.getLockEnabled());
// 		dto.setMinViewTimeMinutes(training.getMinViewTimeMinutes());
// 		dto.setConsentRequired(training.getConsentRequired());
// 		dto.setSkipAllowed(training.getSkipAllowed());
// 		dto.setDeadlineEnabled(training.getDeadlineEnabled());
// 		dto.setDeadlinePattern(training.getDeadlinePattern());
// 		dto.setCustomDeadlineMonths(training.getCustomDeadlineMonths());
// 		dto.setActiveStatus(training.getActiveStatus());
// 		dto.setCreatedBy(training.getCreatedBy());

// 		if (training.getCreatedOn() != null) {
// 			dto.setCreatedOn(formatTimestampToString(training.getCreatedOn()));
// 		}
// 		if (training.getUpdatedOn() != null) {
// 			dto.setUpdatedOn(formatTimestampToString(training.getUpdatedOn()));
// 		}

// 		// Get employee names
// 		if (training.getCreatedBy() != null) {
// 			Optional<Employee> empOpt = employeeRepository.findById(training.getCreatedBy());
// 			if (empOpt.isPresent()) {
// 				dto.setCreatedByName(empOpt.get().getName());
// 			}
// 		}
// 		if (training.getUpdatedBy() != null) {
// 			Optional<Employee> empOpt = employeeRepository.findById(training.getUpdatedBy());
// 			if (empOpt.isPresent()) {
// 				dto.setUpdatedByName(empOpt.get().getName());
// 			}
// 		}

// 		return dto;
// 	}

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
				response.setStatusCode(HttpStatus.BAD_REQUEST.value());
				response.setServiceMessage("Quiz Response Not Found");
				apiLogInfo.setApiResponse(responseStatus);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			employeeQuizResponseStatusMapping.setPassStatus(responseStatus);
			employeeQuizResponseStatusMapping.setUpdatedBy(updatedBy);
			employeeQuizResponseStatusMappingRepository.save(employeeQuizResponseStatusMapping);

			response.setStatusCode(HttpStatus.OK.value());
			response.setServiceMessage("Quiz Response Changed Successfully");
			apiLogInfo.setApiResponse(responseStatus);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setServiceMessage("Failed to Change Quiz Response");
			apiLogInfo.setApiResponse(responseStatus);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}	
		return response;
	}

}
