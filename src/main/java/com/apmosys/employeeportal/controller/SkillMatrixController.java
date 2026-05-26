package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.SkillCategorySaveRequest;
import com.apmosys.employeeportal.dto.SkillDomainFeatureSaveRequest;
import com.apmosys.employeeportal.dto.SkillDomainSaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipDTO;
import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipMasterListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipSaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveDetailDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApproveBulkDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSkillDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSubmitRequest;
import com.apmosys.employeeportal.dto.SkillMatrixApproveSkillViewRowDTO;
import com.apmosys.employeeportal.dto.SkillMatrixHodDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixCategoryListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixCustomSkillDecisionRequest;
import com.apmosys.employeeportal.dto.SkillMatrixDomainFeatureListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixDomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSkillListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubdomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixBulkUploadResultDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubskillListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixProposeSkillRequest;
import com.apmosys.employeeportal.dto.SkillMatrixCertificateUploadResponseDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitContextDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitDraftRequest;
import com.apmosys.employeeportal.dto.SkillMatrixSubmitLockStatusDTO;
import com.apmosys.employeeportal.dto.SkillMatrixApprovedBaselineDTO;
import com.apmosys.employeeportal.dto.SkillMatrixUpdateProjectsRequest;
import com.apmosys.employeeportal.dto.SkillMasterSaveRequest;
import com.apmosys.employeeportal.dto.SkillSubdomainSaveRequest;
import com.apmosys.employeeportal.dto.SubskillMasterSaveRequest;
import com.apmosys.employeeportal.service.SkillMatrixCertificateStorageService;
import com.apmosys.employeeportal.service.SkillMatrixAuthorizationService;
import com.apmosys.employeeportal.service.SkillMatrixMasterBulkUploadService;
import com.apmosys.employeeportal.service.SkillMatrixMasterDataService;
import com.apmosys.employeeportal.service.SkillMatrixSubmitService;
import com.apmosys.employeeportal.skillmatrix.SkillMatrixSubFeatureNames;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api/skill-matrix")
public class SkillMatrixController {

	@Autowired
	private SkillMatrixAuthorizationService skillMatrixAuthorizationService;

	@Autowired
	private SkillMatrixMasterDataService skillMatrixMasterDataService;

	@Autowired
	private SkillMatrixMasterBulkUploadService skillMatrixMasterBulkUploadService;

	@Autowired
	private SkillMatrixSubmitService skillMatrixSubmitService;

	@Autowired
	private SkillMatrixCertificateStorageService skillMatrixCertificateStorageService;

	private int totalElementsToInt(long total) {
		return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
	}

