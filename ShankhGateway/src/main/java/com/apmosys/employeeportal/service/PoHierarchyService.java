package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.PoHierarchyNodeDTO;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;

import lombok.RequiredArgsConstructor;

/**
 * Builds a PO hierarchy tree for email visualization using {@code prevPO} / {@code nextPO}.
 * Cycle-safe; supports multiple roots.
 */
@Service
@RequiredArgsConstructor
public class PoHierarchyService {

	private static final Logger log = LoggerFactory.getLogger(PoHierarchyService.class);

	private static final DateTimeFormatter PO_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

	private final ProjectPoDetailsRepository projectPoDetailsRepository;

	public List<PoHierarchyNodeDTO> buildPoTreeForProject(
			Integer projectId,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds,
			Set<Long> modifiedPoIds) {

		if (projectId == null) {
			return Collections.emptyList();
		}

		List<ProjectPoDetails> rows = projectPoDetailsRepository.findByProjectId(projectId);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Long, PoHierarchyNodeDTO> nodeByPoId = new HashMap<>();
		for (ProjectPoDetails r : rows) {
			if (r == null || r.getPoId() == null) {
				continue;
			}
			PoHierarchyNodeDTO n = new PoHierarchyNodeDTO();
			n.setPoId(r.getPoId());
			n.setPoNo(r.getPoNo());
			n.setActive(r.isActive());
			n.setDeleted(deletedPoIds != null && deletedPoIds.contains(r.getPoId()));
			n.setPrimary(primaryPoIds != null && primaryPoIds.contains(r.getPoId()));
			n.setModified(modifiedPoIds != null && modifiedPoIds.contains(r.getPoId()));
			nodeByPoId.put(r.getPoId(), n);
		}

		Map<Long, Set<Long>> children = new HashMap<>();
		Map<Long, Long> parent = new HashMap<>();

		for (ProjectPoDetails r : rows) {
			if (r == null || r.getPoId() == null) {
				continue;
			}
			Long poId = r.getPoId();
			Long prev = r.getPrevPO();
			Long next = r.getNextPO();

			if (prev != null && nodeByPoId.containsKey(prev)) {
				children.computeIfAbsent(prev, k -> new HashSet<>()).add(poId);
				parent.putIfAbsent(poId, prev);
			}
			if (next != null && nodeByPoId.containsKey(next)) {
				children.computeIfAbsent(poId, k -> new HashSet<>()).add(next);
				parent.putIfAbsent(next, poId);
			}
		}

		List<PoHierarchyNodeDTO> roots = new ArrayList<>();
		for (Long poId : nodeByPoId.keySet()) {
			if (!parent.containsKey(poId)) {
				roots.add(nodeByPoId.get(poId));
			}
		}

		if (roots.isEmpty()) {
			log.warn("PO hierarchy: no roots found (cycle suspected) projectId={}", projectId);
			List<Long> sorted = new ArrayList<>(nodeByPoId.keySet());
			sorted.sort(Comparator.naturalOrder());
			roots.add(nodeByPoId.get(sorted.get(0)));
		}

		for (PoHierarchyNodeDTO root : roots) {
			attachChildren(root, nodeByPoId, children);
		}

		roots.sort(Comparator
				.comparing((PoHierarchyNodeDTO n) -> n.getPoNo() == null ? "" : n.getPoNo())
				.thenComparing(n -> n.getPoId() == null ? 0L : n.getPoId()));

		return roots;
	}

