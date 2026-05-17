package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HolidayDTO {
	
	private Short holidayId;	
	private String occasion;	
	private String dateOfHoliday;
	private String dayOfTheWeek;
	private String optionalHoliday;
	private Long departmentHolidayMapId;	
	private Long deptId;
	private String isActive;
	private String state;
	private String holidayType;
	
	private String fromDate;
	private String toDate;
	private Integer createdBy;
	private String createdbyName;
	private String createdOn;
	private String updatedOn;
	private String updatedByName;
	private Integer updatedBy;
	
	private Integer yearOfOccassion;
	private Integer currentYear;
}
