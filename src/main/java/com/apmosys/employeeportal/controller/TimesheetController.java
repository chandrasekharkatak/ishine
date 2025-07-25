package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeClientSideIdMappingDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.TimesheetService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class TimesheetController {
	
	@Autowired
	TimesheetService timesheetService;
	
	@RequestMapping(value = "/getAllProjectsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllProjectsByEmpId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllActivitiesByProjectIdandEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllActivitiesByProjectIdandEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/addTimesheet", method = RequestMethod.POST)
	public ServiceResponse addTimesheet(@RequestBody TimesheetDTO timesheetDTO,@RequestParam MultipartFile doc) {
		System.out.println("timesheetDTO list : "+timesheetDTO);
		ServiceResponse response = timesheetService.addTimesheet(timesheetDTO,doc);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyTeamTimesheets", method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamTimesheets(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyTeamTimesheets(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyTimesheetsByEmpId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyActivitiesByTimesheetId", method = RequestMethod.POST)
	public ServiceResponse getAllMyActivitiesByTimesheetId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyActivitiesByTimesheetId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getMyReporteesTimesheetRequests", method = RequestMethod.POST)
	public ServiceResponse getMyReporteesTimesheetRequests(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReporteesTimesheetRequests(timesheetDTO);
		return response;
	}	
	
	@RequestMapping(value = "/countMyReporteesTimesheetRequests", method = RequestMethod.POST)
	public ServiceResponse countMyReporteesTimesheetRequests(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.countMyReporteesTimesheetRequests(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateTimesheetRequestById", method = RequestMethod.POST)
	public ServiceResponse updateTimesheetRequestById(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.updateTimesheetRequestById(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateTimesheet", method = RequestMethod.POST)
	public ServiceResponse updateTimesheet(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.updateTimesheet(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getMyReporteesApprovedTimesheets", method = RequestMethod.POST)
	public ServiceResponse getMyReporteesApprovedTimesheets(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReporteesApprovedTimesheets(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getLast7DaysTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getLast7DaysTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getLast7DaysTimesheetsByEmpId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllLeaveTimesheetsWithoutLeaveApplication", method = RequestMethod.POST)
	public ServiceResponse getAllLeaveTimesheetsWithoutLeaveApplication(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllLeaveTimesheetsWithoutLeaveApplication(timesheetDTO);
		return response;
	}
	
	/*		
	 *	Data migration - Client & Project 		
	 */		
			
	@RequestMapping(value="/addClientAndProjectByList" , method = RequestMethod.POST, consumes="application/json")		
	public ServiceResponse addClientAndProjectByList(@RequestBody ProjectDTO[] projectDTO) {			
				
		ServiceResponse response = null;		
				
		 for (ProjectDTO project: projectDTO) {		
			 response = timesheetService.addClientAndProjectByList(project);		
		    }			
		return response;		
	}
	
	@RequestMapping(value = "/getTimesheetsForHomePageByEmpId", method = RequestMethod.POST)
	public ServiceResponse getTimesheetsForHomePageByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getTimesheetsForHomePageByEmpId(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/revokeApprovedTimesheet", method = RequestMethod.POST)
	public ServiceResponse revokeApprovedTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.revokeApprovedTimesheet(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/bulkApproveTimesheetRequest", method = RequestMethod.POST)
	public ServiceResponse bulkApproveTimesheetRequest(@RequestBody TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.bulkApproveTimesheetRequest(timesheetDTO);
		    return response;
	}
	
	@RequestMapping(value = "/bulkRejectTimesheetRequest", method = RequestMethod.POST)
	public ServiceResponse bulkRejectTimesheetRequest(@RequestBody TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.bulkRejectTimesheetRequest(timesheetDTO);
		    return response;
	}
	
	
	 @RequestMapping(value = "/getAllOrDeptWiseEmployeeTimesheetReport",method = RequestMethod.POST)
	    public ServiceResponse getAllOrDeptWiseEmployeeTimesheetReport(@RequestBody FilteredTimesheetDTO filteredTimesheetDTO) {
	        return timesheetService.getAllOrDeptWiseEmployeeTimesheetReport(filteredTimesheetDTO);
	    }
	 
	 @PostMapping("/getLastFilledTimesheetByEmpId")
	 public ServiceResponse getLastFilledTimesheetByEmpId(@RequestBody TimesheetDTO timesheetDTO) {
	     return timesheetService.getLastFilledTimesheetByEmpId(timesheetDTO.getEmpId());
	 }

	
//	@Scheduled(cron = "0 53 17 * * ?")  // Runs at 3:55 pm
//    public void scheduleUpdateCurrentManagerInTimesheets() {
//		System.err.println("----cron started-----");
//        timesheetService.updateCurrentManagerInTimesheets();
//        System.err.println("--cron ended--");
//    }
	
//	public void backfillManagerIds() {
//		System.err.println("----cron started-----");
//            timesheetService.updateTimesheetManagerIds();
//            System.err.println("--cron ended----");
//            
//    }
	 
	 @PostMapping("/getActiveProjectsByEmpId")
	 public ServiceResponse getActiveProjectsByEmpId(@RequestParam Long empId) {
	     return timesheetService.getActiveProjectsByEmpId(empId);
	 }
	 
	 @PostMapping("/getClientSideIdByProjectId")
	 public ServiceResponse getClientSideIdByProjectId(@RequestParam Long projectId) {
	     return timesheetService.getClientSideIdByProjectId(projectId);
	 }
	 
	 @PostMapping("/fetchEmploymentIdByEmpId")
	 public ServiceResponse fetchEmploymentIdByEmpId(@RequestParam Long empId) {
	     return timesheetService.fetchEmploymentIdByEmpId(empId);
	 }
	 
	 @PostMapping("/createClientSideIdMapping")
	 public ServiceResponse createClientSideIdMapping(@RequestBody EmployeeClientSideIdMappingDTO empClientDTO) {
	     return timesheetService.createClientSideIdMapping(empClientDTO);
	 }
	 
	 @PostMapping("/updateClientSideIdMapping")
	 public ServiceResponse updateClientSideIdMapping(@RequestBody EmployeeClientSideIdMappingDTO empClientDTO) {
	     return timesheetService.updateClientSideIdMapping(empClientDTO);
	 }
	 
	 @PostMapping("/getActiveProjectsAndClientSideIdByEmpId")
	 public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(@RequestParam Long empId) {
	     return timesheetService.getActiveProjectsAndClientSideIdByEmpId(empId);
	 }
	 
}
