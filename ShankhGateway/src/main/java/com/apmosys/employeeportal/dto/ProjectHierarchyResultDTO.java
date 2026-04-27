package com.apmosys.employeeportal.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectHierarchyResultDTO {

	private Long currPo;
    private Integer currProject;

    private Long childPo;
    private Integer childProjectId;

    private Long parentPo;
    private Integer parentProjectId;
    
}
