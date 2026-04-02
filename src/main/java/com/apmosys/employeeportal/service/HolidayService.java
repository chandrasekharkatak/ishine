package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.apmosys.employeeportal.model.*;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.enums.DayTypeCode;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class HolidayService {

	@Autowired
	HolidayRepository holidayRepository;

	@Autowired
	TimesheetAggregationHelper aggregationHelper;

	@Autowired
	ActivitiesRepository activitiesRepository;

	@Autowired
	TimesheetMapper timesheetMapper;

	@Autowired
	ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;

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

	@Autowired
	EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    EmployeeTimesheetLocationMappingRepository employeeTimesheetLocationMappingRepository;

    @Autowired
    ProjectTimesheetService projectTimesheetService;

    @Autowired
	ProjectRepository projectRepository;

    @Autowired
	JobRoleRepository jobRoleRepository;

    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;

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
//			     newHoliday.setState(holidayDTO.getState());
			     newHoliday.setHolidayType(holidayDTO.getHolidayType());
			     if(holidayDTO.getHolidayType().equals("WeekOff") ) {
			    	 newHoliday.setState("all");
			     }else {
			    	  newHoliday.setState(holidayDTO.getState());
			     }
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
				holiday.setUpdatedBy(holidayDTO.getUpdatedBy());
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
		try {
			String state = holidayDto.getState();
			List<Object[]> list = holidayRepository.getAllHolidaysList(state);
			logBuilder.append("getAllHolidays size : "+list.size());
			
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
					dto.setUpdatedBy(object[11] != null ? Integer.parseInt(object[11].toString()) : null);
					
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
			  
			  List<Holiday> checkOccasion = holidayRepository.findByOccasion(holidayDTO.getOccasion());
			  Integer yearOfOccassion = holidayRepository.findYearOfOccassion(holidayDTO.getOccasion());
			  
			  if(Integer.valueOf(yearOfOccassion).equals(holidayDTO.getCurrentYear())) {
				  if(checkOccasion != null && !checkOccasion.isEmpty()) {
					  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					  response.setServiceResponse("Occasion already exist!");
					  
					  apiLogInfo.setApiResponse("Occasion already exist!");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				  }
			  }else {
				  response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				  response.setServiceResponse("Occasion Created Successfully");
				  
				  apiLogInfo.setApiResponse("Occasion Created Successfully");			
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
	@Transactional
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
					// for (Employee emp : allEmployees) {
					// 	if (!existingEmpIds.contains(emp.getEmpId())) {

					// 		saveRelationalLeaveTimesheet(emp, holidayDate, holidayObj, startOfDay, endOfDay, holidayDayType, weekoffDayType);
                    //         savedCount++;
					// 	}
					// }
					List<Employee> employeesToProcess = new ArrayList<>();

					for (Employee emp : allEmployees) {
						if (!existingEmpIds.contains(emp.getEmpId())) {
							employeesToProcess.add(emp);
						}
					}

					if (!employeesToProcess.isEmpty()) {

						saveRelationalLeaveTimesheetBulk(
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
	
	 // method called when holiday is filled using reconsileholiday so the holiday context is saved and not leave
    void saveRelationalLeaveTimesheet(Employee emp, LocalDate date, Holiday holidayObj,
            LocalDateTime startOfDay, LocalDateTime endOfDay, DayTypeMasterNew holidayDayType, DayTypeMasterNew weekoffDayType) {
        int resolvedDayTypeId;
        String tsDescription;
        if ("Festival".equalsIgnoreCase(holidayObj.getHolidayType())
                || "nonWorking".equalsIgnoreCase(holidayObj.getHolidayType())) {
            resolvedDayTypeId = holidayDayType.getDayTypeId();
            tsDescription = holidayDayType.getDayType() + " : " + holidayObj.getOccasion();

        } else if ("WeekOff".equalsIgnoreCase(holidayObj.getHolidayType())) {
            resolvedDayTypeId = weekoffDayType.getDayTypeId();
            String day = holidayObj.getDayOfTheWeek().toLowerCase().contains("saturday")
                    ? "Saturday"
                    : "Sunday";
            tsDescription = weekoffDayType.getDayType() + " : " + day;

        } else {
            resolvedDayTypeId = 0;
            tsDescription = holidayObj.getDayOfTheWeek();
        }
        EmployeeTimesheetsNew tsHeader = new EmployeeTimesheetsNew();
        tsHeader.setEmpId(emp.getEmpId());
        tsHeader.setDate(date);
        tsHeader.setIsNightShift(false);
        tsHeader.setStatus(2);
        tsHeader.setTotalWorkingMinutes(0);
        tsHeader.setCreatedBy(emp.getEmpId());
        tsHeader.setCreatedOn(LocalDateTime.now());
        Long managerId = "Reporting Manager".equals(emp.getApprovalsTo())
                ? emp.getReportingManagerId()
                : emp.getManagerId();
        tsHeader.setCurrentManagerId(managerId);
        tsHeader.setDayTypeId(resolvedDayTypeId);
        tsHeader.setDescription(tsDescription);
        tsHeader.setIsSystemGenerated(true);
        tsHeader = employeeTimesheetsNewRepository.save(tsHeader);
        Long newTsId = tsHeader.getTimesheetId();

        EmployeeTimesheetLocationMapping locMapping = EmployeeTimesheetLocationMapping.builder()
                .timesheetId(newTsId)
                .locationTypeId(4)
                .locationInTime(null)
                .locationOutTime(null)
                .build();
        locMapping = employeeTimesheetLocationMappingRepository.save(locMapping);

        List<ProjectNameAndPrjoectIdDTO> projectDTOList = employeeTimesheetsNewRepository
                .getProjectListForDateAndEmpId(emp.getEmpId(), startOfDay, endOfDay);
        if (projectDTOList != null && !projectDTOList.isEmpty()) {
            for (ProjectNameAndPrjoectIdDTO projDto : projectDTOList) {
                ProjectTimesheetDTO projectDTO = new ProjectTimesheetDTO();
                projectDTO.setTimesheetId(newTsId);
                projectDTO.setLocationMappingId(locMapping.getLocationMappingId());
                projectDTO.setProjectId(projDto.getProjectId());
                projectDTO.setStatus(2);
                projectDTO.setActivities(null);
                 projectDTO.setDescription(tsDescription);
                projectTimesheetService.create(newTsId, projectDTO, emp.getEmpId());
            }
        } else {
            ProjectTimesheetDTO defaultProject = new ProjectTimesheetDTO();
            defaultProject.setTimesheetId(newTsId);
            defaultProject.setLocationMappingId(locMapping.getLocationMappingId());
            defaultProject.setStatus(2);
            defaultProject.setActivities(null);
            defaultProject.setDescription(tsDescription);
            int resolvedProjectId = 0; // fallback if still not found

            if (emp.getJobRoleId() != null) {
                JobRole jobRole = jobRoleRepository.findById(emp.getJobRoleId()).orElse(null);
                if (jobRole != null && jobRole.getDeptId() != null) {
                    Optional<Integer> benchProjectId = projectRepository
                            .findBenchProjectIdByDeptId(jobRole.getDeptId());
                    if (benchProjectId.isPresent()) {
                        resolvedProjectId = benchProjectId.get();
                    }
                }
            }
            defaultProject.setProjectId(resolvedProjectId);
            projectTimesheetService.create(newTsId, defaultProject, emp.getEmpId());
        }
    }
  
	public void saveRelationalLeaveTimesheetBulk(
        List<Employee> employees,
        LocalDate date,
        Holiday holidayObj,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay,
        DayTypeMasterNew holidayDayType,
        DayTypeMasterNew weekoffDayType) {

		int resolvedDayTypeId;
		String tsDescription;

		if ("Festival".equalsIgnoreCase(holidayObj.getHolidayType())
				|| "nonWorking".equalsIgnoreCase(holidayObj.getHolidayType())) {

			resolvedDayTypeId = holidayDayType.getDayTypeId();
			tsDescription = holidayDayType.getDayType() + " : " + holidayObj.getOccasion();

		} else if ("WeekOff".equalsIgnoreCase(holidayObj.getHolidayType())) {

			resolvedDayTypeId = weekoffDayType.getDayTypeId();

			String day = holidayObj.getDayOfTheWeek().toLowerCase().contains("saturday")
					? "Saturday"
					: "Sunday";

			tsDescription = weekoffDayType.getDayType() + " : " + day;

		} else {

			resolvedDayTypeId = 0;
			tsDescription = holidayObj.getDayOfTheWeek();
		}

		List<EmployeeTimesheetsNew> timesheets = new ArrayList<>();

		for (Employee emp : employees) {

			EmployeeTimesheetsNew ts = new EmployeeTimesheetsNew();

			ts.setEmpId(emp.getEmpId());
			ts.setDate(date);
			ts.setIsNightShift(false);
			ts.setStatus(2);
			ts.setTotalWorkingMinutes(0);
			ts.setCreatedBy(emp.getEmpId());
			ts.setCreatedOn(LocalDateTime.now());

			Long managerId = "Reporting Manager".equals(emp.getApprovalsTo())
					? emp.getReportingManagerId()
					: emp.getManagerId();

			ts.setCurrentManagerId(managerId);
			ts.setDayTypeId(resolvedDayTypeId);
			ts.setDescription(tsDescription);
			ts.setIsSystemGenerated(true);

			timesheets.add(ts);
		}

		List<EmployeeTimesheetsNew> savedTimesheets =
				employeeTimesheetsNewRepository.saveAll(timesheets);

		List<EmployeeTimesheetLocationMapping> locationMappings = new ArrayList<>();

		for (EmployeeTimesheetsNew ts : savedTimesheets) {

			EmployeeTimesheetLocationMapping loc = EmployeeTimesheetLocationMapping.builder()
					.timesheetId(ts.getTimesheetId())
					.locationTypeId(4)
					.build();

			locationMappings.add(loc);
		}

		List<EmployeeTimesheetLocationMapping> savedLocationMappings = employeeTimesheetLocationMappingRepository.saveAll(locationMappings);
	// 	for (int i = 0; i < employees.size(); i++) {

	// 	Employee emp = employees.get(i);
	// 	EmployeeTimesheetsNew ts = savedTimesheets.get(i);
	// 	EmployeeTimesheetLocationMapping locMapping = locationMappings.get(i);

	// 	Long newTsId = ts.getTimesheetId();	

	// 	List<ProjectNameAndPrjoectIdDTO> projectDTOList =
	// 			timesheetsRepository.getProjectListForDateAndEmpId(
	// 					emp.getEmpId(), startOfDay, endOfDay);

	// 	if (projectDTOList != null && !projectDTOList.isEmpty()) {

	// 		for (ProjectNameAndPrjoectIdDTO projDto : projectDTOList) {

	// 			ProjectTimesheetDTO projectDTO = new ProjectTimesheetDTO();

	// 			projectDTO.setTimesheetId(newTsId);
	// 			projectDTO.setLocationMappingId(locMapping.getLocationMappingId());
	// 			projectDTO.setProjectId(projDto.getProjectId());
	// 			projectDTO.setStatus(2);
	// 			projectDTO.setActivities(null);
	// 			projectDTO.setDescription(tsDescription);

	// 			projectTimesheetService.create(newTsId, projectDTO, emp.getEmpId());
	// 		}

	// 	} else {

	// 		int resolvedProjectId = 0;

	// 		if (emp.getJobRoleId() != null) {

	// 			JobRole jobRole =
	// 					jobRoleRepository.findById(emp.getJobRoleId()).orElse(null);

	// 			if (jobRole != null && jobRole.getDeptId() != null) {

	// 				Optional<Integer> benchProjectId =
	// 						projectRepository.findBenchProjectIdByDeptId(jobRole.getDeptId());

	// 				if (benchProjectId.isPresent()) {
	// 					resolvedProjectId = benchProjectId.get();
	// 				}
	// 			}
	// 		}

	// 		ProjectTimesheetDTO defaultProject = new ProjectTimesheetDTO();

	// 		defaultProject.setTimesheetId(newTsId);
	// 		defaultProject.setLocationMappingId(locMapping.getLocationMappingId());
	// 		defaultProject.setStatus(2);
	// 		defaultProject.setActivities(null);
	// 		defaultProject.setDescription(tsDescription);
	// 		defaultProject.setProjectId(resolvedProjectId);

	// 		projectTimesheetService.create(newTsId, defaultProject, emp.getEmpId());
	// 	}
	// }
	List<ProjectTimesheetStatusNew> projectEntities = new ArrayList<>();

    for (int i = 0; i < employees.size(); i++) {

        Employee emp = employees.get(i);
        EmployeeTimesheetsNew ts = savedTimesheets.get(i);
        EmployeeTimesheetLocationMapping locMapping = savedLocationMappings.get(i);

        Long newTsId = ts.getTimesheetId();

        List<ProjectNameAndPrjoectIdDTO> projectDTOList =
        		employeeTimesheetsNewRepository.getProjectListForDateAndEmpId(
                        emp.getEmpId(), startOfDay, endOfDay);

        if (projectDTOList != null && !projectDTOList.isEmpty()) {

            for (ProjectNameAndPrjoectIdDTO projDto : projectDTOList) {

                ProjectTimesheetDTO projectDTO = new ProjectTimesheetDTO();

                projectDTO.setTimesheetId(newTsId);
                projectDTO.setLocationMappingId(locMapping.getLocationMappingId());
                projectDTO.setProjectId(projDto.getProjectId());
                projectDTO.setStatus(2);
                projectDTO.setActivities(null);
                projectDTO.setDescription(tsDescription);

                projectEntities.add(
                        buildProjectEntity(newTsId, projectDTO, emp.getEmpId())
                );
            }

        } else {

            int resolvedProjectId = 0;

            if (emp.getJobRoleId() != null) {

                JobRole jobRole =
                        jobRoleRepository.findById(emp.getJobRoleId()).orElse(null);

                if (jobRole != null && jobRole.getDeptId() != null) {

                    Optional<Integer> benchProjectId =
                            projectRepository.findBenchProjectIdByDeptId(jobRole.getDeptId());

                    if (benchProjectId.isPresent()) {
                        resolvedProjectId = benchProjectId.get();
                    }
                }
            }

            ProjectTimesheetDTO defaultProject = new ProjectTimesheetDTO();

            defaultProject.setTimesheetId(newTsId);
            defaultProject.setLocationMappingId(locMapping.getLocationMappingId());
            defaultProject.setStatus(2);
            defaultProject.setActivities(null);
            defaultProject.setDescription(tsDescription);
            defaultProject.setProjectId(resolvedProjectId);

            projectEntities.add(
                    buildProjectEntity(newTsId, defaultProject, emp.getEmpId())
            );
        }
    }

    projectTimesheetStatusNewRepository.saveAll(projectEntities);
}

private ProjectTimesheetStatusNew buildProjectEntity(
        Long timesheetId,
        ProjectTimesheetDTO dto,
        Long createdBy) {

    aggregationHelper.calculateAndSetProjectTimesheetTotals(dto);

    if (dto.getStatus() == null) {
        dto.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
    }

    ProjectTimesheetStatusNew entity = timesheetMapper.toEntity(dto, timesheetId);

    String description = "";

    if (dto.getActivities() == null || dto.getActivities().isEmpty()) {
        description = (dto.getDescription() != null && !dto.getDescription().trim().isEmpty())
                ? dto.getDescription()
                : "No activity available in project timesheet";
    } else {
        for (ActivityTimesheetDTO activity : dto.getActivities()) {
            Activity activityMaster = activitiesRepository.findById(activity.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
            description += activityMaster.getActivity() + "<br>";
        }
    }

    entity.setDescription(description);
    entity.setCreatedBy(createdBy);

    return entity;
}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse runTheHolidayCron(LocalDate dateToday) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/runTheHolidayCron");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		
		try {
			//for hardcoded
//			LocalDate dateToday = LocalDate.parse("2024-12-14");
//			LocalDate dateToday = LocalDate.parse("2025-06-14");
//			LocalDate dateToday = LocalDate.now();
//			System.out.println("filling timesheet method started");
			List<Object[]> allEmployee = employeeRepository.getEmployeeDetailForCronExludingSomeEmployees();
			System.err.println("vghgc"+dateToday);
			List<Holiday> publicHoliday = holidayRepository.findByDateOfHoliday(dateToday);
			
		//	Timesheet filler for weekoff day : saturday & sunday
			
			if(!publicHoliday.isEmpty()) {
				
				System.out.println("holiday_size"+publicHoliday.size());
				
				for(Holiday holiday: publicHoliday) {
					String holidayOccassion = holiday.getOccasion();
					String dayOfWeek = holiday.getDayOfTheWeek();
					
					if((holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Saturday")) || (holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Sunday"))) {
						for(Object[] employeeList: allEmployee) {
							Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
							
							Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empId,dateToday);
							
							if(empTimesheet == null) {
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(empId);
								newTimesheet.setDate(dateToday);
								newTimesheet.setDayType("Week Off");
								if(holidayOccassion.equals("Saturday : second saturday") || holidayOccassion.equals("Saturday : fourth saturday")) {
									newTimesheet.setDescription("WeekOff : Saturday");
									newTimesheet.setTotalTime((float)0);
									newTimesheet.setTotalWorkingHours("0");
								}else{
									newTimesheet.setDescription("WeekOff : Sunday");
									newTimesheet.setTotalTime((float)0);
									newTimesheet.setTotalWorkingHours("0");
								}
								newTimesheet.setEmpId(empId);
								// For weekoff's managers don't have to approve the timesheet, if any employee worked on weekoff will revoke this ..
								newTimesheet.setStatus("Approved");
								
//								System.out.println("filling weekoffs");
								
								timesheetsRepository.save(newTimesheet);
							}				
						}
					}
				}		
			}
			
	   //	Timesheet filler for public Holiday
			
			if(!publicHoliday.isEmpty()) {
				System.out.println("vghgc"+publicHoliday.isEmpty());
				System.out.println("holiday_size_holiday"+publicHoliday.size());
				for(Holiday holidays: publicHoliday) {
					String holidayState = holidays.getState();
					
					for(Object[] employeeList: allEmployee) {
						Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
						String workLocation = employeeList[1] != null ? employeeList[1].toString() : null;
						System.out.println("vghgc"+empId);
						Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empId,dateToday);
						if(empTimesheet == null) {
							System.out.println("vghgc"+publicHoliday.isEmpty());
							if((holidayState.equals("all") && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))
									|| (holidayState.equals(workLocation) && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))){  
								
								Timesheet newTimesheet = new Timesheet();
								newTimesheet.getCommonProperty().setCreatedBy(empId);
								newTimesheet.setDate(dateToday);
											
								newTimesheet.setDayType("Public Holiday");
								newTimesheet.setDescription("Public Holiday : " + holidays.getOccasion());
								newTimesheet.setEmpId(empId);
								newTimesheet.setStatus("Approved");
								
//								System.out.println("filling holiday");
								
								timesheetsRepository.save(newTimesheet);
								System.out.println("vghgc"+newTimesheet);
								
							}
						}
					}	
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Timesheet Added successfully");
			apiLogInfo.setApiResponse("Timesheet Added successfully.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			System.out.println("Method end reached");
			}
		catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			throw(e);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
}
