package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeSessionDTO;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class EmployeeSessionService {
	
	private final UserSessionRepository userSessionRepo;
	
	@Autowired
	public EmployeeSessionService(UserSessionRepository userSessionRepo)
	{
		this.userSessionRepo = userSessionRepo;
	}
	
	public ServiceResponse getEmployeeDetailsBySessionEmpId(Long empId) {
	ServiceResponse response =new ServiceResponse();
	try{
		
		if(empId != null  ) {
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(userSessionRepo.findEmployeeDetailsByEmpId(empId));
        response.setServiceMessage("Got the details of Employee");}
		else {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage("No employee found for employee with ID: " + empId);
        }
	}
	catch(Exception e) {
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceError(e.getMessage());
        response.setServiceMessage("Error");
	}
	return response;
	}
}
