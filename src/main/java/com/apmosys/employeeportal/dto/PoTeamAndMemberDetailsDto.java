package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PoTeamAndMemberDetailsDto {

    private Long teamId;
    private String teamName;
    private String isTeamActive;
    private String deptIds;
    private Long spocId;
    private String spocName;

    private Long poId;
    private Long poRequirementMappingId;
    private String role;
    private String experience;
    private String department;
    private boolean isPrmActive;

    private Long empId;
    private String memberName;
    private String employeeRole;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer isShadow;
    private Long isMemberActive;
    private boolean deafultProject;

    public PoTeamAndMemberDetailsDto(Long poId, Long poRequirementMappingId,
            Long teamId, String teamName, String isTeamActive, String deptIds,
            Long spocId, String spocName, String role, String experience, String department, boolean isPrmActive,
            String memberName, String employeeRole, LocalDateTime startDate, LocalDateTime endDate, Integer isShadow,
            Long isMemberActive, boolean deafultProject, Long empId) {
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
        this.isPrmActive = isPrmActive;
        this.memberName = memberName;
        this.employeeRole = employeeRole;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isShadow = isShadow;
        this.isMemberActive = isMemberActive;
        this.deafultProject = deafultProject;
        this.empId = empId;
    }

}
