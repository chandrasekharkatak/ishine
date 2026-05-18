package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoProjectLookupResponseDTO {
	private Integer projectId;
	private Boolean hasClientSideId;
	private Long poProjectId;
}
