package com.apmosys.employeeportal.dto;

import com.apmosys.employeeportal.model.GoalType;
import com.apmosys.employeeportal.model.GoalStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDTO {

    private Long id;
    private String name;
    private String description;
    private GoalType type; 
    private GoalStatus status;
    private String assignedBy;
    private String createdBy;
    private String updatedBy;
    private String approvedBy;
    private String rejectedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String department;
}
