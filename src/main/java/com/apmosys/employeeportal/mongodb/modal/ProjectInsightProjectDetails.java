package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.mongodb.dto.ClientDTO;
import com.apmosys.employeeportal.mongodb.dto.DepartmentDTO;
import com.apmosys.employeeportal.mongodb.dto.DomainDTO;

import lombok.Data;

@Data
@Document(collection = "project_insight_project_details")
public class ProjectInsightProjectDetails {

	@Id
	private String id;
	private Integer projectId;
	private String projectName;
	private Long projectManagerId;
	private String projectManagerName;
	private String apmosysRM;
	private String clientRM;
	private String isDraft;
	private ClientDTO client;
	private String formId;
	private List<DomainDTO> domains;
	private List<DepartmentDTO> departments;
	private Map<String, Object> additionalInfo;
	private List<String> industryDomain;

	private String createdBy;
	private String createdOn;
	private String updatedBy;
	private String updatedOn;

}
