package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@Data
public class GetProjectDetailsForBulkDefaultUpdateProjectDTO {
	
	
	public GetProjectDetailsForBulkDefaultUpdateProjectDTO(
	    Integer projectId,
	    String projectName,
	    Long teamId,
	    String teamName,
	    Long resourceOverviewId,
	    Integer count,
	    String department,
	    String experience,
	    String role
	) {
	    this.projectId = projectId != null ? projectId.intValue() : null; // since field is Integer
	    this.projectName = projectName;
	    this.teamId = teamId;
	    this.teamName = teamName;
	    this.resourceOverviewId = resourceOverviewId;
	    this.count = count;
	    this.department = department;
	    this.experience = experience;
	    this.role = role;
	}

	  Integer projectId;
     String projectName;
    List<GetProjectDetailsForBulkDefaultUpdateTeamDTO> teamList = new ArrayList<>();
     List<ResourceRequirementDTO> resourceRequirement = new ArrayList<>();
    Long teamId;
    String teamName;
    Long resourceOverviewId;
    Integer count;
    String department;
    String experience;
    String role;
    
    public GetProjectDetailsForBulkDefaultUpdateProjectDTO() {}
}