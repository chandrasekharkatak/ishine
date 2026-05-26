package com.apmosys.employeeportal.service;

import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveAspirationDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveBulkDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveCertificationDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveDetailDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveDetailSkillDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveQueueRowDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSkillDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSkillMetaDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSkillViewRowDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSubmitRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSubskillDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveTrainingDTO;
import com.apmosys.employeeportal.dto.SkillMatrixCategoryListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixCustomSkillDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixCustomSkillRequestRowDTO;
import com.apmosys.employeeportal.dto.SkillMatrixDomainFeatureListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixDomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixMySubmissionListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixProposeSkillRequest;
import com.apmosys.employeeportal.dto.SkillMatrixSubdomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitContextDTO;
import com.apmosys.employeeportal.dto.SkillMatrixHodDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitCertificationDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitTrainingDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitDraftProjectDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitDraftProjectSkillDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitDraftRequest;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitDraftSkillDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitLockStatusDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApprovedBaselineDTO;
import com.apmosys.employeeportal.dto.SkillMatrixUpdateProjectsRequest;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitPickSkillDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitProjectHistoryDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitSubskillDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.SkillCategoryMaster;
import com.apmosys.employeeportal.model.SkillDomainFeatureMaster;
import com.apmosys.employeeportal.model.SkillsMaster;
import com.apmosys.employeeportal.model.SubskillsMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DesignationRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.SkillCategoryMasterRepository;
import com.apmosys.employeeportal.repository.SkillDomainFeatureMasterRepository;
import com.apmosys.employeeportal.repository.SkillDomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillSubdomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillsMasterRepository;
import com.apmosys.employeeportal.repository.SubskillsMasterRepository;
import com.apmosys.employeeportal.skillmatrix.SkillMatrixMasterSpecifications;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Service
public class SkillMatrixSubmitService {

	private static final DateTimeFormatter DOJ_ISO = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private SkillsMasterRepository skillsMasterRepository;

	@Autowired
	private SubskillsMasterRepository subskillsMasterRepository;

	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private SkillCategoryMasterRepository skillCategoryMasterRepository;

	@Autowired
	private EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SkillMatrixNotificationService skillMatrixNotificationService;

	@Autowired
	private SkillDomainMasterRepository skillDomainMasterRepository;

	@Autowired
	private SkillSubdomainMasterRepository skillSubdomainMasterRepository;

	@Autowired
	private SkillDomainFeatureMasterRepository skillDomainFeatureMasterRepository;

	private static final int SKILL_POOL_MAX = 500;

