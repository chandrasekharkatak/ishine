package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ReimbursementApprovalMatrixSaveRequestDTO {

	private Long createdBy;
	private List<ReimbursementApprovalMatrixDTO> matrices = new ArrayList<>();
}
