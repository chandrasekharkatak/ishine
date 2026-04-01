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
import com.apmosys.employeeportal.exception.DataNotFoundException;
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

	private static final String OP_RENEW_PO = "renewPoInIshineNew";
	private static final String API_LOG_RENEW_OPERATION = "renewPoInIshine";
	private static final String MSG_RENEW_SUCCESS = "PO renewed successfully";
	private static final String MSG_RENEW_INVALID_EVENT = "Invalid eventType for renew PO";

	private static final String OP_DELETE_PO = "deletePoInIshineNew";
	private static final String API_LOG_DELETE_OPERATION = "deletePoInIshine";
	private static final String MSG_DELETE_SUCCESS = "PO deleted successfully";
	private static final String MSG_DELETE_INVALID_EVENT = "Invalid eventType for delete PO";

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
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					API_LOG_RENEW_OPERATION, PO_PORTAL_LOG_SOURCE, null, httpRequest);

			log.info("[{}] start path={}", OP_RENEW_PO, sourceSystem);

			if (dto == null) {
				log.warn("[{}] request body is null path={}", OP_RENEW_PO, sourceSystem);
				applyRenewPoFailure(response, MSG_PAYLOAD_MISSING);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			} else {
				validationService.validateRenewPoPayload(dto);

				if (dto.getEventType() != SyncRequestType.RENEW_PO) {
					log.warn("[{}] invalid eventType={} path={}", OP_RENEW_PO, dto.getEventType(), sourceSystem);
					applyRenewPoFailure(response, MSG_RENEW_INVALID_EVENT);
					finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				} else {
					finalHttpStatusCode = renewPoCore(dto, response, sourceSystem);
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("[{}] illegal argument path={}", OP_RENEW_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyRenewPoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (DataAccessException e) {
			log.error("[{}] data access error path={} projectId={}", OP_RENEW_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyRenewPoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (RuntimeException e) {
			log.error("[{}] business or validation failure path={} projectId={}", OP_RENEW_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyRenewPoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (Exception e) {
			log.error("[{}] unexpected error path={}", OP_RENEW_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyRenewPoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
		}

		return response;
	}

	/**
	 * Assumes {@code dto} is non-null, validated, and {@link SyncRequestType#RENEW_PO}.
	 *
	 * @return HTTP status code for logging (OK or BAD_REQUEST on controlled failures)
	 */
	private int renewPoCore(RenewedPoSyncDto dto, ServiceResponse response, String sourceSystem) {
		Project project = projectRepository.findByPoProjectId(dto.getProjectId());
		if (project == null) {
			String detail = "Project not found during PO renewal | poProjectId=" + dto.getProjectId();
			ExceptionLogContext.add(detail);
			log.warn("[{}] {} path={}", OP_RENEW_PO, detail, sourceSystem);
			applyRenewPoFailure(response, "Project does not exist for renewal");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return HttpStatus.BAD_REQUEST.value();
		}

		PoDetailsForProjectPoMappingDTO renewedPo = dto.getRenewedPo();
		if (renewedPo == null) {
			log.warn("[{}] renewedPo is null after validation path={}", OP_RENEW_PO, sourceSystem);
			applyRenewPoFailure(response, "Renewed PO cannot be empty from PO");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return HttpStatus.BAD_REQUEST.value();
		}

		if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
			List<POResourceRequirementDTO> resourceRequirementList = renewedPo.getResourceRequirementList();
			if (resourceRequirementList == null || resourceRequirementList.isEmpty()) {
				String msg = "Renewed PO is TNM with no rsrc req from po" + renewedPo.getPoId();
				ExceptionLogContext.add(msg);
				log.warn("[{}] {} path={}", OP_RENEW_PO, msg, sourceSystem);
				applyRenewPoFailure(response, msg);
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return HttpStatus.BAD_REQUEST.value();
			}
		}

		Long renewedPoId = renewedPo.getPoId();
		boolean exists = projectPoDetailsRepository.existsByPoIdAndProjectIdAndActiveTrue(renewedPoId,
				project.getProjectId());
		if (exists) {
			String detail = "Renewed PO already exists in system | poId=" + renewedPoId;
			ExceptionLogContext.add(detail);
			log.warn("[{}] {} path={}", OP_RENEW_PO, detail, sourceSystem);
			applyRenewPoFailure(response, "Renewed PO already exists in system | poId=" + renewedPoId);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return HttpStatus.BAD_REQUEST.value();
		}

		Client client = clientRepository.findByClientId(project.getClientId());
		if (client == null) {
			log.warn("[{}] client not found clientId={} path={}", OP_RENEW_PO, project.getClientId(), sourceSystem);
			applyRenewPoFailure(response, "Client not found for clientId: " + project.getClientId());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return HttpStatus.BAD_REQUEST.value();
		}

		ProjectPoDetails newPo = poDetailsService.createRenewedPo(project, dto, client);

		departmentService.syncDepartmentsRTS(newPo.getPoId(), renewedPo.getDepartmentList(), project.getProjectId(),
				dto.getRenewedByEmpId());

		if (renewedPo.getResourceRequirementList() != null) {
			requirementService.syncRequirementsRTS(newPo.getPoId(), renewedPo.getResourceRequirementList(),
					dto.getRenewedByEmpId());
		}

		poDetailsService.validateAssociatedPosIntegrity(project.getProjectId(), dto.getAssociatePosAfterRenewal());
		poDetailsService.updatePoLinksAfterRenewal(project.getProjectId(), dto);
		teamsService.migrateResourcesAfterRenewal(project.getProjectId(), newPo.getPoId(), dto.getRenewedByEmpId());
		projectService.recalculateProjectDates(project.getProjectId(), true);

		applyRenewPoSuccess(response);
		log.info("[{}] success projectId={} newPoId={} path={}", OP_RENEW_PO, dto.getProjectId(), newPo.getPoId(),
				sourceSystem);
		return HttpStatus.OK.value();
	}

	private static void applyRenewPoSuccess(ServiceResponse response) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(MSG_RENEW_SUCCESS);
	}

	private static void applyRenewPoFailure(ServiceResponse response, String message) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(message);
		response.setServiceError(message);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deletePoInIshineNew(DeletedPoSyncDTO dto) {
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					API_LOG_DELETE_OPERATION, PO_PORTAL_LOG_SOURCE, null, httpRequest);

			log.info("[{}] start path={}", OP_DELETE_PO, sourceSystem);

			if (dto == null) {
				log.warn("[{}] request body is null path={}", OP_DELETE_PO, sourceSystem);
				applyDeletePoFailure(response, MSG_PAYLOAD_MISSING);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			} else {
				validationService.validateDeletePoPayload(dto);

				if (!SyncRequestType.DELETE_PO.equals(dto.getEventType())) {
					ExceptionLogContext.add("Invalid eventType for delete PO");
					log.warn("[{}] invalid eventType={} path={}", OP_DELETE_PO, dto.getEventType(), sourceSystem);
					applyDeletePoFailure(response, MSG_DELETE_INVALID_EVENT);
					finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				} else {
					finalHttpStatusCode = deletePoCore(dto, response, sourceSystem);
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("[{}] validation failed path={}", OP_DELETE_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (DataNotFoundException e) {
			log.error("[{}] resource not found path={} projectId={}", OP_DELETE_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (IllegalStateException e) {
			log.error("[{}] conflict or invalid state path={} projectId={}", OP_DELETE_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.CONFLICT.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (DataAccessException e) {
			log.error("[{}] data access error path={} projectId={}", OP_DELETE_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (RuntimeException e) {
			log.error("[{}] business rule failure path={} projectId={}", OP_DELETE_PO, sourceSystem,
					dto != null ? dto.getProjectId() : null, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (Exception e) {
			log.error("[{}] unexpected error path={}", OP_DELETE_PO, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyDeletePoFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
		}

		return response;
	}

	/**
	 * Assumes {@code dto} is non-null, validated, and {@link SyncRequestType#DELETE_PO}.
	 */
	private int deletePoCore(DeletedPoSyncDTO dto, ServiceResponse response, String sourceSystem) {
		Project project = projectRepository.findByPoProjectId(dto.getProjectId());
		if (project == null) {
			String detail = "Project not found during PO deletion | poProjectId=" + dto.getProjectId();
			ExceptionLogContext.add(detail);
			log.warn("[{}] {} path={}", OP_DELETE_PO, detail, sourceSystem);
			throw new DataNotFoundException("Project does not exist for PO deletion");
		}

		PoDetailsForProjectPoMappingDTO deletedPoDto = dto.getDeletedPo();
		Long deletedPoId = deletedPoDto.getPoId();

		ProjectPoDetails deletedPo = poDetailsService.validateDeletedPoExists(project.getProjectId(), deletedPoId);

		poDetailsService.validateNoActiveTeamsForPo(deletedPo.getPoId());

		poDetailsService.softDeletePo(deletedPo, dto.getDeletedByEmpId(), dto.getDeletedByEmpName(),
				dto.getDeletedOn());

		employeeTeamMapRepository.deleteScheduledEmployeesByPoId(deletedPo.getPoId());

		poDetailsService.validateAssociatedPosIntegrity(project.getProjectId(), dto.getAssociatePos());

		if (dto.getAssociatePos() != null && !dto.getAssociatePos().isEmpty()) {
			poDetailsService.updatePoLinksAfterDeletion(project.getProjectId(), dto);
		}

		if (dto.getAssociatePos() == null || dto.getAssociatePos().isEmpty()) {
			projectService.setActiveFlagAsFalse(project, dto);

			List<Long> teamIds = teamRepository.findActiveTeamIdsByProjectId(project.getProjectId());

			if (teamIds != null && !teamIds.isEmpty()) {
				employeeTeamMapRepository.deleteScheduledEmployeesByTeamIds(teamIds);

				teamRepository.deactivateTeamsByProjectId(project.getProjectId(),
						dto.getDeletedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
						dto.getDeletedByEmpId());
			}
		}

		projectService.recalculateProjectDates(project.getProjectId(), false);

		applyDeletePoSuccess(response);
		log.info("[{}] success projectId={} deletedPoId={} path={}", OP_DELETE_PO, dto.getProjectId(), deletedPoId,
				sourceSystem);
		return HttpStatus.OK.value();
	}

	private static void applyDeletePoSuccess(ServiceResponse response) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(MSG_DELETE_SUCCESS);
	}

	private static void applyDeletePoFailure(ServiceResponse response, String message) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(message);
		response.setServiceError(message);
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
