package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import com.apmosys.employeeportal.dto.GrievanceIssueScenarioDTO;
import com.apmosys.employeeportal.dto.GrievanceProofDownloadDTO;
import com.apmosys.employeeportal.dto.GrievanceFeedbackDTO;
import com.apmosys.employeeportal.dto.GrievanceTicketRequestDTO;
import com.apmosys.employeeportal.dto.GrievanceTicketUpdateDTO;
import com.apmosys.employeeportal.service.GrievanceService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api/grievance")
public class GrievanceController {

	@Autowired
	private GrievanceService grievanceService;

	@PostMapping(value = "/createTicket", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse createTicket(
			@RequestPart("subject") String subject,
			@RequestPart(value = "category", required = false) String category,
			@RequestPart(value = "subCategory", required = false) String subCategory,
			@RequestPart(value = "ticketFeature", required = false) String ticketFeature,
			@RequestPart(value = "issueScenario", required = false) String issueScenario,
			@RequestPart("description") String description,
			@RequestPart(value = "priority", required = false) String priority,
			@RequestParam(value = "proofFiles", required = false) List<MultipartFile> proofFiles,
			@RequestParam(value = "proofFile", required = false) MultipartFile proofFile) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		GrievanceTicketRequestDTO requestDTO = new GrievanceTicketRequestDTO();
		requestDTO.setSubject(subject);
		requestDTO.setCategory(category);
		requestDTO.setSubCategory(subCategory);
		requestDTO.setTicketFeature(ticketFeature);
		requestDTO.setIssueScenario(issueScenario);
		requestDTO.setDescription(description);
		requestDTO.setPriority(priority);
		List<MultipartFile> files = new ArrayList<>();
		if (proofFiles != null) {
			for (MultipartFile f : proofFiles) {
				if (f != null && !f.isEmpty()) {
					files.add(f);
				}
			}
		}
		if (files.isEmpty() && proofFile != null && !proofFile.isEmpty()) {
			files.add(proofFile);
		}
		return grievanceService.createTicket(empId, requestDTO, files);
	}

