package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeKpiDto;
import com.apmosys.employeeportal.model.EmployeeKpi;
import com.apmosys.employeeportal.repository.EmployeeKpiRepository;
import com.apmosys.employeeportal.repository.KpiRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;


@Service
public class EmployeeKpiService {
	


	@Autowired
	private EmployeeKpiRepository  employeeKpiReposiotry;
	
	@Autowired
	private KpiRepository kpiRepository;
	
    public ServiceResponse assignKpiToEmployee(EmployeeKpiDto employeeKpiDto) {
    	ServiceResponse response = new ServiceResponse();
    	try {
        	EmployeeKpi employeeKpi = new EmployeeKpi();
        	if(employeeKpiDto.getTemplateId() == kpiRepository.findbyId(employeeKpiDto.getTemplateId())) {
    		employeeKpi.setTemplateId(employeeKpiDto.getTemplateId());
    		employeeKpi.setEmpId(employeeKpiDto.getEmpId());
    		employeeKpi.setAssignedBy(employeeKpiDto.getAssignedBy());
    		employeeKpi.setAssignedOn(null);
    		employeeKpi.setDepartmentId(employeeKpiDto.getDepartmentId());
    		employeeKpi.setEmployeeRole(employeeKpiDto.getEmployeeRole());
    		employeeKpi.setQuarterId(employeeKpiDto.getQuarterId());
    		
    		EmployeeKpi employee = employeeKpiReposiotry.save(employeeKpi);
    		
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(employee);
            response.setServiceMessage("Employee Goal Updated Successfully");
        	}
    	}catch(Exception e)
    	{
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Assigning");
    	}
    	return response;
    		
    		
    		
}
}