	private Long currentEmpId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}
		Object principal = auth.getPrincipal();
		String raw = null;
		if (principal instanceof String) {
			raw = (String) principal;
		} else if (principal instanceof UserDetails) {
			raw = ((UserDetails) principal).getUsername();
		} else {
			// Fallback: some apps store empId in Authentication#getName()
			raw = auth.getName();
			if (raw == null || raw.trim().isEmpty()) {
				raw = principal.toString();
			}
		}
		try {
			return raw != null ? Long.valueOf(raw.trim()) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private ServiceResponse forbidden() {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("Access denied for this Skill Matrix action");
		return response;
	}

	private ServiceResponse fail(String message) {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(message);
		return response;
	}

	private ServiceResponse success(Object body) {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(body);
		return response;
	}

	@RequestMapping(value = "/submit-for-review/ping", method = RequestMethod.GET)
	public ServiceResponse pingSubmitForReview() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		return success("ok");
	}

	/** Debug helper: returns current employee id resolved from session. */
	@RequestMapping(value = "/submit-for-review/whoami", method = RequestMethod.GET)
	public ServiceResponse whoAmI() {
		Long empId = currentEmpId();
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(empId);
	}

	/**
	 * Logged-in employee snapshot (HRMS) for Submit for review step 1. Skill rows for step 2 use
	 * {@code /submit-for-review/skill-pool} (Required / Optional).
	 */
	@RequestMapping(value = "/submit-for-review/context", method = RequestMethod.GET)
	public ServiceResponse getSubmitForReviewContext() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			SkillMatrixSubmitContextDTO body = skillMatrixSubmitService.buildSubmitContext(empId);
			return success(body);
		} catch (IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	/** Returns whether employee is locked due to Under Review. */
	@RequestMapping(value = "/submit-for-review/lock-status", method = RequestMethod.GET)
	public ServiceResponse getSubmitLockStatus() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			SkillMatrixSubmitLockStatusDTO body = skillMatrixSubmitService.getSubmitLockStatus(empId);
			return success(body);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load submission status.");
		}
	}

	/** Baseline read-only snapshot for Add Skills mode (approved submission). */
	@RequestMapping(value = "/submit-for-review/approved-baseline/{submissionId}", method = RequestMethod.GET)
	public ServiceResponse getApprovedBaseline(@PathVariable("submissionId") String submissionId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			SkillMatrixApprovedBaselineDTO body = skillMatrixSubmitService.loadApprovedBaseline(empId, submissionId);
			return success(body);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load approved baseline.");
		}
	}

	/** Add Skills: update existing projects (no insert). */
	@RequestMapping(value = "/submit-for-review/projects/update", method = RequestMethod.POST)
	public ServiceResponse updateExistingProjects(@RequestBody SkillMatrixUpdateProjectsRequest req) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		try {
			skillMatrixSubmitService.updateExistingProjectsOnly(empId, req);
			return success("OK");
		} catch (IllegalArgumentException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not update projects.");
		}
	}

	/** Active {@code skills_master} rows for the employee's department and skill type (Required / Optional). */
	@RequestMapping(value = "/submit-for-review/skill-pool", method = RequestMethod.GET)
	public ServiceResponse listSubmitSkillPool(@RequestParam("skillType") String skillType,
			@RequestParam(value = "q", required = false) String q) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.listSkillPickPool(empId, skillType, q));
		} catch (IllegalArgumentException ex) {
			return fail(ex.getMessage());
		}
	}

	/** Skill categories for “Skill not in the list?” (searchable). */
	@RequestMapping(value = "/submit-for-review/skill-categories", method = RequestMethod.GET)
	public ServiceResponse listSubmitSkillCategories(@RequestParam(value = "q", required = false) String q) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(skillMatrixSubmitService.listSkillCategoriesForSubmit(q));
	}

	/** Project assignment history from HRMS mapping tables (employee_team_mapping → teams → projects). */
	@RequestMapping(value = "/submit-for-review/projects", method = RequestMethod.GET)
	public ServiceResponse listSubmitProjectHistory() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(skillMatrixSubmitService.listProjectHistoryForSubmit(empId));
	}

	/**
	 * Step 3: Upload certificate file (PDF/JPG/PNG, max 5MB). Returns a reference key that the
	 * client must persist in draft payload under skill.certifications[].fileReferenceKey.
	 */
	@RequestMapping(value = "/submit-for-review/certificates/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse uploadSubmitCertificate(@RequestParam("file") MultipartFile file) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			SkillMatrixCertificateUploadResponseDTO res = skillMatrixCertificateStorageService.store(empId, file);
			return success(res);
		} catch (IllegalArgumentException ex) {
			return fail(ex.getMessage());
		} catch (IOException ex) {
			return fail("Could not upload certificate file.");
		}
	}

	/** Adds a new optional active skill under the employee's department in {@code skills_master}. */
	@RequestMapping(value = "/submit-for-review/propose-skill", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse proposeSubmitSkill(@RequestBody SkillMatrixProposeSkillRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.proposeSkill(empId, body));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/custom-skill-requests", method = RequestMethod.GET)
	public ServiceResponse listCustomSkillRequests(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		var result = skillMatrixSubmitService.listCustomSkillRequestsForHod(empId, page, size);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/approve-requests/custom-skill-requests/{requestId}/decision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse decideCustomSkillRequest(@PathVariable("requestId") Long requestId,
			@RequestBody SkillMatrixCustomSkillDecisionRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.decideCustomSkillRequest(empId, requestId, body));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	/** Loads latest editable draft (draft / rejected / changes_requested) for current employee. */
	@RequestMapping(value = "/submit-for-review/draft", method = RequestMethod.GET)
	public ServiceResponse loadSubmitDraft() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.loadLatestEditableDraft(empId));
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load draft.");
		}
	}

	/** Loads a specific editable submission draft by submissionId (draft / rejected / changes_requested). */
	@RequestMapping(value = "/submit-for-review/draft/{submissionId}", method = RequestMethod.GET)
	public ServiceResponse loadSubmitDraftById(@PathVariable("submissionId") String submissionId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.loadEditableDraftBySubmissionId(empId, submissionId));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load draft.");
		}
	}

	/** Debug: returns empId and whether a draft exists. */
	@RequestMapping(value = "/submit-for-review/draft-debug", method = RequestMethod.GET)
	public ServiceResponse loadSubmitDraftDebug() {
		Long empId = currentEmpId();
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			Object draft = skillMatrixSubmitService.loadLatestEditableDraft(empId);
			return success(new java.util.HashMap<String, Object>() {
				{
					put("empId", empId);
					put("hasDraft", draft != null);
					put("draft", draft);
				}
			});
		} catch (Exception ex) {
			return fail("empId=" + empId + " err=" + (ex.getMessage() != null ? ex.getMessage() : "unknown"));
		}
	}

	/** Saves (upserts) a draft into skillmatrix_* tables. */
	@RequestMapping(value = "/submit-for-review/draft", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse saveSubmitDraft(@RequestBody SkillMatrixSubmitDraftRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.saveDraft(empId, body));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not save draft.");
		}
	}

	/** Final submit for manager approval (locks editing until rejected/changes requested). */
	@RequestMapping(value = "/submit-for-review/submit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse submitForApproval(@RequestBody Map<String, Object> body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		String submissionId = body != null && body.get("submissionId") != null ? String.valueOf(body.get("submissionId")) : null;
		try {
			skillMatrixSubmitService.submitForApproval(empId, submissionId);
			return success("ok");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not submit.");
		}
	}

	/** Read-only skill domains for Step 4 project mapping (no master-configuration role required). */
	/** Configurable Step 5 “skills to develop” chips; falls back to empty list (UI uses static dept config). */
	@RequestMapping(value = "/submit-for-review/aspiration-chips", method = RequestMethod.GET)
	public ServiceResponse listSubmitAspirationChips() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			List<SkillMatrixAspirationChipDTO> body = skillMatrixSubmitService.listAspirationChipsForSubmit(empId);
			return success(body);
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load aspiration chips.");
		}
	}

	@RequestMapping(value = "/submit-for-review/skill-domains", method = RequestMethod.GET)
	public ServiceResponse listSubmitSkillDomains() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(skillMatrixSubmitService.listSkillDomainsForSubmit());
	}

	@RequestMapping(value = "/submit-for-review/skill-subdomains", method = RequestMethod.GET)
	public ServiceResponse listSubmitSkillSubdomains(@RequestParam Integer domainId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(skillMatrixSubmitService.listSkillSubdomainsForSubmit(domainId));
	}

	/**
	 * Read-only features: pass {@code subdomainId} when the domain has sub domains and one is selected;
	 * omit {@code subdomainId} for domain-only features (domain has no sub domains, or direct features).
	 */
	@RequestMapping(value = "/submit-for-review/skill-domain-features", method = RequestMethod.GET)
	public ServiceResponse listSubmitSkillDomainFeatures(@RequestParam Integer domainId,
			@RequestParam(required = false) Integer subdomainId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.SUBMIT_FOR_REVIEW)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		return success(skillMatrixSubmitService.listSkillDomainFeaturesForSubmit(domainId, subdomainId));
	}

	@RequestMapping(value = "/my-submissions/ping", method = RequestMethod.GET)
	public ServiceResponse pingMySubmissions() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MY_SUBMISSIONS)) {
			return forbidden();
		}
		return success("ok");
	}

	@RequestMapping(value = "/my-submissions", method = RequestMethod.GET)
	public ServiceResponse listMySubmissions(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String submissionId,
			@RequestParam(required = false) String deptName,
			@RequestParam(required = false) String designation,
			@RequestParam(required = false) String reportingManagerName,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MY_SUBMISSIONS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		var result = skillMatrixSubmitService.listMySubmissions(empId, page, size, submissionId, deptName, designation,
				reportingManagerName, status, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/approve-requests/ping", method = RequestMethod.GET)
	public ServiceResponse pingApproveRequests() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		return success("ok");
	}

	@RequestMapping(value = "/approve-requests/queue", method = RequestMethod.GET)
	public ServiceResponse listApproveRequestsQueue(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String submissionId,
			@RequestParam(required = false) String employeeName,
			@RequestParam(required = false) String deptName,
			@RequestParam(required = false) String designation,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		var result = skillMatrixSubmitService.listApproveQueue(empId, page, size, q, submissionId, employeeName, deptName,
				designation, status, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}", method = RequestMethod.GET)
	public ServiceResponse getApproveSubmissionDetail(
			@PathVariable("submissionId") String submissionId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			SkillMatrixApproveDetailDTO dto = skillMatrixSubmitService.getApproveSubmissionDetail(empId, submissionId, page, size);
			return success(dto);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/decision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse saveApproveDecision(@PathVariable("submissionId") String submissionId,
			@RequestBody SkillMatrixApproveSkillDecisionRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			skillMatrixSubmitService.saveApproveDecision(empId, submissionId, body);
			return success("Saved");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/submit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse submitApproveReview(@PathVariable("submissionId") String submissionId,
			@RequestBody SkillMatrixApproveSubmitRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			skillMatrixSubmitService.submitApproveReview(empId, submissionId, body);
			return success("Submitted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/skills-meta", method = RequestMethod.GET)
	public ServiceResponse listApproveSubmissionSkillsMeta(@PathVariable("submissionId") String submissionId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.listApproveSubmissionSkillsMeta(empId, submissionId));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/skill/{skillRatingId}", method = RequestMethod.GET)
	public ServiceResponse getApproveSubmissionSkillDetail(
			@PathVariable("submissionId") String submissionId,
			@PathVariable("skillRatingId") long skillRatingId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.getApproveSubmissionSkillDetail(empId, submissionId, skillRatingId));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/bulk-approve-pending", method = RequestMethod.POST)
	public ServiceResponse bulkApprovePending(@PathVariable("submissionId") String submissionId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			int updated = skillMatrixSubmitService.bulkApproveAllPending(empId, submissionId);
			return success(updated);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/approve-requests/submission/{submissionId}/bulk-reject-pending", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse bulkRejectPending(
			@PathVariable("submissionId") String submissionId,
			@RequestBody SkillMatrixApproveBulkDecisionRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			int updated = skillMatrixSubmitService.bulkRejectAllPending(empId, submissionId, body);
			return success(updated);
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	/* ---------------- HOD Approval (Level 2) ---------------- */

	@RequestMapping(value = "/hod-requests/ping", method = RequestMethod.GET)
	public ServiceResponse pingHodRequests() {
		Long empId = currentEmpId();
		// Reuse approve-requests feature for HOD inbox.
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		return success("ok");
	}

	@RequestMapping(value = "/hod-requests/queue", method = RequestMethod.GET)
	public ServiceResponse listHodRequestsQueue(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String submissionId,
			@RequestParam(required = false) String employeeName,
			@RequestParam(required = false) String deptName,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		var result = skillMatrixSubmitService.listHodQueue(empId, page, size, submissionId, employeeName, deptName, status,
				sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/hod-requests/submission/{submissionId}", method = RequestMethod.GET)
	public ServiceResponse getHodSubmissionDetail(@PathVariable("submissionId") String submissionId) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			return success(skillMatrixSubmitService.getHodSubmissionDetail(empId, submissionId));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not load details.");
		}
	}

	@RequestMapping(value = "/hod-requests/submission/{submissionId}/decision", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse saveHodDecision(@PathVariable("submissionId") String submissionId,
			@RequestBody SkillMatrixHodDecisionRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		try {
			skillMatrixSubmitService.saveHodDecision(empId, submissionId, body);
			return success("ok");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		} catch (Exception ex) {
			return fail(ex.getMessage() != null ? ex.getMessage() : "Could not save decision.");
		}
	}

	@RequestMapping(value = "/approve-requests/skill-view", method = RequestMethod.GET)
	public ServiceResponse listApproveSkillView(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String skillName,
			@RequestParam(required = false) String deptName,
			@RequestParam(required = false) String decision,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.APPROVE_REQUESTS)) {
			return forbidden();
		}
		if (empId == null) {
			return fail("Not authenticated.");
		}
		Page<SkillMatrixApproveSkillViewRowDTO> result = skillMatrixSubmitService.listApproveSkillView(empId, page, size,
				q, skillName, deptName, decision, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/ping", method = RequestMethod.GET)
	public ServiceResponse pingMasterConfiguration() {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		return success("ok");
	}

	@RequestMapping(value = "/master-configuration/skill-categories", method = RequestMethod.GET)
	public ServiceResponse listSkillCategories(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String categoryId,
			@RequestParam(required = false) String categoryName,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixCategoryListDTO> result = skillMatrixMasterDataService.listSkillCategories(page, size,
				categoryId, categoryName, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/skill-categories", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSkillCategory(@RequestBody SkillCategorySaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSkillCategory(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-categories/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSkillCategory(@PathVariable("id") Integer id, @RequestBody SkillCategorySaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSkillCategory(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-categories/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSkillCategory(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSkillCategory(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skills", method = RequestMethod.GET)
	public ServiceResponse listSkills(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String skillId,
			@RequestParam(required = false) String skillName,
			@RequestParam(required = false) String categoryName,
			@RequestParam(required = false) String skillType,
			@RequestParam(required = false) String activeDisplay,
			@RequestParam(required = false) String departmentName,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixSkillListDTO> result = skillMatrixMasterDataService.listSkills(page, size, skillId, skillName,
				categoryName, skillType, activeDisplay, departmentName, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/skills", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSkill(@RequestBody SkillMasterSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSkill(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skills/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSkill(@PathVariable("id") Integer id, @RequestBody SkillMasterSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSkill(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skills/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSkill(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSkill(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/subskills", method = RequestMethod.GET)
	public ServiceResponse listSubskills(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String subskillId,
			@RequestParam(required = false) String subskillName,
			@RequestParam(required = false) String skillName,
			@RequestParam(required = false) String activeDisplay,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixSubskillListDTO> result = skillMatrixMasterDataService.listSubskills(page, size, subskillId,
				subskillName, skillName, activeDisplay, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/subskills", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSubskill(@RequestBody SubskillMasterSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSubskill(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/subskills/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSubskill(@PathVariable("id") Integer id, @RequestBody SubskillMasterSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSubskill(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/subskills/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSubskill(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSubskill(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domains", method = RequestMethod.GET)
	public ServiceResponse listSkillDomains(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String domainId,
			@RequestParam(required = false) String domainName,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixDomainListDTO> result = skillMatrixMasterDataService.listSkillDomains(page, size, domainId,
				domainName, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/skill-domains", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSkillDomain(@RequestBody SkillDomainSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSkillDomain(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domains/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSkillDomain(@PathVariable("id") Integer id, @RequestBody SkillDomainSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSkillDomain(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domains/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSkillDomain(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSkillDomain(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-subdomains", method = RequestMethod.GET)
	public ServiceResponse listSkillSubdomains(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String subdomainId,
			@RequestParam(required = false) String subdomainName,
			@RequestParam(required = false) String domainName,
			@RequestParam(required = false) String activeDisplay,
			@RequestParam(required = false) Integer domainId,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixSubdomainListDTO> result = skillMatrixMasterDataService.listSkillSubdomains(page, size,
				subdomainId, subdomainName, domainName, activeDisplay, domainId, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/skill-subdomains", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSkillSubdomain(@RequestBody SkillSubdomainSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSkillSubdomain(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-subdomains/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSkillSubdomain(@PathVariable("id") Integer id,
			@RequestBody SkillSubdomainSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSkillSubdomain(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-subdomains/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSkillSubdomain(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSkillSubdomain(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domain-features", method = RequestMethod.GET)
	public ServiceResponse listSkillDomainFeatures(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String featureId,
			@RequestParam(required = false) String featureName,
			@RequestParam(required = false) String domainName,
			@RequestParam(required = false) String subdomainName,
			@RequestParam(required = false) String activeDisplay,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixDomainFeatureListDTO> result = skillMatrixMasterDataService.listSkillDomainFeatures(page, size,
				featureId, featureName, domainName, subdomainName, activeDisplay, sortColumn, sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/skill-domain-features", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createSkillDomainFeature(@RequestBody SkillDomainFeatureSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createSkillDomainFeature(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domain-features/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateSkillDomainFeature(@PathVariable("id") Integer id,
			@RequestBody SkillDomainFeatureSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateSkillDomainFeature(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/skill-domain-features/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteSkillDomainFeature(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteSkillDomainFeature(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	/** Step 5 “skills to develop” chips for Submit for review (master data). */
	@RequestMapping(value = "/master-configuration/aspiration-chips", method = RequestMethod.GET)
	public ServiceResponse listAspirationChips(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@RequestParam(required = false) String chipId,
			@RequestParam(required = false) String deptId,
			@RequestParam(required = false) String chipLabel,
			@RequestParam(required = false) String departmentName,
			@RequestParam(required = false) String activeDisplay,
			@RequestParam(required = false) String sortColumn,
			@RequestParam(required = false) String sortDirection) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		Page<SkillMatrixAspirationChipMasterListDTO> result = skillMatrixMasterDataService
				.listAspirationChips(page, size, chipId, deptId, chipLabel, departmentName, activeDisplay, sortColumn,
						sortDirection);
		ServiceResponse response = success(result.getContent());
		response.setTotalElements(totalElementsToInt(result.getTotalElements()));
		return response;
	}

	@RequestMapping(value = "/master-configuration/aspiration-chips", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse createAspirationChip(@RequestBody SkillMatrixAspirationChipSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.createAspirationChip(body);
			return success("Created");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/aspiration-chips/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse updateAspirationChip(@PathVariable("id") Integer id,
			@RequestBody SkillMatrixAspirationChipSaveRequest body) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.updateAspirationChip(id, body);
			return success("Updated");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/aspiration-chips/{id}", method = RequestMethod.DELETE)
	public ServiceResponse deleteAspirationChip(@PathVariable("id") Integer id) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			skillMatrixMasterDataService.deleteAspirationChip(id);
			return success("Deleted");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return fail(ex.getMessage());
		}
	}

	@RequestMapping(value = "/master-configuration/bulk/template/{masterType}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadMasterBulkTemplate(@PathVariable("masterType") String masterType) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			byte[] body = skillMatrixMasterBulkUploadService.buildTemplate(masterType);
			String filename = skillMatrixMasterBulkUploadService.templateFilename(masterType);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
			return new ResponseEntity<>(body, headers, HttpStatus.OK);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (IOException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@RequestMapping(value = "/master-configuration/bulk/upload/{masterType}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse bulkUploadMaster(@PathVariable("masterType") String masterType,
			@RequestParam("file") MultipartFile file) {
		Long empId = currentEmpId();
		if (!skillMatrixAuthorizationService.hasSubFeature(empId, SkillMatrixSubFeatureNames.MASTER_CONFIGURATION)) {
			return forbidden();
		}
		try {
			SkillMatrixBulkUploadResultDTO result = skillMatrixMasterBulkUploadService.processUpload(masterType, file);
			return success(result);
		} catch (IllegalArgumentException ex) {
			return fail(ex.getMessage());
		} catch (IOException | InvalidFormatException ex) {
			return fail("Could not read the uploaded file.");
		}
	}
}
