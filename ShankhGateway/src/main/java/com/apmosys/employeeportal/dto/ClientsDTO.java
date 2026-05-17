package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor

public class ClientsDTO {

	private Integer clientId;
	private String clientName;
    private Integer clientLocationId;
	private String clientLocation;
	
	public ClientsDTO(Integer clientId, String clientName, String clientLocation, Integer clientLocationId) {
		this.clientId = clientId;
		this.clientName = clientName;
		this.clientLocation = clientLocation;
		this.clientLocationId = clientLocationId;
	}
}