	@GetMapping("/myTickets")
	public ServiceResponse getMyTickets(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "createdOn") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getMyTickets(empId, page, size, sortBy, sortDir);
	}

	@GetMapping("/myTickets/export")
	public ServiceResponse exportMyTickets() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.exportMyTickets(empId);
	}

	@GetMapping("/allTickets")
	public ServiceResponse getAllTickets(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "createdOn") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getAllTickets(empId, page, size, sortBy, sortDir);
	}

	@GetMapping("/allTickets/export")
	public ServiceResponse exportAllTickets() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.exportAllTickets(empId);
	}

	@GetMapping("/assignedTickets")
	public ServiceResponse getAssignedTickets(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "createdOn") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getAssignedToMeTickets(empId, page, size, sortBy, sortDir);
	}

	@GetMapping("/assignedTickets/export")
	public ServiceResponse exportAssignedTickets() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.exportAssignedToMeTickets(empId);
	}

	@GetMapping("/developmentUsers")
	public ServiceResponse getDevelopmentUsers() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getDevelopmentUsers(empId);
	}

	@GetMapping("/categories")
	public ServiceResponse getTicketCategories() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketCategories();
	}

	@GetMapping("/subCategories")
	public ServiceResponse getTicketSubCategories(@RequestParam("category") String category) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketSubCategoriesForTab(category);
	}

	@GetMapping("/ticketFeatures")
	public ServiceResponse getTicketFeatures(
			@RequestParam("category") String category,
			@RequestParam(value = "subCategory", required = false) String subCategory) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketFeaturesForTabAndSubCategory(category, subCategory);
	}

	@GetMapping("/issueScenarios")
	public ServiceResponse getIssueScenarios(
			@RequestParam("category") String category,
			@RequestParam("subCategory") String subCategory,
			@RequestParam("ticketFeature") String ticketFeature) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getIssueScenariosForSelection(category, subCategory, ticketFeature);
	}

	@GetMapping("/admin/issueScenarios/eligible")
	public ServiceResponse issueScenarioAdminEligible() {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(Boolean.valueOf(grievanceService.canManageIssueScenarios(empId)));
		return response;
	}

	@GetMapping("/admin/issueScenarios")
	public ServiceResponse listAdminIssueScenarios(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "tabKey") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.listAdminIssueScenarios(empId, page, size, sortBy, sortDir);
	}

	@PostMapping("/admin/issueScenarios")
	public ServiceResponse createAdminIssueScenario(@RequestBody GrievanceIssueScenarioDTO dto) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.createAdminIssueScenario(empId, dto);
	}

	@PutMapping("/admin/issueScenarios/{scenarioId}")
	public ServiceResponse updateAdminIssueScenario(@PathVariable Long scenarioId,
			@RequestBody GrievanceIssueScenarioDTO dto) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.updateAdminIssueScenario(empId, scenarioId, dto);
	}

	@DeleteMapping("/admin/issueScenarios/{scenarioId}")
	public ServiceResponse deactivateAdminIssueScenario(@PathVariable Long scenarioId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.deactivateAdminIssueScenario(empId, scenarioId);
	}

	@GetMapping("/ticket/{ticketId}")
	public ServiceResponse getTicketById(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketById(empId, ticketId);
	}

	@GetMapping("/ticket/{ticketId}/proofDocuments")
	public ServiceResponse listTicketProofDocuments(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.listTicketProofDocuments(empId, ticketId);
	}

	/** Paginated audit timeline (newest versions first). Optional {@code field} filters to entries touching that field. */
	@GetMapping("/tickets/{ticketId}/history")
	public ServiceResponse getTicketAuditHistory(
			@PathVariable Long ticketId,
			@RequestParam(required = false) String field,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketAuditHistory(empId, ticketId, field, page, size);
	}

	@GetMapping("/tickets/{ticketId}/versions")
	public ServiceResponse getTicketAuditVersions(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketAuditVersions(empId, ticketId);
	}

	@GetMapping("/tickets/{ticketId}/diff")
	public ServiceResponse getTicketAuditDiff(
			@PathVariable Long ticketId,
			@RequestParam int version1,
			@RequestParam int version2) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.getTicketAuditDiff(empId, ticketId, version1, version2);
	}

	@PostMapping(value = "/updateTicket", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse updateTicket(
			@RequestPart("payload") GrievanceTicketUpdateDTO updateDTO,
			@RequestPart(value = "resolutionDocument", required = false) MultipartFile resolutionDocument) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.updateTicket(empId, updateDTO, resolutionDocument);
	}

	@PostMapping("/submitFeedback")
	public ServiceResponse submitFeedback(@RequestBody GrievanceFeedbackDTO feedbackDTO) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.submitFeedback(empId, feedbackDTO);
	}

	@PostMapping("/reopenTicket/{ticketId}")
	public ServiceResponse reopenTicket(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.reopenTicket(empId, ticketId);
	}

	@PostMapping("/withdrawTicket/{ticketId}")
	public ServiceResponse withdrawTicket(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return unauthorizedResponse();
		}
		return grievanceService.withdrawTicket(empId, ticketId);
	}

	@GetMapping("/downloadProof/{ticketId}")
	public ResponseEntity<Resource> downloadProof(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return ResponseEntity.status(401).build();
		}
		GrievanceProofDownloadDTO dl = grievanceService.resolveProofDownload(empId, ticketId, 0L);
		if (dl == null || dl.getResource() == null || !dl.getResource().exists()) {
			return ResponseEntity.notFound().build();
		}
		String filename = dl.getDownloadFileName() == null ? "proof" : dl.getDownloadFileName();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.body(dl.getResource());
	}

	@GetMapping("/downloadProofDocument/{ticketId}/{documentId}")
	public ResponseEntity<Resource> downloadProofDocument(@PathVariable Long ticketId, @PathVariable Long documentId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return ResponseEntity.status(401).build();
		}
		GrievanceProofDownloadDTO dl = grievanceService.resolveProofDownload(empId, ticketId, documentId);
		if (dl == null || dl.getResource() == null || !dl.getResource().exists()) {
			return ResponseEntity.notFound().build();
		}
		String filename = dl.getDownloadFileName() == null ? "proof" : dl.getDownloadFileName();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.body(dl.getResource());
	}

	@GetMapping("/downloadResolutionDocument/{ticketId}")
	public ResponseEntity<Resource> downloadResolutionDocument(@PathVariable Long ticketId) {
		Long empId = getCurrentEmpId();
		if (empId == null) {
			return ResponseEntity.status(401).build();
		}
		Resource file = grievanceService.getResolutionDocument(empId, ticketId);
		if (file == null || !file.exists()) {
			return ResponseEntity.notFound().build();
		}
		String filename = file.getFilename() == null ? "resolution-document" : file.getFilename();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.body(file);
	}

	private Long getCurrentEmpId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}
		try {
			return Long.valueOf(authentication.getName());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private ServiceResponse unauthorizedResponse() {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("Unauthorized request.");
		return response;
	}
}
