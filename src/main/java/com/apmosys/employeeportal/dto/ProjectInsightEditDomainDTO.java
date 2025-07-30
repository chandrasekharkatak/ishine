package com.apmosys.employeeportal.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightEditDomainDTO {

    private Long parent_id;
    private String parent_id_name;
    private String name;
    private String type;
    
}