	/** JDBC may return Boolean for MySQL BOOLEAN; avoid Integer.parseInt on "true"/"false". */
	private static int jdbcIntCoerce(Object v) {
		if (v == null) {
			return 0;
		}
		if (v instanceof Boolean) {
			return ((Boolean) v) ? 1 : 0;
		}
		if (v instanceof Number) {
			return ((Number) v).intValue();
		}
		String s = v.toString().trim();
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			if ("true".equalsIgnoreCase(s)) {
				return 1;
			}
			if ("false".equalsIgnoreCase(s)) {
				return 0;
			}
			return 0;
		}
	}

	private static boolean jdbcBool(Object v) {
		return jdbcIntCoerce(v) != 0;
	}

	@Transactional(readOnly = true)
	public SkillMatrixSubmitContextDTO buildSubmitContext(Long empId) {
		Employee emp = employeeRepository.findByEmpId(empId);
		if (emp == null) {
			throw new IllegalStateException("Employee record not found.");
		}

		SkillMatrixSubmitContextDTO dto = new SkillMatrixSubmitContextDTO();
		dto.setFullName(emp.getName());
		dto.setEmploymentId(formatEmploymentId(emp));
		dto.setDateOfJoining(emp.getDateOfJoining() != null ? emp.getDateOfJoining().format(DOJ_ISO) : null);
		dto.setReportingManagerName(resolveManagerName(emp));
		dto.setMobileNumber(formatMobile(emp.getMobileNo()));
		dto.setExperience(formatTotalExperienceYears(emp));

		JobRole jobRole = emp.getJobRoleId() != null ? jobRoleRepository.findByjobRoleId(emp.getJobRoleId()) : null;
		dto.setDesignation(resolveDesignationName(emp, jobRole));
		dto.setJobRoleName(jobRole != null && StringUtils.hasText(jobRole.getName()) ? jobRole.getName().trim() : null);

		Long deptId = null;
		String deptName = null;
		if (jobRole != null) {
			deptId = jobRole.getDeptId();
		}

		if (deptId != null) {
			dto.setDepartmentId(deptId);
			departmentRepository.findById(deptId).map(Department::getName).ifPresent(dto::setDepartmentName);
			deptName = dto.getDepartmentName();
			try {
				// Resolve HOD dynamically via DB join (employee -> job_role -> department -> hod_id).
				// This avoids any stale/mismatched deptId assumptions and ensures per-employee correctness.
				Long hodId = departmentRepository.findHodIdByEmpId(empId);
				if (hodId == null) {
					Department d = departmentRepository.findByDeptId(deptId);
					hodId = d != null ? d.getHodId() : null;
				}
				if (hodId != null) {
					Employee hod = employeeRepository.findByEmpId(hodId);
					if (hod != null && StringUtils.hasText(hod.getName())) {
						dto.setHodName(hod.getName().trim());
					}
				}
			} catch (Exception ignore) {
			}
		}

		if (deptId == null) {
			dto.setContextMessage("No job role / department is mapped for this employee; skills cannot be loaded.");
			dto.setSkills(Collections.emptyList());
			return dto;
		}

		if (!StringUtils.hasText(deptName)) {
			dto.setContextMessage("Department name could not be resolved for your job role.");
		}

		long anySkills = skillsMasterRepository
				.count(SkillMatrixMasterSpecifications.skillsAnyActiveForDepartment(deptId));
		if (anySkills == 0) {
			dto.setContextMessage(String.format(
					"No active skills are configured in Skill Matrix for department \"%s\" (id %d).",
					deptName != null ? deptName : "—", deptId));
		}

		/* Skills for step 2 are loaded via {@link #listSkillPickPool} (Required / Optional). */
		dto.setSkills(Collections.emptyList());
		return dto;
	}

	@Transactional(readOnly = true)
	public List<SkillMatrixSubmitPickSkillDTO> listSkillPickPool(Long empId, String skillType, String q) {
		Long deptId = resolveDepartmentIdForEmp(empId);
		if (deptId == null) {
			return Collections.emptyList();
		}
		String normType = normalizePoolSkillType(skillType);
		Specification<SkillsMaster> spec = SkillMatrixMasterSpecifications.skillsDeptPool(deptId, normType, q);
		List<SkillsMaster> rows = skillsMasterRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "skillName"));
		if (rows.size() > SKILL_POOL_MAX) {
			rows = rows.subList(0, SKILL_POOL_MAX);
		}
		return mapSkillsToPickDtos(rows);
	}

	@Transactional(readOnly = true)
	public List<SkillMatrixCategoryListDTO> listSkillCategoriesForSubmit(String q) {
		Specification<SkillCategoryMaster> spec = SkillMatrixMasterSpecifications.skillCategoryFilter(null,
				StringUtils.hasText(q) ? q : null);
		return skillCategoryMasterRepository
				.findAll(spec, PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "categoryName")))
				.getContent().stream()
				.map(c -> new SkillMatrixCategoryListDTO(c.getCategoryId(), c.getCategoryName()))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SkillMatrixSubmitProjectHistoryDTO> listProjectHistoryForSubmit(Long empId) {
		/*
		 * Uses the existing HRMS mapping join:
		 * employee_team_mapping -> teams -> projects (named native query).
		 */
		List<Object[]> rows = employeeTeamMapRepository.getAllProjectsTeamsInfo(empId);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<SkillMatrixSubmitProjectHistoryDTO> out = new ArrayList<>();
		for (Object[] r : rows) {
			// As per EmployeeTeamMap.getAllProjectsTeamsInfo in jpa-named-queries.properties
			// 0 team_id, 1 projectName, 2 teamName, 3 clientName, 5 start_date, 8 end_date, 15 status, 10 project_id, 18 employee_team_map_id
			Long teamId = r.length > 0 && r[0] != null ? Long.valueOf(r[0].toString()) : null;
			String projectName = r.length > 1 && r[1] != null ? r[1].toString() : null;
			String teamName = r.length > 2 && r[2] != null ? r[2].toString() : null;
			String clientName = r.length > 3 && r[3] != null ? r[3].toString() : null;
			String startDate = r.length > 5 && r[5] != null ? r[5].toString() : null;
			String endDate = r.length > 8 && r[8] != null ? r[8].toString() : null;
			String status = r.length > 15 && r[15] != null ? r[15].toString() : null;
			Integer projectId = r.length > 10 && r[10] != null ? Integer.valueOf(r[10].toString()) : null;
			Long employeeTeamMapId = r.length > 18 && r[18] != null ? Long.valueOf(r[18].toString()) : null;
			out.add(new SkillMatrixSubmitProjectHistoryDTO(employeeTeamMapId, projectId, projectName, teamId, teamName,
					clientName, startDate, endDate, status));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public SkillMatrixSubmitDraftRequest loadLatestEditableDraft(Long empId) {
		try {
			List<Map<String, Object>> subs = jdbcTemplate.queryForList(
					"SELECT submission_id, assessment_cycle, cycle_year, status FROM skillmatrix_assessment_submission "
							+ "WHERE employee_id = ? AND status IN ('draft','rejected','changes_requested') "
							+ "ORDER BY updated_at DESC LIMIT 1",
					empId);
			if (subs.isEmpty()) {
				return null;
			}
			String submissionId = String.valueOf(subs.get(0).get("submission_id"));
			return loadDraftBySubmissionIdInternal(submissionId);
		} catch (Exception ex) {
			throw new IllegalStateException("Could not load skill matrix draft.", ex);
		}
	}

	@Transactional(readOnly = true)
	public SkillMatrixApprovedBaselineDTO loadApprovedBaseline(Long empId, String approvedSubmissionId) {
		if (empId == null) {
			throw new IllegalArgumentException("empId is required.");
		}
		String sid = StringUtils.hasText(approvedSubmissionId) ? approvedSubmissionId.trim() : latestApprovedSubmissionId(empId);
		if (!StringUtils.hasText(sid)) {
			throw new IllegalStateException("No approved submission found.");
		}
		List<Map<String, Object>> ok = jdbcTemplate.queryForList(
				"SELECT submission_id FROM skillmatrix_assessment_submission WHERE submission_id=? AND employee_id=? AND status='approved' LIMIT 1",
				sid, empId);
		if (ok.isEmpty()) {
			throw new IllegalArgumentException("Approved submission not found.");
		}
		SkillMatrixSubmitDraftRequest draft = loadDraftBySubmissionIdInternal(sid);
		SkillMatrixApprovedBaselineDTO out = new SkillMatrixApprovedBaselineDTO();
		out.setApprovedSubmissionId(sid);
		if (draft != null) {
			out.setSkills(draft.getSkills() != null ? draft.getSkills() : new ArrayList<>());
			out.setProjects(draft.getProjects() != null ? draft.getProjects() : new ArrayList<>());
			out.setTargetRole2yr(draft.getTargetRole2yr());
			out.setMessageToManager(draft.getMessageToManager());
			out.setAspirationSkillNames(draft.getAspirationSkillNames() != null ? draft.getAspirationSkillNames() : new ArrayList<>());
		}
		return out;
	}

	@Transactional(readOnly = true)
	public SkillMatrixSubmitLockStatusDTO getSubmitLockStatus(Long empId) {
		if (empId == null) {
			throw new IllegalArgumentException("empId is required.");
		}
		try {
			List<Map<String, Object>> subs = jdbcTemplate.queryForList(
					"SELECT submission_id, status, reporting_manager_name, hod_name "
							+ "FROM skillmatrix_assessment_submission WHERE employee_id = ? "
							+ "ORDER BY updated_at DESC LIMIT 1",
					empId);
			if (subs.isEmpty()) {
				// still check approved existence
				String approvedSid = latestApprovedSubmissionId(empId);
				boolean hasApproved = StringUtils.hasText(approvedSid);
				return new SkillMatrixSubmitLockStatusDTO(false, hasApproved, approvedSid, null, null, null, null, null, null, null);
			}
			String submissionId = String.valueOf(subs.get(0).get("submission_id"));
			String status = subs.get(0).get("status") != null ? String.valueOf(subs.get(0).get("status")) : null;
			String st = status != null ? status.trim().toLowerCase(Locale.ROOT) : "";
			boolean locked = "submitted".equals(st) || "under_review".equals(st);
			String approvedSid = latestApprovedSubmissionId(empId);
			boolean hasApproved = StringUtils.hasText(approvedSid);
			String mgrName = subs.get(0).get("reporting_manager_name") != null ? String.valueOf(subs.get(0).get("reporting_manager_name")) : null;
			String hodName = subs.get(0).get("hod_name") != null ? String.valueOf(subs.get(0).get("hod_name")) : null;

			String mgrApproved = null;
			String hodApproved = null;
			String fin = null;
			try {
				List<Map<String, Object>> appr = jdbcTemplate.queryForList(
						"SELECT manager_approved, hod_approved, final_status, manager_name, hod_name "
								+ "FROM skillmatrix_assessment_approval WHERE submission_id = ? "
								+ "ORDER BY revision_round DESC LIMIT 1",
						submissionId);
				if (!appr.isEmpty()) {
					Map<String, Object> a = appr.get(0);
					mgrApproved = a.get("manager_approved") != null ? String.valueOf(a.get("manager_approved")) : null;
					hodApproved = a.get("hod_approved") != null ? String.valueOf(a.get("hod_approved")) : null;
					fin = a.get("final_status") != null ? String.valueOf(a.get("final_status")) : null;
					if (a.get("manager_name") != null) {
						mgrName = String.valueOf(a.get("manager_name"));
					}
					if (a.get("hod_name") != null) {
						hodName = String.valueOf(a.get("hod_name"));
					}
				}
			} catch (Exception ignore) {
			}
			if (!locked) {
				// Not "under review" lock, but still return latest final_status so frontend can block "Rejected" flow.
				return new SkillMatrixSubmitLockStatusDTO(false, hasApproved, approvedSid, submissionId, status, mgrApproved, hodApproved, fin, null, null);
			}

			String pendingWith = null;
			String pendingWithName = null;
			if (!"yes".equalsIgnoreCase(String.valueOf(mgrApproved))) {
				pendingWith = "MANAGER";
				pendingWithName = mgrName;
			} else if ("pending".equalsIgnoreCase(String.valueOf(hodApproved)) && "pending".equalsIgnoreCase(String.valueOf(fin))) {
				pendingWith = "HOD";
				pendingWithName = hodName;
			} else {
				// fallback when status is locked but approvals are not consistent
				pendingWith = "MANAGER";
				pendingWithName = mgrName;
			}
			return new SkillMatrixSubmitLockStatusDTO(true, hasApproved, approvedSid, submissionId, status, mgrApproved, hodApproved, fin, pendingWith, pendingWithName);
		} catch (Exception ex) {
			throw new IllegalStateException("Could not load submission status.", ex);
		}
	}

	private String latestApprovedSubmissionId(Long empId) {
		try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(
					"SELECT submission_id FROM skillmatrix_assessment_submission WHERE employee_id=? AND status='approved' ORDER BY approved_at DESC, updated_at DESC LIMIT 1",
					empId);
			if (rows.isEmpty()) {
				return null;
			}
			Object v = rows.get(0).get("submission_id");
			return v != null ? String.valueOf(v) : null;
		} catch (Exception ignore) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public SkillMatrixSubmitDraftRequest loadEditableDraftBySubmissionId(Long empId, String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		String sid = submissionId.trim();
		List<Map<String, Object>> subs = jdbcTemplate.queryForList(
				"SELECT submission_id FROM skillmatrix_assessment_submission "
						+ "WHERE submission_id = ? AND employee_id = ? AND status IN ('draft','rejected','changes_requested') "
						+ "LIMIT 1",
				sid, empId);
		if (subs.isEmpty()) {
			throw new IllegalArgumentException("Submission not found or not editable.");
		}
		return loadDraftBySubmissionIdInternal(sid);
	}

	private SkillMatrixSubmitDraftRequest loadDraftBySubmissionIdInternal(String submissionId) {
		SkillMatrixSubmitDraftRequest out = new SkillMatrixSubmitDraftRequest();
		out.setSubmissionId(submissionId);
		/* assessment_cycle / cycle_year are stored in DB, but not part of current submit UI payload. */

		// Review notes may live in approval_skill table (latest revision round).
		final Map<Integer, Map<String, Object>> approvalNotesBySkillId = new java.util.HashMap<>();
		try {
			List<Map<String, Object>> appr = jdbcTemplate.queryForList(
					"SELECT id FROM skillmatrix_assessment_approval WHERE submission_id=? ORDER BY revision_round DESC LIMIT 1",
					submissionId);
			Long approvalId = !appr.isEmpty() && appr.get(0).get("id") != null ? ((Number) appr.get(0).get("id")).longValue() : null;
			if (approvalId != null) {
				List<Map<String, Object>> aks = jdbcTemplate.queryForList(
						"SELECT skill_id, manager_decision, manager_comment, hod_decision, hod_comment "
								+ "FROM skillmatrix_assessment_approval_skill WHERE approval_id=?",
						approvalId);
				for (Map<String, Object> r : aks) {
					Integer sid = r.get("skill_id") != null ? Integer.valueOf(r.get("skill_id").toString()) : null;
					if (sid != null) {
						approvalNotesBySkillId.put(sid, r);
					}
				}
			}
		} catch (Exception ignore) {
		}

		List<Map<String, Object>> skills = jdbcTemplate.queryForList(
				"SELECT id, skill_id, skill_name, is_required, self_rating, manager_decision, manager_comment, "
						+ "years_experience, last_used, usage_frequency, what_can_you_do, used_in_project, github_portfolio_url, "
						+ "colleague_endorser, knowledge_session_note "
						+ "FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?",
				submissionId);

		// Certifications (Step 3) grouped by skill_rating_id
		final Map<Long, List<Map<String, Object>>> certByRatingId = new java.util.HashMap<>();
		try {
			List<Map<String, Object>> certRows = jdbcTemplate.queryForList(
					"SELECT skill_rating_id, cert_name, issuing_body, date_obtained, expiry_type, expiry_date, credential_id, credential_url, "
							+ "file_reference_key, original_filename, file_size_bytes, file_mime_type "
							+ "FROM skillmatrix_assessment_certification WHERE submission_id = ? ORDER BY id ASC",
					submissionId);
			for (Map<String, Object> r : certRows) {
				Long rid = r.get("skill_rating_id") != null ? ((Number) r.get("skill_rating_id")).longValue() : null;
				if (rid == null) {
					continue;
				}
				certByRatingId.computeIfAbsent(rid, k -> new java.util.ArrayList<>()).add(r);
			}
		} catch (Exception ignore) {
		}

		// Trainings (Step 3) grouped by skill_rating_id
		final Map<Long, List<Map<String, Object>>> trainByRatingId = new java.util.HashMap<>();
		try {
			List<Map<String, Object>> trRows = jdbcTemplate.queryForList(
					"SELECT skill_rating_id, course_name, platform_institute, completion_year "
							+ "FROM skillmatrix_assessment_training WHERE submission_id = ? ORDER BY id ASC",
					submissionId);
			for (Map<String, Object> r : trRows) {
				Long rid = r.get("skill_rating_id") != null ? ((Number) r.get("skill_rating_id")).longValue() : null;
				if (rid == null) {
					continue;
				}
				trainByRatingId.computeIfAbsent(rid, k -> new java.util.ArrayList<>()).add(r);
			}
		} catch (Exception ignore) {
		}

		List<SkillMatrixSubmitDraftSkillDTO> skillDtos = new ArrayList<>();
		for (Map<String, Object> s : skills) {
			Long ratingId = s.get("id") != null ? Long.valueOf(s.get("id").toString()) : null;
			Integer skillId = s.get("skill_id") != null ? Integer.valueOf(s.get("skill_id").toString()) : null;
			String skillName = (String) s.get("skill_name");
			boolean required = jdbcBool(s.get("is_required"));
			Integer selfRating = s.get("self_rating") != null ? Integer.valueOf(s.get("self_rating").toString()) : null;
			String managerDecision = s.get("manager_decision") != null ? String.valueOf(s.get("manager_decision")) : null;
			String managerComment = s.get("manager_comment") != null ? String.valueOf(s.get("manager_comment")) : null;
			String hodDecision = null;
			String hodComment = null;
			if (skillId != null && (!StringUtils.hasText(managerDecision) || !StringUtils.hasText(managerComment))) {
				Map<String, Object> n = approvalNotesBySkillId.get(skillId);
				if (n != null) {
					if (!StringUtils.hasText(managerDecision) && n.get("manager_decision") != null) {
						managerDecision = String.valueOf(n.get("manager_decision"));
					}
					if (!StringUtils.hasText(managerComment) && n.get("manager_comment") != null) {
						managerComment = String.valueOf(n.get("manager_comment"));
					}
					if (n.get("hod_decision") != null) {
						hodDecision = String.valueOf(n.get("hod_decision"));
					}
					if (n.get("hod_comment") != null) {
						hodComment = String.valueOf(n.get("hod_comment"));
					}
				}
			} else if (skillId != null) {
				Map<String, Object> n = approvalNotesBySkillId.get(skillId);
				if (n != null) {
					if (n.get("hod_decision") != null) {
						hodDecision = String.valueOf(n.get("hod_decision"));
					}
					if (n.get("hod_comment") != null) {
						hodComment = String.valueOf(n.get("hod_comment"));
					}
				}
			}
			String yearsExperience = s.get("years_experience") != null ? String.valueOf(s.get("years_experience")) : null;
			String lastUsed = s.get("last_used") != null ? String.valueOf(s.get("last_used")) : null;
			String usageFrequency = s.get("usage_frequency") != null ? String.valueOf(s.get("usage_frequency")) : null;
			String what = (String) s.get("what_can_you_do");
			boolean usedInProject = jdbcBool(s.get("used_in_project"));
			String github = s.get("github_portfolio_url") != null ? String.valueOf(s.get("github_portfolio_url")) : null;
			String endorser = s.get("colleague_endorser") != null ? String.valueOf(s.get("colleague_endorser")) : null;
			String ks = s.get("knowledge_session_note") != null ? String.valueOf(s.get("knowledge_session_note")) : null;
			SkillMatrixSubmitDraftSkillDTO dto = new SkillMatrixSubmitDraftSkillDTO();
			dto.setSkillId(skillId);
			dto.setSkillName(skillName);
			dto.setRequired(required);
			dto.setSelfRating(selfRating);
			dto.setManagerDecision(managerDecision);
			dto.setManagerComment(managerComment);
			dto.setHodDecision(hodDecision);
			dto.setHodComment(hodComment);
			dto.setYearsExperience(yearsExperience);
			dto.setLastUsed(lastUsed);
			dto.setUsageFrequency(usageFrequency);
			dto.setWhatCanYouDo(what);
			dto.setUsedInProject(usedInProject);
			dto.setGithubPortfolioUrl(github);
			dto.setColleagueEndorser(endorser);
			dto.setKnowledgeSessionNote(ks);
			if (ratingId != null) {
				List<Map<String, Object>> certs = certByRatingId.get(ratingId);
				if (certs != null) {
					for (Map<String, Object> c : certs) {
						SkillMatrixSubmitCertificationDTO cdto = new SkillMatrixSubmitCertificationDTO();
						cdto.setCertName(c.get("cert_name") != null ? String.valueOf(c.get("cert_name")) : null);
						cdto.setIssuingBody(c.get("issuing_body") != null ? String.valueOf(c.get("issuing_body")) : null);
						cdto.setDateObtained(c.get("date_obtained") != null ? String.valueOf(c.get("date_obtained")) : null);
						cdto.setExpiryType(c.get("expiry_type") != null ? String.valueOf(c.get("expiry_type")) : null);
						cdto.setExpiryDate(c.get("expiry_date") != null ? String.valueOf(c.get("expiry_date")) : null);
						cdto.setCredentialId(c.get("credential_id") != null ? String.valueOf(c.get("credential_id")) : null);
						cdto.setCredentialUrl(c.get("credential_url") != null ? String.valueOf(c.get("credential_url")) : null);
						cdto.setFileReferenceKey(c.get("file_reference_key") != null ? String.valueOf(c.get("file_reference_key")) : null);
						cdto.setOriginalFilename(c.get("original_filename") != null ? String.valueOf(c.get("original_filename")) : null);
						cdto.setFileSizeBytes(c.get("file_size_bytes") != null ? ((Number) c.get("file_size_bytes")).intValue() : null);
						cdto.setFileMimeType(c.get("file_mime_type") != null ? String.valueOf(c.get("file_mime_type")) : null);
						dto.getCertifications().add(cdto);
					}
				}
			}
			if (ratingId != null) {
				List<Map<String, Object>> trs = trainByRatingId.get(ratingId);
				if (trs != null) {
					for (Map<String, Object> t : trs) {
						SkillMatrixSubmitTrainingDTO tdto = new SkillMatrixSubmitTrainingDTO();
						tdto.setCourseName(t.get("course_name") != null ? String.valueOf(t.get("course_name")) : null);
						tdto.setPlatformInstitute(
								t.get("platform_institute") != null ? String.valueOf(t.get("platform_institute")) : null);
						tdto.setCompletionYear(t.get("completion_year") != null ? ((Number) t.get("completion_year")).intValue() : null);
						dto.getTrainings().add(tdto);
					}
				}
			}
			if (ratingId != null) {
				List<Map<String, Object>> subsSel = jdbcTemplate.queryForList(
						"SELECT subskill_id, subskill_name FROM skillmatrix_assessment_subskill_selection WHERE skill_rating_id = ?",
						ratingId);
				List<SkillMatrixSubmitSubskillDTO> selected = subsSel.stream()
						.map(r -> new SkillMatrixSubmitSubskillDTO(
								r.get("subskill_id") != null ? Integer.valueOf(r.get("subskill_id").toString()) : null,
								(String) r.get("subskill_name")))
						.collect(Collectors.toList());
				dto.setSelectedSubskills(selected);
			}
			skillDtos.add(dto);
		}
		out.setSkills(skillDtos);

		List<Map<String, Object>> prows = jdbcTemplate.queryForList(
				"SELECT id, project_source, hrms_project_id, project_name, client_or_type, project_status, duration_text, employee_role, allocation_pct, contribution_summary, is_included, "
						+ "domain_specific, skill_domain_id, skill_subdomain_id, skill_domain_feature_id "
						+ "FROM skillmatrix_assessment_project WHERE submission_id = ? ORDER BY id ASC",
				submissionId);
		List<SkillMatrixSubmitDraftProjectDTO> projects = new ArrayList<>();
		for (Map<String, Object> p : prows) {
			Long pid = p.get("id") != null ? Long.valueOf(p.get("id").toString()) : null;
			SkillMatrixSubmitDraftProjectDTO dto = new SkillMatrixSubmitDraftProjectDTO();
			dto.setAssessmentProjectId(pid);
			dto.setProjectSource((String) p.get("project_source"));
			dto.setHrmsProjectId(p.get("hrms_project_id") != null ? Integer.valueOf(p.get("hrms_project_id").toString()) : null);
			dto.setProjectName((String) p.get("project_name"));
			dto.setClientOrType((String) p.get("client_or_type"));
			dto.setProjectStatus((String) p.get("project_status"));
			dto.setDurationText((String) p.get("duration_text"));
			dto.setEmployeeRole((String) p.get("employee_role"));
			dto.setAllocationPct(p.get("allocation_pct") != null ? Integer.valueOf(p.get("allocation_pct").toString()) : null);
			dto.setContributionSummary((String) p.get("contribution_summary"));
			dto.setIncluded(p.get("is_included") == null || jdbcBool(p.get("is_included")));
			dto.setDomainSpecific(jdbcBool(p.get("domain_specific")));
			dto.setSkillDomainId(p.get("skill_domain_id") != null ? Integer.valueOf(p.get("skill_domain_id").toString()) : null);
			dto.setSkillSubdomainId(p.get("skill_subdomain_id") != null ? Integer.valueOf(p.get("skill_subdomain_id").toString()) : null);
			dto.setSkillDomainFeatureId(
					p.get("skill_domain_feature_id") != null ? Integer.valueOf(p.get("skill_domain_feature_id").toString()) : null);
			if (pid != null) {
				List<Map<String, Object>> ps = jdbcTemplate.queryForList(
						"SELECT skill_id, skill_name, level_used, specific_contribution FROM skillmatrix_assessment_project_skill WHERE assessment_project_id = ?",
						pid);
				List<SkillMatrixSubmitDraftProjectSkillDTO> applied = ps.stream()
						.map(r -> new SkillMatrixSubmitDraftProjectSkillDTO(
								r.get("skill_id") != null ? Integer.valueOf(r.get("skill_id").toString()) : null,
								(String) r.get("skill_name"),
								r.get("level_used") != null ? Integer.valueOf(r.get("level_used").toString()) : null,
								(String) r.get("specific_contribution")))
						.collect(Collectors.toList());
				dto.setSkillsApplied(applied);
			}
			projects.add(dto);
		}
		out.setProjects(projects);

		List<Map<String, Object>> asp = jdbcTemplate.queryForList(
				"SELECT id, target_role_2yr, message_to_manager FROM skillmatrix_assessment_aspiration WHERE submission_id = ? LIMIT 1",
				submissionId);
		if (!asp.isEmpty()) {
			out.setTargetRole2yr((String) asp.get(0).get("target_role_2yr"));
			out.setMessageToManager((String) asp.get(0).get("message_to_manager"));
			Long aspId = asp.get(0).get("id") != null ? Long.valueOf(asp.get(0).get("id").toString()) : null;
			if (aspId != null) {
				List<Map<String, Object>> ask = jdbcTemplate.queryForList(
						"SELECT skill_name FROM skillmatrix_assessment_aspiration_skill WHERE aspiration_id = ? ORDER BY id ASC",
						aspId);
				out.setAspirationSkillNames(ask.stream().map(r -> (String) r.get("skill_name")).collect(Collectors.toList()));
			}
		}

		return out;
	}

	@Transactional
	public String saveDraft(Long empId, SkillMatrixSubmitDraftRequest req) {
		if (req == null) {
			throw new IllegalArgumentException("Draft payload is required.");
		}
		// Hard lock: do not allow any draft/edit while latest submission is under review.
		SkillMatrixSubmitLockStatusDTO lock = getSubmitLockStatus(empId);
		if (lock != null && lock.isLocked()) {
			throw new IllegalStateException(
					"Your data is currently under review. Please wait for the decision. You cannot perform any further actions until the review process is completed.");
		}
		Employee emp = employeeRepository.findByEmpId(empId);
		if (emp == null) {
			throw new IllegalStateException("Employee record not found.");
		}
		Long deptId = resolveDepartmentIdForEmp(empId);
		String deptName = deptId != null ? departmentRepository.findById(deptId).map(Department::getName).orElse(null) : null;
		String designation = resolveDesignationName(emp, emp.getJobRoleId() != null ? jobRoleRepository.findByjobRoleId(emp.getJobRoleId()) : null);
		Long mgrId = emp.getManagerId() != null ? emp.getManagerId() : emp.getReportingManagerId();
		String mgrName = mgrId != null ? resolveManagerName(emp) : null;

		// DB schema requires these header fields to be NOT NULL.
		// When HRMS mappings are missing (dept/manager), we must still allow drafts to save.
		final long safeDeptId = deptId != null ? deptId.longValue() : 0L;
		final String safeDeptName = deptName != null ? deptName : "";
		final String safeDesignation = designation != null ? designation : "";
		final long safeMgrId = mgrId != null ? mgrId.longValue() : 0L;
		final String safeMgrName = mgrName != null ? mgrName : "";

		// Resolve HOD for the employee's department (store on submission header for reporting).
		long safeHodId = 0L;
		String safeHodName = "HOD";
		try {
			if (deptId != null) {
				Department d = departmentRepository.findByDeptId(deptId);
				if (d != null && d.getHodId() != null) {
					safeHodId = d.getHodId();
					Employee hod = employeeRepository.findByEmpId(safeHodId);
					if (hod != null && StringUtils.hasText(hod.getName())) {
						safeHodName = hod.getName().trim();
					}
				}
			}
		} catch (Exception ignore) {
		}

		int year = java.time.LocalDate.now(ZoneId.of("Asia/Kolkata")).getYear();
		String cycle = "Skill Matrix";
		String submissionId = StringUtils.hasText(req.getSubmissionId()) ? req.getSubmissionId().trim() : null;
		final String baseId = String.format("SKA-%d-%d", year, empId);
		if (!StringUtils.hasText(submissionId)) {
			submissionId = baseId;
		}

		List<Map<String, Object>> existing = jdbcTemplate.queryForList(
				"SELECT status FROM skillmatrix_assessment_submission WHERE submission_id = ?",
				submissionId);
		if (!existing.isEmpty()) {
			String st = String.valueOf(existing.get(0).get("status"));
			if ("submitted".equalsIgnoreCase(st) || "under_review".equalsIgnoreCase(st) || "approved".equalsIgnoreCase(st)) {
				// If client did not explicitly request editing an existing submissionId,
				// create a new draft id with a suffix instead of failing.
				if (!StringUtils.hasText(req.getSubmissionId())) {
					int next = 2;
					try {
						Integer mx = jdbcTemplate.queryForObject(
								"SELECT MAX(CAST(SUBSTRING_INDEX(submission_id,'-',-1) AS UNSIGNED)) "
										+ "FROM skillmatrix_assessment_submission WHERE submission_id LIKE ?",
								new Object[] { baseId + "-%" }, Integer.class);
						if (mx != null && mx.intValue() >= 2) {
							next = mx.intValue() + 1;
						}
					} catch (Exception ignore) {
					}
					boolean inserted = false;
					for (int i = 0; i < 25 && !inserted; i++) {
						String candidate = baseId + "-" + (next + i);
						try {
							jdbcTemplate.update(
									"INSERT INTO skillmatrix_assessment_submission(submission_id, employee_id, employee_name, dept_id, dept_name, designation, reporting_manager_id, reporting_manager_name, hod_id, hod_name, assessment_cycle, cycle_year, status) "
											+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?, 'draft')",
									candidate, empId, emp.getName(), safeDeptId, safeDeptName, safeDesignation, safeMgrId, safeMgrName, safeHodId,
									safeHodName,
									cycle, year);
							submissionId = candidate;
							inserted = true;
						} catch (org.springframework.dao.DataIntegrityViolationException dup) {
							// try next suffix
						}
					}
					if (!inserted) {
						throw new IllegalStateException("Could not create a new draft. Please try again.");
					}
				} else {
					throw new IllegalStateException("Submission is already submitted and cannot be edited.");
				}
			}
			// If we successfully created a new draft candidate above, do not update the old submitted row.
			if (submissionId != null && submissionId.equals(baseId)) {
				// keep old behavior (editable statuses)
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_submission SET employee_name=?, dept_id=?, dept_name=?, designation=?, reporting_manager_id=?, reporting_manager_name=?, hod_id=?, hod_name=?, experience_in_role=?, assessment_cycle=?, cycle_year=?, status='draft' WHERE submission_id=?",
						emp.getName(), safeDeptId, safeDeptName, safeDesignation, safeMgrId, safeMgrName, safeHodId, safeHodName,
						req.getCurrentStep() != null ? null : null, cycle, year, submissionId);
			} else if (StringUtils.hasText(req.getSubmissionId())) {
				// explicit edit of an editable submissionId
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_submission SET employee_name=?, dept_id=?, dept_name=?, designation=?, reporting_manager_id=?, reporting_manager_name=?, hod_id=?, hod_name=?, experience_in_role=?, assessment_cycle=?, cycle_year=?, status='draft' WHERE submission_id=?",
						emp.getName(), safeDeptId, safeDeptName, safeDesignation, safeMgrId, safeMgrName, safeHodId, safeHodName,
						req.getCurrentStep() != null ? null : null, cycle, year, submissionId);
			}
		} else {
			jdbcTemplate.update(
					"INSERT INTO skillmatrix_assessment_submission(submission_id, employee_id, employee_name, dept_id, dept_name, designation, reporting_manager_id, reporting_manager_name, hod_id, hod_name, assessment_cycle, cycle_year, status) "
							+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?, 'draft')",
					submissionId, empId, emp.getName(), safeDeptId, safeDeptName, safeDesignation, safeMgrId, safeMgrName, safeHodId, safeHodName,
					cycle, year);
		}

		final String submissionIdFinal = submissionId;

		// Preserve manager review notes for rejected/changes-requested edit flows.
		// We overwrite skill rows (delete+insert), so we need to carry forward manager_decision/comment by skill_id.
		final Map<Integer, Map<String, Object>> prevMgrNotesBySkillId = new java.util.HashMap<>();
		try {
			List<Map<String, Object>> prev = jdbcTemplate.queryForList(
					"SELECT skill_id, manager_decision, manager_comment "
							+ "FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?",
					submissionId);
			for (Map<String, Object> r : prev) {
				Integer sid = r.get("skill_id") != null ? Integer.valueOf(r.get("skill_id").toString()) : null;
				if (sid == null) {
					continue;
				}
				// keep only when there is something to show
				Object md = r.get("manager_decision");
				Object mc = r.get("manager_comment");
				if (md != null || mc != null) {
					prevMgrNotesBySkillId.put(sid, r);
				}
			}
		} catch (Exception ignore) {
			// If anything goes wrong, draft save should still proceed.
		}

		// overwrite children
		jdbcTemplate.update("DELETE FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?", submissionIdFinal);
		jdbcTemplate.update("DELETE FROM skillmatrix_assessment_training WHERE submission_id = ?", submissionIdFinal);
		jdbcTemplate.update("DELETE FROM skillmatrix_assessment_certification WHERE submission_id = ?", submissionIdFinal);
		jdbcTemplate.update("DELETE FROM skillmatrix_assessment_project WHERE submission_id = ?", submissionIdFinal);
		jdbcTemplate.update("DELETE FROM skillmatrix_assessment_aspiration WHERE submission_id = ?", submissionIdFinal);

		// skills
		if (req.getSkills() != null) {
			for (SkillMatrixSubmitDraftSkillDTO s : req.getSkills()) {
				if (s.getSkillId() == null) {
					continue;
				}
				KeyHolder kh = new GeneratedKeyHolder();
				jdbcTemplate.update(con -> {
					var ps = con.prepareStatement(
							"INSERT INTO skillmatrix_assessment_skill_rating("
									+ "submission_id, skill_id, skill_name, dept_id, is_required, self_rating, "
									+ "years_experience, last_used, usage_frequency, what_can_you_do, "
									+ "used_in_project, github_portfolio_url, colleague_endorser, knowledge_session_note"
									+ ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new String[] { "id" });
					ps.setString(1, submissionIdFinal);
					ps.setInt(2, s.getSkillId());
					ps.setString(3, s.getSkillName());
					ps.setLong(4, safeDeptId);
					ps.setBoolean(5, s.isRequired());
					ps.setInt(6, s.getSelfRating() != null ? s.getSelfRating() : 0);
					ps.setString(7, StringUtils.hasText(s.getYearsExperience()) ? s.getYearsExperience() : null);
					ps.setString(8, StringUtils.hasText(s.getLastUsed()) ? s.getLastUsed() : null);
					ps.setString(9, StringUtils.hasText(s.getUsageFrequency()) ? s.getUsageFrequency() : null);
					ps.setString(10, StringUtils.hasText(s.getWhatCanYouDo()) ? s.getWhatCanYouDo() : "");
					ps.setBoolean(11, s.getUsedInProject() != null && s.getUsedInProject());
					ps.setString(12, StringUtils.hasText(s.getGithubPortfolioUrl()) ? s.getGithubPortfolioUrl() : null);
					ps.setString(13, StringUtils.hasText(s.getColleagueEndorser()) ? s.getColleagueEndorser() : null);
					ps.setString(14, StringUtils.hasText(s.getKnowledgeSessionNote()) ? s.getKnowledgeSessionNote() : null);
					return ps;
				}, kh);
				Long ratingId = kh.getKey() != null ? kh.getKey().longValue() : null;
				// Restore manager notes (if any) for this skill so rejection reasons stay visible after Save as draft.
				if (ratingId != null && s.getSkillId() != null) {
					Map<String, Object> prevNote = prevMgrNotesBySkillId.get(s.getSkillId());
					if (prevNote != null) {
						String md = prevNote.get("manager_decision") != null ? String.valueOf(prevNote.get("manager_decision")) : null;
						String mc = prevNote.get("manager_comment") != null ? String.valueOf(prevNote.get("manager_comment")) : null;
						if (md != null || mc != null) {
							jdbcTemplate.update(
									"UPDATE skillmatrix_assessment_skill_rating SET manager_decision=?, manager_comment=? WHERE id=?",
									md, mc, ratingId);
						}
					}
				}
				if (ratingId != null && s.getSelectedSubskills() != null) {
					for (SkillMatrixSubmitSubskillDTO sub : s.getSelectedSubskills()) {
						if (sub.getSubskillId() == null) {
							continue;
						}
						jdbcTemplate.update(
								"INSERT INTO skillmatrix_assessment_subskill_selection(skill_rating_id, submission_id, skill_id, subskill_id, subskill_name, is_confident) VALUES(?,?,?,?,?,1)",
								ratingId, submissionIdFinal, s.getSkillId(), sub.getSubskillId(), sub.getSubskillName());
					}
				}

				// Certifications (Step 3)
				if (ratingId != null && s.getCertifications() != null) {
					for (SkillMatrixSubmitCertificationDTO c : s.getCertifications()) {
						if (!StringUtils.hasText(c.getCertName()) || !StringUtils.hasText(c.getIssuingBody())
								|| !StringUtils.hasText(c.getDateObtained())) {
							continue;
						}
						java.sql.Date dob;
						try {
							dob = java.sql.Date.valueOf(java.time.LocalDate.parse(c.getDateObtained().trim()));
						} catch (Exception ignore) {
							continue;
						}
						String expiryType = StringUtils.hasText(c.getExpiryType()) ? c.getExpiryType().trim() : null;
						java.sql.Date expiryDate = null;
						if (StringUtils.hasText(c.getExpiryDate())) {
							try {
								expiryDate = java.sql.Date.valueOf(java.time.LocalDate.parse(c.getExpiryDate().trim()));
							} catch (Exception ignore) {
								expiryDate = null;
							}
						} else if (expiryType != null) {
							try {
								java.time.LocalDate ld = dob.toLocalDate();
								if ("1 year".equalsIgnoreCase(expiryType) || "1_year".equalsIgnoreCase(expiryType)) {
									expiryDate = java.sql.Date.valueOf(ld.plusYears(1));
									expiryType = "1_year";
								} else if ("2 years".equalsIgnoreCase(expiryType) || "2_year".equalsIgnoreCase(expiryType)) {
									expiryDate = java.sql.Date.valueOf(ld.plusYears(2));
									expiryType = "2_year";
								} else if ("3 years".equalsIgnoreCase(expiryType) || "3_year".equalsIgnoreCase(expiryType)) {
									expiryDate = java.sql.Date.valueOf(ld.plusYears(3));
									expiryType = "3_year";
								} else if ("no expiry / lifetime".equalsIgnoreCase(expiryType) || "lifetime".equalsIgnoreCase(expiryType)
										|| "no expiry".equalsIgnoreCase(expiryType)) {
									expiryDate = null;
									expiryType = "lifetime";
								}
							} catch (Exception ignore) {
							}
						}

						String credId = StringUtils.hasText(c.getCredentialId()) ? c.getCredentialId().trim() : null;
						String credUrl = StringUtils.hasText(c.getCredentialUrl()) ? c.getCredentialUrl().trim() : null;
						if (credUrl == null && credId != null && (credId.startsWith("http://") || credId.startsWith("https://"))) {
							credUrl = credId;
							credId = null;
						}

						boolean fileUploaded = StringUtils.hasText(c.getFileReferenceKey());
						jdbcTemplate.update(
								"INSERT INTO skillmatrix_assessment_certification("
										+ "skill_rating_id, submission_id, skill_id, cert_name, issuing_body, date_obtained, "
										+ "expiry_type, expiry_date, credential_id, credential_url, "
										+ "file_uploaded, file_reference_key, original_filename, file_size_bytes, file_mime_type, uploaded_at"
										+ ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, NOW())",
								ratingId, submissionIdFinal, s.getSkillId(),
								c.getCertName().trim(), c.getIssuingBody().trim(), dob,
								expiryType, expiryDate, credId, credUrl,
								fileUploaded ? 1 : 0,
								fileUploaded ? c.getFileReferenceKey().trim() : null,
								StringUtils.hasText(c.getOriginalFilename()) ? c.getOriginalFilename().trim() : null,
								c.getFileSizeBytes(),
								StringUtils.hasText(c.getFileMimeType()) ? c.getFileMimeType().trim() : null);
					}
				}

				// Trainings (Step 3)
				if (ratingId != null && s.getTrainings() != null) {
					for (SkillMatrixSubmitTrainingDTO t : s.getTrainings()) {
						if (!StringUtils.hasText(t.getCourseName())) {
							continue;
						}
						String course = t.getCourseName().trim();
						String platform = StringUtils.hasText(t.getPlatformInstitute()) ? t.getPlatformInstitute().trim() : null;
						Integer completionYear = t.getCompletionYear();
						if (completionYear != null && (completionYear < 1900 || completionYear > 2500)) {
							completionYear = null;
						}
						jdbcTemplate.update(
								"INSERT INTO skillmatrix_assessment_training(skill_rating_id, submission_id, skill_id, course_name, platform_institute, completion_year) "
										+ "VALUES(?,?,?,?,?,?)",
								ratingId, submissionIdFinal, s.getSkillId(), course, platform, completionYear);
					}
				}
			}
		}

		// projects
		if (req.getProjects() != null) {
			for (SkillMatrixSubmitDraftProjectDTO p : req.getProjects()) {
				if (!StringUtils.hasText(p.getProjectName())) {
					continue;
				}
				java.sql.Date projStart = null;
				java.sql.Date projEnd = null;
				// Prefer dates coming from UI payload (yyyy-MM-dd).
				try {
					if (StringUtils.hasText(p.getStartDate())) {
						java.time.LocalDate ld = java.time.LocalDate.parse(p.getStartDate().trim());
						projStart = java.sql.Date.valueOf(ld);
					}
					if (StringUtils.hasText(p.getEndDate())) {
						java.time.LocalDate ld = java.time.LocalDate.parse(p.getEndDate().trim());
						projEnd = java.sql.Date.valueOf(ld);
					}
				} catch (Exception ignore) {
					// fallback to HRMS table lookup below
					projStart = null;
					projEnd = null;
				}
				if (p.getHrmsProjectId() != null) {
					try {
						Map<String, Object> prow = jdbcTemplate.queryForMap(
								"SELECT start_date, end_date FROM projects WHERE project_id = ?",
								p.getHrmsProjectId());
						Object sv = prow.get("start_date");
						Object ev = prow.get("end_date");
						if (projStart == null && sv instanceof java.sql.Date) {
							projStart = (java.sql.Date) sv;
						} else if (projStart == null && sv instanceof java.util.Date) {
							projStart = new java.sql.Date(((java.util.Date) sv).getTime());
						} else if (projStart == null && sv instanceof String && StringUtils.hasText((String) sv)) {
							try {
								projStart = java.sql.Date.valueOf(java.time.LocalDate.parse(((String) sv).trim()));
							} catch (Exception ignore2) {
							}
						}
						if (projEnd == null && ev instanceof java.sql.Date) {
							projEnd = (java.sql.Date) ev;
						} else if (projEnd == null && ev instanceof java.util.Date) {
							projEnd = new java.sql.Date(((java.util.Date) ev).getTime());
						} else if (projEnd == null && ev instanceof String && StringUtils.hasText((String) ev)) {
							try {
								projEnd = java.sql.Date.valueOf(java.time.LocalDate.parse(((String) ev).trim()));
							} catch (Exception ignore2) {
							}
						}
					} catch (EmptyResultDataAccessException ignore) {
						// keep nulls when project row is missing
					}
				}
				final java.sql.Date projStartFinal = projStart;
				final java.sql.Date projEndFinal = projEnd;
				KeyHolder kh = new GeneratedKeyHolder();
				jdbcTemplate.update(con -> {
					var ps = con.prepareStatement(
							"INSERT INTO skillmatrix_assessment_project(submission_id, project_source, hrms_project_id, project_name, client_or_type, project_status, start_date, end_date, duration_text, employee_role, allocation_pct, contribution_summary, is_included, domain_specific, skill_domain_id, skill_subdomain_id, skill_domain_feature_id) "
									+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new String[] { "id" });
					ps.setString(1, submissionIdFinal);
					ps.setString(2, StringUtils.hasText(p.getProjectSource()) ? p.getProjectSource() : "hrms");
					if (p.getHrmsProjectId() != null) {
						ps.setInt(3, p.getHrmsProjectId());
					} else {
						ps.setNull(3, java.sql.Types.INTEGER);
					}
					ps.setString(4, p.getProjectName());
					ps.setString(5, p.getClientOrType());
					ps.setString(6, p.getProjectStatus());
					if (projStartFinal != null) {
						ps.setDate(7, projStartFinal);
					} else {
						ps.setNull(7, java.sql.Types.DATE);
					}
					if (projEndFinal != null) {
						ps.setDate(8, projEndFinal);
					} else {
						ps.setNull(8, java.sql.Types.DATE);
					}
					ps.setString(9, p.getDurationText());
					ps.setString(10, p.getEmployeeRole());
					if (p.getAllocationPct() != null) {
						ps.setInt(11, p.getAllocationPct());
					} else {
						ps.setNull(11, java.sql.Types.INTEGER);
					}
					ps.setString(12, StringUtils.hasText(p.getContributionSummary()) ? p.getContributionSummary() : "");
					ps.setBoolean(13, p.isIncluded());
					boolean domainSpecific = p.isDomainSpecific();
					ps.setBoolean(14, domainSpecific);
					if (domainSpecific && p.getSkillDomainId() != null) {
						ps.setInt(15, p.getSkillDomainId());
					} else {
						ps.setNull(15, java.sql.Types.INTEGER);
					}
					if (domainSpecific && p.getSkillSubdomainId() != null) {
						ps.setInt(16, p.getSkillSubdomainId());
					} else {
						ps.setNull(16, java.sql.Types.INTEGER);
					}
					if (domainSpecific && p.getSkillDomainFeatureId() != null) {
						ps.setInt(17, p.getSkillDomainFeatureId());
					} else {
						ps.setNull(17, java.sql.Types.INTEGER);
					}
					return ps;
				}, kh);
				Long projId = kh.getKey() != null ? kh.getKey().longValue() : null;
				if (projId != null && p.getSkillsApplied() != null) {
					for (SkillMatrixSubmitDraftProjectSkillDTO r : p.getSkillsApplied()) {
						if (r.getSkillId() == null) {
							continue;
						}
						jdbcTemplate.update(
								"INSERT INTO skillmatrix_assessment_project_skill(assessment_project_id, submission_id, skill_id, skill_name, level_used, specific_contribution) VALUES(?,?,?,?,?,?)",
								projId, submissionIdFinal, r.getSkillId(), r.getSkillName(),
								r.getLevelUsed() != null ? r.getLevelUsed() : 0,
								StringUtils.hasText(r.getSpecificContribution()) ? r.getSpecificContribution() : "");
					}
				}
			}
		}

		// aspiration
		KeyHolder akh = new GeneratedKeyHolder();
		jdbcTemplate.update(con -> {
			var ps = con.prepareStatement(
					"INSERT INTO skillmatrix_assessment_aspiration(submission_id, target_role_2yr, message_to_manager) VALUES(?,?,?)",
					new String[] { "id" });
			ps.setString(1, submissionIdFinal);
			ps.setString(2, req.getTargetRole2yr());
			ps.setString(3, req.getMessageToManager());
			return ps;
		}, akh);
		Long aspId = akh.getKey() != null ? akh.getKey().longValue() : null;
		if (aspId != null && req.getAspirationSkillNames() != null) {
			for (String nm : req.getAspirationSkillNames()) {
				if (!StringUtils.hasText(nm)) {
					continue;
				}
				jdbcTemplate.update(
						"INSERT INTO skillmatrix_assessment_aspiration_skill(submission_id, aspiration_id, skill_id, skill_name, is_custom) VALUES(?,?,?,?,1)",
						submissionIdFinal, aspId, null, nm.trim());
			}
		}
		return submissionIdFinal;
	}

	@Transactional
	public void updateExistingProjectsOnly(Long empId, SkillMatrixUpdateProjectsRequest req) {
		if (empId == null) {
			throw new IllegalArgumentException("empId is required.");
		}
		if (req == null || !StringUtils.hasText(req.getSubmissionId())) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		String submissionId = req.getSubmissionId().trim();
		List<Map<String, Object>> ok = jdbcTemplate.queryForList(
				"SELECT submission_id FROM skillmatrix_assessment_submission WHERE submission_id=? AND employee_id=? LIMIT 1",
				submissionId, empId);
		if (ok.isEmpty()) {
			throw new IllegalArgumentException("Submission not found.");
		}
		if (req.getProjects() == null || req.getProjects().isEmpty()) {
			return;
		}
		for (SkillMatrixSubmitDraftProjectDTO p : req.getProjects()) {
			if (p == null || p.getAssessmentProjectId() == null) {
				continue;
			}
			Long pid = p.getAssessmentProjectId();
			// Ensure this project belongs to that submission
			List<Map<String, Object>> okp = jdbcTemplate.queryForList(
					"SELECT id FROM skillmatrix_assessment_project WHERE id=? AND submission_id=? LIMIT 1",
					pid, submissionId);
			if (okp.isEmpty()) {
				continue;
			}
			java.sql.Date projStart = null;
			java.sql.Date projEnd = null;
			try {
				if (StringUtils.hasText(p.getStartDate())) {
					projStart = java.sql.Date.valueOf(java.time.LocalDate.parse(p.getStartDate().trim()));
				}
				if (StringUtils.hasText(p.getEndDate())) {
					projEnd = java.sql.Date.valueOf(java.time.LocalDate.parse(p.getEndDate().trim()));
				}
			} catch (Exception ignore) {
				projStart = null;
				projEnd = null;
			}
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_project SET employee_role=?, allocation_pct=?, contribution_summary=?, is_included=?, "
							+ "domain_specific=?, skill_domain_id=?, skill_subdomain_id=?, skill_domain_feature_id=? "
							+ "WHERE id=? AND submission_id=?",
					StringUtils.hasText(p.getEmployeeRole()) ? p.getEmployeeRole().trim() : null,
					p.getAllocationPct(),
					StringUtils.hasText(p.getContributionSummary()) ? p.getContributionSummary() : "",
					p.isIncluded(),
					p.isDomainSpecific(),
					p.isDomainSpecific() ? p.getSkillDomainId() : null,
					p.isDomainSpecific() ? p.getSkillSubdomainId() : null,
					p.isDomainSpecific() ? p.getSkillDomainFeatureId() : null,
					pid, submissionId);
			// Dates/client/status/name are kept as-is from the original row; only update if payload provided
			if (projStart != null || projEnd != null) {
				jdbcTemplate.update("UPDATE skillmatrix_assessment_project SET start_date=?, end_date=? WHERE id=? AND submission_id=?",
						projStart, projEnd, pid, submissionId);
			}
			if (StringUtils.hasText(p.getClientOrType()) || p.getClientOrType() == null) {
				jdbcTemplate.update("UPDATE skillmatrix_assessment_project SET client_or_type=? WHERE id=? AND submission_id=?",
						p.getClientOrType(), pid, submissionId);
			}
			if (StringUtils.hasText(p.getProjectStatus()) || p.getProjectStatus() == null) {
				jdbcTemplate.update("UPDATE skillmatrix_assessment_project SET project_status=? WHERE id=? AND submission_id=?",
						p.getProjectStatus(), pid, submissionId);
			}
			// Replace project skills
			jdbcTemplate.update("DELETE FROM skillmatrix_assessment_project_skill WHERE assessment_project_id = ?", pid);
			if (p.getSkillsApplied() != null) {
				for (SkillMatrixSubmitDraftProjectSkillDTO sk : p.getSkillsApplied()) {
					if (sk == null || sk.getSkillId() == null) {
						continue;
					}
					jdbcTemplate.update(
							"INSERT INTO skillmatrix_assessment_project_skill(assessment_project_id, submission_id, skill_id, skill_name, level_used, specific_contribution, created_at) "
									+ "VALUES(?,?,?,?,?,?, NOW())",
							pid, submissionId, sk.getSkillId(), sk.getSkillName(), sk.getLevelUsed(),
							StringUtils.hasText(sk.getSpecificContribution()) ? sk.getSpecificContribution() : null);
				}
			}
		}
	}

	@Transactional
	public void submitForApproval(Long empId, String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT status FROM skillmatrix_assessment_submission WHERE submission_id = ? AND employee_id = ?",
				submissionId, empId);
		if (rows.isEmpty()) {
			throw new IllegalStateException("Draft not found.");
		}
		String st = String.valueOf(rows.get(0).get("status"));
		if ("approved".equalsIgnoreCase(st) || "under_review".equalsIgnoreCase(st) || "submitted".equalsIgnoreCase(st)) {
			throw new IllegalStateException("Already submitted.");
		}
		jdbcTemplate.update(
				"UPDATE skillmatrix_assessment_submission SET status='submitted', submitted_at=NOW() WHERE submission_id=?",
				submissionId);
		Employee emp = employeeRepository.findByEmpId(empId);
		Long mgrId = emp != null ? (emp.getManagerId() != null ? emp.getManagerId() : emp.getReportingManagerId()) : null;
		String mgrName = emp != null ? resolveManagerName(emp) : null;

		// Each re-submit creates a new approval round (uq constraint: submission_id + revision_round).
		Integer maxRound = jdbcTemplate.queryForObject(
				"SELECT COALESCE(MAX(revision_round), 0) FROM skillmatrix_assessment_approval WHERE submission_id = ?",
				new Object[] { submissionId }, Integer.class);
		int nextRound = (maxRound != null ? maxRound.intValue() : 0) + 1;

		Long deptId = jdbcTemplate.queryForObject(
				"SELECT dept_id FROM skillmatrix_assessment_submission WHERE submission_id = ?",
				new Object[] { submissionId }, Long.class);
		Long hodId = null;
		String hodName = null;
		try {
			if (deptId != null) {
				Department d = departmentRepository.findByDeptId(deptId);
				if (d != null && d.getHodId() != null) {
					hodId = d.getHodId();
					Employee hod = employeeRepository.findByEmpId(hodId);
					hodName = hod != null ? hod.getName() : null;
				}
			}
		} catch (Exception ignore) {
		}

		// Single-table (skillmatrix_assessment_approval) now tracks manager + HOD statuses.
		jdbcTemplate.update(
				"INSERT INTO skillmatrix_assessment_approval(submission_id, manager_id, manager_name, hod_id, hod_name, revision_round, "
						+ "manager_approved, hod_approved, final_status) "
						+ "VALUES(?,?,?,?,?,?, 'pending','pending','pending')",
				submissionId,
				mgrId != null ? mgrId : 0L,
				mgrName != null ? mgrName : "Manager",
				hodId != null ? hodId : 0L,
				hodName != null ? hodName : "HOD",
				nextRound);
		skillMatrixNotificationService.notifyManagerReviewRequired(submissionId);
	}

	public List<SkillMatrixMySubmissionListDTO> listMySubmissions(Long empId) {
		if (empId == null) {
			return Collections.emptyList();
		}
		String sql = "SELECT s.submission_id, s.assessment_cycle, s.cycle_year, s.dept_name, s.designation, s.reporting_manager_name, "
				+ "s.status, s.submitted_at, s.updated_at, s.created_at, "
				+ "a.manager_approved, a.hod_name, a.hod_approved, a.final_status "
				+ "FROM skillmatrix_assessment_submission s "
				+ "LEFT JOIN skillmatrix_assessment_approval a "
				+ "  ON a.submission_id = s.submission_id "
				+ " AND a.revision_round = (SELECT MAX(x.revision_round) FROM skillmatrix_assessment_approval x WHERE x.submission_id = s.submission_id) "
				+ "WHERE s.employee_id = ? "
				+ "ORDER BY s.updated_at DESC, s.created_at DESC";
		return jdbcTemplate.query(sql, new Object[] { empId }, (rs, rowNum) -> {
			SkillMatrixMySubmissionListDTO dto = new SkillMatrixMySubmissionListDTO();
			dto.setSubmissionId(rs.getString("submission_id"));
			dto.setAssessmentCycle(rs.getString("assessment_cycle"));
			dto.setCycleYear(rs.getInt("cycle_year"));
			dto.setDeptName(rs.getString("dept_name"));
			dto.setDesignation(rs.getString("designation"));
			dto.setReportingManagerName(rs.getString("reporting_manager_name"));
			dto.setManagerApprovalStatus(rs.getString("manager_approved"));
			dto.setHodName(rs.getString("hod_name"));
			dto.setHodApprovalStatus(rs.getString("hod_approved"));
			dto.setFinalStatus(rs.getString("final_status"));
			dto.setStatus(rs.getString("status"));
			dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
			dto.setUpdatedAt(rs.getTimestamp("updated_at"));
			dto.setCreatedAt(rs.getTimestamp("created_at"));
			return dto;
		});
	}

	public Page<SkillMatrixMySubmissionListDTO> listMySubmissions(Long empId, int page, int size, String submissionId,
			String deptName, String designation, String reportingManagerName, String status, String sortColumn,
			String sortDirection) {
		if (empId == null) {
			return Page.empty();
		}
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));
		int offset = safePage * safeSize;

		List<Object> args = new ArrayList<>();
		StringBuilder where = new StringBuilder(" WHERE s.employee_id = ? ");
		args.add(empId);

		if (StringUtils.hasText(submissionId)) {
			where.append(" AND s.submission_id LIKE ? ");
			args.add("%" + submissionId.trim() + "%");
		}
		if (StringUtils.hasText(deptName)) {
			where.append(" AND s.dept_name LIKE ? ");
			args.add("%" + deptName.trim() + "%");
		}
		if (StringUtils.hasText(designation)) {
			where.append(" AND s.designation LIKE ? ");
			args.add("%" + designation.trim() + "%");
		}
		if (StringUtils.hasText(reportingManagerName)) {
			where.append(" AND s.reporting_manager_name LIKE ? ");
			args.add("%" + reportingManagerName.trim() + "%");
		}
		if (StringUtils.hasText(status)) {
			where.append(" AND s.status LIKE ? ");
			args.add("%" + status.trim() + "%");
		}

		String orderBy = " ORDER BY s.updated_at DESC, s.created_at DESC ";
		String sc = StringUtils.hasText(sortColumn) ? sortColumn.trim() : null;
		String sd = StringUtils.hasText(sortDirection) ? sortDirection.trim().toLowerCase(Locale.ROOT) : null;
		if (sc != null) {
			String col = null;
			if ("submissionId".equalsIgnoreCase(sc) || "submission_id".equalsIgnoreCase(sc)) {
				col = "s.submission_id";
			} else if ("deptName".equalsIgnoreCase(sc) || "dept_name".equalsIgnoreCase(sc)) {
				col = "s.dept_name";
			} else if ("designation".equalsIgnoreCase(sc)) {
				col = "s.designation";
			} else if ("reportingManagerName".equalsIgnoreCase(sc) || "reporting_manager_name".equalsIgnoreCase(sc)) {
				col = "s.reporting_manager_name";
			} else if ("managerApprovalStatus".equalsIgnoreCase(sc) || "manager_approved".equalsIgnoreCase(sc)) {
				col = "a.manager_approved";
			} else if ("hodName".equalsIgnoreCase(sc) || "hod_name".equalsIgnoreCase(sc)) {
				col = "a.hod_name";
			} else if ("hodApprovalStatus".equalsIgnoreCase(sc) || "hod_approved".equalsIgnoreCase(sc)) {
				col = "a.hod_approved";
			} else if ("finalStatus".equalsIgnoreCase(sc) || "final_status".equalsIgnoreCase(sc)) {
				col = "a.final_status";
			} else if ("status".equalsIgnoreCase(sc)) {
				col = "s.status";
			} else if ("submittedAt".equalsIgnoreCase(sc) || "submitted_at".equalsIgnoreCase(sc)) {
				col = "s.submitted_at";
			} else if ("updatedAt".equalsIgnoreCase(sc) || "updated_at".equalsIgnoreCase(sc)) {
				col = "s.updated_at";
			}
			if (col != null) {
				String dir = "asc".equals(sd) || "desc".equals(sd) ? sd : "asc";
				orderBy = " ORDER BY " + col + " " + dir + " ";
			}
		}

		String countSql = "SELECT COUNT(1) FROM skillmatrix_assessment_submission s " + where;
		long total = jdbcTemplate.queryForObject(countSql, args.toArray(), Long.class);

		String sql = "SELECT s.submission_id, s.assessment_cycle, s.cycle_year, s.dept_name, s.designation, s.reporting_manager_name, "
				+ "s.status, s.submitted_at, s.updated_at, s.created_at, "
				+ "a.manager_approved, a.hod_name, a.hod_approved, a.final_status "
				+ "FROM skillmatrix_assessment_submission s "
				+ "LEFT JOIN skillmatrix_assessment_approval a "
				+ "  ON a.submission_id = s.submission_id "
				+ " AND a.revision_round = (SELECT MAX(x.revision_round) FROM skillmatrix_assessment_approval x WHERE x.submission_id = s.submission_id) "
				+ where + orderBy + " LIMIT ? OFFSET ? ";
		List<Object> listArgs = new ArrayList<>(args);
		listArgs.add(safeSize);
		listArgs.add(offset);

		List<SkillMatrixMySubmissionListDTO> rows = jdbcTemplate.query(sql, listArgs.toArray(), (rs, rowNum) -> {
			SkillMatrixMySubmissionListDTO dto = new SkillMatrixMySubmissionListDTO();
			dto.setSubmissionId(rs.getString("submission_id"));
			dto.setAssessmentCycle(rs.getString("assessment_cycle"));
			dto.setCycleYear(rs.getInt("cycle_year"));
			dto.setDeptName(rs.getString("dept_name"));
			dto.setDesignation(rs.getString("designation"));
			dto.setReportingManagerName(rs.getString("reporting_manager_name"));
			dto.setManagerApprovalStatus(rs.getString("manager_approved"));
			dto.setHodName(rs.getString("hod_name"));
			dto.setHodApprovalStatus(rs.getString("hod_approved"));
			dto.setFinalStatus(rs.getString("final_status"));
			dto.setStatus(rs.getString("status"));
			dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
			dto.setUpdatedAt(rs.getTimestamp("updated_at"));
			dto.setCreatedAt(rs.getTimestamp("created_at"));
			return dto;
		});

		return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
	}

	// ─────────────────────────────────────────────────────────────
	// Approve Skill Requests (manager)
	// ─────────────────────────────────────────────────────────────

	public Page<SkillMatrixApproveQueueRowDTO> listApproveQueue(Long managerEmpId, int page, int size, String q,
			String submissionId, String employeeName, String deptName, String designation, String status, String sortColumn,
			String sortDirection) {
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));
		int offset = safePage * safeSize;

		List<Object> args = new ArrayList<>();
		// Single inbox: show Manager pending OR HOD pending (after manager approved).
		// Manager pending: review not completed yet.
		// HOD pending: manager_approved='yes' AND hod_approved='pending' AND final_status='pending'.
		// Manager view-only: manager already approved but final is still pending (waiting HOD).
		StringBuilder where = new StringBuilder(
				" WHERE s.submission_id = a.submission_id AND ("
						+ " (a.manager_id = ? AND a.review_completed_at IS NULL) "
						+ " OR "
						+ " (a.hod_id = ? AND a.manager_approved='yes' AND a.hod_approved='pending' AND a.final_status='pending') "
						+ " OR "
						+ " (a.manager_id = ? AND a.manager_approved='yes' AND a.final_status='pending') "
						+ ") ");
		args.add(managerEmpId); // manager path
		args.add(managerEmpId); // hod path uses same logged-in user
		args.add(managerEmpId); // manager view-only path

		String st = StringUtils.hasText(status) ? status.trim().toLowerCase(Locale.ROOT) : "all";
		// Finalized requests list: show FINAL approved/rejected rows (after HOD) for either manager or HOD.
		// This is a view-only list and should not be constrained to the inbox filters above.
		if ("approved".equals(st)) {
			List<Object> approvedArgs = new ArrayList<>();
			StringBuilder approvedWhere = new StringBuilder(
					" WHERE s.submission_id = a.submission_id AND (a.manager_id = ? OR a.hod_id = ?) AND a.final_status IN ('approved','rejected') ");
			approvedArgs.add(managerEmpId);
			approvedArgs.add(managerEmpId);

			if (StringUtils.hasText(q)) {
				String t = "%" + q.trim() + "%";
				approvedWhere.append(" AND (s.employee_name LIKE ? OR s.dept_name LIKE ? OR s.designation LIKE ? OR s.submission_id LIKE ?) ");
				approvedArgs.add(t);
				approvedArgs.add(t);
				approvedArgs.add(t);
				approvedArgs.add(t);
			}
			if (StringUtils.hasText(submissionId)) {
				approvedWhere.append(" AND s.submission_id LIKE ? ");
				approvedArgs.add("%" + submissionId.trim() + "%");
			}
			if (StringUtils.hasText(employeeName)) {
				approvedWhere.append(" AND s.employee_name LIKE ? ");
				approvedArgs.add("%" + employeeName.trim() + "%");
			}
			if (StringUtils.hasText(deptName)) {
				approvedWhere.append(" AND s.dept_name LIKE ? ");
				approvedArgs.add("%" + deptName.trim() + "%");
			}
			if (StringUtils.hasText(designation)) {
				approvedWhere.append(" AND s.designation LIKE ? ");
				approvedArgs.add("%" + designation.trim() + "%");
			}

			String orderByApproved = " ORDER BY COALESCE(s.submitted_at, s.updated_at) DESC ";
			String scA = StringUtils.hasText(sortColumn) ? sortColumn.trim() : null;
			String sdA = StringUtils.hasText(sortDirection) ? sortDirection.trim().toLowerCase(Locale.ROOT) : null;
			if (scA != null) {
				String col = null;
				if ("submittedAt".equalsIgnoreCase(scA) || "submitted_at".equalsIgnoreCase(scA)) {
					col = "s.submitted_at";
				} else if ("employeeName".equalsIgnoreCase(scA) || "employee_name".equalsIgnoreCase(scA)) {
					col = "s.employee_name";
				} else if ("deptName".equalsIgnoreCase(scA) || "dept_name".equalsIgnoreCase(scA)) {
					col = "s.dept_name";
				} else if ("designation".equalsIgnoreCase(scA)) {
					col = "s.designation";
				} else if ("status".equalsIgnoreCase(scA)) {
					col = "s.status";
				} else if ("managerName".equalsIgnoreCase(scA) || "manager_name".equalsIgnoreCase(scA)) {
					col = "a.manager_name";
				} else if ("managerApprovalStatus".equalsIgnoreCase(scA) || "manager_approved".equalsIgnoreCase(scA)) {
					col = "a.manager_approved";
				} else if ("hodName".equalsIgnoreCase(scA) || "hod_name".equalsIgnoreCase(scA)) {
					col = "a.hod_name";
				} else if ("hodApprovalStatus".equalsIgnoreCase(scA) || "hod_approved".equalsIgnoreCase(scA)) {
					col = "a.hod_approved";
				} else if ("finalStatus".equalsIgnoreCase(scA) || "final_status".equalsIgnoreCase(scA)) {
					col = "a.final_status";
				} else if ("reviewDeadline".equalsIgnoreCase(scA) || "review_deadline".equalsIgnoreCase(scA)) {
					col = "a.review_deadline";
				}
				if (col != null) {
					String dir = "desc".equals(sdA) ? "DESC" : "ASC";
					orderByApproved = " ORDER BY " + col + " " + dir + " ";
				}
			}

			String countSqlApproved = "SELECT COUNT(1) FROM skillmatrix_assessment_approval a JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
					+ approvedWhere;
			long totalApproved = jdbcTemplate.queryForObject(countSqlApproved, approvedArgs.toArray(), Long.class);

			String sqlApproved = "SELECT s.submission_id, s.employee_id, s.employee_name, s.dept_name, s.designation, s.status, s.submitted_at, "
					+ "COALESCE(a.review_deadline, DATE_ADD(COALESCE(s.submitted_at, s.updated_at), INTERVAL 15 DAY)) AS review_deadline, "
					+ "a.manager_name, a.manager_approved, a.hod_name, a.hod_approved, a.final_status, a.manager_id, a.hod_id, a.review_completed_at, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating r WHERE r.submission_id = s.submission_id) AS total_skills, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND COALESCE(aks.manager_decision,'pending') <> 'pending') AS reviewed_skills, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'approved') AS c_approved, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'adjusted') AS c_adjusted, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'sent_back') AS c_sent_back, "
					+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'rejected') AS c_rejected "
					+ "FROM skillmatrix_assessment_approval a JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
					+ approvedWhere + orderByApproved + " LIMIT ? OFFSET ? ";

			List<Object> listArgsApproved = new ArrayList<>(approvedArgs);
			listArgsApproved.add(safeSize);
			listArgsApproved.add(offset);

			List<SkillMatrixApproveQueueRowDTO> rowsApproved = jdbcTemplate.query(sqlApproved, listArgsApproved.toArray(), (rs, rowNum) -> {
				SkillMatrixApproveQueueRowDTO dto = new SkillMatrixApproveQueueRowDTO();
				dto.setSubmissionId(rs.getString("submission_id"));
				dto.setEmployeeId(rs.getLong("employee_id"));
				dto.setEmployeeName(rs.getString("employee_name"));
				dto.setDeptName(rs.getString("dept_name"));
				dto.setDesignation(rs.getString("designation"));
				dto.setStatus(rs.getString("status"));
				dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
				dto.setReviewDeadline(rs.getTimestamp("review_deadline"));
				dto.setManagerName(rs.getString("manager_name"));
				dto.setManagerApprovalStatus(rs.getString("manager_approved"));
				dto.setHodName(rs.getString("hod_name"));
				dto.setHodApprovalStatus(rs.getString("hod_approved"));
				dto.setFinalStatus(rs.getString("final_status"));
				dto.setActionEnabled(false);
				dto.setTotalSkills(rs.getInt("total_skills"));
				dto.setReviewedSkills(rs.getInt("reviewed_skills"));
				dto.setApprovedCount(rs.getInt("c_approved"));
				dto.setAdjustedCount(rs.getInt("c_adjusted"));
				dto.setSentBackCount(rs.getInt("c_sent_back"));
				dto.setRejectedCount(rs.getInt("c_rejected"));
				return dto;
			});

			return new PageImpl<>(rowsApproved, PageRequest.of(safePage, safeSize), totalApproved);
		}
		// Keep existing "done" tab behavior only for manager-completed items (HOD UI will use same inbox for now).
		if ("done".equals(st) || "completed".equals(st)) {
			where.append(" AND a.manager_id = ? AND a.review_completed_at IS NOT NULL ");
			args.add(managerEmpId);
		} else {
			where.append(" AND s.status IN ('submitted','under_review','changes_requested') ");
			if ("overdue".equals(st)) {
				// overdue applies only to manager pending path
				where.append(" AND a.manager_id = ? AND a.review_deadline IS NOT NULL AND a.review_deadline < CURDATE() ");
				args.add(managerEmpId);
			} else if ("submitted".equals(st) || "under_review".equals(st) || "changes_requested".equals(st)) {
				where.append(" AND s.status = ? ");
				args.add(st);
			}
		}

		if (StringUtils.hasText(q)) {
			String t = "%" + q.trim() + "%";
			where.append(" AND (s.employee_name LIKE ? OR s.dept_name LIKE ? OR s.designation LIKE ? OR s.submission_id LIKE ?) ");
			args.add(t);
			args.add(t);
			args.add(t);
			args.add(t);
		}

		if (StringUtils.hasText(submissionId)) {
			where.append(" AND s.submission_id LIKE ? ");
			args.add("%" + submissionId.trim() + "%");
		}
		if (StringUtils.hasText(employeeName)) {
			where.append(" AND s.employee_name LIKE ? ");
			args.add("%" + employeeName.trim() + "%");
		}
		if (StringUtils.hasText(deptName)) {
			where.append(" AND s.dept_name LIKE ? ");
			args.add("%" + deptName.trim() + "%");
		}
		if (StringUtils.hasText(designation)) {
			where.append(" AND s.designation LIKE ? ");
			args.add("%" + designation.trim() + "%");
		}

		String orderBy = " ORDER BY COALESCE(a.review_deadline, DATE_ADD(COALESCE(s.submitted_at, s.updated_at), INTERVAL 15 DAY)) ASC, s.submitted_at DESC ";
		String sc = StringUtils.hasText(sortColumn) ? sortColumn.trim() : null;
		String sd = StringUtils.hasText(sortDirection) ? sortDirection.trim().toLowerCase(Locale.ROOT) : null;
		if (sc != null) {
			String col = null;
			if ("submittedAt".equalsIgnoreCase(sc) || "submitted_at".equalsIgnoreCase(sc)) {
				col = "s.submitted_at";
			} else if ("employeeName".equalsIgnoreCase(sc) || "employee_name".equalsIgnoreCase(sc)) {
				col = "s.employee_name";
			} else if ("deptName".equalsIgnoreCase(sc) || "dept_name".equalsIgnoreCase(sc)) {
				col = "s.dept_name";
			} else if ("designation".equalsIgnoreCase(sc)) {
				col = "s.designation";
			} else if ("status".equalsIgnoreCase(sc)) {
				col = "s.status";
			} else if ("managerName".equalsIgnoreCase(sc) || "manager_name".equalsIgnoreCase(sc)) {
				col = "a.manager_name";
			} else if ("managerApprovalStatus".equalsIgnoreCase(sc) || "manager_approved".equalsIgnoreCase(sc)) {
				col = "a.manager_approved";
			} else if ("hodName".equalsIgnoreCase(sc) || "hod_name".equalsIgnoreCase(sc)) {
				col = "a.hod_name";
			} else if ("hodApprovalStatus".equalsIgnoreCase(sc) || "hod_approved".equalsIgnoreCase(sc)) {
				col = "a.hod_approved";
			} else if ("finalStatus".equalsIgnoreCase(sc) || "final_status".equalsIgnoreCase(sc)) {
				col = "a.final_status";
			} else if ("reviewDeadline".equalsIgnoreCase(sc) || "review_deadline".equalsIgnoreCase(sc)) {
				col = "a.review_deadline";
			}
			if (col != null) {
				String dir = "desc".equals(sd) ? "DESC" : "ASC";
				orderBy = " ORDER BY " + col + " " + dir + " ";
			}
		}

		String countSql = "SELECT COUNT(1) FROM skillmatrix_assessment_approval a JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
				+ where;
		long total = jdbcTemplate.queryForObject(countSql, args.toArray(), Long.class);

		String sql = "SELECT s.submission_id, s.employee_id, s.employee_name, s.dept_name, s.designation, s.status, s.submitted_at, "
				+ "COALESCE(a.review_deadline, DATE_ADD(COALESCE(s.submitted_at, s.updated_at), INTERVAL 15 DAY)) AS review_deadline, "
				+ "a.manager_name, a.manager_approved, a.hod_name, a.hod_approved, a.final_status, a.manager_id, a.hod_id, a.review_completed_at, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating r WHERE r.submission_id = s.submission_id) AS total_skills, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND COALESCE(aks.manager_decision,'pending') <> 'pending') AS reviewed_skills, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'approved') AS c_approved, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'adjusted') AS c_adjusted, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'sent_back') AS c_sent_back, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill aks WHERE aks.approval_id = a.id AND aks.manager_decision = 'rejected') AS c_rejected "
				+ "FROM skillmatrix_assessment_approval a JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
				+ where + orderBy + " LIMIT ? OFFSET ? ";

		List<Object> listArgs = new ArrayList<>(args);
		listArgs.add(safeSize);
		listArgs.add(offset);

		List<SkillMatrixApproveQueueRowDTO> rows = jdbcTemplate.query(sql, listArgs.toArray(), (rs, rowNum) -> {
			SkillMatrixApproveQueueRowDTO dto = new SkillMatrixApproveQueueRowDTO();
			dto.setSubmissionId(rs.getString("submission_id"));
			dto.setEmployeeId(rs.getLong("employee_id"));
			dto.setEmployeeName(rs.getString("employee_name"));
			dto.setDeptName(rs.getString("dept_name"));
			dto.setDesignation(rs.getString("designation"));
			dto.setStatus(rs.getString("status"));
			dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
			dto.setReviewDeadline(rs.getTimestamp("review_deadline"));
			dto.setManagerName(rs.getString("manager_name"));
			dto.setManagerApprovalStatus(rs.getString("manager_approved"));
			dto.setHodName(rs.getString("hod_name"));
			dto.setHodApprovalStatus(rs.getString("hod_approved"));
			dto.setFinalStatus(rs.getString("final_status"));

			Long rowManagerId = rs.getLong("manager_id");
			if (rs.wasNull()) {
				rowManagerId = null;
			}
			Long rowHodId = rs.getLong("hod_id");
			if (rs.wasNull()) {
				rowHodId = null;
			}
			String mgrApproved = rs.getString("manager_approved");
			String hodApproved = rs.getString("hod_approved");
			String finalStatus = rs.getString("final_status");
			boolean canManagerReview = managerEmpId != null && rowManagerId != null && managerEmpId.equals(rowManagerId)
					&& rs.getTimestamp("review_completed_at") == null;
			boolean canHodReview = managerEmpId != null && rowHodId != null && managerEmpId.equals(rowHodId)
					&& "yes".equalsIgnoreCase(mgrApproved) && "pending".equalsIgnoreCase(hodApproved)
					&& "pending".equalsIgnoreCase(finalStatus);
			dto.setActionEnabled(canManagerReview || canHodReview);
			dto.setTotalSkills(rs.getInt("total_skills"));
			dto.setReviewedSkills(rs.getInt("reviewed_skills"));
			dto.setApprovedCount(rs.getInt("c_approved"));
			dto.setAdjustedCount(rs.getInt("c_adjusted"));
			dto.setSentBackCount(rs.getInt("c_sent_back"));
			dto.setRejectedCount(rs.getInt("c_rejected"));
			return dto;
		});

		return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
	}

	public SkillMatrixApproveDetailDTO getApproveSubmissionDetail(Long managerEmpId, String submissionId, int page, int size) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));
		List<Map<String, Object>> approvalRows = jdbcTemplate.queryForList(
				"SELECT a.id AS approval_id, a.manager_id, a.hod_id, a.manager_approved, a.hod_approved, a.final_status, "
						+ "a.review_started_at, a.review_completed_at, a.review_deadline, "
						+ "s.employee_id, s.employee_name, s.dept_name, s.designation, s.status, s.submitted_at "
						+ "FROM skillmatrix_assessment_approval a JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
						+ "WHERE a.submission_id = ? AND (a.manager_id = ? OR a.hod_id = ?) ORDER BY a.revision_round DESC LIMIT 1",
				submissionId, managerEmpId, managerEmpId);
		if (approvalRows.isEmpty()) {
			throw new IllegalStateException("No approval request found for this submission.");
		}
		Map<String, Object> ar = approvalRows.get(0);
		long approvalId = ((Number) ar.get("approval_id")).longValue();
		// IMPORTANT: Keep GET read-only. Do not insert/update here.

		SkillMatrixApproveDetailDTO dto = new SkillMatrixApproveDetailDTO();
		dto.setSubmissionId(submissionId);
		dto.setEmployeeId(ar.get("employee_id") != null ? ((Number) ar.get("employee_id")).longValue() : null);
		dto.setEmployeeName(String.valueOf(ar.get("employee_name")));
		dto.setDeptName(String.valueOf(ar.get("dept_name")));
		dto.setDesignation(String.valueOf(ar.get("designation")));
		dto.setStatus(String.valueOf(ar.get("status")));
		dto.setSubmittedAt(toUtilDate(ar.get("submitted_at")));
		dto.setReviewDeadline(toUtilDate(ar.get("review_deadline")));
		Long hid = ar.get("hod_id") != null ? ((Number) ar.get("hod_id")).longValue() : null;
		dto.setViewerRole(hid != null && hid.longValue() == managerEmpId ? "hod" : "manager");
		dto.setManagerApprovalStatus(ar.get("manager_approved") != null ? String.valueOf(ar.get("manager_approved")) : "pending");
		dto.setHodApprovalStatus(ar.get("hod_approved") != null ? String.valueOf(ar.get("hod_approved")) : "pending");
		dto.setFinalStatus(ar.get("final_status") != null ? String.valueOf(ar.get("final_status")) : "pending");

		dto.setAspiration(loadAspirationForSubmission(submissionId));
		Integer totalSkills = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?",
				new Object[] { submissionId }, Integer.class);
		dto.setSkillsTotal(totalSkills != null ? totalSkills : 0);
		dto.setSkillsPage(safePage);
		dto.setSkillsSize(safeSize);

		boolean viewerIsHod = "hod".equalsIgnoreCase(dto.getViewerRole());
		// Decision counts across ALL skills (role-based)
		String col = viewerIsHod ? "aks.hod_decision" : "aks.manager_decision";
		Map<String, Object> c = jdbcTemplate.queryForMap(
				"SELECT "
						+ "SUM(CASE WHEN COALESCE(" + col + ",'pending')='pending' THEN 1 ELSE 0 END) AS c_pending, "
						+ "SUM(CASE WHEN " + col + "='approved' THEN 1 ELSE 0 END) AS c_approved, "
						+ "SUM(CASE WHEN " + col + "='adjusted' THEN 1 ELSE 0 END) AS c_adjusted, "
						+ "SUM(CASE WHEN " + col + "='sent_back' THEN 1 ELSE 0 END) AS c_sent_back, "
						+ "SUM(CASE WHEN " + col + "='rejected' THEN 1 ELSE 0 END) AS c_rejected "
						+ "FROM skillmatrix_assessment_skill_rating r "
						+ "LEFT JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id = ? AND aks.skill_rating_id = r.id "
						+ "WHERE r.submission_id = ?",
				approvalId, submissionId);
		dto.setPendingCount(c.get("c_pending") != null ? ((Number) c.get("c_pending")).intValue() : 0);
		dto.setApprovedCount(c.get("c_approved") != null ? ((Number) c.get("c_approved")).intValue() : 0);
		dto.setAdjustedCount(c.get("c_adjusted") != null ? ((Number) c.get("c_adjusted")).intValue() : 0);
		dto.setSentBackCount(c.get("c_sent_back") != null ? ((Number) c.get("c_sent_back")).intValue() : 0);
		dto.setRejectedCount(c.get("c_rejected") != null ? ((Number) c.get("c_rejected")).intValue() : 0);

		dto.setSkills(loadApproveSkills(approvalId, submissionId, safePage, safeSize));
		return dto;
	}

	public Page<SkillMatrixApproveSkillViewRowDTO> listApproveSkillView(Long managerEmpId, int page, int size, String q,
			String skillName, String deptName, String decision, String sortColumn, String sortDirection) {
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));
		int offset = safePage * safeSize;

		List<Object> args = new ArrayList<>();
		StringBuilder where = new StringBuilder(
				" WHERE a.manager_id = ? AND a.review_completed_at IS NULL AND s.status IN ('submitted','under_review','changes_requested') ");
		args.add(managerEmpId);

		if (StringUtils.hasText(q)) {
			String t = "%" + q.trim() + "%";
			where.append(" AND (s.employee_name LIKE ? OR s.dept_name LIKE ? OR r.skill_name LIKE ? OR s.submission_id LIKE ?) ");
			args.add(t);
			args.add(t);
			args.add(t);
			args.add(t);
		}
		if (StringUtils.hasText(skillName)) {
			where.append(" AND r.skill_name LIKE ? ");
			args.add("%" + skillName.trim() + "%");
		}
		if (StringUtils.hasText(deptName)) {
			where.append(" AND s.dept_name LIKE ? ");
			args.add("%" + deptName.trim() + "%");
		}
		if (StringUtils.hasText(decision)) {
			String d = decision.trim().toLowerCase(Locale.ROOT);
			if ("pending".equals(d) || "approved".equals(d) || "adjusted".equals(d) || "sent_back".equals(d) || "rejected".equals(d)) {
				where.append(" AND COALESCE(aks.manager_decision,'pending') = ? ");
				args.add(d);
			}
		}

		String orderBy = " ORDER BY s.submitted_at DESC, r.skill_name ASC ";
		String sc = StringUtils.hasText(sortColumn) ? sortColumn.trim() : null;
		String sd = StringUtils.hasText(sortDirection) ? sortDirection.trim().toLowerCase(Locale.ROOT) : null;
		if (sc != null) {
			String col = null;
			if ("skillName".equalsIgnoreCase(sc) || "skill_name".equalsIgnoreCase(sc)) {
				col = "r.skill_name";
			} else if ("deptName".equalsIgnoreCase(sc) || "dept_name".equalsIgnoreCase(sc)) {
				col = "s.dept_name";
			} else if ("employeeName".equalsIgnoreCase(sc) || "employee_name".equalsIgnoreCase(sc)) {
				col = "s.employee_name";
			} else if ("decision".equalsIgnoreCase(sc)) {
				col = "aks.manager_decision";
			} else if ("submittedAt".equalsIgnoreCase(sc) || "submitted_at".equalsIgnoreCase(sc)) {
				col = "s.submitted_at";
			}
			if (col != null) {
				String dir = "desc".equals(sd) ? "DESC" : "ASC";
				orderBy = " ORDER BY " + col + " " + dir + " ";
			}
		}

		String baseFrom = " FROM skillmatrix_assessment_approval a "
				+ "JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
				+ "JOIN skillmatrix_assessment_skill_rating r ON r.submission_id = s.submission_id "
				+ "LEFT JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id = a.id AND aks.skill_rating_id = r.id "
				+ "LEFT JOIN skills_master sm ON sm.skill_id = r.skill_id "
				+ "LEFT JOIN skill_category_master scm ON scm.category_id = sm.category_id ";

		String countSql = "SELECT COUNT(1) " + baseFrom + where;
		long total = jdbcTemplate.queryForObject(countSql, args.toArray(), Long.class);

		String sql = "SELECT s.submission_id, s.employee_id, s.employee_name, s.dept_name, s.submitted_at, "
				+ "r.id AS skill_rating_id, r.skill_id, r.skill_name, r.self_rating, "
				+ "COALESCE(aks.manager_decision,'pending') AS decision, aks.manager_rating, "
				+ "scm.category_name AS skill_category, "
				+ "(CASE WHEN (r.github_portfolio_url IS NOT NULL AND r.github_portfolio_url <> '') "
				+ " OR (r.colleague_endorser IS NOT NULL AND r.colleague_endorser <> '') "
				+ " OR (r.used_in_project = 1) THEN 1 ELSE 0 END) AS has_evidence, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_certification c WHERE c.submission_id = s.submission_id AND c.skill_rating_id = r.id) AS has_cert, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_training t WHERE t.submission_id = s.submission_id AND t.skill_rating_id = r.id) AS has_train, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_project_skill ps WHERE ps.submission_id = s.submission_id AND ps.skill_id = r.skill_id) AS has_proj "
				+ baseFrom + where + orderBy + " LIMIT ? OFFSET ? ";

		List<Object> listArgs = new ArrayList<>(args);
		listArgs.add(safeSize);
		listArgs.add(offset);

		List<SkillMatrixApproveSkillViewRowDTO> rows = jdbcTemplate.query(sql, listArgs.toArray(), (rs, rowNum) -> {
			SkillMatrixApproveSkillViewRowDTO dto = new SkillMatrixApproveSkillViewRowDTO();
			dto.setSubmissionId(rs.getString("submission_id"));
			dto.setEmployeeId(rs.getLong("employee_id"));
			dto.setEmployeeName(rs.getString("employee_name"));
			dto.setDeptName(rs.getString("dept_name"));
			dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
			dto.setSkillId(rs.getInt("skill_id"));
			dto.setSkillName(rs.getString("skill_name"));
			dto.setSkillCategory(rs.getString("skill_category"));
			dto.setSelfRating(rs.getInt("self_rating"));
			dto.setDecision(rs.getString("decision"));
			dto.setManagerRating(rs.getObject("manager_rating") != null ? rs.getInt("manager_rating") : null);
			dto.setHasEvidence(rs.getInt("has_evidence") > 0);
			dto.setHasCertification(rs.getInt("has_cert") > 0);
			dto.setHasTraining(rs.getInt("has_train") > 0);
			dto.setHasProject(rs.getInt("has_proj") > 0);
			return dto;
		});

		return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
	}

	@Transactional
	public void saveApproveDecision(Long managerEmpId, String submissionId, SkillMatrixApproveSkillDecisionRequest body) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		if (body == null || body.getSkillRatingId() == null) {
			throw new IllegalArgumentException("skillRatingId is required.");
		}
		String decision = StringUtils.hasText(body.getDecision()) ? body.getDecision().trim().toLowerCase(Locale.ROOT) : "";
		if (!StringUtils.hasText(decision)) {
			throw new IllegalArgumentException("Invalid decision.");
		}

		Long approvalId = latestApprovalIdForApprover(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found.");
		}
		Map<String, Object> appr = jdbcTemplate.queryForMap(
				"SELECT manager_id, hod_id, manager_approved, hod_approved, final_status FROM skillmatrix_assessment_approval WHERE id=?",
				approvalId);
		Long mid = appr.get("manager_id") != null ? ((Number) appr.get("manager_id")).longValue() : null;
		Long hid = appr.get("hod_id") != null ? ((Number) appr.get("hod_id")).longValue() : null;
		boolean isManager = mid != null && mid.longValue() == managerEmpId;
		boolean isHod = hid != null && hid.longValue() == managerEmpId;
		if (!isManager && !isHod) {
			throw new IllegalStateException("Not allowed.");
		}

		String mgrApproved = appr.get("manager_approved") != null ? String.valueOf(appr.get("manager_approved")) : "pending";
		String fin = appr.get("final_status") != null ? String.valueOf(appr.get("final_status")) : "pending";
		if ("approved".equalsIgnoreCase(fin) || "rejected".equalsIgnoreCase(fin)) {
			throw new IllegalStateException("Decision is already final.");
		}

		if (isHod) {
			// HOD can act only after manager approved
			if (!"yes".equalsIgnoreCase(mgrApproved)) {
				throw new IllegalStateException("HOD approval is not enabled yet.");
			}
			if (!List.of("approved", "rejected").contains(decision)) {
				throw new IllegalArgumentException("Invalid decision.");
			}
			if ("rejected".equals(decision) && !StringUtils.hasText(body.getManagerComment())) {
				throw new IllegalArgumentException("comment is required.");
			}
		} else {
			// Manager flow (existing)
			if (!List.of("approved", "adjusted", "sent_back", "rejected").contains(decision)) {
				throw new IllegalArgumentException("Invalid decision.");
			}
			if ("adjusted".equals(decision) && (body.getManagerRating() == null || body.getManagerRating() < 1 || body.getManagerRating() > 5)) {
				throw new IllegalArgumentException("managerRating is required for adjusted (1..5).");
			}
			if (!"approved".equals(decision) && !StringUtils.hasText(body.getManagerComment())) {
				throw new IllegalArgumentException("managerComment is required.");
			}
		}

		// Ensure the rating belongs to this submission.
		Integer exists = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating WHERE id = ? AND submission_id = ?",
				new Object[] { body.getSkillRatingId(), submissionId }, Integer.class);
		if (exists == null || exists.intValue() == 0) {
			throw new IllegalArgumentException("Skill not found for this submission.");
		}

		ensureApprovalSkillRows(approvalId, submissionId);
		if (isHod) {
			String cmt = body.getManagerComment();
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill "
							+ "SET hod_decision=?, hod_comment=?, decided_at=NOW(), "
							+ "decision=(CASE WHEN ?='approved' THEN 'approved' WHEN ?='rejected' THEN 'rejected' ELSE decision END) "
							+ "WHERE approval_id=? AND skill_rating_id=?",
					decision, cmt, decision, decision, approvalId, body.getSkillRatingId());
		} else {
			// IMPORTANT: approval_skill.decision represents FINAL decision; keep it pending until HOD final approval.
			// Manager's per-skill decision is stored in approval_skill.manager_decision.
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill "
							+ "SET manager_decision=?, manager_rating=?, manager_comment=?, decided_at=NOW(), "
							+ "decision=(CASE WHEN ?='rejected' THEN 'rejected' ELSE decision END) "
							+ "WHERE approval_id=? AND skill_rating_id=?",
					decision, body.getManagerRating(), body.getManagerComment(), decision, approvalId, body.getSkillRatingId());
		}

		if (isManager) {
			// Mirror into skill_rating for quick reporting (manager only).
			Integer selfRating = jdbcTemplate.queryForObject(
					"SELECT self_rating FROM skillmatrix_assessment_skill_rating WHERE id = ?",
					new Object[] { body.getSkillRatingId() }, Integer.class);
			Integer finalRating = "adjusted".equals(decision) ? body.getManagerRating() : selfRating;
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_skill_rating SET manager_decision=?, manager_rating=?, manager_comment=?, manager_reviewed_at=NOW(), final_rating=? "
							+ "WHERE id=?",
					decision, body.getManagerRating(), body.getManagerComment(), finalRating, body.getSkillRatingId());
		}
	}

	@Transactional
	public void submitApproveReview(Long managerEmpId, String submissionId, SkillMatrixApproveSubmitRequest body) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		Long approvalId = latestApprovalIdForManager(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found.");
		}

		Integer total = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating WHERE submission_id = ?",
				new Object[] { submissionId }, Integer.class);
		Integer decided = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill WHERE approval_id = ? AND COALESCE(manager_decision,'pending') <> 'pending'",
				new Object[] { approvalId }, Integer.class);
		if (total == null || total.intValue() == 0) {
			throw new IllegalStateException("No skills found.");
		}
		if (decided == null || decided.intValue() != total.intValue()) {
			throw new IllegalStateException("Please decide all skills before submitting.");
		}

		List<Map<String, Object>> decs = jdbcTemplate.queryForList(
				"SELECT manager_decision FROM skillmatrix_assessment_approval_skill WHERE approval_id = ?",
				approvalId);
		boolean hasRejected = decs.stream()
				.anyMatch(r -> "rejected".equalsIgnoreCase(String.valueOf(r.get("manager_decision"))));
		boolean hasSentBack = decs.stream()
				.anyMatch(r -> "sent_back".equalsIgnoreCase(String.valueOf(r.get("manager_decision"))));
		String overall = hasRejected ? "rejected" : (hasSentBack ? "sent_back" : "approved");

		jdbcTemplate.update(
				"UPDATE skillmatrix_assessment_approval SET overall_decision=?, overall_comment=?, review_completed_at=NOW() WHERE id=?",
				overall, body != null ? body.getOverallComment() : null, approvalId);

		// Level-1 (Manager) status tracking in skillmatrix_assessment_approval (same row).
		Map<String, Object> appr = jdbcTemplate.queryForMap(
				"SELECT manager_id, hod_id FROM skillmatrix_assessment_approval WHERE id=?",
				approvalId);
		Long mid = appr.get("manager_id") != null ? ((Number) appr.get("manager_id")).longValue() : null;
		Long hid = appr.get("hod_id") != null ? ((Number) appr.get("hod_id")).longValue() : null;
		boolean sameApprover = mid != null && hid != null && mid.longValue() == hid.longValue() && mid.longValue() == managerEmpId;

		if ("approved".equalsIgnoreCase(overall)) {
			if (sameApprover) {
				// Manager == HOD: finalize in one step.
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_approval SET manager_approved='yes', hod_approved='yes', final_status='approved', manager_comment=?, hod_comment=?, updated_at=NOW() WHERE id=?",
						body != null ? body.getOverallComment() : null, body != null ? body.getOverallComment() : null, approvalId);
				// Finalize per-skill decisions based on manager decisions (since same person).
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_approval_skill SET hod_decision='approved', decision='approved' "
								+ "WHERE approval_id=? AND COALESCE(manager_decision,'pending') IN ('approved','adjusted')",
						approvalId);
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_approval_skill SET hod_decision='rejected', decision='rejected' "
								+ "WHERE approval_id=? AND COALESCE(manager_decision,'pending') IN ('rejected','sent_back')",
						approvalId);
				jdbcTemplate.update("UPDATE skillmatrix_assessment_submission SET status='approved', approved_at=NOW() WHERE submission_id=?",
						submissionId);
				skillMatrixNotificationService.notifyEmployeeStatus(
						submissionId,
						"Approved",
						"Your Skill Matrix submission has been approved.",
						"Approver remarks",
						body != null ? body.getOverallComment() : null);
			} else {
				jdbcTemplate.update(
						"UPDATE skillmatrix_assessment_approval SET manager_approved='yes', hod_approved='pending', final_status='pending', manager_comment=?, updated_at=NOW() WHERE id=?",
						body != null ? body.getOverallComment() : null, approvalId);
				// Manager approved → move to HOD (Level-2). Final status remains pending.
				jdbcTemplate.update("UPDATE skillmatrix_assessment_submission SET status='under_review' WHERE submission_id=?",
						submissionId);
				skillMatrixNotificationService.notifyHodReviewRequired(submissionId, body != null ? body.getOverallComment() : null);
			}
		} else {
			// Any manager rejection stops workflow.
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval SET manager_approved='no', hod_approved='pending', final_status='rejected', manager_comment=?, updated_at=NOW() WHERE id=?",
					body != null ? body.getOverallComment() : null, approvalId);
			jdbcTemplate.update("UPDATE skillmatrix_assessment_submission SET status='rejected', approved_at=NOW() WHERE submission_id=?",
					submissionId);
			String statusLabel = hasSentBack ? "Changes Requested" : "Rejected";
			String introLine = hasSentBack
					? "Your Skill Matrix submission has been sent back by your Manager. Please review the remarks and resubmit."
					: "Your Skill Matrix submission has been rejected by your Manager.";
			skillMatrixNotificationService.notifyEmployeeStatus(
					submissionId,
					statusLabel,
					introLine,
					"Manager remarks",
					body != null ? body.getOverallComment() : null);
		}
	}

	/* ---------------- HOD (Level 2) ---------------- */

	@Transactional(readOnly = true)
	public Page<SkillMatrixApproveQueueRowDTO> listHodQueue(Long hodEmpId, int page, int size, String submissionId, String employeeName,
			String deptName, String status, String sortColumn, String sortDirection) {
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));

		StringBuilder where = new StringBuilder(" WHERE a.hod_id = ? AND a.manager_approved='yes' AND a.final_status='pending' ");
		List<Object> args = new ArrayList<>();
		args.add(hodEmpId);
		if (StringUtils.hasText(submissionId)) {
			where.append(" AND s.submission_id LIKE ? ");
			args.add("%" + submissionId.trim() + "%");
		}
		if (StringUtils.hasText(employeeName)) {
			where.append(" AND s.employee_name LIKE ? ");
			args.add("%" + employeeName.trim() + "%");
		}
		if (StringUtils.hasText(deptName)) {
			where.append(" AND s.dept_name LIKE ? ");
			args.add("%" + deptName.trim() + "%");
		}
		if (StringUtils.hasText(status)) {
			where.append(" AND s.status = ? ");
			args.add(status.trim());
		}

		String orderBy = " ORDER BY s.updated_at DESC ";
		String sql = "SELECT s.submission_id, s.employee_id, s.employee_name, s.dept_name, s.designation, s.status, s.submitted_at, "
				+ "NULL AS review_deadline, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating r WHERE r.submission_id = s.submission_id) AS total_skills, "
				+ "(SELECT COUNT(1) FROM skillmatrix_assessment_skill_rating r WHERE r.submission_id = s.submission_id AND r.manager_decision IS NOT NULL AND r.manager_decision <> 'pending') AS reviewed_skills, "
				+ "0 AS c_approved, 0 AS c_adjusted, 0 AS c_sent_back, 0 AS c_rejected "
				+ "FROM skillmatrix_assessment_approval a "
				+ "JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
				+ where.toString()
				+ orderBy
				+ " LIMIT " + safeSize + " OFFSET " + (safePage * safeSize);

		String countSql = "SELECT COUNT(1) FROM skillmatrix_assessment_approval a "
				+ "JOIN skillmatrix_assessment_submission s ON s.submission_id = a.submission_id "
				+ where.toString();
		long total = jdbcTemplate.queryForObject(countSql, args.toArray(), Long.class);

		List<SkillMatrixApproveQueueRowDTO> rows = jdbcTemplate.query(sql, args.toArray(), (rs, rowNum) -> {
			SkillMatrixApproveQueueRowDTO dto = new SkillMatrixApproveQueueRowDTO();
			dto.setSubmissionId(rs.getString("submission_id"));
			dto.setEmployeeId(rs.getLong("employee_id"));
			dto.setEmployeeName(rs.getString("employee_name"));
			dto.setDeptName(rs.getString("dept_name"));
			dto.setDesignation(rs.getString("designation"));
			dto.setStatus(rs.getString("status"));
			dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
			dto.setReviewDeadline(null);
			dto.setTotalSkills(rs.getInt("total_skills"));
			dto.setReviewedSkills(rs.getInt("reviewed_skills"));
			return dto;
		});
		return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getHodSubmissionDetail(Long hodEmpId, String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		List<Map<String, Object>> ok = jdbcTemplate.queryForList(
				"SELECT 1 FROM skillmatrix_assessment_approval WHERE submission_id=? AND hod_id=? AND manager_approved='yes' AND final_status='pending' ORDER BY revision_round DESC LIMIT 1",
				submissionId, hodEmpId);
		if (ok.isEmpty()) {
			throw new IllegalStateException("No HOD approval request found.");
		}
		Map<String, Object> out = new java.util.HashMap<>();
		Map<String, Object> s = jdbcTemplate.queryForMap(
				"SELECT submission_id, employee_id, employee_name, dept_name, designation, status, submitted_at FROM skillmatrix_assessment_submission WHERE submission_id=?",
				submissionId);
		out.put("submission", s);
		Map<String, Object> a = jdbcTemplate.queryForMap(
				"SELECT manager_approved, hod_approved, final_status, manager_comment, hod_comment, revision_round "
						+ "FROM skillmatrix_assessment_approval WHERE submission_id=? AND hod_id=? ORDER BY revision_round DESC LIMIT 1",
				submissionId, hodEmpId);
		out.put("approval", a);
		return out;
	}

	@Transactional
	public void saveHodDecision(Long hodEmpId, String submissionId, SkillMatrixHodDecisionRequest body) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		String decision = body != null && StringUtils.hasText(body.getDecision()) ? body.getDecision().trim().toLowerCase(Locale.ROOT) : "";
		if (!List.of("approved", "rejected").contains(decision)) {
			throw new IllegalArgumentException("Invalid decision.");
		}
		// enabled only after manager approval, and no further approval after rejection/approval
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT id, manager_approved, final_status FROM skillmatrix_assessment_approval WHERE submission_id=? AND hod_id=? ORDER BY revision_round DESC LIMIT 1",
				submissionId, hodEmpId);
		if (rows.isEmpty()) {
			throw new IllegalStateException("No HOD approval request found.");
		}
		Long approvalId = ((Number) rows.get(0).get("id")).longValue();
		String mgr = String.valueOf(rows.get(0).get("manager_approved"));
		String fin = String.valueOf(rows.get(0).get("final_status"));
		if (!"yes".equalsIgnoreCase(mgr)) {
			throw new IllegalStateException("HOD approval is not enabled yet.");
		}
		if ("approved".equalsIgnoreCase(fin) || "rejected".equalsIgnoreCase(fin)) {
			throw new IllegalStateException("Decision is already final.");
		}

		if ("approved".equals(decision)) {
			// Require all skills to have HOD decision before final approval.
			Integer pendingSkills = jdbcTemplate.queryForObject(
					"SELECT COUNT(1) FROM skillmatrix_assessment_approval_skill WHERE approval_id=? AND COALESCE(hod_decision,'pending')='pending'",
					new Object[] { approvalId }, Integer.class);
			if (pendingSkills != null && pendingSkills.intValue() > 0) {
				throw new IllegalStateException("Please decide all skills before approving.");
			}

			// Mark per-skill FINAL decision based on HOD decisions (manager already approved to reach this stage).
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill SET decision='approved' WHERE approval_id=? AND COALESCE(hod_decision,'pending')='approved'",
					approvalId);
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill SET decision='rejected' WHERE approval_id=? AND COALESCE(hod_decision,'pending')='rejected'",
					approvalId);

			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval SET hod_approved='yes', final_status='approved', hod_comment=?, updated_at=NOW() WHERE id=?",
					body != null ? body.getComment() : null, approvalId);
			jdbcTemplate.update("UPDATE skillmatrix_assessment_submission SET status='approved', approved_at=NOW() WHERE submission_id=?",
					submissionId);
			skillMatrixNotificationService.notifyEmployeeStatus(
					submissionId,
					"Approved",
					"Your Skill Matrix submission has been approved by the Department HOD.",
					"HOD remarks",
					body != null ? body.getComment() : null);
		} else {
			if (body == null || !StringUtils.hasText(body.getComment())) {
				throw new IllegalArgumentException("comment is required.");
			}
			// Reject final: mark any pending per-skill decisions as rejected in final decision as well.
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill SET hod_decision='rejected', hod_comment=?, decision='rejected', decided_at=NOW() "
							+ "WHERE approval_id=? AND COALESCE(hod_decision,'pending')='pending'",
					body.getComment().trim(), approvalId);
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval SET hod_approved='no', final_status='rejected', hod_comment=?, updated_at=NOW() WHERE id=?",
					body != null ? body.getComment() : null, approvalId);
			jdbcTemplate.update("UPDATE skillmatrix_assessment_submission SET status='rejected', approved_at=NOW() WHERE submission_id=?",
					submissionId);
			skillMatrixNotificationService.notifyEmployeeStatus(
					submissionId,
					"Rejected",
					"Your Skill Matrix submission has been rejected by the Department HOD.",
					"HOD remarks",
					body != null ? body.getComment() : null);
		}
	}

	private Long latestApprovalIdForManager(Long managerEmpId, String submissionId) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT id FROM skillmatrix_assessment_approval WHERE submission_id=? AND manager_id=? ORDER BY revision_round DESC LIMIT 1",
				submissionId, managerEmpId);
		if (rows.isEmpty()) {
			return null;
		}
		return ((Number) rows.get(0).get("id")).longValue();
	}

	/** Read-only: allows either manager or HOD to open the detail view. */
	private Long latestApprovalIdForApprover(Long empId, String submissionId) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT id FROM skillmatrix_assessment_approval WHERE submission_id=? AND (manager_id=? OR hod_id=?) ORDER BY revision_round DESC LIMIT 1",
				submissionId, empId, empId);
		if (rows.isEmpty()) {
			return null;
		}
		return ((Number) rows.get(0).get("id")).longValue();
	}

	private void ensureApprovalSkillRows(long approvalId, String submissionId) {
		// Insert one row per skill rating into approval_skill (pending) if missing.
		jdbcTemplate.update(
				"INSERT INTO skillmatrix_assessment_approval_skill(approval_id, skill_rating_id, submission_id, skill_id, skill_name, employee_rating, decision, manager_decision, hod_decision) "
						+ "SELECT ?, r.id, r.submission_id, r.skill_id, r.skill_name, r.self_rating, 'pending', 'pending', 'pending' "
						+ "FROM skillmatrix_assessment_skill_rating r "
						+ "WHERE r.submission_id = ? AND NOT EXISTS ("
						+ "  SELECT 1 FROM skillmatrix_assessment_approval_skill x WHERE x.approval_id = ? AND x.skill_rating_id = r.id"
						+ ")",
				approvalId, submissionId, approvalId);
	}

	private SkillMatrixApproveAspirationDTO loadAspirationForSubmission(String submissionId) {
		SkillMatrixApproveAspirationDTO dto = new SkillMatrixApproveAspirationDTO();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT id, target_role_2yr, message_to_manager FROM skillmatrix_assessment_aspiration WHERE submission_id = ? LIMIT 1",
				submissionId);
		if (rows.isEmpty()) {
			return dto;
		}
		Map<String, Object> r = rows.get(0);
		long aspirationId = ((Number) r.get("id")).longValue();
		dto.setTargetRole2yr(r.get("target_role_2yr") != null ? String.valueOf(r.get("target_role_2yr")) : null);
		dto.setMessageToManager(r.get("message_to_manager") != null ? String.valueOf(r.get("message_to_manager")) : null);
		List<Map<String, Object>> srows = jdbcTemplate.queryForList(
				"SELECT skill_name FROM skillmatrix_assessment_aspiration_skill WHERE aspiration_id = ? ORDER BY id ASC",
				aspirationId);
		dto.setSkillsToLearn(srows.stream().map(x -> String.valueOf(x.get("skill_name"))).collect(Collectors.toList()));
		return dto;
	}

	private List<SkillMatrixApproveDetailSkillDTO> loadApproveSkills(long approvalId, String submissionId, int page, int size) {
		int offset = page * size;
		List<SkillMatrixApproveDetailSkillDTO> skills = jdbcTemplate.query(
				"SELECT r.id AS skill_rating_id, r.skill_id, r.skill_name, scm.category_name AS skill_category, r.is_required, "
						+ "r.self_rating, r.years_experience, r.last_used, r.usage_frequency, r.what_can_you_do, "
						+ "r.used_in_project, r.github_portfolio_url, r.colleague_endorser, r.knowledge_session_note, "
						+ "COALESCE(aks.manager_decision,'pending') AS manager_decision, "
						+ "COALESCE(aks.hod_decision,'pending') AS hod_decision, "
						+ "COALESCE(aks.decision,'pending') AS final_decision, "
						+ "aks.manager_rating, aks.manager_comment "
						+ "FROM skillmatrix_assessment_skill_rating r "
						+ "LEFT JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id = ? AND aks.skill_rating_id = r.id "
						+ "LEFT JOIN skills_master sm ON sm.skill_id = r.skill_id "
						+ "LEFT JOIN skill_category_master scm ON scm.category_id = sm.category_id "
						+ "WHERE r.submission_id = ? ORDER BY r.is_required DESC, r.skill_name ASC LIMIT ? OFFSET ?",
				new Object[] { approvalId, submissionId, size, offset }, (rs, rowNum) -> {
					SkillMatrixApproveDetailSkillDTO dto = new SkillMatrixApproveDetailSkillDTO();
					dto.setSkillRatingId(rs.getLong("skill_rating_id"));
					dto.setSkillId(rs.getInt("skill_id"));
					dto.setSkillName(rs.getString("skill_name"));
					dto.setSkillCategory(rs.getString("skill_category"));
					dto.setRequired(rs.getBoolean("is_required"));
					dto.setSelfRating(rs.getInt("self_rating"));
					dto.setYearsExperience(rs.getString("years_experience"));
					dto.setLastUsed(rs.getString("last_used"));
					dto.setUsageFrequency(rs.getString("usage_frequency"));
					dto.setWhatCanYouDo(rs.getString("what_can_you_do"));
					dto.setUsedInProject(rs.getBoolean("used_in_project"));
					dto.setGithubPortfolioUrl(rs.getString("github_portfolio_url"));
					dto.setColleagueEndorser(rs.getString("colleague_endorser"));
					dto.setKnowledgeSessionNote(rs.getString("knowledge_session_note"));
					String mdec = rs.getString("manager_decision");
					dto.setManagerDecision(StringUtils.hasText(mdec) ? mdec : "pending");
					String hdec = rs.getString("hod_decision");
					dto.setHodDecision(StringUtils.hasText(hdec) ? hdec : "pending");
					String fdec = rs.getString("final_decision");
					dto.setFinalDecision(StringUtils.hasText(fdec) ? fdec : "pending");
					dto.setManagerRating(rs.getObject("manager_rating") != null ? rs.getInt("manager_rating") : null);
					dto.setManagerComment(rs.getString("manager_comment"));
					return dto;
				});

		Map<Long, SkillMatrixApproveDetailSkillDTO> byRatingId = skills.stream()
				.collect(Collectors.toMap(SkillMatrixApproveDetailSkillDTO::getSkillRatingId, x -> x, (a, b) -> a, LinkedHashMap::new));

		// Subskills
		List<Map<String, Object>> subRows = jdbcTemplate.queryForList(
				"SELECT skill_rating_id, subskill_id, subskill_name, is_confident FROM skillmatrix_assessment_subskill_selection WHERE submission_id = ? ORDER BY id ASC",
				submissionId);
		for (Map<String, Object> r : subRows) {
			Long rid = ((Number) r.get("skill_rating_id")).longValue();
			SkillMatrixApproveDetailSkillDTO sk = byRatingId.get(rid);
			if (sk == null) {
				continue;
			}
			SkillMatrixApproveSubskillDTO s = new SkillMatrixApproveSubskillDTO();
			s.setSubskillId(r.get("subskill_id") != null ? ((Number) r.get("subskill_id")).intValue() : null);
			s.setSubskillName(String.valueOf(r.get("subskill_name")));
			s.setConfident(toBool(r.get("is_confident")));
			sk.getSubskills().add(s);
		}

		// Certifications
		List<Map<String, Object>> certRows = jdbcTemplate.queryForList(
				"SELECT skill_rating_id, cert_name, issuing_body, date_obtained FROM skillmatrix_assessment_certification WHERE submission_id = ? ORDER BY id ASC",
				submissionId);
		for (Map<String, Object> r : certRows) {
			Long rid = ((Number) r.get("skill_rating_id")).longValue();
			SkillMatrixApproveDetailSkillDTO sk = byRatingId.get(rid);
			if (sk == null) {
				continue;
			}
			SkillMatrixApproveCertificationDTO c = new SkillMatrixApproveCertificationDTO();
			c.setCertName(String.valueOf(r.get("cert_name")));
			c.setIssuingBody(String.valueOf(r.get("issuing_body")));
			c.setDateObtained(toUtilDate(r.get("date_obtained")));
			sk.getCertifications().add(c);
		}

		// Trainings
		List<Map<String, Object>> trRows = jdbcTemplate.queryForList(
				"SELECT skill_rating_id, course_name, platform_institute, completion_year FROM skillmatrix_assessment_training WHERE submission_id = ? ORDER BY id ASC",
				submissionId);
		for (Map<String, Object> r : trRows) {
			Long rid = ((Number) r.get("skill_rating_id")).longValue();
			SkillMatrixApproveDetailSkillDTO sk = byRatingId.get(rid);
			if (sk == null) {
				continue;
			}
			SkillMatrixApproveTrainingDTO t = new SkillMatrixApproveTrainingDTO();
			t.setCourseName(String.valueOf(r.get("course_name")));
			t.setPlatformInstitute(r.get("platform_institute") != null ? String.valueOf(r.get("platform_institute")) : null);
			t.setCompletionYear(r.get("completion_year") != null ? ((Number) r.get("completion_year")).intValue() : null);
			sk.getTrainings().add(t);
		}

		// Project evidence (linked in step 4)
		List<Map<String, Object>> prRows = jdbcTemplate.queryForList(
				"SELECT ps.skill_id, p.project_name FROM skillmatrix_assessment_project_skill ps "
						+ "JOIN skillmatrix_assessment_project p ON p.id = ps.assessment_project_id "
						+ "WHERE ps.submission_id = ? ORDER BY p.id ASC",
				submissionId);
		for (Map<String, Object> r : prRows) {
			Integer skillId = r.get("skill_id") != null ? ((Number) r.get("skill_id")).intValue() : null;
			String pname = r.get("project_name") != null ? String.valueOf(r.get("project_name")) : null;
			if (skillId == null || pname == null) {
				continue;
			}
			for (SkillMatrixApproveDetailSkillDTO sk : skills) {
				if (Objects.equals(sk.getSkillId(), skillId) && !sk.getProjectNames().contains(pname)) {
					sk.getProjectNames().add(pname);
				}
			}
		}

		return skills;
	}

	@Transactional(readOnly = true)
	public List<SkillMatrixApproveSkillMetaDTO> listApproveSubmissionSkillsMeta(Long managerEmpId, String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		Long approvalId = latestApprovalIdForApprover(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found for this submission.");
		}
		// IMPORTANT: Keep GET read-only. Do not insert/update here.
		return jdbcTemplate.query(
				"SELECT r.id AS skill_rating_id, r.skill_id, r.skill_name, scm.category_name AS skill_category, r.is_required, r.self_rating, "
						+ "COALESCE(aks.manager_decision,'pending') AS decision, aks.manager_rating "
						+ "FROM skillmatrix_assessment_skill_rating r "
						+ "LEFT JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id = ? AND aks.skill_rating_id = r.id "
						+ "LEFT JOIN skills_master sm ON sm.skill_id = r.skill_id "
						+ "LEFT JOIN skill_category_master scm ON scm.category_id = sm.category_id "
						+ "WHERE r.submission_id = ? "
						+ "ORDER BY r.is_required DESC, r.skill_name ASC",
				new Object[] { approvalId, submissionId }, (rs, rowNum) -> {
					SkillMatrixApproveSkillMetaDTO dto = new SkillMatrixApproveSkillMetaDTO();
					dto.setSkillRatingId(rs.getLong("skill_rating_id"));
					dto.setSkillId(rs.getInt("skill_id"));
					dto.setSkillName(rs.getString("skill_name"));
					dto.setSkillCategory(rs.getString("skill_category"));
					dto.setRequired(rs.getBoolean("is_required"));
					dto.setSelfRating(rs.getInt("self_rating"));
					dto.setDecision(rs.getString("decision"));
					dto.setManagerRating(rs.getObject("manager_rating") != null ? rs.getInt("manager_rating") : null);
					return dto;
				});
	}

	@Transactional(readOnly = true)
	public SkillMatrixApproveDetailSkillDTO getApproveSubmissionSkillDetail(Long managerEmpId, String submissionId, long skillRatingId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		Long approvalId = latestApprovalIdForApprover(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found for this submission.");
		}
		// IMPORTANT: Keep GET read-only. Do not insert/update here.
		// Load the specific skill row (same shape as list) but filtered
		List<SkillMatrixApproveDetailSkillDTO> skills = jdbcTemplate.query(
				"SELECT r.id AS skill_rating_id, r.skill_id, r.skill_name, scm.category_name AS skill_category, r.is_required, "
						+ "r.self_rating, r.years_experience, r.last_used, r.usage_frequency, r.what_can_you_do, "
						+ "r.used_in_project, r.github_portfolio_url, r.colleague_endorser, r.knowledge_session_note, "
						+ "COALESCE(aks.manager_decision,'pending') AS manager_decision, "
						+ "COALESCE(aks.hod_decision,'pending') AS hod_decision, "
						+ "COALESCE(aks.decision,'pending') AS final_decision, "
						+ "aks.manager_rating, aks.manager_comment "
						+ "FROM skillmatrix_assessment_skill_rating r "
						+ "LEFT JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id = ? AND aks.skill_rating_id = r.id "
						+ "LEFT JOIN skills_master sm ON sm.skill_id = r.skill_id "
						+ "LEFT JOIN skill_category_master scm ON scm.category_id = sm.category_id "
						+ "WHERE r.submission_id = ? AND r.id = ? LIMIT 1",
				new Object[] { approvalId, submissionId, skillRatingId }, (rs, rowNum) -> {
					SkillMatrixApproveDetailSkillDTO dto = new SkillMatrixApproveDetailSkillDTO();
					dto.setSkillRatingId(rs.getLong("skill_rating_id"));
					dto.setSkillId(rs.getInt("skill_id"));
					dto.setSkillName(rs.getString("skill_name"));
					dto.setSkillCategory(rs.getString("skill_category"));
					dto.setRequired(rs.getBoolean("is_required"));
					dto.setSelfRating(rs.getInt("self_rating"));
					dto.setYearsExperience(rs.getString("years_experience"));
					dto.setLastUsed(rs.getString("last_used"));
					dto.setUsageFrequency(rs.getString("usage_frequency"));
					dto.setWhatCanYouDo(rs.getString("what_can_you_do"));
					dto.setUsedInProject(rs.getBoolean("used_in_project"));
					dto.setGithubPortfolioUrl(rs.getString("github_portfolio_url"));
					dto.setColleagueEndorser(rs.getString("colleague_endorser"));
					dto.setKnowledgeSessionNote(rs.getString("knowledge_session_note"));
					String mdec = rs.getString("manager_decision");
					dto.setManagerDecision(StringUtils.hasText(mdec) ? mdec : "pending");
					String hdec = rs.getString("hod_decision");
					dto.setHodDecision(StringUtils.hasText(hdec) ? hdec : "pending");
					String fdec = rs.getString("final_decision");
					dto.setFinalDecision(StringUtils.hasText(fdec) ? fdec : "pending");
					dto.setManagerRating(rs.getObject("manager_rating") != null ? rs.getInt("manager_rating") : null);
					dto.setManagerComment(rs.getString("manager_comment"));
					return dto;
				});
		if (skills.isEmpty()) {
			throw new IllegalStateException("Skill not found for this submission.");
		}
		SkillMatrixApproveDetailSkillDTO sk = skills.get(0);
		// Attach evidence for this one skill
		Long rid = sk.getSkillRatingId();
		List<Map<String, Object>> subRows = jdbcTemplate.queryForList(
				"SELECT subskill_id, subskill_name, is_confident FROM skillmatrix_assessment_subskill_selection WHERE submission_id = ? AND skill_rating_id = ? ORDER BY id ASC",
				submissionId, rid);
		for (Map<String, Object> r : subRows) {
			SkillMatrixApproveSubskillDTO s = new SkillMatrixApproveSubskillDTO();
			s.setSubskillId(r.get("subskill_id") != null ? ((Number) r.get("subskill_id")).intValue() : null);
			s.setSubskillName(String.valueOf(r.get("subskill_name")));
			s.setConfident(toBool(r.get("is_confident")));
			sk.getSubskills().add(s);
		}
		List<Map<String, Object>> certRows = jdbcTemplate.queryForList(
				"SELECT cert_name, issuing_body, date_obtained FROM skillmatrix_assessment_certification WHERE submission_id = ? AND skill_rating_id = ? ORDER BY id ASC",
				submissionId, rid);
		for (Map<String, Object> r : certRows) {
			SkillMatrixApproveCertificationDTO c = new SkillMatrixApproveCertificationDTO();
			c.setCertName(String.valueOf(r.get("cert_name")));
			c.setIssuingBody(String.valueOf(r.get("issuing_body")));
			c.setDateObtained(toUtilDate(r.get("date_obtained")));
			sk.getCertifications().add(c);
		}
		List<Map<String, Object>> trRows = jdbcTemplate.queryForList(
				"SELECT course_name, platform_institute, completion_year FROM skillmatrix_assessment_training WHERE submission_id = ? AND skill_rating_id = ? ORDER BY id ASC",
				submissionId, rid);
		for (Map<String, Object> r : trRows) {
			SkillMatrixApproveTrainingDTO t = new SkillMatrixApproveTrainingDTO();
			t.setCourseName(String.valueOf(r.get("course_name")));
			t.setPlatformInstitute(r.get("platform_institute") != null ? String.valueOf(r.get("platform_institute")) : null);
			t.setCompletionYear(r.get("completion_year") != null ? ((Number) r.get("completion_year")).intValue() : null);
			sk.getTrainings().add(t);
		}
		List<Map<String, Object>> prRows = jdbcTemplate.queryForList(
				"SELECT ps.skill_id, p.project_name FROM skillmatrix_assessment_project_skill ps "
						+ "JOIN skillmatrix_assessment_project p ON p.id = ps.assessment_project_id "
						+ "WHERE ps.submission_id = ? AND ps.skill_id = ? ORDER BY p.id ASC",
				submissionId, sk.getSkillId());
		for (Map<String, Object> r : prRows) {
			String pname = r.get("project_name") != null ? String.valueOf(r.get("project_name")) : null;
			if (pname != null && !sk.getProjectNames().contains(pname)) {
				sk.getProjectNames().add(pname);
			}
		}
		return sk;
	}

	@Transactional
	public int bulkApproveAllPending(Long managerEmpId, String submissionId) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		Long approvalId = latestApprovalIdForApprover(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found.");
		}
		Map<String, Object> appr = jdbcTemplate.queryForMap(
				"SELECT manager_id, hod_id, manager_approved, final_status FROM skillmatrix_assessment_approval WHERE id=?",
				approvalId);
		Long mid = appr.get("manager_id") != null ? ((Number) appr.get("manager_id")).longValue() : null;
		Long hid = appr.get("hod_id") != null ? ((Number) appr.get("hod_id")).longValue() : null;
		boolean isManager = mid != null && mid.longValue() == managerEmpId;
		boolean isHod = hid != null && hid.longValue() == managerEmpId;
		if (!isManager && !isHod) {
			throw new IllegalStateException("Not allowed.");
		}
		String fin = appr.get("final_status") != null ? String.valueOf(appr.get("final_status")) : "pending";
		if ("approved".equalsIgnoreCase(fin) || "rejected".equalsIgnoreCase(fin)) {
			throw new IllegalStateException("Decision is already final.");
		}
		String mgrApproved = appr.get("manager_approved") != null ? String.valueOf(appr.get("manager_approved")) : "pending";
		if (isHod && !"yes".equalsIgnoreCase(mgrApproved)) {
			throw new IllegalStateException("HOD approval is not enabled yet.");
		}
		ensureApprovalSkillRows(approvalId, submissionId);
		int updated;
		if (isHod) {
			// HOD pending -> approved (also final approved for each skill)
			updated = jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill aks "
							+ "JOIN skillmatrix_assessment_skill_rating r ON r.id = aks.skill_rating_id "
							+ "SET aks.hod_decision='approved', aks.hod_comment=NULL, aks.decided_at=NOW(), aks.decision='approved' "
							+ "WHERE aks.approval_id=? AND r.submission_id=? AND COALESCE(aks.hod_decision,'pending')='pending'",
					approvalId, submissionId);
		} else {
			// Manager pending -> approved (final stays pending)
			updated = jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill aks "
							+ "JOIN skillmatrix_assessment_skill_rating r ON r.id = aks.skill_rating_id "
							+ "SET aks.manager_decision='approved', aks.manager_rating=NULL, aks.manager_comment=NULL, aks.decided_at=NOW() "
							+ "WHERE aks.approval_id=? AND r.submission_id=? AND COALESCE(aks.manager_decision,'pending')='pending'",
					approvalId, submissionId);
			// Mirror into skill_rating: decision + final_rating=self_rating
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_skill_rating r "
							+ "JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id=? AND aks.skill_rating_id=r.id "
							+ "SET r.manager_decision='approved', r.manager_rating=NULL, r.manager_comment=NULL, r.manager_reviewed_at=NOW(), r.final_rating=r.self_rating "
							+ "WHERE r.submission_id=? AND aks.manager_decision='approved'",
					approvalId, submissionId);
		}
		return updated;
	}

	@Transactional
	public int bulkRejectAllPending(Long managerEmpId, String submissionId, SkillMatrixApproveBulkDecisionRequest body) {
		if (!StringUtils.hasText(submissionId)) {
			throw new IllegalArgumentException("submissionId is required.");
		}
		if (body == null || !StringUtils.hasText(body.getManagerComment())) {
			throw new IllegalArgumentException("managerComment is required for reject-all.");
		}
		Long approvalId = latestApprovalIdForApprover(managerEmpId, submissionId);
		if (approvalId == null) {
			throw new IllegalStateException("No approval request found.");
		}
		Map<String, Object> appr = jdbcTemplate.queryForMap(
				"SELECT manager_id, hod_id, manager_approved, final_status FROM skillmatrix_assessment_approval WHERE id=?",
				approvalId);
		Long mid = appr.get("manager_id") != null ? ((Number) appr.get("manager_id")).longValue() : null;
		Long hid = appr.get("hod_id") != null ? ((Number) appr.get("hod_id")).longValue() : null;
		boolean isManager = mid != null && mid.longValue() == managerEmpId;
		boolean isHod = hid != null && hid.longValue() == managerEmpId;
		if (!isManager && !isHod) {
			throw new IllegalStateException("Not allowed.");
		}
		String fin = appr.get("final_status") != null ? String.valueOf(appr.get("final_status")) : "pending";
		if ("approved".equalsIgnoreCase(fin) || "rejected".equalsIgnoreCase(fin)) {
			throw new IllegalStateException("Decision is already final.");
		}
		String mgrApproved = appr.get("manager_approved") != null ? String.valueOf(appr.get("manager_approved")) : "pending";
		if (isHod && !"yes".equalsIgnoreCase(mgrApproved)) {
			throw new IllegalStateException("HOD approval is not enabled yet.");
		}
		ensureApprovalSkillRows(approvalId, submissionId);
		String cmt = body.getManagerComment().trim();
		int updated;
		if (isHod) {
			updated = jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill aks "
							+ "JOIN skillmatrix_assessment_skill_rating r ON r.id = aks.skill_rating_id "
							+ "SET aks.hod_decision='rejected', aks.hod_comment=?, aks.decided_at=NOW(), aks.decision='rejected' "
							+ "WHERE aks.approval_id=? AND r.submission_id=? AND COALESCE(aks.hod_decision,'pending')='pending'",
					cmt, approvalId, submissionId);
		} else {
			updated = jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_approval_skill aks "
							+ "JOIN skillmatrix_assessment_skill_rating r ON r.id = aks.skill_rating_id "
							+ "SET aks.manager_decision='rejected', aks.decision='rejected', aks.manager_rating=NULL, aks.manager_comment=?, aks.decided_at=NOW() "
							+ "WHERE aks.approval_id=? AND r.submission_id=? AND COALESCE(aks.manager_decision,'pending')='pending'",
					cmt, approvalId, submissionId);
			jdbcTemplate.update(
					"UPDATE skillmatrix_assessment_skill_rating r "
							+ "JOIN skillmatrix_assessment_approval_skill aks ON aks.approval_id=? AND aks.skill_rating_id=r.id "
							+ "SET r.manager_decision='rejected', r.manager_rating=NULL, r.manager_comment=?, r.manager_reviewed_at=NOW(), r.final_rating=r.self_rating "
							+ "WHERE r.submission_id=? AND aks.manager_decision='rejected'",
					approvalId, cmt, submissionId);
		}
		return updated;
	}

	/**
	 * MySQL/JDBC may return {@link LocalDateTime}/{@link LocalDate} depending on driver.
	 * Convert safely to {@link java.util.Date} for DTOs.
	 */
	private static Date toUtilDate(Object v) {
		if (v == null) {
			return null;
		}
		if (v instanceof Date) {
			return (Date) v;
		}
		if (v instanceof java.sql.Timestamp) {
			return new Date(((java.sql.Timestamp) v).getTime());
		}
		if (v instanceof java.sql.Date) {
			return new Date(((java.sql.Date) v).getTime());
		}
		if (v instanceof LocalDateTime) {
			LocalDateTime ldt = (LocalDateTime) v;
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		if (v instanceof LocalDate) {
			LocalDate ld = (LocalDate) v;
			return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}

	private static boolean toBool(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		if (v instanceof Number) {
			return ((Number) v).intValue() != 0;
		}
		String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
		return "1".equals(s) || "true".equals(s) || "y".equals(s) || "yes".equals(s);
	}

	@Transactional
	public SkillMatrixCustomSkillRequestRowDTO proposeSkill(Long empId, SkillMatrixProposeSkillRequest body) {
		ensureCustomSkillRequestTable();
		if (body == null || !StringUtils.hasText(body.getSkillName())) {
			throw new IllegalArgumentException("Skill name is required.");
		}
		if (body.getCategoryId() == null) {
			throw new IllegalArgumentException("Category is required.");
		}
		if (!skillCategoryMasterRepository.existsById(body.getCategoryId())) {
			throw new IllegalArgumentException("Invalid category.");
		}
		Long deptId = resolveDepartmentIdForEmp(empId);
		if (deptId == null) {
			throw new IllegalStateException("No department is mapped for this employee.");
		}
		Employee emp = employeeRepository.findByEmpId(empId);
		if (emp == null) {
			throw new IllegalStateException("Employee record not found.");
		}
		JobRole jobRole = emp.getJobRoleId() != null ? jobRoleRepository.findByjobRoleId(emp.getJobRoleId()) : null;
		Department dept = departmentRepository.findByDeptId(deptId);
		Long hodId = dept != null ? dept.getHodId() : null;
		if (hodId == null || hodId.longValue() <= 0L) {
			throw new IllegalStateException("HOD is not configured for your department.");
		}
		Employee hod = employeeRepository.findByEmpId(hodId);
		String name = body.getSkillName().trim();
		if (skillsMasterRepository.existsInDepartmentIgnoreCaseSkillName(deptId, name)) {
			throw new IllegalStateException("A skill with this name already exists for your department.");
		}
		SkillCategoryMaster cat = skillCategoryMasterRepository.findById(body.getCategoryId()).orElse(null);
		String catName = cat != null ? cat.getCategoryName() : null;

		Long existingPending = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_custom_skill_request "
						+ "WHERE dept_id = ? AND LOWER(skill_name) = LOWER(?) AND status = 'pending'",
				new Object[] { deptId, name }, Long.class);
		if (existingPending != null && existingPending.longValue() > 0L) {
			throw new IllegalStateException("This skill is already pending HOD approval.");
		}

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			java.sql.PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO skillmatrix_custom_skill_request("
							+ "requested_by_emp_id, requested_by_name, designation, dept_id, dept_name, hod_id, hod_name, "
							+ "skill_name, category_id, category_name, status) "
							+ "VALUES(?,?,?,?,?,?,?,?,?,?,'pending')",
					new String[] { "id" });
			ps.setLong(1, empId);
			ps.setString(2, emp.getName());
			ps.setString(3, resolveDesignationName(emp, jobRole));
			ps.setLong(4, deptId);
			ps.setString(5, dept != null ? dept.getName() : null);
			ps.setLong(6, hodId);
			ps.setString(7, hod != null ? hod.getName() : "HOD");
			ps.setString(8, name);
			ps.setInt(9, body.getCategoryId());
			ps.setString(10, catName);
			return ps;
		}, keyHolder);
		Number id = keyHolder.getKey();
		if (id != null) {
			skillMatrixNotificationService.notifyCustomSkillRequestPending(id.longValue());
		}
		return loadCustomSkillRequestRow(id != null ? id.longValue() : null);
	}

	@Transactional
	public Page<SkillMatrixCustomSkillRequestRowDTO> listCustomSkillRequestsForHod(Long reviewerEmpId, int page, int size) {
		ensureCustomSkillRequestTable();
		int safePage = Math.max(0, page);
		int safeSize = Math.min(100, Math.max(1, size));
		int offset = safePage * safeSize;
		Long total = jdbcTemplate.queryForObject(
				"SELECT COUNT(1) FROM skillmatrix_custom_skill_request WHERE hod_id = ?",
				new Object[] { reviewerEmpId }, Long.class);
		List<SkillMatrixCustomSkillRequestRowDTO> rows = jdbcTemplate.query(
				"SELECT id, requested_by_emp_id, requested_by_name, designation, dept_name, hod_name, skill_name, "
						+ "category_id, category_name, status, approved_skill_type, decision_comment, created_skill_id, "
						+ "created_at, decided_at, hod_id "
						+ "FROM skillmatrix_custom_skill_request WHERE hod_id = ? "
						+ "ORDER BY CASE WHEN status = 'pending' THEN 0 ELSE 1 END, created_at DESC LIMIT ? OFFSET ?",
				new Object[] { reviewerEmpId, safeSize, offset },
				(rs, rowNum) -> mapCustomSkillRequestRow(rs, reviewerEmpId));
		return new PageImpl<>(rows, PageRequest.of(safePage, safeSize), total != null ? total.longValue() : 0L);
	}

	@Transactional
	public SkillMatrixCustomSkillRequestRowDTO decideCustomSkillRequest(Long reviewerEmpId, Long requestId,
			SkillMatrixCustomSkillDecisionRequest body) {
		ensureCustomSkillRequestTable();
		if (requestId == null) {
			throw new IllegalArgumentException("requestId is required.");
		}
		if (body == null || !StringUtils.hasText(body.getDecision())) {
			throw new IllegalArgumentException("decision is required.");
		}
		Map<String, Object> row = jdbcTemplate.queryForList(
				"SELECT id, hod_id, dept_id, skill_name, category_id, status "
						+ "FROM skillmatrix_custom_skill_request WHERE id = ?",
				requestId).stream().findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Custom skill request not found."));
		Long hodId = row.get("hod_id") != null ? ((Number) row.get("hod_id")).longValue() : null;
		if (hodId == null || reviewerEmpId == null || hodId.longValue() != reviewerEmpId.longValue()) {
			throw new IllegalStateException("You are not authorized to review this custom skill request.");
		}
		String status = row.get("status") != null ? String.valueOf(row.get("status")).trim().toLowerCase(Locale.ROOT) : "";
		if (!"pending".equals(status)) {
			throw new IllegalStateException("This custom skill request is already finalized.");
		}

		String decision = body.getDecision().trim().toLowerCase(Locale.ROOT);
		String comment = StringUtils.hasText(body.getComment()) ? body.getComment().trim() : null;
		if ("approved".equals(decision)) {
			String skillType = normalizeCustomSkillType(body.getSkillType());
			Long deptId = row.get("dept_id") != null ? ((Number) row.get("dept_id")).longValue() : null;
			String skillName = row.get("skill_name") != null ? String.valueOf(row.get("skill_name")).trim() : null;
			Integer categoryId = row.get("category_id") != null ? ((Number) row.get("category_id")).intValue() : null;
			if (deptId == null || !StringUtils.hasText(skillName) || categoryId == null) {
				throw new IllegalStateException("Custom skill request is missing required data.");
			}
			if (skillsMasterRepository.existsInDepartmentIgnoreCaseSkillName(deptId, skillName)) {
				throw new IllegalStateException("A skill with this name already exists for the department.");
			}
			SkillsMaster skill = new SkillsMaster();
			skill.setSkillName(skillName);
			skill.setCategoryId(categoryId);
			skill.setSkillType(skillType);
			skill.setIsActive(Boolean.TRUE);
			skill.setDepartmentId(deptId);
			SkillsMaster saved = skillsMasterRepository.save(skill);
			jdbcTemplate.update(
					"UPDATE skillmatrix_custom_skill_request SET status='approved', approved_skill_type=?, decision_comment=?, "
							+ "created_skill_id=?, decided_at=NOW(), updated_at=NOW() WHERE id=?",
					skillType, comment, saved.getSkillId(), requestId);
			skillMatrixNotificationService.notifyCustomSkillRequestStatus(
					requestId,
					"Approved",
					"Your custom skill request has been approved by the HOD and added to the Skill Matrix master.");
		} else if ("rejected".equals(decision)) {
			if (!StringUtils.hasText(comment)) {
				throw new IllegalArgumentException("Comment is required when rejecting a custom skill request.");
			}
			jdbcTemplate.update(
					"UPDATE skillmatrix_custom_skill_request SET status='rejected', decision_comment=?, decided_at=NOW(), updated_at=NOW() WHERE id=?",
					comment, requestId);
			skillMatrixNotificationService.notifyCustomSkillRequestStatus(
					requestId,
					"Rejected",
					"Your custom skill request has been rejected by the HOD.");
		} else {
			throw new IllegalArgumentException("decision must be approved or rejected.");
		}
		return loadCustomSkillRequestRow(requestId);
	}

	private SkillMatrixCustomSkillRequestRowDTO loadCustomSkillRequestRow(Long requestId) {
		if (requestId == null) {
			throw new IllegalStateException("Could not create custom skill request.");
		}
		return jdbcTemplate.query(
				"SELECT id, requested_by_emp_id, requested_by_name, designation, dept_name, hod_name, skill_name, "
						+ "category_id, category_name, status, approved_skill_type, decision_comment, created_skill_id, "
						+ "created_at, decided_at, hod_id "
						+ "FROM skillmatrix_custom_skill_request WHERE id = ?",
				new Object[] { requestId },
				rs -> rs.next() ? mapCustomSkillRequestRow(rs, null) : null);
	}

	private SkillMatrixCustomSkillRequestRowDTO mapCustomSkillRequestRow(java.sql.ResultSet rs, Long reviewerEmpId)
			throws java.sql.SQLException {
		SkillMatrixCustomSkillRequestRowDTO dto = new SkillMatrixCustomSkillRequestRowDTO();
		dto.setRequestId(rs.getLong("id"));
		dto.setRequestedByEmpId(rs.getLong("requested_by_emp_id"));
		dto.setRequestedByName(rs.getString("requested_by_name"));
		dto.setDesignation(rs.getString("designation"));
		dto.setDeptName(rs.getString("dept_name"));
		dto.setHodName(rs.getString("hod_name"));
		dto.setSkillName(rs.getString("skill_name"));
		dto.setCategoryId(rs.getObject("category_id") != null ? rs.getInt("category_id") : null);
		dto.setCategoryName(rs.getString("category_name"));
		dto.setStatus(rs.getString("status"));
		dto.setApprovedSkillType(rs.getString("approved_skill_type"));
		dto.setDecisionComment(rs.getString("decision_comment"));
		dto.setCreatedSkillId(rs.getObject("created_skill_id") != null ? rs.getInt("created_skill_id") : null);
		dto.setCreatedAt(rs.getTimestamp("created_at"));
		dto.setDecidedAt(rs.getTimestamp("decided_at"));
		if (reviewerEmpId != null) {
			long hodId = rs.getLong("hod_id");
			dto.setActionEnabled("pending".equalsIgnoreCase(dto.getStatus()) && reviewerEmpId.longValue() == hodId);
		}
		return dto;
	}

	private String normalizeCustomSkillType(String raw) {
		if (!StringUtils.hasText(raw)) {
			throw new IllegalArgumentException("Select whether the approved skill is Required or Optional.");
		}
		String value = raw.trim().toLowerCase(Locale.ROOT);
		if ("required".equals(value)) {
			return "Required";
		}
		if ("optional".equals(value)) {
			return "Optional";
		}
		throw new IllegalArgumentException("skillType must be Required or Optional.");
	}

	private void ensureCustomSkillRequestTable() {
		jdbcTemplate.execute(
				"CREATE TABLE IF NOT EXISTS skillmatrix_custom_skill_request ("
						+ "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
						+ "requested_by_emp_id BIGINT NOT NULL, "
						+ "requested_by_name VARCHAR(100) NOT NULL, "
						+ "designation VARCHAR(150), "
						+ "dept_id BIGINT NOT NULL, "
						+ "dept_name VARCHAR(150), "
						+ "hod_id BIGINT NOT NULL, "
						+ "hod_name VARCHAR(100), "
						+ "skill_name VARCHAR(150) NOT NULL, "
						+ "category_id INT NOT NULL, "
						+ "category_name VARCHAR(150), "
						+ "status VARCHAR(20) NOT NULL DEFAULT 'pending', "
						+ "approved_skill_type VARCHAR(20), "
						+ "decision_comment TEXT, "
						+ "created_skill_id INT, "
						+ "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
						+ "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
						+ "decided_at DATETIME NULL"
						+ ")");
	}

	private Long resolveDepartmentIdForEmp(Long empId) {
		Employee emp = employeeRepository.findByEmpId(empId);
		if (emp == null || emp.getJobRoleId() == null) {
			return null;
		}
		JobRole jr = jobRoleRepository.findByjobRoleId(emp.getJobRoleId());
		return jr != null ? jr.getDeptId() : null;
	}

	private static String normalizePoolSkillType(String skillType) {
		if (!StringUtils.hasText(skillType)) {
			throw new IllegalArgumentException("skillType is required (Required or Optional).");
		}
		String t = skillType.trim().toLowerCase(Locale.ROOT);
		if ("required".equals(t) || "optional".equals(t)) {
			return t;
		}
		throw new IllegalArgumentException("skillType must be Required or Optional.");
	}

	private List<SkillMatrixSubmitPickSkillDTO> mapSkillsToPickDtos(List<SkillsMaster> rows) {
		if (rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<Integer> skillIds = rows.stream().map(SkillsMaster::getSkillId).filter(Objects::nonNull)
				.collect(Collectors.toList());
		List<SubskillsMaster> allSubs = subskillsMasterRepository.findBySkillIdInOrderBySubskillNameAsc(skillIds);
		Map<Integer, List<SubskillsMaster>> subsBySkillId = allSubs.stream()
				.filter(su -> su.getIsActive() == null || Boolean.TRUE.equals(su.getIsActive()))
				.collect(Collectors.groupingBy(SubskillsMaster::getSkillId));
		List<SkillMatrixSubmitPickSkillDTO> out = new ArrayList<>();
		for (SkillsMaster sm : rows) {
			out.add(toPickDto(sm, subsBySkillId.getOrDefault(sm.getSkillId(), Collections.emptyList())));
		}
		return out;
	}

	private static SkillMatrixSubmitPickSkillDTO toPickDto(SkillsMaster sm, List<SubskillsMaster> subs) {
		SkillMatrixSubmitPickSkillDTO dto = new SkillMatrixSubmitPickSkillDTO();
		dto.setSkillId(sm.getSkillId());
		dto.setSkillName(sm.getSkillName());
		dto.setSkillType(sm.getSkillType());
		dto.setCategoryId(sm.getCategoryId());
		if (sm.getSkillCategory() != null) {
			dto.setCategoryName(sm.getSkillCategory().getCategoryName());
		}
		List<SkillMatrixSubmitSubskillDTO> subDtos = new ArrayList<>();
		for (SubskillsMaster su : subs) {
			subDtos.add(new SkillMatrixSubmitSubskillDTO(su.getSubskillId(), su.getSubskillName()));
		}
		dto.setSubskills(subDtos);
		return dto;
	}

	private static String formatEmploymentId(Employee e) {
		if (e.getEmployeementId() == null) {
			return null;
		}
		long id = e.getEmployeementId();
		if ("true".equalsIgnoreCase(e.getIsConsultant())) {
			return "CS-" + id;
		}
		if ("true".equalsIgnoreCase(e.getIsApmosysProduct())) {
			return "AP-" + id;
		}
		return "A-" + id;
	}

	private String resolveManagerName(Employee emp) {
		Long mgrId = emp.getManagerId() != null ? emp.getManagerId() : emp.getReportingManagerId();
		if (mgrId == null) {
			return null;
		}
		Employee mgr = employeeRepository.findByEmpId(mgrId);
		return mgr != null ? mgr.getName() : null;
	}

	private static String formatMobile(Long mobile) {
		if (mobile == null) {
			return null;
		}
		return String.valueOf(mobile);
	}

	/**
	 * Same total as My Profile → Total Experience: {@code totalExperience} (prior years) + tenure since DOJ,
	 * using the same 365.25-day year as {@code EmployeeService.calculateTotalExperience} in the Angular app.
	 */
	private static String formatTotalExperienceYears(Employee emp) {
		double previousExp = emp.getTotalExperience() != null ? emp.getTotalExperience().doubleValue() : 0.0;
		double apmosysExp = 0.0;
		if (emp.getDateOfJoining() != null) {
			long dojMillis = emp.getDateOfJoining().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			long diff = System.currentTimeMillis() - dojMillis;
			apmosysExp = diff / (1000.0 * 60.0 * 60.0 * 24.0 * 365.25);
		}
		double total = previousExp + apmosysExp;
		double rounded = Math.round(total * 10.0) / 10.0;
		return String.format(Locale.ENGLISH, "%.1f", rounded);
	}

	private String resolveDesignationName(Employee emp, JobRole jobRole) {
		if (emp.getDesignationId() != null) {
			Designation d = designationRepository.findByDesignationId(emp.getDesignationId());
			if (d != null && StringUtils.hasText(d.getDesignationName())) {
				return d.getDesignationName().trim();
			}
		}
		if (jobRole != null && StringUtils.hasText(jobRole.getName())) {
			return jobRole.getName().trim();
		}
		return null;
	}

	private static boolean isSkillMatrixMasterActive(Boolean isActive) {
		return isActive == null || Boolean.TRUE.equals(isActive);
	}

	/**
	 * Step 5 aspiration chips for the employee’s department; falls back to {@code dept_id = 0} pool when
	 * no department-specific rows exist.
	 */
	@Transactional(readOnly = true)
	public List<SkillMatrixAspirationChipDTO> listAspirationChipsForSubmit(Long empId) {
		Long deptId = resolveDepartmentIdForEmp(empId);
		if (deptId != null) {
			List<Map<String, Object>> deptRows = jdbcTemplate.queryForList(
					"SELECT chip_id, chip_label FROM skillmatrix_aspiration_chip_master WHERE is_active = 1 AND dept_id = ? ORDER BY sort_order ASC, chip_id ASC",
					deptId);
			if (!deptRows.isEmpty()) {
				return mapAspirationChipRows(deptRows);
			}
		}
		List<Map<String, Object>> globalRows = jdbcTemplate.queryForList(
				"SELECT chip_id, chip_label FROM skillmatrix_aspiration_chip_master WHERE is_active = 1 AND dept_id = 0 ORDER BY sort_order ASC, chip_id ASC");
		return mapAspirationChipRows(globalRows);
	}

	private static List<SkillMatrixAspirationChipDTO> mapAspirationChipRows(List<Map<String, Object>> rows) {
		Map<String, SkillMatrixAspirationChipDTO> byLabel = new LinkedHashMap<>();
		for (Map<String, Object> r : rows) {
			String label = r.get("chip_label") != null ? String.valueOf(r.get("chip_label")).trim() : "";
			if (label.isEmpty() || byLabel.containsKey(label)) {
				continue;
			}
			Integer id = r.get("chip_id") != null ? ((Number) r.get("chip_id")).intValue() : null;
			byLabel.put(label, new SkillMatrixAspirationChipDTO(id, label));
		}
		return new ArrayList<>(byLabel.values());
	}

	/** Read-only domain list for Submit for review (employees without master-configuration role). */
	@Transactional(readOnly = true)
	public List<SkillMatrixDomainListDTO> listSkillDomainsForSubmit() {
		return skillDomainMasterRepository.findAllByOrderByDomainNameAsc().stream()
				.map(d -> new SkillMatrixDomainListDTO(d.getDomainId(), d.getDomainName()))
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SkillMatrixSubdomainListDTO> listSkillSubdomainsForSubmit(Integer domainId) {
		if (domainId == null) {
			return Collections.emptyList();
		}
		return skillSubdomainMasterRepository.findByDomainIdOrderBySubdomainNameAsc(domainId).stream()
				.filter(su -> isSkillMatrixMasterActive(su.getIsActive()))
				.map(su -> new SkillMatrixSubdomainListDTO(su.getSubdomainId(), su.getDomainId(), null,
						su.getSubdomainName(), su.getIsActive()))
				.collect(Collectors.toList());
	}

	/**
	 * Features for the submit UI: either domain-only ({@code subdomainId} null) or tied to a sub domain.
	 */
	@Transactional(readOnly = true)
	public List<SkillMatrixDomainFeatureListDTO> listSkillDomainFeaturesForSubmit(Integer domainId,
			Integer subdomainId) {
		if (domainId == null) {
			return Collections.emptyList();
		}
		List<SkillDomainFeatureMaster> rows = subdomainId != null
				? skillDomainFeatureMasterRepository.findByDomainIdAndSubdomainIdOrderByFeatureNameAsc(domainId,
						subdomainId)
				: skillDomainFeatureMasterRepository.findByDomainIdAndSubdomainIdIsNullOrderByFeatureNameAsc(domainId);
		return rows.stream()
				.filter(f -> isSkillMatrixMasterActive(f.getIsActive()))
				.map(f -> new SkillMatrixDomainFeatureListDTO(f.getFeatureId(), f.getDomainId(), null, f.getSubdomainId(),
						null, f.getFeatureName(), f.getIsActive()))
				.collect(Collectors.toList());
	}
}
