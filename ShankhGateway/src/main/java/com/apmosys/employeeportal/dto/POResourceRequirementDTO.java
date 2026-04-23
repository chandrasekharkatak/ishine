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
public class POResourceRequirementDTO {
	
	private Long resourceOverviewId;
	private String role;
    private Long count;
    private String experience;
    private String department;
    private Long clientRoleId;
    private Date yearWiseRateCartStartDate;
    private Date yearWiseRateCartEndDate;
    private Date lineItemStartDate;
    private Date lineItemEndDate;
    private Long poId;

}
