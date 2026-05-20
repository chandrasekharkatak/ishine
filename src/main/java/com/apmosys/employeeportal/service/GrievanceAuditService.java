package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.GrievanceAuditDiffResponseDTO;
import com.apmosys.employeeportal.dto.GrievanceAuditFieldChangeDTO;
import com.apmosys.employeeportal.dto.GrievanceAuditTimelineEntryDTO;
import com.apmosys.employeeportal.dto.GrievanceAuditTimelinePageDTO;
import com.apmosys.employeeportal.dto.GrievanceAuditVersionDTO;
import com.apmosys.employeeportal.grievance.audit.GrievanceAuditEventType;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.GrievanceAuditLog;
import com.apmosys.employeeportal.model.GrievanceAuditLogDetail;
import com.apmosys.employeeportal.model.GrievanceTicket;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.GrievanceAuditLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Immutable audit trail for grievance tickets: snapshots, field diffs, versioning.
 * Paths are masked in stored JSON; use file-name fields for attachment identity.
 */
@Service
public class GrievanceAuditService {

	private static final Logger LOG = LoggerFactory.getLogger(GrievanceAuditService.class);

	private static final int MAX_PAGE = 100;
	private static final Set<String> MASK_PATH_KEYS = Set.of("proofFilePath", "resolutionDocPath");
	private static final Set<String> SKIP_DIFF_KEYS = Set.of();
	/** Only {@code updatedBy} is resolved to employee name; {@code assignedToEmpId}/{@code createdByEmpId} stay numeric IDs. */
	private static final Set<String> PORTAL_EMP_ID_FIELDS = Set.of("updatedBy");
	private static final Set<String> PORTAL_EMP_ID_NUMERIC_FIELDS = Set.of("assignedToEmpId", "createdByEmpId");
	private static final Pattern PORTAL_EMP_ID_DIGITS = Pattern.compile("-?\\d+");
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	@Autowired
	private GrievanceAuditLogRepository auditLogRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EmployeeRepository employeeRepository;

	/** Deep scalar copy for "before" state (not persisted). */
	public GrievanceTicket copyTicketScalars(GrievanceTicket src) {
		if (src == null) {
			return null;
		}
		GrievanceTicket c = new GrievanceTicket();
		c.setTicketId(src.getTicketId());
		c.setTicketNumber(src.getTicketNumber());
		c.setCreatedByEmpId(src.getCreatedByEmpId());
		c.setCreatedByName(src.getCreatedByName());
		c.setCreatedByEmployeeId(src.getCreatedByEmployeeId());
		c.setCreatedByDepartment(src.getCreatedByDepartment());
		c.setCreatedByPhone(src.getCreatedByPhone());
		c.setCreatedByProjectName(src.getCreatedByProjectName());
		c.setCreatedByClientName(src.getCreatedByClientName());
		c.setCreatedByReportingManager(src.getCreatedByReportingManager());
		c.setSubject(src.getSubject());
		c.setCategory(src.getCategory());
		c.setSubCategory(src.getSubCategory());
		c.setTicketFeature(src.getTicketFeature());
		c.setIssueScenario(src.getIssueScenario());
		c.setDescription(src.getDescription());
		c.setPriority(src.getPriority());
		c.setStatus(src.getStatus());
		c.setAssignedToEmpId(src.getAssignedToEmpId());
		c.setAssignedToName(src.getAssignedToName());
		c.setResolutionRemarks(src.getResolutionRemarks());
		c.setResolutionCategory(src.getResolutionCategory());
		c.setActionTaken(src.getActionTaken());
		c.setResolvedBy(src.getResolvedBy());
		c.setResolutionDate(src.getResolutionDate());
		c.setResolutionDocName(src.getResolutionDocName());
		c.setResolutionDocPath(src.getResolutionDocPath());
		c.setFeedbackRating(src.getFeedbackRating());
		c.setFeedbackComments(src.getFeedbackComments());
		c.setFeedbackOn(src.getFeedbackOn());
		c.setProofFileName(src.getProofFileName());
		c.setProofFilePath(src.getProofFilePath());
		c.setCreatedOn(src.getCreatedOn());
		c.setUpdatedOn(src.getUpdatedOn());
		c.setUpdatedBy(src.getUpdatedBy());
		c.setIsActive(src.getIsActive());
		c.setClosedByCreator(src.getClosedByCreator());
		return c;
	}

