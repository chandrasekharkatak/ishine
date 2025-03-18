package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.apmosys.employeeportal.dto.Employee360DTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDto;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.Employee360Repository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class Employee360Service {
	
	@Autowired
	Employee360Repository employee360Repository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;

	private String LocalDate;
	
	public ServiceResponse getLeaveDataPerMonthByEmpId(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getLeaveDataPerMonthByEmpId");
		apiLogInfo.setApiUrl("/api/getLeaveDataPerMonthByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + empId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			String[] financialYearDates=getFinancialYearDates();
			System.out.println("financialYearDates====>"+financialYearDates[0]+" "+financialYearDates[1]);
			List<Object[]> employee360 = employee360Repository.getLeaveDataPerMonthByEmpIdFromTo(empId,financialYearDates[0],financialYearDates[1]);
			
			if(!employee360.isEmpty()) {
				
				for (Object[] employee360dto : employee360) { 
					Employee360DTO dto = new Employee360DTO();
					
		            dto.setLeaveYear(employee360dto[0] != null ? employee360dto[0].toString() : null);
		            dto.setLeaveMonth(employee360dto[1] != null ? employee360dto[1].toString() : null);
		            dto.setLeaveType(employee360dto[2] != null ? employee360dto[2].toString() : null);
		            dto.setLeaveStatus(employee360dto[3] != null ? employee360dto[3].toString() : null);
		            dto.setTotalDays(employee360dto[4] != null ? employee360dto[4].toString() : null);
		            dto.setYear(financialYearDates[1]!=null?financialYearDates[1]:null);
				    employeeDtoList.add(dto);
				}
			}else {	
				Employee360DTO dto = new Employee360DTO();
				dto.setYear(financialYearDates[1]!=null?financialYearDates[1]:null);
			    employeeDtoList.add(dto);
				}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeDtoList);
			apiLogInfo.setApiResponse("Employee leave details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public String[] getFinancialYearDates() {
        Calendar calendar = Calendar.getInstance(); 
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // January is 0

        // Determine the financial year
        if (month < Calendar.APRIL) {
            year--; // Move to the previous financial year
        }
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, Calendar.APRIL, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(year+1 , Calendar.MARCH, 31);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return new String[]{formatter.format(startDate.getTime()), formatter.format(endDate.getTime())};
    }
	
	public ServiceResponse getEmployeeDetails(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getEmployeeDetails");
		apiLogInfo.setApiUrl("/api/getEmployeeDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + empId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			
			List<Object[]> objectList = employeeRepository.getEmployeeByEmpId(empId);
			
			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					
					Employee360DTO dto = new Employee360DTO();
					
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setName(object[24] != null ? object[24].toString() : null);
					dto.setManagerId(object[48] != null ? Long.parseLong(object[48].toString()) : null);
					dto.setManagerName(object[43] != null ? object[43].toString() : null);
					dto.setDepartmentName(object[45] != null ? object[45].toString() : null);
					dto.setDepartmentId(object[46] != null ? Long.parseLong(object[46].toString()) : null);
					
					employeeDtoList.add(dto);
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeDtoList);
			apiLogInfo.setApiResponse("Employee leave details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
		
	public ServiceResponse get360TimesheetDetails(String status,long empId,long projectId,long teamName,long managerId, String startDate, String endDate) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("get360TimesheetDetails");
	    apiLogInfo.setApiUrl("/api/get360TimesheetDetails");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("status : " + status);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			Map<Long, Employee360DTO> employeeMap = new HashMap<>();

	        // Parse and convert date strings to LocalDate
	        LocalDate localStartDate = null;
	        LocalDate localEndDate = null;
	        SimpleDateFormat formatedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
	        
	        if (startDate != null && !startDate.isEmpty() && !startDate.equalsIgnoreCase("null") &&
	        	    endDate != null && !endDate.isEmpty() && !endDate.equalsIgnoreCase("null")) {
	            Date startingDate = formatedDate.parse(startDate);
	            Date endingDate = formatedDate.parse(endDate);
	            localStartDate = startingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            localEndDate = endingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	        } else {
	            System.out.println("No date range provided, sending null for dates.");
	        }
	        
	        System.out.println("localStartDate: " + localStartDate + " (Type: " + ((localStartDate != null) ? localStartDate.getClass().getSimpleName() : "null") + ")");
	        System.out.println("localEndDate: " + localEndDate + " (Type: " + ((localEndDate != null) ? localEndDate.getClass().getSimpleName() : "null") + ")");

	        List<Object[]> timesheetData=employeeRepository.getTimesheetData(status,empId,managerId, localStartDate, localEndDate,projectId,teamName);
	        if(!timesheetData.isEmpty()) {
	        	for(Object[] sheet : timesheetData) {
				        Employee360DTO dto = new Employee360DTO();
						dto.setEmpId(Long.parseLong(sheet[1].toString()));
				        dto.setName((String) sheet[2]);
				        dto.setDate(sheet[3]!=null?((java.sql.Date)sheet[3]).toString():null);
				        dto.setDayType(sheet[4] != null ? sheet[4].toString() : null);
				        if (sheet[5] != null) {
				           dto.setOfficeInTime(((java.sql.Timestamp) sheet[5]).toLocalDateTime());}
				        if (sheet[6] != null) {
					           dto.setOfficeOutTime(((java.sql.Timestamp) sheet[6]).toLocalDateTime());}
		                dto.setTotalTime(sheet[7] != null ? sheet[7].toString() : null);
				        dto.setStatus(sheet[8] != null ? sheet[8].toString() : null);
				        dto.setTimesheetId(Long.parseLong(sheet[0].toString()));
				        dto.setRemarks(sheet[9] != null ? sheet[9].toString() : null);
				        dto.setEmploymentId (sheet[10] != null ? Long.parseLong(sheet[10].toString()) : null);
				        if (sheet[11] != null) {
					           dto.setCreatedOn(((java.sql.Timestamp) sheet[11]).toLocalDateTime());}
				        dto.setManagerId(sheet[12]!=null?Long.parseLong(sheet[12].toString()):null);
	        		List<Object[]> activityData=employeeRepository.getActivityData(Long.parseLong(sheet[0].toString()));
	        		List<EmployeeTimesheetDto> activityList = mapActivityData(activityData);
	        		// Set activity list in DTO
	                dto.setTimeSheetlist(activityList);
	                employeeMap.put(dto.getTimesheetId(), dto);
	        	}
		}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeMap);
			apiLogInfo.setApiResponse("Timesheet details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	
	private List<EmployeeTimesheetDto> mapActivityData(List<Object[]> activityData) {
	    List<EmployeeTimesheetDto> activityList = new ArrayList<>();
	    if (!activityData.isEmpty()) {
	        for (Object[] activityDto : activityData) {
	            EmployeeTimesheetDto activity = new EmployeeTimesheetDto();
	            activity.setActivityId(activityDto[0]!=null?Long.parseLong(activityDto[0].toString()):null);
	            activity.setActivity(activityDto[1] != null ? activityDto[1].toString() : null);
	            activity.setCompletionTime(activityDto[3] != null ?Float.parseFloat( activityDto[3].toString()) : null);
	            activity.setTeamId(activityDto[4] != null ? Long.parseLong(activityDto[4].toString()) : null);
	            activity.setTeamName(activityDto[5] != null ? activityDto[5].toString() : null);
	            activity.setProjectId(activityDto[6] != null ?Long.parseLong( activityDto[6].toString()) : null);
	            activity.setProjectName(activityDto[7] != null ? activityDto[7].toString() : null);
	            activity.setTimesheetId(activityDto[2] != null ? Long.parseLong(activityDto[2].toString()) : 0);
	            activityList.add(activity);
	        }
	    }
	    return activityList;
	}

	
	public ServiceResponse updateStatus(String status,List<Long>timesheetId,Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get360TimesheetDetails");
		apiLogInfo.setApiUrl("/api/updateStatus");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timesheetId : " + timesheetId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			List<Timesheet> timesheet = timesheetsRepository.findByTimesheetIdIn(timesheetId);
			
			if (!timesheet.isEmpty()) {
				for(Timesheet timesheetobj:timesheet){
					timesheetobj.setStatus(status);
					timesheetobj.setTimesheetStatusUpdatedBy(updatedBy);
//					timesheetobj.setUpdatedOn(new Date());
//					timesheetobj.setRemarks(timesheetDTO.getRejectReason());
		            timesheetsRepository.save(timesheetobj);
				}
				}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Updated Successfully!!");
			apiLogInfo.setApiResponse("Employee_Timesheet details updated.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse getAll360LeaveApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employee Emp Id : "+leaveDTO.getEmpId());
		
		try {
			
			 LocalDate localFromDate = null;
		     LocalDate localToDate = null;
		     SimpleDateFormat formatedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		        
			
			if (leaveDTO.getFromDate() != null && !leaveDTO.getFromDate().isEmpty() && !leaveDTO.getFromDate().equalsIgnoreCase("null") &&
					leaveDTO.getToDate() != null && !leaveDTO.getToDate().isEmpty() && !leaveDTO.getToDate().equalsIgnoreCase("null")) {
	            Date fromDate = formatedDate.parse(leaveDTO.getFromDate());
	            Date toDate = formatedDate.parse(leaveDTO.getToDate());
	            localFromDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            localToDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	        } else {
	            System.out.println("No date range provided, sending null for dates.");
	        }
			
			List<Object[]> list = employeeLeaveRepository
					.getAllLeaveApplicationsByEmpIdAndFromDate(leaveDTO.getEmpId(),localFromDate,localToDate);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {LeaveDTO dto = new LeaveDTO();
				dto.setEmployeeName(object[0] != null ? object[0].toString() : null);
				dto.setFromDate(object[1] != null ? object[1].toString() : null);
				dto.setToDate(object[2] != null ? object[2].toString() : null);
				dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
				dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
				dto.setStatus(object[5] != null ? object[5].toString() : null);
				dto.setReason(object[6] != null ? object[6].toString() : null);
				dto.setLeaveType(object[7] != null ? object[7].toString() : null);
				dto.setLeaveStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
				dto.setLeaveStatusUpdatedBy(object[9] != null ? Long.parseLong(object[9].toString()) : null);
				dto.setLeaveId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
				dto.setRemark(object[11] != null ? object[11].toString() : null);
				dto.setApproverName(object[12] != null ? object[12].toString() : null);
				dto.setApproverId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
				dto.setApproverEmail(object[14] != null ? object[14].toString() : null);
				dto.setManagerApprovalStatus(object[15] != null ? object[15].toString() : null);
				
				dto.setLevel2ApproverId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
				dto.setLevel2ApproverName(object[17] != null ? object[17].toString() : null);
				dto.setLevel2ApproverEmail(object[18] != null ? object[18].toString() : null);
				dto.setLevel2ApprovalStatus(object[19] != null ? object[19].toString() : null);
				dto.setLevel3ApproverId(object[20] != null ? Long.parseLong(object[20].toString()) : null);
				dto.setLevel3ApproverName(object[21] != null ? object[21].toString() : null);
				dto.setLevel3ApprovalStatus(object[22] != null ? object[22].toString() : null);
				dto.setLevel3ApproverEmail(object[23] != null ? object[23].toString() : null);
				dto.setCurrentApprovalLevel(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
				dto.setFinalApprovalLevel(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
				dto.setCreatedByName(object[26] != null ? object[26].toString() : null);
				dto.setLeaveEmpId(object[27] != null ? Long.parseLong(object[27].toString()) : null);
				dto.setEmployeementId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
				dto.setLeaveTypeMasterId(object[29] != null ? Short.parseShort(object[29].toString()) : null);
				
				dtoList.add(dto);	
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Applications found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	
	public ServiceResponse get360PendingCompOffRequestsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_reportee_comp_off_applications ");
		apiLogInfo.setApiUrl("/api/getPendingCompOffRequestsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + leaveDTO.getEmpId() );
		try {

			List<Object[]> objectList = compOffLeaveRepository
					.getPendingCompOffRequestsByEmpIdAndStatus(leaveDTO.getEmpId());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");

			} else {

				objectList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();

					dto.setCompOffLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCompOffReasons(object[1] != null ? object[1].toString() : null);
					dto.setDescription(object[2] != null ? object[2].toString() : null);
					dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
					dto.setCreatedByName(object[4] != null ? object[4].toString() : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setFromDate(object[6] != null ? object[6].toString() : null);
					dto.setToDate(object[7] != null ? object[7].toString() : null);
					dto.setNoOfDays(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setEmail(object[10] != null ? object[10].toString() : null) ;
					dto.setEmployeementId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					dto.setManagerId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
					dto.setManagerEmail(object[13] != null ? object[13].toString() : null);
					dto.setFinalApprovalLevel(object[14] != null ? Integer.parseInt(object[14].toString()) : null);
					dto.setLevel2ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					dto.setManagerApprovalStatus(object[16] != null ? object[16].toString() : null);	
					dto.setReportingManagerId(object[17] != null ? Long.parseLong(object[17].toString()) : null);
					dto.setCurrentApprovalLevel(object[18] != null ? Integer.parseInt(object[18].toString()) : null);	
					dto.setLevel2ApprovalStatus(object[19] != null ? object[19].toString() : null);
					dto.setLevel2ApproverName(object[20] != null ? object[20].toString() : null);
					dto.setApproverName(object[21] != null ? object[21].toString() : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList" +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
}
