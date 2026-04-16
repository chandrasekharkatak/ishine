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
@Table(name = "training_skip")
public class TrainingSkip {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long skipId;
	
	@ManyToOne
	@JoinColumn(name = "training_id", nullable = false)
	private TrainingMaster trainingMaster;

	@Column(name = "quiz_id")
	private Long quizId;
	
	@Column(name = "emp_id", nullable = false)
	private Long empId;
	
	@Column(name = "cycle_number", nullable = false)
	private Integer cycleNumber;
	
	@Column(name = "skip_count", nullable = false, columnDefinition = "INT DEFAULT 1")
	private Integer skipCount;
	
	@Column(name = "first_skipped_on", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private Timestamp firstSkippedOn;
	
	@Column(name = "last_skipped_on", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private Timestamp lastSkippedOn;
}
