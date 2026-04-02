package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.apmosys.employeeportal.dto.GrievanceEmployee360CountDTO;
import com.apmosys.employeeportal.dto.GrievanceAuditDiffResponseDTO;
import com.apmosys.employeeportal.dto.GrievanceProofDocumentDTO;
import com.apmosys.employeeportal.dto.GrievanceProofDownloadDTO;
import com.apmosys.employeeportal.dto.GrievanceIssueScenarioDTO;
import com.apmosys.employeeportal.dto.GrievanceIssueScenarioPageDTO;
import com.apmosys.employeeportal.dto.GrievanceTicketListPageDTO;
import com.apmosys.employeeportal.dto.GrievanceTicketRequestDTO;
import com.apmosys.employeeportal.dto.GrievanceTicketUpdateDTO;
import com.apmosys.employeeportal.dto.GrievanceFeedbackDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.FeatureMaster;
import com.apmosys.employeeportal.model.GrievanceDocument;
import com.apmosys.employeeportal.model.GrievanceIssueScenario;
import com.apmosys.employeeportal.model.GrievanceTicket;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.FeatureMasterRepository;
import com.apmosys.employeeportal.repository.GrievanceDocumentRepository;
import com.apmosys.employeeportal.repository.GrievanceIssueScenarioRepository;
import com.apmosys.employeeportal.repository.GrievanceTicketRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.repository.TabMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.model.TabMaster;

@Service
public class GrievanceService {
	private static final Logger log = LoggerFactory.getLogger(GrievanceService.class);
	private static final String TICKET_NUMBER_PREFIX = "APMOSYS_";
	private static final String GRIEVANCE_FEATURE_NAME = "Grievance";
	/** Sub-feature in {@code sub_feature_master}; access via {@code role_subfeature_mapping} only (see grievance-rbac-seed.sql). */
	private static final String GRIEVANCE_ISSUE_SCENARIOS_SUBFEATURE = "Grievance Issue Scenarios";
	private static final long MAX_PROOF_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
	private static final int MAX_PROOF_FILES_COUNT = 5;
	/** Auto-close RESOLVED tickets this many days after resolution_date. */
	private static final long GRIEVANCE_AUTO_CLOSE_AFTER_RESOLVED_DAYS = 3L;

	@Autowired
	private GrievanceTicketRepository grievanceTicketRepository;

	@Autowired
	private GrievanceDocumentRepository grievanceDocumentRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private RoleFeatureMapRepository roleFeatureMapRepository;

	@Autowired
	private SubFeatureMasterRepository subFeatureMasterRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private TabMasterRepository tabMasterRepository;

	@Autowired
	private FeatureMasterRepository featureMasterRepository;

	@Autowired
	private GrievanceIssueScenarioRepository grievanceIssueScenarioRepository;

	@Autowired
	private EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private GrievanceAuditService grievanceAuditService;

	@Autowired
	private MailService mailService;

	@Value("${file.location.documents.grievance}")
	private String grievanceDocumentLocation;

	@Value("${grievance.default.assignee.email:pratikshya.routray@apmosys.com}")
	private String grievanceDefaultAssigneeEmail;

	@Value("${app.mail.default-cc:}")
	private String grievanceDefaultCc;

	private enum GrievanceEmailEventType {
		CREATE,
		ASSIGNMENT_CHANGE,
		STATUS_CHANGE,
		RESOLVED_CLOSED
	}

