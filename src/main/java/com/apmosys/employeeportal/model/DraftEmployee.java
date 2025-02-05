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



@Entity
@Getter
@Setter
public class DraftEmployee {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long draftEmpId;

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
	private Short probationPeriod;
	//added by rahul for reffredType
		private String referedType;
		private String referedName;
		//end of the code
		

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
	
	private String graduationType;
	private String pursuing;
	private Short yearOfPassing;
	private String passingGrade;
	
	@Column(columnDefinition="varchar(1000) DEFAULT 'Add about yourself.'")
	private String aboutMe;
	@Column(columnDefinition="varchar(1000) DEFAULT 'Add your views.'")	
	private String viewsOnOrganisation;
	
	private String experience;
	
	private String role;
	private String workLocation;
	private String updateApplicationStatus;
	
	@Column(length = 50)
	private String mothersName;
	@Column(length = 50)
	private String spouse;	
	@Column(length = 50)
	private String child1;
	@Column(length = 50)
	private String child2;
	@Column(length = 50)
	private String child3;
	@Column(columnDefinition = "float DEFAULT NULL")
	private Float totalExperience;
	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String billable;
	private Long reportingManagerId;
	private String approvalsTo;
	
	private Long designationId;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfResign;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private String dateOfRelieving;
	
	private String isConsultant;
	private String isApprenticeship;
	
}
