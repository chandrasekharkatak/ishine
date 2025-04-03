package com.apmosys.employeeportal.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.PerformanceDTO;
import com.apmosys.employeeportal.dto.QuarterCycleDTO;
import com.apmosys.employeeportal.dto.ReviewTypeDTO;
import com.apmosys.employeeportal.service.PerformanceService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class PerformanceController {
	
	@Autowired
	private PerformanceService performanceService;
	
	@RequestMapping(value = "/addReviewType", method = RequestMethod.POST)
	public ServiceResponse addReviewType(@RequestBody ReviewTypeDTO reviewTypeDTO) {

		ServiceResponse response = performanceService.addReviewType(reviewTypeDTO);
		return response;
	}
	
	
	@RequestMapping(value = "/getReviewType", method = RequestMethod.GET)
	public ServiceResponse getReviewType() {

		ServiceResponse response = performanceService.getReviewType();
		return response;
	}
	
    @RequestMapping(value = "/deleteReviewType",method = RequestMethod.POST)
    public ServiceResponse deleteReviewType(@RequestBody Long reviewTypeId) {
    	
    	ServiceResponse response = performanceService.deleteReviewType(reviewTypeId);
    	return response;
    }
    
    
    @RequestMapping(value = "/updateReviewType", method = RequestMethod.POST)
	public ServiceResponse updateReviewType(@RequestBody ReviewTypeDTO reviewTypeDTO) {

		ServiceResponse response = performanceService.updateReviewType(reviewTypeDTO);
		return response;
	}
    
    @RequestMapping(value = "/getReviewTypeById", method = RequestMethod.POST)
   	public ServiceResponse getReviewTypeById(@RequestBody ReviewTypeDTO reviewTypeDTO) {

   		ServiceResponse response = performanceService.getReviewTypeById(reviewTypeDTO);
   		return response;
   	}
    
	@RequestMapping(value = "/createQuarterCycle", method = RequestMethod.POST)
	public ServiceResponse createQuarterCycle(@RequestBody QuarterCycleDTO quarterCycleDTO) {

		ServiceResponse response = performanceService.createQuarterCycle(quarterCycleDTO);
		return response;
	}
	
	@RequestMapping(value = "/getQuartersByYear/{financialYear}", method = RequestMethod.GET)
	public ServiceResponse getQuartersByYear(@PathVariable String financialYear) {
		ServiceResponse response = performanceService.getQuartersByYear(financialYear);
		return response;
	}
	
	@RequestMapping(value = "/getAllQuarterCycles", method = RequestMethod.GET)
	public ServiceResponse getAllQuarterCycles() {
		ServiceResponse response = performanceService.getAllQuarterCycles();
		return response;
	}
	
	@RequestMapping(value = "/isEnable", method = RequestMethod.POST)
	public ServiceResponse isEnable(@RequestBody QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = performanceService.isEnable(quarterCycleDTO);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/isDelete", method = RequestMethod.POST)
	public ServiceResponse isDelete(@RequestBody QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = performanceService.isDelete(quarterCycleDTO);
		return serviceResponse;
	}
	
	@RequestMapping(value = "/getQuarterCycleById/{quarterid}", method = RequestMethod.GET)
	public ServiceResponse getQuarterCycleById(@PathVariable("quarterid") Long quarterId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = performanceService.getQuarterCycleById(quarterId);
		return serviceResponse;		
	}
	
	@RequestMapping(value = "/updateQuarterCycle", method = RequestMethod.POST)
	public ServiceResponse updateQuarterCycle(@RequestBody QuarterCycleDTO quarterCycleDTO) {
		ServiceResponse response = performanceService.updateQuarterCycle(quarterCycleDTO);
		return response;
	}
	
	@RequestMapping(value = "/getReviewDataForQuarter", method = RequestMethod.GET)
	public ServiceResponse getReviewDataForQuarter() {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = performanceService.getReviewDataForQuarter();
		return serviceResponse;		
	}
	
	@RequestMapping(value = "/submitEmployeePerformanceHOD", method = RequestMethod.POST)
	public ServiceResponse submitEmployeePerformanceHOD(@RequestBody PerformanceDTO employeePerformanceDTO) {
	    ServiceResponse response = performanceService.submitEmployeePerformanceHOD(employeePerformanceDTO);
	    return response;
	}
	
	@RequestMapping(value = "/getEmployeePerformanceHOD", method = RequestMethod.GET)
	public ServiceResponse getEmployeePerformanceHOD() {
	    ServiceResponse response = performanceService.getEmployeePerformanceHOD();
	    return response;
	}

	@RequestMapping(value = "/hrAndHODEmployeePerformanceView", method = RequestMethod.POST)
	public ServiceResponse hrAndHODEmployeePerformanceView(@RequestBody PerformanceDTO employeePerformanceDTO) {
	    ServiceResponse response = performanceService.hrAndHODEmployeePerformanceView(employeePerformanceDTO);
	    return response;
	}
	
	@RequestMapping(value = "/submitEmployeePerformanceHR",  method = RequestMethod.POST)
	public ServiceResponse submitEmployeePerformanceHR(@RequestBody PerformanceDTO employeePerformanceDTO) {
	    ServiceResponse response = performanceService.submitEmployeePerformanceHR(employeePerformanceDTO);
	    return response;
	}
	
	@RequestMapping(value = "/updateEmployeePerformanceHOD",  method = RequestMethod.POST)
	public ServiceResponse updateEmployeePerformanceHOD(@RequestBody PerformanceDTO employeePerformanceDTO) {
	    ServiceResponse response = performanceService.updateEmployeePerformanceHOD(employeePerformanceDTO);
	    return response;
	}

	

	@RequestMapping(value = "/getAllDepartmentbyEmployeecont", method = RequestMethod.GET)
	public ServiceResponse DepartmentbyEmployeecont() {
	    ServiceResponse response = performanceService.DepartmentbyEmployeecont();
	    return response;
	}
	
	@RequestMapping(value = "/getReviewLabelForEveryDepartment", method = RequestMethod.GET)
	public ServiceResponse getReviewLabelForEveryDepartment() {
	    ServiceResponse response = performanceService.getReviewLabelForEveryDepartment();
	    return response;
	}
	
	@RequestMapping(value = "/exportExcelForHodAndManger", method = RequestMethod.POST)
	public ServiceResponse exportExcelForHodAndManger(@RequestBody HrHodHrViewPerformance hrHodHrViewPerformance) {
	    ServiceResponse response = performanceService.exportExcelForHodAndManger(hrHodHrViewPerformance);
	    return response;
	}
	
}
