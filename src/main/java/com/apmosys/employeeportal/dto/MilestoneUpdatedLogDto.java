package com.apmosys.employeeportal.dto;


import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneUpdatedLogDto {

    private Long id;
    private Long milestoneId;
    private Long poId;
    private Long projectId;

    private String milestoneName;
    private Date milestoneStartDate;
    private Date milestoneEndDate;

    private String description;
    private String remarks;
    private String milestoneStatus;

    private Long lineItemId;
    private String lineItemName;
    private String projectName;
    private String poNumber;

    private Date extendedDate;
    
    private Long updatedBy;
    private String updatedByName;

    private Long milestoneExtensionReasonId;
    private String milestoneExtensionReasonText;
}
