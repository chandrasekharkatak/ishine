package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContributionResponseRemarks {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long responeRemarkId;
	private Long remarkBy;
	private String remark;
	private Long userContributionId;
	
	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
	
}
