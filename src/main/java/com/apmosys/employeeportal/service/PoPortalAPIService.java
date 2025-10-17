package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.MilestoneExpireDto;
import com.apmosys.employeeportal.dto.MilestoneUpdatedLogDto;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectPoDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ProjectWiseMilestoneDto;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.MilestoneExtensionReason;
import com.apmosys.employeeportal.model.MilestoneUpdatedLog;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.MilestoneExtensionReasonRepository;
import com.apmosys.employeeportal.repository.MilestoneUpdatedLogRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.response.ResourceRequirementResponse;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.PoportalApiException;
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
	
    @Value("${poPortal.api.milestoneExpiry}")
	 private String getExpiryMilestoneUrl;
    
    @Value("${poPortal.api.updateMilestoneExtendedDate}")
    private String updateMilestoneEndDateExternalUrl;
	
	@Value("${poPortal.api.getAllPoByProjectId}")
    private String getAllPoByProjectIdUrl;
	
	@Value("${poPortal.api.getAllProjectNameByPoNoLike}")
    private String getAllProjectNameByPoNoLikeUrl;

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
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	private MilestoneUpdatedLogRepository milestoneUpdatedLogRepository;

	@Autowired
	private MilestoneExtensionReasonRepository milestoneExtensionReasonRepository;
	
	@Autowired
	private LogService logService;
	
	
	private static final Logger logger = LoggerFactory.getLogger(PoPortalAPIService.class);

	
	
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

			if (dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("COMPLETED")) {
			    if (file == null || file.isEmpty()) {
			        String msg = "Milestone document file is required when marking milestone as COMPLETED.";
			        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        serviceResponse.setServiceResponse(msg);
			        finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			        exceptionDetailsForLog = msg;
			        return serviceResponse;
			    }
			}
			
			if (file != null && !file.isEmpty()) {
			    List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png");
			    if (!allowedContentTypes.contains(file.getContentType())) {
			        String msg = "Invalid file type. Only JPG, JPEG, or PNG files are allowed.";
			        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        serviceResponse.setServiceResponse(msg);
			        finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			        exceptionDetailsForLog = msg;
			        return serviceResponse;
			    }
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

			
		

			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("dto", jsonPart);

			// Only add file if present
			if (file != null && !file.isEmpty()) {
			    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
			        @Override
			        public String getFilename() {
			            return file.getOriginalFilename();
			        }
			    };
			    body.add("file", fileResource);
			}

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
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
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
	
	
	
	
	
	
	
	
	
		@Async
		@Scheduled(cron = "${milestoneExpiryNotifier.time}")
		public void milestoneExpiryNotifierMail() {
		    System.out.println("======= Project Expiry Job Started =======");
	
		    String traceId = UUID.randomUUID().toString();
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    headers.set("X-Trace-Id", traceId);
		    headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	
		    HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
	
		    ApiLog initialLog = null;
		    int httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		    String exceptionDetails = null;
	
		    try {
		        
		        initialLog = apiLogUtility.startLog(traceId, "/api/milestoneExpiryNotifierMail", "EmployeePortal", 0L, null);
	
		        MilestoneExpireDto[] milestoneArray = restTemplate.exchange(
		            getExpiryMilestoneUrl,
		            HttpMethod.POST,
		            requestEntity,
		            MilestoneExpireDto[].class
		        ).getBody();
	
		        List<MilestoneExpireDto> milestones = Arrays.asList(
		            milestoneArray != null ? milestoneArray : new MilestoneExpireDto[0]
		        );
	
		        System.out.println("Fetched " + milestones.size() + " expiring milestones.");
		        
		        SimpleDateFormat sdf = new SimpleDateFormat("d/MM/yyyy");
	
		        for (MilestoneExpireDto milestone : milestones) {
		            String poNumber = milestone.getPoNo();
		            String projectName = milestone.getProjectName();
		            String milestoneName = milestone.getName();
		            String lineItemName = milestone.getLineItemName();
		            Long projectId = milestone.getProjectId();
		            Date milestoneStartDate = milestone.getStartDate();
		            Date milestoneEndDate = milestone.getEndDate();
		            String milestoneStatus = milestone.getStatus();
		            
		            String formattedStartDate = sdf.format(milestoneStartDate);
		            String formattedEndDate = sdf.format(milestoneEndDate);
		            
		            
		    
		            
		            
		            
		            
	                 
		            Optional<RmAndHodEmailDto> optionalEmails = getRmAndHodEmails(projectId);

		            if (!optionalEmails.isPresent()) {
		                System.out.println("Skipping milestone due to missing RM/HOD data: " + milestoneName);
		                continue;
		            }

		            RmAndHodEmailDto emailDto = optionalEmails.get();
		            List<String> rmEmails = emailDto.getRmEmails();
		            List<String> hodEmails = emailDto.getHodEmails();
		            List<String> directorEmails = projectRepository.findDirectorEmails();

		            List<String> toRecipients = new ArrayList<>();
		            if (rmEmails != null) {
		                toRecipients.addAll(rmEmails.stream().filter(e -> e != null && !e.isEmpty()).collect(Collectors.toList()));
		            }
		            if (hodEmails != null) {
		                toRecipients.addAll(hodEmails.stream().filter(e -> e != null && !e.isEmpty()).collect(Collectors.toList()));
		            }

		            List<String> ccRecipients = directorEmails.stream()
		                    .filter(e -> e != null && !e.trim().isEmpty())
		                    .distinct()
		                    .collect(Collectors.toList());

		            if (toRecipients.isEmpty()) {
		                System.out.println("Skipping milestone due to all emails being empty: " + milestoneName);
		                continue;
		            }

		            String subject = "Project Milestone Expiry Notification: " + projectName;
		            String body = "<html><body>"
		                + "<p>Dear Team,</p>"
		                + "<p>Action Required: The following project milestone expires in 5 days.</p>"
		                + "<table border='1' style='border-collapse: collapse;'>"
		                + "<tr><th>PO Number</th><td>" + poNumber + "</td></tr>"
		                + "<tr><th>Project Name</th><td>" + projectName + "</td></tr>"
		                + "<tr><th>Milestone Name</th><td>" + milestoneName + "</td></tr>"
		                + "<tr><th>Line Item</th><td>" + lineItemName + "</td></tr>"
		                + "<tr><th>Milestone Start Date</th><td>" + formattedStartDate + "</td></tr>"
		                + "<tr><th>Milestone End Date</th><td>" + formattedEndDate + "</td></tr>"
		                + "<tr><th>Status</th><td>" + milestoneStatus + "</td></tr>"
		                + "</table>"
		                + "<p>Please take the necessary actions.</p>"
		                + "<p>Regards,<br>ApMoSys Technologies</p>"
		                + "</body></html>";

		            try {
		                mailService.sendMailToMultipleRecipients(toRecipients, ccRecipients, subject, body);
		                System.out.println("Mail sent for project: " + projectName);

		            } catch (Exception e) {
		                System.err.println("Error sending mail for project: " + projectName);
		                e.printStackTrace();
		                exceptionDetails = "Mail Error: " + e.getMessage();
		            }

		        }
	
		        httpStatusCode = HttpStatus.OK.value();
		    } catch (Exception e) {
		        System.err.println("Exception occurred in milestoneExpiryNotifierMail");
		        e.printStackTrace();
		        exceptionDetails = "Job Failed: " + e.toString();
		    } finally {
		        if (initialLog != null && initialLog.getId() != null) {
		            apiLogUtility.endLog(
		                initialLog.getId(),
		                getExpiryMilestoneUrl,
		                httpStatusCode,
		                exceptionDetails,
		                null
		            );
		        }
		    }
	
		    System.out.println("======= Project Expiry Job Completed =======");
		}

	
	
	
	
	
	
	
	
	
	
	
	
	
	public ServiceResponse getAllMilestoneToBeExpired(Long rmId) {
	    ServiceResponse response = new ServiceResponse();
	    List<MilestoneExpireDto> milestones = new ArrayList<>();
	    String traceId = UUID.randomUUID().toString();
	    ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String exceptionDetailsForLog = null;
	    boolean isLogEnded = false;

	    try {
	       
	    	System.out.println("rmMail"+" "+rmId);
	        if (rmId == null || Objects.isNull(rmId)) {
	            String message = "rmId is null or empty.";
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(message);
	            response.setServiceError(message);
	            response.setServiceMessage(message);
	            return response;
	        }

	      
	        initialLog = apiLogUtility.startLog(
	            traceId,
	            "getAllMilestoneToBeExpired",
	            "poPortal",
	            getCurrentUserId(),
	            httpRequest
	        );

	        
	        List<Long> projectIds = projectRepository.findPoProjectIdsByProjectManagerIdWithJoin(rmId);
	        
	        System.out.println("projectIds="+projectIds);
	        
	        if (projectIds == null || projectIds.isEmpty()) {
	            String message = "No project IDs found for RM: " + rmId;
	            response.setServiceResponse(message);
	            finalHttpStatusCode = HttpStatus.OK.value();
	            return response;
	        }

	       
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());

	        HttpEntity<List<Long>> requestEntity = new HttpEntity<>(projectIds, headers);
	        ResponseEntity<MilestoneExpireDto[]> apiResponse = restTemplate.exchange(
	        	getExpiryMilestoneUrl,
	            HttpMethod.POST,
	            requestEntity,
	            MilestoneExpireDto[].class
	        );

	        finalHttpStatusCode = apiResponse.getStatusCodeValue();

	        if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
	            milestones = Arrays.asList(apiResponse.getBody());
	            
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceMessage("Successfully retived milestone tobe expired from po");
	            
	            response.setServiceResponse(milestones);
	        } else {
	            String msg = "External API returned status: " + apiResponse.getStatusCode();
	            exceptionDetailsForLog = msg;
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(msg);
	            response.setServiceError(msg);
	            response.setServiceMessage(msg);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        exceptionDetailsForLog = e.toString();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Unexpected error occurred.");
	        response.setServiceError(e.getMessage());
	        response.setServiceMessage(e.getMessage());
	    } finally {
	        try {
	            if (initialLog != null && initialLog.getId() != null && !isLogEnded) {
	                String logMsg = (exceptionDetailsForLog != null)
	                        ? exceptionDetailsForLog
	                        : "Fetched " + milestones.size() + " milestones.";
	                apiLogUtility.endLog(initialLog.getId(), getExpiryMilestoneUrl, finalHttpStatusCode, logMsg, httpRequest);
	            }
	        } catch (Exception logEx) {
	            logEx.printStackTrace();
	        }
	    }

	    return response;
	}
	
	
	
	
	
	
	
	
	

	@Transactional(rollbackFor = PoportalApiException.class)
	public ServiceResponse updateMilestoneExtendedDate(MilestoneUpdatedLogDto dto) {
		ServiceResponse response = new ServiceResponse();
		String traceId = UUID.randomUUID().toString();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;

		try {
			if (dto == null || dto.getMilestoneId() == null || dto.getExtendedDate() == null) {
				String message = "Milestone ID or extended date is missing.";
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(message);
				response.setServiceError(message);
				response.setServiceMessage(message);
				return response;
			}

			initialLog = apiLogUtility.startLog(traceId, "updateMilestoneExtendedDate", "poPortal", getCurrentUserId(),
					httpRequest);

			MilestoneUpdatedLog log = MilestoneUpdatedLog.builder()
					.milestoneId(dto.getMilestoneId())
					.poId(dto.getPoId())
					.projectId(dto.getProjectId())
					.milestoneName(dto.getMilestoneName())
					.milestoneStartDate(dto.getMilestoneStartDate())
					.milestoneEndDate(dto.getMilestoneEndDate())
					.description(dto.getDescription())
					.remarks(dto.getRemarks())
					.milestoneStatus(dto.getMilestoneStatus())
					.lineItemId(dto.getLineItemId())
					.lineItemName(dto.getLineItemName())
					.projectName(dto.getProjectName())
					.poNumber(dto.getPoNumber())
					.extendedDate(dto.getExtendedDate())
					.updatedBy(dto.getUpdatedBy())
					.updatedOn(new Date())
					.build();

			if (dto.getMilestoneExtensionReasonId() != null) {
				milestoneExtensionReasonRepository.findById(dto.getMilestoneExtensionReasonId())
						.ifPresent(log::setMilestoneExtensionReason);
			}

			milestoneUpdatedLogRepository.save(log);

			MilestoneExpireDto updateRequest = new MilestoneExpireDto();
			updateRequest.setId(dto.getMilestoneId());
			updateRequest.setEndDate(dto.getExtendedDate());
			updateRequest.setUpdatedBy(dto.getUpdatedBy());

			String milestoneExtensionReason = (log.getMilestoneExtensionReason() != null)
					? log.getMilestoneExtensionReason().getMilestoneExtensionReason()
					: null;

			updateRequest.setMilestoneExtensionReason(
					"Other".equalsIgnoreCase(milestoneExtensionReason)
							? dto.getMilestoneExtensionReasonText()
							: milestoneExtensionReason);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());

			HttpEntity<MilestoneExpireDto> requestEntity = new HttpEntity<>(updateRequest, headers);
			ResponseEntity<String> externalResponse = restTemplate.exchange(updateMilestoneEndDateExternalUrl,
					HttpMethod.POST, requestEntity, String.class);

			finalHttpStatusCode = externalResponse.getStatusCodeValue();

			if (externalResponse.getStatusCode() != HttpStatus.OK) {
				throw new PoportalApiException("External API failed with status: " + externalResponse.getStatusCode());
			}

			boolean emailSent = sendMilestoneUpdateMail(dto, log);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceMessage(emailSent
					? "Milestone updated and email notification sent."
					: "Milestone updated, but email notification could not be sent.");

		} catch (PoportalApiException apiEx) {
			exceptionDetailsForLog = apiEx.toString();
			logger.error("External API failed: {}", exceptionDetailsForLog);

			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Failed to update milestone due to external API error.");
			response.setServiceError(apiEx.getMessage());
			response.setServiceMessage(apiEx.getMessage());

			throw apiEx;

		} catch (Exception e) {
			exceptionDetailsForLog = e.toString();
			logger.error("Error in updateMilestoneExtendedDate: {}", exceptionDetailsForLog);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceMessage("Milestone updated, but with warnings: " + e.getMessage());
		} finally {
			try {
				if (initialLog != null) {
					String logMsg = (exceptionDetailsForLog != null)
							? exceptionDetailsForLog
							: "Milestone extended successfully.";
					apiLogUtility.endLog(initialLog.getId(), updateMilestoneEndDateExternalUrl,
							finalHttpStatusCode, logMsg, httpRequest);
				}
			} catch (Exception logEx) {
				logger.error("Failed to end log: {}", logEx.getMessage());
			}
		}

		return response;
	}

	private boolean sendMilestoneUpdateMail(MilestoneUpdatedLogDto dto, MilestoneUpdatedLog log) {
		try {
			Optional<RmAndHodEmailDto> optionalEmails = getRmAndHodEmails(dto.getProjectId());
			if (optionalEmails.isEmpty()) {
				logger.warn("No email addresses found for project: {}", dto.getProjectId());
				return false;
			}

			RmAndHodEmailDto emailDto = optionalEmails.get();
			List<String> rmEmails = emailDto.getRmEmails() != null ? emailDto.getRmEmails() : Collections.emptyList();
			List<String> hodEmails = emailDto.getHodEmails() != null ? emailDto.getHodEmails()
					: Collections.emptyList();

		    List<String> directorEmails = projectRepository.findDirectorEmails();

			List<String> toRecipients = Stream.concat(rmEmails.stream(), hodEmails.stream())
					.filter(e -> e != null && !e.trim().isEmpty())
					.distinct()
					.collect(Collectors.toList());

			if (toRecipients.isEmpty()) {
				logger.warn("Skipping milestone email due to empty RM/HOD emails: {}", dto.getMilestoneName());
				return false;
			}

		    List<String> ccRecipients = directorEmails.stream()
		            .filter(e -> e != null && !e.trim().isEmpty())
		            .distinct()
		            .collect(Collectors.toList());
		    
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			String extendedDate = formatter.format(dto.getExtendedDate());
			String endDate = formatter.format(dto.getMilestoneEndDate());
			String startDate = formatter.format(dto.getMilestoneStartDate());

			String subject = "Project Milestone Extend Notification: " + dto.getProjectName();
			String body = "<html><body>"
					+ "<p>Dear Team,</p>"
					+ "<p>The following project milestone end date has been extended from " + endDate + " to "
					+ extendedDate + ".</p>"
					+ "<table border='1' style='border-collapse: collapse;'>"
					+ "<tr><th>PO Number</th><td>" + dto.getPoNumber() + "</td></tr>"
					+ "<tr><th>Project Name</th><td>" + dto.getProjectName() + "</td></tr>"
					+ "<tr><th>Milestone Name</th><td>" + dto.getMilestoneName() + "</td></tr>"
					+ "<tr><th>Line Item</th><td>" + dto.getLineItemName() + "</td></tr>"
					+ "<tr><th>Milestone Start Date</th><td>" + startDate + "</td></tr>"
					+ "<tr><th>Milestone End Date</th><td>" + endDate + "</td></tr>"
					+ "<tr><th>Milestone Extended Date</th><td>" + extendedDate + "</td></tr>"
					+ "<tr><th>Status</th><td>" + dto.getMilestoneStatus() + "</td></tr>"
					+ "</table>"
					+ "<p>Please take the necessary actions.</p>"
					+ "<p>Regards,<br>ApMoSys Technologies</p>"
					+ "</body></html>";

			mailService.sendMailToMultipleRecipients(toRecipients, ccRecipients, subject, body);
			logger.info("Email sent successfully for milestone: {}", dto.getMilestoneName());
			return true;

		} catch (Exception e) {
			logger.error("Failed to send email for milestone {}: {}", dto.getMilestoneName(), e.getMessage());
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
	public ServiceResponse getAllMilestoneExtendReason() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    StringBuilder logBuilder = new StringBuilder("Fetching milestone extension reasons... ");

	    apiLogInfo.setApiUrl("/api/getAllMilestoneExtendReason");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        List<MilestoneExtensionReason> reasons = milestoneExtensionReasonRepository
	                .findAllByOrderByMilestoneExtensionReasonAsc();

	        response.setServiceResponse(reasons);
	        response.setServiceMessage("Milestone extension reasons fetched successfully.");
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Successfully retrieved milestone extension reasons.");
	        logBuilder.append("Success.");
	    } catch (Exception e) {
	        logBuilder.append("Failed. Exception: ").append(e.getMessage());

	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage("Failed to fetch milestone extension reasons.");
	        response.setServiceError(e.getMessage());
	        response.setServiceResponse(Collections.emptyList());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Error: " + e.getMessage());

	        
	      
	    }

	   
	    System.out.println(logBuilder.toString());

	    return response;
	}
	
	
	
	
	
	
	
	
	
	public ServiceResponse getMilestoneProjectWise() {
	    ServiceResponse response = new ServiceResponse();
	    String traceId = UUID.randomUUID().toString();
	    ApiLog initialLog = null;
	    List<MilestoneExpireDto> milestones = new ArrayList<>();
	    try {
	  
	        initialLog = apiLogUtility.startLog(traceId, "getMilestoneProjectWise", "poPortal", getCurrentUserId(), httpRequest);
	        
	    
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<List<Long>> requestEntity = new HttpEntity<>(null, headers);
	        
	       
	        ResponseEntity<MilestoneExpireDto[]> apiResponse = restTemplate.exchange(
	            getExpiryMilestoneUrl, 
	            HttpMethod.POST, 
	            requestEntity, 
	            MilestoneExpireDto[].class
	        );
	    
	        if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
	            milestones = Arrays.asList(apiResponse.getBody());
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceMessage("Successfully fetched milestones.");
	      
	            Map<Long, List<MilestoneExpireDto>> milestonesByProject = milestones.stream()
	                    .collect(Collectors.groupingBy(MilestoneExpireDto::getProjectId));
	            List<ProjectWiseMilestoneDto> projectWithMilestonesList = new ArrayList<>();
	            for (Long projectId : milestonesByProject.keySet()) {
	                List<MilestoneExpireDto> milestoneList = milestonesByProject.get(projectId);
	                Project project = projectRepository.findByPoProjectId(projectId);
	                if (project == null) {
	                    String msg = "Project with ID " + projectId + " not found based on the milestone.";
	                    logger.warn(msg);
	                    
	                    continue;
	                }
	                
	                ProjectWiseMilestoneDto projectWithMilestones = new ProjectWiseMilestoneDto();
	                projectWithMilestones.setProject(project);
	                projectWithMilestones.setMilestones(milestoneList);
	                projectWithMilestonesList.add(projectWithMilestones);
	            }
	            response.setServiceResponse(projectWithMilestonesList);
	            logger.info("Grouped {} milestones based on project IDs.", milestones.size());
	        } else {
	            String errorMsg = "Failed to fetch milestones: " + apiResponse.getStatusCode();
	            logger.error(errorMsg);
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceMessage(errorMsg);
	            response.setStatusCode(200);
	        }
	    } catch (Exception e) {
	        logger.error("Unexpected error occurred while fetching milestones: {}", e.getMessage(), e);
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Unexpected error occurred.");
	        response.setServiceError(e.getMessage());
	        response.setServiceMessage("Error: " + e.getMessage());
	    } finally {
	       
	        try {
	            if (initialLog != null && initialLog.getId() != null) {
	                String logMsg = initialLog.getId() != null ? "Total milestones fetched: " + milestones.size() : "Log ended without fetching milestones.";
	                apiLogUtility.endLog(initialLog.getId(), getExpiryMilestoneUrl, HttpStatus.OK.value(), logMsg, httpRequest);
	            }
	        } catch (Exception logEx) {
	            logger.error("Failed to end log: {}", logEx.getMessage());
	        }
	    }
	    return response;
	}
	
	public Optional<RmAndHodEmailDto> getRmAndHodEmails(Long projectId) {
	    List<Object[]> results = projectRepository.findRawRmAndHodEmailsByProjectId(projectId);

	    if (results.isEmpty()) {
	        return Optional.empty();
	    }

	    Set<String> rmEmails = new HashSet<>();
	    Set<String> hodEmails = new HashSet<>();

	    for (Object[] row : results) {
	        if (row[0] != null && !row[0].toString().trim().isEmpty()) {
	            rmEmails.add(row[0].toString().trim());
	        }
	        if (row[1] != null && !row[1].toString().trim().isEmpty()) {
	            hodEmails.add(row[1].toString().trim());
	        }
	    }

	    return Optional.of(new RmAndHodEmailDto(
	        new ArrayList<>(rmEmails),
	        new ArrayList<>(hodEmails)
	    ));
	}
	
	
	
	

	
	public ServiceResponse getResourceCountByPoprojectId(List<String> projectNames) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Resource Count");
	    apiLogInfo.setApiUrl("/api/getResourceCountByPoprojectId");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (projectNames == null || projectNames.isEmpty()) {
	            throw new IllegalArgumentException("poProjectId list is required");
	        }

	        Map<String, Integer> resourceCounts = new HashMap<>();
	        List<Object[]> results = projectRepository.getResourceCountsByProjectType(projectNames);

	        for (Object[] row : results) {
	            if (row[0] == null) continue;

	            // Normalize project type string
	            String projectType = row[0].toString().trim().toLowerCase();

	            if (projectType.contains("tnm")) {
	                projectType = "TNM";
	            } else if (projectType.contains("fixed")) {
	                projectType = "Fixed Cost";
	            } else if (projectType.contains("monitoring")) {
	                projectType = "Monitoring";
	            } else {
	                // Default unknown types if new type appears
	                projectType = Character.toUpperCase(projectType.charAt(0)) + projectType.substring(1);
	            }

	            Integer count = ((Number) row[1]).intValue();
	            resourceCounts.put(projectType, count);
	        }

	        // Ensure all expected types exist in map
	        resourceCounts.putIfAbsent("TNM", 0);
	        resourceCounts.putIfAbsent("Fixed Cost", 0);
	        resourceCounts.putIfAbsent("Monitoring", 0);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(resourceCounts);

	        apiLogInfo.setApiResponse("Success");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (IllegalArgumentException ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(ex.getMessage());
	        apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    } catch (Exception ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Unexpected error: " + ex.getMessage());
	        apiLogInfo.setApiResponse("Unexpected Error: " + ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    } finally {
	        logService.logMyInfo(httpRequest, apiLogInfo);
	    }

	    return response;
	}


	public ServiceResponse getResourceCountListByPoprojectName(List<String> projectNames) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Resource List");
	    apiLogInfo.setApiUrl("/api/getResourceCountListByPoprojectName");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (projectNames == null || projectNames.isEmpty()) {
	            throw new IllegalArgumentException("poProjectName list is required");
	        }

	        List<Map<String, Object>> resourceList = new ArrayList<Map<String, Object>>();
	        List<Object[]> results = projectRepository.getResourceListByProjectType(projectNames);
	        for (Object[] row : results) {
	            Map<String, Object> map = new HashMap<String, Object>();
	            map.put("empId", row[0]);
	            map.put("employementId", row[1]);
	            map.put("empName", row[2]);
	            map.put("department", row[3]);
	            map.put("role", row[4]);
	            map.put("teamName", row[5]);
	            map.put("projectManagerName", row[6]);
	            map.put("projectName", row[7]); 
	            resourceList.add(map);
	        }	       

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(resourceList);
	        response.setServiceMessage("Resource list fetched successfully");
	        response.setStatusCode(200);
	        apiLogInfo.setApiResponse("Success");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (IllegalArgumentException ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(ex.getMessage());
	        apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    } catch (Exception ex) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Unexpected error: " + ex.getMessage());
	        apiLogInfo.setApiResponse("Unexpected Error: " + ex.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    } finally {
	        logService.logMyInfo(httpRequest, apiLogInfo);
	    }

	    return response;
	}



	public ServiceResponse getAllPoByProjectId(List<Long> poProjectIdList) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			initialLog = apiLogUtility.startLog(traceId, "getAllPoByProjectId", "Ishine", getCurrentUserId(),
					httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse
						.setServiceResponse("Critical Error: Could not initialize logging for fetching the data.");
				return serviceResponse;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());

			HttpEntity<List<Long>> requestEntity = new HttpEntity<>(poProjectIdList, headers);
			ResponseEntity<ProjectPoDTO[]> responseEntity = restTemplate.exchange(getAllPoByProjectIdUrl,
					HttpMethod.POST, requestEntity, ProjectPoDTO[].class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				finalHttpStatusCode = HttpStatus.OK.value();
			}
			ProjectPoDTO[] projectPoDTOs = responseEntity.getBody();
			List<ProjectPoDTO> poPortalprojectList = new ArrayList<>();
			if (projectPoDTOs != null && projectPoDTOs.length > 0) {
				poPortalprojectList = Arrays.asList(projectPoDTOs);
				serviceResponse.setServiceResponse(poPortalprojectList);
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("No projects found for the given project ids in PoPortal");
			}
		} catch (Exception e) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Error fetching projects: " + e.getMessage());
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), getAllPoByProjectIdUrl, finalHttpStatusCode, finalLogDetails,
						httpRequest);
			}
		}
		return serviceResponse;
	}

	public ServiceResponse getAllProjectNameByPoNoLike(String poNo) {
		ServiceResponse serviceResponse = new ServiceResponse();
		ApiLog initialLog = null;
		String traceId = UUID.randomUUID().toString();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String exceptionDetailsForLog = null;
		try {
			initialLog = apiLogUtility.startLog(traceId, "getAllProjectNameByPoNoLike", "Ishine", getCurrentUserId(),
					httpRequest);
			if (initialLog == null || initialLog.getId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for fetching the data.");
				return serviceResponse;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());

			HttpEntity<String> requestEntity = new HttpEntity<>(poNo, headers);

			ResponseEntity<ProjectPoDTO[]> responseEntity = restTemplate.exchange(getAllProjectNameByPoNoLikeUrl,
					HttpMethod.POST, requestEntity, ProjectPoDTO[].class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				finalHttpStatusCode = HttpStatus.OK.value();
			}

			ProjectPoDTO[] projectPoDTOs = responseEntity.getBody();
			List<ProjectPoDTO> poPortalprojectList = new ArrayList<>();
			if (projectPoDTOs != null && projectPoDTOs.length > 0) {
				poPortalprojectList = Arrays.asList(projectPoDTOs);
				serviceResponse.setServiceResponse(poPortalprojectList);
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("No projects found for the given po no in PoPortal");
			}
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Error fetching projects: " + e.getMessage());
			exceptionDetailsForLog = e.toString();
			return serviceResponse;
		} finally {
			if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), getAllProjectNameByPoNoLikeUrl, finalHttpStatusCode, finalLogDetails,
						httpRequest);
			}
		}
		return serviceResponse;
	}

}