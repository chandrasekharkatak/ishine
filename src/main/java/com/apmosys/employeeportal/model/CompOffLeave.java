package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Component
public class CompOffLeave {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long compOffLeaveId;
	
	private Long empId;
	private Integer managerId;
	
	private Short leaveTypeMasterId;
	private Short leaveStatusId;
	
	private Integer reason;
	@Column(length = 500)
	private String description;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private Long createdBy;
	
	private LocalDate fromDate;
	private LocalDate toDate;
	private Float noOfDays;	
	private String leaveCode;
	private Long leaveStatusUpdatedBy;
	private Long hodId;
	private LocalDate approverDate;
	private String compOffStatus;
	private Long leaveId;
	
	// added by anurag for reject reason
	private String rejectCompOffReason;
	
	private Integer currentApprovalLevel;
	private Integer finalApprovalLevel;

	private String managerApprovalStatus;
	
	private Long level2ApproverId;
	private String level2ApprovalStatus;
	
	private Long reportingManagerId;

}
