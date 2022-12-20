package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import com.apmosys.employeeportal.dto.AppreciationEventDTO;	
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class AuthenticationService {

	@Value("${valid.attempt}")
	private Integer failedAttempt;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	TabMasterService tabMasterService;
	
	@Autowired	
	AppreciationService appreciationService;

	@Autowired
	EmployeeService employeeService;

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
	private HttpServletRequest httpRequest;
	
	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;
	
	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSize;

	static ConcurrentHashMap<Long, String> userSessionList = new ConcurrentHashMap<Long, String>();
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
					boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());
					
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
									int otp = random.nextInt(9999 - 1000)
											+ 1000; /* Random number will be generated between 1000 and 9999 */
									employee.setOtp(otp);
									mailService.sendMail(employeedto.getEmail(), "Regarding otp",
											"Please find your otp " + otp);
									employee.setInvalidAccessAttempt(0);

									employeeRepository.save(employee);

									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Valid Credentials. OTP sent to email.");
									
									apiLogInfo.setApiResponse("Valid Credentials. OTP sent to email.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
									
								} else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL_1);
									response.setServiceResponse(
											"User already logged in.Do you want to logout of existing session ?");
								}
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

	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		/* FOR SESSION LOG INFO */
		LogDTO logInfo = new LogDTO();
		logInfo.setEmpId(employeedto.getEmpId());
		logInfo.setFeatureName("Login");
		/*FOR API LOG */
		LogDTO apiLogInfo = new LogDTO();
		logInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Confirm OTP");
		apiLogInfo.setApiUrl("/api/authenticateUserWithOTP");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+employeedto.getEmpId()+", Email : "+ employeedto.getEmail()+", Otp : "+ employeedto.getOtp());
		
		try {
			
			String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employeedto.getOtp().toString().equals(employee.getOtp().toString())
					|| employeedto.getOtp().toString().equals(portalStaticOtp)) {
				ServiceResponse serviceResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId());
				EmployeeDTO currentEmployeeDto = employeeService.getEmployeeInfoOnLogin(employeedto.getEmail());
				AppreciationEventDTO currentEventDto = appreciationService.getAppreciationEventInfo();	

				logInfo.setLoginTime(df.format(new Date()));
				boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());

				if (!isUserLoggedIn) {
					userSessionList.put(employee.getEmpId(), sessionString);
					userLogInfoList.put(employee.getEmpId(), logInfo);
				} else {
					userSessionList.put(employee.getEmpId(), sessionString);
					userLogInfoList.put(employee.getEmpId(), logInfo);
				}

				Object[] object = new Object[8];
				object[0] = currentEmployeeDto;
				object[1] = serviceResponse.getServiceResponse();
				object[2] = sessionString;
				object[3] = sessionTimeout;
				object[4] = maxFileSize.replace("MB","");
				object[5] = maxRequestSize.replace("MB","");
				object[6] = logInfo;
				object[7] = currentEventDto;
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(object);
				response.setServiceMessage("OTP validated successfully. User Log in success.");
				
				apiLogInfo.setApiResponse("OTP validated successfully. User Log in success.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("OTP validation failed. Please try again.");
				
				apiLogInfo.setApiResponse("OTP validation failed. Please try again.");			
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
		
		if(employeedto.getEmail().equals("admin3@apmosys.com") || employeedto.getEmail().equals("admin2@apmosys.com")) {
			System.out.println("\n ================== userSessionList ================== \n");
			System.out.println(userSessionList);
			System.out.println("\n ================== userSessionList ================== \n");
			
			logBuilder.append("userSessionList : "+ userSessionList);
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

			boolean isUserLoggedIn = userSessionList.containsKey(employeedto.getEmpId());
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
				
				userSessionList.remove(employeedto.getEmpId());
				
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
		try {
			String sessionString = userSessionList.get(employeedto.getEmpId());

			if (sessionString != null) {
				if (sessionString.equals(employeedto.getSessionString())) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Session exists.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Sessionstring is different. Logging out of application.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Session not found. Logging out of application.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmailWhenForgotPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Send OTP");
		apiLogInfo.setApiUrl("/api/checkEmailWhenForgotPassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Email : "+employeedto.getEmail());
		
		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null) {
				logBuilder.append(", Employee IsNew : "+employee.getIsNew());
				if(employee.getIsNew().equals("true")) {	
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
				    response.setServiceResponse("Forgot Password feature is not for New User.");
				    
				    apiLogInfo.setApiResponse("Forgot Password feature is not for New User.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}else {	
					if(employee.getEmploymentstatus().equals("InActive") || employee.getInvalidAccessAttempt()>= failedAttempt) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
						response.setServiceResponse("This user is not authorized for this activity ");
					}
					else {
						Random random = new Random();	
						int otp = random.nextInt(9999 - 1000) + 1000;	
						employee.setOtp(otp);	
						employeeRepository.save(employee);	
							
						mailService.sendMail(employeedto.getEmail(), "Regarding otp","Please find your otp "+otp);	
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
						response.setServiceResponse("OTP sent to emailId.");
						
						apiLogInfo.setApiResponse("OTP sent to emailId.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					
				}	
			} else {	
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
				response.setServiceResponse("Enter valid credentials.");
				
				apiLogInfo.setApiResponse("Enter valid credentials.");			
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

	public ServiceResponse checkOTPWhenForgotPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Confirm OTP");
		apiLogInfo.setApiUrl("/api/checkOTPWhenForgotPassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Email : "+employeedto.getEmail()+ ", Otp : "+employeedto.getOtp());
		
		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null && (employee.getOtp().equals(employeedto.getOtp()))) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("OTP verified successfully.");
				
				apiLogInfo.setApiResponse("OTP verified successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
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

	public ServiceResponse resendOTP(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setFeatureName("Login");
		apiLogInfo.setSubFeatureName("Resend OTP");
		apiLogInfo.setApiUrl("/api/resendOTP");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Email : "+employeedto.getEmail());
		
		try {
			
			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
			
			if(employee != null) {
				
				Random random = new Random();
				int otp = random.nextInt(9999 - 1000)
						+ 1000; /* Random number will be generated between 1000 and 9999 */
				employee.setOtp(otp);
				
				mailService.sendMail(employeedto.getEmail(), "Regarding otp",
						"Please find your otp " + otp);

				Employee employeeSaved = employeeRepository.save(employee);

				if(employeeSaved != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("OTP sent to email.");
					
					apiLogInfo.setApiResponse("OTP sent to email.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update OTP.");
					
					apiLogInfo.setApiResponse("Failed to update OTP.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				
				apiLogInfo.setApiResponse("Employee not found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
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

}
