package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
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
    private Long poRequirementMappingId;
    private String role;
    private String experience;
    private String department;
    private boolean isPrmActive;

    private Long empId;
    private String memberName;
    private String employeeRole;
    private List<String> employeeRoles;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer isShadow;
    private Long isMemberActive;
    private boolean defaultProject;
    private String memberDepartment;
    private String employementId;
    private String billableType;
    private String prevExp;
    private String currentExp;
    private String totalExp;
    private String jobRoleName;
    private List<Integer> otherActiveProjectIds;

    private Integer clientId;
    private String clientName;

    private Long createdBy;
    private Long updatedBy;

    private List<Long> selectedEmpIds;
    private List<EmployeeOtherActiveProject> otherActiveProjects;

    public RmgTeamMemberDto(String memberName, String employeeRole, List<String> employeeRoles, LocalDateTime startDate,
            LocalDateTime endDate, Integer isShadow, Long isMemberActive, boolean defaultProject) {
        this.memberName = memberName;
        this.employeeRole = employeeRole;
        this.employeeRoles = employeeRoles;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isShadow = isShadow;
        this.isMemberActive = isMemberActive;
        this.defaultProject = defaultProject;
    }

    public RmgTeamMemberDto(Long empId, List<String> employeeRoles) {
        this.empId = empId;
        this.employeeRoles = employeeRoles;
    }

}