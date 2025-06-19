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
	private String projectName;
	private Long teamId;
	private String teamName;
	
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
	public ResourceRequirementDTO(Long poProjectId, String projectName, Long teamId, String teamName,
			Long resourceOverviewId, Integer count, String department, String experience) {
		this.poProjectId = poProjectId;
		this.projectName = projectName;
		this.teamId = teamId;
		this.teamName = teamName;
		this.resourceOverviewId = resourceOverviewId;
		this.count = count;
		this.department = department;
		this.experience = experience;
	}
}