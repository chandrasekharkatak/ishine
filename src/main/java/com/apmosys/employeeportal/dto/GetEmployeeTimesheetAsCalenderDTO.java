package com.apmosys.employeeportal.dto;

import java.util.Map;

import com.apmosys.employeeportal.model.TimesheetDataDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetEmployeeTimesheetAsCalenderDTO {
	
	private Long empId;
	private String clientSideId;
	private String startDate;
	private String teamName;
	private Long teamId;
	private String employeeName;
	private String spoc;
	private String billableType;
	private String employeeRole;
	private String department;
	private Integer projectId;
	private String projectName;
	private String projectManagerName;
	private String poNo;
	private String clientName;
	private Long reportingManagerId;
	private String monthName;
	private Integer expectedTimesheetFillCount;
	private Integer apmosysTimesheetFilledCount;
	private Integer clientSideNotFilledCount;
	private Integer clientSidePendingCount;
	private Integer clientSideApprovedCount;
	private Map<String, TimesheetDataDTO> timesheetData;

}
