package com.apmosys.employeeportal.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;

import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PoSyncOrchestratorService {
	
	@Autowired
	ClientService clientService;
	
	@Autowired
	ProjectService projectService; 
	
	@Autowired
	DepartmentService departmentService;
	
	@Autowired
	PoDetailsService poDetailsService;
	
	
	@Autowired
	ResourceRequirementService requirementService;
	
	@Transactional(rollbackFor = Exception.class)
    public ServiceResponse poCrudOperationsInIshineNew(ProjectPoMappingWithResourceDTO dto) {
		
		 ServiceResponse response = new ServiceResponse();
		
		 if (dto == null)
	            throw new RuntimeException("DTO from PO portal is null");
		 
		 if (dto.getProjectId() == null)
	            throw new RuntimeException("PoProjectId missing");
		 
		 if (dto.getPoDetailsList() == null || dto.getPoDetailsList().isEmpty())
	            throw new RuntimeException("PO details missing");
		 
		 Client client = clientService.resolveClient(dto.getClientName());
		 
		 if (dto.getEventType() == SyncRequestType.CREATE_PROJECT) {
			 
			 Project project = projectService.createProjectRTS(dto, client);	 
			 ProjectPoDetails po = poDetailsService.createPoRTS(project, dto, client);
			 departmentService.syncDepartmentsRTS(po.getPoId(),
	                    dto.getPoDetailsList().get(0).getDepartmentList());
			 
			 requirementService.syncRequirementsRTS(po.getPoId(),
	                    dto.getPoDetailsList().get(0).getResourceRequirementList());
			 
//		 }else if () {
			 
		 }else {
			 throw new RuntimeException("Unsupported eventType");
			 
		 }
		 
		
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("PO sync successful");
	        return response;
		 
		 
		
		
	}

}
