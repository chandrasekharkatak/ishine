package com.apmosys.employeeportal.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.apmosys.employeeportal.dto.AutoMigrationDTO;
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
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.EmailTrigger;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.ExceptionUtils;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PoSyncOrchestratorService {

	private static final Logger log = LoggerFactory.getLogger(PoSyncOrchestratorService.class);

	private static final String OP_UPDATE_CLIENT_ADDRESS = "updateClientAddressIdOfPos";
	private static final String API_LOG_OPERATION = "updateClientAddrIdOfPoInIshine";
	private static final String PO_PORTAL_LOG_SOURCE = "PoPortal";
	private static final String MSG_PAYLOAD_MISSING = "Request payload is missing";
	private static final String MSG_PO_IDS_EMPTY = "PO IDs cannot be null or empty";
	private static final String MSG_CLIENT_ADDRESS_SUCCESS = "Client address updated successfully";

	private static final String OP_UPDATE_RM_IN_PO = "updateRmDetailsInPo";
	private static final String API_LOG_RM_UPDATE_OPERATION = "updateRmOfPoInIshine";
	private static final String MSG_RM_PO_IDS_NULL_ENTRY = "PO IDs list contains null value";
	private static final String MSG_RM_NO_ROWS_UPDATED = "No active PO records were updated for the given PO ID(s)";

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
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	TeamRepository teamRepository;

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
					throw new RuntimeException("Project does not exist");
				}
				ProjectPoDetails po = projectPoDetailsRepository
						.findByPoIdAndProjectIdAndActiveTrue(poDto.getPoId(), project.getProjectId()).orElseThrow(() -> {
							ExceptionLogContext.add("PO does not exist in iShine while update" + " | poId="
									+ poDto.getPoId() + " | poProjectId=" + dto.getProjectId());
							return new RuntimeException("PO not found in Ishine");
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
					
					List<AutoMigrationDTO> autoMigrated =
					        teamsService.migrateFromPreviousPOOnUpdate(
					                project.getProjectId(),
					                po.getPoId(),
					                poDto.getUpdatedByEmpId()
					        );
					
					if (!changes.isEmpty() || !autoMigrated.isEmpty()) {
						requirementService.sendRequirementChangeMail(po.getPoId(), changes , autoMigrated);
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

			boolean exists = projectPoDetailsRepository.existsByPoIdAndProjectIdAndActiveTrue(dto.getRenewedPo().getPoId(),
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
			response.setServiceResponse("PO renewed successfully");
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
	        
	        employeeTeamMapRepository.deleteScheduledEmployeesByPoId(deletedPo.getPoId());

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
		        
		        List<Long> teamIds = teamRepository.findActiveTeamIdsByProjectId(project.getProjectId());
		        
		        if (teamIds != null && !teamIds.isEmpty()) {
		        	employeeTeamMapRepository.deleteScheduledEmployeesByTeamIds(teamIds);
		        	
		        	 teamRepository.deactivateTeamsByProjectId(
		                     project.getProjectId(),
		                     dto.getDeletedOn().toInstant()
		                             .atZone(ZoneId.systemDefault())
		                             .toLocalDateTime(),
		                     dto.getDeletedByEmpId()
		             );
		        }
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
	        
	        if(dto.getPrimaryProject() == null || dto.getPrimaryProject().getProjectId() == null) {
	        	ExceptionLogContext.add("Primary project or po project ID is null ");
	            throw new RuntimeException("Project information is missing or not yet synchronized with iShine.");
	        }

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
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(
					poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					API_LOG_RM_UPDATE_OPERATION,
					PO_PORTAL_LOG_SOURCE,
					null,
					httpRequest);

			log.info("[{}] start path={}", OP_UPDATE_RM_IN_PO, sourceSystem);

			String validationError = validateRmUpdateOrchestratorRequest(dto);
			if (validationError != null) {
				log.warn("[{}] validation failed: {}", OP_UPDATE_RM_IN_PO, validationError);
				applyRmUpdateFailure(response, validationError);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			} else {
				validationService.validateRmUpdateSyncPayload(dto);

				List<Long> poIds = dto.getPoIds();
				Long rmEmpId = dto.getUpdatedApmosysRmEmpId();
				String rmEmpName = dto.getUpdatedApmosysRmEmpName();
				String rmEmail = dto.getUpdatedApmosysRmEmail();

				poDetailsService.validateAllPosAreActive(poIds);
				validationService.validateEmployeeExists(rmEmpId, rmEmpName);

				int updatedCount = projectPoDetailsRepository.updateRmForActivePos(poIds, rmEmpId, rmEmpName, rmEmail,
						rmEmpId);

				if (updatedCount <= 0) {
					log.warn("[{}] no rows updated poIds={} rmEmpId={}", OP_UPDATE_RM_IN_PO, poIds, rmEmpId);
					applyRmUpdateFailure(response, MSG_RM_NO_ROWS_UPDATED);
					finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				} else {
					applyRmUpdateSuccess(response, updatedCount);
					finalHttpStatusCode = HttpStatus.OK.value();
					log.info("[{}] success updatedCount={} poIds={} rmEmpId={} path={}", OP_UPDATE_RM_IN_PO,
							updatedCount, poIds, rmEmpId, sourceSystem);
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("[{}] illegal argument path={}", OP_UPDATE_RM_IN_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyRmUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
		} catch (DataAccessException e) {
			log.error("[{}] data access error path={} poIds={}", OP_UPDATE_RM_IN_PO, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyRmUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} catch (RuntimeException e) {
			log.error("[{}] business or validation failure path={} poIds={}", OP_UPDATE_RM_IN_PO, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyRmUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
		} catch (Exception e) {
			log.error("[{}] unexpected error path={}", OP_UPDATE_RM_IN_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyRmUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode,
						ExceptionLogContext.get(), httpRequest);
			}
		}
		return response;
	}

	/**
	 * @return error message if invalid, or {@code null} if basic request shape is valid
	 */
	private static String validateRmUpdateOrchestratorRequest(RmUpdateSyncDto dto) {
		if (dto == null) {
			return MSG_PAYLOAD_MISSING;
		}
		if (dto.getPoIds() == null || dto.getPoIds().isEmpty()) {
			return MSG_PO_IDS_EMPTY;
		}
		if (dto.getPoIds().stream().anyMatch(Objects::isNull)) {
			return MSG_RM_PO_IDS_NULL_ENTRY;
		}
		return null;
	}

	private static void applyRmUpdateSuccess(ServiceResponse response, int updatedCount) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("RM updated successfully for " + updatedCount + " PO(s)");
	}

	private static void applyRmUpdateFailure(ServiceResponse response, String message) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(message);
		response.setServiceError(message);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateClientAddressIdOfPos(PoClientAddressUpdateDTO dto) {
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(
					poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					API_LOG_OPERATION,
					PO_PORTAL_LOG_SOURCE,
					null,
					httpRequest);

			log.info("[{}] start path={}", OP_UPDATE_CLIENT_ADDRESS, sourceSystem);

			String validationMessage = validateUpdateClientAddressRequest(dto);
			if (validationMessage != null) {
				log.warn("[{}] validation failed: {}", OP_UPDATE_CLIENT_ADDRESS, validationMessage);
				applyClientAddressUpdateFailure(response, validationMessage);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			} else {
				validationService.validatePoClientAddressUpdatePayload(dto);

				validationService.validateEmployeeExists(dto.getUpdatedByEmpId(), dto.getUpdatedByEmpName());

				poDetailsService.validateAllPosAreActive(dto.getPoIds());

				poDetailsService.updateClientAddressForPos(dto);

				applyClientAddressUpdateSuccess(response);
				finalHttpStatusCode = HttpStatus.OK.value();
				log.info("[{}] success poIds={} clientAddressId={} path={}",
						OP_UPDATE_CLIENT_ADDRESS, dto.getPoIds(), dto.getClientAddressId(), sourceSystem);
			}
		} catch (IllegalArgumentException e) {
			log.error("[{}] illegal argument path={}", OP_UPDATE_CLIENT_ADDRESS, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
		} catch (DataAccessException e) {
			log.error("[{}] data access error path={} poIds={}", OP_UPDATE_CLIENT_ADDRESS, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} catch (RuntimeException e) {
			log.error("[{}] business rule or validation failure path={} poIds={}", OP_UPDATE_CLIENT_ADDRESS,
					sourceSystem, dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
		} catch (Exception e) {
			log.error("[{}] unexpected error path={}", OP_UPDATE_CLIENT_ADDRESS, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode,
						ExceptionLogContext.get(), httpRequest);
			}
		}
		return response;
	}

	/**
	 * @return error message if invalid, or {@code null} if basic request shape is valid
	 */
	private static String validateUpdateClientAddressRequest(PoClientAddressUpdateDTO dto) {
		if (dto == null) {
			return MSG_PAYLOAD_MISSING;
		}
		if (dto.getPoIds() == null || dto.getPoIds().isEmpty()) {
			return MSG_PO_IDS_EMPTY;
		}
		if (dto.getPoIds().stream().anyMatch(Objects::isNull)) {
			return "PO IDs cannot contain null entries";
		}
		return null;
	}

	private static void applyClientAddressUpdateSuccess(ServiceResponse response) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(MSG_CLIENT_ADDRESS_SUCCESS);
	}

	private static void applyClientAddressUpdateFailure(ServiceResponse response, String message) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(message);
		response.setServiceError(message);
	}

	private static String buildRequestPathForLogging(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		return (query != null && !query.isEmpty()) ? uri + "?" + query : uri;
	}

}
