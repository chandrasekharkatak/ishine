package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.TrainingConsentDTO;
import com.apmosys.employeeportal.dto.TrainingRequestDTO;
import com.apmosys.employeeportal.dto.TrainingSkipDTO;
import com.apmosys.employeeportal.serviceInterface.TrainingUserService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * Controller for User Training operations
 * Handles user journey operations like attending training, skipping, consent, etc.
 */
@RestController
@RequestMapping(path = "/api/training")
public class TrainingUserController {

	@Autowired
	private TrainingUserService trainingUserService;
	
	@Value("${file.location.documents.training}")
	private String trainingFileLocation;

	// ==================== Employee Training APIs ====================
	@PostMapping(value = "/getUserTrainings")
	public ServiceResponse getUserTrainings(@RequestBody TrainingRequestDTO request) {
		try {
			if (request == null || request.getEmpId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID is required");
				return response;
			}
			return trainingUserService.getUserTrainings(request.getEmpId());
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error fetching user trainings: " + e.getMessage());
			return response;
		}
	}

	@PostMapping(value = "/submitConsent")
	public ServiceResponse submitConsent(@RequestBody TrainingConsentDTO consentDTO) {
		try {
			if (consentDTO == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Consent data is required");
				return response;
			}
			if (consentDTO.getEmpId() == null || consentDTO.getTrainingId() == null || consentDTO.getContentId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID, Training ID, and Content ID are required");
				return response;
			}
			return trainingUserService.submitConsent(consentDTO);
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error submitting consent: " + e.getMessage());
			return response;
		}
	}

	@PostMapping(value = "/skipTraining")
	public ServiceResponse skipTraining(@RequestBody TrainingSkipDTO skipDTO) {
		try {
			if (skipDTO == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Skip data is required");
				return response;
			}
			if (skipDTO.getEmpId() == null || skipDTO.getTrainingId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID and Training ID are required");
				return response;
			}
			return trainingUserService.skipTraining(skipDTO);
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error skipping training: " + e.getMessage());
			return response;
		}
	}

	@PostMapping(value = "/getLockStatus")
	public ServiceResponse getLockStatus(@RequestBody TrainingRequestDTO request) {
		try {
			if (request == null || request.getEmpId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID is required");
				return response;
			}
			return trainingUserService.getLockStatus(request.getEmpId());
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error fetching lock status: " + e.getMessage());
			return response;
		}
	}

	@PostMapping(value = "/checkTrainingFrequency")
	public ServiceResponse checkTrainingFrequency(@RequestBody TrainingRequestDTO request) {
		try {
			if (request == null || request.getEmpId() == null || request.getTrainingId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID and Training ID are required");
				return response;
			}
			return trainingUserService.checkTrainingFrequency(request.getEmpId(), request.getTrainingId());
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error checking training frequency: " + e.getMessage());
			return response;
		}
	}

	// ==================== Internal/Cron APIs ====================
	
	@PostMapping(value = "/checkTrainingRequirements")
	public ServiceResponse checkTrainingRequirements(@RequestBody TrainingRequestDTO request) {
		try {
			if (request == null || request.getEmpId() == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID is required");
				return response;
			}
			return trainingUserService.checkTrainingRequirements(request.getEmpId());
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error checking training requirements: " + e.getMessage());
			return response;
		}
	}

	@GetMapping("/getQuizQuestionByTrainingId/{trainingId}")
	public ServiceResponse getQuizQuestionByTrainingId(@PathVariable Integer trainingId) {
		try {
			if (trainingId == null) {
				ServiceResponse response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Training ID is required");
				return response;
			}
			return trainingUserService.getQuizQuestionByTrainingId(trainingId);
		} catch (Exception e) {
			e.printStackTrace();
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error fetching quiz questions: " + e.getMessage());
			return response;
		}
	}

}
