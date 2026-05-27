package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.EmployeeRepository;

@Service
public class SkillMatrixNotificationService {

	@Autowired
	private MailService mailService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EmployeeRepository employeeRepository;

	private static final String EMAIL_BRAND = "#1B3461";
	private static final String EMAIL_MUTED = "#64748B";
	private static final String EMAIL_BORDER = "#E2E8F0";

	public void notifyManagerReviewRequired(String submissionId) {
		SubmissionMailContext ctx = loadSubmissionContext(submissionId);
		if (ctx == null || !StringUtils.hasText(ctx.managerEmail)) {
			return;
		}
		String body = skillMatrixEmailWrapper(
				"Action Required",
				"Dear <strong>" + esc(fallback(ctx.managerName, "Manager")) + "</strong>,",
				"A Skill Matrix submission is awaiting your review.",
				submissionSummaryHtml(ctx),
				null,
				"Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix &rarr; Approve Requests</strong> to review this submission.");
		sendMailSafe(ctx.managerEmail, ctx.employeeEmail,
				"Skill Matrix " + fallback(ctx.submissionId, "") + " - Action Required",
				body);
	}

	public void notifyHodReviewRequired(String submissionId, String managerComment) {
		SubmissionMailContext ctx = loadSubmissionContext(submissionId);
		if (ctx == null || !StringUtils.hasText(ctx.hodEmail)) {
			return;
		}
		String body = skillMatrixEmailWrapper(
				"Action Required",
				"Dear <strong>" + esc(fallback(ctx.hodName, "HOD")) + "</strong>,",
				"A Skill Matrix submission has been approved by the Manager and is awaiting your review.",
				submissionSummaryHtml(ctx),
				styledRemarksBlock("Manager remarks", managerComment),
				"Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix &rarr; Approve Requests</strong> to complete the review.");
		sendMailSafe(ctx.hodEmail, ctx.employeeEmail,
				"Skill Matrix " + fallback(ctx.submissionId, "") + " - HOD Review Required",
				body);
	}

	public void notifyEmployeeStatus(String submissionId, String statusLabel, String introLine, String remarksLabel,
			String remarks) {
		SubmissionMailContext ctx = loadSubmissionContext(submissionId);
		if (ctx == null || !StringUtils.hasText(ctx.employeeEmail)) {
			return;
		}
		String body = skillMatrixEmailWrapper(
				fallback(statusLabel, friendlySubmissionStatus(ctx.status)),
				"Dear <strong>" + esc(fallback(ctx.employeeName, "Employee")) + "</strong>,",
				esc(fallback(introLine, "Your Skill Matrix submission has been updated.")),
				submissionSummaryHtml(ctx),
				styledRemarksBlock(fallback(remarksLabel, "Review remarks"), remarks),
				buildEmployeeCta(statusLabel));
		sendMailSafe(ctx.employeeEmail, null,
				"Skill Matrix " + fallback(ctx.submissionId, "") + " - " + fallback(statusLabel, "Updated"),
				body);
	}

	public void notifyCustomSkillRequestPending(Long requestId) {
		CustomSkillMailContext ctx = loadCustomSkillContext(requestId);
		if (ctx == null || !StringUtils.hasText(ctx.hodEmail)) {
			return;
		}
		String body = skillMatrixEmailWrapper(
				"Action Required",
				"Dear <strong>" + esc(fallback(ctx.hodName, "HOD")) + "</strong>,",
				"A custom skill request is awaiting your approval.",
				customSkillSummaryHtml(ctx),
				null,
				"Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix &rarr; Approve Requests</strong> to review this custom skill request.");
		sendMailSafe(ctx.hodEmail, ctx.requestedByEmail,
				"Skill Matrix Custom Skill Request - Action Required",
				body);
	}

	public void notifyCustomSkillRequestStatus(Long requestId, String statusLabel, String introLine) {
		CustomSkillMailContext ctx = loadCustomSkillContext(requestId);
		if (ctx == null || !StringUtils.hasText(ctx.requestedByEmail)) {
			return;
		}
		String extra = styledRemarksBlock("HOD remarks", ctx.decisionComment);
		if ("approved".equalsIgnoreCase(ctx.status) && StringUtils.hasText(ctx.approvedSkillType)) {
			extra += "<div style='margin:0 0 18px 0;padding:12px 16px;background:#F8FAFC;border:1px solid " + EMAIL_BORDER
					+ ";border-radius:8px;'>"
					+ "<span style='display:block;font-size:12px;font-weight:700;color:" + EMAIL_MUTED
					+ ";text-transform:uppercase;letter-spacing:0.04em;'>Approved skill type</span>"
					+ "<span style='font-size:14px;color:#0F172A;line-height:1.5;'>"
					+ esc(ctx.approvedSkillType) + "</span></div>";
		}
		String body = skillMatrixEmailWrapper(
				fallback(statusLabel, friendlyCustomSkillStatus(ctx.status)),
				"Dear <strong>" + esc(fallback(ctx.requestedByName, "Employee")) + "</strong>,",
				esc(fallback(introLine, "Your custom skill request has been updated.")),
				customSkillSummaryHtml(ctx),
				extra,
				"Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix &rarr; Submit for review</strong> if you need to continue working on your submission.");
		sendMailSafe(ctx.requestedByEmail, null,
				"Skill Matrix Custom Skill Request - " + fallback(statusLabel, "Updated"),
				body);
	}

