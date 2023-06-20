package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class ActivitiesService {

	@Autowired
	ActivitiesRepository activitiesRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	TimesheetActivityMapRepository timesheetActivityMapRepository;

	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	public ServiceResponse createActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_activity");
		apiLogInfo.setApiUrl("/api/createActivity");
		apiLogInfo.setLogLevel("INfo");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : "+activityDTO.getProjectId()+", teamName : "+activityDTO.getTeamId());
		try {
			
			List<Activity> dtoList = new ArrayList<Activity>();
			
			// Multiple department
			StringBuilder department = new StringBuilder("");
			for(String deptId: activityDTO.getDepartmentList()) {
				department.append(deptId).append(",");
			}
			
				Activity newActivity = new Activity();
				
				newActivity.setActivity(activityDTO.getActivity());
				newActivity.setEta(activityDTO.getEta());
				newActivity.setTeamId(activityDTO.getTeamId());
				newActivity.setEmployeeRole(activityDTO.getEmployeeRole());
				newActivity.setDeptIds(department.toString());
				newActivity.getCommonProperty().setCreatedBy(activityDTO.getCreatedBy());
				
				dtoList.add(newActivity);

			List<Activity> newActivityCreated = activitiesRepository.saveAll(dtoList);

			Optional.ofNullable(newActivityCreated).ifPresentOrElse((activity) -> {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New activity added to team.");
				apiLogInfo.setApiResponse("New Activity added to team");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to add new activity to team.");
				apiLogInfo.setApiResponse("Failed to add new activity to team.");
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

	public ServiceResponse updateActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_activity");
		apiLogInfo.setApiUrl("/api/updateActivity");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("activityId : "+activityDTO.getActivityId()+", projectName : "+activityDTO.getProjectName());

		try {

			Optional<Activity> existingActivityObject = activitiesRepository.findById(activityDTO.getActivityId());

			if (existingActivityObject.isPresent()) {
				Activity existingActivity = existingActivityObject.get();

				existingActivity.setActivity(activityDTO.getActivity());
				existingActivity.setEta(activityDTO.getEta());
				existingActivity.setTeamId(activityDTO.getTeamId());
				existingActivity.setEmployeeRole(activityDTO.getEmployeeRole());
				existingActivity.getCommonProperty().setUpdatedBy(activityDTO.getUpdatedBy());
				existingActivity.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

				Activity existingActivityUpdated = activitiesRepository.save(existingActivity);

				Optional.ofNullable(existingActivityUpdated).ifPresentOrElse((activity) -> {

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Activity updated.");
					apiLogInfo.setApiResponse("Activity Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to updated activity");
					apiLogInfo.setApiResponse("Failed to create activity");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				});

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activity found for given id.");
				apiLogInfo.setApiResponse("No activity found for given id");
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

	public ServiceResponse getAllActivitiesByProjectIdAndTeamId(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllActivitiesByProjectIdAndTeamId");
		apiLogInfo.setApiUrl("/api/getAllActivitiesByProjectIdAndTeamId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder=new StringBuilder();
		logBuilder.append("getAllActivitiesByProjectIdAndTeamId size : "+ activitiesRepository
					.getAllActivitiesByProjectIdAndTeamId(activityDTO.getProjectId(), activityDTO.getTeamId()).size());
		
		try {

			List<Object[]> activityList = activitiesRepository
					.getAllActivitiesByProjectIdAndTeamId(activityDTO.getProjectId(), activityDTO.getTeamId());

			Optional.ofNullable(activityList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found, Activity list is empty");
					apiLogInfo.setApiResponse("No activities found, Activity list is empty");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);				
				} else {

					List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

					list.forEach((object) -> {

						ActivityDTO dto = new ActivityDTO();

						dto.setActivityId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setProjectId(object[1] != null ? Integer.parseInt(object[1].toString()) : null);
						dto.setProjectName(object[2] != null ? object[2].toString() : null);
						dto.setClientName(object[3] != null ? object[3].toString() : null);
						dto.setTeamName(object[4] != null ? object[4].toString() : null);
						dto.setTeamId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
						dto.setActivity(object[6] != null ? object[6].toString() : null);
						dto.setEta(object[7] != null ? Float.parseFloat(object[7].toString()) : null);
						dto.setCreatedByName(object[8] != null ? object[8].toString() : null);
						dto.setCreatedOn(object[9] != null ? object[9].toString() : null);
						dto.setEmployeeRole(object[10] != null ? object[10].toString() : null);
						dto.setDepartmentList(object[11] != null ? object[11].toString().split(",") : null);

						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("list fetched of size : "+dtoList.size());
					apiLogInfo.setApiResponse(ServiceResponse.STATUS_SUCCESS);
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

	public ServiceResponse deleteActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_activity");
		apiLogInfo.setApiUrl("/api/deleteActivity");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Activity Id : "+activityDTO.getActivityId());
		
		try {
			if (activityDTO.getActivityId() != null) {
				Optional<Activity> activityObject = activitiesRepository.findById(activityDTO.getActivityId());
				if(!activityObject.isEmpty()) {
					
					Long count = timesheetActivityMapRepository.countByActivityId(activityDTO.getActivityId());
					
					if(count == 0) {
						activitiesRepository.deleteById(activityDTO.getActivityId());
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Activity deleted.");
						apiLogInfo.setApiResponse("Activity deleted.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Activity cannot be deleted as it is mapped with timesheet.");
						apiLogInfo.setApiResponse("Activity cannot be deleted as it is mapped with timesheet.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Activity id cannot be null.");
					apiLogInfo.setApiResponse("Activity id cannot be null.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);			
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Activity id cannot be null.");
				apiLogInfo.setApiResponse("Activity id cannot be null.");
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

}
