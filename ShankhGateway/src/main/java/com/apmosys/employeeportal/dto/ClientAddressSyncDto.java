package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientAddressSyncDto {
	
	private Long clientAddressId;
    private String clientLocation;
    private String clientState;

}
