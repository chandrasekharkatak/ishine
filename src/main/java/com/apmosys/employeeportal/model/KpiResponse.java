package com.apmosys.employeeportal.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;
    
    private Long empId;
    private Long kpiId;
    private String response;
    private String remarks;
    private Float score;
    private String approvedBy;
    private LocalDateTime createdAt;
    private String departmentName;
    private Long departmentId;
    private Long quarterId;
}