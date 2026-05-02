package com.apmosys.employeeportal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.service.AuthenticationService;

@Component
public class EmployeePortalInterceptor implements HandlerInterceptor{
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	UserSessionRepository userSessionRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	
	private final List<String> WHITELISTED_APIS = Arrays.asList(
			"/api/authenticateUser",
			"/api/authenticateUserWithOTP",
			"/api/auth/po/session-login",
			"/api/auth/po/verifyTokenOfPoPortalForDirectAccess",
			"/api/auth/po/session-logout",
			"/api/checkEmailWhenForgotPassword",
			"/api/checkOTPWhenForgotPassword",
			"/api/resendOTP",
			"/api/checkUserSession",
			"/api/runTheHolidayCron",
			
			"/employeeportalapp/api/authenticateUser",
			"/employeeportalapp/api/authenticateUserWithOTP",
			"/employeeportalapp/api/auth/po/session-login",
			"/employeeportalapp/api/auth/po/verifyTokenOfPoPortalForDirectAccess",
			"/employeeportalapp/api/auth/po/session-logout",
			"/employeeportalapp/api/checkEmailWhenForgotPassword",
			"/employeeportalapp/api/checkOTPWhenForgotPassword",
			"/employeeportalapp/api/resendOTP",
			"/employeeportalapp/api/checkUserSession",
			"/api/timesheetUploadTest/bulk-migrate",
			"/api/timesheetUploadTest/bulk-migrate-uat",
			
			"/employeeportal/api/authenticateUser",
			"/employeeportal/api/authenticateUserWithOTP",
			"/employeeportal/api/auth/po/session-login",
			"/employeeportal/api/auth/po/verifyTokenOfPoPortalForDirectAccess",
			"/employeeportal/api/auth/po/session-logout",
			"/employeeportal/api/checkEmailWhenForgotPassword",
			"/employeeportal/api/checkOTPWhenForgotPassword",
			"/employeeportal/api/resendOTP",
			"/employeeportal/api/checkUserSession",
			"/employeeportal/api/logoutUser",
			"/employeeportal/api/downloadFileFromQrCode",
			"/employeeportal/api/downloadFileFromQrCode/textImg",
			"/employeeportal/api/downloadFileFromQrCode/pdf",
			"/employeeportal/api/renewPoInIshineNew",
			"/employeeportal/api/deletePoInIshineNew",
			"/employeeportal/api/linkPoInIshineNew",
			"/api/linkPoInIshineNew",
			"/employeeportal/api/poCrudOperationsInIshine",
			"/employeeportal/api/poCrudOperationsInIshineNew",
			"/api/poCrudOperationsInIshine",
			"/api/poCrudOperationsInIshineNew",
			"/api/renewPoInIshineNew",
			"/api/deletePoInIshineNew",
			"/api/updateRmDetailsInPo",
			"/api/updateAddressInPos",
			"/api/getTeamAndTimeSheetDetails",
			"/employeeportal/api/updateRmDetailsInPo",
			"/employeeportal/api/updateAddressInPos",
			"/employeeportal/api/poProjectTimesheetSync",
			"/api/poProjectTimesheetSync",
			"/employeeportal/api/getEmployeeAndTimesheetDetails",
			"/api/getEmployeeAndTimesheetDetails",
			"/employeeportal/api/getProjectDetailsByEmpIdAndDateRange",
			"/api/getProjectDetailsByEmpIdAndDateRange",
			"/employeeportal/api/getTeamAndTimeSheetDetails",
			"/employeeportal/api/getAllEmployeeInfo",
			"/employeeportal/api/getAllDepartmentInfo",
			"/employeeportal/api/getAllJobRoleInfo",
			"/api/getAllEmployeeInfo",
			"/api/getAllDepartmentInfo",
			"/api/getAllJobRoleInfo",
			"/employeeportal/api/handleTeamsAsPerLinkedPo",
			"/employeeportal/api/getProjectStatusByPoProjectId",
			"/employeeportal/api/getMilestoneById",
			"/employeeportal/api/syncPoProjectAndTeam",
			"/employeeportal/api/getResourceCountFromProjectId",
			"/employeeportal/api/sendTimesheetDetailsToShankh",
			"/api/sendTimesheetDetailsToShankh",
			"/employeeportal/api/getActiveTeamAndTimeSheetWithForRm",
			 "/employeeportal/api/getAllApprovedPoWithTimesheet",
			 "/employeeportal/api/getDocumentDataByDocIdForPO",
			 "/api/getDocumentDataByDocIdForPO",
			 "/employeeportal/api/checkActiveAndPendingEmployeeMappingWithResourceOverViewId",
			 "/employeeportal/api/getAllMilestoneToBeExpired",
			 "/browser/perfData",
			    "/browser/errorLog",
			    "/browser/errorLogs",
			    "/v3/segment",
			    "/v3/segments",
			    "/browser/perfData/webVitals",
			    "/employeeportal/api/getResourceCountByPoprojectId",
			    "/api/getResourceCountByPoprojectId",
			    "/api/getResourceCountListByPoprojectName",
			    "/employeeportal/api/getResourceCountListByPoprojectName",
				"/employeeportal/api/runTheHolidayCron",
			// Training APIs - Always allowed even when locked
			"/api/training/getPendingTraining",
			"/api/training/getUserTrainings",
			"/api/training/submitConsent",
			"/api/training/skipTraining",
			"/api/training/getLockStatus",
			"/api/training/downloadContent",
			"/api/training/checkTrainingFrequency",
			"/employeeportal/api/training/getPendingTraining",
			"/employeeportal/api/training/getUserTrainings",
			"/employeeportal/api/training/submitConsent",
			"/employeeportal/api/training/skipTraining",
			"/employeeportal/api/training/getLockStatus",
			"/employeeportal/api/training/downloadContent",
			"/employeeportal/api/training/checkTrainingFrequency",
			"/employeeportal/api/runTheHolidayCron",
			"/api/ishineToPoEmpDetails",
			"/employeeportal/api/ishineToPoEmpDetails",
			"/employeeportal/api/oneTimeUpdatePoClientId",
			"/api/oneTimeUpdatePoClientId",
			"/api/healthCheck",
			"/employeeportal/api/healthCheck",
			"/api/getResourceCountFromPoId",
			"/api/getAllApprovedPoWithTimesheet",
			"/employeeportal/api/getResourceCountFromPoId",
			"/api/poCrudOperationsInIshine",
			"/api/getExtensionDocumentById",
			"/employeeportal/api/getEmployeesWorkingInProjects",
			"/api/getEmployeesWorkingInProjects"

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

        // ✅ Skip static or non-API routes
        if (!request.getRequestURI().contains("/employeeportal/api/") && !request.getRequestURI().contains("/api/") && !request.getRequestURI().contains("/employeeportal/api/")) {
            return true;
        }

        // ✅ Allow preflight (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ✅ Allow whitelisted APIs
        boolean isWhitelisted = false;
        for (String api : WHITELISTED_APIS) {
            if (api.equals(request.getRequestURI()) || request.getRequestURI().startsWith(api.replace("*", ""))) {
                isWhitelisted = true;
                break;
            }
        }
        
        if (isWhitelisted) {
            return true;
        }

        // ✅ Get Authorization header
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }
        
