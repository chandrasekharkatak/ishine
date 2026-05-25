package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

@Data
public class TravelDeskTicketSubmitRequestDTO {
	private BigInteger empId;
	private String fullName;
	private String email;
	private String departmentName;
	private String designationName;
	private String mobileNo;
	private BigInteger managerEmpId;
	private String managerName;
	private String managerEmail;
	private List<TravelDeskTicketLineInputDTO> lines;
}
