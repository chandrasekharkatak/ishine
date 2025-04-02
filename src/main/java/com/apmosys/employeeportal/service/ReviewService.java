package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ReviewTableDto;
import com.apmosys.employeeportal.model.QuestionnaireResponse;
import com.apmosys.employeeportal.model.ReviewTable;
import com.apmosys.employeeportal.repository.QuestionnaireResponseRepository;
import com.apmosys.employeeportal.repository.ReviewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReviewService {
	
	@Autowired
	private ReviewRepository reviewTableRepository;
	
	@Autowired
	private QuestionnaireResponseRepository questionnaireResponseRepository;
	
    public ServiceResponse createReview(ReviewTableDto reviewTableDto) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (reviewTableDto.getEmployeeId() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Employee ID is required");
                return response;
            }

            if (reviewTableDto.getQuarterId() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Quarter is required");
                return response;
            }

            if (reviewTableDto.getKpiScore() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("KPI Score is required");
                return response;
            }

           
            Float calculatedRating = calculateRatingFromQuestionnaire(
                    reviewTableDto.getEmployeeId(), 
                    reviewTableDto.getQuarterId());

            
            reviewTableDto.setRating(calculatedRating);
            
            
            if (reviewTableDto.getReviewDate() == null) {
                reviewTableDto.setReviewDate(LocalDate.now());
            }

            ReviewTable reviewTable = convertToEntity(reviewTableDto);
            

            ReviewTable savedReview = reviewTableRepository.save(reviewTable);
            
          
            ReviewTableDto savedDto = convertToDto(savedReview);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(savedDto);
            response.setServiceMessage("Review Created Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Creating Review");
        }
        return response;
    }
    
    private Float calculateRatingFromQuestionnaire(Long employeeId, Integer quarter) {
                List<QuestionnaireResponse> responses = questionnaireResponseRepository
                .findByEmpIdAndQuarter(employeeId, quarter);
        
        if (responses == null || responses.isEmpty()) {
            return 0.0f;
        }
        
        float totalScore = (float) responses.stream()
                .mapToDouble(resp -> resp.getScore() != null ? resp.getScore() : 0.0f)
                .sum();
        
        return totalScore / responses.size();
    }
    
    public ServiceResponse getAllReviews() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<ReviewTable> reviews = reviewTableRepository.findAll();
            
            List<ReviewTableDto> dtoList = reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            response.setServiceMessage("All Reviews Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Review");
        }
        return response;
    }
    
    public ServiceResponse getReviewsByEmployeeId(Long employeeId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<ReviewTable> reviews = reviewTableRepository.findByEmployeeId(employeeId);
            
            List<ReviewTableDto> dtoList = reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            if (!dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Reviews Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("No Reviews Found for Employee");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Reviews");
        }
        return response;
    }
    
    public ServiceResponse getReviewsByEmployeeIdAndQuarter(Long employeeId, Integer quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<ReviewTable> reviews = reviewTableRepository.findByEmployeeIdAndQuarter(employeeId, quarterId);
            
            List<ReviewTableDto> dtoList = reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            if (!dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Reviews Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("No Reviews Found for Employee and Quarter");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Reviews");
        }
        return response;
    }
    
    private ReviewTableDto convertToDto(ReviewTable reviewTable) {
        ReviewTableDto dto = new ReviewTableDto();
        dto.setReviewerId(reviewTable.getReviewerId());
        dto.setEmployeeId(reviewTable.getEmployeeId());
        dto.setQuarterId(reviewTable.getQuarter());
        dto.setRating(reviewTable.getRating());
        dto.setRemarks(reviewTable.getRemarks());
        dto.setReviewDate(reviewTable.getReviewDate());
        dto.setKpiScore(reviewTable.getKpiScore());
        return dto;
    }

    private ReviewTable convertToEntity(ReviewTableDto dto) {
        ReviewTable entity = new ReviewTable();
        if (dto.getReviewerId() != null) {
            entity.setReviewerId(dto.getReviewerId());
        }
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setQuarter(dto.getQuarterId());
        entity.setRating(dto.getRating());
        entity.setRemarks(dto.getRemarks());
        entity.setReviewDate(dto.getReviewDate());
        entity.setKpiScore(dto.getKpiScore());
        return entity;
    }
	

}