	@Transactional
	public void recordTicketCreated(GrievanceTicket ticket, Long empId, String empName, int proofFileCount) {
		if (ticket == null || ticket.getTicketId() == null) {
			return;
		}
		try {
			int next = nextAuditVersionNo(ticket.getTicketId());
			Map<String, Object> afterMap = ticketToAuditableMap(ticket);
			Map<String, Object> beforeMap = new LinkedHashMap<>();
			List<GrievanceAuditLogDetail> details = buildCreationDetails(afterMap, proofFileCount);
			String summary = "Ticket " + safe(ticket.getTicketNumber()) + " created"
					+ (proofFileCount > 0 ? " with " + proofFileCount + " proof attachment(s)." : ".");
			persistLog(ticket.getTicketId(), GrievanceAuditEventType.TICKET_CREATED, summary, next, empId, empName, beforeMap,
					afterMap, details, null);
		} catch (Exception e) {
			LOG.warn("Grievance audit (TICKET_CREATED) skipped for ticket {} — apply grievance-audit-schema.sql for full audit. {}",
					ticket.getTicketId(), e.toString());
		}
	}

	@Transactional
	public void recordTicketUpdated(GrievanceTicket before, GrievanceTicket after, Long empId, String empName,
			boolean resolutionDocAdded, String reason) {
		if (after == null || after.getTicketId() == null || before == null) {
			return;
		}
		Map<String, Object> oldMap = ticketToAuditableMap(before);
		Map<String, Object> newMap = ticketToAuditableMap(after);
		List<GrievanceAuditLogDetail> details = diffToDetails(oldMap, newMap);
		if (details.isEmpty() && !resolutionDocAdded) {
			return;
		}
		if (resolutionDocAdded && details.stream().noneMatch(d -> "resolutionDocName".equals(d.getFieldName()))) {
			GrievanceAuditLogDetail d = new GrievanceAuditLogDetail();
			d.setFieldName("resolutionDocName");
			d.setOldValue(stringify(oldMap.get("resolutionDocName")));
			d.setNewValue(stringify(newMap.get("resolutionDocName")));
			details.add(d);
		}
		try {
			int next = nextAuditVersionNo(after.getTicketId());
			GrievanceAuditEventType event = resolveUpdateEventType(details, after.getStatus(), resolutionDocAdded);
			String summary = buildUpdateSummary(details, event, after);
			persistLog(after.getTicketId(), event, summary, next, empId, empName, oldMap, newMap, details, reason);
		} catch (Exception e) {
			LOG.warn("Grievance audit (update) skipped for ticket {}: {}", after.getTicketId(), e.toString());
		}
	}

	@Transactional
	public void recordFeedbackSubmitted(GrievanceTicket before, GrievanceTicket after, Long empId, String empName) {
		if (after == null || before == null || after.getTicketId() == null) {
			return;
		}
		Map<String, Object> oldMap = ticketToAuditableMap(before);
		Map<String, Object> newMap = ticketToAuditableMap(after);
		List<GrievanceAuditLogDetail> details = diffToDetails(oldMap, newMap);
		if (details.isEmpty()) {
			return;
		}
		try {
			int next = nextAuditVersionNo(after.getTicketId());
			String summary = "Feedback submitted" + (after.getFeedbackRating() != null ? " (" + after.getFeedbackRating() + "/5)." : ".");
			persistLog(after.getTicketId(), GrievanceAuditEventType.COMMENT_ADDED, summary, next, empId, empName, oldMap, newMap,
					details, null);
		} catch (Exception e) {
			LOG.warn("Grievance audit (feedback) skipped for ticket {}: {}", after.getTicketId(), e.toString());
		}
	}

