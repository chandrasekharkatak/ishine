package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class AppreciationEventDTO {
	
private Long appreciationEventId;
	
	private String appreciationEventName;
	
	private String fromDate;
	
	private String toDate;
	
	private LocalDateTime createdOn;
	
    private List<EmployeeDTO> enableAppreciationList;
    
    private String appreciateType;
    
	private Long updatedBy;

}
