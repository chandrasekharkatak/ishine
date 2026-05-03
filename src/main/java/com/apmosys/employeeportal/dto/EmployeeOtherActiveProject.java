package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeOtherActiveProject {

	private Long empId;
	private Integer projectId;
	private String projectName;
	private String projectType;
	private Date projectStartDate;
	private Long poId;
	private Long teamId;
	private Long poResourceRequirementId;
	private String poNo;
	private String teamName;
	private String employeeRole;
	private String role;
	private String department;
	private String resourceRequirement;
	private String experience;
	private Long count;
	private Long updatedBy;
	private LocalDateTime startDate;
	private Long etmId;

	public EmployeeOtherActiveProject(Long empId, Integer projectId, String projectName, Long poId, Long teamId,
			Long poResourceRequirementId, String poNo, String teamName, String employeeRole, String role,
			String department, String experience, Long count, LocalDateTime startDate) {
		this.empId = empId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.poId = poId;
		this.teamId = teamId;
		this.poResourceRequirementId = poResourceRequirementId;
		this.poNo = poNo;
		this.teamName = teamName;
		this.employeeRole = employeeRole;
		this.startDate = startDate;
		this.resourceRequirement = getResourceRequirementString(role, department, experience, count);
	}

	public EmployeeOtherActiveProject(Long empId, Integer projectId, String projectName, Long poId, Long teamId,
			Long poResourceRequirementId, String poNo, String teamName, String employeeRole, String role,
			String department, String experience, Long count, LocalDateTime startDate, Long etmId) {
		this.empId = empId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.poId = poId;
		this.teamId = teamId;
		this.poResourceRequirementId = poResourceRequirementId;
		this.poNo = poNo;
		this.teamName = teamName;
		this.employeeRole = employeeRole;
		this.startDate = startDate;
		this.resourceRequirement = getResourceRequirementString(role, department, experience, count);
		this.etmId = etmId;
	}

	public EmployeeOtherActiveProject(Long empId, Integer projectId, String projectName, String projectType,
			Date projectStartDate, Long poId, Long teamId,
			Long poResourceRequirementId, String poNo, String teamName, String employeeRole, String role,
			String department, String experience, Long count, LocalDateTime startDate, Long etmId) {
		this.empId = empId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.projectStartDate = projectStartDate;
		this.poId = poId;
		this.teamId = teamId;
		this.poResourceRequirementId = poResourceRequirementId;
		this.poNo = poNo;
		this.teamName = teamName;
		this.employeeRole = employeeRole;
		this.startDate = startDate;
		this.resourceRequirement = getResourceRequirementString(role, department, experience, count);
		this.etmId = etmId;
	}

	public EmployeeOtherActiveProject(Long empId, Integer projectId, String projectName, String projectType,
			Date projectStartDate, Long poId, Long teamId,
			Long poResourceRequirementId, String poNo, String teamName, String employeeRole, String role,
			String department, String experience, Long count, LocalDateTime startDate) {
		this.empId = empId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.projectStartDate = projectStartDate;
		this.poId = poId;
		this.teamId = teamId;
		this.poResourceRequirementId = poResourceRequirementId;
		this.poNo = poNo;
		this.teamName = teamName;
		this.employeeRole = employeeRole;
		this.startDate = startDate;
		this.resourceRequirement = getResourceRequirementString(role, department, experience, count);
	}

	private String getResourceRequirementString(String role, String department, String experience, Long count) {
		StringBuilder sb = new StringBuilder();
		if (role == null || role.replace(" ", "").equals("") || StringUtils.isEmpty(role)) {
			return "-";
		}
		sb.append("Role - ").append(role);
		if (department != null && !department.replace(" ", "").equals("") && !StringUtils.isEmpty(department)) {
			sb.append(" | Dept - ").append(department);
		}
		if (experience != null && !experience.replace(" ", "").equals("") && !StringUtils.isEmpty(experience)) {
			sb.append(" | Exp - ").append(experience).append(" yrs");
		}
		if (count != null) {
			sb.append(" | Required Resource Count - ").append(count);
		}
		return sb.toString();
	}
}
