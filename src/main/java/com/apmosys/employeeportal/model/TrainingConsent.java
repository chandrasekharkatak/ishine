package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "training_consent")
public class TrainingConsent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long consentId;
	
	@ManyToOne
	@JoinColumn(name = "training_id", nullable = false)
	private TrainingMaster trainingMaster;
	
	@ManyToOne
	@JoinColumn(name = "content_id", nullable = false)
	private TrainingContent trainingContent;
	
	@Column(name = "emp_id", nullable = false)
	private Long empId;
	
	@Column(name = "consent_timestamp", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private Timestamp consentTimestamp;
	
	@Column(name = "completion_cycle_number", nullable = false)
	private Integer completionCycleNumber;
	
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
}
