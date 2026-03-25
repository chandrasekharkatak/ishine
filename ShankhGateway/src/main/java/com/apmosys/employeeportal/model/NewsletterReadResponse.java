package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

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
@Table(name = "employee_document_read_responses")
public class NewsletterReadResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long documentReadResponseId;
	
	private Long documentId;
	private Long empId;
	
	@Column(name = "readOn",columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
}
