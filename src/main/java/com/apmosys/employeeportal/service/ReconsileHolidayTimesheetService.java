package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.enums.DayTypeCode;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReconsileHolidayTimesheetService {
	
	@Autowired 
	HolidayService holidayService;
	
	@Autowired
	DayTypeMasterNewRepository dayTypeMasterNewRepository;
	
	@Autowired
	HolidayRepository holidayRepository;
	
	@Autowired
	EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	
	
	public ServiceResponse reconsileHolidayTimesheet(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/reconsileHolidayTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		
		try {
			
			if(holidayDTO.getDateOfHoliday()==null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Please provide the date for which reconsile is intended");
				return response;
			}
			LocalDate holidayDate = LocalDate.parse(holidayDTO.getDateOfHoliday());
			Holiday holidayObj = holidayRepository.findFirstByDateOfHolidayAndState(holidayDate,"all");

			if (holidayObj == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No holiday found for this date");
				return response;
			}
			List<Employee> allEmployees = employeeRepository.findAllActiveEmployeesObject();
			List<Long> existingEmpIds = employeeTimesheetsNewRepository.findEmpIdsByDate(holidayDate);
		    LocalDateTime startOfDay = holidayDate.atStartOfDay();
            LocalDateTime endOfDay = holidayDate.atTime(LocalTime.MAX);
            int savedCount = 0;
            
            DayTypeMasterNew holidayDayType = dayTypeMasterNewRepository
                    .findByDayType(DayTypeCode.APMOSYS_HOLIDAY.getDbValue());
            DayTypeMasterNew weekoffDayType = dayTypeMasterNewRepository
                    .findByDayType(DayTypeCode.WEEK_OFF.getDbValue());

            if (holidayDayType == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("DayType '" + DayTypeCode.HOLIDAY.getDbValue()
                        + "' not found in day_type_master_new.");
                return response;
            }
            if (weekoffDayType == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("DayType '" + DayTypeCode.WEEK_OFF.getDbValue()
                        + "' not found in day_type_master_new.");
                return response;
            }
				if(!allEmployees.isEmpty()) {
					List<Employee> employeesToProcess = new ArrayList<>();

					for (Employee emp : allEmployees) {
						if (!existingEmpIds.contains(emp.getEmpId())) {
							employeesToProcess.add(emp);
						}
					}

					if (!employeesToProcess.isEmpty()) {

						holidayService.saveRelationalLeaveTimesheetBulk(
								employeesToProcess,
								holidayDate,
								holidayObj,
								startOfDay,
								endOfDay,
								holidayDayType,
								weekoffDayType
						);

						savedCount += employeesToProcess.size();
					}

					if (savedCount > 0) {
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(savedCount + " timesheets added successfully.");
						apiLogInfo.setApiResponse(savedCount + " missing records filled.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Everyone is already up to date.");
						apiLogInfo.setApiResponse("No missing records found.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

}
