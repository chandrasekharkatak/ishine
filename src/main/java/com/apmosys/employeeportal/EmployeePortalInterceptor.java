package com.apmosys.employeeportal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.service.AuthenticationService;

@Component
public class EmployeePortalInterceptor implements HandlerInterceptor{
	
	@Autowired
	AuthenticationService authenticationService;
	
	
	private final List<String> WHITELISTED_APIS = Arrays.asList(
			"/employeeportal/api/authenticateUser",
			"/employeeportal/api/authenticateUserWithOTP",
			"/employeeportal/api/checkEmailWhenForgotPassword",
			"/employeeportal/api/checkOTPWhenForgotPassword",
			"/employeeportal/api/resendOTP",
			"/employeeportal/api/checkUserSession",
			"/employeeportal/api/logoutUser",
			"/employeeportal/api/downloadFileFromQrCode",
			"/employeeportal/api/downloadFileFromQrCode/textImg",
			"/employeeportal/api/downloadFileFromQrCode/pdf",
//			"/employeeportal/api/getAllEmployees",
//			"/api/getAllEmployees",
			"/employeeportal/api/poCrudOperationsInIshine",
			"/employeeportal/api/poProjectTimesheetSync",
			"/employeeportal/api/getEmployeeAndTimesheetDetails",
			"/employeeportal/api/getProjectDetailsByEmpIdAndDateRange",
			"/employeeportal/api/getTeamAndTimeSheetDetails",
			"/employeeportal/api/getAllEmployeeInfo",
			"/employeeportal/api/getAllDepartmentInfo",
			"/employeeportal/api/getAllJobRoleInfo",
			"/employeeportal/api/handleTeamsAsPerLinkedPo",
			"/employeeportal/api/getProjectStatusByPoProjectId",
			"/employeeportal/api/getMilestoneById",
			"/employeeportal/api/syncPoProjectAndTeam",
			"/employeeportal/api/getResourceCountFromProjectId",
			"/employeeportal/api/sendTimesheetDetailsToShankh",
			"/employeeportal/api/getActiveTeamAndTimeSheetWithForRm",
			 "/employeeportal/api/getAllApprovedPoWithTimesheet",
			 "/employeeportal/api/getDocumentDataByDocIdForPO",
			 "/employeeportal/api/checkActiveAndPendingEmployeeMappingWithResourceOverViewId",
			 "/employeeportal/api/getAllMilestoneToBeExpired",
			 "/browser/perfData",
			    "/browser/errorLog",
			    "/browser/errorLogs",
			    "/v3/segment",
			    "/v3/segments",
			    "/browser/perfData/webVitals"
			);
	
//	private final List<String> SKYWALKING_PROXIED_PATHS = Arrays.asList(
//		    "/browser/perfData",
//		    "/browser/errorLog",
//		    "/browser/errorLogs",
//		    "/v3/segment",
//		    "/v3/segments"
//		);
			
	private final String POPORTAL_SESSION_KEY = "Nguif3kxwSDzmojAtj6M93aJlfJqsAWj9blFug4JWkHsoQ2LYgWiApqDe1GZqmpV"; 
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		// For Application Start-up and assets
		if(!request.getRequestURI().contains("/employeeportal/api/")) {
			return true;
		}

		// For Pre-flight methods
		if("OPTIONS".equals(request.getMethod())) {
			return true;
		}
		
		// white-listed APIs
		for (String api : WHITELISTED_APIS){
			if (api.equals(request.getRequestURI())) {
					return true;
				}
		};
		
//		for (String proxyPath : SKYWALKING_PROXIED_PATHS) {
//		    if (request.getRequestURI().startsWith(proxyPath)) {
//		        return true;
//		    }
//		}	
		
		final String requestTokenHeader = request.getHeader("Authorization");

		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			
			final String SESSION_TOKEN = requestTokenHeader.substring(7);
			
			// WHITELISTING APIs for PoPortal
			if(POPORTAL_SESSION_KEY.equals(SESSION_TOKEN)) {
				return true;
			}
			
			boolean isUserAuthenticated = 
					authenticationService.checkUserToken(SESSION_TOKEN);

			if (isUserAuthenticated) {
				return true;
			}else {
				response.setStatus(401);
				return false;
			}

		} else {
			response.setStatus(401);
			return false;

		}

	}

	
}
