package com.apmosys.employeeportal.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.ResourceNotFoundException;
import com.apmosys.employeeportal.Exception.TrainingException;
import com.apmosys.employeeportal.dto.LockStatusDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PendingTrainingDTO;
import com.apmosys.employeeportal.dto.SurveyQuestionDTO;
import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingFrequencyDTO;
import com.apmosys.employeeportal.dto.TrainingIdResponsePassStatusDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
import com.apmosys.employeeportal.dto.UserTrainingDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeQuizResponseStatusMapping;
import com.apmosys.employeeportal.model.SurveyQuestion;
import com.apmosys.employeeportal.model.TrainingConsent;
import com.apmosys.employeeportal.model.TrainingContent;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.model.TrainingSkip;
import com.apmosys.employeeportal.repository.EmployeeQuizResponseStatusMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TrainingConsentRepository;
import com.apmosys.employeeportal.repository.TrainingContentRepository;
import com.apmosys.employeeportal.repository.TrainingMasterRepository;
import com.apmosys.employeeportal.repository.TrainingQuizMappingRepository;
import com.apmosys.employeeportal.repository.TrainingSkipRepository;
import com.apmosys.employeeportal.serviceInterface.TrainingUserService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

/**
 * Service implementation for User Training operations
 * Handles user journey operations like attending training, skipping, consent, etc.
 */
@Service
public class TrainingUserServiceImpl implements TrainingUserService {

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
	private EmployeeQuizResponseStatusMappingRepository employeeQuizResponseStatusMappingRepository;

	@Autowired
	private TrainingQuizMappingRepository trainingQuizMappingRepository;

	@Autowired
	@Lazy
	private TrainingConfigServiceImpl trainingConfigServiceImpl;
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;

	@Value("${training.job.role.exclude}")
	private String trainingJobRoleExclude;

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
			   Integer cycleNumber =calculateCurrentCycle(consentDTO.getEmpId(), consentDTO.getTrainingId());
			    consentDTO.setCompletionCycleNumber(cycleNumber);
			
			
			// Validate that content is current active content
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(consentDTO.getTrainingId() ,   PageRequest.of(0, 1))
			        .stream()
			        .findFirst();
			
			if (activeContentOpt.isEmpty() || !activeContentOpt.get().getContentId().equals(consentDTO.getContentId())) {
				// response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				// response.setServiceResponse("Content is not the current active content for this training");
				apiLogInfo.setApiResponse("Invalid content");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Content is not the current active content for this training");
				// return response;
			}

			Long activeQuizId = trainingQuizMappingRepository.findActiveSurveyIdByTraining(consentDTO.getTrainingId());

			if(activeQuizId == null) {
				activeQuizId = null;
			}
			
			// Check if consent already exists
			Optional<TrainingConsent> existingConsent = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndQuizIdAndCycleNumber(
				consentDTO.getEmpId(),
				consentDTO.getTrainingId(),
				consentDTO.getContentId(),
				activeQuizId,
				cycleNumber
			);
			
			if (existingConsent.isPresent()) {
				apiLogInfo.setApiResponse("Consent already exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("Consent already exists");
			}
			
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(consentDTO.getTrainingId());
			Optional<TrainingContent> contentOpt = trainingContentRepository.findByContentId(consentDTO.getContentId());
			
			if (trainingOpt.isEmpty() || contentOpt.isEmpty()) {
				apiLogInfo.setApiResponse("Training/content not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training or content not found");
			}
			
			TrainingConsent consent = new TrainingConsent();
			consent.setTrainingMaster(trainingOpt.get());
			consent.setTrainingContent(contentOpt.get());
			consent.setEmpId(consentDTO.getEmpId());
			consent.setCompletionCycleNumber(cycleNumber);
			consent.setCreatedBy(consentDTO.getEmpId());
			consent.setQuizId(activeQuizId);
			
			trainingConsentRepository.save(consent);
			
			// Delete skip record if exists (use the same cycle number)
			Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
				consentDTO.getEmpId(),
				consentDTO.getTrainingId(),
				activeQuizId,
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
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
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
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Training ID: ").append(skipDTO.getTrainingId())
				  .append(", Emp ID: ").append(skipDTO.getEmpId())
				  .append(", Cycle: ").append(skipDTO.getCycleNumber());
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(skipDTO.getTrainingId());
			Long activeQuizId = trainingQuizMappingRepository.findActiveSurveyIdByTraining(skipDTO.getTrainingId());
			
			if (trainingOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training not found");
				return response;
			}
			
			TrainingMaster training = trainingOpt.get();
			
			// Validate skip allowed
			if (!"true".equals(training.getSkipAllowed())) {
				apiLogInfo.setApiResponse("Skip is not allowed for this training");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("Skip is not allowed for this training");
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
					apiLogInfo.setApiResponse("Deadline has passed. Skip is not allowed");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					throw new TrainingException("Deadline has passed. Skip is not allowed");
				}
			}
			
