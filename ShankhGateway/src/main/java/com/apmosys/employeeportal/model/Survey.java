package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
@Table(name = "surveys")
public class Survey {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyId;
	
	private String surveyName;
	
	@Column(columnDefinition = "varchar(10) DEFAULT 'Y'")
	private String isActive;
	
	private String description;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	private Long createdBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;	

	@Column(name = "cut_off_questions")
	private Integer cutOffQuestions;
		
	private Long updatedBy;
	
	private String type;
	private String imageUrl;
    private String videoUrl;

}
