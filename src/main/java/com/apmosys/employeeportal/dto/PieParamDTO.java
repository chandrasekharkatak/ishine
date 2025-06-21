package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PieParamDTO {
	 private Integer inActiveFlag;
	    private Integer lowerAge;
	    private Integer upperAge;
		private String billable;
		private String billableType;
		private String employmentstatus;
		private String gender;
		private String experience;
		private List<CustomFilterDTO> queryList;
}
