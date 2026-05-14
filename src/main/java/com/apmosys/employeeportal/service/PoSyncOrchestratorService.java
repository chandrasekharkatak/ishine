package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.AutoMigrationDTO;
import com.apmosys.employeeportal.dto.ClientDetailsSyncDto;
import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RmgTeamDto;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.RequirementChangeDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.service.ClientPoPortalSyncTransactionalService.OneClientSyncOutcome;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.EmailTrigger;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.ExceptionUtils;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class PoSyncOrchestratorService {

	private static final Logger log = LoggerFactory.getLogger(PoSyncOrchestratorService.class);

	/** Log / trace name aligned with REST path {@code /api/updateAddressInPos}. */
	private static final String OP_UPDATE_ADDRESS_IN_POS = "updateAddressInPos";
	private static final String API_LOG_UPDATE_ADDRESS_IN_POS = "updateAddressInPos";
	private static final String PO_PORTAL_LOG_SOURCE = "PoPortal";
	private static final String MSG_PAYLOAD_MISSING = "Request payload is missing";
	private static final String MSG_PO_IDS_EMPTY = "PO IDs cannot be null or empty";
	private static final String MSG_CLIENT_ADDR_PO_IDS_NULL_ENTRY = "PO IDs cannot contain null entries";
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
	
	private static final String SYNC_CLIENT_FROM_PO_CRON_ENDPOINT = "syncClientFromPoCron";


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
	CronJobService cronJobService;
	
	@Value("${exceptiondataReconcile.maildev}")
	private String exceptionMaildev;
	
	@Value("${exceptiondataReconcile.mailplsql}")
	private String exceptionMailplsql;

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
	MailService mailService;

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
	
	@Value("${po.portal.client.sync.cron.enabled:true}")
	private boolean poPortalClientSyncCronEnabled;

	@Value("${poPortal.api.syncCLientDetails}")
	private String poPortalSyncClientDetailsUrl;

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ClientPoPortalSyncTransactionalService clientPoPortalSyncTransactionalService;


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
		List<AutoMigrationDTO> autoMigrated =
		        teamsService.migrateResourcesAfterRenewalDTO(
		                project.getProjectId(),
		                newPo.getPoId(),
		                dto.getRenewedByEmpId()
		        );

		if (!autoMigrated.isEmpty()) {
			cronJobService.sendAutoMigrationMail(autoMigrated);
		}
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
	public ServiceResponse updateAddressInPos(PoClientAddressUpdateDTO dto) {
		ServiceResponse response = new ServiceResponse();
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(
					poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					API_LOG_UPDATE_ADDRESS_IN_POS,
					PO_PORTAL_LOG_SOURCE,
					null,
					httpRequest);

			log.info("[{}] start path={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem);

			String validationMessage = validateUpdateClientAddressRequest(dto);
			if (validationMessage != null) {
				log.warn("[{}] validation failed: {}", OP_UPDATE_ADDRESS_IN_POS, validationMessage);
				applyClientAddressUpdateFailure(response, validationMessage);
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			} else {
				validationService.validatePoClientAddressUpdatePayload(dto);

				validationService.validateEmployeeExists(dto.getUpdatedByEmpId(), dto.getUpdatedByEmpName());

				poDetailsService.validateAllPosAreActive(dto.getPoIds());

				poDetailsService.updateClientAddressForPos(dto);

				applyClientAddressUpdateSuccess(response);
				finalHttpStatusCode = HttpStatus.OK.value();
				log.info("[{}] success poIds={} clientAddressId={} path={}",
						OP_UPDATE_ADDRESS_IN_POS, dto.getPoIds(), dto.getClientAddressId(), sourceSystem);
			}
		} catch (IllegalArgumentException e) {
			log.warn("[{}] invalid input path={} reason={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem,
					ExceptionUtils.getExceptionMessage(e));
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (IllegalStateException e) {
			log.error("[{}] inconsistent state path={} poIds={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.CONFLICT.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (DataAccessException e) {
			log.error("[{}] data access error path={} poIds={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (RuntimeException e) {
			log.error("[{}] business rule failure path={} poIds={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem,
					dto != null ? dto.getPoIds() : null, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (Exception e) {
			log.error("[{}] unexpected error path={}", OP_UPDATE_ADDRESS_IN_POS, sourceSystem, e);
			ExceptionLogContext.add(e);
			applyClientAddressUpdateFailure(response, ExceptionUtils.getExceptionMessage(e));
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
			return MSG_CLIENT_ADDR_PO_IDS_NULL_ENTRY;
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
	
	public void syncClientsFromPoPortalCron() {
		if (!poPortalClientSyncCronEnabled) {
			log.debug("syncClientsFromPoPortalCron skipped: po.portal.client.sync.cron.enabled=false");
			return;
		}
		

		String traceId = UUID.randomUUID().toString();
		ExceptionLogContext.clear();

		HttpServletRequest cronHttpRequest = null;
		String sourceSystem = httpRequest.getRequestURI().toString();

		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		StringBuilder failureDigest = new StringBuilder();

		int clientsEligible = 0;
		int clientsProcessed = 0;
		int clientsFailed = 0;
		int totalSyncedAddresses = 0;
		int totalSkippedAddresses = 0;
		int totalDeactivated = 0;

		try {
			initialLog = apiLogUtility.startLog(
				    traceId,
				    SYNC_CLIENT_FROM_PO_CRON_ENDPOINT,
				    PO_PORTAL_LOG_SOURCE,
				    null,
				    httpRequest
				);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			log.info("[syncClientsFromPoPortalCron] traceId={} calling PO client sync API", traceId);
			ResponseEntity<ClientDetailsSyncDto[]> responseEntity = restTemplate.exchange(poPortalSyncClientDetailsUrl,
					HttpMethod.GET, requestEntity, ClientDetailsSyncDto[].class);

			finalHttpStatusCode = responseEntity.getStatusCodeValue();
			ClientDetailsSyncDto[] body = responseEntity.getBody();
			List<ClientDetailsSyncDto> poClients = body == null ? new ArrayList<>() : Arrays.asList(body);

			Map<Long, ClientDetailsSyncDto> clientsToSync = buildClientsToSyncForPoPortalCron(poClients, traceId,
					failureDigest);
			clientsEligible = clientsToSync.size();

			if (clientsToSync.isEmpty()) {
				log.warn("[syncClientsFromPoPortalCron] traceId={} no eligible clients after duplicate filter (raw={})",
						traceId, poClients.size());
				finalHttpStatusCode = HttpStatus.OK.value();
				return;
			}

			for (ClientDetailsSyncDto poDto : clientsToSync.values()) {
				try {
					OneClientSyncOutcome outcome = clientPoPortalSyncTransactionalService.syncOneClientFromPo(poDto);
					clientsProcessed++;
					totalSyncedAddresses += outcome.syncedAddresses;
					totalSkippedAddresses += outcome.skippedAddresses;
					totalDeactivated += outcome.deactivatedLocations;
					if (log.isDebugEnabled()) {
						log.debug(
								"[syncClientsFromPoPortalCron] traceId={} poClientId={} syncedAddr={} skippedAddr={} deactivated={}",
								traceId, poDto.getClientid(), outcome.syncedAddresses, outcome.skippedAddresses,
								outcome.deactivatedLocations);
					}
				} catch (Exception ex) {
					clientsFailed++;
					String label = poDto.getClientid() + ":" + poDto.getClientName();

			        String cleanError = buildClearErrorMessage(ex, poDto);

			        failureDigest.append(label)
			                .append(" → ")
			                .append(cleanError)
			                .append(" || ");
					ExceptionLogContext.add(ex);
					
					log.error("[syncClientsFromPoPortalCron] traceId={} failed poClientId={} name={}", traceId,
							poDto.getClientid(), poDto.getClientName(), ex);
				}
			}

			finalHttpStatusCode = HttpStatus.OK.value();
			log.info(
					"[syncClientsFromPoPortalCron] traceId={} done eligible={} processed={} failed={} syncedAddresses={} skippedAddresses={} deactivatedInPo={}",
					traceId, clientsEligible, clientsProcessed, clientsFailed, totalSyncedAddresses,
					totalSkippedAddresses, totalDeactivated);

		} catch (RestClientException ex) {
			ExceptionLogContext.add(ex);
			finalHttpStatusCode = HttpStatus.BAD_GATEWAY.value();
			log.error("[syncClientsFromPoPortalCron] traceId={} REST error calling PO", traceId, ex);
		} catch (Exception ex) {
			ExceptionLogContext.add(ex);
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			log.error("[syncClientsFromPoPortalCron] traceId={} unexpected error", traceId, ex);
		} finally {
			String exceptionDetailsForLog = buildEndLogDetailsForClientSyncCron(failureDigest);
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
			try {
		        if (exceptionDetailsForLog != null && !exceptionDetailsForLog.isEmpty()) {

		        	String formattedFailures = exceptionDetailsForLog != null
		        	        ? exceptionDetailsForLog.replace("||", "<br/><br/>")
		        	        : "No failures";

		        	String mailBody =
		        	        "<b>Trace ID:</b> " + traceId + "<br/><br/>"

		        	      + "<b>Summary:</b><br/>"
		        	      + "Total Eligible Clients: " + clientsEligible + "<br/>"
		        	      + "Processed: " + clientsProcessed + "<br/>"
		        	      + "Failed: <span style='color:red;'>" + clientsFailed + "</span><br/>"
		        	      + "Synced Addresses: " + totalSyncedAddresses + "<br/>"
		        	      + "Skipped Addresses: " + totalSkippedAddresses + "<br/>"
		        	      + "Deactivated Locations: " + totalDeactivated + "<br/><br/>"

		        	      + "<b>Failure Details:</b><br/><br/>"
		        	      + formattedFailures;

		            mailService.sendMailWithCC(
		            		exceptionMailplsql,
		            		exceptionMaildev,
		                    "Client PO Sync Issues | TraceId : " + traceId,
		                    mailBody
		            );
		        }
		    } catch (Exception mailEx) {
		        log.error("Failed to send exception mail for traceId={}", traceId, mailEx);
		    }
			ExceptionLogContext.clear();
			
		}
	}
	
	private String buildClearErrorMessage(Exception ex, ClientDetailsSyncDto poDto) {

	    Throwable root = ex;
	    while (root.getCause() != null) {
	        root = root.getCause();
	    }

	    String message = root.getMessage();

	    String base = "PoClientId=" + poDto.getClientid() +
	                  ", ClientName=" + poDto.getClientName() + " → ";

	    if (root instanceof javax.persistence.NonUniqueResultException) {
	        return base + " DUPLICATE IN DB: Multiple records found for same client/location";
	    }

	    if (root instanceof IllegalStateException &&
	            message != null && message.contains("Duplicate clientAddressId")) {
	        return base + " DUPLICATE ADDRESS IN PO: " + message;
	    }

	    if (root instanceof IllegalArgumentException) {
	        return base + " INVALID DATA: " + message;
	    }

	    return base + " UNKNOWN ERROR: " + (message != null ? message : "No details");
	}



	private Map<Long, ClientDetailsSyncDto> buildClientsToSyncForPoPortalCron(List<ClientDetailsSyncDto> poClients,
			String traceId, StringBuilder failureDigest) {
		Map<Long, Integer> idCounts = new HashMap<>();
		for (ClientDetailsSyncDto dto : poClients) {
			if (dto != null && dto.getClientid() != null) {
				idCounts.merge(dto.getClientid(), 1, Integer::sum);
			}
		}
		for (Map.Entry<Long, Integer> e : idCounts.entrySet()) {
			if (e.getValue() > 1) {
				String msg = "duplicate poClientId=" + e.getKey() + " in PO payload (" + e.getValue()
						+ " rows); sync skipped for this client";
				failureDigest.append(msg).append(" || ");
				ExceptionLogContext.add(msg);
				log.warn("[syncClientsFromPoPortalCron] traceId={} {}", traceId, msg);
			}
		}

		Map<Long, ClientDetailsSyncDto> toSync = new LinkedHashMap<>();
		for (ClientDetailsSyncDto dto : poClients) {
			if (dto == null || dto.getClientid() == null) {
				continue;
			}
			if (idCounts.get(dto.getClientid()) > 1) {
				continue;
			}
			toSync.putIfAbsent(dto.getClientid(), dto);
		}
		return toSync;
	}

	private String buildEndLogDetailsForClientSyncCron(StringBuilder failureDigest) {
		String ctx = ExceptionLogContext.get();
		StringBuilder sb = new StringBuilder();
		if (failureDigest.length() > 0) {
			sb.append("clientFailures: ").append(failureDigest);
		}
		if (ctx != null && !ctx.isEmpty()) {
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(ctx);
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse completionDateOfProject(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("completionDateOfProject");
		apiLogInfo.setApiUrl("/api/completionDateOfProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (resourceManagementDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (resourceManagementDTO.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (resourceManagementDTO.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current User Employee Id cannot be null!!");
				return response;
			}
			if (resourceManagementDTO.getProjectCompletionDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Provided Project Completion Date cannot be null!!");
				return response;
			}

			logBuilder.append("ProjectType : " + resourceManagementDTO.getProjectType() + " ,ProjectId :"
					+ resourceManagementDTO.getProjectId() + " ,ProjectName :" + resourceManagementDTO.getName());

			Project projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			if (projectObj == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				apiLogInfo.setApiResponse("Project not found!!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			// Validate active resources against completion date (block if any active start date is after completion date)
			LocalDate completionDate;
			try {
				completionDate = LocalDate.parse(resourceManagementDTO.getProjectCompletionDate());
			} catch (Exception ex) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Project Completion Date format. Expected yyyy-MM-dd.");
				return response;
			}

			List<EmployeeTeamMap> activeEtms = employeeTeamMapRepository.findByProjectIdAndActive(projectObj.getProjectId(), 1L);
			boolean invalidFutureStartExists = activeEtms != null && activeEtms.stream()
					.anyMatch(etm -> etm != null && etm.getStartDate() != null
							&& etm.getStartDate().toLocalDate().isAfter(completionDate));
			if (invalidFutureStartExists) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("RESOURCE_START_DATE_AFTER_COMPLETION_DATE");
				response.setServiceResponse1(
						"There are resource(s) mapped to this project whose start date is greater than the selected completion date. Please resolve it to continue.");
				return response;
			}

			// Inactivate resources using centralized team deletion/removal flow (ensures ETM endDate = completionDate)
			List<Team> activeTeams = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
			if (activeTeams != null && !activeTeams.isEmpty()) {
				List<RmgTeamDto> teamDtoList = new ArrayList<>();
				for (Team t : activeTeams) {
					if (t == null || t.getTeamId() == null) {
						continue;
					}
					RmgTeamDto dto = new RmgTeamDto();
					dto.setTeamId(t.getTeamId());
					dto.setTeamName(t.getTeamName());
					dto.setProjectId(projectObj.getProjectId());
					dto.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
					dto.setCustomEndDate(false);
					dto.setEndDate(completionDate.atStartOfDay());
					teamDtoList.add(dto);
				}

				if (!teamDtoList.isEmpty()) {
					PoDetailsDto deletePayload = new PoDetailsDto();
					deletePayload.setProjectId(projectObj.getProjectId());
					deletePayload.setTeamList(teamDtoList);

					ServiceResponse deleteResp = teamsService.deleteSelectedTeams(deletePayload);
					if (deleteResp == null || deleteResp.getServiceStatus() == null
							|| !ServiceResponse.STATUS_SUCCESS.equals(deleteResp.getServiceStatus())) {
						String msg = deleteResp != null && deleteResp.getServiceResponse() != null
								? String.valueOf(deleteResp.getServiceResponse())
								: "Unable to inactivate project resources before completion.";
						throw new RuntimeException(msg);
					}
				}
			}

			projectObj.setProjectCompletionDate(resourceManagementDTO.getProjectCompletionDate());
			projectObj.setActive("false");
			projectObj.setProjectStatus(resourceManagementDTO.getProjectStatus());
			projectObj.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
			projectObj.setUpdatedOn(LocalDateTime.now());
			Project projectDbResponse = projectRepository.save(projectObj);

			String projectType = (projectDbResponse.getPoProjectType() != null
					&& !projectDbResponse.getPoProjectType().trim().equals("")) ? projectDbResponse.getPoProjectType()
							: "Internal";	
			resourceManagementDTO.setProjectType(projectType);
			
			
			// if (!projectType.equals("Internal")) {
			// 	resourceManagementDTO.setPoProjectId(projectDbResponse.getPoProjectId());
			// 	ServiceResponse poPortalResponse = resourceManagementService.sendProjectInfoToPoPortal(resourceManagementDTO);
			// 	if (poPortalResponse != null && poPortalResponse.getServiceStatus() != null
			// 			&& poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
			// 		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			// 		response.setServiceResponse("Completion status updated to Shankh portal!");
			// 		apiLogInfo.setApiResponse("Reverse synced successfully!");
			// 		apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			// 	} else {
			// 		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			// 		response.setServiceResponse("Unable to intimate completion status to Shankh portal!");
			// 		apiLogInfo.setApiResponse("Reverse synced failed!");
			// 		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			// 		throw new RuntimeException("Reverse synced failed, Unable to sync project completion status to Shankh portal!!");
			// 	}
			// }

			// if (projectDbResponse != null) {
			// 	resourceManagementService.sendProjectCompletionMail(projectDbResponse,resourceManagementDTO.getUpdatedBy(), logBuilder);
			// }

			if (projectDbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Status Updated As Completed !!");
				apiLogInfo.setApiResponse("Project Status Updated As Completed");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Status Not Updated");
				apiLogInfo.setApiResponse("Project Status Not Updated to Completed");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			log.error("Error occured while marking Project as complete : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

}
