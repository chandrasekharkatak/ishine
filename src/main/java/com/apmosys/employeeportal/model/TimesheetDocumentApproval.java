package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class TimesheetDocumentApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long allocId;
    
    private Long timesheetId;
    private Long approverId;
    private String approvalStatus;
    private Integer levelId;
    private Integer previousLevelId;
    private Long previousApproverId;
    private Integer rejectionId;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
}
