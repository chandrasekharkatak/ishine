package com.apmosys.employeeportal.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;

import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.ExceptionUtils;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PoSyncOrchestratorService {

	@Autowired
	ClientService clientService;

	@Autowired
	ProjectService projectService;
	
	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	DepartmentService departmentService;

	@Autowired
	PoDetailsService poDetailsService;

	@Autowired
	ResourceRequirementService requirementService;
	
	@Autowired
	ProjectPoDetailsRepository projectPoDetailsRepository;
	
	@Autowired
	private ApiLogUtility apiLogUtility;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Autowired
	private HttpServletRequest httpRequest;
	
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse poCrudOperationsInIshineNew(ProjectPoMappingWithResourceDTO dto) {
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/poCrudOperationsInIshine");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		StringBuilder exceptionDetailsForLog = new StringBuilder();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"poCrudOperationsInIshineNew", "PoPortal", null , httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();
		ServiceResponse response = new ServiceResponse();
		
		try {
		
		validateIncomingPayload(dto);

		

		if (dto == null) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			ExceptionLogContext.add("DTO from PO portal is null");
			throw new RuntimeException("DTO from PO portal is null");
		}

		if (dto.getProjectId() == null) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			ExceptionLogContext.add("PoProjectId missing from PO");
			throw new RuntimeException("PoProjectId missing from PO");
		}

		if (dto.getPoDetailsList() == null || dto.getPoDetailsList().isEmpty()) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			ExceptionLogContext.add("PO details missing from PO");
			throw new RuntimeException("PO details missing from PO");
		}

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

		if (poDto.getDepartmentList() == null || poDto.getDepartmentList().isEmpty()) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			ExceptionLogContext.add("Department list cannot be null or empty of TNM Project From PO");
			throw new RuntimeException("Department list cannot be null or empty of TNM Project From PO");
		}

		if ("TNM".equalsIgnoreCase(dto.getProjectType())) {
			
			if (poDto.getResourceRequirementList() == null || poDto.getResourceRequirementList().isEmpty()) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("Resource requirement list is mandatory for TNM projects ,missing from PO");
				throw new RuntimeException("Resource requirement list is mandatory for TNM projects ,missing from PO");
			}
		}

		Client client = clientService.resolveClient(dto.getClientName());

		if (dto.getEventType() == SyncRequestType.CREATE_PROJECT) {

			Project project = projectService.createProjectRTS(dto, client);
			ProjectPoDetails po = poDetailsService.createPoRTS(project, dto, client);
			departmentService.syncDepartmentsRTS(po.getPoId(), dto.getPoDetailsList().get(0).getDepartmentList());

			if (poDto.getResourceRequirementList() != null) {
				requirementService.syncRequirementsRTS(po.getPoId(),
						dto.getPoDetailsList().get(0).getResourceRequirementList());
			}

		} else if (dto.getEventType() == SyncRequestType.UPDATE_PO) {

			Project project = projectRepository.findByPoProjectId(dto.getProjectId());

			if (project == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("Project does not exist in ishine while update po for poProjectId = " + dto.getProjectId());
				throw new RuntimeException("Project does not exist for poProjectId=" + dto.getProjectId());
			}
			ProjectPoDetails po =
			        projectPoDetailsRepository
			                .findByPoIdAndProjectId(
			                        poDto.getPoId(),
			                        project.getProjectId())
			                .orElseThrow(() -> {
			                    ExceptionLogContext.add(
			                            "PO does not exist in iShine while update"
			                            + " | poId=" + poDto.getPoId()
			                            + " | poProjectId=" + dto.getProjectId()
			                    );
			                    return new RuntimeException(
			                            "PO does not exist for poId=" + poDto.getPoId()
			                    );
			                });

            boolean projectChanged =
                    projectService.updateProjectIfChanged(project, dto, client);

            boolean poChanged =
                    poDetailsService.updatePoIfChanged(po, dto, client);
            
            departmentService.syncDepartmentsRTS(
                    po.getPoId(),
                    poDto.getDepartmentList()
            );

            if (poDto.getResourceRequirementList() != null) {
                requirementService.syncRequirementsRTS(
                        po.getPoId(),
                        poDto.getResourceRequirementList()
                );
            }
		} else {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			ExceptionLogContext.add("Unsupported eventType from Po " + dto.getEventType() );
			throw new RuntimeException("Unsupported eventType");

		}
		finalHttpStatusCode = HttpStatus.OK.value();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("PO sync successful");
		return response;
		}catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
			return response;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
		}
		
	}
	
	
	private void validateIncomingPayload(ProjectPoMappingWithResourceDTO dto) {

	   
	    require(dto.getEventType(), "eventType");
	    require(dto.getProjectId(), "projectId");
	    require(dto.getProjectName(), "projectName");
	    require(dto.getProjectType(), "projectType");
	    require(dto.getProjectStartDate(), "projectStartDate");
	    require(dto.getProjectEndDate(), "projectEndDate");
	    require(dto.getClientId(), "clientId");
	    require(dto.getClientName(), "clientName");

	    if (dto.getPoDetailsList() == null || dto.getPoDetailsList().isEmpty()) {
	    	ExceptionLogContext.add("poDetailsList cannot be null or empty from PO" );
	        throw new RuntimeException("poDetailsList cannot be null or empty");
	    }

	    PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

	   
	    require(poDto.getPoId(), "poId");
	    require(poDto.getPoNo(), "poNo");
	    require(poDto.getPoStartDate(), "poStartDate");
	    require(poDto.getPoEndDate(), "poEndDate");
	    require(poDto.getClientAddressId(), "clientAddressId");
	    require(poDto.getClientLocation(), "clientLocation");
	    require(poDto.getClientState(), "clientState");
	    require(poDto.getApmosysRmEmpName(), "apmosysRmEmpName");
	    require(poDto.getApmosysRmEmail(), "apmosysRmEmail");
	    require(poDto.getClientRmName(), "clientRmName");
	 
	    

	    if (poDto.getDepartmentList() == null || poDto.getDepartmentList().isEmpty()) {
	    	ExceptionLogContext.add("departmentList cannot be null or empty from po" );
	        throw new RuntimeException("departmentList cannot be null or empty");
	    }

	    // ---------- CREATE vs UPDATE ----------
	    if (dto.getEventType() == SyncRequestType.CREATE_PROJECT) {

	        require(poDto.getCreatedByEmpId(), "createdByEmpId");
	        require(poDto.getCreatedByEmpName(), "createdByEmpName");

	    } else if (dto.getEventType() == SyncRequestType.UPDATE_PO) {

	        require(poDto.getUpdatedByEmpId(), "updatedByEmpId");
	        require(poDto.getUpdatedByEmpName(), "updatedByEmpName");

	    } else {
	        throw new RuntimeException("Unsupported eventType");
	    }

	   
	    if ("TNM".equalsIgnoreCase(dto.getProjectType())) {

	        if (poDto.getResourceRequirementList() == null ||
	            poDto.getResourceRequirementList().isEmpty()) {
	            throw new RuntimeException(
	                    "resourceRequirementList is mandatory for TNM projects");
	        }

	        for (POResourceRequirementDTO r : poDto.getResourceRequirementList()) {
	            validateResourceRequirement(r);
	        }
	        
	    }
	}
	
	private void validateResourceRequirement(POResourceRequirementDTO r) {

	    require(r.getResourceOverviewId(), "resourceOverviewId");
	    require(r.getClientRoleId(), "clientRoleId");
	    require(r.getRole(), "role");
	    require(r.getDepartment(), "department");
	    require(r.getExperience(), "experience");
	    require(r.getCount(), "count");
	    require(r.getYearWiseRateCartStartDate(), "yearWiseRateCartStartDate");
	    require(r.getYearWiseRateCartEndDate(), "yearWiseRateCartEndDate");
	    require(r.getLineItemStartDate(), "lineItemStartDate");
	    require(r.getLineItemEndDate(), "lineItemEndDate");
	}
	
	private void require(Object value, String fieldName) {
	    if (value == null) {
	        throw new RuntimeException(fieldName + " cannot be null");
	    }
	}

	
	


}
