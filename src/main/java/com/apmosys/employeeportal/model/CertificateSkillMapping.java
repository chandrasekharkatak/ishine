package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Audited
public class CertificateSkillMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long certificateSKillId;
	private Long employeeCertificateId;
	private Long skillId;
	private String additionalSkill;
	private Long proficiencyId;
	private Long empId;
	private Boolean scActive;
	 @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
		private Timestamp cscreatedOn;
		
		private Long cscreatedBy;
		
		
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
		private LocalDateTime csupdatedOn;
		
		private Long csupdatedBy;	

}
