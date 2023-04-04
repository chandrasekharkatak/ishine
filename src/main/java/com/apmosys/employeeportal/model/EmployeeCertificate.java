package com.apmosys.employeeportal.model;

import java.time.LocalDate;

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
@Setter
@Getter
@Audited
public class EmployeeCertificate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeCertificateId;

	private String certificationName;
	
	private String duration;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfCompletion;
	
	private String modeOfCourse;
	
	private String certificationNumber;
	
	private Long empId;	
	
	private String isDraft;
}
