package com.apmosys.employeeportal.service;

import java.util.List;

import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.polink.BoardingGroupDto;
import com.apmosys.employeeportal.dto.polink.PoLinkEmailContentDto;
import com.apmosys.employeeportal.dto.polink.PreviousRequirementDto;
import com.apmosys.employeeportal.dto.polink.RequirementDto;
import com.apmosys.employeeportal.dto.polink.ResourceImpactDto;
import com.apmosys.employeeportal.model.Project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Renders the PO link success email (inline HTML, email-client safe).
 */
@Service
@RequiredArgsConstructor
public class PoLinkImpactEmailBuilder {

	private static final String C_HEADER = "#2F80ED";
	private static final String C_MUTED = "#6b7280";
	private static final String C_CARD_BG = "#f8fafc";
	private static final String C_BORDER = "#e2e8f0";

	private final PoLinkEmailDataService poLinkEmailDataService;

	public String buildEmailBody(Project primaryProject, IshineLinkProjectDto dto) {
		PoLinkEmailContentDto data = poLinkEmailDataService.buildEmailContent(primaryProject, dto);
		return render(data);
	}

	private String render(PoLinkEmailContentDto d) {
		StringBuilder h = new StringBuilder();
		h.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>PO Linking Completed</title></head>");
		h.append("<body style=\"margin:0;padding:0;background:#f1f5f9;\">");
		h.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f1f5f9;\">");
		h.append("<tr><td align=\"center\" style=\"padding:24px 12px;\">");
		h.append("<table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;width:100%;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#1e293b;\">");

		// Header (keep styling)
		h.append("<tr><td style=\"background:").append(C_HEADER)
				.append(";color:#fff;padding:20px 24px;border-radius:8px 8px 0 0;\">");
		h.append("<div style=\"font-size:18px;font-weight:700;letter-spacing:0.3px;\">PO Linking Completed</div>");
		h.append("<div style=\"font-size:13px;opacity:0.95;margin-top:6px;\">")
				.append(esc(d.getProjectDisplayName()))
				.append("</div></td></tr>");

		h.append("<tr><td style=\"background:#fff;border:1px solid ").append(C_BORDER)
				.append(";border-top:0;padding:0;\">");

		// Primary project + merged/removed (first body block)
		h.append(cardOpen());
		h.append("<div style=\"font-size:12px;font-weight:700;color:").append(C_MUTED)
				.append(";text-transform:uppercase;letter-spacing:0.06em;margin-bottom:6px;\">Primary Project</div>");
		h.append("<div style=\"font-size:16px;font-weight:600;margin-bottom:16px;\">")
				.append(esc(d.getProjectDisplayName()))
				.append("</div>");

		h.append("<div style=\"font-size:12px;font-weight:700;color:").append(C_MUTED)
				.append(";text-transform:uppercase;letter-spacing:0.06em;margin-bottom:8px;\">Merged / Removed Project</div>");
		if (d.getMergedRemovedProjectLines() == null || d.getMergedRemovedProjectLines().isEmpty()) {
			h.append("<div style=\"color:").append(C_MUTED).append(";font-size:14px;\">None listed for this sync.</div>");
		} else {
			h.append("<ul style=\"margin:0;padding-left:18px;line-height:1.5;\">");
			for (String line : d.getMergedRemovedProjectLines()) {
				h.append("<li style=\"margin:0 0 6px 0;\">").append(esc(line)).append("</li>");
			}
			h.append("</ul>");
		}
		h.append(cardClose());

		// Plain-language resource note
		h.append(cardOpen());
		h.append("<p style=\"margin:0;line-height:1.5;color:#334155;\">")
				.append("Some resources may be moved to the updated PO if the same role exists in both old and new POs.")
				.append("</p>");
		h.append(cardClose());

		ResourceImpactDto imp = d.getResourceImpact();
		if (imp != null && imp.isProjectDisplayNameChanged()) {
			h.append(infoBanner("Project name in the request differed from the saved name: <b>"
					+ esc(imp.getPreviousProjectDisplayName()) + "</b> &rarr; <b>"
					+ esc(imp.getCurrentProjectDisplayName()) + "</b>."));
		}

		if (d.isShowNoRequirementDataBanner()) {
			h.append(warningBanner(d.getNoRequirementBannerNote()));
		}

		// What changed
		h.append(sectionTitle("What changed"));
		h.append("<div style=\"padding:0 24px 20px 24px;\">")
				.append(d.getWhatChangedHtml() != null ? d.getWhatChangedHtml() : "")
				.append("</div>");

