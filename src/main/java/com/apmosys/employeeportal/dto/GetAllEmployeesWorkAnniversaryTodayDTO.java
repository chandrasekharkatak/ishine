package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetAllEmployeesWorkAnniversaryTodayDTO {

	private Long empId;
	private String name;
	private String email;
	private String departmentName;
    private String totalYearsWorked;
	private String dateOfJoining;
	
}
