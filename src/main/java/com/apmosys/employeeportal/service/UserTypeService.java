package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.UserTypeCrudMappingDTO;
import com.apmosys.employeeportal.dto.UserTypeDTO;
import com.apmosys.employeeportal.model.Modules;
import com.apmosys.employeeportal.model.UserType;
import com.apmosys.employeeportal.model.UserTypeCrudMapping;
import com.apmosys.employeeportal.repository.ModulesRepository;
import com.apmosys.employeeportal.repository.UserTypeCrudMappingRepository;
import com.apmosys.employeeportal.repository.UserTypeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class UserTypeService {

	@Autowired
	UserTypeRepository userTypeRepository;

	@Autowired
	UserTypeCrudMappingRepository userTypeCrudMappingRepository;

	@Autowired
	ModulesRepository modulesRepository;

	public ServiceResponse getAllUserTypes() {

		ServiceResponse response = new ServiceResponse();
		try {
			List<UserType> list = userTypeRepository.findAll();
			List<UserTypeCrudMapping> mapList = userTypeCrudMappingRepository.findAll();

			if (list != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(list);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("User Type List is null.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	

	public ServiceResponse createUserType(UserTypeDTO userTypeDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			UserType newUserType = new UserType();
			newUserType.setUserType(userTypeDTO.getUserType());
			UserType newUserTypeCreated = userTypeRepository.save(newUserType);
			if (newUserTypeCreated != null) {

				UserTypeCrudMapping map = new UserTypeCrudMapping();
				List<UserTypeCrudMapping> mapList = new ArrayList<>();
				List<Modules> modulesList = modulesRepository.findAll();

				for (Modules m : modulesList) {
					map = new UserTypeCrudMapping();
					map.setCreation("false");
					map.setDeletion("false");
					map.setView("false");
					map.setEdit("false");
					map.setModuleVisibilty("false");
					map.setModule(m.getModule());
					map.setModuleId(m.getModuleId());
					map.setUserTypeId(newUserTypeCreated.getUserTypeId());
					mapList.add(map);
				}

				userTypeCrudMappingRepository.saveAll(mapList);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("User Type Created And CRUD Mapping Done Successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("User Type Creation Failed.");
				return response;
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
