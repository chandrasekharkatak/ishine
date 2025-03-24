package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class KpiResponseDTO {
	
	private Long empId;
	
	private Long kpiId;

	private Long responseId;
	 private String approvedBy;
	    private LocalDateTime createdAt;

	
	private String response;
	
	private String remarks;
	
	private Float score;
	
	  private String departmentName;
	    private Long departmentId;
	    private Long quarterId;
	    
	    private String quarter;
}
	
	
	
	
	


