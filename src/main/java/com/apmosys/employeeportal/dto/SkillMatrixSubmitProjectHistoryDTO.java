package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitProjectHistoryDTO {
	private Long employeeTeamMapId;
	private Integer projectId;
	private String projectName;
	private Long teamId;
	private String teamName;
	private String clientName;
	private String startDate;
	private String endDate;
	private String status;
}

