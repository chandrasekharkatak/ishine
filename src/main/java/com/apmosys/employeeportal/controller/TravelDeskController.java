package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.TravelDeskDTO;
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
}
