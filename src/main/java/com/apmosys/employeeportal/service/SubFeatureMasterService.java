package com.apmosys.employeeportal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class SubFeatureMasterService {
	
	@Autowired
	SubFeatureMasterRepository subFeatureMasterRepository;
	
	public ServiceResponse getSubfeaturesByJobRoleId(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try
		{
//			Optional<JobRole> jobRoleObject = subFeatureMasterRepository.find(jobRoleDTO.getId());
//			if (jobRoleObject.isPresent()) {
//				JobRole jobRoleToBeDeleted = jobRoleObject.get();				
//				jobRoleRepository.deleteById(jobRoleToBeDeleted.getJobRoleId());				
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse("Job Role Deleted.");
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Job Role Not Found.");
//			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
