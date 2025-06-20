package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetActiveProjectDetailsIfMultipleDTO {
	
	private Integer projectId;
    private String projectName;

    public GetActiveProjectDetailsIfMultipleDTO(Integer projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

}
