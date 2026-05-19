package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GrievanceAuditDiffResponseDTO {

	private Integer version1;
	private Integer version2;
	private List<GrievanceAuditFieldChangeDTO> changes = new ArrayList<>();
}
