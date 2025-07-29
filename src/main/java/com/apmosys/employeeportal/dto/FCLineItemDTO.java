package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FCLineItemDTO {

	private Long id;

	private Long poId;

	private Long projectId;
	private Long poProjectId;

	private String name;

	private String status;
	
	
	private List<FCProjectMilestoneDTO>  milestones;

}
