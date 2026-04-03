package com.apmosys.employeeportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class EmployeePortalInterceptor implements HandlerInterceptor {

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	UserSessionRepository userSessionRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	@Value("${server.servlet.context-path:}")
	private String servletContextPath;

	private List<String> whitelistedApis;

	private static String normalizeContextPath(String cp) {
		if (cp == null || cp.isEmpty()) {
			return "";
		}
		String t = cp.trim();
		if (t.endsWith("/")) {
			return t.substring(0, t.length() - 1);
		}
		return t;
	}

	@PostConstruct
	public void buildGatewayWhitelist() {
		String cp = normalizeContextPath(servletContextPath);
		String[] apiSuffixes = new String[] {
				"/api/getAllEmployeeInfo",
				"/api/getAllDepartmentInfo",
				"/api/getAllJobRoleInfo",
				"/api/poProjectTimesheetSync",
				"/api/getTeamAndTimeSheetDetails",
				"/api/getEmployeeAndTimesheetDetails",
				"/api/getProjectDetailsByEmpIdAndDateRange",
				"/api/poCrudOperationsInIshineNew",
				"/api/renewPoInIshineNew",
				"/api/deletePoInIshineNew",
				"/api/linkPoInIshineNew",
				"/api/updateRmDetailsInPo",
				"/api/updateAddressInPos",
				"/api/sendTimesheetDetailsToShankh",
				"/api/getResourceCountFromProjectId",
				"/api/getResourceCountFromPoId",
				"/api/getAllApprovedPoWithTimesheet",
				"/api/getDocumentDataByDocIdForPO",
				"/api/getResourceCountListByPoprojectName",
				"/api/healthCheck",
				"/api/test"
		};
		whitelistedApis = new ArrayList<>();
		for (String suffix : apiSuffixes) {
			 whitelistedApis.add(cp + suffix);

			    if (cp == null || cp.isEmpty()) {
			        whitelistedApis.add("/shankhgateway" + suffix);
			    } else {
			        whitelistedApis.add("/shankhgateway" + cp + suffix);
			    }
		}
	}

	private final String poportalSessionKey = "Nguif3kxwSDzmojAtj6M93aJlfJqsAWj9blFug4JWkHsoQ2LYgWiApqDe1GZqmpV";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();
		if (!uri.contains("/api/")) {
			return true;
		}

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		boolean isWhitelisted = false;
		for (String api : whitelistedApis) {
			if (api.equals(uri) || uri.startsWith(api.replace("*", ""))) {
				isWhitelisted = true;
				break;
			}
		}

		if (isWhitelisted) {
			return true;
		}

		String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
			return false;
		}

		String[] parts = requestTokenHeader.substring(7).split("\\|");
		String sessionToken = parts[0];
		String empId = parts.length > 1 ? parts[1] : null;

		if (poportalSessionKey.equals(sessionToken)) {
			return true;
		}

		UserSession session = userSessionRepository.findBySessionKey(sessionToken);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired session");
			return false;
		}

		if (empId != null && !empId.isEmpty()) {
			if (!Objects.equals(session.getEmpId(), Long.valueOf(empId))) {
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

		if (jobRoleAccess == null) {
			return true;
		}

		boolean allowed = false;
		if (session.getFetaureIds() != null && !session.getFetaureIds().isEmpty()
				&& jobRoleAccess.featureIds().length > 0) {
			Set<Long> mappedFeatureIds = Arrays.stream(session.getFetaureIds().split(",")).map(String::trim)
					.filter(s -> !s.isEmpty()).map(Long::valueOf).collect(Collectors.toSet());

			for (long allowedFeatureId : jobRoleAccess.featureIds()) {
				if (mappedFeatureIds.contains(allowedFeatureId)) {
					allowed = true;
					break;
				}
			}
		}

		if (!allowed) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for this subfeature");
			return false;
		}

		return true;
	}
}
