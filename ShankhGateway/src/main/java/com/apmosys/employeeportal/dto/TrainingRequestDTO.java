package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingRequestDTO {
	
	private Long empId;
	private Integer trainingId;
	private Integer contentId;
	private Integer cycleNumber;
}
