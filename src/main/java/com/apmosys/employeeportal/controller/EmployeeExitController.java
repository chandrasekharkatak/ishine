package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeExitDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.service.EmployeeExitService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeExitController {
	
	@Autowired
	EmployeeExitService employeeExitService;
	
                               /* Employee Exit Controller*/

	@RequestMapping(value = "/getEmployeeResignationDetails" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeResignationDetails(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.getEmployeeResignationDetails(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateEmployeeResignationDetails" ,method = RequestMethod.POST)
	public ServiceResponse updateEmployeeResignationDetails(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.updateEmployeeResignationDetails(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeExitAssetDetails" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeExitAssetDetails(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.getEmployeeExitAssetDetails(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeInfo" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeInfo(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.getEmployeeInfo(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/setDeptHeadConcent" ,method = RequestMethod.POST)
	public ServiceResponse setDeptHeadConcent(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.setDeptHeadConcent(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/setExitInterviewResponseByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getExitInterviewQuestion(@RequestBody SurveyDTO surveyDTO) {
		
		ServiceResponse response = employeeExitService.setExitInterviewResponseByEmpId(surveyDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAnsweredInterviewByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAnsweredInterviewByEmpId(@RequestBody SurveyDTO surveyDTO) {
		
		ServiceResponse response = employeeExitService.getAnsweredInterviewByEmpId(surveyDTO);
		return response;
	}
	
	@RequestMapping(value = "/getExitInterviewResponseBySurveyIdAndEmp" ,method = RequestMethod.POST)
	public ServiceResponse getExitInterviewResponseBySurveyIdAndEmp(@RequestBody SurveyDTO surveyDTO) {
		
		ServiceResponse response = employeeExitService.getExitInterviewResponseBySurveyIdAndEmp(surveyDTO);
		return response;
	}
	
                                  /* Employee Resignation Application Controller*/
	
	@RequestMapping(value = "/createResignationApplication" ,method = RequestMethod.POST)
	public ServiceResponse createResignationApplication(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.createResignationApplication(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllResignationApplication" ,method = RequestMethod.GET)
	public ServiceResponse getAllResignationApplication() {
		
		ServiceResponse response = employeeExitService.getAllResignationApplication();
		return response;
	}
	
	@RequestMapping(value = "/approveResignationApplication" ,method = RequestMethod.POST)
	public ServiceResponse approveResignationApplication(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.approveResignationApplication(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/rejectResignationApplication" ,method = RequestMethod.POST)
	public ServiceResponse rejectResignationApplication(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.rejectResignationApplication(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllProjectByEmpId(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.getAllProjectByEmpId(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/revokeResignationApplication" ,method = RequestMethod.POST)
	public ServiceResponse revokeResignationApplication(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.revokeResignationApplication(employeeExitDTO);
		return response;
	}
	
	@RequestMapping(value = "/revokeMyResignationApplication" ,method = RequestMethod.POST)
	public ServiceResponse revokeMyResignationApplication(@RequestBody EmployeeExitDTO employeeExitDTO) {
		
		ServiceResponse response = employeeExitService.revokeMyResignationApplication(employeeExitDTO);
		return response;
	}
	
}
