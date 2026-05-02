package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoSessionLoginRequestDTO {

	/**
	 * Request: required. Response: echoed from request for Po convenience.
	 */
	private Long empId;

	/**
	 * Request: omit (null). Response: current or newly issued {@code user_session.po_token}.
	 */
	private String poToken;
}
