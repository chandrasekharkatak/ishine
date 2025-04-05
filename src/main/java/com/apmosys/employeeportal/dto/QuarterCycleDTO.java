package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import com.apmosys.employeeportal.model.Notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class QuarterCycleDTO {
	
    private Long quarterId;
    
    private Integer fromYear;
    
    private Integer toYear;
    
    private String fromMonth;
    
    private String toMonth;
	
	private String financialYear;
	
	private String quarterCycle;
	
	private  Long createdBy;
	
	private String createdByName;
	
	private String createdOn;
	
	private Long updatedBy;
	
	private String updatedByName;
	
	private String UpdatedOn;
	
	private Boolean isActive;  
    private Boolean isEnable;
	
	

}
