package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.FCLineItemDTO;
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
	
	
	
	private Long getCurrentUserId() {
		String sessionToken = httpRequest.getHeader("Authorization");
		if (sessionToken != null) {
			sessionToken = sessionToken.substring(7);
		}
		UserSession existingUserSession = userSessionRepo.findBySessionKey(sessionToken);
		return existingUserSession != null ? existingUserSession.getEmpId() : null;
	}
}
