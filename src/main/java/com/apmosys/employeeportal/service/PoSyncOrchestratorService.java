package com.apmosys.employeeportal.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.RequirementChangeDTO;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.EmailTrigger;
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
	
	@Autowired
	private ValidationService validationService;
	
	@Autowired
	private TeamsService teamsService;
	
	@Autowired
	private ResourceManagementService resourceManagementService;

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse poCrudOperationsInIshineNew(ProjectPoMappingWithResourceDTO dto) {

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/poCrudOperationsInIshine");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = new ApiLog();
		StringBuilder exceptionDetailsForLog = new StringBuilder();
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"poCrudOperationsInIshineNew", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();
		ServiceResponse response = new ServiceResponse();

		try {
			
			if (dto == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("DTO from PO portal is null");
				throw new RuntimeException("DTO from PO portal is null");
			}

			validationService.validateIncomingPayload(dto);

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

			Client client = clientService.resolveClient(dto.getClientName(),dto.getClientId());
			
			

			if (dto.getEventType() == SyncRequestType.CREATE_PROJECT) {

				Project project = projectService.createProjectRTS(dto, client);
				ProjectPoDetails po = poDetailsService.createPoRTS(project, dto, client);
				
				
				departmentService.syncDepartmentsRTS(po.getPoId(), dto.getPoDetailsList().get(0).getDepartmentList(),project.getProjectId(),poDto.getCreatedByEmpId());

				if (poDto.getResourceRequirementList() != null) {
					requirementService.syncRequirementsRTS(po.getPoId(),
							dto.getPoDetailsList().get(0).getResourceRequirementList(),poDto.getCreatedByEmpId());
				}
				
				projectService.recalculateProjectDates(project.getProjectId(),false);

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
				
		
				departmentService.syncDepartmentsRTS(po.getPoId(), poDto.getDepartmentList(),project.getProjectId(),poDto.getUpdatedByEmpId());

				if (poDto.getResourceRequirementList() != null) {
					 List<RequirementChangeDTO> changes =
					            requirementService.detectRequirementChanges(
					                    po.getPoId(),
					                    poDto.getResourceRequirementList());

					
					requirementService.syncRequirementsRTS(po.getPoId(), poDto.getResourceRequirementList(),poDto.getUpdatedByEmpId());
					
					if (!changes.isEmpty()) {
						requirementService.sendRequirementChangeMail(po.getPoId(), changes);
				    }
				}
				
				projectService.recalculateProjectDates(project.getProjectId(),false);
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
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
			return response;
//			throw e;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
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

			validationService.validateRenewPoPayload(dto);

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
					ExceptionLogContext.add("Renewed PO is TNM with no rsrc req from po" + dto.getRenewedPo().getPoId());
					throw new RuntimeException(
							"Renewed PO is TNM with no rsrc req from po" + dto.getRenewedPo().getPoId());	
				}
			}

//			projectService.updateProjectDatesAfterRenewal(project, dto);

			boolean exists = projectPoDetailsRepository.existsByPoIdAndProjectId(dto.getRenewedPo().getPoId(),
					project.getProjectId());

			if (exists) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				ExceptionLogContext.add("Renewed PO already exists in system | poId=" + dto.getRenewedPo().getPoId());
				throw new RuntimeException(
						"Renewed PO already exists in system | poId=" + dto.getRenewedPo().getPoId());
			}

			Client client = clientRepository.findByClientId(project.getClientId());
			ProjectPoDetails newPo = poDetailsService.createRenewedPo(project, dto, client);
			
			
			departmentService.syncDepartmentsRTS(newPo.getPoId(), dto.getRenewedPo().getDepartmentList(),project.getProjectId(),dto.getRenewedByEmpId());

			if (dto.getRenewedPo().getResourceRequirementList() != null) {
				
				requirementService.syncRequirementsRTS(newPo.getPoId(),
						dto.getRenewedPo().getResourceRequirementList(),dto.getRenewedByEmpId());
			}

			poDetailsService.validateAssociatedPosIntegrity(project.getProjectId(), dto.getAssociatePosAfterRenewal());

			poDetailsService.updatePoLinksAfterRenewal(project.getProjectId(), dto);
			
			teamsService.migrateResourcesAfterRenewal(project.getProjectId(),newPo.getPoId(),dto.getRenewedByEmpId());
			
			projectService.recalculateProjectDates(project.getProjectId(),true);
			
			//when project state is alredy completed after  renew the project status should change to
			finalHttpStatusCode = HttpStatus.OK.value();

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("PO sync successful");
			return response;

		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
			return response;
//			throw e;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
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
	            ExceptionLogContext.add("Invalid eventType for delete PO");
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

	        poDetailsService.validateAssociatedPosIntegrity(
	                project.getProjectId(),
	                dto.getAssociatePos()
	        );
	        
	        if (dto.getAssociatePos() != null && !dto.getAssociatePos().isEmpty()) {
	            poDetailsService.updatePoLinksAfterDeletion(
	                    project.getProjectId(),
	                    dto
	            );
	        }

	      //when no associated po and the delte po is also delted
	        if(dto.getAssociatePos() == null || dto.getAssociatePos().isEmpty()) {
		        projectService.setActiveFlagAsFalse(project,dto);
	        }
	        
	        projectService.recalculateProjectDates(project.getProjectId(),false);
	       

	       