	private SubmissionMailContext loadSubmissionContext(String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			return null;
		}
		java.util.List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT s.submission_id, s.employee_id, s.employee_name, s.dept_name, s.designation, s.status, s.submitted_at, "
						+ "a.manager_id, a.manager_name, a.hod_id, a.hod_name, a.manager_comment, a.hod_comment, a.final_status "
						+ "FROM skillmatrix_assessment_submission s "
						+ "LEFT JOIN skillmatrix_assessment_approval a "
						+ "  ON a.submission_id = s.submission_id "
						+ " AND a.revision_round = (SELECT MAX(x.revision_round) FROM skillmatrix_assessment_approval x WHERE x.submission_id = s.submission_id) "
						+ "WHERE s.submission_id = ? LIMIT 1",
				submissionId);
		if (rows.isEmpty()) {
			return null;
		}
		Map<String, Object> row = rows.get(0);
		SubmissionMailContext ctx = new SubmissionMailContext();
		ctx.submissionId = value(row.get("submission_id"));
		ctx.employeeId = longValue(row.get("employee_id"));
		ctx.employeeName = value(row.get("employee_name"));
		ctx.deptName = value(row.get("dept_name"));
		ctx.designation = value(row.get("designation"));
		ctx.status = value(row.get("status"));
		ctx.submittedAt = timestampValue(row.get("submitted_at"));
		ctx.managerId = longValue(row.get("manager_id"));
		ctx.managerName = value(row.get("manager_name"));
		ctx.hodId = longValue(row.get("hod_id"));
		ctx.hodName = value(row.get("hod_name"));
		ctx.skillCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?",
				Integer.class,
				submissionId);

		ctx.employeeEmail = emailByEmpId(ctx.employeeId);
		ctx.managerEmail = emailByEmpId(ctx.managerId);
		ctx.hodEmail = emailByEmpId(ctx.hodId);
		return ctx;
	}

	private CustomSkillMailContext loadCustomSkillContext(Long requestId) {
		if (requestId == null) {
			return null;
		}
		java.util.List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT id, requested_by_emp_id, requested_by_name, designation, dept_name, hod_id, hod_name, skill_name, "
						+ "category_name, status, approved_skill_type, decision_comment, created_at, decided_at "
						+ "FROM skillmatrix_custom_skill_request WHERE id = ?",
				requestId);
		if (rows.isEmpty()) {
			return null;
		}
		Map<String, Object> row = rows.get(0);
		CustomSkillMailContext ctx = new CustomSkillMailContext();
		ctx.requestId = longValue(row.get("id"));
		ctx.requestedByEmpId = longValue(row.get("requested_by_emp_id"));
		ctx.requestedByName = value(row.get("requested_by_name"));
		ctx.designation = value(row.get("designation"));
		ctx.deptName = value(row.get("dept_name"));
		ctx.hodId = longValue(row.get("hod_id"));
		ctx.hodName = value(row.get("hod_name"));
		ctx.skillName = value(row.get("skill_name"));
		ctx.categoryName = value(row.get("category_name"));
		ctx.status = value(row.get("status"));
		ctx.approvedSkillType = value(row.get("approved_skill_type"));
		ctx.decisionComment = value(row.get("decision_comment"));
		ctx.createdAt = timestampValue(row.get("created_at"));
		ctx.requestedByEmail = emailByEmpId(ctx.requestedByEmpId);
		ctx.hodEmail = emailByEmpId(ctx.hodId);
		return ctx;
	}

	private String submissionSummaryHtml(SubmissionMailContext ctx) {
		String label = "padding:6px 0;font-size:12px;color:" + EMAIL_MUTED + ";width:150px;vertical-align:top;";
		String value = "padding:6px 0;font-size:14px;color:#0F172A;font-weight:500;vertical-align:top;";
		StringBuilder sb = new StringBuilder();
		sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#F8FAFC;border:1px solid ")
				.append(EMAIL_BORDER).append(";border-radius:8px;padding:4px 16px;margin:0 0 20px 0;'>");
		appendSummaryRow(sb, label, value, "Submission ID", esc(ctx.submissionId));
		appendSummaryRow(sb, label, value, "Employee", esc(fallback(ctx.employeeName, "—")));
		appendSummaryRow(sb, label, value, "Department", esc(fallback(ctx.deptName, "—")));
		appendSummaryRow(sb, label, value, "Designation", esc(fallback(ctx.designation, "—")));
		appendSummaryRow(sb, label, value, "Submitted On", esc(formatTs(ctx.submittedAt)));
		appendSummaryRow(sb, label, value, "Current Status", esc(friendlySubmissionStatus(ctx.status)));
		appendSummaryRow(sb, label, value, "Total Skills", String.valueOf(ctx.skillCount != null ? ctx.skillCount : 0));
		sb.append("</table>");
		return sb.toString();
	}

	private String customSkillSummaryHtml(CustomSkillMailContext ctx) {
		String label = "padding:6px 0;font-size:12px;color:" + EMAIL_MUTED + ";width:150px;vertical-align:top;";
		String value = "padding:6px 0;font-size:14px;color:#0F172A;font-weight:500;vertical-align:top;";
		StringBuilder sb = new StringBuilder();
		sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#F8FAFC;border:1px solid ")
				.append(EMAIL_BORDER).append(";border-radius:8px;padding:4px 16px;margin:0 0 20px 0;'>");
		appendSummaryRow(sb, label, value, "Request ID", ctx.requestId != null ? String.valueOf(ctx.requestId) : "");
		appendSummaryRow(sb, label, value, "Employee", esc(fallback(ctx.requestedByName, "—")));
		appendSummaryRow(sb, label, value, "Department", esc(fallback(ctx.deptName, "—")));
		appendSummaryRow(sb, label, value, "Designation", esc(fallback(ctx.designation, "—")));
		appendSummaryRow(sb, label, value, "Skill Name", esc(fallback(ctx.skillName, "—")));
		appendSummaryRow(sb, label, value, "Category", esc(fallback(ctx.categoryName, "—")));
		appendSummaryRow(sb, label, value, "Status", esc(friendlyCustomSkillStatus(ctx.status)));
		appendSummaryRow(sb, label, value, "Requested On", esc(formatTs(ctx.createdAt)));
		sb.append("</table>");
		return sb.toString();
	}

	private void appendSummaryRow(StringBuilder sb, String labelStyle, String valueStyle, String label, String value) {
		sb.append("<tr><td style='").append(labelStyle).append("'>").append(esc(label)).append("</td><td style='")
				.append(valueStyle).append("'>").append(value == null ? "" : value).append("</td></tr>");
	}

	private String styledRemarksBlock(String label, String remarks) {
		if (!StringUtils.hasText(remarks)) {
			return "";
		}
		return "<div style='margin:0 0 20px 0;padding:12px 16px;background:#FFFBEB;border:1px solid #FDE68A;border-radius:6px;'>"
				+ "<span style='display:block;font-size:12px;font-weight:600;color:#92400E;margin-bottom:4px;'>"
				+ esc(label) + "</span>"
				+ "<span style='font-size:14px;color:#78350F;line-height:1.5;'>" + esc(remarks.trim()) + "</span></div>";
	}

	private String buildEmployeeCta(String statusLabel) {
		String normalized = statusLabel != null ? statusLabel.trim().toLowerCase(Locale.ROOT) : "";
		if (normalized.contains("change")) {
			return "Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix &rarr; Submit for review</strong> to update and resubmit your submission.";
		}
		return "Please sign in to <strong>iShine</strong> and open <strong>Skill Matrix</strong> to view the latest submission status.";
	}

	private String skillMatrixEmailWrapper(String headerRightLabel, String greetingHtml, String introHtml,
			String summaryHtml, String extraHtml, String ctaLine) {
		StringBuilder inner = new StringBuilder();
		inner.append("<p style='margin:0 0 8px 0;font-size:16px;color:#0F172A;'>").append(greetingHtml).append("</p>");
		inner.append("<p style='margin:0 0 20px 0;font-size:14px;line-height:1.5;color:#475569;'>").append(introHtml)
				.append("</p>");
		inner.append(summaryHtml);
		if (StringUtils.hasText(extraHtml)) {
			inner.append(extraHtml);
		}
		if (StringUtils.hasText(ctaLine)) {
			inner.append("<p style='margin:20px 0 0 0;padding:14px 16px;background:#EFF6FF;border-left:4px solid ")
					.append(EMAIL_BRAND)
					.append(";border-radius:4px;font-size:13px;line-height:1.5;color:#1E3A5F;'>")
					.append(ctaLine).append("</p>");
		}
		return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'></head>"
				+ "<body style='margin:0;padding:0;background:#F1F5F9;font-family:Segoe UI,Helvetica,Arial,sans-serif;'>"
				+ "<table width='100%' cellpadding='0' cellspacing='0' style='background:#F1F5F9;padding:28px 16px;'><tr><td align='center'>"
				+ "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;background:#ffffff;border-radius:10px;overflow:hidden;border:1px solid "
				+ EMAIL_BORDER + ";'>"
				+ "<tr><td style='background:" + EMAIL_BRAND + ";padding:18px 24px;'>"
				+ "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
				+ "<td align='left' style='font-size:17px;font-weight:700;color:#ffffff;letter-spacing:0.2px;'>iShine Skill Matrix</td>"
				+ "<td align='right' style='font-size:13px;font-weight:600;color:#ffffff;white-space:nowrap;'>"
				+ esc(headerRightLabel) + "</td></tr></table></td></tr>"
				+ "<tr><td style='padding:20px 24px 24px 24px;'>" + inner
				+ "</td></tr>"
				+ "<tr><td style='padding:16px 24px;background:#F8FAFC;border-top:1px solid " + EMAIL_BORDER
				+ ";font-size:11px;color:" + EMAIL_MUTED + ";line-height:1.5;'>"
				+ "This is an automated message from iShine. Please do not reply to this email.<br>"
				+ "Regards,<br><strong>ApMoSys Technologies</strong> &middot; iShine HRMS"
				+ "</td></tr></table></td></tr></table></body></html>";
	}

	private void sendMailSafe(String to, String cc, String subject, String html) {
		try {
			if (!StringUtils.hasText(to)) {
				return;
			}
			if (StringUtils.hasText(cc)) {
				mailService.sendMailWithCC(to.trim(), cc.trim(), subject, html);
			} else {
				mailService.sendMail(to.trim(), subject, html);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String emailByEmpId(Long empId) {
		if (empId == null || empId.longValue() <= 0L) {
			return null;
		}
		try {
			Employee emp = employeeRepository.findByEmpId(empId);
			return emp != null && StringUtils.hasText(emp.getEmail()) ? emp.getEmail().trim() : null;
		} catch (Exception ex) {
			return null;
		}
	}

	private String friendlySubmissionStatus(String status) {
		if (!StringUtils.hasText(status)) {
			return "Updated";
		}
		String s = status.trim().toLowerCase(Locale.ROOT);
		if ("submitted".equals(s) || "under_review".equals(s)) {
			return "Pending review";
		}
		if ("approved".equals(s)) {
			return "Approved";
		}
		if ("rejected".equals(s)) {
			return "Rejected";
		}
		return status;
	}

	private String friendlyCustomSkillStatus(String status) {
		if (!StringUtils.hasText(status)) {
			return "Updated";
		}
		String s = status.trim().toLowerCase(Locale.ROOT);
		if ("pending".equals(s)) {
			return "Pending HOD approval";
		}
		if ("approved".equals(s)) {
			return "Approved";
		}
		if ("rejected".equals(s)) {
			return "Rejected";
		}
		return status;
	}

	private static String value(Object v) {
		return v != null ? String.valueOf(v) : null;
	}

	private static Long longValue(Object v) {
		return v instanceof Number ? ((Number) v).longValue() : null;
	}

	private static Timestamp timestampValue(Object v) {
		return v instanceof Timestamp ? (Timestamp) v : null;
	}

	private static String formatTs(Timestamp ts) {
		if (ts == null) {
			return "—";
		}
		return new SimpleDateFormat("dd MMM yyyy hh:mm a").format(ts);
	}

	private static String fallback(String value, String fallback) {
		return StringUtils.hasText(value) ? value.trim() : fallback;
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static class SubmissionMailContext {
		private String submissionId;
		private Long employeeId;
		private String employeeName;
		private String employeeEmail;
		private String deptName;
		private String designation;
		private String status;
		private Timestamp submittedAt;
		private Long managerId;
		private String managerName;
		private String managerEmail;
		private Long hodId;
		private String hodName;
		private String hodEmail;
		private Integer skillCount;
	}

	private static class CustomSkillMailContext {
		private Long requestId;
		private Long requestedByEmpId;
		private String requestedByName;
		private String requestedByEmail;
		private String designation;
		private String deptName;
		private Long hodId;
		private String hodName;
		private String hodEmail;
		private String skillName;
		private String categoryName;
		private String status;
		private String approvedSkillType;
		private String decisionComment;
		private Timestamp createdAt;
	}
}