	@Transactional
	public ServiceResponse createTicket(Long empId, GrievanceTicketRequestDTO requestDTO, List<MultipartFile> proofFiles) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (requestDTO == null || isBlank(requestDTO.getSubject()) || isBlank(requestDTO.getDescription())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Subject and description are required.");
				return response;
			}
			if (isBlank(requestDTO.getCategory())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Category is required.");
				return response;
			}
			if (isBlank(requestDTO.getSubCategory())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Sub-category is required.");
				return response;
			}
			if (isBlank(requestDTO.getTicketFeature())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Feature is required.");
				return response;
			}
			String tabKey = resolveIssueScenarioTabKey(requestDTO.getCategory());
			List<String> scenarioChoices = List.of();
			if (!isBlank(tabKey)) {
				scenarioChoices = grievanceIssueScenarioRepository.findScenarioLabels(tabKey,
						safeParam(requestDTO.getTicketFeature()),
						safeParam(requestDTO.getSubCategory()));
				scenarioChoices = scenarioChoices.stream().distinct().collect(Collectors.toList());
			}
			String issueScenarioToStore = null;
			if (!scenarioChoices.isEmpty() && !isBlank(requestDTO.getIssueScenario())) {
				String chosen = requestDTO.getIssueScenario().trim();
				boolean allowed = scenarioChoices.stream().anyMatch(s -> s != null && chosen.equals(s.trim()));
				if (!allowed) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid issue scenario selection.");
					return response;
				}
				issueScenarioToStore = chosen;
			}
			List<MultipartFile> proofList = proofFiles == null ? new ArrayList<>()
					: proofFiles.stream().filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
			String proofValidationError = validateProofFileList(proofList);
			if (proofValidationError != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(proofValidationError);
				return response;
			}

			Optional<Employee> employeeOpt = employeeRepository.findById(empId);
			if (employeeOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				return response;
			}

			Employee employee = employeeOpt.get();
			Employee defaultAssignee = resolveDefaultAssignee();
			if (defaultAssignee == null || defaultAssignee.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No active Development or HR assignee found. Please contact admin.");
				return response;
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());

			GrievanceTicket ticket = new GrievanceTicket();
			ticket.setTicketNumber(generateUniqueTicketNumber());
			ticket.setCreatedByEmpId(empId);
			ticket.setCreatedByName(employee.getName());
			ticket.setCreatedByEmployeeId(employee.getEmployeementId());
			ticket.setCreatedByDepartment(resolveDepartmentName(employee));
			applyCreatorSnapshotFields(ticket, employee);
			ticket.setSubject(requestDTO.getSubject().trim());
			ticket.setCategory(isBlank(requestDTO.getCategory()) ? null : requestDTO.getCategory().trim());
			ticket.setSubCategory(isBlank(requestDTO.getSubCategory()) ? null : requestDTO.getSubCategory().trim());
			ticket.setTicketFeature(isBlank(requestDTO.getTicketFeature()) ? null : requestDTO.getTicketFeature().trim());
			ticket.setIssueScenario(issueScenarioToStore);
			ticket.setDescription(requestDTO.getDescription().trim());
			ticket.setPriority(isBlank(requestDTO.getPriority()) ? "MEDIUM" : requestDTO.getPriority().trim().toUpperCase(Locale.ROOT));
			ticket.setStatus("OPEN");
			ticket.setAssignedToEmpId(defaultAssignee.getEmpId());
			ticket.setAssignedToName(defaultAssignee.getName());
			ticket.setCreatedOn(now);
			ticket.setUpdatedOn(now);
			ticket.setUpdatedBy(empId);
			ticket.setIsActive(1);

			ticket = grievanceTicketRepository.save(ticket);

			List<GrievanceDocument> persisted = persistProofDocuments(ticket.getTicketId(), empId, proofList);
			if (!persisted.isEmpty()) {
				GrievanceDocument first = persisted.get(0);
				ticket.setProofFileName(first.getOriginalFileName());
				ticket.setProofFilePath(first.getStoredFilePath());
				ticket = grievanceTicketRepository.save(ticket);
			}

			grievanceAuditService.recordTicketCreated(ticket, empId, employee.getName(), proofList.size());
			triggerGrievanceEmail(GrievanceEmailEventType.CREATE, ticket, null, null);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(ticket);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/** @return error message or null if valid */
	private String validateProofFileList(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return "Proof document is mandatory.";
		}
		if (files.size() > MAX_PROOF_FILES_COUNT) {
			return "You can attach at most " + MAX_PROOF_FILES_COUNT + " proof files.";
		}
		for (MultipartFile proofFile : files) {
			if (proofFile.getSize() > MAX_PROOF_FILE_SIZE_BYTES) {
				return "Each proof attachment must be 5MB or smaller.";
			}
			String originalName = proofFile.getOriginalFilename() == null ? "proof" : proofFile.getOriginalFilename();
			String extension = extensionOf(originalName);
			if (!isAllowedProofExtension(extension)) {
				return "Unsupported proof file type. Allowed: pdf, png, jpg, jpeg, doc, docx.";
			}
		}
		return null;
	}

	private static String extensionOf(String originalName) {
		int dotIndex = originalName.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
			return originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
		}
		return "";
	}

	private List<GrievanceDocument> persistProofDocuments(Long ticketId, Long empId, List<MultipartFile> proofFiles) {
		try {
			Path grievanceDir = Paths.get(grievanceDocumentLocation);
			if (!Files.exists(grievanceDir)) {
				Files.createDirectories(grievanceDir);
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());
			List<GrievanceDocument> saved = new ArrayList<>();
			int order = 0;
			for (MultipartFile proofFile : proofFiles) {
				String originalName = proofFile.getOriginalFilename() == null ? "proof" : proofFile.getOriginalFilename();
				String extension = extensionOf(originalName);
				if (!isAllowedProofExtension(extension)) {
					throw new IllegalArgumentException("Unsupported proof file type.");
				}
				String safeName = "grievance_" + ticketId + "_" + empId + "_" + UUID.randomUUID() + extension;
				Path destination = grievanceDir.resolve(safeName);
				Files.copy(proofFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

				GrievanceDocument doc = new GrievanceDocument();
				doc.setTicketId(ticketId);
				doc.setOriginalFileName(originalName);
				doc.setStoredFilePath(destination.toString());
				doc.setFileSizeBytes(proofFile.getSize());
				doc.setSortOrder(order++);
				doc.setCreatedOn(now);
				doc.setIsActive(1);
				saved.add(grievanceDocumentRepository.save(doc));
			}
			return saved;
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to save proof document: " + e.getMessage());
		}
	}

	private boolean isStoredProofPathAllowed(String storedPath) {
		if (isBlank(storedPath)) {
			return false;
		}
		try {
			Path base = Paths.get(grievanceDocumentLocation).toAbsolutePath().normalize();
			Path candidate = Paths.get(storedPath).toAbsolutePath().normalize();
			return candidate.startsWith(base) && Files.exists(candidate);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isAllowedProofExtension(String extension) {
		return ".pdf".equals(extension)
				|| ".png".equals(extension)
				|| ".jpg".equals(extension)
				|| ".jpeg".equals(extension)
				|| ".doc".equals(extension)
				|| ".docx".equals(extension);
	}

	private Employee resolveDefaultAssignee() {
		Employee configuredAssignee = null;
		if (!isBlank(grievanceDefaultAssigneeEmail)) {
			configuredAssignee = employeeRepository.findByEmail(grievanceDefaultAssigneeEmail.trim());
		}
		if (configuredAssignee != null && configuredAssignee.getEmpId() != null) {
			return configuredAssignee;
		}

		List<EmployeeDTO> assigneePool = employeeRepository.getActiveEmployeesForGrievanceAssigneeList("Development");
		if (assigneePool == null || assigneePool.isEmpty()) {
			return null;
		}

		Long fallbackEmpId = assigneePool.get(0).getEmpId();
		if (fallbackEmpId == null) {
			return null;
		}
		return employeeRepository.findById(fallbackEmpId).orElse(null);
	}

	public ServiceResponse getMyTickets(Long empId, int pageOneBased, int size, String sortBy, String sortDir) {
		ServiceResponse response = new ServiceResponse();
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceTicket> pageResult = grievanceTicketRepository.findByCreatedByEmpIdAndIsActive(empId, 1, pageable);
		ensureTicketNumbers(pageResult.getContent());
		GrievanceTicketListPageDTO dto = toPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	public ServiceResponse getAssignedToMeTickets(Long empId, int pageOneBased, int size, String sortBy, String sortDir) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewAssignedTicketsQueue(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. Assigned tickets are available only for HR and Development.");
			return response;
		}
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceTicket> pageResult = grievanceTicketRepository.findByAssignedToEmpIdAndIsActive(empId, 1, pageable);
		ensureTicketNumbers(pageResult.getContent());
		GrievanceTicketListPageDTO dto = toPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	/**
	 * Employee 360: tickets raised by {@code targetEmpId}. Viewer must be the same employee, have
	 * view-all grievance access, or HR/Development queue access.
	 */
	public ServiceResponse getEmployee360TicketsRaised(Long viewerEmpId, Long targetEmpId, int pageOneBased, int size,
			String sortBy, String sortDir, String fromDate, String toDate) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewEmployee360Grievance(viewerEmpId, targetEmpId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		Timestamp fromTs = parseStartOfDayOptional(fromDate);
		Timestamp toTs = parseEndOfDayOptional(toDate);
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceTicket> pageResult = grievanceTicketRepository.findRaisedForEmployee360(targetEmpId, 1, fromTs, toTs,
				pageable);
		ensureTicketNumbers(pageResult.getContent());
		GrievanceTicketListPageDTO dto = toPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	/**
	 * Employee 360: tickets assigned to {@code targetEmpId}.
	 */
	public ServiceResponse getEmployee360TicketsAssigned(Long viewerEmpId, Long targetEmpId, int pageOneBased, int size,
			String sortBy, String sortDir, String fromDate, String toDate) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewEmployee360Grievance(viewerEmpId, targetEmpId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		Timestamp fromTs = parseStartOfDayOptional(fromDate);
		Timestamp toTs = parseEndOfDayOptional(toDate);
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceTicket> pageResult = grievanceTicketRepository.findAssignedForEmployee360(targetEmpId, 1, fromTs, toTs,
				pageable);
		ensureTicketNumbers(pageResult.getContent());
		GrievanceTicketListPageDTO dto = toPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	/** Total counts for Employee 360 (no date filter); used for UI visibility. */
	public ServiceResponse getEmployee360TicketCounts(Long viewerEmpId, Long targetEmpId) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewEmployee360Grievance(viewerEmpId, targetEmpId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		GrievanceEmployee360CountDTO counts = new GrievanceEmployee360CountDTO();
		counts.setRaisedCount(grievanceTicketRepository.countByCreatedByEmpIdAndIsActive(targetEmpId, 1));
		counts.setAssignedCount(grievanceTicketRepository.countByAssignedToEmpIdAndIsActive(targetEmpId, 1));
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(counts);
		return response;
	}

	private boolean canViewEmployee360Grievance(Long viewerEmpId, Long targetEmpId) {
		if (viewerEmpId == null || targetEmpId == null) {
			return false;
		}
		if (viewerEmpId.equals(targetEmpId)) {
			return true;
		}
		if (canViewAllTickets(viewerEmpId)) {
			return true;
		}
		return canViewAssignedTicketsQueue(viewerEmpId);
	}

	private static Timestamp parseStartOfDayOptional(String yyyyMmDd) {
		if (yyyyMmDd == null || yyyyMmDd.isBlank()) {
			return null;
		}
		try {
			LocalDate d = LocalDate.parse(yyyyMmDd.trim());
			return Timestamp.valueOf(d.atStartOfDay());
		} catch (Exception e) {
			return null;
		}
	}

	private static Timestamp parseEndOfDayOptional(String yyyyMmDd) {
		if (yyyyMmDd == null || yyyyMmDd.isBlank()) {
			return null;
		}
		try {
			LocalDate d = LocalDate.parse(yyyyMmDd.trim());
			return Timestamp.valueOf(d.atTime(LocalTime.of(23, 59, 59, 999_000_000)));
		} catch (Exception e) {
			return null;
		}
	}

	public ServiceResponse exportAssignedToMeTickets(Long empId) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewAssignedTicketsQueue(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		List<GrievanceTicket> tickets = grievanceTicketRepository.findByAssignedToEmpIdAndIsActiveOrderByCreatedOnDesc(empId, 1);
		ensureTicketNumbers(tickets);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(tickets);
		return response;
	}

	public ServiceResponse getAllTickets(Long empId, int pageOneBased, int size, String sortBy, String sortDir) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewAllTickets(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to view all grievance tickets.");
			return response;
		}
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceTicket> pageResult = grievanceTicketRepository.findByIsActive(1, pageable);
		ensureTicketNumbers(pageResult.getContent());
		GrievanceTicketListPageDTO dto = toPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	public ServiceResponse exportMyTickets(Long empId) {
		ServiceResponse response = new ServiceResponse();
		List<GrievanceTicket> tickets = grievanceTicketRepository.findByCreatedByEmpIdAndIsActiveOrderByCreatedOnDesc(empId, 1);
		ensureTicketNumbers(tickets);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(tickets);
		return response;
	}

	public ServiceResponse exportAllTickets(Long empId) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewAllTickets(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to export all grievance tickets.");
			return response;
		}
		List<GrievanceTicket> tickets = grievanceTicketRepository.findByIsActiveOrderByCreatedOnDesc(1);
		ensureTicketNumbers(tickets);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(tickets);
		return response;
	}

	public ServiceResponse getTicketById(Long empId, Long ticketId) {
		ServiceResponse response = new ServiceResponse();
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}
		GrievanceTicket ticket = ticketOpt.get();
		boolean isPrivileged = canViewAllTickets(empId);
		boolean isAssignedUser = empId.equals(ticket.getAssignedToEmpId());
		boolean isCreator = empId.equals(ticket.getCreatedByEmpId());
		if (!isPrivileged && !isAssignedUser && !isCreator) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to view this ticket.");
			return response;
		}
		if (isBlank(ticket.getTicketNumber())) {
			ticket.setTicketNumber(generateUniqueTicketNumber());
			grievanceTicketRepository.save(ticket);
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(ticket);
		return response;
	}

	public ServiceResponse getDevelopmentUsers(Long empId) {
		ServiceResponse response = new ServiceResponse();
		if (!canViewAllTickets(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to view assignee list.");
			return response;
		}
		List<EmployeeDTO> users = employeeRepository.getActiveEmployeesForGrievanceAssigneeList("Development");
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(users);
		return response;
	}

	public ServiceResponse getTicketCategories() {
		ServiceResponse response = new ServiceResponse();
		List<String> categories = tabMasterRepository.findAll().stream()
				.map(TabMaster::getTabName)
				.filter(name -> name != null && !name.trim().isEmpty())
				.map(String::trim)
				.distinct()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(categories);
		return response;
	}

	/**
	 * Sub-categories for grievance: all {@code sub_feature_master} names linked to features under the tab
	 * whose name matches {@code categoryTabName} (same as category dropdown = tab name).
	 */
	public ServiceResponse getTicketSubCategoriesForTab(String categoryTabName) {
		ServiceResponse response = new ServiceResponse();
		if (isBlank(categoryTabName)) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(List.of());
			return response;
		}
		String needle = categoryTabName.trim().toLowerCase(Locale.ROOT);
		Optional<TabMaster> tabOpt = tabMasterRepository.findAll().stream()
				.filter(t -> t.getTabName() != null && t.getTabName().trim().toLowerCase(Locale.ROOT).equals(needle))
				.findFirst();
		if (tabOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(List.of());
			return response;
		}
		List<FeatureMaster> features = featureMasterRepository.findByTabId(tabOpt.get().getTabId());
		TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (FeatureMaster fm : features) {
			if (fm.getFeatureId() == null) {
				continue;
			}
			for (SubFeatureMaster s : subFeatureMasterRepository.findByFeatureId(fm.getFeatureId())) {
				if (s.getSubFeatureName() != null && !s.getSubFeatureName().isBlank()) {
					names.add(s.getSubFeatureName().trim());
				}
			}
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(new ArrayList<>(names));
		return response;
	}

	/**
	 * Feature names ({@code feature_master.feature_name}) for features under the tab that include the given sub-feature name.
	 */
	public ServiceResponse getTicketFeaturesForTabAndSubCategory(String categoryTabName, String subCategoryName) {
		ServiceResponse response = new ServiceResponse();
		if (isBlank(categoryTabName)) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(List.of());
			return response;
		}
		String tabNeedle = categoryTabName.trim().toLowerCase(Locale.ROOT);
		Optional<TabMaster> tabOpt = tabMasterRepository.findAll().stream()
				.filter(t -> t.getTabName() != null && t.getTabName().trim().toLowerCase(Locale.ROOT).equals(tabNeedle))
				.findFirst();
		if (tabOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(List.of());
			return response;
		}
		List<FeatureMaster> features = featureMasterRepository.findByTabId(tabOpt.get().getTabId());
		TreeSet<String> featureNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		if (isBlank(subCategoryName)) {
			for (FeatureMaster fm : features) {
				if (fm.getFeatureName() != null && !fm.getFeatureName().isBlank()) {
					featureNames.add(fm.getFeatureName().trim());
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(featureNames));
			return response;
		}
		String subNeedle = subCategoryName.trim().toLowerCase(Locale.ROOT);
		for (FeatureMaster fm : features) {
			if (fm.getFeatureId() == null || isBlank(fm.getFeatureName())) {
				continue;
			}
			for (SubFeatureMaster s : subFeatureMasterRepository.findByFeatureId(fm.getFeatureId())) {
				if (s.getSubFeatureName() != null
						&& s.getSubFeatureName().trim().toLowerCase(Locale.ROOT).equals(subNeedle)) {
					featureNames.add(fm.getFeatureName().trim());
					break;
				}
			}
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(new ArrayList<>(featureNames));
		return response;
	}

	/**
	 * Curated issue scenarios for Timesheet / Leave (tab name contains "timesheet" or "leave").
	 */
	public ServiceResponse getIssueScenariosForSelection(String categoryTabName, String subCategoryName,
			String ticketFeatureName) {
		ServiceResponse response = new ServiceResponse();
		String tabKey = resolveIssueScenarioTabKey(categoryTabName);
		if (isBlank(tabKey)) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(List.of());
			return response;
		}
		List<String> labels = grievanceIssueScenarioRepository.findScenarioLabels(tabKey,
				safeParam(ticketFeatureName),
				safeParam(subCategoryName));
		List<String> distinct = new ArrayList<>(labels.stream().distinct().collect(Collectors.toList()));
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(distinct);
		return response;
	}

	/**
	 * Admin UI + APIs: job roles mapped to Grievance sub-feature {@value #GRIEVANCE_ISSUE_SCENARIOS_SUBFEATURE} in
	 * {@code role_subfeature_mapping} (see grievance-rbac-seed.sql). Sub-feature name: {@code Grievance Issue Scenarios}.
	 */
	public boolean canManageIssueScenarios(Long empId) {
		if (empId == null) {
			return false;
		}
		Optional<Employee> empOpt = employeeRepository.findById(empId);
		return empOpt.filter(this::canManageIssueScenarios).isPresent();
	}

	public ServiceResponse listAdminIssueScenarios(Long empId, int pageOneBased, int size, String sortBy, String sortDir) {
		ServiceResponse response = new ServiceResponse();
		if (!canManageIssueScenarios(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to manage grievance issue scenarios.");
			return response;
		}
		int pageIndex = Math.max(0, pageOneBased - 1);
		int pageSize = clampPageSize(size);
		Pageable pageable = buildIssueScenarioPageable(pageIndex, pageSize, sortBy, sortDir);
		Page<GrievanceIssueScenario> pageResult = grievanceIssueScenarioRepository.findAll(pageable);
		for (GrievanceIssueScenario s : pageResult.getContent()) {
			s.setCategoryDisplayName(displayNameForScenarioTabKey(s.getTabKey()));
		}
		GrievanceIssueScenarioPageDTO dto = toIssueScenarioPageDto(pageResult, pageOneBased, sortBy, sortDir);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}

	public ServiceResponse createAdminIssueScenario(Long empId, GrievanceIssueScenarioDTO dto) {
		ServiceResponse response = new ServiceResponse();
		if (!canManageIssueScenarios(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to manage grievance issue scenarios.");
			return response;
		}
		if (dto == null || isBlank(dto.getScenarioLabel())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Scenario label is required.");
			return response;
		}
		String resolvedTabKey = resolveTabKeyForAdminScenario(dto);
		if (isBlank(resolvedTabKey)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Choose a valid portal category (tab from the list).");
			return response;
		}
		if (!isAllowedScenarioTabKey(resolvedTabKey)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("That portal module cannot be used for issue scenarios.");
			return response;
		}
		dto.setTabKey(resolvedTabKey);
		if (dto.getScenarioLabel().trim().length() > 500) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Scenario label must be 500 characters or less.");
			return response;
		}
		if (dto.getFeatureName() != null && dto.getFeatureName().trim().length() > 200) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Feature name must be 200 characters or less.");
			return response;
		}
		if (dto.getSubFeatureName() != null && dto.getSubFeatureName().trim().length() > 200) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Sub-feature name must be 200 characters or less.");
			return response;
		}
		GrievanceIssueScenario entity = new GrievanceIssueScenario();
		applyIssueScenarioDto(dto, entity, true);
		grievanceIssueScenarioRepository.save(entity);
		entity.setCategoryDisplayName(displayNameForScenarioTabKey(entity.getTabKey()));
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(entity);
		return response;
	}

	public ServiceResponse updateAdminIssueScenario(Long empId, Long scenarioId, GrievanceIssueScenarioDTO dto) {
		ServiceResponse response = new ServiceResponse();
		if (!canManageIssueScenarios(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to manage grievance issue scenarios.");
			return response;
		}
		if (scenarioId == null || dto == null || isBlank(dto.getScenarioLabel())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Scenario id and label are required.");
			return response;
		}
		String resolvedTabKey = resolveTabKeyForAdminScenario(dto);
		if (isBlank(resolvedTabKey)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Choose a valid portal category (tab from the list).");
			return response;
		}
		if (!isAllowedScenarioTabKey(resolvedTabKey)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("That portal module cannot be used for issue scenarios.");
			return response;
		}
		dto.setTabKey(resolvedTabKey);
		if (dto.getScenarioLabel().trim().length() > 500) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Scenario label must be 500 characters or less.");
			return response;
		}
		if (dto.getFeatureName() != null && dto.getFeatureName().trim().length() > 200) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Feature name must be 200 characters or less.");
			return response;
		}
		if (dto.getSubFeatureName() != null && dto.getSubFeatureName().trim().length() > 200) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Sub-feature name must be 200 characters or less.");
			return response;
		}
		Optional<GrievanceIssueScenario> opt = grievanceIssueScenarioRepository.findById(scenarioId);
		if (opt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Issue scenario not found.");
			return response;
		}
		GrievanceIssueScenario entity = opt.get();
		applyIssueScenarioDto(dto, entity, false);
		grievanceIssueScenarioRepository.save(entity);
		entity.setCategoryDisplayName(displayNameForScenarioTabKey(entity.getTabKey()));
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(entity);
		return response;
	}

	public ServiceResponse deactivateAdminIssueScenario(Long empId, Long scenarioId) {
		ServiceResponse response = new ServiceResponse();
		if (!canManageIssueScenarios(empId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. You are not authorized to manage grievance issue scenarios.");
			return response;
		}
		if (scenarioId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Scenario id is required.");
			return response;
		}
		Optional<GrievanceIssueScenario> opt = grievanceIssueScenarioRepository.findById(scenarioId);
		if (opt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Issue scenario not found.");
			return response;
		}
		GrievanceIssueScenario entity = opt.get();
		entity.setIsActive(0);
		grievanceIssueScenarioRepository.save(entity);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Issue scenario deactivated.");
		return response;
	}

	private boolean canManageIssueScenarios(Employee employee) {
		if (employee == null || employee.getJobRoleId() == null) {
			return false;
		}
		Optional<SubFeatureMaster> subOpt = subFeatureMasterRepository.findBySubFeatureNameUnderFeature(
				GRIEVANCE_ISSUE_SCENARIOS_SUBFEATURE, GRIEVANCE_FEATURE_NAME);
		if (subOpt.isPresent() && subOpt.get().getSubFeatureMasterId() != null) {
			RoleFeatureMap map = roleFeatureMapRepository.findByJobRoleIdAndSubFeatureMasterId(employee.getJobRoleId(),
					subOpt.get().getSubFeatureMasterId());
			if (map != null) {
				return true;
			}
		}
		// Align with grievance-rbac-seed.sql if mapping row missing or DB not re-seeded yet
		return canManageIssueScenariosByPolicy(employee);
	}

	/**
	 * Admins (role or department name), or Development with VP or Project Manager in job role text.
	 */
	private boolean canManageIssueScenariosByPolicy(Employee employee) {
		String dept = Optional.ofNullable(resolveDepartmentName(employee)).orElse("").toLowerCase(Locale.ROOT);
		String roleBlob = buildJobRoleTextForAccess(employee).toLowerCase(Locale.ROOT);
		if (roleBlob.contains("admin") || dept.contains("admin")) {
			return true;
		}
		return dept.contains("development")
				&& (roleBlob.contains("vp") || roleBlob.contains("project manager"));
	}

	private String buildJobRoleTextForAccess(Employee employee) {
		StringBuilder sb = new StringBuilder();
		if (employee.getJobRoleId() != null) {
			Optional<JobRole> jrOpt = jobRoleRepository.findById(employee.getJobRoleId());
			if (jrOpt.isPresent()) {
				JobRole jr = jrOpt.get();
				if (jr.getName() != null) {
					sb.append(jr.getName()).append(' ');
				}
				if (jr.getEmployeeRole() != null) {
					sb.append(jr.getEmployeeRole()).append(' ');
				}
			}
		}
		return sb.toString();
	}

	private boolean isAllowedScenarioTabKey(String tabKey) {
		if (isBlank(tabKey)) {
			return false;
		}
		String k = tabKey.trim().toLowerCase(Locale.ROOT);
		for (TabMaster tab : tabMasterRepository.findAll()) {
			if (scenarioStorageKeyForTab(tab).equalsIgnoreCase(k)) {
				return true;
			}
		}
		return false;
	}

	private String resolveTabKeyForAdminScenario(GrievanceIssueScenarioDTO dto) {
		if (dto == null) {
			return "";
		}
		if (!isBlank(dto.getCategoryTabName())) {
			String k = resolveIssueScenarioTabKey(dto.getCategoryTabName());
			if (!isBlank(k)) {
				return k;
			}
		}
		if (!isBlank(dto.getTabKey())) {
			return resolveStoredScenarioTabKeyFromClientValue(dto.getTabKey());
		}
		return "";
	}

	private String resolveStoredScenarioTabKeyFromClientValue(String raw) {
		if (isBlank(raw)) {
			return "";
		}
		String n = raw.trim().toLowerCase(Locale.ROOT);
		for (TabMaster tab : tabMasterRepository.findAll()) {
			String key = scenarioStorageKeyForTab(tab);
			if (key.equalsIgnoreCase(n)) {
				return key;
			}
		}
		return applyLegacyScenarioKeyAliases(normalizeScenarioKey(raw));
	}

	private Optional<TabMaster> findTabByDisplayName(String categoryTabName) {
		if (isBlank(categoryTabName)) {
			return Optional.empty();
		}
		String needle = categoryTabName.trim().toLowerCase(Locale.ROOT);
		return tabMasterRepository.findAll().stream()
				.filter(t -> t.getTabName() != null && t.getTabName().trim().toLowerCase(Locale.ROOT).equals(needle))
				.findFirst();
	}

	private String normalizeScenarioKey(String raw) {
		if (raw == null) {
			return "";
		}
		String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
		while (s.contains("__")) {
			s = s.replace("__", "_");
		}
		s = s.replaceAll("^_+", "").replaceAll("_+$", "");
		if (s.length() > 50) {
			s = s.substring(0, 50);
		}
		return s;
	}

	/**
	 * Keeps existing seeded rows on {@code timesheet} / {@code leave} aligned with route-based keys such as
	 * {@code user_timesheet}.
	 */
	private String applyLegacyScenarioKeyAliases(String normalizedKey) {
		if (isBlank(normalizedKey)) {
			return "";
		}
		if (normalizedKey.contains("timesheet")) {
			return "timesheet";
		}
		if (normalizedKey.contains("leave")) {
			return "leave";
		}
		return normalizedKey;
	}

	private String scenarioStorageKeyForTab(TabMaster tab) {
		if (tab == null) {
			return "";
		}
		String base;
		if (!isBlank(tab.getTabRouteName())) {
			base = normalizeScenarioKey(tab.getTabRouteName());
		} else {
			base = normalizeScenarioKey(tab.getTabName());
		}
		return applyLegacyScenarioKeyAliases(base);
	}

	private String displayNameForScenarioTabKey(String tabKey) {
		if (isBlank(tabKey)) {
			return null;
		}
		String k = tabKey.trim().toLowerCase(Locale.ROOT);
		for (TabMaster tab : tabMasterRepository.findAll()) {
			if (scenarioStorageKeyForTab(tab).equalsIgnoreCase(k)) {
				return tab.getTabName() != null ? tab.getTabName().trim() : tabKey;
			}
		}
		return tabKey;
	}

	private void applyIssueScenarioDto(GrievanceIssueScenarioDTO dto, GrievanceIssueScenario entity, boolean isCreate) {
		entity.setTabKey(dto.getTabKey().trim().toLowerCase(Locale.ROOT));
		String feat = dto.getFeatureName();
		entity.setFeatureName(isBlank(feat) ? null : feat.trim());
		String sub = dto.getSubFeatureName();
		entity.setSubFeatureName(isBlank(sub) ? null : sub.trim());
		entity.setScenarioLabel(dto.getScenarioLabel().trim());
		int order = dto.getSortOrder() != null ? dto.getSortOrder() : 0;
		entity.setSortOrder(order);
		if (isCreate) {
			entity.setIsActive(dto.getIsActive() != null && dto.getIsActive() == 0 ? 0 : 1);
		} else {
			if (dto.getIsActive() != null) {
				entity.setIsActive(dto.getIsActive() == 0 ? 0 : 1);
			}
		}
	}

	private static String safeParam(String value) {
		return value == null ? "" : value.trim();
	}

	/**
	 * Resolves stored {@code tab_key} from portal tab display name (same strings as grievance category dropdown).
	 */
	private String resolveIssueScenarioTabKey(String categoryTabName) {
		Optional<TabMaster> tabOpt = findTabByDisplayName(categoryTabName);
		if (tabOpt.isEmpty()) {
			return "";
		}
		return scenarioStorageKeyForTab(tabOpt.get());
	}

	@Transactional
	public ServiceResponse updateTicket(Long empId, GrievanceTicketUpdateDTO updateDTO, MultipartFile resolutionDocument) {
		ServiceResponse response = new ServiceResponse();

		if (updateDTO == null || updateDTO.getTicketId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}

		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(updateDTO.getTicketId(), 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}
		GrievanceTicket ticket = ticketOpt.get();
		GrievanceTicket auditBefore = grievanceAuditService.copyTicketScalars(ticket);
		Long previousAssignedEmpId = ticket.getAssignedToEmpId();
		String previousStatus = normalizeStatus(ticket.getStatus());
		boolean isPrivileged = canViewAllTickets(empId);
		boolean isAssignedUser = empId.equals(ticket.getAssignedToEmpId());
		if (!isPrivileged && !isAssignedUser) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied. Only assigned user can update this ticket.");
			return response;
		}

		if (!isBlank(updateDTO.getSubject())) {
			ticket.setSubject(updateDTO.getSubject().trim());
		}
		if (!isBlank(updateDTO.getCategory())) {
			ticket.setCategory(updateDTO.getCategory().trim());
		}
		if (!isBlank(updateDTO.getDescription())) {
			ticket.setDescription(updateDTO.getDescription().trim());
		}
		if (!isBlank(updateDTO.getPriority())) {
			ticket.setPriority(updateDTO.getPriority().trim().toUpperCase(Locale.ROOT));
		}
		if (!isBlank(updateDTO.getStatus())) {
			String nextStatus = normalizeStatus(updateDTO.getStatus());
			if (!isValidStatus(nextStatus)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid status. Allowed values: OPEN, IN_PROGRESS, ESCALATED, RESOLVED, RE-OPENED, CLOSED, REJECTED.");
				return response;
			}
			ticket.setStatus(nextStatus);
			if ("CLOSED".equals(nextStatus)) {
				ticket.setClosedByCreator(0);
			}
		}
		if (updateDTO.getAssignedToEmpId() != null && isPrivileged) {
			if (!isEmployeeEligibleForGrievanceAssignment(updateDTO.getAssignedToEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Assignee must belong to Development or HR department.");
				return response;
			}
			ticket.setAssignedToEmpId(updateDTO.getAssignedToEmpId());
		}
		if (!isBlank(updateDTO.getAssignedToName())) {
			ticket.setAssignedToName(updateDTO.getAssignedToName().trim());
		}
		if (!isBlank(updateDTO.getResolutionRemarks())) {
			ticket.setResolutionRemarks(updateDTO.getResolutionRemarks().trim());
		}
		if (!isBlank(updateDTO.getResolutionCategory())) {
			ticket.setResolutionCategory(updateDTO.getResolutionCategory().trim());
		}
		if (!isBlank(updateDTO.getActionTaken())) {
			ticket.setActionTaken(updateDTO.getActionTaken().trim());
		}
		if (resolutionDocument != null && !resolutionDocument.isEmpty()) {
			if (resolutionDocument.getSize() > MAX_PROOF_FILE_SIZE_BYTES) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Resolution document must be 5MB or smaller.");
				return response;
			}
			attachResolutionDocument(ticket, empId, resolutionDocument);
		}
		if ("RESOLVED".equals(ticket.getStatus())) {
			if (isBlank(ticket.getResolutionCategory())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Resolution category is required when status is Resolved.");
				return response;
			}
			if (isBlank(ticket.getActionTaken())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Action taken is required when status is Resolved.");
				return response;
			}
			if (isBlank(ticket.getResolutionRemarks())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Resolution remarks are required when status is Resolved.");
				return response;
			}
		}
		if ("RESOLVED".equals(ticket.getStatus()) || "CLOSED".equals(ticket.getStatus())) {
			Optional<Employee> resolverOpt = employeeRepository.findById(empId);
			ticket.setResolvedBy(resolverOpt.map(Employee::getName).orElse("System"));
			if (ticket.getResolutionDate() == null) {
				ticket.setResolutionDate(new Timestamp(System.currentTimeMillis()));
			}
		}
		ticket.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		ticket.setUpdatedBy(empId);

		String actorName = employeeRepository.findById(empId).map(Employee::getName).orElse("User " + empId);
		boolean resolutionDocAdded = resolutionDocument != null && !resolutionDocument.isEmpty();
		grievanceAuditService.recordTicketUpdated(auditBefore, ticket, empId, actorName, resolutionDocAdded, null);
		grievanceTicketRepository.save(ticket);
		triggerUpdateNotifications(ticket, previousAssignedEmpId, previousStatus);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(ticket);
		return response;
	}

	private void triggerUpdateNotifications(GrievanceTicket ticket, Long previousAssignedEmpId, String previousStatus) {
		Long currentAssignedEmpId = ticket.getAssignedToEmpId();
		String currentStatus = normalizeStatus(ticket.getStatus());

		boolean assignmentChanged = !java.util.Objects.equals(previousAssignedEmpId, currentAssignedEmpId);
		boolean statusChanged = !java.util.Objects.equals(previousStatus, currentStatus);
		boolean resolvedOrClosed = "RESOLVED".equals(currentStatus) || "CLOSED".equals(currentStatus);

		if (assignmentChanged) {
			triggerGrievanceEmail(GrievanceEmailEventType.ASSIGNMENT_CHANGE, ticket, previousAssignedEmpId, previousStatus);
		}
		if (statusChanged) {
			triggerGrievanceEmail(GrievanceEmailEventType.STATUS_CHANGE, ticket, previousAssignedEmpId, previousStatus);
		}
		if (statusChanged && resolvedOrClosed) {
			triggerGrievanceEmail(GrievanceEmailEventType.RESOLVED_CLOSED, ticket, previousAssignedEmpId, previousStatus);
		}
	}

	private void triggerGrievanceEmail(GrievanceEmailEventType eventType,
			GrievanceTicket ticket,
			Long previousAssignedEmpId,
			String previousStatus) {
		try {
			String to = resolveEventToRecipient(eventType, ticket);
			if (isBlank(to)) {
				log.warn("Skipping grievance email {} for ticket {} because TO recipient is blank.",
						eventType, ticket != null ? ticket.getTicketNumber() : null);
				return;
			}

			List<String> eventCc = resolveEventCcRecipients(eventType, ticket);
			List<String> cc = mergeCcRecipients(eventCc, parseConfiguredCcRecipients());
			cc.removeIf(addr -> addr.equalsIgnoreCase(to));

			String subject = buildEmailSubject(eventType, ticket);
			String body = buildEmailBody(eventType, ticket, previousAssignedEmpId, previousStatus);
			sendEmail(to, cc, subject, body);

			log.info("Grievance mail triggered event={} ticketId={} ticketNumber={} to={} cc={} triggeredAt={}",
					eventType,
					ticket != null ? ticket.getTicketId() : null,
					ticket != null ? ticket.getTicketNumber() : null,
					to,
					cc,
					new Timestamp(System.currentTimeMillis()));
		} catch (Exception ex) {
			log.error("Failed to trigger grievance email {} for ticketId={} ticketNumber={}",
					eventType,
					ticket != null ? ticket.getTicketId() : null,
					ticket != null ? ticket.getTicketNumber() : null,
					ex);
		}
	}

	private String resolveEventToRecipient(GrievanceEmailEventType eventType, GrievanceTicket ticket) {
		if (ticket == null) {
			return null;
		}
		if (eventType == GrievanceEmailEventType.CREATE || eventType == GrievanceEmailEventType.ASSIGNMENT_CHANGE) {
			return resolveEmployeeEmail(ticket.getAssignedToEmpId());
		}
		return resolveEmployeeEmail(ticket.getCreatedByEmpId());
	}

	private List<String> resolveEventCcRecipients(GrievanceEmailEventType eventType, GrievanceTicket ticket) {
		if (ticket == null) {
			return Collections.emptyList();
		}
		List<String> recipients = new ArrayList<>();
		if (eventType == GrievanceEmailEventType.CREATE || eventType == GrievanceEmailEventType.ASSIGNMENT_CHANGE) {
			String creatorEmail = resolveEmployeeEmail(ticket.getCreatedByEmpId());
			if (!isBlank(creatorEmail)) {
				recipients.add(creatorEmail);
			}
		} else {
			String assigneeEmail = resolveEmployeeEmail(ticket.getAssignedToEmpId());
			if (!isBlank(assigneeEmail)) {
				recipients.add(assigneeEmail);
			}
		}
		return recipients;
	}

	private List<String> parseConfiguredCcRecipients() {
		if (isBlank(grievanceDefaultCc)) {
			return Collections.emptyList();
		}
		return Arrays.stream(grievanceDefaultCc.split(","))
				.map(value -> value == null ? "" : value.trim())
				.filter(value -> !value.isEmpty())
				.collect(Collectors.toList());
	}

	private List<String> mergeCcRecipients(List<String> eventCc, List<String> defaultCc) {
		LinkedHashSet<String> unique = new LinkedHashSet<>();
		if (eventCc != null) {
			eventCc.stream()
					.filter(value -> !isBlank(value))
					.map(String::trim)
					.forEach(unique::add);
		}
		if (defaultCc != null) {
			defaultCc.stream()
					.filter(value -> !isBlank(value))
					.map(String::trim)
					.forEach(unique::add);
		}
		return new ArrayList<>(unique);
	}

	private String resolveEmployeeEmail(Long empId) {
		if (empId == null) {
			return null;
		}
		return employeeRepository.findById(empId)
				.map(Employee::getEmail)
				.map(String::trim)
				.filter(email -> !email.isEmpty())
				.orElse(null);
	}

	private String resolveEmployeeName(Long empId) {
		if (empId == null) {
			return "N/A";
		}
		return employeeRepository.findById(empId)
				.map(Employee::getName)
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.orElse("EmpId " + empId);
	}

	private String buildEmailSubject(GrievanceEmailEventType eventType, GrievanceTicket ticket) {
		String ticketNo = ticket != null ? ticket.getTicketNumber() : "";
		switch (eventType) {
			case CREATE:
				return "[Grievance] Ticket Created - " + ticketNo;
			case ASSIGNMENT_CHANGE:
				return "[Grievance] Ticket Reassigned - " + ticketNo;
			case STATUS_CHANGE:
				return "[Grievance] Ticket Status Changed - " + ticketNo;
			case RESOLVED_CLOSED:
				return "[Grievance] Ticket " + normalizeStatus(ticket != null ? ticket.getStatus() : "") + " - " + ticketNo;
			default:
				return "[Grievance] Ticket Update - " + ticketNo;
		}
	}

	private String buildEmailBody(GrievanceEmailEventType eventType,
			GrievanceTicket ticket,
			Long previousAssignedEmpId,
			String previousStatus) {
		StringBuilder body = new StringBuilder();
		body.append("<html><body>");
		body.append("<p>Hello,</p>");
		body.append("<p>Grievance ticket notification event: <b>").append(eventType).append("</b></p>");
		if (eventType == GrievanceEmailEventType.ASSIGNMENT_CHANGE) {
			body.append("<p><b>Assignment changed from:</b> ")
					.append(resolveEmployeeName(previousAssignedEmpId))
					.append(" <b>to:</b> ")
					.append(resolveEmployeeName(ticket.getAssignedToEmpId()))
					.append("</p>");
		}
		if (eventType == GrievanceEmailEventType.STATUS_CHANGE || eventType == GrievanceEmailEventType.RESOLVED_CLOSED) {
			body.append("<p><b>Status changed from:</b> ")
					.append(isBlank(previousStatus) ? "N/A" : previousStatus)
					.append(" <b>to:</b> ")
					.append(normalizeStatus(ticket.getStatus()))
					.append("</p>");
		}
		body.append("<h4>Ticket Details</h4>");
		body.append("<table border='1' cellspacing='0' cellpadding='6' style='border-collapse:collapse;'>");
		appendBodyRow(body, "Ticket ID", safeString(ticket.getTicketNumber()));
		appendBodyRow(body, "Title", safeString(ticket.getSubject()));
		appendBodyRow(body, "Description", safeString(ticket.getDescription()));
		appendBodyRow(body, "Status", safeString(ticket.getStatus()));
		appendBodyRow(body, "Assigned User", safeString(ticket.getAssignedToName()) + " (" + safeString(ticket.getAssignedToEmpId()) + ")");
		appendBodyRow(body, "Created Timestamp", safeString(ticket.getCreatedOn()));
		appendBodyRow(body, "Last Updated Timestamp", safeString(ticket.getUpdatedOn()));
		body.append("</table>");
		body.append("<p>Regards,<br/>iShine Portal</p>");
		body.append("</body></html>");
		return body.toString();
	}

	private void appendBodyRow(StringBuilder body, String label, String value) {
		body.append("<tr><td><b>")
				.append(label)
				.append("</b></td><td>")
				.append(value)
				.append("</td></tr>");
	}

	private String safeString(Object value) {
		return value == null ? "N/A" : String.valueOf(value);
	}

	private void sendEmail(String to, List<String> cc, String subject, String body) {
		try {
			mailService.sendMailToMultipleRecipients(
					Collections.singletonList(to),
					cc == null ? Collections.emptyList() : cc,
					subject,
					body);
		} catch (Exception ex) {
			log.error("Failed to send grievance email to={} cc={} subject={}", to, cc, subject, ex);
		}
	}

	@Transactional
	public ServiceResponse submitFeedback(Long empId, GrievanceFeedbackDTO feedbackDTO) {
		ServiceResponse response = new ServiceResponse();
		if (feedbackDTO == null || feedbackDTO.getTicketId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(feedbackDTO.getTicketId(), 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}
		GrievanceTicket ticket = ticketOpt.get();
		GrievanceTicket auditBefore = grievanceAuditService.copyTicketScalars(ticket);
		if (!empId.equals(ticket.getCreatedByEmpId())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Only ticket creator can submit feedback.");
			return response;
		}
		String ticketStatus = ticket.getStatus();
		boolean resolvedFeedback = "RESOLVED".equals(ticketStatus);
		boolean closedFeedback = "CLOSED".equals(ticketStatus);
		if (!resolvedFeedback && !closedFeedback) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Feedback can be submitted only when the ticket is Resolved or Closed.");
			return response;
		}
		if (closedFeedback && Integer.valueOf(1).equals(ticket.getClosedByCreator())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Feedback was already recorded when you closed this ticket.");
			return response;
		}
		if (ticket.getFeedbackOn() != null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Feedback has already been submitted for this ticket.");
			return response;
		}
		if (feedbackDTO.getRating() == null || feedbackDTO.getRating() < 1 || feedbackDTO.getRating() > 5) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Rating should be between 1 and 5.");
			return response;
		}
		ticket.setFeedbackRating(feedbackDTO.getRating());
		ticket.setFeedbackComments(isBlank(feedbackDTO.getComments()) ? null : feedbackDTO.getComments().trim());
		ticket.setFeedbackOn(new Timestamp(System.currentTimeMillis()));
		ticket.setUpdatedBy(empId);
		ticket.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		String actorName = employeeRepository.findById(empId).map(Employee::getName).orElse("User " + empId);
		grievanceAuditService.recordFeedbackSubmitted(auditBefore, ticket, empId, actorName);
		grievanceTicketRepository.save(ticket);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(ticket);
		return response;
	}

	/**
	 * Ticket creator may reopen RESOLVED tickets, or CLOSED tickets that were not closed by the creator with feedback.
	 * Status becomes RE-OPENED (not OPEN).
	 */
	@Transactional
	public ServiceResponse reopenTicket(Long empId, Long ticketId) {
		ServiceResponse response = new ServiceResponse();
		if (ticketId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}
		GrievanceTicket ticket = ticketOpt.get();
		GrievanceTicket auditBefore = grievanceAuditService.copyTicketScalars(ticket);
		if (!empId.equals(ticket.getCreatedByEmpId())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Only the ticket creator can reopen this ticket.");
			return response;
		}
		boolean resolved = "RESOLVED".equals(ticket.getStatus());
		boolean closedEligible = "CLOSED".equals(ticket.getStatus())
				&& !Integer.valueOf(1).equals(ticket.getClosedByCreator());
		if (!resolved && !closedEligible) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			if ("CLOSED".equals(ticket.getStatus()) && Integer.valueOf(1).equals(ticket.getClosedByCreator())) {
				response.setServiceResponse("This ticket was closed by you with feedback and cannot be reopened.");
			} else {
				response.setServiceResponse("Only resolved or eligible closed tickets can be reopened.");
			}
			return response;
		}
		ticket.setStatus("RE-OPENED");
		ticket.setClosedByCreator(0);
		ticket.setUpdatedBy(empId);
		ticket.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		String actorName = employeeRepository.findById(empId).map(Employee::getName).orElse("User " + empId);
		grievanceAuditService.recordReopen(auditBefore, ticket, empId, actorName);
		grievanceTicketRepository.save(ticket);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(ticket);
		return response;
	}

	@Transactional
	public ServiceResponse withdrawTicket(Long empId, Long ticketId) {
		ServiceResponse response = new ServiceResponse();
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}

		GrievanceTicket ticket = ticketOpt.get();
		GrievanceTicket auditBefore = grievanceAuditService.copyTicketScalars(ticket);
		if (!empId.equals(ticket.getCreatedByEmpId())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Only ticket creator can withdraw this ticket.");
			return response;
		}

		ticket.setStatus("WITHDRAWN");
		ticket.setIsActive(0);
		ticket.setUpdatedBy(empId);
		ticket.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		String actorName = employeeRepository.findById(empId).map(Employee::getName).orElse("User " + empId);
		grievanceAuditService.recordWithdraw(auditBefore, ticket, empId, actorName);
		grievanceTicketRepository.save(ticket);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Ticket withdrawn successfully.");
		return response;
	}

	public Resource getProofDocument(Long empId, Long ticketId) {
		GrievanceProofDownloadDTO dto = resolveProofDownload(empId, ticketId, 0L);
		return dto == null ? null : dto.getResource();
	}

	/**
	 * Lists proof files for a ticket. {@code documentId} 0 in downloads refers to legacy rows
	 * (only {@code grievance_ticket.proof_file_path} set) exposed as a single synthetic entry.
	 */
	public ServiceResponse listTicketProofDocuments(Long empId, Long ticketId) {
		ServiceResponse response = new ServiceResponse();
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket not found.");
			return response;
		}
		GrievanceTicket ticket = ticketOpt.get();
		boolean canAccess = canViewAllTickets(empId) || empId.equals(ticket.getCreatedByEmpId())
				|| empId.equals(ticket.getAssignedToEmpId());
		if (!canAccess) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		List<GrievanceDocument> docs = grievanceDocumentRepository.findByTicketIdAndIsActiveOrderBySortOrderAscDocumentIdAsc(ticketId, 1);
		List<GrievanceProofDocumentDTO> out = new ArrayList<>();
		for (GrievanceDocument d : docs) {
			out.add(new GrievanceProofDocumentDTO(d.getDocumentId(), d.getOriginalFileName(), d.getSortOrder()));
		}
		if (out.isEmpty() && !isBlank(ticket.getProofFilePath()) && isStoredProofPathAllowed(ticket.getProofFilePath())) {
			String legacyName = !isBlank(ticket.getProofFileName()) ? ticket.getProofFileName() : "proof";
			out.add(new GrievanceProofDocumentDTO(0L, legacyName, 0));
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(out);
		return response;
	}

	/**
	 * @param documentId use {@code 0} for first file (table order) or legacy-only ticket proof path
	 */
	public GrievanceProofDownloadDTO resolveProofDownload(Long empId, Long ticketId, Long documentId) {
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			return null;
		}
		GrievanceTicket ticket = ticketOpt.get();
		boolean canAccess = canViewAllTickets(empId) || empId.equals(ticket.getCreatedByEmpId())
				|| empId.equals(ticket.getAssignedToEmpId());
		if (!canAccess) {
			return null;
		}
		try {
			if (documentId != null && documentId > 0) {
				Optional<GrievanceDocument> docOpt = grievanceDocumentRepository.findById(documentId);
				if (docOpt.isEmpty() || !Integer.valueOf(1).equals(docOpt.get().getIsActive())
						|| !ticketId.equals(docOpt.get().getTicketId())) {
					return null;
				}
				GrievanceDocument doc = docOpt.get();
				if (!isStoredProofPathAllowed(doc.getStoredFilePath())) {
					return null;
				}
				Path path = Paths.get(doc.getStoredFilePath()).normalize();
				return new GrievanceProofDownloadDTO(new FileSystemResource(path), doc.getOriginalFileName());
			}
			List<GrievanceDocument> docs = grievanceDocumentRepository.findByTicketIdAndIsActiveOrderBySortOrderAscDocumentIdAsc(ticketId, 1);
			if (!docs.isEmpty()) {
				GrievanceDocument first = docs.get(0);
				if (!isStoredProofPathAllowed(first.getStoredFilePath())) {
					return null;
				}
				Path path = Paths.get(first.getStoredFilePath()).normalize();
				return new GrievanceProofDownloadDTO(new FileSystemResource(path), first.getOriginalFileName());
			}
			if (isBlank(ticket.getProofFilePath()) || !isStoredProofPathAllowed(ticket.getProofFilePath())) {
				return null;
			}
			Path path = Paths.get(ticket.getProofFilePath()).normalize();
			String name = !isBlank(ticket.getProofFileName()) ? ticket.getProofFileName() : "proof";
			return new GrievanceProofDownloadDTO(new FileSystemResource(path), name);
		} catch (InvalidPathException e) {
			return null;
		}
	}

	public Resource getResolutionDocument(Long empId, Long ticketId) {
		Optional<GrievanceTicket> ticketOpt = grievanceTicketRepository.findByTicketIdAndIsActive(ticketId, 1);
		if (ticketOpt.isEmpty()) {
			return null;
		}
		GrievanceTicket ticket = ticketOpt.get();
		boolean canAccess = canViewAllTickets(empId) || empId.equals(ticket.getCreatedByEmpId()) || empId.equals(ticket.getAssignedToEmpId());
		if (!canAccess || isBlank(ticket.getResolutionDocPath())) {
			return null;
		}
		try {
			Path path = Paths.get(ticket.getResolutionDocPath()).normalize();
			if (!Files.exists(path)) {
				return null;
			}
			return new FileSystemResource(path);
		} catch (InvalidPathException e) {
			return null;
		}
	}

	private boolean canViewAllTickets(Long empId) {
		Optional<Employee> employeeOpt = employeeRepository.findById(empId);
		if (employeeOpt.isEmpty()) {
			return false;
		}

		Employee employee = employeeOpt.get();
		if (hasViewAllTicketsSubfeature(employee.getJobRoleId())) {
			return true;
		}

		String roleText = "";

		if (employee.getJobRoleId() != null) {
			JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
			if (jobRole != null && jobRole.getEmployeeRole() != null) {
				roleText = jobRole.getEmployeeRole();
			}
			if ((roleText == null || roleText.isBlank()) && jobRole != null && jobRole.getName() != null) {
				roleText = jobRole.getName();
			}
		}

		if ((roleText == null || roleText.isBlank()) && employee.getRole() != null) {
			roleText = employee.getRole();
		}

		String normalized = roleText == null ? "" : roleText.toLowerCase(Locale.ROOT);
		return normalized.contains("admin") || normalized.contains("vp") || normalized.contains("project manager");
	}

	private boolean hasViewAllTicketsSubfeature(Long jobRoleId) {
		if (jobRoleId == null) {
			return false;
		}

		List<RoleFeatureMap> mappings = roleFeatureMapRepository.findByJobRoleId(jobRoleId);
		if (mappings == null || mappings.isEmpty()) {
			return false;
		}

		Set<Long> mappedSubFeatureIds = mappings.stream()
				.map(RoleFeatureMap::getSubFeatureMasterId)
				.filter(id -> id != null && id > 0)
				.collect(Collectors.toSet());

		if (mappedSubFeatureIds.isEmpty()) {
			return false;
		}

		List<SubFeatureMaster> allSubFeatures = subFeatureMasterRepository.findAllById(mappedSubFeatureIds);
		return allSubFeatures.stream()
				.map(SubFeatureMaster::getSubFeatureName)
				.filter(name -> name != null && !name.isBlank())
				.map(name -> name.toLowerCase(Locale.ROOT))
				.anyMatch(name -> name.contains("view all tickets") || name.contains("all grievance"));
	}

	private boolean canViewAssignedTicketsQueue(Long empId) {
		Optional<Employee> employeeOpt = employeeRepository.findById(empId);
		if (employeeOpt.isEmpty()) {
			return false;
		}
		Employee employee = employeeOpt.get();
		if (hasAssignedTicketsSubfeature(employee.getJobRoleId())) {
			return true;
		}
		return isHrOrDevelopmentDepartmentForEmployee(employee);
	}

	private boolean hasAssignedTicketsSubfeature(Long jobRoleId) {
		if (jobRoleId == null) {
			return false;
		}
		List<RoleFeatureMap> mappings = roleFeatureMapRepository.findByJobRoleId(jobRoleId);
		if (mappings == null || mappings.isEmpty()) {
			return false;
		}
		Set<Long> mappedSubFeatureIds = mappings.stream()
				.map(RoleFeatureMap::getSubFeatureMasterId)
				.filter(id -> id != null && id > 0)
				.collect(Collectors.toSet());
		if (mappedSubFeatureIds.isEmpty()) {
			return false;
		}
		List<SubFeatureMaster> allSubFeatures = subFeatureMasterRepository.findAllById(mappedSubFeatureIds);
		return allSubFeatures.stream()
				.map(SubFeatureMaster::getSubFeatureName)
				.filter(name -> name != null && !name.isBlank())
				.map(name -> name.toLowerCase(Locale.ROOT))
				.anyMatch(name -> name.contains("assigned tickets"));
	}

	private boolean isHrOrDevelopmentDepartmentForEmployee(Employee employee) {
		String deptName = resolveDepartmentName(employee);
		if (deptName == null || deptName.isBlank()) {
			return false;
		}
		String d = deptName.toLowerCase(Locale.ROOT).trim();
		if (d.contains("development")) {
			return true;
		}
		if ("hr".equals(d) || d.contains("human resource")) {
			return true;
		}
		return false;
	}

	private boolean isEmployeeInDevelopmentDepartment(Long assigneeEmpId) {
		if (assigneeEmpId == null) {
			return false;
		}
		Optional<Employee> employeeOpt = employeeRepository.findById(assigneeEmpId);
		if (employeeOpt.isEmpty() || employeeOpt.get().getJobRoleId() == null) {
			return false;
		}

		Long assigneeDeptId = jobRoleRepository.findDeptIdByJobRoleId(employeeOpt.get().getJobRoleId());
		Long developmentDeptId = departmentRepository.findByDepartmentnameIgnoreCase("Development");
		return assigneeDeptId != null && developmentDeptId != null && assigneeDeptId.equals(developmentDeptId);
	}

	private boolean isEmployeeInHrDepartment(Long assigneeEmpId) {
		Optional<Employee> employeeOpt = employeeRepository.findById(assigneeEmpId);
		if (employeeOpt.isEmpty()) {
			return false;
		}
		return isHrDepartmentName(resolveDepartmentName(employeeOpt.get()));
	}

	private boolean isHrDepartmentName(String deptName) {
		if (deptName == null || deptName.isBlank()) {
			return false;
		}
		String d = deptName.toLowerCase(Locale.ROOT).trim();
		return "hr".equals(d) || d.contains("human resource");
	}

	private boolean isEmployeeEligibleForGrievanceAssignment(Long assigneeEmpId) {
		if (assigneeEmpId == null) {
			return false;
		}
		return isEmployeeInDevelopmentDepartment(assigneeEmpId) || isEmployeeInHrDepartment(assigneeEmpId);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean isValidStatus(String status) {
		return "OPEN".equals(status)
				|| "IN_PROGRESS".equals(status)
				|| "ESCALATED".equals(status)
				|| "RESOLVED".equals(status)
				|| "RE-OPENED".equals(status)
				|| "CLOSED".equals(status)
				|| "REJECTED".equals(status);
	}

	private String normalizeStatus(String status) {
		if (status == null) {
			return "";
		}
		return status.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
	}

	private String resolveDepartmentName(Employee employee) {
		if (employee == null || employee.getJobRoleId() == null) {
			return null;
		}
		Long deptId = jobRoleRepository.findDeptIdByJobRoleId(employee.getJobRoleId());
		if (deptId == null) {
			return null;
		}
		return departmentRepository.findDepartmentNameFromDeptId(deptId);
	}

	/** Persists creator context at submission time (not updated on later profile changes). */
	private void applyCreatorSnapshotFields(GrievanceTicket ticket, Employee employee) {
		if (employee == null) {
			return;
		}
		String phone = formatPhoneSnapshot(employee);
		if (!isBlank(phone)) {
			ticket.setCreatedByPhone(phone.trim());
		}
		EmpPrimaryProjectMapping mapping = empPrimaryProjectMappingRepository.findByEmpIdAndIsMapped(employee.getEmpId(), "Y");
		if (mapping != null && mapping.getPrimaryProjectId() != null) {
			Project project = projectRepository.findByProjectId(mapping.getPrimaryProjectId().intValue());
			if (project != null) {
				if (!isBlank(project.getProjectName())) {
					ticket.setCreatedByProjectName(project.getProjectName().trim());
				}
				if (!isBlank(project.getClientName())) {
					ticket.setCreatedByClientName(project.getClientName().trim());
				}
			}
		}
		String rmName = resolveReportingManagerName(employee);
		if (!isBlank(rmName)) {
			ticket.setCreatedByReportingManager(rmName.trim());
		}
	}

	private static String formatPhoneSnapshot(Employee employee) {
		if (employee.getMobileNo() != null) {
			return String.valueOf(employee.getMobileNo());
		}
		if (employee.getAlternateMobileNo() != null) {
			return String.valueOf(employee.getAlternateMobileNo());
		}
		return null;
	}

	private String resolveReportingManagerName(Employee employee) {
		if (employee.getReportingManagerId() != null) {
			Optional<Employee> rm = employeeRepository.findById(employee.getReportingManagerId());
			if (rm.isPresent() && !isBlank(rm.get().getName())) {
				return rm.get().getName().trim();
			}
		}
		if (employee.getManagerId() != null) {
			Optional<Employee> mgr = employeeRepository.findById(employee.getManagerId());
			if (mgr.isPresent() && !isBlank(mgr.get().getName())) {
				return mgr.get().getName().trim();
			}
		}
		return null;
	}

	private void attachResolutionDocument(GrievanceTicket ticket, Long empId, MultipartFile resolutionDocument) {
		try {
			String originalName = resolutionDocument.getOriginalFilename() == null ? "resolution_doc" : resolutionDocument.getOriginalFilename();
			String extension = "";
			int dotIndex = originalName.lastIndexOf('.');
			if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
				extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
			}
			if (!isAllowedProofExtension(extension)) {
				throw new IllegalArgumentException("Unsupported resolution file type. Allowed: pdf, png, jpg, jpeg, doc, docx.");
			}
			Path grievanceDir = Paths.get(grievanceDocumentLocation);
			if (!Files.exists(grievanceDir)) {
				Files.createDirectories(grievanceDir);
			}
			String safeName = "grievance_resolution_" + empId + "_" + UUID.randomUUID() + extension;
			Path destination = grievanceDir.resolve(safeName);
			Files.copy(resolutionDocument.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
			ticket.setResolutionDocName(originalName);
			ticket.setResolutionDocPath(destination.toString());
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to save resolution document: " + e.getMessage());
		}
	}

	private void ensureTicketNumbers(List<GrievanceTicket> tickets) {
		if (tickets == null || tickets.isEmpty()) {
			return;
		}
		boolean updated = false;
		for (GrievanceTicket ticket : tickets) {
			if (isBlank(ticket.getTicketNumber())) {
				ticket.setTicketNumber(generateUniqueTicketNumber());
				updated = true;
			}
		}
		if (updated) {
			grievanceTicketRepository.saveAll(tickets);
		}
	}

	private static final int MAX_PAGE_SIZE = 100;

	private int clampPageSize(int size) {
		if (size <= 0) {
			return 20;
		}
		return Math.min(size, MAX_PAGE_SIZE);
	}

	private String resolveSortProperty(String sortBy) {
		if (isBlank(sortBy)) {
			return "createdOn";
		}
		switch (sortBy.trim()) {
		case "displayTicketId":
			return "ticketNumber";
		case "subject":
		case "category":
		case "priority":
		case "status":
		case "createdByName":
		case "assignedToName":
		case "createdOn":
		case "ticketNumber":
			return sortBy.trim();
		default:
			return "createdOn";
		}
	}

	private Sort.Direction resolveSortDirection(String sortDir) {
		if (isBlank(sortDir)) {
			return Sort.Direction.DESC;
		}
		String d = sortDir.trim().toLowerCase(Locale.ROOT);
		if ("asc".equals(d)) {
			return Sort.Direction.ASC;
		}
		return Sort.Direction.DESC;
	}

	private Pageable buildPageable(int pageIndex, int pageSize, String sortBy, String sortDir) {
		String property = resolveSortProperty(sortBy);
		Sort sort = Sort.by(resolveSortDirection(sortDir), property);
		return PageRequest.of(pageIndex, pageSize, sort);
	}

	private GrievanceTicketListPageDTO toPageDto(Page<GrievanceTicket> pageResult, int pageOneBased, String sortBy, String sortDir) {
		GrievanceTicketListPageDTO dto = new GrievanceTicketListPageDTO();
		dto.setContent(pageResult.getContent());
		dto.setTotalElements(pageResult.getTotalElements());
		dto.setTotalPages(pageResult.getTotalPages());
		dto.setPage(pageOneBased);
		dto.setSize(pageResult.getSize());
		dto.setSortBy(resolveSortProperty(sortBy));
		dto.setSortDir(resolveSortDirection(sortDir).name().toLowerCase(Locale.ROOT));
		return dto;
	}

	private String resolveIssueScenarioSortProperty(String sortBy) {
		if (isBlank(sortBy)) {
			return "tabKey";
		}
		switch (sortBy.trim()) {
		case "tabKey":
		case "subFeatureName":
		case "featureName":
		case "scenarioLabel":
		case "sortOrder":
		case "isActive":
		case "scenarioId":
			return sortBy.trim();
		default:
			return "tabKey";
		}
	}

	private Pageable buildIssueScenarioPageable(int pageIndex, int pageSize, String sortBy, String sortDir) {
		String property = resolveIssueScenarioSortProperty(sortBy);
		Sort sort = Sort.by(resolveSortDirection(sortDir), property);
		return PageRequest.of(pageIndex, pageSize, sort);
	}

	private GrievanceIssueScenarioPageDTO toIssueScenarioPageDto(Page<GrievanceIssueScenario> pageResult,
			int pageOneBased, String sortBy, String sortDir) {
		GrievanceIssueScenarioPageDTO dto = new GrievanceIssueScenarioPageDTO();
		dto.setContent(pageResult.getContent());
		dto.setTotalElements(pageResult.getTotalElements());
		dto.setTotalPages(pageResult.getTotalPages());
		dto.setPage(pageOneBased);
		dto.setSize(pageResult.getSize());
		dto.setSortBy(resolveIssueScenarioSortProperty(sortBy));
		dto.setSortDir(resolveSortDirection(sortDir).name().toLowerCase(Locale.ROOT));
		return dto;
	}

	private String generateUniqueTicketNumber() {
		for (int i = 0; i < 30; i++) {
			int number = ThreadLocalRandom.current().nextInt(1000, 10000);
			String ticketNumber = TICKET_NUMBER_PREFIX + number;
			if (!grievanceTicketRepository.existsByTicketNumber(ticketNumber)) {
				return ticketNumber;
			}
		}
		return TICKET_NUMBER_PREFIX + (System.currentTimeMillis() % 10000);
	}

	/** Marks RESOLVED tickets as CLOSED when resolution_date is at least 3 days in the past. Runs hourly. */
	@Scheduled(cron = "0 0 * * * ?")
	@Transactional
	public void autoCloseResolvedGrievanceTickets() {
		try {
			long cutoffMs = System.currentTimeMillis()
					- GRIEVANCE_AUTO_CLOSE_AFTER_RESOLVED_DAYS * 24L * 60L * 60L * 1000L;
			Timestamp cutoff = new Timestamp(cutoffMs);
			List<GrievanceTicket> list = grievanceTicketRepository.findResolvedEligibleForAutoClose(cutoff);
			if (list.isEmpty()) {
				return;
			}
			List<GrievanceTicket> beforeCopies = new ArrayList<>();
			for (GrievanceTicket t : list) {
				beforeCopies.add(grievanceAuditService.copyTicketScalars(t));
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());
			for (int i = 0; i < list.size(); i++) {
				GrievanceTicket t = list.get(i);
				t.setStatus("CLOSED");
				t.setClosedByCreator(0);
				t.setUpdatedOn(now);
				t.setUpdatedBy(null);
				grievanceAuditService.recordSystemAutoClose(beforeCopies.get(i), t);
			}
			grievanceTicketRepository.saveAll(list);
			log.info("Auto-closed {} grievance ticket(s) (resolved on or before {})", list.size(), cutoff);
		} catch (Exception e) {
			log.warn("autoCloseResolvedGrievanceTickets failed", e);
		}
	}

	private boolean canAccessTicketForAudit(Long empId, Long ticketId) {
		if (empId == null || ticketId == null) {
			return false;
		}
		Optional<GrievanceTicket> opt = grievanceTicketRepository.findById(ticketId);
		if (opt.isEmpty()) {
			return false;
		}
		GrievanceTicket t = opt.get();
		boolean isPrivileged = canViewAllTickets(empId);
		boolean isAssignedUser = empId.equals(t.getAssignedToEmpId());
		boolean isCreator = empId.equals(t.getCreatedByEmpId());
		return isPrivileged || isAssignedUser || isCreator;
	}

	public ServiceResponse getTicketAuditHistory(Long empId, Long ticketId, String field, int page, int size) {
		ServiceResponse response = new ServiceResponse();
		if (ticketId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}
		if (!canAccessTicketForAudit(empId, ticketId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(grievanceAuditService.getTimeline(ticketId, field, page, size));
		return response;
	}

	public ServiceResponse getTicketAuditVersions(Long empId, Long ticketId) {
		ServiceResponse response = new ServiceResponse();
		if (ticketId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}
		if (!canAccessTicketForAudit(empId, ticketId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(grievanceAuditService.listVersions(ticketId));
		return response;
	}

	public ServiceResponse getTicketAuditDiff(Long empId, Long ticketId, int version1, int version2) {
		ServiceResponse response = new ServiceResponse();
		if (ticketId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Ticket id is required.");
			return response;
		}
		if (!canAccessTicketForAudit(empId, ticketId)) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Access denied.");
			return response;
		}
		Optional<GrievanceAuditDiffResponseDTO> diff = grievanceAuditService.diffVersions(ticketId, version1, version2);
		if (diff.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("One or both versions were not found.");
			return response;
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(diff.get());
		return response;
	}
}
