package com.apmosys.employeeportal.service;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;
import org.hibernate.internal.build.AllowSysOut;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class TimesheetService {

	@Autowired
	TimesheetsRepository timesheetsRepository;

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
					.getActivitiesByProjectIdAndEmployeeId(timesheetDTO.getProjectId(), timesheetDTO.getEmpId());

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

						dto.setActivityId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setActivity(object[1] != null ? object[1].toString() : null);
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

	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/addTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +timesheetDTO.getEmpId()+ "dayType:" +timesheetDTO.getDayType());
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {

			List<ActivityDTO> allTimesheetActivities = timesheetDTO.getAllTimesheetActivities();
			Timesheet newTimesheet = new Timesheet();

			newTimesheet.setEmpId(timesheetDTO.getEmpId());
			newTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
			newTimesheet.setDayType(timesheetDTO.getDayType());
			if (timesheetDTO.getDayType().equals("Holiday")) {
				newTimesheet.setDescription(timesheetDTO.getDescription());
			} else {
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
			newTimesheet.getCommonProperty().setCreatedBy(timesheetDTO.getCreatedBy());

			Timesheet newTimesheetCreated = timesheetsRepository.save(newTimesheet);

			if (!timesheetDTO.getDayType().equals("Holiday")) {				
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
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate() );
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
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate() );
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
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
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
						dto.setActivityId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setProjectId(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
						dto.setTimesheetActivityMapId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
						dto.setClientId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
						dto.setClientLocationId(object[13] != null ? Integer.parseInt(object[13].toString()) : null);
						
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

	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
		apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ "status : " +timesheetDTO.getStatus());
		try {

			List<Object[]> objectList = timesheetsRepository
					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus());

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
						dto.setEmployeementId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
						dto.setTotalTime(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
						dto.setEmail(object[10] != null ? object[10].toString() : null);
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

	public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_all_team_requests");
		apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId());
		try {

			Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequests(timesheetDTO.getManagerId());

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
							mailService.sendMail(timesheetDTO.getEmail(),
									"Regarding Timesheet Rejection ", "Employee Id"+" A-"+timesheetDTO.getEmployeementId()+
									" "+ " <br> "+" Employee Name -"+" "+timesheetDTO.getEmployeeName()+" <br> "+timesheetDTO.getRejectReason());
							
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
		logBuilder.append("timeSheetId : " +timesheetDTO.getTimesheetId()+ "dayType : " +timesheetDTO.getDayType());
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {

			Optional<Timesheet> timesheet = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			if (timesheet.isPresent()) {

				Timesheet existingTimesheet = timesheet.get();

				existingTimesheet.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				existingTimesheet.getCommonProperty().setUpdatedBy(timesheetDTO.getCreatedBy());
				existingTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate(), "yyyy-MM-dd"));
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

				if (timesheetDTO.getDayType().equals("Holiday")) {
					timesheetActivityMapRepository.deleteByTimesheetId(timesheetDTO.getTimesheetId());
					existingTimesheet.setDescription(timesheetDTO.getDescription());
					existingTimesheet.setDayType(timesheetDTO.getDayType());

				} else {
					existingTimesheet.setDayType(timesheetDTO.getDayType());

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
								map.setDescription(activity.getDescription());
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
									map.setDescription(activity.getDescription());
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
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet updation failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
		apiLogInfo.setApiUrl("/api/getMyReporteesApprovedTimesheets");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +timesheetDTO.getManagerId()+ "startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate());
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
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
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
		logBuilder.append("managerId : " +projectDTO.getProjectManagerId()+ "projectId : " +projectDTO.getProjectId() );
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
		logBuilder.append("startDate : " +timesheetDTO.getStartDate()+ "endDate : " +timesheetDTO.getEndDate() );
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Object[]> objectList = timesheetsRepository.getTimesheetsForHomePageByEmpId(timesheetDTO.getEmpId(),
					start, end);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No timesheets found. List is empty.");
					
					
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
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
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
		}
		return response;
		
	}
	
	public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
	
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
		}
		return response;
		
	}
	

}
