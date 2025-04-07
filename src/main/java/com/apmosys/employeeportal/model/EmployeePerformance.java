package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class EmployeePerformance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employee_performance_id;
	private Long quarterId;
	private String quarter_period;
	private String completion_status;
	private Long emp_id;
	private String final_rating;
	private Long manager_id;
	private String manager_remarks;
	private LocalDateTime manager_review_date;
	private Long hod_id;
	private LocalDateTime hod_approval_date;
	private LocalDateTime hod_rejected_date;
	private String hod_remarks;
	private Long hr_id;
	private LocalDateTime hr_review_date;
	private String hr_review_status;
	private String hr_remarks;
	@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean rejectStatus = false;
	
	 @PrePersist
	    public void prePersist() {
	        if (rejectStatus == null) {
	            rejectStatus = false;  
	        }
	    }
	
	
//	@OneToMany
//	private ReviewData quarter_master_id;
	
	
}
