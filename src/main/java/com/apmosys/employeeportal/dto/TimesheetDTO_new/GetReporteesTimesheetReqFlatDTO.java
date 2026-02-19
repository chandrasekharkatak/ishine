package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
//@AllArgsConstructor
@NoArgsConstructor
public class GetReporteesTimesheetReqFlatDTO {
	
	private Long timesheetId;
    private Long empId;
    private String employmentId;
    private String employeeName;
    private String dayType;
    private LocalDate date;
    private Boolean isNightShift;
    private LocalDateTime workCheckIn;
    private LocalDateTime workCheckOut;
    private Long projectCount;
    private Long locationCount;
    private String appliedBy;
    private LocalDateTime appliedOn;

    private String workLocationType;
    private LocalDateTime locationInTime;
    private LocalDateTime locationOutTime;
    private Long locationMappingId;

    private Integer projectId;
    private String projectName;
    private String clientName;
    private String clientLocation;
    private String poNo;
    private String shadowEmp;
    private Integer status;
    private Integer totalClientWorkingMinutes;
    private String description;
    
    private String activity;
    private String activityDescription;
    private Short durationMinutes;
    private String teamName;
    
    private Long docId;
    private String docName;
    private Boolean finalFlag;
    private Long bulkApprovedDocId;
    private String mimeType;
    private Integer docsProjectId;


	public GetReporteesTimesheetReqFlatDTO(Long timesheetId, Long empId, String employmentId, String employeeName,
			String dayType, LocalDate date, Boolean isNightShift, LocalDateTime workCheckIn, LocalDateTime workCheckOut,
			Long projectCount, Long locationCount, String appliedBy, LocalDateTime appliedOn, String workLocationType,
			LocalDateTime locationInTime, LocalDateTime locationOutTime, Long locationMappingId, Integer projectId,
			String projectName, String clientName, String clientLocation, String poNo, String shadowEmp, Integer status,
			Integer totalClientWorkingMinutes, String description, String activity, String activityDescription,
			Short durationMinutes, String teamName, Long docId, String docName, Boolean finalFlag,
			Long bulkApprovedDocId, String mimeType, Integer docsProjectId) {
		super();
		this.timesheetId = timesheetId;
		this.empId = empId;
		this.employmentId = employmentId;
		this.employeeName = employeeName;
		this.dayType = dayType;
		this.date = date;
		this.isNightShift = isNightShift;
		this.workCheckIn = workCheckIn;
		this.workCheckOut = workCheckOut;
		this.projectCount = projectCount;
		this.locationCount = locationCount;
		this.appliedBy = appliedBy;
		this.appliedOn = appliedOn;
		this.workLocationType = workLocationType;
		this.locationInTime = locationInTime;
		this.locationOutTime = locationOutTime;
		this.locationMappingId = locationMappingId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.clientName = clientName;
		this.clientLocation = clientLocation;
		this.poNo = poNo;
		this.shadowEmp = shadowEmp;
		this.status = status;
		this.totalClientWorkingMinutes = totalClientWorkingMinutes;
		this.description = description;
		this.activity = activity;
		this.activityDescription = activityDescription;
		this.durationMinutes = durationMinutes;
		this.teamName = teamName;
		this.docId = docId;
		this.docName = docName;
		this.finalFlag = finalFlag;
		this.bulkApprovedDocId = bulkApprovedDocId;
		this.mimeType = mimeType;
		this.docsProjectId = docsProjectId;
	}
	

	public GetReporteesTimesheetReqFlatDTO(Long timesheetId, Long empId, String employmentId, String employeeName,
			String dayType, LocalDate date, Boolean isNightShift, LocalDateTime workCheckIn, LocalDateTime workCheckOut,
			Long projectCount, Long locationCount, String appliedBy, LocalDateTime appliedOn, String workLocationType,
			LocalDateTime locationInTime, LocalDateTime locationOutTime, Long locationMappingId, Integer projectId,
			String projectName, String clientName, String clientLocation, String poNo, String shadowEmp, Integer status,
			Integer totalClientWorkingMinutes, String description, String activity, String activityDescription,
			Short durationMinutes, String teamName, Long docId, String docName, Boolean finalFlag,
			Long bulkApprovedDocId, String mimeType) {
		super();
		this.timesheetId = timesheetId;
		this.empId = empId;
		this.employmentId = employmentId;
		this.employeeName = employeeName;
		this.dayType = dayType;
		this.date = date;
		this.isNightShift = isNightShift;
		this.workCheckIn = workCheckIn;
		this.workCheckOut = workCheckOut;
		this.projectCount = projectCount;
		this.locationCount = locationCount;
		this.appliedBy = appliedBy;
		this.appliedOn = appliedOn;
		this.workLocationType = workLocationType;
		this.locationInTime = locationInTime;
		this.locationOutTime = locationOutTime;
		this.locationMappingId = locationMappingId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.clientName = clientName;
		this.clientLocation = clientLocation;
		this.poNo = poNo;
		this.shadowEmp = shadowEmp;
		this.status = status;
		this.totalClientWorkingMinutes = totalClientWorkingMinutes;
		this.description = description;
		this.activity = activity;
		this.activityDescription = activityDescription;
		this.durationMinutes = durationMinutes;
		this.teamName = teamName;
		this.docId = docId;
		this.docName = docName;
		this.finalFlag = finalFlag;
		this.bulkApprovedDocId = bulkApprovedDocId;
		this.mimeType = mimeType;
	}
    
    
}