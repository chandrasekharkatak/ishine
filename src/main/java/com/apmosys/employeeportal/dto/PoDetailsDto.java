package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PoDetailsDto {

    private Long id;
    private Long poId;
    private Integer projectId;
    private String poNo;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedOn;
    private String apmosysRM;
    private String clientRm;
    private String apmosysRmEmail;
    private String poStartDate;
    private String poEndDate;
    private boolean active;
    private String prevPO;
    private String nextPO;
    private String msg;

    private Integer totalRequirements;
    private Integer assigned;
    private Integer difference;
    private Long assignedPending;
    private Long assignedApproved;

    private List<Long> selectedTeamIds;
    private List<RmgTeamDto> teamList;
    private boolean isUpdate;
    private Long updatedBy;

    public PoDetailsDto(Long id, Long poId, Integer projectId, String poNo, String poStartDate, String poEndDate,
            boolean active) {
        this.id = id;
        this.poId = poId;
        this.projectId = projectId;
        this.poNo = poNo;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
        this.active = active;
    }

    public PoDetailsDto(Long id, Long poId, Integer projectId, String poNo, String poStartDate, String poEndDate,
            boolean active, Long assignedApproved, Long assignedPending) {
        this.id = id;
        this.poId = poId;
        this.projectId = projectId;
        this.poNo = poNo;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
        this.active = active;
        this.assignedApproved = assignedApproved;
        this.assignedPending = assignedPending;
    }

}
