package com.apmosys.employeeportal.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class ReimbursementTicketActorDTO {
	private BigInteger empId;
	private String email;
}
