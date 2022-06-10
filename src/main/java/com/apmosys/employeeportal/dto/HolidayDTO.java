package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HolidayDTO {
	
	private Short holidayId;	
	private String occasion;	
	private String dateOfHoliday;
	private String dayOfTheWeek;

}
