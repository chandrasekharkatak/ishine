package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class HolidayService {

	@Autowired
	HolidayRepository holidayRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;

	@Transactional
	public ServiceResponse addHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_holidays");
		apiLogInfo.setApiUrl("/api/addHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("occasion : " + holidayDTO.getOccasion()+ "optionalHoliday : " +holidayDTO.getOptionalHoliday()+  "holidayType : " +holidayDTO.getHolidayType());
		try {
			     Holiday newHoliday = new Holiday();

			     newHoliday.setOccasion(holidayDTO.getOccasion());
			     newHoliday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd"));
			     newHoliday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
			     newHoliday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
			     newHoliday.setState(holidayDTO.getState());
			     newHoliday.setHolidayType(holidayDTO.getHolidayType());
//			     newHoliday.setCreatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
			     newHoliday.setCreatedBy(holidayDTO.getCreatedBy());

			     Holiday newHolidayCreated = holidayRepository.save(newHoliday);

			     if (newHolidayCreated != null) {
			     	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				    response.setServiceResponse("New holiday added.");
				    
				    apiLogInfo.setApiResponse("New holiday added.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			     } else {
			    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				    response.setServiceResponse("Failed to add new holiday.");
				   
				    apiLogInfo.setApiResponse("Failed to add new holiday.");			
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

	@Transactional
	public ServiceResponse updateHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_holidays");
		apiLogInfo.setApiUrl("/api/updateHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("holidayId : " + holidayDTO.getHolidayId());
		String message = "";
		try {
			Optional<Holiday> existingHoliday = holidayRepository.findById(holidayDTO.getHolidayId());

			if (existingHoliday.isPresent()) {
				Holiday holiday = existingHoliday.get();

				holiday.setOccasion(holidayDTO.getOccasion());
				holiday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
				holiday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd"));
				holiday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
				holiday.setState(holidayDTO.getState());
				holiday.setHolidayType(holidayDTO.getHolidayType());
				holiday.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				holiday.setUpdatedBy(Integer.parseInt(holidayDTO.getUpdatedBy()));
				Holiday dbResponse = holidayRepository.save(holiday);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Holiday updated successfully." + message);
					
					apiLogInfo.setApiResponse("Holiday updated successfully." + message);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update holiday.");
					
					apiLogInfo.setApiResponse("Failed to update holiday.");	
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
					
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such holiday available.");
				
				apiLogInfo.setApiResponse("No such holiday available.");			
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
	
	public ServiceResponse deleteHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/deleteHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("holidayId : " + holidayDTO.getHolidayId());
		try {
			Optional<Holiday> holidayObject = holidayRepository.findById(holidayDTO.getHolidayId());
			if (holidayObject.isPresent()) {
				Holiday holidayToBeDeleted = holidayObject.get();
				holidayRepository.deleteById(holidayToBeDeleted.getHolidayId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Holiday Deleted.");
				
				apiLogInfo.setApiResponse("Holiday Deleted.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday Not Found.");
				
				apiLogInfo.setApiResponse("Holiday Not Found.");			
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

	public ServiceResponse getAllHolidays(HolidayDTO holidayDto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("get_AllHoliday");
		apiLogInfo.setApiUrl("/api/getAllHolidays");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllHolidays size : "+holidayRepository.findAll().size());
		try {
			String state = holidayDto.getState();
			List<Object[]> list = holidayRepository.getAllHolidaysList(state);
			
			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday list is empty.");
				apiLogInfo.setApiResponse("Holiday list is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				list.forEach((object) -> {

					HolidayDTO dto = new HolidayDTO();
					dto.setHolidayId(object[0] != null ? Short.valueOf(object[0].toString()) : null);
					dto.setOccasion(object[1] != null ? object[1].toString() : null);
					dto.setDayOfTheWeek(object[2] != null ? object[2].toString() : null);
					dto.setDateOfHoliday(object[3] != null ? object[3].toString() : null);
					dto.setHolidayType(object[4] != null ? object[4].toString() : null);
					dto.setCreatedOn(object[5] != null ? object[5].toString() : null);
					dto.setCreatedbyName(object[6] != null ? object[6].toString() : null);
					dto.setUpdatedOn(object[7] != null ? object[7].toString() : null);
					dto.setUpdatedByName(object[8] != null ? object[8].toString() : null);
					dto.setCreatedBy(object[9] != null ? Integer.parseInt(object[9].toString()) : null);
					dto.setState(object[10] != null ? object[10].toString() : null);
					
					dtoList.add(dto);
				});      
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);			}

		} catch (Exception e) {
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
	
	// getAllHoliday
	public ServiceResponse getAllHoliday() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = holidayRepository.getAllHolidays();

			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday list is empty.");
			} else {
				list.forEach((object) -> {

					HolidayDTO dto = new HolidayDTO();
					dto.setHolidayId(object[0] != null ? Short.valueOf(object[0].toString()) : null);
					dto.setOccasion(object[1] != null ? object[1].toString() : null);
					dto.setDayOfTheWeek(object[2] != null ? object[2].toString() : null);
					dto.setDateOfHoliday(object[3] != null ? object[3].toString() : null);
					dto.setHolidayType(object[4] != null ? object[4].toString() : null);
					dto.setCreatedOn(object[5] != null ? object[5].toString() : null);
					dto.setCreatedbyName(object[6] != null ? object[6].toString() : null);
					dto.setUpdatedOn(object[7] != null ? object[7].toString() : null);
					dto.setUpdatedByName(object[8] != null ? object[8].toString() : null);
					dto.setCreatedBy(object[9] != null ? Integer.parseInt(object[9].toString()) : null);
					dto.setState(object[10] != null ? object[10].toString() : null);
					
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

	public ServiceResponse getAllHolidayByEmpWorkLocation(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllHolidayByEmpWorkLocation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		
		try {

			List<Object[]> list = employeeRepository.getAllHolidayByEmpWorkLocation(employeedto.getEmpId());
			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();
			
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Holidays found");
				
				apiLogInfo.setApiResponse("No Holidays found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					HolidayDTO dto = new HolidayDTO();
					dto.setOccasion(object[0] != null ? object[0].toString() : null);
					dto.setDayOfTheWeek(object[1] != null ? object[1].toString() : null);
					dto.setDateOfHoliday(object[2] != null ? object[2].toString() : null);
					dto.setState(object[3] != null ? object[3].toString() : null);
					dto.setOptionalHoliday(object[4] != null ? object[4].toString() : null);
					dtoList.add(dto);		
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList : " +dtoList);			
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

	public ServiceResponse checkOccasionIfAlreadyExist(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkOccasionIfAlreadyExist");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("occasion : " + holidayDTO.getOccasion());
				
		  try {
			  Holiday checkOccasion = holidayRepository.findByOccasion(holidayDTO.getOccasion());
			  Integer yearOfOccassion = holidayRepository.findYearOfOccassion(holidayDTO.getOccasion());
//			  System.out.println(yearOfOccassion);
//			  System.out.println(holidayDTO.getCurrentYear());
			  
			  if(yearOfOccassion == holidayDTO.getCurrentYear()) {
				  if(checkOccasion != null) {
					  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					  response.setServiceResponse("Occasion already exist!");
					  
					  apiLogInfo.setApiResponse("Occasion already exist!");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				  }
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

	public ServiceResponse getHolidayWeekOffSize(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getHolidayWeekOffSize");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getHolidayWeekOffSize : "+holidayRepository.getHolidayWeekOffSize(holidayDTO.getFromDate(), holidayDTO.getToDate(),holidayDTO.getState()));
		try {
		List<Object[]> list = holidayRepository.getHolidayWeekOffSize(holidayDTO.getFromDate(), holidayDTO.getToDate(),holidayDTO.getState());
		List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();

		System.out.println(list+ "list");
		
		if (list.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No Holidays found");
			apiLogInfo.setApiResponse("No Holidays found");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
		 else {

				list.forEach((object) -> {
					HolidayDTO dto = new HolidayDTO();
					dto.setHolidayId(object[0] != null ? Short.parseShort(object[0].toString()) : null);
					dto.setDateOfHoliday(object[1] != null ? object[1].toString() : null);
					dto.setDayOfTheWeek(object[2] != null ? object[2].toString() : null);
					dto.setOccasion(object[3] != null ? object[3].toString() : null);
					dto.setOptionalHoliday(object[4] != null ? object[4].toString() : null);
					dto.setState(object[6] != null ? object[6].toString() : null);

					dtoList.add(dto);		
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		 }	
			}
		catch (Exception e) {
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
	
	public ServiceResponse reconsileHolidayTimesheet(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/reconsileHolidayTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		
		try {
			
			LocalDate holidayDate = LocalDate.parse(holidayDTO.getDateOfHoliday());
			Holiday holidayObj = holidayRepository.findFirstByDateOfHolidayAndState(holidayDate,"all");
			System.out.println(" ANurag "+holidayObj+ " holidayDate  : "+holidayDate);
			
			List<Employee> allEmployee = employeeRepository.findAll();
			
			if(holidayObj != null){
				if(!allEmployee.isEmpty()) {
					allEmployee.forEach((emp) -> {
						
						if(!emp.getEmploymentstatus().equals("InActive")) {
							Timesheet timesheetObj = timesheetsRepository.findByEmpIdAndDate(emp.getEmpId(), holidayDate);
							
							if(timesheetObj == null) {
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(emp.getEmpId());
								newTimesheet.setDate(holidayDate);
								
								if(holidayObj.getHolidayType().equals("Festival")) {
									newTimesheet.setDayType("Public Holiday");
									newTimesheet.setDescription("Public Holiday : " + holidayObj.getOccasion());
									newTimesheet.setTotalTime((float)0);
									newTimesheet.setTotalWorkingHours("0");
								}
								
								if(holidayObj.getHolidayType().equals("WeekOff")) {
									if(holidayObj.getOccasion().equals("Saturday : second saturday") || holidayObj.getOccasion().equals("Saturday : fourth saturday")) {
										newTimesheet.setDescription("WeekOff : Saturday");
										newTimesheet.setDayType("Week Off");
										newTimesheet.setTotalTime((float)0);
										newTimesheet.setTotalWorkingHours("0");
									}else{
										newTimesheet.setDescription("WeekOff : Sunday");
										newTimesheet.setDayType("Week Off");
										newTimesheet.setTotalTime((float)0);
										newTimesheet.setTotalWorkingHours("0");
									}
								}
								
								newTimesheet.setEmpId(emp.getEmpId());
								newTimesheet.setStatus("Approved");
								
								Timesheet dbResponse = timesheetsRepository.save(newTimesheet);
								
								if(dbResponse != null) {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Timesheet Added successfully");
									apiLogInfo.setApiResponse("Timesheet Added successfully.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to add Timesheet");
									apiLogInfo.setApiResponse("Unable to add Timesheet");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
								}
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Timesheet Added Earlier for this date ");
							}
						}
					});
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee List is empty");
					apiLogInfo.setApiResponse("Employee List is empty");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			    response.setServiceResponse("No holiday found for the specified date and state");
			    apiLogInfo.setApiResponse("No holiday found for the specified date and state");
			    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
