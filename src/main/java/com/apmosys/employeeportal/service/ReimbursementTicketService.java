package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.ClientIdAndName;
import com.apmosys.employeeportal.dto.ReimbursementClaimDecisionDTO;
import com.apmosys.employeeportal.dto.ReimbursementDashboardFilterDTO;
import com.apmosys.employeeportal.dto.ReimbursementFinanceTicketActionDTO;
import com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketActorDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketClaimInputDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketStageActionDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO.ReimbursementApprovalMatrixLevelDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketSubmitRequestDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ExpenditureType;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ReimbursementTicket;
import com.apmosys.employeeportal.model.ReimbursementTicketAuditLog;
import com.apmosys.employeeportal.model.ReimbursementTicketClaim;
import com.apmosys.employeeportal.model.ReimbursementTicketDaySeq;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ExpenditureTypeRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ReimbursementTicketAuditLogRepository;
import com.apmosys.employeeportal.repository.ReimbursementTicketDaySeqRepository;
import com.apmosys.employeeportal.repository.ReimbursementTicketRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementTicketService {

	@Autowired
	private ReimbursementTicketRepository ticketRepository;

	@Autowired
	private ReimbursementTicketDaySeqRepository reimbursementTicketDaySeqRepository;

	@Autowired
	private ReimbursementTicketAuditLogRepository auditLogRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;

	@Autowired
	private ClientsRepository clientsRepository;

	@Autowired
	private ExpenditureTypeRepository expenditureTypeRepository;

	@Autowired
	private ReimbursementApprovalMatrixService reimbursementApprovalMatrixService;

	@Autowired
	private ReimbursementTicketMatrixWorkflowService matrixWorkflowService;

	@Autowired
	private ReimbursementSubmissionSettingsService reimbursementSubmissionSettingsService;

	@Value("${reimbursement.workflow.hr.mail:hrm2@apmosys.com}")
	private String workflowHrMail;

	@Value("${reimbursement.workflow.finance.mail:finance2@apmosys.com}")
	private String workflowFinanceMail;

	@Value("${reimbursement.ticket-id.zone:Asia/Kolkata}")
	private String reimbursementTicketIdZone;

	/** Sentinel project id for Business Development manual "Others" line (not a real projects.project_id). */
	public static final long REIMBURSEMENT_PROJECT_OTHERS_ID = -1L;

	private static String normEmail(String e) {
		return e == null ? "" : e.trim().toLowerCase(Locale.ROOT);
	}

	/** Human-facing ticket reference (public number); falls back to numeric PK when not yet migrated. */
	private String ticketDisplayRef(ReimbursementTicket t) {
		if (t == null) {
			return "";
		}
		if (StringUtils.hasText(t.getTicketNo())) {
			return t.getTicketNo();
		}
		return t.getTicketId() != null ? String.valueOf(t.getTicketId()) : "";
	}

	/**
	 * Allocates the next APM-RMB-YYYYMMDD-#### for today in {@link #reimbursementTicketIdZone}, using a locked
	 * per-day counter row so concurrent submits get unique numbers.
	 */
	private String allocateNextPublicTicketNo() {
		String zoneId = StringUtils.hasText(reimbursementTicketIdZone) ? reimbursementTicketIdZone.trim()
				: "Asia/Kolkata";
		ZoneId zone = ZoneId.of(zoneId);
		String dayKey = LocalDate.now(zone).format(DateTimeFormatter.BASIC_ISO_DATE);
		reimbursementTicketDaySeqRepository.ensureDayRow(dayKey);
		ReimbursementTicketDaySeq row = reimbursementTicketDaySeqRepository.findByDayKeyForUpdate(dayKey)
				.orElseThrow(() -> new IllegalStateException("Missing ticket day sequence for " + dayKey));
		int prev = row.getLastSeq() == null ? 0 : row.getLastSeq();
		int next = prev + 1;
		row.setLastSeq(next);
		reimbursementTicketDaySeqRepository.saveAndFlush(row);
		return "APM-RMB-" + dayKey + "-" + String.format("%04d", next);
	}

	private void audit(Long ticketId, Long claimId, BigInteger actorEmpId, String actorEmail, String action,
			String remarks) {
		ReimbursementTicketAuditLog log = new ReimbursementTicketAuditLog();
		log.setTicketId(ticketId);
		log.setClaimId(claimId);
		log.setActorEmpId(actorEmpId);
		log.setActorEmail(actorEmail);
		log.setAction(action);
		log.setRemarks(remarks);
		log.setCreatedOn(nowTs());
		auditLogRepository.save(log);
	}

	private Timestamp nowTs() {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		ts.setNanos(ts.getNanos() / 1000 * 1000);
		return ts;
	}

	private String displayTicketStatus(ReimbursementTicket t) {
		switch (t.getWorkflowStage()) {
		case ReimbursementTicket.STAGE_PENDING_HOD:
			return "Submitted";
		case ReimbursementTicket.STAGE_PENDING_LEVEL:
			return matrixWorkflowService.usesMatrixWorkflow(t) ? "Pending approval" : "Submitted";
		case ReimbursementTicket.STAGE_PENDING_HR:
			return "Approved by HOD";
		case ReimbursementTicket.STAGE_PENDING_FINANCE:
			return matrixWorkflowService.usesMatrixWorkflow(t) ? "Pending finance" : "Approved by HR";
		case ReimbursementTicket.STAGE_PAID:
			return "Paid";
		case ReimbursementTicket.STAGE_REJECTED:
			return "Rejected";
		default:
			return t.getWorkflowStage();
		}
	}

	/**
	 * Roll up per-claim statuses into one label per approval level for list/detail views.
	 */
	private String aggregateLevel1ApproverStatus(ReimbursementTicket t) {
		List<ReimbursementTicketClaim> claims = t.getClaims();
		if (claims == null || claims.isEmpty()) {
			return "—";
		}
		boolean anyPendingHod = claims.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_HOD.equals(c.getClaimStatus()));
		if (anyPendingHod) {
			return "Pending";
		}
		boolean anyHodRejected = claims.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(c.getClaimStatus()));
		boolean anyPastHod = claims.stream().anyMatch(c -> {
			String s = c.getClaimStatus();
			return !ReimbursementTicketClaim.STATUS_PENDING_HOD.equals(s)
					&& !ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(s);
		});
		if (anyHodRejected && anyPastHod) {
			return "Partial";
		}
		if (anyHodRejected) {
			return "Rejected";
		}
		return "Approved";
	}

	private String aggregateLevel2ApproverStatus(ReimbursementTicket t) {
		List<ReimbursementTicketClaim> claims = t.getClaims();
		if (claims == null || claims.isEmpty()) {
			return "—";
		}
		if (claims.stream().anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_HOD.equals(c.getClaimStatus()))) {
			return "—";
		}
		List<ReimbursementTicketClaim> hrScope = claims.stream()
				.filter(c -> !ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(c.getClaimStatus()))
				.collect(Collectors.toList());
		if (hrScope.isEmpty()) {
			return "—";
		}
		if (hrScope.stream().anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_HR.equals(c.getClaimStatus()))) {
			return "Pending";
		}
		boolean anyHrRejected = hrScope.stream()
				.anyMatch(c -> ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(c.getClaimStatus()));
		boolean anyPastHr = hrScope.stream().anyMatch(c -> {
			String s = c.getClaimStatus();
			return ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(s)
					|| ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(s)
					|| ReimbursementTicketClaim.STATUS_PAID.equals(s);
		});
		if (anyHrRejected && anyPastHr) {
			return "Partial";
		}
		if (anyHrRejected) {
			return "Rejected";
		}
		if (anyPastHr) {
			return "Approved";
		}
		return "—";
	}

	private String aggregateLevel3ApproverStatus(ReimbursementTicket t) {
		List<ReimbursementTicketClaim> claims = t.getClaims();
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

	private String claimSummaryHtml(ReimbursementTicket ticket) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1' cellpadding='4' cellspacing='0'><tr><th>Claim</th><th>Type</th><th>Project</th><th>Client</th><th>Amount</th><th>Status</th><th>Purpose</th></tr>");
		for (ReimbursementTicketClaim c : ticket.getClaims().stream().sorted(Comparator.comparing(ReimbursementTicketClaim::getLineNo))
				.collect(Collectors.toList())) {
			String proj = StringUtils.hasText(c.getProjectName()) ? c.getProjectName()
					: (c.getProjectId() != null ? ("#" + c.getProjectId()) : "—");
			String client = StringUtils.hasText(c.getClientName()) ? c.getClientName() : "—";
			sb.append("<tr><td>").append(c.getLineNo()).append("</td><td>").append(esc(c.getExpenditureType()))
					.append("</td><td>").append(esc(shorten(proj, 60))).append("</td><td>").append(esc(shorten(client, 40)))
					.append("</td><td>").append(c.getAmount())
					.append("</td><td>").append(esc(c.getClaimStatus()))
					.append("</td><td>").append(esc(shorten(c.getPurpose(), 80))).append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String shorten(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() <= max ? s : s.substring(0, max) + "…";
	}

	private void sendMailSafe(String to, String cc, String subject, String html) {
		try {
			if (!StringUtils.hasText(to)) {
				return;
			}
			String ccUse = StringUtils.hasText(cc) ? cc : workflowHrMail;
			mailService.sendMailWithCC(to, ccUse, subject, html);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Project from team / timesheet mapping (employee_team_mapping → teams → projects). */
	private String resolveProjectNameFromTeamMapping(Long empId, Long projectId) {
		if (empId == null || projectId == null) {
			return null;
		}
		List<Object[]> rows = employeeTeamMapRepository.findProjectsByTeamId(empId);
		for (Object[] o : rows) {
			if (o == null || o.length < 6 || o[4] == null) {
				continue;
			}
			long pid = Long.parseLong(o[4].toString());
			if (projectId.longValue() == pid) {
				return o[5] != null ? o[5].toString() : "";
			}
		}
		return null;
	}

	/**
	 * Departments where this employee is HOD, plus (for VP/HOD job role) the job_role department.
	 * Used to scope department-linked projects for reimbursement.
	 */
	private List<Long> reimbursementDepartmentScope(Long empId) {
		if (empId == null) {
			return new ArrayList<>();
		}
		Set<Long> deptIds = new LinkedHashSet<>();
		if (Boolean.TRUE.equals(departmentRepository.existsByHodId(empId))) {
			List<GetDeptIdByRoleDTO> hodDepts = departmentRepository.findDeptIdsByHodId2(empId);
			if (hodDepts != null) {
				for (GetDeptIdByRoleDTO d : hodDepts) {
					if (d != null && d.getDeptId() != null) {
						deptIds.add(d.getDeptId());
					}
				}
			}
		}
		List<Integer> vpRow = employeeRepository.findOneIfVpHodEmployee(empId);
		List<Integer> hodJobRow = employeeRepository.findOneIfJobRoleHodEmployee(empId);
		if ((vpRow != null && !vpRow.isEmpty()) || (hodJobRow != null && !hodJobRow.isEmpty())) {
			Long jrDept = employeeRepository.findJobRoleDeptIdByEmpId(empId);
			if (jrDept != null) {
				deptIds.add(jrDept);
			}
		}
		return new ArrayList<>(deptIds);
	}

	private String resolveProjectNameFromDepartmentProjects(Long empId, Long projectId) {
		if (empId == null || projectId == null) {
			return null;
		}
		List<Long> deptIds = reimbursementDepartmentScope(empId);
		if (deptIds.isEmpty()) {
			return null;
		}
		List<Object[]> rows = projectRepository.findActiveProjectsForReimbursementByDeptIds(deptIds);
		for (Object[] o : rows) {
			if (o == null || o.length < 2 || o[0] == null) {
				continue;
			}
			long pid = Long.parseLong(o[0].toString());
			if (projectId.longValue() != pid) {
				continue;
			}
			String pname = o[1] != null ? o[1].toString() : "";
			if (o.length > 2 && o[2] != null && StringUtils.hasText(o[2].toString())) {
				return pname + " (" + o[2].toString() + ")";
			}
			return pname;
		}
		return null;
	}

	private String resolveProjectNameForClaim(Long empId, Long projectId) {
		if (projectId != null && projectId.longValue() == REIMBURSEMENT_PROJECT_OTHERS_ID) {
			return null;
		}
		String team = resolveProjectNameFromTeamMapping(empId, projectId);
		if (team != null) {
			return team;
		}
		String dept = resolveProjectNameFromDepartmentProjects(empId, projectId);
		if (dept != null) {
			return dept;
		}
		return resolveProjectNameFromPrimaryMapping(empId, projectId);
	}

	/** When team mapping yields no row (inactive team, missing client_locations join, etc.), allow mapped primary project. */
	private String resolveProjectNameFromPrimaryMapping(Long empId, Long projectId) {
		if (empId == null || projectId == null) {
			return null;
		}
		List<EmpPrimaryProjectMapping> list = empPrimaryProjectMappingRepository
				.findAllByEmpIdAndIsMappedOrderByMappingIdDesc(empId, "Y");
		if (list == null) {
			return null;
		}
		for (EmpPrimaryProjectMapping pm : list) {
			if (pm == null || pm.getPrimaryProjectId() == null) {
				continue;
			}
			if (pm.getPrimaryProjectId().longValue() != projectId.longValue()) {
				continue;
			}
			int pid = pm.getPrimaryProjectId().intValue();
			Project p = projectRepository.findByProjectId(pid);
			if (p != null && "true".equalsIgnoreCase(p.getActive())) {
				return p.getProjectName() != null ? p.getProjectName() : "";
			}
			return null;
		}
		return null;
	}

	/**
	 * Adds the employee's active primary project to the picker when it is missing from team/department lists
	 * (e.g. RMG set primary but no active employee_team_mapping row passes the reimbursement team query).
	 */
	private void mergePrimaryMappedProjectIfAbsent(Long empId, List<Map<String, Object>> projects) {
		if (empId == null || projects == null) {
			return;
		}
		Set<Long> seen = new LinkedHashSet<>();
		for (Map<String, Object> m : projects) {
			Object pid = m.get("projectId");
			if (pid instanceof Number) {
				seen.add(((Number) pid).longValue());
			}
		}
		List<EmpPrimaryProjectMapping> primaryMappings = empPrimaryProjectMappingRepository
				.findAllByEmpIdAndIsMappedOrderByMappingIdDesc(empId, "Y");
		if (primaryMappings == null) {
			return;
		}
		for (EmpPrimaryProjectMapping pm : primaryMappings) {
			if (pm == null || pm.getPrimaryProjectId() == null) {
				continue;
			}
			long pid = pm.getPrimaryProjectId().longValue();
			if (seen.contains(pid)) {
				return;
			}
			Project p = projectRepository.findByProjectId((int) pid);
			if (p == null || !"true".equalsIgnoreCase(p.getActive())) {
				continue;
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("projectId", pid);
			row.put("projectName", p.getProjectName() != null ? p.getProjectName() : "");
			row.put("clientName", p.getClientName() != null ? p.getClientName() : "");
			if (p.getClientId() != null) {
				row.put("clientId", p.getClientId());
			} else {
				row.put("clientId", null);
			}
			projects.add(0, row);
			return;
		}
	}

	private boolean isBusinessDevelopmentDepartment(String deptName) {
		if (!StringUtils.hasText(deptName)) {
			return false;
		}
		String n = deptName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
		return n.contains("business development");
	}

	private boolean showOthersProjectOption(Long empId) {
		if (empId == null) {
			return false;
		}
		String dept = employeeRepository.getDepartment(empId);
		return isBusinessDevelopmentDepartment(dept);
	}

	private void addOthersRowIfApplicable(List<Map<String, Object>> projects, boolean showOthers) {
		if (!showOthers) {
			return;
		}
		boolean already = projects.stream()
				.anyMatch(m -> m.get("projectId") instanceof Number
						&& ((Number) m.get("projectId")).longValue() == REIMBURSEMENT_PROJECT_OTHERS_ID);
		if (already) {
			return;
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("projectId", REIMBURSEMENT_PROJECT_OTHERS_ID);
		row.put("projectName", "Others");
		row.put("clientName", "");
		row.put("clientId", null);
		projects.add(0, row);
	}

	private boolean isProjectAllowedForReimbursement(Long empId, Long projectId) {
		if (projectId != null && projectId.longValue() == REIMBURSEMENT_PROJECT_OTHERS_ID) {
			return showOthersProjectOption(empId);
		}
		return resolveProjectNameForClaim(empId, projectId) != null;
	}

	public ServiceResponse fetchClaimProjectOptions(ReimbursementTicketActorDTO body) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (body == null || body.getEmpId() == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("empId is required.");
				return resp;
			}
			long empId = body.getEmpId().longValue();
			boolean showOthers = showOthersProjectOption(empId);
			List<Long> deptScope = reimbursementDepartmentScope(empId);
			List<Map<String, Object>> projects = new ArrayList<>();
			String pickerSource = "TEAM";
			if (!deptScope.isEmpty()) {
				pickerSource = "DEPARTMENT";
				List<Object[]> rows = projectRepository.findActiveProjectsForReimbursementByDeptIds(deptScope);
				for (Object[] o : rows) {
					if (o == null || o[0] == null) {
						continue;
					}
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("projectId", Long.parseLong(o[0].toString()));
					m.put("projectName", o[1] != null ? o[1].toString() : "");
					m.put("clientName", o.length > 2 && o[2] != null ? o[2].toString() : "");
					if (o.length > 3 && o[3] != null) {
						m.put("clientId", Integer.parseInt(o[3].toString()));
					} else {
						m.put("clientId", null);
					}
					projects.add(m);
				}
			} else {
				List<Object[]> rows = employeeTeamMapRepository.findProjectsByTeamId(empId);
				Set<Long> seen = new LinkedHashSet<>();
				for (Object[] o : rows) {
					if (o == null || o.length < 6 || o[4] == null) {
						continue;
					}
					long pid = Long.parseLong(o[4].toString());
					if (!seen.add(pid)) {
						continue;
					}
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("projectId", pid);
					m.put("projectName", o[5] != null ? o[5].toString() : "");
					m.put("clientName", o[1] != null ? o[1].toString() : "");
					if (o[0] != null) {
						m.put("clientId", Integer.parseInt(o[0].toString()));
					} else {
						m.put("clientId", null);
					}
					projects.add(m);
				}
			}
			mergePrimaryMappedProjectIfAbsent(empId, projects);
			addOthersRowIfApplicable(projects, showOthers);
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("pickerSource", pickerSource);
			out.put("projects", projects);
			out.put("showOthersOption", showOthers);
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(out);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	/**
	 * All rows from {@code clients} (via JPA) for BD "Others" claim line — not client_locations join.
	 */
	@Transactional(readOnly = true)
	public ServiceResponse fetchReimbursementClientsFromMaster() {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ClientIdAndName> rows = clientsRepository.findAllClientIdAndName();
			List<Map<String, Object>> list = new ArrayList<>();
			for (ClientIdAndName row : rows) {
				if (row == null || row.getClientId() == null) {
					continue;
				}
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("clientId", row.getClientId());
				m.put("clientName", row.getClientName() != null ? row.getClientName() : "");
				list.add(m);
			}
			list.sort(Comparator.comparing(o -> String.valueOf(o.get("clientName")), String.CASE_INSENSITIVE_ORDER));
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(list);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	private void validateClaimInput(ReimbursementTicketClaimInputDTO c, int index, BigInteger empId) {
		if (c == null || !StringUtils.hasText(c.getExpenditureType())) {
			throw new IllegalArgumentException("Claim " + index + ": expenditure type is required.");
		}
		ExpenditureType et = expenditureTypeRepository.findByExpenditureTypeName(c.getExpenditureType()).orElse(null);
		if (et == null) {
			throw new IllegalArgumentException("Claim " + index + ": invalid expenditure type.");
		}
		if (c.getProjectId() == null) {
			throw new IllegalArgumentException("Claim " + index + ": project is required.");
		}
		if (empId == null) {
			throw new IllegalArgumentException("Claim " + index + ": employee context is required.");
		}
		long eid = empId.longValue();
		if (c.getProjectId().longValue() == REIMBURSEMENT_PROJECT_OTHERS_ID) {
			String dept = employeeRepository.getDepartment(eid);
			if (!isBusinessDevelopmentDepartment(dept)) {
				throw new IllegalArgumentException("Claim " + index + ": \"Others\" is only available for Business Development.");
			}
			if (!StringUtils.hasText(c.getOthersProjectName()) || c.getOthersProjectName().trim().isEmpty()) {
				throw new IllegalArgumentException("Claim " + index + ": enter the project name for Others.");
			}
			if (c.getOthersProjectName().trim().length() > 500) {
				throw new IllegalArgumentException("Claim " + index + ": project name must be at most 500 characters.");
			}
			if (c.getClientId() == null) {
				throw new IllegalArgumentException("Claim " + index + ": choose a client when using Others.");
			}
			Client client = clientsRepository.findByClientId(c.getClientId());
			if (client == null) {
				throw new IllegalArgumentException("Claim " + index + ": invalid client selection.");
			}
		} else if (!isProjectAllowedForReimbursement(eid, c.getProjectId())) {
			throw new IllegalArgumentException("Claim " + index
					+ ": selected project is not allowed for your profile. Pick a project from the list (contact RMG if the list is empty).");
		}
		if (c.getAmount() == null || c.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Claim " + index + ": amount must be greater than zero.");
		}
		if (c.getDocIds() == null || c.getDocIds().isEmpty()) {
			throw new IllegalArgumentException("Claim " + index + ": at least one supporting document is required.");
		}
		if (!StringUtils.hasText(c.getPurpose()) || c.getPurpose().trim().isEmpty()) {
			throw new IllegalArgumentException("Claim " + index + ": purpose is required.");
		}
		String exp = c.getExpenditureType();
		if ("Food".equalsIgnoreCase(exp)) {
			if (c.getDateOfFood() == null) {
				throw new IllegalArgumentException("Claim " + index + ": food date is required for Food.");
			}
			if (!StringUtils.hasText(c.getFoodAllowanceType())) {
				throw new IllegalArgumentException("Claim " + index + ": food allowance type is required.");
			}
		} else {
			if (c.getFromDate() == null || c.getToDate() == null) {
				throw new IllegalArgumentException("Claim " + index + ": from and to dates are required.");
			}
			if (c.getToDate().before(c.getFromDate())) {
				throw new IllegalArgumentException("Claim " + index + ": to date must be on or after from date.");
			}
		}
		if ("Travel".equalsIgnoreCase(exp)) {
			if (!StringUtils.hasText(c.getTravelMode())) {
				throw new IllegalArgumentException("Claim " + index + ": travel mode is required.");
			}
			if ("Personal Vehicle".equalsIgnoreCase(c.getTravelMode())) {
				if (!StringUtils.hasText(c.getVehicleType())) {
					throw new IllegalArgumentException("Claim " + index + ": vehicle type is required.");
				}
				if (c.getDistance() == null || c.getDistance().compareTo(BigInteger.ZERO) <= 0) {
					throw new IllegalArgumentException("Claim " + index + ": distance is required for personal vehicle.");
				}
			}
		}
	}

	@Transactional
	public ServiceResponse submitTicket(ReimbursementTicketSubmitRequestDTO req) {
		ServiceResponse resp = new ServiceResponse();
		try {
			reimbursementSubmissionSettingsService.assertSubmissionAllowed();
			if (req.getClaims() == null || req.getClaims().isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("At least one claim is required.");
				return resp;
			}
			if (req.getHodEmpId() == null || !StringUtils.hasText(req.getHodEmail())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("HOD details are required.");
				return resp;
			}
			int idx = 1;
			for (ReimbursementTicketClaimInputDTO c : req.getClaims()) {
				validateClaimInput(c, idx++, req.getEmpId());
			}

			ReimbursementTicket ticket = new ReimbursementTicket();
			ticket.setEmpId(req.getEmpId());
			ticket.setFullName(req.getFullName());
			ticket.setEmail(req.getEmail());
			ticket.setDepartment(req.getDepartmentName());
			ticket.setDesignation(req.getDesignationName());
			ticket.setMobileNo(req.getMobileNo() != null ? req.getMobileNo().toString() : null);
			ticket.setHodEmpId(req.getHodEmpId());
			ticket.setHodName(req.getHodName());
			ticket.setHodEmail(req.getHodEmail().trim());
			ticket.setSubmittedOn(nowTs());
			ticket.setIsActive(1);
			ticket.setTicketNo(allocateNextPublicTicketNo());

			int line = 1;
			for (ReimbursementTicketClaimInputDTO in : req.getClaims()) {
				ReimbursementTicketClaim cl = new ReimbursementTicketClaim();
				cl.setTicket(ticket);
				cl.setLineNo(line++);
				cl.setExpenditureType(in.getExpenditureType());
				ExpenditureType et = expenditureTypeRepository.findByExpenditureTypeName(in.getExpenditureType()).orElse(null);
				cl.setExpenditureTypeDescription(et != null ? et.getDescription() : null);
				cl.setAmount(in.getAmount());
				cl.setTravelMode(in.getTravelMode());
				cl.setDistance(in.getDistance());
				cl.setVehicleType(in.getVehicleType());
				cl.setFoodAllowanceType(in.getFoodAllowanceType());
				cl.setDateOfFood(in.getDateOfFood() != null ? new Timestamp(in.getDateOfFood().getTime()) : null);
				cl.setFromDate(in.getFromDate() != null ? new Timestamp(in.getFromDate().getTime()) : null);
				cl.setToDate(in.getToDate() != null ? new Timestamp(in.getToDate().getTime()) : null);
				cl.setPurpose(in.getPurpose().trim());
				if (in.getProjectId() != null && in.getProjectId().longValue() == REIMBURSEMENT_PROJECT_OTHERS_ID) {
					cl.setProjectId(null);
					cl.setProjectName(in.getOthersProjectName().trim());
					cl.setClientId(in.getClientId());
					Client client = clientsRepository.findByClientId(in.getClientId());
					cl.setClientName(client != null ? client.getClientName() : null);
				} else {
					cl.setProjectId(in.getProjectId());
					cl.setProjectName(resolveProjectNameForClaim(req.getEmpId().longValue(), in.getProjectId()));
					Project p = projectRepository.findByProjectId(in.getProjectId().intValue());
					if (p != null) {
						cl.setClientId(p.getClientId());
						cl.setClientName(p.getClientName());
					}
				}
				cl.setDocIds(in.getDocIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
				ticket.getClaims().add(cl);
			}

			matrixWorkflowService.initializeOnSubmit(ticket);

			ReimbursementTicket saved = ticketRepository.save(ticket);
			audit(saved.getTicketId(), null, req.getEmpId(), req.getEmail(), "TICKET_SUBMITTED", null);

			String ccHrFinance = workflowHrMail + "," + workflowFinanceMail;
			String notifyTo = matrixWorkflowService.usesMatrixWorkflow(saved)
					? matrixWorkflowService.notifyEmailForCurrentLevel(saved)
					: saved.getHodEmail();
			String notifyName = matrixWorkflowService.usesMatrixWorkflow(saved) ? "Approver" : saved.getHodName();
			String subj = "Reimbursement ticket " + ticketDisplayRef(saved) + " – action required";
			String body = "<p>Dear " + esc(notifyName) + ",</p>"
					+ "<p><strong>Ticket ID:</strong> " + esc(ticketDisplayRef(saved)) + "<br>"
					+ "<strong>Status:</strong> " + esc(displayTicketStatus(saved)) + "</p>"
					+ "<p>An employee has submitted a reimbursement ticket for your review.</p>"
					+ claimSummaryHtml(saved)
					+ "<p>Regards,<br/>IShine</p>";
			if (StringUtils.hasText(notifyTo)) {
				sendMailSafe(notifyTo, ccHrFinance, subj, body);
			}

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toViewMap(saved));
			resp.setServiceMessage("Ticket submitted successfully.");
		} catch (IllegalArgumentException ex) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchMyTickets(BigInteger empId) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ReimbursementTicket> list = ticketRepository.findByEmpIdWithClaims(empId);
			list.sort(Comparator.comparing(ReimbursementTicket::getSubmittedOn,
					Comparator.nullsLast(Comparator.naturalOrder())).reversed());
			List<Map<String, Object>> out = list.stream().map(this::toViewMap).collect(Collectors.toList());
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(out);
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchTicketsForApproval(ReimbursementTicketActorDTO actor) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ReimbursementTicket> out = new ArrayList<>();
			String email = normEmail(actor.getEmail());
			BigInteger empId = actor.getEmpId();

			if (empId != null) {
				out.addAll(ticketRepository.findTicketsWithClaimsByStageAndHod(ReimbursementTicket.STAGE_PENDING_HOD,
						empId));
			}
			if (email.equals(normEmail(workflowHrMail))) {
				out.addAll(ticketRepository.findTicketsWithClaimsByStage(ReimbursementTicket.STAGE_PENDING_HR));
			}
			if (email.equals(normEmail(workflowFinanceMail))) {
				out.addAll(ticketRepository.findTicketsWithClaimsByStage(ReimbursementTicket.STAGE_PENDING_FINANCE));
			}
			for (ReimbursementTicket t : ticketRepository
					.findTicketsWithClaimsByStage(ReimbursementTicket.STAGE_PENDING_LEVEL)) {
				if (matrixWorkflowService.canActorApprove(t, empId, actor.getEmail())) {
					out.add(t);
				}
			}
			// de-duplicate
			Map<Long, ReimbursementTicket> map = new LinkedHashMap<>();
			for (ReimbursementTicket t : out) {
				map.putIfAbsent(t.getTicketId(), t);
			}
			List<Map<String, Object>> views = map.values().stream().map(this::toViewMap).collect(Collectors.toList());
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(views);
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	/**
	 * All reimbursement tickets this actor is in the approval chain for (any workflow stage),
	 * including approved, rejected, and paid. HODs see every ticket where they are the assigned HOD;
	 * configured HR / finance mailboxes see all active tickets (same responsibility scope as dashboard).
	 */
	@Transactional(readOnly = true)
	public ServiceResponse fetchAllTicketsAssignedToActor(ReimbursementTicketActorDTO actor) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ReimbursementTicket> out = new ArrayList<>();
			String email = normEmail(actor.getEmail());
			BigInteger empId = actor.getEmpId();

			if (empId != null) {
				out.addAll(ticketRepository.findAllTicketsWithClaimsByHodEmpId(empId));
			}
			if (StringUtils.hasText(actor.getEmail())) {
				String hodEmailParam = actor.getEmail().trim();
				if (StringUtils.hasText(hodEmailParam)) {
					out.addAll(ticketRepository.findAllTicketsWithClaimsByHodEmail(hodEmailParam));
				}
			}
			if (email.equals(normEmail(workflowHrMail)) || email.equals(normEmail(workflowFinanceMail))) {
				out.addAll(ticketRepository.findAllActiveWithClaims());
			}
			for (ReimbursementTicket t : ticketRepository.findAllActiveWithClaims()) {
				if (matrixWorkflowService.isActorInApprovalChain(t, empId, actor.getEmail())) {
					out.add(t);
				}
			}
			Map<Long, ReimbursementTicket> map = new LinkedHashMap<>();
			for (ReimbursementTicket t : out) {
				map.putIfAbsent(t.getTicketId(), t);
			}
			List<ReimbursementTicket> merged = new ArrayList<>(map.values());
			merged.sort(Comparator.comparing(ReimbursementTicket::getSubmittedOn,
					Comparator.nullsLast(Comparator.naturalOrder())).reversed());
			List<Map<String, Object>> views = merged.stream().map(this::toViewMap).collect(Collectors.toList());
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(views);
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse processHodAction(ReimbursementTicketStageActionDTO action) {
		return processStageDecisions(action, true);
	}

	@Transactional
	public ServiceResponse processHrAction(ReimbursementTicketStageActionDTO action) {
		return processStageDecisions(action, false);
	}

	private ServiceResponse processMatrixLevelDecisions(ReimbursementTicketStageActionDTO action,
			ReimbursementTicket ticket, ServiceResponse resp) {
		try {
			if (!matrixWorkflowService.canActorApprove(ticket, action.getActorEmpId(), action.getActorEmail())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("You are not authorized to act on this approval level.");
				return resp;
			}
			String pendingStatus = ReimbursementTicketClaim.STATUS_PENDING_APPROVAL;
			List<ReimbursementTicketClaim> pending = ticket.getClaims().stream()
					.filter(c -> pendingStatus.equals(c.getClaimStatus())).collect(Collectors.toList());
			if (pending.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("No pending claims to process.");
				return resp;
			}
			if (action.getDecisions() == null || action.getDecisions().size() != pending.size()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Provide exactly one decision per pending claim (" + pending.size() + ").");
				return resp;
			}
			Map<Long, ReimbursementTicketClaim> byId = pending.stream()
					.collect(Collectors.toMap(ReimbursementTicketClaim::getClaimId, x -> x));
			for (ReimbursementClaimDecisionDTO d : action.getDecisions()) {
				if (d.getClaimId() == null || !byId.containsKey(d.getClaimId())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Invalid claim id in decisions.");
					return resp;
				}
				if (d.getApproved() == null) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Each decision must set approved true/false.");
					return resp;
				}
				if (Boolean.FALSE.equals(d.getApproved())
						&& (!StringUtils.hasText(d.getRemarks()) || d.getRemarks().trim().isEmpty())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Remarks are mandatory when rejecting a claim.");
					return resp;
				}
				if (Boolean.TRUE.equals(d.getApproved())
						&& (!StringUtils.hasText(d.getRemarks()) || d.getRemarks().trim().isEmpty())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Remarks are mandatory when approving a claim.");
					return resp;
				}
			}
			for (ReimbursementClaimDecisionDTO d : action.getDecisions()) {
				ReimbursementTicketClaim c = byId.get(d.getClaimId());
				if (Boolean.TRUE.equals(d.getApproved())) {
					c.setClaimStatus(ReimbursementTicketClaim.STATUS_PENDING_APPROVAL);
					c.setHodRemarks(d.getRemarks());
				} else {
					c.setClaimStatus(ReimbursementTicketClaim.STATUS_LEVEL_REJECTED);
					c.setHodRemarks(d.getRemarks());
				}
				audit(ticket.getTicketId(), c.getClaimId(), action.getActorEmpId(), action.getActorEmail(),
						"MATRIX_LEVEL_DECISION", d.getRemarks());
			}
			boolean anyStillPending = ticket.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(c.getClaimStatus()));
			if (!anyStillPending) {
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
				ticket.setCurrentAssigneeEmpId(null);
			} else {
				matrixWorkflowService.advanceAfterLevelDecisions(ticket);
			}
			if (allTicketClaimsRejected(ticket)) {
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
				ticket.setCurrentAssigneeEmpId(null);
			}
			ticketRepository.save(ticket);

			String notifyTo = matrixWorkflowService.notifyEmailForCurrentLevel(ticket);
			String ccLine = workflowHrMail + "," + workflowFinanceMail;
			if (ReimbursementTicket.STAGE_PENDING_FINANCE.equals(ticket.getWorkflowStage())) {
				notifyTo = workflowFinanceMail;
			}
			String subj = "Reimbursement ticket " + ticketDisplayRef(ticket) + " – " + displayTicketStatus(ticket);
			String body = "<p>Dear " + esc(ticket.getFullName()) + ",</p>"
					+ "<p><strong>Ticket ID:</strong> " + esc(ticketDisplayRef(ticket)) + "<br>"
					+ "<strong>Status:</strong> " + esc(displayTicketStatus(ticket)) + "</p>"
					+ claimSummaryHtml(ticket)
					+ "<p>Regards,<br/>IShine</p>";
			sendMailSafe(ticket.getEmail(), ccLine, subj, body);
			if (StringUtils.hasText(notifyTo)
					&& ReimbursementTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
				String approverSubj = "Reimbursement ticket " + ticketDisplayRef(ticket) + " – action required";
				String approverBody = "<p>A reimbursement ticket requires your approval.</p>"
						+ "<p><strong>Ticket ID:</strong> " + esc(ticketDisplayRef(ticket)) + "</p>"
						+ claimSummaryHtml(ticket)
						+ "<p>Regards,<br/>IShine</p>";
				sendMailSafe(notifyTo, ccLine, approverSubj, approverBody);
			}

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toViewMap(ticket));
			resp.setServiceMessage("Processed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	private ServiceResponse processStageDecisions(ReimbursementTicketStageActionDTO action, boolean hodStage) {
		ServiceResponse resp = new ServiceResponse();
		try {
			Optional<ReimbursementTicket> opt = ticketRepository.findByIdWithClaims(action.getTicketId());
			if (opt.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Ticket not found.");
				return resp;
			}
			ReimbursementTicket ticket = opt.get();
			if (matrixWorkflowService.usesMatrixWorkflow(ticket)
					&& ReimbursementTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
				return processMatrixLevelDecisions(action, ticket, resp);
			}
			String expectedStage = hodStage ? ReimbursementTicket.STAGE_PENDING_HOD : ReimbursementTicket.STAGE_PENDING_HR;
			if (!expectedStage.equals(ticket.getWorkflowStage())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Ticket is not awaiting this approval stage.");
				return resp;
			}
			if (hodStage) {
				if (action.getActorEmpId() == null || !action.getActorEmpId().equals(ticket.getHodEmpId())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Only the assigned HOD can act on this ticket.");
					return resp;
				}
			} else {
				if (!normEmail(action.getActorEmail()).equals(normEmail(workflowHrMail))) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Only HR can act at this stage.");
					return resp;
				}
			}

			String pendingStatus = hodStage ? ReimbursementTicketClaim.STATUS_PENDING_HOD
					: ReimbursementTicketClaim.STATUS_PENDING_HR;
			List<ReimbursementTicketClaim> pending = ticket.getClaims().stream()
					.filter(c -> pendingStatus.equals(c.getClaimStatus())).collect(Collectors.toList());
			if (pending.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("No pending claims to process.");
				return resp;
			}
			if (action.getDecisions() == null || action.getDecisions().size() != pending.size()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Provide exactly one decision per pending claim (" + pending.size() + ").");
				return resp;
			}

			Map<Long, ReimbursementTicketClaim> byId = pending.stream()
					.collect(Collectors.toMap(ReimbursementTicketClaim::getClaimId, x -> x));
			for (ReimbursementClaimDecisionDTO d : action.getDecisions()) {
				if (d.getClaimId() == null || !byId.containsKey(d.getClaimId())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Invalid claim id in decisions.");
					return resp;
				}
				if (d.getApproved() == null) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Each decision must set approved true/false.");
					return resp;
				}
				if (Boolean.FALSE.equals(d.getApproved())
						&& (!StringUtils.hasText(d.getRemarks()) || d.getRemarks().trim().isEmpty())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Remarks are mandatory when rejecting a claim.");
					return resp;
				}
				if (Boolean.TRUE.equals(d.getApproved())
						&& (!StringUtils.hasText(d.getRemarks()) || d.getRemarks().trim().isEmpty())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Remarks are mandatory when approving a claim.");
					return resp;
				}
			}

			for (ReimbursementClaimDecisionDTO d : action.getDecisions()) {
				ReimbursementTicketClaim c = byId.get(d.getClaimId());
				if (Boolean.TRUE.equals(d.getApproved())) {
					c.setClaimStatus(hodStage ? ReimbursementTicketClaim.STATUS_PENDING_HR
							: ReimbursementTicketClaim.STATUS_PENDING_FINANCE);
					if (hodStage) {
						c.setHodRemarks(d.getRemarks());
					} else {
						c.setHrRemarks(d.getRemarks());
					}
				} else {
					c.setClaimStatus(hodStage ? ReimbursementTicketClaim.STATUS_HOD_REJECTED
							: ReimbursementTicketClaim.STATUS_HR_REJECTED);
					if (hodStage) {
						c.setHodRemarks(d.getRemarks());
					} else {
						c.setHrRemarks(d.getRemarks());
					}
				}
				audit(ticket.getTicketId(), c.getClaimId(), action.getActorEmpId(), action.getActorEmail(),
						hodStage ? "HOD_CLAIM_DECISION" : "HR_CLAIM_DECISION",
						d.getRemarks());
			}

			boolean anyPendingHod = ticket.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_HOD.equals(c.getClaimStatus()));
			boolean anyPendingHr = ticket.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_HR.equals(c.getClaimStatus()));
			boolean anyPendingFinance = ticket.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus()));

			if (hodStage) {
				if (anyPendingHod) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("All HOD-pending claims must be decided in one submission.");
					return resp;
				}
				if (anyPendingHr) {
					ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_HR);
				} else {
					ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
				}
			} else {
				if (anyPendingHr) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("All HR-pending claims must be decided in one submission.");
					return resp;
				}
				if (anyPendingFinance) {
					ticket.setWorkflowStage(ReimbursementTicket.STAGE_PENDING_FINANCE);
				} else {
					ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
				}
			}
			/* If every line is rejected at any level, force closed-rejected (employee submits a new ticket). */
			if (allTicketClaimsRejected(ticket)) {
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
			}

			ticketRepository.save(ticket);

			String ccLine = hodStage ? (workflowHrMail + "," + workflowFinanceMail) : workflowFinanceMail;
			String subj = "Reimbursement ticket " + ticketDisplayRef(ticket) + " – " + displayTicketStatus(ticket);
			String body = "<p>Dear " + esc(ticket.getFullName()) + ",</p>"
					+ "<p><strong>Ticket ID:</strong> " + esc(ticketDisplayRef(ticket)) + "<br>"
					+ "<strong>Status:</strong> " + esc(displayTicketStatus(ticket)) + "</p>"
					+ claimSummaryHtml(ticket)
					+ "<p>Regards,<br/>IShine</p>";
			sendMailSafe(ticket.getEmail(), ccLine, subj, body);

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toViewMap(ticket));
			resp.setServiceMessage("Processed successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse processFinanceAction(ReimbursementFinanceTicketActionDTO action) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (!normEmail(action.getActorEmail()).equals(normEmail(workflowFinanceMail))) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Only Finance can perform this action.");
				return resp;
			}
			Optional<ReimbursementTicket> opt = ticketRepository.findByIdWithClaims(action.getTicketId());
			if (opt.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Ticket not found.");
				return resp;
			}
			ReimbursementTicket ticket = opt.get();
			if (!ReimbursementTicket.STAGE_PENDING_FINANCE.equals(ticket.getWorkflowStage())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Ticket is not with Finance.");
				return resp;
			}
			String act = action.getAction() == null ? "" : action.getAction().trim().toUpperCase(Locale.ROOT);
			if (!"PAID".equals(act) && !"REJECTED".equals(act)) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Action must be PAID or REJECTED.");
				return resp;
			}
			if ("REJECTED".equals(act) && (!StringUtils.hasText(action.getRemarks())
					|| action.getRemarks().trim().isEmpty())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Remarks are mandatory when Finance rejects a ticket.");
				return resp;
			}
			if ("PAID".equals(act) && (!StringUtils.hasText(action.getRemarks())
					|| action.getRemarks().trim().isEmpty())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Remarks are mandatory when Finance marks a ticket as paid.");
				return resp;
			}

			List<ReimbursementTicketClaim> financePending = ticket.getClaims().stream()
					.filter(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus()))
					.collect(Collectors.toList());
			if (financePending.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("No claims pending finance.");
				return resp;
			}

			if ("PAID".equals(act)) {
				for (ReimbursementTicketClaim c : financePending) {
					c.setClaimStatus(ReimbursementTicketClaim.STATUS_PAID);
				}
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_PAID);
				ticket.setPaidOn(nowTs());
				ticket.setFinanceRejectReason(null);
			} else {
				for (ReimbursementTicketClaim c : financePending) {
					c.setClaimStatus(ReimbursementTicketClaim.STATUS_FINANCE_REJECTED);
				}
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
				ticket.setFinanceRejectReason(action.getRemarks().trim());
			}
			if (allTicketClaimsRejected(ticket)) {
				ticket.setWorkflowStage(ReimbursementTicket.STAGE_REJECTED);
			}
			ticketRepository.save(ticket);
			audit(ticket.getTicketId(), null, action.getActorEmpId(), action.getActorEmail(),
					"FINANCE_" + act, action.getRemarks());

			String ccHodHr = ticket.getHodEmail() + "," + workflowHrMail;
			String subj = "Reimbursement ticket " + ticketDisplayRef(ticket) + " – " + displayTicketStatus(ticket);
			String body = "<p>Dear " + esc(ticket.getFullName()) + ",</p>"
					+ "<p><strong>Ticket ID:</strong> " + esc(ticketDisplayRef(ticket)) + "<br>"
					+ "<strong>Status:</strong> " + esc(displayTicketStatus(ticket)) + "</p>"
					+ ("REJECTED".equals(act)
							? "<p><strong>Finance remarks:</strong> " + esc(action.getRemarks()) + "</p>"
							: ("PAID".equals(act)
									? "<p><strong>Finance / approval notes:</strong> " + esc(action.getRemarks()) + "</p>"
									: ""))
					+ claimSummaryHtml(ticket)
					+ "<p>Regards,<br/>IShine</p>";
			sendMailSafe(ticket.getEmail(), ccHodHr, subj, body);

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toViewMap(ticket));
			resp.setServiceMessage("Finance action recorded.");
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchAuditLog(Long ticketId) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ReimbursementTicketAuditLog> rows = auditLogRepository.findByTicketIdOrderByCreatedOnAsc(ticketId);
			List<Map<String, Object>> out = new ArrayList<>();
			for (ReimbursementTicketAuditLog row : rows) {
				if (row == null) {
					continue;
				}
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("auditId", row.getAuditId());
				m.put("ticketId", row.getTicketId());
				m.put("claimId", row.getClaimId());
				m.put("actorEmpId", row.getActorEmpId());
				m.put("actorEmail", row.getActorEmail());
				m.put("action", row.getAction());
				m.put("remarks", row.getRemarks());
				m.put("createdOn", row.getCreatedOn());
				m.put("actorDisplayName", resolveAuditActorDisplayName(row.getActorEmpId(), row.getActorEmail()));
				out.add(m);
			}
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(out);
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	/** Best-effort employee name for audit UI (falls back to email / id). */
	private String resolveAuditActorDisplayName(BigInteger actorEmpId, String actorEmail) {
		try {
			if (actorEmpId != null) {
				Employee e = employeeRepository.findByEmpId(actorEmpId.longValue());
				if (e != null && StringUtils.hasText(e.getName())) {
					return e.getName().trim();
				}
			}
			String mail = actorEmail != null ? actorEmail.trim() : "";
			if (StringUtils.hasText(mail)) {
				Employee e = employeeRepository.findByEmail(mail);
				if (e == null) {
					e = employeeRepository.findByEmail(normEmail(mail));
				}
				if (e != null && StringUtils.hasText(e.getName())) {
					return e.getName().trim();
				}
			}
		} catch (Exception ignore) {
			// best-effort only
		}
		if (StringUtils.hasText(actorEmail)) {
			return actorEmail.trim();
		}
		if (actorEmpId != null) {
			return "Employee ID " + actorEmpId;
		}
		return "";
	}

	private static long bigIntegerToLong(BigInteger b) {
		return b == null ? 0L : b.longValue();
	}

	private static BigDecimal claimAmountOrZero(ReimbursementTicketClaim c) {
		return c != null && c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO;
	}

	private static double bigDecimalToDouble(BigDecimal b) {
		return b == null ? 0.0 : b.doubleValue();
	}

	private boolean hasClaimLevelFilters(ReimbursementDashboardFilterDTO filter) {
		if (filter == null) {
			return false;
		}
		return filter.getProjectId() != null || filter.getClientId() != null
				|| StringUtils.hasText(filter.getExpenditureType());
	}

	private boolean claimMatchesClaimFilters(ReimbursementTicketClaim c, ReimbursementDashboardFilterDTO filter) {
		if (filter == null || !hasClaimLevelFilters(filter)) {
			return true;
		}
		if (filter.getProjectId() != null
				&& (c.getProjectId() == null || !filter.getProjectId().equals(c.getProjectId()))) {
			return false;
		}
		if (filter.getClientId() != null
				&& (c.getClientId() == null || !filter.getClientId().equals(c.getClientId()))) {
			return false;
		}
		if (StringUtils.hasText(filter.getExpenditureType())
				&& (c.getExpenditureType() == null
						|| !c.getExpenditureType().equalsIgnoreCase(filter.getExpenditureType().trim()))) {
			return false;
		}
		return true;
	}

	private java.util.stream.Stream<ReimbursementTicketClaim> claimsForMetrics(ReimbursementTicket t,
			ReimbursementDashboardFilterDTO filter) {
		if (t.getClaims() == null || t.getClaims().isEmpty()) {
			return java.util.stream.Stream.empty();
		}
		return t.getClaims().stream().filter(c -> claimMatchesClaimFilters(c, filter));
	}

	private boolean ticketMatchesDashboardFilters(ReimbursementTicket t, ReimbursementDashboardFilterDTO filter,
			Timestamp fromTs, Timestamp toTs) {
		if (fromTs != null && t.getSubmittedOn() != null && t.getSubmittedOn().before(fromTs)) {
			return false;
		}
		if (toTs != null && t.getSubmittedOn() != null && t.getSubmittedOn().after(toTs)) {
			return false;
		}
		if (filter != null) {
			if (StringUtils.hasText(filter.getDepartment())) {
				String td = t.getDepartment();
				if (td == null || !td.trim().equalsIgnoreCase(filter.getDepartment().trim())) {
					return false;
				}
			}
			if (StringUtils.hasText(filter.getTicketStatus())
					&& !displayTicketStatus(t).equalsIgnoreCase(filter.getTicketStatus().trim())) {
				return false;
			}
			if (StringUtils.hasText(filter.getWorkflowStage())
					&& (t.getWorkflowStage() == null
							|| !t.getWorkflowStage().equalsIgnoreCase(filter.getWorkflowStage().trim()))) {
				return false;
			}
			if (filter.getEmployeeEmpId() != null && t.getEmpId() != null
					&& t.getEmpId().longValue() != filter.getEmployeeEmpId().longValue()) {
				return false;
			}
			if (hasClaimLevelFilters(filter)) {
				boolean any = t.getClaims().stream().anyMatch(c -> claimMatchesClaimFilters(c, filter));
				if (!any) {
					return false;
				}
			}
		}
		return true;
	}

	private static String yearMonthKey(Timestamp ts) {
		if (ts == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return String.format(Locale.ROOT, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
	}

	private static boolean isPipelinePendingClaimStatus(String st) {
		return ReimbursementTicketClaim.STATUS_PENDING_HOD.equals(st)
				|| ReimbursementTicketClaim.STATUS_PENDING_HR.equals(st)
				|| ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(st)
				|| ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(st);
	}

	/** Mixed approved/paid and rejected claim lines, including matrix LEVEL_REJECTED. */
	private boolean ticketHasPartialApprovalOutcome(ReimbursementTicket t) {
		if ("Partial".equals(aggregateLevel1ApproverStatus(t))
				|| "Partial".equals(aggregateLevel2ApproverStatus(t))) {
			return true;
		}
		List<ReimbursementTicketClaim> claims = t.getClaims();
		if (claims == null || claims.isEmpty()) {
			return false;
		}
		boolean anyRejected = claims.stream().anyMatch(c -> isRejectedClaimStatus(c.getClaimStatus()));
		boolean anyApproved = claims.stream().anyMatch(c -> {
			String s = c.getClaimStatus();
			return ReimbursementTicketClaim.STATUS_PAID.equals(s)
					|| ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(s)
					|| ReimbursementTicketClaim.STATUS_PENDING_APPROVAL.equals(s);
		});
		return anyRejected && anyApproved;
	}

	/** Same visibility as approve-reimbursement "All tickets" for the logged-in actor. */
	private boolean ticketVisibleToDashboardActor(ReimbursementTicket t, ReimbursementDashboardFilterDTO filter) {
		if (filter == null || filter.getActorEmpId() == null) {
			return true;
		}
		BigInteger empId = BigInteger.valueOf(filter.getActorEmpId());
		String email = filter.getActorEmail();
		if (empId != null && t.getHodEmpId() != null && empId.equals(t.getHodEmpId())) {
			return true;
		}
		if (StringUtils.hasText(email) && StringUtils.hasText(t.getHodEmail())
				&& normEmail(t.getHodEmail()).equals(normEmail(email))) {
			return true;
		}
		if (StringUtils.hasText(email)
				&& (normEmail(email).equals(normEmail(workflowHrMail))
						|| normEmail(email).equals(normEmail(workflowFinanceMail)))) {
			return true;
		}
		return matrixWorkflowService.isActorInApprovalChain(t, empId, email);
	}

	private static boolean isRejectedClaimStatus(String st) {
		return ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(st)
				|| ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(st)
				|| ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(st)
				|| ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(st);
	}

	/** Every claim line is terminal-rejected — ticket must be closed rejected (new submission required). */
	private static boolean allTicketClaimsRejected(ReimbursementTicket ticket) {
		if (ticket.getClaims() == null || ticket.getClaims().isEmpty()) {
			return false;
		}
		for (ReimbursementTicketClaim c : ticket.getClaims()) {
			if (!isRejectedClaimStatus(c.getClaimStatus())) {
				return false;
			}
		}
		return true;
	}

	private Map<String, Double> buildPendingAmountAgingBuckets(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter) {
		Map<String, BigDecimal> acc = new LinkedHashMap<>();
		acc.put("0-7", BigDecimal.ZERO);
		acc.put("8-15", BigDecimal.ZERO);
		acc.put("16-30", BigDecimal.ZERO);
		acc.put("30+", BigDecimal.ZERO);
		long now = System.currentTimeMillis();
		for (ReimbursementTicket t : tickets) {
			if (t.getSubmittedOn() == null) {
				continue;
			}
			long days = Math.max(0L, (now - t.getSubmittedOn().getTime()) / 86400000L);
			String bucket = days <= 7L ? "0-7" : days <= 15L ? "8-15" : days <= 30L ? "16-30" : "30+";
			BigDecimal pend = claimsForMetrics(t, filter)
					.filter(c -> isPipelinePendingClaimStatus(c.getClaimStatus()))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (pend.signum() > 0) {
				acc.merge(bucket, pend, BigDecimal::add);
			}
		}
		Map<String, Double> out = new LinkedHashMap<>();
		for (Map.Entry<String, BigDecimal> e : acc.entrySet()) {
			out.put(e.getKey(), bigDecimalToDouble(e.getValue()));
		}
		return out;
	}

	private String abbrevLabel(String s, int max) {
		if (!StringUtils.hasText(s)) {
			return "—";
		}
		String t = s.trim();
		return t.length() <= max ? t : t.substring(0, Math.max(0, max - 1)) + "…";
	}

	private Map<String, Object> buildProjectEmployeeStackPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter, int maxProjects, int maxEmpsPerProject) {
		Map<Long, BigDecimal> projTotals = new HashMap<>();
		Map<Long, Map<String, BigDecimal[]>> cell = new HashMap<>();
		Map<Long, String> pname = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			String emp = StringUtils.hasText(t.getFullName()) ? t.getFullName().trim()
					: ("Employee #" + (t.getEmpId() != null ? t.getEmpId() : "?"));
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				if (c.getProjectId() == null) {
					continue;
				}
				Long pid = c.getProjectId();
				BigDecimal a = claimAmountOrZero(c);
				projTotals.merge(pid, a, BigDecimal::add);
				if (!pname.containsKey(pid) || !StringUtils.hasText(pname.get(pid))) {
					pname.put(pid, StringUtils.hasText(c.getProjectName()) ? c.getProjectName().trim()
							: ("Project #" + pid));
				}
				BigDecimal[] ar = cell.computeIfAbsent(pid, k -> new HashMap<String, BigDecimal[]>())
						.computeIfAbsent(emp, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO,
								BigDecimal.ZERO, BigDecimal.ZERO });
				ar[0] = ar[0].add(a);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					ar[1] = ar[1].add(a);
				} else if (isPipelinePendingClaimStatus(st)) {
					ar[2] = ar[2].add(a);
				} else if (isRejectedClaimStatus(st)) {
					ar[3] = ar[3].add(a);
				}
			}
		}
		List<Long> topProj = projTotals.entrySet().stream()
				.sorted((a, b) -> b.getValue().compareTo(a.getValue())).map(Map.Entry::getKey).limit(maxProjects)
				.collect(Collectors.toList());
		List<String> categories = new ArrayList<>();
		List<Double> raised = new ArrayList<>();
		List<Double> paid = new ArrayList<>();
		List<Double> pending = new ArrayList<>();
		List<Double> rejected = new ArrayList<>();
		for (Long pid : topProj) {
			String pn = pname.getOrDefault(pid, "Project #" + pid);
			Map<String, BigDecimal[]> emMap = cell.getOrDefault(pid, Collections.emptyMap());
			List<Map.Entry<String, BigDecimal[]>> sortedEm = emMap.entrySet().stream()
					.sorted((a, b) -> b.getValue()[0].compareTo(a.getValue()[0])).limit(maxEmpsPerProject)
					.collect(Collectors.toList());
			for (Map.Entry<String, BigDecimal[]> e : sortedEm) {
				categories.add(abbrevLabel(pn, 22) + " · " + abbrevLabel(e.getKey(), 20));
				BigDecimal[] v = e.getValue();
				raised.add(bigDecimalToDouble(v[0]));
				paid.add(bigDecimalToDouble(v[1]));
				pending.add(bigDecimalToDouble(v[2]));
				rejected.add(bigDecimalToDouble(v[3]));
			}
		}
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("categories", categories);
		out.put("raised", raised);
		out.put("paid", paid);
		out.put("pending", pending);
		out.put("rejected", rejected);
		return out;
	}

	private void sortBarTotalRows(List<Map<String, Object>> rows) {
		rows.sort((a, b) -> {
			long ra = ((Number) a.get("requested")).longValue();
			long rb = ((Number) b.get("requested")).longValue();
			int cmp = Long.compare(rb, ra);
			if (cmp != 0) {
				return cmp;
			}
			return String.valueOf(a.get("label")).compareToIgnoreCase(String.valueOf(b.get("label")));
		});
	}

	/** JPQL projection often returns client names when JPA {@link Client} entity has a null name column. */
	private Map<Integer, String> loadClientNamesFromClientsJpql() {
		Map<Integer, String> m = new HashMap<>();
		for (ClientIdAndName row : clientsRepository.findAllClientIdAndName()) {
			if (row != null && row.getClientId() != null && StringUtils.hasText(row.getClientName())) {
				m.put(row.getClientId(), row.getClientName().trim());
			}
		}
		return m;
	}

	/** Denormalized {@code client_name} on claim rows (not filter-scoped) for dashboard labels. */
	private Map<Integer, String> collectClientNamesFromTicketClaims(List<ReimbursementTicket> tickets) {
		Map<Integer, String> m = new HashMap<>();
		if (tickets == null) {
			return m;
		}
		for (ReimbursementTicket t : tickets) {
			if (t.getClaims() == null) {
				continue;
			}
			for (ReimbursementTicketClaim c : t.getClaims()) {
				if (c.getClientId() == null || !StringUtils.hasText(c.getClientName())) {
					continue;
				}
				Integer cid = c.getClientId();
				String nm = c.getClientName().trim();
				m.merge(cid, nm, (a, b) -> a.length() >= b.length() ? a : b);
			}
		}
		return m;
	}

	/**
	 * Prefer {@link Client#getClientName()}, then JPQL client list, then claim-line {@code client_name}, else
	 * {@code Client #id}.
	 */
	private String resolveDashboardClientLabel(Integer clientId, Client loadedClient, Map<Integer, String> jpqlNames,
			Map<Integer, String> claimNames) {
		if (clientId == null) {
			return "—";
		}
		if (loadedClient != null && StringUtils.hasText(loadedClient.getClientName())) {
			return loadedClient.getClientName().trim();
		}
		String s = jpqlNames != null ? jpqlNames.get(clientId) : null;
		if (StringUtils.hasText(s)) {
			return s.trim();
		}
		s = claimNames != null ? claimNames.get(clientId) : null;
		if (StringUtils.hasText(s)) {
			return s.trim();
		}
		return "Client #" + clientId;
	}

	private Map<String, Object> buildClientBarTotalsPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter, Map<Integer, String> clientNameJpql,
			Map<Integer, String> clientNameClaims) {
		Map<Integer, BigDecimal> reqByCid = new HashMap<>();
		Map<Integer, BigDecimal> paidByCid = new HashMap<>();
		Map<Integer, BigDecimal> pendByCid = new HashMap<>();
		Map<Integer, BigDecimal> rejByCid = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				if (c.getClientId() == null) {
					continue;
				}
				Integer cid = c.getClientId();
				BigDecimal a = claimAmountOrZero(c);
				reqByCid.merge(cid, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByCid.merge(cid, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByCid.merge(cid, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByCid.merge(cid, a, BigDecimal::add);
				}
			}
		}
		Set<Integer> ids = new TreeSet<>(reqByCid.keySet());
		ids.addAll(paidByCid.keySet());
		ids.addAll(pendByCid.keySet());
		ids.addAll(rejByCid.keySet());
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Integer cid : ids) {
			Client cl = clientsRepository.findByClientId(cid);
			String label = resolveDashboardClientLabel(cid, cl, clientNameJpql, clientNameClaims);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("label", label);
			row.put("clientId", cid);
			row.put("requested", bigDecimalToDouble(reqByCid.getOrDefault(cid, BigDecimal.ZERO)));
			row.put("paid", bigDecimalToDouble(paidByCid.getOrDefault(cid, BigDecimal.ZERO)));
			row.put("pending", bigDecimalToDouble(pendByCid.getOrDefault(cid, BigDecimal.ZERO)));
			row.put("rejected", bigDecimalToDouble(rejByCid.getOrDefault(cid, BigDecimal.ZERO)));
			rows.add(row);
		}
		sortBarTotalRows(rows);
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("rows", rows);
		return out;
	}

	/**
	 * One point per row in {@code clients} table (ordered by name): raised / paid / pending / rejected for
	 * in-scope claim lines. Clients with no claims show zeros — used for the client analytics line chart.
	 */
	private Map<String, Object> buildClientLineChartPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter, Map<Integer, String> clientNameJpql,
			Map<Integer, String> clientNameClaims) {
		Map<Integer, BigDecimal> reqByCid = new HashMap<>();
		Map<Integer, BigDecimal> paidByCid = new HashMap<>();
		Map<Integer, BigDecimal> pendByCid = new HashMap<>();
		Map<Integer, BigDecimal> rejByCid = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				if (c.getClientId() == null) {
					continue;
				}
				Integer cid = c.getClientId();
				BigDecimal a = claimAmountOrZero(c);
				reqByCid.merge(cid, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByCid.merge(cid, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByCid.merge(cid, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByCid.merge(cid, a, BigDecimal::add);
				}
			}
		}
		List<Client> allClients = new ArrayList<>(clientsRepository.findAll());
		allClients.sort(Comparator.comparing(
				cl -> resolveDashboardClientLabel(cl.getClientId(), cl, clientNameJpql, clientNameClaims),
				String.CASE_INSENSITIVE_ORDER));
		List<String> categories = new ArrayList<>();
		List<Double> raised = new ArrayList<>();
		List<Double> paid = new ArrayList<>();
		List<Double> pending = new ArrayList<>();
		List<Double> rejected = new ArrayList<>();
		for (Client cl : allClients) {
			if (cl == null || cl.getClientId() == null) {
				continue;
			}
			Integer cid = cl.getClientId();
			String name = resolveDashboardClientLabel(cid, cl, clientNameJpql, clientNameClaims);
			categories.add(abbrevLabel(name, 40));
			raised.add(bigDecimalToDouble(reqByCid.getOrDefault(cid, BigDecimal.ZERO)));
			paid.add(bigDecimalToDouble(paidByCid.getOrDefault(cid, BigDecimal.ZERO)));
			pending.add(bigDecimalToDouble(pendByCid.getOrDefault(cid, BigDecimal.ZERO)));
			rejected.add(bigDecimalToDouble(rejByCid.getOrDefault(cid, BigDecimal.ZERO)));
		}
		Map<String, Object> pack = new LinkedHashMap<>();
		pack.put("categories", categories);
		pack.put("raised", raised);
		pack.put("paid", paid);
		pack.put("pending", pending);
		pack.put("rejected", rejected);
		return pack;
	}

	/**
	 * One point per row in {@code projects} master (ordered by name): raised / paid / pending / rejected for in-scope
	 * claim lines. Projects with no claims show zeros. Project ids present on claims but missing from the master table
	 * (e.g. legacy rows) are appended after the master list, sorted by label.
	 */
	private Map<String, Object> buildProjectLineChartPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter) {
		Map<Long, BigDecimal> reqByPid = new HashMap<>();
		Map<Long, BigDecimal> paidByPid = new HashMap<>();
		Map<Long, BigDecimal> pendByPid = new HashMap<>();
		Map<Long, BigDecimal> rejByPid = new HashMap<>();
		Map<Long, String> labelByPid = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				if (c.getProjectId() == null) {
					continue;
				}
				Long pid = c.getProjectId();
				BigDecimal a = claimAmountOrZero(c);
				reqByPid.merge(pid, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByPid.merge(pid, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByPid.merge(pid, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByPid.merge(pid, a, BigDecimal::add);
				}
				if (!labelByPid.containsKey(pid) || !StringUtils.hasText(labelByPid.get(pid))) {
					String lbl = StringUtils.hasText(c.getProjectName()) ? c.getProjectName().trim()
							: ("Project #" + pid);
					labelByPid.put(pid, lbl);
				}
			}
		}
		List<Project> allProjects = new ArrayList<>(projectRepository.findAll());
		allProjects.removeIf(p -> p == null || p.getProjectId() == null);
		allProjects.sort(Comparator.comparing(
				p -> (p.getProjectName() != null ? p.getProjectName().trim() : ""),
				String.CASE_INSENSITIVE_ORDER));
		Set<Long> masterPids = new LinkedHashSet<>();
		for (Project p : allProjects) {
			masterPids.add(p.getProjectId().longValue());
		}
		List<String> categories = new ArrayList<>();
		List<Double> raised = new ArrayList<>();
		List<Double> paid = new ArrayList<>();
		List<Double> pending = new ArrayList<>();
		List<Double> rejected = new ArrayList<>();
		for (Project p : allProjects) {
			Long pid = p.getProjectId().longValue();
			String name = StringUtils.hasText(p.getProjectName()) ? p.getProjectName().trim() : ("Project #" + pid);
			categories.add(abbrevLabel(name, 40));
			raised.add(bigDecimalToDouble(reqByPid.getOrDefault(pid, BigDecimal.ZERO)));
			paid.add(bigDecimalToDouble(paidByPid.getOrDefault(pid, BigDecimal.ZERO)));
			pending.add(bigDecimalToDouble(pendByPid.getOrDefault(pid, BigDecimal.ZERO)));
			rejected.add(bigDecimalToDouble(rejByPid.getOrDefault(pid, BigDecimal.ZERO)));
		}
		List<Long> orphanPids = reqByPid.keySet().stream().filter(id -> !masterPids.contains(id))
				.sorted(Comparator.comparing(id -> labelByPid.getOrDefault(id, "Project #" + id),
						String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
		for (Long pid : orphanPids) {
			categories.add(abbrevLabel(labelByPid.getOrDefault(pid, "Project #" + pid), 40));
			raised.add(bigDecimalToDouble(reqByPid.getOrDefault(pid, BigDecimal.ZERO)));
			paid.add(bigDecimalToDouble(paidByPid.getOrDefault(pid, BigDecimal.ZERO)));
			pending.add(bigDecimalToDouble(pendByPid.getOrDefault(pid, BigDecimal.ZERO)));
			rejected.add(bigDecimalToDouble(rejByPid.getOrDefault(pid, BigDecimal.ZERO)));
		}
		Map<String, Object> pack = new LinkedHashMap<>();
		pack.put("categories", categories);
		pack.put("raised", raised);
		pack.put("paid", paid);
		pack.put("pending", pending);
		pack.put("rejected", rejected);
		return pack;
	}

	/**
	 * One point per row in {@code department} master (via {@link DepartmentRepository#getAllDeptsList()}), ordered by
	 * name: raised / paid / pending / rejected for in-scope claim lines on tickets in that department (matched
	 * case-insensitively to ticket.department). Departments with no matching tickets show zeros. Extra categories are
	 * appended for ticket department strings that do not match any master row (e.g. legacy typos).
	 */
	private Map<String, Object> buildDepartmentLineChartPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter) {
		Map<String, BigDecimal> reqByNorm = new HashMap<>();
		Map<String, BigDecimal> paidByNorm = new HashMap<>();
		Map<String, BigDecimal> pendByNorm = new HashMap<>();
		Map<String, BigDecimal> rejByNorm = new HashMap<>();
		Map<String, String> normToDisplay = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			String displayDept = (t.getDepartment() != null && StringUtils.hasText(t.getDepartment()))
					? t.getDepartment().trim() : "Unknown";
			String nk = displayDept.toLowerCase(Locale.ROOT);
			normToDisplay.putIfAbsent(nk, displayDept);
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				BigDecimal a = claimAmountOrZero(c);
				reqByNorm.merge(nk, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByNorm.merge(nk, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByNorm.merge(nk, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByNorm.merge(nk, a, BigDecimal::add);
				}
			}
		}
		List<Department> masterDepts = new ArrayList<>(departmentRepository.getAllDeptsList());
		masterDepts.sort(Comparator.comparing(
				d -> (d != null && d.getName() != null ? d.getName().trim() : ""),
				String.CASE_INSENSITIVE_ORDER));
		List<String> categories = new ArrayList<>();
		List<Double> raised = new ArrayList<>();
		List<Double> paid = new ArrayList<>();
		List<Double> pending = new ArrayList<>();
		List<Double> rejected = new ArrayList<>();
		Set<String> masterNormKeys = new LinkedHashSet<>();
		for (Department d : masterDepts) {
			if (d == null || !StringUtils.hasText(d.getName())) {
				continue;
			}
			String label = d.getName().trim();
			String nk = label.toLowerCase(Locale.ROOT);
			masterNormKeys.add(nk);
			categories.add(abbrevLabel(label, 28));
			raised.add(bigDecimalToDouble(reqByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			paid.add(bigDecimalToDouble(paidByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			pending.add(bigDecimalToDouble(pendByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			rejected.add(bigDecimalToDouble(rejByNorm.getOrDefault(nk, BigDecimal.ZERO)));
		}
		List<String> orphanKeys = reqByNorm.keySet().stream().filter(k -> !masterNormKeys.contains(k))
				.sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		for (String nk : orphanKeys) {
			String disp = normToDisplay.getOrDefault(nk, nk);
			categories.add(abbrevLabel(disp, 28));
			raised.add(bigDecimalToDouble(reqByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			paid.add(bigDecimalToDouble(paidByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			pending.add(bigDecimalToDouble(pendByNorm.getOrDefault(nk, BigDecimal.ZERO)));
			rejected.add(bigDecimalToDouble(rejByNorm.getOrDefault(nk, BigDecimal.ZERO)));
		}
		Map<String, Object> pack = new LinkedHashMap<>();
		pack.put("categories", categories);
		pack.put("raised", raised);
		pack.put("paid", paid);
		pack.put("pending", pending);
		pack.put("rejected", rejected);
		return pack;
	}

	private Map<String, Object> buildProjectBarTotalsPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter) {
		Map<Long, BigDecimal> reqByPid = new HashMap<>();
		Map<Long, BigDecimal> paidByPid = new HashMap<>();
		Map<Long, BigDecimal> pendByPid = new HashMap<>();
		Map<Long, BigDecimal> rejByPid = new HashMap<>();
		Map<Long, String> labelByPid = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				if (c.getProjectId() == null) {
					continue;
				}
				Long pid = c.getProjectId();
				BigDecimal a = claimAmountOrZero(c);
				reqByPid.merge(pid, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByPid.merge(pid, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByPid.merge(pid, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByPid.merge(pid, a, BigDecimal::add);
				}
				if (!labelByPid.containsKey(pid) || !StringUtils.hasText(labelByPid.get(pid))) {
					String lbl = StringUtils.hasText(c.getProjectName()) ? c.getProjectName().trim()
							: ("Project #" + pid);
					labelByPid.put(pid, lbl);
				}
			}
		}
		Set<Long> pids = new TreeSet<>(reqByPid.keySet());
		pids.addAll(paidByPid.keySet());
		pids.addAll(pendByPid.keySet());
		pids.addAll(rejByPid.keySet());
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Long pid : pids) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("label", labelByPid.getOrDefault(pid, "Project #" + pid));
			row.put("projectId", pid);
			row.put("requested", bigDecimalToDouble(reqByPid.getOrDefault(pid, BigDecimal.ZERO)));
			row.put("paid", bigDecimalToDouble(paidByPid.getOrDefault(pid, BigDecimal.ZERO)));
			row.put("pending", bigDecimalToDouble(pendByPid.getOrDefault(pid, BigDecimal.ZERO)));
			row.put("rejected", bigDecimalToDouble(rejByPid.getOrDefault(pid, BigDecimal.ZERO)));
			rows.add(row);
		}
		sortBarTotalRows(rows);
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("rows", rows);
		return out;
	}

	private Map<String, Object> buildDepartmentBarTotalsPack(List<ReimbursementTicket> tickets,
			ReimbursementDashboardFilterDTO filter) {
		Map<String, BigDecimal> reqByDept = new HashMap<>();
		Map<String, BigDecimal> paidByDept = new HashMap<>();
		Map<String, BigDecimal> pendByDept = new HashMap<>();
		Map<String, BigDecimal> rejByDept = new HashMap<>();
		for (ReimbursementTicket t : tickets) {
			String dept = (t.getDepartment() != null && StringUtils.hasText(t.getDepartment()))
					? t.getDepartment().trim() : "Unknown";
			for (ReimbursementTicketClaim c : claimsForMetrics(t, filter).collect(Collectors.toList())) {
				BigDecimal a = claimAmountOrZero(c);
				reqByDept.merge(dept, a, BigDecimal::add);
				String st = c.getClaimStatus();
				if (ReimbursementTicketClaim.STATUS_PAID.equals(st)) {
					paidByDept.merge(dept, a, BigDecimal::add);
				} else if (isPipelinePendingClaimStatus(st)) {
					pendByDept.merge(dept, a, BigDecimal::add);
				} else if (isRejectedClaimStatus(st)) {
					rejByDept.merge(dept, a, BigDecimal::add);
				}
			}
		}
		Set<String> depts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		depts.addAll(reqByDept.keySet());
		depts.addAll(paidByDept.keySet());
		depts.addAll(pendByDept.keySet());
		depts.addAll(rejByDept.keySet());
		List<Map<String, Object>> rows = new ArrayList<>();
		for (String dept : depts) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("label", dept);
			row.put("requested", bigDecimalToDouble(reqByDept.getOrDefault(dept, BigDecimal.ZERO)));
			row.put("paid", bigDecimalToDouble(paidByDept.getOrDefault(dept, BigDecimal.ZERO)));
			row.put("pending", bigDecimalToDouble(pendByDept.getOrDefault(dept, BigDecimal.ZERO)));
			row.put("rejected", bigDecimalToDouble(rejByDept.getOrDefault(dept, BigDecimal.ZERO)));
			rows.add(row);
		}
		sortBarTotalRows(rows);
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("rows", rows);
		return out;
	}

	private boolean financeLevelEngaged(ReimbursementTicket t) {
		if (ReimbursementTicket.STAGE_PAID.equals(t.getWorkflowStage())
				|| ReimbursementTicket.STAGE_PENDING_FINANCE.equals(t.getWorkflowStage())) {
			return true;
		}
		if (t.getClaims() != null) {
			return t.getClaims().stream()
					.anyMatch(c -> ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(c.getClaimStatus()));
		}
		return false;
	}

	private static String dashboardRejectionLevelLabel(String claimStatus) {
		if (ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(claimStatus)) {
			return "HOD L1";
		}
		if (ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(claimStatus)) {
			return "HR L2";
		}
		if (ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(claimStatus)) {
			return "Approval level";
		}
		if (ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(claimStatus)) {
			return "Finance L3";
		}
		return "—";
	}

	private String dashboardRejectionReason(ReimbursementTicket t, ReimbursementTicketClaim c) {
		String st = c.getClaimStatus();
		if (ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(st)
				|| ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(st)) {
			return c.getHodRemarks();
		}
		if (ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(st)) {
			return c.getHrRemarks();
		}
		if (ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(st)) {
			return t.getFinanceRejectReason();
		}
		return null;
	}

	private String dashboardRejectionActorLabel(ReimbursementTicket t, ReimbursementTicketClaim c) {
		String st = c.getClaimStatus();
		if (ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(st)) {
			return StringUtils.hasText(t.getHodName()) ? t.getHodName() : "Head of department";
		}
		if (ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(st)) {
			return "Matrix approver";
		}
		if (ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(st)) {
			String hrName = lookupEmployeeNameByEmail(workflowHrMail);
			return StringUtils.hasText(hrName) ? hrName : "HR approver";
		}
		if (ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(st)) {
			String finName = lookupEmployeeNameByEmail(workflowFinanceMail);
			return StringUtils.hasText(finName) ? finName : "Finance";
		}
		return "—";
	}

	@Transactional(readOnly = true)
	public ServiceResponse dashboard(ReimbursementDashboardFilterDTO filter) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<ReimbursementTicket> allActive = ticketRepository.findAllActiveWithClaims();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final Timestamp fromTs;
			if (filter != null && StringUtils.hasText(filter.getFromDate())) {
				fromTs = new Timestamp(sdf.parse(filter.getFromDate().trim()).getTime());
			} else {
				fromTs = null;
			}
			final Timestamp toTs;
			if (filter != null && StringUtils.hasText(filter.getToDate())) {
				toTs = new Timestamp(sdf.parse(filter.getToDate().trim()).getTime() + 86400000L - 1);
			} else {
				toTs = null;
			}

			List<ReimbursementTicket> actorScoped = allActive.stream()
					.filter(t -> ticketVisibleToDashboardActor(t, filter)).collect(Collectors.toList());
			List<ReimbursementTicket> tickets = actorScoped.stream()
					.filter(t -> ticketMatchesDashboardFilters(t, filter, fromTs, toTs)).collect(Collectors.toList());

			long claimCount = tickets.stream().mapToLong(t -> claimsForMetrics(t, filter).count()).sum();
			BigDecimal submittedAmt = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal paidAmt = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal rejectedAmt = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> isRejectedClaimStatus(c.getClaimStatus()))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal approvedAmt = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus())
							|| ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			long rejectedClaimLines = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> isRejectedClaimStatus(c.getClaimStatus()))
					.count();
			long approvedClaimLines = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus())
							|| ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()))
					.count();
			long partialApprovalTickets = tickets.stream().filter(this::ticketHasPartialApprovalOutcome).count();

			long pendingHod = tickets.stream()
					.filter(t -> ReimbursementTicket.STAGE_PENDING_HOD.equals(t.getWorkflowStage())).count();
			long pendingHr = tickets.stream()
					.filter(t -> ReimbursementTicket.STAGE_PENDING_HR.equals(t.getWorkflowStage())).count();
			long pendingFin = tickets.stream()
					.filter(t -> ReimbursementTicket.STAGE_PENDING_FINANCE.equals(t.getWorkflowStage())).count();

			Map<String, BigDecimal> byDept = new HashMap<>();
			for (ReimbursementTicket t : tickets) {
				String d = t.getDepartment() != null ? t.getDepartment() : "Unknown";
				BigDecimal sum = claimsForMetrics(t, filter).map(ReimbursementTicketClaim::getAmount)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				byDept.merge(d, sum, BigDecimal::add);
			}

			Map<String, Long> byCategory = new HashMap<>();
			for (ReimbursementTicket t : tickets) {
				claimsForMetrics(t, filter).forEach(c -> {
					String cat = c.getExpenditureType() != null ? c.getExpenditureType() : "Unknown";
					byCategory.merge(cat, 1L, Long::sum);
				});
			}

			Map<String, BigDecimal> topClaimers = new LinkedHashMap<>();
			Map<String, BigDecimal> empTotals = new HashMap<>();
			for (ReimbursementTicket t : tickets) {
				String name = t.getFullName() != null ? t.getFullName() : String.valueOf(t.getEmpId());
				BigDecimal sum = claimsForMetrics(t, filter).map(ReimbursementTicketClaim::getAmount)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				empTotals.merge(name, sum, BigDecimal::add);
			}
			empTotals.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(10)
					.forEach(e -> topClaimers.put(e.getKey(), e.getValue()));

			BigDecimal pipelinePendingAmt = tickets.stream().flatMap(t -> claimsForMetrics(t, filter))
					.filter(c -> isPipelinePendingClaimStatus(c.getClaimStatus()))
					.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			Map<String, Map<String, Object>> empBoard = new LinkedHashMap<>();
			for (ReimbursementTicket t : tickets) {
				String key = t.getEmpId() != null ? t.getEmpId().toString()
						: "u:" + String.valueOf(t.getTicketId());
				Map<String, Object> row = empBoard.computeIfAbsent(key, k -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("empId", t.getEmpId());
					m.put("fullName", t.getFullName());
					m.put("department", t.getDepartment());
					m.put("ticketCount", 0L);
					m.put("requested", BigDecimal.ZERO);
					m.put("paid", BigDecimal.ZERO);
					m.put("rejectedLines", 0L);
					m.put("claimLines", 0L);
					return m;
				});
				row.put("ticketCount", ((Long) row.get("ticketCount")) + 1);
				claimsForMetrics(t, filter).forEach(c -> {
					row.put("claimLines", ((Long) row.get("claimLines")) + 1);
					BigDecimal amt = claimAmountOrZero(c);
					row.put("requested", ((BigDecimal) row.get("requested")).add(amt));
					if (ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus())) {
						row.put("paid", ((BigDecimal) row.get("paid")).add(amt));
					}
					if (isRejectedClaimStatus(c.getClaimStatus())) {
						row.put("rejectedLines", ((Long) row.get("rejectedLines")) + 1);
					}
				});
			}
			List<Map<String, Object>> employeeLeaderboard = empBoard.values().stream()
					.sorted((a, b) -> ((BigDecimal) b.get("requested")).compareTo((BigDecimal) a.get("requested")))
					.limit(20).map(m -> {
						Map<String, Object> out = new LinkedHashMap<>(m);
						long cl = (Long) out.get("claimLines");
						long rj = (Long) out.get("rejectedLines");
						double rejPct = cl == 0 ? 0d : (100.0 * rj / cl);
						out.put("rejectionRatePct", Math.round(rejPct * 10.0) / 10.0);
						out.remove("rejectedLines");
						out.remove("claimLines");
						return out;
					}).collect(Collectors.toList());

			Timestamp cutoff = new Timestamp(System.currentTimeMillis() - 7L * 86400000L);
			long aging = tickets.stream()
					.filter(t -> (ReimbursementTicket.STAGE_PENDING_HOD.equals(t.getWorkflowStage())
							|| ReimbursementTicket.STAGE_PENDING_HR.equals(t.getWorkflowStage())
							|| ReimbursementTicket.STAGE_PENDING_FINANCE.equals(t.getWorkflowStage())
							|| ReimbursementTicket.STAGE_PENDING_LEVEL.equals(t.getWorkflowStage()))
							&& t.getSubmittedOn() != null && t.getSubmittedOn().before(cutoff))
					.count();

			Map<String, Long> ticketStatusBreakdown = new LinkedHashMap<>();
			for (ReimbursementTicket t : tickets) {
				String ds = displayTicketStatus(t);
				ticketStatusBreakdown.merge(ds, 1L, Long::sum);
			}

			Map<String, Map<String, Double>> monthlyTrend = new TreeMap<>();
			for (ReimbursementTicket t : tickets) {
				String ym = yearMonthKey(t.getSubmittedOn());
				if (ym == null) {
					continue;
				}
				Map<String, Double> bucket = monthlyTrend.computeIfAbsent(ym, k -> {
					Map<String, Double> m = new LinkedHashMap<>();
					m.put("requested", 0.0);
					m.put("paid", 0.0);
					return m;
				});
				BigDecimal req = claimsForMetrics(t, filter).map(ReimbursementTicketClaim::getAmount)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				bucket.put("requested", bucket.get("requested") + bigDecimalToDouble(req));
			}
			for (ReimbursementTicket t : tickets) {
				if (!ReimbursementTicket.STAGE_PAID.equals(t.getWorkflowStage()) || t.getPaidOn() == null) {
					continue;
				}
				String pym = yearMonthKey(t.getPaidOn());
				if (pym == null) {
					continue;
				}
				Map<String, Double> bucket = monthlyTrend.computeIfAbsent(pym, k -> {
					Map<String, Double> m = new LinkedHashMap<>();
					m.put("requested", 0.0);
					m.put("paid", 0.0);
					return m;
				});
				BigDecimal paidOnTicket = claimsForMetrics(t, filter)
						.filter(c -> ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()))
						.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				bucket.put("paid", bucket.get("paid") + bigDecimalToDouble(paidOnTicket));
			}

			Map<String, Long> rejectionsByExpenditureType = new LinkedHashMap<>();
			Map<String, Long> rejectionReasonBuckets = new LinkedHashMap<>();
			List<Map<String, Object>> rejectionLog = new ArrayList<>();
			for (ReimbursementTicket t : tickets) {
				claimsForMetrics(t, filter).forEach(c -> {
					if (!isRejectedClaimStatus(c.getClaimStatus())) {
						return;
					}
					String et = c.getExpenditureType() != null ? c.getExpenditureType() : "Unknown";
					rejectionsByExpenditureType.merge(et, 1L, Long::sum);
					String reason = dashboardRejectionReason(t, c);
					if (!StringUtils.hasText(reason)) {
						reason = "(no reason)";
					}
					rejectionReasonBuckets.merge(reason.trim(), 1L, Long::sum);
					if (rejectionLog.size() < 100) {
						Map<String, Object> row = new LinkedHashMap<>();
						row.put("ticketId", t.getTicketId());
						row.put("ticketNo", t.getTicketNo());
						row.put("employeeName", t.getFullName());
						row.put("expenditureType", et);
						row.put("amount", c.getAmount());
						row.put("rejectedBy", dashboardRejectionActorLabel(t, c));
						row.put("rejectionLevel", dashboardRejectionLevelLabel(c.getClaimStatus()));
						row.put("reason", reason);
						row.put("rejectedOn", null);
						rejectionLog.add(row);
					}
				});
			}

			List<Map<String, Object>> ticketRows = tickets.stream()
					.sorted(Comparator.comparing(ReimbursementTicket::getSubmittedOn,
							Comparator.nullsLast(Comparator.naturalOrder())).reversed())
					.limit(150).map(this::toViewMap).collect(Collectors.toList());

			long submittedTotal = tickets.size();
			long l1Reviewed = tickets.stream()
					.filter(t -> !ReimbursementTicket.STAGE_PENDING_HOD.equals(t.getWorkflowStage())).count();
			long l2Reviewed = tickets.stream()
					.filter(t -> !ReimbursementTicket.STAGE_PENDING_HOD.equals(t.getWorkflowStage())
							&& !ReimbursementTicket.STAGE_PENDING_HR.equals(t.getWorkflowStage()))
					.count();
			long l3Reviewed = tickets.stream().filter(this::financeLevelEngaged).count();
			long paidClosed = tickets.stream().filter(t -> ReimbursementTicket.STAGE_PAID.equals(t.getWorkflowStage()))
					.count();

			List<String> departmentOptions = new ArrayList<>();
			Set<String> deptNormSeen = new HashSet<>();
			List<Department> deptMasterList = new ArrayList<>(departmentRepository.getAllDeptsList());
			deptMasterList.sort(Comparator.comparing(
					d -> (d != null && d.getName() != null ? d.getName().trim() : ""),
					String.CASE_INSENSITIVE_ORDER));
			for (Department d : deptMasterList) {
				if (d == null || !StringUtils.hasText(d.getName())) {
					continue;
				}
				String nm = d.getName().trim();
				String nk = nm.toLowerCase(Locale.ROOT);
				if (deptNormSeen.add(nk)) {
					departmentOptions.add(nm);
				}
			}
			Set<String> expenditureTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			Set<String> displayStatuses = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			Set<String> workflowStages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			List<Map<String, Object>> projectOptions = new ArrayList<>();
			Set<Long> projectIdsSeen = new HashSet<>();
			for (ReimbursementTicket t : actorScoped) {
				displayStatuses.add(displayTicketStatus(t));
				if (t.getWorkflowStage() != null) {
					workflowStages.add(t.getWorkflowStage());
				}
				if (t.getClaims() != null) {
					for (ReimbursementTicketClaim c : t.getClaims()) {
						if (c.getExpenditureType() != null && StringUtils.hasText(c.getExpenditureType())) {
							expenditureTypes.add(c.getExpenditureType().trim());
						}
						if (c.getProjectId() != null && !projectIdsSeen.contains(c.getProjectId())) {
							projectIdsSeen.add(c.getProjectId());
							Map<String, Object> po = new LinkedHashMap<>();
							po.put("projectId", c.getProjectId());
							po.put("projectName", c.getProjectName() != null ? c.getProjectName()
									: String.valueOf(c.getProjectId()));
							projectOptions.add(po);
						}
					}
				}
			}
			projectOptions.sort(Comparator.comparing(m -> String.valueOf(m.get("projectName"))));
			Map<Integer, String> clientNameJpql = loadClientNamesFromClientsJpql();
			Map<Integer, String> clientNameClaims = collectClientNamesFromTicketClaims(actorScoped);
			List<Map<String, Object>> clientOptions = new ArrayList<>();
			List<Client> allClientsForFilter = new ArrayList<>(clientsRepository.findAll());
			allClientsForFilter.sort(Comparator.comparing(
					c -> resolveDashboardClientLabel(c.getClientId(), c, clientNameJpql, clientNameClaims),
					String.CASE_INSENSITIVE_ORDER));
			for (Client cEnt : allClientsForFilter) {
				if (cEnt == null || cEnt.getClientId() == null) {
					continue;
				}
				Map<String, Object> co = new LinkedHashMap<>();
				co.put("clientId", cEnt.getClientId());
				co.put("clientName",
						resolveDashboardClientLabel(cEnt.getClientId(), cEnt, clientNameJpql, clientNameClaims));
				clientOptions.add(co);
			}

			List<Map<String, Object>> alerts = new ArrayList<>();
			for (ReimbursementTicket t : tickets) {
				if (ReimbursementTicket.STAGE_PENDING_FINANCE.equals(t.getWorkflowStage())
						&& t.getSubmittedOn() != null
						&& t.getSubmittedOn().before(new Timestamp(System.currentTimeMillis() - 2L * 86400000L))) {
					BigDecimal pendingAmt = claimsForMetrics(t, filter)
							.filter(c -> ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus()))
							.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					if (pendingAmt.compareTo(new BigDecimal("50000")) >= 0) {
						Map<String, Object> a = new LinkedHashMap<>();
						a.put("severity", "HIGH");
						a.put("type", "FINANCE_SLA");
						a.put("ticketId", t.getTicketId());
						a.put("ticketNo", t.getTicketNo());
						a.put("message", "High-value ticket pending Finance > 2 days");
						a.put("amountPending", pendingAmt);
						alerts.add(a);
					}
				}
				if (ReimbursementTicket.STAGE_PENDING_HOD.equals(t.getWorkflowStage())
						&& t.getSubmittedOn() != null
						&& t.getSubmittedOn().before(new Timestamp(System.currentTimeMillis() - 2L * 86400000L))) {
					Map<String, Object> a = new LinkedHashMap<>();
					a.put("severity", "HIGH");
					a.put("type", "HOD_SLA");
					a.put("ticketId", t.getTicketId());
					a.put("ticketNo", t.getTicketNo());
					a.put("message", "HOD approval overdue (> 2 days)");
					alerts.add(a);
				}
			}

			Map<String, Object> dash = new LinkedHashMap<>();
			dash.put("ticketCount", tickets.size());
			dash.put("claimCount", claimCount);
			dash.put("totalSubmittedAmount", submittedAmt);
			dash.put("totalPaidAmount", paidAmt);
			dash.put("totalRejectedClaimsAmount", rejectedAmt);
			dash.put("totalApprovedAmount", approvedAmt);
			dash.put("approvedClaimLines", approvedClaimLines);
			dash.put("rejectedClaimLines", rejectedClaimLines);
			dash.put("partialApprovalTickets", partialApprovalTickets);
			dash.put("pipelinePendingAmount", pipelinePendingAmt);
			dash.put("employeeLeaderboard", employeeLeaderboard);
			dash.put("pendingHodTickets", pendingHod);
			dash.put("pendingHrTickets", pendingHr);
			dash.put("pendingFinanceTickets", pendingFin);
			dash.put("departmentSpending", byDept);
			dash.put("claimsByExpenditureType", byCategory);
			dash.put("topClaimersByAmount", topClaimers);
			dash.put("agingPendingTicketsOver7Days", aging);
			dash.put("ticketStatusBreakdown", ticketStatusBreakdown);
			dash.put("monthlyTrend", monthlyTrend);
			dash.put("barTotalsByClient",
					buildClientBarTotalsPack(tickets, filter, clientNameJpql, clientNameClaims));
			dash.put("barTotalsByProject", buildProjectBarTotalsPack(tickets, filter));
			dash.put("barTotalsByDepartment", buildDepartmentBarTotalsPack(tickets, filter));
			dash.put("pendingAmountAging", buildPendingAmountAgingBuckets(tickets, filter));
			dash.put("clientLineChartPack", buildClientLineChartPack(tickets, filter, clientNameJpql, clientNameClaims));
			dash.put("departmentLineChartPack", buildDepartmentLineChartPack(tickets, filter));
			dash.put("projectLineChartPack", buildProjectLineChartPack(tickets, filter));
			dash.put("projectEmployeeStack", buildProjectEmployeeStackPack(tickets, filter, 8, 6));
			dash.put("rejectionsByExpenditureType", rejectionsByExpenditureType);
			dash.put("rejectionReasonBuckets", rejectionReasonBuckets);
			dash.put("rejectionLog", rejectionLog);
			dash.put("ticketRows", ticketRows);
			dash.put("approvalFunnel", new LinkedHashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					put("submitted", submittedTotal);
					put("l1Reviewed", l1Reviewed);
					put("l2Reviewed", l2Reviewed);
					put("l3Reviewed", l3Reviewed);
					put("paidClosed", paidClosed);
				}
			});
			Map<String, Object> filterOptions = new LinkedHashMap<>();
			filterOptions.put("departments", departmentOptions);
			filterOptions.put("expenditureTypes", new ArrayList<>(expenditureTypes));
			filterOptions.put("ticketStatuses", new ArrayList<>(displayStatuses));
			filterOptions.put("workflowStages", new ArrayList<>(workflowStages));
			filterOptions.put("projects", projectOptions);
			filterOptions.put("clients", clientOptions);
			List<Map<String, Object>> employeeOptions = new ArrayList<>();
			Set<String> empOptKeys = new HashSet<>();
			for (ReimbursementTicket t : actorScoped) {
				if (t.getEmpId() == null) {
					continue;
				}
				String ek = t.getEmpId().toString();
				if (empOptKeys.add(ek)) {
					Map<String, Object> eo = new LinkedHashMap<>();
					eo.put("empId", t.getEmpId().longValue());
					eo.put("fullName", t.getFullName());
					employeeOptions.add(eo);
				}
			}
			employeeOptions.sort(Comparator.comparing(m -> String.valueOf(m.get("fullName")),
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
			filterOptions.put("employees", employeeOptions);
			dash.put("filterOptions", filterOptions);
			dash.put("alerts", alerts);

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(dash);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	private List<Map<String, Object>> buildApprovalLevelsForTicket(ReimbursementTicket t) {
		if (matrixWorkflowService.usesMatrixWorkflow(t)) {
			List<Map<String, Object>> matrixLevels = matrixWorkflowService.buildApprovalLevels(t);
			if (!matrixLevels.isEmpty()) {
				return matrixLevels;
			}
		}
		Long empId = t.getEmpId() != null ? t.getEmpId().longValue() : null;
		ReimbursementApprovalMatrixDTO matrix = reimbursementApprovalMatrixService
				.findMatchingMatrixForEmployee(empId, t.getDepartment());
		List<Map<String, Object>> out = new ArrayList<>();
		String hodName = t.getHodName();
		String hrName = lookupEmployeeNameByEmail(workflowHrMail);
		String finName = lookupEmployeeNameByEmail(workflowFinanceMail);
		String s1 = aggregateLevel1ApproverStatus(t);
		String s2 = aggregateLevel2ApproverStatus(t);
		String s3 = aggregateLevel3ApproverStatus(t);

		if (matrix == null || matrix.getLevels() == null || matrix.getLevels().isEmpty()) {
			out.add(approvalLevelRow(1, "HOD", hodName, s1, false));
			out.add(approvalLevelRow(2, "HR", hrName, s2, false));
			out.add(approvalLevelRow(3, "Finance", finName, s3, true));
			return out;
		}

		List<ReimbursementApprovalMatrixLevelDTO> cfg = matrix.getLevels();
		for (int i = 0; i < cfg.size(); i++) {
			ReimbursementApprovalMatrixLevelDTO lvl = cfg.get(i);
			String label = reimbursementApprovalMatrixService.formatLevelLabel(lvl);
			String name = matrixWorkflowService.plannedApproverDisplay(lvl, t);
			String status = statusForConfiguredLevelBeyondThree(t);
			if (i == 0 && !matrixWorkflowService.usesMatrixWorkflow(t)) {
				name = hodName;
				status = s1;
			} else if (i == 1 && !matrixWorkflowService.usesMatrixWorkflow(t)) {
				name = hrName;
				status = s2;
			} else if (i == 2 && !matrixWorkflowService.usesMatrixWorkflow(t)) {
				name = finName;
				status = s3;
			}
			out.add(approvalLevelRow(lvl.getOrder() != null ? lvl.getOrder() : i + 1, label, name, status, false));
		}
		out.add(approvalLevelRow(cfg.size() + 1, "Finance", finName, s3, true));
		return out;
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

	private String plannedApproverDisplay(ReimbursementApprovalMatrixLevelDTO lvl, ReimbursementTicket t) {
		if (lvl == null) {
			return "—";
		}
		if ("REPORTING_MANAGER".equals(lvl.getRouting()) && t.getEmpId() != null) {
			Employee emp = employeeRepository.findByEmpId(t.getEmpId().longValue());
			if (emp != null && emp.getManagerId() != null) {
				Employee mgr = employeeRepository.findByEmpId(emp.getManagerId());
				if (mgr != null && StringUtils.hasText(mgr.getName())) {
					return mgr.getName();
				}
			}
		}
		if ("HOD_SUBMITTER_DEPT".equals(lvl.getRouting())) {
			return t.getHodName();
		}
		if (lvl.getAssigneeEmployeeId() != null) {
			Employee assignee = employeeRepository.findByEmpId(lvl.getAssigneeEmployeeId());
			if (assignee != null && StringUtils.hasText(assignee.getName())) {
				return assignee.getName();
			}
		}
		if ("POOL_ANY_IN_SCOPE".equals(lvl.getRouting())) {
			return "Approver pool";
		}
		if ("SPECIFIC_IN_SCOPE".equals(lvl.getRouting())) {
			return "Specific approver";
		}
		return "—";
	}

	private String statusForConfiguredLevelBeyondThree(ReimbursementTicket t) {
		if (ReimbursementTicket.STAGE_PAID.equals(t.getWorkflowStage())) {
			return "Approved";
		}
		if (ReimbursementTicket.STAGE_REJECTED.equals(t.getWorkflowStage())) {
			return "—";
		}
		return "Pending";
	}

	private void syncLegacyLevelFields(Map<String, Object> m, List<Map<String, Object>> approvalLevels) {
		for (int i = 0; i < approvalLevels.size(); i++) {
			Map<String, Object> row = approvalLevels.get(i);
			int idx = i + 1;
			m.put("level" + idx + "ApproverName", row.get("approverName"));
			m.put("level" + idx + "ApproverStatus", row.get("approverStatus"));
			m.put("level" + idx + "Label", row.get("levelLabel"));
		}
		if (!approvalLevels.isEmpty()) {
			m.put("level1ApproverName", approvalLevels.get(0).get("approverName"));
			m.put("level1ApproverStatus", approvalLevels.get(0).get("approverStatus"));
		}
		if (approvalLevels.size() > 1) {
			m.put("level2ApproverName", approvalLevels.get(1).get("approverName"));
			m.put("level2ApproverStatus", approvalLevels.get(1).get("approverStatus"));
		}
		if (approvalLevels.size() > 2) {
			m.put("level3ApproverName", approvalLevels.get(2).get("approverName"));
			m.put("level3ApproverStatus", approvalLevels.get(2).get("approverStatus"));
		}
	}

	private Map<String, Object> toViewMap(ReimbursementTicket t) {
		Map<String, Object> m = new LinkedHashMap<>();
		List<Map<String, Object>> approvalLevels = buildApprovalLevelsForTicket(t);
		m.put("ticketId", t.getTicketId());
		m.put("ticketNo", t.getTicketNo());
		m.put("recordType", "TICKET");
		m.put("displayStatus", displayTicketStatus(t));
		m.put("workflowStage", t.getWorkflowStage());
		m.put("approvalMatrixId", t.getApprovalMatrixId());
		m.put("currentLevelOrder", t.getCurrentLevelOrder());
		m.put("currentAssigneeEmpId", t.getCurrentAssigneeEmpId());
		m.put("approvalLevels", approvalLevels);
		syncLegacyLevelFields(m, approvalLevels);
		m.put("level2ApproverEmail", workflowHrMail);
		m.put("level3ApproverEmail", workflowFinanceMail);
		m.put("empId", t.getEmpId());
		m.put("employeementId", lookupEmployeementIdByEmpId(t.getEmpId()));
		m.put("fullName", t.getFullName());
		m.put("email", t.getEmail());
		m.put("department", t.getDepartment());
		m.put("designation", t.getDesignation());
		m.put("mobileNo", t.getMobileNo());
		m.put("hodName", t.getHodName());
		m.put("hodEmail", t.getHodEmail());
		m.put("hodEmpId", t.getHodEmpId());
		m.put("submittedOn", t.getSubmittedOn());
		m.put("financeRejectReason", t.getFinanceRejectReason());
		m.put("paidOn", t.getPaidOn());
		BigDecimal total = t.getClaims().stream().map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		m.put("totalClaimAmount", total);
		BigDecimal paidClaimAmount = t.getClaims().stream()
				.filter(c -> ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus()))
				.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		m.put("paidClaimAmount", paidClaimAmount);
		List<Map<String, Object>> rejectedLines = buildRejectedClaimBreakdown(t);
		m.put("rejectedClaimLines", rejectedLines);
		m.put("rejectionSummaryText", buildRejectionSummaryText(rejectedLines));
		m.put("payableApprovedAmount", t.getClaims().stream()
				.filter(c -> ReimbursementTicketClaim.STATUS_PAID.equals(c.getClaimStatus())
						|| ReimbursementTicketClaim.STATUS_PENDING_FINANCE.equals(c.getClaimStatus()))
				.map(ReimbursementTicketClaim::getAmount).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add));
		List<Map<String, Object>> claimViews = t.getClaims().stream().sorted(Comparator.comparing(ReimbursementTicketClaim::getLineNo))
				.map(c -> {
					Map<String, Object> cm = new LinkedHashMap<>();
					cm.put("claimId", c.getClaimId());
					cm.put("lineNo", c.getLineNo());
					cm.put("expenditureType", c.getExpenditureType());
					cm.put("expenditureTypeDescription", c.getExpenditureTypeDescription());
					cm.put("amount", c.getAmount());
					cm.put("fromDate", c.getFromDate());
					cm.put("toDate", c.getToDate());
					cm.put("dateOfFood", c.getDateOfFood());
					cm.put("purpose", c.getPurpose());
					cm.put("travelMode", c.getTravelMode());
					cm.put("distance", c.getDistance());
					cm.put("vehicleType", c.getVehicleType());
					cm.put("foodAllowanceType", c.getFoodAllowanceType());
					cm.put("projectId", c.getProjectId());
					cm.put("projectName", c.getProjectName());
					cm.put("clientId", c.getClientId());
					String resolvedClientName = c.getClientName();
					if (!StringUtils.hasText(resolvedClientName) && c.getClientId() != null) {
						try {
							resolvedClientName = clientsRepository.findById(c.getClientId())
									.map(Client::getClientName)
									.orElse(null);
						} catch (Exception ignore) {
							// best-effort lookup
						}
					}
					cm.put("clientName", resolvedClientName);
					cm.put("docIds", parseDocIds(c.getDocIds()));
					cm.put("claimStatus", c.getClaimStatus());
					cm.put("hodRemarks", c.getHodRemarks());
					cm.put("hrRemarks", c.getHrRemarks());
					return cm;
				}).collect(Collectors.toList());
		m.put("claims", claimViews);
		return m;
	}

	private List<Map<String, Object>> buildRejectedClaimBreakdown(ReimbursementTicket t) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (t.getClaims() == null) {
			return out;
		}
		String hodName = StringUtils.hasText(t.getHodName()) ? t.getHodName() : "Head of department";
		String hrName = lookupEmployeeNameByEmail(workflowHrMail);
		String finName = lookupEmployeeNameByEmail(workflowFinanceMail);
		String finReason = t.getFinanceRejectReason();
		for (ReimbursementTicketClaim c : t.getClaims().stream()
				.sorted(Comparator.comparing(ReimbursementTicketClaim::getLineNo, Comparator.nullsLast(Comparator.naturalOrder())))
				.collect(Collectors.toList())) {
			String st = c.getClaimStatus();
			if (!StringUtils.hasText(st) || !st.contains("REJECTED")) {
				continue;
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("lineNo", c.getLineNo());
			row.put("expenditureType", c.getExpenditureType());
			row.put("amount", c.getAmount());
			row.put("claimStatus", st);
			if (ReimbursementTicketClaim.STATUS_HOD_REJECTED.equals(st)) {
				row.put("rejectedBy", hodName + " (HOD)");
				row.put("reason", c.getHodRemarks());
			} else if (ReimbursementTicketClaim.STATUS_HR_REJECTED.equals(st)) {
				String who = StringUtils.hasText(hrName) ? hrName : "HR approver";
				row.put("rejectedBy", who + " (HR)");
				row.put("reason", c.getHrRemarks());
			} else if (ReimbursementTicketClaim.STATUS_LEVEL_REJECTED.equals(st)) {
				row.put("rejectedBy", "Approver");
				row.put("reason", c.getHodRemarks());
			} else if (ReimbursementTicketClaim.STATUS_FINANCE_REJECTED.equals(st)) {
				String who = StringUtils.hasText(finName) ? finName : "Finance";
				row.put("rejectedBy", who + " (Finance)");
				row.put("reason", finReason);
			} else {
				row.put("rejectedBy", "—");
				row.put("reason", "");
			}
			out.add(row);
		}
		return out;
	}

	private String buildRejectionSummaryText(List<Map<String, Object>> rejectedLines) {
		if (rejectedLines == null || rejectedLines.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map<String, Object> r : rejectedLines) {
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			Object ln = r.get("lineNo");
			Object et = r.get("expenditureType");
			sb.append("Claim ").append(ln != null ? ln : "?").append(" ").append(et != null ? et : "");
		}
		return sb.toString();
	}

	private List<Long> parseDocIds(String raw) {
		if (!StringUtils.hasText(raw)) {
			return Collections.emptyList();
		}
		return Arrays.stream(raw.split(","))
				.map(s -> s != null ? s.trim() : "")
				.filter(s -> !s.isEmpty())
				.map(s -> {
					try {
						return Long.valueOf(s);
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private Long lookupEmployeementIdByEmpId(BigInteger empIdKey) {
		if (empIdKey == null) {
			return null;
		}
		try {
			Employee emp = employeeRepository.findByEmpId(empIdKey.longValue());
			if (emp != null) {
				return emp.getEmployeementId();
			}
		} catch (Exception ignore) {
			// best-effort lookup
		}
		return null;
	}

	private String lookupEmployeeNameByEmail(String email) {
		final String e = normEmail(email);
		if (!StringUtils.hasText(e)) {
			return "";
		}
		try {
			Employee emp = employeeRepository.findByEmail(e);
			if (emp != null && StringUtils.hasText(emp.getName())) {
				return emp.getName();
			}
		} catch (Exception ignore) {
			// best-effort lookup
		}
		return e;
	}
}
