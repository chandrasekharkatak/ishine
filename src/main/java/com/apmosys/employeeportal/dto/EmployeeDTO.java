package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Set;

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
	
	
	public EmployeeDTO(Long empId, String name, String jobRoleName,
            Long departmentId, Long employeementId, String billableType, String employmentId) {
this.empId = empId;
this.name = name;
this.jobRoleName = jobRoleName;
this.departmentId = departmentId;
this.employeementId = employeementId;
this.billableType = billableType;
this.employmentId = employmentId; // The CS-xxx or A-xxx value
}
	public EmployeeDTO(
		    Long empId,
		    String name,
		    String jobRoleName,
		    Long departmentId,
		    String departmentName,
		    Long jobRoleId,
		    String billableType,
		    String employmentId
		) {
		    this.empId = empId;
		    this.name = name;
		    this.jobRoleName = jobRoleName;
		    this.departmentId = departmentId;
		    this.departmentName = departmentName;
		    this.jobRoleId = jobRoleId;
		    this.billableType = billableType;
		    this.employmentId = employmentId;
		}
	 public EmployeeDTO(Long empId, Long employeementId, String email, String employmentstatus,
             Long mobileNo, Long managerId, String managerName,
             String jobRoleName, String deptName, String name,String isConsultant,String isApprenticeship,String isApmosysProduct) {
this.empId = empId;
this.employeementId = employeementId;
this.email = email;
this.employmentstatus = employmentstatus;
this.mobileNo = mobileNo;
this.managerId = managerId;
this.managerName = managerName;
this.jobRoleName = jobRoleName;
this.deptName = deptName;
this.name = name;
this.isConsultant = isConsultant;
this.isApprenticeship = isApprenticeship;
this.isApmosysProduct = isApmosysProduct;
}

	private Long empId;
	
	private Long employeementId;
	private Long draftEmpId;
	private String name;
	//added by rahul for reffredType
	private String referedType;
	private String referedName;
	//end of the code
	
	private Double performanceStatusPercentage;
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
	private String employeementIdAccToET;
	
	private String dateRange;
	
	
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
	private String poStartDate;
	private String poEndDate;
	private String poNo;
	private String poProjectType;
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
	
	private List<ProjectDTO> projectList;
	
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
    
	private String employeeConfirmationDate;
	
	private String isConsultant;
	// private String isApprenticeship;
	private String currentExperienceYear;
	private String dayOnbench;
	private String onbenchDate;
	
	//by priyadarshini
	private String isApprenticeship;
//	private List<Long> deptId;
	
    private String isRetain;
	private String dateOfRetain;
	
	private Set<String> projects;
    private Set<String> teams;
    
    private Long reporteeCountManager;
    private Long reporteeCountReportingManager;
    
    private String projectIds;
    private String selectedProjectId;
    private String hodDepartmentName;
    private String teamIds;
    private String employmentId;
    private String defaultprojectType;
    private Integer defaultProjectId;
    private String defaultProjectName;
    private Long defaultTeamId;
    private Integer isShadowResource;
    private String[] defaultTeamEmployeeRole;
    private Integer inActiveFlag;
    private Integer lowerAge;
    private Integer upperAge;
    private Long selectedResourceOverviewId;
    private Long timesheetId;
    private Long totalTimesheetsFilled;
    private String deptName;
    private List<Long> deptId;
    private Integer days;
    private String employeeType;
    
    //added
    private String isApmosysProduct;
    private String employmentIdAcToET;
    
    
    
//    findAllEmployeesWithoutAnyBillable  findAllEmployeesWithoutAnyBillableInDeptId
    public EmployeeDTO(Long empId, Long employeementId, String email, String employmentstatus,
            Long mobileNo, Long managerId, String managerName, String jobRoleName,
            String departmentName, String name, String billableType,String isConsultant,String isApprenticeship,String isApmosysProduct) {
this.empId = empId != null ? empId : null;
this.employeementId = employeementId !=null ? employeementId : null;
this.email = email !=null ? email : null;
this.employmentstatus = employmentstatus != null ? employmentstatus : null;
this.mobileNo = mobileNo != null ? mobileNo : null;
this.managerId = managerId !=null ? managerId : null;
this.managerName = managerName != null ? managerName: null;
this.jobRoleName = jobRoleName !=null ? jobRoleName : null;
this.departmentName = departmentName !=null ? departmentName : null;
this.name = name !=null ? name : null;
this.billableType = billableType !=null ? billableType : null;
this.isConsultant = isConsultant;
this.isApprenticeship = isApprenticeship;
this.isApmosysProduct = isApmosysProduct;
} 
 
    
    
	public EmployeeDTO() {
	};
	
	private String clientSideId;

    
    public EmployeeDTO(Long reportingManagerId, Long hodId, Long managerId) {
    this.reportingManagerId = reportingManagerId;
    this.hodId = hodId;
    this.managerId = managerId;
    
}
	
}
