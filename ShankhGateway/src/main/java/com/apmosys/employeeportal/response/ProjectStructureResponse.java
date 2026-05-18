package com.apmosys.employeeportal.response;

import java.util.Arrays;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectStructureResponse {
	
	
	private Long[] deptIds;
	private String clientName;
	private String projectName;
	private String type;
	private String deptAb;
	private String deptName;
	
	
	
	
	public ProjectStructureResponse(Object[] row)
	{
		this.clientName = row[0] == null  ? null:row[0].toString();
		this.projectName =row[1] == null  ? null:row[1].toString();
		this.type = row[2] == null  ? null:row[2].toString();
		this.deptAb = row[3] == null  ? null:row[3].toString();
		this.deptName =row[4] == null  ? null:row[4].toString();
		String deptIdStr = row[5] != null ? row[5].toString() : "";
	    if (!deptIdStr.isEmpty()) {
	        this.deptIds = Arrays.stream(deptIdStr.split(","))
	                             .map(String::trim)
	                             .map(Long::valueOf)
	                             .toArray(Long[]::new);
	    } else {
	        this.deptIds = new Long[5];
	    }
	}

	
}
