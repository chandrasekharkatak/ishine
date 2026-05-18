package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class PerformanceRatingDTO {
	
	private Long reviewTypeId;
    private BigDecimal rating;
    private String reviewLabel;
    private Long quarterId;
    private Long performanceRatingId;
    /** Per-criterion comment (employee_rating_performance.criteria_remark). */
    private String criteriaRemark;

}
