package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ChartsCountDTO;
import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.DepartmentBillableDTO;
import com.apmosys.employeeportal.dto.DepartmentWiseCountDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LeaveTrendAnalysisDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PieChartListDTO;
import com.apmosys.employeeportal.dto.PieParamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ReportCountDTO;
import com.apmosys.employeeportal.dto.ReportListDTO;
import com.apmosys.employeeportal.dto.ReportsQueryDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.WorkLocationCountDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ReportDashboardRepository;
import com.apmosys.employeeportal.repository.ReportDashboardRepository;
import com.apmosys.employeeportal.repository.ReportDashboardRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.serviceInterface.ReportDashboardService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.ToLong_helper;

@Service
public class ReportDashboardServiceImpl implements ReportDashboardService {

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	 @Autowired
	 private CustomFilterService customFilterService;

	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired	
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	TimesheetActivityMapRepository timesheetActivityMapRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	
	@Autowired
	private ReportDashboardRepository reportDashboardRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	ConcurrentHashMap<String, List<Object[]>> worklocationMap = new ConcurrentHashMap<String, List<Object[]>>();

	@Override
	public ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
       // apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getLast8DaysLeaveReport");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("StartDate : " + leaveDto.getStartDate() + " , EndDate : " + leaveDto.getEndDate());
		try {
			List<Object[]> list = employeeLeaveRepository.getLast8DaysLeaveReport(stringToDateTimeParser.getDate(leaveDto.getStartDate(), "yyyy-MM-dd"), stringToDateTimeParser.getDate(leaveDto.getEndDate(), "yyyy-MM-dd"));

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave history not found. Kindly check date range.");
                apiLogInfo.setApiResponse("Leave history not found. kindly check date range");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setDepartmentName(object[1] != null ? object[1].toString() : null);
					dto.setEmployeeName(object[2] != null ? object[2].toString() : null);
					dto.setFromDate(object[3] != null ? object[3].toString() : null);
					dto.setToDate(object[4] != null ? object[4].toString() : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setLeaveType(object[6] != null ? object[6].toString() : null);
					dto.setFromDateDayType(object[7] != null ? Float.parseFloat(object[7].toString()) : null);
					dto.setToDateDayType(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setIsConsultant(object[9] != null ? object[9].toString() : null);
					dto.setIsApprenticeship(object[10] != null ? object[10].toString() : null);
					dto.setEmpId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					dto.setManagerId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("dtoList" + dtoList.size());
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

	@Override
	public ServiceResponse getLast9DaysTimesheetReport() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getLast9DaysTimesheetReport");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("StartDate : " + LocalDate.now().minusDays(8)  + " ,EndDate :" + LocalDate.now().minusDays(1));

		try {
			LocalDate start = LocalDate.now().minusDays(8);

			LocalDate end = LocalDate.now().minusDays(1);

			List<Object[]> timesheetList = timesheetsRepository.getLast9DaysPendingTimesheetReport(start, end);

			List<Object[]> employeeList = employeeRepository.getAllEmployees();

			List<Object[]> filledTimesheetList = timesheetsRepository.getLast9DaysFilledTimesheetReport(start, end);

			List<TimesheetDTO> dtoList = new ArrayList<>();

			if (timesheetList != null) {
				employeeList.forEach((employee) -> {
					
					TimesheetDTO dto = new TimesheetDTO();

					dto.setEmployeementId(employee[0] != null ? Long.parseLong(employee[0].toString()) : null);
					dto.setEmployeeName(employee[29] != null ? employee[29].toString() : null);
					dto.setDepartmentName(employee[47] != null ? employee[47].toString() : null);
					dto.setEmail(employee[14] != null ? employee[14].toString() : null);
					dto.setMobileNo(employee[27] != null ? Long.parseLong(employee[27].toString()) : null);
					dto.setManagerName(employee[51] != null ? employee[51].toString() : null);
					dto.setEmpId(employee[50] != null ? Long.parseLong(employee[50].toString()) : null);
					dto.setPendingEodCount(8L);
					dto.setLegend("Pending By User");
					dto.setEmploymentstatus(employee[17] != null ? employee[17].toString() : null);
					dto.setIsConsultant(employee[76] != null? employee[76].toString() : null);
					dto.setManagerId(employee[25] != null ? Long.parseLong(employee[25].toString()) : null);
					timesheetList.forEach((timesheet) -> {

						Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
						Long employeeEmpId = employee[50] != null ? Long.parseLong(employee[50].toString()) : null;

						if (timesheetEmpId.equals(employeeEmpId)) {

							Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;

							Long pendingEodCount = 8 - filledEodCount;

							dto.setPendingEodCount(pendingEodCount);

						}

					});
					dtoList.add(dto);
				});

				if (filledTimesheetList != null) {
					filledTimesheetList.forEach((filledTimesheet) -> {
						TimesheetDTO dto = new TimesheetDTO();
						
						dto.setLegend(filledTimesheet[0] != null ? filledTimesheet[0].toString() : null);
						dto.setEmployeementId(filledTimesheet[1] != null ? Long.parseLong(filledTimesheet[1].toString()) : null);
						dto.setEmployeeName(filledTimesheet[2] != null ? filledTimesheet[2].toString() : null);
						dto.setDepartmentName(filledTimesheet[3] != null ? filledTimesheet[3].toString() : null);
						dto.setEmail(filledTimesheet[4] != null ? filledTimesheet[4].toString() : null);
						dto.setMobileNo(filledTimesheet[5] != null ? Long.parseLong(filledTimesheet[5].toString()) : null);
						dto.setDate(filledTimesheet[6] != null ? filledTimesheet[6].toString() : null);
						dto.setTotalWorkingHours(filledTimesheet[7] != null ? Float.parseFloat(filledTimesheet[7].toString()) : null);
						dto.setDayType(filledTimesheet[8] != null ? filledTimesheet[8].toString() : null);
						dto.setManagerName(filledTimesheet[9] != null ? filledTimesheet[9].toString() : null);
						dtoList.add(dto);
					});
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("TimeSheetReport :" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found. Kindly check date range.");
                apiLogInfo.setApiResponse("Timesheet not found.Kindly check data range.");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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

	
	@Override	
	public ServiceResponse getLeaveTrendAnalysisReport(LeaveDTO leaveDto) {	
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getLeaveTrendAnalysisReport");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("FromDate : " + leaveDto.getFromDate() + " ,ToDate : " + leaveDto.getToDate());
		try {	
			ServiceResponse lastEightDayResponse = getLast8DaysLeaveReport(leaveDto);	
				
			if(lastEightDayResponse.getServiceStatus().equals("Success")) {	
				List<LeaveDTO> leavedata = (List<LeaveDTO>)lastEightDayResponse.getServiceResponse();	
					
				List<LeaveDTO> newLeaveData = new ArrayList<>();	
			
				if(leavedata != null) {	
					leavedata.forEach((obj) -> {	
						LocalDate tempdate = LocalDate.parse(obj.getFromDate());	
						LocalDate toDate = LocalDate.parse(obj.getToDate());	
						long i = 0L;	
							
						while(i <= ChronoUnit.DAYS.between(tempdate, toDate)) {	
							LeaveDTO dto = new LeaveDTO();	
								
							dto.setEmployeementId(obj.getEmployeementId());	
							dto.setDepartmentName(obj.getDepartmentName());	
							dto.setEmployeeName(obj.getEmployeeName());	
							dto.setFromDate(tempdate.toString());	
							dto.setToDate(obj.getToDate());	
							dto.setStatus(obj.getStatus());	
							dto.setLeaveType(obj.getLeaveType());	
							newLeaveData.add(dto);	
								
							tempdate = tempdate.plusDays(1);	
						}	
					});	
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
					response.setServiceResponse(newLeaveData);
					apiLogInfo.setApiResponse("newLeaveData :"+ newLeaveData.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}	
			}		
		}catch(Exception e) {	
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

	@Override
	public ServiceResponse getEmployeeWorkLocationForSummary() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
       // apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getEmployeeWorkLocationForSummary");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
       

		try {
			
			List<TimesheetDTO> dtoList = new ArrayList<>();
			
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			calendar.add(Calendar.MONTH, -1);
			calendar.set(Calendar.DATE, 1);

			LocalDate firstDateOfPreviousMonth = LocalDate.parse(dateFormat.format(calendar.getTime()));
			
			calendar.set(Calendar.DATE,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			LocalDate lastDateOfPreviousMonth = LocalDate.parse(dateFormat.format(calendar.getTime()));
			
			List<Object[]> employeeList = employeeRepository.getEmployeeDetailForCron();
			 
			for(Object[] empObj : employeeList) {
				
				Long empId = empObj[0] != null ? Long.parseLong(empObj[0].toString()) : null;
				Long employeementId = empObj[3] != null ? Long.parseLong(empObj[3].toString()) : null;
				
				
//				System.out.println("Emp ID :" + empId);
				
				List<Timesheet> monthlyTimesheet = timesheetsRepository.
						findAllByEmpIdAndDateBetweenOrderByDateDesc(empId, firstDateOfPreviousMonth, lastDateOfPreviousMonth);
					
						for(Timesheet timesheetObj: monthlyTimesheet) {								
							List<Object[]> objectList = timesheetActivityMapRepository.activitiesByTimesheetId(timesheetObj.getTimesheetId());
							
							if(!objectList.isEmpty()) {
								for(Object[] object : objectList) {
									TimesheetDTO dto = new TimesheetDTO();
									
									dto.setEmpId(object[0] != null ? Long.parseLong(empObj[0].toString()): null);									
									dto.setEmployeeName(object[9] != null ? object[9].toString() : null);
									dto.setProjectName(object[5] != null ? object[5].toString() : null);
									dto.setClientName(object[6] != null ? object[6].toString() : null);
									dto.setClientLocation(object[7] != null ? object[7].toString() : null);
									dto.setTeamName(object[8] != null ? object[8].toString() : null);
									dto.setManagerName(object[10] != null ? object[10].toString() : null);
									dto.setDate(timesheetObj.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
									dto.setEmployeementId(employeementId);
									dto.setIsConsultant(object[17] != null ? object[17].toString() : null);
									dto.setIsApprenticeship(object[18] != null ? object[18].toString() : null);									dtoList.add(dto);
								}
								logBuilder.append("EmpId: " + empId);
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Timesheet Activities not found.");
                                apiLogInfo.setApiResponse("Timesheet Activities not found");			
                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}
						}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
			response.setServiceResponse(dtoList);

            apiLogInfo.setApiResponse("dtoList: " + dtoList.size());			
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
		}catch(Exception e) {
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

//	public ServiceResponse getDepartmentWiseBillableData(LeaveDTO leaveDto) {
//		ServiceResponse response = new ServiceResponse();
//		 
//		try {
//			List<Object[]> getAllBillableEmployee = employeeRepository.getBillableEmpWithDepartment();
//			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//			
//			getAllBillableEmployee.forEach((object)->{
//				EmployeeDTO dto = new EmployeeDTO();
//				
//				dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//				dto.setEmail(object[1] != null ? object[1].toString() : null);
//				dto.setName(object[2] != null ? object[2].toString() : null);
//				dto.setManagerName(object[3] != null ? object[3].toString() : null);
//				dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
//				dto.setHodName(object[5] != null ? object[5].toString() : null);
//				dto.setBillable(object[6] != null ? object[6].toString() : null);
//				dto.setBillableType(object[7] != null ? object[7].toString() : null);
//				dto.setMobileNo(object[8] != null ? Long.parseLong(object[8].toString()): null);
//				dto.setMothersName(object[9] != null ? object[9].toString() : null);
//				dto.setApprovalsTo(object[10] != null ? object[10].toString() : null);
//				dto.setMaritalStatus(object[11] != null ? object[11].toString() : null);	
//				dto.setProjectName(object[12] != null ? object[12].toString() : null);
//				dto.setClientName(object[13] != null ? object[13].toString() : null);
//				dto.setGender(object[14] != null ? object[14].toString() : null);
//				dto.setEmploymentstatus(object[15] != null ? object[15].toString() : null);
//				dto.setTotalExperience(object[16] != null ? Float.parseFloat(object[16].toString()) : null);	
//				dto.setDateOfBirth(object[17] != null ? object[17].toString() : null);
//				dto.setDateOfJoining(object[18] != null ? object[18].toString() : null);
//				dto.setWorkLocation(object[19] != null ? object[19].toString() : null);
//				dto.setExperience(object[20] != null ? object[20].toString() : null);
//				dto.setEmpId(object[21] != null ? Long.parseLong(object[21].toString()) : null);
//				dto.setTeamName(object[22] != null ? object[22].toString() : null);
//				dto.setIsConsultant(object[23] != null ? object[23].toString() : null);
//				dto.setIsApprenticeship(object[24] != null ? object[24].toString() : null);
//						
//				if (object[25] != null && object[12] != null) {
//				 String[] projectIds = object[25].toString().split(",");
//				 String[] projectNames = object[12].toString().split(",");
//				 
//				 List<ProjectDTO> projectList = new ArrayList<>();
//				 for (int i = 0; i < projectIds.length; i++) {
//					 ProjectDTO projectDTO = new ProjectDTO();
//				        projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
//				        projectDTO.setProjectName(projectNames[i].trim());
//				        projectList.add(projectDTO);
//				    }
//				    dto.setProjectList(projectList);
//				}
//				
//				
//				
////				ServiceResponse completionResponse = profileCompletionReport(dto);
////				EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
////				
////				dto.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
//				
//				ServiceResponse completionResponse = profileCompletionReport(dto);
//				Object emp = completionResponse.getServiceResponse();
//
//				if (emp instanceof EmployeeDTO) {
//				    EmployeeDTO employeeDTO = (EmployeeDTO) emp;
//				    dto.setProfileCompletedPercent(employeeDTO.getProfileCompletedPercent());
//				} else {
//				    dto.setProfileCompletedPercent(0.00);
//				    if (emp instanceof String) {
//				        System.out.println("ServiceResponse message: " + emp);
//				    }
//				}
//
//				
//				
//				dtoList.add(dto);
//				
//			});
//			
//			if(dtoList != null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//				System.out.println(dtoList);
//				System.err.println("__________________________________________________---------------_________---------________-----______---____--____-");
//			}
//			else {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse("List is empty !!");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something went wrong !!");
//			
//		}
//		
//		return response;
//	}
	
	public ServiceResponse getDepartmentWiseBillableData(LeaveDTO leaveDto) {
	    ServiceResponse response = new ServiceResponse();
	    
	    try {
	        List<Object[]> getAllBillableEmployee = employeeRepository.getBillableEmpWithDepartment();
	        List<EmployeeDTO> dtoList = new ArrayList<>();

	        getAllBillableEmployee.forEach((object) -> {
	            EmployeeDTO dto = new EmployeeDTO();

	            dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	            dto.setEmail(object[1] != null ? object[1].toString() : null);
	            dto.setName(object[2] != null ? object[2].toString() : null);
	            dto.setManagerName(object[3] != null ? object[3].toString() : null);
	            dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
	            dto.setHodName(object[5] != null ? object[5].toString() : null);
	            dto.setBillable(object[6] != null ? object[6].toString() : null);
	            dto.setBillableType(object[7] != null ? object[7].toString() : null);
	            dto.setMobileNo(object[8] != null ? Long.parseLong(object[8].toString()) : null);
	            dto.setMothersName(object[9] != null ? object[9].toString() : null);
	            dto.setApprovalsTo(object[10] != null ? object[10].toString() : null);
	            dto.setMaritalStatus(object[11] != null ? object[11].toString() : null);
	            dto.setProjectName(object[12] != null ? object[12].toString() : null);
	            dto.setClientName(object[13] != null ? object[13].toString() : null);
	            dto.setGender(object[14] != null ? object[14].toString() : null);
	            dto.setEmploymentstatus(object[15] != null ? object[15].toString() : null);
	            dto.setTotalExperience(object[16] != null ? Float.parseFloat(object[16].toString()) : null);
	            dto.setDateOfBirth(object[17] != null ? object[17].toString() : null);
	            dto.setDateOfJoining(object[18] != null ? object[18].toString() : null);
	            dto.setWorkLocation(object[19] != null ? object[19].toString() : null);
	            dto.setExperience(object[20] != null ? object[20].toString() : null);
	            dto.setEmpId(object[21] != null ? Long.parseLong(object[21].toString()) : null);
	            dto.setTeamName(object[22] != null ? object[22].toString() : null);
	            dto.setIsConsultant(object[23] != null ? object[23].toString() : null);
	            dto.setIsApprenticeship(object[24] != null ? object[24].toString() : null);
	            dto.setManagerId(object[26] != null ? Long.parseLong(object[26].toString()) : null);
	           
	            if (object[25] != null && object[12] != null) {
	                String projectIdStr = object[25].toString().trim();
	                String projectNameStr = object[12].toString().trim();

	                
	                if (!projectIdStr.isEmpty() && !projectNameStr.isEmpty()) {
	                    String[] projectIds = projectIdStr.split(",");
	                    String[] projectNames = projectNameStr.split(",");

	                    
	                    List<ProjectDTO> projectList = new ArrayList<>();
	                    int length = Math.min(projectIds.length, projectNames.length);
	                    
	                    for (int i = 0; i < length; i++) {
	                        try {
	                            ProjectDTO projectDTO = new ProjectDTO();
	                            projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
	                            projectDTO.setProjectName(projectNames[i].trim());
	                            projectList.add(projectDTO);
	                        } catch (NumberFormatException e) {
	                            System.err.println("Invalid projectId: " + projectIds[i]);
	                        }
	                    }
	                    dto.setProjectList(projectList);
	                }
	            }

	            // Profile completion check
	            ServiceResponse completionResponse = profileCompletionReport(dto);
	            Object emp = completionResponse.getServiceResponse();

	            if (emp instanceof EmployeeDTO) {
	                EmployeeDTO employeeDTO = (EmployeeDTO) emp;
	                dto.setProfileCompletedPercent(employeeDTO.getProfileCompletedPercent());
	            } else {
	                dto.setProfileCompletedPercent(0.00);
	                if (emp instanceof String) {
	                    System.out.println("ServiceResponse message: " + emp);
	                }
	            }

	            dtoList.add(dto);
	        });

	        if (!dtoList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("List is empty !!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong !!");
	    }

	    return response;
	}

	
	public ServiceResponse profileCompletionReport(EmployeeDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		Double proileCompleted = 0.00;
		Double totalFields = 0.00;
		
		try {
			
			List<Object[]> employeeProile = employeeRepository.getEmployeeProfileCompletion(leaveDto.getEmpId());
			
			
			if (employeeProile.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Profile not found !!");
			} else {
				Object[] employee = employeeProile.get(0);
				totalFields = (double) employee.length;
						
				for(int i = 0; i < employee.length; i++) {
					if(employee[i] != null) {
						proileCompleted++;
					}
				}
				
				Double profileCompletedPercent = (proileCompleted/totalFields)*100;
				
				leaveDto.setProfileCompletedPercent(Double.parseDouble(df.format(profileCompletedPercent)));
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(leaveDto);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}	
		
		return response;
	}
	//employee summary status graph
	public ServiceResponse getAllGraphEmployeeSummary() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllGraphEmployeeSummary");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	        List<Object[]> object = reportDashboardRepository.getAllGraphEmployeeSummary();
	        List<ReportCountDTO> dtoList = new ArrayList<>();
	        
	        object.forEach((count) -> {
	            ReportCountDTO reportDto = new ReportCountDTO();
	            
	            // Billable Status 
	            reportDto.setBillableYes(count[0] != null ? Long.parseLong(count[0].toString()) : 0L);
	            reportDto.setBillableNo(count[1] != null ? Long.parseLong(count[1].toString()) : 0L);
	            reportDto.setBillableOther(count[2] != null ? Long.parseLong(count[2].toString()) : 0L);
	            
	            // Age Groups 
	            reportDto.setAge18to25(count[3] != null ? Long.parseLong(count[3].toString()) : 0L);
	            reportDto.setAge25to35(count[4] != null ? Long.parseLong(count[4].toString()) : 0L);
	            reportDto.setAge35to45(count[5] != null ? Long.parseLong(count[5].toString()) : 0L);
	            reportDto.setAgeAbove45(count[6] != null ? Long.parseLong(count[6].toString()) : 0L);
	            
	            // Gender 
	            reportDto.setGenderMale(count[7] != null ? Long.parseLong(count[7].toString()) : 0L);
	            reportDto.setGenderFemale(count[8] != null ? Long.parseLong(count[8].toString()) : 0L);
	            reportDto.setGenderOther(count[9] != null ? Long.parseLong(count[9].toString()) : 0L);
	            
	            // Employment Status 
	            reportDto.setEmployeeStatusConfirmed(count[10] != null ? Long.parseLong(count[10].toString()) : 0L);
	            reportDto.setEmployeeStatusResigned(count[11] != null ? Long.parseLong(count[11].toString()) : 0L);
	            reportDto.setEmployeeStatusProbation(count[12] != null ? Long.parseLong(count[12].toString()) : 0L);
	            reportDto.setEmployeeStatusRetain(count[13] != null ? Long.parseLong(count[13].toString()) : 0L);
	            reportDto.setEmployeeStatusInActive(count[14] != null ? Long.parseLong(count[14].toString()) : 0L);
	            
	            // Experience Type 
	            reportDto.setLateralCount(count[15] != null ? Long.parseLong(count[15].toString()) : 0L);
	            reportDto.setFresherCount(count[16] != null ? Long.parseLong(count[16].toString()) : 0L);
	            
	            // Apprentice Years 
	            reportDto.setApprenticeYears0to1(count[17] != null ? Long.parseLong(count[17].toString()) : 0L);
	            reportDto.setApprenticeYears1to2(count[18] != null ? Long.parseLong(count[18].toString()) : 0L);
	            reportDto.setApprenticeYears2to5(count[19] != null ? Long.parseLong(count[19].toString()) : 0L);
	            reportDto.setApprenticeYears5to10(count[20] != null ? Long.parseLong(count[20].toString()) : 0L);
	            reportDto.setApprenticeYearsAbove10(count[21] != null ? Long.parseLong(count[21].toString()) : 0L);
	            
	            // Employee Years 
	            reportDto.setEmployeeYears0to1(count[22] != null ? Long.parseLong(count[22].toString()) : 0L);
	            reportDto.setEmployeeYears1to2(count[23] != null ? Long.parseLong(count[23].toString()) : 0L);
	            reportDto.setEmployeeYears2to5(count[24] != null ? Long.parseLong(count[24].toString()) : 0L);
	            reportDto.setEmployeeYears5to10(count[25] != null ? Long.parseLong(count[25].toString()) : 0L);
	            reportDto.setEmployeeYearsAbove10(count[26] != null ? Long.parseLong(count[26].toString()) : 0L);
	            
	            // Consultant Years 
	            reportDto.setConsultantYear0to1(count[27] != null ? Long.parseLong(count[27].toString()) : 0L);
	            reportDto.setConsultantYear1to2(count[28] != null ? Long.parseLong(count[28].toString()) : 0L);
	            reportDto.setConsultantYear2to5(count[29] != null ? Long.parseLong(count[29].toString()) : 0L);
	            reportDto.setConsultantYear5to10(count[30] != null ? Long.parseLong(count[30].toString()) : 0L);
	            reportDto.setConsultantYearAbove10(count[31] != null ? Long.parseLong(count[31].toString()) : 0L);
	           
	            // Billable Types 
	            reportDto.setFixedCost(count[32] != null ? Long.parseLong(count[32].toString()) : 0L);
	            reportDto.setTNM(count[33] != null ? Long.parseLong(count[33].toString()) : 0L);
	            reportDto.setBench(count[34] != null ? Long.parseLong(count[34].toString()) : 0L);
	            reportDto.setShadow(count[35] != null ? Long.parseLong(count[35].toString()) : 0L);
	            reportDto.setInternalRNDProducts(count[36] != null ? Long.parseLong(count[36].toString()) : 0L);
	            
	            // Employee Counts 
	            reportDto.setTotalEmployeeCountDisplay(count[37] != null ? Long.parseLong(count[37].toString()) : 0L);
	            reportDto.setProbationCountDisplay(count[38] != null ? Long.parseLong(count[38].toString()) : 0L);
	            reportDto.setApprenticeCountDisplay(count[39] != null ? Long.parseLong(count[39].toString()) : 0L);
	            reportDto.setConsultantCountDisplay(count[40] != null ? Long.parseLong(count[40].toString()) : 0L);
	            reportDto.setRegularCountDisplay(count[41] != null ? Long.parseLong(count[41].toString()) : 0L);
	            
	            dtoList.add(reportDto);
	        });
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	    }
	    
	    return response;
	}
	
	public ServiceResponse getAllPieGraphListSummary(PieParamDTO pieParamDto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllPieGraphListSummary");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	    	List<Object[]> object = customFilterService.getCustomPieGraphListSummary(pieParamDto, pieParamDto.getQueryList());

	        List<ReportListDTO> dtoList = new ArrayList<>();

	        object.forEach((obj) -> {
	        	ReportListDTO dto = new ReportListDTO();
	            dto.setEmployeementId(obj[0] != null ? obj[0].toString() : null);
	            dto.setEmployeeType(obj[1] != null ? obj[1].toString() : null);
	            dto.setName(obj[2] != null ? obj[2].toString() : null);
	            dto.setExperience(obj[3] != null ? obj[3].toString() : null);
	            dto.setDepartmentName(obj[4] != null ? obj[4].toString() : null);
	            dto.setEmail(obj[5] != null ? obj[5].toString() : null);
	            dto.setManagerName(obj[6] != null ? obj[6].toString() : null);
	            dto.setBillable(obj[7] != null ? obj[7].toString() : null);
	            dto.setBillableType(obj[8] != null ? obj[8].toString() : null);
	            dto.setProjectName(obj[9] != null ? obj[9].toString() : null);
	            dto.setClientName(obj[10] != null ? obj[10].toString() : null);
	            dto.setDateOfJoining(obj[11] != null ? obj[11].toString() : null);
	            dto.setMobileNo(obj[12] != null ? obj[12].toString() : null);
	            dto.setEmploymentstatus(obj[13] != null ? obj[13].toString() : null);
	            dto.setTotalExperience(obj[14] != null ? obj[14].toString() : null);
	            dto.setGender(obj[15] != null ? obj[15].toString() : null);
	            dto.setWorkLocation(obj[16] != null ? obj[16].toString() : null);
	            dto.setAge(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
	            dto.setProfileKycStatus(obj[18] != null ? obj[18].toString() : null);

	            dtoList.add(dto);
	        });

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}
	
	public ServiceResponse getDepartmentWiseKycCount() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getDepartmentWiseKycCount");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	        logBuilder.append("Fetching department-wise KYC count data");
	        
	        List<Object[]> rawData = reportDashboardRepository.getDepartmentWiseKycCount();
	        
	        if (rawData != null && !rawData.isEmpty()) {
	            List<DepartmentWiseCountDTO> kycCountList = rawData.stream()
	                .map(row -> new DepartmentWiseCountDTO(
	                    (String) row[0],     
	                    (String) row[1],      
	                    ((Number) row[2]).longValue()  
	                ))
	                .collect(Collectors.toList());
	            
	            // Optional: Group by department 
//	            Map<String, List<DepartmentWiseCountDTO>> groupedByDepartment = kycCountList.stream()
//	                .collect(Collectors.groupingBy(DepartmentWiseCountDTO::getDepartmentName));
	            
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(kycCountList); 
	            
	            logBuilder.append(" - Successfully retrieved ").append(kycCountList.size()).append(" records");
	            
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(new ArrayList<>());
	            logBuilder.append(" - No data found");
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while fetching department-wise KYC count.");
	        response.setServiceError(e.getMessage());
	        
	        logBuilder.append(" - Error occurred: ").append(e.getMessage());
	    }
	    
	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse getJoiningVsResignationCount(ReportsQueryDTO request) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getJoiningVsResignationCount");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		
try {
	List<Object[]> counts = reportDashboardRepository.getJoiningVsResignationCount(
            request.getEmployeementId(),
            request.getName(),
            request.getDeptId(),
            request.getJobRoleId(),
            request.getManagerId(),
            request.getTeamId(),
            request.getProjectId(),
            request.getClientId(),
            request.getEmploymentstatus(),
            request.getDateOfJoining(),
            request.getCity(),
            request.getBloodGroup(),
            request.getGender(),
            request.getProbationPeriod(),
            request.getNoticePeriod(),
            request.getMaritalStatus(),
            request.getBankName(),
            request.getState(),
            request.getCreatedOn(),
            request.getCreatedBy(),
            request.getExperience(),
            request.getWorkLocation(),
            request.getYear()
            );
    
    if (counts != null && !counts.isEmpty()) {
        List<ChartsCountDTO> dtoList = new ArrayList<>();
        
    	counts.forEach((object)-> {
    		ChartsCountDTO dto = new ChartsCountDTO();
    		
    		dto.setMonthName(object[0] !=null? object[0].toString() : null);
    		dto.setYear(object[1] !=null ? Integer.parseInt(object[1].toString()) : null);
    		dto.setApprenticeCount(object[2] != null ? Long.parseLong(object[2].toString()): null);
    		dto.setConsultantCount(object[3] != null ? Long.parseLong(object[3].toString()): null);
    		dto.setRegularCount(object[4] != null ? Long.parseLong(object[4].toString()): null);
    		dto.setResignCount(object[5] != null ? Long.parseLong(object[5].toString()): null);
    		dtoList.add(dto);
    	});

        
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(dtoList);
        
        logBuilder.append(" - Successfully retrieved ").append(counts.size()).append(" records");
        
    } else {
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(new ArrayList<>());
        logBuilder.append(" No data found");
    }

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	public ServiceResponse getAllEmployeeCountDepartmentWise() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getAllEmployeeCountDepartmentWise");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		
   try {
                
	
	 List<Object[]> data=reportDashboardRepository.getAllEmployeeCountDepartmentWise();
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(data);

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;


	}
	

//	@Override
//	public ServiceResponse getDepartmentWiseBillableNonBillableSummary(ReportsQueryDTO request) {
//	    ServiceResponse response = new ServiceResponse();
//	    ToLong_helper toLong_helper = new ToLong_helper();
//	    List<DepartmentBillableDTO> data;
//	    
//	    try {
//	      
//
//	        boolean isFilterEmpty =
//	            (request.getDeptId() == null || request.getDeptId().isEmpty()) &&
//	            (request.getEmployeementId() == null || request.getEmployeementId().isEmpty()) &&
//	            (request.getName() == null || request.getName().isEmpty()) &&
//	            (request.getJobRoleId() == null || request.getJobRoleId().isEmpty()) &&
//	            (request.getManagerId() == null || request.getManagerId().isEmpty()) &&
//	            (request.getTeamId() == null || request.getTeamId().isEmpty()) &&
//	            (request.getProjectId() == null || request.getProjectId().isEmpty()) &&
//	            (request.getClientId() == null || request.getClientId().isEmpty()) &&
//	            (request.getEmploymentstatus() == null || request.getEmploymentstatus().isEmpty()) &&
//	            (request.getDateOfJoining() == null || request.getDateOfJoining().isEmpty()) &&
//	            (request.getCity() == null || request.getCity().isEmpty()) &&
//	            (request.getBloodGroup() == null || request.getBloodGroup().isEmpty()) &&
//	            (request.getGender() == null || request.getGender().isEmpty()) &&
//	            (request.getProbationPeriod() == null || request.getProbationPeriod().isEmpty()) &&
//	            (request.getNoticePeriod() == null || request.getNoticePeriod().isEmpty()) &&
//	            (request.getMaritalStatus() == null || request.getMaritalStatus().isEmpty()) &&
//	            (request.getBankName() == null || request.getBankName().isEmpty()) &&
//	            (request.getState() == null || request.getState().isEmpty()) &&
//	            (request.getCreatedOn() == null || request.getCreatedOn().isEmpty()) &&
//	            (request.getCreatedBy() == null || request.getCreatedBy().isEmpty()) &&
//	            (request.getExperience() == null || request.getExperience().isEmpty()) &&
//	            (request.getWorkLocation() == null || request.getWorkLocation().isEmpty()) &&
//	            (request.getYear() == null) &&
//	            (request.getBillableType() == null || request.getBillableType().isEmpty());
//
//	        if (isFilterEmpty) {
//	            data = reportDashboardRepository.getDepartmentWiseBillableNonBillableSummary();
//	        } else {
//	        	List<Object[]> rawData = reportDashboardRepository.getSelectedDepartmentBillableSummary(
//	                    request.getEmployeementId(),       // List<Long>
//	                     request.getName(),                 // String
//	                    request.getDeptId(),              // List<Long>
//	                    request.getJobRoleId(),            // List<Long>
//	                    request.getManagerId(),            // List<Long>
//	                    request.getTeamId(),               // List<Long>
//	                    request.getProjectId(),            // List<Long>
//	                    request.getClientId(),             // List<Long>
//	                    request.getEmploymentstatus(),     // String
//	                    request.getDateOfJoining(),        // String
//	                    request.getCity(),                 // String
//	                    request.getBloodGroup(),           // String
//	                    request.getGender(),               // String
//	                    request.getProbationPeriod(),     // List<Integer>
//	                    request.getNoticePeriod(),         // List<Long>
//	                    request.getMaritalStatus(),        // String
//	                    request.getBankName(),             // String
//	                    request.getState(),                // String
//	                    request.getCreatedOn(),            // String
//	                    request.getCreatedBy(),            // List<Long>
//	                    request.getExperience(),           // String
//	                    request.getWorkLocation(),         // String
//	                    request.getYear(),                 // Long
//	                    request.getBillableType()          // String
//	            );
//	        }
//
//
//	        if (data != null && !data.isEmpty()) {
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse(data);
//	        } else {
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse(Collections.emptyList());
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something Went Wrong.");
//	        response.setServiceError(e.getMessage());
//	    }
//	    return response;
//	}
//
//	
	
	
	
	public ServiceResponse getDepartmentWiseBillableNonBillableSummary(ReportsQueryDTO request) {
	    ServiceResponse response = new ServiceResponse();
	    List<DepartmentBillableDTO> data = new ArrayList<>();

	    try {
	        boolean isFilterEmpty =
	            (request.getDeptId() == null || request.getDeptId().isEmpty()) &&
	            (request.getEmployeementId() == null || request.getEmployeementId().isEmpty()) &&
	            (request.getName() == null || request.getName().isEmpty()) &&
	            (request.getJobRoleId() == null || request.getJobRoleId().isEmpty()) &&
	            (request.getManagerId() == null || request.getManagerId().isEmpty()) &&
	            (request.getTeamId() == null || request.getTeamId().isEmpty()) &&
	            (request.getProjectId() == null || request.getProjectId().isEmpty()) &&
	            (request.getClientId() == null || request.getClientId().isEmpty()) &&
	            (request.getEmploymentstatus() == null || request.getEmploymentstatus().isEmpty()) &&
	            (request.getDateOfJoining() == null || request.getDateOfJoining().isEmpty()) &&
	            (request.getCity() == null || request.getCity().isEmpty()) &&
	            (request.getBloodGroup() == null || request.getBloodGroup().isEmpty()) &&
	            (request.getGender() == null || request.getGender().isEmpty()) &&
	            (request.getProbationPeriod() == null || request.getProbationPeriod().isEmpty()) &&
	            (request.getNoticePeriod() == null || request.getNoticePeriod().isEmpty()) &&
	            (request.getMaritalStatus() == null || request.getMaritalStatus().isEmpty()) &&
	            (request.getBankName() == null || request.getBankName().isEmpty()) &&
	            (request.getState() == null || request.getState().isEmpty()) &&
	            (request.getCreatedOn() == null || request.getCreatedOn().isEmpty()) &&
	            (request.getCreatedBy() == null || request.getCreatedBy().isEmpty()) &&
	            (request.getExperience() == null || request.getExperience().isEmpty()) &&
	            (request.getWorkLocation() == null || request.getWorkLocation().isEmpty()) &&
	            (request.getYear() == null) &&
	            (request.getBillableType() == null || request.getBillableType().isEmpty());

	        if (isFilterEmpty) {
	            data = reportDashboardRepository.getDepartmentWiseBillableNonBillableSummary();
	        } else {
	            List<Object[]> rawData = reportDashboardRepository.getSelectedDepartmentBillableSummary(
	                request.getEmployeementId(),
	                request.getName(),
	                request.getDeptId(),
	                request.getJobRoleId(),
	                request.getManagerId(),
	                request.getTeamId(),
	                request.getProjectId(),
	                request.getClientId(),
	                request.getEmploymentstatus(),
	                request.getDateOfJoining(),
	                request.getCity(),
	                request.getBloodGroup(),
	                request.getGender(),
	                request.getProbationPeriod(),
	                request.getNoticePeriod(),
	                request.getMaritalStatus(),
	                request.getBankName(),
	                request.getState(),
	                request.getCreatedOn(),
	                request.getCreatedBy(),
	                request.getExperience(),
	                request.getWorkLocation(),
	                request.getYear(),
	                request.getBillableType()
	            );

	            // Convert rawData to DepartmentBillableDTO list
	            for (Object[] row : rawData) {
	                DepartmentBillableDTO dto = new DepartmentBillableDTO();
	                dto.setDepartmentName((String) row[0]);
	                dto.setBillableType((String) row[1]);
	                
	                dto.setEmployeeCount(row[2] != null ? ((Number) row[2]).longValue() : 0L);

	                data.add(dto);
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(data.isEmpty() ? Collections.emptyList() : data);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

	
	  public ServiceResponse getEmployeeDetailsByDepartmentAndBillableType(ReportsQueryDTO request) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setApiUrl("/api/getEmployeeDetailsByDepartmentAndBillableType");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();

	        try {
	            List<Object[]> employeeDetails = reportDashboardRepository.findEmployeesByDepartmentAndBillableType(
	                    request.getDeptId()
	                    ,request.getBillableType()
	            );

	            if (employeeDetails != null && !employeeDetails.isEmpty()) {
	                List<ReportListDTO> dtoList = new ArrayList<>();

	                employeeDetails.forEach((object) -> {
	                	ReportListDTO dto = new ReportListDTO();
	                    
	                    dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
	                    dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
	                    dto.setName(object[2] != null ? object[2].toString() : null);
	                    dto.setExperience(object[3] != null ? object[3].toString() : null);
	                    dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
	                    dto.setEmail(object[5] != null ? object[5].toString() : null);
	                    dto.setManagerName(object[6] != null ? object[6].toString() : null);
	                    dto.setBillable(object[7] != null ? object[7].toString() : null);
	                    dto.setBillableType(object[8] != null ? object[8].toString() : null);
	                    dto.setProjectName(object[9] != null ? object[9].toString() : null);
	                    dto.setClientName(object[10] != null ? object[10].toString() : null);
	                    dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
	                    dto.setMobileNo(object[12] != null ? object[12].toString() : null);
	                    dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
	                    dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
	                    dto.setGender(object[15] != null ? object[15].toString() : null);
	                    dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
	                    dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
	                    dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);
	                    
	                    dtoList.add(dto);
	                });

	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(dtoList);

	                logBuilder.append(" - Successfully retrieved ").append(employeeDetails.size()).append(" employee records");

	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse(new ArrayList<>());
	                logBuilder.append(" No employee data found");
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            logBuilder.append(" Error occurred: ").append(e.getMessage());
	        }

	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	  public ServiceResponse getEmployeeDetailsByEmploymentType(ReportsQueryDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getEmployeeDetailsByEmploymentType");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    
		    try {
		    	List<Object[]> employeeDetails = customFilterService.getCustomEmployeesByEmploymentTypeList(request);
		        
		        if (employeeDetails != null && !employeeDetails.isEmpty()) {
		            List<ReportListDTO> dtoList = new ArrayList<>();
		            employeeDetails.forEach((object) -> {
		                ReportListDTO dto = new ReportListDTO();
		                
		                dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
		                dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
		                dto.setName(object[2] != null ? object[2].toString() : null);
		                dto.setExperience(object[3] != null ? object[3].toString() : null);
		                dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
		                dto.setEmail(object[5] != null ? object[5].toString() : null);
		                dto.setManagerName(object[6] != null ? object[6].toString() : null);
		                dto.setBillable(object[7] != null ? object[7].toString() : null);
		                dto.setBillableType(object[8] != null ? object[8].toString() : null);
		                dto.setProjectName(object[9] != null ? object[9].toString() : null);
		                dto.setClientName(object[10] != null ? object[10].toString() : null);
		                dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
		                dto.setMobileNo(object[12] != null ? object[12].toString() : null);
		                dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
		                dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
		                dto.setGender(object[15] != null ? object[15].toString() : null);
		                dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
		                dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
		                dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);
		                
		                dtoList.add(dto);
		            });
		            
		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(dtoList);
		            logBuilder.append(" - Successfully retrieved ").append(employeeDetails.size()).append(" employee records");
		        } else {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(new ArrayList<>());
		            logBuilder.append(" No employee data found");
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something Went Wrong.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" Error occurred: ").append(e.getMessage());
		    }
		    
		    apiLogInfo.setApiRequest(logBuilder.toString());
		    logService.logMyInfo(httpRequest, apiLogInfo);
		    return response;
		}

	  public ServiceResponse getEmployeesByExperience(ReportsQueryDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getEmployeesByExperience");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();

		    try {
		    	List<Object[]> employeeDetails = customFilterService.getCustomEmployeesByExperience(request);
		    	
//		    	List<Object[]> employeeDetails = reportDashboardRepository.findEmployeesExperience(request.getEmployeeType(), request.getLowerValue(), request.getUpperValue());
		    	
		        if (employeeDetails != null && !employeeDetails.isEmpty()) {
		            List<ReportListDTO> dtoList = new ArrayList<>();

		            employeeDetails.forEach((object) -> {
		                ReportListDTO dto = new ReportListDTO();

		                dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
		                dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
		                dto.setName(object[2] != null ? object[2].toString() : null);
		                dto.setExperience(object[3] != null ? object[3].toString() : null);
		                dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
		                dto.setEmail(object[5] != null ? object[5].toString() : null);
		                dto.setManagerName(object[6] != null ? object[6].toString() : null);
		                dto.setBillable(object[7] != null ? object[7].toString() : null);
		                dto.setBillableType(object[8] != null ? object[8].toString() : null);
		                dto.setProjectName(object[9] != null ? object[9].toString() : null);
		                dto.setClientName(object[10] != null ? object[10].toString() : null);
		                dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
		                dto.setMobileNo(object[12] != null ? object[12].toString() : null);
		                dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
		                dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
		                dto.setGender(object[15] != null ? object[15].toString() : null);
		                dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
		                dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
		                dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);

		                dtoList.add(dto);
		            });

		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(dtoList);

		            logBuilder.append(" - Successfully retrieved ").append(employeeDetails.size()).append(" employee records");
		            logBuilder.append(" for employee type: ").append(request.getEmployeeType());
		            logBuilder.append(" with experience range: ").append(request.getLowerValue()).append(" to ").append(request.getUpperValue());

		        } else {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(new ArrayList<>());
		            logBuilder.append(" No employee data found for employee type: ").append(request.getEmployeeType());
		            logBuilder.append(" with experience range: ").append(request.getLowerValue()).append(" to ").append(request.getUpperValue());
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something Went Wrong.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" Error occurred: ").append(e.getMessage());
		        logBuilder.append(" for employee type: ").append(request.getEmployeeType());
		        logBuilder.append(" with experience range: ").append(request.getLowerValue()).append(" to ").append(request.getUpperValue());
		    }

		    apiLogInfo.setApiRequest(logBuilder.toString());
		    logService.logMyInfo(httpRequest, apiLogInfo);
		    return response;
		}
	  
	  public ServiceResponse getEmployeeDetailsByDepartmentAndKyc(ReportsQueryDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getEmployeeDetailsByDepartmentAndKyc");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();

		    try {
		        List<Object[]> employeeDetails = reportDashboardRepository.findEmployeesByDepartmentAndKyc(
		            request.getDeptId(),
		            request.getIsUserInfoUpdated()
		        );

		        if (employeeDetails != null && !employeeDetails.isEmpty()) {
		            List<ReportListDTO> dtoList = new ArrayList<>();

		            employeeDetails.forEach((object) -> {
		                ReportListDTO dto = new ReportListDTO();

		                dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
		                dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
		                dto.setName(object[2] != null ? object[2].toString() : null);
		                dto.setExperience(object[3] != null ? object[3].toString() : null);
		                dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
		                dto.setEmail(object[5] != null ? object[5].toString() : null);
		                dto.setManagerName(object[6] != null ? object[6].toString() : null);
		                dto.setBillable(object[7] != null ? object[7].toString() : null);
		                dto.setBillableType(object[8] != null ? object[8].toString() : null);
		                dto.setProjectName(object[9] != null ? object[9].toString() : null);
		                dto.setClientName(object[10] != null ? object[10].toString() : null);
		                dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
		                dto.setMobileNo(object[12] != null ? object[12].toString() : null);
		                dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
		                dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
		                dto.setGender(object[15] != null ? object[15].toString() : null);
		                dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
		                dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
		                dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);

		                dtoList.add(dto);
		            });

		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(dtoList);

		            logBuilder.append(" - Successfully retrieved ").append(employeeDetails.size()).append(" employee records");
		            logBuilder.append(" for departments: ").append(request.getDeptId());
		            logBuilder.append(" with KYC status filter: ").append(request.getIsUserInfoUpdated());

		        } else {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(new ArrayList<>());
		            logBuilder.append(" No employee data found for departments: ").append(request.getDeptId());
		            logBuilder.append(" with KYC status filter: ").append(request.getIsUserInfoUpdated());
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something Went Wrong.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" Error occurred: ").append(e.getMessage());
		        logBuilder.append(" for departments: ").append(request.getDeptId());
		        logBuilder.append(" with KYC status filter: ").append(request.getIsUserInfoUpdated());
		    }

		    apiLogInfo.setApiRequest(logBuilder.toString());
		    logService.logMyInfo(httpRequest, apiLogInfo);
		    return response;
		}
	  
	  public ServiceResponse getDepartmentwiseEmployeesByType(ReportsQueryDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getDepartmentwiseEmployeesByType");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();

		    try {
		        List<Object[]> employeeDetails = reportDashboardRepository.findDepartmentwiseEmployees(
		            request.getDeptId(),
		            request.getEmployeeType()
		        );

		        if (employeeDetails != null && !employeeDetails.isEmpty()) {
		            List<ReportListDTO> dtoList = new ArrayList<>();

		            employeeDetails.forEach((object) -> {
		                ReportListDTO dto = new ReportListDTO();

		                dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
		                dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
		                dto.setName(object[2] != null ? object[2].toString() : null);
		                dto.setExperience(object[3] != null ? object[3].toString() : null);
		                dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
		                dto.setEmail(object[5] != null ? object[5].toString() : null);
		                dto.setManagerName(object[6] != null ? object[6].toString() : null);
		                dto.setBillable(object[7] != null ? object[7].toString() : null);
		                dto.setBillableType(object[8] != null ? object[8].toString() : null);
		                dto.setProjectName(object[9] != null ? object[9].toString() : null);
		                dto.setClientName(object[10] != null ? object[10].toString() : null);
		                dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
		                dto.setMobileNo(object[12] != null ? object[12].toString() : null);
		                dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
		                dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
		                dto.setGender(object[15] != null ? object[15].toString() : null);
		                dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
		                dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
		                dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);

		                dtoList.add(dto);
		            });

		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(dtoList);

		            logBuilder.append(" - Successfully retrieved ").append(employeeDetails.size()).append(" employee records");
		            logBuilder.append(" for departments: ").append(request.getDeptId());
		            logBuilder.append(" with employee type: ").append(request.getEmployeeType());

		        } else {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(new ArrayList<>());
		            logBuilder.append(" No employee data found for departments: ").append(request.getDeptId());
		            logBuilder.append(" with employee type: ").append(request.getEmployeeType());
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something Went Wrong.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" Error occurred: ").append(e.getMessage());
		        logBuilder.append(" for departments: ").append(request.getDeptId());
		        logBuilder.append(" with employee type: ").append(request.getEmployeeType());
		    }

		    apiLogInfo.setApiRequest(logBuilder.toString());
		    logService.logMyInfo(httpRequest, apiLogInfo);
		    return response;
		}
	  public ServiceResponse getJoinVsResignEmployeeDetails(ReportsQueryDTO request) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setApiUrl("/api/getJoinVsResignEmployeeDetails");
	        apiLogInfo.setLogLevel("INFO");

	        StringBuilder logBuilder = new StringBuilder();

	        try {
	            logBuilder.append("Fetching getJoiningVsResignationCountList data");

	            List<Object[]> data = customFilterService.getCustomJoinVsResignEmployeeDetails(request);
	            List<ReportListDTO> dtoList = new ArrayList<>();
	            
	            data.forEach((object)-> {
	            	ReportListDTO dto = new ReportListDTO();
	            	dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
	                dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
	                dto.setName(object[2] != null ? object[2].toString() : null);
	                dto.setExperience(object[3] != null ? object[3].toString() : null);
	                dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
	                dto.setEmail(object[5] != null ? object[5].toString() : null);
	                dto.setManagerName(object[6] != null ? object[6].toString() : null);
	                dto.setBillable(object[7] != null ? object[7].toString() : null);
	                dto.setBillableType(object[8] != null ? object[8].toString() : null);
	                dto.setProjectName(object[9] != null ? object[9].toString() : null);
	                dto.setClientName(object[10] != null ? object[10].toString() : null);
	                dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
	                dto.setMobileNo(object[12] != null ? object[12].toString() : null);
	                dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
	                dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
	                dto.setGender(object[15] != null ? object[15].toString() : null);
	                dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
	                dto.setAge(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
	                dto.setProfileKycStatus(object[18] != null ? object[18].toString() : null);
	            	
	                dtoList.add(dto);
	            });
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList); // raw object for now

	            logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" records");

	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong while fetching join/resign employee details.");
	            response.setServiceError(e.getMessage());

	            logBuilder.append(" - Error occurred: ").append(e.getMessage());
	        }

	        // Print or store the log info
	        System.out.println(logBuilder.toString());

	        return response;
	    }
	  public ServiceResponse getLeaveTrendDetails() {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getLeaveTrendDetails");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    
		    try {
		        logBuilder.append("Fetching leave trend data");
		        
		        List<Object[]> data = reportDashboardRepository.getLeaveTrend();
		        List<ReportCountDTO> dtoList = new ArrayList<>();
		        
		        data.forEach((object) -> {
		            ReportCountDTO dto = new ReportCountDTO();
		            dto.setTypeOfLeave(object[0] != null ? object[0].toString() : null);
		            dto.setLeaveDate(object[1] != null ? LocalDate.parse(object[1].toString()) : null);
		            dto.setTotalLeaveDays(object[2] != null ? Long.parseLong(object[2].toString()) : 0);
		            
		            dtoList.add(dto);
		        });
		        
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(dtoList);
		        logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" leave trend records");
		        
		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong while fetching leave trend details.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" - Error occurred: ").append(e.getMessage());
		    }
		    
		    System.out.println(logBuilder.toString());
		    return response;
		}
	  public ServiceResponse customgetLeaveTrendDetails(ReportsQueryDTO request) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setApiUrl("/api/getLeaveTrendDetails");
	        apiLogInfo.setLogLevel("INFO");

	        try {
	            // Call the new method in CustomFilterService
	            List<Object[]> data = customFilterService.getCustomLeaveTrendDetails(request.getQueryList());
	            
	            List<ReportCountDTO> dtoList = new ArrayList<>();
	            data.forEach((object) -> {
	                ReportCountDTO dto = new ReportCountDTO();
	                dto.setTypeOfLeave(object[0] != null ? object[0].toString() : null);
	                dto.setLeaveDate(object[1] != null ? LocalDate.parse(object[1].toString()) : null);
	                dto.setTotalLeaveDays(object[2] != null ? Long.parseLong(object[2].toString()) : 0);
	                dtoList.add(dto);
	            });
	            
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong while fetching leave trend details.");
	            response.setServiceError(e.getMessage());
	        }
	        
	        // Add logging if needed
	        return response;
	    }
	  public ServiceResponse getLeaveTrendAnalysis(ReportsQueryDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getLeaveTrendAnalysis");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    
		    try {
		        logBuilder.append("Fetching leave trend analysis data");
		        
		        List<Object[]> data = customFilterService.getCustomLeaveTrendAnalysis(request);
		        List<LeaveTrendAnalysisDTO> dtoList = new ArrayList<>();
		        
		        data.forEach((object) -> {
		            LeaveTrendAnalysisDTO dto = new LeaveTrendAnalysisDTO();
		            dto.setEmployeementId(object[0] != null ? (object[0].toString()) : null);
		            dto.setDepartmentName(object[1] != null ? object[1].toString() : null);
		            dto.setEmployeeName(object[2] != null ? object[2].toString() : null);
		            dto.setFromDate(object[3] != null ? LocalDate.parse(object[3].toString()) : null);
		            dto.setToDate(object[4] != null ? LocalDate.parse(object[4].toString()) : null);
		            dto.setStatus(object[5] != null ? object[5].toString() : null);
		            dto.setFromDateDayType(object[6] != null ? object[6].toString() : null);
		            dto.setToDateDayType(object[7] != null ? object[7].toString() : null);
		            dto.setEmployeeType(object[8] != null ? object[8].toString() : null);
		            dto.setManagerId(object[9] != null ? (object[9].toString()) : null);
		            dto.setTypeOfLeave(object[10] != null ? object[10].toString() : null);
		            dto.setLeaveDate(object[11] != null ? LocalDate.parse(object[11].toString()) : null);
		            
		            dtoList.add(dto);
		        });
		        
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(dtoList);
		        logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" leave trend analysis records");
		        
		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong while fetching leave trend analysis details.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" - Error occurred: ").append(e.getMessage());
		    }
		    
		    System.out.println(logBuilder.toString());
		    return response;
		}
	  
	  public ServiceResponse getWorkLocationDetails() {
		   
			    
		  ServiceResponse response = new ServiceResponse();
		    
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getWorkLocationDetails");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();

		    try {
		        logBuilder.append("Fetching work location data");
		        
		        List<Object[]> data;
		        List<WorkLocationCountDTO> dtoList = new ArrayList<>();
		        
		        if(!worklocationMap.isEmpty()) {
		        	data = worklocationMap.get("Success");
		        }else {
		        	data = reportDashboardRepository.getWorkLocation();
		        	worklocationMap.put("Success", data);
		        }

		        data.forEach((object) -> {
		            WorkLocationCountDTO dto = new WorkLocationCountDTO();
		            dto.setClientLocation(object[0] != null ? object[0].toString() : null);
		            dto.setEmployeeCOUNT(object[1] != null ? Long.parseLong(object[1].toString()) : 0);

		            dtoList.add(dto);
		        });

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(dtoList);
		        logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" work location records");

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong while fetching work location details.");
		        response.setServiceError(e.getMessage());
		        logBuilder.append(" - Error occurred: ").append(e.getMessage());
		    }

		    System.out.println(logBuilder.toString());
		    return response;
		}
	  
	  public ServiceResponse getWorkLocationSummaryDetails(ReportsQueryDTO reportQueryDTO) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getWorkLocationSummaryDetails");
		    apiLogInfo.setLogLevel("INFO");

		    StringBuilder logBuilder = new StringBuilder();

		    try {
		        logBuilder.append("Fetching work location summary data for location: ").append(reportQueryDTO.getWorkLocation());

		        List<Object[]> data = customFilterService.getCustomWorkLocationSummaryDetails(reportQueryDTO);
		        List<ReportListDTO> dtoList = new ArrayList<>();

		        data.forEach((object) -> {
		            ReportListDTO dto = new ReportListDTO();
		            dto.setEmployeementId(object[0] != null ? object[0].toString() : null);
		            dto.setEmployeeType(object[1] != null ? object[1].toString() : null);
		            dto.setName(object[2] != null ? object[2].toString() : null);
//		            dto.setExperience(object[3] != null ? object[3].toString() : null);
		            dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
		            dto.setEmail(object[5] != null ? object[5].toString() : null);
		            dto.setManagerName(object[6] != null ? object[6].toString() : null);
		            dto.setBillable(object[7] != null ? object[7].toString() : null);
		            dto.setBillableType(object[8] != null ? object[8].toString() : null);
		            dto.setProjectName(object[9] != null ? object[9].toString() : null);
		            dto.setClientName(object[10] != null ? object[10].toString() : null);
//		            dto.setDateOfJoining(object[11] != null ? object[11].toString() : null);
//		            dto.setMobileNo(object[12] != null ? object[12].toString() : null);
//		            dto.setEmploymentstatus(object[13] != null ? object[13].toString() : null);
		            dto.setTotalExperience(object[14] != null ? object[14].toString() : null);
//		            dto.setGender(object[15] != null ? object[15].toString() : null);
		            dto.setWorkLocation(object[16] != null ? object[16].toString() : null);
		            // For client_location, we can set it in workLocation or add it as a custom field
		             dto.setClientLocation(object[17] != null ? object[17].toString() : null); 
//		            dto.setAge(object[18] != null ? Integer.parseInt(object[18].toString()) : null);
//		            dto.setProfileKycStatus(object[19] != null ? object[19].toString() : null);

		            dtoList.add(dto);
		        });

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(dtoList);

		        logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" work location summary records");

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Something went wrong while fetching work location summary details.");
		        response.setServiceError(e.getMessage());

		        logBuilder.append(" - Error occurred: ").append(e.getMessage());
		    }

		    System.out.println(logBuilder.toString());

		    return response;
		}
	  
	  public ServiceResponse customGetAllGraphEmployeeSummary(ReportsQueryDTO request) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setApiUrl("/api/customGetAllGraphEmployeeSummary");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();

	        try {
	            // Call the new dynamic method in CustomFilterService
	            List<Object[]> object = customFilterService.getCustomGraphEmployeeSummary(request);
	            List<ReportCountDTO> dtoList = new ArrayList<>();

	            if (object != null && !object.isEmpty()) {
	                object.forEach((count) -> {
	                    ReportCountDTO reportDto = new ReportCountDTO();
	                    
	                    // Billable Status
	                    reportDto.setBillableYes(count[0] != null ? Long.parseLong(count[0].toString()) : 0L);
	                    reportDto.setBillableNo(count[1] != null ? Long.parseLong(count[1].toString()) : 0L);
	                    reportDto.setBillableOther(count[2] != null ? Long.parseLong(count[2].toString()) : 0L);

	                    // Age Groups
	                    reportDto.setAge18to25(count[3] != null ? Long.parseLong(count[3].toString()) : 0L);
	                    reportDto.setAge25to35(count[4] != null ? Long.parseLong(count[4].toString()) : 0L);
	                    reportDto.setAge35to45(count[5] != null ? Long.parseLong(count[5].toString()) : 0L);
	                    reportDto.setAgeAbove45(count[6] != null ? Long.parseLong(count[6].toString()) : 0L);

	                    // Gender
	                    reportDto.setGenderMale(count[7] != null ? Long.parseLong(count[7].toString()) : 0L);
	                    reportDto.setGenderFemale(count[8] != null ? Long.parseLong(count[8].toString()) : 0L);
	                    reportDto.setGenderOther(count[9] != null ? Long.parseLong(count[9].toString()) : 0L);

	                    // Employment Status
	                    reportDto.setEmployeeStatusConfirmed(count[10] != null ? Long.parseLong(count[10].toString()) : 0L);
	                    reportDto.setEmployeeStatusResigned(count[11] != null ? Long.parseLong(count[11].toString()) : 0L);
	                    reportDto.setEmployeeStatusProbation(count[12] != null ? Long.parseLong(count[12].toString()) : 0L);
	                    reportDto.setEmployeeStatusRetain(count[13] != null ? Long.parseLong(count[13].toString()) : 0L);
	                    reportDto.setEmployeeStatusInActive(count[14] != null ? Long.parseLong(count[14].toString()) : 0L);

	                    // Experience Type
	                    reportDto.setLateralCount(count[15] != null ? Long.parseLong(count[15].toString()) : 0L);
	                    reportDto.setFresherCount(count[16] != null ? Long.parseLong(count[16].toString()) : 0L);

	                    // Apprentice Years
	                    reportDto.setApprenticeYears0to1(count[17] != null ? Long.parseLong(count[17].toString()) : 0L);
	                    // ... continue mapping for all 42 columns
	                    reportDto.setApprenticeYears1to2(count[18] != null ? Long.parseLong(count[18].toString()) : 0L);
	                    reportDto.setApprenticeYears2to5(count[19] != null ? Long.parseLong(count[19].toString()) : 0L);
	                    reportDto.setApprenticeYears5to10(count[20] != null ? Long.parseLong(count[20].toString()) : 0L);
	                    reportDto.setApprenticeYearsAbove10(count[21] != null ? Long.parseLong(count[21].toString()) : 0L);
	                    
	                    // Employee Years
	                    reportDto.setEmployeeYears0to1(count[22] != null ? Long.parseLong(count[22].toString()) : 0L);
	                    reportDto.setEmployeeYears1to2(count[23] != null ? Long.parseLong(count[23].toString()) : 0L);
	                    reportDto.setEmployeeYears2to5(count[24] != null ? Long.parseLong(count[24].toString()) : 0L);
	                    reportDto.setEmployeeYears5to10(count[25] != null ? Long.parseLong(count[25].toString()) : 0L);
	                    reportDto.setEmployeeYearsAbove10(count[26] != null ? Long.parseLong(count[26].toString()) : 0L);
	                    
	                    // Consultant Years
	                    reportDto.setConsultantYear0to1(count[27] != null ? Long.parseLong(count[27].toString()) : 0L);
	                    reportDto.setConsultantYear1to2(count[28] != null ? Long.parseLong(count[28].toString()) : 0L);
	                    reportDto.setConsultantYear2to5(count[29] != null ? Long.parseLong(count[29].toString()) : 0L);
	                    reportDto.setConsultantYear5to10(count[30] != null ? Long.parseLong(count[30].toString()) : 0L);
	                    reportDto.setConsultantYearAbove10(count[31] != null ? Long.parseLong(count[31].toString()) : 0L);
	                   
	                    // Billable Types
	                    reportDto.setFixedCost(count[32] != null ? Long.parseLong(count[32].toString()) : 0L);
	                    reportDto.setTNM(count[33] != null ? Long.parseLong(count[33].toString()) : 0L);
	                    reportDto.setBench(count[34] != null ? Long.parseLong(count[34].toString()) : 0L);
	                    reportDto.setShadow(count[35] != null ? Long.parseLong(count[35].toString()) : 0L);
	                    reportDto.setInternalRNDProducts(count[36] != null ? Long.parseLong(count[36].toString()) : 0L);
	                    
	                    // Employee Counts
	                    reportDto.setTotalEmployeeCountDisplay(count[37] != null ? Long.parseLong(count[37].toString()) : 0L);
	                    reportDto.setProbationCountDisplay(count[38] != null ? Long.parseLong(count[38].toString()) : 0L);
	                    reportDto.setApprenticeCountDisplay(count[39] != null ? Long.parseLong(count[39].toString()) : 0L);
	                    reportDto.setConsultantCountDisplay(count[40] != null ? Long.parseLong(count[40].toString()) : 0L);
	                    reportDto.setRegularCountDisplay(count[41] != null ? Long.parseLong(count[41].toString()) : 0L);
	                    
	                    dtoList.add(reportDto);
	                });
	            }

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            logBuilder.append("Successfully fetched graph summary data.");

	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong while fetching graph summary.");
	            response.setServiceError(e.getMessage());
	            logBuilder.append("Error occurred: ").append(e.getMessage());
	        }

	        // Add logging here if needed
	        return response;
	    }
	  
	  
	  
	  //Custom for Join & Resign Count
	  
	  public ServiceResponse customGetJoiningVsResignationCount(ReportsQueryDTO request) {
	        ServiceResponse response = new ServiceResponse();
	        // ... (logging setup)

	        try {
	            // 1. Create the DTO that the repository method expects
	            ReportsQueryDTO queryDto = new ReportsQueryDTO();
	            queryDto.setYear(request.getYear());

	            // 2. Map the generic queryList to the specific fields of ReportsQueryDTO
	            if (request.getQueryList() != null && !request.getQueryList().isEmpty()) {
	                for (CustomFilterDTO filter : request.getQueryList()) {
	                    if (filter.getValue() == null || filter.getValue().trim().isEmpty()) {
	                        continue; // Skip empty filters
	                    }
	                    // Use a switch on the column name from the frontend
	                    switch (filter.getColumn()) {
	                        case "Department":
	                            // Assuming value is a single ID. Adjust if it's a list.
	                        	Department dept = departmentRepository.findByName(filter.getValue());
	                            if (dept != null) {
	                                // Use the ID from the fetched department object
	                                queryDto.setDeptId(Collections.singletonList(dept.getDeptId()));}
	                            break;
	                        case "Employee Id":
	                            queryDto.setEmployeementId(Collections.singletonList(Long.parseLong(filter.getValue())));
	                            break;
	                        case "Full Name":
	                            queryDto.setName(filter.getValue());
	                            break;
	                        case "Gender":
	                            queryDto.setGender(filter.getValue());
	                            break;
	                        case "Employment Status":
	                            queryDto.setEmploymentstatus(filter.getValue());
	                            break;
	                        // Add cases for ALL other filterable columns your query supports
	                        // e.g., "Job Role", "Manager", "Team Name", "Project Name", etc.
	                    }
	                }
	            }
	            
	            // 3. Call the repository with the now-populated DTO
	            List<Object[]> counts = reportDashboardRepository.getJoiningVsResignationCount(
	                queryDto.getEmployeementId(),
	                queryDto.getName(),
	                queryDto.getDeptId(),
	                queryDto.getJobRoleId(),
	                queryDto.getManagerId(),
	                queryDto.getTeamId(),
	                queryDto.getProjectId(),
	                queryDto.getClientId(),
	                queryDto.getEmploymentstatus(),
	                queryDto.getDateOfJoining(),
	                queryDto.getCity(),
	                queryDto.getBloodGroup(),
	                queryDto.getGender(),
	                queryDto.getProbationPeriod(),
	                queryDto.getNoticePeriod(),
	                queryDto.getMaritalStatus(),
	                queryDto.getBankName(),
	                queryDto.getState(),
	                queryDto.getCreatedOn(),
	                queryDto.getCreatedBy(),
	                queryDto.getExperience(),
	                queryDto.getWorkLocation(),
	                queryDto.getYear()
	            );

	            // 4. Process the results (this part remains the same)
	            if (counts != null && !counts.isEmpty()) {
	                List<ChartsCountDTO> dtoList = new ArrayList<>();
	                counts.forEach((object) -> {
	                    ChartsCountDTO dto = new ChartsCountDTO();
	                    dto.setMonthName(object[0] != null ? object[0].toString() : null);
	                    dto.setYear(object[1] != null ? Integer.parseInt(object[1].toString()) : null);
	                    dto.setApprenticeCount(object[2] != null ? Long.parseLong(object[2].toString()) : null);
	                    dto.setConsultantCount(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	                    dto.setRegularCount(object[4] != null ? Long.parseLong(object[4].toString()) : null);
	                    dto.setResignCount(object[5] != null ? Long.parseLong(object[5].toString()) : null);
	                    dtoList.add(dto);
	                });
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(dtoList);
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(new ArrayList<>());
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong while fetching Join vs. Resign data.");
	            response.setServiceError(e.getMessage());
	        }

	        // ... (logging)
	        return response;
	    }
	  
	  
	  // Work Location Summary
	  
	  public ServiceResponse customGetWorkLocationDetails(ReportsQueryDTO request) {
			ServiceResponse response = new ServiceResponse();
			LogDTO apiLogInfo = new LogDTO();
			apiLogInfo.setApiUrl("/api/getWorkLocationDetails");
			apiLogInfo.setLogLevel("INFO");
			StringBuilder logBuilder = new StringBuilder();

			try {
				logBuilder.append("Fetching filtered work location data");

				// Call the new dynamic method in CustomFilterService
				List<Object[]> data = customFilterService.getCustomWorkLocationDetails(request);
				
				// The caching logic is removed because the results are now dynamic
				// if(!worklocationMap.isEmpty()) { ... } // REMOVE THIS

				List<WorkLocationCountDTO> dtoList = new ArrayList<>();
				data.forEach((object) -> {
					WorkLocationCountDTO dto = new WorkLocationCountDTO();
					dto.setClientLocation(object[0] != null ? object[0].toString() : null);
					dto.setEmployeeCOUNT(object[1] != null ? Long.parseLong(object[1].toString()) : 0);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				logBuilder.append(" - Successfully retrieved ").append(data.size()).append(" work location records");

			} catch (Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something went wrong while fetching work location details.");
				response.setServiceError(e.getMessage());
				logBuilder.append(" - Error occurred: ").append(e.getMessage());
			}

			System.out.println(logBuilder.toString());
			return response;
		}
	}

	


