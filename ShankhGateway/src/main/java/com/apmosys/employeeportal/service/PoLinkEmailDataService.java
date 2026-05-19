package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.polink.BoardingTableRowDto;
import com.apmosys.employeeportal.dto.polink.PoLinkEmailContentDto;
import com.apmosys.employeeportal.dto.polink.PreviousPoRequirementsBlockDto;
import com.apmosys.employeeportal.dto.polink.PreviousRequirementDto;
import com.apmosys.employeeportal.dto.polink.RequirementDto;
import com.apmosys.employeeportal.dto.polink.ResourceImpactDto;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.util.PoStatusUtil;

import lombok.RequiredArgsConstructor;

/**
 * Loads requirement/mapping data (DTO, then resource_requirement, then TNM for PO-scoped rows) and
 * builds structured email content.
 */
@Service
@RequiredArgsConstructor
public class PoLinkEmailDataService {

	private static final Logger log = LoggerFactory.getLogger(PoLinkEmailDataService.class);

	/**
	 * When false, skips DB-heavy assembly for email sections not rendered in
	 * {@link PoLinkImpactEmailBuilder} (Previous/Current PO Requirements, Boarding Snapshot).
	 */
	static final boolean INCLUDE_REQUIREMENT_AND_BOARDING_EMAIL_SECTIONS = false;

	private static final ZoneId ZONE = ZoneId.systemDefault();

	private final PoRequirementMappingRepository poRequirementMappingRepository;
	private final EmployeeTeamMapRepository employeeTeamMapRepository;
	private final ResourceRequirementRepository resourceRequirementRepository;
	private final ProjectRepository projectRepository;
	private final ProjectPoDetailsRepository projectPoDetailsRepository;

