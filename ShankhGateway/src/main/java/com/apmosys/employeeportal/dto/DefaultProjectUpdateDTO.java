package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DefaultProjectUpdateDTO {
	
	private Integer projectId;
	private Long updatedBy;
	private List<Long> empIds;

}
