package com.apmosys.employeeportal.dto;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.enums.SyncRequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPoMappingWithResourceDTO {
	
	private SyncRequestType eventType;
	private Long projectId;
    private String projectName;
    private String projectType;
    private Date projectStartDate;
    private Date projectEndDate;
    private Long clientId;
    private String clientName;
    private String projectStatus;
    private List<PoDetailsForProjectPoMappingDTO> poDetailsList;
	
	

}