	public PoLinkEmailContentDto buildEmailContent(Project primaryProject, IshineLinkProjectDto dto) {
		PoLinkEmailContentDto out = new PoLinkEmailContentDto();
		if (primaryProject == null) {
			out.setProjectDisplayName("Project");
			out.setWhatChangedHtml(buildWhatChangedSummaryHtml(null, null, dto));
			out.setIncludePreviousPoRequirementsSection(false);
			out.setMergedRemovedProjectLines(buildMergedRemovedLines(dto));
			return out;
		}

		String primaryName = trim(primaryProject.getProjectName());
		out.setProjectDisplayName(primaryName);
		ProjectPoMappingWithResourceDTO primaryPayload = dto != null ? dto.getPrimaryProject() : null;

		out.setMergedRemovedProjectLines(buildMergedRemovedLines(dto));
		out.setWhatChangedHtml(buildWhatChangedSummaryHtml(primaryProject, primaryPayload, dto));

		ResourceImpactDto impact = new ResourceImpactDto();
		if (primaryPayload != null && primaryProject.getProjectName() != null) {
			String dtoName = trim(primaryPayload.getProjectName());
			String dbName = trim(primaryProject.getProjectName());
			if (!dtoName.isEmpty() && !dbName.isEmpty() && !dtoName.equalsIgnoreCase(dbName)) {
				impact.setProjectDisplayNameChanged(true);
				impact.setPreviousProjectDisplayName(dbName);
				impact.setCurrentProjectDisplayName(dtoName);
			}
		}
		out.setResourceImpact(impact);

		if (!INCLUDE_REQUIREMENT_AND_BOARDING_EMAIL_SECTIONS) {
			out.setIncludePreviousPoRequirementsSection(false);
			out.setPreviousRequirementBlocks(Collections.emptyList());
			out.setCurrentRequirementRows(Collections.emptyList());
			out.setBoardingTableRows(Collections.emptyList());
			return out;
		}

		Integer internalProjectId = primaryProject.getProjectId();
		Set<Long> deletedPoIds = new HashSet<>();
		if (dto != null && dto.getDeletedProjects() != null) {
			for (ProjectPoMappingWithResourceDTO del : dto.getDeletedProjects()) {
				if (del == null || del.getPoDetailsList() == null) {
					continue;
				}
				for (PoDetailsForProjectPoMappingDTO p : del.getPoDetailsList()) {
					if (p != null && p.getPoId() != null) {
						deletedPoIds.add(p.getPoId());
					}
				}
			}
		}
		Set<Long> primaryPoIds = new HashSet<>();
		if (primaryPayload != null && primaryPayload.getPoDetailsList() != null) {
			for (PoDetailsForProjectPoMappingDTO p : primaryPayload.getPoDetailsList()) {
				if (p != null && p.getPoId() != null) {
					primaryPoIds.add(p.getPoId());
				}
			}
		}

		boolean showPreviousReq = !isMonitoringPrimary(primaryProject);
		out.setIncludePreviousPoRequirementsSection(showPreviousReq);
		if (showPreviousReq) {
			out.setPreviousRequirementBlocks(groupPreviousRequirements(buildPreviousRequirementRows(dto)));
		} else {
			out.setPreviousRequirementBlocks(Collections.emptyList());
		}

		List<RequirementDto> currentTable = new ArrayList<>();

		Map<Long, ProjectPoDetails> ppdByPo = loadPpdMap(internalProjectId);

		if (primaryPayload != null && primaryPayload.getPoDetailsList() != null) {
			for (PoDetailsForProjectPoMappingDTO po : primaryPayload.getPoDetailsList()) {
				if (po == null || po.getPoId() == null) {
					continue;
				}
				String poLabel = po.getPoNo() != null ? po.getPoNo() : "PO";
				List<PoRequirementMapping> prms =
						poRequirementMappingRepository.findByPoIdAndActiveTrue(po.getPoId());
				if (prms == null) {
					prms = List.of();
				}

				List<ReqLine> lines = getRequirementsFromDtoOrDb(po, internalProjectId, prms);

				Set<Long> coveredPrm = new HashSet<>();
				ProjectPoDetails rowPpd = ppdByPo.get(po.getPoId());
				for (ReqLine line : lines) {
					currentTable.add(toRequirementRow(poLabel, line, po.getPoId(), prms, coveredPrm, rowPpd,
							deletedPoIds, primaryPoIds));
				}

				for (PoRequirementMapping m : prms) {
					if (m == null || m.getPoRequirementMappingId() == null) {
						continue;
					}
					if (coveredPrm.contains(m.getPoRequirementMappingId())) {
						continue;
					}
					LocalDateTime ms = m.getLineItemStartDate() != null ? m.getLineItemStartDate() : LocalDateTime.MIN;
					LocalDateTime me = m.getLineItemEndDate() != null ? m.getLineItemEndDate() : LocalDateTime.MAX;
					long mcnt = 0;
					if (m.getRoleId() != null) {
						Long c = employeeTeamMapRepository.countDistinctActiveEmployeesOnPoAndRoleInWindow(
								po.getPoId(), m.getRoleId(), ms, me);
						mcnt = c != null ? c : 0;
						assertCountMatchesEmployees(po.getPoId(), m.getRoleId(), ms, me, mcnt);
					} else {
						Long c = employeeTeamMapRepository.countDistinctActiveEmployeesOnPrmInWindow(
								m.getPoRequirementMappingId(), ms, me);
						mcnt = c != null ? c : 0;
					}
					long reqCount = m.getCount() != null ? m.getCount() : 0;
					String roleDept = formatRoleDept(m.getRole(), m.getDepartment());
					String poStatus = poDateStatus(rowPpd);
					boolean expiredRow = "Expired".equals(poStatus);
					String staffingText = mcnt == 0 ? "Underboarded" : "Overboarded";
					if (mcnt == 0) {
						log.info(
								"PO link email: uncovered PRM with zero mapped headcount poId={} prmId={}; listing as Underboarded",
								po.getPoId(), m.getPoRequirementMappingId());
					}
					currentTable.add(RequirementDto.builder()
							.poLabel(poLabel)
							.roleAndDepartment(roleDept)
							.requiredCount(reqCount)
							.mappedCount(mcnt)
							.statusText(staffingText)
							.statusColor("#ea580c")
							.poLifecycleStatus(poStatus)
							.expiredRow(expiredRow)
							.build());
				}

				if (lines.isEmpty() && prms.isEmpty()) {
					String poStatus = poDateStatus(rowPpd);
					currentTable.add(RequirementDto.builder()
							.poLabel(poLabel)
							.roleAndDepartment("--")
							.requiredCount(0)
							.mappedCount(0)
							.statusText("No requirement data (DTO and resource_requirement checked)")
							.statusColor("#6b7280")
							.poLifecycleStatus(poStatus)
							.expiredRow("Expired".equals(poStatus))
							.build());
				}
			}
		}

		out.setCurrentRequirementRows(currentTable);
		out.setBoardingTableRows(buildBoardingTableRows(
				primaryPayload, internalProjectId, currentTable, ppdByPo, dto, deletedPoIds, primaryPoIds));

		return out;
	}

