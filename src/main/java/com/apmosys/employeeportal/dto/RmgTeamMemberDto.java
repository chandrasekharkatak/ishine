package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgTeamMemberDto {

	private Integer projectId;
	private String projectName;
	private String projectStatus;
	private String projectType;
	private String poProjectType;
	private String internalProjectType;
	private LocalDateTime projectStartDate;

	private Long teamId;
	private String teamName;
	private String isTeamActive;
	private String deptIds;
	private Long spocId;
	private String spocName;
	private Long etmId;
	private Long rescRemovedBy;
	private boolean isCustomDate;
	private LocalDateTime rescEndDate;
	private Integer mappedDefaultProjectId;

	private Long poId;
	private String poNo;
	private LocalDateTime poStartDate;
	private LocalDateTime poEndDate;

	private Long roleId;
	private String role;
	private String experience;
	private String department;

	private Long poRequirementMappingId;
	private boolean isPrmActive;
	private LocalDateTime lineItemStartDate;
	private LocalDateTime lineItemEndDate;
	private String displayRequirement;

	private Long empId;
	private Long empTeamDepartmentId;
	private String empTeamDepartmentName;
	private String memberName;
	private String employeeRole;
	private List<String> employeeRoles;
	private LocalDateTime startDate;
	private LocalDateTime dbStartDate;
	private LocalDateTime endDate;
	private LocalDateTime dbEndDate;
	private Integer isShadow;
	private Integer dbIsShadow;
	private Long isMemberActive;
	private boolean defaultProject;
	private boolean dbDefaultProject;
	private String memberDepartment;
	private String employementId;
	private String billableType;
	private String prevExp;
	private String currentExp;
	private String totalExp;
	private String jobRoleName;
	private String employmentStatus;
	private List<Integer> otherActiveProjectIds;

	private Integer clientId;
	private String clientName;

	private Long createdBy;
	private Long updatedBy;

	private List<Long> selectedEmpIds;
	private List<EmployeeOtherActiveProject> otherActiveProjects;
	private List<Integer> projectIds;

	public RmgTeamMemberDto(String memberName, String employeeRole, List<String> employeeRoles, LocalDateTime startDate,
			LocalDateTime endDate, Integer isShadow, Long isMemberActive, boolean defaultProject) {
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.employeeRoles = employeeRoles;
		this.startDate = startDate;
		this.dbStartDate = startDate;
		this.endDate = endDate;
		this.isShadow = isShadow;
		this.isMemberActive = isMemberActive;
		this.defaultProject = defaultProject;
		this.dbDefaultProject = defaultProject;
		this.dbIsShadow = isShadow;
	}

	public RmgTeamMemberDto(Long empId, List<String> employeeRoles) {
		this.empId = empId;
		this.employeeRoles = employeeRoles;
	}

	public RmgTeamMemberDto(Long teamId, String teamName, String poNo, Long poId, Long poRequirementMappingId,
			String role, String experience, String department, Boolean isPrmActive, LocalDateTime lineItemStartDate,
			LocalDateTime lineItemEndDate, Long empId, String memberName, String employeeRole, Long isMemberActive,
			Integer isShadow, Boolean defaultProject, LocalDateTime startDate, LocalDateTime endDate) {
		this.teamId = teamId;
		this.teamName = teamName;
		this.poNo = poNo;
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.isPrmActive = isPrmActive != null ? isPrmActive : false;
		this.lineItemStartDate = lineItemStartDate;
		this.lineItemEndDate = lineItemEndDate;
		this.empId = empId;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.employeeRoles = getEmployeeRolesFromString(employeeRole);
		this.isMemberActive = isMemberActive;
		this.isShadow = isShadow;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.dbIsShadow = this.isShadow;
		this.dbDefaultProject = this.defaultProject;
		this.startDate = startDate;
		this.dbStartDate = startDate;
		this.endDate = endDate;
		this.dbEndDate = endDate;
	}

	private List<String> getEmployeeRolesFromString(String employeeRole) {
		return (employeeRole != null && !employeeRole.trim().equals("")) ? Arrays.asList(employeeRole.split(","))
				: List.of("Employee");
	}

	public RmgTeamMemberDto(Long etmId, Long empTeamDepartmentId, String empTeamDepartmentName, Long teamId,
			String teamName, Long poId, String poNo, Long poRequirementMappingId, Long roleId, String role,
			String experience, String department, LocalDateTime lineItemStartDate, LocalDateTime lineItemEndDate,
			LocalDateTime poStartDate, LocalDateTime poEndDate, Long empId, String memberName, String employeeRole,
			Long isMemberActive, Integer isShadow, Boolean defaultProject, LocalDateTime startDate,
			LocalDateTime endDate) {
		this.etmId = etmId;
		this.empTeamDepartmentId = empTeamDepartmentId;
		this.empTeamDepartmentName = empTeamDepartmentName;
		this.teamId = teamId;
		this.teamName = teamName;
		this.poNo = poNo;
		this.poId = poId;
		this.poRequirementMappingId = poRequirementMappingId;
		this.roleId = roleId;
		this.role = role;
		this.experience = experience;
		this.department = department;
		this.lineItemStartDate = lineItemStartDate;
		this.lineItemEndDate = lineItemEndDate;
		this.empId = empId;
		this.memberName = memberName;
		this.employeeRole = employeeRole;
		this.employeeRoles = getEmployeeRolesFromString(employeeRole);
		this.isMemberActive = isMemberActive;
		this.isShadow = isShadow;
		this.defaultProject = defaultProject != null ? defaultProject : false;
		this.dbIsShadow = this.isShadow;
		this.dbDefaultProject = this.defaultProject;
		this.startDate = startDate;
		this.dbStartDate = startDate;
		this.endDate = endDate;
		this.dbEndDate = endDate;
		this.poStartDate = poStartDate;
		this.poEndDate = poEndDate;
	}
}