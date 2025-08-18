package com.apmosys.employeeportal.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {

	private Integer clientId;
	private String clientName;
	
}
