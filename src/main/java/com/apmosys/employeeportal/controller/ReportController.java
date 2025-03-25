package com.apmosys.employeeportal.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	@RequestMapping(value="/leaveReport" , method = RequestMethod.GET)
	public ServiceResponse leaveReport() {		
		
		ServiceResponse response =	reportService.leaveReport();
		return response;
	}
	
	@RequestMapping(value="/timesheetReport" , method = RequestMethod.GET)
	public ServiceResponse timesheetReport() {		
		
		ServiceResponse response =	reportService.timesheetReport();
		return response;
	}
	
	@RequestMapping(value="/getMappedSubFeatureList" , method = RequestMethod.POST)
	public ServiceResponse getMappedSubFeatureList(@RequestBody EmployeeDTO employeeDto) {		
		
		ServiceResponse response =	reportService.getMappedSubFeatureList(employeeDto);
		return response;
	}
	
	@RequestMapping(value="/getAllSubFeatureList" , method = RequestMethod.GET)
	public ServiceResponse getAllSubFeatureList() {		
		
		ServiceResponse response =	reportService.getAllSubFeatureList();
		return response;
	}
	
	@RequestMapping(value = "/getDefaultMapping", method = RequestMethod.GET)
	public ServiceResponse getDefaultMapping() {

		ServiceResponse response = reportService.getDefaultMapping();
		return response;
	}
	
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

		@RequestMapping(value="/getAllEmployeesReportByProjectType" , method = RequestMethod.GET)
		public ServiceResponse getAllEmployeesReportByProjectType() {		
			
			ServiceResponse response =	reportService.getAllEmployeesReportByProjectType();
			return response;
		}
		
		@RequestMapping(value="/getAllEmployeesReportByProjectTypeInConsolidated" , method = RequestMethod.GET)
		public ServiceResponse getAllEmployeesReportByProjectTypeInConsolidated() {		
			
			ServiceResponse response =	reportService.getAllEmployeesReportByProjectTypeInConsolidated();
			return response;
		}
}
