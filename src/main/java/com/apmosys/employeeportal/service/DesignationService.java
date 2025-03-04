package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DesignationDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.DesignationDepartmentMap;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.DesignationDepartmentMapRepository;
import com.apmosys.employeeportal.repository.DesignationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;


@Service
public class DesignationService {
	
	@Autowired
	DesignationRepository designationRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	DesignationDepartmentMapRepository designationDepartmentMapRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	public ServiceResponse createDesignation(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_designation");
		apiLogInfo.setApiUrl("/api/createDesignation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationName : "+designationDTO.getDesignationName()+ ", createdBy : "+designationDTO.getCreatedBy()+", deptIdList : "+designationDTO.getDeptIdList());
				
		try {
			Designation designationObj = new Designation();
			
			designationObj.setDesignationName(designationDTO.getDesignationName());
			designationObj.setCreatedBy(designationDTO.getCreatedBy());
			
			Designation dbResponse = designationRepository.save(designationObj);
			
			if(dbResponse != null) {
				List<DesignationDepartmentMap> mapList = new ArrayList<DesignationDepartmentMap>();
				
				for(Long deptId : designationDTO.getDeptIdList()) {
					DesignationDepartmentMap designationDeptObj = new DesignationDepartmentMap();
					
					designationDeptObj.setDeptId(deptId);
					designationDeptObj.setDesignationId(dbResponse.getDesignationId());
					
					mapList.add(designationDeptObj);
				}
				List<DesignationDepartmentMap> mapResponse = designationDepartmentMapRepository.saveAll(mapList);
				
				if(!mapResponse.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Designation Created Successfully.");
					
					apiLogInfo.setApiResponse("Designation Created Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to create New Designation.");
					
					apiLogInfo.setApiResponse("Unable to create New Designation.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to create New Designation.");
				
				apiLogInfo.setApiResponse("Unable to create New Designation.");
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

	public ServiceResponse getAllDesignation() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllDesignation");
		apiLogInfo.setApiUrl("/api/getAllDesignation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllDesignation size : "+designationRepository.findAllDesignation().size());
		
		try {
			
			List<Object[]> designationList = designationRepository.findAllDesignation();
			List<DesignationDTO> dtoList = new ArrayList<DesignationDTO>();
			
			if(!designationList.isEmpty()) {
				designationList.forEach((object) -> {
					DesignationDTO dto = new DesignationDTO();
					
					dto.setDesignationId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					dto.setDesignationName(object[3] != null ? object[3].toString() : null);
					dto.setUpdatedByName(object[4] != null ? object[4].toString() : null);
					dto.setUpdatedOn(object[5] != null ? object[5].toString() : null);
					dto.setCreatedBy(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					dto.setUpdatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					
					dtoList.add(dto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Designation List is empty.");
				apiLogInfo.setApiResponse("Designation List is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse checkDesignationName(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkDesignationName");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationName : "+designationDTO.getDesignationName());
		
		try {
			
			Designation designationObj = designationRepository.findByDesignationName(designationDTO.getDesignationName());
			
			if(designationObj != null) {
				if(((designationDTO.getDesignationId() != null) && (!designationDTO.getDesignationId().equals(designationDTO.getDesignationId()))
						|| designationDTO.getDesignationId() == null)) {
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Designation name already exists.");
					
					apiLogInfo.setApiResponse("Designation name already exists.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
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

	public ServiceResponse getDesignationById(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getDesignationById");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationId : "+designationDTO.getDesignationId()+", designationName : "+designationDTO.getDesignationName());		
		
		try {
			
			Designation designationObj = designationRepository.findByDesignationId(designationDTO.getDesignationId());
			
			if(designationObj != null) {
				DesignationDTO dtoObj = new DesignationDTO();
				
				dtoObj.setDesignationId(designationObj.getDesignationId());
				dtoObj.setDesignationName(designationObj.getDesignationName());
				
				List<DesignationDepartmentMap> mappObj = designationDepartmentMapRepository.findByDesignationId(designationObj.getDesignationId());
				Set<Long> deptids = new HashSet<Long>();
				
				if(!mappObj.isEmpty()) {
					mappObj.forEach((object) -> {
						deptids.add(object.getDeptId());
					});
				}
				
				if(!deptids.isEmpty()) {
					dtoObj.setDeptIdList(deptids.toArray(new Long[deptids.size()]));
				}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoObj);
				
				apiLogInfo.setApiResponse("List Fetched of size : "+dtoObj);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find Designation.");
				
				apiLogInfo.setApiResponse("Unable to find Designation.");
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

	public ServiceResponse updateDesignation(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_designation");
		apiLogInfo.setApiUrl("/api/updateDesignation");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationId : "+designationDTO.getDesignationId()+", designationName : "+designationDTO.getDesignationName());
		
		try {
			
			Designation designationObj = designationRepository.findByDesignationId(designationDTO.getDesignationId());
			
			if(designationObj != null) {
				
				designationObj.setDesignationName(designationDTO.getDesignationName());
				designationObj.setUpdatedBy(designationDTO.getUpdatedBy());
				designationObj.setUpdatedOn(Timestamp.valueOf(stringToDateTimeParser.getCurrentDateTime()));
				
				Designation dbResponse = designationRepository.save(designationObj);
				
				if(dbResponse != null) {
					
					//Save new Department while update
					for(Long id :designationDTO.getDeptIdList()) {
						DesignationDepartmentMap mappObj = designationDepartmentMapRepository.findByDesignationIdAndDeptId(dbResponse.getDesignationId(),id);
						
						if(mappObj == null) {
							DesignationDepartmentMap designationDeptObj = new DesignationDepartmentMap();
							
							designationDeptObj.setDeptId(id);
							designationDeptObj.setDesignationId(dbResponse.getDesignationId());
							
							designationDepartmentMapRepository.save(designationDeptObj);
						}
					}
					
					//Delete Department
					List<DesignationDepartmentMap> mappObj = designationDepartmentMapRepository.findByDesignationId(dbResponse.getDesignationId());
					
					if(!mappObj.isEmpty()) {
						mappObj.forEach((object) -> {
							boolean contains = Arrays.stream(designationDTO.getDeptIdList()).anyMatch(i -> i == object.getDeptId());
							if(!contains) {
								designationDepartmentMapRepository.deleteById(object.getDesignationDeptMapId());
							}
						});
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Designation updated successfully.");
					
					apiLogInfo.setApiResponse("Designation updated successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find Designation.");
				
				apiLogInfo.setApiResponse("Unable to find Designation.");
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

	public ServiceResponse getDesignationByDeptId(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_DesignationByDeptId");
		apiLogInfo.setApiUrl("/api/getDesignationByDeptId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationId : "+designationDTO.getDesignationId()+", designationName : "+designationDTO.getDesignationName()+", deptId : "+designationDTO.getDeptId());
		
		try {
			
			List<Object[]> mappObj = designationDepartmentMapRepository.findAllDesignationByDeptId(designationDTO.getDeptId());
			List<DesignationDTO> dtoList = new ArrayList<DesignationDTO>();
			
			if(!mappObj.isEmpty()) {
				mappObj.forEach((object) -> {
					DesignationDTO dto = new DesignationDTO();
					
					dto.setDesignationName(object[0] != null ? object[0].toString() : null);
					dto.setDesignationId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("List Fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Designation found.");
				
				apiLogInfo.setApiResponse("No Designation found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setLogLevel("ERROR");
			
			response.setServiceError(e.getMessage());
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteDesignation(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_designation");
		apiLogInfo.setApiUrl("/api/deleteDesignation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationId : "+designationDTO.getDesignationId()+", designationName : "+designationDTO.getDesignationName());
		
		try {
			
			Designation designationObj = designationRepository.findByDesignationId(designationDTO.getDesignationId());
			Set<Long> deptIds = new HashSet<Long>();
			
			if (designationObj != null) {

				Long count = employeeRepository.countByDesignationId(designationObj.getDesignationId());
				if (count == 0) {
					
					//Delete DesignationDeptMappings
					List<DesignationDepartmentMap> desginationDeptMap = designationDepartmentMapRepository.findByDesignationId(designationObj.getDesignationId());
					
					if(!desginationDeptMap.isEmpty()) {
						desginationDeptMap.forEach((object) -> {
							designationDepartmentMapRepository.deleteById(object.getDesignationDeptMapId());
						});
					}
					
					//Delete Designation
					designationRepository.deleteById(designationObj.getDesignationId());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Designation Deleted Successfully.");
					
					apiLogInfo.setApiResponse("Designation Deleted Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					
					//filtered Designation List
					List<DesignationDepartmentMap> desginationDeptMap = designationDepartmentMapRepository.findByDesignationId(designationObj.getDesignationId());
					if(!desginationDeptMap.isEmpty()) {
						desginationDeptMap.forEach((object) -> {
							deptIds.add(object.getDeptId());
						});
					}
					
					List<DesignationDTO> dtoList = new ArrayList<DesignationDTO>();
					if(!deptIds.isEmpty()) {	
						int deptIdsLength = deptIds.size();
						
						List<Object[]> filteredDesignation = designationDepartmentMapRepository.getFilteredDesignation(deptIds,deptIdsLength);
						
						if(!filteredDesignation.isEmpty()) {
							filteredDesignation.forEach((object) -> {
								
								DesignationDTO dto = new DesignationDTO();
								

								dto.setDesignationId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
								dto.setDesignationName(object[1] != null ? object[1].toString() : null);
								
								dtoList.add(dto);
							});
							
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse(dtoList);
							
							apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							
						}
					}
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Designation Not Found.");
				
				apiLogInfo.setApiResponse("Designation Not Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setLogLevel("ERROR");
			
			response.setServiceError(e.getMessage());
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse changeEmployeeDesignationMapping(DesignationDTO designationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/changeEmployeeDesignationMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("designationId : "+designationDTO.getDesignationId()+", designationName : "+designationDTO.getDesignationName()+", newDesignationId"+designationDTO.getNewDesignationId());
		
		try {
			List<Employee> employeeDesignation = employeeRepository.findByDesignationId(designationDTO.getDesignationId());
			List<Employee> updatedEmployeeInfo = new ArrayList<Employee>();
			
			
			if (!employeeDesignation.isEmpty()) {
				employeeDesignation.forEach((object) -> {
					object.setDesignationId(designationDTO.getNewDesignationId());
					
					updatedEmployeeInfo.add(object);
				});
				
				if(!updatedEmployeeInfo.isEmpty()) {
					List<Employee> dbResponse = employeeRepository.saveAll(updatedEmployeeInfo);
					
					if(!dbResponse.isEmpty()) {
						ServiceResponse deleteResponse = deleteDesignation(designationDTO);
						
						if(deleteResponse.getServiceStatus().equals("Success")) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(deleteResponse.getServiceResponse());
							
							apiLogInfo.setApiResponse(deleteResponse.getServiceResponse().toString());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);							
							
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse(deleteResponse.getServiceResponse());
							
							apiLogInfo.setApiResponse(deleteResponse.getServiceResponse().toString());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to change Designation & Employee Mapping.");
						
						apiLogInfo.setApiResponse("Unable to change Designation & Employee Mapping.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to update employee Designation.");
					
					apiLogInfo.setApiResponse("Unable to update employee Designation.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No employee found for designation update.");
				
				apiLogInfo.setApiResponse("No employee found for designation update.");
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

}
