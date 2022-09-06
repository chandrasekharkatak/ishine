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
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long empId;
	private Long employeementId;

	private String name;

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfBirth;
	private String email;
	private String gender;
	private String bloodGroup;
	private String maritalStatus;

	private String fatherName;
	private String placeOfBirth;
	private String motherTongue;
	private String passportNumber;
	private Long aadhar;
	private String panNumber;

	private Long mobileNo;
	private Long landline;
	@Column(length = 1000)
	private String address;
	private String city;
	private String state;
	private String country;
	private Integer pincode;
	private Long alternateMobileNo;
	@Column(length = 1000)
	private String permanentAddress;
	private String emergencyContactPerson;
	private String relation;
	private Long emergencyContactMobile;

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfJoining;
	private String employmentstatus;
	private Short noticePeriod;


	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	//@Column(columnDefinition="TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP")
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private Integer createdBy;	
	private Integer updatedBy;	
	private Long managerId;
	
//	@OneToOne
//	@JoinColumn(name = "jobRoleId")
	private Long jobRoleId;
	
	private String bankName;
	private String bankAccountNo;
	private String bankIFSCCode;
	private String pfAccountNumber;
	private String previousPfAccountNumber;
	private String uan;
	private String esicNumber;
	
	@Column(columnDefinition="varchar(1000) DEFAULT 'Add about yourself.'")
	private String aboutMe;
	@Column(columnDefinition="varchar(1000) DEFAULT 'Add your views.'")
	private String viewsOnOrganisation;	
	
	private String password;
	private Integer otp;
	private String profileImageName;
	
	
	private String graduationType;
	private String pursuing;
	private Short yearOfPassing;
	private String passingGrade;
	
	private String experience;
	
	private String role;
	private String workLocation;
	
	@Column(nullable = false)
	private Integer invalidAccessAttempt;
	
	
}
