package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.DomainDTO;
import com.apmosys.employeeportal.dto.EmployeeteamDto;
import com.apmosys.employeeportal.model.Employee;
//import com.apmosys.employeeportal.model.ServiceResponse;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class employeeDeptService {
    @Autowired
    private UserSessionRepository userSessionRepo;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    public ServiceResponse findEmployeesInSameDepartmentAsCurrentUser(Long hodId) {
       ServiceResponse response = new ServiceResponse();
       
       try {
			
    	   List<Object[]> getNames = employeeRepository.findEmployeesInSameDepartmentAsCurrentUser(hodId);
			List<EmployeeteamDto> dtoList = new ArrayList<>();
			System.out.println("........"+getNames);
			if(!getNames.isEmpty()) {
					for(Object[] object : getNames) {
						EmployeeteamDto employee = new EmployeeteamDto();
						
						
						employee.setEmpId(object[0]!= null ? Long.parseLong(object[0].toString()) : null);
						employee.setName(object[1] != null ? object[1].toString(): null);
						employee.setEmployeementId(object[2]!= null ? Long.parseLong(object[2].toString()) : null);
						employee.setGoalsCompleted(null);
						employee.setTotalGoals(object[3]!= null ? Long.parseLong(object[3].toString()) : null);
					
						dtoList.add(employee);
					}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
		
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Domain Found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
       return response;
    }
	
}