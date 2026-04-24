package com.apmosys.employeeportal.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.ForeignKey;

import lombok.*;

@Entity
@Table(name = "milestone_updated_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneUpdatedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long milestoneId;
    private Long poId;
    private Long projectId;

    private String milestoneName;
    private Date milestoneStartDate;
    private Date milestoneEndDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private String milestoneStatus;

    private Long lineItemId;
    private String lineItemName;
    private String projectName;
    private String poNumber;
    
    @Column(name = "extended_date")
    private Date extendedDate;
    
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "custom_reason",length = 255)
    private String customReason;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_extension_reason_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_milestone_reason"))
    private MilestoneExtensionReason milestoneExtensionReason;
    
    @Column(name = "updated_on")
    private Date updatedOn;
    

}