			// Check if skip record exists
			Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
				skipDTO.getEmpId(),
				skipDTO.getTrainingId(),
				activeQuizId,
				cycleNumber
			);
			
			TrainingSkip skip;
			if (skipOpt.isPresent()) {
				// Update existing record
				skip = skipOpt.get();
				skip.setSkipCount(skip.getSkipCount() + 1);
				skip.setQuizId(activeQuizId);
				// last_skipped_on will be auto-updated by database
			} else {
				// Create new record
				skip = new TrainingSkip();
				skip.setTrainingMaster(training);
				skip.setEmpId(skipDTO.getEmpId());
				skip.setQuizId(activeQuizId);
				skip.setCycleNumber(cycleNumber);
				skip.setSkipCount(1);
			}
			
			trainingSkipRepository.save(skip);
			
			// Check lock status
			getLockStatusInternal(skipDTO.getEmpId());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training skipped successfully");
			apiLogInfo.setApiResponse("Training skipped successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
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

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Content ID: ").append(contentId);

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
				apiLogInfo.setApiResponse("External link content cannot be downloaded");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new TrainingException("External link content cannot be downloaded");
	        }

	        if (content.getContentPath() == null || content.getContentPath().isBlank()) {
	            // response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            // response.setServiceResponse("Content file path not found");
	            // return response;
				apiLogInfo.setApiResponse("Content file path not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Content file path not found");
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
	            // response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            // response.setServiceResponse("Invalid file path");
	            // return response;
				apiLogInfo.setApiResponse("Invalid file path");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Invalid file path");
	        }

	        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
	            // response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            // response.setServiceResponse("File not found on server");
	            // return response;
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
	                mimeType = null; // fallback handled below
	            }
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
	        e.printStackTrace();
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
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

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Emp ID: ").append(empId);
		
		try {
			LockStatusDTO lockStatus = getLockStatusInternal(empId);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(lockStatus);
			apiLogInfo.setApiResponse("Lock status fetched successfully");
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

			List<Integer> allTrainingsWithQuiz = employeeQuizResponseStatusMappingRepository.getTrainingIdsHavingQuiz(allActiveTrainings.stream().map(TrainingMaster::getTrainingId).collect(Collectors.toList()));

			List<TrainingIdResponsePassStatusDTO> alreadyAttemptedQuizes = employeeQuizResponseStatusMappingRepository.getTrainingIdsHavingQuizAndEmpId(allTrainingsWithQuiz, empId);

			List<TrainingConsent> alreadySeenContent = trainingConsentRepository.findByEmpIdAndTrainingIdsIn(empId, allActiveTrainings.stream().map(TrainingMaster::getTrainingId).collect(Collectors.toList()));
			
			List<UserTrainingDTO> userTrainings = new ArrayList<>();
			
			for (TrainingMaster training : allActiveTrainings) {
			    UserTrainingDTO userTraining = new UserTrainingDTO();
				userTraining.setTrainingId(training.getTrainingId());
				userTraining.setTrainingName(training.getTrainingName());
				userTraining.setTrainingType(training.getTrainingType());
				userTraining.setLockEnabled("true".equals(training.getLockEnabled()));
				userTraining.setMandatoryFlag(training.getMandatoryFlag());
				userTraining.setMinViewTimeMinutes(training.getMinViewTimeMinutes());
				userTraining.setConsentRequired(training.getConsentRequired());
				userTraining.setSkipAllowed(training.getSkipAllowed());
				userTraining.setRequiredFrequency(calculateTotalFrequency(training));
				userTraining.setHasQuiz(allTrainingsWithQuiz.contains(training.getTrainingId()));
				userTraining.setHasSeenContent(alreadySeenContent.stream().anyMatch(e -> e.getTrainingMaster().getTrainingId().equals(training.getTrainingId())));
				userTraining.setQuizAttempted(alreadyAttemptedQuizes.stream().anyMatch(e -> e.getTrainingId().equals(training.getTrainingId())));
				
				Long activeQuizId = trainingQuizMappingRepository.findActiveSurveyIdByTraining(training.getTrainingId());
				// Calculate completion count
				int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId(),activeQuizId);
				userTraining.setCompletionCount(completionCount);
				
				// Get current active content
				Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(training.getTrainingId(), PageRequest.of(0, 1))
				        .stream()
				        .findFirst();
				if (activeContentOpt.isEmpty()) {
					continue; // Skip trainings without active content
				}
				
				TrainingContent activeContent = activeContentOpt.get();
				TrainingContentDTO contentDTO = convertToTrainingContentDTO(activeContent);
				userTraining.setContent(contentDTO);
				
				// Calculate current cycle based on deadline pattern and current date (user action independent)
				int currentCycle = calculateCurrentCycleByDate(training);
				userTraining.setCurrentCycleNumber(currentCycle);

				if(activeQuizId == null) {
					activeQuizId = null;
				}
				
				// Check if consent exists for current active content in current cycle
				Optional<TrainingConsent> consentOpt = trainingConsentRepository.findByEmpIdAndTrainingIdAndContentIdAndQuizIdAndCycleNumber(
					empId,
					training.getTrainingId(),
					activeContent.getContentId(),
					activeQuizId,
					currentCycle
				);
				
				// Check if skip exists for current cycle
				// Optional<TrainingSkip> skipOpt = trainingSkipRepository.findByEmpIdAndTrainingIdAndCycleNumber(
				// 	empId,
				// 	training.getTrainingId(),
				// 	activeQuizId,
				// 	currentCycle
				// );
				
				// Determine status
				String status;
				if (consentOpt.isPresent()) {
					status = "COMPLETED";
					// Get last completed date (most recent consent)
					// List<TrainingConsent> allConsents = trainingConsentRepository.findByEmpIdAndTrainingIdAndQuizId(empId, training.getTrainingId(), activeQuizId);
					// if (!allConsents.isEmpty()) {
					// 	// List is already sorted DESC by consentTimestamp, so first element is most recent
					// 	TrainingConsent mostRecentConsent = allConsents.get(0);
					// 	if (mostRecentConsent != null && mostRecentConsent.getConsentTimestamp() != null) {
					// 		LocalDate lastCompleted = mostRecentConsent.getConsentTimestamp()
					// 		                .toLocalDateTime()
					// 		                .toLocalDate();


					// 		userTraining.setLastCompletedOn(lastCompleted);
					// 	}
					// }
				}
				// else if (skipOpt.isPresent()) {
				// 	status = "SKIPPED";
				// 	TrainingSkip skip = skipOpt.get();
				// 	if (skip != null) {
				// 		userTraining.setSkipCount(skip.getSkipCount());
				// 	}
				// } 
				else {
					status = "PENDING";

					
				}
				List<TrainingConsent> allConsents = trainingConsentRepository.findByEmpIdAndTrainingIdAndQuizId(empId, training.getTrainingId(), activeQuizId);
					if (!allConsents.isEmpty()) {
						// List is already sorted DESC by consentTimestamp, so first element is most recent
						TrainingConsent mostRecentConsent = allConsents.get(0);
						if (mostRecentConsent != null && mostRecentConsent.getConsentTimestamp() != null) {
							LocalDate lastCompleted = mostRecentConsent.getConsentTimestamp()
							                .toLocalDateTime()
							                .toLocalDate();


							userTraining.setLastCompletedOn(lastCompleted);
						}
					}
				
				userTraining.setStatus(status);
				
				// Calculate deadline
				LocalDate cycleDeadline = null;
				boolean isDeadlineCrossed = false;
				if ("true".equals(training.getDeadlineEnabled()) && training.getDeadlinePattern() != null) {
					cycleDeadline = calculateCycleDeadline(training, currentCycle);
					if (cycleDeadline != null) {
						isDeadlineCrossed = LocalDate.now().isAfter(cycleDeadline);
						// userTraining.setDeadline(Date.valueOf(cycleDeadline));
						userTraining.setDeadline(cycleDeadline);
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
			System.out.println("userTrainings ==>  "+userTrainings.size());
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(userTrainings);
			apiLogInfo.setApiResponse("User trainings fetched successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiRequest("Emp ID: " + empId);
			logService.logMyInfo(httpRequest, apiLogInfo);
			throw e;
		}
		
		apiLogInfo.setApiRequest("Emp ID: " + empId);
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse checkTrainingFrequency(Long empId, Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Check Training Frequency");
		apiLogInfo.setApiUrl("/api/training/checkTrainingFrequency");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Emp ID: ").append(empId).append(", Training ID: ").append(trainingId);
		
		try {
			Optional<TrainingMaster> trainingOpt = trainingMasterRepository.findByTrainingId(trainingId);
			
			if (trainingOpt.isEmpty()) {
				// response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				// response.setServiceResponse("Training not found");
				// return response;
				apiLogInfo.setApiResponse("Training not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("Training not found");
			}
			
			TrainingMaster training = trainingOpt.get();

			Long activeQuizId = trainingQuizMappingRepository.findActiveSurveyIdByTraining(trainingId);
			
			// Calculate completion count in last 12 months
			Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
			Long completionCount = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, activeQuizId, fromDate);
			
			TrainingFrequencyDTO dto = new TrainingFrequencyDTO();
			dto.setTrainingId(trainingId);
			dto.setTrainingName(training.getTrainingName());
			dto.setCompletionCount(completionCount != null ? completionCount.intValue() : 0);
			dto.setRequiredFrequency(calculateTotalFrequency(training));
			dto.setNeedsAssignment(completionCount < calculateTotalFrequency(training));

			// Get last completed date
			List<TrainingConsent> consents = trainingConsentRepository.findByEmpIdAndTrainingIdAndQuizId(empId, trainingId, activeQuizId);
			if (!consents.isEmpty()) {
				dto.setLastCompletedOn(consents.get(0).getConsentTimestamp().toLocalDateTime().toString());
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dto);
			apiLogInfo.setApiResponse("Training frequency checked successfully");
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
	public ServiceResponse checkTrainingRequirements(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Check Training Requirements");
		apiLogInfo.setApiUrl("/api/training/checkTrainingRequirements");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Emp ID: ").append(empId);
		
		try {
			// This is for cron job - check if employee needs training
			// For Phase-1, just return success
			// Actual implementation would check all employees if empId is null
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Training requirements checked");
			apiLogInfo.setApiResponse("Training requirements checked successfully");
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

	
	
	private int countCompletionsInLast12Months(Long empId, Integer trainingId, Long quizId) {
		Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
		Long count = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId,quizId, fromDate);
		return count != null ? count.intValue() : 0;
	}
	
	/*for employee prospective*/
	private int calculateCurrentCycle(Long empId, Integer trainingId) {
		/*Integer maxCycle = trainingConsentRepository.findMaxCycleNumber(empId, trainingId);
		return maxCycle != null ? maxCycle + 1 : 1;*/
		TrainingMaster training =trainingMasterRepository.findById(trainingId).get();
		int currentCycle= calculateCurrentCycleByDate(training);
         System.out.println("currentCycle=> "+currentCycle);
		return currentCycle;
		
	}
	
	/**
	 * Calculate current cycle based on deadline pattern and current date (user action independent)
	 * This determines which cycle we are currently in based on the calendar/deadline pattern
	 * Example: MID_YEAR pattern - if current month is March, we're in Cycle 1 (before June 30)
	 *          If current month is July, we're in Cycle 2 (after June 30, before December 31)
	 */
	private int calculateCurrentCycleByDate(TrainingMaster training) {
		if (training.getDeadlinePattern() == null) {
			// If no deadline pattern, default to cycle 1
			return 1;
		}
		
		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		String pattern = training.getDeadlinePattern();
		
		switch (pattern) {
			case "YEARLY":
				// Once per year: always cycle 1 until December 31
				return 1;
				
			case "MID_YEAR":
				// Cycle 1: Jan 1 - June 30, Cycle 2: July 1 - December 31
				if (now.getMonthValue() <= 6) {
					return 1; // January to June
				} else {
					return 2; // July to December
				}
				
			case "YEAR_END":
				// Same as MID_YEAR
				if (now.getMonthValue() <= 6) {
					return 1; // January to June
				} else {
					return 2; // July to December
				}
				
			case "QUARTERLY":
				// Cycle 1: Jan-Mar (deadline Mar 31), Cycle 2: Apr-Jun (deadline Jun 30)
				// Cycle 3: Jul-Sep (deadline Sep 30), Cycle 4: Oct-Dec (deadline Dec 31)
				int month = now.getMonthValue();
				if (month <= 3) {
					return 1;
				} else if (month <= 6) {
					return 2;
				} else if (month <= 9) {
					return 3;
				} else {
					return 4;
				}
				
			case "CUSTOM":
				if (training.getCustomDeadlineMonths() != null) {
					String[] months = training.getCustomDeadlineMonths().split(",");
					int currentMonth = now.getMonthValue();
					
					// Find which cycle we're in based on current month
					for (int i = 0; i < months.length; i++) {
						int deadlineMonth = Integer.parseInt(months[i].trim());
						if (currentMonth <= deadlineMonth) {
							return i + 1; // Cycle numbers start from 1
						}
					}
					// If current month is after all deadline months, we're in the last cycle
					return months.length;
				}
				return 1;
				
			default:
				return 1;
		}
	}
	
	private int calculateTotalFrequency(TrainingMaster training) {

	    if (training.getDeadlinePattern() == null) {
	        return 1; // Default
	    }

	    switch (training.getDeadlinePattern()) {

	        case "YEARLY":
	            return 1;

	        case "MID_YEAR":
	        case "YEAR_END":
	            return 2;

	        case "QUARTERLY":
	            return 4;

	        case "CUSTOM":
	            if (training.getCustomDeadlineMonths() != null &&
	                !training.getCustomDeadlineMonths().trim().isEmpty()) {

	                // Each deadline month represents one cycle
	                return training.getCustomDeadlineMonths().split(",").length;
	            }
	            return 1;

	        default:
	            return 1;
	    }
	}
	
	private LocalDate calculateCycleDeadline(TrainingMaster training, int cycleNumber) {
		if (training.getDeadlinePattern() == null) {
			return null;
		}
		
		int currentYear = LocalDate.now().getYear();
		String pattern = training.getDeadlinePattern();
		
		switch (pattern) {
			case "YEARLY":
				// Once per year: always last day of year (December 31)
				return LocalDate.of(currentYear, 12, 31);
				
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
	
	public LockStatusDTO getLockStatusInternal(Long empId) {

		LockStatusDTO defaultStatus = createUnlockedStatus();

		List<TrainingMaster> mandatoryTrainings = trainingMasterRepository.findActiveMandatoryTrainings("true", "true");
         System.out.println("mandatoryTrainings "+mandatoryTrainings);

		for (TrainingMaster training : mandatoryTrainings) {

			Long activeQuizId = trainingQuizMappingRepository.findActiveSurveyIdByTraining(training.getTrainingId());

			Optional<LockStatusDTO> evaluated = evaluateTrainingForLock(empId, training, activeQuizId);
              System.out.println(" evaluated : "+evaluated.isPresent());
			// Best case → immediately freeze
			if (evaluated.isPresent() && evaluated.get().getIsLocked()) {
				return evaluated.get();
			}

			// Training pending but not locked → keep info for routing
			if (evaluated.isPresent()) {
				defaultStatus = evaluated.get();
			}
		}

		return defaultStatus;
	}

	private Optional<LockStatusDTO> evaluateTrainingForLock(Long empId, TrainingMaster training, Long quizId) {

		int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId(), quizId);
      System.out.println("completionCount > "+completionCount);
		if (completionCount >= calculateTotalFrequency(training)){
			return Optional.empty(); // No pending
		}

		Optional<TrainingContent> activeContentOpt = trainingContentRepository
				.findCurrentActiveContent(training.getTrainingId(), PageRequest.of(0, 1)).stream().findFirst();

		if (activeContentOpt.isEmpty()) {
			return Optional.empty();
		} 
        System.out.println("activeContentOpt => "+activeContentOpt.isPresent());
		int currentCycle = calculateCurrentCycle(empId, training.getTrainingId());
		TrainingContent activeContent = activeContentOpt.get();
		 System.out.println("currentCycle > "+currentCycle);
		Optional<TrainingConsent> consentOpt = trainingConsentRepository
				.findByEmpIdAndTrainingIdAndContentIdAndQuizIdAndCycleNumber(empId, training.getTrainingId(),
						activeContent.getContentId(),quizId, currentCycle);
		
		if (consentOpt.isPresent()) {
			return Optional.empty(); // Current cycle already completed
		}

		boolean attendedAtLeastOnce = trainingConsentRepository.existsByEmpIdAndTrainingIdAndContentIdAndQuizId(empId,
				training.getTrainingId(), activeContent.getContentId(),quizId);

		boolean deadlineCrossed = isDeadlineCrossed(training, currentCycle);
		boolean lockEnabled = "true".equals(training.getLockEnabled());

		boolean shouldFreeze = (lockEnabled && !attendedAtLeastOnce) || deadlineCrossed;

		boolean hardLock = deadlineCrossed || lockEnabled; 
		
		System.err.println("deadlineCrossed "+deadlineCrossed+" lockEnabled "+" shouldFreeze "+shouldFreeze +" hardLock "+hardLock);

		LockStatusDTO status = buildPendingStatus(training, currentCycle, shouldFreeze, hardLock, deadlineCrossed);

		return Optional.of(status);
	}

	private LockStatusDTO createUnlockedStatus() {
		LockStatusDTO dto = new LockStatusDTO();
		dto.setIsLocked(false);
		dto.setHasMandatoryTrainingPending(false);
		dto.setIsHardLock(false);
		return dto;
	}

	private boolean isDeadlineCrossed(TrainingMaster training, int cycle) {
		if (!"true".equals(training.getDeadlineEnabled())) {
			return false;
		}

		LocalDate deadline = calculateCycleDeadline(training, cycle);
		if (deadline == null) {
			return false;
		}

		LocalDate today = LocalDate.now();

		return today.isAfter(deadline) || today.isEqual(deadline);
	}

	private LockStatusDTO buildPendingStatus(TrainingMaster training, int cycle, boolean isLocked, boolean isHardLock,
			boolean deadlineCrossed) {

		LockStatusDTO dto = new LockStatusDTO();

		dto.setHasMandatoryTrainingPending(true);
		dto.setLockedTrainingId(training.getTrainingId());
		dto.setLockedTrainingName(training.getTrainingName());
		dto.setCurrentCycleNumber(cycle);
		dto.setLockReason("Mandatory training pending");
		dto.setCanSkip("true".equals(training.getSkipAllowed()));
		dto.setDeadlineCrossed(deadlineCrossed);

		dto.setIsLocked(isLocked);
		dto.setIsHardLock(isHardLock);

		return dto;
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

	@Override
	public ServiceResponse getQuizQuestionByTrainingId(Integer trainingId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getQuizQuestionByTrainingId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TrainingId : " + trainingId);
		try {

			List<SurveyQuestion> questionList = trainingQuizMappingRepository.findActiveByTraining(trainingId);

			if (questionList == null || questionList.isEmpty()) {
				apiLogInfo.setApiResponse("No quiz questions found for training");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				throw new ResourceNotFoundException("No quiz questions found for training ID: " + trainingId);
			}

			Long quizId = questionList.get(0).getSurveyId();

			List<SurveyQuestionDTO> dtoList = new ArrayList<SurveyQuestionDTO>();

			questionList.forEach((object) -> {

				SurveyQuestionDTO dto = new SurveyQuestionDTO();

				dto.setSurveyQuestionId(object.getSurveyQuestionId());
				dto.setSurveyId(object.getSurveyId());
				dto.setQuestion(object.getQuestion());
				dto.setOptionType(object.getOptionType());
				dto.setOptions(object.getOptions());
				dto.setRequired(object.getRequired());
				dto.setDescription(object.getDescription());
				dtoList.add(dto);
			});

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(Map.of("quizId", quizId, "allSurveyQuestionList", dtoList));
			apiLogInfo.setApiResponse("All Questions By SurveyId Fetched");			
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

	public ResponseEntity<List<String>> getAllSlides(Integer trainingId, Integer contentId) {
		// Get the content
		TrainingContent content = trainingContentRepository.findById(contentId)
			.orElseThrow(() -> new ResourceNotFoundException("Content not found"));
		
		// Get slides directory path from content (you stored this during conversion)
		String slidesPath = content.getSlidesPath(); // e.g., /path/to/slides-251/
		
		// Get all slide images
		File slidesDir = new File(slidesPath);
		File[] slideFiles = slidesDir.listFiles((dir, name) -> 
			name.endsWith(".png") && (name.startsWith("slide-") || name.startsWith("page-")));
		
		// Sort slides by number
		Arrays.sort(slideFiles, (a, b) -> {
			int numA = extractNumber(a.getName());
			int numB = extractNumber(b.getName());
			return Integer.compare(numA, numB);
		});
		
		// Generate URLs for each slide
		List<String> slideUrls = new ArrayList<>();
		for (File slide : slideFiles) {
			String slideUrl = "/api/training/slide/" + trainingId + "/" + contentId + "/" + slide.getName();
			slideUrls.add(slideUrl);
		}
		
		return ResponseEntity.ok(slideUrls);
	}

	public ResponseEntity<Resource> getSlide(Integer trainingId, Integer contentId, String slideName) {
        
		String slidesPath = "";

		if(trainingConfigServiceImpl.getTrainingContentMap(contentId) != null) {
			slidesPath = trainingConfigServiceImpl.getTrainingContentMap(contentId);
		} else {
			TrainingContent content = trainingContentRepository.findById(contentId)
				.orElseThrow(() -> new ResourceNotFoundException("Content not found"));
			slidesPath = content.getSlidesPath();
			trainingConfigServiceImpl.setTrainingContentMap(contentId, slidesPath);
		}
        
        Path slidePath = Paths.get(slidesPath).resolve(slideName);
        
        // Security check
        Path basePath = Paths.get(trainingFileLocation).toAbsolutePath().normalize();
        if (!slidePath.toAbsolutePath().normalize().startsWith(basePath)) {
            throw new RuntimeException("Invalid file path");
        }
        
        Resource resource = new FileSystemResource(slidePath.toFile());
        
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(resource);
    }

    private int extractNumber(String filename) {
        Matcher matcher = Pattern.compile("\\d+").matcher(filename);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

}
