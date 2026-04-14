package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class TimesheetDocumentApprovalHierarchy {

    @Id
    private Integer levelId;

    private String role;
    private Integer hierarchyOrder;
    private Boolean active;
    private LocalDateTime createdOn;
    private Long createdBy;
    private LocalDateTime updatedOn;
    private Long updatedBy;
}
