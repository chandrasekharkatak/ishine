package com.apmosys.employeeportal.dto;

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

    private List<RmgTeamMemberDto> rmgTeamMemberList;

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, boolean isPrmActive) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
    }

}