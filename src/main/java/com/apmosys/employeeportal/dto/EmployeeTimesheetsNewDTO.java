package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EmployeeTimesheetsNewDTO {
	 private Long timesheetId;

	    private Long createdBy;

	    private LocalDateTime createdOn;

	    private Long updatedBy;

	    private Boolean isNightShift;

	    private LocalDateTime updatedOn;

	    private LocalDate date;

	    private Integer dayTypeId;

	    private Long empId;

	    private Integer status;

	    private LocalDateTime workCheckIn;

	    private LocalDateTime workCheckOut;

	    private Integer totalWorkingMinutes;

	    private Long leaveTypeMasterId;

	    private String description;

	    private Long currentManagerId;
	    private String daytype;
	    private Long EmployementID;
	    private String isProd;
	    
	    public EmployeeTimesheetsNewDTO(
	            Long timesheetId,
	            Long createdBy,
	            LocalDateTime createdOn,
	            Long updatedBy,
	            Boolean isNightShift,
	            LocalDateTime updatedOn,
	            LocalDate date,
	            Integer dayTypeId,
	            Long empId,
	            Integer status,
	            LocalDateTime workCheckIn,
	            LocalDateTime workCheckOut,
	            Integer totalWorkingMinutes,
	            Long leaveTypeMasterId,
	            String description,
	            Long currentManagerId,
	            String daytype
	    ) {
	        this.timesheetId = timesheetId;
	        this.createdBy = createdBy;
	        this.createdOn = createdOn;
	        this.updatedBy = updatedBy;
	        this.isNightShift = isNightShift;
	        this.updatedOn = updatedOn;
	        this.date = date;
	        this.dayTypeId = dayTypeId;
	        this.empId = empId;
	        this.status = status;
	        this.workCheckIn = workCheckIn;
	        this.workCheckOut = workCheckOut;
	        this.totalWorkingMinutes = totalWorkingMinutes;
	        this.leaveTypeMasterId = leaveTypeMasterId;
	        this.description = description;
	        this.currentManagerId = currentManagerId;
	        this.daytype = daytype;
	    }
	    

	    public EmployeeTimesheetsNewDTO(
	            Long timesheetId,
	            Long createdBy,
	            LocalDateTime createdOn,
	            Long updatedBy,
	            Boolean isNightShift,
	            LocalDateTime updatedOn,
	            LocalDate date,
	            Integer dayTypeId,
	            Long empId,
	            Integer status,
	            LocalDateTime workCheckIn,
	            LocalDateTime workCheckOut,
	            Integer totalWorkingMinutes,
	            Long leaveTypeMasterId,
	            String description,
	            Long currentManagerId,
	            Long employementID,
	            String isProd
	    ) {
	        this.timesheetId = timesheetId;
	        this.createdBy = createdBy;
	        this.createdOn = createdOn;
	        this.updatedBy = updatedBy;
	        this.isNightShift = isNightShift;
	        this.updatedOn = updatedOn;
	        this.date = date;
	        this.dayTypeId = dayTypeId;
	        this.empId = empId;
	        this.status = status;
	        this.workCheckIn = workCheckIn;
	        this.workCheckOut = workCheckOut;
	        this.totalWorkingMinutes = totalWorkingMinutes;
	        this.leaveTypeMasterId = leaveTypeMasterId;
	        this.description = description;
	        this.currentManagerId = currentManagerId;
	        this.EmployementID = employementID;
	        this.isProd=isProd;
	    }
}
