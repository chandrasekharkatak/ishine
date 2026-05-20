package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

@Data
public class ReimbursementTicketStageActionDTO {
	private Long ticketId;
	private BigInteger actorEmpId;
	private String actorEmail;
	private List<ReimbursementClaimDecisionDTO> decisions;
}
