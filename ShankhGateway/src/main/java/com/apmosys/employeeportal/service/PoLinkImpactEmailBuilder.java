package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.polink.BoardingTableRowDto;
import com.apmosys.employeeportal.dto.polink.PoLinkEmailContentDto;
import com.apmosys.employeeportal.dto.polink.PreviousPoRequirementsBlockDto;
import com.apmosys.employeeportal.dto.polink.PreviousRequirementDto;
import com.apmosys.employeeportal.dto.polink.RequirementDto;
import com.apmosys.employeeportal.dto.polink.ResourceImpactDto;
import com.apmosys.employeeportal.model.Project;

import lombok.RequiredArgsConstructor;

/**
 * Renders the PO link success email using table-only structure and inline CSS.
 */
@Service
@RequiredArgsConstructor
public class PoLinkImpactEmailBuilder {

	private static final Logger log = LoggerFactory.getLogger(PoLinkImpactEmailBuilder.class);

	private static final String C_PRIMARY = "#2F80ED";
	private static final String C_BG = "#f4f6f8";
	private static final String C_BANNER_BG = "#E8F1FD";
	private static final String C_SOFT_BG = "#F4F8FE";
	private static final String C_INNER_HL = "#E8F1FD";
	private static final String C_TABLE_HEAD_SOFT = "#F4F8FE";
	private static final String C_MUTED = "#6b7280";
	private static final String C_BORDER = "#e2e8f0";
	private static final String FONT = "Segoe UI, Arial, Helvetica, sans-serif";

	private final PoLinkEmailDataService poLinkEmailDataService;

	public String buildEmailBody(Project primaryProject, IshineLinkProjectDto dto) {
		PoLinkEmailContentDto data = poLinkEmailDataService.buildEmailContent(primaryProject, dto);
		return render(data);
	}

	private String render(PoLinkEmailContentDto d) {
		StringBuilder h = new StringBuilder(12_000);
		h.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>PO Linking Completed</title></head><body>");

		// Outer container (same pattern as renew; only theme differs)
		h.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f6f8; padding:20px 0; font-family:")
				.append(FONT).append(";\">");
		h.append("<tr><td align=\"center\" style=\"font-family:").append(FONT).append(";\">");
		h.append("<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff; border-radius:10px; overflow:hidden; font-family:")
				.append(FONT).append(";\">");

		// Header gradient (NEW REQUIREMENT)
		h.append("<tr><td style=\"background: linear-gradient(90deg, #DDE2ED 0%, #F2F4F9 100%);")
				.append(" color:#2F80ED; padding:16px 20px; font-size:18px; font-weight:600; text-align:center; font-family:")
				.append(FONT).append(";\">");
		h.append("PO Linking Completed");
		h.append("</td></tr>");

		// Subheader line (keeps prior header content)
		h.append("<tr><td style=\"padding:12px 20px; background: linear-gradient(90deg, #DDE2ED 0%, #F2F4F9 100%);").append(C_BANNER_BG)
				.append("; font-family:").append(FONT).append("; font-size:14px; color:#555; text-align:center;\">");
		h.append("<strong>Primary Project:</strong> ").append(esc(d.getProjectDisplayName()));
		h.append("</td></tr>");

		// Card: Inactivated Projects (+ optional info note)
		h.append(cardRowOpen());
		h.append(cardHeader("Inactivated Projects"));
		List<String> inactive = d.getMergedRemovedProjectLines();
		if (inactive == null || inactive.isEmpty()) {
			h.append(subText("None listed for this sync."));
		} else {
			h.append(bullets(inactive));
		}
		ResourceImpactDto imp = d.getResourceImpact();
		if (imp != null && imp.isProjectDisplayNameChanged()) {
			h.append(spacerRow(10));
			h.append(infoBox("Project name in the request differed from the saved name: <b>"
					+ esc(imp.getPreviousProjectDisplayName()) + "</b> &rarr; <b>"
					+ esc(imp.getCurrentProjectDisplayName()) + "</b>."));
		}
		h.append(cardRowClose());

		// Card: What Changed
		h.append(cardRowOpen());
		h.append(cardHeader("What Changed"));
		h.append(innerHighlightOpen());
		h.append(d.getWhatChangedHtml() != null ? d.getWhatChangedHtml() : "");
		h.append(innerHighlightClose());
		h.append(cardRowClose());

		// Card: Previous PO Requirements (non-monitoring only)
		if (d.isIncludePreviousPoRequirementsSection()) {
			h.append(cardRowOpen());
			h.append(cardHeader("Previous PO Requirements"));
			h.append(renderPreviousRequirementBlocks(d.getPreviousRequirementBlocks()));
			h.append(cardRowClose());
		}

		// Card: Current PO Requirements
		h.append(cardRowOpen());
		h.append(cardHeader("Current PO Requirements"));
		h.append(subText("Project: " + esc(d.getProjectDisplayName())));
		h.append(requirementsTable(d.getCurrentRequirementRows()));
		h.append(cardRowClose());

		// Card: Boarding Snapshot (renew-style grouped cards)
		h.append(cardRowOpen());
		h.append(cardHeader("Boarding Snapshot"));
		h.append(boardingSnapshotCards(d.getBoardingTableRows()));
		h.append(cardRowClose());

		// Card: Please Note
		h.append(cardRowOpen());
		h.append(cardHeader("Please Note:"));
		h.append(bullets(List.of(
				"Check that resources are assigned to the correct PO",
				"Fix any missing or extra allocation",
				"Update team mapping if required",
				"Some resources may be moved to the updated PO if the same role exists in both old and new POs.")));
		h.append(cardRowClose());

		// Footer (KEEP AS IS) - centered (NEW REQUIREMENT)
		h.append("<tr><td style=\"padding:12px 20px; font-size:12px; color:#777; text-align:center; font-family:")
				.append(FONT).append(";\">");
		h.append("This message was generated automatically after PO link sync.");
		h.append("</td></tr>");

		h.append("</table></td></tr></table></body></html>");

		String html = h.toString();
		// 9) DEBUG STEP (MANDATORY) - print final HTML
		log.info("PO link success email HTML: {}", html);
		return html;
	}

