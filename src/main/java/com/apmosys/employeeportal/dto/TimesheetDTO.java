package com.apmosys.employeeportal.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimesheetDTO {

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

}
