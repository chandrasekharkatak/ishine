package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.service.ActivityTemplateService;
import com.apmosys.employeeportal.service.ApiSourceService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ApiSourceController {
	
	@Autowired
	ApiSourceService apiSourceService;

	@RequestMapping( value = "/getAllApiList", method = RequestMethod.GET)
    public ResponseEntity<List<ApiSourceDTO>> getAllApiList() {
        List<ApiSourceDTO> response = apiSourceService.getAllApiList();
        return ResponseEntity.ok(response);
    }

}
