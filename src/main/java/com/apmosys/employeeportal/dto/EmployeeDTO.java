package com.apmosys.employeeportal.dto;

import java.util.List;

import javax.persistence.Column;

import com.apmosys.employeeportal.model.Notification;

import java.time.LocalDate;	

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
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
	
	private Long teamId;
	private String teamName;
	private Long teamLeadId;
	private Integer projectId;
	private String projectName;
	private String startDate;
	private String updatedOn;
	private String teamLeadName;
	private String employeeRole;
	private String clientName;
	private String clientLocation;
	private String dateOfRelieving;
	private String reference;
	private String backgroundVerificationStatus;
	
	private Long newManagerId;
	private Long oldManagerId;
	private String isAppreciationEnable; 	
	
    private String fromDate;	
		
	private String toDate;	
		
	private Long appreciationEventId;
	private String tabName;
	private Long subFeatureId;
	private String subFeatureName;
	private Long featureId;
	private String featureName;
	private String permission;
	private String isAssigned;
	private Long tabId;
	private List<FeatureMasterDTO> permissionList;
	
	private Integer reporteeCount;
	private String hierarchyType;
//	private String eventCreatedOn;
	private List<AssetDTO> deptHeadConsentList;
	private Double profileCompletedPercent;
		
	private Long hodId;
	private String hodName;
	private String hodEmail;
	private String updatedByName;
	private String createdByName;
	
	private String isTimesheetLockCheckEnable;
	private String timesheetLockUpdatedOn;
	private Integer timesheetBackDatedDays;
	private Integer compOffLockDays;
	private Integer leaveBackdatedLockDays;
	private Integer leaveFuturedatedLockDays;
	private Integer revokeReporteeLeaveValidity;

	private Long reportingManagerId;
	private String approvalsTo;
	private String reportingManagerName;
	private String reportingManagerEmail;
	
	private Long[] specializationList;
	private Long[] domainList;
	
	private String specializationName;
	private String domainName;
	
	private Long designationId;
	private String designationName;
	
	private String bucketName;
	private String timesheetStatus;
	
	private Object policyReadConsent;
	
	private Object notificationConsent;
	private Notification releaseNoteNotification;
	private String poPortalAllProjectApi;
	
	private String unlockTimesheetFor;
	private String isTimesheetFilledByMember;
	private String isDateOfRelievingToday;
	
	private Float newBalance;
	private Float oldBalance;
	private Integer previouseCompOffExpiredCount;
	
	private Object newsletterReadCheck;
	private String employmentReleaseStatus;
	
	// added by anurag countReporties
	private Long noOfReporties;
	
//	added by anurag
	private String updateType;
	
	private Long pipId;
	private String pipReason;
	private String pipFlag;
	
	private String billableType;
	private String employeeName;
	private Long count_of_employees;
	private String reportiesFlag;
    
	private LocalDate employeeConfirmationDate;
	
	private String isConsultant;
	private String currentExperienceYear;
	private String dayOnbench;
	private String onbenchDate;
}