	private static void attachChildren(
			PoHierarchyNodeDTO root,
			Map<Long, PoHierarchyNodeDTO> nodeByPoId,
			Map<Long, Set<Long>> children) {

		if (root == null || root.getPoId() == null) {
			return;
		}

		Set<Long> globalVisited = new HashSet<>();
		Deque<PoHierarchyNodeDTO> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			PoHierarchyNodeDTO current = stack.pop();
			Long id = current.getPoId();
			if (id == null || !globalVisited.add(id)) {
				continue;
			}

			Set<Long> childIds = children.getOrDefault(id, Collections.emptySet());
			if (childIds.isEmpty()) {
				continue;
			}

			List<PoHierarchyNodeDTO> childNodes = new ArrayList<>();
			for (Long cid : childIds) {
				PoHierarchyNodeDTO cn = nodeByPoId.get(cid);
				if (cn != null && !Objects.equals(cn.getPoId(), current.getPoId())) {
					childNodes.add(cn);
				}
			}

			childNodes.sort(Comparator
					.comparing((PoHierarchyNodeDTO n) -> n.getPoNo() == null ? "" : n.getPoNo())
					.thenComparing(n -> n.getPoId() == null ? 0L : n.getPoId()));

			current.getChildren().clear();
			current.getChildren().addAll(childNodes);

			for (int i = childNodes.size() - 1; i >= 0; i--) {
				PoHierarchyNodeDTO cn = childNodes.get(i);
				if (cn != null && cn.getPoId() != null && !globalVisited.contains(cn.getPoId())) {
					stack.push(cn);
				}
			}
		}
	}

	/**
	 * Plain, email-safe tree: PO dates, lifecycle (Active / Expired / etc.), ASCII branches.
	 */
	public String formatBusinessPlainTree(
			String primaryProjectDisplayName,
			Integer projectId,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds,
			Set<Long> modifiedPoIds) {

		String proj = primaryProjectDisplayName != null ? primaryProjectDisplayName : "";
		if (projectId == null) {
			return "<div style=\"font-family:Consolas,Monaco,monospace;font-size:13px;line-height:1.55;\">"
					+ "Primary Project: " + escape(proj) + "<br/>"
					+ "<span style=\"color:#6b7280\">No project id for hierarchy.</span></div>";
		}

		List<ProjectPoDetails> rows = projectPoDetailsRepository.findByProjectId(projectId);
		if (rows == null || rows.isEmpty()) {
			return "<div style=\"font-family:Consolas,Monaco,monospace;font-size:13px;line-height:1.55;\">"
					+ "Primary Project: " + escape(proj) + "<br/>"
					+ "<span style=\"color:#6b7280\">No PO rows found for this project.</span></div>";
		}

		Map<Long, ProjectPoDetails> byPo = new HashMap<>();
		for (ProjectPoDetails r : rows) {
			if (r != null && r.getPoId() != null) {
				byPo.put(r.getPoId(), r);
			}
		}

		List<PoHierarchyNodeDTO> roots = buildPoTreeForProject(projectId, deletedPoIds, primaryPoIds, modifiedPoIds);
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"font-size:13px;line-height:1.6;color:#0f172a;\">");
		sb.append("Primary Project: ").append(escape(proj)).append("<br/><br/>");
		for (int i = 0; i < roots.size(); i++) {
			appendAsciiTreeForRoot(sb, roots.get(i), byPo, deletedPoIds, primaryPoIds, new HashSet<>());
			if (i < roots.size() - 1) {
				sb.append("<br/>");
			}
		}
		sb.append("</div>");
		return sb.toString();
	}

	/** Email-safe: spaces, hyphens, and ASCII arrows only (no box-drawing Unicode). */
	private void appendAsciiTreeForRoot(
			StringBuilder sb,
			PoHierarchyNodeDTO root,
			Map<Long, ProjectPoDetails> byPo,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds,
			Set<Long> visiting) {

		if (root == null || root.getPoId() == null) {
			return;
		}
		ProjectPoDetails r = byPo.get(root.getPoId());
		String poNo = r != null && r.getPoNo() != null ? r.getPoNo() : "PO";
		String d0 = formatPoDate(r != null ? r.getPoStartDate() : null);
		String d1 = formatPoDate(r != null ? r.getPoEndDate() : null);
		String status = deriveLifecycleLabel(r, root.getPoId(), deletedPoIds, primaryPoIds);
		sb.append("PO: ").append(escape(poNo)).append(" (").append(escape(d0)).append(" -> ")
				.append(escape(d1)).append(") [").append(lifecycleStatusHtml(status)).append("]<br/>");

		List<PoHierarchyNodeDTO> ch = root.getChildren();
		if (ch == null || ch.isEmpty()) {
			return;
		}
		sb.append("&nbsp;&nbsp;Linked:<br/>");
		for (PoHierarchyNodeDTO c : ch) {
			appendAsciiChild(sb, c, byPo, deletedPoIds, primaryPoIds, visiting, 2);
		}
	}

	private void appendAsciiChild(
			StringBuilder sb,
			PoHierarchyNodeDTO node,
			Map<Long, ProjectPoDetails> byPo,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds,
			Set<Long> visiting,
			int indentSteps) {

		if (node == null || node.getPoId() == null) {
			return;
		}
		Long poId = node.getPoId();
		if (!visiting.add(poId)) {
			sb.append(repeatNbsp(indentSteps)).append("- (cycle skipped)<br/>");
			return;
		}
		ProjectPoDetails r = byPo.get(poId);
		String poNo = r != null && r.getPoNo() != null ? r.getPoNo() : "PO";
		String d0 = formatPoDate(r != null ? r.getPoStartDate() : null);
		String d1 = formatPoDate(r != null ? r.getPoEndDate() : null);
		String status = deriveLifecycleLabel(r, poId, deletedPoIds, primaryPoIds);
		sb.append(repeatNbsp(indentSteps)).append("- ").append(escape(poNo)).append(" (")
				.append(escape(d0)).append(" -> ").append(escape(d1)).append(") [")
				.append(lifecycleStatusHtml(status)).append("]<br/>");

		List<PoHierarchyNodeDTO> ch = node.getChildren();
		if (ch != null) {
			for (PoHierarchyNodeDTO c : ch) {
				appendAsciiChild(sb, c, byPo, deletedPoIds, primaryPoIds, visiting, indentSteps + 2);
			}
		}
		visiting.remove(poId);
	}

	private static String repeatNbsp(int steps) {
		if (steps <= 0) {
			return "";
		}
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < steps; i++) {
			s.append("&nbsp;");
		}
		return s.toString();
	}

	private static String formatPoDate(LocalDateTime dt) {
		if (dt == null) {
			return "--";
		}
		return PO_DATE.format(dt.toLocalDate());
	}

	/** Email hierarchy: highlight Active lifecycle in green. */
	private static String lifecycleStatusHtml(String status) {
		String e = escape(status);
		if ("Active".equals(status)) {
			return "<span style=\"color:#15803d;font-weight:600;\">" + e + "</span>";
		}
		return e;
	}

	private static String deriveLifecycleLabel(
			ProjectPoDetails r,
			Long poId,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds) {

		LocalDate today = LocalDate.now();
		if (deletedPoIds != null && deletedPoIds.contains(poId)
				&& (primaryPoIds == null || !primaryPoIds.contains(poId))) {
			return "Removed";
		}
		if (r != null && !r.isActive()) {
			return "Inactive";
		}
		if (r != null && r.getPoEndDate() != null && r.getPoEndDate().toLocalDate().isBefore(today)) {
			return "Expired";
		}
		if (r != null && r.getPoStartDate() != null && r.getPoStartDate().toLocalDate().isAfter(today)) {
			return "Future";
		}
		return "Active";
	}

	public String formatTreeHtml(List<PoHierarchyNodeDTO> roots) {
		if (roots == null || roots.isEmpty()) {
			return "<i>No PO hierarchy available</i>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<div style='font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace;'>");
		for (PoHierarchyNodeDTO root : roots) {
			formatNode(sb, root, "", true, new HashSet<>());
		}
		sb.append("</div>");
		return sb.toString();
	}

	private static void formatNode(
			StringBuilder sb,
			PoHierarchyNodeDTO node,
			String prefix,
			boolean isLast,
			Set<Long> path) {

		if (node == null) {
			return;
		}
		Long id = node.getPoId();
		if (id != null && !path.add(id)) {
			sb.append(prefix).append(isLast ? "└── " : "├── ")
					.append(label(node))
					.append(" <span style='color:#b91c1c'>(cycle)</span><br/>");
			return;
		}

		sb.append(prefix).append(isLast ? "└── " : "├── ").append(label(node)).append("<br/>");

		List<PoHierarchyNodeDTO> children = node.getChildren();
		if (children == null || children.isEmpty()) {
			if (id != null) {
				path.remove(id);
			}
			return;
		}

		String childPrefix = prefix + (isLast ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "│&nbsp;&nbsp;&nbsp;");
		for (int i = 0; i < children.size(); i++) {
			formatNode(sb, children.get(i), childPrefix, i == children.size() - 1, path);
		}

		if (id != null) {
			path.remove(id);
		}
	}

	private static String label(PoHierarchyNodeDTO n) {
		String base = escape(n.getPoNo() != null ? n.getPoNo() : "PO");

		List<String> tags = new ArrayList<>();
		if (n.isPrimary()) {
			tags.add("<span style=\"color:#166534;font-weight:600;background:#dcfce7;padding:1px 6px;border-radius:4px;font-size:11px\">[PRIMARY]</span>");
		}
		if (n.isDeleted()) {
			tags.add("<span style=\"color:#991b1b;font-weight:600;background:#fee2e2;padding:1px 6px;border-radius:4px;font-size:11px\">[DELETED]</span>");
		}
		if (n.isModified()) {
			tags.add("<span style=\"color:#92400e;font-weight:600;background:#ffedd5;padding:1px 6px;border-radius:4px;font-size:11px\">[MERGED]</span>");
		}
		if (!n.isActive()) {
			tags.add("<span style='color:#6b7280'>INACTIVE</span>");
		}

		if (tags.isEmpty()) {
			return base;
		}
		return base + " &nbsp;[" + String.join(" | ", tags) + "]";
	}

	private static String escape(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