	private static boolean isMonitoringPrimary(Project p) {
		String t = trim(p.getPoProjectType());
		return t.equalsIgnoreCase("monitoring");
	}

	private List<PreviousPoRequirementsBlockDto> groupPreviousRequirements(List<PreviousRequirementDto> flat) {
		Map<String, List<PreviousRequirementDto>> byProject = new LinkedHashMap<>();
		for (PreviousRequirementDto r : flat) {
			if (r == null) {
				continue;
			}
			String key = trim(r.getProjectName());
			if (key.isEmpty()) {
				key = "--";
			}
			byProject.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
		}
		List<PreviousPoRequirementsBlockDto> blocks = new ArrayList<>();
		for (Map.Entry<String, List<PreviousRequirementDto>> e : byProject.entrySet()) {
			blocks.add(PreviousPoRequirementsBlockDto.builder()
					.projectDisplayName(e.getKey())
					.rows(e.getValue())
					.build());
		}
		return blocks;
	}

	private Map<Long, ProjectPoDetails> loadPpdMap(Integer internalProjectId) {
		Map<Long, ProjectPoDetails> map = new HashMap<>();
		if (internalProjectId == null) {
			return map;
		}
		List<ProjectPoDetails> list = projectPoDetailsRepository.findByProjectId(internalProjectId);
		if (list == null) {
			return map;
		}
		for (ProjectPoDetails p : list) {
			if (p != null && p.getPoId() != null) {
				map.put(p.getPoId(), p);
			}
		}
		return map;
	}

	private List<String> buildMergedRemovedLines(IshineLinkProjectDto dto) {
		List<String> lines = new ArrayList<>();
		if (dto == null || dto.getDeletedProjects() == null) {
			return lines;
		}
		for (ProjectPoMappingWithResourceDTO del : dto.getDeletedProjects()) {
			if (del == null) {
				continue;
			}
			String name = trim(del.getProjectName());
			if (name.isEmpty()) {
				name = "Merged context";
			}
			lines.add(name);
		}
		return lines;
	}

	private String buildWhatChangedSummaryHtml(
			Project primaryProject,
			ProjectPoMappingWithResourceDTO primaryPayload,
			IshineLinkProjectDto dto) {

		String primaryProjEsc = esc(trim(primaryProject != null ? primaryProject.getProjectName() : ""));
		String oldPo = "";
		String primaryPo = "";
		if (dto != null && dto.getDeletedProjects() != null && !dto.getDeletedProjects().isEmpty()) {
			ProjectPoMappingWithResourceDTO d0 = dto.getDeletedProjects().get(0);
			if (d0 != null && d0.getPoDetailsList() != null) {
				for (PoDetailsForProjectPoMappingDTO p : d0.getPoDetailsList()) {
					if (p != null && p.getPoNo() != null && !p.getPoNo().isEmpty()) {
						oldPo = trim(p.getPoNo());
						break;
					}
				}
			}
		}
		if (primaryPayload != null && primaryPayload.getPoDetailsList() != null) {
			for (PoDetailsForProjectPoMappingDTO p : primaryPayload.getPoDetailsList()) {
				if (p != null && p.getPoNo() != null && !p.getPoNo().isEmpty()) {
					primaryPo = trim(p.getPoNo());
					break;
				}
			}
		}

		boolean hadMerge =
				dto != null && dto.getDeletedProjects() != null && !dto.getDeletedProjects().isEmpty();

		StringBuilder sb = new StringBuilder();
		sb.append("<ul style=\"margin:8px 0 0 18px;padding:0;color:#334155;line-height:1.55;font-size:14px;\">");
		if (!oldPo.isEmpty() && !primaryPo.isEmpty()) {
			sb.append("<li>PO <b>").append(esc(oldPo)).append("</b> is now linked to <b>")
					.append(esc(primaryPo)).append("</b> (primary PO).</li>");
		} else if (!primaryPo.isEmpty()) {
			sb.append("<li>Primary PO <b>").append(esc(primaryPo)).append("</b> is in effect.</li>");
		}
		if (hadMerge) {
			sb.append("<li>Previous project context is no longer active.</li>");
		}
		sb.append("<li>All references should now use the primary project");
		if (!primaryProjEsc.isEmpty()) {
			sb.append(" &quot;").append(primaryProjEsc).append("&quot;");
		}
		sb.append(".</li>");
		sb.append("</ul>");
		return sb.toString();
	}

