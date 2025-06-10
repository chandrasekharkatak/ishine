package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResourceRequirementDTO {

	private String role;
    private Integer count;
    private String experience;
    private String department;
    private Long resourceOverviewId;
	private Integer projectId;
	private Long poProjectId;
	
	public ResourceRequirementDTO(String role,Integer count,String experience,String department,Long resourceOverviewId,Integer projectId){
		this.role = role;
		this.count = count;
		this.experience = experience;
		this.department = department;
		this.resourceOverviewId = resourceOverviewId;
		this.projectId = projectId;
	}
	public ResourceRequirementDTO(String role,Integer count,String experience,String department,Long resourceOverviewId,Long poProjectId){
		this.role = role;
		this.count = count;
		this.experience = experience;
		this.department = department;
		this.resourceOverviewId = resourceOverviewId;
		this.poProjectId = poProjectId;
	}
    
}