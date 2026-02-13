package com.apmosys.employeeportal.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.EmployeeClientSideIdMappingDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.FinalBulkUploadDTO;
import com.apmosys.employeeportal.dto.FinalDocumentDownloadDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderByProjectIdDTO;
import com.apmosys.employeeportal.dto.GetMyReporteesTimesheetRequestsPayload;
import com.apmosys.employeeportal.dto.GetProjectListForDateAndEmpIdPayload;
import com.apmosys.employeeportal.dto.GetTimesheetDashboardCountForEmployeeDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetApprovalNewDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetStatusCountDTO;
import com.apmosys.employeeportal.service.TimesheetApprovalService;
import com.apmosys.employeeportal.service.TimesheetService;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.EncryptionUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping(path = "/api")
public class TimesheetController {
	
	@Autowired
	TimesheetService timesheetService;
		
//		@Autowired
//		TimesheetApprovalServiceNew timesheetApprovalServiceNew;
		

		@Autowired
TimesheetServiceNew timesheetServiceNew;
		
//		@Autowired
//		TimesheetEncryptionHelper timesheetEncryptionHelper;
		
		@Autowired
		TimesheetApprovalService timesheetApprovalService;

	@Value("${timesheet.minus.days.for.bulk.upload}")
	private Integer minusDays;

