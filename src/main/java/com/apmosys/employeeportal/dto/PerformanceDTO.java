package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.apmosys.employeeportal.model.Notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@Setter
@Getter
public class PerformanceDTO {
	private Long employeePerformanceId;
    private String completionStatus;
    private Long empId;
    private String finalRating;
    private String hodApprovalDate;
    private String hodRemarks;
    private Long hodId;
    private Long quarterId;
    private String quarterCycle;
    private List<PerformanceRatingDTO> performanceRatings;
    private String financialYear;
    private String currentStatus;
    private String reviewLabel;
    private String reviewFieldType;
    private String condition;
    private Long deptId;
    private Long reviewTypeId;
    private BigDecimal ratingValue;
    private Long hrId;
    private String hrRemark;
    private LocalDate hrReviewDate;
    private String hrReviewStatus;
    /** Manager submission status for list (Submitted/Pending). */
    private String managerReviewStatus;
    /** HOD submission status for list (Submitted/Pending). */
    private String hodReviewStatus;
    private Long performanceRatingId;
    private String isUserHaveTeam;
    private String employeeRole;
    private Long departmentId;
    private String tabType;
    private Boolean rejectStatus;
    private String actionBy;
    
}
