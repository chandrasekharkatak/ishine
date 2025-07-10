package com.apmosys.employeeportal.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.MultiValueMap;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
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
	
	@Value("{poPortal.api.sendMilestoneFile}")
	private String sendFileUrl;
	
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
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getOriginalFilename()).build());
			headers.setContentType(MediaType.valueOf(file.getContentType()));
			Resource fileResource = (Resource) new ByteArrayResource(file.getBytes());
			HttpEntity<Resource> requestEntity = new HttpEntity<>(fileResource, headers);
			String finalsendUrl = UriComponentsBuilder.fromHttpUrl(sendFileUrl).pathSegment(String.valueOf(dto.getId())).toUriString();
			ResponseEntity<ServiceResponse> apiResponse = restTemplate.exchange(finalsendUrl, HttpMethod.PUT, requestEntity, ServiceResponse.class);

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
	
	private Long getCurrentUserId() {
		String sessionToken = httpRequest.getHeader("Authorization");
		if (sessionToken != null) {
			sessionToken = sessionToken.substring(7);
		}
		UserSession existingUserSession = userSessionRepo.findBySessionKey(sessionToken);
		return existingUserSession != null ? existingUserSession.getEmpId() : null;
	}
	
	
	
}
