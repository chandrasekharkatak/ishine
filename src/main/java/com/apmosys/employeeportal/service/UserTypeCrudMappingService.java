package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.UserTypeCrudMappingDTO;
import com.apmosys.employeeportal.dto.UserTypeDTO;
import com.apmosys.employeeportal.model.UserTypeCrudMapping;
import com.apmosys.employeeportal.repository.UserTypeCrudMappingRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class UserTypeCrudMappingService {

//	@Autowired
//	UserTypeCrudMappingRepository userTypeCrudMappingRepository;
//	
//	@Autowired
//	StringToDateTimeParser stringToDateTimeParser;
//
//	public ServiceResponse getAllUserTypeCrudMappings() {
//
//		ServiceResponse response = new ServiceResponse();
//		try {
//			List<UserTypeCrudMapping> mapList = userTypeCrudMappingRepository.findAll();
//
//			if (mapList != null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(mapList);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("User Type CRUD Map List is null.");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}
//
//	public ServiceResponse updateCrudMappingsByMapId(UserTypeCrudMappingDTO userTypeCrudMappingDTO) {
//		ServiceResponse response = new ServiceResponse();
//
//		try {
//			Optional<UserTypeCrudMapping> crudMapOptional = userTypeCrudMappingRepository.findById(userTypeCrudMappingDTO.getMapId());
//			
//			if(crudMapOptional.isPresent())
//			{
//				UserTypeCrudMapping existingCrudMapping =crudMapOptional.get();
//				
//				existingCrudMapping.setCreation(userTypeCrudMappingDTO.getCreation());
//				existingCrudMapping.setDeletion(userTypeCrudMappingDTO.getDeletion());
//				existingCrudMapping.setView(userTypeCrudMappingDTO.getView());
//				existingCrudMapping.setEdit(userTypeCrudMappingDTO.getEdit());
//				existingCrudMapping.setModuleVisibilty(userTypeCrudMappingDTO.getModuleVisibilty());
//				existingCrudMapping.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				
//				UserTypeCrudMapping dbResponse = userTypeCrudMappingRepository.save(existingCrudMapping);
//				
//				if (dbResponse != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("CRUD mapping Updated.");
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("CRUD mapping Updation Failed.");
//				}
//			}
//			else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("CRUD mapping Not Found");
//			}
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}
//
//	public ServiceResponse getUserTypeCrudMappingsByUserTypeId(UserTypeDTO userTypeDTO) {
//		ServiceResponse response = new ServiceResponse();
//		try
//		{
//			List<UserTypeCrudMapping> list = userTypeCrudMappingRepository.findAllByUserTypeId(userTypeDTO.getUserTypeId());
//			
//			if(list.isEmpty())
//			{
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("User Type CRUD Map List not found for user type Id "+userTypeDTO.getUserTypeId()+".");
//			}
//			else
//			{
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(list);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}
}
