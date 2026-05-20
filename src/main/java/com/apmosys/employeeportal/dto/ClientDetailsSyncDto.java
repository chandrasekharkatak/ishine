package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetailsSyncDto {
	
	private Long clientid; //PO PK

	private String clientName; 

    private List<ClientAddressSyncDto> clientAddress;

}
