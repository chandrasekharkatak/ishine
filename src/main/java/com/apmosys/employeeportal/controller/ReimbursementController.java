package com.apmosys.employeeportal.controller;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ExpenditureTypeDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixSaveRequestDTO;
import com.apmosys.employeeportal.dto.ReimbursementDashboardFilterDTO;
import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.dto.ReimbursementFinanceTicketActionDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketActorDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketStageActionDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketSubmitRequestDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.dto.TravelModeDTO;
import com.apmosys.employeeportal.service.ReimbursementApprovalMatrixService;
import com.apmosys.employeeportal.service.ReimbursementService;
import com.apmosys.employeeportal.service.ReimbursementTicketService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ReimbursementController {

	@Autowired
	private ReimbursementService reimbursementService;

	@Autowired
	private ReimbursementTicketService reimbursementTicketService;

	@Autowired
	private ReimbursementApprovalMatrixService reimbursementApprovalMatrixService;

	@PostMapping("/fetchReimbursementData")
	public ServiceResponse fetchReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchReimbursementData(reimbursementObj.getEmpId());

	}

	@PostMapping("/fetchReimbursementDataforApproval")
	public ServiceResponse fetchReimbursementDataforApproval(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchReimbursementDataforApproval(reimbursementObj.getEmpId());

	}

	@PostMapping("/saveReimbursementData")
	public ServiceResponse saveReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.saveReimbursementData(reimbursementObj);

	}

	@PostMapping("/updateReimbursementData")
	public ServiceResponse updateReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.updateReimbursementData(reimbursementObj);

	}

	@PostMapping("/revokeReimbursement")
	public ServiceResponse revokeReimbursement(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.revokeReimbursement(reimbursementObj.getRequestId());

	}

	@PostMapping("/approveOrRejectReimbursement")
	public ServiceResponse approveOrRejectReimbursement(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.approveOrRejectReimbursement(reimbursementObj);

	}

	@PostMapping("/uploadFileReimbursement")
	public ServiceResponse uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName, @RequestParam("uploadedBy") Long uploadedBy) {
		ServiceResponse serviceResponse = reimbursementService.uploadFile(file, displayName, uploadedBy);
		return serviceResponse;
	}

	@PostMapping("/fetchTotalReimbursementData")
	public ServiceResponse fetchTotalReimbursementData(@RequestBody ReimbursementDTO reimbursementObj) {
		return reimbursementService.fetchTotalReimbursementData();

	}

	@GetMapping("/getAllDocumentsReimbursmentThroughRequestId")
	public ServiceResponse getAllDocumentsReimbursmentThroughRequestId(
			@RequestParam("requestId") BigInteger requestId) {
		return reimbursementService.getAllDocumentsReimbursmentThroughRequestId(requestId);
	}

	@RequestMapping(value = "/previewDocumentReimbursment", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> previewDocumentReimbursment(
			@RequestBody TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
		return ResponseEntity.ok(reimbursementService.previewDocumentReimbursment(reimbursementRequestDTO));
	}

	@RequestMapping(value = "/updateReimbursementDetailsByAccountsTeam", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> updateReimbursementDetailsByAccountsTeam(
			@RequestBody ReimbursementDTO reimbursementRequestDTO) {
		return ResponseEntity
				.ok(reimbursementService.updateReimbursementDetailsByAccountsTeam(reimbursementRequestDTO));
	}

	@PostMapping("/saveExpenditureType")
	public ServiceResponse createExpenditureType(@RequestBody ExpenditureTypeDTO expenditureTypeDTO) {
		return reimbursementService.saveExpenditureType(expenditureTypeDTO);
	}

	@GetMapping("/onGetExpenditureType")
	public ServiceResponse onGetTravelCass() {
		return reimbursementService.getAllExpenditureType();
	}

	@PostMapping("/saveReimbursementTravelMode")
	public ServiceResponse saveTravelMode(@RequestBody TravelModeDTO travelModeDTO) {
		return reimbursementService.saveTravelMode(travelModeDTO);
	}

	@GetMapping("/getReimbursementTravelMode")
	public ServiceResponse getAllgetTravelModes() {
		return reimbursementService.getAllgetTravelModes();
	}

	@PostMapping("/saveVehicleType")
	public ServiceResponse createVehicleType(@RequestBody TravelModeDTO travelModeDTO) {
		return reimbursementService.saveVehicleType(travelModeDTO);
	}
	
	@GetMapping("/onGetVehicleType")
	public ServiceResponse onGetVehicleType() {
		return reimbursementService.getAllVehicleType();
	}
	
	
	@PostMapping("/saveFoodType")
	public ServiceResponse createFoodType(@RequestBody TravelModeDTO travelModeDTO) {
		return reimbursementService.saveFoodType(travelModeDTO);
	}
	
	@GetMapping("/onGetFoodType")
	public ServiceResponse onGetFoodType() {
		return reimbursementService.getAllFoodType();
	}

	@PostMapping("/deleteExpenditureType")
	public ServiceResponse deleteExpenditureType(@RequestBody ExpenditureTypeDTO dto) {
		return reimbursementService.deleteExpenditureType(dto.getId());
	}

	@PostMapping("/deleteReimbursementTravelMode")
	public ServiceResponse deleteReimbursementTravelMode(@RequestBody TravelModeDTO dto) {
		return reimbursementService.deleteTravelMode(dto.getTravelModeId());
	}

	@PostMapping("/deleteVehicleType")
	public ServiceResponse deleteVehicleType(@RequestBody TravelModeDTO dto) {
		return reimbursementService.deleteVehicleType(dto.getVehicleTypeId());
	}

	@PostMapping("/deleteFoodType")
	public ServiceResponse deleteFoodType(@RequestBody TravelModeDTO dto) {
		return reimbursementService.deleteFoodType(dto.getFoodTypeId());
	}

	@PostMapping("/saveReimbursementTicket")
	public ServiceResponse saveReimbursementTicket(@RequestBody ReimbursementTicketSubmitRequestDTO body) {
		return reimbursementTicketService.submitTicket(body);
	}

	@PostMapping("/fetchReimbursementClaimProjectOptions")
	public ServiceResponse fetchReimbursementClaimProjectOptions(@RequestBody ReimbursementTicketActorDTO body) {
		return reimbursementTicketService.fetchClaimProjectOptions(body);
	}

	@PostMapping("/fetchReimbursementClientsFromMaster")
	public ServiceResponse fetchReimbursementClientsFromMaster(
			@RequestBody(required = false) java.util.Map<String, Object> body) {
		return reimbursementTicketService.fetchReimbursementClientsFromMaster();
	}

	@PostMapping("/fetchMyReimbursementTickets")
	public ServiceResponse fetchMyReimbursementTickets(@RequestBody ReimbursementTicketActorDTO body) {
		return reimbursementTicketService.fetchMyTickets(body.getEmpId());
	}

	@PostMapping("/fetchReimbursementTicketsForApproval")
	public ServiceResponse fetchReimbursementTicketsForApproval(@RequestBody ReimbursementTicketActorDTO body) {
		return reimbursementTicketService.fetchTicketsForApproval(body);
	}

	@PostMapping("/fetchReimbursementTicketsAssignedAll")
	public ServiceResponse fetchReimbursementTicketsAssignedAll(@RequestBody ReimbursementTicketActorDTO body) {
		return reimbursementTicketService.fetchAllTicketsAssignedToActor(body);
	}

	@PostMapping("/processReimbursementTicketHod")
	public ServiceResponse processReimbursementTicketHod(@RequestBody ReimbursementTicketStageActionDTO body) {
		return reimbursementTicketService.processHodAction(body);
	}

	@PostMapping("/processReimbursementTicketHr")
	public ServiceResponse processReimbursementTicketHr(@RequestBody ReimbursementTicketStageActionDTO body) {
		return reimbursementTicketService.processHrAction(body);
	}

	@PostMapping("/processReimbursementTicketFinance")
	public ServiceResponse processReimbursementTicketFinance(@RequestBody ReimbursementFinanceTicketActionDTO body) {
		return reimbursementTicketService.processFinanceAction(body);
	}

	@PostMapping("/fetchReimbursementTicketAuditByTicketId")
	public ServiceResponse fetchReimbursementTicketAuditByTicketId(
			@RequestBody(required = false) java.util.Map<String, Long> body) {
		Long ticketId = body != null ? body.get("ticketId") : null;
		if (ticketId == null) {
			ServiceResponse r = new ServiceResponse();
			r.setServiceStatus(ServiceResponse.STATUS_FAIL);
			r.setServiceError("ticketId is required.");
			return r;
		}
		return reimbursementTicketService.fetchAuditLog(ticketId);
	}

	@PostMapping("/fetchReimbursementDashboard")
	public ServiceResponse fetchReimbursementDashboard(@RequestBody(required = false) ReimbursementDashboardFilterDTO filter) {
		return reimbursementTicketService.dashboard(filter != null ? filter : new ReimbursementDashboardFilterDTO());
	}

	@GetMapping("/getAllReimbursementApprovalMatrices")
	public ServiceResponse getAllReimbursementApprovalMatrices() {
		return reimbursementApprovalMatrixService.getAllApprovalMatrices();
	}

	@GetMapping("/resolveReimbursementApprovalMatrixForEmployee")
	public ServiceResponse resolveReimbursementApprovalMatrixForEmployee(@RequestParam Long empId) {
		return reimbursementApprovalMatrixService.resolveForEmployee(empId);
	}

	@PostMapping("/saveAllReimbursementApprovalMatrices")
	public ServiceResponse saveAllReimbursementApprovalMatrices(
			@RequestBody ReimbursementApprovalMatrixSaveRequestDTO request) {
		return reimbursementApprovalMatrixService.saveAllApprovalMatrices(request);
	}

	@PostMapping("/saveReimbursementApprovalMatrix")
	public ServiceResponse saveReimbursementApprovalMatrix(
			@RequestBody ReimbursementApprovalMatrixSaveRequestDTO request) {
		return reimbursementApprovalMatrixService.saveApprovalMatrix(request);
	}

	@PostMapping("/deleteReimbursementApprovalMatrix")
	public ServiceResponse deleteReimbursementApprovalMatrix(@RequestBody ReimbursementApprovalMatrixDTO dto) {
		return reimbursementApprovalMatrixService.deleteApprovalMatrix(dto != null ? dto.getMatrixId() : null);
	}

}
