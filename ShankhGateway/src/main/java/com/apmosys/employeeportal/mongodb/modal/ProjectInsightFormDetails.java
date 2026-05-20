package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;

import javax.persistence.Id;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.dto.FormFieldDTO;

import lombok.Data;

@Data
@Document(collection = "project_insight_form_details")
public class ProjectInsightFormDetails {

	@Id
	private String id;
	private String parentId;
	private String parentType;
	private String formName;
	private List<FormFieldDTO> fields;

	private String createdBy;
	private String createdOn;
	private String updatedBy;
	private String updatedOn;

}
