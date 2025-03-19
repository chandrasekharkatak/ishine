package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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
	public ServiceResponse fetchTravelData() {
		
		return null;
		
	}
	
	@PostMapping("/saveTravelData")
	public ServiceResponse saveTravelData(TravelDeskDTO travelData) {
		return travelDeskService.saveTravelData(travelData);
		
	}
	
	@PostMapping("/updateTravelData")
	public ServiceResponse updateTravelData() {
		return null;
		
	}
	
	@PostMapping("/revokeTravel")
	public ServiceResponse revokeTravel() {
		return null;
		
	}
	
	@PostMapping("/approveOrRejectTravel")
	public ServiceResponse approveOrRejectTravel() {
		return null;
		
	}
}
