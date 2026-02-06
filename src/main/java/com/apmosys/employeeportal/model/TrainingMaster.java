package com.apmosys.employeeportal.model;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "training_master")
public class TrainingMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer trainingId;
	
	@Column(name = "training_name", nullable = false)
	private String trainingName;
	
	@Column(name = "training_type", nullable = false)
	private String trainingType;
	
	@Column(name = "mandatory_flag", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'false'")
	private String mandatoryFlag;
	
	@Column(name = "effective_from", nullable = false)
	private Date effectiveFrom;
	
	@Column(name = "effective_to")
	private Date effectiveTo;
	
//	@Column(name = "frequency_per_year", nullable = false, columnDefinition = "INT DEFAULT 2")
//	private Integer frequencyPerYear;
	
	@Column(name = "lock_enabled", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'false'")
	private String lockEnabled;
	
	@Column(name = "min_view_time_minutes")
	private Integer minViewTimeMinutes;
	
	@Column(name = "consent_required", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'true'")
	private String consentRequired;
	
	@Column(name = "skip_allowed", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'true'")
	private String skipAllowed;
	
	@Column(name = "deadline_enabled", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'false'")
	private String deadlineEnabled;
	
	@Column(name = "deadline_pattern")
	private String deadlinePattern;
	
	@Column(name = "custom_deadline_months")
	private String customDeadlineMonths;
	
	@Column(name = "active_status", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'true'")
	private String activeStatus;
	
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
	
	@Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
	
	@Column(name = "updated_by")
	private Long updatedBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@Column(name = "updated_on")
	private Timestamp updatedOn;
}
