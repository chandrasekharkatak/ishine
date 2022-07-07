package com.apmosys.employeeportal.dto;



import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ActivityDTO {

	private Long activityId;
	private Integer projectId;
	private String projectName;
	private String clientName;
	private Long teamId;
	private String teamName;
	private String activity;
	private Float eta;
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;

}
