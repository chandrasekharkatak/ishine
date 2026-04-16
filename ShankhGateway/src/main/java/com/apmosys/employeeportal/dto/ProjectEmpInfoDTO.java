package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ProjectEmpInfoDTO {
	
	private Long empId;
    private Integer projectId;
    private String projectName;
    private String poProjectType;
    private String internalProjectType;
    private Integer isShadow;

}
