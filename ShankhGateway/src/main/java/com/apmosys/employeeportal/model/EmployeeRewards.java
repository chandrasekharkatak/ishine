package com.apmosys.employeeportal.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class EmployeeRewards {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long rewardId;
//	private Long empId;
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
	private String rewardTypeName;
	private Long rewardedTo;
	private int rewardType;
	private Long managerId;
	private Long teamLeadId;
	private int isActive;
	private LocalDate fromDate;
	private LocalDate toDate;
	private Long id;
	private Long teamId;
//	@Column(name = "created_on")
//	private LocalDateTime createdOn;
//	@Column(name = "updated_by")
//	private Long updatedBy;

	
	@Column(length = 1000)
	private String remark;
	
	private String ofmonthyear;
	
	private Long rewardCategoryId;
	
	private Boolean isQuarterEnable;

}

