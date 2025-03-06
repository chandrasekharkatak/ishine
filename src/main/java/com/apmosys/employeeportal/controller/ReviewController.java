package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	public ServiceResponse createReview(@RequestBody ReviewTableDto reviewDto) {
		return reviewTableService.createReview(reviewDto);
	}
	
	
    @GetMapping("/employee/{employeeId}")
    public ServiceResponse getReviewsByEmployeeId(@PathVariable Long employeeId) {
        return reviewTableService.getReviewsByEmployeeId(employeeId);
    }

    @GetMapping("/employee/{employeeId}/quarter/{quarter}")
    public ServiceResponse getReviewsByEmployeeIdAndQuarter(
            @PathVariable Long employeeId,
            @PathVariable Integer quarter) {
        return reviewTableService.getReviewsByEmployeeIdAndQuarter(employeeId, quarter);
    }
    
    @GetMapping
    public ServiceResponse getAllReviews() {
        return reviewTableService.getAllReviews();
    }
}