	private List<PreviousRequirementDto> buildPreviousRequirementRows(IshineLinkProjectDto dto) {
		List<PreviousRequirementDto> prev = new ArrayList<>();
		if (dto == null || dto.getDeletedProjects() == null) {
			return prev;
		}
		for (ProjectPoMappingWithResourceDTO del : dto.getDeletedProjects()) {
			if (del == null || del.getPoDetailsList() == null) {
				continue;
			}
			String projectName = trim(del.getProjectName());
			if (projectName.isEmpty()) {
				projectName = "--";
			}
			Integer delInternalId = null;
			if (del.getProjectId() != null) {
				Project p = projectRepository.findByPoProjectId(del.getProjectId());
				if (p != null) {
					delInternalId = p.getProjectId();
				}
			}
			for (PoDetailsForProjectPoMappingDTO po : del.getPoDetailsList()) {
				if (po == null || po.getPoId() == null) {
					continue;
				}
				String poLabel = po.getPoNo() != null ? po.getPoNo() : "PO";
				List<PoRequirementMapping> prms =
						poRequirementMappingRepository.findByPoIdAndActiveTrue(po.getPoId());
				if (prms == null) {
					prms = List.of();
				}
				List<ReqLine> lines = getRequirementsFromDtoOrDb(po, delInternalId, prms);
				if (lines.isEmpty()) {
					prev.add(PreviousRequirementDto.builder()
							.projectName(projectName)
							.poLabel(poLabel)
							.roleLabel("--")
							.requiredCount(0)
							.build());
					continue;
				}
				for (ReqLine line : lines) {
					prev.add(PreviousRequirementDto.builder()
							.projectName(projectName)
							.poLabel(poLabel)
							.roleLabel(formatRoleDept(line.role, line.dept))
							.requiredCount(line.required)
							.build());
				}
			}
		}
		return prev;
	}

