package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import com.apmosys.employeeportal.enums.ChangeType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RequirementChangeDTO {
	
	 private Long roleId;
	    private String role;
	    private String department;
	    private String experience;

	    private String lineItemStartDate;
	    private String lineItemEndDate;

	    private Long oldCount;
	    private Long newCount;

	    private ChangeType changeType;

	    private List<EmployeeImpactDTO> impactedEmployees = new ArrayList<>();

}