	@Transactional
	public void recordReopen(GrievanceTicket before, GrievanceTicket after, Long empId, String empName) {
		recordSimpleTransition(before, after, empId, empName, GrievanceAuditEventType.STATUS_CHANGED,
				"Ticket reopened (status set to RE-OPENED).");
	}

	@Transactional
	public void recordWithdraw(GrievanceTicket before, GrievanceTicket after, Long empId, String empName) {
		recordSimpleTransition(before, after, empId, empName, GrievanceAuditEventType.STATUS_CHANGED,
				"Ticket withdrawn by creator.");
	}

	@Transactional
	public void recordSystemAutoClose(GrievanceTicket before, GrievanceTicket after) {
		recordSimpleTransition(before, after, null, "System (auto-close)", GrievanceAuditEventType.TICKET_CLOSED,
				"Ticket auto-closed after resolution window.");
	}

	private void recordSimpleTransition(GrievanceTicket before, GrievanceTicket after, Long empId, String empName,
			GrievanceAuditEventType type, String summaryFallback) {
		if (after == null || before == null || after.getTicketId() == null) {
			return;
		}
		Map<String, Object> oldMap = ticketToAuditableMap(before);
		Map<String, Object> newMap = ticketToAuditableMap(after);
		List<GrievanceAuditLogDetail> details = diffToDetails(oldMap, newMap);
		if (details.isEmpty()) {
			return;
		}
		try {
			int next = nextAuditVersionNo(after.getTicketId());
			String summary = buildUpdateSummary(details, type, after);
			if (isBlank(summary)) {
				summary = summaryFallback;
			}
			persistLog(after.getTicketId(), type, summary, next, empId, empName, oldMap, newMap, details, null);
		} catch (Exception e) {
			LOG.warn("Grievance audit ({}) skipped for ticket {}: {}", type, after.getTicketId(), e.toString());
		}
	}

	private int nextAuditVersionNo(Long ticketId) {
		Integer max = auditLogRepository.findMaxVersionNoByTicketId(ticketId);
		return (max == null ? 0 : max) + 1;
	}

	@Transactional(readOnly = true)
	public GrievanceAuditTimelinePageDTO getTimeline(Long ticketId, String fieldFilter, int pageOneBased, int size) {
		int pageIdx = Math.max(0, pageOneBased - 1);
		int pageSize = size <= 0 ? 20 : Math.min(size, MAX_PAGE);
		Pageable pageable = PageRequest.of(pageIdx, pageSize, Sort.by(Sort.Direction.DESC, "versionNo"));
		Page<GrievanceAuditLog> page;
		if (fieldFilter == null || fieldFilter.trim().isEmpty()) {
			page = auditLogRepository.findByTicketIdAndIsDeletedOrderByVersionNoDesc(ticketId, 0, pageable);
		} else {
			page = auditLogRepository.findByTicketIdAndField(ticketId, fieldFilter.trim(), pageable);
		}
		GrievanceAuditTimelinePageDTO out = new GrievanceAuditTimelinePageDTO();
		out.setContent(page.getContent().stream().map(this::toTimelineEntry).collect(Collectors.toList()));
		out.setTotalElements(page.getTotalElements());
		out.setTotalPages(page.getTotalPages());
		out.setPage(pageOneBased);
		out.setSize(pageSize);
		return out;
	}

	@Transactional(readOnly = true)
	public List<GrievanceAuditVersionDTO> listVersions(Long ticketId) {
		List<GrievanceAuditLog> rows = auditLogRepository.findByTicketIdAndIsDeletedOrderByVersionNoAsc(ticketId, 0);
		List<GrievanceAuditVersionDTO> list = new ArrayList<>();
		for (GrievanceAuditLog a : rows) {
			list.add(new GrievanceAuditVersionDTO(a.getVersionNo(), formatTs(a.getChangedAt()), a.getEventType(), a.getSummary(),
					a.getChangedByName()));
		}
		return list;
	}

