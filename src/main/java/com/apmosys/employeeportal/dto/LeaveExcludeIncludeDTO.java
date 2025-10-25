package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LeaveExcludeIncludeDTO {
	
	private List<Long> empIds;
    private Boolean isInclude;
    private Boolean isExclude;
    private Integer createdBy;
    
    
}