		// PO hierarchy
		h.append(sectionTitle("PO hierarchy"));
		h.append("<div style=\"padding:0 24px 20px 24px;\">");
		h.append("<div style=\"background:").append(C_CARD_BG).append(";border:1px solid ")
				.append(C_BORDER).append(";border-radius:8px;padding:14px 16px;\">");
		h.append(d.getHierarchyTreeHtml() != null ? d.getHierarchyTreeHtml() : "");
		h.append("</div></div>");

		// Previous requirements
		h.append(sectionTitle("Previous PO requirements (removed / merged context)"));
		h.append("<div style=\"padding:0 16px 20px 16px;\">");
		h.append(previousRequirementsTable(d.getPreviousRequirementRows()));
		h.append("</div>");

		// Current requirements
		h.append(sectionTitle("Current PO requirements"));
		h.append("<div style=\"padding:0 24px 4px 24px;font-size:14px;font-weight:600;color:#334155;\">Project: ")
				.append(esc(d.getProjectDisplayName())).append("</div>");
		h.append("<div style=\"padding:0 16px 20px 16px;\">");
		h.append(currentRequirementsTable(d.getCurrentRequirementRows()));
		h.append("</div>");

		// Boarding
		h.append(sectionTitle("Boarding snapshot"));
		h.append("<div style=\"padding:0 24px 20px 24px;\">");
		h.append(renderBoarding(d.getBoardingGroups()));
		h.append("</div>");

		// Action
		h.append(sectionTitle("What you should do"));
		h.append("<div style=\"padding:0 24px 24px 24px;\">");
		h.append("<ul style=\"margin:0;padding-left:18px;line-height:1.55;color:#334155;\">");
		h.append("<li>Check that resources are assigned to the correct PO.</li>");
		h.append("<li>Fix any missing or extra resource allocation.</li>");
		h.append("<li>Update team mapping if required.</li>");
		h.append("</ul></div>");

		h.append("<div style=\"border-top:1px solid ").append(C_BORDER)
				.append(";padding:16px 24px;font-size:12px;color:").append(C_MUTED).append(";\">");
		h.append("This message was generated automatically after PO link sync.</div>");

