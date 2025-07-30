package com.apmosys.employeeportal.mongodb.modal;

import javax.persistence.Id;

import org.bson.types.ObjectId;

import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import com.apmosys.employeeportal.mongodb.dto.FormDataDTO;
import com.apmosys.employeeportal.mongodb.dto.FormStructureDTO;

import lombok.Data;

@Document(collection = "project_insight_structure")
@Data
public class ProjectInsightStructure {

	@Id
	private ObjectId id;
	private FormStructureDTO structure;
	private FormDataDTO data;
	private String createdBy;
	private String createdOn;
	private String updatedOn;
	private String updatedBy;
	private String isDraft;
	
	// @Version
	// private Long version;

}
