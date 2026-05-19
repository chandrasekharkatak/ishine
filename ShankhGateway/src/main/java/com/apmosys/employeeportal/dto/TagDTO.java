package com.apmosys.employeeportal.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {

	private Long tagId;
	private Long projectId;
	private String tag;
	private String entityType;
	private Long entityId;
	private String type;
	
	private StringBuilder projectText;
	private String tagType; //response
	
}
