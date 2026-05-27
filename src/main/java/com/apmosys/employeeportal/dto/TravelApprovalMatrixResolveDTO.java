package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TravelApprovalMatrixResolveDTO {

	private Long matrixId;
	private String matrixName;
	/** Human-readable chain e.g. Reporting manager → Pool → Finance */
	private String approvalFlowSummary;
	private List<ApprovalLevelColumnDTO> levelColumns = new ArrayList<>();

	@Data
	public static class ApprovalLevelColumnDTO {
		private Integer order;
		private String levelLabel;
		private String routing;
		private boolean financeStep;
	}
}
