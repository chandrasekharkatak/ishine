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
	
    public EmployeeKpi assignKpiToEmployee(EmployeeKpiDto employeeKpiDto) {
    	
        	EmployeeKpi employeeKpi = new EmployeeKpi();
        	if(employeeKpiDto.getTemplateId() == kpiRepository.findbyId(employeeKpiDto.getTemplateId())) {
    		employeeKpi.setTemplateId(employeeKpiDto.getTemplateId());
    		employeeKpi.setEmpId(employeeKpiDto.getEmpId());
    		employeeKpi.setAssignedBy(null);
    		employeeKpi.setAssignedOn(null);
    		employeeKpi.setDepartmentId(employeeKpiDto.getDepartmentId());
    		employeeKpi.setEmployeeRole(employeeKpiDto.getEmployeeRole());
    		employeeKpi.setQuarterId(employeeKpiDto.getQuarterId());
        	}
    		
    		return employeeKpiReposiotry.save(employeeKpi);
    		
}
}
