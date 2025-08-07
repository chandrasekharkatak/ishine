package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.mongodb.dto.BadgePathDTO;
import com.apmosys.employeeportal.mongodb.dto.OptionValueDTO;

import lombok.Data;

@Data
@Document(collection = "project_insight_questions_details")
public class ProjectInsightQuestionDetails {

	@Id
	private String id;
	private String question;
	private String description;
	private boolean required;
	private boolean documentUpload;
	private String currentActiveBadgeLevel;
	private String optionType;
	private String parentId;
	private String parentType;
	private List<String> parentPathIds; 

	private List<OptionValueDTO> optionsList;
	private List<BadgePathDTO> badgePathList;

	private List<Object> toTaggedEmployeeIdList;
	private List<Object> toAssignedEmployeeIdList;

	private String createdBy;
	private String createdOn;
	private String updatedBy;
	private String updatedOn;

}
