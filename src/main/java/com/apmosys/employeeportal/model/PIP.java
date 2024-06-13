package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PIP {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pipId;
	private String pipReason;
	private String revReason;
	private Long empId;
	private LocalDateTime createdOn;
	private boolean pipFlag;
	private String createdBy;
	private String updatedBy;
	private LocalDateTime updatedOn;
	
	private String extendDays;
	private String startDate;
	private String endDate;
	private Long aging;
	private String extendReason;
}
