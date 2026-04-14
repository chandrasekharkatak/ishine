package com.apmosys.employeeportal.dto;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPoDepartmentMapDTO {
	
private Integer projectId;
	
	private String clientName;
	private String clientLocation;
	private String state;
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long empId;
	private Timestamp approvedOn;
    
	private Timestamp createdOn;
	private Integer clientId;
	private String departmentName;
	private Long poProjectId;
	private String active;
	private String syncProject;
	private String isDraftProject;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	private String role;
    private Integer count;
    private String experience;
    private String poStartDate;
	private String poEndDate;
	private String poNo;
	private String poProjectType;
	private String apmosysRM;
	private String clientRM;
	private Boolean isRenewable;
	private String deptId;
	private String status;
	private String apmosysRmEmail;
	private String projectCompletionDate;
	private String projectStatus;
	private String internalProjectType;
	private Boolean hasClientSideId;
	private Boolean clientFlag;
    private Long poId;
}
