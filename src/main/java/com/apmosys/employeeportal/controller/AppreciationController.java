package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.service.AppreciationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class AppreciationController {
    @Autowired
    private AppreciationService appreciationService;

    @RequestMapping(value = "/saveAppreciation",method = RequestMethod.POST)
	public ServiceResponse saveAppreciation(@RequestBody AppreciationDTO appreciationDTO){
		ServiceResponse response=appreciationService.saveAppreciation(appreciationDTO);
		return response;
	}   
    
}
