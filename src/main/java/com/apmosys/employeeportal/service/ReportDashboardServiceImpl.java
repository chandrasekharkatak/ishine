package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
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

	@Override
	public ServiceResponse getLast8DaysLeaveReport(LeaveDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository.getLast8DaysLeaveReport(stringToDateTimeParser.getDate(leaveDto.getStartDate(), "yyyy-MM-dd"), stringToDateTimeParser.getDate(leaveDto.getEndDate(), "yyyy-MM-dd"));

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave history not found. Kindly check date range.");
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
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getLast9DaysTimesheetReport() {
		ServiceResponse response = new ServiceResponse();
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
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found. Kindly check date range.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	
	@Override	
	public ServiceResponse getLeaveTrendAnalysisReport(LeaveDTO leaveDto) {	
		ServiceResponse response = new ServiceResponse();	
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
				}	
			}		
		}catch(Exception e) {	
			e.printStackTrace();	
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);	
			response.setServiceResponse("Something Went Wrong.");	
			response.setServiceError(e.getMessage());	
		}	
		return response;	
	}	
}
