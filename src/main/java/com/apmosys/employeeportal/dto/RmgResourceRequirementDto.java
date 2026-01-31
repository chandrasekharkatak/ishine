package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgResourceRequirementDto {

    private Long poRequirementMappingId;
    private Long poId;
    private String role;
    private String experience;
    private String department;
    private boolean isPrmActive;
    private Long count;

    private List<RmgTeamMemberDto> rmgTeamMemberList;
    private Integer projectId;
    private Long teamId;
    private Long updatedBy;
    private boolean isupdate;
    private boolean isNewRequirementInTeam;

    private String clientName;
    private String projectType;
    private LocalDateTime endDate;
    private boolean isCustomEndDate;
    
    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, boolean isPrmActive) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, boolean isPrmActive, Long count) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
        this.count = count;
    }

}