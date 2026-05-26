package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TravelApprovalMatrixSaveRequestDTO {

	private Long createdBy;
	private List<TravelApprovalMatrixDTO> matrices = new ArrayList<>();
}
