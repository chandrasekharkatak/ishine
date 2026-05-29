package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO.ReimbursementApprovalMatrixLevelDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.ReimbursementTicket;
import com.apmosys.employeeportal.model.ReimbursementTicketClaim;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;

@Service
public class ReimbursementTicketMatrixWorkflowService {

	@Autowired
	private ReimbursementApprovalMatrixService matrixService;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Value("${reimbursement.workflow.hr.mail:hrm2@apmosys.com}")
	private String workflowHrMail;

	@Value("${reimbursement.workflow.finance.mail:finance2@apmosys.com}")
	private String workflowFinanceMail;

	public boolean usesMatrixWorkflow(ReimbursementTicket ticket) {
		return ticket != null && ticket.getApprovalMatrixId() != null;
	}

	public void initializeOnSubmit(ReimbursementTicket ticket) {
		Long empId = ticket.getEmpId() != null ? ticket.getEmpId().longValue() : null;
		ReimbursementApprovalMatrixDTO matrix = matrixService.findMatchingMatrixForEmployee(empId,
				ticket.getDepartment());
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_HOD);
			for (ReimbursementTicketClaim cl : ticket.getClaims()) {
				cl.setClaimStatus(ReimbursementTicketClaim.STATUS_PENDING_HOD);
			}
			return;
		}
		ticket.setApprovalMatrixId(matrix.getMatrixId());
		ticket.setCurrentLevelOrder(1);
		ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_LEVEL);
		for (ReimbursementTicketClaim cl : ticket.getClaims()) {
			cl.setClaimStatus(ReimbursementTicketClaim.STATUS_PENDING_APPROVAL);
		}
		applyCurrentLevelAssignee(ticket, matrix);
	}

	/** True when the actor may approve at the ticket's current matrix level (pending actions only). */
	public boolean canActorApprove(ReimbursementTicket ticket, BigInteger actorEmpId, String actorEmail) {
		if (!usesMatrixWorkflow(ticket)
				|| !ReimbursementTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
			return false;
		}
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return false;
		}
		ReimbursementApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			return false;
		}
		Long actorId = actorEmpId != null ? actorEmpId.longValue() : null;
		return actorMatchesLevel(ticket, level, actorId);
	}

	/**
	 * True when the actor is configured as an approver at any level in the ticket's matrix
	 * (past, current, or future), for read-only "all assigned tickets" lists.
	 */
	public boolean isActorInApprovalChain(ReimbursementTicket ticket, BigInteger actorEmpId, String actorEmail) {
		if (!usesMatrixWorkflow(ticket)) {
			return false;
		}
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			return false;
		}
		Long actorId = actorEmpId != null ? actorEmpId.longValue() : null;
		if (actorId == null) {
			return false;
		}
		for (ReimbursementApprovalMatrixLevelDTO level : matrix.getLevels()) {
			if (actorMatchesLevel(ticket, level, actorId)) {
				return true;
			}
		}
		return false;
	}

	private boolean actorMatchesLevel(ReimbursementTicket ticket, ReimbursementApprovalMatrixLevelDTO level,
			Long actorId) {
		if (actorId == null || level == null) {
			return false;
		}
		String routing = normRouting(level.getRouting());
		switch (routing) {
		case "REPORTING_MANAGER":
			return actorId.equals(resolveReportingManagerEmpId(ticket));
		case "HOD_SUBMITTER_DEPT":
			return ticket.getHodEmpId() != null && actorId.equals(ticket.getHodEmpId().longValue());
		case "SPECIFIC_IN_SCOPE":
			return level.getAssigneeEmployeeId() != null && level.getAssigneeEmployeeId().equals(actorId);
		case "POOL_ANY_IN_SCOPE":
			return poolMemberEmpIds(level).contains(actorId);
		default:
			return false;
		}
	}

	public String notifyEmailForCurrentLevel(ReimbursementTicket ticket) {
		List<String> all = notifyEmailsForCurrentLevelAll(ticket);
		return all.isEmpty() ? null : all.get(0);
	}

	/** All approver mailbox(es) for the ticket's current matrix level (pool = every member). */
	public List<String> notifyEmailsForCurrentLevelAll(ReimbursementTicket ticket) {
		java.util.LinkedHashSet<String> emails = new java.util.LinkedHashSet<>();
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return new ArrayList<>();
		}
		ReimbursementApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			return new ArrayList<>();
		}
		Long assignee = ticket.getCurrentAssigneeEmpId();
		if (assignee != null) {
			addEmployeeEmail(emails, assignee);
		}
		String routing = normRouting(level.getRouting());
		if ("REPORTING_MANAGER".equals(routing)) {
			Long mgrId = resolveReportingManagerEmpId(ticket);
			if (mgrId != null) {
				addEmployeeEmail(emails, mgrId);
			}
		}
		if ("HOD_SUBMITTER_DEPT".equals(routing) && StringUtils.hasText(ticket.getHodEmail())) {
			emails.add(ticket.getHodEmail().trim().toLowerCase(Locale.ROOT));
		}
		if ("SPECIFIC_IN_SCOPE".equals(routing) && level.getAssigneeEmployeeId() != null) {
			addEmployeeEmail(emails, level.getAssigneeEmployeeId());
		}
		if ("POOL_ANY_IN_SCOPE".equals(routing)) {
			for (Long id : poolMemberEmpIds(level)) {
				addEmployeeEmail(emails, id);
			}
		}
		return new ArrayList<>(emails);
	}

	private void addEmployeeEmail(java.util.Set<String> emails, Long empId) {
		if (empId == null) {
			return;
		}
		Employee e = employeeRepository.findByEmpId(empId);
		if (e != null && StringUtils.hasText(e.getEmail())) {
			emails.add(e.getEmail().trim().toLowerCase(Locale.ROOT));
		}
	}

	public String notifyApproverDisplayName(ReimbursementTicket ticket) {
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return "Approver";
		}
		ReimbursementApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			return "Approver";
		}
		Long assignee = ticket.getCurrentAssigneeEmpId();
		if (assignee != null) {
			Employee e = employeeRepository.findByEmpId(assignee);
			if (e != null && StringUtils.hasText(e.getName())) {
				return e.getName().trim();
			}
		}
		String display = plannedApproverDisplay(level, ticket);
		return StringUtils.hasText(display) && !"—".equals(display) ? display : "Approver";
	}

	public String currentLevelLabel(ReimbursementTicket ticket) {
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return "Approval level";
		}
		ReimbursementApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			return "Approval level";
		}
		return matrixService.formatLevelLabel(level);
	}

	public void advanceAfterLevelDecisions(ReimbursementTicket ticket) {
		ReimbursementApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return;
		}
		boolean anyPendingApproval = ticket.getClaims().stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(c.getClaimStatus()));
		if (!anyPendingApproval) {
			ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
			return;
		}
		int current = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		int maxLevel = matrix.getLevels() != null ? matrix.getLevels().size() : 0;
		if (current < maxLevel) {
			ticket.setCurrentLevelOrder(current + 1);
			ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_LEVEL);
			applyCurrentLevelAssignee(ticket, matrix);
			return;
		}
		for (ReimbursementTicketClaim c : ticket.getClaims()) {
			if (ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(c.getClaimStatus())) {
				c.setClaimStatus(ReimbursementTicketClaim.STATUS_PENDING_FINANCE);
			}
		}
		ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_FINANCE);
		ticket.setCurrentAssigneeEmpId(null);
	}

	public List<Map<String, Object>> buildApprovalLevels(ReimbursementTicket ticket) {
		ReimbursementApprovalMatrixDTO matrix = ticket.getApprovalMatrixId() != null
				? matrixService.getMatrixById(ticket.getApprovalMatrixId())
				: matrixService.findMatchingMatrixForEmployee(
						ticket.getEmpId() != null ? ticket.getEmpId().longValue() : null, ticket.getDepartment());
		List<Map<String, Object>> out = new ArrayList<>();
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			return out;
		}
		int currentOrder = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		String stage = ticket.getWorkflowStage();
		boolean heldForCycle = ReimbursementTicket.STAGE_HELD_FOR_CYCLE.equals(stage);
		boolean atFinance = ReimbursementTicket.STAGE_PENDING_FINANCE.equals(stage)
				|| ReimbursementTicket.STAGE_PAID.equals(stage);
		boolean closedRejected = ReimbursementTicket.STAGE_REJECTED.equals(stage);
		boolean atLevelStage = ReimbursementTicket.STAGE_PENDING_LEVEL.equals(stage);

		for (ReimbursementApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
			int order = lvl.getOrder() != null ? lvl.getOrder() : out.size() + 1;
			String label = matrixService.formatLevelLabel(lvl);
			String name = resolveApproverDisplayName(lvl, ticket, atLevelStage && order == currentOrder);
			String status;
			if (heldForCycle) {
				status = order == 1 ? "Queued" : "—";
			} else if (closedRejected) {
				status = order < currentOrder || (!atLevelStage && order <= currentOrder) ? levelAggregateStatus(ticket, order)
						: (order == currentOrder && atLevelStage ? levelAggregateStatus(ticket, order) : "—");
			} else if (order < currentOrder || (atFinance && order <= matrix.getLevels().size())) {
				status = levelAggregateStatus(ticket, order);
				if ("—".equals(status) && atFinance) {
					status = "Approved";
				}
			} else if (atLevelStage && order == currentOrder) {
				status = levelAggregateStatus(ticket, order);
			} else {
				status = "—";
			}
			out.add(approvalLevelRow(order, label, name, status, false));
		}
		String finName = lookupEmployeeNameByEmail(workflowFinanceMail);
		String finStatus = heldForCycle ? "—" : financeAggregateStatus(ticket);
		out.add(approvalLevelRow(matrix.getLevels().size() + 1, "Finance", finName, finStatus, true));
		return out;
	}

	private String levelAggregateStatus(ReimbursementTicket ticket, int levelOrder) {
		if (!usesMatrixWorkflow(ticket)) {
			return "—";
		}
		int current = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		String stage = ticket.getWorkflowStage();
		if (ReimbursementTicket.STAGE_PENDING_LEVEL.equals(stage) && levelOrder == current) {
			boolean anyPending = ticket.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(c.getClaimStatus()));
			if (anyPending) {
				return "Pending";
			}
		}
		if (ReimbursementTicket.STAGE_PENDING_LEVEL.equals(stage) && levelOrder > current) {
			return "—";
		}
		boolean anyLevelRejected = ticket.getClaims().stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(c.getClaimStatus()));
		boolean anyPast = ticket.getClaims().stream().anyMatch(c -> {
			String s = c.getClaimStatus();
			return ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(s)
					|| ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(s)
					|| ReimbursementTicketClaim.STATUS_PAID.equals(s)
					|| (ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(s) && levelOrder < current);
		});
		if (anyLevelRejected && anyPast && levelOrder <= current) {
			return "Partial";
		}
		if (anyLevelRejected && levelOrder == current && ReimbursementTicket.STAGE_REJECTED.equals(stage)) {
			return "Rejected";
		}
		if (levelOrder < current || ReimbursementTicket.STAGE_PENDING_FINANCE.equals(stage)
				|| ReimbursementTicket.STAGE_PAID.equals(stage)) {
			return "Approved";
		}
		return "—";
	}

	private String financeAggregateStatus(ReimbursementTicket ticket) {
		List<ReimbursementTicketClaim> claims = ticket.getClaims();
		if (claims == null || claims.isEmpty()) {
			return "—";
		}
		List<ReimbursementTicketClaim> financeScope = claims.stream().filter(c -> {
			String s = c.getClaimStatus();
			return ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(s)
					|| ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(s)
					|| ReimbursementTicketClaim.STATUS_PAID.equals(s);
		}).collect(Collectors.toList());
		if (financeScope.isEmpty()) {
			return "—";
		}
		if (financeScope.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus()))) {
			return "Pending";
		}
		boolean anyFinRejected = financeScope.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(c.getClaimStatus()));
		boolean anyPaid = financeScope.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()));
		if (anyFinRejected && anyPaid) {
			return "Partial";
		}
		if (anyFinRejected) {
			return "Rejected";
		}
		if (anyPaid) {
			return "Approved";
		}
		return "—";
	}

	private void applyCurrentLevelAssignee(ReimbursementTicket ticket, ReimbursementApprovalMatrixDTO matrix) {
		ReimbursementApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			ticket.setCurrentAssigneeEmpId(null);
			return;
		}
		String routing = normRouting(level.getRouting());
		Long assignee = null;
		switch (routing) {
		case "REPORTING_MANAGER":
			assignee = resolveReportingManagerEmpId(ticket);
			break;
		case "HOD_SUBMITTER_DEPT":
			assignee = ticket.getHodEmpId() != null ? ticket.getHodEmpId().longValue() : null;
			break;
		case "SPECIFIC_IN_SCOPE":
			assignee = level.getAssigneeEmployeeId();
			break;
		case "POOL_ANY_IN_SCOPE":
			assignee = null;
			break;
		default:
			break;
		}
		ticket.setCurrentAssigneeEmpId(assignee);
	}

	private Long resolveReportingManagerEmpId(ReimbursementTicket ticket) {
		if (ticket.getEmpId() == null) {
			return null;
		}
		Employee emp = employeeRepository.findByEmpId(ticket.getEmpId().longValue());
		return emp != null ? emp.getManagerId() : null;
	}

	private Set<Long> poolMemberEmpIds(ReimbursementApprovalMatrixLevelDTO level) {
		Set<Long> deptIds = level.getDepartmentIds() != null ? new HashSet<>(level.getDepartmentIds())
				: Collections.emptySet();
		Set<Long> roleIds = level.getJobRoleIds() != null ? new HashSet<>(level.getJobRoleIds())
				: Collections.emptySet();
		if (deptIds.isEmpty() && roleIds.isEmpty()) {
			return Collections.emptySet();
		}
		Set<Long> out = new HashSet<>();
		List<Long> deptList = new ArrayList<>(deptIds);
		List<EmployeeDTO> candidates = deptList.isEmpty() ? Collections.emptyList()
				: employeeRepository.getAllEmployeesByDepartmentIds(deptList);
		for (EmployeeDTO dto : candidates) {
			if (dto == null || dto.getEmpId() == null) {
				continue;
			}
			Long jrId = dto.getJobRoleId();
			if (!roleIds.isEmpty() && (jrId == null || !roleIds.contains(jrId))) {
				continue;
			}
			if (!deptIds.isEmpty() && jrId != null) {
				Long jrDept = jobRoleRepository.findDeptIdByJobRoleId(jrId);
				if (jrDept != null && !deptIds.contains(jrDept)) {
					continue;
				}
			}
			out.add(dto.getEmpId());
		}
		return out;
	}

	private ReimbursementApprovalMatrixLevelDTO levelAtOrder(ReimbursementApprovalMatrixDTO matrix, Integer order) {
		if (matrix == null || matrix.getLevels() == null || order == null) {
			return null;
		}
		for (ReimbursementApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
			if (order.equals(lvl.getOrder())) {
				return lvl;
			}
		}
		if (order >= 1 && order <= matrix.getLevels().size()) {
			return matrix.getLevels().get(order - 1);
		}
		return null;
	}

	private String normRouting(String routing) {
		return routing == null ? "" : routing.trim().toUpperCase(Locale.ROOT);
	}

	/** Prefer the active assignee on the current level; otherwise matrix routing rules. */
	public String resolveApproverDisplayName(ReimbursementApprovalMatrixLevelDTO lvl, ReimbursementTicket t,
			boolean useCurrentAssignee) {
		if (useCurrentAssignee && t.getCurrentAssigneeEmpId() != null) {
			Employee assignee = employeeRepository.findByEmpId(t.getCurrentAssigneeEmpId().longValue());
			if (assignee != null && StringUtils.hasText(assignee.getName())) {
				return assignee.getName().trim();
			}
		}
		return plannedApproverDisplay(lvl, t);
	}

	public String plannedApproverDisplay(ReimbursementApprovalMatrixLevelDTO lvl, ReimbursementTicket t) {
		if (lvl == null) {
			return "—";
		}
		String routing = normRouting(lvl.getRouting());
		if ("REPORTING_MANAGER".equals(routing)) {
			Long mgrId = resolveReportingManagerEmpId(t);
			if (mgrId != null) {
				Employee mgr = employeeRepository.findByEmpId(mgrId);
				if (mgr != null && StringUtils.hasText(mgr.getName())) {
					return mgr.getName();
				}
			}
		}
		if ("HOD_SUBMITTER_DEPT".equals(routing)) {
			return t.getHodName();
		}
		if (lvl.getAssigneeEmployeeId() != null) {
			Employee assignee = employeeRepository.findByEmpId(lvl.getAssigneeEmployeeId());
			if (assignee != null && StringUtils.hasText(assignee.getName())) {
				return assignee.getName();
			}
		}
		if ("POOL_ANY_IN_SCOPE".equals(routing)) {
			return "Approver pool";
		}
		if ("SPECIFIC_IN_SCOPE".equals(routing)) {
			return "—";
		}
		return "—";
	}

	private String lookupEmployeeNameByEmail(String email) {
		if (!StringUtils.hasText(email)) {
			return "—";
		}
		Employee e = employeeRepository.findByEmail(email.trim());
		return e != null && StringUtils.hasText(e.getName()) ? e.getName() : email.trim();
	}

	private Map<String, Object> approvalLevelRow(int order, String levelLabel, String approverName, String approverStatus,
			boolean financeStep) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("order", order);
		row.put("levelLabel", levelLabel);
		row.put("approverName", StringUtils.hasText(approverName) ? approverName : "—");
		row.put("approverStatus", StringUtils.hasText(approverStatus) ? approverStatus : "—");
		row.put("financeStep", financeStep);
		return row;
	}
}
