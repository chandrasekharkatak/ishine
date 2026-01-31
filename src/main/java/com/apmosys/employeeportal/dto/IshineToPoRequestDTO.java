package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IshineToPoRequestDTO {
	
	 	private Long poId;
	    private Integer projectId;              
	    private String projectName;

	    private Date startDateOfBilling;
	    private Date endDateOfBilling;
	    
	    private Long clientId;
	    private Long addressId;
	    
	    private Long userId;

}
