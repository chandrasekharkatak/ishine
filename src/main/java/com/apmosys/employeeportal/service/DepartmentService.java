package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.event.StoreListener;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.DesignationDepartmentMap;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DesignationDepartmentMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class DepartmentService {
	private final Map<String, List<DepartmentDTO>> DepartmentDTOCache = new ConcurrentHashMap<>();

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	JobRoleRepository jobRoleRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	DesignationDepartmentMapRepository designationDepartmentMapRepository;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	EmployeeOnBoardingRepository employeeOnBoardingRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Value("${poPortal.api.syncDepartment}")
	private String syncDepartmentApi;
	
	@Value("${poPortal.api.isDepartmentUsed}")
	private String isDeparmentUsedPoPortal;
	
	@Value("${poPortal.api.deleteDepartment}")
	private String deleteDeparmentPoPortal;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Autowired
	private ApiLogUtility apiLogUtility;
	
	@Autowired
	private PoPortalAPIService poPortalAPIService;

	@Transactional
	public ServiceResponse createDepartment(DepartmentDTO departmentDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_department");
		apiLogInfo.setApiUrl("/api/createDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("departmentName : " +departmentDTO.getName()+ "hodId : " +departmentDTO.getHodId()+ "createdBy : " +departmentDTO.getCreatedBy());
		try {
			// Check if abbreviation already exists
			 Department existingDept = departmentRepository.findByDeptAbbreviation(departmentDTO.getDeptAbbreviation());
		        if (existingDept != null) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Department Abbreviation already exists.");
		            
		            apiLogInfo.setApiResponse("Department Abbreviation already exists.");            
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);  
		            apiLogInfo.setLogLevel("ERROR");
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
		        }
		        
		        System.err.println(departmentDTO.getName()) ; 
		        List<Department> existingDeptName = departmentRepository.findByDeptName(departmentDTO.getName());
		        System.err.println(existingDeptName) ;     
		        if (existingDeptName != null && !existingDeptName.isEmpty()) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Department Name already exists.");
		            
		            apiLogInfo.setApiResponse("Department Name already exists.");            
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);  
		            apiLogInfo.setLogLevel("ERROR");
		            logService.logMyInfo(httpRequest, apiLogInfo);
		            return response;
		        }
		        		
		        		
		        		
		        		
		    Department newDepartment = new Department();
		        
			newDepartment.setName(departmentDTO.getName()); 
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());
			newDepartment.setDeptAbbreviation(departmentDTO.getDeptAbbreviation());

			Department newDepartmentCreated = departmentRepository.save(newDepartment);

			if (newDepartmentCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New department created.");
				
				apiLogInfo.setApiResponse("New department created.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New department creation Failed.");
				
				apiLogInfo.setApiResponse("New department creation Failed.");			
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
	
	@Transactional
	public ServiceResponse createDepartmentByList(DepartmentDTO departmentDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		

		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("create_department");
		apiLogInfo.setApiUrl("/api/createDepartmentByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " +departmentDTO.getDeptId()+", departmentName : " +departmentDTO.getName()+ ", hodId : " +departmentDTO.getHodId()+ ", createdBy : " +departmentDTO.getCreatedBy());
		try {
			Department newDepartment = new Department();
			newDepartment.setDeptId(departmentDTO.getDeptId());
			newDepartment.setName(departmentDTO.getName());
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());

			Department newDepartmentCreated = departmentRepository.save(newDepartment);

			if (newDepartmentCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New department created.");
				
				apiLogInfo.setApiResponse("New department created");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New department creation Failed.");
				
				apiLogInfo.setApiResponse("New department creation Failed");			
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

	public ServiceResponse getAllDepartments() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllDepartment");
		apiLogInfo.setApiUrl("/api/createDepartmentByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllDepartment size : "+departmentRepository.getAllDepartments().size());
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

		try {
			List<DepartmentDTO> allDepartmentList = departmentRepository.getAllDepartments();
			if (allDepartmentList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department List is Empty.");
				apiLogInfo.setApiResponse("Department List is Empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				List<DepartmentDTO> dtoList = new ArrayList<DepartmentDTO>();
//				for (DepartmentDTO object : allDepartmentList) {
//					DepartmentDTO departmentDTO = new DepartmentDTO();
//					departmentDTO.setDeptId(Long.parseLong(object[0].toString()));
//					departmentDTO.setCreatedBy(Integer.parseInt(object[1].toString()));
//					departmentDTO.setCreatedOn(object[2].toString());
//					departmentDTO.setName(object[3].toString());
//					departmentDTO.setCreatedByName(object[4].toString());
//					departmentDTO.setHodName(object[5].toString());
//					departmentDTO.setHodId(Long.parseLong(object[6].toString()));
//					departmentDTO.setUpdatedOn(object[7] != null ? object[7].toString(): null);
//					departmentDTO.setUpdatedByName(object[8] != null ? object[8].toString() : null);
//					departmentDTO.setDeptAbbreviation(object[9] != null ? object[9].toString() : null);
//					departmentDTO.setUpdatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
//					dtoList.add(departmentDTO);      
//				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(allDepartmentList);
				apiLogInfo.setApiResponse("dtoList Size : "+allDepartmentList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}
		} catch (Exception e) {
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

	public ServiceResponse getAllDepartmentsFromId(Long empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
		
		List<Department> deptList = departmentRepository.findByHodId(empId);
		if(!deptList.isEmpty() && deptList!= null) {
		serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		serviceResponse.setServiceResponse(deptList);
		return serviceResponse;
		}else {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("No data found..!");
			return serviceResponse;
		}
		}
		catch(Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			return serviceResponse;
		}
	}
	public ServiceResponse updateDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_department");
		apiLogInfo.setApiUrl("/api/updateDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " + departmentDTO.getDeptId()+", departmentName : "+departmentDTO.getDeptName());
		try {
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isPresent()) {
				Department departmentToBeUpdated = departmentObject.get();
				
				// Check if the abbreviation is being updated and if the new one already exists
				if (departmentDTO.getDeptAbbreviation() != null && 
		                (departmentToBeUpdated.getDeptAbbreviation() == null || 
		                !departmentToBeUpdated.getDeptAbbreviation().equals(departmentDTO.getDeptAbbreviation()))) {
	                Department existingDept = departmentRepository.findByDeptAbbreviation(departmentDTO.getDeptAbbreviation());
	                if (existingDept != null && !existingDept.getDeptId().equals(departmentDTO.getDeptId())) {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Department Abbreviation already exists.");
	                    
	                    apiLogInfo.setApiResponse("Department Abbreviation already exists.");            
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);  
	                    apiLogInfo.setLogLevel("ERROR");
	                    logService.logMyInfo(httpRequest, apiLogInfo);
	                    return response;
	                }
	            }
				departmentToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				departmentToBeUpdated.setName(departmentDTO.getName());
				departmentToBeUpdated.setHodId(departmentDTO.getHodId());
				departmentToBeUpdated.setDeptAbbreviation(departmentDTO.getDeptAbbreviation());
				departmentToBeUpdated.setUpdatedBy(departmentDTO.getUpdatedBy());

				Department dbResponse = departmentRepository.save(departmentToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Department Updated.");
					
					apiLogInfo.setApiResponse("Department Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department Updation Failed.");
					
					apiLogInfo.setApiResponse("Department Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found");
				
				apiLogInfo.setApiResponse("Department Not Found");			
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

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_department");
		apiLogInfo.setApiUrl("/api/deleteDepartment");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("deptId : " + departmentDTO.getDeptId());
		try {
			boolean isDeptUsedInPoPortal = false;
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found.");
				apiLogInfo.setApiResponse("Department Not Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			Department departmentToBeDeleted = departmentObject.get();
			Long count = jobRoleRepository.countByDeptId(departmentToBeDeleted.getDeptId());

			ServiceResponse syncResponse = poPortalAPIService.isDepartmentUsedInPoPortal(departmentToBeDeleted.getDeptId());
			if (syncResponse != null && syncResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				String deptarmentUsedFlag = syncResponse.getServiceResponse().toString(); // 0=Not Present, 1=Present but not used, 2=Used
				if (deptarmentUsedFlag.equals("0")) {
					isDeptUsedInPoPortal = false;
				}
				if (deptarmentUsedFlag.equals("1")) { // Delete dept from poPortal
					poPortalAPIService.deleteDepartment(departmentToBeDeleted.getDeptId());
				}
				if (deptarmentUsedFlag.equals("2")) {
					isDeptUsedInPoPortal = true;
				}
			}

			if (count == 0 && !isDeptUsedInPoPortal) {
				departmentRepository.deleteById(departmentToBeDeleted.getDeptId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Department deleted.");
				apiLogInfo.setApiResponse("Department deleted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				DepartmentDTO dtoObject = new DepartmentDTO();
				dtoObject.setIsDeptUsedInIshine(count != 0 ? "true" : "false");
				dtoObject.setIsDeptUsedInPoPortal(isDeptUsedInPoPortal ? "true" : "false");
				response.setServiceResponse(dtoObject);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse(dtoObject + "Department cannot be deleted as it is mapped to job role(s).");
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
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse changeDepartmentJobRoleMapping(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/changeDepartmentJobRoleMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder("deptId: ").append(departmentDTO.getDeptId());
		try {
			List<JobRole> jobRoles = jobRoleRepository.findByDeptId(departmentDTO.getOldDeptId());
			if (!jobRoles.isEmpty()) {
				processJobRoles(jobRoles, departmentDTO, response, apiLogInfo);
			} else {
				handleNoJobRolesFound(departmentDTO, response, apiLogInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private void processJobRoles(List<JobRole> jobRoles, DepartmentDTO departmentDTO, ServiceResponse response, LogDTO apiLogInfo) {
		boolean isUsedInIshine = Boolean.parseBoolean(departmentDTO.getIsDeptUsedInIshine());
		for (JobRole jobRole : jobRoles) {
			if (isUsedInIshine) {
				jobRole.setDeptId(departmentDTO.getDeptId());
				jobRoleRepository.save(jobRole);
			} else {
				jobRole.setName("Not used in Ishine");
			}

			updateDesignationDepartmentMapping(departmentDTO, response, apiLogInfo);
			updateProjectDepartmentMapping(departmentDTO, response, apiLogInfo);
			updateAssetDepartmentMapping(departmentDTO, response, apiLogInfo);

			if (Boolean.parseBoolean(departmentDTO.getIsDeptUsedInPoPortal())) {
				handlePoPortalSync(departmentDTO, response);
			} else {
				response.setServiceResponse("Department Deleted successfully.");
				apiLogInfo.setApiRequest("Department Deleted successfully.");
			}
		}
	}

	private void updateDesignationDepartmentMapping(DepartmentDTO dto, ServiceResponse response, LogDTO apiLogInfo) {
		List<DesignationDepartmentMap> list = designationDepartmentMapRepository.findByDeptId(dto.getOldDeptId());
		if (!list.isEmpty()) {
			list.forEach(d -> d.setDeptId(dto.getDeptId()));
			List<DesignationDepartmentMap> updated = designationDepartmentMapRepository.saveAll(list);

			if (!updated.isEmpty()) {
				response.setServiceResponse("Designation Department Mapping Changed successfully.");
				apiLogInfo.setApiRequest("Designation Department Mapping Changed successfully.");
			} else {
				response.setServiceResponse("Unable to change Designation Department Mapping.");
				apiLogInfo.setApiResponse("Unable to change Designation Department Mapping.");
			}
		}
	}

	private void updateProjectDepartmentMapping(DepartmentDTO dto, ServiceResponse response, LogDTO apiLogInfo) {
		List<ProjectDepartmentMap> list = projectDepartmentMapRepository.findByDeptId(dto.getOldDeptId());
		if (!list.isEmpty()) {
			list.forEach(p -> p.setDeptId(dto.getDeptId()));
			List<ProjectDepartmentMap> updated = projectDepartmentMapRepository.saveAll(list);

			if (!updated.isEmpty()) {
				response.setServiceResponse("Project Department Mapping Changed successfully.");
				apiLogInfo.setApiRequest("Project Department Mapping Changed successfully.");
			} else {
				response.setServiceResponse("Unable to change Project Department Mapping.");
				apiLogInfo.setApiResponse("Unable to change Project Department Mapping.");
			}
		}
	}

	private void updateAssetDepartmentMapping(DepartmentDTO dto, ServiceResponse response, LogDTO apiLogInfo) {
		List<Asset> list = employeeOnBoardingRepository.findByDeptId(dto.getOldDeptId());
		if (!list.isEmpty()) {
			list.forEach(a -> a.setDeptId(dto.getDeptId()));
			List<Asset> updated = employeeOnBoardingRepository.saveAll(list);

			if (!updated.isEmpty()) {
				response.setServiceResponse("Asset Department Mapping Changed successfully.");
				apiLogInfo.setApiRequest("Asset Department Mapping Changed successfully.");
			} else {
				response.setServiceResponse("Unable to change Asset Department Mapping.");
				apiLogInfo.setApiResponse("Unable to change Asset Department Mapping.");
			}
		}
	}

	private void handlePoPortalSync(DepartmentDTO dto, ServiceResponse response) {
		ServiceResponse syncResponse = poPortalAPIService.syncDeleteDepartmentWithPoPortal(dto, "changeDepartmentJobRoleMapping");
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		if (!syncResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		response.setServiceResponse(syncResponse.getServiceResponse());
	}

	private void handleNoJobRolesFound(DepartmentDTO dto, ServiceResponse response, LogDTO apiLogInfo) {
		if (Boolean.parseBoolean(dto.getIsDeptUsedInPoPortal())) {
			handlePoPortalSync(dto, response);
		} else {
			response.setServiceResponse("No job role Found");
			apiLogInfo.setApiResponse("No job role Found");
		}
	}

	public ServiceResponse checkDepartmentName(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkDepartmentName");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("departmentName : "+departmentDTO.getName());
		try {
			
			Department deptObj = departmentRepository.findByName(departmentDTO.getName());
			
			if(deptObj != null) {
				if((departmentDTO.getDeptId() != null) && !departmentDTO.getDeptId().equals(deptObj.getDeptId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department with same name already exists !!");
					apiLogInfo.setApiResponse("Department with same name already exists !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
				if(departmentDTO.getDeptId() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department with same name already exists !!");
					apiLogInfo.setApiResponse("Department with same name already exists !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
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

	public ServiceResponse getAllDepartmentInfo() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllDepartmentInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest), "getAllDepartmentInfo", "PoPortal", null, httpRequest);
			List<PoPortalDTO> poPortalDTOList = departmentRepository.getDepartmentInfo();
			if (!poPortalDTOList.isEmpty()) {
				logBuilder.append("getAllDepartmentInfo size : " + poPortalDTOList.size());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(poPortalDTOList);
				apiLogInfo.setApiResponse("dtoList size : " + poPortalDTOList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Info not found.");
				apiLogInfo.setApiResponse("Department Info not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
				throw new DataNotFoundException("Department Details Not Found in Database.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
			}
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	

}
