package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.SubFeatureMasterDTO;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeRole;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRoleMasterRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class JobRoleService {

	private static final Logger log = LoggerFactory.getLogger(JobRoleService.class);

	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	SubFeatureMasterRepository subFeatureMasterRepository;

	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeRoleMasterRepository employeeRoleMasterRepository;

	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Value("${poPortal.api.syncJobRole}")
	private String syncJobRoleApi;
	
	@Value("${poPortal.api.isJobRoleUsed}")
	private String isJobRoleUsedPoPortal;
	
	@Value("${poPortal.api.deleteJobRole}")
	private String deleteJobRolePoPortal;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Autowired
	private ApiLogUtility apiLogUtility;

	@Autowired
	private PoPortalAPIService poPortalAPIService;
	
//	public ServiceResponse createJobRole(JobRoleDTO jobRoleDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("Create Role");
//		apiLogInfo.setApiUrl("/api/createJobRole");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("createdBy : " + jobRoleDTO.getCreatedById() + "name : " +jobRoleDTO.getName()+ "employeeRole :" +jobRoleDTO.getEmployeeRole()+ "departmentId : " +jobRoleDTO.getDepartmentId());
//		System.out.println(jobRoleDTO);
//		try {
//			JobRole newJobRole = new JobRole();
//			newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
//			newJobRole.setName(jobRoleDTO.getName());
//			newJobRole.setEmployeeRole(jobRoleDTO.getEmployeeRole());
//			newJobRole.setDeptId(jobRoleDTO.getDepartmentId());
//
//			JobRole dbResponse = jobRoleRepository.save(newJobRole);
//			if (dbResponse != null) {
//
////				List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository
////						.findBySubFeatureType((short) 1);
//				List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
//						.findByEmployeeRoleAndPermission(newJobRole.getEmployeeRole(), "Y");
//
//				if (defaultSubFeatureList.isEmpty()) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("New Job Role Created. But Default SubFeatures List Is Empty.");
//					
//					apiLogInfo.setApiResponse("New Job Role Created. But Default SubFeatures List Is Empty.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					
//					return response;
//				} else {
//					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
//					for (EmployeeRole employeeRole : defaultSubFeatureList) {
//						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
//						roleFeatureMap.setSubFeatureMasterId(employeeRole.getSubFeatureMasterId());
//						roleFeatureMap.setJobRoleId(dbResponse.getJobRoleId());
//						roleFeatureMapList.add(roleFeatureMap);
//					}
//					List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository.saveAll(roleFeatureMapList);
//					if (savedRoleFeatureMapList.isEmpty()) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(
//								"New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");
//						
//						apiLogInfo.setApiResponse("New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");			
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						return response;
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse("New Job Role Created.");
//						
//						apiLogInfo.setApiResponse("New Job Role Created.");			
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					}
//				}
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("New Job Role Creation Failed.");
//				
//				apiLogInfo.setApiResponse("New Job Role Creation Failed");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createJobRole(JobRoleDTO jobRoleDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Create Role");
	    apiLogInfo.setApiUrl("/api/createJobRole");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("createdBy : ").append(jobRoleDTO.getCreatedById())
	              .append(", name : ").append(jobRoleDTO.getName())
	              .append(", employeeRole : ").append(jobRoleDTO.getEmployeeRole())
	              .append(", departmentId : ").append(jobRoleDTO.getDepartmentId());

	    try {
	        // --- Step 1: Validate duplicate role name under the same department ---
	        JobRole existingRole = jobRoleRepository.findDuplicatesByNameAndDeptId(
	                jobRoleDTO.getName().trim(),
	                jobRoleDTO.getDepartmentId()
	        );

	        if (existingRole != null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job Role with same name already exists in this department.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Duplicate Job Role Found.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Step 2: Create and save new Job Role ---
	        JobRole newJobRole = new JobRole();
	        newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
	        newJobRole.setName(jobRoleDTO.getName().trim());
	        newJobRole.setEmployeeRole(jobRoleDTO.getEmployeeRole());
	        newJobRole.setDeptId(jobRoleDTO.getDepartmentId());

	        JobRole savedJobRole = jobRoleRepository.save(newJobRole);

	        if (savedJobRole == null || savedJobRole.getJobRoleId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job Role creation failed. Repository returned null.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Repository returned null while saving Job Role.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Step 3: Fetch Default SubFeatures (permissions) ---
	        List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
	                .findByEmployeeRoleAndPermission(savedJobRole.getEmployeeRole(), "Y");

	        if (defaultSubFeatureList == null || defaultSubFeatureList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job Role created, but no default subfeatures found.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Job Role created, but default subfeatures list empty.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Step 4: Assign Default SubFeatures to new Role ---
	        List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();
	        for (EmployeeRole employeeRole : defaultSubFeatureList) {
	            RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
	            roleFeatureMap.setSubFeatureMasterId(employeeRole.getSubFeatureMasterId());
	            roleFeatureMap.setJobRoleId(savedJobRole.getJobRoleId());
	            roleFeatureMapList.add(roleFeatureMap);
	        }

	        List<RoleFeatureMap> savedRoleFeatureMaps = roleFeatureMapRepository.saveAll(roleFeatureMapList);
	        if (savedRoleFeatureMaps == null || savedRoleFeatureMaps.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job Role created, but subfeatures could not be assigned.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Failed to assign default subfeatures to Job Role.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Step 5: All Success ---
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("New Job Role created successfully.");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Job Role created successfully.");
	        apiLogInfo.setApiRequest(logBuilder.toString());

	    } catch (DataIntegrityViolationException e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Duplicate entry detected while creating Job Role.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("DataIntegrityViolationException: " + e.getMessage());

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong during Job Role creation.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	    }

	    // --- Step 6: Log the complete request and response ---
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse createJobRoleByList(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/createJobRoleByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("createdBy : " + jobRoleDTO.getCreatedById() + "name : " +jobRoleDTO.getName()+ "employeeRole :" +jobRoleDTO.getEmployeeRole()+ "departmentId : " +jobRoleDTO.getDepartmentId());
		try {
			JobRole newJobRole = new JobRole();
			// Department department = new Department();
			newJobRole.setJobRoleId(jobRoleDTO.getJobRoleId());
			newJobRole.setCreatedBy(jobRoleDTO.getCreatedById());
			newJobRole.setName(jobRoleDTO.getName());
			// department.setDept_id(jobRoleDTO.getDepartmentId());
			newJobRole.setDeptId(jobRoleDTO.getDepartmentId());
			newJobRole.setEmployeeRole(jobRoleDTO.getEmployeeRole());

			JobRole dbResponse = jobRoleRepository.save(newJobRole);
			if (dbResponse != null) {

				List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository
						.findBySubFeatureType((short) 1);

				if (defaultSubFeatureMasterList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("New Job Role Created. But Default SubFeatures List Is Empty.");
				
					apiLogInfo.setApiResponse("New Job Role Created. But Default SubFeatures List Is Empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
					return response;
				} else {
					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
					for (SubFeatureMaster subFeatureMaster : defaultSubFeatureMasterList) {
						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
						roleFeatureMap.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
						roleFeatureMap.setJobRoleId(dbResponse.getJobRoleId());
						roleFeatureMapList.add(roleFeatureMap);
					}
					List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository.saveAll(roleFeatureMapList);
					if (savedRoleFeatureMapList.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");
						
						apiLogInfo.setApiResponse("New Job Role Created. But Default SubFeatures Was Not Assigned To The Role.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						return response;
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("New Job Role Created.");
						
						apiLogInfo.setApiResponse("New Job Role Created.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Job Role Creation Failed.");
				
				apiLogInfo.setApiResponse("New Job Role Creation Failed.");			
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

	public ServiceResponse getAllJobRole() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allJobRoleList = jobRoleRepository.getAllJobRoles();
			List<JobRoleDTO> dtoList = new ArrayList<>();
			if (!allJobRoleList.isEmpty()) {

				for (Object[] object : allJobRoleList) {
					JobRoleDTO jobRoleDTO = new JobRoleDTO();
					jobRoleDTO.setName(object[0] != null ? object[0].toString() : null);
					jobRoleDTO.setEmployeeRole(object[1] != null ? object[1].toString() : null);
					jobRoleDTO.setCreatedByName(object[2] != null ? object[2].toString() : null);
					jobRoleDTO.setCreatedOn(object[3] != null ? object[3].toString() : null);
					jobRoleDTO.setDepartmentId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					jobRoleDTO.setDepartmentName(object[5] != null ? object[5].toString() : null);
					jobRoleDTO.setJobRoleId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					jobRoleDTO.setUpdatedByName(object[7] != null ? object[7].toString() : null);
					jobRoleDTO.setUpdatedOn(object[8] != null ? object[8].toString() : null);
					jobRoleDTO.setCreatedBy(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					jobRoleDTO.setUpdatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : 0);
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

//	@Transactional
//	public ServiceResponse updateJobRole(JobRoleDTO jobRoleDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("Update Role");
//		apiLogInfo.setApiUrl("/api/updateJobRole");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("jobRoleId : " +jobRoleDTO.getJobRoleId()+ "updatedBy : " + jobRoleDTO.getUpdatedBy() + "name : " +jobRoleDTO.getName()+ "employeeRole :" +jobRoleDTO.getEmployeeRole()+ "departmentId : " +jobRoleDTO.getDepartmentId());
//		try {
//			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getJobRoleId());
//			if (jobRoleObject.isPresent()) {
//				JobRole jobRoleToBeUpdated = jobRoleObject.get();
//
//				if (!jobRoleDTO.getEmployeeRole().equals(jobRoleToBeUpdated.getEmployeeRole())) {
//
//					roleFeatureMapRepository.deleteByJobRoleId(jobRoleDTO.getJobRoleId());
//
//					List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();
//
//					List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
//							.findByEmployeeRoleAndPermission(jobRoleDTO.getEmployeeRole(), "Y");
//					defaultSubFeatureList.forEach(dto -> {
//
//						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
//						roleFeatureMap.setJobRoleId(jobRoleDTO.getJobRoleId());
//						roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureMasterId());
//						roleFeatureMapList.add(roleFeatureMap);
//
//					});
//
//					roleFeatureMapRepository.saveAll(roleFeatureMapList);
//
//				}
//
//				jobRoleToBeUpdated.setUpdatedBy(jobRoleDTO.getUpdatedBy());
//				jobRoleToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				// jobRoleToBeUpdated.setName(jobRoleDTO.getName() + "-" +
//				// jobRoleDTO.getEmployeeRole());
//				jobRoleToBeUpdated.setName(jobRoleDTO.getName());
//				jobRoleToBeUpdated.setEmployeeRole(jobRoleDTO.getEmployeeRole());
//				jobRoleToBeUpdated.setDeptId(jobRoleDTO.getDepartmentId());
//				JobRole dbResponse = jobRoleRepository.save(jobRoleToBeUpdated);
//
//				if (dbResponse != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Job Role Updated.");
//					
//					apiLogInfo.setApiResponse("Job Role Updated");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Job Role Updation Failed.");
//					
//					apiLogInfo.setApiResponse("Job Role Updation Failed");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Job Role Not Found");
//				
//				apiLogInfo.setApiResponse("Job Role Not Found");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateJobRole(JobRoleDTO jobRoleDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Update Role");
	    apiLogInfo.setApiUrl("/api/updateJobRole");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("jobRoleId : ").append(jobRoleDTO.getJobRoleId())
	              .append(", updatedBy : ").append(jobRoleDTO.getUpdatedBy())
	              .append(", name : ").append(jobRoleDTO.getName())
	              .append(", employeeRole : ").append(jobRoleDTO.getEmployeeRole())
	              .append(", departmentId : ").append(jobRoleDTO.getDepartmentId());

	    try {
	        // --- Null or Invalid Input Checks ---
	        if (jobRoleDTO == null || jobRoleDTO.getJobRoleId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid JobRole data provided.");
	            
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Invalid JobRoleDTO input.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getJobRoleId());
	        if (jobRoleObject.isPresent()) {
	            JobRole jobRoleToBeUpdated = jobRoleObject.get();

	            // --- Check for EmployeeRole change ---
	            if (jobRoleDTO.getEmployeeRole() != null && 
	                !jobRoleDTO.getEmployeeRole().equals(jobRoleToBeUpdated.getEmployeeRole())) {

	                // Delete old mappings safely
	                roleFeatureMapRepository.deleteByJobRoleId(jobRoleDTO.getJobRoleId());

	                List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
	                        .findByEmployeeRoleAndPermission(jobRoleDTO.getEmployeeRole(), "Y");

	                // --- Null/Empty check before mapping ---
	                if (defaultSubFeatureList != null && !defaultSubFeatureList.isEmpty()) {
	                    List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();
	                    for (EmployeeRole dto : defaultSubFeatureList) {
	                        if (dto != null && dto.getSubFeatureMasterId() != null) {
	                            RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
	                            roleFeatureMap.setJobRoleId(jobRoleDTO.getJobRoleId());
	                            roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureMasterId());
	                            roleFeatureMapList.add(roleFeatureMap);
	                        }
	                    }

	                    if (!roleFeatureMapList.isEmpty()) {
	                        roleFeatureMapRepository.saveAll(roleFeatureMapList);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("SubFeature list was empty. No RoleFeature mappings created.");
	                        
	                        apiLogInfo.setApiResponse("SubFeature list was empty. No RoleFeature mappings created.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                    }
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("No default subfeatures found for selected EmployeeRole.");
	                    
	                    apiLogInfo.setApiResponse("No default subfeatures found for selected EmployeeRole.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                }
	            }

	            // --- Update JobRole fields ---
	            jobRoleToBeUpdated.setUpdatedBy(jobRoleDTO.getUpdatedBy());
	            jobRoleToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
	            jobRoleToBeUpdated.setName(jobRoleDTO.getName());
	            jobRoleToBeUpdated.setEmployeeRole(jobRoleDTO.getEmployeeRole());
	            jobRoleToBeUpdated.setDeptId(jobRoleDTO.getDepartmentId());

	            JobRole dbResponse = jobRoleRepository.save(jobRoleToBeUpdated);

	            if (dbResponse != null) {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Job Role Updated.");

	                apiLogInfo.setApiResponse("Job Role Updated");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Job Role Updation Failed.");

	                apiLogInfo.setApiResponse("Job Role Updation Failed");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            }

	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Job Role Not Found.");

	            apiLogInfo.setApiResponse("Job Role Not Found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (DataIntegrityViolationException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Duplicate or invalid data found during update.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("DataIntegrityViolationException: " + e.getMessage());

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteJobRole(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Role");
		apiLogInfo.setApiUrl("/api/deleteJobRole");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("jobRoleId : " + jobRoleDTO.getJobRoleId());
		try {
			boolean isJobRoleUsedInPoPortal = false;
			Optional<JobRole> jobRoleObject = jobRoleRepository.findById(jobRoleDTO.getJobRoleId());
			if (jobRoleObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found.");
				apiLogInfo.setApiResponse("Job Role Not Found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			JobRole jobRoleToBeDeleted = jobRoleObject.get();
			Long count = employeeRepository.countByJobRoleId(jobRoleToBeDeleted.getJobRoleId());
			ServiceResponse syncResponse = poPortalAPIService
					.isJobRoleUsedInPoPortal(jobRoleToBeDeleted.getJobRoleId());
			if (syncResponse != null && syncResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				String deptarmentUsedFlag = syncResponse.getServiceResponse().toString(); // 0=Not Present, 1=Present but not used, 2=Used
				if (deptarmentUsedFlag.equals("0")) {
					isJobRoleUsedInPoPortal = false;
				}
				if (deptarmentUsedFlag.equals("1")) { // JobRole to from poPortal
					poPortalAPIService.deleteJobRole(jobRoleToBeDeleted.getJobRoleId());
				}
				if (deptarmentUsedFlag.equals("2")) {
					isJobRoleUsedInPoPortal = true;
				}
			}

			if (count == 0 && !isJobRoleUsedInPoPortal) {
				jobRoleRepository.deleteById(jobRoleToBeDeleted.getJobRoleId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Job role deleted.");
				apiLogInfo.setApiResponse("Job role deleted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				JobRoleDTO dtoObject = new JobRoleDTO();
				dtoObject.setIsJobRoleUsedInIshine(count != 0 ? "true" : "false");
				dtoObject.setIsJobRoleUsedInPoPortal(isJobRoleUsedInPoPortal ? "true" : "false");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(dtoObject);
				apiLogInfo.setApiResponse(dtoObject + "Job role cannot be deleted as it is mapped to employee.");
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
	public ServiceResponse changeEmployeeJobRoleMapping(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_role");
		apiLogInfo.setApiUrl("/api/changeEmployeeJobRoleMapping");
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiRequest("jobRoleId: " + jobRoleDTO.getOldJobRoleId());

		try {
			List<Employee> employeeList = employeeRepository.findByJobRoleId(jobRoleDTO.getOldJobRoleId());
			if (!employeeList.isEmpty()) {
				updateEmployeesJobRole(employeeList, jobRoleDTO);
			}

			boolean isPoPortalUsed = Boolean.parseBoolean(jobRoleDTO.getIsJobRoleUsedInPoPortal());
			if (isPoPortalUsed) {
				return syncDeleteJobRoleWithPoPortal(jobRoleDTO, response, apiLogInfo);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(employeeList.isEmpty() ? "No Employee found, but Job Role deleted successfully." : "Job Role deleted successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse(response.getServiceResponse().toString());
			}
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}

		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private void updateEmployeesJobRole(List<Employee> employees, JobRoleDTO jobRoleDTO) {
		boolean isUsedInIshine = Boolean.parseBoolean(jobRoleDTO.getIsJobRoleUsedInIshine());
		for (Employee emp : employees) {
			if (isUsedInIshine) {
				emp.setJobRoleId(jobRoleDTO.getJobRoleId());
				employeeRepository.save(emp);
			} else {
				emp.setName("Not used in Ishine");
			}
		}
	}

	private ServiceResponse syncDeleteJobRoleWithPoPortal(JobRoleDTO jobRoleDTO, ServiceResponse response,LogDTO logInfo) {
		ServiceResponse portalResponse = poPortalAPIService.syncDeleteJobRoleWithPoPortal(jobRoleDTO, "changeEmployeeJobRoleMapping");
		if (ServiceResponse.STATUS_SUCCESS.equals(portalResponse.getServiceStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} else {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		response.setServiceResponse(portalResponse.getServiceResponse());
		logInfo.setApiResponse(portalResponse.getServiceResponse().toString());
		logInfo.setApiStatus(response.getServiceStatus());
		return response;
	}

	public ServiceResponse syncDeleteJobRoleWithPoPortal(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			//Sync deleted jobRole with PoPortal
			JobRole jobRoleObject = jobRoleRepository.findByjobRoleId(jobRoleDTO.getJobRoleId());
			
			if(jobRoleObject != null) {
				JobRoleDTO syncObject = new JobRoleDTO();
				
				syncObject.setRoleId(jobRoleObject.getJobRoleId());
				syncObject.setRoleName(jobRoleObject.getName());
				syncObject.setDeptId(jobRoleObject.getDeptId());
				
				try {
					
					final String syncUrl = syncJobRoleApi;
					RestTemplate restTemplate = new RestTemplate();
					String syncResponse = restTemplate.postForObject(syncUrl, syncObject, String.class, jobRoleDTO.getOldJobRoleId());	
					
					JSONObject json = new JSONObject(syncResponse);
					
					if(json.getInt("httpStatusCode") == 200) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Job Role deleted & Synced with PoPortal");
					}
					
				}catch(InternalServerError e) {
					JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
					
				}catch(HttpClientErrorException e) {
                    JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("JobRole not found.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

//	public ServiceResponse addNewSubFeatures(SubFeatureMasterDTO subFeatureMasterDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		//apiLogInfo.setSubFeatureName("Appreciation");
//		apiLogInfo.setApiUrl("/api/addNewSubFeatures");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("employeeRoles : " + subFeatureMasterDTO.getEmployeeRoleList()+ "subFeatureName : " +subFeatureMasterDTO.getSubFeatureName());
//		try {
//
//			List<EmployeeRole> employeeRoles = subFeatureMasterDTO.getEmployeeRoleList();
//
//			SubFeatureMaster subFeatureMaster = subFeatureMasterRepository
//					.findBySubFeatureName(subFeatureMasterDTO.getSubFeatureName());
//
//			if (subFeatureMaster != null) {
//				List<EmployeeRole> employeeRoleList = new ArrayList<>();
//
//				for (EmployeeRole role : employeeRoles) {
//					EmployeeRole employeeRole = new EmployeeRole();
//
//					employeeRole.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
//					employeeRole.setSubFeatureName(subFeatureMasterDTO.getSubFeatureName());
//
//					employeeRole.setEmployeeRole(role.getEmployeeRole());
//					employeeRole.setPermission(role.getPermission());
//
//					employeeRoleList.add(employeeRole);
//				}
//
//				List<EmployeeRole> list = employeeRoleMasterRepository.saveAll(employeeRoleList);
//				if (list.size() > 0) {
//					List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
//							.findBySubFeatureMasterIdAndPermission(subFeatureMaster.getSubFeatureMasterId(), "Y");
//
//					if (defaultSubFeatureList.size() > 0) {
//
//						List<String> employeeRolelist = defaultSubFeatureList.stream().map((employeeRole) -> {
//							return employeeRole.getEmployeeRole();
//						}).collect(Collectors.toList());
//
//						List<Object[]> objectArrayList = roleFeatureMapRepository
//								.getRolesToBeMappedWithNewSubFeature(employeeRolelist);
//
//						Optional.ofNullable(objectArrayList).ifPresentOrElse((objectlist) -> {
//							if (objectlist.isEmpty()) {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Employee role list is empty.");
//								
//								apiLogInfo.setApiResponse("Employee role list is empty");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//								
//							} else {
//								List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
//								for (EmployeeRole role : defaultSubFeatureList) {
//									for (Object[] object : objectlist) {
//										if (object[1].toString().equals(role.getEmployeeRole())) {
//											RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
//											roleFeatureMap
//													.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
//											roleFeatureMap.setJobRoleId(
//													object[0] != null ? Long.parseLong(object[0].toString()) : null);
//											System.out.println("Subfeature mapped to " + role.getEmployeeRole()
//													+ " role. With job role id " + object[0].toString());
//
//											roleFeatureMapList.add(roleFeatureMap);
//										}
//									}
//								}
//
//								List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository
//										.saveAll(roleFeatureMapList);
//
//								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								response.setServiceResponse("Subfeature added to employee_role_master table.Role mappings added to role_subfeature_mapping");
//								
//								apiLogInfo.setApiResponse("Subfeature added to employee_role_master table.Role mappings added to role_subfeature_mapping");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//
//							}
//						}, () -> {
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							response.setServiceResponse("Employee role list is null.");
//							
//							apiLogInfo.setApiResponse("Employee role list is null");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						});
//
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(
//								"Subfeature added to employee_role_master table. But no role mapping done as permission was set to"
//										+ " 'N'.");
//						
//						apiLogInfo.setApiResponse("Subfeature added to employee_role_master table. But no role mapping done as permission was set to"
//								+ " 'N'.");			
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					}
//
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Failed to add subfeature in subfeature_master_table.");
//					
//					apiLogInfo.setApiResponse("Failed to add subfeature in subfeature_master_table.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Subfeature not found in subfeature_master_table.");
//				
//				apiLogInfo.setApiResponse("Subfeature not found in subfeature_master_table.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addNewSubFeatures(SubFeatureMasterDTO subFeatureMasterDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/addNewSubFeatures");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("employeeRoles : ").append(subFeatureMasterDTO != null ? subFeatureMasterDTO.getEmployeeRoleList() : "null")
	              .append(", subFeatureName : ").append(subFeatureMasterDTO != null ? subFeatureMasterDTO.getSubFeatureName() : "null");

	    try {
	        // --- Basic Validation ---
	        if (subFeatureMasterDTO == null || subFeatureMasterDTO.getSubFeatureName() == null || subFeatureMasterDTO.getSubFeatureName().trim().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid subFeatureMasterDTO or SubFeatureName is missing.");
	            
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Invalid input data for subFeatureMasterDTO.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        List<EmployeeRole> employeeRoles = subFeatureMasterDTO.getEmployeeRoleList();
	        if (employeeRoles == null || employeeRoles.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee role list is empty or null.");
	            
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Employee role list is empty or null.");
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Check for SubFeature existence ---
	        SubFeatureMaster subFeatureMaster = subFeatureMasterRepository
	                .findBySubFeatureName(subFeatureMasterDTO.getSubFeatureName().trim());

	        if (subFeatureMaster == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Subfeature not found in subfeature_master_table.");
	            
	            apiLogInfo.setApiResponse("Subfeature not found in subfeature_master_table.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Prepare EmployeeRole List for Insertion ---
	        List<EmployeeRole> employeeRoleList = new ArrayList<>();
	        for (EmployeeRole role : employeeRoles) {
	            if (role != null && role.getEmployeeRole() != null && !role.getEmployeeRole().trim().isEmpty()) {
	                // Check for duplicate employeeRole-subFeatureMasterId mapping
	                EmployeeRole existingRole = employeeRoleMasterRepository
	                        .findBySubFeatureMasterIdAndEmployeeRole(subFeatureMaster.getSubFeatureMasterId(), role.getEmployeeRole().trim());
	                if (existingRole == null) {
	                    EmployeeRole employeeRole = new EmployeeRole();
	                    employeeRole.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
	                    employeeRole.setSubFeatureName(subFeatureMasterDTO.getSubFeatureName().trim());
	                    employeeRole.setEmployeeRole(role.getEmployeeRole().trim());
	                    employeeRole.setPermission(role.getPermission() != null ? role.getPermission().trim() : "N");
	                    employeeRoleList.add(employeeRole);
	                }
	            }
	        }

	        if (employeeRoleList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No new EmployeeRoles to insert — duplicates or invalid entries found.");
	            
	            apiLogInfo.setApiResponse("No new EmployeeRoles to insert — duplicates detected.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Save All EmployeeRoles ---
	        List<EmployeeRole> list = employeeRoleMasterRepository.saveAll(employeeRoleList);
	        if (list == null || list.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Failed to add subfeature in employee_role_master table.");
	            
	            apiLogInfo.setApiResponse("Failed to add subfeature in employee_role_master table.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Find Default SubFeatures with Permission 'Y' ---
	        List<EmployeeRole> defaultSubFeatureList = employeeRoleMasterRepository
	                .findBySubFeatureMasterIdAndPermission(subFeatureMaster.getSubFeatureMasterId(), "Y");

	        if (defaultSubFeatureList == null || defaultSubFeatureList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(
	                    "Subfeature added to employee_role_master table, but no role mapping done as permission was set to 'N'.");
	            
	            apiLogInfo.setApiResponse("Subfeature added but no 'Y' permission found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Collect EmployeeRoles with Permission 'Y' ---
	        List<String> employeeRoleNames = defaultSubFeatureList.stream()
	                .map(EmployeeRole::getEmployeeRole)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());

	        if (employeeRoleNames.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee role list is empty for mapping.");
	            
	            apiLogInfo.setApiResponse("Employee role list empty for mapping.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Fetch roles to be mapped with the new SubFeature ---
	        List<Object[]> objectArrayList = roleFeatureMapRepository
	                .getRolesToBeMappedWithNewSubFeature(employeeRoleNames);

	        if (objectArrayList == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee role list is null.");
	            
	            apiLogInfo.setApiResponse("Employee role list is null.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (objectArrayList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee role list is empty.");
	            
	            apiLogInfo.setApiResponse("Employee role list is empty.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // --- Create RoleFeatureMap entries ---
	        List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();
	        for (EmployeeRole role : defaultSubFeatureList) {
	            for (Object[] object : objectArrayList) {
	                if (object != null && object.length >= 2 && object[1] != null &&
	                    object[1].toString().equals(role.getEmployeeRole())) {

	                    RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
	                    roleFeatureMap.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
	                    roleFeatureMap.setJobRoleId(object[0] != null ? Long.parseLong(object[0].toString()) : null);

	                    roleFeatureMapList.add(roleFeatureMap);
	                }
	            }
	        }

	        if (!roleFeatureMapList.isEmpty()) {
	            roleFeatureMapRepository.saveAll(roleFeatureMapList);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Subfeature added and role mappings updated successfully.");

	            apiLogInfo.setApiResponse("Subfeature added and role mappings updated successfully.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No role-feature mappings were created due to mismatched roles.");
	            
	            apiLogInfo.setApiResponse("No role-feature mappings created.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (DataIntegrityViolationException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Duplicate or invalid data encountered.");
	        response.setServiceError(e.getMessage());
	        
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("DataIntegrityViolationException: " + e.getMessage());

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	        
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse checkJobRole(JobRoleDTO jobRoleDto) {
	
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/checkJobRole");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("name : " + jobRoleDto.getName()+ "departmentId :" +jobRoleDto.getDepartmentId());
		try {
			Integer checkExistingRole = jobRoleRepository.findByNameAndDeptId(jobRoleDto.getName(), jobRoleDto.getDepartmentId());
			
			if(Integer.valueOf(checkExistingRole).equals(0)) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("JobRole created !");
				
				apiLogInfo.setApiResponse("JobRole created !");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else if(checkExistingRole != 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("JobRole already exist!");
					
					apiLogInfo.setApiResponse("JobRole already exist!");			
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

//	@Transactional
//	public ServiceResponse updateJobRoleSubFeatureMapping(JobRoleDTO jobRoleDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("update JobRole SubFeature");
//		apiLogInfo.setApiUrl("api/updateJobRoleSubFeatureMapping");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("JobroleId :" + jobRoleDTO.getJobRoleId());
//		try {
//			
//			List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();
//
//			jobRoleDTO.getUpdatedJobRoleFeatureMapping().forEach(dto -> {
//				
//				if(dto.getIsAssigned().equals("true")) {
//					RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
//					roleFeatureMap.setJobRoleId(dto.getJobRoleId());
//					roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureId());
//					roleFeatureMapList.add(roleFeatureMap);
//				}else if(dto.getIsAssigned().equals("false")) {
//					RoleFeatureMap mappingToBeDeleted = roleFeatureMapRepository
//							.findByJobRoleIdAndSubFeatureMasterId(dto.getJobRoleId(), dto.getSubFeatureId());
//					if(mappingToBeDeleted != null) {
//					    roleFeatureMapRepository.deleteById(mappingToBeDeleted.getRoleFeatureMapId());
//					}
//				}
//			});
//
//			roleFeatureMapRepository.saveAll(roleFeatureMapList);
//
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse("Role ACL Updated");
//			
//			apiLogInfo.setApiResponse("Role ACL updated!");
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//
//        apiLogInfo.setApiRequest(logBuilder.toString());
//        logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateJobRoleSubFeatureMapping(JobRoleDTO jobRoleDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("update JobRole SubFeature");
	    apiLogInfo.setApiUrl("/api/updateJobRoleSubFeatureMapping");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("JobroleId : ").append(jobRoleDTO.getJobRoleId());

	    try {
	        // Validation: Check if DTO or mapping list is null/empty
	        if (jobRoleDTO == null || jobRoleDTO.getUpdatedJobRoleFeatureMapping() == null 
	                || jobRoleDTO.getUpdatedJobRoleFeatureMapping().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid or empty job role feature mapping data.");
	            apiLogInfo.setApiResponse("Invalid or empty job role feature mapping data.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();

	        // Process each DTO for add/delete
	        jobRoleDTO.getUpdatedJobRoleFeatureMapping().forEach(dto -> {
	            if (dto != null && dto.getJobRoleId() != null && dto.getSubFeatureId() != null) {

	                if ("true".equalsIgnoreCase(dto.getIsAssigned())) {
	                    // Check duplicate before adding
	                    RoleFeatureMap existingMap = roleFeatureMapRepository
	                            .findByJobRoleIdAndSubFeatureMasterId(dto.getJobRoleId(), dto.getSubFeatureId());

	                    if (existingMap == null) {
	                        RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
	                        roleFeatureMap.setJobRoleId(dto.getJobRoleId());
	                        roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureId());
	                        roleFeatureMapList.add(roleFeatureMap);
	                    }
	                } else if ("false".equalsIgnoreCase(dto.getIsAssigned())) {
	                    // Delete existing mapping if exists
	                    RoleFeatureMap mappingToBeDeleted = roleFeatureMapRepository
	                            .findByJobRoleIdAndSubFeatureMasterId(dto.getJobRoleId(), dto.getSubFeatureId());
	                    if (mappingToBeDeleted != null) {
	                        roleFeatureMapRepository.deleteById(mappingToBeDeleted.getRoleFeatureMapId());
	                    }
	                }
	            }
	        });

	        // Save only if non-empty
	        if (!roleFeatureMapList.isEmpty()) {
	            roleFeatureMapRepository.saveAll(roleFeatureMapList);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Role ACL Updated");

	        apiLogInfo.setApiResponse("Role ACL updated!");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception occurred while updating JobRole SubFeature Mapping: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);

	    return response;
	}


	public ServiceResponse getAllJobRoleInfo() {
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(
					poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"getAllJobRoleInfo",
					"PoPortal",
					null,
					httpRequest);

			List<JobRole> roles = jobRoleRepository.findAll();
			if (roles.isEmpty()) {
				applyJobRoleInfoNotFoundResponse(response);
				finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
			} else {
				List<PoPortalDTO> dtoList = mapJobRolesToPoPortalDtos(roles);
				applyJobRoleInfoSuccessResponse(response, dtoList);
				finalHttpStatusCode = HttpStatus.OK.value();
				log.debug("getAllJobRoleInfo returned {} roles", dtoList.size());
			}
		} catch (Exception e) {
			log.error("getAllJobRoleInfo failed: {}", e.getMessage(), e);
			applyJobRoleInfoSystemErrorResponse(response, e);
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}
		return response;
	}

	private static String buildRequestPathForLogging(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		return (query != null && !query.isEmpty()) ? uri + "?" + query : uri;
	}

	private List<PoPortalDTO> mapJobRolesToPoPortalDtos(List<JobRole> roles) {
		return roles.stream()
				.filter(Objects::nonNull)
				.map(this::toPoPortalJobRoleDto)
				.collect(Collectors.toList());
	}

	private PoPortalDTO toPoPortalJobRoleDto(JobRole role) {
		PoPortalDTO dto = new PoPortalDTO();
		dto.setRoleId(role.getJobRoleId());
		dto.setDeptId(role.getDeptId());
		dto.setRoleName(role.getName());
		return dto;
	}

	private static void applyJobRoleInfoSuccessResponse(ServiceResponse response, List<PoPortalDTO> dtoList) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dtoList);
	}

	private static void applyJobRoleInfoNotFoundResponse(ServiceResponse response) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("JobRole Info not found.");
	}

	private static void applyJobRoleInfoSystemErrorResponse(ServiceResponse response, Exception e) {
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		response.setServiceError(e.getMessage());
	}

}
