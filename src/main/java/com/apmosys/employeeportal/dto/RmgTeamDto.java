package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private Long teamLeadId;
    private String teamLeadName;

    private Long createdBy;
    private Long updatedBy;

    private List<Long> deptIds;
    private List<RmgResourceRequirementDto> rmgResourceRequirementList;
    
    private LocalDateTime endDate;

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

    public RmgTeamDto(Long teamId, String teamName, Long poId, String isActive, Long spocId, String spocName,
            String deptIds, Long teamLeadId, String teamLeadName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.poId = poId;
        this.isActive = isActive;
        this.spocId = spocId;
        this.spocName = spocName;
        this.deptIds = getDeptIdListFromString(deptIds);
        this.teamLeadId = teamLeadId;
        this.teamLeadName = teamLeadName;
    }

    private List<Long> getDeptIdListFromString(String deptIds) {
        return Optional.ofNullable(deptIds)
                .filter(s -> !s.isBlank())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
