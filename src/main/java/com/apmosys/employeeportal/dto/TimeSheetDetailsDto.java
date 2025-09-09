package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetDetailsDto {
	
	private Long timesheet_id;
	private Integer project_id;
	private Long teamId;
	private Long emp_id;
	private String dayType;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;
	
	private Float totalTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime apmosysStartTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime apmosysEndTime;
	
	private String totalWorkingHours; // FROM IN-OUT Time
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime clientStartTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime clientEndTime;
	
	private Boolean isShadowTimesheet;
	private String totalClientWorkingHours;
	private Integer projectId;
	private Boolean hasClientSideId;
	private Long shadowFor;
	private Long docId;
	private String shadowEmployeeName;
	private List<TimesheetDocumentDetailsDTO> docData;
	
	public TimeSheetDetailsDto(
	        Long timesheet_id,
	        Integer project_id,
	        Long teamId,
	        Long emp_id,
	        String dayType,
	        LocalDate date) {
	    this.timesheet_id = timesheet_id;
	    this.project_id = project_id;
	    this.teamId = teamId;
	    this.emp_id = emp_id;
	    this.dayType = dayType;
	    this.date = date;
	}
	
	public TimeSheetDetailsDto(
	        Long timesheet_id,
	        Integer project_id,
	        Long teamId,
	        Long emp_id,
	        String dayType,
	        LocalDate date,LocalDateTime officeInTime,
	        LocalDateTime officeOutTime,
	        LocalDateTime clientInTime,LocalDateTime clientOutTime,
	        Boolean isShadowTimesheet,
	        Long shadowEmpId,Long docId,String shadowEmployeeName) {
	    this.timesheet_id = timesheet_id;
	    this.project_id = project_id;
	    this.teamId = teamId;
	    this.emp_id = emp_id;
	    this.dayType = dayType;
	    this.date = date;
//	    this.totalTime = totalTime; 
	    this.apmosysStartTime = officeInTime;
	    this.apmosysEndTime = officeOutTime; 
//	    this.totalWorkingHours = totalWorkingHours;
	    this.clientStartTime = clientInTime; 
	    this.clientEndTime = clientOutTime; 
	    this.isShadowTimesheet = isShadowTimesheet;
//	    this.totalClientWorkingHours = totalClientWorkingHours; 
//	    this.hasClientSideId = hasClientSideId; 
	    this.shadowFor = shadowEmpId;
	    this.docId = docId;
	    this.shadowEmployeeName=shadowEmployeeName;
	}
}
