package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectQuestionStatusDto {
	private String projectName;
	private String projectId;
    private Integer approvedCount;
    private Integer draftCount;
    private Integer pendingCount;
    private Integer totalCount;
}