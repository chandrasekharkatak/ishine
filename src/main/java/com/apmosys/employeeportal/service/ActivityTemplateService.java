package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
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

	public ServiceResponse createActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
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
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Activity already exist.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
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
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Activities found.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getActivityTemplateById(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
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

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Activities found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateActivityTemplate(ActivityTemplateDTO activityTemplateDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Long> activityId = new ArrayList<Long>();
			ActivityTemplate activityDbResponse = null;
			ActivityTemplate dbResposne = null;
			for (ActivityDTO object : activityTemplateDTO.getTemplateActivityList()) {
				
				ActivityTemplate checkActivityName = activityTemplateRepository
						.findByTemplateActivityAndDeptIdAndEmployeeRole(object.getActivity(), activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
				
				if(checkActivityName != null) {
					activityTemplateDTO.getTemplateActivityList().remove(object);
				}else {
					if (object.getActivityTemplateId() != null) {
						ActivityTemplate activityTemplate = activityTemplateRepository.getById(object.getActivityTemplateId());
						
						activityId.add(object.getActivityTemplateId());
						activityTemplate.setTemplateActivity(object.getActivity());
						activityDbResponse = activityTemplateRepository.save(activityTemplate);
					} else {
						ActivityTemplate activity = new ActivityTemplate();
						
						activity.setDeptId(activityTemplateDTO.getDeptId());
						activity.setEmployeeRole(activityTemplateDTO.getEmployeeRole());
						activity.setTemplateActivity(object.getActivity());
						dbResposne = activityTemplateRepository.save(activity);
						activityId.add(dbResposne.getActivityTemplateId());
					}					
				}
				
			}
			List<ActivityTemplate> activityToBeDeleted = activityTemplateRepository
					.findByActivityTemplateIdNotInAndDeptIdAndEmployeeRole(activityId, activityTemplateDTO.getDeptId(), activityTemplateDTO.getEmployeeRole());
			if(!activityToBeDeleted.isEmpty()) {
				activityToBeDeleted.forEach((object) -> {
					activityTemplateRepository.deleteById(object.getActivityTemplateId());
				});
			}else {
				response.setServiceResponse("No Activities found to delete.");
			}
			
			if(activityDbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Activity template updated successfully.");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Activity template updation failed.");
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
