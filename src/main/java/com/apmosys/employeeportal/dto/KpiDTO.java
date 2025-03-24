package com.apmosys.employeeportal.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDTO {

    private Long id;
    private String name;
    private String description;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String department;
    private List<kpiItemDTO> kpis;
  
    private Long quarterId;
    
    private String quarter;
}





