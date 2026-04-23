package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectInsightResponsePointsDTO {

	private Long projectInsightResponsePointId;
	private Double points;
	private Long pointsBy;
	private String pointsByName;
	private Long responseId;
	private String isPointsDrafted;
	private Timestamp updatedOn;
	private Timestamp finalSubmittedOn;

}
