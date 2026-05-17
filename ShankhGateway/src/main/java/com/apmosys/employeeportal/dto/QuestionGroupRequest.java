package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionGroupRequest {
	Long empId;
	String parentId;
	String parentType;
	Boolean toAllChilds;
}
