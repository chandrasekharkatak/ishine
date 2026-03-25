package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CertificateDocumentMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long docId;
	
	private Long empId;
	
	@Lob
	private byte[] docData;
	
	private String docMimeType;
	
	private String docName;
	
	private Boolean dActive;
	
	 @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
		private Timestamp doccreatedOn;
		
		private Long doccreatedBy;
		
		
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
		private LocalDateTime docupdatedOn;
		
		private Long docupdatedBy;	

}
