package com.apmosys.employeeportal.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
import com.apmosys.employeeportal.dto.TimeSheetRequestDto;
import com.apmosys.employeeportal.service.PoSyncOrchestratorService;
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

	@PostMapping("/poCrudOperationsInIshineNew")
	public ServiceResponse poCrudOperationsInIshineNew(HttpServletRequest httpRequest,
			@RequestBody ProjectPoMappingWithResourceDTO poPortalProjects) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.poCrudOperationsInIshineNew(poPortalProjects);
	}

	@PostMapping("/renewPoInIshineNew")
	public ServiceResponse renewPoInIshineNew(HttpServletRequest httpRequest, @RequestBody RenewedPoSyncDto dto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.renewPoInIshineNew(dto);
	}

	@PostMapping("/deletePoInIshineNew")
	public ServiceResponse deletePoInIshineNew(HttpServletRequest httpRequest, @RequestBody DeletedPoSyncDTO dto) {
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

	@PostMapping("/updateAddressInPos")
	public ServiceResponse updateClientAddressIdOfPos(HttpServletRequest httpRequest,
			@RequestBody PoClientAddressUpdateDTO dto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poSyncOrchestratorService.updateClientAddressIdOfPos(dto);
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
		return resourceManagementService.getAllApprovedPoWithTimesheet();
	}

	@GetMapping("/healthCheck")
	public String healthCheck(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return "IShine is online...";
	}
}
