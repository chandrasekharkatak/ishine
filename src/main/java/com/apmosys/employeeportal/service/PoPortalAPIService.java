package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import java.util.Comparator;
import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import com.apmosys.employeeportal.model.MilestoneExtensionReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.DepartmentIdAndNameDto;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.MilestoneExpireDto;
import com.apmosys.employeeportal.dto.MilestoneUpdatedLogDto;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ProjectWiseMilestoneDto;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.MilestoneUpdatedLog;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.RoleDetails;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.MilestoneExtensionReasonRepository;
import com.apmosys.employeeportal.repository.MilestoneUpdatedLogRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.RoleDetailsRepository;
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
	
	@Autowired
	ClientsRepository clientRepository;
	
	@Autowired
	RoleDetailsRepository roleDetailsRepository;
	
	@Autowired
	ClientService clientService;
	
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
    
    
    @Value("${poPortal.api.getAllMailsByProjectId}")
    private String getAllMailsByProjectId;
    
    
    @Value("${poPortal.api.getAllPoOfProjects}")
    private String getAllPoOfProjects;

    
    @Value("${poPortal.api.updateMilestoneExtendedDate}")
    private String updateMilestoneEndDateExternalUrl;
    
    
    @Value("${poPortal.api.getSDEDOfProjects}")
    private String getStartDateEndDateOfProjectsFromPO;
    
    @Autowired
    private PoRequirementMappingRepository poRequirementMappingRepository;
	
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
	private ProjectPoDetailsRepository projectPoDetailsRepository;
	
	@Autowired
	private PoDepartmentMappingRepository poDepartmentMappingRepository;
	
	
	
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
//				DepartmentDTO syncObject = new DepartmentDTO();
//				syncObject.setDeptId(deptObj.getDeptId());
//				syncObject.setDeptName(deptObj.getName());
////				syncObject.setHodEmploymentId("A-".concat(empObj.getEmployeementId().toString()));
//				syncObject.setHodId(deptObj.getHodId());
//				syncObject.setBillable(deptObj.getIsBillable());
//				syncObject.setTnm(deptObj.getIsTnm());
				
				
				PoPortalDTO syncObject = new PoPortalDTO();
				syncObject.setDeptId(deptObj.getDeptId());
				syncObject.setDeptName(deptObj.getName());
				syncObject.setHodId(deptObj.getHodId().toString());
				syncObject.setDeptAbbreviation(deptObj.getDeptAbbreviation());
				syncObject.setIsBillable(deptObj.getIsBillable());
				syncObject.setIsTnm(deptObj.getIsTnm());
				
				
				
				
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
	    	e.printStackTrace();
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
	
	
	
	

	
	public ServiceResponse getResourceCountByPoprojectId(List<String> poNos) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Resource Count");
	    apiLogInfo.setApiUrl("/api/getResourceCountByPoprojectId");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (poNos == null || poNos.isEmpty()) {
	            throw new IllegalArgumentException("poNo list is required");
	        }

	        Map<String, Integer> resourceCounts = new HashMap<>();
	        List<Object[]> results = projectRepository.getResourceCountsByProjectType(poNos);

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


	public ServiceResponse getResourceCountListByPoprojectName(List<String> poNos) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Resource List");
	    apiLogInfo.setApiUrl("/api/getResourceCountListByPoprojectName");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        // if (projectNames == null || projectNames.isEmpty()) {
	        //     throw new IllegalArgumentException("poProjectName list is required");
	        // }
			 if (poNos == null || poNos.isEmpty()) {
	            throw new IllegalArgumentException("Po No list is required");
	        }


	        List<Map<String, Object>> resourceList = new ArrayList<Map<String, Object>>();
	        List<Object[]> results = projectRepository.getResourceListByProjectType(poNos);
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
	            map.put("poName", row[8]);
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




