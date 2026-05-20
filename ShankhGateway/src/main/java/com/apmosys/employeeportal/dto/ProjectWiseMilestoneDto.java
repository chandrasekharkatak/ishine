package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.Project;

import lombok.Data;

@Data
public class ProjectWiseMilestoneDto {
	private Project project;
    private List<MilestoneExpireDto> milestones; 

}
