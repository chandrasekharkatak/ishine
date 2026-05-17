package com.apmosys.employeeportal.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
public class ExpiredProjectDTO {

	 private List<Long> deptIds;
	    private Integer days;
	
	 
}
