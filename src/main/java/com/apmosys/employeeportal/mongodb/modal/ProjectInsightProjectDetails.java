package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Document(collection = "project_insight_structure")
@Data
public class ProjectInsightProjectDetails {
	
	@Id
	private String id;
	private String projectName;
	private Integer projectId;
	private Client client;
	List<Department> departments;
	Map<String,Object> additionalInfo;
	
	
	

}
