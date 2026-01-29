package com.apmosys.employeeportal.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.ClientsRepository;
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
	ClientsRepository clientRepository;

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
				"poCrudOperationsInIshineNew", "PoPortal", null, httpRequest);

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
					throw new RuntimeException(
							"Resource requirement list is mandatory for TNM projects ,missing from PO");
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
					ExceptionLogContext.add(
							"Project does not exist in ishine while update po for poProjectId = " + dto.getProjectId());
					throw new RuntimeException("Project does not exist for poProjectId=" + dto.getProjectId());
				}
				ProjectPoDetails po = projectPoDetailsRepository
						.findByPoIdAndProjectId(poDto.getPoId(), project.getProjectId()).orElseThrow(() -> {
							ExceptionLogContext.add("PO does not exist in iShine while update" + " | poId="
									+ poDto.getPoId() + " | poProjectId=" + dto.getProjectId());
							return new RuntimeException("PO does not exist for poId=" + poDto.getPoId());
						});

				boolean projectChanged = projectService.updateProjectIfChanged(project, dto, client);

				boolean poChanged = poDetailsService.updatePoIfChanged(po, dto, client);

				departmentService.syncDepartmentsRTS(po.getPoId(), poDto.getDepartmentList());

				if (poDto.getResourceRequirementList() != null) {
					requirementService.syncRequirementsRTS(po.getPoId(), poDto.getResourceRequirementList());
				}
			} else {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("Unsupported eventType from Po " + dto.getEventType());
				throw new RuntimeException("Unsupported eventType");

			}
			finalHttpStatusCode = HttpStatus.OK.value();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("PO sync successful");
			return response;
		} catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
//			return response;
			throw e;

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
			ExceptionLogContext.add("poDetailsList cannot be null or empty from PO");
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
			ExceptionLogContext.add("departmentList cannot be null or empty from po");
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

			if (poDto.getResourceRequirementList() == null || poDto.getResourceRequirementList().isEmpty()) {
				throw new RuntimeException("resourceRequirementList is mandatory for TNM projects");
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
			throw new RuntimeException(fieldName + " cannot be null from PO");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse renewPoInIshineNew(RenewedPoSyncDto dto) {

		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURI().toString();

		ServiceResponse response = new ServiceResponse();

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"renewPoInIshine", "PoPortal", null, httpRequest);

			validateRenewPoPayload(dto);

			if (dto.getEventType() != SyncRequestType.RENEW_PO) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new RuntimeException("Invalid eventType for renew PO");
			}

			Project project = projectRepository.findByPoProjectId(dto.getProjectId());

			if (project == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("Project not found during PO renewal | poProjectId=" + dto.getProjectId());
				throw new RuntimeException("Project does not exist for renewal");
			}
			
			if(project.getPoProjectType().equalsIgnoreCase("TNM")) {
				if(dto.getRenewedPo().getResourceRequirementList() == null || dto.getRenewedPo().getResourceRequirementList().isEmpty() )
				{
					finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
					throw new RuntimeException(
							"Renewed PO is TNM with no rsrc req from po" + dto.getRenewedPo().getPoId());	
				}
			}

			projectService.updateProjectDatesAfterRenewal(project, dto);

			boolean exists = projectPoDetailsRepository.existsByPoIdAndProjectId(dto.getRenewedPo().getPoId(),
					project.getProjectId());

			if (exists) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new RuntimeException(
						"Renewed PO already exists in system | poId=" + dto.getRenewedPo().getPoId());
			}

			Client client = clientRepository.findByClientId(project.getClientId());
			ProjectPoDetails newPo = poDetailsService.createRenewedPo(project, dto, client);
			departmentService.syncDepartmentsRTS(newPo.getPoId(), dto.getRenewedPo().getDepartmentList());

			if (dto.getRenewedPo().getResourceRequirementList() != null) {
				requirementService.syncRequirementsRTS(newPo.getPoId(),
						dto.getRenewedPo().getResourceRequirementList());
			}

			poDetailsService.validateAssociatedPosIntegrity(project.getProjectId(), dto.getAssociatePosAfterRenewal());

			poDetailsService.updatePoLinksAfterRenewal(project.getProjectId(), dto);

			finalHttpStatusCode = HttpStatus.OK.value();

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("PO sync successful");
			return response;

		} catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
