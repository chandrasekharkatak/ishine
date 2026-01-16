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
@AllArgsConstructor
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

}