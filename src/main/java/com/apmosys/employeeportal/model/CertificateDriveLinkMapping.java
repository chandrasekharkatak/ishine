package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CertificateDriveLinkMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long driveId;
	
	private Long empId;
	
	private String driveLink;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp drcreatedOn;
	
	private Long drcreatedBy; 
	
	private Boolean drActive;
}