	private static String cardRowOpen() {
		// Card only (no outer soft-blue wrapper/border effect)
		return "<tr><td style=\"padding:0 0 12px 0; font-family:" + FONT + ";\">"
				+ "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff; border-left:4px solid " + C_PRIMARY
				+ "; border-radius:8px; border:1px solid " + C_BORDER + "; font-family:" + FONT
				+ ";\"><tr><td style=\"padding:12px; font-family:" + FONT + ";\">";
	}

	private static String cardRowClose() {
		return "</td></tr></table></td></tr>";
	}

	private static String cardHeader(String text) {
		return "<div style=\"font-size:16px; font-weight:600; color:" + C_PRIMARY + "; margin-bottom:10px; font-family:" + FONT + ";\">"
				+ esc(text) + "</div>";
	}

	private static String subText(String textEsc) {
		return "<div style=\"font-size:13px; color:#555; font-family:" + FONT + ";\">" + textEsc + "</div>";
	}

	private static String spacerRow(int px) {
		return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; font-family:" + FONT + ";\"><tr><td style=\"height:"
				+ px + "px; line-height:" + px + "px; font-size:1px;\">&nbsp;</td></tr></table>";
	}

	private static String infoBox(String htmlInner) {
		return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; margin-top:10px; font-family:" + FONT + ";\"><tr><td style=\"background:" + C_INNER_HL + "; border:1px solid #bfdbfe; color:#1e3a5f; padding:10px 12px; font-size:13px; font-family:"
				+ FONT + ";\">" + htmlInner + "</td></tr></table>";
	}

	private static String innerHighlightOpen() {
		return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; margin-top:6px; font-family:" + FONT
				+ ";\"><tr><td style=\"background:" + C_INNER_HL + "; padding:10px; border-radius:6px; font-size:13px; font-family:" + FONT
				+ "; color:#555;\">";
	}

	private static String innerHighlightClose() {
		return "</td></tr></table>";
	}

