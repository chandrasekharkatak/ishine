package com.apmosys.employeeportal.utility;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.repository.ApiLogRepository;
import com.auth0.jwt.interfaces.DecodedJWT;


@Service
public class ApiLogUtility {

	@Autowired
	private ApiLogRepository apilogrepository;
	
    @Autowired
    private PoPortalAPIAuthenticationJWTUtility jwtUtility; 
	
	public void setLog(String traceId,String endPoint,String portal,String initApi,Long userId,String apiStatus,HttpServletRequest request,
			HttpServletResponse response, String ex)
	{
		
		ApiLog log = new ApiLog();
	    
	    try {
	        log.setTraceId(traceId);
	        log.setApiEndpointName(endPoint);
	        log.setPortal(portal);
	        log.setInitiatedFromApi(initApi);
	        log.setApiMethodType(request.getMethod());
	        log.setEndpointUrl(request.getRequestURL().toString());
	        log.setRequestTimestamp(LocalDateTime.now());
	        
	        log.setRequestedByUserId(userId); 

	        String authHeader = request.getHeader("Authorization");
	        if (authHeader != null && authHeader.startsWith(portal)) {
	            String token = authHeader.substring(authHeader.indexOf(" ") + 1);
	            DecodedJWT decodedJWT = jwtUtility.validateToken(token);
	            String userIdStr = decodedJWT.getSubject();
	            if (userIdStr != null) {
	                log.setRequestedByUserId(Long.parseLong(userIdStr));
	            }
	        }
	        
	        if (ex != null) {
	            log.setApiStatus("FAILURE");
	            String exceptionDetails = ex.length() > 2000 ? ex.substring(0, 1997) + "..." : ex;
	            log.setExceptionDetails(exceptionDetails);
	        } else {
	            log.setApiStatus("SUCCESS");
	        }

	    } catch (Exception e) {
	        log.setApiStatus("FAILURE");
	        
	        String internalExceptionMessage = "Error during log creation: " + e.getMessage();
	        log.setExceptionDetails(internalExceptionMessage.length() > 2000 
	            ? internalExceptionMessage.substring(0, 1997) + "..." 
	            : internalExceptionMessage);

	   	        if(log.getRequestedByUserId() == null){
	            log.setRequestedByUserId(0L);
	        }

	    } 
	}
}


