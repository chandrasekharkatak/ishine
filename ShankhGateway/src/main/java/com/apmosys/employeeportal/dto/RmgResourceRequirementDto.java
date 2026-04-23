package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgResourceRequirementDto {

    private Integer projectId;
    private String projectType;
    private String clientName;

    private Long poId;
    private String poNo;
    private LocalDateTime poStartDate;
    private LocalDateTime poEndDate;

    private Long roleId;
    private String role;
    private String experience;
    private String department;
    private LocalDateTime requirementStartDate;
    private LocalDateTime requirementEndDate;
    private boolean isPrmActive;
    private Long poRequirementMappingId;

    private Long count;
    private Long assignedPending;
    private Long assignedApproved;
    private Integer difference;

    private Long updatedBy;
    private boolean isupdate;
    private String displayRequirement;

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

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, boolean isPrmActive, Long count, LocalDateTime requirementStartDate,
            LocalDateTime requirementEndDate) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
        this.count = count;
        this.requirementStartDate = requirementStartDate;
        this.requirementEndDate = requirementEndDate;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String role, String experience,
            String department, boolean isPrmActive, Long count, Long assignedPending, Long assignedApproved) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
        this.count = count;
        this.assignedPending = assignedPending;
        this.assignedApproved = assignedApproved;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long assignedPending, Long assignedApproved) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.assignedPending = assignedPending;
        this.assignedApproved = assignedApproved;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, String poNo, LocalDateTime poStartDate,
            LocalDateTime poEndDate, Long roleId, String role, String experience, String department,
            LocalDateTime requirementStartDate, LocalDateTime requirementEndDate, Long count) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.poNo = poNo;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
        this.roleId = roleId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.requirementStartDate = requirementStartDate;
        this.requirementEndDate = requirementEndDate;
        this.count = count;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, Long roleId, String role,
            String experience, String department, boolean isPrmActive, Long count) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.roleId = roleId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.isPrmActive = isPrmActive;
        this.count = count;
    }

    public RmgResourceRequirementDto(Long poRequirementMappingId, Long poId, Long roleId, String role,
            String experience, String department, Long count, LocalDateTime requirementStartDate,
            LocalDateTime requirementEndDate) {
        this.poRequirementMappingId = poRequirementMappingId;
        this.poId = poId;
        this.roleId = roleId;
        this.role = role;
        this.experience = experience;
        this.department = department;
        this.count = count;
        this.requirementStartDate = requirementStartDate;
        this.requirementEndDate = requirementEndDate;
    }

}