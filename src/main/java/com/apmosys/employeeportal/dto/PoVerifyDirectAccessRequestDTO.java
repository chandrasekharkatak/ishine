package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoVerifyDirectAccessRequestDTO {
	private Long empId;
	private String poToken;
	private String deepLink;
}
