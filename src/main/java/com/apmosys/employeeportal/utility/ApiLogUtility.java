package com.apmosys.employeeportal.utility;

import java.time.LocalDateTime;
import java.util.Optional;

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
	
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    public ApiLog startLog(String traceId,String endPoint, String portal, Long userId, HttpServletRequest request) {
        ApiLog log = new ApiLog();
        try {
            log.setTraceId(traceId);
            log.setApiEndpointName(endPoint);
            log.setPortal(portal);
            log.setInitiatedFromApi(request.getRequestURI());
            log.setApiMethodType(request.getMethod());
            log.setRequestTimestamp(LocalDateTime.now());
            log.setRequestedByUserId(userId);
            log.setApiStatus(STATUS_IN_PROGRESS);

            return apilogrepository.save(log);
        } catch (Exception e) {
            return null;
        }
    }

    public ApiLog endLog(Long id, int httpStatusCode, String exceptionDetails,HttpServletRequest request) {
    	 try {
            Optional<ApiLog> optionalLog = apilogrepository.findById(id);

             if (optionalLog.isPresent()) {
                 ApiLog log = optionalLog.get();
                 
                 
                 log.setApiStatusCode(httpStatusCode);
                 log.setEndpointUrl(request.getRequestURL().toString());
                 log.setResponseTimestamp(LocalDateTime.now());

                 if (exceptionDetails != null) {
                     String details = exceptionDetails.length() > 2000
                             ? exceptionDetails.substring(0, 1997) + "..."
                             : exceptionDetails;
                     log.setExceptionDetails(details);
                     log.setApiStatus(STATUS_FAILURE);
                 }
                 else {
                	 log.setApiStatus(STATUS_SUCCESS);
                 }
                 
                 return apilogrepository.save(log);
             } else {
            	 System.err.println("Could not find ApiLog ");
                 return null;
             }

         } catch (Exception e) {
        	 System.err.println("An unexpected error occurred while ending log ");
             e.printStackTrace();
             return null;
         }
}
}