	@Value("${check.minus.days.for.bulk.upload}")
	private Boolean checkMinusDaysForBulkUpload;
	
	
	@JobRoleAccess(featureIds = {7,15,16})
	@RequestMapping(value = "/getAllProjectsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllProjectsByEmpId(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16})
	@RequestMapping(value = "/getAllActivitiesByProjectIdandEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllActivitiesByProjectIdandEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15})
	@RequestMapping(value = "/addTimesheetWithClient", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse addTimesheetWithClient(@RequestPart("dto") String encryptedDto,
			@RequestPart(value = "doc1",required = false) MultipartFile doc1,
			@RequestPart(value = "doc2",required = false) MultipartFile doc2) throws Exception 
//	,@RequestBody TimesheetDTO timesheetDTO, @RequestBody MultipartFile doc
	{
		EncryptionUtil encryptionService = new EncryptionUtil();
		String decryptedJson = encryptionService.decryptMinor(encryptedDto);

	    // 🔹 Convert decrypted JSON into DTO
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

	    TimesheetDTO dto = objectMapper.readValue(decryptedJson, TimesheetDTO.class);
		System.out.println("timesheetDTO list : "+dto);
		ServiceResponse response = timesheetService.addTimesheet(dto,doc1,doc2);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16})
	@RequestMapping(value = "/getAllMyTeamTimesheets", method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamTimesheets(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyTeamTimesheets(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16})
	@RequestMapping(value = "/getAllMyTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyTimesheetsByEmpId(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16,24})
	@RequestMapping(value = "/getAllMyActivitiesByTimesheetId", method = RequestMethod.POST)
	public ServiceResponse getAllMyActivitiesByTimesheetId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getAllMyActivitiesByTimesheetId(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16,24})
	@RequestMapping(value = "/getMyReporteesTimesheetRequests", method = RequestMethod.POST)
	public ServiceResponse getMyReporteesTimesheetRequests(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReporteesApprovedTimesheets2(timesheetDTO);
		return response;
	}	
	@JobRoleAccess(featureIds = {15,16,24})
	@RequestMapping(value = "/countMyReporteesTimesheetRequests", method = RequestMethod.POST)
	public ServiceResponse countMyReporteesTimesheetRequests(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.countMyReporteesTimesheetRequests(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16,24})
	@RequestMapping(value = "/updateTimesheetRequestById", method = RequestMethod.POST)
	public ServiceResponse updateTimesheetRequestById(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.updateTimesheetRequestById(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {15,16,14,3,24})
	@RequestMapping(value = "/updateTimesheet", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse updateTimesheet(@RequestPart("dto") String encryptedDto,
			@RequestPart(value = "doc1",required = false) MultipartFile doc1,
			@RequestPart(value = "doc2",required = false) MultipartFile doc2) throws Exception 
//	,@RequestBody TimesheetDTO timesheetDTO, @RequestBody MultipartFile doc
	{
		EncryptionUtil encryptionService = new EncryptionUtil();
		String decryptedJson = encryptionService.decryptMinor(encryptedDto);

	    // 🔹 Convert decrypted JSON into DTO
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

	    TimesheetDTO dto = objectMapper.readValue(decryptedJson, TimesheetDTO.class);
		ServiceResponse response = timesheetService.updateTimesheet(dto,doc1,doc2);
		return response;
	}
	@JobRoleAccess(featureIds = {16})
	@RequestMapping(value = "/getMyReporteesApprovedTimesheets", method = RequestMethod.POST)
	public ServiceResponse getMyReporteesApprovedTimesheets(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReporteesApprovedTimesheets(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getMyReporteesApprovedTimesheets2", method = RequestMethod.POST)
	public ServiceResponse getMyReporteesApprovedTimesheets2(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReporteesApprovedTimesheets2(timesheetDTO);
		return response;
	}	
	
	
	
	
	@PostMapping(value ="/getMyReportees")
	public ServiceResponse getMyReportees(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getMyReportees(timesheetDTO);
		return response;
	}
	
	
	
	@RequestMapping(value = "/getLast7DaysTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getLast7DaysTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getLast7DaysTimesheetsByEmpId(timesheetDTO);
		return response;
	}

	@JobRoleAccess(featureIds = {26})
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
	
	@JobRoleAccess(featureIds = {3,24})
	@RequestMapping(value = "/getTimesheetsForHomePageByEmpId", method = RequestMethod.POST)
	public ServiceResponse getTimesheetsForHomePageByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getTimesheetsForHomePageByEmpId(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {16})
	@RequestMapping(value = "/revokeApprovedTimesheet", method = RequestMethod.POST)
	public ServiceResponse revokeApprovedTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.revokeApprovedTimesheet(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {16,24})
	@RequestMapping(value = "/bulkApproveTimesheetRequest", method = RequestMethod.POST)
	public ServiceResponse bulkApproveTimesheetRequest(@RequestBody TimesheetApprovalNewDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.bulkApproveTimesheetRequest(timesheetDTO);
		    return response;
	}
	@JobRoleAccess(featureIds = {16,24})
	@RequestMapping(value = "/bulkRejectTimesheetRequest", method = RequestMethod.POST)
	public ServiceResponse bulkRejectTimesheetRequest(@RequestBody TimesheetDTO timesheetDTO) {
		
		ServiceResponse response = timesheetService.bulkRejectTimesheetRequest(timesheetDTO);
		    return response;
	}
	
	@JobRoleAccess(featureIds = {26})
	 @RequestMapping(value = "/getAllOrDeptWiseEmployeeTimesheetReport",method = RequestMethod.POST)
	    public ServiceResponse getAllOrDeptWiseEmployeeTimesheetReport(@RequestBody FilteredTimesheetDTO filteredTimesheetDTO) {
	        return timesheetService.getAllOrDeptWiseEmployeeTimesheetReport(filteredTimesheetDTO);
	    }
	@JobRoleAccess(featureIds = {24})
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
		// @JobRoleAccess(featureIds = {15})
		 @PostMapping("/getProjectListForDateAndEmpId")
		 public ServiceResponse getProjectListForDateAndEmpId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
		     return timesheetService.getProjectListForDateAndEmpId(payload);
		 }
	
		@JobRoleAccess(featureIds = {15,16})
		 @PostMapping("/getClientSideIdByProjectId")
		 public ServiceResponse getClientSideIdByProjectId(@RequestParam Long projectId) {
		     return timesheetService.getClientSideIdByProjectId(projectId);
		 }
		@JobRoleAccess(featureIds = {15})
		 @PostMapping("/fetchEmploymentIdByEmpId")
		 public ServiceResponse fetchEmploymentIdByEmpId(@RequestParam Long empId) {
		     return timesheetService.fetchEmploymentIdByEmpId(empId);
		 }
		@JobRoleAccess(featureIds = {15})
		 @PostMapping("/updateClientSideIdMapping")
		 public ServiceResponse updateClientSideIdMapping(@RequestBody EmployeeClientSideIdMappingDTO empClientDTO) {
		     return timesheetService.updateClientSideIdMapping(empClientDTO);
		 }
		@JobRoleAccess(featureIds = {15})
		 @PostMapping("/getActiveProjectsAndClientSideIdByEmpId")
		 public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
		     return timesheetService.getActiveProjectsAndClientSideIdByEmpId(payload);
		 }

		@JobRoleAccess(featureIds = {15})
		 @GetMapping("/getEmployeeListByProjectId")
		 public ServiceResponse getEmployeeListByProjectId(@RequestParam Integer projectId,
				 @RequestParam Long currentUser,
				 @RequestParam(required = false) String date) {
			 
			 // If date is provided, use date-aware variant (shadow timesheet, etc.)
			 if (date != null && !date.isEmpty()) {
				 LocalDate localDate = LocalDate.parse(date.substring(0, 10));
				 LocalDateTime startOfDay = localDate.atStartOfDay();
				 LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
				 return timesheetService.getEmployeeListByProjectIdForDate(projectId, currentUser, startOfDay, endOfDay);
			 }
			 
		     return timesheetService.getEmployeeListByProjectId(projectId,currentUser);
		 }
		@JobRoleAccess(featureIds = {15})
		 @PostMapping("/getClientSideIdByProjectIdAndEmpId")
		 public ServiceResponse getClientSideIdByProjectIdAndEmpId(@RequestParam Long projectId,@RequestParam Long empId) {
		     return timesheetService.getClientSideIdByProjectIdAndEmpId(projectId,empId);
		 }
		 
		@JobRoleAccess(featureIds = {15,16,24})
		 @GetMapping("/getDocumentDataByDocId")
		 public ServiceResponse getDocumentDataByDocId(@RequestParam Long docId) {
		     return timesheetService.getDocumentDataByDocId(docId);
		 }
		
		@JobRoleAccess(featureIds = {15,16,24})
		@GetMapping("/getFinalDocumentDataByDocId")
		public ServiceResponse getFinalDocumentDataByDocId(@RequestParam Long timesheetId,@RequestParam Long docId) {
			return timesheetService.getFinalDocumentDataByDocId(timesheetId,docId);
		}
		
	 @PostMapping("/getOneMonthTimesheetReport")
	 public ServiceResponse getOneMonthTimesheetReport(@RequestBody TimesheetDTO timesheetDTO) {
			ServiceResponse timesheetList = timesheetService.getTimesheetForEmployee(timesheetDTO);
			return timesheetList;
		}
	 @JobRoleAccess(featureIds = {15})
	 @GetMapping("/checkIfProjectRequiresClientId")
	 public ServiceResponse checkIfProjectRequiresClientId(@RequestParam Integer projectId) {
		 return timesheetService.checkIfProjectRequiresClientId(projectId);
	 }
	 @PostMapping("/totalVmsFilledCount")
	    public ServiceResponse totalVmsFilledCount(@RequestBody TimesheetDTO timesheetDTO) {
	        ServiceResponse response = timesheetService.totalVmsFilledCount(timesheetDTO);
	        return response;
	    }
	 @JobRoleAccess(featureIds = {16})
	 @PostMapping("/totalIshineFilledCount")
	    public ServiceResponse totalIshineFilledCount(@RequestBody TimesheetDTO timesheetDTO) {
		 ServiceResponse response = timesheetService.totalIshineFilledCount(timesheetDTO.getStatus());
	        return response;
	    }
	 
	 @PostMapping("/totalvmsNotFilled")
	    public ServiceResponse totalvmsNotFilled(@RequestBody TimesheetDTO timesheetDTO) {
	        ServiceResponse response = timesheetService.totalvmsNotFilled(timesheetDTO);
	        return response;
	    }
	 
	 @PostMapping("/totalIshineNotFilledCount")
	    public ServiceResponse totalIshineNotFilledCount(@RequestBody TimesheetDTO timesheetDTO) {
		 ServiceResponse response = timesheetService.totalIshineNotFilledCount(timesheetDTO);
	        return response;
	    }
	 
	 @RequestMapping(value="/getVmsDocumentApprovalStatusWiseCount",method=RequestMethod.GET)
	 public ServiceResponse getVmsDocumentApprovalStatusWiseCount() {
		 ServiceResponse reponse= timesheetService.getVmsDocumentApprovalStatusWiseCount();
		 return reponse;
	 }
	 
	 @JobRoleAccess(featureIds = {15})
	 @PostMapping(value = "/bulkFinalDocumentUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	 public ServiceResponse bulkFinalDocumentUpload(
	         @RequestPart("finalFile") MultipartFile file,
	         @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
	         @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
		         @RequestParam("empId") Long empId,@RequestParam("createdBy") Long createdBy) throws Exception {
	     
	     System.out.println("Received file: " + file.getOriginalFilename());
	     System.out.println("From Date: " + fromDate);
	     System.out.println("To Date: " + toDate);
	     
	     ServiceResponse reponse= timesheetService.replaceAllTemporaryFileWithFinalFile(file,fromDate,toDate,empId,createdBy);
	     // TODO: Add your processing logic here
	     
	     return reponse;
	 }
	 
	 @JobRoleAccess(featureIds = {15})
	 @GetMapping("/getAllDisabledDateListForBulkDocSubmit")
	 public ServiceResponse getAllDisabledDateListForBulkDocSubmit(@RequestParam("projectId") Integer projectId,@RequestParam("empId") Long empId) {
		
		 ServiceResponse reponse= timesheetService.getAllDisabledDateListForBulkDocSubmit(projectId,empId);
		
		return reponse;
	 }
	 
	 
	 @PostMapping(value = "/getEmployeeViewForClientAttendanceStatus")
		public ServiceResponse getEmployeeViewForClientAttendanceStatus(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
			 ServiceResponse reponse= timesheetServiceNew.getEmployeeViewForClientAttendanceStatus(object);
			 System.out.println("Response from service: " + reponse);
			 return reponse;
		}
	 

	 @RequestMapping(value = "/getEmployeeTimesheetsByProject", method =RequestMethod.POST)
	 public ServiceResponse getEmployeeTimesheetsByProject(@RequestBody TimesheetDTO timesheetDTO) {
	      
	     ServiceResponse reponse= timesheetService.getEmployeeTimesheetsByProject(timesheetDTO);
	     
	     return reponse;
	 }
	 
	 @JobRoleAccess(featureIds = {14,15,24,63,16})
	 @RequestMapping(value = "/getRejectionReason",method = RequestMethod.GET)
	 public ServiceResponse getRejectionReason() {
	      
	     ServiceResponse reponse= timesheetService.getRejectionReason();
	     
	     return reponse;
	 }
	 
	 @PostMapping("/approveOrRejectDocument")
	 public ServiceResponse approveOrRejectDocument(@RequestParam("docId") Long docId, @RequestParam("approvedOrRejectedBy") Long approvedOrRejectedBy, @RequestParam("approvalStatus") String approvalStatus,@RequestParam("timesheetId") Long timesheetId) {
	      
	     ServiceResponse reponse= timesheetService.approveOrRejectDocument(docId,approvedOrRejectedBy,approvalStatus);
	     
	     return reponse;
	 }
	 @JobRoleAccess(featureIds = {63})
	 @PostMapping(value = "/setTimesheetRejectReason")
	 public ServiceResponse setTimesheetRejectReason(@RequestBody TimesheetRejectionReasonsMasterDTO rejectReasonObj) {
		 
		 ServiceResponse reponse= timesheetService.setTimesheetRejectReason(rejectReasonObj);
	     return reponse;
	     
	 }
	 @JobRoleAccess(featureIds = {63})
	 @GetMapping(value = "/getRejectionReasonById")
	 public ServiceResponse getRejectionReasonById(@RequestParam Long rejectionId) {
		 
		 ServiceResponse reponse= timesheetService.getRejectionReasonById(rejectionId);
	     return reponse;
	     
	 }
	 
	 @PostMapping(value = "/getProjectViewForClientAttendanceStatus")
	 public ServiceResponse getProjectViewForClientAttendanceStatus(@RequestBody TimesheetDTO timesheetDTO) {

		 ServiceResponse reponse= timesheetService.getProjectViewForClientAttendanceStatus(timesheetDTO);
	     return reponse;
	     
	 }
	 @JobRoleAccess(featureIds = {63})
	 @PostMapping(value = "/updateActiveByRejectIdId")
	 public ServiceResponse updateActiveByRejectIdId(@RequestBody TimesheetRejectionReasonsMasterDTO rejectReasonObj) {
		 ServiceResponse reponse= timesheetService.updateActiveByRejectIdId(rejectReasonObj);
		 return reponse;
	}
	 @JobRoleAccess(featureIds = {16})
	 @RequestMapping(value= "/getAllEmployeeDSROfRM",method=RequestMethod.POST)
	 public ServiceResponse getAllEmployeeDSROfRM(@RequestBody TimesheetDTO timesheetDTO) {
		 ServiceResponse reponse= timesheetService.getAllEmployeeDSROfRM(timesheetDTO);
	     return reponse;
	     
	 }
	 @JobRoleAccess(featureIds = {15,16})
	 @PostMapping(value = "/getEmployeeTimesheetAsCalender")
	 public ServiceResponse getEmployeeTimesheetAsCalender(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
		 ServiceResponse reponse= timesheetService.getEmployeeTimesheetAsCalender(object);
		  return reponse;
	 }
	 @JobRoleAccess(featureIds = {15,16,24})
	 @RequestMapping(value= "/approveTimesheetRequest",method=RequestMethod.POST)
	 public ServiceResponse approveTimesheetRequest(@RequestBody TimesheetDTO timesheetDTO) {
		 ServiceResponse reponse= timesheetService.approveTimesheetRequest(timesheetDTO);
	     return reponse;
	     
	 }
	 @JobRoleAccess(featureIds = {15,16})
	 @PostMapping(value = "/getEmployeeTimesheetAsCalenderByProjectId")
	 public ServiceResponse getEmployeeTimesheetAsCalenderByProjectId(@RequestBody GetEmployeeTimesheetAsCalenderByProjectIdDTO object) {  
		 ServiceResponse reponse= timesheetService.getEmployeeTimesheetAsCalenderByProjectId(object);
		  return reponse;
	 }
	 
	 @PostMapping(value = "/getTimesheetDashboardCountForEmployee")
	 public ServiceResponse getTimesheetDashboardCountForEmployee(@RequestBody GetTimesheetDashboardCountForEmployeeDTO payload) {  
		    Integer month = (Integer) payload.getMonth();
		    Integer year = (Integer) payload.getYear();
		    Long empId = Long.valueOf(payload.getEmpId());
			Boolean isClientDashboard =  Boolean.valueOf(payload.getIsClientDashboard());
//			String billableType=String.valueOf(payload.getSelectedBillableType());
		    List<String> billableTypes = payload.getSelectedBillableTypes(); // use the list
			String employeeActive = String.valueOf(payload.getSelectedEmployeeStatus());
		 ServiceResponse reponse= timesheetService.getTimesheetDashboardCountForEmployee(month,year,empId,isClientDashboard,billableTypes,employeeActive,payload.getClientSideFilter(),payload.getMultiPOs());
		  return reponse;
	 }
	 
	 @GetMapping(value = "/getTimesheetDashboardCountForProject")
	 public ServiceResponse getTimesheetDashboardCountForProject(@RequestParam Integer month, @RequestParam Integer year,@RequestParam Long empId,
			 @RequestParam Boolean isClientDashboard,@RequestParam List<String> billableType,@RequestParam String projectActive) {  
		 ServiceResponse reponse= timesheetService.getTimesheetDashboardCountForProject(month,year,empId,isClientDashboard,billableType,projectActive);
		  return reponse;
	 }
	 @JobRoleAccess(featureIds = {15,24})
	 @PostMapping(value = "/getLastFilledTimesheetByEmp")
	 public ServiceResponse getLastFilledTimesheetByEmp(@RequestBody TimesheetDTO timesheetDTO) { 
	     Long empId = timesheetDTO.getEmpId();
	     ServiceResponse response = timesheetService.getLastFilledTimesheetByEmp(empId);
	     return response;
	 }

	 @PostMapping("/getEmployeeByNameAndEmpidForTimesheet")
		public ServiceResponse getEmployeeByNameAndEmpidForTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		 ServiceResponse response = timesheetService.getEmployeeByNameAndEmpidForTimesheet(timesheetDTO);
		    return response;
		}
	 
		@PostMapping("/getDocumentsByEmpAndDate")
		public ServiceResponse getDocumentsByEmpAndDate(@RequestBody TimesheetDTO timesheetDTO) {
			ServiceResponse response = timesheetService.getDocumentsByEmpAndDate(timesheetDTO);
			return response;
		}
		

		// @PostMapping(value = "/getProjectByMonthRangeAndEmpId")
		// public ServiceResponse getProjectByMonthRangeAndEmpId(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
		// 	 ServiceResponse reponse= timesheetService.getProjectByMonthRangeAndEmpId(object);
		// 	 return reponse;
		// }

		@PostMapping(value = "/getEmployeeSummaryOnExport")
		public ServiceResponse getEmployeeSummaryOnExport(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
			 ServiceResponse reponse= timesheetService.getEmployeeSummaryOnExportAccordingToStatus(object);
			 return reponse;
		}

		@PostMapping("/isInTNMProject")
		public ServiceResponse employeeInTNMProject(@RequestParam Long empId) {
			return timesheetService.isEmployeeInTNMProject(empId);
		}
		
		@PostMapping("/wasEmployeeInClientProjCurrAndPrevMon")
		public ServiceResponse wasEmployeeInClientProjCurrAndPrevMon(@RequestParam Long empId) {
			return timesheetService.wasEmployeeInClientProjCurrAndPrevMon(empId);
		}
   
		@PostMapping("/isClientIdMandetory")
		public ServiceResponse isClientMandetory(@RequestBody int projectId) {
			return timesheetService.isClientMandetory(projectId);
		} 
	
	// @PostMapping(value = "/getProjectByMonthRangeAndEmpId")
	// public ServiceResponse getProjectByMonthRangeAndEmpId(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
	// 	 ServiceResponse reponse= timesheetService.getProjectByMonthRangeAndEmpId(object);
	// 	 return reponse;
	// }

			
		@GetMapping(value="/getAllDayTypes")
		public ServiceResponse getAllDayTipes() {  
			 ServiceResponse reponse= timesheetService.getAllDayTypes();
			 return reponse;
		}
		
		// @JobRoleAccess(featureIds = {7,15,16})
		// @PostMapping(value = "/getClientDetailsByProjectIdAndEmpId")
		// public ServiceResponse getClientDetailsByProjectIdAndEmpId(@RequestBody GetEmployeeSummaryOnExportDTO timesheetDTO) {
	

		// 	ServiceResponse response = timesheetService.getClientDetailsByProjectIdAndEmpId(timesheetDTO);
		// }

	@PostMapping(value = "/getProjectByMonthRangeAndEmpId") 	
	public ServiceResponse getProjectByMonthRangeAndEmpId(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
		 ServiceResponse reponse= timesheetService.getProjectByMonthRangeAndEmpId(object);
		 return reponse;
	}
	
	
	@JobRoleAccess(featureIds = {7,15,16})
	@PostMapping(value = "/getClientDetailsByProjectIdAndEmpId")
	public ServiceResponse getClientDetailsByProjectIdAndEmpId(@RequestBody GetEmployeeSummaryOnExportDTO timesheetDTO) {

		ServiceResponse response = timesheetService.getClientDetailsByProjectIdAndEmpId(timesheetDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15})
	 @PostMapping("/getOtherTeamMembersByDateAndProjectId")
	 public ServiceResponse getOtherTeamMembersByDateAndProjectId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
	     return timesheetService.getOtherTeamMembersByDateAndProjectId(payload);
	 }
	
	@PostMapping(value = "/getMyReporteesAndClientSideProjectsInMonthYear")
	public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
		 ServiceResponse reponse= timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(timesheetDTO);
		 return reponse;
	}
	
	
	@PostMapping(value = "/getMyProjectsInMonthYear")
	public ServiceResponse getMyProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
		 ServiceResponse reponse= timesheetService.getMyProjectsInMonthYear(timesheetDTO);
		 return reponse;
	}
	
	
	@PostMapping("/downloadFinalDocuments")
	public ResponseEntity<byte[]> downloadFinalDocuments(
	        @RequestBody FinalDocumentDownloadDTO dto) {

	    try {
	        byte[] zipBytes = timesheetService
	                .downloadFinalDocumentsZip(
	                        dto.getProjectId(),
	                        dto.getMonth(),
	                        dto.getYear(),
	                        dto.getEmpId()
	                );

	        String zipName =
	                dto.getProjectName() + "_" +
	                dto.getMonth() + "_" +
	                dto.getYear() + ".zip";

//	        return ResponseEntity.ok()
//	                .header(HttpHeaders.CONTENT_DISPOSITION,
//	                        "attachment; filename=" + zipName)
//	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//	                .body(zipBytes);
	        
	        return ResponseEntity.ok()
	                .header(
	                    HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=\"" + zipName + "\""
	                )
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(zipBytes);

	    } catch (Exception e) {
	        e.printStackTrace(); 

	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(null);
	    }
	}

	
	@PostMapping("/getDocumentsBySelectedEmpId")
	public ServiceResponse getDocumentsBySelectedEmpId(@RequestBody FinalDocumentDownloadDTO dto) {
		ServiceResponse response = timesheetService.getDocumentsBySelectedEmpId(dto);
		return response;
	}
	
	@PostMapping("/getDepartmentStatusSummary")
	public ServiceResponse getDepartmentStatusSummary(
	        @RequestBody GetEmployeeSummaryOnExportDTO requestDTO) {

	    ServiceResponse response =
	    		timesheetService.getDepartmentStatusSummary(requestDTO);

	    return response;
	}

	@JobRoleAccess(featureIds = {15,16})
	@PostMapping(value = "/bulkFinalUploadProjectBased", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse bulkFinalUploadProjectBased(
			@RequestPart("finalFile") MultipartFile file, @RequestPart("finalBulkUploadDTO") FinalBulkUploadDTO finalBulkUploadDTO ) {

		ServiceResponse reponse= timesheetService.bulkFinalUploadProjectBased(finalBulkUploadDTO, file);
		return reponse;
	}

	@JobRoleAccess(featureIds = {15,16})
	@GetMapping("/getPreviousMinusDays")
	public ServiceResponse getPreviousMinusDays() {
		ServiceResponse response = new ServiceResponse();
		try {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			Map<String, Object> map = new HashMap<>();
			map.put("minusDays", minusDays);
			map.put("checkMinusDaysForBulkUpload", checkMinusDaysForBulkUpload);
			response.setServiceResponse(map);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(ServiceResponse.STATUS_FAIL);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return response;
		}
	}

	@PostMapping("/getTimesheetStatusCountByManager")
		public ServiceResponse getTimesheetStatusCountByManager(
		        @RequestBody TimesheetStatusCountDTO timesheetCount) {
				ServiceResponse response = timesheetServiceNew.getTimesheetStatusCountsByManager(timesheetCount.getManagerId());
		    return response;
	}
		
		// @JobRoleAccess(featureIds = {15})
		//  @PostMapping("/getOtherTeamMembersByDateAndProjectId")
		//  public ServiceResponse getOtherTeamMembersByDateAndProjectId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
		//      return timesheetService.getOtherTeamMembersByDateAndProjectId(payload);
		//  }
		
		// @PostMapping(value = "/getMyReporteesAndClientSideProjectsInMonthYear")
		// public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
		// 	 ServiceResponse reponse= timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(timesheetDTO);
		// 	 return reponse;
		// }
		
		
		// @PostMapping(value = "/getMyProjectsInMonthYear")
		// public ServiceResponse getMyProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
		// 	 ServiceResponse reponse= timesheetService.getMyProjectsInMonthYear(timesheetDTO);
		// 	 return reponse;
		// }
		
		@JobRoleAccess(featureIds = {15,16,24})
		@PostMapping("/getMyReporteesTimesheetRequestsNew")
		public ServiceResponse getMyReporteesTimesheetRequestsNew(
		        @RequestBody GetMyReporteesTimesheetRequestsPayload payload) {
	
		    return timesheetApprovalService
		            .getMyReporteesTimesheetRequestsNew(payload);
		}
	
	// @JobRoleAccess(featureIds = {15})
	//  @PostMapping("/getOtherTeamMembersByDateAndProjectId")
	//  public ServiceResponse getOtherTeamMembersByDateAndProjectId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
	//      return timesheetService.getOtherTeamMembersByDateAndProjectId(payload);
	//  }
	
	// @PostMapping(value = "/getMyReporteesAndClientSideProjectsInMonthYear")
	// public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
	// 	 ServiceResponse reponse= timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(timesheetDTO);
	// 	 return reponse;
	// }
	
	
	// @PostMapping(value = "/getMyProjectsInMonthYear")
	// public ServiceResponse getMyProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
	// 	 ServiceResponse reponse= timesheetService.getMyProjectsInMonthYear(timesheetDTO);
	// 	 return reponse;
	// }
	
	// @PostMapping("/downloadFinalDocuments")
	// public ResponseEntity<byte[]> downloadFinalDocuments(
	//         @RequestBody FinalDocumentDownloadDTO dto) {

	//     byte[] zipBytes = timesheetService
	//         .downloadFinalDocumentsZip(
	//             dto.getProjectId(),
	//             dto.getMonth(),
	//             dto.getYear(),
	//             dto.getEmpId()
	//         );

	//     String zipName =
	//         dto.getProjectName() + "_" +
	//         dto.getMonth() + "_" +
	//         dto.getYear() + ".zip";

	//     return ResponseEntity.ok()
	//         .header(HttpHeaders.CONTENT_DISPOSITION,
	//             "attachment; filename=" + zipName)
	//         .contentType(MediaType.APPLICATION_OCTET_STREAM)
	//         .body(zipBytes);
	// }
	
	// @PostMapping("/getDocumentsBySelectedEmpId")
	// public ServiceResponse getDocumentsBySelectedEmpId(@RequestBody FinalDocumentDownloadDTO dto) {
	// 	ServiceResponse response = timesheetService.getDocumentsBySelectedEmpId(dto);
	// 	return response;
	// }
	
	// @PostMapping("/getDepartmentStatusSummary")
	// public ServiceResponse getDepartmentStatusSummary(
	//         @RequestBody GetEmployeeSummaryOnExportDTO requestDTO) {

	//     ServiceResponse response =
	//     		timesheetService.getDepartmentStatusSummary(requestDTO);

	//     return response;
	// }

	// @JobRoleAccess(featureIds = {15,16})
	// @PostMapping(value = "/bulkFinalUploadProjectBased", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// public ServiceResponse bulkFinalUploadProjectBased(
	// 		@RequestPart("finalFile") MultipartFile file, @RequestPart("finalBulkUploadDTO") FinalBulkUploadDTO finalBulkUploadDTO ) {

	// 	ServiceResponse reponse= timesheetService.bulkFinalUploadProjectBased(finalBulkUploadDTO, file);
	// 	return reponse;
	// }

	// @JobRoleAccess(featureIds = {15,16})
	// @GetMapping("/getPreviousMinusDays")
	// public ServiceResponse getPreviousMinusDays() {
	// 	ServiceResponse response = new ServiceResponse();
	// 	try {
	// 		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	// 		Map<String, Object> map = new HashMap<>();
	// 		map.put("minusDays", minusDays);
	// 		map.put("checkMinusDaysForBulkUpload", checkMinusDaysForBulkUpload);
	// 		response.setServiceResponse(map);
	// 		return response;
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	// 		response.setServiceError(ServiceResponse.STATUS_FAIL);
	// 		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
	// 		return response;
	// 	}
	// }

		 
}