        String[] parts = requestTokenHeader.substring(7).split("\\|"); // 7 removes "Bearer "
        String sessionToken = parts[0];
        String empId = parts.length > 1 ? parts[1] : null;
//        String sessionToken = requestTokenHeader.substring(7);

        // ✅ Allow PoPortal system key
        if (POPORTAL_SESSION_KEY.equals(sessionToken)) {
            return true;
        }

        // ✅ Validate user session from DB
        UserSession session = userSessionRepository.findBySessionKey(sessionToken);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired session");
            return false;
        } 
        
        if(empId != null && !empId.isEmpty()) {
        	if(!Objects.equals(session.getEmpId(),Long.valueOf(empId))) {
				userSessionRepository.delete(session);
	            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	            return false;
        	}
        	UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(empId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }


        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        JobRoleAccess jobRoleAccess = method.getMethodAnnotation(JobRoleAccess.class);

        // ✅ If no annotation, allow
        if (jobRoleAccess == null) {
            return true;
        }

        // ✅ Authorization check
        boolean allowed = false;
        if (session.getFetaureIds() != null && !session.getFetaureIds().isEmpty() && jobRoleAccess.featureIds().length > 0) {
            Set<Long> mappedFeatureIds = Arrays.stream(session.getFetaureIds().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .collect(Collectors.toSet());

            for (long allowedFeatureId : jobRoleAccess.featureIds()) {
                if (mappedFeatureIds.contains(allowedFeatureId)) {
                    allowed = true;
                    break;
                }
            }
        }

        // ✅ Deny access if no match
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for this subfeature");
            return false;
        }

        return true;
    }

	
}
