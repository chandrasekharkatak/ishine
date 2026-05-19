package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetEmployeeTimesheetAsCalenderByProjectIdDTO {
	
	private Integer projectId;
	private Integer month;
	private Integer year;
	private Long empId;
	private Boolean allEmp;
	private LocalDate fromDate;
	private LocalDate toDate;
	private Object poNo;
	private Long poProjectId;

}
