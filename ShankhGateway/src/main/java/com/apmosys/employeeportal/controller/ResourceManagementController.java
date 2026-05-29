package com.apmosys.employeeportal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.IshineToPoRequestDTO;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.ProjectViewResolveBulkItemDTO;
import com.apmosys.employeeportal.dto.ProjectViewResolveBulkRequestDTO;
import com.apmosys.employeeportal.dto.ProjectViewResolveResponseDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
import com.apmosys.employeeportal.dto.TimeSheetRequestDto;
import com.apmosys.employeeportal.service.PoSyncOrchestratorService;
import com.apmosys.employeeportal.service.ProjectHierarchyResolverService;
import com.apmosys.employeeportal.service.ResourceManagementService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ResourceManagementController {

	@Autowired
	ResourceManagementService resourceManagementService;

	@Autowired
	PoSyncOrchestratorService poSyncOrchestratorService;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@Autowired
	ProjectHierarchyResolverService projectHierarchyResolverService;

	@PostMapping("/poCrudOperationsInIshineNew")
	public ServiceResponse poCrudOperationsInIshineNew(HttpServletRequest httpRequest,
			@RequestBody ProjectPoMappingWithResourceDTO poPortalProjects) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.poCrudOperationsInIshineNew(poPortalProjects);
	}

	@PostMapping("/renewPoInIshineNew")
	 public ServiceResponse renewPoInIshineNew(HttpServletRequest httpRequest, @Valid @RequestBody RenewedPoSyncDto dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.renewPoInIshineNew(dto);
	 }

	@PostMapping("/deletePoInIshineNew")
	 public ServiceResponse deletePoInIshineNew(HttpServletRequest httpRequest,@Valid @RequestBody  DeletedPoSyncDTO dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.deletePoInIshineNew(dto);
	 }

	@PostMapping("/linkPoInIshineNew")
	public ServiceResponse linkPoInIshineNew(HttpServletRequest httpRequest, @RequestBody IshineLinkProjectDto dto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.linkPoInIshineNew(dto);
	}

	@PostMapping("/updateRmDetailsInPo")
	public ServiceResponse updateRmDetailsInPo(HttpServletRequest httpRequest, @RequestBody RmUpdateSyncDto dto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.updateRmOdPos(dto);
	}
	
	@GetMapping("/resolveProjectViewId")
	public ServiceResponse resolveProjectViewId(@RequestParam String projectViewId) {
		ServiceResponse response = new ServiceResponse();
		String resolved = projectHierarchyResolverService.resolveProjectViewId(projectViewId);
		String resolvedName = projectHierarchyResolverService.resolveProjectNameFromProjectViewId(projectViewId);
		ProjectViewResolveResponseDTO dto = new ProjectViewResolveResponseDTO(projectViewId, resolved,
				resolved != null && !resolved.equals(projectViewId), resolvedName);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dto);
		return response;
	}
	
	@PostMapping("/resolveProjectViewIds")
	public ServiceResponse resolveProjectViewIds(@RequestBody ProjectViewResolveBulkRequestDTO request) {
		ServiceResponse response = new ServiceResponse();
		Map<String, ProjectViewResolveBulkItemDTO> out = new HashMap<>();
		
		if (request != null && request.getProjectViewIds() != null) {
			for (String original : request.getProjectViewIds()) {
				if (original == null || original.trim().isEmpty()) {
					continue;
				}
				String resolved = projectHierarchyResolverService.resolveProjectViewId(original);
				boolean redirected = resolved != null && !Objects.equals(resolved, original);
				String resolvedName = projectHierarchyResolverService.resolveProjectNameFromProjectViewId(original);
				out.put(original, new ProjectViewResolveBulkItemDTO(resolved, redirected, resolvedName));
			}
		}
		
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(out);
		return response;
	}

	@PostMapping("/updateAddressInPos")
	 public ServiceResponse updateAddressInPos(HttpServletRequest httpRequest,
			 @Valid @RequestBody PoClientAddressUpdateDTO dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.updateAddressInPos(dto);
	 }

	@PostMapping(value = "/sendTimesheetDetailsToShankh")
	public ServiceResponse sendTimesheetDetailsToShankh(HttpServletRequest httpRequest,
			@RequestBody TimeSheetRequestDto payloadDTO) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.sendTimesheetDetailsToShankh(payloadDTO);
	}

	@PostMapping("/getResourceCountFromProjectId")
	public ServiceResponse getResourceCountFromProjectId(HttpServletRequest httpRequest,
			@RequestBody List<Long> proIds) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.getResourceCountFromProjectId(proIds);
	}

	@PostMapping("/getDocumentDataByDocIdForPO")
	public ServiceResponse getDocumentDataByDocId(HttpServletRequest httpRequest, @RequestBody Long docId)
			throws Exception {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.getDocumentDataByDocId(docId);
	}

	@PostMapping("/getResourceCountFromPoId")
	public ServiceResponse getResourceCountFromPoId(HttpServletRequest httpRequest, @RequestBody List<Long> poIds)
			throws Exception {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.getResourceCountFromPoId(poIds);
	}

	@GetMapping("/getAllApprovedPoWithTimesheet")
	public ServiceResponse getAllApprovedPoWithTimesheet(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.getAllApprovedPoWithTimesheet();
	}

	@GetMapping("/healthCheck")
	public String healthCheck(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return "IShine is online...";
	}
	
	@GetMapping("/test")
	public String test(HttpServletRequest httpRequest) {
		return "IShine is online...";
	}

	@PostMapping("/ishineToPoEmpDetails")
	public ServiceResponse ishineToPoEmpDetails(HttpServletRequest httpRequest, @RequestBody IshineToPoRequestDTO ishineToPoRequest) {
//		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.ishineToPoEmpDetails(ishineToPoRequest);
	}
}
