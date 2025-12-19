package com.apmosys.employeeportal.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.AclColumnDTO;
import com.apmosys.employeeportal.dto.AclColumnDTO;
import com.apmosys.employeeportal.dto.BulkBillableUpdateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.service.BioMaxService;
import com.apmosys.employeeportal.service.ReportService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ReportController {
	
	@Autowired
	ReportService reportService;
	
	
	@Autowired
	BioMaxService bioMaxService;
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/leaveReport" , method = RequestMethod.GET)
	public ServiceResponse leaveReport() {		
		
		ServiceResponse response =	reportService.leaveReport();
		return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/timesheetReport" , method = RequestMethod.GET)
	public ServiceResponse timesheetReport() {		
		
		ServiceResponse response =	reportService.timesheetReport();
		return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/getMappedSubFeatureList" , method = RequestMethod.POST)
	public ServiceResponse getMappedSubFeatureList(@RequestBody EmployeeDTO employeeDto) {		
		
		ServiceResponse response =	reportService.getMappedSubFeatureList(employeeDto);
		return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/getAllSubFeatureList" , method = RequestMethod.GET)
	public ServiceResponse getAllSubFeatureList() {		
		
		ServiceResponse response =	reportService.getAllSubFeatureList();
		return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value = "/getDefaultMapping", method = RequestMethod.POST)
	public ServiceResponse getDefaultMapping(@RequestBody List<AclColumnDTO> aclColumnDTO) {

		ServiceResponse response = reportService.getDefaultMapping(aclColumnDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value = "/updateDefaultFeatureMapping", method = RequestMethod.POST)
	public ServiceResponse updateDefaultFeatureMapping(@RequestBody JobRoleDTO jobRoleDTO) {

		ServiceResponse response = reportService.updateDefaultFeatureMapping(jobRoleDTO);
		return response;
	}
	
	
	
	//biomatric data link
		@GetMapping(value = "/getBioData")
		public ServiceResponse getBioData(@RequestParam String startDate,@RequestParam String endDate) throws SQLException {
			System.out.println("getBioData api call....................");
			ServiceResponse response = bioMaxService.getEmpBioData(startDate,endDate);
			
			return response;
		}
		
		
		@GetMapping("/getBioDataById")
		   public ServiceResponse getBioDataById(@RequestParam String empId,@RequestParam String date)
		   {
			   ServiceResponse response=bioMaxService.getEmpBioDataById(empId,date);
			   return response;
		   }
		
		@JobRoleAccess(featureIds = {26})
		@RequestMapping(value = "/getPoProjectDetailsBOthPOAndInternal", method = RequestMethod.GET)
		public ServiceResponse getPoProjectDetailsForPoProjects() {
			
			ServiceResponse response = reportService.getPoProjectDetailsBOthPOAndInternal();
			return response;
		}
		
		@JobRoleAccess(featureIds = {26})
		@RequestMapping(value ="/updateEmployeeReportBillableType", method = RequestMethod.POST)
		public ServiceResponse updateBillableType(@RequestBody EmployeeDTO employeeDTO) {
			ServiceResponse response = reportService.updateBillableType(employeeDTO);
			return response;
		}
		
		@JobRoleAccess(featureIds = {26})
		@RequestMapping(value="/updateBulkBillableEmployeeReport",method = RequestMethod.POST)
		public ServiceResponse updateBulkBillableEmployeeReport(@RequestBody BulkBillableUpdateDTO bulkBillableUpdateDTO) {
			ServiceResponse response = reportService.updateBulkBillableEmployeeReport(bulkBillableUpdateDTO);
			return response;
		}
		
		
		
		@Scheduled(cron = "0 59 23 * * ?")
		public ServiceResponse runDefaultProjectMappingCron() {
		    return reportService.updateDefaultProjectMappings();
		}
}