	@Transactional(readOnly = true)
	public Optional<GrievanceAuditDiffResponseDTO> diffVersions(Long ticketId, int version1, int version2) {
		if (version1 == version2) {
			GrievanceAuditDiffResponseDTO empty = new GrievanceAuditDiffResponseDTO();
			empty.setVersion1(version1);
			empty.setVersion2(version2);
			return Optional.of(empty);
		}
		Optional<GrievanceAuditLog> o1 = auditLogRepository.findByTicketIdAndVersionNoAndIsDeleted(ticketId, version1, 0);
		Optional<GrievanceAuditLog> o2 = auditLogRepository.findByTicketIdAndVersionNoAndIsDeleted(ticketId, version2, 0);
		if (o1.isEmpty() || o2.isEmpty()) {
			return Optional.empty();
		}
		Map<String, Object> m1 = parseSnapshot(o1.get().getNewSnapshot());
		Map<String, Object> m2 = parseSnapshot(o2.get().getNewSnapshot());
		GrievanceAuditDiffResponseDTO dto = new GrievanceAuditDiffResponseDTO();
		dto.setVersion1(version1);
		dto.setVersion2(version2);
		dto.setChanges(diffMapsToDto(m1, m2));
		return Optional.of(dto);
	}

	private GrievanceAuditTimelineEntryDTO toTimelineEntry(GrievanceAuditLog a) {
		GrievanceAuditTimelineEntryDTO e = new GrievanceAuditTimelineEntryDTO();
		e.setAuditId(a.getAuditId());
		e.setTicketId(a.getTicketId());
		e.setEventType(a.getEventType());
		e.setSummary(a.getSummary());
		e.setVersionNo(a.getVersionNo());
		e.setChangedBy(a.getChangedBy());
		e.setChangedByName(a.getChangedByName());
		e.setChangedAt(formatTs(a.getChangedAt()));
		e.setReason(a.getReason());
		if (a.getDetails() != null) {
			for (GrievanceAuditLogDetail d : a.getDetails()) {
				e.getFieldChanges().add(new GrievanceAuditFieldChangeDTO(d.getFieldName(),
						resolvePortalEmpIdForDisplay(d.getFieldName(), coerceEmpIdDetailString(d.getFieldName(), d.getOldValue())),
						resolvePortalEmpIdForDisplay(d.getFieldName(), coerceEmpIdDetailString(d.getFieldName(), d.getNewValue()))));
			}
		}
		e.setHasFieldDetails(!e.getFieldChanges().isEmpty());
		return e;
	}

	private void persistLog(Long ticketId, GrievanceAuditEventType eventType, String summary, int versionNo, Long changedBy,
			String changedByName, Map<String, Object> oldMap, Map<String, Object> newMap, List<GrievanceAuditLogDetail> details,
			String reason) {
		GrievanceAuditLog log = new GrievanceAuditLog();
		log.setTicketId(ticketId);
		log.setEventType(eventType.name());
		log.setSummary(truncate(summary, 2000));
		log.setVersionNo(versionNo);
		log.setChangedBy(changedBy);
		log.setChangedByName(truncate(changedByName, 200));
		log.setChangedAt(new Timestamp(System.currentTimeMillis()));
		log.setReason(truncate(reason, 1000));
		log.setIsDeleted(0);
		try {
			log.setOldSnapshot(oldMap == null || oldMap.isEmpty() ? null : objectMapper.writeValueAsString(oldMap));
			log.setNewSnapshot(objectMapper.writeValueAsString(newMap));
		} catch (Exception ex) {
			throw new IllegalStateException("Audit snapshot serialization failed", ex);
		}
		for (GrievanceAuditLogDetail d : details) {
			d.setAuditLog(log);
			log.getDetails().add(d);
		}
		auditLogRepository.save(log);
	}

