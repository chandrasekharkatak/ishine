package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
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
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class AuthenticationService {

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	TabMasterService tabMasterService;

	@Autowired
	EmployeeService employeeService;

	@Value("${portal.static.otp}")
	private String portalStaticOtp;

	@Value("${idle.session.timeout}")
	private Integer sessionTimeout;

	static ConcurrentHashMap<Long, String> userSessionList = new ConcurrentHashMap<Long, String>();

	public ServiceResponse authenticateUser(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null) {

				boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());

				if (!employee.getEmploymentstatus().equals("InActive")) {

					if (!isUserLoggedIn) {
						String dbPassword = EncryptDecrypt.decrypt(employee.getPassword());
						String orignalPassword = EncryptDecrypt.decrypt(employeedto.getPassword());

						if (dbPassword.equals(orignalPassword)) {

							Random random = new Random();
							int otp = random.nextInt(9999 - 1000)
									+ 1000; /* Random number will be generated between 1000 and 9999 */
							employee.setOtp(otp);

							employeeRepository.save(employee);

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Valid Credentials. OTP sent to email.");

						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Invalid Password");
						}

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL_1);
						response.setServiceResponse(
								"User already logged in.Do you want to logout of existing session ?");
					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("InActive User");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Credentials.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {
			String sessionString = LocalDateTime.now().toString() + employeedto.getEmail();
			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employeedto.getOtp().toString().equals(employee.getOtp().toString())
					|| employeedto.getOtp().toString().equals(portalStaticOtp)) {
				ServiceResponse serviceResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId());
				EmployeeDTO currentEmployeeDto = employeeService.getEmployeeInfoOnLogin(employeedto.getEmail());

				boolean isUserLoggedIn = userSessionList.containsKey(employee.getEmpId());

				if (!isUserLoggedIn) {

					userSessionList.put(employee.getEmpId(), sessionString);

				} else {

					userSessionList.put(employee.getEmpId(), sessionString);

				}

				Object[] object = new Object[4];
				object[0] = currentEmployeeDto;
				object[1] = serviceResponse.getServiceResponse();
				object[2] = sessionString;
				object[3] = sessionTimeout;

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(object);
				response.setServiceMessage("OTP validated successfully. User Log in success.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("OTP validation failed. Please try again.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse logoutUser(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {

			boolean isUserLoggedIn = userSessionList.containsKey(employeedto.getEmpId());

			if (isUserLoggedIn) {

				userSessionList.remove(employeedto.getEmpId());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Session destroyed. User Logout successfull");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Session already destroyed");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
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

		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null) {
				Random random = new Random();
				int otp = random.nextInt(9999 - 1000) + 1000;
				employee.setOtp(otp);
				employeeRepository.save(employee);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("OTP sent to emailId.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Enter valid credentials.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkOTPWhenForgotPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null && (employee.getOtp().equals(employeedto.getOtp()))) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("OTP verified successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid OTP. Please try again");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
