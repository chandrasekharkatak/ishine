package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReimbursementTicketClaimInputDTO {
	private Integer lineNo;
	private String expenditureType;
	private BigDecimal amount;
	private String travelMode;
	private BigInteger distance;
	private String vehicleType;
	private String foodAllowanceType;
	private Date dateOfFood;
	private Date fromDate;
	private Date toDate;
	private String purpose;
	private String businessJustification;
	private Boolean hodApproval;
	private Date preApprovalDate;
	private List<Long> preApprovalDocIds;
	/** Project from employee RMG / timesheet mapping; -1 = Others, -2 = POC (see manual project name + client). */
	private Long projectId;
	/** Manual project title when projectId is -1 (Others). */
	private String othersProjectName;
	/** Manual project title when projectId is -2 (POC). */
	private String pocProjectName;
	/** Master {@code clients.client_id} when client is from the clients table. */
	private Integer clientId;
	/** Reimbursement-only client when chosen from a prior prospective entry. */
	private Long reimbursementClientId;
	/** Create/find prospective client by name (reimbursement_client table only). */
	private String prospectiveClientName;
	/** MASTER or PROSPECTIVE_NEW_CLIENT */
	private String clientCategory;
	private Boolean recurringExpense;
	private Boolean pocProject;
	private List<Long> docIds;
}
