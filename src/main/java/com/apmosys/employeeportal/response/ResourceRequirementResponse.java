package com.apmosys.employeeportal.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceRequirementResponse {

	
	private Long resourceOverviewId;
	@Override
	public String toString() {
		return "ResourceRequirement [resourceOverviewId=" + resourceOverviewId + ", role=" + role + ", count=" + count
				+ ", experience=" + experience + ", department=" + department + "]";
	}
	private String role;
    private Long count;
    private String experience;
    private String department;
}

