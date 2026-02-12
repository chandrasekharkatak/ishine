package com.apmosys.employeeportal.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.apmosys.employeeportal.dto.EmployeeteamDto;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.PerformanceDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
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
	
	@JobRoleAccess(featureIds = {52,53})
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

	@RequestMapping(value = "/getAllDepartmentbyEmployeecont", method = RequestMethod.POST)
	public ServiceResponse DepartmentbyEmployeecont(@RequestBody HrHodHrViewPerformance hrHodHrViewPerformance ) {
	    ServiceResponse response = performanceService.DepartmentbyEmployeecont(hrHodHrViewPerformance);
	    return response;
	}
	
	@JobRoleAccess(featureIds = {53})
	@RequestMapping(value = "/getReviewLabelForEveryDepartment", method = RequestMethod.GET)
	public ServiceResponse getReviewLabelForEveryDepartment() {
	    ServiceResponse response = performanceService.getReviewLabelForEveryDepartment();
	    return response;
	}
	
	
	/*
	 * -------------------------- Review Apis ---------------------------------------
	 * */	
	
	
	@RequestMapping(value = "/checkUserHaveTeam", method = RequestMethod.POST)
	public ResponseEntity<PerformanceDTO> checkUserHaveTeam(@RequestBody PerformanceDTO performanceDTO){
		return performanceService.checkUserHaveTeam(performanceDTO);
	}
	
	@RequestMapping(value = "/addRemarkAsPerQuestion", method = RequestMethod.POST)
	public ResponseEntity<ProjectInsightDTO> addRemarkAsPerQuestion(@RequestBody ProjectInsightDTO projectInsightDTO){
		return performanceService.addRemarkAsPerQuestion(projectInsightDTO);
	}
	
	/*
	 * ----------------------------- Team dashboard Apis---------------------------------
	 * */
	
	@RequestMapping(value="/getTeamEmployeeListInTeamDashboard", method = RequestMethod.POST)
	public ResponseEntity<List<EmployeeteamDto>> getTeamEmployeeListInTeamDashboard(@RequestBody PerformanceDTO performanceDTO) {
		return performanceService.getTeamEmployeeListInTeamDashboard(performanceDTO);
	}
	
	@RequestMapping(value = "/exportExcelForHodAndManger", method = RequestMethod.POST)
	public ServiceResponse exportExcelForHodAndManger(@RequestBody HrHodHrViewPerformance hrHodHrViewPerformance) {
	    ServiceResponse response = performanceService.exportExcelForHodAndManger(hrHodHrViewPerformance);
	    return response;
	}	
	
	@RequestMapping(value = "/currentStatusForPerformanceTableView", method = RequestMethod.GET)
	public ServiceResponse currentStatusForPerformanceTableView() {
	    ServiceResponse response = performanceService.currentStatusForPerformanceTableView();
	    return response;
	}
	
	@RequestMapping(value = "/getAllEmployeePerformanceForQuarter", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeePerformanceForQuarter(
	        @RequestParam String financialYear,
	        @RequestParam Long quarterId,
	        @RequestParam(required = true) Long empId,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    return performanceService.getAllEmployeePerformanceForQuarter(
	            financialYear,quarterId ,empId ,page, size
	    );
	}
	
	@RequestMapping(value = "/exportExcelForEligiblePreview" , method = RequestMethod.GET)
	public ServiceResponse exportExcelForEligiblePreview (
			@RequestParam String fYear ,
			@RequestParam Long empId
			)
	
	{
		ServiceResponse response = performanceService.exportExcelForEligiblePreview(fYear, empId);
		return response;
	}
	
	@RequestMapping(value = "/getCurrentUserDepartment/{empId}" , method = RequestMethod.GET)
	public ServiceResponse getCurrentUserDepartment(@PathVariable Long empId )
	{
		ServiceResponse response = performanceService.getCurrentUserDepartment(empId);
		return response;
	}
}
