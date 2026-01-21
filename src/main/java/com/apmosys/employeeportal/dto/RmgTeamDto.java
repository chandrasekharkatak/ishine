package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgTeamDto {

    private Long teamId;
    private String teamName;
    private Long poId;
    private String isActive;
    private Long spocId;
    private String spocName;

    private Long createdBy;
    private Long updatedBy;

    private List<Long> deptIds;
    private List<RmgResourceRequirementDto> rmgResourceRequirementList;

    public RmgTeamDto(Long teamId, String teamName, Long poId, String isActive, Long spocId, String spocName,
            List<Long> deptIds) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.poId = poId;
        this.isActive = isActive;
        this.spocId = spocId;
        this.spocName = spocName;
        this.deptIds = deptIds;
    }

}
