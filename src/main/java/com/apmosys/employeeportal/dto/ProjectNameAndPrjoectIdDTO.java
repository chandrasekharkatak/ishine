package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectNameAndPrjoectIdDTO {

	private Integer projectId;
	private String projectName;
	private String internalProjectType;
	private String projectType;
	private Date startDate;
	private String poProjectType;
	private Boolean hasClientFlag;
	private Boolean hasClientSideId;
	private Integer isShadow;

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName) {
		this.projectId = projectId;
		this.projectName = projectName;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, String internalProjectType) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.internalProjectType = internalProjectType;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, String internalProjectType,
			String poProjectType) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.internalProjectType = internalProjectType;
		this.poProjectType = poProjectType;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, Boolean clientFlag,
			Boolean hasClientSideId) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.hasClientFlag = clientFlag;
		this.hasClientSideId = hasClientSideId;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, String internalProjectType,
			String poProjectType, String projectType, Date startDate) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.internalProjectType = internalProjectType;
		this.poProjectType = poProjectType;
		this.projectType = projectType;
		this.startDate = startDate;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, Boolean clientFlag,
			Boolean hasClientSideId, String poProjectType, Integer isShadow) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.hasClientFlag = clientFlag;
		this.hasClientSideId = hasClientSideId;
		this.poProjectType = poProjectType;
		this.isShadow = isShadow;
	}
}
