package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeViewForClientAttendanceStatusDTO {

    private Long empId;
    private String clientSideId;
	private String startdate;  
	private String teamName;
	private Long timesheetId;
	private Long teamId;
	private String name;
	private String spoc;
	private String billableType;
	private String employeeRole;
	private String departmentName;
	private Integer projectId;
	private String projectName;
	private String projectManagerName;
	private String poNo;
	private String clientName;
	private Integer totalExpectedFillCount;
	private Integer totalIshineFilledCount;
	private Integer totalClientSideNotFilledCount;
	private Integer totalClientSidePendingCount;
	private Integer totalClientSideApprovedCount;
	private Double clientSideApprovedPercent;
	private Double clientSidePendingPercent;
	private Double clientSideNotFilledPercent;
    private Long deptId;
    private String employeementId;
    private Long reportingManagerId;
	

}
