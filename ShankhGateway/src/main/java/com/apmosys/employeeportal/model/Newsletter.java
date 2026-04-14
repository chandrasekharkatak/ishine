package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
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
@Table(name="documents")
public class Newsletter {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long documentId;
	
	private String displayName;
	
	private String fileName;
	
	private String type;

	private String readEnabled;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	private Integer createdBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private Integer updatedBy;
	
	private Long typeId;
	
}
