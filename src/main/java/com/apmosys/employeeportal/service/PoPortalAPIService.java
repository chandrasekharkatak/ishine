package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.UserSessionRepository;
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
	
	public ServiceResponse callGetFCLineItemDetails(Integer projectId) {
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
		String url = getFCLineItemDetailsURL + projectId;
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
			apiLogUtility.endLog(initialLog.getId(), finalHttpStatusCode, finalLogDetails, httpRequest);
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
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());	
			headers.setContentType(MediaType.valueOf(file.getContentType()));
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getOriginalFilename()).build());
			HttpEntity<FCProjectMilestoneDTO> dtoEntity = new HttpEntity<>(dto, headers);
		    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		    body.add("milestoneData", dtoEntity);
		    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes());
	        body.add("File",fileResource); 
	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			ResponseEntity<ServiceResponse> apiResponse = restTemplate.exchange(sendFileUrl, HttpMethod.PUT, requestEntity, ServiceResponse.class);

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
		} finally {
			if (initialLog != null && initialLog.getId() != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), finalHttpStatusCode, finalLogDetails, httpRequest);
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
				apiLogUtility.endLog(initialLog.getId(), finalHttpStatusCode, finalLogDetails, httpRequest);
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
        ResponseEntity<ResourceManagementDTO> apiResponse = null;
        try {
        	
        	
        	initialLog = apiLogUtility.startLog(traceId, "fetchPoPortalProjectById", "Ishine", getCurrentUserId(), httpRequest);

	        if (initialLog == null || initialLog.getId() == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceResponse("Critical Error: Could not initialize logging for the API call.");
	            return serviceResponse;
	        }
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("X-Trace-Id", traceId);
	        headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
	        HttpEntity<?> entity = new HttpEntity<>(headers);
	        
	        String url = poPortalProjectByIdURL + projectId;
            apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, ResourceManagementDTO.class);
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
        } finally {
        	if (initialLog != null) {
				String finalLogDetails = (exceptionDetailsForLog != null) ? exceptionDetailsForLog : null;
				apiLogUtility.endLog(initialLog.getId(), finalHttpStatusCode, finalLogDetails, httpRequest);
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
	
	
	
}
