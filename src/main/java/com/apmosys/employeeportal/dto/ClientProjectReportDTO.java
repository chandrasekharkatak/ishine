package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ClientProjectReportDTO {
    private String clientName;
    private String departmentName;
    private Integer active;
    private String projectName;
    
    private String deptIds;           
    private List<Long> deptIdsList;   
    private Long deptId;
    
    private Integer totalProjects;
    private Integer totalActiveProjects;
    private Integer totalInactiveProjects;

    public ClientProjectReportDTO() {}

  
    
    
}