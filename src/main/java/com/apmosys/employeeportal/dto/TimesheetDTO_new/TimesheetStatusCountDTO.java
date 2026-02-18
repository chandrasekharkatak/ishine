package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimesheetStatusCountDTO {

	  private String smStatus;
	  private Long count;

	    public TimesheetStatusCountDTO(String smStatus, Long count) {
	        this.smStatus = smStatus;
	        this.count = count;
	    }

	  private Long managerId;
	  private Boolean clientFilter;

}
