package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO.TravelApprovalMatrixLevelDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.TravelDeskTicket;
import com.apmosys.employeeportal.model.TravelDeskTicketLine;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;

@Service
public class TravelDeskTicketMatrixWorkflowService {

	@Autowired
	private TravelApprovalMatrixService matrixService;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Value("${travel.workflow.hr.mail:hrm2@apmosys.com}")
	private String workflowHrMail;

	@Value("${travel.workflow.admin.mail:finance2@apmosys.com}")
	private String workflowAdminMail;

	public boolean usesMatrixWorkflow(TravelDeskTicket ticket) {
		return ticket != null && ticket.getApprovalMatrixId() != null;
	}

	public void initializeOnSubmit(TravelDeskTicket ticket) {
		Long empId = ticket.getEmpId() != null ? ticket.getEmpId().longValue() : null;
		TravelApprovalMatrixDTO matrix = matrixService.findMatchingMatrixForEmployee(empId,
				ticket.getDepartment());
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			ticket.setApprovalMatrixId(null);
			ticket.setCurrentLevelOrder(1);
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_PENDING_LEVEL);
			for (TravelDeskTicketLine cl : ticket.getLines()) {
				cl.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_APPROVAL);
			}
			ticket.setCurrentAssigneeEmpId(ticket.getManagerEmpId() != null
					? ticket.getManagerEmpId().longValue()
					: null);
			return;
		}
		ticket.setApprovalMatrixId(matrix.getMatrixId());
		ticket.setCurrentLevelOrder(1);
		ticket.setWorkflowStage(TravelDeskTicket.STAGE_PENDING_LEVEL);
		for (TravelDeskTicketLine cl : ticket.getLines()) {
			cl.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_APPROVAL);
		}
		applyCurrentLevelAssignee(ticket, matrix);
	}

	/** True when the actor may complete Travel Admin booking for a ticket at {@code PENDING_ADMIN}. */
	public boolean canActorFulfillAsTravelAdmin(TravelDeskTicket ticket, BigInteger actorEmpId, String actorEmail,
			String workflowAdminMail) {
		if (!TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
			return false;
		}
		String email = actorEmail == null ? "" : actorEmail.trim().toLowerCase(Locale.ROOT);
		if (StringUtils.hasText(workflowAdminMail)
				&& email.equals(workflowAdminMail.trim().toLowerCase(Locale.ROOT))) {
			return true;
		}
		if (actorEmpId == null || ticket.getCurrentAssigneeEmpId() == null) {
			return false;
		}
		return actorEmpId.longValue() == ticket.getCurrentAssigneeEmpId().longValue();
	}

	/** True when the actor may approve at the ticket's current matrix level (pending actions only). */
	public boolean canActorApprove(TravelDeskTicket ticket, BigInteger actorEmpId, String actorEmail) {
		if (!TravelDeskTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
			return false;
		}
		if (!usesMatrixWorkflow(ticket)) {
			Long actorId = actorEmpId != null ? actorEmpId.longValue() : null;
			if (ticket.getCurrentAssigneeEmpId() != null && actorId != null
					&& actorId.equals(ticket.getCurrentAssigneeEmpId())) {
				return true;
			}
			return StringUtils.hasText(ticket.getManagerEmail())
					&& normEmail(ticket.getManagerEmail()).equals(normEmail(actorEmail));
		}
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return false;
		}
		TravelApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
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
	public boolean isActorInApprovalChain(TravelDeskTicket ticket, BigInteger actorEmpId, String actorEmail) {
		Long actorId = actorEmpId != null ? actorEmpId.longValue() : null;
		String email = normEmail(actorEmail);
		if (!usesMatrixWorkflow(ticket)) {
			if (actorId != null && ticket.getManagerEmpId() != null
					&& actorId.equals(ticket.getManagerEmpId().longValue())) {
				return true;
			}
			if (StringUtils.hasText(ticket.getManagerEmail())
					&& normEmail(ticket.getManagerEmail()).equals(email)) {
				return true;
			}
			if (StringUtils.hasText(workflowAdminMail)
					&& normEmail(workflowAdminMail).equals(email)) {
				return true;
			}
			if (ticket.getCurrentAssigneeEmpId() != null && actorId != null
					&& actorId.equals(ticket.getCurrentAssigneeEmpId())) {
				return true;
			}
			return false;
		}
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			return false;
		}
		if (actorId == null) {
			return false;
		}
		for (TravelApprovalMatrixLevelDTO level : matrix.getLevels()) {
			if (actorMatchesLevel(ticket, level, actorId)) {
				return true;
			}
		}
		if (actorIsConfiguredTravelAdmin(matrix, actorId)) {
			return true;
		}
		if (ticket.getCurrentAssigneeEmpId() != null
				&& actorId.equals(ticket.getCurrentAssigneeEmpId().longValue())) {
			return true;
		}
		return false;
	}

	/** True when the employee is the Travel Admin assignee on the ticket's approval matrix. */
	private boolean actorIsConfiguredTravelAdmin(TravelApprovalMatrixDTO matrix, Long actorId) {
		if (matrix == null || matrix.getAdmin() == null || actorId == null) {
			return false;
		}
		Long assignee = matrix.getAdmin().getAssigneeEmployeeId();
		return assignee != null && assignee.equals(actorId);
	}

	private boolean actorMatchesLevel(TravelDeskTicket ticket, TravelApprovalMatrixLevelDTO level,
			Long actorId) {
		if (actorId == null || level == null) {
			return false;
		}
		String routing = normRouting(level.getRouting());
		switch (routing) {
		case "REPORTING_MANAGER":
			return actorId.equals(resolveReportingManagerEmpId(ticket));
		case "HOD_SUBMITTER_DEPT":
			return ticket.getManagerEmpId() != null && actorId.equals(ticket.getManagerEmpId().longValue());
		case "SPECIFIC_IN_SCOPE":
			return level.getAssigneeEmployeeId() != null && level.getAssigneeEmployeeId().equals(actorId);
		case "POOL_ANY_IN_SCOPE":
			return poolMemberEmpIds(level).contains(actorId);
		default:
			return false;
		}
	}

	public String notifyEmailForCurrentLevel(TravelDeskTicket ticket) {
		List<String> all = notifyEmailsForCurrentLevelAll(ticket);
		return all.isEmpty() ? null : all.get(0);
	}

	/** All approver mailbox(es) for the ticket's current matrix level (pool = every member). */
	public List<String> notifyEmailsForCurrentLevelAll(TravelDeskTicket ticket) {
		java.util.LinkedHashSet<String> emails = new java.util.LinkedHashSet<>();
		if (!usesMatrixWorkflow(ticket)) {
			if (StringUtils.hasText(ticket.getManagerEmail())) {
				emails.add(normEmail(ticket.getManagerEmail()));
			}
			return new ArrayList<>(emails);
		}
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return new ArrayList<>();
		}
		TravelApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
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
		if ("HOD_SUBMITTER_DEPT".equals(routing) && StringUtils.hasText(ticket.getManagerEmail())) {
			emails.add(ticket.getManagerEmail().trim().toLowerCase(Locale.ROOT));
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

	public String notifyApproverDisplayName(TravelDeskTicket ticket) {
		if (!usesMatrixWorkflow(ticket)) {
			return StringUtils.hasText(ticket.getManagerName()) ? ticket.getManagerName().trim() : "HOD";
		}
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return "Approver";
		}
		TravelApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
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

	public String currentLevelLabel(TravelDeskTicket ticket) {
		if (!usesMatrixWorkflow(ticket)) {
			boolean atOrPastAdmin = TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())
					|| TravelDeskTicket.STAGE_COMPLETED.equals(ticket.getWorkflowStage())
					|| ticket.getLines().stream().anyMatch(line -> {
						String status = line.getLineStatus();
						return TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(status)
								|| TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(status)
								|| TravelDeskTicketLine.STATUS_FULFILLED.equals(status);
					});
			if (atOrPastAdmin) {
				return "Travel Admin";
			}
			return "HOD";
		}
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		if (matrix == null) {
			return "Approval level";
		}
		TravelApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
		if (level == null) {
			return "Approval level";
		}
		return matrixService.formatLevelLabel(level);
	}

	public void advanceAfterLevelDecisions(TravelDeskTicket ticket) {
		TravelApprovalMatrixDTO matrix = matrixService.getMatrixById(ticket.getApprovalMatrixId());
		boolean anyPendingApproval = ticket.getLines().stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(c.getLineStatus()));
		if (!anyPendingApproval) {
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
			return;
		}
		if (matrix == null) {
			for (TravelDeskTicketLine c : ticket.getLines()) {
				if (TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(c.getLineStatus())) {
					c.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_ADMIN);
				}
			}
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_PENDING_ADMIN);
			ticket.setCurrentAssigneeEmpId(null);
			return;
		}
		int current = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		int maxLevel = matrix.getLevels() != null ? matrix.getLevels().size() : 0;
		if (current < maxLevel) {
			ticket.setCurrentLevelOrder(current + 1);
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_PENDING_LEVEL);
			applyCurrentLevelAssignee(ticket, matrix);
			return;
		}
		for (TravelDeskTicketLine c : ticket.getLines()) {
			if (TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(c.getLineStatus())) {
				c.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_ADMIN);
			}
		}
		ticket.setWorkflowStage(TravelDeskTicket.STAGE_PENDING_ADMIN);
		Long adminAssignee = matrix.getAdmin() != null ? matrix.getAdmin().getAssigneeEmployeeId() : null;
		ticket.setCurrentAssigneeEmpId(adminAssignee);
	}

	public List<Map<String, Object>> buildApprovalLevels(TravelDeskTicket ticket) {
		TravelApprovalMatrixDTO matrix = ticket.getApprovalMatrixId() != null
				? matrixService.getMatrixById(ticket.getApprovalMatrixId())
				: matrixService.findMatchingMatrixForEmployee(
						ticket.getEmpId() != null ? ticket.getEmpId().longValue() : null, ticket.getDepartment());
		List<Map<String, Object>> out = new ArrayList<>();
		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			out.add(approvalLevelRow(1, "HOD",
					StringUtils.hasText(ticket.getManagerName()) ? ticket.getManagerName().trim() : "—",
					fallbackHodStatus(ticket), false));
			out.add(approvalLevelRow(2, "Travel Admin",
					plannedTravelAdminDisplay(null, ticket),
					adminAggregateStatus(ticket), true));
			return out;
		}
		int currentOrder = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		String stage = ticket.getWorkflowStage();
		boolean atAdminStage = TravelDeskTicket.STAGE_PENDING_ADMIN.equals(stage)
				|| TravelDeskTicket.STAGE_COMPLETED.equals(stage);
		boolean closedRejected = TravelDeskTicket.STAGE_REJECTED.equals(stage);
		boolean atLevelStage = TravelDeskTicket.STAGE_PENDING_LEVEL.equals(stage);

		for (TravelApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
			int order = lvl.getOrder() != null ? lvl.getOrder() : out.size() + 1;
			String label = matrixService.formatLevelLabel(lvl);
			String name = plannedApproverDisplay(lvl, ticket);
			String status;
			if (closedRejected) {
				status = order < currentOrder || (!atLevelStage && order <= currentOrder) ? levelAggregateStatus(ticket, order)
						: (order == currentOrder && atLevelStage ? levelAggregateStatus(ticket, order) : "—");
			} else if (order < currentOrder || (atAdminStage && order <= matrix.getLevels().size())) {
				status = levelAggregateStatus(ticket, order);
				if ("—".equals(status) && atAdminStage) {
					status = "Approved";
				}
			} else if (atLevelStage && order == currentOrder) {
				status = levelAggregateStatus(ticket, order);
			} else {
				status = "—";
			}
			out.add(approvalLevelRow(order, label, name, status, false));
		}
		String finName = plannedTravelAdminDisplay(matrix, ticket);
		String finStatus = adminAggregateStatus(ticket);
		out.add(approvalLevelRow(matrix.getLevels().size() + 1, "Travel Admin", finName, finStatus, true));
		return out;
	}

	private String levelAggregateStatus(TravelDeskTicket ticket, int levelOrder) {
		if (!usesMatrixWorkflow(ticket)) {
			return "—";
		}
		int current = ticket.getCurrentLevelOrder() != null ? ticket.getCurrentLevelOrder() : 1;
		String stage = ticket.getWorkflowStage();
		if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(stage) && levelOrder == current) {
			boolean anyPending = ticket.getLines().stream()
					.anyMatch(c -> TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(c.getLineStatus()));
			if (anyPending) {
				return "Pending";
			}
		}
		if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(stage) && levelOrder > current) {
			return "—";
		}
		boolean anyLevelRejected = ticket.getLines().stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_LEVEL_REJECTED.equals(c.getLineStatus()));
		boolean anyPast = ticket.getLines().stream().anyMatch(c -> {
			String s = c.getLineStatus();
			return TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(s)
					|| TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(s)
					|| TravelDeskTicketLine.STATUS_FULFILLED.equals(s)
					|| (TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(s) && levelOrder < current);
		});
		if (anyLevelRejected && anyPast && levelOrder <= current) {
			return "Partial";
		}
		if (anyLevelRejected && levelOrder == current && TravelDeskTicket.STAGE_REJECTED.equals(stage)) {
			return "Rejected";
		}
		if (levelOrder < current || TravelDeskTicket.STAGE_PENDING_ADMIN.equals(stage)
				|| TravelDeskTicket.STAGE_COMPLETED.equals(stage)) {
			return "Approved";
		}
		return "—";
	}

	private String adminAggregateStatus(TravelDeskTicket ticket) {
		List<TravelDeskTicketLine> claims = ticket.getLines();
		if (claims == null || claims.isEmpty()) {
			return "—";
		}
		List<TravelDeskTicketLine> financeScope = claims.stream().filter(c -> {
			String s = c.getLineStatus();
			return TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(s)
					|| TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(s)
					|| TravelDeskTicketLine.STATUS_FULFILLED.equals(s);
		}).collect(Collectors.toList());
		if (financeScope.isEmpty()) {
			return "—";
		}
		if (financeScope.stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(c.getLineStatus()))) {
			return "Pending";
		}
		boolean anyFinRejected = financeScope.stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(c.getLineStatus()));
		boolean anyPaid = financeScope.stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_FULFILLED.equals(c.getLineStatus()));
		if (anyFinRejected && anyPaid) {
			return "Partial";
		}
		if (anyFinRejected) {
			return "Rejected";
		}
		if (anyPaid) {
			return "Booked";
		}
		return "—";
	}

	private void applyCurrentLevelAssignee(TravelDeskTicket ticket, TravelApprovalMatrixDTO matrix) {
		if (matrix == null) {
			ticket.setCurrentAssigneeEmpId(ticket.getManagerEmpId() != null
					? ticket.getManagerEmpId().longValue()
					: null);
			return;
		}
		TravelApprovalMatrixLevelDTO level = levelAtOrder(matrix, ticket.getCurrentLevelOrder());
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
			assignee = ticket.getManagerEmpId() != null ? ticket.getManagerEmpId().longValue() : null;
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

	private Long resolveReportingManagerEmpId(TravelDeskTicket ticket) {
		if (ticket.getEmpId() == null) {
			return null;
		}
		Employee emp = employeeRepository.findByEmpId(ticket.getEmpId().longValue());
		return emp != null ? emp.getManagerId() : null;
	}

	private Set<Long> poolMemberEmpIds(TravelApprovalMatrixLevelDTO level) {
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

	private TravelApprovalMatrixLevelDTO levelAtOrder(TravelApprovalMatrixDTO matrix, Integer order) {
		if (matrix == null || matrix.getLevels() == null || order == null) {
			return null;
		}
		for (TravelApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
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

	public String plannedApproverDisplay(TravelApprovalMatrixLevelDTO lvl, TravelDeskTicket t) {
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
			return t.getManagerName();
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

	private String plannedTravelAdminDisplay(TravelApprovalMatrixDTO matrix, TravelDeskTicket ticket) {
		if (matrix != null && matrix.getAdmin() != null && matrix.getAdmin().getAssigneeEmployeeId() != null) {
			String fromMatrix = employeeNameByEmpId(matrix.getAdmin().getAssigneeEmployeeId());
			if (StringUtils.hasText(fromMatrix)) {
				return fromMatrix;
			}
		}
		if (ticket != null && ticket.getCurrentAssigneeEmpId() != null) {
			String fromTicket = employeeNameByEmpId(ticket.getCurrentAssigneeEmpId().longValue());
			if (StringUtils.hasText(fromTicket)) {
				return fromTicket;
			}
		}
		return lookupEmployeeNameByEmail(workflowAdminMail);
	}

	private String employeeNameByEmpId(Long empId) {
		if (empId == null) {
			return null;
		}
		Employee e = employeeRepository.findByEmpId(empId);
		return e != null && StringUtils.hasText(e.getName()) ? e.getName().trim() : null;
	}

	private String lookupEmployeeNameByEmail(String email) {
		if (!StringUtils.hasText(email)) {
			return "—";
		}
		String normalized = email.trim();
		Employee e = employeeRepository.findByEmail(normalized);
		if (e == null && normalized.contains("@")) {
			e = employeeRepository.findFirstByEmailLocalPartIgnoreCase(
					normalized.substring(0, normalized.indexOf('@') + 1));
		}
		return e != null && StringUtils.hasText(e.getName()) ? e.getName().trim() : normalized;
	}

	private String fallbackHodStatus(TravelDeskTicket ticket) {
		String stage = ticket.getWorkflowStage();
		if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(stage)) {
			return "Pending";
		}
		boolean anyApprovedPastHod = ticket.getLines().stream().anyMatch(c -> {
			String s = c.getLineStatus();
			return TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(s)
					|| TravelDeskTicketLine.STATUS_FULFILLED.equals(s)
					|| TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(s);
		});
		boolean anyRejectedByHod = ticket.getLines().stream()
				.anyMatch(c -> TravelDeskTicketLine.STATUS_LEVEL_REJECTED.equals(c.getLineStatus()));
		if (anyRejectedByHod && anyApprovedPastHod) {
			return "Partial";
		}
		if (anyRejectedByHod && TravelDeskTicket.STAGE_REJECTED.equals(stage)) {
			return "Rejected";
		}
		if (anyApprovedPastHod || TravelDeskTicket.STAGE_COMPLETED.equals(stage)
				|| TravelDeskTicket.STAGE_PENDING_ADMIN.equals(stage)) {
			return "Approved";
		}
		return "—";
	}

	private String normEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private Map<String, Object> approvalLevelRow(int order, String levelLabel, String approverName, String approverStatus,
			boolean adminStep) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("order", order);
		row.put("levelLabel", levelLabel);
		row.put("approverName", StringUtils.hasText(approverName) ? approverName : "—");
		row.put("approverStatus", StringUtils.hasText(approverStatus) ? approverStatus : "—");
		row.put("adminStep", adminStep);
		row.put("financeStep", adminStep);
		return row;
	}
}
