package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.TravelDeskTicketActorDTO;
import com.apmosys.employeeportal.dto.TravelDeskDashboardFilterDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketLineInputDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketStageActionDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketSubmitRequestDTO;
import com.apmosys.employeeportal.dto.TravelLineAdminFulfillmentDTO;
import com.apmosys.employeeportal.dto.TravelLineDecisionDTO;
import com.apmosys.employeeportal.model.TravelDeskTicket;
import com.apmosys.employeeportal.model.TravelDeskTicketAudit;
import com.apmosys.employeeportal.model.TravelDeskTicketDaySeq;
import com.apmosys.employeeportal.model.TravelDeskTicketLine;
import com.apmosys.employeeportal.repository.TravelDeskTicketAuditRepository;
import com.apmosys.employeeportal.repository.TravelDeskTicketDaySeqRepository;
import com.apmosys.employeeportal.repository.TravelDeskTicketRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelDeskTicketService {

	@Autowired
	private TravelDeskTicketRepository ticketRepository;

	@Autowired
	private TravelDeskTicketDaySeqRepository daySeqRepository;

	@Autowired
	private TravelDeskTicketAuditRepository auditRepository;

	@Autowired
	private TravelDeskTicketMatrixWorkflowService matrixWorkflowService;

	@Autowired
	private TravelDeskNotificationService notificationService;

	@Value("${travel.ticket.id.zone:Asia/Kolkata}")
	private String travelTicketIdZone;

	@Value("${travel.workflow.admin.mail:finance2@apmosys.com}")
	private String workflowAdminMail;

	@Value("${travel.policy.advance-days:7}")
	private int travelPolicyAdvanceDays;

	@Value("${travel.policy.max-future-days:60}")
	private int travelPolicyMaxFutureDays;

	@Transactional
	public ServiceResponse submitTicket(TravelDeskTicketSubmitRequestDTO req) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (req.getLines() == null || req.getLines().isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("At least one travel request line is required.");
				return resp;
			}
			if (req.getManagerEmpId() == null || !StringUtils.hasText(req.getManagerEmail())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Manager details are required.");
				return resp;
			}
			int idx = 1;
			for (TravelDeskTicketLineInputDTO line : req.getLines()) {
				validateLineInput(line, idx++);
			}

			TravelDeskTicket ticket = new TravelDeskTicket();
			ticket.setEmpId(req.getEmpId());
			ticket.setFullName(req.getFullName());
			ticket.setEmail(req.getEmail());
			ticket.setDepartment(req.getDepartmentName());
			ticket.setDesignation(req.getDesignationName());
			ticket.setMobileNo(req.getMobileNo() != null ? req.getMobileNo().toString() : null);
			ticket.setManagerEmpId(req.getManagerEmpId());
			ticket.setManagerName(req.getManagerName());
			ticket.setManagerEmail(req.getManagerEmail().trim());
			ticket.setSubmittedOn(nowTs());
			ticket.setIsActive(1);
			ticket.setTicketNo(allocateNextPublicTicketNo());

			int lineNo = 1;
			for (TravelDeskTicketLineInputDTO in : req.getLines()) {
				TravelDeskTicketLine ln = new TravelDeskTicketLine();
				ln.setTicket(ticket);
				ln.setLineNo(lineNo++);
				ln.setRequestType(trim(in.getRequestType()));
				ln.setTravelMode(trim(in.getTravelMode()));
				ln.setTravelClass(trim(in.getTravelClass()));
				ln.setTripType(trim(in.getTripType()));
				ln.setTravelReason(trim(in.getTravelReason()));
				ln.setPurpose(trim(in.getPurpose()));
				ln.setFromLocation(trim(in.getFromLocation()));
				ln.setToLocation(trim(in.getToLocation()));
				ln.setFromDate(in.getFromDate() != null ? new Timestamp(in.getFromDate().getTime()) : null);
				ln.setToDate(in.getToDate() != null ? new Timestamp(in.getToDate().getTime()) : null);
				ln.setHotelCategory(trim(in.getHotelCategory()));
				ln.setHotelSubCategory(trim(in.getHotelSubCategory()));
				ln.setCity(trim(in.getCity()));
				ln.setProjectId(in.getProjectId());
				ln.setProjectName(trim(in.getProjectName()));
				ln.setClientId(in.getClientId());
				ln.setClientName(trim(in.getClientName()));
				ln.setSupportingDocIds(joinDocIds(in));
				ln.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_APPROVAL);
				ticket.getLines().add(ln);
			}

			matrixWorkflowService.initializeOnSubmit(ticket);

			TravelDeskTicket saved = ticketRepository.save(ticket);
			audit(saved.getTicketId(), null, req.getEmpId(), req.getEmail(), "TICKET_SUBMITTED", null);
			notificationService.notifyEmployeeTicketSubmitted(saved);
			notificationService.notifyCurrentStageActionRequired(saved,
					"A new Travel Desk ticket has been submitted. Please review and take the next action.");

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toViewMap(saved));
			resp.setServiceMessage("Travel ticket submitted successfully.");
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
			List<TravelDeskTicket> list = ticketRepository.findByEmpIdWithLines(empId);
			list.sort(Comparator.comparing(TravelDeskTicket::getSubmittedOn,
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
	public ServiceResponse fetchTicketsForApproval(TravelDeskTicketActorDTO actor) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<TravelDeskTicket> out = new ArrayList<>();
			BigInteger empId = actor != null ? actor.getEmpId() : null;
			String email = normEmail(actor != null ? actor.getEmail() : null);

			for (TravelDeskTicket t : ticketRepository
					.findTicketsWithLinesByStage(TravelDeskTicket.STAGE_PENDING_LEVEL)) {
				if (matrixWorkflowService.canActorApprove(t, empId, actor != null ? actor.getEmail() : null)) {
					out.add(t);
				}
			}
			if (email.equals(normEmail(workflowAdminMail))) {
				for (TravelDeskTicket t : ticketRepository
						.findTicketsWithLinesByStage(TravelDeskTicket.STAGE_PENDING_ADMIN)) {
					out.add(t);
				}
			} else if (empId != null) {
				for (TravelDeskTicket t : ticketRepository
						.findTicketsWithLinesByStage(TravelDeskTicket.STAGE_PENDING_ADMIN)) {
					if (t.getCurrentAssigneeEmpId() != null
							&& empId.longValue() == t.getCurrentAssigneeEmpId().longValue()) {
						out.add(t);
					}
				}
			}

			Map<Long, TravelDeskTicket> map = new LinkedHashMap<>();
			for (TravelDeskTicket t : out) {
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

	@Transactional(readOnly = true)
	public ServiceResponse fetchAllTicketsAssignedToActor(TravelDeskTicketActorDTO actor) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<TravelDeskTicket> out = new ArrayList<>();
			BigInteger empId = actor != null ? actor.getEmpId() : null;
			String email = normEmail(actor != null ? actor.getEmail() : null);

			if (email.equals(normEmail(workflowAdminMail))) {
				out.addAll(ticketRepository.findAllActiveWithLines());
			}
			for (TravelDeskTicket t : ticketRepository.findAllActiveWithLines()) {
				if (matrixWorkflowService.isActorInApprovalChain(t, empId, actor != null ? actor.getEmail() : null)) {
					out.add(t);
				}
			}
			Map<Long, TravelDeskTicket> map = new LinkedHashMap<>();
			for (TravelDeskTicket t : out) {
				map.putIfAbsent(t.getTicketId(), t);
			}
			List<TravelDeskTicket> merged = new ArrayList<>(map.values());
			merged.sort(Comparator.comparing(TravelDeskTicket::getSubmittedOn,
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

	@Transactional(readOnly = true)
	public ServiceResponse dashboard(TravelDeskDashboardFilterDTO filter) {
		ServiceResponse resp = new ServiceResponse();
		try {
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

			List<TravelDeskTicket> allActive = ticketRepository.findAllActiveWithLines();
			boolean fullScope = filter != null && Boolean.TRUE.equals(filter.getDashboardFullScope());
			List<TravelDeskTicket> actorScoped = fullScope ? allActive
					: allActive.stream().filter(t -> ticketVisibleToDashboardActor(t, filter)).collect(Collectors.toList());
			List<TravelDeskTicket> tickets = actorScoped.stream()
					.filter(t -> ticketMatchesDashboardFilters(t, filter, fromTs, toTs)).collect(Collectors.toList());
			List<TravelDeskTicketLine> lines = tickets.stream().flatMap(t -> linesForDashboardMetrics(t, filter))
					.collect(Collectors.toList());

			long bookedRequestCount = lines.stream().filter(this::isBookedDashboardLine).count();
			long rejectedRequestCount = lines.stream().filter(this::isRejectedDashboardLine).count();
			BigDecimal totalSpend = lines.stream().map(this::lineBookingAmountOrZero).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal avgSpend = bookedRequestCount == 0 ? BigDecimal.ZERO
					: totalSpend.divide(BigDecimal.valueOf(bookedRequestCount), 2, RoundingMode.HALF_UP);

			Map<String, Long> ticketStatusBreakdown = new LinkedHashMap<>();
			for (TravelDeskTicket t : tickets) {
				ticketStatusBreakdown.merge(displayTicketStatus(t), 1L, Long::sum);
			}
			Map<String, Long> stageCountRaw = tickets.stream()
					.collect(Collectors.groupingBy(
							t -> t.getWorkflowStage() != null ? t.getWorkflowStage() : "UNKNOWN",
							Collectors.counting()));
			LinkedHashMap<String, Long> workflowStageBreakdown = new LinkedHashMap<>();
			for (String stg : Arrays.asList(TravelDeskTicket.STAGE_PENDING_LEVEL, TravelDeskTicket.STAGE_PENDING_ADMIN,
					TravelDeskTicket.STAGE_COMPLETED, TravelDeskTicket.STAGE_REJECTED)) {
				workflowStageBreakdown.put(stg, stageCountRaw.getOrDefault(stg, 0L));
			}
			stageCountRaw.entrySet().stream().filter(e -> !workflowStageBreakdown.containsKey(e.getKey()))
					.sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
					.forEach(e -> workflowStageBreakdown.put(e.getKey(), e.getValue()));

			Map<String, Map<String, Object>> projectBoard = new LinkedHashMap<>();
			Map<String, Map<String, Object>> clientBoard = new LinkedHashMap<>();
			Map<String, Map<String, Object>> employeeBoard = new LinkedHashMap<>();
			TreeMap<String, BigDecimal> monthlySpend = new TreeMap<>();
			Set<String> projectsWithSpend = new HashSet<>();
			Set<String> clientsWithSpend = new HashSet<>();
			for (TravelDeskTicket t : tickets) {
				List<TravelDeskTicketLine> scopedLines = linesForDashboardMetrics(t, filter).collect(Collectors.toList());
				for (TravelDeskTicketLine ln : scopedLines) {
					String projectName = dashboardProjectLabel(ln);
					String clientName = dashboardClientLabel(ln);
					String projectKey = ln.getProjectId() != null ? "P:" + ln.getProjectId() : "PN:" + projectName + "|" + clientName;
					String clientKey = ln.getClientId() != null ? "C:" + ln.getClientId() : "CN:" + clientName;

					Map<String, Object> projectRow = projectBoard.computeIfAbsent(projectKey, k -> {
						Map<String, Object> m = new LinkedHashMap<>();
						m.put("projectId", ln.getProjectId());
						m.put("projectName", projectName);
						m.put("clientName", clientName);
						m.put("label", projectName);
						m.put("requestCount", 0L);
						m.put("bookedRequestCount", 0L);
						m.put("pendingRequestCount", 0L);
						m.put("rejectedRequestCount", 0L);
						m.put("requested", BigDecimal.ZERO);
						return m;
					});
					projectRow.put("requestCount", ((Long) projectRow.get("requestCount")) + 1L);
					if (isBookedDashboardLine(ln)) {
						projectRow.put("bookedRequestCount", ((Long) projectRow.get("bookedRequestCount")) + 1L);
						projectRow.put("requested",
								((BigDecimal) projectRow.get("requested")).add(lineBookingAmountOrZero(ln)));
					}
					if (isPendingDashboardLine(ln)) {
						projectRow.put("pendingRequestCount", ((Long) projectRow.get("pendingRequestCount")) + 1L);
					}
					if (isRejectedDashboardLine(ln)) {
						projectRow.put("rejectedRequestCount", ((Long) projectRow.get("rejectedRequestCount")) + 1L);
					}

					Map<String, Object> clientRow = clientBoard.computeIfAbsent(clientKey, k -> {
						Map<String, Object> m = new LinkedHashMap<>();
						m.put("clientId", ln.getClientId());
						m.put("clientName", clientName);
						m.put("label", clientName);
						m.put("requestCount", 0L);
						m.put("bookedRequestCount", 0L);
						m.put("pendingRequestCount", 0L);
						m.put("rejectedRequestCount", 0L);
						m.put("requested", BigDecimal.ZERO);
						return m;
					});
					clientRow.put("requestCount", ((Long) clientRow.get("requestCount")) + 1L);
					if (isBookedDashboardLine(ln)) {
						clientRow.put("bookedRequestCount", ((Long) clientRow.get("bookedRequestCount")) + 1L);
						clientRow.put("requested",
								((BigDecimal) clientRow.get("requested")).add(lineBookingAmountOrZero(ln)));
					}
					if (isPendingDashboardLine(ln)) {
						clientRow.put("pendingRequestCount", ((Long) clientRow.get("pendingRequestCount")) + 1L);
					}
					if (isRejectedDashboardLine(ln)) {
						clientRow.put("rejectedRequestCount", ((Long) clientRow.get("rejectedRequestCount")) + 1L);
					}

					if (isBookedDashboardLine(ln)) {
						String empKey = t.getEmpId() != null ? String.valueOf(t.getEmpId()) : String.valueOf(t.getTicketId());
						Map<String, Object> empRow = employeeBoard.computeIfAbsent(empKey, k -> {
							Map<String, Object> m = new LinkedHashMap<>();
							m.put("empId", t.getEmpId());
							m.put("fullName", StringUtils.hasText(t.getFullName()) ? t.getFullName().trim() : "Employee");
							m.put("department", StringUtils.hasText(t.getDepartment()) ? t.getDepartment().trim() : "—");
							m.put("requested", BigDecimal.ZERO);
							m.put("bookedRequestCount", 0L);
							return m;
						});
						empRow.put("requested", ((BigDecimal) empRow.get("requested")).add(lineBookingAmountOrZero(ln)));
						empRow.put("bookedRequestCount", ((Long) empRow.get("bookedRequestCount")) + 1L);

						String projectSpendKey = StringUtils.hasText(projectName) ? projectName : projectKey;
						String clientSpendKey = StringUtils.hasText(clientName) ? clientName : clientKey;
						projectsWithSpend.add(projectSpendKey);
						clientsWithSpend.add(clientSpendKey);
						String monthKey = travelMonthKey(
								ln.getFulfilledOn() != null ? ln.getFulfilledOn()
										: (t.getCompletedOn() != null ? t.getCompletedOn() : t.getSubmittedOn()));
						if (monthKey != null) {
							monthlySpend.merge(monthKey, lineBookingAmountOrZero(ln), BigDecimal::add);
						}
					}
				}
			}

			List<Map<String, Object>> projectRows = projectBoard.values().stream().map(this::finalizeTravelSpendRow)
					.sorted(this::compareTravelSpendRows).collect(Collectors.toList());
			List<Map<String, Object>> clientRows = clientBoard.values().stream().map(this::finalizeTravelSpendRow)
					.sorted(this::compareTravelSpendRows).collect(Collectors.toList());
			List<Map<String, Object>> employeeRows = employeeBoard.values().stream().map(m -> {
				Map<String, Object> out = new LinkedHashMap<>(m);
				out.put("totalSpend", out.get("requested"));
				return out;
			}).sorted((a, b) -> ((BigDecimal) b.get("requested")).compareTo((BigDecimal) a.get("requested")))
					.collect(Collectors.toList());

			List<String> monthCategories = new ArrayList<>();
			List<Double> monthSpend = new ArrayList<>();
			monthlySpend.forEach((ym, amt) -> {
				monthCategories.add(formatTravelMonthLabel(ym));
				monthSpend.add(bigDecimalToDouble(amt));
			});
			Map<String, Object> monthlySpendPack = new LinkedHashMap<>();
			monthlySpendPack.put("categories", monthCategories);
			monthlySpendPack.put("spend", monthSpend);

			Map<String, Object> dash = new LinkedHashMap<>();
			dash.put("ticketCount", tickets.size());
			dash.put("requestCount", lines.size());
			dash.put("bookedRequestCount", bookedRequestCount);
			dash.put("rejectedRequestCount", rejectedRequestCount);
			dash.put("completedTicketCount",
					tickets.stream().filter(t -> TravelDeskTicket.STAGE_COMPLETED.equals(t.getWorkflowStage())).count());
			dash.put("pendingAdminTickets",
					tickets.stream().filter(t -> TravelDeskTicket.STAGE_PENDING_ADMIN.equals(t.getWorkflowStage())).count());
			dash.put("pendingApprovalTickets",
					tickets.stream().filter(t -> TravelDeskTicket.STAGE_PENDING_LEVEL.equals(t.getWorkflowStage())).count());
			dash.put("totalSpend", totalSpend);
			dash.put("averageSpendPerBookedRequest", avgSpend);
			dash.put("projectsWithSpend", projectsWithSpend.size());
			dash.put("clientsWithSpend", clientsWithSpend.size());
			dash.put("ticketStatusBreakdown", ticketStatusBreakdown);
			dash.put("workflowStageBreakdown", workflowStageBreakdown);
			dash.put("projectSpendRows", projectRows);
			dash.put("clientSpendRows", clientRows);
			dash.put("employeeSpendRows", employeeRows);
			dash.put("monthlySpendPack", monthlySpendPack);

			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(dash);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse processApprovalAction(TravelDeskTicketStageActionDTO action) {
		ServiceResponse resp = new ServiceResponse();
		try {
			Optional<TravelDeskTicket> opt = ticketRepository.findByIdWithLines(action.getTicketId());
			if (opt.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Ticket not found.");
				return resp;
			}
			TravelDeskTicket ticket = opt.get();
			if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
				return processMatrixLevelDecisions(action, ticket, resp);
			}
			if (TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
				return processTravelAdminAction(action, ticket, resp);
			}
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("Ticket is not awaiting your approval action.");
		} catch (Exception e) {
			e.printStackTrace();
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	private ServiceResponse processMatrixLevelDecisions(TravelDeskTicketStageActionDTO action, TravelDeskTicket ticket,
			ServiceResponse resp) {
		if (!matrixWorkflowService.canActorApprove(ticket, action.getActorEmpId(), action.getActorEmail())) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("You are not authorized to act on this approval level.");
			return resp;
		}
		List<TravelDeskTicketLine> pending = ticket.getLines().stream()
				.filter(l -> TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(l.getLineStatus()))
				.collect(Collectors.toList());
		if (pending.isEmpty()) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("No pending request lines to process.");
			return resp;
		}
		if (action.getDecisions() == null || action.getDecisions().size() != pending.size()) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("Provide exactly one decision per pending line (" + pending.size() + ").");
			return resp;
		}
		Map<Long, TravelDeskTicketLine> byId = pending.stream()
				.collect(Collectors.toMap(TravelDeskTicketLine::getLineId, x -> x));
		for (TravelLineDecisionDTO d : action.getDecisions()) {
			if (d.getLineId() == null || !byId.containsKey(d.getLineId())) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Invalid line id in decisions.");
				return resp;
			}
			if (d.getApproved() == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Each decision must set approved true/false.");
				return resp;
			}
			if (!StringUtils.hasText(d.getRemarks()) || d.getRemarks().trim().isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Remarks are mandatory for each line decision.");
				return resp;
			}
		}
		for (TravelLineDecisionDTO d : action.getDecisions()) {
			TravelDeskTicketLine ln = byId.get(d.getLineId());
			if (Boolean.TRUE.equals(d.getApproved())) {
				ln.setLineStatus(TravelDeskTicketLine.STATUS_PENDING_APPROVAL);
			} else {
				ln.setLineStatus(TravelDeskTicketLine.STATUS_LEVEL_REJECTED);
			}
			ln.setApproverRemarks(d.getRemarks().trim());
			audit(ticket.getTicketId(), ln.getLineId(), action.getActorEmpId(), action.getActorEmail(),
					"MATRIX_LEVEL_DECISION", d.getRemarks());
		}
		boolean anyStillPending = ticket.getLines().stream()
				.anyMatch(l -> TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(l.getLineStatus()));
		if (!anyStillPending) {
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
		} else {
			matrixWorkflowService.advanceAfterLevelDecisions(ticket);
		}
		if (allLinesRejected(ticket)) {
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
		}
		ticketRepository.save(ticket);
		String approverIntro;
		String submitterIntro = null;
		String terminalExtra = null;
		if (TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
			approverIntro = "All approval levels are complete. This ticket is now pending Travel Admin booking.";
		} else if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
			approverIntro = "The previous approval step is complete. This ticket is now pending your approval.";
		} else if (TravelDeskTicket.STAGE_REJECTED.equals(ticket.getWorkflowStage())) {
			approverIntro = null;
			submitterIntro = "Your Travel Desk ticket has been rejected. Please review the latest remarks in iShine.";
		} else {
			approverIntro = "This Travel Desk ticket has been updated.";
		}
		notificationService.notifyAfterWorkflowTransition(ticket, approverIntro, submitterIntro, terminalExtra);
		resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		resp.setServiceResponse(toViewMap(ticket));
		resp.setServiceMessage("Processed successfully.");
		return resp;
	}

	private ServiceResponse processTravelAdminAction(TravelDeskTicketStageActionDTO action, TravelDeskTicket ticket,
			ServiceResponse resp) {
		if (!matrixWorkflowService.canActorFulfillAsTravelAdmin(ticket, action.getActorEmpId(), action.getActorEmail(),
				workflowAdminMail)) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("You are not authorized to complete Travel Admin booking for this ticket.");
			return resp;
		}
		String act = action.getAction() == null ? "" : action.getAction().trim().toUpperCase(Locale.ROOT);
		if (!"BOOKED".equals(act) && !"REJECTED".equals(act)) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("Action must be BOOKED or REJECTED.");
			return resp;
		}
		if ("REJECTED".equals(act) && (!StringUtils.hasText(action.getRemarks())
				|| action.getRemarks().trim().isEmpty())) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("Rejection reason is mandatory when rejecting a ticket.");
			return resp;
		}
		String adminRemarks = StringUtils.hasText(action.getRemarks()) ? action.getRemarks().trim() : "";
		List<TravelDeskTicketLine> adminPending = ticket.getLines().stream()
				.filter(l -> TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(l.getLineStatus()))
				.collect(Collectors.toList());
		if (adminPending.isEmpty()) {
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("No requests are pending Travel Admin booking.");
			return resp;
		}
		if ("REJECTED".equals(act)) {
			for (TravelDeskTicketLine ln : adminPending) {
				ln.setLineStatus(TravelDeskTicketLine.STATUS_ADMIN_REJECTED);
				ln.setApproverRemarks(adminRemarks);
			}
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
			ticket.setAdminRemarks(adminRemarks);
			ticket.setAdminActorEmpId(action.getActorEmpId() != null ? action.getActorEmpId().longValue() : null);
			audit(ticket.getTicketId(), null, action.getActorEmpId(), action.getActorEmail(), "ADMIN_REJECTED",
					adminRemarks);
		} else {
			if (action.getLineFulfillments() == null
					|| action.getLineFulfillments().size() != adminPending.size()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError(
						"Provide booking reference and proof for each pending line (" + adminPending.size() + ").");
				return resp;
			}
			Map<Long, TravelDeskTicketLine> byId = adminPending.stream()
					.collect(Collectors.toMap(TravelDeskTicketLine::getLineId, x -> x));
			for (TravelLineAdminFulfillmentDTO f : action.getLineFulfillments()) {
				if (f.getLineId() == null || !byId.containsKey(f.getLineId())) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Invalid line id in booking details.");
					return resp;
				}
				if (f.getAdminProofDocIds() == null || f.getAdminProofDocIds().isEmpty()) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("At least one booking proof document is required for each line.");
					return resp;
				}
				if (f.getBookingAmount() == null || f.getBookingAmount().signum() <= 0) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Enter a valid booking amount for each line.");
					return resp;
				}
			}
			Timestamp fulfilledOn = nowTs();
			for (TravelLineAdminFulfillmentDTO f : action.getLineFulfillments()) {
				TravelDeskTicketLine ln = byId.get(f.getLineId());
				if (StringUtils.hasText(f.getBookingReference())) {
					ln.setBookingReference(f.getBookingReference().trim());
				}
				ln.setBookingAmount(f.getBookingAmount());
				ln.setAdminProofDocIds(joinDocIdList(f.getAdminProofDocIds()));
				ln.setLineStatus(TravelDeskTicketLine.STATUS_FULFILLED);
				ln.setFulfilledOn(fulfilledOn);
				ln.setApproverRemarks(adminRemarks.isEmpty() ? null : adminRemarks);
			}
			boolean anyStillPending = ticket.getLines().stream()
					.anyMatch(l -> TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(l.getLineStatus()));
			if (!anyStillPending) {
				ticket.setWorkflowStage(TravelDeskTicket.STAGE_COMPLETED);
				ticket.setCurrentAssigneeEmpId(null);
				ticket.setCompletedOn(fulfilledOn);
			}
			ticket.setAdminRemarks(adminRemarks.isEmpty() ? null : adminRemarks);
			ticket.setAdminActorEmpId(action.getActorEmpId() != null ? action.getActorEmpId().longValue() : null);
			audit(ticket.getTicketId(), null, action.getActorEmpId(), action.getActorEmail(), "ADMIN_BOOKED",
					adminRemarks.isEmpty() ? "Marked as booked" : adminRemarks);
		}
		if (allLinesRejected(ticket)) {
			ticket.setWorkflowStage(TravelDeskTicket.STAGE_REJECTED);
			ticket.setCurrentAssigneeEmpId(null);
		}
		ticketRepository.save(ticket);
		if ("BOOKED".equals(act)) {
			notificationService.notifySubmitterStatus(ticket,
					"Your Travel Desk ticket has been booked by Travel Admin.",
					notificationService.bookingDetailsHtml(ticket, adminRemarks));
		} else {
			notificationService.notifySubmitterStatus(ticket,
					"Your Travel Desk ticket has been rejected by Travel Admin.",
					notificationService.bookingDetailsHtml(ticket, adminRemarks));
		}
		resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		resp.setServiceResponse(toViewMap(ticket));
		resp.setServiceMessage("Travel Admin action recorded.");
		return resp;
	}

	private boolean allLinesRejected(TravelDeskTicket ticket) {
		List<TravelDeskTicketLine> lines = ticket.getLines();
		if (lines == null || lines.isEmpty()) {
			return false;
		}
		return lines.stream().allMatch(l -> TravelDeskTicketLine.STATUS_LEVEL_REJECTED.equals(l.getLineStatus())
				|| TravelDeskTicketLine.STATUS_ADMIN_REJECTED.equals(l.getLineStatus()));
	}

	private String displayTicketStatus(TravelDeskTicket t) {
		if (t == null || t.getWorkflowStage() == null) {
			return "—";
		}
		switch (t.getWorkflowStage()) {
		case TravelDeskTicket.STAGE_PENDING_LEVEL:
			return "Pending approval";
		case TravelDeskTicket.STAGE_PENDING_ADMIN:
			return "Pending travel admin";
		case TravelDeskTicket.STAGE_COMPLETED:
			return "Booked";
		case TravelDeskTicket.STAGE_REJECTED:
			return "Rejected";
		default:
			return t.getWorkflowStage();
		}
	}

	private boolean ticketVisibleToDashboardActor(TravelDeskTicket t, TravelDeskDashboardFilterDTO filter) {
		if (t == null || filter == null) {
			return true;
		}
		BigInteger actorEmpId = filter.getActorEmpId() != null ? BigInteger.valueOf(filter.getActorEmpId()) : null;
		String actorEmail = filter.getActorEmail();
		String email = normEmail(actorEmail);
		if (!StringUtils.hasText(email) && actorEmpId == null) {
			return true;
		}
		if (email.equals(normEmail(workflowAdminMail))) {
			return true;
		}
		if (actorEmpId != null && t.getEmpId() != null && actorEmpId.longValue() == t.getEmpId().longValue()) {
			return true;
		}
		if (matrixWorkflowService.isActorInApprovalChain(t, actorEmpId, actorEmail)) {
			return true;
		}
		return actorEmpId != null && t.getCurrentAssigneeEmpId() != null
				&& actorEmpId.longValue() == t.getCurrentAssigneeEmpId().longValue();
	}

	private boolean ticketMatchesDashboardFilters(TravelDeskTicket t, TravelDeskDashboardFilterDTO filter, Timestamp fromTs,
			Timestamp toTs) {
		if (t == null) {
			return false;
		}
		if (fromTs != null && (t.getSubmittedOn() == null || t.getSubmittedOn().before(fromTs))) {
			return false;
		}
		if (toTs != null && (t.getSubmittedOn() == null || t.getSubmittedOn().after(toTs))) {
			return false;
		}
		if (filter == null) {
			return true;
		}
		if (StringUtils.hasText(filter.getDepartment())) {
			String dept = trim(t.getDepartment());
			if (!StringUtils.hasText(dept) || !dept.equalsIgnoreCase(filter.getDepartment().trim())) {
				return false;
			}
		}
		if (filter.getEmployeeEmpId() != null) {
			if (t.getEmpId() == null || filter.getEmployeeEmpId().longValue() != t.getEmpId().longValue()) {
				return false;
			}
		}
		if (StringUtils.hasText(filter.getTicketStatus())) {
			String ds = displayTicketStatus(t);
			if (!filter.getTicketStatus().trim().equalsIgnoreCase(ds)) {
				return false;
			}
		}
		if (StringUtils.hasText(filter.getWorkflowStage())) {
			String stage = trim(t.getWorkflowStage());
			if (!StringUtils.hasText(stage) || !stage.equalsIgnoreCase(filter.getWorkflowStage().trim())) {
				return false;
			}
		}
		if ((filter.getProjectId() != null || filter.getClientId() != null)
				&& !linesForDashboardMetrics(t, filter).findAny().isPresent()) {
			return false;
		}
		return true;
	}

	private Stream<TravelDeskTicketLine> linesForDashboardMetrics(TravelDeskTicket t, TravelDeskDashboardFilterDTO filter) {
		Stream<TravelDeskTicketLine> stream = (t != null && t.getLines() != null ? t.getLines() : Collections.<TravelDeskTicketLine>emptyList())
				.stream();
		if (filter == null) {
			return stream;
		}
		if (filter.getProjectId() != null) {
			long pid = filter.getProjectId().longValue();
			stream = stream.filter(ln -> ln.getProjectId() != null && ln.getProjectId().longValue() == pid);
		}
		if (filter.getClientId() != null) {
			int cid = filter.getClientId().intValue();
			stream = stream.filter(ln -> ln.getClientId() != null && ln.getClientId().intValue() == cid);
		}
		return stream;
	}

	private boolean isBookedDashboardLine(TravelDeskTicketLine ln) {
		return ln != null && (TravelDeskTicketLine.STATUS_FULFILLED.equals(ln.getLineStatus())
				|| lineBookingAmountOrZero(ln).compareTo(BigDecimal.ZERO) > 0);
	}

	private boolean isPendingDashboardLine(TravelDeskTicketLine ln) {
		if (ln == null || !StringUtils.hasText(ln.getLineStatus())) {
			return false;
		}
		return TravelDeskTicketLine.STATUS_PENDING_APPROVAL.equals(ln.getLineStatus())
				|| TravelDeskTicketLine.STATUS_PENDING_ADMIN.equals(ln.getLineStatus());
	}

	private boolean isRejectedDashboardLine(TravelDeskTicketLine ln) {
		return ln != null && StringUtils.hasText(ln.getLineStatus()) && ln.getLineStatus().contains("REJECTED");
	}

	private BigDecimal lineBookingAmountOrZero(TravelDeskTicketLine ln) {
		return ln != null && ln.getBookingAmount() != null ? ln.getBookingAmount() : BigDecimal.ZERO;
	}

	private String dashboardProjectLabel(TravelDeskTicketLine ln) {
		String projectName = trim(ln != null ? ln.getProjectName() : null);
		if (StringUtils.hasText(projectName)) {
			return projectName;
		}
		return ln != null && ln.getProjectId() != null ? ("Project " + ln.getProjectId()) : "Unmapped project";
	}

	private String dashboardClientLabel(TravelDeskTicketLine ln) {
		String clientName = trim(ln != null ? ln.getClientName() : null);
		if (StringUtils.hasText(clientName)) {
			return clientName;
		}
		return ln != null && ln.getClientId() != null ? ("Client " + ln.getClientId()) : "Unmapped client";
	}

	private Map<String, Object> finalizeTravelSpendRow(Map<String, Object> row) {
		Map<String, Object> out = new LinkedHashMap<>(row);
		BigDecimal spend = (BigDecimal) out.get("requested");
		long booked = (Long) out.get("bookedRequestCount");
		out.put("totalSpend", spend);
		out.put("averageSpend",
				booked == 0 ? BigDecimal.ZERO : spend.divide(BigDecimal.valueOf(booked), 2, RoundingMode.HALF_UP));
		return out;
	}

	private int compareTravelSpendRows(Map<String, Object> a, Map<String, Object> b) {
		BigDecimal aSpend = (BigDecimal) a.get("requested");
		BigDecimal bSpend = (BigDecimal) b.get("requested");
		int bySpend = bSpend.compareTo(aSpend);
		if (bySpend != 0) {
			return bySpend;
		}
		return Long.compare((Long) b.get("requestCount"), (Long) a.get("requestCount"));
	}

	private String travelMonthKey(Timestamp ts) {
		if (ts == null) {
			return null;
		}
		LocalDate d = ts.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
		return d.format(DateTimeFormatter.ofPattern("yyyy-MM"));
	}

	private String formatTravelMonthLabel(String yearMonth) {
		if (!StringUtils.hasText(yearMonth)) {
			return "—";
		}
		try {
			return LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
		} catch (Exception e) {
			return yearMonth;
		}
	}

	private double bigDecimalToDouble(BigDecimal value) {
		return value == null ? 0d : value.doubleValue();
	}

	private void syncLegacyLevelFields(TravelDeskTicket t, Map<String, Object> m,
			List<Map<String, Object>> approvalLevels) {
		if (approvalLevels == null) {
			return;
		}
		m.put("level", t.getCurrentLevelOrder());
		m.put("currentApprovalLevel", matrixWorkflowService.currentLevelLabel(t));
		for (int i = 0; i < approvalLevels.size(); i++) {
			Map<String, Object> row = approvalLevels.get(i);
			int idx = i + 1;
			m.put("level" + idx + "ApproverName", row.get("approverName"));
			m.put("level" + idx + "ApproverStatus", row.get("approverStatus"));
		}
		if (!approvalLevels.isEmpty()) {
			m.put("hodName", approvalLevels.get(0).get("approverName"));
			m.put("status", approvalLevels.get(0).get("approverStatus"));
			m.put("level1approverRemarks", "");
		}
		if (approvalLevels.size() > 1) {
			m.put("level2approverName", approvalLevels.get(1).get("approverName"));
			m.put("level2approverStatus", approvalLevels.get(1).get("approverStatus"));
			m.put("level2approverRemarks", "");
		}
		if (approvalLevels.size() > 2) {
			m.put("level3approverName", approvalLevels.get(2).get("approverName"));
			m.put("level3approverStatus", approvalLevels.get(2).get("approverStatus"));
			m.put("level3approverRemarks", "");
		}
		m.put("finalStatus", m.get("displayStatus"));
	}

	private Map<String, Object> toViewMap(TravelDeskTicket t) {
		Map<String, Object> m = new LinkedHashMap<>();
		List<Map<String, Object>> approvalLevels = matrixWorkflowService.buildApprovalLevels(t);
		m.put("ticketId", t.getTicketId());
		m.put("ticketNo", t.getTicketNo());
		m.put("recordType", "TICKET");
		m.put("displayStatus", displayTicketStatus(t));
		m.put("workflowStage", t.getWorkflowStage());
		m.put("approvalMatrixId", t.getApprovalMatrixId());
		m.put("currentLevelOrder", t.getCurrentLevelOrder());
		m.put("currentAssigneeEmpId", t.getCurrentAssigneeEmpId());
		m.put("currentLevelLabel", matrixWorkflowService.currentLevelLabel(t));
		m.put("approvalLevels", approvalLevels);
		syncLegacyLevelFields(t, m, approvalLevels);
		m.put("empId", t.getEmpId());
		m.put("fullName", t.getFullName());
		m.put("name", t.getFullName());
		m.put("email", t.getEmail());
		m.put("department", t.getDepartment());
		m.put("designation", t.getDesignation());
		m.put("mobileNo", t.getMobileNo());
		m.put("managerName", t.getManagerName());
		m.put("hodName", t.getManagerName());
		m.put("submittedOn", t.getSubmittedOn());
		m.put("appliedOn", t.getSubmittedOn());
		m.put("lineCount", t.getLines() != null ? t.getLines().size() : 0);
		if (t.getLines() != null) {
			m.put("lines", t.getLines().stream().map(this::lineView).collect(Collectors.toList()));
		}
		return m;
	}

	private Map<String, Object> lineView(TravelDeskTicketLine ln) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("lineId", ln.getLineId());
		m.put("lineNo", ln.getLineNo());
		m.put("requestType", ln.getRequestType());
		m.put("travelMode", ln.getTravelMode());
		m.put("travelClass", ln.getTravelClass());
		m.put("tripType", ln.getTripType());
		m.put("purpose", ln.getPurpose());
		m.put("fromLocation", ln.getFromLocation());
		m.put("toLocation", ln.getToLocation());
		m.put("fromDate", ln.getFromDate());
		m.put("toDate", ln.getToDate());
		m.put("hotelCategory", ln.getHotelCategory());
		m.put("hotelSubCategory", ln.getHotelSubCategory());
		m.put("cityCategory", ln.getHotelSubCategory());
		m.put("city", ln.getCity());
		m.put("lineStatus", ln.getLineStatus());
		m.put("projectName", ln.getProjectName());
		m.put("clientName", ln.getClientName());
		m.put("approverRemarks", ln.getApproverRemarks());
		m.put("bookingReference", ln.getBookingReference());
		m.put("bookingAmount", ln.getBookingAmount());
		m.put("adminProofDocIds", parseDocIds(ln.getAdminProofDocIds()));
		m.put("fulfilledOn", ln.getFulfilledOn());
		m.put("lineStatusDisplay", displayLineStatus(ln.getLineStatus()));
		m.put("docIds", parseDocIds(ln.getSupportingDocIds()));
		return m;
	}

	private String displayLineStatus(String status) {
		if (!StringUtils.hasText(status)) {
			return "—";
		}
		switch (status) {
		case TravelDeskTicketLine.STATUS_PENDING_APPROVAL:
			return "Pending approval";
		case TravelDeskTicketLine.STATUS_PENDING_ADMIN:
			return "Pending travel admin";
		case TravelDeskTicketLine.STATUS_FULFILLED:
			return "Booked";
		case TravelDeskTicketLine.STATUS_LEVEL_REJECTED:
		case TravelDeskTicketLine.STATUS_ADMIN_REJECTED:
			return "Rejected";
		default:
			return status;
		}
	}

	private String joinDocIdList(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return null;
		}
		return ids.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
	}

	private List<Long> parseDocIds(String raw) {
		if (!StringUtils.hasText(raw)) {
			return Collections.emptyList();
		}
		return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(s -> {
			try {
				return Long.valueOf(s);
			} catch (Exception e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private String normEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private void validateLineInput(TravelDeskTicketLineInputDTO line, int index) {
		if (line == null) {
			throw new IllegalArgumentException("Line " + index + " is empty.");
		}
		if (!StringUtils.hasText(line.getRequestType())) {
			throw new IllegalArgumentException("Line " + index + ": request type is required.");
		}
		if (!StringUtils.hasText(line.getPurpose())) {
			throw new IllegalArgumentException("Line " + index + ": purpose is required.");
		}
		if (line.getProjectId() == null) {
			throw new IllegalArgumentException("Line " + index + ": project is required.");
		}
		validateTravelDates(line, index);
		if (isHotelRequestLine(line)) {
			if (!StringUtils.hasText(line.getHotelCategory())) {
				throw new IllegalArgumentException("Line " + index + ": hotel category is required.");
			}
			if (!StringUtils.hasText(line.getHotelSubCategory())) {
				throw new IllegalArgumentException("Line " + index + ": hotel sub-category is required.");
			}
			if (!StringUtils.hasText(line.getCity())) {
				throw new IllegalArgumentException("Line " + index + ": city is required.");
			}
			if (line.getToDate() == null) {
				throw new IllegalArgumentException("Line " + index + ": check-out date is required.");
			}
		} else {
			if (!StringUtils.hasText(line.getTravelMode())) {
				throw new IllegalArgumentException("Line " + index + ": travel mode is required.");
			}
			if (!StringUtils.hasText(line.getTravelClass())) {
				throw new IllegalArgumentException("Line " + index + ": travel class is required.");
			}
			if (!StringUtils.hasText(line.getFromLocation())) {
				throw new IllegalArgumentException("Line " + index + ": from location is required.");
			}
			if (!StringUtils.hasText(line.getToLocation())) {
				throw new IllegalArgumentException("Line " + index + ": to location is required.");
			}
		}
	}

	private void validateTravelDates(TravelDeskTicketLineInputDTO line, int index) {
		if (line.getFromDate() == null) {
			throw new IllegalArgumentException("Line " + index + ": start date is required.");
		}
		ZoneId zone = ZoneId.of(StringUtils.hasText(travelTicketIdZone) ? travelTicketIdZone.trim() : "Asia/Kolkata");
		LocalDate from = line.getFromDate().toInstant().atZone(zone).toLocalDate();
		LocalDate today = LocalDate.now(zone);
		LocalDate earliest = today.plusDays(travelPolicyAdvanceDays);
		LocalDate latest = today.plusDays(travelPolicyMaxFutureDays);
		if (from.isBefore(earliest)) {
			throw new IllegalArgumentException("Line " + index + ": requests must be planned at least "
					+ travelPolicyAdvanceDays + " days in advance (ApMoSys Travel Policy).");
		}
		if (from.isAfter(latest)) {
			throw new IllegalArgumentException("Line " + index + ": start date cannot be more than "
					+ travelPolicyMaxFutureDays + " days from today.");
		}
		if (line.getToDate() != null) {
			LocalDate to = line.getToDate().toInstant().atZone(zone).toLocalDate();
			if (to.isBefore(from)) {
				throw new IllegalArgumentException("Line " + index + ": end date cannot be before start date.");
			}
			if (to.isAfter(latest)) {
				throw new IllegalArgumentException("Line " + index + ": end date cannot be more than "
						+ travelPolicyMaxFutureDays + " days from today.");
			}
		}
	}

	private boolean isHotelRequestLine(TravelDeskTicketLineInputDTO line) {
		String type = trim(line.getRequestType());
		if (!StringUtils.hasText(type)) {
			return false;
		}
		String n = type.toLowerCase(Locale.ROOT);
		return n.contains("hotel") && (n.contains("lodg") || n.contains("accommod") || n.contains("stay"));
	}

	private String joinDocIds(TravelDeskTicketLineInputDTO in) {
		List<String> parts = new ArrayList<>();
		if (in.getDocIds() != null) {
			for (Long id : in.getDocIds()) {
				if (id != null) {
					parts.add(String.valueOf(id));
				}
			}
		}
		if (in.getKycDocumentId() != null) {
			parts.add(String.valueOf(in.getKycDocumentId()));
		}
		return parts.isEmpty() ? null : String.join(",", parts);
	}

	private String allocateNextPublicTicketNo() {
		String zoneId = StringUtils.hasText(travelTicketIdZone) ? travelTicketIdZone.trim() : "Asia/Kolkata";
		ZoneId zone = ZoneId.of(zoneId);
		String dayKey = LocalDate.now(zone).format(DateTimeFormatter.BASIC_ISO_DATE);
		daySeqRepository.ensureDayRow(dayKey);
		TravelDeskTicketDaySeq row = daySeqRepository.findByDayKeyForUpdate(dayKey)
				.orElseThrow(() -> new IllegalStateException("Missing ticket day sequence for " + dayKey));
		int prev = row.getLastSeq() == null ? 0 : row.getLastSeq();
		int next = prev + 1;
		row.setLastSeq(next);
		daySeqRepository.saveAndFlush(row);
		return "APM-TRV-" + dayKey + "-" + String.format("%04d", next);
	}

	private void audit(Long ticketId, Long lineId, BigInteger actorEmpId, String actorEmail, String action,
			String remarks) {
		TravelDeskTicketAudit log = new TravelDeskTicketAudit();
		log.setTicketId(ticketId);
		log.setLineId(lineId);
		log.setActorEmpId(actorEmpId);
		log.setActorEmail(actorEmail);
		log.setAction(action);
		log.setRemarks(remarks);
		auditRepository.save(log);
	}

	private static Timestamp nowTs() {
		return new Timestamp(System.currentTimeMillis());
	}

	private static String trim(String s) {
		return s != null ? s.trim() : null;
	}
}
