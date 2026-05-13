package com.apmosys.employeeportal.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.text.SimpleDateFormat;
import com.apmosys.employeeportal.dto.AppreciationEventDTO;	
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.OtpRateLimitConfig;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LMSDTO;
import com.apmosys.employeeportal.dto.LMSEmailSend;
import com.apmosys.employeeportal.dto.LMSRedirect;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoSessionLoginRequestDTO;
import com.apmosys.employeeportal.dto.PoVerifyDirectAccessRequestDTO;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.PoSessionAccessLog;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.FeatureMasterRepository;
import com.apmosys.employeeportal.repository.PoSessionAccessLogRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthenticationService {

	/** Logged when Po calls session-login before any deep link exists. */
	private static final String PO_LOG_DEEP_LINK_TOKEN_FETCH = "(no-direct-link-yet)";
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	TabMasterService tabMasterService;
	
	@Autowired	
	AppreciationService appreciationService;

	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	OtpRateLimitConfig otpConfig;
	
	@Autowired
	FeatureMasterRepository featureMasterRepository;

	@Value("${portal.static.otp}")
	private String portalStaticOtp;

	@Value("${idle.session.timeout}")
	private Integer sessionTimeout;
	
	private Integer count = 0;
	private Integer invalidAccessAttempt=0;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private UserSessionRepository userSessionRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	@Autowired
	private PoSessionAccessLogRepository poSessionAccessLogRepository;

	@Autowired
	private ApiLogUtility apiLogUtility;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;
	
	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSize;
	
	@Value("${timesheet.backdated.days}")
	private Integer timesheetBackDatedDays;

	@Value("${compoff.lock.days}")
	private Integer compOffLockDays;
	
	@Value("${leave.backdated.lock.days}")
	private Integer leaveBackdatedLockDays;

	@Value("${leave.future.lock.days}")
	private Integer leaveFutureLockDays;
	
	@Value("${revoke.reportee.leave.validity}")
	private Integer revokeReporteeLeaveValidity;

	@Value("${otp.timeout.period}")
	private Long otpTimeoutPeriod;
	
	@Value("${poPortal.api.allProjects}")
	private String poPortalAllProjectApi;
	
	@Value("${valid.attempt}")
	private Integer failedAttempt;
	
//	private static ConcurrentHashMap<Long, String> userSessionList = new ConcurrentHashMap<Long, String>();
	public static ConcurrentHashMap<Long, LogDTO> userLogInfoList = new ConcurrentHashMap<Long, LogDTO>();

	public ServiceResponse authenticateUser(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Sign In");
		apiLogInfo.setApiUrl("/api/authenticateUser");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Email : "+employeedto.getEmail());

		try {	
				Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
				if (employee != null) {
//					boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());
					UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
					boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
					
					logBuilder.append(", InvalidAccessAttempt : "+employee.getInvalidAccessAttempt()+ ", isUserLoggedIn : "+isUserLoggedIn);
					if(employee.getInvalidAccessAttempt()>failedAttempt) {
						
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Account Blocked !!");
						
						apiLogInfo.setApiResponse("Account Blocked !!.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						
					} else if (!employee.getEmploymentstatus().equals("InActive")) {

						String dbPassword = EncryptDecrypt.decrypt(employee.getPassword());
						String dtoPassword = EncryptDecrypt.decrypt(employeedto.getPassword());

							if (dbPassword.equals(dtoPassword)) {
								if (!isUserLoggedIn) {
									
									Random random = new Random();
									int otp = random.nextInt(999999 - 100000)
											+ 100000; /* Random number will be generated between 1000 and 9999 */
//									employee.setOtp(otp);
//									employee.setOtp(EncryptDecrypt.encryptOtp(String.valueOf(otp)));
									
									String plainOtp = String.valueOf(otp);

									// Save encrypted OTP in DB
									employee.setOtp(EncryptDecrypt.encryptOtp(plainOtp));
									
									mailService.sendMail(employeedto.getEmail(), "Regarding otp",
											"Please find your otp " + plainOtp);
									employee.setInvalidAccessAttempt(0);
									employee.setOtpUpdatedOn(LocalDateTime.now());

									employeeRepository.save(employee);

									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Valid Credentials. OTP sent to email.");
									
									apiLogInfo.setApiResponse("Valid Credentials. OTP sent to email.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
									
								} else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL_1);
									response.setServiceResponse(
											"User already logged in. Do you want to logout of existing session ?");
									apiLogInfo.setApiResponse("User already logged in. Do you want to logout of existing session ?");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);								}
							} else {

								count = employee.getInvalidAccessAttempt() + 1;
								System.out.println("counter :" + count);
								employee.setInvalidAccessAttempt(count);
								employeeRepository.save(employee);

								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Invalid Credentials.");
								
								apiLogInfo.setApiResponse("Invalid Credentials.");			
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

							}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("InActive User");
						
						apiLogInfo.setApiResponse("InActive User");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid Credentials.");
					
					apiLogInfo.setApiResponse("Invalid Credentials.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
				
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
			}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//		
////		String inputEmail = employeedto.getEmail();
////		if (!inputEmail.contains("@")) {
////		    employeedto.setEmail(inputEmail + "@apmosysemp.com");
////		}
//		/* FOR SESSION LOG INFO */
//		LogDTO logInfo = new LogDTO();
//		logInfo.setEmpId(employeedto.getEmpId());
//		logInfo.setFeatureName("Login");
//		/*FOR API LOG */
//		LogDTO apiLogInfo = new LogDTO();
//		logInfo.setFeatureName("Login");
//		apiLogInfo.setSubFeatureName("Confirm OTP");
//		apiLogInfo.setApiUrl("/api/authenticateUserWithOTP");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("EmpId : "+employeedto.getEmpId()+", Email : "+ employeedto.getEmail()+", Otp : "+ employeedto.getOtp());
//		
//		try {
//			
//			String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
//			String encSessionString  = EncryptDecrypt.encrypt(sessionString);
//			
//			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
//
//			Long otpDiff = ChronoUnit.MINUTES.between(employee.getOtpUpdatedOn(), LocalDateTime.now());
//			
//			if(otpDiff < otpTimeoutPeriod) {
//				if (employeedto.getOtp().toString().equals(employee.getOtp().toString()) || (employeedto.getOtp().toString()).equals(portalStaticOtp.toString())) {
//					ServiceResponse serviceResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId());
//					EmployeeDTO currentEmployeeDto = employeeService.getEmployeeInfoOnLogin(employeedto.getEmail());
//					AppreciationEventDTO currentEventDto = appreciationService.getAppreciationEventInfo();	
//					
//					System.err.println("Enable appreciation ::   "+ currentEventDto);
//
//					currentEmployeeDto.setTimesheetBackDatedDays(timesheetBackDatedDays);
//					currentEmployeeDto.setCompOffLockDays(compOffLockDays);
//					currentEmployeeDto.setLeaveBackdatedLockDays(leaveBackdatedLockDays);
//					currentEmployeeDto.setLeaveFuturedatedLockDays(leaveFutureLockDays);
//					currentEmployeeDto.setRevokeReporteeLeaveValidity(revokeReporteeLeaveValidity);
//					currentEmployeeDto.setPoPortalAllProjectApi(poPortalAllProjectApi);
//					
//					logInfo.setLoginTime(df.format(new Date()));
////					boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());
//	
//					UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
//					boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
//					
//					if (isUserLoggedIn) {
//						userSessionRepository.deleteById(existingUserSession.getUserSessionId());
//					}
//					
//					System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//					System.out.println(" ");
//					System.out.println(" ");
//					System.out.println("currentEmployeeDto  "+currentEmployeeDto);
////					userSessionList.put(employee.getEmpId(), sessionString);
//					UserSession newSession  = new UserSession();
//					newSession.setEmpId(employee.getEmpId());
//					newSession.setLoginTime(LocalDateTime.now());
//					newSession.setLastCheckTime(LocalDateTime.now());
//					newSession.setSessionKey(encSessionString);
//					
//					userSessionRepository.save(newSession);
//					
//					userLogInfoList.put(employee.getEmpId(), logInfo);
//
//					Object[] object = new Object[9];
//					object[0] = currentEmployeeDto;
//					object[1] = serviceResponse.getServiceResponse();
//					object[2] = encSessionString;
//					object[3] = sessionTimeout;
//					object[4] = maxFileSize.replace("MB","");
//					object[5] = maxRequestSize.replace("MB","");
//					object[6] = logInfo;
//					object[7] = currentEventDto;
//					
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(object);
//					response.setServiceMessage("OTP validated successfully. User Log in success.");
//					
//					apiLogInfo.setApiResponse("OTP validated successfully. User Log in success.");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("OTP validation failed. Please try again.");
//					
//					apiLogInfo.setApiResponse("OTP validation failed. Please try again.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("OTP Expired, Please re-send new otp.");
//				
//				apiLogInfo.setApiResponse("OTP Expired, Please re-send new otp.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}


	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();
	    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    String encryptedOtpForLog = EncryptDecrypt.encryptOtp(employeedto.getOtp());
	    LogDTO logInfo = new LogDTO();
	    logInfo.setEmpId(employeedto.getEmpId());
	    logInfo.setFeatureName("Login");

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setFeatureName("Login");
	    apiLogInfo.setSubFeatureName("Confirm OTP");
	    apiLogInfo.setApiUrl("/api/authenticateUserWithOTP");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("EmpId : ").append(employeedto.getEmpId())
	              .append(", Email : ").append(employeedto.getEmail())
	              .append(", Otp : ").append(encryptedOtpForLog);

	    try {
	        Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee not found.");
	            return response;
	        }

	        if (employee.getOtpCooldownUntil() != null &&
	            employee.getOtpCooldownUntil().isAfter(LocalDateTime.now())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Account locked due to multiple failures. Try again at :-" 
	                    + employee.getOtpCooldownUntil());
	            apiLogInfo.setApiResponse("Account locked due to multiple failures");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        Long otpDiff = ChronoUnit.MINUTES.between(employee.getOtpUpdatedOn(), LocalDateTime.now());
	        if (otpDiff > otpTimeoutPeriod) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("OTP Expired, Please re-send new OTP.");
	            apiLogInfo.setApiResponse("OTP expired");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

//	        String encryptedInputOtp = EncryptDecrypt.encryptOtp(employeedto.getOtp());
	        String plainOtp = employeedto.getOtp();
//	        String encryptedInputOtp = EncryptDecrypt.encryptOtp(plainOtp);

	        boolean isValidOtp = plainOtp.equals(employee.getOtp()) 
	                             || employeedto.getOtp().equals(portalStaticOtp.toString());

	        if (isValidOtp) {
	            employee.setOtpFailedAttempts(0);
	            employee.setOtpCooldownUntil(null);
	            employeeRepository.save(employee);

	            String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
	            String encSessionString = EncryptDecrypt.encrypt(sessionString);

	            ServiceResponse serviceResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId(), employee.getEmpId());
	            EmployeeDTO currentEmployeeDto = employeeService.getEmployeeInfoOnLogin(employeedto.getEmail());
	            AppreciationEventDTO currentEventDto = appreciationService.getAppreciationEventInfo();

	            currentEmployeeDto.setTimesheetBackDatedDays(timesheetBackDatedDays);
	            currentEmployeeDto.setCompOffLockDays(compOffLockDays);
	            currentEmployeeDto.setLeaveBackdatedLockDays(leaveBackdatedLockDays);
	            currentEmployeeDto.setLeaveFuturedatedLockDays(leaveFutureLockDays);
	            currentEmployeeDto.setRevokeReporteeLeaveValidity(revokeReporteeLeaveValidity);
//	            currentEmployeeDto.setPoPortalAllProjectApi(poPortalAllProjectApi);

	            logInfo.setLoginTime(df.format(new Date()));

	            UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
	            if (existingUserSession != null) {
	                userSessionRepository.deleteById(existingUserSession.getUserSessionId());
	            }
	            List<Long> featureIds = featureMasterRepository.getAllFeatureIdsByJobRoleIds(employee.getJobRoleId());
	            
	            String subFeatureIds = featureIds.stream()
//	            	    .map(RoleFeatureMap::getSubFeatureMasterId)
	            	    .map(String::valueOf)
	            	    .collect(Collectors.joining(","));
	            
	            UserSession newSession = new UserSession();
	            newSession.setEmpId(employee.getEmpId());
	            newSession.setLoginTime(LocalDateTime.now());
	            newSession.setLastCheckTime(LocalDateTime.now());
	            newSession.setSessionKey(encSessionString);
	            newSession.setFetaureIds(subFeatureIds);
	            userSessionRepository.save(newSession);

	            userLogInfoList.put(employee.getEmpId(), logInfo);

	            Object[] object = new Object[9];
	            object[0] = currentEmployeeDto;
	            object[1] = serviceResponse.getServiceResponse();
	            object[2] = encSessionString;
	            object[3] = sessionTimeout;
	            object[4] = maxFileSize.replace("MB", "");
	            object[5] = maxRequestSize.replace("MB", "");
	            object[6] = logInfo;
	            object[7] = currentEventDto;

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(object);
	            response.setServiceMessage("OTP validated successfully. User Log in success.");

	            apiLogInfo.setApiResponse("OTP validated successfully. User Log in success.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            return response;
	        }

	        int failed = (employee.getOtpFailedAttempts() == null) ? 0 : employee.getOtpFailedAttempts();
	        failed++;
	        employee.setOtpFailedAttempts(failed);

	        if (failed >= otpConfig.getMaxFailedAttempts()) {
	            employee.setOtpCooldownUntil(LocalDateTime.now().plusMinutes(otpConfig.getCooldownMinutes()));
	        }
	        employeeRepository.save(employee);

	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("OTP validation failed. Please try again.");

	        apiLogInfo.setApiResponse("OTP validation failed");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}



	public ServiceResponse logoutUser(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/logoutUserlogoutUser");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+employeedto.getEmpId());
		
		try {

//			boolean isUserLoggedIn = userSessionList.containsKey(employeedto.getEmpId());
			UserSession existingUserSession = userSessionRepository.findByEmpId(employeedto.getEmpId());
			boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
			boolean isUserLogInfoAvailable = userLogInfoList.containsKey(employeedto.getEmpId());
			
			
			if (isUserLoggedIn) {
				
				if(isUserLogInfoAvailable) {
					LogDTO sessionLogInfo = userLogInfoList.get(employeedto.getEmpId());
					sessionLogInfo.setLogoutTime(df.format(new Date()));
					
					apiLogInfo.setApiResponse("Session destroyed. User Logout successfull.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
					apiLogInfo.setApiRequest(logBuilder.toString());
					logService.logMyInfo(httpRequest, apiLogInfo);
					
					userLogInfoList.remove(employeedto.getEmpId());
				}
				
//				userSessionList.remove(employeedto.getEmpId());
				userSessionRepository.deleteById(existingUserSession.getUserSessionId());
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Session destroyed. User Logout successfull");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Session already destroyed");
				
				apiLogInfo.setApiResponse("Session already destroyed.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
	}

	public ServiceResponse checkUserSession(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkUserSession");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+employeedto.getEmpId());
		try {
//			String sessionString = userSessionList.get(employeedto.getEmpId());
			
			UserSession existingUserSession = userSessionRepository.findByEmpId(employeedto.getEmpId());
			boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
//			System.out.println(existingUserSession.getSessionKey());
//			System.out.println(employeedto.getSessionString());
			if (isUserLoggedIn) {
				if (existingUserSession.getSessionKey().equals(employeedto.getSessionString())) {
					
					existingUserSession.setLastCheckTime(LocalDateTime.now());
					userSessionRepository.save(existingUserSession);
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Session exists.");
					apiLogInfo.setApiResponse("Session exists.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Sessionstring is different. Logging out of application.");
					apiLogInfo.setApiResponse("Sessionstring is different. Logging out of application.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Session not found. Logging out of application.");
				apiLogInfo.setApiResponse("Session not found. Logging out of application.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse checkEmailWhenForgotPassword(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setFeatureName("Login");
	    apiLogInfo.setSubFeatureName("Forgot Password - Send OTP");
	    apiLogInfo.setApiUrl("/api/checkEmailWhenForgotPassword");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Email : ").append(employeedto.getEmail());

	    try {
	        Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Enter valid credentials.");

	            apiLogInfo.setApiResponse("Enter valid credentials.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        logBuilder.append(", Employee IsNew : ").append(employee.getIsNew());

	        // Restriction for new users
	        if ("true".equalsIgnoreCase(employee.getIsNew())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Forgot Password feature is not for New User.");

	            apiLogInfo.setApiResponse("Forgot Password feature is not for New User.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        // Restriction for inactive or blocked accounts
	        if ("InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("This user is not authorized for this activity.");
	            return response;
	        }
	        if (employee.getInvalidAccessAttempt() > failedAttempt) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("This user is blocked due to multiple failed attempts. Please contact HR Team.");
	            return response;
	        }

	        if (employee.getOtpCooldownUntil() != null &&
	                employee.getOtpCooldownUntil().isAfter(LocalDateTime.now())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Too many attempts. Please wait until: "
	                    + employee.getOtpCooldownUntil());
	            return response;
	        }

	        if (employee.getOtpRequestWindowStart() == null ||
	                employee.getOtpRequestWindowStart().isBefore(LocalDateTime.now().minusHours(1))) {
	            employee.setOtpRequestWindowStart(LocalDateTime.now());
	            employee.setOtpRequestCount(0);
	        }

	        if (employee.getOtpRequestCount() >= otpConfig.getMaxRequestsPerHour()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Too many OTP requests. Please try again after 1 hour.");
	            return response;
	        }

	        Random random = new Random();
	        int otp = random.nextInt(900000) + 100000;
	        employee.setOtp(EncryptDecrypt.encryptOtp(String.valueOf(otp)));
	        employee.setOtpUpdatedOn(LocalDateTime.now());

	        employee.setOtpRequestCount(employee.getOtpRequestCount() + 1);

	        Employee employeeSaved = employeeRepository.save(employee);

	        if (employeeSaved != null) {
	            mailService.sendMail(employeedto.getEmail(), "Forgot Password OTP",
	                    "Please find your OTP: " + otp);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("OTP sent to emailId.");

	            apiLogInfo.setApiResponse("OTP sent to emailId.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Failed to update OTP.");

	            apiLogInfo.setApiResponse("Failed to update OTP.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	
//added by rahul for LMS Redirection

	public ServiceResponse LMSRedirection(String email,String url) {
		ServiceResponse response = new ServiceResponse();
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        LMSEmailSend email2=new LMSEmailSend();
        email2.setEmail(email);
        // Create the HttpEntity with the emailRequest body and headers
        HttpEntity<String> request = new HttpEntity<>(email, headers);

        // Send the POST request and get the response
        ResponseEntity<LMSDTO> responsefrom = restTemplate.exchange(
        		url,
                HttpMethod.POST,
                request,
                LMSDTO.class
        );
        LMSDTO dto1=responsefrom.getBody();
        if(dto1.isStatus()) {
        	
        response.setServiceStatus("success");
        response.setServiceMessage("Login");
        response.setServiceResponse(dto1.getUrl());
        
        }else {
        	Employee employee = employeeRepository.findByEmail(email);

        	 response.setServiceStatus(""+dto1.isStatus());
             response.setServiceMessage("Login");
             response.setServiceResponse(responsefrom.getBody());
             
        }
        
       return response;
    }
	
	
	public ServiceResponse checkOTPWhenForgotPassword(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setFeatureName("Login");
	    apiLogInfo.setSubFeatureName("Confirm OTP (Forgot Password)");
	    apiLogInfo.setApiUrl("/api/checkOTPWhenForgotPassword");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Email : ").append(employeedto.getEmail())
	              .append(", Otp : ").append(employeedto.getOtp());

	    try {
	        Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee not found.");
	            return response;
	        }
	        if (employee.getOtpCooldownUntil() != null &&
	            employee.getOtpCooldownUntil().isAfter(LocalDateTime.now())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Account locked due to multiple failed attempts. Try again at "
	                                        + employee.getOtpCooldownUntil());
	            apiLogInfo.setApiResponse("Account locked due to multiple failures");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        Long otpDiff = ChronoUnit.MINUTES.between(employee.getOtpUpdatedOn(), LocalDateTime.now());
	        if (otpDiff > otpTimeoutPeriod) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("OTP Expired, Please re-send new OTP.");
	            apiLogInfo.setApiResponse("OTP expired");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }
	        
//	        String encryptedInputOtp = EncryptDecrypt.encryptOtp(employeedto.getOtp());
	        String plainOtp = employeedto.getOtp();
//	        String encryptedInputOtp = EncryptDecrypt.encryptOtp(plainOtp);

	        boolean isValidOtp = plainOtp.equals(employee.getOtp()) 
	                             || employeedto.getOtp().equals(portalStaticOtp.toString());


	        if (isValidOtp) {
	            employee.setOtpFailedAttempts(0);
	            employee.setOtpCooldownUntil(null);
	            employeeRepository.save(employee);

	            String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
	            String encSessionString = EncryptDecrypt.encrypt(sessionString);

	            UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
	            if (existingUserSession != null) {
	                userSessionRepository.deleteById(existingUserSession.getUserSessionId());
	            }

	            UserSession newSession = new UserSession();
	            newSession.setEmpId(employee.getEmpId());
	            newSession.setLoginTime(LocalDateTime.now());
	            newSession.setLastCheckTime(LocalDateTime.now());
	            newSession.setSessionKey(encSessionString);
	            userSessionRepository.save(newSession);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(encSessionString);

	            apiLogInfo.setApiResponse("OTP verified successfully.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            int failed = (employee.getOtpFailedAttempts() == null) ? 0 : employee.getOtpFailedAttempts();
	            failed++;
	            employee.setOtpFailedAttempts(failed);

	            if (failed >= otpConfig.getMaxFailedAttempts()) {
	                employee.setOtpCooldownUntil(LocalDateTime.now().plusMinutes(otpConfig.getCooldownMinutes()));
	            }

	            employeeRepository.save(employee);

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid OTP. Please try again");
	            apiLogInfo.setApiResponse("Invalid OTP. Please try again.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

		
	
//	public ServiceResponse checkOTPWhenForgotPassword(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setFeatureName("Login");
//		apiLogInfo.setSubFeatureName("Confirm OTP");
//		apiLogInfo.setApiUrl("/api/checkOTPWhenForgotPassword");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("Email : "+employeedto.getEmail()+ ", Otp : "+employeedto.getOtp());
//		
//		try {
//
//			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
//
//			Long otpDiff = ChronoUnit.MINUTES.between(employee.getOtpUpdatedOn(), LocalDateTime.now());
//			
//			if(otpDiff < otpTimeoutPeriod) {
//				if (employee != null && (employee.getOtp().equals(employeedto.getOtp()))) {
//					
//					String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
//					String encSessionString  = EncryptDecrypt.encrypt(sessionString);
//					
//					UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
//					boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
//					
//					if (isUserLoggedIn) {
//						userSessionRepository.deleteById(existingUserSession.getUserSessionId());
//					}
//					
//					UserSession newSession  = new UserSession();
//					newSession.setEmpId(employee.getEmpId());
//					newSession.setLoginTime(LocalDateTime.now());
//					newSession.setLastCheckTime(LocalDateTime.now());
//					newSession.setSessionKey(encSessionString);
//					
//					userSessionRepository.save(newSession);
//					
//					
//					
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(encSessionString);
//					
//					apiLogInfo.setApiResponse("OTP verified successfully.");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Invalid OTP. Please try again");
//					
//					apiLogInfo.setApiResponse("Invalid OTP. Please try again.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("OTP Expired, Please re-send new otp.");
//				
//				apiLogInfo.setApiResponse("OTP Expired, Please re-send new otp.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

//	public ServiceResponse resendOTP(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setFeatureName("Login");
//		apiLogInfo.setSubFeatureName("Resend OTP");
//		apiLogInfo.setApiUrl("/api/resendOTP");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("Email : "+employeedto.getEmail());
//		
//		try {
//			
//			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
//			
//			if(employee != null) {
//				
//				Random random = new Random();
//				int otp = random.nextInt(999999 - 100000)
//						+ 100000; /* Random number will be generated between 1000 and 9999 */
//				employee.setOtp(otp);
//				employee.setOtpUpdatedOn(LocalDateTime.now());
//				
//				mailService.sendMail(employeedto.getEmail(), "Regarding otp",
//						"Please find your otp " + otp);
//
//				Employee employeeSaved = employeeRepository.save(employee);
//
//				if(employeeSaved != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("OTP sent to email.");
//					
//					apiLogInfo.setApiResponse("OTP sent to email.");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Failed to update OTP.");
//					
//					apiLogInfo.setApiResponse("Failed to update OTP.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee not found.");
//				
//				apiLogInfo.setApiResponse("Employee not found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	


	public ServiceResponse resendOTP(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setFeatureName("Login");
	    apiLogInfo.setSubFeatureName("Resend OTP");
	    apiLogInfo.setApiUrl("/api/resendOTP");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Email : ").append(employeedto.getEmail());

	    try {
	        Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee not found.");
	            return response; 
	        }

	        
	        if (employee.getOtpCooldownUntil() != null &&
	            employee.getOtpCooldownUntil().isAfter(LocalDateTime.now())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Too many attempts. Please wait until: " 
	                    + employee.getOtpCooldownUntil());
	            return response; 
	        }

	        
	        if (employee.getOtpRequestWindowStart() == null ||
	            employee.getOtpRequestWindowStart().isBefore(LocalDateTime.now().minusHours(1))) {
	            
	            employee.setOtpRequestWindowStart(LocalDateTime.now());
	            employee.setOtpRequestCount(0);
	        }

	        if (employee.getOtpRequestCount() >= otpConfig.getMaxRequestsPerHour()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Too many OTP requests. Please try after 1 hour.");
	            return response; 
	        }
	        Long otpDiff = ChronoUnit.MINUTES.between(employee.getOtpUpdatedOn(), LocalDateTime.now());
	        if (otpDiff < otpTimeoutPeriod) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Previous OTP is still valid. Please wait 1 minute.");
	            apiLogInfo.setApiResponse("OTP expired");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }
	        
	        Random random = new Random();
	        int otp = random.nextInt(900000) + 100000;
	        //employee.setOtp(otp);
			employee.setOtp(EncryptDecrypt.encryptOtp(String.valueOf(otp)));
	        employee.setOtpUpdatedOn(LocalDateTime.now());

	        
	        employee.setOtpRequestCount(employee.getOtpRequestCount() + 1);

	        
	        Employee employeeSaved = employeeRepository.save(employee);

	        if (employeeSaved != null) {
	            mailService.sendMail(employee.getEmail(), "Login OTP", "Your OTP is: " + otp);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("OTP sent to email.");

	            apiLogInfo.setApiResponse("OTP sent to email.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Failed to update OTP.");

	            apiLogInfo.setApiResponse("Failed to update OTP.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}


	/**
	 * Po backend: {@code POST /api/auth/po/session-login}. Returns {@link PoSessionLoginRequestDTO} with
	 * {@code empId} and {@code poToken}:
	 * <ul>
	 * <li>No {@code user_session} row: create session + new token.</li>
	 * <li>Row exists, {@code po_token} null/empty: generate token, bind to row, return it.</li>
	 * <li>Row exists with mapped {@code po_token}: return existing token (session unchanged).</li>
	 * </ul>
	 * No deep link at this stage; logs use {@link #PO_LOG_DEEP_LINK_TOKEN_FETCH}.
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse authenticateFromPoSession(PoSessionLoginRequestDTO requestDto) throws Exception {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/auth/po/session-login");
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"authPoSessionLogin", "PoPortal", null, httpRequest);

			if (requestDto == null || requestDto.getEmpId() == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Po session-login payload: empId is required.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"authPoSessionLogin", "PoPortal", requestDto.getEmpId(), httpRequest);

			Employee employee = employeeRepository.findById(requestDto.getEmpId()).orElse(null);
			if (employee == null || "InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee not found or inactive.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				return response;
				throw new DataNotFoundException("Employee not found or inactive.");
			}

			LocalDateTime now = LocalDateTime.now();
			UserSession existing = userSessionRepository.findByEmpId(employee.getEmpId());

			if (existing == null) {
				String newToken = generatePoPortalAccessToken();
				PoBindResult bind = bindOrCreateUserSessionForPo(employee, newToken, now);
				savePoSessionAccessLog(employee.getEmpId(), PO_LOG_DEEP_LINK_TOKEN_FETCH, "PO_SESS_LOGIN_NEW_SESSION",
						bind.session.getSessionKey(), now);
				poSessionLoginTokenSuccess(response, employee.getEmpId(), newToken, "Po access token issued.");
				finalHttpStatusCode = HttpStatus.OK.value();
				apiLogInfo.setApiResponse(response.getServiceMessage());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				return response;
			}

			boolean hasMappedPoToken = existing.getPoToken() != null && !existing.getPoToken().trim().isEmpty();
			if (hasMappedPoToken) {
				savePoSessionAccessLog(employee.getEmpId(), PO_LOG_DEEP_LINK_TOKEN_FETCH, "PO_SESS_LOGIN_EXISTING_TOKEN_RETURNED",
						existing.getSessionKey(), now);
				poSessionLoginTokenSuccess(response, employee.getEmpId(), existing.getPoToken(),
						"Po access token returned.");
				finalHttpStatusCode = HttpStatus.OK.value();
				apiLogInfo.setApiResponse(response.getServiceMessage());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				return response;
			}

			String newToken = generatePoPortalAccessToken();
			PoBindResult bind = bindOrCreateUserSessionForPo(employee, newToken, now);
			savePoSessionAccessLog(employee.getEmpId(), PO_LOG_DEEP_LINK_TOKEN_FETCH, "PO_SESS_LOGIN_TOKEN_MAPPED",
					bind.session.getSessionKey(), now);
			poSessionLoginTokenSuccess(response, employee.getEmpId(), newToken, "Po access token issued.");
			finalHttpStatusCode = HttpStatus.OK.value();
			apiLogInfo.setApiResponse(response.getServiceMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		} catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Unable to complete Po session-login (token fetch).");
//			response.setServiceError(e.getMessage());
			apiLogInfo.setApiResponse((String) response.getServiceResponse());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			return response;
			throw new Exception(e.getMessage());
		} 
		finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
			ExceptionLogContext.clear();
		}
	}

	private ServiceResponse poSessionLoginTokenSuccess(ServiceResponse response, Long empId, String poToken,
			String serviceMessage) {
		PoSessionLoginRequestDTO body = new PoSessionLoginRequestDTO();
		body.setEmpId(empId);
		body.setPoToken(poToken);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(body);
		response.setServiceMessage(serviceMessage);
		return response;
	}

	/**
	 * iShine frontend (direct link): validates presented token, rotates {@code po_token}, returns OTP-equivalent
	 * payload for browser session bootstrap.
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse verifyTokenOfPoPortalForDirectAccess(PoVerifyDirectAccessRequestDTO requestDto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Po direct access verify");
		apiLogInfo.setApiUrl("/api/auth/po/verifyTokenOfPoPortalForDirectAccess");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {
			if (requestDto == null || requestDto.getEmpId() == null || requestDto.getPoToken() == null
					|| requestDto.getPoToken().trim().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Po verify payload.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("empId or poToken missing.");
				return response;
			}
			logBuilder.append("empId: ").append(requestDto.getEmpId());
			if (requestDto.getDeepLink() == null || requestDto.getDeepLink().trim().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("deepLink is required for Po direct access verification.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append(", deepLink missing.");
				return response;
			}
			logBuilder.append(", deepLink length: ").append(requestDto.getDeepLink().trim().length());

			Employee employee = employeeRepository.findById(requestDto.getEmpId()).orElse(null);
			if (employee == null || "InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found or inactive.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			UserSession userSession = userSessionRepository.findByEmpId(requestDto.getEmpId());
			if (userSession == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No active session for employee.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			if (userSession.getPoToken() == null || !userSession.getPoToken().equals(requestDto.getPoToken())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Po token mismatch.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			LocalDateTime now = LocalDateTime.now();
			String deepLink = requestDto.getDeepLink().trim();
			String rotated = generatePoPortalAccessToken();
			userSession.setPoToken(rotated);
			userSession.setLastCheckTime(now);
			userSessionRepository.save(userSession);

			savePoSessionAccessLog(employee.getEmpId(), deepLink, "PO_TOKEN_VERIFIED_AND_ROTATED", userSession.getSessionKey(),
					now);

			response = buildPoPortalLoginSuccessResponse(employee, userSession, deepLink, "Po direct access verified.");
			if (ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())) {
				apiLogInfo.setApiResponse(response.getServiceMessage());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				apiLogInfo.setApiResponse(
						response.getServiceResponse() != null ? String.valueOf(response.getServiceResponse()) : "Build payload failed.");
				apiLogInfo.setApiStatus(response.getServiceStatus());
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Unable to verify Po direct access.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiResponse((String) response.getServiceResponse());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return response;
		} finally {
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
	}

	private String generatePoPortalAccessToken() {
		byte[] bytes = new byte[32];
		new SecureRandom().nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static final class PoBindResult {
		private final UserSession session;
		private final boolean createdNewSessionRow;

		private PoBindResult(UserSession session, boolean createdNewSessionRow) {
			this.session = session;
			this.createdNewSessionRow = createdNewSessionRow;
		}
	}

	private PoBindResult bindOrCreateUserSessionForPo(Employee employee, String poToken, LocalDateTime now) throws Exception {
		UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
		if (existingUserSession != null) {
			existingUserSession.setPoToken(poToken);
			existingUserSession.setLastCheckTime(now);
			userSessionRepository.save(existingUserSession);
			return new PoBindResult(existingUserSession, false);
		}

		String rawSessionString = LocalDateTime.now().toString() + employee.getEmail();
		String encSessionString = EncryptDecrypt.encrypt(rawSessionString);
		List<Long> featureIds = featureMasterRepository.getAllFeatureIdsByJobRoleIds(employee.getJobRoleId());
		String subFeatureIds = featureIds.stream().map(String::valueOf).collect(Collectors.joining(","));

		UserSession newSession = new UserSession();
		newSession.setEmpId(employee.getEmpId());
		newSession.setLoginTime(now);
		newSession.setLastCheckTime(now);
		newSession.setSessionKey(encSessionString);
		newSession.setPoToken(poToken);
		newSession.setFetaureIds(subFeatureIds);
		userSessionRepository.save(newSession);
		return new PoBindResult(newSession, true);
	}

	private ServiceResponse buildPoPortalLoginSuccessResponse(Employee employee, UserSession userSession, String deepLink,
			String successMessage) {
		ServiceResponse response = new ServiceResponse();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			ServiceResponse tabResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId(), employee.getEmpId());
			EmployeeDTO currentEmployeeDto = employeeService.getEmployeeInfoOnLogin(employee.getEmail());
			AppreciationEventDTO currentEventDto = appreciationService.getAppreciationEventInfo();

			currentEmployeeDto.setTimesheetBackDatedDays(timesheetBackDatedDays);
			currentEmployeeDto.setCompOffLockDays(compOffLockDays);
			currentEmployeeDto.setLeaveBackdatedLockDays(leaveBackdatedLockDays);
			currentEmployeeDto.setLeaveFuturedatedLockDays(leaveFutureLockDays);
			currentEmployeeDto.setRevokeReporteeLeaveValidity(revokeReporteeLeaveValidity);

			LogDTO logInfo = userLogInfoList.getOrDefault(employee.getEmpId(), new LogDTO());
			logInfo.setEmpId(employee.getEmpId());
			logInfo.setFeatureName("Login");
			if (logInfo.getLoginTime() == null) {
				logInfo.setLoginTime(df.format(new Date()));
			}
			userLogInfoList.put(employee.getEmpId(), logInfo);

			Object[] object = new Object[9];
			object[0] = currentEmployeeDto;
			object[1] = tabResponse.getServiceResponse();
			object[2] = userSession.getSessionKey();
			object[3] = sessionTimeout;
			object[4] = maxFileSize.replace("MB", "");
			object[5] = maxRequestSize.replace("MB", "");
			object[6] = logInfo;
			object[7] = currentEventDto;
			object[8] = deepLink;

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(object);
			response.setServiceMessage(successMessage);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Unable to build Po session payload.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse logoutFromPoSession(PoSessionLoginRequestDTO requestDto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/auth/po/session-logout");
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"authPoSessionLogout", "PoPortal", null, httpRequest);

			if (requestDto == null || requestDto.getEmpId() == null || requestDto.getPoToken() == null
					|| requestDto.getPoToken().trim().isEmpty()) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Po logout payload.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"authPoSessionLogout", "PoPortal", requestDto.getEmpId(), httpRequest);

			UserSession userSession = userSessionRepository.findByEmpId(requestDto.getEmpId());
			if (userSession == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Session already destroyed.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			if (userSession.getPoToken() == null || !userSession.getPoToken().equals(requestDto.getPoToken())) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Po token mismatch. Session not deleted.");
				apiLogInfo.setApiResponse((String) response.getServiceResponse());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			savePoSessionAccessLog(requestDto.getEmpId(), "/logout", "LOGOUT", userSession.getSessionKey(), LocalDateTime.now());
			userSessionRepository.deleteById(userSession.getUserSessionId());
			userLogInfoList.remove(requestDto.getEmpId());

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Po session logout successful.");
			finalHttpStatusCode = HttpStatus.OK.value();
			apiLogInfo.setApiResponse((String) response.getServiceResponse());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		} catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Unable to logout Po session.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiResponse((String) response.getServiceResponse());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return response;
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
			ExceptionLogContext.clear();
		}
	}

	private void savePoSessionAccessLog(Long empId, String deepLink, String action, String sessionKey, LocalDateTime accessedAt) {
		PoSessionAccessLog accessLog = new PoSessionAccessLog();
		accessLog.setEmpId(empId);
		accessLog.setDeepLink(deepLink);
		accessLog.setAction(action);
		accessLog.setSessionKey(sessionKey);
		accessLog.setAccessedAt(accessedAt);
		poSessionAccessLogRepository.save(accessLog);
	}

	public boolean checkUserToken(String token) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkUserToken");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("session key :"+userSessionRepository.findBySessionKey(token));
		UserSession existingUserSession = userSessionRepository.findBySessionKey(token);
		if(existingUserSession != null) {
			response.setServiceResponse("Token exists.");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Token exists.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}else {
			response.setServiceResponse("Token not found.");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse("Token not found.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return (existingUserSession != null) ? true : false;

	}

}
