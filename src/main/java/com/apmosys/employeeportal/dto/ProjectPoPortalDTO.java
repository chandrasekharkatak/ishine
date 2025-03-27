package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectPoPortalDTO {
	private Long id;
    private String name;
    private String projectType;
    private String projectManager;
    private String projectManagerName;

    private String startDate; // Use String for dates
    private String endDate;

    private String status;
    private List<String> department;
    private String clientName;
    private List<String> clientLocation;
    private String clientState;

    private String createdOn;

    private String poNo;
    private String poStartDate;
    private String poEndDate;
    private String poProjectType;
    private String PoProjectId;
    private List<ResourceRequirementDTO> resourceRequirements;
 
}
