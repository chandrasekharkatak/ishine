package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.TravelDeskTicket;
import com.apmosys.employeeportal.model.TravelDeskTicketLine;
import com.apmosys.employeeportal.repository.EmployeeRepository;

@Service
public class TravelDeskNotificationService {

	@Autowired
	private MailService mailService;

	@Autowired
	private TravelDeskTicketMatrixWorkflowService matrixWorkflowService;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Value("${travel.workflow.admin.mail:finance2@apmosys.com}")
	private String workflowAdminMail;

	private static final String EMAIL_BRAND = "#1B3461";
	private static final String EMAIL_MUTED = "#64748B";
	private static final String EMAIL_BORDER = "#E2E8F0";
	private static final String HOTEL_ACCENT = "#B35F0A";
	private static final String FLIGHT_ACCENT = "#193D8A";

	public void notifyEmployeeTicketSubmitted(TravelDeskTicket ticket) {
		if (ticket == null || !StringUtils.hasText(ticket.getEmail())) {
			return;
		}
		String nextActor = currentActionOwnerLabel(ticket);
		String intro = "Your Travel Desk ticket has been submitted successfully.";
		String cta = StringUtils.hasText(nextActor)
				? "The ticket is now with <strong>" + esc(nextActor)
						+ "</strong>. You will receive another update after the next action."
				: "You will receive another update when the next action is taken.";
		String body = travelEmailWrapper("Submitted",
				"Dear <strong>" + esc(fallback(ticket.getFullName(), "Employee")) + "</strong>,",
				intro,
				ticketSummaryHtml(ticket),
				"Requests in this ticket",
				lineSummaryHtml(ticket),
				null,
				cta);
		sendMailSafe(ticket.getEmail(), null,
				"Travel Ticket " + ticketDisplayRef(ticket) + " – Submitted",
				body);
	}

	public void notifyCurrentStageActionRequired(TravelDeskTicket ticket, String introLine) {
		if (ticket == null) {
			return;
		}
		List<String> toEmails = currentStageRecipients(ticket);
		if (toEmails.isEmpty()) {
			return;
		}
		String approverName = currentActionOwnerLabel(ticket);
		String subject = "Travel Ticket " + ticketDisplayRef(ticket) + " – Action Required";
		String cta;
		if (TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
			cta = "Please sign in to <strong>iShine</strong> and open <strong>Travel Desk &rarr; Approve Travel Request</strong> to complete booking or reject the ticket.";
		} else {
			cta = "Please sign in to <strong>iShine</strong> and open <strong>Travel Desk &rarr; Approve Travel Request</strong> to review this ticket.";
		}
		String body = travelEmailWrapper("Action Required",
				"Dear <strong>" + esc(fallback(approverName, "Approver")) + "</strong>,",
				esc(introLine),
				ticketSummaryHtml(ticket),
				"Requests in this ticket",
				lineSummaryHtml(ticket),
				null,
				cta);
		sendMailSafe(String.join(",", toEmails), submitterCc(ticket), subject, body);
	}

	public void notifyAfterWorkflowTransition(TravelDeskTicket ticket, String introForApprover,
			String introForSubmitterIfTerminal, String terminalExtraHtml) {
		if (ticket == null) {
			return;
		}
		String stage = ticket.getWorkflowStage();
		if (TravelDeskTicket.STAGE_REJECTED.equals(stage) || TravelDeskTicket.STAGE_COMPLETED.equals(stage)) {
			notifySubmitterStatus(ticket, introForSubmitterIfTerminal, terminalExtraHtml);
			return;
		}
		if (StringUtils.hasText(introForApprover)) {
			notifyCurrentStageActionRequired(ticket, introForApprover);
		}
	}

	public void notifySubmitterStatus(TravelDeskTicket ticket, String introLine, String extraHtml) {
		if (ticket == null || !StringUtils.hasText(ticket.getEmail())) {
			return;
		}
		String subject = "Travel Ticket " + ticketDisplayRef(ticket) + " – " + displayTicketStatus(ticket);
		String body = travelEmailWrapper(displayTicketStatus(ticket),
				"Dear <strong>" + esc(fallback(ticket.getFullName(), "Employee")) + "</strong>,",
				esc(fallback(introLine, "Your Travel Desk ticket has been updated.")),
				ticketSummaryHtml(ticket),
				"Requests in this ticket",
				lineSummaryHtml(ticket),
				extraHtml,
				null);
		sendMailSafe(ticket.getEmail(), null, subject, body);
	}

