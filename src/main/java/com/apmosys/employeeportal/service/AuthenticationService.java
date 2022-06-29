package com.apmosys.employeeportal.service;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
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
	
	@Autowired
	HttpSession session;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	TabMasterService tabMasterService;
	
	@Autowired
	EmployeeService employeeService;

	public ServiceResponse authenticateUser(EmployeeDTO employeedto) {
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
				
				EmployeeDTO dto = new EmployeeDTO();
				dto.setEmpId(employee.getEmpId());
				
				EmployeeDTO currentEmployeeDto = (EmployeeDTO) employeeService.getEmployeeByEmpId(dto).getServiceResponse();
				
				session = request.getSession();
				session.invalidate();
				session = request.getSession(true);
				session.setAttribute("currentEmployee", currentEmployee);
				session.setAttribute("currentEmployeeDto",currentEmployeeDto);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Valid Credentials. OTP sent to email.");
			}
			System.out.println("employee : "+ employee);
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

	public ServiceResponse authenticateUserWithOTP(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			Employee employee = (Employee) session.getAttribute("currentEmployee");
			EmployeeDTO currentEmployeeDto = (EmployeeDTO) session.getAttribute("currentEmployeeDto");
			
					
			if(employeedto.getOtp().toString().equals(employee.getOtp().toString()))
			{
				ServiceResponse serviceResponse = tabMasterService.getTabsByRoleId(employee.getJobRoleId());	
				
				Object[] object = new Object[2];				
				object[0] = currentEmployeeDto;
				object[1] = serviceResponse.getServiceResponse();
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(object);
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
