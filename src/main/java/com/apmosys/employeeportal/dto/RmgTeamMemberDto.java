package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgTeamMemberDto {

    private Long empId;
    private String memberName;
    private String employeeRole;
    private List<String> employeeRoles;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer isShadow;
    private Long isMemberActive;
    private boolean deafultProject;
    private String memberDepartment;
    private String employementId;
    private String billableType;
    private String prevExp;
    private String currentExp;
    private String totalExp;
    private String jobRoleName;

    public RmgTeamMemberDto(String memberName, String employeeRole,List<String> employeeRoles, LocalDateTime startDate,
            LocalDateTime endDate,
            Integer isShadow, Long isMemberActive, boolean deafultProject) {
        this.memberName = memberName;
        this.employeeRole = employeeRole;
        this.employeeRoles = employeeRoles;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isShadow = isShadow;
        this.isMemberActive = isMemberActive;
        this.deafultProject = deafultProject;
    }

}