		h.append("</td></tr></table></td></tr></table></body></html>");
		return h.toString();
	}

	private static String cardOpen() {
		return "<div style=\"margin:16px 16px 0 16px;padding:16px 18px;border:1px solid "
				+ C_BORDER + ";border-radius:8px;background:#fff;\">";
	}

	private static String cardClose() {
		return "</div>";
	}

	private static String sectionTitle(String text) {
		return "<div style=\"padding:20px 24px 8px 24px;\">"
				+ "<span style=\"font-size:15px;font-weight:700;color:#0f172a;\">" + esc(text) + "</span></div>";
	}

	private static String warningBanner(String note) {
		return "<div style=\"margin:0 16px 16px 16px;padding:12px 14px;border-radius:8px;"
				+ "background:#fff7ed;border:1px solid #fed7aa;color:#9a3412;font-size:13px;line-height:1.5;\">"
				+ "<b>Please note.</b> " + (note != null ? esc(note) : "") + "</div>";
	}

	private static String infoBanner(String htmlInner) {
		return "<div style=\"margin:0 16px 16px 16px;padding:12px 14px;border-radius:8px;"
				+ "background:#eff6ff;border:1px solid #bfdbfe;color:#1e3a5f;font-size:13px;line-height:1.5;\">"
				+ htmlInner + "</div>";
	}

	private String previousRequirementsTable(List<PreviousRequirementDto> rows) {
		StringBuilder t = new StringBuilder();
		t.append(tableStart());
		t.append(headerRow(new String[] { "Project", "PO", "Role", "Required" }));
		if (rows == null || rows.isEmpty()) {
			t.append(rowEmpty(4, "No previous PO requirement rows (nothing removed in payload or no data)."));
		} else {
			int i = 0;
			String lastProj = null;
			for (PreviousRequirementDto r : rows) {
				if (r == null) {
					continue;
				}
				String proj = r.getProjectName() != null ? r.getProjectName() : "";
				if (lastProj == null || !lastProj.equals(proj)) {
					t.append("<tr><td colspan=\"4\" style=\"padding:10px 8px;background:#e2e8f0;font-weight:700;font-size:13px;color:#0f172a;border-bottom:1px solid ")
							.append(C_BORDER).append(";\">Project: ")
							.append(esc(proj.isEmpty() ? "--" : proj)).append("</td></tr>");
					lastProj = proj;
				}
				String bg = zebra(i++);
				t.append("<tr style=\"background:").append(bg).append(";\">");
				t.append(td(esc(r.getProjectName())));
				t.append(td(esc(r.getPoLabel())));
				t.append(td(esc(r.getRoleLabel())));
				t.append(td(String.valueOf(r.getRequiredCount())));
				t.append("</tr>");
			}
		}
		t.append("</table>");
		return t.toString();
	}

	private String currentRequirementsTable(List<RequirementDto> rows) {
		StringBuilder t = new StringBuilder();
		t.append(tableStart());
		t.append(headerRow(new String[] { "PO", "Role", "Required", "Mapped", "Status" }));
		if (rows == null || rows.isEmpty()) {
			t.append(rowEmpty(5, "No current PO rows."));
		} else {
			int i = 0;
			for (RequirementDto r : rows) {
				if (r == null) {
					continue;
				}
				String bg = zebra(i++);
				if (r.isExpiredRow()) {
					bg = "#fef2f2";
				}
				t.append("<tr style=\"background:").append(bg).append(";\">");
				t.append(td(esc(r.getPoLabel())));
				t.append(td(esc(r.getRoleAndDepartment())));
				t.append(td(String.valueOf(r.getRequiredCount())));
				t.append(td(String.valueOf(r.getMappedCount())));
				String poStat = r.getPoLifecycleStatus() != null ? r.getPoLifecycleStatus() : "--";
				String poStatColor = poStatusColumnColor(poStat, r.isExpiredRow());
				t.append("<td style=\"padding:10px 8px;font-size:13px;color:").append(poStatColor)
						.append(";font-weight:600;\">").append(esc(poStat)).append("</td>");
				t.append("</tr>");
			}
		}
		t.append("</table>");
		return t.toString();
	}

	private String renderBoarding(List<BoardingGroupDto> groups) {
		if (groups == null || groups.isEmpty()) {
			return "<span style=\"color:" + C_MUTED + ";font-style:italic;\">No boarding groups in scope for this snapshot.</span>";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
		for (BoardingGroupDto g : groups) {
			if (g == null) {
				continue;
			}
			String border = g.isHighlightGroup() ? "border-left:3px solid #dc2626;" : "";
			sb.append("<tr><td style=\"padding:12px 0 14px 0;border-bottom:1px solid ").append(C_BORDER)
					.append(";").append(border).append("\">");
			sb.append("<div style=\"font-weight:700;margin-bottom:6px;\">")
					.append(esc(g.getPoLabel()))
					.append(" <span style=\"color:").append(C_MUTED).append(";font-weight:600;\">(")
					.append(esc(g.getPoStateLabel())).append(")</span></div>");
			if (g.getMemberLines() != null && !g.getMemberLines().isEmpty()) {
				sb.append("<ul style=\"margin:0;padding-left:20px;line-height:1.5;\">");
				for (String m : g.getMemberLines()) {
					String liColor = g.isHighlightGroup() ? "#b91c1c" : "#15803d";
					sb.append("<li style=\"color:").append(liColor).append(";font-size:13px;\">")
							.append(esc(m)).append("</li>");
				}
				sb.append("</ul>");
			}
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private static String tableStart() {
		return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border:1px solid "
				+ C_BORDER + ";\">";
	}

	private String headerRow(String[] labels) {
		StringBuilder r = new StringBuilder();
		r.append("<tr style=\"background:#f1f5f9;\">");
		for (String th : labels) {
			r.append("<th style=\"text-align:left;padding:10px 8px;font-size:12px;font-weight:700;color:#475569;border-bottom:1px solid ")
					.append(C_BORDER).append(";\">").append(esc(th)).append("</th>");
		}
		r.append("</tr>");
		return r.toString();
	}

	private String rowEmpty(int cols, String msg) {
		return "<tr><td colspan=\"" + cols + "\" style=\"padding:12px 8px;font-size:13px;color:#64748b;\">"
				+ esc(msg) + "</td></tr>";
	}

	private static String zebra(int i) {
		return (i % 2 == 0) ? "#fff" : "#f8fafc";
	}

	private static String td(String s) {
		return "<td style=\"padding:10px 8px;font-size:13px;border-bottom:1px solid " + C_BORDER + ";\">" + s + "</td>";
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
