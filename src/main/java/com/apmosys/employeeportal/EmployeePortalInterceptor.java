package com.apmosys.employeeportal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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
			"/api/checkEmailWhenForgotPassword",
			"/api/checkOTPWhenForgotPassword",
			"/api/resendOTP",
			"/api/checkUserSession",
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
			    "/browser/perfData/webVitals",
			    "/employeeportal/api/getResourceCountByPoprojectId",
			    "/api/getResourceCountByPoprojectId"
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
        if (!request.getRequestURI().contains("/employeeportal/api/") && !request.getRequestURI().contains("/api/")) {
            return true;
        }

        // ✅ Allow preflight (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ✅ Allow whitelisted APIs
        for (String api : WHITELISTED_APIS) {
            if (api.equals(request.getRequestURI())) {
                return true;
            }
        }

        // ✅ Get Authorization header
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        String sessionToken = requestTokenHeader.substring(7);

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

        // ✅ Extract Job Role ID
//        Long jobRoleId = employeeRepository.getJobRoleId(session.getEmpId());
//        if (jobRoleId == null) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Job role not found for this employee");
//            return false;
//        }

        // ✅ Fetch subfeatures mapped to this job role
//        List<RoleFeatureMap> featureMapped = roleFeatureMapRepository.findByJobRoleId(jobRoleId);

        // ✅ If handler is not a controller method, skip
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
        if (session.getFetaureIds() != null && !session.getFetaureIds().isEmpty() && jobRoleAccess.subFeatureIds().length > 0) {
            Set<Long> mappedSubFeatureIds = Arrays.stream(session.getFetaureIds().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .collect(Collectors.toSet());

            for (long allowedSubFeatureId : jobRoleAccess.subFeatureIds()) {
                if (mappedSubFeatureIds.contains(allowedSubFeatureId)) {
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
