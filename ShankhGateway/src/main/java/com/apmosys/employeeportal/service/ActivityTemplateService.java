package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;


@Service
public class ActivityTemplateService {
	
	@Autowired
	ActivityTemplateRepository activityTemplateRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;	

	public ServiceResponse createActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_activityTempltae");
		apiLogInfo.setApiUrl("/api/createActivityTemplate");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : "+activityTemplateDTO.getDeptId());
		
		try {
			List<ActivityTemplate> dtoList = new ArrayList<ActivityTemplate>();
			
			activityTemplateDTO.getTemplateActivityList().forEach((object) -> {
				
				ActivityTemplate checkActivityName = activityTemplateRepository
						.findByTemplateActivityAndDeptIdAndEmployeeRole(object.getActivity(), activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
				
				if(checkActivityName == null) {
					ActivityTemplate activity = new ActivityTemplate();
					
					activity.setDeptId(activityTemplateDTO.getDeptId());
					activity.setEmployeeRole(activityTemplateDTO.getEmployeeRole());
					activity.setTemplateActivity(object.getActivity());
					dtoList.add(activity);
				}
			});
			List<ActivityTemplate> dbResponse = activityTemplateRepository.saveAll(dtoList);
			
			if(!dbResponse.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Activity template created successfully.");
				
				apiLogInfo.setApiResponse("Activity template created successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Activity already exist.");
				
				apiLogInfo.setApiResponse("Activity already exist.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse("ERROR");
			
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		
		return response;
	}

	public ServiceResponse getActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActivityTemplate");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("activityTemplateId : "+activityTemplateDTO.getActivityTemplateId()+",templateName : "+activityTemplateDTO.getDepartmentName());
		
		try {
			
			List<ActivityTemplate> activityTemplate = activityTemplateRepository
					.getByDeptId(activityTemplateDTO.getDeptId());
			List<ActivityTemplateDTO> dtoList = new ArrayList<ActivityTemplateDTO>();
			
			if(activityTemplate != null) {
				
				activityTemplate.forEach((object) -> {
					ActivityTemplateDTO dto = new ActivityTemplateDTO();
					Department department = departmentRepository.getById(object.getDeptId());
					dto.setActivityTemplateId(object.getActivityTemplateId());
					dto.setDepartmentName(department.getName());
					dto.setDeptId(object.getDeptId());
					dto.setEmployeeRole(object.getEmployeeRole());
					dto.setActivityDescription("All activities of " + department.getName() + " department");
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);

				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Activities found.");
				
				apiLogInfo.setApiResponse("No Activities found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
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

	public ServiceResponse getActivityTemplateById(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActivityTemplateById");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("activityTemplateId : "+activityTemplateDTO.getActivityTemplateId());
		
		ActivityTemplateDTO dto = new ActivityTemplateDTO();
		List<ActivityDTO> actList = new ArrayList<ActivityDTO>();
		try {
			
			List<ActivityTemplate> activityTemplateById = activityTemplateRepository
					.getByDeptIdAndEmployeeRole(activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
			
			dto.setDeptId(activityTemplateDTO.getDeptId());
			dto.setEmployeeRole(activityTemplateDTO.getEmployeeRole());
			
			if(!activityTemplateById.isEmpty()) {
				activityTemplateById.forEach((object) -> {
					ActivityDTO activity = new ActivityDTO();
					activity.setActivity(object.getTemplateActivity());
					activity.setActivityTemplateId(object.getActivityTemplateId());
					actList.add(activity);
				});
				
				dto.setTemplateActivityList(actList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
				apiLogInfo.setApiResponse("Template fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Activities found.");
				apiLogInfo.setApiResponse("No Activities found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse updateActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_activityTempltae");
		apiLogInfo.setApiUrl("/api/updateActivityTemplate");
		apiLogInfo.setLogLevel("INFO");
		
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("activityTemplateId : "+activityTemplateDTO.getActivityTemplateId()+", activity : "+activityTemplateDTO.getActivity());		
		try {
			List<Long> activityId = new ArrayList<Long>();
			for(ActivityDTO object : activityTemplateDTO.getTemplateActivityList()) {
				if(object.getActivityTemplateId() != null) {
					ActivityTemplate activityTemplate = activityTemplateRepository.getById(object.getActivityTemplateId());
					
					activityTemplate.setTemplateActivity(object.getActivity());
					ActivityTemplate activityDbResponse = activityTemplateRepository.save(activityTemplate);
					
					if(activityDbResponse != null) {
						activityId.add(object.getActivityTemplateId());
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Activity template updated successfully.");
						apiLogInfo.setApiResponse("Activity template updated successfully.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Activity template updation failed.");
						apiLogInfo.setApiResponse("Activity template updation failed.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					ActivityTemplate checkActivityName = activityTemplateRepository
							.findByTemplateActivityAndDeptIdAndEmployeeRole(object.getActivity(), activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
					
					if(checkActivityName == null) {
						ActivityTemplate activity = new ActivityTemplate();
						
						activity.setDeptId(activityTemplateDTO.getDeptId());
						activity.setEmployeeRole(activityTemplateDTO.getEmployeeRole());
						activity.setTemplateActivity(object.getActivity());
						ActivityTemplate dbResposne = activityTemplateRepository.save(activity);
						
						if(dbResposne != null) {
							activityId.add(dbResposne.getActivityTemplateId());
							
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("New Activity Added in Template Successfully.");
							apiLogInfo.setApiResponse("New Activity Added in Template Successfully.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to add new Actvity in template.");
							apiLogInfo.setApiResponse("Unable to add new Actvity in template.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}						
					}
				}
			}
			
			//Remove Activity from template
			
			List<ActivityTemplate> activityToBeDeleted = activityTemplateRepository
					.findByActivityTemplateIdNotInAndDeptIdAndEmployeeRole(activityId, activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
			if(!activityToBeDeleted.isEmpty()) {
				activityToBeDeleted.forEach((object) -> {
					activityTemplateRepository.deleteById(object.getActivityTemplateId());
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

}
