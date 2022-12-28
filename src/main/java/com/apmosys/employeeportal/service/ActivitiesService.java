package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Tuple;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
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

	public ServiceResponse createActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Activity newActivity = new Activity();

			newActivity.setActivity(activityDTO.getActivity());
			newActivity.setEta(activityDTO.getEta());
			newActivity.setTeamId(activityDTO.getTeamId());
			newActivity.setEmployeeRole(activityDTO.getEmployeeRole());
			newActivity.getCommonProperty().setCreatedBy(activityDTO.getCreatedBy());

			Activity newActivityCreated = activitiesRepository.save(newActivity);

			Optional.ofNullable(newActivityCreated).ifPresentOrElse((activity) -> {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New activity added to team.");

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to add new activity to team.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
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

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to updated activity");
				});

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activity found for given id.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllActivitiesByProjectIdAndTeamId(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> activityList = activitiesRepository
					.getAllActivitiesByProjectIdAndTeamId(activityDTO.getProjectId(), activityDTO.getTeamId());

			Optional.ofNullable(activityList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
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

						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No activities found.Activity list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse deleteActivity(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (activityDTO.getActivityId() != null) {
				Optional<Activity> activityObject = activitiesRepository.findById(activityDTO.getActivityId());
				if(!activityObject.isEmpty()) {
					
					Long count = timesheetActivityMapRepository.countByActivityId(activityDTO.getActivityId());
					
					if(count == 0) {
						activitiesRepository.deleteById(activityDTO.getActivityId());
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Activity deleted.");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Activity cannot be deleted as it is mapped with timesheet.");
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Activity id cannot be null.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Activity id cannot be null.");
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