//			return response;
			throw e;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
		}

	}
	
	private void validateRenewPoPayload(RenewedPoSyncDto dto) {

	    require(dto.getEventType(), "eventType");
	    require(dto.getProjectId(), "projectId");
	    require(dto.getProjectName(), "projectName");
	    require(dto.getRenewedByEmpId(), "renewedByEmpId");
	    require(dto.getRenewedByEmpName(), "renewedByEmpName");
	    require(dto.getRenewedOn(), "renewedOn");
//	    require(dto.getRenewedPo(), "renewedPo");

	    if (dto.getAssociatePosAfterRenewal() == null ||
	        dto.getAssociatePosAfterRenewal().isEmpty()) {
	        throw new RuntimeException("associatePosAfterRenewal cannot be empty from Po");
	    }
	    
	    if (dto.getRenewedPo() == null ) {
		        throw new RuntimeException("Renewed PO cannot be empty from PO");
		    }
	    
	    PoDetailsForProjectPoMappingDTO poDto = dto.getRenewedPo();
	    
	    
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
			ExceptionLogContext.add("departmentList cannot be null or empty from po");
			throw new RuntimeException("departmentList cannot be null or empty");
		}
		
		  if(poDto.getResourceRequirementList()!= null) {

			for (POResourceRequirementDTO r : poDto.getResourceRequirementList()) {
				validateResourceRequirement(r);
			}
		  }

		
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deletePoInIshineNew(DeletedPoSyncDTO dto) {

	    ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String sourceSystem = httpRequest.getRequestURI().toString();
	    ServiceResponse response = new ServiceResponse();

	    try {
	        initialLog = apiLogUtility.startLog(
	                poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
	                "deletePoInIshine",
	                "PoPortal",
	                null,
	                httpRequest
	        );

//	        validateDeletePoPayload(dto);

	        if (dto.getEventType() != SyncRequestType.DELETE_PO) {
	            throw new RuntimeException("Invalid eventType for delete PO");
	        }

	        Project project =
	                projectRepository.findByPoProjectId(dto.getProjectId());

	        if (project == null) {
	            ExceptionLogContext.add(
	                    "Project not found during PO deletion | poProjectId=" + dto.getProjectId()
	            );
	            throw new RuntimeException("Project does not exist for PO deletion");
	        }

	      
	        poDetailsService.validateAssociatedPosIntegrity(
	                project.getProjectId(),
	                dto.getAssociatePos()
	        );

	        
	        ProjectPoDetails deletedPo =
	                poDetailsService.validateDeletedPoExists(
	                        project.getProjectId(),
	                        dto.getDeletedPo().getPoId()
	                );

	       
	        poDetailsService.validateNoActiveTeamsForPo(
	                deletedPo.getPoId()
	        );

	       
	        poDetailsService.softDeletePo(
	                deletedPo,
	                dto.getDeletedByEmpId(),
	                dto.getDeletedByEmpName(),
	                dto.getDeletedOn()
	        );

	      
	        if (dto.getAssociatePos() != null && !dto.getAssociatePos().isEmpty()) {
	            poDetailsService.updatePoLinksAfterDeletion(
	                    project.getProjectId(),
	                    dto
	            );
	        }

	      //when no associated po and the delte po is also delted
	        projectService.setActiveFlagAsFalse(project,dto);
	       

	       
	        projectService.updateProjectDatesAfterDeletion(
	                project,
	                dto
	        );

	        finalHttpStatusCode = HttpStatus.OK.value();
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("PO deleted successfully");
	        return response;

	    } catch (Exception e) {
	    	ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
//			return response;
			throw e;
	    } finally {
	        if (initialLog != null) {
	            apiLogUtility.endLog(
	                    initialLog.getId(),
	                    sourceSystem,
	                    finalHttpStatusCode,
	                    ExceptionLogContext.get(),
	                    httpRequest
	            );
	        }
	    }
	}



}
