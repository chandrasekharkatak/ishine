package com.apmosys.employeeportal.controller;

	import com.apmosys.employeeportal.dto.KresponseDTO;
import com.apmosys.employeeportal.service.KresponseService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.*;

	import java.util.List;

	@RestController
	@RequestMapping("/api/kpi-responses")
	public class KresponseController {

	    @Autowired
	    private KresponseService kresponseService;

	    @PostMapping("/save/{empId}/quarter/{quarterId}")
	    public ServiceResponse saveResponses(@RequestBody List<KresponseDTO> responses, @PathVariable Long empId, @PathVariable Long quarterId) {
	        ServiceResponse response = new ServiceResponse();
	        try {
	          
	            kresponseService.saveResponses(responses, empId, quarterId);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceMessage("Responses saved successfully!");
	        } catch (Exception e) {
	          
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError(e.getMessage());
	            response.setErrorStackTrace(e.toString());
	        }
	        return response;
	    }
	}
	
	