	public String bookingDetailsHtml(TravelDeskTicket ticket, String remarks) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.hasText(remarks)) {
			sb.append(styledRemarksBlock("Travel Admin notes", remarks));
		}
		sb.append("<div style='margin:0 0 18px 0;padding:12px 16px;background:#F8FAFC;border:1px solid ")
				.append(EMAIL_BORDER).append(";border-radius:8px;'>");
		sb.append("<p style='margin:0 0 8px 0;font-size:12px;font-weight:700;color:")
				.append(EMAIL_MUTED).append(";text-transform:uppercase;letter-spacing:0.04em;'>Booking details</p>");
		List<TravelDeskTicketLine> lines = sortedLines(ticket);
		for (TravelDeskTicketLine line : lines) {
			sb.append("<div style='margin:10px 0 0 0;padding-top:10px;border-top:1px dashed ")
					.append(EMAIL_BORDER).append(";'>");
			sb.append("<strong style='font-size:13px;color:#0F172A;'>")
					.append(esc(lineBadgeLabel(line))).append(" request</strong><br>");
			sb.append("<span style='font-size:13px;color:#334155;'>")
					.append(esc(routeLabel(line))).append("</span><br>");
			sb.append("<span style='font-size:13px;color:#334155;'><strong>Booking reference:</strong> ")
					.append(esc(fallback(line.getBookingReference(), "Pending update"))).append("</span><br>");
			sb.append("<span style='font-size:13px;color:#334155;'><strong>Amount:</strong> ")
					.append(esc(formatAmount(line.getBookingAmount()))).append("</span>");
			sb.append("</div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	private List<String> currentStageRecipients(TravelDeskTicket ticket) {
		LinkedHashSet<String> out = new LinkedHashSet<>();
		if (TravelDeskTicket.STAGE_PENDING_LEVEL.equals(ticket.getWorkflowStage())) {
			if (matrixWorkflowService.usesMatrixWorkflow(ticket)) {
				out.addAll(matrixWorkflowService.notifyEmailsForCurrentLevelAll(ticket));
			} else if (StringUtils.hasText(ticket.getManagerEmail())) {
				out.add(normEmail(ticket.getManagerEmail()));
			}
		} else if (TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
			if (ticket.getCurrentAssigneeEmpId() != null) {
				String email = employeeEmailByEmpId(ticket.getCurrentAssigneeEmpId());
				if (StringUtils.hasText(email)) {
					out.add(normEmail(email));
				}
			}
			if (out.isEmpty() && StringUtils.hasText(workflowAdminMail)) {
				out.add(normEmail(workflowAdminMail));
			}
		}
		return new ArrayList<>(out);
	}

	private String currentActionOwnerLabel(TravelDeskTicket ticket) {
		if (ticket == null) {
			return null;
		}
		if (TravelDeskTicket.STAGE_PENDING_ADMIN.equals(ticket.getWorkflowStage())) {
			if (ticket.getCurrentAssigneeEmpId() != null) {
				String name = employeeNameByEmpId(ticket.getCurrentAssigneeEmpId());
				if (StringUtils.hasText(name)) {
					return name;
				}
			}
			String byMail = lookupEmployeeNameByEmail(workflowAdminMail);
			return StringUtils.hasText(byMail) ? byMail : "Travel Admin";
		}
		if (matrixWorkflowService.usesMatrixWorkflow(ticket)) {
			return matrixWorkflowService.notifyApproverDisplayName(ticket);
		}
		return StringUtils.hasText(ticket.getManagerName()) ? ticket.getManagerName().trim() : "HOD";
	}

	private String submitterCc(TravelDeskTicket ticket) {
		return ticket != null && StringUtils.hasText(ticket.getEmail()) ? ticket.getEmail().trim() : null;
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

	private String ticketDisplayRef(TravelDeskTicket ticket) {
		if (ticket == null) {
			return "";
		}
		if (StringUtils.hasText(ticket.getTicketNo())) {
			return ticket.getTicketNo();
		}
		return ticket.getTicketId() != null ? String.valueOf(ticket.getTicketId()) : "";
	}

	private String displayTicketStatus(TravelDeskTicket ticket) {
		if (ticket == null || !StringUtils.hasText(ticket.getWorkflowStage())) {
			return "Updated";
		}
		switch (ticket.getWorkflowStage()) {
		case TravelDeskTicket.STAGE_PENDING_LEVEL:
			return "Pending approval";
		case TravelDeskTicket.STAGE_PENDING_ADMIN:
			return "Pending travel admin";
		case TravelDeskTicket.STAGE_COMPLETED:
			return "Booked";
		case TravelDeskTicket.STAGE_REJECTED:
			return "Rejected";
		default:
			return ticket.getWorkflowStage();
		}
	}

	private String ticketSummaryHtml(TravelDeskTicket ticket) {
		String label = "padding:6px 0;font-size:12px;color:" + EMAIL_MUTED + ";width:140px;vertical-align:top;";
		String value = "padding:6px 0;font-size:14px;color:#0F172A;font-weight:500;vertical-align:top;";
		StringBuilder sb = new StringBuilder();
		sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:#F8FAFC;border:1px solid ")
				.append(EMAIL_BORDER).append(";border-radius:8px;padding:4px 16px;margin:0 0 20px 0;'>");
		appendSummaryRow(sb, label, value, "Ticket ID", esc(ticketDisplayRef(ticket)));
		appendSummaryRow(sb, label, value, "Status", esc(displayTicketStatus(ticket)));
		appendSummaryRow(sb, label, value, "Employee", esc(fallback(ticket.getFullName(), "—")));
		appendSummaryRow(sb, label, value, "Department", esc(fallback(ticket.getDepartment(), "—")));
		appendSummaryRow(sb, label, value, "Submitted On", esc(formatTs(ticket.getSubmittedOn())));
		sb.append("</table>");
		return sb.toString();
	}

	private void appendSummaryRow(StringBuilder sb, String labelStyle, String valueStyle, String label, String value) {
		sb.append("<tr><td style='").append(labelStyle).append("'>").append(esc(label)).append("</td><td style='")
				.append(valueStyle).append("'>").append(value == null ? "" : value).append("</td></tr>");
	}

	private String lineSummaryHtml(TravelDeskTicket ticket) {
		String th = "padding:10px 12px;background:" + EMAIL_BRAND + ";color:#ffffff;font-size:12px;font-weight:600;text-align:left;";
		String td = "padding:10px 12px;border-bottom:1px solid " + EMAIL_BORDER
				+ ";font-size:13px;color:#334155;vertical-align:top;";
		StringBuilder sb = new StringBuilder();
		sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid ")
				.append(EMAIL_BORDER).append(";border-radius:6px;border-collapse:separate;overflow:hidden;'>");
		sb.append("<tr><th style='").append(th).append("'>#</th><th style='").append(th)
				.append("'>Type</th><th style='").append(th).append("'>Route / stay</th><th style='").append(th)
				.append("'>Dates</th><th style='").append(th).append("'>Mode / category</th><th style='").append(th)
				.append("'>Status</th><th style='").append(th).append("'>Purpose</th></tr>");
		boolean alt = false;
		for (TravelDeskTicketLine line : sortedLines(ticket)) {
			String rowBg = alt ? "background:#F8FAFC;" : "background:#ffffff;";
			alt = !alt;
			sb.append("<tr>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(line.getLineNo()).append("</td>");
			sb.append("<td style='").append(td).append(rowBg).append("'><span style='display:inline-block;padding:2px 8px;border-radius:999px;font-size:11px;font-weight:700;background:")
					.append(isHotelLine(line) ? "rgba(242,140,24,0.14);color:" + HOTEL_ACCENT
							: "rgba(25,61,138,0.12);color:" + FLIGHT_ACCENT)
					.append(";'>").append(esc(lineBadgeLabel(line))).append("</span><br>")
					.append("<span>").append(esc(fallback(line.getRequestType(), "—"))).append("</span></td>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(esc(routeLabel(line))).append("</td>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(esc(dateLabel(line))).append("</td>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(esc(modeOrCategoryLabel(line))).append("</td>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(esc(lineStatusLabel(line))).append("</td>");
			sb.append("<td style='").append(td).append(rowBg).append("'>").append(esc(fallback(line.getPurpose(), "—"))).append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private String travelEmailWrapper(String headerRightLabel, String greetingHtml, String introHtml,
			String ticketSummaryHtml, String sectionTitle, String linesTableHtml, String extraSectionHtml,
			String ctaLine) {
		StringBuilder inner = new StringBuilder();
		inner.append("<p style='margin:0 0 8px 0;font-size:16px;color:#0F172A;'>").append(greetingHtml).append("</p>");
		inner.append("<p style='margin:0 0 20px 0;font-size:14px;line-height:1.5;color:#475569;'>").append(introHtml)
				.append("</p>");
		inner.append(ticketSummaryHtml);
		if (StringUtils.hasText(extraSectionHtml)) {
			inner.append(extraSectionHtml);
		}
		inner.append("<p style='margin:0 0 10px 0;font-size:13px;font-weight:600;color:").append(EMAIL_BRAND)
				.append(";'>").append(esc(sectionTitle)).append("</p>");
		inner.append(linesTableHtml);
		if (StringUtils.hasText(ctaLine)) {
			inner.append(
					"<p style='margin:20px 0 0 0;padding:14px 16px;background:#EFF6FF;border-left:4px solid ")
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
				+ "<td align='left' style='font-size:17px;font-weight:700;color:#ffffff;letter-spacing:0.2px;'>iShine Travel Desk</td>"
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

	private String styledRemarksBlock(String label, String remarks) {
		if (!StringUtils.hasText(remarks)) {
			return "";
		}
		return "<div style='margin:0 0 20px 0;padding:12px 16px;background:#FFFBEB;border:1px solid #FDE68A;border-radius:6px;'>"
				+ "<span style='display:block;font-size:12px;font-weight:600;color:#92400E;margin-bottom:4px;'>"
				+ esc(label) + "</span>"
				+ "<span style='font-size:14px;color:#78350F;line-height:1.5;'>" + esc(remarks.trim()) + "</span></div>";
	}

	private List<TravelDeskTicketLine> sortedLines(TravelDeskTicket ticket) {
		if (ticket == null || ticket.getLines() == null) {
			return new ArrayList<>();
		}
		return ticket.getLines().stream()
				.sorted(Comparator.comparing(TravelDeskTicketLine::getLineNo,
						Comparator.nullsLast(Comparator.naturalOrder())))
				.collect(Collectors.toList());
	}

	private boolean isHotelLine(TravelDeskTicketLine line) {
		String type = line != null ? line.getRequestType() : null;
		if (!StringUtils.hasText(type)) {
			return false;
		}
		String normalized = type.trim().toLowerCase(Locale.ROOT);
		return normalized.contains("hotel") && (normalized.contains("lodg") || normalized.contains("stay")
				|| normalized.contains("accommod"));
	}

	private String lineBadgeLabel(TravelDeskTicketLine line) {
		return isHotelLine(line) ? "Hotel" : "Flight";
	}

	private String routeLabel(TravelDeskTicketLine line) {
		if (isHotelLine(line)) {
			return fallback(line.getCity(), "Stay city not specified");
		}
		return fallback(line.getFromLocation(), "—") + " → " + fallback(line.getToLocation(), "—");
	}

	private String dateLabel(TravelDeskTicketLine line) {
		String from = formatTs(line != null ? line.getFromDate() : null);
		String to = formatTs(line != null ? line.getToDate() : null);
		return StringUtils.hasText(to) ? from + " to " + to : from;
	}

	private String modeOrCategoryLabel(TravelDeskTicketLine line) {
		if (isHotelLine(line)) {
			return fallback(line.getHotelCategory(), "—") + " / " + fallback(line.getHotelSubCategory(), "—");
		}
		return fallback(line.getTravelMode(), "—") + " / " + fallback(line.getTravelClass(), "—");
	}

	private String formatAmount(java.math.BigDecimal amount) {
		if (amount == null) {
			return "—";
		}
		return "Rs. " + String.format(Locale.ENGLISH, "%,.2f", amount);
	}

	private String lineStatusLabel(TravelDeskTicketLine line) {
		if (line == null || !StringUtils.hasText(line.getLineStatus())) {
			return "—";
		}
		switch (line.getLineStatus()) {
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
			return line.getLineStatus();
		}
	}

	private String formatTs(Timestamp ts) {
		if (ts == null) {
			return "—";
		}
		return new SimpleDateFormat("dd/MM/yyyy").format(ts);
	}

	private String employeeNameByEmpId(Long empId) {
		if (empId == null) {
			return null;
		}
		Employee e = employeeRepository.findByEmpId(empId);
		return e != null && StringUtils.hasText(e.getName()) ? e.getName().trim() : null;
	}

	private String employeeEmailByEmpId(Long empId) {
		if (empId == null) {
			return null;
		}
		Employee e = employeeRepository.findByEmpId(empId);
		return e != null && StringUtils.hasText(e.getEmail()) ? e.getEmail().trim() : null;
	}

	private String lookupEmployeeNameByEmail(String email) {
		if (!StringUtils.hasText(email)) {
			return null;
		}
		String normalized = email.trim();
		Employee e = employeeRepository.findByEmail(normalized);
		if (e == null && normalized.contains("@")) {
			e = employeeRepository.findFirstByEmailLocalPartIgnoreCase(
					normalized.substring(0, normalized.indexOf('@') + 1));
		}
		return e != null && StringUtils.hasText(e.getName()) ? e.getName().trim() : normalized;
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private String normEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private String fallback(String value, String fallback) {
		return StringUtils.hasText(value) ? value.trim() : fallback;
	}
}