public ServiceResponse getAllMailsByProjectId(Long projectId) {
        ServiceResponse serviceResponse = new ServiceResponse();
        ApiLog initialLog = null;
        String traceId = UUID.randomUUID().toString();
        int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String exceptionDetailsForLog = null;
        ResponseEntity<List<String>> apiResponse = null;
        try {
        	
        	String url;
//        	initialLog = apiLogUtility.startLog(traceId, "getAllMailsByProjectId", "Ishine", getCurrentUserId(), httpRequest);
//	        if (initialLog == null || initialLog.getId() == null) {
//	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the API call.");
//	            return serviceResponse;
//	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<?> entity = new HttpEntity<>(headers);
	        url = getAllMailsByProjectId + projectId;
			apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<String>>() {});
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


	public static void main(String[] args) {
	    PoPortalAPIService service = new PoPortalAPIService();
		ServiceResponse response = service.getAllMailsByProjectId(8933L);
		System.out.println(response);
	}
	
	
//	@Transactional(rollbackFor = PoportalApiException.class)
//	public ServiceResponse syncProjectPoFromPoPortal() {
//
//	    ServiceResponse response = new ServiceResponse();
//	    String traceId = UUID.randomUUID().toString();
//	    ApiLog initialLog = null;
//	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
//	    StringBuilder exceptionDetailsForLog = new StringBuilder();
//
//	    List<ProjectPoMappingWithResourceDTO> externalApiResponse = new ArrayList<ProjectPoMappingWithResourceDTO>();
//
//	    try {
//	        initialLog = apiLogUtility.startLog(
//	                traceId,
//	                "syncProjectPoFromPoPortal",
//	                "Ishine",
//	                6l,
//	                httpRequest
//	        );
//	        
//	        poRequirementMappingRepository.deleteAllRecords();
//	        poDepartmentMappingRepository.deleteAllRecords();
//	        projectPoDetailsRepository.deleteAllRecords();
//
//	        logger.info("Old PO data cleared before sync");
//	        System.out.println("Old PO data cleared before sync \n");
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.set("Authorization",
//	                poPortalAPIAuthenticationJWTUtility.generateAccessToken());
//	        headers.set("X-Trace-Id", traceId);
//	        logger.info("header created \n");
//	        HttpEntity<?> entity = new HttpEntity<>(headers);
//	        logger.info("Initiating call to Shankh Portal \n");
//	        ResponseEntity<List<ProjectPoMappingWithResourceDTO>> apiResponse =
//	                restTemplate.exchange(
//	                        getAllPoOfProjects,
//	                        HttpMethod.GET,
//	                        entity,
//	                        new ParameterizedTypeReference<List<ProjectPoMappingWithResourceDTO>>() {}
//	                );
//
//	        logger.info("Completed call with Shankh Portal \n");
//	        finalHttpStatusCode = apiResponse.getStatusCodeValue();
//	        externalApiResponse = apiResponse.getBody();
//
//	        logger.info("Recieved body with data \n"+externalApiResponse);
//	        if (apiResponse.getStatusCode() != HttpStatus.OK || apiResponse.getBody() == null) {
//		        System.out.println("PO Portal API failed or returned empty response");
//	            throw new PoportalApiException("PO Portal API failed or returned empty response");
//	        }
//
//	        for (ProjectPoMappingWithResourceDTO projectDto : apiResponse.getBody()) {
//
//	            
//	            if (projectDto.getPoDetailsList() == null) {
//	                exceptionDetailsForLog.append(
//	                        "poDetailsList NULL | poProjectId=")
//	                        .append(projectDto.getProjectId())
//	                        .append(" || ");
//
//			        System.out.println("poDetailsList NULL | poProjectId= "+ projectDto.getProjectId());
//			        logger.info("poDetailsList NULL | poProjectId= \n"+ projectDto.getProjectId());
//	                continue;
//	            }
//
//		        logger.info("Initiated query to project repo \n");
//	            Project project = projectRepository
//	                    .findByPoProjectId(projectDto.getProjectId());
//
//	            if (project == null) {
//	                exceptionDetailsForLog.append(
//	                        "Project not found | poProjectId=")
//	                        .append(projectDto.getProjectId())
//	                        .append(" || ");
//	                logger.info("Project not found  | poProjectId= \n" + projectDto.getProjectId());
//	                System.out.println("Project not found  | poProjectId= "+ projectDto.getProjectId());
//	                continue;
//	            }
//	            
//	            Client client = clientRepository.findByClientId(project.getClientId());            
//	            Date minPoStartDate = null;
//	            Date maxPoEndDate = null;
//
//	            for (PoDetailsForProjectPoMappingDTO poDto : projectDto.getPoDetailsList()) {
//	            	
//	            	
//	            	
//	            	if (poDto.getPoStartDate() != null) {
//	                    if (minPoStartDate == null || poDto.getPoStartDate().before(minPoStartDate)) {
//	                        minPoStartDate = poDto.getPoStartDate();
//	                    }
//	                }
//
//	                if (poDto.getPoEndDate() != null) {
//	                    if (maxPoEndDate == null || poDto.getPoEndDate().after(maxPoEndDate)) {
//	                        maxPoEndDate = poDto.getPoEndDate();
//	                    }
//	                }
//	                try {
//	                	logger.info("call to saveSinglePoTransactional \n");
//	                    saveSinglePoTransactional(projectDto, poDto, project,client);
//	                } catch (PoportalApiException ex) {
//	                	ex.printStackTrace();
//						logger.info("catch for saveSinglePoTransactional \n");
//	                    exceptionDetailsForLog.append(
//	                            "Rollback PO | poProjectId=")
//	                            .append(projectDto.getProjectId())
//	                            .append(", poId=")
//	                            .append(poDto.getPoId())
//	                            .append(", reason=")
//	                            .append(ex.getMessage())
//	                            .append(" || ");
//	                }
//	            }
//	            if (minPoStartDate != null && maxPoEndDate != null) {
//
//	                Date projectStartDate = projectDto.getProjectStartDate();
//	                Date projectEndDate = projectDto.getProjectEndDate();
//
//	                boolean mismatch = false;
//
//	                if (projectStartDate == null || !projectStartDate.equals(minPoStartDate)) {
//	                    mismatch = true;
//	                }
//
//	                if (projectEndDate == null || !projectEndDate.equals(maxPoEndDate)) {
//	                    mismatch = true;
//	                }
//
//	                if (mismatch) {
//	                    exceptionDetailsForLog.append(
//	                            "Project-PO date mismatch | poProjectId=")
//	                            .append(projectDto.getProjectId())
//	                            .append(", projectStartDate=")
//	                            .append(projectStartDate)
//	                            .append(", expectedStartDate=")
//	                            .append(minPoStartDate)
//	                            .append(", projectEndDate=")
//	                            .append(projectEndDate)
//	                            .append(", expectedEndDate=")
//	                            .append(maxPoEndDate)
//	                            .append(" || ");
//
//	                    logger.info("Project-PO date mismatch | poProjectId=" + projectDto.getProjectId());
//	                }
//	            }
//	        }
//
//	        response.setServiceResponse(externalApiResponse);
//	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        response.setServiceMessage("PO sync completed with partial validations.");
//
//	    } catch (PoportalApiException ex) {
//	        exceptionDetailsForLog.append(ex.getMessage());
//			logger.info("catch for PoportalApiException \n");
//	        ex.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        response.setServiceMessage("PO sync failed due to external API error.");
//	        response.setServiceError(ex.getMessage());
//
//	        throw ex; 
//
//	    } catch (Exception e) {
//	        exceptionDetailsForLog.append(e.getMessage());
//			logger.info("catch for Exception \n");
//	        e.printStackTrace();
//            response.setServiceResponse(externalApiResponse);      
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        response.setServiceMessage("PO sync completed with warnings.");
//	        return response;
//	    } finally {
//	        try {
//	            if (initialLog != null) {
//	                apiLogUtility.endLog(
//	                        initialLog.getId(),
//	                        getAllPoOfProjects,
//	                        finalHttpStatusCode,
//	                        exceptionDetailsForLog.toString(),
//	                        httpRequest
//	                );
//	            }
//	        } catch (Exception logEx) {
//	        	logEx.printStackTrace();
//			logger.info("catch for Exception logEx \n");
//	            logger.error("Failed to end API log", logEx);
//	        }
//	        try {
//	            if (exceptionDetailsForLog.length() > 0) {
//
//	                String mailBody =
//	                        "<b>Trace ID:</b> " + traceId + "<br/><br/>"
//	                      + "<b>API:</b> syncProjectPoFromPoPortal<br/><br/>"
//	                      + "<b>Exception Details:</b><br/>"
//	                      + "<pre>" + exceptionDetailsForLog.toString() + "</pre>";
//
//	                mailService.sendMail(
//	                        "prarthana.lenka@apmosys.com",
//	                        "PO Sync Issues | TraceId : " + traceId,
//	                        mailBody
//	                );
//	            }
//	        } catch (Exception mailEx) {
//	          
//	            mailEx.printStackTrace();
//	            logger.error("Failed to send exception mail", mailEx);
//	        }
//	    }
//
//	    return response;
//	}
//
//	
//	
//	@Transactional(rollbackFor = PoportalApiException.class)
//	public void saveSinglePoTransactional(
//	        ProjectPoMappingWithResourceDTO projectDto,
//	        PoDetailsForProjectPoMappingDTO poDto,
//	        Project project,Client client) {
//
//	   
//	    if (poDto.getDepartmentList() == null) {
//	        throw new PoportalApiException("Department list is NULL");
//	    }
//	    logger.info("department list is not null \n");
//	    
//	    if ("TNM".equalsIgnoreCase(projectDto.getProjectType())
//	            && poDto.getResourceRequirementList() == null) {
//	        throw new PoportalApiException("TNM project missing resources");
//	    }
//
//	    logger.info("TNM project not missing resources \n");
//	    
//	    ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
//				poDto.getClientState(),poDto.getClientAddressId());
//	   
//	    ProjectPoDetails poDetails = new ProjectPoDetails();
//	    poDetails.setPoId(poDto.getPoId());
//	    poDetails.setProjectId(project.getProjectId());
//	    poDetails.setPoNo(poDto.getPoNo());
//	    
//        
//        poDetails.setPoStartDate(convert(poDto.getPoStartDate()));
//        poDetails.setPoEndDate(convert(poDto.getPoEndDate()));
//	   
//	   
////	    poDetails.setClientLocationId(poDto.getClientAddressId());
//	    poDetails.setMsg(poDto.getCommentForRmg());
//	    poDetails.setApmosysRM(poDto.getApmosysRmEmpName());
//	    poDetails.setApmosysRmEmail(poDto.getApmosysRmEmail());
//	    poDetails.setClientRm(poDto.getClientRmName());
//	    poDetails.setPrevPO(poDto.getPrevPo());
//	    poDetails.setNextPO(poDto.getNextPO());
//	    poDetails.setActive(poDto.isActive());
//	    poDetails.setPoProjectId(projectDto.getProjectId());
//	    poDetails.setClientLocationId(Long.valueOf(cl.getClientLocationId()));	
//	    poDetails.setClientAddressId(poDto.getClientAddressId());
////	    Long createdByEmpPk = validateAndGetCreatedByEmpId(
////	            poDto.getCreatedByEmpId(),
////	            poDto.getCreatedByEmpName()
////	    );
////	    poDetails.setCreatedBy(createdByEmpPk);
//
////	    poDetails.setCreatedBy(123l);
//	  
//
//
//	    
//	    
//	    
//	    System.out.println("saved in po details");
//	    logger.info("saved in po details");
//	    projectPoDetailsRepository.save(poDetails);
//
//	   
//	    for (DepartmentIdAndNameDto dept : poDto.getDepartmentList()) {
//	        PoDepartmentMapping map = new PoDepartmentMapping();
//	        map.setPoId(poDto.getPoId());
//	        map.setDeptId(dept.getDeptId());
//	        map.setActive(true);
//	        map.setProjectId(project.getProjectId());
//	        System.out.println("saved in poDepartmentMappingRepository");
//	        logger.info("saved in poDepartmentMappingRepository");
//	        poDepartmentMappingRepository.save(map);
//	    }
//
//	   
//	    if (poDto.getResourceRequirementList() != null) {
//	        for (POResourceRequirementDTO req : poDto.getResourceRequirementList()) {
//	            PoRequirementMapping prm = new PoRequirementMapping();
//	            prm.setPoId(poDto.getPoId());
//	            prm.setRole(req.getRole());
//	            prm.setExperience(req.getExperience());
//	            prm.setCount(req.getCount());
//	            prm.setClientRoleId(req.getClientRoleId());
//	            prm.setDepartment(req.getDepartment());            
//	            prm.setActive(true);
//	            prm.setLineItemEndDate(convert(req.getLineItemEndDate()));
//	            prm.setLineItemStartDate(convert(req.getLineItemStartDate()));
//	            prm.setYearWiseRateCartStartDate(convert(req.getYearWiseRateCartStartDate()));   
//	            prm.setYearWiseRateCartEndDate(convert(req.getYearWiseRateCartEndDate()));            
//	            
//		        System.out.println("saved in poRequirementMappingRepository");
//		        logger.info("saved in poRequirementMappingRepository");
//	            poRequirementMappingRepository.save(prm);
//	        }
//	    }
//	}
	
	
//	@Transactional(rollbackFor = PoportalApiException.class)
	public ServiceResponse syncProjectPoFromPoPortal() {

	    ServiceResponse response = new ServiceResponse();
	    String traceId = UUID.randomUUID().toString();
	    ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    StringBuilder exceptionDetailsForLog = new StringBuilder();

	    List<ProjectPoMappingWithResourceDTO> externalApiResponse = new ArrayList<ProjectPoMappingWithResourceDTO>();

	    try {
	        initialLog = apiLogUtility.startLog(
	                traceId,
	                "syncProjectPoFromPoPortal",
	                "Ishine",
	                6l,
	                httpRequest
	        );
	        
	       

	        

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization",
	                poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        headers.set("X-Trace-Id", traceId);
	        logger.info("header created \n");
	        HttpEntity<?> entity = new HttpEntity<>(headers);
	        logger.info("Initiating call to Shankh Portal \n");
	        ResponseEntity<List<ProjectPoMappingWithResourceDTO>> apiResponse =
	                restTemplate.exchange(
	                        getAllPoOfProjects,
	                        HttpMethod.GET,
	                        entity,
	                        new ParameterizedTypeReference<List<ProjectPoMappingWithResourceDTO>>() {}
	                );

	        logger.info("Completed call with Shankh Portal \n");
	        finalHttpStatusCode = apiResponse.getStatusCodeValue();
	        externalApiResponse = apiResponse.getBody();

	        logger.info("Recieved body with data \n"+externalApiResponse);
	        if (apiResponse.getStatusCode() != HttpStatus.OK || apiResponse.getBody() == null) {
	        	 exceptionDetailsForLog.append("PO Portal API failed or returned empty response");
		        System.out.println("PO Portal API failed or returned empty response");
	            throw new PoportalApiException("PO Portal API failed or returned empty response");
	        }
	        
	        
//	        clearOldData();
	        poRequirementMappingRepository.deleteAllRecords();
	        poDepartmentMappingRepository.deleteAllRecords();
	        projectPoDetailsRepository.deleteAllRecords();
	        
	        logger.info("Old PO data cleared before sync");
	        System.out.println("Old PO data cleared before sync \n");

	        for (ProjectPoMappingWithResourceDTO projectDto : apiResponse.getBody()) {
	        	
//	        	System.out.println("Processing Project | poProjectId={} | projectName={}"+
//	        	        projectDto.getProjectId());
	        	
	        	 logger.info("Processing Project | poProjectId={} | projectName={}",
	        	            projectDto.getProjectId(),
	        	            projectDto.getProjectName());

	            
	            if (projectDto.getPoDetailsList() == null) {
	                exceptionDetailsForLog.append(
	                        "poDetailsList NULL | poProjectId=")
	                        .append(projectDto.getProjectId())
	                        .append(" || ");

			        System.out.println("poDetailsList NULL | poProjectId= "+ projectDto.getProjectId());
			        logger.info("poDetailsList NULL | poProjectId= \n"+ projectDto.getProjectId());
	                continue;
	            }

		        logger.info("Initiated query to project repo \n");
	            Project project = projectRepository
	                    .findByPoProjectId(projectDto.getProjectId());

	            if (project == null) {
	                exceptionDetailsForLog.append(
	                        "Project not found | poProjectId=")
	                        .append(projectDto.getProjectId())
	                        .append(" || ");
	                logger.info("Project not found  | poProjectId= \n" + projectDto.getProjectId());
	                System.out.println("Project not found  | poProjectId= "+ projectDto.getProjectId());
	                continue;
	            }
	            
	            Client client = clientRepository.findByClientId(project.getClientId());            
	            
	            boolean allPoSuccess = true;
	            for (PoDetailsForProjectPoMappingDTO poDto : projectDto.getPoDetailsList()) { 
	            	
	            	System.out.println("Processing PO | poProjectId={} | projectName={}"+
	            			poDto.getPoId());
		        	
	            	
	            	 logger.info("Processing PO | poProjectId={} | poId={} | poNo={}",
	            	            projectDto.getProjectId(),
	            	            poDto.getPoId());
	                try {
	                	logger.info("call to saveSinglePoTransactional \n");
	                    saveSinglePoTransactional(projectDto, poDto, project,client);
	                } catch (PoportalApiException ex) {
	                	ex.printStackTrace();
	                	allPoSuccess = false;
						logger.info("catch for saveSinglePoTransactional \n");
	                    exceptionDetailsForLog.append(
	                            "Rollback PO | poProjectId=")
	                            .append(projectDto.getProjectId())
	                            .append(", poId=")
	                            .append(poDto.getPoId())
	                            .append(", reason=")
	                            .append(ex.getMessage())
	                            .append(" || ");
	                }
	            }
	            if (allPoSuccess) {
	            	clientService.updateProjectAfterSuccessfulSync(project, projectDto);
	            }
	            
	        }

	        response.setServiceResponse(externalApiResponse);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceMessage("PO sync completed with partial validations.");

	    } catch (PoportalApiException ex) {
	        exceptionDetailsForLog.append(ex.getMessage());
			logger.info("catch for PoportalApiException \n");
	        ex.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage("PO sync failed due to external API error.");
	        response.setServiceError(ex.getMessage());

	        throw ex; 

	    } catch (Exception e) {
	        exceptionDetailsForLog.append(e.getMessage());
			logger.info("catch for Exception \n");
	        e.printStackTrace();
            response.setServiceResponse(externalApiResponse);      
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceMessage("PO sync completed with warnings.");
	        return response;
	    } finally {
	        try {
	            if (initialLog != null) {
	                apiLogUtility.endLog(
	                        initialLog.getId(),
	                        getAllPoOfProjects,
	                        finalHttpStatusCode,
	                        exceptionDetailsForLog.toString(),
	                        httpRequest
	                );
	            }
	        } catch (Exception logEx) {
	        	logEx.printStackTrace();
			logger.info("catch for Exception logEx \n");
	            logger.error("Failed to end API log", logEx);
	        }
	        try {
	            if (exceptionDetailsForLog.length() > 0) {

	                String mailBody =
	                        "<b>Trace ID:</b> " + traceId + "<br/><br/>"
	                      + "<b>API:</b> syncProjectPoFromPoPortal<br/><br/>"
	                      + "<b>Exception Details:</b><br/>"
	                      + "<pre>" + exceptionDetailsForLog.toString() + "</pre>";

	                mailService.sendMailWithCC(
	                        "prarthana.lenka@apmosys.com","sumit.modi@apmosys.com",
	                        "PO Sync Issues | TraceId : " + traceId,
	                        mailBody
	                );
	            }
	        } catch (Exception mailEx) {
	          
	            mailEx.printStackTrace();
	            logger.error("Failed to send exception mail", mailEx);
	        }
	    }

	    return response;
	}

	
	
	@Transactional(rollbackFor = PoportalApiException.class)
	public void saveSinglePoTransactional(
	        ProjectPoMappingWithResourceDTO projectDto,
	        PoDetailsForProjectPoMappingDTO poDto,
	        Project project,Client client) {

	   
		if (project == null || client == null) {
			throw new PoportalApiException("Project or Client cannot be null");
		}
	    if (poDto.getDepartmentList() == null) {
	        throw new PoportalApiException("Department list is NULL");
	    }
	    logger.info("department list is not null ");
	    
	    if ("TNM".equalsIgnoreCase(projectDto.getProjectType())
	            && poDto.getResourceRequirementList() == null) {
	        throw new PoportalApiException("TNM project missing resources");
	    }
	    
	    if(poDto.getCreatedByEmpId()!= null) {
	    boolean createdExists = employeeRepository
                .existsByEmpIdAndEmployeeName(
                        poDto.getCreatedByEmpId(),
                        poDto.getCreatedByEmpName());
	    

        if (!createdExists) {
            throw new PoportalApiException(
                    "Employee not found for createdBy | empId="
                            + poDto.getCreatedByEmpId()
                            + ", name=" + poDto.getCreatedByEmpName());
        }
	    }

      
	    if(poDto.getUpdatedByEmpId() != null) {
        boolean updatedExists = employeeRepository
                .existsByEmpIdAndEmployeeName(
                        poDto.getUpdatedByEmpId(),
                        poDto.getUpdatedByEmpName());

        if (!updatedExists) {
            throw new PoportalApiException(
                    "Employee not found for updatedBy | empId="
                            + poDto.getUpdatedByEmpId()
                            + ", name=" + poDto.getUpdatedByEmpName());
        }
	    }
	    
	    

	    logger.info("TNM project not missing resources ");
	    ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState(),poDto.getClientAddressId());
	   
	    ProjectPoDetails poDetails = new ProjectPoDetails();
	    poDetails.setPoId(poDto.getPoId());
	    poDetails.setProjectId(project.getProjectId());
	    poDetails.setPoNo(poDto.getPoNo());
	    
        
        poDetails.setPoStartDate(convert(poDto.getPoStartDate()));
        poDetails.setPoEndDate(convert(poDto.getPoEndDate()));
	   
	   
//	    poDetails.setClientLocationId(poDto.getClientAddressId());
	    poDetails.setMsg(poDto.getCommentForRmg());
	    poDetails.setApmosysRM(poDto.getApmosysRmEmpName());
	    poDetails.setApmosysRmEmail(poDto.getApmosysRmEmail());
	    poDetails.setClientRm(poDto.getClientRmName());
	    poDetails.setPrevPO(poDto.getPrevPo());
	    poDetails.setNextPO(poDto.getNextPO());
	    poDetails.setActive(true);
	    poDetails.setPoProjectId(projectDto.getProjectId());
	    poDetails.setClientLocationId(Long.valueOf(cl.getClientLocationId()));	
	    poDetails.setClientAddressId(poDto.getClientAddressId());

	    
	    poDetails.setCreatedBy(poDto.getCreatedByEmpId());
        poDetails.setUpdatedBy(poDto.getUpdatedByEmpId());
        poDetails.setPoCreatedOn(convert(poDto.getCreatedOn()));
        poDetails.setPoUpdatedOn(convert(poDto.getUpdatedOn()));
	    
	    
	    projectPoDetailsRepository.save(poDetails);
		logger.info("PO details saved successfully, poId={}", poDto.getPoId());
	   
		List<PoDepartmentMapping> deptMappings = new ArrayList<>();
	    for (DepartmentIdAndNameDto dept : poDto.getDepartmentList()) {
	        PoDepartmentMapping map = new PoDepartmentMapping();
	        map.setPoId(poDto.getPoId());
	        map.setDeptId(dept.getDeptId());
	        map.setActive(true);
	        map.setProjectId(project.getProjectId());
	        map.setCreatedBy(poDto.getCreatedByEmpId());
	        // System.out.println("saved in poDepartmentMappingRepository");
	        logger.info("saved in poDepartmentMappingRepository");
			deptMappings.add(map);
	        // poDepartmentMappingRepository.save(map);
	    }
		poDepartmentMappingRepository.saveAll(deptMappings);

	   
	    if (poDto.getResourceRequirementList() != null) {
			 List<PoRequirementMapping> requirementMappings = new ArrayList<>();
            for (POResourceRequirementDTO req : poDto.getResourceRequirementList()) {

              
                RoleDetails roleDetails = roleDetailsRepository
                        .findByRoleAndDepartmentAndExperience(
                                req.getRole(),
                                req.getDepartment(),
                                req.getExperience())
                        .orElseGet(() -> {
                            RoleDetails role = new RoleDetails();
                            role.setRole(req.getRole());
                            role.setDepartment(req.getDepartment());
                            role.setExperience(req.getExperience());
                            return roleDetailsRepository.save(role);
                        });

                PoRequirementMapping prm = new PoRequirementMapping();

                prm.setPoId(poDto.getPoId());
                prm.setRoleId(roleDetails.getRoleId());
                prm.setCount(req.getCount());
                prm.setActive(true);

                prm.setLineItemStartDate(convert(req.getLineItemStartDate()));
                prm.setLineItemEndDate(convert(req.getLineItemEndDate()));
                prm.setYearWiseRateCartStartDate(convert(req.getYearWiseRateCartStartDate()));
                prm.setYearWiseRateCartEndDate(convert(req.getYearWiseRateCartEndDate()));

                prm.setCreatedBy(poDto.getCreatedByEmpId());

                // poRequirementMappingRepository.save(prm);
				requirementMappings.add(prm);
            }
			poRequirementMappingRepository.saveAll(requirementMappings);
        }
		logger.info("PO sync completed for poId={}", poDto.getPoId());
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public void clearOldData() {
	    poRequirementMappingRepository.deleteAllRecords();
	    poDepartmentMappingRepository.deleteAllRecords();
	    projectPoDetailsRepository.deleteAllRecords();
	}
	
	private LocalDateTime convert(Date date) {
	    if (date == null) return null;

	    if (date instanceof java.sql.Date) {
	        return ((java.sql.Date) date)
	                .toLocalDate()
	                .atStartOfDay();
	    }

	    return date.toInstant()
	            .atZone(ZoneId.systemDefault())
	            .toLocalDateTime();
	}
	
	private Long validateAndGetCreatedByEmpId(String createdByEmpId,
            String createdByEmpName) {

if (createdByEmpId == null || !createdByEmpId.startsWith("A-")) {
throw new PoportalApiException("Invalid createdByEmpId format");
}

Long employmentId;
try {
employmentId = Long.parseLong(createdByEmpId.substring(2));
} catch (NumberFormatException e) {
throw new PoportalApiException("Invalid employment id in createdByEmpId");
}

Long empId = employeeRepository
.findByEmploymentIdAndEmployeeName(employmentId, createdByEmpName)
.orElseThrow(() ->
new PoportalApiException(
"Employee mismatch: employmentId="
      + employmentId
      + ", name="
      + createdByEmpName
)
);
logger.info("created by empId =  \n"+empId);
return empId; 
}
	
	
	
//	@Transactional(rollbackFor = PoportalApiException.class)
//	public ServiceResponse updateSDEDOfproject() {
//
//	    ServiceResponse response = new ServiceResponse();
//	    String traceId = UUID.randomUUID().toString();
//	    ApiLog initialLog = null;
//	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
//	    StringBuilder exceptionDetailsForLog = new StringBuilder();
//
//	    List<ProjectPoMappingWithResourceDTO> externalApiResponse = new ArrayList<ProjectPoMappingWithResourceDTO>();
//
//	    try {
//	        initialLog = apiLogUtility.startLog(
//	                traceId,
//	                "syncStartDateEndDateOfProjectFromPo",
//	                "Ishine",
//	                6l,
//	                httpRequest
//	        );
//	        
//	        logger.info("log table set start");
//	        
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.set("Authorization",
//	                poPortalAPIAuthenticationJWTUtility.generateAccessToken());
//	        headers.set("X-Trace-Id", traceId);
//
//	        HttpEntity<?> entity = new HttpEntity<>(headers);
//	        
//	        logger.info("header set");
//
//	        ResponseEntity<List<ProjectPoMappingWithResourceDTO>> apiResponse =
//	                restTemplate.exchange(
//	                		getStartDateEndDateOfProjectsFromPO,
//	                        HttpMethod.GET,
//	                        entity,
//	                        new ParameterizedTypeReference<List<ProjectPoMappingWithResourceDTO>>() {}
//	                );
//
//	        finalHttpStatusCode = apiResponse.getStatusCodeValue();
//	        externalApiResponse = apiResponse.getBody();
//
//	        if (apiResponse.getStatusCode() != HttpStatus.OK || apiResponse.getBody() == null) {
//		        System.out.println("PO Portal API failed or returned empty response");
//	            throw new PoportalApiException("PO Portal API failed or returned empty response");
//	        }
//	        
//	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//	        for (ProjectPoMappingWithResourceDTO projectDto : apiResponse.getBody()) {
//	        		            
//	        	 try {
//	                 Project project = projectRepository
//	                         .findByPoProjectId(projectDto.getProjectId());
//
//	                 if (project == null) {
//	                     exceptionDetailsForLog.append(
//	                             "Project not found | poProjectId=")
//	                             .append(projectDto.getProjectId())
//	                             .append(" || ");
//	                     continue;
//	                 }
//
//	                 if (projectDto.getProjectStartDate() == null &&
//	                		    projectDto.getProjectEndDate() == null) {
//
//	                		    exceptionDetailsForLog.append(
//	                		            "Start & End date missing | poProjectId=")
//	                		            .append(projectDto.getProjectId())
//	                		            .append(" || ");
//	                		    continue;
//	                		}
//
//	                		if (projectDto.getProjectStartDate() == null) {
//	                		    exceptionDetailsForLog.append(
//	                		            "Start date missing | poProjectId=")
//	                		            .append(projectDto.getProjectId())
//	                		            .append(" || ");
//	                		    continue;
//	                		}
//
//	                		if (projectDto.getProjectEndDate() == null) {
//	                		    exceptionDetailsForLog.append(
//	                		            "End date missing | poProjectId=")
//	                		            .append(projectDto.getProjectId())
//	                		            .append(" || ");
//	                		    continue;
//	                		}
//
//
//	                 project.setStartDate(
//	                         dateFormat.format(projectDto.getProjectStartDate()));
//	                 project.setEndDate(
//	                         dateFormat.format(projectDto.getProjectEndDate()));
//
//	                 projectRepository.save(project);
//	                 logger.info("saved in proj table");
//
//	             } catch (Exception ex) {
//	                 exceptionDetailsForLog.append(
//	                         "Update failed | poProjectId=")
//	                         .append(projectDto.getProjectId())
//	                         .append(", reason=")
//	                         .append(ex.getMessage())
//	                         .append(" || ");
//	             }
//	         }
//
//
//	        response.setServiceResponse(externalApiResponse);
//	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        response.setServiceMessage("Project SD ED update completed  ");
//
//	    } catch (PoportalApiException ex) {
//	        exceptionDetailsForLog.append(ex.getMessage());
//
//	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        response.setServiceMessage("Project SD ED update failed due to external API error.");
//	        response.setServiceError(ex.getMessage());
//
//	        throw ex; 
//
//	    } catch (Exception e) {
//	        exceptionDetailsForLog.append(e.getMessage());
//            response.setServiceResponse(externalApiResponse);      
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	        response.setServiceMessage("Project SD ED update completed with warnings.");
//	        return response;
//	    } finally {
//	        try {
//	            if (initialLog != null) {
//	                apiLogUtility.endLog(
//	                        initialLog.getId(),
//	                        getStartDateEndDateOfProjectsFromPO,
//	                        finalHttpStatusCode,
//	                        exceptionDetailsForLog.toString(),
//	                        httpRequest
//	                );
//	            }
//	        } catch (Exception logEx) {
//	            logger.error("Failed to end API log", logEx);
//	        }
//	        try {
//	            if (exceptionDetailsForLog.length() > 0) {
//
//	                String mailBody =
//	                        "<b>Trace ID:</b> " + traceId + "<br/><br/>"
//	                      + "<b>API:</b> updateSDEDOfproject <br/><br/>"
//	                      + "<b>Exception Details:</b><br/>"
//	                      + "<pre>" + exceptionDetailsForLog.toString() + "</pre>";
//
//	                mailService.sendMail(
//	                        "prarthana.lenka@apmosys.com",
//	                        "Project Start Date  end Date Update Issues | TraceId : " + traceId,
//	                        mailBody
//	                );
//	            }
//	        } catch (Exception mailEx) {
//	          
//	            mailEx.printStackTrace();
//	            logger.error("Failed to send exception mail", mailEx);
//	        }
//	    }
//
//	    return response;
//	}
	
	
	
	




	
        	  
        	
        	
 


}
	
        	  
        	
        	
 

	

