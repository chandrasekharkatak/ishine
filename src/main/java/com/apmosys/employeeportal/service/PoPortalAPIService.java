package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.response.ResourceRequirementResponse;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PoPortalAPIService {
	
	@Value("${poPortal.api.getFCLineItemDetails}")
	private String getFCLineItemDetailsURL;
	
	@Value("${poPortal.api.updateMilestones}")
	private String sendFileUrl;
	
	@Value("${poPortal.api.allProjects}")
	private String allPoPortalProjects;
	
	@Value("${poPortal.api.getProjectById}")
	private String poPortalProjectByIdURL;
	
	@Value("${poPortal.api.getcount}")
	private String poPortalCountById;
	
	@Value("${poPortal.api.syncDepartment}")
	private String syncDepartmentWithPoPortal;
	
	@Value("${poPortal.api.isDepartmentUsed}")
	private String isDeparmentUsedInPoPortal;
	
	@Value("${poPortal.api.deleteDepartment}")
	private String deleteDeparmentFromPoPortal;
	
	@Value("${poPortal.api.syncJobRole}")
	private String syncJobRoleWithPoPortal;
	
	@Value("${poPortal.api.isJobRoleUsed}")
	private String isJobRoleUsedInPoPortal;
	
	@Value("${poPortal.api.deleteJobRole}")
	private String deleteJobRoleFromPoPortal;
	
	@Value("${poPortal.api.syncProject}")
	private String reversesyncurl;
	
	@Value("${poPortal.api.getMilestoneDoc}")
	private String getDocumentUrl;
	
	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@Autowired
	private ApiLogUtility apiLogUtility;

	@Autowired
	private UserSessionRepository userSessionRepo;
	
	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	public ServiceResponse callGetFCLineItemDetails(Long poProjectId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		initialLog = apiLogUtility.startLog(traceId, "getFcLineItemDetails", "Ishine", getCurrentUserId(),httpRequest);

		if (initialLog == null || initialLog.getId() == null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
			return serviceResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", traceId);
		headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String url = getFCLineItemDetailsURL + poProjectId;
		ResponseEntity<List<FCLineItemDTO>> apiResponse = null;
		try {
 	        apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity,new ParameterizedTypeReference<List<FCLineItemDTO>>() {});
			if (apiResponse.getStatusCode() == HttpStatus.OK) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(apiResponse.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Error fetching Milestones for Project.");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Error fetching Milestones for Project.");
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
			apiLogUtility.endLog(initialLog.getId(),url ,finalHttpStatusCode, finalLogDetails, httpRequest);
		}
		return serviceResponse;
	}
	
	
	public ServiceResponse updateMilestoneById(FCProjectMilestoneDTO dto, MultipartFile file) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			if (dto == null || dto.getId() == null) {
				String msg = "Milestone DTO and ID cannot be null.";
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse(msg);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				exceptionDetailsForLog = msg;
				return serviceResponse;
			}

			if (file == null || file.isEmpty()) {
				String msg = "Milestone document file is required for an update.";
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse(msg);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				exceptionDetailsForLog = msg;
				return serviceResponse;
			}

			List<String> allowedContentTypes = Arrays.asList("application/pdf", "image/jpeg", "image/png");
			if (!allowedContentTypes.contains(file.getContentType())) {
				String msg = "Invalid file type. Only PDF, JPG, JPEG, or PNG files are allowed.";
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse(msg);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				exceptionDetailsForLog = msg;
				return serviceResponse;
			}
			
			initialLog = apiLogUtility.startLog(traceId, "updateMilestoneById", "Ishine", getCurrentUserId(),httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the update process.");
				return serviceResponse;
			}
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("X-Trace-Id", traceId);
//			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());	
//			headers.setContentType(MediaType.valueOf(file.getContentType()));
//			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getOriginalFilename()).build());
//			HttpEntity<FCProjectMilestoneDTO> dtoEntity = new HttpEntity<>(dto, headers);
//		    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//		    body.add("milestoneData", dtoEntity);
//		    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes());
//	        body.add("File",fileResource); 
//	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//			ResponseEntity<String> apiResponse = restTemplate.exchange(sendFileUrl, HttpMethod.PUT, requestEntity, String.class);

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
			headers.setContentType(MediaType.MULTIPART_FORM_DATA); 

			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<FCProjectMilestoneDTO> jsonPart = new HttpEntity<>(dto, jsonHeaders);

			
			ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
			    @Override
			    public String getFilename() {
			        return file.getOriginalFilename();
			    }
			};

			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("dto", jsonPart);
			body.add("file", fileResource);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> apiResponse = restTemplate.exchange(
			    sendFileUrl, 
			    HttpMethod.PUT, 
			    requestEntity, 
			    String.class
			);
			if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
				serviceResponse.setServiceResponse(apiResponse.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Failed to update milestone via external service. Status: " + apiResponse.getStatusCode());
				finalHttpStatusCode = apiResponse.getStatusCodeValue();
				exceptionDetailsForLog = "External API returned non-OK status: " + apiResponse.getStatusCode();
			}
		} catch (Exception e) {
			String errorMsg = "An unexpected error occurred during the milestone update process.";
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(errorMsg);
			serviceResponse.setServiceError(e.getMessage());
			exceptionDetailsForLog = e.toString();
			e.printStackTrace();
		} finally {
			if (initialLog != null && initialLog.getId() != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(),sendFileUrl ,finalHttpStatusCode, finalLogDetails, httpRequest);
			}
		}
		return serviceResponse;
	}
	
	
	public ServiceResponse getAllProjectsFromPoPortal() {
		ServiceResponse serviceResponse = new ServiceResponse();
		List<ProjectPoPortalDTO> poPortalprojectList = new ArrayList<>();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			initialLog = apiLogUtility.startLog(traceId, "getProjectCloneFromPoPortal", "Ishine", getCurrentUserId(), httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
				return serviceResponse;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<ProjectPoPortalDTO[]> responseEntity = restTemplate.exchange(allPoPortalProjects, HttpMethod.GET, entity, ProjectPoPortalDTO[].class);
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				finalHttpStatusCode = HttpStatus.OK.value();
			}
			ProjectPoPortalDTO[] projects = responseEntity.getBody();
			poPortalprojectList = Arrays.asList(projects != null ? projects : new ProjectPoPortalDTO[0]);
			serviceResponse.setServiceResponse(poPortalprojectList);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Error fetching projects: " + e.getMessage());
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), allPoPortalProjects,finalHttpStatusCode, finalLogDetails, httpRequest);
			}
		}
		return serviceResponse;
	}
	
	public ServiceResponse fetchPoPortalProjectById(Long projectId) {
        ServiceResponse serviceResponse = new ServiceResponse();
        ApiLog initialLog = null;
        String traceId = UUID.randomUUID().toString();
        int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String exceptionDetailsForLog = null;
        ResponseEntity<List<ResourceRequirementResponse>> apiResponse = null;
        try {
        	
        	String url;
        	initialLog = apiLogUtility.startLog(traceId, "getAllResourceRequirementForProject", "Ishine", getCurrentUserId(), httpRequest);
	        if (initialLog == null || initialLog.getId() == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the API call.");
	            return serviceResponse;
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<?> entity = new HttpEntity<>(headers);
	        url = poPortalProjectByIdURL + projectId;

			apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ResourceRequirementResponse>>() {});
            finalHttpStatusCode = apiResponse.getStatusCodeValue();
            
            if (apiResponse.getStatusCode() == HttpStatus.OK) {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                serviceResponse.setServiceResponse(apiResponse.getBody());
            } else {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                serviceResponse.setServiceResponse("Error fetching project details from PO Portal. Status: " + apiResponse.getStatusCode());
            }
        } catch (Exception e) {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceResponse("Failed to communicate with PO Portal to fetch project details.");
            exceptionDetailsForLog = e.toString();
            e.printStackTrace();
        } finally {
        	if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(),poPortalProjectByIdURL,finalHttpStatusCode, finalLogDetails, httpRequest);
			}
        }
        return serviceResponse;
    }
	
	public ServiceResponse getCountByProjectId(Long projectId) {
        ServiceResponse serviceResponse = new ServiceResponse();
        ApiLog initialLog = null;
        String traceId = UUID.randomUUID().toString();
        int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String exceptionDetailsForLog = null;
        ResponseEntity<Long> apiResponse = null;
        try {
        	initialLog = apiLogUtility.startLog(traceId, "getResourceRequirementCountByProjectId", "Ishine", getCurrentUserId(), httpRequest);
	        if (initialLog == null || initialLog.getId() == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the API call.");
	            return serviceResponse;
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<?> entity = new HttpEntity<>(headers);
	        String url = poPortalCountById + projectId;
            apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, Long.class);
            finalHttpStatusCode = apiResponse.getStatusCodeValue();

            if (apiResponse.getStatusCode() == HttpStatus.OK) {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                serviceResponse.setServiceResponse(apiResponse.getBody());
            } else {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                serviceResponse.setServiceResponse("Error fetching project count details from PO Portal. Status: " + apiResponse.getStatusCode());
            }
        } catch (Exception e) {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceResponse("Failed to communicate with PO Portal to fetch project count details.");
            exceptionDetailsForLog = e.toString();
        } finally {
        	if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(),poPortalCountById ,finalHttpStatusCode, finalLogDetails, httpRequest);
			}
        }
        return serviceResponse;
    }
	
	
	
	
	
	public ServiceResponse syncDeleteDepartmentWithPoPortal(DepartmentDTO departmentDTO,String endPointName) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			initialLog = apiLogUtility.startLog(traceId, endPointName, "Ishine", getCurrentUserId(), httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
				return serviceResponse;
			}
			// Sync Deleted Dept with PoPortal
			Department deptObj = departmentRepository.findByDeptId(departmentDTO.getDeptId());
			if (deptObj != null) {
				Employee empObj = employeeRepository.findByEmpId(deptObj.getHodId());
				DepartmentDTO syncObject = new DepartmentDTO();
				syncObject.setDeptId(deptObj.getDeptId());
				syncObject.setDeptName(deptObj.getName());
				syncObject.setHodEmploymentId("A-".concat(empObj.getEmployeementId().toString()));
				
				final String syncUrl = syncDepartmentWithPoPortal;
				HttpHeaders headers = new HttpHeaders();
				headers.set("X-Trace-Id", traceId);
				headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<Object> requestEntity = new HttpEntity<>(syncObject, headers);
				ResponseEntity<String> responseEntity = restTemplate.exchange(syncUrl,HttpMethod.POST,requestEntity,String.class,departmentDTO.getOldDeptId());
				String syncResponse = responseEntity.getBody();
				finalHttpStatusCode = responseEntity.getStatusCode().value();
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse("Department Deleted & Synced with PoPortal");
				}				
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Department not found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			serviceResponse.setServiceError(e.getMessage());
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(),syncDepartmentWithPoPortal, finalHttpStatusCode, finalLogDetails, httpRequest);
			}
		}
		return serviceResponse;
	}
	
	public ServiceResponse isDepartmentUsedInPoPortal(Long departmentId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		initialLog = apiLogUtility.startLog(traceId, "deleteDepartment", "Ishine", getCurrentUserId(),httpRequest);

		if (initialLog == null || initialLog.getId() == null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
			return serviceResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", traceId);
		headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String url = isDeparmentUsedInPoPortal + departmentId;
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(response.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("An error occurred while verifying department usage in PoPortal.");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("An error occurred while verifying department usage in PoPortal.");
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
			apiLogUtility.endLog(initialLog.getId(), isDeparmentUsedInPoPortal,finalHttpStatusCode, finalLogDetails, httpRequest);
		}
		return serviceResponse;
	}
	
	public void deleteDepartment(Long departmentId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		initialLog = apiLogUtility.startLog(traceId, "deleteDepartment", "Ishine", getCurrentUserId(), httpRequest);

		if (initialLog == null || initialLog.getId() == null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
			return;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", traceId);
		headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String url = deleteDeparmentFromPoPortal + departmentId;
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(response.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("An error occurred while deleting department from PoPortal.");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("An error occurred while deleting department from PoPortal.");
			exceptionDetailsForLog = e.toString();
		} finally {
			String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
			apiLogUtility.endLog(initialLog.getId(), deleteDeparmentFromPoPortal,finalHttpStatusCode, finalLogDetails, httpRequest);
		}
	}
	
	public ServiceResponse syncDeleteJobRoleWithPoPortal(JobRoleDTO jobRoleDTO,String endPointName) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			initialLog = apiLogUtility.startLog(traceId, endPointName, "Ishine", getCurrentUserId(), httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
				return serviceResponse;
			}
			// Sync Deleted JobRole with PoPortal
			JobRole jobRoleObject = jobRoleRepository.findByjobRoleId(jobRoleDTO.getJobRoleId());
			if(jobRoleObject != null) {
				JobRoleDTO syncObject = new JobRoleDTO();
				syncObject.setRoleId(jobRoleObject.getJobRoleId());
				syncObject.setRoleName(jobRoleObject.getName());
				syncObject.setDeptId(jobRoleObject.getDeptId());
				
				final String syncUrl = syncJobRoleWithPoPortal;
				HttpHeaders headers = new HttpHeaders();
				headers.set("X-Trace-Id", traceId);
				headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<Object> requestEntity = new HttpEntity<>(syncObject, headers);
				ResponseEntity<String> responseEntity = restTemplate.exchange(syncUrl,HttpMethod.POST,requestEntity,String.class,jobRoleDTO.getOldJobRoleId());
				
				String syncResponse = responseEntity.getBody();
				finalHttpStatusCode = responseEntity.getStatusCode().value();
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse("Job Role deleted & Synced with PoPortal.");
				}				
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("JobRole not found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			serviceResponse.setServiceError(e.getMessage());
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), syncJobRoleWithPoPortal,finalHttpStatusCode, finalLogDetails, httpRequest);
			}
		}
		return serviceResponse;
	}
	
	public ServiceResponse isJobRoleUsedInPoPortal(Long jobRoleId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		initialLog = apiLogUtility.startLog(traceId, "deleteJobRole", "Ishine", getCurrentUserId(),httpRequest);

		if (initialLog == null || initialLog.getId() == null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
			return serviceResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", traceId);
		headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String url = isJobRoleUsedInPoPortal + jobRoleId;
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(response.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("An error occurred while verifying JobRole usage in PoPortal.");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("An error occurred while verifying JobRole usage in PoPortal.");
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
			apiLogUtility.endLog(initialLog.getId(),isJobRoleUsedInPoPortal ,finalHttpStatusCode, finalLogDetails, httpRequest);
		}
		return serviceResponse;
	}

	public void deleteJobRole(Long jobRoleId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		initialLog = apiLogUtility.startLog(traceId, "deleteJobRole", "Ishine", getCurrentUserId(), httpRequest);

		if (initialLog == null || initialLog.getId() == null) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
			return;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Trace-Id", traceId);
		headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(headers);
		String url = deleteJobRoleFromPoPortal + jobRoleId;
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(response.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("An error occurred while deleting JobRole from PoPortal.");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("An error occurred while deleting JobRole from PoPortal.");
			exceptionDetailsForLog = e.toString();
		} finally {
			String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
			apiLogUtility.endLog(initialLog.getId(),deleteJobRoleFromPoPortal ,finalHttpStatusCode, finalLogDetails, httpRequest);
		}
	}
	
	public ServiceResponse syncProjectData(List<PoProjectSyncDTO> projectInfo) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    ApiLog initialLog = null;
	    String traceId = UUID.randomUUID().toString();
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String exceptionDetailsForLog = null;

	    try {
	        initialLog = apiLogUtility.startLog(traceId, "syncProjectData", "PoPortal", getCurrentUserId(), httpRequest);
	        if (initialLog == null || initialLog.getId() == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the sync process.");
	            return serviceResponse;
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        headers.setContentType(MediaType.APPLICATION_JSON); 
	        HttpEntity<List<PoProjectSyncDTO>> entity = new HttpEntity<>(projectInfo, headers);
	        final String syncUrl = reversesyncurl; 
	        ResponseEntity<String> responseEntity = restTemplate.postForEntity(syncUrl, entity, String.class);

	        finalHttpStatusCode = responseEntity.getStatusCodeValue();
	        
	        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//	            JSONObject json = new JSONObject(responseEntity.getBody());
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(responseEntity.getBody());
				finalHttpStatusCode = HttpStatus.OK.value();
	        } else {
	            // Handle other non-error success codes if necessary
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Received non-OK status: " + responseEntity.getStatusCode());
	        }

	    } catch (Exception e) {
	        exceptionDetailsForLog = e.toString();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceResponse("An unexpected error occurred during sync.");
	        serviceResponse.setServiceError(e.getMessage());
	    } finally {
	        if (initialLog != null) {
	            apiLogUtility.endLog(initialLog.getId(),reversesyncurl ,finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
	        }
	    }
	    return serviceResponse;
	}

	
	private Long getCurrentUserId() {
		String sessionToken = httpRequest.getHeader("Authorization");
		if (sessionToken != null) {
			sessionToken = sessionToken.substring(7);
		}
		UserSession existingUserSession = userSessionRepo.findBySessionKey(sessionToken);
		return existingUserSession != null ? existingUserSession.getEmpId() : null;
	}	
	
	public FCProjectMilestoneDTO getMilestoneDocumentFromExternalApi(Long milestoneId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    ApiLog initialLog = null;
	    String traceId = UUID.randomUUID().toString();
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String exceptionDetailsForLog = null;

	    try {
	        if (milestoneId == null) {
	            throw new IllegalArgumentException("Milestone ID cannot be null.");
	        }

	        initialLog = apiLogUtility.startLog(traceId, "getMilestoneDocument", "Ishine", getCurrentUserId(), httpRequest);
	
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

	       
	        String fullUrl = getDocumentUrl + milestoneId; 
	        
	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<FCProjectMilestoneDTO> apiResponse = restTemplate.exchange(
	            fullUrl,
	            HttpMethod.GET,
	            requestEntity,
	            FCProjectMilestoneDTO.class 
	        );

	        if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
	            finalHttpStatusCode = HttpStatus.OK.value();
	            return apiResponse.getBody();
	        } else {
	            exceptionDetailsForLog = "External API returned non-OK status: " + apiResponse.getStatusCode();
	            finalHttpStatusCode = apiResponse.getStatusCodeValue();
	            throw new HttpClientErrorException(apiResponse.getStatusCode(), "Failed to retrieve document from external service.");
	        }

	    } catch (Exception e) {
	        exceptionDetailsForLog = e.toString();
	        e.printStackTrace();
	        throw new RuntimeException("An unexpected error occurred.", e);
	    } finally {
	        if (initialLog != null && initialLog.getId() != null) {
	            apiLogUtility.endLog(initialLog.getId(),getDocumentUrl ,finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
	        }
	    }
	}
	
}
