package com.apmosys.employeeportal.serviceInterface;

import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface TrainingService {
	
	// HR Configuration APIs
	ServiceResponse getAllTrainings(String activeStatus, String mandatoryFlag);
	ServiceResponse addTrainingContent(TrainingContentDTO contentDTO, Long createdBy);
	ServiceResponse updateTrainingContent(TrainingContentDTO contentDTO);
	ServiceResponse getTrainingContent(Integer trainingId);
	ServiceResponse deactivateTraining(Integer trainingId, Long updatedBy);
	
	// Combined APIs - Training with Content
	ServiceResponse createTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long createdBy, MultipartFile file);
	ServiceResponse updateTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long updatedBy);
	
	// Employee Training APIs
	ServiceResponse getPendingTraining(Long empId);
	ServiceResponse getUserTrainings(Long empId);
	ServiceResponse submitConsent(TrainingConsentDTO consentDTO);
	ServiceResponse skipTraining(TrainingSkipDTO skipDTO);
	ServiceResponse getLockStatus(Long empId);
	ServiceResponse downloadContent(Integer contentId);
	ServiceResponse checkTrainingFrequency(Long empId, Integer trainingId);
	
	// Reporting APIs
	ServiceResponse getEmployeeTrainingHistory(Long empId, Integer trainingId);
	ServiceResponse getComplianceReport(Integer trainingId, Long departmentId, String status);
	
	// Internal/Cron APIs
	ServiceResponse checkTrainingRequirements(Long empId);
}
