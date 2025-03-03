package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class BiomaxRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long biomaxreequestId;
	private Long biomaxTitle;
	private Long empId;
	private Long reportingManagerId;
	private LocalDateTime biomaxrequestDate;
	private String biomaxStatus;
	private LocalDateTime createdOn;
	private Long statusBy;
	private String requestRemark;
	private LocalDateTime statusDate;
	private boolean isEnabled;
	
	
}