	private List<GrievanceAuditLogDetail> buildCreationDetails(Map<String, Object> afterMap, int proofCount) {
		List<GrievanceAuditLogDetail> list = new ArrayList<>();
		for (Map.Entry<String, Object> e : afterMap.entrySet()) {
			if (SKIP_DIFF_KEYS.contains(e.getKey()) || MASK_PATH_KEYS.contains(e.getKey())) {
				continue;
			}
			String nv = stringifyAuditFieldValue(e.getKey(), e.getValue());
			if (isBlank(nv)) {
				continue;
			}
			GrievanceAuditLogDetail d = new GrievanceAuditLogDetail();
			d.setFieldName(e.getKey());
			d.setOldValue("");
			d.setNewValue(nv);
			list.add(d);
		}
		if (proofCount > 0) {
			GrievanceAuditLogDetail d = new GrievanceAuditLogDetail();
			d.setFieldName("proofAttachmentsCount");
			d.setOldValue("");
			d.setNewValue(String.valueOf(proofCount));
			list.add(d);
		}
		return list;
	}

	private List<GrievanceAuditLogDetail> diffToDetails(Map<String, Object> oldMap, Map<String, Object> newMap) {
		Set<String> keys = new LinkedHashSet<>();
		keys.addAll(oldMap.keySet());
		keys.addAll(newMap.keySet());
		List<GrievanceAuditLogDetail> out = new ArrayList<>();
		for (String key : keys) {
			if (SKIP_DIFF_KEYS.contains(key)) {
				continue;
			}
			String s1 = stringifyAuditFieldValue(key, oldMap.get(key));
			String s2 = stringifyAuditFieldValue(key, newMap.get(key));
			if (Objects.equals(s1, s2)) {
				continue;
			}
			GrievanceAuditLogDetail d = new GrievanceAuditLogDetail();
			d.setFieldName(key);
			d.setOldValue(s1);
			d.setNewValue(s2);
			out.add(d);
		}
		return out;
	}

	private List<GrievanceAuditFieldChangeDTO> diffMapsToDto(Map<String, Object> m1, Map<String, Object> m2) {
		Set<String> keys = new LinkedHashSet<>();
		keys.addAll(m1.keySet());
		keys.addAll(m2.keySet());
		List<GrievanceAuditFieldChangeDTO> out = new ArrayList<>();
		for (String key : keys) {
			if (SKIP_DIFF_KEYS.contains(key)) {
				continue;
			}
			String s1 = stringifyAuditFieldValue(key, m1.get(key));
			String s2 = stringifyAuditFieldValue(key, m2.get(key));
			if (Objects.equals(s1, s2)) {
				continue;
			}
			out.add(new GrievanceAuditFieldChangeDTO(key, resolvePortalEmpIdForDisplay(key, s1), resolvePortalEmpIdForDisplay(key, s2)));
		}
		return out;
	}

	/**
	 * For {@code updatedBy} only: map stored portal emp id to {@link Employee#getName()}; other fields pass through.
	 */
	private String resolvePortalEmpIdForDisplay(String fieldName, String rawValue) {
		if (rawValue == null || rawValue.isEmpty()) {
			return rawValue == null ? "" : rawValue;
		}
		if (!PORTAL_EMP_ID_FIELDS.contains(fieldName)) {
			return rawValue;
		}
		String trimmed = rawValue.trim();
		try {
			long id = Long.parseLong(trimmed);
			if (id <= 0L) {
				return rawValue;
			}
			Optional<Employee> opt = employeeRepository.findById(id);
			if (opt.isEmpty()) {
				return trimmed;
			}
			String name = opt.get().getName();
			return (name == null || name.isBlank()) ? trimmed : name.trim();
		} catch (NumberFormatException e) {
			return rawValue;
		}
	}

