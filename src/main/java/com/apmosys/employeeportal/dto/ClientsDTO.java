package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ClientsDTO {

	private Integer clientId;
	private String clientName;
    private Integer clientLocationId;
	private String clientLocation;
	
}
