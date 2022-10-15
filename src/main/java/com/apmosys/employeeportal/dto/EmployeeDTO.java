package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeDTO {

	private Long empId;
	private Long employeementId;
	private Long draftEmpId;
	private String name;
	private Long managerId;
	private String dateOfJoining;
	private String dateOfBirth;
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
	private String address;
	private String city;
	private String state;
	private String country;
	private Integer pincode;
	private Long alternateMobileNo;
	private String permanentAddress;
	private String emergencyContactPerson;
	private String relation;
	private Long emergencyContactMobile;
	private String employmentstatus;
	private Short noticePeriod;
	private Long jobRoleId;
	private String bankName;
	private String bankAccountNo;
	private String bankIFSCCode;
	private String pfAccountNumber;
	private String previousPfAccountNumber;
	private String uan;
	private String esicNumber;
	private String aboutMe;
	private String viewsOnOrganisation;
	private String password;
	private Integer otp;
	private String newPassword;
	private byte[] imageBytes;
	private Long userTypeId;
	private String managerName;
	private String jobRoleName;
	private Long departmentId;
	private String departmentName;
	private Integer createdBy;
	private String createdOn;
	private String graduationType;
	private String pursuing;
	private Short yearOfPassing;
	private String passingGrade;
	private List<EmployeeCertificateDTO> certifications;
	private List<PreviousEmploymentDTO> previousEmploymentList;
	private String experience;
	private String role;
	private List<DepartmentDTO> departmentList;

	private String workLocation;
	private Integer timesheetLockDays;

	private List<EmployeeCertificateDTO> updatedCertifications;
	private List<PreviousEmploymentDTO> updatedPreviousEmploymentList;
	private String isDraft;

	private String sessionString;

	private Integer invalidAccessAttempt;
	private Integer failedAttempt;
	private List<CustomFilterDTO> queryList;

	private String mothersName;
	private String spouse;
	private String child1;
	private String child2;
	private String child3;
	private Float totalExperience;
	private String billable;

	private String managerEmail;

	private String isNew;
	private String secondaryEmail;

	private String updateApplicationStatus;
	private Short probationPeriod;
	private String dateOfResign;

	private String remarks;
	private Long updatedBy;

	private List<EmployeeDocumentDTO> documentList;
	private String isUserInfoUpdated;

	// appreciation
	private String appreciateType;
	private String reason;
	private Long empIdAppreciated;
	// name
	// private String name;
	private String nameAppreciate;
	// MAILID
	// private String email;
	private String emailAppreciated;
	// employementID
	private Long appreciationBy;
	private Long appreciationTo;

	private String managerMail;
	
	private String updateChild;
}
