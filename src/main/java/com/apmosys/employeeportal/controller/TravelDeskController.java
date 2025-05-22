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

import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.dto.TravelModeDTO;
import com.apmosys.employeeportal.dto.TravelReasonDTO;
import com.apmosys.employeeportal.service.TravelDeskService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class TravelDeskController {
	
	@Autowired
	private TravelDeskService travelDeskService;
	
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
	
	@GetMapping("/getTravelReason")
	public ServiceResponse getAllTravelReasons() {
	    return travelDeskService.getAllTravelReasons();
	}
	
    @PostMapping("/saveTravelMode")
    public ServiceResponse saveTravelMode(@RequestBody TravelModeDTO travelModeDTO) {
        return travelDeskService.saveTravelMode(travelModeDTO);
    }
    
    
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
}
