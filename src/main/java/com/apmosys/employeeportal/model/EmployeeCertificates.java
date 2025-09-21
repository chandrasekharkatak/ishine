package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDate;
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
public class EmployeeCertificates {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeCertificateId;
	private Long empId;
	private String certificateName;
	private String specialization;
	private Long deptId;
	private Long proficiencyId;
	private String issuingAuthority;
	private LocalDate validFrom;
	private LocalDate expiresOn;
	private Boolean cActive;
	private Long docId;
	private String certificateStatus;
	 @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
		private Timestamp createdOn;
		
		private Long createdBy;
		
		
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
		private LocalDateTime updatedOn;
		
		private Long updatedBy;	
	
	
	
  
}