	private List<BoardingTableRowDto> buildBoardingTableRows(
			ProjectPoMappingWithResourceDTO primary,
			Integer internalProjectId,
			List<RequirementDto> currentRows,
			Map<Long, ProjectPoDetails> ppdByPo,
			IshineLinkProjectDto dto,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIdSet) {

		List<BoardingTableRowDto> rows = new ArrayList<>();
		if (primary == null || primary.getPoDetailsList() == null) {
			return rows;
		}

		Map<Long, String> poToRemovedProject = new HashMap<>();
		if (dto != null && dto.getDeletedProjects() != null) {
			for (ProjectPoMappingWithResourceDTO del : dto.getDeletedProjects()) {
				if (del == null || del.getPoDetailsList() == null) {
					continue;
				}
				String pn = trim(del.getProjectName());
				for (PoDetailsForProjectPoMappingDTO p : del.getPoDetailsList()) {
					if (p != null && p.getPoId() != null && !pn.isEmpty()) {
						poToRemovedProject.putIfAbsent(p.getPoId(), pn);
					}
				}
			}
		}

		for (PoDetailsForProjectPoMappingDTO po : primary.getPoDetailsList()) {
			if (po == null || po.getPoId() == null) {
				continue;
			}
			List<PoRequirementMapping> mappings = Optional
					.ofNullable(poRequirementMappingRepository.findByPoIdAndActiveTrue(po.getPoId()))
					.orElse(Collections.emptyList());

			String poNo = po.getPoNo() != null ? po.getPoNo() : "PO";
			ProjectPoDetails ppd = ppdByPo.get(po.getPoId());
			String poStatus = poDateStatus(ppd);
			boolean mergedFromDeleted = mergedFromDeletedContext(po.getPoId(), deletedPoIds, primaryPoIdSet);
			String fromProject = poToRemovedProject.get(po.getPoId());

			if (mappings.isEmpty()) {
				log.info("PO link email boarding: no PRM rows for poId={} poNo={}; emitting empty-resource snapshot",
						po.getPoId(), poNo);
				rows.add(BoardingTableRowDto.builder()
						.poLabel(poNo)
						.status(poStatus)
						.resourceName("-")
						.team("-")
						.remark("No resources mapped")
						.build());
				continue;
			}

			List<ReqLine> reqLines = getRequirementsFromDtoOrDb(po, internalProjectId, mappings);

			Set<String> seenRoleWindow = new HashSet<>();
			Set<String> memberKeys = new LinkedHashSet<>();
			List<com.apmosys.employeeportal.dto.EmployeeImpactDTO> orderedEmps = new ArrayList<>();

			for (PoRequirementMapping m : mappings) {
				if (m == null || m.getRoleId() == null) {
					continue;
				}
				LocalDateTime ms = m.getLineItemStartDate() != null ? m.getLineItemStartDate() : LocalDateTime.MIN;
				LocalDateTime me = m.getLineItemEndDate() != null ? m.getLineItemEndDate() : LocalDateTime.MAX;

				List<LocalDateTime[]> windows = new ArrayList<>();
				if (reqLines.isEmpty()) {
					windows.add(new LocalDateTime[] { ms, me });
				} else {
					for (ReqLine rl : reqLines) {
						if (!lineItemsOverlap(rl.winStart, rl.winEnd, m.getLineItemStartDate(), m.getLineItemEndDate())) {
							continue;
						}
						LocalDateTime qs = rl.winStart.isAfter(ms) ? rl.winStart : ms;
						LocalDateTime qe = rl.winEnd.isBefore(me) ? rl.winEnd : me;
						if (!qs.isAfter(qe)) {
							windows.add(new LocalDateTime[] { qs, qe });
						}
					}
				}

				for (LocalDateTime[] w : windows) {
					String winKey = m.getRoleId() + "|" + w[0] + "|" + w[1];
					if (!seenRoleWindow.add(winKey)) {
						continue;
					}
					long cnt = countByPoRoleWindow(po.getPoId(), m.getRoleId(), w[0], w[1]);
					List<com.apmosys.employeeportal.dto.EmployeeImpactDTO> emps =
							employeeTeamMapRepository.findActiveEmployeesByPoAndRoleWithinDates(
									po.getPoId(), m.getRoleId(), w[0], w[1]);
					int n = emps == null ? 0 : emps.size();
					if (n != cnt) {
						log.warn(
								"PO link email: mapped count {} != boarding list size {} for poId={} roleId={} window [{} .. {}]",
								cnt, n, po.getPoId(), m.getRoleId(), w[0], w[1]);
					}
					if (emps != null) {
						for (com.apmosys.employeeportal.dto.EmployeeImpactDTO e : emps) {
							if (e == null) {
								continue;
							}
							String dedupeKey = (trim(e.getEmployeeName()) + "|" + trim(e.getTeamName()))
									.toLowerCase(Locale.ROOT);
							if (memberKeys.add(dedupeKey)) {
								orderedEmps.add(e);
							}
						}
					}
				}
			}

			boolean expired = "Expired".equals(poStatus);
			boolean over = isPoAttentionForBoarding(poNo, currentRows);
			long totalMappedOnPo = sumMappedForPo(poNo, currentRows);
			boolean highlight = expired || mergedFromDeleted || over;
			boolean showOk = !highlight && !orderedEmps.isEmpty() && totalMappedOnPo > 0 && "Active".equals(poStatus);
			boolean needsEmptyRow = orderedEmps.isEmpty();

			String baseRemark = boardingRemarkForPo(expired, over, mergedFromDeleted, showOk, fromProject);

			if (needsEmptyRow) {
				log.info("PO link email boarding: no active employees in evaluated windows for poNo={}", poNo);
				rows.add(BoardingTableRowDto.builder()
						.poLabel(poNo)
						.status(poStatus)
						.resourceName("-")
						.team("-")
						.remark("No resources mapped")
						.build());
				continue;
			}

			for (com.apmosys.employeeportal.dto.EmployeeImpactDTO e : orderedEmps) {
				String name = trim(e.getEmployeeName());
				if (name.isEmpty()) {
					name = "-";
				}
				String team = trim(e.getTeamName());
				if (team.isEmpty()) {
					team = "-";
				}
				rows.add(BoardingTableRowDto.builder()
						.poLabel(poNo)
						.status(poStatus)
						.resourceName(name)
						.team(team)
						.remark(baseRemark)
						.build());
			}
		}
		return rows;
	}

