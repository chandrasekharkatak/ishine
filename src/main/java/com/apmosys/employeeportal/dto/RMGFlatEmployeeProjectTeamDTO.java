package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Set;

import com.apmosys.employeeportal.model.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
public class RMGFlatEmployeeProjectTeamDTO {
	private Long empId;
    private Long employeementId;
    private String name;
    private String employmentstatus;
    private String billable;
    private String billableType;
    private String department;

    private Integer projectId;
    private String clientName;
    private String projectName;
    private Long poProjectId;
    private String poStartDate;
    private String poEndDate;
    private String apmosysRM;
    private String clientRM;
    private String poProjectType;
    private String poNo;
    private String projectActive;

    private Long teamId;
    private String teamName;
    private String teamIsActive;
    private String employeeRole;
    private Integer etmActive;
    private Long pmEmpId;
    private String pmName;
    private String isConsultant;
    private String isApprenticeship;
    private String isApmosysProduct;
    
	public RMGFlatEmployeeProjectTeamDTO(Long empId, Long employeementId, String name, String employmentstatus,
			String billable, String billableType, String department, Integer projectId, String clientName,
			String projectName, Long poProjectId, String poStartDate, String poEndDate, String apmosysRM,
			String clientRM, String poProjectType, String poNo, String projectActive, Long teamId, String teamName,
			String teamIsActive, String employeeRole, Integer etmActive) {
		super();
		this.empId = empId;
		this.employeementId = employeementId;
		this.name = name;
		this.employmentstatus = employmentstatus;
		this.billable = billable;
		this.billableType = billableType;
		this.department = department;
		this.projectId = projectId;
		this.clientName = clientName;
		this.projectName = projectName;
		this.poProjectId = poProjectId;
		this.poStartDate = poStartDate;
		this.poEndDate = poEndDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.poProjectType = poProjectType;
		this.poNo = poNo;
		this.projectActive = projectActive;
		this.teamId = teamId;
		this.teamName = teamName;
		this.teamIsActive = teamIsActive;
		this.employeeRole = employeeRole;
		this.etmActive = etmActive;
	}
	public RMGFlatEmployeeProjectTeamDTO(Long employeementId, String name, String departmentName, String billableType,
			Integer projectId, String projectName, String clientName, String apmosysRM, String clientRM, String poNo,
			String poProjectType, String poStartDate, String poEndDate) {
		this.employeementId = employeementId;
		this.name = name;
		this.department = departmentName;
		this.billableType = billableType;
		this.projectId = projectId;
		this.projectName = projectName;
		this.poStartDate = poStartDate;
		this.poEndDate = poEndDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.poProjectType = poProjectType;
		this.poNo = poNo;
		this.clientName = clientName;
	}
	
	public RMGFlatEmployeeProjectTeamDTO(
		    Long empId,
		    Long employeementId,
		    String billable,
		    String billableType,
		    String name,
		    String departmentName,
		    Integer projectId,
		    String projectName,
		    Long poProjectId,
		    String poStartDate,
		    String poEndDate,
		    String apmosysRM,
		    String clientRM,
		    String poProjectType,
		    String poNo,
		    String clientName,
		    Long teamId,
		    String teamName,
		    String teamIsActive,
		    String employeeRole,
		    Long etmActive,
		    Long pmEmpId,
		    String pmName,
		    String isConsultant,
		    String isApprenticeship,
		    String isApmosysProduct
		) {
		    this.empId = empId;
		    this.employeementId = employeementId;
		    this.billable = billable;
		    this.billableType = billableType;
		    this.name = name;
		    this.department = departmentName;
		    this.projectId = projectId;
		    this.projectName = projectName;
		    this.poProjectId = poProjectId;
		    this.poStartDate = poStartDate;
		    this.poEndDate = poEndDate;
		    this.apmosysRM = apmosysRM;
		    this.clientRM = clientRM;
		    this.poProjectType = poProjectType;
		    this.poNo = poNo;
		    this.clientName = clientName;
		    this.teamId = teamId;
		    this.teamName = teamName;
		    this.teamIsActive = teamIsActive;
		    this.employeeRole = employeeRole;
		    this.etmActive = Integer.parseInt(etmActive.toString());
		    this.pmEmpId = pmEmpId;
		    this.pmName = pmName;
		    this.isConsultant = isConsultant;
		    this.isApprenticeship = isApprenticeship;
		    this.isApmosysProduct = isApmosysProduct;
		}

}
