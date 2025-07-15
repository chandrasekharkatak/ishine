package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class HierarchyOptionDTO {
	
	private Long id;
    private String name;
    private Boolean isChildAvailable;
    private String hierarchyType;

}