	private static String boardingRemarkForPo(
			boolean expired,
			boolean over,
			boolean mergedFromDeleted,
			boolean showOk,
			String fromProject) {

		if (showOk) {
			return "-";
		}
		if (expired) {
			String r = "Needs attention";
			if (fromProject != null && !fromProject.isEmpty()) {
				return r + " (from " + fromProject + ")";
			}
			return r;
		}
		if (over) {
			return "Review Allocation";
		}
		if (mergedFromDeleted) {
			if (fromProject != null && !fromProject.isEmpty()) {
				return "Review Allocation (from " + fromProject + ")";
			}
			return "Review Allocation";
		}
		return "-";
	}

	private static long sumMappedForPo(String poLabel, List<RequirementDto> currentRows) {
		long s = 0;
		for (RequirementDto r : currentRows) {
			if (r != null && poLabel.equals(r.getPoLabel())) {
				s += r.getMappedCount();
			}
		}
		return s;
	}

	private static boolean isPoAttentionForBoarding(String poLabel, List<RequirementDto> currentRows) {
		for (RequirementDto r : currentRows) {
			if (r == null || !poLabel.equals(r.getPoLabel()) || r.getStatusText() == null) {
				continue;
			}
			if (r.getStatusText().contains("Overboard")) {
				return true;
			}
		}
		return false;
	}

	private long countByPoRoleWindow(Long poId, Long roleId, LocalDateTime qs, LocalDateTime qe) {
		if (poId == null || roleId == null || qs.isAfter(qe)) {
			return 0;
		}
		Long c = employeeTeamMapRepository.countDistinctActiveEmployeesOnPoAndRoleInWindow(poId, roleId, qs, qe);
		return c != null ? c : 0;
	}

	private void assertCountMatchesEmployees(Long poId, Long roleId, LocalDateTime qs, LocalDateTime qe, long count) {
		if (poId == null || roleId == null || qs.isAfter(qe)) {
			return;
		}
		List<com.apmosys.employeeportal.dto.EmployeeImpactDTO> emps =
				employeeTeamMapRepository.findActiveEmployeesByPoAndRoleWithinDates(poId, roleId, qs, qe);
		int n = emps == null ? 0 : emps.size();
		if (n != count) {
			log.warn(
					"PO link email: requirement table mapped count {} != employee list size {} for poId={} roleId={} window [{} .. {}]",
					count, n, poId, roleId, qs, qe);
		}
	}

	private static String poDateStatus(ProjectPoDetails ppd) {
		if (ppd == null) {
			return "Unknown";
		}
		return PoStatusUtil.getPoStatus(
				LocalDate.now(),
				PoStatusUtil.toLocalDate(ppd.getPoStartDate()),
				PoStatusUtil.toLocalDate(ppd.getPoEndDate()));
	}

	private static boolean mergedFromDeletedContext(Long poId, Set<Long> deletedPoIds, Set<Long> primaryPoIds) {
		return deletedPoIds != null && poId != null && deletedPoIds.contains(poId)
				&& (primaryPoIds == null || !primaryPoIds.contains(poId));
	}

	private enum Source {
		DTO, DB_PROJECT_RR, DB_PRM
	}

	private static class ReqLine {
		Long prmId;
		Long roleId;
		String role;
		String dept;
		Long clientRoleId;
		long required;
		LocalDateTime winStart;
		LocalDateTime winEnd;
		Source source;
	}

