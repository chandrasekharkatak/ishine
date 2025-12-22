package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.GoalTemplatesDto;
import com.apmosys.employeeportal.dto.ReviewTableDto;
import com.apmosys.employeeportal.repository.ReviewRepository;
import com.apmosys.employeeportal.service.ReviewService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

	
	@Autowired
	private ReviewService reviewTableService;
	
	@PostMapping
	public ResponseEntity<ServiceResponse> createReview(@RequestBody ReviewTableDto reviewDto) {
	    ServiceResponse response = reviewTableService.createReview(reviewDto);
	    
	    HttpStatus httpStatus = response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) 
	        ? HttpStatus.OK 
	        : HttpStatus.BAD_REQUEST;
	        
	    return ResponseEntity
	        .status(httpStatus)
	        .body(response);
	}
	
    @GetMapping("/employee/{employeeId}")
    public ServiceResponse getReviewsByEmployeeId(@PathVariable Long employeeId) {
        return reviewTableService.getReviewsByEmployeeId(employeeId);
    }

    @GetMapping("/employee/{employeeId}/quarter/{quarter}")
    public ResponseEntity<?> getReviewsByEmployeeIdAndQuarter(
            @PathVariable Long employeeId,
            @PathVariable Integer quarter) {
        ServiceResponse response = reviewTableService.getReviewsByEmployeeIdAndQuarter(employeeId, quarter);
        
        HttpStatus httpStatus;
        if (response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
            // Check if response data is empty
            if (response .getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
                httpStatus = HttpStatus.NO_CONTENT; // 204 No Content
            } else {
                httpStatus = HttpStatus.OK; // 200 OK
            }
        } else {
            httpStatus = HttpStatus.BAD_REQUEST; // 400 Bad Request
        }
        
        return ResponseEntity
            .status(httpStatus)
            .body(response);
    }
    
    @GetMapping
    public ServiceResponse getAllReviews() {
        return reviewTableService.getAllReviews();
    }
}
