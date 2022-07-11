package com.apmosys.employeeportal.service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
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

	public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Project> projectList = projectRepository.findByEmpId(timesheetDTO.getEmpId());

			if (projectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No projects found for given employee Id.");
			} else {
				Type typeList = new TypeToken<List<ProjectDTO>>() {
				}.getType();
				List<ProjectDTO> previousEmploymentList = modelMapper.map(projectList, typeList);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(previousEmploymentList);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllActivitiesByProjectIdandEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = projectRepository
					.getActivitiesByProjectIdAndEmployeeId(timesheetDTO.getProjectId(), timesheetDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
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

	public ServiceResponse addTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<ActivityDTO> allTimesheetActivities = timesheetDTO.getAllTimesheetActivities();
			Timesheet newTimesheet = new Timesheet();

			newTimesheet.setEmpId(timesheetDTO.getEmpId());
			newTimesheet.setDate(stringToDateTimeParser.getDate(timesheetDTO.getDate()));
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

			Optional.ofNullable(newTimesheetCreated.getEmpId()).ifPresentOrElse((timesheet) -> {

				List<TimesheetActivityMap> mapList = new ArrayList<TimesheetActivityMap>();

				allTimesheetActivities.forEach((activity) -> {

					TimesheetActivityMap map = new TimesheetActivityMap();
					map.setActivityId(activity.getActivityId());
					map.setCompletionTime(activity.getCompletionTime());
					map.setDescription(activity.getDescription());
					map.setTimesheetId(newTimesheetCreated.getTimesheetId());
					mapList.add(map);
				});

				List<TimesheetActivityMap> activityMapped = timesheetActivityMapRepository.saveAll(mapList);

				if (activityMapped.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet added , but activity not mapped.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet added successfully");
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not generated");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());

			LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

			List<Timesheet> timesheetList = timesheetsRepository
					.findAllByEmpIdAndDateBetweenOrderByDateDesc(timesheetDTO.getEmpId(), start, end);

			Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet list is empty");
				} else {

					Type typeList = new TypeToken<List<TimesheetDTO>>() {
					}.getType();
					List<TimesheetDTO> timesheetDTOList = modelMapper.map(list, typeList);

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(timesheetDTOList);

				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllMyActivitiesByTimesheetId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = timesheetActivityMapRepository
					.activitiesByTimesheetId(timesheetDTO.getTimesheetId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activities found.Activity list is empty");
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
						dto.setClientLocation(object[6] != null ? object[6].toString() : null);
						dto.setTeamName(object[7] != null ? object[7].toString() : null);

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

	public ServiceResponse getMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = timesheetsRepository
					.getMyReporteesTimesheetRequests(timesheetDTO.getManagerId(), timesheetDTO.getStatus());

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
						dto.setDayType(object[2] != null ? object[2].toString() : null);
						dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
						dto.setDescription(object[4] != null ? object[4].toString() : null);
						dto.setStatus(object[5] != null ? object[5].toString() : null);
						dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
						dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  timesheets found. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateTimesheetRequestById(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<Timesheet> timesheetobject = timesheetsRepository.findById(timesheetDTO.getTimesheetId());

			timesheetobject.ifPresentOrElse((timesheet) -> {

				timesheet.setStatus(timesheetDTO.getStatus());

				Timesheet updatedTimesheet = timesheetsRepository.save(timesheet);

				if (updatedTimesheet.getEmpId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Timesheet status updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet status updation failed.");
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
