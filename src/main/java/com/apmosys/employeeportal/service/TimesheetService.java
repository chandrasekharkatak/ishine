package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.AuditCustomRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@EnableAsync
@Service
public class TimesheetService {

	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	AuditCustomRepository auditCustomRepository ;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	ActivitiesRepository activitiesRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	TimesheetActivityMapRepository timesheetActivityMapRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	MailService mailService;

	@Value("${timesheet.lock.days}")
	private Integer timesheetLockDays;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

//	public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		try {
//
//			List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getTeamId());
//			System.out.println("projects :" +projectList.toString());
////			List<Project> projectList = projectRepository.findAll();
//
//			if (projectList.isEmpty()) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No projects found for given employee Id.");
//			} else {
//				Type typeList = new TypeToken<List<ProjectDTO>>() {
//				}.getType();
//				List<ProjectDTO> previousEmploymentList = modelMapper.map(projectList, typeList);
//
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(previousEmploymentList);
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

	public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId() );

		try {

			List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getEmpId());
			List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();

			if (!projectList.isEmpty()) {
				TimesheetDTO timesheetDto = new TimesheetDTO();

				for (Object[] object : projectList) {
					timesheetDto = new TimesheetDTO();
					timesheetDto.setClientId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					timesheetDto.setClientName(object[1] != null ? object[1].toString() : null);
					timesheetDto.setClientLocationId(object[2] != null ? Integer.parseInt(object[2].toString()) : null);
					timesheetDto.setClientLocation(object[3] != null ? object[3].toString() : null);
					timesheetDto.setProjectId(object[4] != null ? Integer.parseInt(object[4].toString()) : null);
					timesheetDto.setProjectName(object[5] != null ? object[5].toString() : null);
	  				timesheetDto.setTeamName(object[6] != null ? object[6].toString() : null);
					timesheetDto.setTeamId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
//					timesheetDto.setActivity(object[8] != null ? object[8].toString() : null);
//					timesheetDto.setActivityId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					listDto.add(timesheetDto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listDto);
				
				apiLogInfo.setApiResponse("listDto : " +listDto );			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				System.out.println("Project List :" + timesheetDto);
				System.out.println("List is : from dto " + listDto);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project List is empty !!");
				
				apiLogInfo.setApiResponse("Project List is empty !!" );			
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
	public List<TimesheetDTO> getAllProjectsByEmpIdForBioMax(String employeeCode,String date) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();

		try {

			//List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getEmpId());
		
			List<Object[]> objectList = timesheetActivityMapRepository
					.activitiesByTimesheetIdforBiomax(employeeCode,date);
			System.out.println("objectList"+objectList.size()+""+employeeCode+"=="+date);
			if (!objectList.isEmpty()) {
				TimesheetDTO timesheetDto = new TimesheetDTO();

				for (Object[] object : objectList) {
					timesheetDto = new TimesheetDTO();
					//timesheetDto.setClientId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
				timesheetDto.setClientName(object[7] != null ? object[7].toString() : null);
				
				//timesheetDto.setClientLocationId(object[3] != null ? Integer.parseInt(object[2].toString()) : null);
					timesheetDto.setClientLocation(object[9] != null ? object[9].toString() : null);
//					timesheetDto.setProjectId(object[10] != null ? Integer.parseInt(object[4].toString()) : null);
				timesheetDto.setProjectName(object[6] != null ? object[6].toString() : null);
	  				timesheetDto.setTeamName(object[8] != null ? object[8].toString() : null);
//	  				
					//timesheetDto.setTeamId(object[5] != null ? Long.parseLong(object[7].toString()) : null);
					timesheetDto.setActivity(object[10] != null ? object[10].toString() : null);
//					timesheetDto.setActivityId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
				listDto.add(timesheetDto);
				}
				return listDto;
			} else {
				return listDto;
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
		return listDto;
	}

	public ServiceResponse getAllActivitiesByProjectIdandEmpId(TimesheetDTO timesheetDTO) {

		System.out.println(timesheetDTO);
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllActivitiesByProjectIdandEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " +timesheetDTO.getProjectId()+ "empId : " +timesheetDTO.getEmpId());
		
		try {
			

			List<Object[]> objectList = projectRepository
					.getActivitiesByTeamIdAndEmployeeId(timesheetDTO.getTeamId(), timesheetDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
					
					apiLogInfo.setApiResponse("No activities found.Activity list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

					list.forEach((object) -> {
						String[] employeeRoleInTeam = (object[4] != null ? object[4].toString() : null).split(",");
						boolean contains = Arrays.stream(employeeRoleInTeam).anyMatch((object[3] != null ? object[3].toString() : null)::equals);

						if(contains) {
							ActivityDTO dto = new ActivityDTO();

							dto.setActivityId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
							dto.setActivity(object[1] != null ? object[1].toString() : null);
							dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
							dto.setDepartmentList(object[5] != null ? object[5].toString().split(",") : null);
//							dto.setDeptIds(object[5] != null ? object[5].toString() : null);
							dtoList.add(dto);
						}
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activities found.Activity list is null");
				
				apiLogInfo.setApiResponse("No activities found.Activity list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		System.out.println("timesheetDTO currentManagerId : "+timesheetDTO.getCurrentManagerId());
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/addTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId()+ "dayType:" +timesheetDTO.getDayType());
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {

			List<ActivityDTO> allTimesheetActivities = timesheetDTO.getAllTimesheetActivities();
			Timesheet newTimesheet = new Timesheet();

			newTimesheet.setEmpId(timesheetDTO.getEmpId());
			newTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
			newTimesheet.setDayType(timesheetDTO.getDayType());
			System.out.println("timesheetDTO.getCurrentmanagerId() ==> "+timesheetDTO.getCurrentManagerId());

			newTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());	
			if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave")) {
				
				newTimesheet.setDescription(timesheetDTO.getDescription());
				newTimesheet.setTotalTime((float)0);
				newTimesheet.setTotalWorkingHours("0");
			} else {
				
				// LocalDateTime dateTime = LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter);

				newTimesheet.setOfficeInTime(LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter));
				newTimesheet.setOfficeOutTime(LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter));
				newTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());
				
				String description = "";
				if (allTimesheetActivities.isEmpty()) {
					description = "No activity available in timesheet";
				} else {
					for (ActivityDTO activity : allTimesheetActivities) {

						description = description.concat(activity.getActivity() + "<br>");

					}
				}
				newTimesheet.setDescription(description);
			}
			newTimesheet.setStatus("Pending");
			newTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
			newTimesheet.getCommonProperty().setCreatedBy(timesheetDTO.getCreatedBy());

			Timesheet newTimesheetCreated = timesheetsRepository.save(newTimesheet);

			if (!timesheetDTO.getDayType().equals("Public Holiday") && !timesheetDTO.getDayType().equals("Week Off") && !timesheetDTO.getDayType().equals("Leave")) {				
				Optional.ofNullable(newTimesheetCreated.getEmpId()).ifPresentOrElse((timesheet) -> {
					
					List<TimesheetActivityMap> mapList = new ArrayList<TimesheetActivityMap>();
					allTimesheetActivities.forEach((activity) -> {

						TimesheetActivityMap map = new TimesheetActivityMap();
						map.setActivityId(activity.getActivityId());
						map.setCompletionTime(activity.getCompletionTime());
						if(activity.getDescription() == null) {
							Activity activityObj = activitiesRepository.getById(activity.getActivityId());
							map.setDescription(activityObj.getActivity());
						}else {
							map.setDescription(activity.getDescription());
						}
						map.setTimesheetId(newTimesheetCreated.getTimesheetId());
						map.setClientLocationId(activity.getClientLocationId());
						
						mapList.add(map);

						Float savedTime = newTimesheetCreated.getTotalTime() != null ? newTimesheetCreated.getTotalTime() : 0;
						Float totalTime = activity.getCompletionTime() + savedTime;
						newTimesheet.setTotalTime(totalTime);
						timesheetsRepository.save(newTimesheet);
					});

					List<TimesheetActivityMap> activityMapped = timesheetActivityMapRepository.saveAll(mapList);

					if (activityMapped.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Timesheet added , but activity not mapped.");
						
						apiLogInfo.setApiResponse("Timesheet added , but activity not mapped.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet added successfully");
						
						apiLogInfo.setApiResponse("Timesheet added successfully");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet not generated");
					
					apiLogInfo.setApiResponse("Timesheet not generated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				});
			} else {
				if (newTimesheetCreated == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet not generated");
					
					apiLogInfo.setApiResponse("Timesheet not generated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet added successfully");
					
					apiLogInfo.setApiResponse("Timesheet added successfully");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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

	public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllMyTimesheetsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> timesheetList = timesheetsRepository
					.getAllMyTimesheets(timesheetDTO.getEmpId(), start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {

					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString() ) : null);
						dto.setStatus(object[6] != null ? object[6].toString() : null);
						dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
						dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
						dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setRemarks(object[10] != null ? object[10].toString() : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						
						// Get InActive Activities In Timesheet
						if(dto.getStatus().equals("Pending")) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetId(dto.getTimesheetId());
							
							List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
							
							if(!inactiveActivityList.isEmpty()) {
								inactiveActivityList.forEach((actObject) -> {
									ActivityDTO actDto = new ActivityDTO();
									actDto.setTimesheetActivityMapId(actObject[0]!= null ? Long.parseLong(actObject[0].toString()) : null);
									
									inactiveDtoList.add(actDto);
								});
							
								dto.setInactiveTimesheetActivities(inactiveDtoList);						}							
						}
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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
	
	
	public ServiceResponse getAllMyTeamTimesheets(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/getAllMyTeamTimesheets");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> timesheetList = timesheetsRepository
					.getAllMyTeamTimesheets(timesheetDTO.getCreatedBy(), start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString() ) : null);
						dto.setStatus(object[6] != null ? object[6].toString() : null);
						dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
						dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
						dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setRemarks(object[10] != null ? object[10].toString() : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						
						// Get InActive Activities In Timesheet
						if(dto.getDayType().equals("Working") && (dto.getStatus().equals("Pending") || dto.getStatus().equals("Rejected"))) {
							List<Object[]> inactiveActivityList = timesheetsRepository
									.getInactiveActivitiesByTimesheetId(dto.getTimesheetId());
							
							List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
							
							if(!inactiveActivityList.isEmpty()) {
								inactiveActivityList.forEach((actObject) -> {
									ActivityDTO actDto = new ActivityDTO();
									actDto.setTimesheetActivityMapId(actObject[0] != null ? Long.parseLong(actObject[0].toString()) : null);
									
									inactiveDtoList.add(actDto);
								});
							
								dto.setInactiveTimesheetActivities(inactiveDtoList);						}							
						}	
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size: " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse getAllMyActivitiesByTimesheetId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("update_timesheet");
		apiLogInfo.setApiUrl("/api/getAllMyActivitiesByTimesheetId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		
		try {

			List<Object[]> objectList = timesheetActivityMapRepository
					.activitiesByTimesheetId(timesheetDTO.getTimesheetId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
					
					apiLogInfo.setApiResponse("No activities found.Activity list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

					list.forEach((object) -> {

						ActivityDTO dto = new ActivityDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setActivity(object[1] != null ? object[1].toString() : null);
						dto.setEta(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
						dto.setDescription(object[3] != null ? object[3].toString() : null);
						dto.setCompletionTime(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
						dto.setProjectName(object[5] != null ? object[5].toString() : null);
						dto.setClientName(object[6] != null ? object[6].toString() : null);
						dto.setClientLocation(object[7] != null ? object[7].toString() : null);
						dto.setTeamName(object[8] != null ? object[8].toString() : null);
						dto.setEmployeeName(object[9] != null ? object[9].toString() : null);
						dto.setManagerName(object[10] != null ? object[10].toString() : null);
						dto.setActivityId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
						dto.setProjectId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
						dto.setTimesheetActivityMapId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
						dto.setClientId(object[14] != null ? Integer.parseInt(object[14].toString()) : null);
						dto.setClientLocationId(object[15] != null ? Integer.parseInt(object[15].toString()) : null);
						dto.setTeamId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activities found.Activity list is null");
				
				apiLogInfo.setApiResponse("No activities found.Activity list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

//	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
//		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,status : " +timesheetDTO.getStatus());
//		try {
//
//			List<Object[]> objectList = timesheetsRepository
//					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus());
//
//			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
//
//				if (list.isEmpty()) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("No timesheets found. List is empty.");
//					
//					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				} else {
//					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
//
//					list.forEach((object) -> {
//
//						TimesheetDTO dto = new TimesheetDTO();
//						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//						dto.setDate(object[1] != null ? object[1].toString() : null);
//						dto.setDayType(object[2] != null ? object[2].toString() : null);
//						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
//						dto.setDescription(object[4] != null ? object[4].toString() : null);
//						dto.setStatus(object[5] != null ? object[5].toString() : null);
//						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
//						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
//						dto.setEmployeementId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
//						dto.setTotalTime(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
//						dto.setEmail(object[10] != null ? object[10].toString() : null);
//						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
//						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
//						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
//						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
//						dtoList.add(dto);
//					});
//
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(dtoList);
//					
//					apiLogInfo.setApiResponse("dtoList : " +dtoList );			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
//
//			}, () -> {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No  timesheets found. List is null.");
//				
//				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			});
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
//	these changes are added for temporary , we have to add one more field that is manager id in employee_timesheets table 

	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,status : " +timesheetDTO.getStatus());
		try {

			Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId()); 

			List<Object[]> objectList = timesheetsRepository
					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus(),employeeData.getDateOfJoining());

			
			
			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();
						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setStatus(object[5] != null ? object[5].toString() : null);
						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
						dto.setEmployeementId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
						dto.setTotalTime(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
						dto.setEmail(object[10] != null ? object[10].toString() : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setIsConsultant(object[16] != null ? object[16].toString() : null);
						dto.setIsApprenticeship(object[17] != null ? object[17].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList );			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	
//	public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("view_all_team_requests");
//		apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("managerId : " +timesheetDTO.getManagerId());
//		try {
//
//			Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequests(timesheetDTO.getManagerId());
//
//			if (applicationCount == 0) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No timesheet request(s) found.");
//				
//				apiLogInfo.setApiResponse("No timesheet request(s) found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//			} else {
//				timesheetDTO = new TimesheetDTO();
//				timesheetDTO.setApplicationCount(applicationCount);
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(timesheetDTO);
//				
//				apiLogInfo.setApiResponse("timesheetDTO : " +timesheetDTO);			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_all_team_requests");
		apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId());
		try {

			Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId()); 
			
			Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequests(timesheetDTO.getManagerId(),employeeData.getDateOfJoining());

			if (applicationCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No timesheet request(s) found.");
				
				apiLogInfo.setApiResponse("No timesheet request(s) found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				timesheetDTO = new TimesheetDTO();
				timesheetDTO.setApplicationCount(applicationCount);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetDTO);
				
				apiLogInfo.setApiResponse("timesheetDTO : " +timesheetDTO);			
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
	
	public ServiceResponse updateTimesheetRequestById(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_request_status");
		apiLogInfo.setApiUrl("/api/updateTimesheetRequestById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId());
		try {

			Optional<Timesheet> timesheetobject = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			timesheetobject.ifPresentOrElse((timesheet) -> {

				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheet.setRemarks(timesheetDTO.getRejectReason());

				Timesheet updatedTimesheet = timesheetsRepository.save(timesheet);

				if (updatedTimesheet.getEmpId() != null) {
					System.out.println("updatedTimesheet.getStatus ()  : "+ updatedTimesheet.getStatus());
					if (updatedTimesheet.getStatus().equals("Approved")) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet status Approved.");
						System.out.println("updatedTimesheet.getStatus ()  : "+ updatedTimesheet.getStatus());
						
						apiLogInfo.setApiResponse("Timesheet status Approved.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet status Rejected.");
						
						apiLogInfo.setApiResponse("Timesheet status Rejected.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						try {
//							mailService.sendMail(timesheetDTO.getEmail(),
//									"Regarding Timesheet Rejection ", "Employee Id"+" A-"+timesheetDTO.getEmployeementId()+
//									" "+ " <br> "+" Employee Name -"+" "+timesheetDTO.getEmployeeName()+
//									" <br> "+"Your Timesheet has been rejected "+timesheetDTO.getRejectReason());
							
							mailService.sendMailWithCC(timesheetDTO.getEmail(), timesheetDTO.getManagerEmail(), "Regarding Timesheet Request Rejection", 
									"Dear "+ timesheetDTO.getEmployeeName()+","+
							"<br> "
							+" &nbsp;"+" &nbsp;"+" "+"Your timesheet application has been rejected by "+ timesheetDTO.getManagerName() +"."+
							"<br>"+"<br>"+"<b>"+"Timesheet Details :"+"<b>"+
							"<br>"+
							"EmpID :"+" "+ timesheetDTO.getEmployeementId()+
							"<br>"+
							"Name :"+" "+ timesheetDTO.getEmployeeName()+
							"<br>"+
							" Date :"+" "+ timesheetDTO.getDate() +
							"<br>"+
							" Day Type : "+" "+ timesheetDTO.getDayType() 
							+"<br>"+
							"Total Working Hours :"+" "+timesheetDTO.getTotalWorkingOfficeHours()+" "+"(hrs)"+
							"<br>"+
							"Rejection reason :"+" "+timesheetDTO.getRejectReason());
							
						} catch (Exception e) {
							e.printStackTrace();
							response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
							response.setServiceResponse("Something Went Wrong.");
							response.setServiceError(e.getMessage());
							
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setLogLevel("ERROR");
						}
					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet status updation failed.");
					
					apiLogInfo.setApiResponse("Timesheet status updation failed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
				
				apiLogInfo.setApiResponse("Timesheet not found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse updateTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_timesheet");
		apiLogInfo.setApiUrl("/api/updateTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId()+ " ,dayType : " +timesheetDTO.getDayType());
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {

			Optional<Timesheet> timesheet = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			if (timesheet.isPresent()) {

				Timesheet existingTimesheet = timesheet.get();
                existingTimesheet.setCurrentManagerId(timesheetDTO.getCurrentManagerId());
				existingTimesheet.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				existingTimesheet.getCommonProperty().setUpdatedBy(timesheetDTO.getCreatedBy());
				existingTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
				existingTimesheet.setIsNightShift(timesheetDTO.getIsNightShift());
				/*
				 * Only pending/rejected timesheet can be updated by employee. So even if
				 * employee is updating pending timesheet or rejected timesheet the status
				 * should be set to "pending" in db. Hence we have hard coded "status" of
				 * timesheet being updated to be "pending",
				 * existingTimesheet.setStatus("Pending"); this way both pending/rejected
				 * timesheet would be set to "pending" status by default.
				 * 
				 */
				existingTimesheet.setStatus("Pending");
				existingTimesheet.setTotalTime((float) 0);

				if (timesheetDTO.getDayType().equals("Public Holiday") || timesheetDTO.getDayType().equals("Week Off") || timesheetDTO.getDayType().equals("Leave")) {
					timesheetActivityMapRepository.deleteByTimesheetId(timesheetDTO.getTimesheetId());
					existingTimesheet.setDescription(timesheetDTO.getDescription());
					existingTimesheet.setDayType(timesheetDTO.getDayType());
					existingTimesheet.setOfficeInTime(null);
					existingTimesheet.setOfficeOutTime(null);
					existingTimesheet.setTotalWorkingHours("0");

				} else {
					existingTimesheet.setDayType(timesheetDTO.getDayType());
					existingTimesheet.setOfficeInTime(LocalDateTime.parse(timesheetDTO.getOfficeInTime(), formatter));
					existingTimesheet.setOfficeOutTime(LocalDateTime.parse(timesheetDTO.getOfficeOutTime(), formatter));
					existingTimesheet.setTotalWorkingHours(timesheetDTO.getTotalWorkingOfficeHours());

					List<ActivityDTO> updatedTimesheetActivities = timesheetDTO.getAllTimesheetActivities();

					String description = "";

					if (updatedTimesheetActivities.isEmpty()) {
						description = "No activity available in timesheet";
					} else {
						for (ActivityDTO activity : updatedTimesheetActivities) {

							description = description.concat(activity.getActivity() + "<br>");

						}
					}

					existingTimesheet.setDescription(description);

					timesheetDTO.getUpdatedTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() == null)
							.forEach((activity) -> {

								TimesheetActivityMap map = new TimesheetActivityMap();
								map.setActivityId(activity.getActivityId());
								map.setCompletionTime(activity.getCompletionTime());
								if(activity.getDescription() == null) {
									Activity activityObj = activitiesRepository.getById(activity.getActivityId());
									map.setDescription(activityObj.getActivity());
								}else {
									map.setDescription(activity.getDescription());
								}
								map.setTimesheetId(timesheetDTO.getTimesheetId());
								map.setClientLocationId(activity.getClientLocationId());
								timesheetActivityMapRepository.save(map);

							});
					timesheetDTO.getUpdatedTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() != null)
							.forEach((activity) -> {

								timesheetActivityMapRepository.deleteById(activity.getTimesheetActivityMapId());

							});

					timesheetDTO.getAllTimesheetActivities().stream()
							.filter(activities -> activities.getTimesheetActivityMapId() != null)
							.forEach((activity) -> {

								Optional<TimesheetActivityMap> existingMap = timesheetActivityMapRepository
										.findById(activity.getTimesheetActivityMapId());

								if (existingMap.isPresent()) {
									TimesheetActivityMap map = existingMap.get();

									map.setActivityId(activity.getActivityId());
									map.setCompletionTime(activity.getCompletionTime());
									if(activity.getDescription() == null) {
										Activity activityObj = activitiesRepository.getById(activity.getActivityId());
										map.setDescription(activityObj.getActivity());
									}else {
										map.setDescription(activity.getDescription());
									}
									map.setClientLocationId(activity.getClientLocationId());
									timesheetActivityMapRepository.save(map);
								}
							});

				}

				Float totalTime = (float) 0;

				List<TimesheetActivityMap> timesheetActivityMapObj = timesheetActivityMapRepository
						.findByTimesheetId(timesheetDTO.getTimesheetId());
				for (TimesheetActivityMap mappingFound : timesheetActivityMapObj) {
					totalTime += mappingFound.getCompletionTime();
				}
				existingTimesheet.setTotalTime(totalTime);

				Timesheet updatedTimesheet = timesheetsRepository.save(existingTimesheet);

				if (updatedTimesheet.getTimesheetId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet updated.");
					apiLogInfo.setApiResponse("Timesheet updated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet updation failed.");
					apiLogInfo.setApiResponse("timesheet updation failed");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found.");
				apiLogInfo.setApiResponse("timesheet not found");			
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

	public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
		apiLogInfo.setApiUrl("/api/getMyReporteesApprovedTimesheets");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ " ,startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate());
		try {
			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> objectList = timesheetsRepository.getMyReporteesApprovedTimesheets(
					timesheetDTO.getManagerId(), start, end);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("Appreciation not Enabled");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setStatus(object[5] != null ? object[5].toString() : null);
						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
						dto.setTotalTime(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
						dto.setRemarks(object[9] != null ? object[9].toString() : null);
						dto.setEmployeementId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
						dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
						dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
						dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
						dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
						dto.setLeaveType(object[15] != null ? object[15].toString() : null);
						dto.setIsConsultant(object[16] != null ? object[16].toString() : null);
						dto.setIsApprenticeship(object[17] != null ? object[17].toString() : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse getLast7DaysTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/getLast7DaysTimesheetsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId());
		try {

			List<Object[]> objectList = timesheetsRepository.getLast7DaysTimesheetsByEmpId(timesheetDTO.getEmpId(),
					LocalDate.now().minusDays(timesheetLockDays));

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					apiLogInfo.setApiResponse("No timesheets found. List is empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size : " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse addClientAndProjectByList(ProjectDTO projectDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/addClientAndProjectByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +projectDTO.getProjectManagerId()+ " ,projectId : " +projectDTO.getProjectId() );
		try {

			Optional<Employee> EmpId = Optional
					.ofNullable(employeeRepository.findByEmployeementId(projectDTO.getProjectManagerId()));
			if (EmpId.isPresent()) {

				Employee employee = EmpId.get();
				Long primaryId = employee.getEmpId();

				Project project = new Project();

				project.setProjectId(projectDTO.getProjectId());
				project.setClientLocation(projectDTO.getClientLocation());
				project.setClientName(projectDTO.getClientName());
				project.setProjectManagerId(primaryId);
				project.setProjectName(projectDTO.getProjectName());
				project.setState(projectDTO.getState());

				project.setCreatedOn(projectDTO.getCreatedOn());

				Project dbresponse = projectRepository.save(project);

				if (dbresponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project and Client added.");
					
					apiLogInfo.setApiResponse("Project and Client added.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Creation of Project and client failed");
					
					apiLogInfo.setApiResponse("Creation of Project and client failed");			
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

	public ServiceResponse getTimesheetsForHomePageByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_timesheet_display");
		apiLogInfo.setApiUrl("/api/getTimesheetsForHomePageByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ " ,endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> objectList = timesheetsRepository.getTimesheetsForHomePageByEmpId(timesheetDTO.getEmpId(),
					start, end);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					apiLogInfo.setApiResponse("No timesheets found. List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
					
				} else {
					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO dto = new TimesheetDTO();

						dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setDate(object[1] != null ? object[1].toString() : null);
						dto.setWeekDayName(object[2] != null ? object[2].toString() : null);
						dto.setTotalWorkingHours(object[3] != null ? Float.parseFloat(object[3].toString()) : 0);
						dto.setStatus(object[4] != null ? object[4].toString() : null);
						dto.setDayType(object[5] != null ? object[5].toString() : null);
						dto.setDescription(object[6] != null ? object[6].toString() : null);
						dto.setActivity(object[7] != null ? object[7].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
				
				apiLogInfo.setApiResponse("No  timesheets found. List is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			});

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

	public ServiceResponse revokeApprovedTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("revoke_reportee_timesheet");
		apiLogInfo.setApiUrl("/api/revokeApprovedTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId() );
		try {

			Optional<Timesheet> timesheetObj = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			timesheetObj.ifPresentOrElse((timesheetFound) -> {

				timesheetFound.setStatus("Pending");
				timesheetsRepository.save(timesheetFound);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Timesheet revoked successfully");
				
				apiLogInfo.setApiResponse("Timesheet revoked successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found.");
				
				apiLogInfo.setApiResponse("Timesheet not found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);				
				
			});

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
	
	public ServiceResponse bulkApproveTimesheetRequest(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("BulkApproveTimesheetRequest");
		apiLogInfo.setApiUrl("/api/bulkApproveTimesheetRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BulkApprovedList : " + timesheetDTO.getBulkApprovedList().size());
	
		try {
			
			for (TimesheetDTO timesheet : timesheetDTO.getBulkApprovedList()) {
				
				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				response = updateTimesheetRequestById(timesheet);
			    
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
	
	public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("BulkRejectTimesheetRequest");
		apiLogInfo.setApiUrl("/api/bulkRejectTimesheetRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BulkRejectList : " + timesheetDTO.getBulkRejectList().size());
		try {
			
			for (TimesheetDTO timesheet : timesheetDTO.getBulkRejectList()) {
				
				timesheet.setStatus(timesheetDTO.getStatus());
				timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
				timesheet.setRejectReason(timesheetDTO.getRejectReason());
				response = updateTimesheetRequestById(timesheet);   
				System.out.println("   timesheet Reject reason __" +timesheetDTO.getRejectReason());
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
	
	
	public ServiceResponse getAllLeaveTimesheetsWithoutLeaveApplication(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveTimesheetsWithoutLeaveApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> timesheetList = timesheetsRepository
					.getAllLeaveTimesheetsWithoutLeaveApplication(start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
					
					apiLogInfo.setApiResponse("Timesheet list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {

					List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

					list.forEach((object) -> {

						TimesheetDTO timesheetDto = new TimesheetDTO();
						timesheetDto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						timesheetDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
						timesheetDto.setDate(object[2] != null ? object[2].toString() : null);
						timesheetDto.setDayType(object[3] != null ? object[3].toString() : null);
						timesheetDto.setDescription(object[4] != null ? object[4].toString() : null);
						timesheetDto.setStatus(object[5] != null ? object[5].toString() : null);
						timesheetDto.setLeaveType(object[6] != null ? object[6].toString() : null);
						timesheetDto.setManagerName(object[7] != null ? object[7].toString() : null);
						timesheetDto.setDepartmentName(object[8] != null ? object[8].toString() : null);
						timesheetDto.setCreatedOn(object[9] != null ? object[9].toString() : null);
						timesheetDto.setUpdatedOn(object[10] != null ? object[10].toString() : null);
						timesheetDto.setTimesheetStatusUpdatedByName(object[11] != null ? object[11].toString() : null);
						timesheetDto.setIsConsultant(object[12] != null ? object[12].toString() : null);
						timesheetDto.setIsApprenticeship(object[13] != null ? object[13].toString() : null);
						
						dtoList.add(timesheetDto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList size : " +dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
				
				apiLogInfo.setApiResponse("Timesheet list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

//	@Transactional
//	public void updateCurrentManagerInTimesheets() {
//	    List<Object[]> empAndManagerList = timesheetsRepository.findEmployeesAndTheirManagers();
//
//	    for (Object[] empAndManager : empAndManagerList) {
//	        Long empId = ((BigInteger) empAndManager[0]).longValue();
//	        Long managerId = ((BigInteger) empAndManager[1]).longValue();
//
//	        // Update current_manager_id in the employee_timesheets table
//	        timesheetsRepository.updateCurrentManagerId(empId, managerId);
//	    }
//	}
	
//	public List<Object[]> getManagerIdUpdates(Long empId) {
//	    String query = "SELECT e.manager_id, MAX(e.updated_on) AS latest_update " +
//	                   "FROM employee_aud e " +
//	                   "WHERE e.emp_id = :emp_id " +
//	                   "GROUP BY e.manager_id " +
//	                   "ORDER BY latest_update DESC " +
//	                   "LIMIT 2";
//	    List<Object[]> results = auditCustomRepository.readAuditCustomNativeQueryy(query, empId);
//	    
//	    // Convert the Timestamp to LocalDateTime
//	    for (Object[] result : results) {
//	        if (result[1] instanceof Timestamp) {
//	            result[1] = ((Timestamp) result[1]).toLocalDateTime();
//	        }
//	    }
//	    
//	    return results;
//	}
//
//	@Async
//	@Scheduled(cron = "0 05 17 * * ?")
//	public void updateTimesheetManagerIds() {
//		System.err.println("--cron started----")	;    
//		List<Long> employeeIds = timesheetsRepository.findDistinctEmpIds(); // Method to fetch distinct empIds
//		
//	    for (Long empId : employeeIds) {
//	        List<Object[]> managerUpdates = getManagerIdUpdates(empId);
//
//	        if (managerUpdates.isEmpty() || managerUpdates.get(0)[0] == null) {
//	            continue; // No manager updates found or the first managerId is null, skip this employee
//	        }
//
//	        // Convert BigInteger to Long
//	        Long firstManagerId = ((BigInteger) managerUpdates.get(0)[0]).longValue();
//	        LocalDateTime latestUpdate = (LocalDateTime) managerUpdates.get(0)[1];
//	        Long secondManagerId = managerUpdates.size() > 1 ? ((BigInteger) managerUpdates.get(1)[0]).longValue() : null;
//
//	        List<Timesheet> timesheets = timesheetsRepository.findTimesheetsByEmpIdOrderByCreatedOn(empId);
//
//	        for (Timesheet timesheet : timesheets) {
//	            LocalDateTime createdOn = timesheet.getCommonProperty().getCreatedOn().toLocalDateTime(); // Access createdOn
//
//	            if (latestUpdate == null || createdOn.isBefore(latestUpdate)) {
//	                timesheetsRepository.updateCurrentManagerId(timesheet.getTimesheetId(), 
//	                                                            secondManagerId != null ? secondManagerId : firstManagerId);
//	            } else {
//	                timesheetsRepository.updateCurrentManagerId(timesheet.getTimesheetId(), firstManagerId);
//	            }
//	        }
//	    }
//	    System.err.println("--------cron ended---");
//	}

    
    

}
