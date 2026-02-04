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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LockStatusDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PendingTrainingDTO;
import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingFrequencyDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
import com.apmosys.employeeportal.dto.UserTrainingDTO;
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
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;

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
			Optional<TrainingContent> activeContentOpt = trainingContentRepository.findCurrentActiveContent(consentDTO.getTrainingId() ,PageRequest.of(0, 1))
			        .stream()
			        .findFirst();
			
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
			
			System.out.println("userTrainings "+allActiveTrainings.size());
			
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
				userTraining.setRequiredFrequency(training.getFrequencyPerYear());
				
				// Calculate completion count
				int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId());
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
			System.out.println("userTrainings ==>  "+userTrainings.size());
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

	
	
	private int countCompletionsInLast12Months(Long empId, Integer trainingId) {
		Timestamp fromDate = Timestamp.valueOf(LocalDate.now().minusMonths(12).atStartOfDay());
		Long count = trainingConsentRepository.countCompletionsInLast12Months(empId, trainingId, fromDate);
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
	
	private LockStatusDTO getLockStatusInternal(Long empId) {

		LockStatusDTO defaultStatus = createUnlockedStatus();

		List<TrainingMaster> mandatoryTrainings = trainingMasterRepository.findActiveMandatoryTrainings("true", "true");
         System.out.println("mandatoryTrainings "+mandatoryTrainings);
		for (TrainingMaster training : mandatoryTrainings) {

			Optional<LockStatusDTO> evaluated = evaluateTrainingForLock(empId, training);
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

	private Optional<LockStatusDTO> evaluateTrainingForLock(Long empId, TrainingMaster training) {

		int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId());
      System.out.println("completionCount > "+completionCount);
		if (completionCount >= training.getFrequencyPerYear()) {
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
				.findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(empId, training.getTrainingId(),
						activeContent.getContentId(), currentCycle);
		
		System.err.println("consentOpt : > "+consentOpt.isPresent());

		if (consentOpt.isPresent()) {
			return Optional.empty(); // Current cycle already completed
		}

		boolean attendedAtLeastOnce = trainingConsentRepository.existsByEmpIdAndTrainingIdAndContentId(empId,
				training.getTrainingId(), activeContent.getContentId());

		boolean deadlineCrossed = isDeadlineCrossed(training, currentCycle);
		boolean lockEnabled = "true".equals(training.getLockEnabled());

		boolean shouldFreeze = (lockEnabled && !attendedAtLeastOnce) || deadlineCrossed;

		boolean hardLock = deadlineCrossed;
		
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
	
}
