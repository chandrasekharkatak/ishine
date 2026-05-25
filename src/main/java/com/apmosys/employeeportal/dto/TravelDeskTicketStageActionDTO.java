package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

@Data
public class TravelDeskTicketStageActionDTO {
	private Long ticketId;
	private BigInteger actorEmpId;
	private String actorEmail;
	private List<TravelLineDecisionDTO> decisions;
	/** BOOKED or REJECTED (Travel Admin stage). */
	private String action;
	private String remarks;
	private List<TravelLineAdminFulfillmentDTO> lineFulfillments;
}
