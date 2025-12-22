package com.apmosys.employeeportal.dto;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApprovalRequest {
	public ProjectInsightResponseDetails response;
	public Boolean doReassign;
	public Boolean editedByApprover;
	public String reviewerName;
}
