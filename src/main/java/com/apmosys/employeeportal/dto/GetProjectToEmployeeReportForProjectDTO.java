package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetProjectToEmployeeReportForProjectDTO {
	
	private Long projectId;
    private Long poProjectId;
    private String projectName;
    private Long projectManagerId;
    private String projectManager;
    private String poNo;
    private String poProjectType;
    private String clientRM;
    private String apmosysRM;
    private String poStartDate;
    private String poEndDate;
    private Integer employeeCount;
    private Integer projectCount;
    private List<GetProjectToEmployeeReportForTeamDTO> teamDetails;

}
