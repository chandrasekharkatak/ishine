package com.apmosys.employeeportal.model;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "goal_templates")
public class GoalTemplates {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;
    
    @Column(name = "title")
    private String title;
    
    
    @Column(name = "quarter_id")
    private Long quarterId;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    
    @Column(name = "departmentId")
    private Long departmentId;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "is_approved")
    private Boolean isApproved;
}