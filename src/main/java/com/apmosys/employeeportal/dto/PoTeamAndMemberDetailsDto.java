package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PoTeamAndMemberDetailsDto {

	private Integer projectId;
	private String projectName;
	private String projectStatus;
	private String poProjectType;
	private String internalProjectType;

	private Long teamId;
	private String teamName;
	private String isTeamActive;
	private String deptIds;
	private Long spocId;
	private String spocName;
	private Long etmId;

	private Long poId;
	private String poNo;
	private LocalDateTime poStartDate;
	private LocalDateTime poEndDate;

	private String role;
	private String experience;
	private String department;

	private Long poRequirementMappingId;
	private boolean isPrmActive;
	private Long count;
	private Long assignedPending;
	private Long assignedApproved;

	private Long empId;
	private String memberName;
	private String employeeRole;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Integer isShadow;
	private Long isMemberActive;
	private boolean defaultProject;
	private String billableType;

	private Integer clientId;
	private String clientName;

	public PoTeamAndMemberDetailsDto(Long poId, Long poRequirementMappingId, Long teamId, String teamName,
			String isTeamActive, String deptIds, Long spocId, String spocName, String role, String experience,
			String department, Boolean isPrmActive, String memberName, String employeeRole, LocalDateTime startDate,
			LocalDateTime endDate, Integer isShadow, Long isMemberActive, Boolean defaultProject, Long empId) {
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.teamId = teamId;
		this.teamName = teamName;
		this.isTeamActive = isTeamActive;
		this.deptIds = deptIds;
		this.spocId = spocId;
		this.spocName = spocName;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.isPrmActive = isPrmActive != null ? isPrmActive : false;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isShadow = isShadow;
		this.isMemberActive = isMemberActive;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.empId = empId;
	}

	public PoTeamAndMemberDetailsDto(Long teamId, String teamName, Long poId, Long poRequirementMappingId, String role,
			String experience, String department, Boolean isPrmActive, Long empId, String memberName,
			String employeeRole, Long isMemberActive, Integer isShadow, Boolean defaultProject, LocalDateTime startDate,
			LocalDateTime endDate) {
		this.teamId = teamId;
		this.teamName = teamName;
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.isPrmActive = isPrmActive != null ? isPrmActive : false;
		this.empId = empId;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.isMemberActive = isMemberActive;
		this.isShadow = isShadow;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public PoTeamAndMemberDetailsDto(Long poId, Long poRequirementMappingId, String role, String experience,
			String department, Boolean isPrmActive, Long empId, String memberName, String employeeRole,
			Long isMemberActive, Integer isShadow, Boolean defaultProject, LocalDateTime startDate,
			LocalDateTime endDate) {
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.isPrmActive = isPrmActive != null ? isPrmActive : false;
		this.empId = empId;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.isMemberActive = isMemberActive;
		this.isShadow = isShadow;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public PoTeamAndMemberDetailsDto(Long poId, Long poRequirementMappingId, String role, String experience,
			String department, Boolean isPrmActive, Long empId, String memberName, String employeeRole,
			Long isMemberActive, Integer isShadow, Boolean defaultProject, LocalDateTime startDate,
			LocalDateTime endDate, Long count) {
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.isPrmActive = isPrmActive != null ? isPrmActive : false;
		this.empId = empId;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.isMemberActive = isMemberActive;
		this.isShadow = isShadow;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.startDate = startDate;
		this.endDate = endDate;
		this.count = count;
	}

	public PoTeamAndMemberDetailsDto(Integer projectId, String projectName, String projectStatus, String poProjectType,
			String internalProjectType, Long poId, String poNo, Long poRequirementMappingId, String role,
			String experience, String department, Long teamId, String teamName, Integer clientId, String clientName,
			Long empId, String billableType, Long isMemberActive, LocalDateTime startDate, LocalDateTime endDate,
			Long etmId) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectStatus = projectStatus;
		this.poProjectType = poProjectType;
		this.internalProjectType = internalProjectType;
		this.poId = poId;
		this.poNo = poNo;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.teamId = teamId;
		this.teamName = teamName;
		this.clientId = clientId;
		this.clientName = clientName;
		this.empId = empId;
		this.billableType = billableType;
		this.isMemberActive = isMemberActive;
		this.startDate = startDate;
		this.endDate = endDate;
		this.etmId = etmId;
	}

	public PoTeamAndMemberDetailsDto(Integer projectId, String projectName, String projectStatus, String poProjectType,
			String internalProjectType, Long poId, String poNo, Long poRequirementMappingId, String role,
			String experience, String department, Long teamId, String teamName, Integer clientId, String clientName,
			Long empId, String billableType, Long isMemberActive, LocalDateTime startDate, LocalDateTime endDate,
			Long etmId, LocalDateTime poStartDate, LocalDateTime poEndDate) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectStatus = projectStatus;
		this.poProjectType = poProjectType;
		this.internalProjectType = internalProjectType;
		this.poId = poId;
		this.poNo = poNo;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.teamId = teamId;
		this.teamName = teamName;
		this.clientId = clientId;
		this.clientName = clientName;
		this.empId = empId;
		this.billableType = billableType;
		this.isMemberActive = isMemberActive;
		this.startDate = startDate;
		this.endDate = endDate;
		this.etmId = etmId;
		this.poStartDate = poStartDate;
		this.poEndDate = poEndDate;
	}
}
