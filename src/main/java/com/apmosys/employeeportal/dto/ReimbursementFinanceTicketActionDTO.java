package com.apmosys.employeeportal.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class ReimbursementFinanceTicketActionDTO {
	private Long ticketId;
	private BigInteger actorEmpId;
	private String actorEmail;
	/** PAID or REJECTED */
	private String action;
	private String remarks;
}
