package com.apmosys.employeeportal.serviceInterface;

import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ComplianceReportDTO;
import com.apmosys.employeeportal.dto.TrainingContentDTO;
import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * Service interface for Training Configuration operations (HR/Admin)
 * Handles CRUD operations for training configuration and content management
 */
public interface TrainingConfigService {
	
	/**
	 * Get all trainings with optional filters
	 * @param activeStatus Optional filter for active status
	 * @param mandatoryFlag Optional filter for mandatory flag
	 * @return ServiceResponse containing list of trainings
	 */
	ServiceResponse getAllTrainings(String activeStatus, String mandatoryFlag);
	
	/**
	 * Create a new training with content in a single operation
	 * @param trainingDTO Training master data
	 * @param contentDTO Content data
	 * @param createdBy User ID who created the training
	 * @param file Optional file upload for content
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse createTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long createdBy, MultipartFile file);
	
	/**
	 * Update an existing training with content
	 * @param trainingDTO Training master data
	 * @param contentDTO Content data
	 * @param updatedBy User ID who updated the training
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse updateTrainingWithContent(TrainingMasterDTO trainingDTO, TrainingContentDTO contentDTO, Long updatedBy);
	
	/**
	 * Add content to an existing training
	 * @param contentDTO Content data
	 * @param createdBy User ID who created the content
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse addTrainingContent(TrainingContentDTO contentDTO, Long createdBy);
	
	/**
	 * Update existing training content
	 * @param contentDTO Content data with contentId
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse updateTrainingContent(TrainingContentDTO contentDTO);
	
	/**
	 * Get training content for a specific training
	 * @param trainingId Training ID
	 * @return ServiceResponse containing content information
	 */
	ServiceResponse getTrainingContent(Integer trainingId);
	
	/**
	 * Deactivate a training (soft delete)
	 * @param trainingId Training ID to deactivate
	 * @param updatedBy User ID who deactivated the training
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse deactivateTraining(Integer trainingId, Long updatedBy);
	
	/**
	 * Download/view training content file
	 * @param contentId Content ID
	 * @return ServiceResponse containing file resource and metadata
	 */
	ServiceResponse downloadContent(Integer contentId);
	
	/**
	 * Get employee training history (for reporting)
	 * @param empId Employee ID
	 * @param trainingId Optional training ID filter
	 * @return ServiceResponse containing training history
	 */
	ServiceResponse getEmployeeTrainingHistory(Long empId, Integer trainingId);
	
	/**
	 * Get compliance report for training
	 * @param trainingId Training ID
	 * @param departmentId Optional department ID filter
	 * @param status Optional status filter
	 * @return ServiceResponse containing compliance report data
	 */
	ServiceResponse getComplianceReport(Integer trainingId, Long departmentId, String status);
}