	private static String bullets(List<String> lines) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; margin-top:10px; font-family:")
				.append(FONT).append(";\">");
		for (String s : lines) {
			if (s == null || s.trim().isEmpty()) {
				continue;
			}
			sb.append("<tr>");
			sb.append("<td width=\"16\" valign=\"top\" style=\"padding:2px 0; font-size:14px; color:#334155; font-family:")
					.append(FONT).append(";\">&bull;</td>");
			sb.append("<td valign=\"top\" style=\"padding:2px 0; font-size:13px; color:#555; font-family:")
					.append(FONT).append(";\">").append(esc(s)).append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	// Previous requirements blocks (project label outside table)
	private String renderPreviousRequirementBlocks(List<PreviousPoRequirementsBlockDto> blocks) {
		if (blocks == null || blocks.isEmpty()) {
			return subText("No previous PO requirement rows.");
		}
		StringBuilder sb = new StringBuilder();
		for (PreviousPoRequirementsBlockDto b : blocks) {
			if (b == null) {
				continue;
			}
			sb.append(subText("Project: " + esc(b.getProjectDisplayName() != null ? b.getProjectDisplayName() : "--")));
			sb.append(simpleTableStart());
			sb.append(tableHeaderRow(new String[] { "PO", "Role", "Required" }));
			List<PreviousRequirementDto> rows = b.getRows();
			if (rows == null || rows.isEmpty()) {
				sb.append(tableEmptyRow(3, "No rows."));
			} else {
				for (PreviousRequirementDto r : rows) {
					if (r == null) {
						continue;
					}
					sb.append("<tr>");
					sb.append(td(esc(r.getPoLabel())));
					sb.append(td(esc(r.getRoleLabel())));
					sb.append(td(String.valueOf(r.getRequiredCount())));
					sb.append("</tr>");
				}
			}
			sb.append("</table>");
			sb.append(spacerRow(10));
		}
		return sb.toString();
	}

	// 4) TABLE STYLING (MANDATORY FIX)
	private static String simpleTableStart() {
		return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; font-size:13px; margin-top:10px; font-family:" + FONT + ";\">";
	}

	private static String tableHeaderRow(String[] labels) {
		StringBuilder r = new StringBuilder();
		r.append("<tr style=\"background:").append(C_TABLE_HEAD_SOFT).append(";\">");
		for (String th : labels) {
			r.append("<th style=\"text-align:left; padding:10px; font-size:13px; border:1px solid ")
					.append(C_BORDER).append("; font-family:").append(FONT)
					.append("; font-weight:600; color:#333;\">").append(esc(th)).append("</th>");
		}
		r.append("</tr>");
		return r.toString();
	}

	private static String tableEmptyRow(int cols, String msg) {
		return "<tr><td colspan=\"" + cols + "\" style=\"padding:8px; font-size:13px; border:1px solid " + C_BORDER
				+ "; font-family:" + FONT + "; color:#555;\">" + esc(msg) + "</td></tr>";
	}

	private static String td(String s) {
		return "<td style=\"padding:10px; font-size:13px; border:1px solid " + C_BORDER + "; font-family:" + FONT + "; color:#333;\">" + s + "</td>";
	}

	private String requirementsTable(List<RequirementDto> rows) {
		StringBuilder t = new StringBuilder();
		t.append(simpleTableStart());
		t.append(tableHeaderRow(new String[] { "PO", "Role", "Required", "Mapped", "Staffing", "Status" }));
		if (rows == null || rows.isEmpty()) {
			t.append(tableEmptyRow(6, "No current PO rows."));
		} else {
			for (RequirementDto r : rows) {
				if (r == null) {
					continue;
				}
				t.append("<tr>");
				t.append(td(esc(r.getPoLabel())));
				t.append(td(esc(r.getRoleAndDepartment())));
				t.append(td(String.valueOf(r.getRequiredCount())));
				t.append(td(String.valueOf(r.getMappedCount())));
				String staff = r.getStatusText() != null ? r.getStatusText() : "--";
				String staffColor = r.getStatusColor() != null ? r.getStatusColor() : C_MUTED;
				t.append("<td style=\"padding:8px; font-size:13px; border:1px solid ").append(C_BORDER)
						.append("; font-family:").append(FONT).append("; color:").append(esc(staffColor))
						.append("; font-weight:600;\">").append(esc(staff)).append("</td>");
				String poStat = r.getPoLifecycleStatus() != null ? r.getPoLifecycleStatus() : "--";
				String poStatColor = poStatusColumnColor(poStat, r.isExpiredRow());
				t.append("<td style=\"padding:8px; font-size:13px; border:1px solid ").append(C_BORDER)
						.append("; font-family:").append(FONT).append("; color:").append(poStatColor)
						.append("; font-weight:600;\">").append(esc(poStat)).append("</td>");
				t.append("</tr>");
			}
		}
		t.append("</table>");
		return t.toString();
	}

	private String boardingSnapshotCards(List<BoardingTableRowDto> rows) {
		if (rows == null || rows.isEmpty()) {
			return subText("No boarding rows in scope for this snapshot.");
		}

		class Group {
			final String po;
			final String status;
			final List<BoardingTableRowDto> members = new ArrayList<>();
			Group(String po, String status) { this.po = po; this.status = status; }
		}

		List<Group> groups = new ArrayList<>();
		for (BoardingTableRowDto r : rows) {
			if (r == null) {
				continue;
			}
			String po = r.getPoLabel() != null ? r.getPoLabel() : "PO";
			String st = r.getStatus() != null ? r.getStatus() : "--";
			Group g = null;
			for (Group x : groups) {
				if (x.po.equals(po) && x.status.equals(st)) {
					g = x;
					break;
				}
			}
			if (g == null) {
				g = new Group(po, st);
				groups.add(g);
			}
			g.members.add(r);
		}

		StringBuilder sb = new StringBuilder();
		for (Group g : groups) {
			String color = poStatusColumnColor(g.status, "Expired".equals(g.status));
			sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; margin-top:10px; font-family:")
					.append(FONT).append(";\">");
			sb.append("<tr><td style=\"border-left:4px solid ").append(C_PRIMARY)
					.append("; background:#ffffff; border-radius:8px; border:1px solid ").append(C_BORDER)
					.append("; padding:12px; font-family:").append(FONT).append(";\">");

			sb.append("<div style=\"font-weight:600; color:").append(C_PRIMARY).append("; font-family:").append(FONT).append(";\">")
					.append(esc(g.po)).append(" <span style=\"color:").append(color).append("; font-weight:600;\">(")
					.append(esc(g.status)).append(")</span></div>");
			for (BoardingTableRowDto r : g.members) {
				String name = r.getResourceName() != null ? r.getResourceName() : "-";
				String team = r.getTeam() != null ? r.getTeam() : "-";
				String remark = r.getRemark() != null ? r.getRemark() : "-";
				String line;
				if ("-".equals(name) && "-".equals(team)) {
					line = "No resources mapped";
				} else {
					line = esc(name) + " (" + esc(team) + ")";
				}
				sb.append("<div style=\"margin-top:6px; font-size:13px; color:#555; font-family:").append(FONT).append(";\">")
						.append(line);
				if (remark != null && !remark.isEmpty() && !"-".equals(remark) && !"No resources mapped".equalsIgnoreCase(remark)) {
					sb.append(" <span style=\"color:#777;\">&mdash; ").append(esc(remark)).append("</span>");
				}
				sb.append("</div>");
			}
			sb.append("</td></tr></table>");
		}
		return sb.toString();
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String poStatusColumnColor(String poStat, boolean expiredRow) {
		if (poStat == null || poStat.isEmpty() || "--".equals(poStat)) {
			return C_MUTED;
		}
		if ("Expired".equals(poStat) || expiredRow) {
			return "#b91c1c";
		}
		if ("Active".equals(poStat)) {
			return "#15803d";
		}
		if ("Future".equals(poStat)) {
			return "#64748b";
		}
		if ("Unknown".equals(poStat)) {
			return "#6b7280";
		}
		return C_MUTED;
	}
}
