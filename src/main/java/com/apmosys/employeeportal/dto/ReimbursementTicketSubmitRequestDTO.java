package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

@Data
public class ReimbursementTicketSubmitRequestDTO {
	private BigInteger empId;
	private String fullName;
	private String email;
	private String departmentName;
	private String designationName;
	private String mobileNo;
	private BigInteger hodEmpId;
	private String hodName;
	private String hodEmail;
	private List<ReimbursementTicketClaimInputDTO> claims;
}
