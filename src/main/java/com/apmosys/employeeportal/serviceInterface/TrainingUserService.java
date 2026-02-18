package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;
/**
 * Service interface for User Training operations
 * Handles user journey operations like attending training, skipping, consent, etc.
 */
public interface TrainingUserService {
		
	/**
	 * Get all trainings for an employee (with status, cycles, etc.)
	 * @param empId Employee ID
	 * @return ServiceResponse containing list of user trainings
	 */
	ServiceResponse getUserTrainings(Long empId);
	
	/**
	 * Submit consent after attending training
	 * @param consentDTO Consent data including trainingId, empId, cycleNumber, etc.
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse submitConsent(TrainingConsentDTO consentDTO);
	
	/**
	 * Skip a training (if allowed)
	 * @param skipDTO Skip data including trainingId, empId, cycleNumber, etc.
	 * @return ServiceResponse indicating success or failure
	 */
	ServiceResponse skipTraining(TrainingSkipDTO skipDTO);
	
	/**
	 * Get lock status for an employee (check if training lock is active)
	 * @param empId Employee ID
	 * @return ServiceResponse containing lock status information
	 */
	ServiceResponse getLockStatus(Long empId);
	
	/**
	 * Download/view training content file (for user viewing)
	 * @param contentId Content ID
	 * @return ServiceResponse containing file resource and metadata
	 */
	ServiceResponse downloadContent(Integer contentId);
	
	/**
	 * Check training frequency requirements for an employee
	 * @param empId Employee ID
	 * @param trainingId Training ID
	 * @return ServiceResponse containing frequency check results
	 */
	ServiceResponse checkTrainingFrequency(Long empId, Integer trainingId);
	
	/**
	 * Check training requirements for an employee (internal/cron use)
	 * @param empId Employee ID
	 * @return ServiceResponse containing training requirements status
	 */
	ServiceResponse checkTrainingRequirements(Long empId);

	ServiceResponse getQuizQuestionByTrainingId(Integer trainingId);

}
