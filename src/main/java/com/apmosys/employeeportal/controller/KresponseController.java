package com.apmosys.employeeportal.controller;

	import com.apmosys.employeeportal.dto.KresponseDTO;
import com.apmosys.employeeportal.service.KresponseService;

	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.*;

	import java.util.List;

	@RestController
	@RequestMapping("/api/kpi-responses")
	public class KresponseController {

	    @Autowired
	    private KresponseService kresponseService;

	    @PostMapping("/save/{empId}/{quarterId}")
	    public ResponseEntity<String> saveResponses(@RequestBody List<KresponseDTO> responses,@PathVariable Long empId,@PathVariable Long quarterId) {
	        kresponseService.saveResponses(responses,empId,quarterId);
	        return ResponseEntity.ok("Responses saved successfully!");
	    }
	}
	
	