//	        projectService.updateProjectDatesAfterDeletion(
//	                project,
//	                dto
//	        );

	        finalHttpStatusCode = HttpStatus.OK.value();
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("PO deleted successfully");
	        return response;

	    } catch (Exception e) {
	    	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	    	ExceptionLogContext.add(e);
			e.printStackTrace();
//			exceptionDetailsForLog.append(e.printStackTrace());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
			return response;
//			throw e;
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
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse linkPoInIshineNew(IshineLinkProjectDto dto) {

	    ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String sourceSystem = httpRequest.getRequestURI().toString();
	    ServiceResponse response = new ServiceResponse();
	    String ishineStatus ="" ;
	    

	    try {
	        initialLog = apiLogUtility.startLog(
	                poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
	                "linkPoInIshine",
	                "PoPortal",
	                null,
	                httpRequest
	        );

	        // Primary project must exist
	        Project primaryProject =
	                projectRepository.findByPoProjectId(
	                        dto.getPrimaryProject().getProjectId());

	        if (primaryProject == null) {
	            ExceptionLogContext.add(
	                    "Primary project not found | poProjectId="
	                            + dto.getPrimaryProject().getProjectId());
	            throw new RuntimeException("Primary project does not exist");
	        }

	        if (dto.getDeletedProjects() == null || dto.getDeletedProjects().isEmpty()) {

	            poDetailsService.updatePoOrderOnly(
	                    primaryProject.getProjectId(),
	                    dto.getPrimaryProject()
	            );
	            
	            ishineStatus = dto.getPrimaryProject().getIshineProjectStatus();
	        }

	        else {

	            poDetailsService.validatePoLinkIntegrity(
	                    primaryProject,
	                    dto
	            );

	            poDetailsService.movePosToPrimaryProject(
	                    primaryProject,
	                    dto
	            );
	            
	            poDetailsService.validateAssociatedPosIntegrity(
	                    primaryProject.getProjectId(),
	                    dto.getPrimaryProject().getPoDetailsList()
	            );
	            
	            
	            poDetailsService.liftAndShiftTeamNew(dto);

	            projectService.deactivateDeletedProjects(
	                    dto.getDeletedProjects()
	            );

	            poDetailsService.updatePoOrderOnly(
	                    primaryProject.getProjectId(),
	                    dto.getPrimaryProject()
	            );

		        projectService.recalculateProjectDates(primaryProject.getProjectId(),false);
	            
	           ishineStatus = resourceManagementService.ishineStatusReturn( dto.getDeletedProjects(),primaryProject);
	           
	           EmailTrigger.sendAfterCommit(() ->
	           		poDetailsService.sendPoLinkSuccessMail(primaryProject, dto)
		       );

	        }

	        finalHttpStatusCode = HttpStatus.OK.value();
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("PO linking successful");
	        response.setServiceResponse1(ishineStatus);
	        return response;

	    } catch (Exception e) {
	    	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	        ExceptionLogContext.add(e);
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	        return response;
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
	
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateRmOdPos(RmUpdateSyncDto dto) {

	    ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String sourceSystem = httpRequest.getRequestURI().toString();
	    ServiceResponse response = new ServiceResponse();
	    
	    

	    try {
	        initialLog = apiLogUtility.startLog(
	                poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
	                "updateRmOfPoInIshine",
	                "PoPortal",
	                null,
	                httpRequest
	        );
	        validationService.validateRmUpdateSyncPayload(dto);
	        poDetailsService.validateAllPosAreActive(dto.getPoIds());
	        validationService.validateEmployeeExists(
	                dto.getUpdatedApmosysRmEmpId(),
	                dto.getUpdatedApmosysRmEmpName()
	        );
	        
	       
	        int updatedCount = projectPoDetailsRepository.updateRmForActivePos(
	                dto.getPoIds(),
	                dto.getUpdatedApmosysRmEmpId(),
	                dto.getUpdatedApmosysRmEmpName(),
	                dto.getUpdatedApmosysRmEmail(),
	                dto.getUpdatedApmosysRmEmpId()
	                
	        );
	        
	        if (updatedCount != dto.getPoIds().size()) {
	            throw new RuntimeException(" PO records updation failed");
	        }

	        finalHttpStatusCode = HttpStatus.OK.value();
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(
	                "RM updated successfully for " + updatedCount + " PO(s)"
	        );
	        return response;
	        
	       
	    }catch (Exception e) {
	    	finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
	        ExceptionLogContext.add(e);
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	        return response;
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
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateClientAddressIdOfPos(PoClientAddressUpdateDTO dto) {
		ApiLog initialLog = null;
	    int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	    String sourceSystem = httpRequest.getRequestURI().toString();
	    ServiceResponse response = new ServiceResponse();
	    try {
	    	
	    	 initialLog = apiLogUtility.startLog(
		                poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
		                "updateClientAddrIdOfPoInIshine",
		                "PoPortal",
		                null,
		                httpRequest
		        );
	    	 
	    	 
	    	 validationService.validatePoClientAddressUpdatePayload(dto);
	    	 
	    	 validationService.validateEmployeeExists(
	                 dto.getUpdatedByEmpId(),
	                 dto.getUpdatedByEmpName()
	         );
	    	 
	    	 poDetailsService.validateAllPosAreActive(dto.getPoIds());
	    	 
	    	 
	    	 poDetailsService.updateClientAddressForPos(dto);
	    	

	         finalHttpStatusCode = HttpStatus.OK.value();
	         response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	         response.setServiceResponse("Client address updated successfully");

	         return response;
	    	
	    }catch (Exception e) {
	    	finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
	        ExceptionLogContext.add(e);
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	        return response;
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
