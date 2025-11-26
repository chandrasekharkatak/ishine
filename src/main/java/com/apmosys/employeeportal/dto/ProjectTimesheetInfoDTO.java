package com.apmosys.employeeportal.dto;

import java.util.Map;

import com.apmosys.employeeportal.model.TimesheetDataDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class ProjectTimesheetInfoDTO {

	private String startDate;
	private String teamName;
	private Long teamId;
	private String spoc;
	private String employeeRole;
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
	private String employmentId;
	private String present;
	private String weekOff;
	private String holiday;
	private String leave;
	private String compOff;
	private String na;
	private String halfDay;
	private String totalNoOfDays;
	private String employmentStatus;
	private String endDate;
	private String readyForInvoicing;
	private String projectStatus;
	private String projectActive;
    private String clientSideId;
}
