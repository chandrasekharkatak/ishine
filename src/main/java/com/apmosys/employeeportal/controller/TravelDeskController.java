package com.apmosys.employeeportal.controller;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.CityDTO;
import com.apmosys.employeeportal.dto.HotelCategoryDTO;
import com.apmosys.employeeportal.dto.HotelSubCategoryDTO;
import com.apmosys.employeeportal.dto.TravelClassRequest;
import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.dto.TravelDeskDashboardFilterDTO;
import com.apmosys.employeeportal.dto.TravelModeDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixSaveRequestDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketActorDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketStageActionDTO;
import com.apmosys.employeeportal.dto.TravelDeskTicketSubmitRequestDTO;
import com.apmosys.employeeportal.dto.TravelReasonDTO;
import com.apmosys.employeeportal.service.TravelApprovalMatrixService;
import com.apmosys.employeeportal.service.TravelDeskService;
import com.apmosys.employeeportal.service.TravelDeskTicketService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class TravelDeskController {
	
	@Autowired
	private TravelDeskService travelDeskService;

	@Autowired
	private TravelApprovalMatrixService travelApprovalMatrixService;

	@Autowired
	private TravelDeskTicketService travelDeskTicketService;
	
	@PostMapping("/fetchTravelData")
	public ServiceResponse fetchTravelData(@RequestBody TravelDeskDTO travelData) {
		
		return travelDeskService.fetchUserTravel(travelData.getEmployeeId());
		 
	}
	
	@PostMapping("/fetchTravelDataForApproval")
	public ServiceResponse fetchTravelDataForApproval(@RequestBody TravelDeskDTO travelData) {
		
		return travelDeskService.fetchUserTravelForApproval(travelData.getEmployeeId());
		 
	}
	
	
	
	@PostMapping("/saveTravelData")
	public ServiceResponse saveTravelData(@RequestBody TravelDeskDTO travelData) {
		return travelDeskService.saveTravelData(travelData);
		
	}
	
	@PostMapping("/updateTravelData")
	public ServiceResponse updateTravelData(@RequestBody TravelDeskDTO travelData) {
		return travelDeskService.updateTravelData(travelData);
		
	}
	
	@PostMapping("/revokeTravel")
	public ServiceResponse revokeTravel(@RequestBody TravelDeskDTO travelData) {
		return travelDeskService.revokeTravel(travelData.getRequestId());
		
	}
	
	@PostMapping("/approveOrRejectTravel")
	public ServiceResponse approveOrRejectTravel(@RequestBody TravelDeskDTO travelData) {
		return travelDeskService.approveRejectTravel(travelData);
		
	}
	
	@PostMapping("/uploadFile")
	public ServiceResponse uploadFile(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy
			) {
		ServiceResponse serviceResponse = travelDeskService.uploadFile(file, displayName, uploadedBy);
		return serviceResponse;
	}
	
	@PostMapping("/totalTravelData")
	public ServiceResponse totalTravelData(@RequestBody TravelDeskDTO travelData) {
		
		return travelDeskService.totalTravelData();
		 
	}
	@GetMapping("/getAllDocumentsThroughRequestId")
	public ServiceResponse getAllDocsThroughReqId(@RequestParam("requestId") BigInteger requestId) {
	    return travelDeskService.getAllDocsThroughReqId(requestId);
	}
	
	@PostMapping("/travel-reason/create")
	public ServiceResponse createTravelReason(@RequestBody TravelReasonDTO travelReasonDTO) {
	    return travelDeskService.saveTravelReason(travelReasonDTO);
	}

	@PostMapping("/deleteTravelReason")
	public ServiceResponse deleteTravelReason(@RequestBody TravelReasonDTO dto) {
	    return travelDeskService.deleteTravelReason(dto.getId());
	}
	
	@JobRoleAccess(featureIds = {55,59})
	@GetMapping("/getTravelReason")
	public ServiceResponse getAllTravelReasons() {
	    return travelDeskService.getAllTravelReasons();
	}
	
    @PostMapping("/saveTravelMode")
    public ServiceResponse saveTravelMode(@RequestBody TravelModeDTO travelModeDTO) {
        return travelDeskService.saveTravelMode(travelModeDTO);
    }

    @PostMapping("/deleteTravelMode")
    public ServiceResponse deleteTravelMode(@RequestBody TravelModeDTO dto) {
        return travelDeskService.deleteTravelMode(dto.getTravelModeId());
    }
    
    @JobRoleAccess(featureIds = {55,59,60,61})
	@GetMapping("/getTravelMode")
	public ServiceResponse getAllgetTravelModes() {
	    return travelDeskService.getAllgetTravelModes();
	}
	
	@PostMapping("/uploadTicket")
	public ServiceResponse uploadTicket(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy,
			@RequestParam("requestId") BigInteger requestId
			) {
		ServiceResponse serviceResponse = travelDeskService.uploadTicket(file, displayName, uploadedBy,requestId);
		return serviceResponse;
	}
    @PostMapping("/saveTravelClass")
    public ServiceResponse saveTravelClass(@RequestBody TravelClassRequest travelClassDTO) {
        return travelDeskService.saveTravelClass(travelClassDTO);
    }

    @PostMapping("/deleteTravelClass")
    public ServiceResponse deleteTravelClass(@RequestBody TravelClassRequest dto) {
        return travelDeskService.deleteTravelClass(dto.getTravelClassId());
    }
    
    @PostMapping("/getTravelModeByReason")
    public ServiceResponse getTravelModeByReason(@RequestBody String travelReason) {
        return travelDeskService.getTravelModeByReason(travelReason);
    }
    
    @PostMapping("/saveHotelCategory")
    public ServiceResponse saveHotelCategory(@RequestBody HotelCategoryDTO dto) {
        return travelDeskService.saveHotelCategory(dto);
    }

    @PostMapping("/deleteHotelCategory")
    public ServiceResponse deleteHotelCategory(@RequestBody HotelCategoryDTO dto) {
        return travelDeskService.deleteHotelCategory(dto.getId());
    }
    @JobRoleAccess(featureIds = {55,59})
	@GetMapping("/getHotelCategory")
	public ServiceResponse getHotelCategory() {
	    return travelDeskService.getHotelCategory();
	}
	
	@PostMapping("/saveHotelSubCategory")
	public ServiceResponse saveHotelSubCategory(@RequestBody HotelSubCategoryDTO hotelSubCategoryDTO) {
	    return travelDeskService.saveHotelSubCategory(hotelSubCategoryDTO);
	}

	@PostMapping("/deleteHotelSubCategory")
	public ServiceResponse deleteHotelSubCategory(@RequestBody HotelSubCategoryDTO dto) {
	    return travelDeskService.deleteHotelSubCategory(dto.getId());
	}
	
	@JobRoleAccess(featureIds = {55,59})
	@GetMapping("/getHotelSubCategory")
	public ServiceResponse getHotelSubCategory() {
	    return travelDeskService.getHotelSubCategory();
	}
	
	@PostMapping("/saveCity")
	public ServiceResponse saveCity(@RequestBody CityDTO cityDTO) {
	    return travelDeskService.saveCity(cityDTO);
	}

	@PostMapping("/deleteCity")
	public ServiceResponse deleteCity(@RequestBody CityDTO dto) {
	    return travelDeskService.deleteCity(dto.getCityId());
	}
	
    @PostMapping("/getTravelClassByMode")
    public ServiceResponse getTravelClassByMode(@RequestBody Long travelModeId) {
        return travelDeskService.getTravelClassByTravelModeId(travelModeId);
    }
    
    @PostMapping("/getCityBySubCategory")
    public ServiceResponse getCityBySubCategory(@RequestBody String travelReason) {
        return travelDeskService.getCityBySubCategory(travelReason);
    }

    @JobRoleAccess(featureIds = {55, 59})
    @PostMapping("/getHotelSubCategoryByCategory")
    public ServiceResponse getHotelSubCategoryByCategory(@RequestBody String hotelCategoryName) {
        return travelDeskService.getHotelSubCategoryByCategory(hotelCategoryName);
    }

    @JobRoleAccess(featureIds = {55, 59})
    @PostMapping("/getCitiesByHotelSubCategoryId")
    public ServiceResponse getCitiesByHotelSubCategoryId(@RequestBody Long hotelSubCategoryId) {
        return travelDeskService.getCitiesByHotelSubCategoryId(hotelSubCategoryId);
    }
    
    @JobRoleAccess(featureIds = {55,59})
	@GetMapping("/getCity")
	public ServiceResponse getCity() {
	    return travelDeskService.getCity();
	}
	
	
	@GetMapping("/onGetTravelCass")
	public ServiceResponse onGetTravelCass() {
	    return travelDeskService.onGetTravelCass();
	}

	@PostMapping("/uploadKycDocument")
	public ServiceResponse uploadKycDocument(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			@RequestParam("displayName") String displayName,
			@RequestParam("uploadedBy") Long uploadedBy
			) {
		ServiceResponse serviceResponse = travelDeskService.uploadKycDocument(file, displayName, uploadedBy);
		return serviceResponse;
	}

	@GetMapping("/getAllTravelApprovalMatrices")
	public ServiceResponse getAllTravelApprovalMatrices() {
		return travelApprovalMatrixService.getAllApprovalMatrices();
	}

	@GetMapping("/resolveTravelApprovalMatrixForEmployee")
	public ServiceResponse resolveTravelApprovalMatrixForEmployee(@RequestParam Long empId) {
		return travelApprovalMatrixService.resolveForEmployee(empId);
	}

	@PostMapping("/saveTravelApprovalMatrix")
	public ServiceResponse saveTravelApprovalMatrix(@RequestBody TravelApprovalMatrixSaveRequestDTO request) {
		return travelApprovalMatrixService.saveApprovalMatrix(request);
	}

	@PostMapping("/deleteTravelApprovalMatrix")
	public ServiceResponse deleteTravelApprovalMatrix(@RequestBody TravelApprovalMatrixDTO dto) {
		return travelApprovalMatrixService.deleteApprovalMatrix(dto != null ? dto.getMatrixId() : null);
	}

	@PostMapping("/saveTravelDeskTicket")
	public ServiceResponse saveTravelDeskTicket(@RequestBody TravelDeskTicketSubmitRequestDTO body) {
		return travelDeskTicketService.submitTicket(body);
	}

	@PostMapping("/fetchMyTravelDeskTickets")
	public ServiceResponse fetchMyTravelDeskTickets(@RequestBody TravelDeskTicketSubmitRequestDTO body) {
		return travelDeskTicketService.fetchMyTickets(body != null ? body.getEmpId() : null);
	}

	@PostMapping("/fetchTravelDeskTicketsForApproval")
	public ServiceResponse fetchTravelDeskTicketsForApproval(@RequestBody TravelDeskTicketActorDTO body) {
		return travelDeskTicketService.fetchTicketsForApproval(body);
	}

	@PostMapping("/fetchTravelDeskTicketsAssignedAll")
	public ServiceResponse fetchTravelDeskTicketsAssignedAll(@RequestBody TravelDeskTicketActorDTO body) {
		return travelDeskTicketService.fetchAllTicketsAssignedToActor(body);
	}

	@PostMapping("/fetchTravelDeskDashboard")
	public ServiceResponse fetchTravelDeskDashboard(@RequestBody(required = false) TravelDeskDashboardFilterDTO body) {
		return travelDeskTicketService.dashboard(body != null ? body : new TravelDeskDashboardFilterDTO());
	}

	@PostMapping("/processTravelDeskTicketApproval")
	public ServiceResponse processTravelDeskTicketApproval(@RequestBody TravelDeskTicketStageActionDTO body) {
		return travelDeskTicketService.processApprovalAction(body);
	}
}