	/**
	 * Order: DTO, then {@code resource_requirement} for the internal project, then TNM line items.
	 */
	private List<ReqLine> getRequirementsFromDtoOrDb(
			PoDetailsForProjectPoMappingDTO po,
			Integer internalProjectId,
			List<PoRequirementMapping> prms) {

		List<ReqLine> out = new ArrayList<>();
		Long poId = po.getPoId();

		List<POResourceRequirementDTO> dtoReqs = po.getResourceRequirementList();
		if (dtoReqs != null && !dtoReqs.isEmpty()) {
			for (POResourceRequirementDTO r : dtoReqs) {
				if (r == null) {
					continue;
				}
				ReqLine line = new ReqLine();
				line.role = r.getRole();
				line.dept = r.getDepartment();
				line.clientRoleId = r.getClientRoleId();
				line.required = r.getCount() != null ? r.getCount() : 0;
				line.winStart = startOfDay(r.getLineItemStartDate());
				line.winEnd = endOfDay(r.getLineItemEndDate());
				line.source = Source.DTO;
				PoRequirementMapping match = matchPrmFromDto(r, prms);
				if (match != null) {
					line.prmId = match.getPoRequirementMappingId();
					line.roleId = match.getRoleId();
				}
				out.add(line);
			}
			return out;
		}

		if (internalProjectId != null) {
			List<ResourceRequirementDTO> rr = resourceRequirementRepository.findByProjectId(internalProjectId);
			if (rr != null && !rr.isEmpty()) {
				for (ResourceRequirementDTO rrRow : rr) {
					if (rrRow == null) {
						continue;
					}
					ReqLine line = new ReqLine();
					line.role = rrRow.getRole();
					line.dept = rrRow.getDepartment();
					line.required = rrRow.getCount() != null ? rrRow.getCount().longValue() : 0;
					line.winStart = LocalDateTime.MIN;
					line.winEnd = LocalDateTime.MAX;
					line.source = Source.DB_PROJECT_RR;
					PoRequirementMapping match = matchPrmFromDeptRole(poId, line.role, line.dept, prms);
					if (match != null) {
						line.prmId = match.getPoRequirementMappingId();
						line.roleId = match.getRoleId();
					}
					out.add(line);
				}
				return out;
			}
		}

		List<RmgResourceRequirementDto> rmg = poRequirementMappingRepository.getPoRequirementDataByPoId(poId);
		if (rmg != null && !rmg.isEmpty()) {
			for (RmgResourceRequirementDto x : rmg) {
				if (x == null || x.getPoRequirementMappingId() == null) {
					continue;
				}
				ReqLine line = new ReqLine();
				line.prmId = x.getPoRequirementMappingId();
				line.roleId = x.getRoleId();
				line.role = x.getRole();
				line.dept = x.getDepartment();
				line.required = x.getCount() != null ? x.getCount() : 0;
				line.winStart = x.getRequirementStartDate() != null ? x.getRequirementStartDate() : LocalDateTime.MIN;
				line.winEnd = x.getRequirementEndDate() != null ? x.getRequirementEndDate() : LocalDateTime.MAX;
				line.source = Source.DB_PRM;
				out.add(line);
			}
		}

		return out;
	}

	private RequirementDto toRequirementRow(
			String poLabel,
			ReqLine line,
			Long poId,
			List<PoRequirementMapping> prms,
			Set<Long> coveredPrm,
			ProjectPoDetails ppd,
			Set<Long> deletedPoIds,
			Set<Long> primaryPoIds) {

		LocalDateTime qs = line.winStart;
		LocalDateTime qe = line.winEnd;
		PoRequirementMapping prm = null;
		if (line.prmId != null) {
			prm = prms.stream()
					.filter(p -> p != null && Objects.equals(p.getPoRequirementMappingId(), line.prmId))
					.findFirst()
					.orElse(null);
			if (prm != null) {
				coveredPrm.add(prm.getPoRequirementMappingId());
				LocalDateTime ms = prm.getLineItemStartDate() != null ? prm.getLineItemStartDate() : LocalDateTime.MIN;
				LocalDateTime me = prm.getLineItemEndDate() != null ? prm.getLineItemEndDate() : LocalDateTime.MAX;
				qs = qs.isAfter(ms) ? qs : ms;
				qe = qe.isBefore(me) ? qe : me;
			}
		}

		Long effectiveRoleId = line.roleId != null ? line.roleId : (prm != null ? prm.getRoleId() : null);
		long mapped = countMappedHeadcount(line, poId, prm, qs, qe, effectiveRoleId);
		if (effectiveRoleId != null && poId != null && !qs.isAfter(qe)) {
			assertCountMatchesEmployees(poId, effectiveRoleId, qs, qe, mapped);
		}

		String poStatus = poDateStatus(ppd);
		boolean expiredRow = "Expired".equals(poStatus);

		String roleDept = formatRoleDept(
				line.role != null ? line.role : (prm != null ? prm.getRole() : ""),
				line.dept != null ? line.dept : (prm != null ? prm.getDepartment() : ""));

		String statusText;
		String color;
		if (line.source == Source.DB_PROJECT_RR && line.prmId == null) {
			statusText = "Review mapping (project baseline)";
			color = "#6b7280";
		} else if (line.required > 0 && mapped < line.required) {
			statusText = "Underboarded";
			color = "#ea580c";
		} else if (line.required > 0 && mapped > line.required) {
			statusText = "Overboarded";
			color = "#dc2626";
		} else if (line.required == 0 && mapped > 0) {
			statusText = "Overboarded";
			color = "#ea580c";
		} else if (line.required == 0 && mapped == 0) {
			statusText = "Underboarded";
			color = "#ea580c";
		} else {
			statusText = "Balanced";
			color = "#16a34a";
		}

		return RequirementDto.builder()
				.poLabel(poLabel)
				.roleAndDepartment(roleDept)
				.requiredCount(line.required)
				.mappedCount(mapped)
				.statusText(statusText)
				.statusColor(color)
				.poLifecycleStatus(poStatus)
				.expiredRow(expiredRow)
				.build();
	}

