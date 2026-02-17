package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoClientAddressUpdateDTO {
	
	 private List<Long> poIds;

	    private Long clientAddressId;
	    private String clientLocation;
	    private String clientState;

	    private Long updatedByEmpId; 
	    private String updatedByEmpName;
	    private Date updatedOn;


}
