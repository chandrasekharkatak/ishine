package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.DomainDTO;
import com.apmosys.employeeportal.service.DomainService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class DomainController {
	
	@Autowired
	DomainService domainService;
	
	@JobRoleAccess(featureIds = {41})
	@RequestMapping(value="/createDomain" , method = RequestMethod.POST)
	public ServiceResponse createDomain(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.createDomain(domainDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {3,41})
	@RequestMapping(value="/getAllDomain" , method = RequestMethod.GET)
	public ServiceResponse getAllDomain() {		
		
		ServiceResponse response =	domainService.getAllDomain();
		return response;
	}
	@JobRoleAccess(featureIds = {41})
	@RequestMapping(value="/getDomainSpecializationByDomainId" , method = RequestMethod.POST)
	public ServiceResponse getDomainSpecializationByDomainId(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.getDomainSpecializationByDomainId(domainDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {41})
	@RequestMapping(value="/updateDomain" , method = RequestMethod.POST)
	public ServiceResponse updateDomain(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.updateDomain(domainDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {41})
	@RequestMapping(value="/deleteDomain" , method = RequestMethod.POST)
	public ServiceResponse deleteDomain(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.deleteDomain(domainDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {41})
	@RequestMapping(value="/checkDomainName" , method = RequestMethod.POST)
	public ServiceResponse checkDomainName(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.checkDomainName(domainDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {3,41,8})
	@RequestMapping(value="/getDomainSpecialization" , method = RequestMethod.POST)
	public ServiceResponse getDomainSpecialization(@RequestBody DomainDTO domainDTO) {		
		
		ServiceResponse response =	domainService.getDomainSpecialization(domainDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {8})
	@RequestMapping(value="/getDomainSpecializationByEmpId" , method = RequestMethod.POST)
	public ServiceResponse getDomainSpecializationByEmpId(@RequestBody DomainDTO domainDTO) {	
		
		ServiceResponse response =	domainService.getDomainSpecializationByEmpId(domainDTO);
		return response;
	}

}