	private GrievanceAuditEventType resolveUpdateEventType(List<GrievanceAuditLogDetail> details, String newStatus,
			boolean resolutionDocAdded) {
		boolean closed = "CLOSED".equalsIgnoreCase(safe(newStatus));
		boolean statusTouched = details.stream().anyMatch(d -> "status".equals(d.getFieldName()));
		boolean assignTouched = details.stream().anyMatch(d -> "assignedToEmpId".equals(d.getFieldName())
				|| "assignedToName".equals(d.getFieldName()));
		boolean onlyAttachment = resolutionDocAdded && details.stream().allMatch(d -> "resolutionDocName".equals(d.getFieldName())
				|| "resolutionDocPath".equals(d.getFieldName()) || "updatedOn".equals(d.getFieldName())
				|| "updatedBy".equals(d.getFieldName()) || "resolvedBy".equals(d.getFieldName())
				|| "resolutionDate".equals(d.getFieldName()));

		if (closed && statusTouched) {
			return GrievanceAuditEventType.TICKET_CLOSED;
		}
		if (resolutionDocAdded && (onlyAttachment || details.stream().anyMatch(d -> "resolutionDocName".equals(d.getFieldName())))) {
			if (!statusTouched && !assignTouched && details.size() <= 6) {
				return GrievanceAuditEventType.ATTACHMENT_ADDED;
			}
		}
		if (statusTouched) {
			return GrievanceAuditEventType.STATUS_CHANGED;
		}
		if (assignTouched) {
			return GrievanceAuditEventType.ASSIGNED_USER_CHANGED;
		}
		return GrievanceAuditEventType.FIELD_UPDATED;
	}

	private String buildUpdateSummary(List<GrievanceAuditLogDetail> details, GrievanceAuditEventType type, GrievanceTicket after) {
		if (details == null || details.isEmpty()) {
			return type.name().replace('_', ' ');
		}
		Optional<GrievanceAuditLogDetail> st = details.stream().filter(d -> "status".equals(d.getFieldName())).findFirst();
		if (st.isPresent()) {
			return "Status changed from \"" + truncate(st.get().getOldValue(), 80) + "\" to \"" + truncate(st.get().getNewValue(), 80)
					+ "\".";
		}
		Optional<GrievanceAuditLogDetail> as = details.stream()
				.filter(d -> "assignedToName".equals(d.getFieldName()) || "assignedToEmpId".equals(d.getFieldName())).findFirst();
		if (as.isPresent() && type == GrievanceAuditEventType.ASSIGNED_USER_CHANGED) {
			return "Assignee updated.";
		}
		if (type == GrievanceAuditEventType.ATTACHMENT_ADDED) {
			return "Resolution attachment added or updated.";
		}
		GrievanceAuditLogDetail first = details.get(0);
		return "Updated " + first.getFieldName() + (details.size() > 1 ? " and " + (details.size() - 1) + " other field(s)." : ".");
	}

	private Map<String, Object> ticketToAuditableMap(GrievanceTicket t) {
		Map<String, Object> m = new LinkedHashMap<>();
		put(m, "ticketId", t.getTicketId());
		put(m, "ticketNumber", t.getTicketNumber());
		put(m, "createdByEmpId", t.getCreatedByEmpId());
		put(m, "createdByName", t.getCreatedByName());
		put(m, "createdByEmployeeId", t.getCreatedByEmployeeId());
		put(m, "createdByDepartment", t.getCreatedByDepartment());
		put(m, "createdByPhone", t.getCreatedByPhone());
		put(m, "createdByProjectName", t.getCreatedByProjectName());
		put(m, "createdByClientName", t.getCreatedByClientName());
		put(m, "createdByReportingManager", t.getCreatedByReportingManager());
		put(m, "subject", t.getSubject());
		put(m, "category", t.getCategory());
		put(m, "subCategory", t.getSubCategory());
		put(m, "ticketFeature", t.getTicketFeature());
		put(m, "issueScenario", t.getIssueScenario());
		put(m, "description", t.getDescription());
		put(m, "priority", t.getPriority());
		put(m, "status", t.getStatus());
		put(m, "assignedToEmpId", t.getAssignedToEmpId());
		put(m, "assignedToName", t.getAssignedToName());
		put(m, "resolutionRemarks", t.getResolutionRemarks());
		put(m, "resolutionCategory", t.getResolutionCategory());
		put(m, "actionTaken", t.getActionTaken());
		put(m, "resolvedBy", t.getResolvedBy());
		putTs(m, "resolutionDate", t.getResolutionDate());
		put(m, "resolutionDocName", t.getResolutionDocName());
		m.put("resolutionDocPath", maskPath(t.getResolutionDocPath()));
		put(m, "feedbackRating", t.getFeedbackRating());
		put(m, "feedbackComments", t.getFeedbackComments());
		putTs(m, "feedbackOn", t.getFeedbackOn());
		put(m, "proofFileName", t.getProofFileName());
		m.put("proofFilePath", maskPath(t.getProofFilePath()));
		putTs(m, "createdOn", t.getCreatedOn());
		putTs(m, "updatedOn", t.getUpdatedOn());
		put(m, "updatedBy", t.getUpdatedBy());
		put(m, "isActive", t.getIsActive());
		put(m, "closedByCreator", t.getClosedByCreator());
		return m;
	}

