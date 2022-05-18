package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class JobRoleService {

	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	public ServiceResponse createJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			JobRole newJobRole = new JobRole();
		//	Department department = new Department();
			newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
			newJobRole.setName(jobRoleDTO.getName());
		//	department.setDept_id(jobRoleDTO.getDepartmentId());
			newJobRole.setDeptId(jobRoleDTO.getDepartmentId());

			JobRole dbResponse = jobRoleRepository.save(newJobRole);
			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New Job Role Created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Job Role Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllJobRole() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allJobRoleList = jobRoleRepository.getAllJobRoles();
			List<JobRoleDTO> dtoList = new ArrayList<>();
			if (!allJobRoleList.isEmpty()) {
				
				for(Object[] object:allJobRoleList)
				{
					JobRoleDTO jobRoleDTO = new JobRoleDTO();
					jobRoleDTO.setName(object[0].toString());
					jobRoleDTO.setCreatedBy(object[1].toString());
					jobRoleDTO.setCreatedOn(object[2].toString());
					dtoList.add(jobRoleDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role List is empty.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getId());
			if(jobRoleObject.isPresent())
			{
				JobRole jobRoleToBeUpdated = jobRoleObject.get();
		//		Department department = new Department();
		//		department.setDept_id(jobRoleDTO.getDepartmentId());
				jobRoleToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				jobRoleToBeUpdated.setName(jobRoleDTO.getName());
				jobRoleToBeUpdated.setDeptId(jobRoleDTO.getDepartmentId());
				
				JobRole dbResponse = jobRoleRepository.save(jobRoleToBeUpdated);
				
				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Job Role Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Job Role Updation Failed.");
				}
			}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found");
			}
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

	public ServiceResponse deleteJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getId());
			if (jobRoleObject.isPresent()) {
				JobRole jobRoleToBeDeleted = jobRoleObject.get();				
				jobRoleRepository.deleteById(jobRoleToBeDeleted.getJobRoleId());				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Job Role Deleted.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found.");
			}	
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
