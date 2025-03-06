package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AppraisalSummaryDto;
import com.apmosys.employeeportal.model.AppraisalSummary;
import com.apmosys.employeeportal.model.QuestionnaireResponse;
import com.apmosys.employeeportal.model.ReviewTable;
import com.apmosys.employeeportal.repository.AppraisalSummaryRepository;
import com.apmosys.employeeportal.repository.QuestionnaireRepository;
import com.apmosys.employeeportal.repository.QuestionnaireResponseRepository;
import com.apmosys.employeeportal.repository.ReviewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppraisalSummaryService {

    @Autowired
    private AppraisalSummaryRepository appraisalSummaryRepository;
    
    @Autowired
    private ReviewRepository reviewRepo;

//    private QuestionnaireRepository questionnaireResponseRepository
    @Autowired
    private QuestionnaireResponseRepository questionnaireResponseRepo;
    
//    public ServiceResponse createAppraisalSummary(AppraisalSummaryDto appraisalSummaryDto) {
//        ServiceResponse response = new ServiceResponse();
//        try {
//         
//        	if (appraisalSummaryDto.getEmployeeId() == null) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceMessage("Employee ID is required");
//                return response;
//            }
//
//           
//            AppraisalSummary appraisalSummary = convertToEntity(appraisalSummaryDto);
//            
//            AppraisalSummary savedAppraisalSummary = appraisalSummaryRepository.save(appraisalSummary);
//            
//            AppraisalSummaryDto savedDto = convertToDto(savedAppraisalSummary);
//            
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(savedDto);
//            response.setServiceMessage("Appraisal Summary Created Successfully");
//        } catch (Exception e) {
//            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//            response.setServiceError(e.getMessage());
//            response.setServiceMessage("Error Creating Appraisal Summary");
//        }
//        return response;
//    }
    public ServiceResponse calculateAndCreateAppraisalSummary(Long employeeId) {
        ServiceResponse response = new ServiceResponse();
        try {

            List<QuestionnaireResponse> responses = questionnaireResponseRepo.findByEmpId(employeeId);
            List<ReviewTable> response1 = reviewRepo.findByEmployeeId(employeeId);
            if (responses == null || responses.size() != 4) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Incomplete data: Exactly 4 questionnaire responses are required");
                return response;
            }
            if (response1 == null || response1.size() != 4) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Incomplete data: Exactly 4 questionnaire responses are required");
                return response;
            }
            float totalScore = (float) responses.stream()
            	    .mapToDouble(resp -> resp.getScore() != null ? resp.getScore() : 0.0f)
            	    .sum();
            float totalScore1 = (float) response1.stream()
            	    .mapToDouble(resp -> resp.getKpiScore() != null ? resp.getKpiScore() : 0.0f)
            	    .sum();
            
            float averageScore = (totalScore / 4.0f) + (totalScore1/4.0f);
            //last mein convert to 40 also add kpi score to this 
            Integer finalRating = calculateFinalRating(averageScore);
            AppraisalSummaryDto appraisalSummaryDto = new AppraisalSummaryDto();
            appraisalSummaryDto.setEmployeeId(employeeId);
            appraisalSummaryDto.setAppraisalScore(averageScore);
            appraisalSummaryDto.setAppraisalPercentage(String.format("%.2f%%", averageScore));
            appraisalSummaryDto.setFinalRating(averageScore);
            appraisalSummaryDto.setFinalRemarks(generateRemarks(finalRating));

   
            AppraisalSummary appraisalSummary = convertToEntity(appraisalSummaryDto);
            AppraisalSummary savedAppraisalSummary = appraisalSummaryRepository.save(appraisalSummary);

            AppraisalSummaryDto savedDto = convertToDto(savedAppraisalSummary);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(savedDto);
            response.setServiceMessage("Appraisal Summary Calculated and Created Successfully");

        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Calculating Appraisal Summary");
        }
        return response;
    }

    public ServiceResponse getAppraisalSummaryById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<AppraisalSummary> appraisalSummary = appraisalSummaryRepository.findById(id);
            
            if (appraisalSummary.isPresent()) {
                AppraisalSummaryDto dto = convertToDto(appraisalSummary.get());
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                response.setServiceMessage("Appraisal Summary Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Appraisal Summary Not Found");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Appraisal Summary");
        }
        return response;
    }

    public ServiceResponse getAllAppraisalSummaries() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<AppraisalSummary> appraisalSummaries = appraisalSummaryRepository.findAll();
            
            List<AppraisalSummaryDto> dtoList = appraisalSummaries.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            response.setServiceMessage("All Appraisal Summaries Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Appraisal Summaries");
        }
        return response;
    }

    public ServiceResponse getAppraisalSummaryByEmployeeId(Long employeeId) {
        ServiceResponse response = new ServiceResponse();
        try {
           
            List<AppraisalSummary> appraisalSummaries = appraisalSummaryRepository.findByEmployeeId(employeeId);
            
            
            List<AppraisalSummaryDto> dtoList = appraisalSummaries.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            if (!dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Appraisal Summaries Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("No Appraisal Summaries Found for Employee");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Appraisal Summaries");
        }
        return response;
    }
    
    private Integer calculateFinalRating(float averageScore) {
        if (averageScore >= 9) return 5; // Exceptional
        if (averageScore >= 8) return 4; // Excellent
        if (averageScore >= 7) return 3; // Good
        if (averageScore >= 6) return 2; // Needs Improvement
        return 1; // Poor
    }
    
    private String generateRemarks(Integer finalRating) {
        switch (finalRating) {
            case 5: return "Exceptional Performance";
            case 4: return "Excellent Performance";
            case 3: return "Good Performance";
            case 2: return "Needs Improvement";
            case 1: return "Poor Performance";
            default: return "Performance Evaluation Incomplete";
        }
    }
    
    public AppraisalSummaryDto convertToDto(AppraisalSummary appraisalSummary) {
        AppraisalSummaryDto dto = new AppraisalSummaryDto();
        dto.setEmployeeId(appraisalSummary.getEmployeeId());
        dto.setFinalRating(appraisalSummary.getFinalRating());
        dto.setFinalRemarks(appraisalSummary.getFinalRemarks());
        dto.setAppraisalScore(appraisalSummary.getAppraisalScore());
        dto.setAppraisalPercentage(appraisalSummary.getAppraisalPercentage());
        return dto;
    }

    public AppraisalSummary convertToEntity(AppraisalSummaryDto dto) {
        AppraisalSummary entity = new AppraisalSummary();
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setFinalRating(dto.getFinalRating());
        entity.setFinalRemarks(dto.getFinalRemarks());
        entity.setAppraisalScore(dto.getAppraisalScore());
        entity.setAppraisalPercentage(dto.getAppraisalPercentage());
        return entity;
    }
}