	/**
	 * Matches boarding snapshot: prefer {@code po_id} + {@code role_id} (same as
	 * {@link EmployeeTeamMapRepository#findActiveEmployeesByPoAndRoleWithinDates}), then PRM id.
	 */
	private long countMappedHeadcount(
			ReqLine line,
			Long poId,
			PoRequirementMapping prm,
			LocalDateTime qs,
			LocalDateTime qe,
			Long effectiveRoleId) {

		if (qs.isAfter(qe)) {
			return 0;
		}
		Long roleId = effectiveRoleId != null ? effectiveRoleId
				: (line.roleId != null ? line.roleId : (prm != null ? prm.getRoleId() : null));
		if (poId != null && roleId != null) {
			Long c = employeeTeamMapRepository.countDistinctActiveEmployeesOnPoAndRoleInWindow(poId, roleId, qs, qe);
			return c != null ? c : 0;
		}
		if (line.prmId != null) {
			Long c = employeeTeamMapRepository.countDistinctActiveEmployeesOnPrmInWindow(line.prmId, qs, qe);
			return c != null ? c : 0;
		}
		return 0;
	}

	private PoRequirementMapping matchPrmFromDto(POResourceRequirementDTO req, List<PoRequirementMapping> prms) {
		for (PoRequirementMapping m : prms) {
			if (m == null) {
				continue;
			}
			if (matchesDtoToPrm(req, m)) {
				return m;
			}
		}
		return null;
	}

	private PoRequirementMapping matchPrmFromDeptRole(
			Long poId,
			String role,
			String dept,
			List<PoRequirementMapping> prms) {

		for (PoRequirementMapping m : prms) {
			if (m == null) {
				continue;
			}
			if (Objects.equals(m.getPoId(), poId)
					&& eqIc(trim(role), trim(m.getRole()))
					&& eqIc(trim(dept), trim(m.getDepartment()))) {
				return m;
			}
		}
		return null;
	}

	private static boolean matchesDtoToPrm(POResourceRequirementDTO req, PoRequirementMapping m) {
		if (req.getClientRoleId() != null && m.getClientRoleId() != null) {
			return Objects.equals(req.getClientRoleId(), m.getClientRoleId());
		}
		return eqIc(trim(req.getDepartment()), trim(m.getDepartment()))
				&& eqIc(trim(req.getRole()), trim(m.getRole()));
	}

	private static boolean lineItemsOverlap(
			LocalDateTime reqStart,
			LocalDateTime reqEnd,
			LocalDateTime mapStart,
			LocalDateTime mapEnd) {

		LocalDateTime ms = mapStart != null ? mapStart : LocalDateTime.MIN;
		LocalDateTime me = mapEnd != null ? mapEnd : LocalDateTime.MAX;
		return !reqStart.isAfter(me) && !ms.isAfter(reqEnd);
	}

	private static String formatRoleDept(String role, String dept) {
		String r = trim(role);
		String d = trim(dept);
		if (r.isEmpty() && d.isEmpty()) {
			return "--";
		}
		if (r.isEmpty()) {
			return d;
		}
		if (d.isEmpty()) {
			return r;
		}
		return r + " / " + d;
	}

	private static LocalDateTime startOfDay(Date d) {
		if (d == null) {
			return LocalDateTime.MIN;
		}
		return d.toInstant().atZone(ZONE).toLocalDate().atStartOfDay();
	}

	private static LocalDateTime endOfDay(Date d) {
		if (d == null) {
			return LocalDateTime.MAX;
		}
		return d.toInstant().atZone(ZONE).toLocalDate().atTime(23, 59, 59, 999_999_999);
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	private static boolean eqIc(String a, String b) {
		return a.equalsIgnoreCase(b);
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
