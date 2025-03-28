package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "question_master")
public class QuestionMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long questionMasterId;
	
	private Long entityId;
    private String entityType;
	
	private String question;
	private String description;
	
	@Column(length = 20)
	private String optionType;

	@Column(length = 1000)
	private String options;

	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String required;
	
	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String documentUpload;
	
	
	
}
