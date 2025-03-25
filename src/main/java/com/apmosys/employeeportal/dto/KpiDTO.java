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
  
    private String department;
    private Long departmentId;
    private List<KpisDTO> kpis;
    private Long quarterId;
    private String createdBy;
    private String quarter;
}