package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Main Timesheet DTO - Wrapper for hierarchical timesheet structure.
 * 
 * NEW STRUCTURE (Hierarchical):
 * - employeeTimesheet: One per day per employee
 * - projectTimesheets: Multiple per day (one per project)
 *   - activities: Multiple per project (nested)
 * 
 * OLD STRUCTURE (Flat - maintained for backward compatibility):
 * - All existing fields remain for backward compatibility
 * 
 * @author System
 * @version 2.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TimesheetDTO {
	
	// ========== NEW HIERARCHICAL STRUCTURE ==========
	
	/**
	 * Employee-level timesheet data (one per day)
	 * NEW: Primary structure for timesheet creation/update
	 */
	@JsonProperty("employeeTimesheet")
	private EmployeeTimesheetDTO employeeTimesheet;
	
	/**
	 * Project-level timesheet data (multiple per day)
	 * NEW: Contains project-specific data and activities
	 */
	@JsonProperty("projectTimesheets")
	private List<ProjectTimesheetDTO> projectTimesheets;
	
	// ========== OLD STRUCTURE (Backward Compatibility) ==========
	// These fields are maintained for backward compatibility with existing APIs
	// Will be deprecated gradually
	
	private Integer projectId;
	private Integer clientId;
	private String clientName;
	private Integer clientLocationId;
	private String clientLocation;
	private String state;
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long managerId;
	private Long empId;
	private String approvedOn;
	private String status;
	private Long timesheetId;
	private String date;
	private String dayType;
	private String employeeName;
	private String createdByName;
	private Long CreatedByEmpId;
	private String createdOn;
	private String startDate;
	private String endDate;
	private Long createdBy;
	private List<ActivityDTO> allTimesheetActivities;
	private List<ActivityDTO> updatedTimesheetActivities;
    private Long activityTimesheetId;
	private Long employeementId;

	private Long applicationCount;

	private String weekDayName;
	private Float totalWorkingHours;

	private Float totalTime;

	private Long timesheetStatusUpdatedBy;
	private String timesheetStatusUpdatedByName;
	private String updatedOn;

	private Long teamId;
	private String activity;

	private String departmentName;
	private String email;
	private Long mobileNo;
	private String managerName;
	private Long pendingEodCount;
	private String legend;
	private String rejectReason;
	private List<CustomFilterDTO> queryList;
	private List<CustomFilterDTO> queryList1;

	private String employmentstatus;
	private List<TimesheetDTO> bulkApprovedList;
	private List<TimesheetDTO> bulkRejectList;
	
	private String remarks;
	private String teamName;
	private Long activityId;
	
	//project
	private String active;

	private String officeInTime;
	private String officeOutTime;
	private String totalWorkingOfficeHours; //<-- totalWorkingHours in Timesheet Model
	private String isNightShift;
	private String managerEmail;
	
	private String isCron; //<-- for allEmployee DSR report cronJob
	private Integer year;
	private String month;
	private Long actualEODCount;
	private Long resourceCount;
	private Long expectedEODCount;
	private Long currentManagerId;
	
	private String leaveType;
	private List<ActivityDTO> inactiveTimesheetActivities;
	private String isConsultant;
	private String isApprenticeship;
	
	private List<EmployeeTimesheetDto> timeSheet;
	
	private String TeamLeadName;

	private Long currentUser;

	 private String isApmosysProduct;
	 private String employmentIdAcToET;

	private String clientSideId;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime clientInTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime clientOutTime;
	
	private Boolean isShadowTimesheet;
	
	private Boolean escalationFlag;
	
	private Integer currentEscalationLevel;
	
	private String totalClientWorkingHours;
	
	private String clientApprovalStatus;

	private Boolean hasClientSideId;
	
	private String employmentId;
	
	private Long shadowEmpId;
	 
	private List<TimesheetDocumentDetailsDTO> documentData;
	private TimesheetDocumentDetails documentEntityData;
	
	private Long docId;
	private String fromDate;

	private String toDate;
	private Long allocId;
	private Integer previousLevelId;
	private Long previousApproverId;
	private Integer rejectionId;
	private Integer levelId;
	private Integer submittedCount;
	private Integer ClientPendingCount;
	private Integer ClientApprovedCount;
	private Long hodId;
	private Long rmId;
	private Integer rejectedhierarchyOrder;
	private String isSearch;
	private List<TimesheetDTO> pendingApprovalList;
	private Long filledDocument;
	private Long approvedDocument;
	private Integer month1;
	private Long updatedBy;
	
	private Integer page;
	private Integer size;
	private Boolean isClientDashboard;
	private Boolean dataForExcel;
	private ColumnFilterDTO columnFilter;
	private String searchKey; 
	private String statusUpdatedBy;
	private Map<String, String> filters;
	private Long filledTimesheetCount;
//	private String billableType;
	private List<String> billableTypes;
	private String projectActive;
	private String sortBy;
	private List<String> sortByForTimesheetLeaveReport;
	private String sortDirection;
	private Boolean exportAll;
	private String sort;
	private String field;
	private List<String> sortColumn;
	private Integer totalEmployees;
	private Boolean client;	

	private String shadowFor;
	private Long bulkApprovedDocId;
	private List<Long> empIds;
	
	// ========== CONSTRUCTORS (Backward Compatibility) ==========
	
	public TimesheetDTO(
			Long employeementId,
			String employeeName,
			LocalDate date,
			String dayType,
			String description,
			String status,
			String leaveType,
			String managerName,
			String departmentName,
			Timestamp createdOn,
			LocalDateTime updatedOn,
			String statusUpdatedBy,
			String isConsultant,
			String isApprenticeship,
			Long managerId,
			Long timesheetStatusUpdatedBy,
			Long empId,
			String isApmosysProduct) {

		this.employeementId = employeementId;
		this.employeeName = employeeName;
		this.date = (date != null) ? date.toString() : null;
		this.dayType = dayType;
		this.description = description;
		this.status = status;
		this.leaveType = leaveType;
		this.managerName = managerName;
		this.departmentName = departmentName;
		this.createdOn = (createdOn != null) ? createdOn.toString() : null;
		this.updatedOn = (updatedOn != null) ? updatedOn.toString() : null;
		this.statusUpdatedBy = statusUpdatedBy;
		this.isConsultant = isConsultant;
		this.isApprenticeship = isApprenticeship;
		this.managerId = managerId;
		this.timesheetStatusUpdatedBy = timesheetStatusUpdatedBy;
		this.empId = empId;
		this.isApmosysProduct = isApmosysProduct;
	}

	public TimesheetDTO(
			Long employeementId,
			String employeeName,
			LocalDate date,
			String dayType,
			String description,
			String status,
			String leaveType,
			String managerName,
			String departmentName,
			Date createdOn,
			LocalDateTime updatedOn,
			String statusUpdatedBy,
			String isConsultant,
			String isApprenticeship,
			Long managerId,
			Long timesheetStatusUpdatedBy,
			Long empId,
			String isApmosysProduct) {

		this.employeementId = employeementId;
		this.employeeName = employeeName;
		this.date = (date != null) ? date.toString() : null;
		this.dayType = dayType;
		this.description = description;
		this.status = status;
		this.leaveType = leaveType;
		this.managerName = managerName;
		this.departmentName = departmentName;
		this.createdOn = (createdOn != null) ? createdOn.toString() : null;
		this.updatedOn = (updatedOn != null) ? updatedOn.toString() : null;
		this.statusUpdatedBy = statusUpdatedBy;
		this.isConsultant = isConsultant;
		this.isApprenticeship = isApprenticeship;
		this.managerId = managerId;
		this.timesheetStatusUpdatedBy = timesheetStatusUpdatedBy;
		this.empId = empId;
		this.isApmosysProduct = isApmosysProduct;
	}

	public TimesheetDTO(
			Long employeementId,
			String employeeName,
			LocalDate date,
			String dayType,
			String description,
			String status,
			String leaveType,
			String managerName,
			String departmentName,
			Date createdOn,
			LocalDateTime updatedOn,
			String statusUpdatedBy,
			String isConsultant,
			String isApprenticeship,
			Long managerId,
			Long timesheetStatusUpdatedBy,
			Long empId) {

		this.employeementId = employeementId;
		this.employeeName = employeeName;
		this.date = (date != null) ? date.toString() : null;
		this.dayType = dayType;
		this.description = description;
		this.status = status;
		this.leaveType = leaveType;
		this.managerName = managerName;
		this.departmentName = departmentName;
		this.createdOn = (createdOn != null) ? createdOn.toString() : null;
		this.updatedOn = (updatedOn != null) ? updatedOn.toString() : null;
		this.statusUpdatedBy = statusUpdatedBy;
		this.isConsultant = isConsultant;
		this.isApprenticeship = isApprenticeship;
		this.managerId = managerId;
		this.timesheetStatusUpdatedBy = timesheetStatusUpdatedBy;
		this.empId = empId;
	}
	
	public TimesheetDTO(Long empId,String employeeName) {
		this.empId = empId;
		this.employeeName = employeeName;
	}

	
	public TimesheetDTO(
		    Long timesheetId,              
		    LocalDate date,                 
		    String dayType,                 
		    String employeeName,            
		    String description,             
		    String status,                 
		    String createdByName,          
		    Long createdByEmpId,            
		    String createdOn,               
		    Long employeementId,            
		    Float totalTime,                
		    String email,                   
		    LocalDateTime officeInTime,    
		    LocalDateTime officeOutTime,    
		    String totalWorkingHours,       
		    String isNightShift,           
		    Long currentManagerId,          
		    String isConsultant,           
		    String isApprenticeship,        
		    Long empId                      
		) {
		    this.timesheetId = timesheetId;
		    this.date = date.toString();    
		    this.dayType = dayType;
		    this.employeeName = employeeName;
		    this.description = description;
		    this.status = status;
		    this.createdByName = createdByName;
		    this.CreatedByEmpId = createdByEmpId;
		    this.createdOn = createdOn;
		    this.employeementId = employeementId;
		    this.totalTime = totalTime;
		    this.email = email;
		    this.officeInTime = officeInTime.toString();    
		    this.officeOutTime = officeOutTime.toString();  
		    this.totalWorkingHours = Float.parseFloat(totalWorkingHours); 
		    this.isNightShift = isNightShift;
		    this.currentManagerId = currentManagerId;
		    this.isConsultant = isConsultant;
		    this.isApprenticeship = isApprenticeship;
		    this.empId = empId;
		}
	
	// ========== NEW CONSTRUCTOR FOR HIERARCHICAL STRUCTURE ==========
	
	/**
	 * Constructor for new hierarchical structure
	 */
	public TimesheetDTO(EmployeeTimesheetDTO employeeTimesheet, List<ProjectTimesheetDTO> projectTimesheets) {
		this.employeeTimesheet = employeeTimesheet;
		this.projectTimesheets = projectTimesheets;
	}
	
	/**
	 * Constructor for JPQL query using EmployeeTimesheetsNew entity
	 * Matches query: getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise
	 * Parameters: employeementId, name, date, dayType, description, status, leaveType, 
	 *             managerName, departmentName, createdOn (LocalDateTime), updatedOn, 
	 *             statusUpdatedBy (name), isConsultant, isApprenticeship, managerId, 
	 *             updatedBy (ID), empId, isApmosysProduct
	 */
	public TimesheetDTO(
			Long employeementId,
			String employeeName,
			LocalDate date,
			String dayType,
			String description,
			String status,
			String leaveType,
			String managerName,
			String departmentName,
			LocalDateTime createdOn,
			LocalDateTime updatedOn,
			String statusUpdatedBy,
			String isConsultant,
			String isApprenticeship,
			Long managerId,
			Long updatedBy,
			Long empId,
			String isApmosysProduct) {

		this.employeementId = employeementId;
		this.employeeName = employeeName;
		this.date = (date != null) ? date.toString() : null;
		this.dayType = dayType;
		this.description = description;
		this.status = status;
		this.leaveType = leaveType;
		this.managerName = managerName;
		this.departmentName = departmentName;
		this.createdOn = (createdOn != null) ? createdOn.toString() : null;
		this.updatedOn = (updatedOn != null) ? updatedOn.toString() : null;
		this.statusUpdatedBy = statusUpdatedBy;
		this.isConsultant = isConsultant;
		this.isApprenticeship = isApprenticeship;
		this.managerId = managerId;
		this.updatedBy = updatedBy;
		this.timesheetStatusUpdatedBy = updatedBy; // Also set for backward compatibility
		this.empId = empId;
		this.isApmosysProduct = isApmosysProduct;
	}
}
