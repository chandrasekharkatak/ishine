package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetailsForActivityDTO {

	private Integer clientId;
    private String clientName;

    private List<ClientLocationDTO> clientLocations = new ArrayList<>();
    private ProjectDetailsForActivityDTO project;
    
}
