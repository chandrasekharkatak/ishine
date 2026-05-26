package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TravelApprovalMatrixDTO {

	private Long matrixId;
	private String name;
	private String createdByName;
	private Timestamp createdOn;
	private String updatedByName;
	private Timestamp updatedOn;
	private List<Long> applicabilityDepartmentIds = new ArrayList<>();
	private List<Long> applicabilityJobRoleIds = new ArrayList<>();
	private List<TravelApprovalMatrixLevelDTO> levels = new ArrayList<>();
	private TravelApprovalMatrixAdminDTO admin = new TravelApprovalMatrixAdminDTO();

	@Data
	public static class TravelApprovalMatrixLevelDTO {
		private Integer order;
		private List<Long> departmentIds = new ArrayList<>();
		private List<Long> jobRoleIds = new ArrayList<>();
		private String routing;
		private Long assigneeEmployeeId;
	}

	@Data
	public static class TravelApprovalMatrixAdminDTO {
		private Long departmentId;
		private Long assigneeEmployeeId;
	}
}
