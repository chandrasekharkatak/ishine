package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "timesheet_rejection_reasons_master")
@ToString
public class TimesheetRejectionReasonsMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long rejectionId;
	private String rejectionReason;
	private Boolean active;
	private Long createdBy;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdOn;
	private Long updatedBy;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedOn;
	
}