	private static String maskPath(String path) {
		return path == null || path.isEmpty() ? null : "[REDACTED_PATH]";
	}

	private static void put(Map<String, Object> m, String k, Object v) {
		m.put(k, v);
	}

	private static void putTs(Map<String, Object> m, String k, Timestamp ts) {
		m.put(k, ts == null ? null : ts.toInstant().toString());
	}

	private Map<String, Object> parseSnapshot(String json) {
		if (json == null || json.isEmpty()) {
			return new LinkedHashMap<>();
		}
		try {
			return objectMapper.readValue(json, MAP_TYPE);
		} catch (Exception e) {
			return new LinkedHashMap<>();
		}
	}

	private static String stringify(Object o) {
		if (o == null) {
			return "";
		}
		return String.valueOf(o);
	}

	/**
	 * Ensures {@code assignedToEmpId} / {@code createdByEmpId} are shown as portal numeric ids even when snapshots or
	 * legacy detail rows stored a person name (JSON type coercion or older bugs).
	 */
	private String stringifyAuditFieldValue(String fieldName, Object o) {
		if (o == null) {
			return "";
		}
		if (!PORTAL_EMP_ID_NUMERIC_FIELDS.contains(fieldName)) {
			return stringify(o);
		}
		if (o instanceof Number) {
			return String.valueOf(((Number) o).longValue());
		}
		String s = String.valueOf(o).trim();
		if (s.isEmpty()) {
			return "";
		}
		if (PORTAL_EMP_ID_DIGITS.matcher(s).matches()) {
			return s;
		}
		return resolveNameToPortalEmpId(s);
	}

	/** Timeline rows read string columns; coerce name-shaped values back to emp id for *EmpId fields. */
	private String coerceEmpIdDetailString(String fieldName, String raw) {
		if (raw == null) {
			return "";
		}
		if (!PORTAL_EMP_ID_NUMERIC_FIELDS.contains(fieldName)) {
			return raw;
		}
		String s = raw.trim();
		if (s.isEmpty()) {
			return "";
		}
		if (PORTAL_EMP_ID_DIGITS.matcher(s).matches()) {
			return s;
		}
		return resolveNameToPortalEmpId(s);
	}

	private String resolveNameToPortalEmpId(String displayName) {
		if (displayName == null || displayName.isEmpty()) {
			return "";
		}
		List<Long> ids = employeeRepository.findEmpIdsByNameIgnoreCaseTrim(displayName);
		if (ids != null && !ids.isEmpty()) {
			return String.valueOf(ids.get(0));
		}
		return displayName;
	}

	private static String formatTs(Timestamp ts) {
		return ts == null ? "" : ts.toInstant().toString();
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		if (s.length() <= max) {
			return s;
		}
		return s.substring(0, max - 3) + "...";
	}
}
