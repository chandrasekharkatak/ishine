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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.serviceInterface.ReportDashboardService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class ReportDashboardServiceImpl implements ReportDashboardService {

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	TimesheetsRepository timesheetsRepository;

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
}
