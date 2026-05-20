package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OtherProjectSetDTO {
	
	private List<Long> empIds;
	private Integer projectId;
}
