package com.apmosys.employeeportal.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@ToString
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
	
	//added by rahul employee refred
	@Column(length =50, nullable = true)
	private String referedType;
	@Column(length =100, nullable = true)
	private String referedName;
	
	//end 

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
	
	@NotAudited
	private String password;
	
	@NotAudited
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
	@NotAudited
	private Integer invalidAccessAttempt;
	private Short probationPeriod;
	

	private String isNew;
	
	private String secondaryEmail;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfResign;
	
	private String isUserInfoUpdated;

	
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
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private String dateOfRelieving;
	private String reference;
	private String backgroundVerificationStatus;
	
	@NotAudited
	@Column(columnDefinition = "varchar(10) DEFAULT 'False'")
	private String isAppreciationEnable; //added for appreciation by suchi
	
	@NotAudited
	@Column(columnDefinition = "varchar(10) DEFAULT 'true'")
	private String isTimesheetLockCheckEnable;
	
	@NotAudited
	private LocalDate timesheetLockUpdatedOn;
	
	private Long reportingManagerId;
	
	private String approvalsTo;

	@NotAudited
	private LocalDateTime otpUpdatedOn;
	
	private Long designationId;
	
	private String employmentReleaseStatus;
	
	private boolean pipFlag = false;
	private Long pipId;
	
	private String billableType;
	
	@Column(name = "employee_confirmation_date")
    private LocalDate employeeConfirmationDate;
	
	
	@Column(name = "is_consultant")
	private String isConsultant;

	private String onbenchDate;
	@Column(name = "is_apprenticeship")
	private String isApprenticeship;
	
	@Column(name = "is_apmosys_product", length = 10)
	private String isApmosysProduct;
	
	
    private String isRetain;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfRetain;
	
	@Column(name = "extended_period")
	private Short extendedPeriod;
	
   private String reasonOfExtension;//reason_of_extension	
   
   private Long isConfirmedClicked;
   
   private boolean longOverdueNotified;
   
//   @Column(name = "reason_for_reduction")
//   private String reasonForReduction;
   
   private Long isExtensionClicked;
   
}
