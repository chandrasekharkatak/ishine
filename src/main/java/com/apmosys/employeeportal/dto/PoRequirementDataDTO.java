package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoRequirementDataDTO {
    private Long poRequirementMappingId;
    private Long poId;
    private String role;
    private String experience;
    private String department;
    private Long teamId;
    private Long projectId;
    private String projectName;
    private String poNo;
    private String poStartDate;
    private String poEndDate;

    public PoRequirementDataDTO(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, Long teamId, Integer projectId, String projectName, String poNo, String poStartDate,
            String poEndDate) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.teamId = teamId;
        this.projectId = projectId != null ? projectId.longValue() : null;
        this.projectName = projectName;
        this.poNo = poNo;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
    }

}