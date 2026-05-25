package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReimbursementTicketClaimInputDTO {
	private Integer lineNo;
	private String expenditureType;
	private BigDecimal amount;
	private String travelMode;
	private BigInteger distance;
	private String vehicleType;
	private String foodAllowanceType;
	private Date dateOfFood;
	private Date fromDate;
	private Date toDate;
	private String purpose;
	/** Project from employee RMG / timesheet mapping; persisted on claim. Use -1 for BD "Others" (see othersProjectName + clientId). */
	private Long projectId;
	/** Manual project title when projectId is -1 (Business Development only; validated server-side). */
	private String othersProjectName;
	/** Required when projectId is -1; also persisted for normal projects when resolved from project master. */
	private Integer clientId;
	private List<Long> docIds;
}
