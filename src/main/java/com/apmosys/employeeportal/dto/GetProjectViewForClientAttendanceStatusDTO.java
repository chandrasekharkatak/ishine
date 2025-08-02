package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetProjectViewForClientAttendanceStatusDTO {
	
	private Integer projectId;
	private String projectName;
	private String projectManagerName;
	private String poNo;
	private String projectType;
	private String clientName;
	private String apmosysRM;
	private String apmosysRMEmail;
	private String clientRM;
	private Integer totalExpectedFillCount;
	private Integer totalIshineFilledCount;
	private Integer totalClientSideNotFilledCount;
	private Integer totalClientSidePendingCount;
	private Integer totalClientSideApprovedCount;
	private Double clientSideApprovedPercent;
	private Double clientSidePendingPercent;
	private Double clientSideNotFilledPercent;
	
}
