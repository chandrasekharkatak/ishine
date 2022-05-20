package com.apmosys.employeeportal.service;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class AuthenticationService {
	
	@Autowired
	EmployeeRepository employeeRepository;

	public ServiceResponse authenticateUser(EmployeeDTO employeedto , HttpSession session) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			Employee employee = employeeRepository.findByEmailAndPassword(employeedto.getEmail(),employeedto.getPassword());
			
			if(employee == null)
			{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Credentials.");
			}
			else
			{
				Random random = new Random();				
				int otp = random.nextInt(9999 - 1000) + 1000; /*Random number will be generated between 1000 and 9999 */
				employee.setOtp(otp);
				Employee currentEmployee = employeeRepository.save(employee);
				session.setAttribute("currentEmployee", currentEmployee);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Valid Credentials. OTP sent to email.");
			}
			System.out.println(employee);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto, HttpSession session) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			Employee employee = (Employee) session.getAttribute("currentEmployee");
	//		System.out.println(employee);
			if(employeedto.getOtp() == employee.getOtp())
			{
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(employee);
				response.setServiceMessage("OTP validated successfully. User Log in success.");
			}
			else
			{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceMessage("OTP validation failed. Please try again.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
