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
@Table(name = "training_quiz_mapping")
public class TrainingQuizMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mapping_id")
	private Integer mappingId;
	
	@ManyToOne
	@JoinColumn(name = "training_id", nullable = false)
	private TrainingMaster trainingMaster;
	
	@ManyToOne
	@JoinColumn(name = "content_id", nullable = true)
	private TrainingContent trainingContent;
	
	@ManyToOne
	@JoinColumn(name = "survey_id", nullable = false)
	private Survey survey;
	
	@Column(name = "is_mandatory", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
	private Boolean isMandatory;
	
	@Column(name = "must_pass_to_complete", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
	private Boolean mustPassToComplete;
	
	@Column(name = "active_status", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'true'")
	private String activeStatus;
	
	@Column(name = "created_by")
	private Long createdBy;
	
	@Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
	
	@Column(name = "updated_by")
	private Long updatedBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@Column(name = "updated_on")
	private Timestamp updatedOn;
}
