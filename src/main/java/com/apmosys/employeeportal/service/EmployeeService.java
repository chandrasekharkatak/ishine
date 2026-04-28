 package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.AppreciationAndRewardsCountDto;
import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationDetailsDTO;
import com.apmosys.employeeportal.dto.BulkSkillRowData;
import com.apmosys.employeeportal.dto.CertificateDTO;
import com.apmosys.employeeportal.dto.DateRangeDTO;
import com.apmosys.employeeportal.dto.DefaultProjectEmployeeConfig;
import com.apmosys.employeeportal.dto.DefaultProjectUpdateDTO;
import com.apmosys.employeeportal.dto.DefaulterResponseDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeProjection;
import com.apmosys.employeeportal.dto.EmployeeSkillProficiencyDTO;
import com.apmosys.employeeportal.dto.ExpiredPOMailSendDTO;
import com.apmosys.employeeportal.dto.ExpiredProjectDTOForNotification;
import com.apmosys.employeeportal.dto.ExpiredPOMailSendDTO.PoObject;
import com.apmosys.employeeportal.dto.GetAllEmployeesWorkAnniversaryTodayDTO;
import com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.GetTeamAndTimesheetDetailsDTO;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.LockStatusDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PageResponseDTO;
import com.apmosys.employeeportal.dto.PendingTimesheetDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.PoRequirementDataDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.RowData;
import com.apmosys.employeeportal.dto.SearchEmpPayloadDTO;
import com.apmosys.employeeportal.dto.SearchEmployeeDTO;
import com.apmosys.employeeportal.dto.SkillCertConfigDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.exception.BadRequestException;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.CertificateDocumentMapping;
import com.apmosys.employeeportal.model.CertificateDriveLinkMapping;
import com.apmosys.employeeportal.model.CertificateSkillMapping;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeAssetMap;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.EmployeeCertificates;
import com.apmosys.employeeportal.model.EmployeeDefaulterConsent;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeNotificationConsent;
import com.apmosys.employeeportal.model.EmployeeSkillProficiencyMapping;
import com.apmosys.employeeportal.model.EmployeeSpecializationMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Log;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.NewsletterReadResponse;
import com.apmosys.employeeportal.model.Notification;
import com.apmosys.employeeportal.model.PolicyReadResponse;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.model.PredefinedSkills;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.model.Proficiency;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.UploadPolicy;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.AppreciationRepository;
import com.apmosys.employeeportal.repository.AuditCustomRepository;
import com.apmosys.employeeportal.repository.CertificateDocumentMapRepository;
import com.apmosys.employeeportal.repository.CertificateDriveLinkMapRepository;
import com.apmosys.employeeportal.repository.CertificateSkillMapRepository;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificatesRepository;
import com.apmosys.employeeportal.repository.EmployeeDefaulterConsentRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeNotificationConsentRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeSkillProficiencyMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeSpecializationMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FieldAlterationRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.LogsRepository;
import com.apmosys.employeeportal.repository.NewsletterReadResponseRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.NotificationRepository;
import com.apmosys.employeeportal.repository.PIPRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.PolicyReadResponseRepository;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.repository.PredefinedSkillsRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
import com.apmosys.employeeportal.repository.ProficiencyRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.UploadPolicyRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.request.EmployeeTimesheetProjectRequest;
import com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse;
import com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse;
import com.apmosys.employeeportal.serviceInterface.TrainingUserService;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.DbTable;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.LogEvents;
import com.apmosys.employeeportal.utility.NotificationUtil;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.TypeConversionUtil;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;


@Service
public class EmployeeService {

	@Value("${valid.attempt}")
	private Integer failedAttempt;
	
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

	/** PoPortal integration: GET /api/getAllEmployeeInfo */
	private static final String API_GET_ALL_EMPLOYEE_INFO = "/api/getAllEmployeeInfo";
	private static final String SUBFEATURE_GET_ALL_EMPLOYEE_INFO = "get_all_employee_info";
	private static final String OPERATION_GET_ALL_EMPLOYEE_INFO = "getAllEmployeeInfo";
	private static final String PO_PORTAL_LOG_SOURCE = "PoPortal";
	private static final String LOG_LEVEL_INFO = "INFO";
	private static final String LOG_LEVEL_ERROR = "ERROR";
	private static final String MSG_EMPLOYEE_INFO_NOT_FOUND = "Employee Info not found.";
	private static final String MSG_SYSTEM_ERROR = "Something Went Wrong.";

	@Autowired
	EmployeeRepository employeeRepository;

    @Autowired
    EmployeeDefaulterConsentRepository employeeDefaulterConsentRepository;
	
	@Autowired
	EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;
	
	@Autowired
	ProjectManagerMappingRepository projectManagerMappingRepository;
	
	@Autowired
	CertificateDocumentMapRepository certificateDocumentRepository;
	
	@Autowired
	EmployeeCertificatesRepository employeeCertificatesRepository;
	
	
	@Autowired
	ProjectOverheadMappingRepository projectOverheadMappingRepository;
	
	@Autowired
	CertificateSkillMapRepository certificateSkillMapRepository;

	@Autowired
	DraftEmployeeRepository draftEmployeeRepository;
	
	
	@Autowired
	ProficiencyRepository proficiencyRepository;
	
	@Autowired
	QuarterCycleRepository quarterCycleRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	
	 @Autowired
	    private JdbcTemplate jdbcTemplate;
	 
	 @Autowired
	    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	CertificateDriveLinkMapRepository certificateDriveLinkMappingRepository;
	
	@Autowired
	EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;
	
	@Autowired
	FieldAlterationRepository fieldAlterationRepository;
	
	@Autowired
	NotificationUtil mailNotify;
	
	@Autowired
	EmployeeSkillProficiencyMappingRepository employeeSkillProficiencyMappingRepository;
	
	@Autowired
	PredefinedSkillsRepository predefinedSkillsRepository;

    @Autowired
    PortalConfigRepository portalConfigRepository;

	@Value("${default.password}")
	String defaultPaswword;

	@Value("${file.location.image}")
	private String imageFileLocation;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${EXPIRY_THRESHOLD_PERCENT}")
	private Long EXPIRY_THRESHOLD_PERCENT;
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${training.job.role.exclude}")
	private String trainingJobRoleExclude;
	
	@Value("${bd.mail}")
	private String businessMail;
	
	@Value("${vp_mails}")
	private String vpMails;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	ResourceManagementService resourceManagementService;
	
	@Autowired
	private LeavePolicyMasterRepository leavePolicyMasterRepository;

	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;

	@Autowired
	PreviousEmploymentRepository previousEmploymentRepository;

	@Autowired
	EmployeeCertificateRepository employeeCertificateRepository;
	
	@Autowired
	EmployeeOnBoardingRepository employeeOnboardingRepository;
	
	@Autowired
	EmployeeOnBoardingMapRepository employeeOnboardingMapRepository;
	
	@Autowired
	EmployeeSpecializationMapRepository employeeSpecializationMapRepository;
	
//	@Autowired
//	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	ResourceRequirementRepository resourceRequirementRepository;
	
	@Autowired
	private  UploadPolicyRepository uploadPolicyRepository;

	@Autowired
	PolicyReadResponseRepository policyReadResponseRepository;
	
	@Autowired
	PoRequirementMappingRepository poRequirementMappingRepository;
	
	@Autowired
	private ModelMapper mapper;

	@Value("${timesheet.lock.days}")
	private Integer timesheetLockDays;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private MailService mailService;

	@Autowired
	private LogsRepository logsRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	NotificationRepository notificationRepository;
	
	@Autowired
	EmployeeNotificationConsentRepository employeeNotificationConsentRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Value("${timesheet.reconcile.days}")
	private Long timesheetReconcileDays;
	
	@Autowired
	AuditCustomRepository auditCustomRepository;
	
	@Autowired
	UserSessionRepository userSessionRepository;
	
	@Autowired
	TrainingUserService trainingUserService;
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Autowired
	private NewsletterReadResponseRepository newsletterReadResponseRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	PIPRepository pipRepository;
	
	@Autowired
	AppreciationRepository appreciationRepository;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@Autowired
	private ApiLogUtility apiLogUtility;
	
    @Autowired
    private EntityManager entityManager;
	
	@Autowired
	private TeamMembersService teamMembersService;

	
//	@Value("${bd.mail}")
//	private String businessMail;
	
	private final Map<String, List<EmployeeDTO>> employeeCache = new ConcurrentHashMap<>();





	@Transactional
	public ServiceResponse createEmployee(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_employee");
		apiLogInfo.setApiUrl("/api/createEmployee");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId : " + employeedto.getEmployeementId()+ "jobRoleId : " +employeedto.getJobRoleId()+ "createdBy : " +employeedto.getCreatedBy());
		try {

			ServiceResponse employeementIdExists = checkEmployeementId(employeedto);

			if (employeementIdExists.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(employeementIdExists.getServiceResponse()
						+ " Kindly provide a different value for Employment ID.");
				
				apiLogInfo.setApiResponse(employeementIdExists.getServiceResponse()
						+ " Kindly provide a different value for Employment ID.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
				return response;
			}
//			if (!validationService.validateManagerId(employeedto.getManagerId())) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Manager Id does not exists.");
//				return response;
//			}
			if (!validationService.validateJobRoleId(employeedto.getJobRoleId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job role Id does not exists.");
				
				apiLogInfo.setApiResponse("Job role Id does not exists.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			Employee employee = new Employee();

			employee.setEmployeementId(employeedto.getEmployeementId());
			employee.setName(employeedto.getName());
			employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));
			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd"));
			employee.setEmail(employeedto.getEmail());
			employee.setSecondaryEmail(employeedto.getSecondaryEmail());
			employee.setMobileNo(employeedto.getMobileNo());
			employee.setManagerId(employeedto.getManagerId());
			employee.setJobRoleId(employeedto.getJobRoleId());
			employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
			employee.setCreatedBy(employeedto.getCreatedBy());
			employee.setReferedType(employeedto.getReferedType());
			employee.setReferedName(employeedto.getReferedName());

			if (employeedto.getExperience().equals("Fresher")) {
				employee.setExperience(employeedto.getExperience());
				employee.setTotalExperience(0f);
			} else {
				employee.setExperience(employeedto.getExperience());
				employee.setTotalExperience(employeedto.getTotalExperience());
			}

			employee.setNoticePeriod(employeedto.getNoticePeriod());
			employee.setEmploymentstatus(employeedto.getEmploymentstatus());
			employee.setWorkLocation(employeedto.getWorkLocation());
			employee.setInvalidAccessAttempt(0);

			employee.setAboutMe(employeedto.getAboutMe());
			employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
			employee.setIsNew("true");
			employee.setProbationPeriod(employeedto.getProbationPeriod());
			employee.setIsUserInfoUpdated("false");

			employee.setProbationPeriod(employeedto.getProbationPeriod());
			employee.setMothersName(employeedto.getMothersName());
			employee.setSpouse(employeedto.getSpouse());
			employee.setChild1(employeedto.getChild1());
			employee.setChild2(employeedto.getChild2());
			employee.setChild3(employeedto.getChild3());
//			employee.setBillable(employeedto.getBillable());
//			employee.setBillableType(employeedto.getBillableType());
			employee.setIsTimesheetLockCheckEnable("true");
			employee.setDesignationId(employeedto.getDesignationId());
			employee.setIsConsultant(employeedto.getIsConsultant());
			employee.setIsApprenticeship(employeedto.getIsApprenticeship());
			employee.setIsApmosysProduct(employeedto.getIsApmosysProduct());	
			
			
			if ("No".equals(employeedto.getOnbenchDate())) {
			    // Keep the existing value (no need to set it again)
			} else {
			    employee.setOnbenchDate(employeedto.getOnbenchDate());
			}
			
			employee.setReportingManagerId(employeedto.getReportingManagerId());
			if(employeedto.getReportingManagerId() == null) {
				employee.setApprovalsTo(null);
			}else {
				employee.setApprovalsTo(employeedto.getApprovalsTo());
			}

			Employee newEmployee = employeeRepository.save(employee);	
			Project proj = new Project();
			StringBuilder employeeRole = new StringBuilder("");
			for (String empRole : employeedto.getDefaultTeamEmployeeRole()) {
				employeeRole.append(empRole).append(",");
			}
			
			if (employeedto.getDefaultProjectId() != null) {
				Department dept = departmentRepository.findByDeptId(employeedto.getDepartmentId());
				String departmentname = dept.getName();

				EmployeeTeamMap employeeTeamMap = new EmployeeTeamMap();
				employeeTeamMap.setEmpId(newEmployee.getEmpId());
				employeeTeamMap.setTeamId(employeedto.getDefaultTeamId());
				employeeTeamMap.setActive(2l);
				employeeTeamMap.setStartDate(employeedto.getNewEtmStartDate() != null ? employeedto.getNewEtmStartDate() : LocalDateTime.now());
				employeeTeamMap.setEmployeeRole(employeeRole.toString());
				employeeTeamMap.setIsShadow(employeedto.getIsShadowResource());
				employeeTeamMap.setPoId(employeedto.getPoId());
				employeeTeamMap.setRoleId(employeedto.getPoRoleId());
				employeeTeamMap.setEmpTeamDepartmentId(dept != null ? dept.getDeptId() : null);
				employeeTeamMap.setUpdatedBy(Long.parseLong(newEmployee.getCreatedBy().toString()));
				employeeTeamMap.setUpdatedOn(LocalDateTime.now());
				EmployeeTeamMap dbEmployeeTeamMap = employeeTeamMapRepository.save(employeeTeamMap);

				Project project = projectRepository.findByProjectId(employeedto.getDefaultProjectId());
				if (project != null) {
					project.setIsDraftProject("true");
					project.setUpdatedBy(Long.parseLong(newEmployee.getCreatedBy().toString()));
					project.setUpdatedOn(LocalDateTime.now());
					proj = projectRepository.save(project);
				}
				
				teamMembersService.updateEmployeeDefaultProjectIfUpdated(List.of(newEmployee.getEmpId()),
						List.of(newEmployee.getEmpId()), project, Long.parseLong(newEmployee.getCreatedBy().toString()), List.of(dbEmployeeTeamMap));

			}	
						
				if (newEmployee.getEmpId() != null) {

				List<LeaveTypeMaster> leaveTypeMasterList = leaveTypeMasterRepository.findAll();

				List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();

				List<LeaveBalanceLog> logList = new ArrayList<LeaveBalanceLog>();

				leaveTypeMasterList.forEach((leaveType) -> {

					EmployeeLeavesMap map = new EmployeeLeavesMap();
					LeaveBalanceLog log = new LeaveBalanceLog();

					map.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
					map.setEmpId(newEmployee.getEmpId());
					map.setBalance((float) 0);
					map.setPendingForApproval((float) 0);
					mapList.add(map);

					log.setBalance(0.0f);
					log.setEmpId(newEmployee.getEmpId());
					log.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.addLeave);
					log.setUpdateBalanceBy("+0.0");
					logList.add(log);

				});

				List<EmployeeLeavesMap> list = employeeLeavesMapRepository.saveAll(mapList);
				List<LeaveBalanceLog> updatedLogList = leaveBalanceLogRepository.saveAll(logList);

				if (list != null && updatedLogList != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Profile Created.");
					
					apiLogInfo.setApiResponse("Employee Profile Created");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					Log log = new Log();

					log.setEmpId(employeedto.getCreatedBy().longValue());
					log.setEvent(LogEvents.CREATE);
					log.setTableName(DbTable.EMPLOYEE);
					log.setTableEntryId(newEmployee.getEmpId());
					log.setRemarks("Employee profile created.");

					logsRepository.save(log);
					
					List<Asset> assetList = employeeOnboardingRepository.findAll();
					List<EmployeeAssetMap> assetMappingObj = new ArrayList<>();
					
					if(assetList != null) {
						for(Asset assetObj : assetList) {
							EmployeeAssetMap employeeAssetMap = new EmployeeAssetMap();
							
							employeeAssetMap.setAssetId(assetObj.getAssetId());
							employeeAssetMap.setEmpId(newEmployee.getEmpId());
							employeeAssetMap.setIsAssigned("false");
							
							assetMappingObj.add(employeeAssetMap);
						}
						
					employeeOnboardingMapRepository.saveAll(assetMappingObj);
					}

					List<Object[]> objectList = jobRoleRepository.getHoDByJobRoleId(newEmployee.getJobRoleId());
					EmployeeDTO hod = new EmployeeDTO();

					if (objectList != null) {
						for (Object[] object : objectList) {
							hod.setEmail((object[1] != null) ? object[1].toString() : null);
							hod.setDepartmentName((object[2] != null) ? object[2].toString() : null);
							hod.setJobRoleName((object[3] != null) ? object[3].toString() : null);
						}
					}
					
					if(newEmployee != null) {
						if(proj != null && proj.getProjectId() != null && proj.getProjectName()!=null && !"".equalsIgnoreCase(proj.getProjectName())) {
							mailService.sendMail(newEmployee.getSecondaryEmail(), "Regarding employee profile creation",
									"Your account has been created. <br>Username: " + newEmployee.getEmail()
											+ "<br>Password: " + defaultPaswword);
							mailService.sendMail(hod.getEmail(), "Regarding new employee",
									newEmployee.getName() + " has been inducted in " + hod.getDepartmentName()
											+ " department as " + hod.getJobRoleName()+ ". \n\n"
											        + "Project assigned: " + proj.getProjectName()+ ".");
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Could not fetch employee's default project details.");
							
							apiLogInfo.setApiResponse("Could not fetch employee's default project details.");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Could not fetch employee's details.");
						
						apiLogInfo.setApiResponse("Could not fetch employee's details.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Creation Failed.");
					
					apiLogInfo.setApiResponse("Employee Profile Creation Failed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional
	public ServiceResponse createEmployeeByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/createEmployeeByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId : " + employeedto.getEmployeementId()+ "aadhar : " +employeedto.getAadhar()+ "alternateMobileNo : " +employeedto.getAlternateMobileNo()+ "jobRoleId : " +employeedto.getJobRoleId()+ "employeementStatus"+employeedto.getEmploymentstatus());

		try {
			
			
			Employee employee = new Employee();

			employee.setEmployeementId(employeedto.getEmployeementId());
			employee.setName(employeedto.getName());
			employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));
			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd"));
			employee.setEmail(employeedto.getEmail());
			employee.setSecondaryEmail(employeedto.getSecondaryEmail());
			employee.setMobileNo(employeedto.getMobileNo());
			employee.setManagerId(employeedto.getManagerId());
			
			if (jobRoleRepository.findById(employeedto.getJobRoleId()).isEmpty()) {
				employee.setJobRoleId(138L);
			} else {
				employee.setJobRoleId(employeedto.getJobRoleId());
			}
//			employee.setJobRoleId(employeedto.getJobRoleId());
			
			employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
			employee.setCreatedBy(employeedto.getCreatedBy());

			if (employeedto.getExperience().equals("Fresher")) {
				employee.setExperience(employeedto.getExperience());
				employee.setTotalExperience(0f);
			} else {
				employee.setExperience(employeedto.getExperience());
				employee.setTotalExperience(employeedto.getTotalExperience());
			}

			employee.setNoticePeriod((short) 90);
			employee.setEmploymentstatus(employeedto.getEmploymentstatus());
			employee.setWorkLocation(employeedto.getWorkLocation());
			employee.setInvalidAccessAttempt(0);

			employee.setAboutMe(employeedto.getAboutMe());
			employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
			employee.setIsNew("true");
			employee.setProbationPeriod((short)180);
			employee.setIsUserInfoUpdated("false");
			employee.setBillable(employeedto.getBillable());

			Employee newEmployee = employeeRepository.save(employee);

			if (newEmployee.getEmpId() != null) {

				List<LeaveTypeMaster> leaveTypeMasterList = leaveTypeMasterRepository.findAll();

				List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();

				List<LeaveBalanceLog> logList = new ArrayList<LeaveBalanceLog>();

				leaveTypeMasterList.forEach((leaveType) -> {

					EmployeeLeavesMap map = new EmployeeLeavesMap();
					LeaveBalanceLog log = new LeaveBalanceLog();

					map.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
					map.setEmpId(newEmployee.getEmpId());
					map.setBalance((float) 0);
					map.setPendingForApproval((float) 0);
					mapList.add(map);

					log.setBalance(0.0f);
					log.setEmpId(newEmployee.getEmpId());
					log.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.addLeave);
					log.setUpdateBalanceBy("+0.0");
					logList.add(log);

				});

				List<EmployeeLeavesMap> list = employeeLeavesMapRepository.saveAll(mapList);
				List<LeaveBalanceLog> updatedLogList = leaveBalanceLogRepository.saveAll(logList);

				if (list != null && updatedLogList != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Profile Created.");

					Log log = new Log();

					log.setEmpId(employeedto.getCreatedBy().longValue());
					log.setEvent(LogEvents.CREATE);
					log.setTableName(DbTable.EMPLOYEE);
					log.setTableEntryId(newEmployee.getEmpId());
					log.setRemarks("Employee profile created.");

					logsRepository.save(log);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Creation Failed.");
				}
			}
			
			

//			Employee employee = new Employee();
//
//			employee.setEmployeementId(employeedto.getEmployeementId());
//			employee.setName(employeedto.getName());
//			if (employeedto.getDateOfBirth() == null) {
//				employee.setDateOfBirth(stringToDateTimeParser.getDate("2001-01-01", "yyyy-MM-dd"));
//			} else {
//				employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));
//			}
//			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd"));
//			employee.setManagerId(employeedto.getManagerId());
//			employee.setEmail(employeedto.getEmail());
//			employee.setGender(employeedto.getGender());
//			employee.setBloodGroup(employeedto.getBloodGroup());
//			employee.setMaritalStatus(employeedto.getMaritalStatus());
//			employee.setFatherName(employeedto.getFatherName());
//			employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
//			employee.setMotherTongue(employeedto.getMotherTongue());
//			employee.setPassportNumber(employeedto.getPassportNumber());
//			if (employeedto.getAadhar() == null) {
//				employee.setAadhar(123456789012l);
//			} else {
//				employee.setAadhar(employeedto.getAadhar());
//			}
//			employee.setPanNumber(employeedto.getPanNumber());
//			employee.setMobileNo(employeedto.getMobileNo());
//			employee.setLandline(employeedto.getLandline());
//			employee.setAddress(employeedto.getAddress());
//			employee.setCity(employeedto.getCity());
//			employee.setState(employeedto.getState());
//			employee.setCountry(employeedto.getCountry());
//			employee.setPincode(employeedto.getPincode());
//			if (employeedto.getAlternateMobileNo() == null) {
//				employee.setAlternateMobileNo(1111111111l);
//			} else {
//				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
//			}
//			employee.setPermanentAddress(employeedto.getPermanentAddress());
//			employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
//			employee.setRelation(employeedto.getRelation());
//			employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
//			employee.setNoticePeriod(employeedto.getNoticePeriod());
//
//			LocalDate joinDate = stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd");
//			LocalDate todayDate = LocalDate.now();
//			LocalDate returnvalue = todayDate.minusMonths(9);
//			Integer result = returnvalue.compareTo(joinDate);
//
//			if (employeedto.getEmploymentstatus().equals("N")) {
//				employee.setEmploymentstatus("InActive");
//			} else if (result <= 0) {
//				employee.setEmploymentstatus("Probation");
//			} else if (result >= 0) {
//				employee.setEmploymentstatus("Confirmed");
//			}
//
////			employee.setEmploymentstatus(employeedto.getEmploymentstatus());
//			employee.setBankName(employeedto.getBankName());
//			employee.setBankAccountNo(employeedto.getBankAccountNo());
//			employee.setBankIFSCCode(employeedto.getBankIFSCCode());
//			employee.setPfAccountNumber(employeedto.getPfAccountNumber());
//			employee.setPreviousPfAccountNumber(employeedto.getPreviousPfAccountNumber());
//			employee.setUan(employeedto.getUan());
//			employee.setEsicNumber(employeedto.getEsicNumber());
//			employee.setGraduationType(employeedto.getGraduationType());
//			employee.setPursuing(employeedto.getPursuing());
//			employee.setYearOfPassing(employeedto.getYearOfPassing());
//			employee.setPassingGrade(employeedto.getPassingGrade());
//			employee.setAboutMe("Add about yourself.");
//			employee.setViewsOnOrganisation("Add your views.");
//
//			if (jobRoleRepository.findById(employeedto.getJobRoleId()).isEmpty()) {
//				employee.setJobRoleId(138L);
//			} else {
//				employee.setJobRoleId(employeedto.getJobRoleId());
//			}
//
//			// employee.setJobRoleId(employeedto.getJobRoleId());
//
//			employee.setPassword(employeedto.getPassword());
//			// employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
//			employee.setCreatedBy(employeedto.getCreatedBy());
//			employee.setExperience(employeedto.getExperience());
//			employee.setRole(employeedto.getRole());
//			employee.setWorkLocation(employeedto.getWorkLocation());
//			employee.setInvalidAccessAttempt(0);
//
//			Employee newEmployee = employeeRepository.save(employee);
//
//			Optional.ofNullable(employeedto.getPreviousEmploymentList()).ifPresent((previousEmployerList) -> {
//
//				if (!previousEmployerList.isEmpty()) {
//					previousEmployerList.forEach((previousEmployer) -> {
//						previousEmployer.setEmpId(newEmployee.getEmpId());
//
//					});
//					addPreviousEmployer(previousEmployerList, employeedto.getIsDraft());
//				}
//
//			});
//
//			Optional.ofNullable(employeedto.getCertifications()).ifPresent((certificationList) -> {
//
//				if (!certificationList.isEmpty()) {
//					certificationList.forEach((certification) -> {
//						certification.setEmpId(newEmployee.getEmpId());
//
//					});
//					addCertifications(certificationList, employeedto.getIsDraft());
//				}
//
//			});
//
//			List<LeaveTypeMaster> leaveTypeMasterList = leaveTypeMasterRepository.findAll();
//
//			List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();
//
//			List<LeaveBalanceLog> logList = new ArrayList<LeaveBalanceLog>();
//
//			leaveTypeMasterList.forEach((leaveType) -> {
//
//				EmployeeLeavesMap map = new EmployeeLeavesMap();
//				LeaveBalanceLog log = new LeaveBalanceLog();
//
//				map.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
//				map.setEmpId(newEmployee.getEmpId());
//				map.setBalance((float) 0);
//				map.setPendingForApproval((float) 0);
//				mapList.add(map);
//
//				log.setBalance(0.0f);
//				log.setEmpId(newEmployee.getEmpId());
//				log.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
//				log.setMessage(LeaveLogMessage.addLeave);
//				log.setUpdateBalanceBy("+0.0");
//				logList.add(log);
//
//			});
//
//			List<EmployeeLeavesMap> list = employeeLeavesMapRepository.saveAll(mapList);
//			List<LeaveBalanceLog> updatedLogList = leaveBalanceLogRepository.saveAll(logList);
//
//			if (list != null && updatedLogList != null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse("Employee Profile Created.");
//
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee Profile Creation Failed.");
//			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public boolean addPreviousEmployer(List<PreviousEmploymentDTO> previousEmployeeDTOList, String isDraft) {
		ServiceResponse response=new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/addPreviousEmployer");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		List<PreviousEmployment> list = new ArrayList<PreviousEmployment>();

		previousEmployeeDTOList.forEach((previousEmployeeDTO) -> {

			PreviousEmployment previousEmployment = new PreviousEmployment();
			LocalDate dateOfJoining;
			LocalDate dateOfRelieving;
			if(previousEmployeeDTO.getDateOfJoining() != null) {
				dateOfJoining = stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd");
				logBuilder.append("dateOfJoining : "+dateOfJoining );
				response.setServiceResponse("Employee joining date exiust.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Employee joining date exiust");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				dateOfJoining = null;
				response.setServiceResponse("Employee joining date not found");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Employee joining date not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			if(previousEmployeeDTO.getDateOfRelieving() != null){
				dateOfRelieving = stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd");
				logBuilder.append("dateOfRelieving : "+dateOfRelieving);
				response.setServiceResponse("Employee relieving date found");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Employee relieving date found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceResponse("Employee relieving date not found");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Employee relieving date not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				dateOfRelieving = null;
			}

			previousEmployment.setDateOfJoining(dateOfJoining);
			previousEmployment.setDateOfRelieving(dateOfRelieving);

			previousEmployment.setDesignation(previousEmployeeDTO.getDesignation());
			previousEmployment.setHrContactNumber(previousEmployeeDTO.getHrContactNumber());
			previousEmployment.setHrName(previousEmployeeDTO.getHrName());
			previousEmployment.setManagerName(previousEmployeeDTO.getManagerName());
			previousEmployment.setManagerContactNumber(previousEmployeeDTO.getManagerContactNumber());
			previousEmployment.setEmployerName(previousEmployeeDTO.getEmployerName());
			previousEmployment.setEmpId(previousEmployeeDTO.getEmpId());
			previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());
			previousEmployment.setIsDraft(isDraft);

			list.add(previousEmployment);

		});
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);		
		return (previousEmploymentRepository.saveAll(list).isEmpty()) ? false : true;
	}

	public boolean addCertifications(List<EmployeeCertificateDTO> employeeCertifcateDTOList, String isDraft) {		
		List<EmployeeCertificate> list = new ArrayList<EmployeeCertificate>();

		employeeCertifcateDTOList.forEach((certificate) -> {
			
			LocalDate dateOfCompletion;
			if(certificate.getDateOfCompletion() != null) {
				dateOfCompletion = stringToDateTimeParser.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd");				
			}else {
				dateOfCompletion = null;
			}
			

			EmployeeCertificate employeeCertificate = new EmployeeCertificate();

			employeeCertificate.setCertificationName(certificate.getCertificationName());
			employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
			employeeCertificate.setDateOfCompletion(dateOfCompletion);
			employeeCertificate.setDuration(certificate.getDuration());
			employeeCertificate.setEmpId(certificate.getEmpId());
//			employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
			employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());
			employeeCertificate.setIsDraft(isDraft);
			list.add(employeeCertificate);

		});

		return (employeeCertificateRepository.saveAll(list).isEmpty()) ? false : true;
	}
	
	public AppreciationDetails getEmployeeAppreciationByEmpId(EmployeeAppreciationRequest request) {
	    List<AppreciationDetailsDTO> appreciationList;

	    if (request.getFromDate() == null || request.getToDate() == null) {
	        appreciationList = appreciationRepository.getAppreciationDetailsByEmpId(request.getEmpId());
	    } else {
	        appreciationList = appreciationRepository.getAppreciationDetailsByEmpIdAndDateRange(
	            request.getEmpId(), request.getFromDate(), request.getToDate());
	    }

	    AppreciationDetails appreciationDetails = new AppreciationDetails();
	    appreciationDetails.setAppreciationDto(appreciationList);
	    return appreciationDetails;
	}
	
	public List<DateRangeDTO> getDateRangesForDropdown(Long empId) {
	    List<Object[]> dateRanges = appreciationRepository.getAllDateRangesByEmpId(empId);

	    List<DateRangeDTO> dateRangeDTOs = new ArrayList<>();
	    for (Object[] range : dateRanges) {
	        String fromDate = (String) range[0];
	        String toDate = (String) range[1];
	        dateRangeDTOs.add(new DateRangeDTO(fromDate, toDate));
	    }

	    return dateRangeDTOs;
	}
	
	public AppreciationDetails getTeamAppreciationByEmpId(EmployeeAppreciationRequest request) {
		List<AppreciationDetailsDTO> appreciationList;

	    appreciationList = appreciationRepository.getTeamAppreciationDetailsByEmpId(request.getEmpId());
	    AppreciationDetails appreciationDetails = new AppreciationDetails();
	    appreciationDetails.setAppreciationDto(appreciationList);
	    return appreciationDetails;
	}
	
	public ServiceResponse getEmployeeByEmpId(EmployeeDTO employeedto) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_employee");
		apiLogInfo.setApiUrl("/api/getEmployeeByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		
		EmployeeDTO empDTO = new EmployeeDTO();
		List<EmployeeCertificateDTO> certificationDTOlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> previousEmploymentDTOList = new ArrayList<PreviousEmploymentDTO>();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			List<Object[]> objectList = employeeRepository.getEmployeeByEmpId(employeedto.getEmpId());
			List<EmployeeCertificate> certificationsList = employeeCertificateRepository.findByEmpIdAndIsDraft(employeedto.getEmpId(), employeedto.getIsDraft());
			List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository.findByEmpIdAndIsDraft(employeedto.getEmpId(), employeedto.getIsDraft());
			List<Object[]> domaninSpecializationList = employeeSpecializationMapRepository.getEmployeeDomainInfo(employeedto.getEmpId());

			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					empDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
					empDTO.setCity(object[8] != null ? object[8].toString() : null);
					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
					empDTO.setDateOfBirth(
							object[10] != null ? format.format(format.parse(object[10].toString())) : null);
					empDTO.setDateOfJoining(
							object[11] != null ? format.format(format.parse(object[11].toString())) : null);
					empDTO.setEmail(object[12] != null ? object[12].toString() : null);
					empDTO.setEmergencyContactMobile(object[13] != null ? Long.parseLong(object[13].toString()) : null);
					empDTO.setEmploymentstatus(object[14] != null ? object[14].toString() : null);
					empDTO.setEsicNumber(object[15] != null ? object[15].toString() : null);
					empDTO.setFatherName(object[16] != null ? object[16].toString() : null);
					empDTO.setGender(object[17] != null ? object[17].toString() : null);
					empDTO.setGraduationType(object[18] != null ? object[18].toString() : null);
					empDTO.setPursuing(object[19] != null ? object[19].toString() : null);
					empDTO.setLandline(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					empDTO.setMaritalStatus(object[21] != null ? object[21].toString() : null);
					empDTO.setMobileNo(object[22] != null ? Long.parseLong(object[22].toString()) : null);
					empDTO.setMotherTongue(object[23] != null ? object[23].toString() : null);
					empDTO.setName(object[24] != null ? object[24].toString() : null);
					empDTO.setNoticePeriod(object[25] != null ? Short.parseShort(object[25].toString()) : null);
					empDTO.setAlternateMobileNo(object[26] != null ? Long.parseLong(object[26].toString()) : null);
					empDTO.setPanNumber(object[27] != null ? object[27].toString() : null);
					empDTO.setPassportNumber(object[28] != null ? object[28].toString() : null);
					empDTO.setPermanentAddress(object[29] != null ? object[29].toString() : null);
					empDTO.setPfAccountNumber(object[30] != null ? object[30].toString() : null);
					empDTO.setPincode(object[31] != null ? Integer.parseInt(object[31].toString()) : null);
					empDTO.setPlaceOfBirth(object[32] != null ? object[32].toString() : null);
					empDTO.setPassingGrade(object[33] != null ? object[33].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[34] != null ? object[34].toString() : null);
					empDTO.setRelation(object[35] != null ? object[35].toString() : null);
					empDTO.setState(object[36] != null ? object[36].toString() : null);
					empDTO.setUan(object[37] != null ? object[37].toString() : null);
					empDTO.setViewsOnOrganisation(object[38] != null ? object[38].toString() : null);
					empDTO.setYearOfPassing(object[39] != null ? Short.parseShort(object[39].toString()) : null);
					empDTO.setEmergencyContactPerson(object[41] != null ? object[41].toString() : null);
					empDTO.setManagerName(object[43] != null ? object[43].toString() : null);
					empDTO.setJobRoleName(object[44] != null ? object[44].toString() : null);
					empDTO.setDepartmentName(object[45] != null ? object[45].toString() : null);
					empDTO.setDepartmentId(object[46] != null ? Long.parseLong(object[46].toString()) : null);
					empDTO.setJobRoleId(object[47] != null ? Long.parseLong(object[47].toString()) : null);
					empDTO.setManagerId(object[48] != null ? Long.parseLong(object[48].toString()) : null);
					empDTO.setWorkLocation(object[49] != null ? (object[49].toString()) : null);
					empDTO.setExperience(object[50] != null ? (object[50].toString()) : null);
					empDTO.setRole(object[51] != null ? (object[51].toString()) : null);
					empDTO.setEmployeementId(object[52] != null ? Long.parseLong(object[52].toString()) : null);
					empDTO.setProbationPeriod(object[53] != null ? Short.parseShort(object[53].toString()) : null);
					empDTO.setDateOfResign(
							object[54] != null ? format.format(format.parse(object[54].toString())) : null);
					empDTO.setBillable(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild1(object[56] != null ? (object[56].toString()) : null);
					empDTO.setChild2(object[57] != null ? (object[57].toString()) : null);
					empDTO.setChild3(object[58] != null ? (object[58].toString()) : null);
					empDTO.setMothersName(object[59] != null ? (object[59].toString()) : null);
					empDTO.setSpouse(object[60] != null ? (object[60].toString()) : null);
					empDTO.setTotalExperience(object[61] != null ? Float.parseFloat(object[61].toString()) : null);
					empDTO.setSecondaryEmail(object[62] != null ? object[62].toString() : null);
					empDTO.setDateOfRelieving(object[63] != null ? format.format(format.parse(object[63].toString())) : null);
					empDTO.setReportingManagerId(object[64] != null ? Long.parseLong(object[64].toString()) : null);
					empDTO.setApprovalsTo(object[65] != null ? object[65].toString() : null);
					empDTO.setReportingManagerName(object[66] != null ? object[66].toString() : null);
					empDTO.setDesignationId(object[67] != null ? Long.parseLong(object[67].toString()) : null);
					empDTO.setDesignationName(object[68] != null ? object[68].toString() : null);
					empDTO.setEmploymentReleaseStatus(object[69] != null ? object[69].toString() : null);
					empDTO.setBillableType(object[70] != null ? object[70].toString() : null);
					empDTO.setIsConsultant(object[71] != null ? object[71].toString() : null);
					empDTO.setIsApprenticeship(object[72] != null ? object[72].toString() : null);
					empDTO.setReferedType(object[73] != null ? object[73].toString() : null);
					empDTO.setReferedName(object[74] != null ? object[74].toString() : null);

					empDTO.setEmployeeConfirmationDate(object[75] != null ? format.format(format.parse(object[75].toString())) : null);		
					empDTO.setIsApmosysProduct(object[76] != null ? object[76].toString() : null);
					String employmentId = empDTO.getEmployeementId().toString();

					if (employmentId != null) {

					    String prefix = "A-"; 

					    if ("true".equalsIgnoreCase(empDTO.getIsApmosysProduct())) {
					        prefix = "AP-";
					    } else if ("true".equalsIgnoreCase(empDTO.getIsConsultant())) {
					        prefix = "CS-";
					    }

					    empDTO.setEmployeementIdAccToET(prefix + employmentId);
					}
                    empDTO.setOnRollDate(object[77]!=null ? format.format(format.parse(object[77].toString())) : null);
					if (object[42] != null) {

						File actualFile = new File(
								Paths.get(imageFileLocation + File.separator + object[42].toString()).toString());

						if (actualFile.exists()) {
							byte[] imageBytes = Files.readAllBytes(
									Paths.get(imageFileLocation + File.separator + object[42].toString()));
							empDTO.setImageBytes(imageBytes);
						}

					}
					

					
				}

				if (!certificationsList.isEmpty()) {

					for (EmployeeCertificate empCert : certificationsList) {
						EmployeeCertificateDTO dto = new EmployeeCertificateDTO();

						dto.setEmployeeCertificateId(empCert.getEmployeeCertificateId());
						dto.setCertificationName(empCert.getCertificationName());
						dto.setDuration(empCert.getDuration());
						dto.setModeOfCourse(empCert.getModeOfCourse());
						dto.setDateOfCompletion(empCert.getDateOfCompletion().toString());
						dto.setCertificationNumber(empCert.getCertificationNumber());

						certificationDTOlist.add(dto);
					}
					empDTO.setCertifications(certificationDTOlist);
				}

				if (!previousEmploymentList.isEmpty()) {
					for (PreviousEmployment pervEmploy : previousEmploymentList) {
						PreviousEmploymentDTO dto = new PreviousEmploymentDTO();

						dto.setPreviousEmploymentId(pervEmploy.getPreviousEmploymentId());
						dto.setEmployerName(pervEmploy.getEmployerName());
						dto.setDateOfJoining(pervEmploy.getDateOfJoining().toString());
						dto.setDateOfRelieving(pervEmploy.getDateOfRelieving().toString());
						dto.setYearsOfExperience(pervEmploy.getYearsOfExperience());
						dto.setManagerName(pervEmploy.getManagerName());
						dto.setManagerContactNumber(pervEmploy.getManagerContactNumber());
						dto.setHrName(pervEmploy.getHrName());
						dto.setHrContactNumber(pervEmploy.getHrContactNumber());
						dto.setDesignation(pervEmploy.getDesignation());

						previousEmploymentDTOList.add(dto);
					}
					empDTO.setPreviousEmploymentList(previousEmploymentDTOList);
				}
				
				if(!domaninSpecializationList.isEmpty()) {
					Set<Long> domainIds = new HashSet<Long>();
					Set<Long> specializationIds = new HashSet<Long>();
					
					domaninSpecializationList.forEach((object) -> {
						domainIds.add(object[5] != null ? Long.parseLong(object[5].toString()) : null);
						specializationIds.add(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					});
					
					empDTO.setDomainList(domainIds.toArray(new Long[domainIds.size()]));
					empDTO.setSpecializationList(specializationIds.toArray(new Long[specializationIds.size()]));
				}
				
				EmpPrimaryProjectMapping employeeProject = empPrimaryProjectMappingRepository.findByEmpIdAndIsMapped(employeedto.getEmpId(),"Y");
				if(employeeProject!=null) {
					Project project = projectRepository.findByProjectId(employeeProject.getPrimaryProjectId().intValue());
					empDTO.setDefaultProjectName(project.getProjectName());			
					empDTO.setProjectId(employeeProject.getPrimaryProjectId().intValue());		
				}
						
						
						
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(empDTO);
				
				apiLogInfo.setApiResponse("empDTO : " +empDTO);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not Found");
				
				apiLogInfo.setApiResponse("Employee Profile Not Found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_employee");
		apiLogInfo.setApiUrl("/api/deleteEmployeeByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		try {

			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employeeToBeDeleted = employeeObject.get();
				Long count = employeeRepository.countByManagerId(employeeToBeDeleted.getEmpId());

				if (count == 0) {

					List<EmployeeTeamMap> deleteEmpFromTeam = employeeTeamMapRepository
							.findByEmpId(employeeToBeDeleted.getEmpId());

					for (EmployeeTeamMap empToBeDeletedFromTeam : deleteEmpFromTeam) {
						// 1: Active 0: InActive
						empToBeDeletedFromTeam.setActive((long) 0);
						if(empToBeDeletedFromTeam.getUpdatedOn() == null) {
							empToBeDeletedFromTeam.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
						}	
						employeeTeamMapRepository.save(empToBeDeletedFromTeam);
					}
					employeeToBeDeleted.setEmploymentstatus("InActive");
					employeeRepository.save(employeeToBeDeleted);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employment Status changed to InActive");
					
					apiLogInfo.setApiResponse("Employment Status changed to InActive");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee cannot be deleted as reportee(s) are mapped to him/her.");
					
					apiLogInfo.setApiResponse("Employee cannot be deleted as reportee(s) are mapped to him/her.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not Found");
				
				apiLogInfo.setApiResponse("Employee Profile Not Found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse changeManagerMapping(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_template");
		apiLogInfo.setApiUrl("/api/changeManagerMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("oldManagerId : " + employeedto.getOldManagerId()+ "managerId : " +employeedto.getManagerId()) ;
		try {
			
			List<Employee> mappedEmployees = employeeRepository.findByManagerId(employeedto.getOldManagerId());
			
			if(!mappedEmployees.isEmpty()) {
				
				for(Employee employee: mappedEmployees) {
					employee.setManagerId(employeedto.getManagerId());
					
					Employee dbResponse = employeeRepository.save(employee);

					if (dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Department Deleted");
						
						apiLogInfo.setApiResponse("Department Deleted.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Job Role Updation Failed.");
					
						apiLogInfo.setApiResponse("Job Role Updation Failed.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						
					}
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Reportee(s) Found");
				
				apiLogInfo.setApiResponse("No Reportee(s) Found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
//	public ServiceResponse updateEmployeeByEmpId(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("update_employee");
//		apiLogInfo.setApiUrl("/api/updateEmployeeByEmpId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("empId : " + employeedto.getEmpId()+ "certification :" +employeedto.getCertifications()+ "updatedCertification :"+employeedto.getUpdatedCertifications()+ "experience : "+employeedto.getExperience());
//		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
//		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();
//
//		List<Employee> listOfReporties = employeeRepository.findByManagerId(employeedto.getEmpId());
//		List<Employee> listOfEmp = new ArrayList<>();
//		
//		try {
//			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
//			if (employeeObject.isPresent()) {
//				Employee employee = employeeObject.get();
//
//				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				employee.setEmployeementId(employeedto.getEmployeementId());
//				employee.setName(employeedto.getName());
//				employee.setDateOfBirth(employeedto.getDateOfBirth() != null
//						? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd")
//						: null);
//				employee.setDateOfJoining(employeedto.getDateOfJoining() != null
//						? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd")
//						: null);
//				
//				if(employeedto.getReportiesFlag().equals("No")) {
//					System.err.println("New manager updated "+ employeedto.getManagerId());
//					employee.setManagerId(employeedto.getManagerId());	
//				}
//				else {
//					for(Employee emp : listOfReporties) {
//						emp.setManagerId(employeedto.getNewManagerId());
//						employeeRepository.save(emp);
//						System.err.println(" Manager mapping done ");
//						
//					}
//					
//				}
//					
//				
//				employee.setEmail(employeedto.getEmail());
//				employee.setSecondaryEmail(employeedto.getSecondaryEmail());
//				employee.setGender(employeedto.getGender());
//				employee.setBloodGroup(employeedto.getBloodGroup());
//				employee.setMaritalStatus(employeedto.getMaritalStatus());
//				employee.setFatherName(employeedto.getFatherName());
//				employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
//				employee.setMotherTongue(employeedto.getMotherTongue());
//				employee.setPassportNumber(employeedto.getPassportNumber());
//				employee.setAadhar(employeedto.getAadhar());
//				employee.setPanNumber(employeedto.getPanNumber());
//				employee.setMobileNo(employeedto.getMobileNo());
//				employee.setLandline(employeedto.getLandline());
//				employee.setAddress(employeedto.getAddress());
//				employee.setCity(employeedto.getCity());
//				employee.setState(employeedto.getState());
//				employee.setCountry(employeedto.getCountry());
//				employee.setPincode(employeedto.getPincode());
//				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
//				employee.setPermanentAddress(employeedto.getPermanentAddress());
//				employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
//				employee.setRelation(employeedto.getRelation());
//				employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
//				employee.setNoticePeriod(employeedto.getNoticePeriod());
//				employee.setEmploymentstatus(employeedto.getEmploymentstatus());
//				employee.setBankName(employeedto.getBankName());
//				employee.setBankAccountNo(employeedto.getBankAccountNo());
//				employee.setBankIFSCCode(employeedto.getBankIFSCCode());
//				employee.setPfAccountNumber(employeedto.getPfAccountNumber());
//				employee.setPreviousPfAccountNumber(employeedto.getPreviousPfAccountNumber());
//				employee.setUan(employeedto.getUan());
//				employee.setEsicNumber(employeedto.getEsicNumber());
//				employee.setGraduationType(employeedto.getGraduationType());
//				employee.setPursuing(employeedto.getPursuing());
//				employee.setYearOfPassing(employeedto.getYearOfPassing());
//				employee.setPassingGrade(employeedto.getPassingGrade());
//				employee.setAboutMe(employeedto.getAboutMe());
//				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
//				employee.setJobRoleId(employeedto.getJobRoleId());
//				employee.setExperience(employeedto.getExperience());
//				employee.setRole(employeedto.getRole());
//				employee.setWorkLocation(employeedto.getWorkLocation());
//				employee.setProbationPeriod(employeedto.getProbationPeriod());
//				if(employee.getEmploymentstatus().equals("Resigned") || employee.getEmploymentstatus().equals("InActive") )  {
//					System.out.println("Right method call    ");
//					employee.setDateOfResign(employeedto.getDateOfResign() != null
//							? stringToDateTimeParser.getDate(employeedto.getDateOfResign(), "yyyy-MM-dd")
//							: null);
//					
//					if(employeedto.getUpdateType() != null) {
//						if(employeedto.getUpdateType().equals("automatic") && (!employeedto.getEmploymentstatus().equals("Resigned")) ) {
//							for(Employee emp : listOfReporties) {
//								emp.setManagerId(employeedto.getNewManagerId());
////								employeeRepository.save(emp);
//								listOfEmp.add(emp);
//								System.err.println(" Manager mapping done ");
//								
//							}
//							
////							get compOff leaves 
//							
////							Optional<List<CompOffLeave>> findListOfCompOff = compOffLeaveRepository.findCompOffByEmpId(employeedto.getEmpId());
////							
////							if(findListOfCompOff.isPresent()) {
////								List<CompOffLeave> findCompOffs = findListOfCompOff.get();
////								
////								for (CompOffLeave compOff : findCompOffs) {
////									System.out.println(" compOff Id     ::   \n"+compOff.getCompOffLeaveId());
////									compOff.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////									
////									compOffLeaveRepository.save(compOff);	
////								}
////							}
//							
////							get leaves 
//							
////							Optional<List<EmployeeLeave>> findListOfLeaves = employeeLeaveRepository.findLeaveByManagerId(employeedto.getEmpId());
////							System.err.println(" findListOfLeaves    "+findListOfLeaves);
////							if(findListOfLeaves.isPresent()) {
////								List<EmployeeLeave> findLeaves = findListOfLeaves.get();
////								
////							for (EmployeeLeave empLeaves : findLeaves) {
////								System.out.println( " leavesId   ::   \n"+empLeaves.getLeaveId());
////								empLeaves.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////								employeeLeaveRepository.save(empLeaves);
////								
////							}
////						}						
//							
//							// department HOD 
//							
//							Optional<List<Department>> findDept = Optional.ofNullable(departmentRepository.findByHodId(employeedto.getEmpId()));
//							if(findDept.isPresent() && !findDept.isEmpty()) {
//								System.err.println(" department update call ");
//								List<Department> listOfDept = findDept.get();
//								int count =0;
//								for (Department department : listOfDept) {
//									department.setHodId(employeedto.getNewManagerId());
//									departmentRepository.save(department);
//									count = count+1;
//								}
//								System.err.println(" count total "+count);
//							}
//							
//							
//							employeeRepository.saveAll(listOfEmp);
//							}	
//					}
//					
//					
//				}else if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed")) {
//					employee.setDateOfResign(null);
//				}
//				if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed") || employee.getEmploymentstatus().equals("Resigned")) {
//					employee.setEmploymentReleaseStatus(null);
//				}else {
//					employee.setEmploymentReleaseStatus(employeedto.getEmploymentReleaseStatus());
//				}
//				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
//				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
//				employee.setBillable(employeedto.getBillable());
//				employee.setBillableType(employeedto.getBillableType());
//				employee.setChild1(employeedto.getChild1());
//				employee.setChild2(employeedto.getChild2());
//				employee.setChild3(employeedto.getChild3());
//				employee.setMothersName(employeedto.getMothersName());
//				employee.setSpouse(employeedto.getSpouse());
//				employee.setTotalExperience(employeedto.getTotalExperience());
//				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				employee.setDesignationId(employeedto.getDesignationId());
//				employee.setReportingManagerId(employeedto.getReportingManagerId());
//				if(employeedto.getReportingManagerId() == null) {
//					employee.setApprovalsTo(null);
//				}else {
//					employee.setApprovalsTo(employeedto.getApprovalsTo());
//				}
//				
//				// Certification
//				// Case 1 : Updating Existing certification
//				if (employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
//					employeedto.getCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() != null)
//							.forEach((certificate) -> {
//
//								EmployeeCertificate employeeCertificate = employeeCertificateRepository
//										.findById(certificate.getEmployeeCertificateId()).get();
//
//								employeeCertificate.setCertificationName(certificate.getCertificationName());
//								employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
//								employeeCertificate.setDateOfCompletion(stringToDateTimeParser
//										.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
//								employeeCertificate.setDuration(certificate.getDuration());
//								employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
//								employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());
//
//								employeeCertificateRepository.save(employeeCertificate);
//							});
//				}
//
//				if (employeedto.getUpdatedCertifications() != null
//						&& !employeedto.getUpdatedCertifications().isEmpty()) {
//					// Case 2 : Adding New certification
//					newCertificationlist = employeedto.getUpdatedCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() == null)
//							.collect(Collectors.toList());
//					if (!newCertificationlist.isEmpty()) {
//						newCertificationlist.forEach((certification) -> {
//							certification.setEmpId(employeedto.getEmpId());
//
//						});
//						addCertifications(newCertificationlist, employeedto.getIsDraft());
//					}
//
//					// Case 3 : Deleting Removed certification
//					employeedto.getUpdatedCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() != null)
//							.forEach((certification) -> {
//								employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
//							});
//				}
//
//				// Previous Employer
//				if (employeedto.getExperience().equals("Fresher")) {
//					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
//							.findByEmpId(employeedto.getEmpId());
//
//					if (!previousEmploymentList.isEmpty()) {
//						previousEmploymentList.stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((prevEmployer) -> {
//									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
//								});
//					}
//				} else {
//					if (employeedto.getPreviousEmploymentList() != null
//							&& !employeedto.getPreviousEmploymentList().isEmpty()) {
//						employeedto.getPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((previousEmployeeDTO) -> {
//
//									PreviousEmployment previousEmployment = previousEmploymentRepository
//											.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();
//
//									previousEmployment.setDateOfJoining(stringToDateTimeParser
//											.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd"));
//									previousEmployment.setDateOfRelieving(stringToDateTimeParser
//											.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd"));
//									previousEmployment.setDesignation(previousEmployeeDTO.getDesignation());
//									previousEmployment.setHrContactNumber(previousEmployeeDTO.getHrContactNumber());
//									previousEmployment.setHrName(previousEmployeeDTO.getHrName());
//									previousEmployment.setManagerName(previousEmployeeDTO.getManagerName());
//									previousEmployment
//											.setManagerContactNumber(previousEmployeeDTO.getManagerContactNumber());
//									previousEmployment.setEmployerName(previousEmployeeDTO.getEmployerName());
//									previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());
//
//									previousEmploymentRepository.save(previousEmployment);
//								});
//					}
//
//					if (employeedto.getUpdatedPreviousEmploymentList() != null
//							&& !employeedto.getUpdatedPreviousEmploymentList().isEmpty()) {
//						newPreviousEmploymentList = employeedto.getUpdatedPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null)
//								.collect(Collectors.toList());
//						if (!newPreviousEmploymentList.isEmpty()) {
//							newPreviousEmploymentList.forEach((previousEmployer) -> {
//								previousEmployer.setEmpId(employeedto.getEmpId());
//
//							});
//							addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
//						}
//
//						employeedto.getUpdatedPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((prevEmployer) -> {
//									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
//								});
//					}
//				}
//				
//				//Employee Specialization Mapping
//				
//				if(employeedto.getSpecializationList() != null && employeedto.getSpecializationList().length != 0) {
//					List<EmployeeSpecializationMap> mappingObj = employeeSpecializationMapRepository.findByEmpId(employee.getEmpId());
//					if(!mappingObj.isEmpty()) {
//						mappingObj.forEach((object) -> {
//							boolean contains = Arrays.stream(employeedto.getSpecializationList()).anyMatch(i -> i.equals(object.getSpecializationId()));
//							
//							//Delete Specialization
//							if(!contains) {
//								employeeSpecializationMapRepository.deleteById(object.getEmpSpecializationMapId());
//							}
//							
//						});
//					}
//					for(Long specializationId: employeedto.getSpecializationList()) {
//						EmployeeSpecializationMap empMapObj = employeeSpecializationMapRepository.findByEmpIdAndSpecializationId(employee.getEmpId(),specializationId);
//						
//						//Add new Specialization
//						if(empMapObj == null) {
//								EmployeeSpecializationMap empSpecObj = new EmployeeSpecializationMap();
//								
//								empSpecObj.setEmpId(employee.getEmpId());
//								empSpecObj.setSpecializationId(specializationId);
//								
//								EmployeeSpecializationMap dbResponse = employeeSpecializationMapRepository.save(empSpecObj);
//						}
//					}
//				}
//				
//				
//				Employee dbResponse = employeeRepository.save(employee);
//					
//				
//				if (dbResponse != null) {
//					
//					//Update Draft
//					DraftEmployee draftEmployee = draftEmployeeRepository.findByEmployeementId(dbResponse.getEmployeementId());
//					
//					if(draftEmployee != null) {
//						draftEmployee.setName(dbResponse.getName());
//						draftEmployee.setDateOfBirth(dbResponse.getDateOfBirth());
//						draftEmployee.setDateOfJoining(dbResponse.getDateOfJoining());
//						draftEmployee.setManagerId(dbResponse.getManagerId());
//						draftEmployee.setEmail(dbResponse.getEmail());
//						draftEmployee.setMobileNo(dbResponse.getMobileNo());
//						draftEmployee.setNoticePeriod(dbResponse.getNoticePeriod());
//						draftEmployee.setEmploymentstatus(dbResponse.getEmploymentstatus());
//						draftEmployee.setJobRoleId(dbResponse.getJobRoleId());
//						draftEmployee.setExperience(dbResponse.getExperience());
//						draftEmployee.setRole(dbResponse.getRole());
//						draftEmployee.setWorkLocation(dbResponse.getWorkLocation());
//						draftEmployee.setUpdatedBy(Integer.parseInt(dbResponse.getUpdatedBy().toString()));
//						draftEmployee.setBillable(dbResponse.getBillable());
//						draftEmployee.setTotalExperience(dbResponse.getTotalExperience());
//						draftEmployee.setUpdatedOn(dbResponse.getUpdatedOn());
//						draftEmployee.setDesignationId(dbResponse.getDesignationId());
//						draftEmployee.setDateOfResign(dbResponse.getDateOfResign());
//						draftEmployee.setDateOfRelieving(dbResponse.getDateOfRelieving());
//						
//						draftEmployee.setReportingManagerId(dbResponse.getReportingManagerId());
//						if(dbResponse.getReportingManagerId() == null){
//							draftEmployee.setApprovalsTo(null);
//						}else {							
//							draftEmployee.setApprovalsTo(dbResponse.getApprovalsTo());
//						}
//						
//						draftEmployeeRepository.save(draftEmployee);
//					}
//					
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Employee Profile Updated.");
//					
//					apiLogInfo.setApiResponse("Employee Profile Updated.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Employee Profile Updation Failed.");
//					
//					apiLogInfo.setApiResponse("Employee Profile Updation Failed.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee Profile Not Found");
//				
//				apiLogInfo.setApiResponse("Employee Profile Not Found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
//	public ServiceResponse updateEmployeeByEmpId(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("update_employee");
//		apiLogInfo.setApiUrl("/api/updateEmployeeByEmpId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("empId : " + employeedto.getEmpId()+ "certification :" +employeedto.getCertifications()+ "updatedCertification :"+employeedto.getUpdatedCertifications()+ "experience : "+employeedto.getExperience());
//		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
//		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();
//
//		List<Employee> listOfReporties = employeeRepository.findByManagerId(employeedto.getEmpId());
//		List<Employee> listOfEmp = new ArrayList<>();
//		
//		try {
//			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
//			if (employeeObject.isPresent()) {
//				Employee employee = employeeObject.get();
//
//				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				employee.setEmployeementId(employeedto.getEmployeementId());
//				employee.setName(employeedto.getName());
//				employee.setDateOfBirth(employeedto.getDateOfBirth() != null
//						? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd")
//						: null);
//				employee.setDateOfJoining(employeedto.getDateOfJoining() != null
//						? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd")
//						: null);
//				employee.setManagerId(employeedto.getManagerId());
//				employee.setEmail(employeedto.getEmail());
//				employee.setSecondaryEmail(employeedto.getSecondaryEmail());
//				employee.setGender(employeedto.getGender());
//				employee.setBloodGroup(employeedto.getBloodGroup());
//				employee.setMaritalStatus(employeedto.getMaritalStatus());
//				employee.setFatherName(employeedto.getFatherName());
//				employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
//				employee.setMotherTongue(employeedto.getMotherTongue());
//				employee.setPassportNumber(employeedto.getPassportNumber());
//				employee.setAadhar(employeedto.getAadhar());
//				employee.setPanNumber(employeedto.getPanNumber());
//				employee.setMobileNo(employeedto.getMobileNo());
//				employee.setLandline(employeedto.getLandline());
//				employee.setAddress(employeedto.getAddress());
//				employee.setCity(employeedto.getCity());
//				employee.setState(employeedto.getState());
//				employee.setCountry(employeedto.getCountry());
//				employee.setPincode(employeedto.getPincode());
//				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
//				employee.setPermanentAddress(employeedto.getPermanentAddress());
//				employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
//				employee.setRelation(employeedto.getRelation());
//				employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
//				employee.setNoticePeriod(employeedto.getNoticePeriod());
//				employee.setEmploymentstatus(employeedto.getEmploymentstatus());
//				employee.setBankName(employeedto.getBankName());
//				employee.setBankAccountNo(employeedto.getBankAccountNo());
//				employee.setBankIFSCCode(employeedto.getBankIFSCCode());
//				employee.setPfAccountNumber(employeedto.getPfAccountNumber());
//				employee.setPreviousPfAccountNumber(employeedto.getPreviousPfAccountNumber());
//				employee.setUan(employeedto.getUan());
//				employee.setEsicNumber(employeedto.getEsicNumber());
//				employee.setGraduationType(employeedto.getGraduationType());
//				employee.setPursuing(employeedto.getPursuing());
//				employee.setYearOfPassing(employeedto.getYearOfPassing());
//				employee.setPassingGrade(employeedto.getPassingGrade());
//				employee.setAboutMe(employeedto.getAboutMe());
//				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
//				employee.setJobRoleId(employeedto.getJobRoleId());
//				employee.setExperience(employeedto.getExperience());
//				employee.setRole(employeedto.getRole());
//				employee.setWorkLocation(employeedto.getWorkLocation());
//				employee.setProbationPeriod(employeedto.getProbationPeriod());
//				if(employee.getEmploymentstatus().equals("Resigned") || employee.getEmploymentstatus().equals("InActive") )  {
//					System.out.println("Right method call    ");
//					employee.setDateOfResign(employeedto.getDateOfResign() != null
//							? stringToDateTimeParser.getDate(employeedto.getDateOfResign(), "yyyy-MM-dd")
//							: null);
//					
//					if(employeedto.getUpdateType() != null) {
//						if(employeedto.getUpdateType().equals("automatic") && (!employeedto.getEmploymentstatus().equals("Resigned")) ) {
//							for(Employee emp : listOfReporties) {
//								emp.setManagerId(employeedto.getNewManagerId());
//								listOfEmp.add(emp);
//								System.err.println(" Manager mapping done ");
//								
//							}
//							
////							get compOff leaves 
//							
////							Optional<List<CompOffLeave>> findListOfCompOff = compOffLeaveRepository.findCompOffByEmpId(employeedto.getEmpId());
////							
////							if(findListOfCompOff.isPresent()) {
////								List<CompOffLeave> findCompOffs = findListOfCompOff.get();
////								
////								for (CompOffLeave compOff : findCompOffs) {
////									System.out.println(" compOff Id     ::   \n"+compOff.getCompOffLeaveId());
////									compOff.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////									
////									compOffLeaveRepository.save(compOff);	
////								}
////							}
////							
//////							get leaves 
////							
////							Optional<List<EmployeeLeave>> findListOfLeaves = employeeLeaveRepository.findLeaveByManagerId(employeedto.getEmpId());
////							System.err.println(" findListOfLeaves    "+findListOfLeaves);
////							if(findListOfLeaves.isPresent()) {
////								List<EmployeeLeave> findLeaves = findListOfLeaves.get();
////								
////							for (EmployeeLeave empLeaves : findLeaves) {
////								System.out.println( " leavesId   ::   \n"+empLeaves.getLeaveId());
////								empLeaves.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////								employeeLeaveRepository.save(empLeaves);
////								
////							}
////						}						
////							
//							// department HOD 
//							
////							Optional<List<Department>> findDept = Optional.ofNullable(departmentRepository.findByHodId(employeedto.getEmpId()));
////							if(findDept.isPresent() && !findDept.isEmpty()) {
////								System.err.println(" department update call ");
////								List<Department> listOfDept = findDept.get();
////								int count =0;
////								for (Department department : listOfDept) {
////									department.setHodId(employeedto.getNewManagerId());
////									departmentRepository.save(department);
////									count = count+1;
////								}
////								System.err.println(" count total "+count);
////							}
////							
//							
//							employeeRepository.saveAll(listOfEmp);
//							}	
//					}
//					
//					
//				}else if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed")) {
//					employee.setDateOfResign(null);
//				}
//				if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed") || employee.getEmploymentstatus().equals("Resigned")) {
//					employee.setEmploymentReleaseStatus(null);
//				}else {
//					employee.setEmploymentReleaseStatus(employeedto.getEmploymentReleaseStatus());
//				}
//				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
//				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
//				employee.setBillable(employeedto.getBillable());
//				employee.setBillableType(employeedto.getBillableType());
//				employee.setChild1(employeedto.getChild1());
//				employee.setChild2(employeedto.getChild2());
//				employee.setChild3(employeedto.getChild3());
//				employee.setMothersName(employeedto.getMothersName());
//				employee.setSpouse(employeedto.getSpouse());
//				employee.setTotalExperience(employeedto.getTotalExperience());
//				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				employee.setDesignationId(employeedto.getDesignationId());
//				employee.setReportingManagerId(employeedto.getReportingManagerId());
//				if(employeedto.getReportingManagerId() == null) {
//					employee.setApprovalsTo(null);
//				}else {
//					employee.setApprovalsTo(employeedto.getApprovalsTo());
//				}
//				
//				// Certification
//				// Case 1 : Updating Existing certification
//				if (employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
//					employeedto.getCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() != null)
//							.forEach((certificate) -> {
//
//								EmployeeCertificate employeeCertificate = employeeCertificateRepository
//										.findById(certificate.getEmployeeCertificateId()).get();
//
//								employeeCertificate.setCertificationName(certificate.getCertificationName());
//								employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
//								employeeCertificate.setDateOfCompletion(stringToDateTimeParser
//										.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
//								employeeCertificate.setDuration(certificate.getDuration());
//								employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
//								employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());
//
//								employeeCertificateRepository.save(employeeCertificate);
//							});
//				}
//
//				if (employeedto.getUpdatedCertifications() != null
//						&& !employeedto.getUpdatedCertifications().isEmpty()) {
//					// Case 2 : Adding New certification
//					newCertificationlist = employeedto.getUpdatedCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() == null)
//							.collect(Collectors.toList());
//					if (!newCertificationlist.isEmpty()) {
//						newCertificationlist.forEach((certification) -> {
//							certification.setEmpId(employeedto.getEmpId());
//
//						});
//						addCertifications(newCertificationlist, employeedto.getIsDraft());
//					}
//
//					// Case 3 : Deleting Removed certification
//					employeedto.getUpdatedCertifications().stream()
//							.filter((certification) -> certification.getEmployeeCertificateId() != null)
//							.forEach((certification) -> {
//								employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
//							});
//				}
//
//				// Previous Employer
//				if (employeedto.getExperience().equals("Fresher")) {
//					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
//							.findByEmpId(employeedto.getEmpId());
//
//					if (!previousEmploymentList.isEmpty()) {
//						previousEmploymentList.stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((prevEmployer) -> {
//									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
//								});
//					}
//				} else {
//					if (employeedto.getPreviousEmploymentList() != null
//							&& !employeedto.getPreviousEmploymentList().isEmpty()) {
//						employeedto.getPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((previousEmployeeDTO) -> {
//
//									PreviousEmployment previousEmployment = previousEmploymentRepository
//											.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();
//
//									previousEmployment.setDateOfJoining(stringToDateTimeParser
//											.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd"));
//									previousEmployment.setDateOfRelieving(stringToDateTimeParser
//											.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd"));
//									previousEmployment.setDesignation(previousEmployeeDTO.getDesignation());
//									previousEmployment.setHrContactNumber(previousEmployeeDTO.getHrContactNumber());
//									previousEmployment.setHrName(previousEmployeeDTO.getHrName());
//									previousEmployment.setManagerName(previousEmployeeDTO.getManagerName());
//									previousEmployment
//											.setManagerContactNumber(previousEmployeeDTO.getManagerContactNumber());
//									previousEmployment.setEmployerName(previousEmployeeDTO.getEmployerName());
//									previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());
//
//									previousEmploymentRepository.save(previousEmployment);
//								});
//					}
//
//					if (employeedto.getUpdatedPreviousEmploymentList() != null
//							&& !employeedto.getUpdatedPreviousEmploymentList().isEmpty()) {
//						newPreviousEmploymentList = employeedto.getUpdatedPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null)
//								.collect(Collectors.toList());
//						if (!newPreviousEmploymentList.isEmpty()) {
//							newPreviousEmploymentList.forEach((previousEmployer) -> {
//								previousEmployer.setEmpId(employeedto.getEmpId());
//
//							});
//							addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
//						}
//
//						employeedto.getUpdatedPreviousEmploymentList().stream()
//								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
//								.forEach((prevEmployer) -> {
//									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
//								});
//					}
//				}
//				
//				//Employee Specialization Mapping
//				
//				if(employeedto.getSpecializationList() != null && employeedto.getSpecializationList().length != 0) {
//					List<EmployeeSpecializationMap> mappingObj = employeeSpecializationMapRepository.findByEmpId(employee.getEmpId());
//					if(!mappingObj.isEmpty()) {
//						mappingObj.forEach((object) -> {
//							boolean contains = Arrays.stream(employeedto.getSpecializationList()).anyMatch(i -> i.equals(object.getSpecializationId()));
//							
//							//Delete Specialization
//							if(!contains) {
//								employeeSpecializationMapRepository.deleteById(object.getEmpSpecializationMapId());
//							}
//							
//						});
//					}
//					for(Long specializationId: employeedto.getSpecializationList()) {
//						EmployeeSpecializationMap empMapObj = employeeSpecializationMapRepository.findByEmpIdAndSpecializationId(employee.getEmpId(),specializationId);
//						
//						//Add new Specialization
//						if(empMapObj == null) {
//								EmployeeSpecializationMap empSpecObj = new EmployeeSpecializationMap();
//								
//								empSpecObj.setEmpId(employee.getEmpId());
//								empSpecObj.setSpecializationId(specializationId);
//								
//								EmployeeSpecializationMap dbResponse = employeeSpecializationMapRepository.save(empSpecObj);
//						}
//					}
//				}
//				
//				
//				Employee dbResponse = employeeRepository.save(employee);
//					
//				
//				if (dbResponse != null) {
//					
//					//Update Draft
//					DraftEmployee draftEmployee = draftEmployeeRepository.findByEmployeementId(dbResponse.getEmployeementId());
//					
//					if(draftEmployee != null) {
//						draftEmployee.setName(dbResponse.getName());
//						draftEmployee.setDateOfBirth(dbResponse.getDateOfBirth());
//						draftEmployee.setDateOfJoining(dbResponse.getDateOfJoining());
//						draftEmployee.setManagerId(dbResponse.getManagerId());
//						draftEmployee.setEmail(dbResponse.getEmail());
//						draftEmployee.setMobileNo(dbResponse.getMobileNo());
//						draftEmployee.setNoticePeriod(dbResponse.getNoticePeriod());
//						draftEmployee.setEmploymentstatus(dbResponse.getEmploymentstatus());
//						draftEmployee.setJobRoleId(dbResponse.getJobRoleId());
//						draftEmployee.setExperience(dbResponse.getExperience());
//						draftEmployee.setRole(dbResponse.getRole());
//						draftEmployee.setWorkLocation(dbResponse.getWorkLocation());
//						draftEmployee.setUpdatedBy(Integer.parseInt(dbResponse.getUpdatedBy().toString()));
//						draftEmployee.setBillable(dbResponse.getBillable());
//						draftEmployee.setTotalExperience(dbResponse.getTotalExperience());
//						draftEmployee.setUpdatedOn(dbResponse.getUpdatedOn());
//						draftEmployee.setDesignationId(dbResponse.getDesignationId());
//						draftEmployee.setDateOfResign(dbResponse.getDateOfResign());
//						draftEmployee.setDateOfRelieving(dbResponse.getDateOfRelieving());
//						
//						draftEmployee.setReportingManagerId(dbResponse.getReportingManagerId());
//						if(dbResponse.getReportingManagerId() == null){
//							draftEmployee.setApprovalsTo(null);
//						}else {							
//							draftEmployee.setApprovalsTo(dbResponse.getApprovalsTo());
//						}
//						
//						draftEmployeeRepository.save(draftEmployee);
//					}
//					
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Employee Profile Updated.");
//					
//					apiLogInfo.setApiResponse("Employee Profile Updated.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Employee Profile Updation Failed.");
//					
//					apiLogInfo.setApiResponse("Employee Profile Updation Failed.");			
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee Profile Not Found");
//				
//				apiLogInfo.setApiResponse("Employee Profile Not Found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	public ServiceResponse updateEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_employee");
		apiLogInfo.setApiUrl("/api/updateEmployeeByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId()+ "certification :" +employeedto.getCertifications()+ "updatedCertification :"+employeedto.getUpdatedCertifications()+ "experience : "+employeedto.getExperience());
		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
//		System.out.println("newCertificationlist : " + newCertificationlist);
		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();
//		System.out.println("newPreviousEmploymentList : " + newPreviousEmploymentList);
		List<Employee> listOfReporties = employeeRepository.findByManagerId(employeedto.getEmpId());
//		System.out.println("listOfReporties : " + listOfReporties);
		List<Employee> listOfEmp = new ArrayList<>();
//		System.out.println("listOfEmp : "+listOfEmp);		
		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			
			if (employeeObject.isPresent()) {
				Long updatedBy = employeedto.getUpdatedBy();

				Employee employee2 = employeeObject.get();
				if(!employee2.getEmployeementId().equals(employeedto.getEmployeementId())) {
					ServiceResponse employeementIdExists = checkEmployeementId(employeedto);
					if (employeementIdExists.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(employeementIdExists.getServiceResponse()
								+ " Kindly provide a different value for Employment ID.");
						
						apiLogInfo.setApiResponse(employeementIdExists.getServiceResponse()
								+ " Kindly provide a different value for Employment ID.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						
						return response;
					}
				}
//				System.out.println("Employee 1 : " + employee);
				
				//timesheet validation before setting inactive 
				if (employeedto.getEmploymentstatus().equals("InActive")) {
					
					// will check for Active, Confirmed, or Probation
					String currentStatus = employee2.getEmploymentstatus();
					if (currentStatus.equals("Active") || 
						currentStatus.equals("Confirmed") || 
						currentStatus.equals("Probation")) {
						
						LocalDate dateOfRelieving = null;
						if (employeedto.getDateOfRelieving() != null && !employeedto.getDateOfRelieving().isEmpty()) {
						    dateOfRelieving = LocalDate.parse(employeedto.getDateOfRelieving());
						} else if (employee2.getDateOfRelieving() != null && !employee2.getDateOfRelieving().isEmpty()) {
						    dateOfRelieving = LocalDate.parse(employee2.getDateOfRelieving());
						}

						ServiceResponse timesheetValidation = getPendingTimesheetProjects(
						    employeedto.getEmpId(),
						    dateOfRelieving
						);
				        
				        if (timesheetValidation.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				            response.setServiceResponse(timesheetValidation.getServiceResponse());
				            
				            apiLogInfo.setApiResponse((String) timesheetValidation.getServiceResponse());
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				            
				            apiLogInfo.setApiRequest(logBuilder.toString());
				            logService.logMyInfo(httpRequest, apiLogInfo);
				            return response;
						}
					}
				}

				if (Boolean.TRUE.equals(employeedto.getIsUpdateDefaultProject())) {

					if (employeedto.getDefaultProjectId() != null) {
						List<EmployeeTeamMap> existingTeamMappingForFutureDate = employeeTeamMapRepository.findByEmpIdAndTeamIdAndStartDateGreaterThanCurrentDate(employee2.getEmpId(), employeedto.getDefaultTeamId());	
						if (existingTeamMappingForFutureDate != null && !existingTeamMappingForFutureDate.isEmpty() && existingTeamMappingForFutureDate.size() > 1) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				            response.setServiceResponse("Multiple team mappings found for the same employee and team for the future date. Kindly contact admin!!");
				            apiLogInfo.setApiResponse((String) "Multiple team mappings found for the same employee and team for the future date. Kindly contact admin!!");
				            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				            apiLogInfo.setApiRequest(logBuilder.toString());
				            logService.logMyInfo(httpRequest, apiLogInfo);
				            return response;
						}

						List<EmployeeTeamMap> existingTeamMappings = employeeTeamMapRepository
								.findByEmpIdAndProjectId(employeedto.getEmpId(), employeedto.getProjectId());
						if (existingTeamMappings != null) {

							LocalDateTime now = LocalDateTime.now();
							for (EmployeeTeamMap existingMap : existingTeamMappings) {
								if (employeedto.getOldEtmEndDate() == null) {
									existingMap.setActive(0L);
									existingMap.setEndDate(now);
								} else {
									if (!employeedto.getOldEtmEndDate().toLocalDate().isAfter(now.toLocalDate())) {
										existingMap.setActive(0L);
									}
									existingMap.setEndDate(employeedto.getOldEtmEndDate());
								}

								existingMap.setUpdatedBy(updatedBy);
							}
							employeeTeamMapRepository.saveAll(existingTeamMappings);
						}

						StringBuilder employeeRole = new StringBuilder("");
						for (String empRole : employeedto.getDefaultTeamEmployeeRole()) {
							employeeRole.append(empRole).append(",");
						}

						Department dept = departmentRepository.findByDeptId(employeedto.getDepartmentId());

						EmployeeTeamMap employeeTeamMap =  new EmployeeTeamMap();
						if (existingTeamMappingForFutureDate != null && !existingTeamMappingForFutureDate.isEmpty() && existingTeamMappingForFutureDate.size() == 1) {
							employeeTeamMap = existingTeamMappingForFutureDate.get(0);
						}
						employeeTeamMap.setEmpId(employee2.getEmpId());
						employeeTeamMap.setTeamId(employeedto.getDefaultTeamId());
						employeeTeamMap.setActive(2l);
						employeeTeamMap.setStartDate(employeedto.getNewEtmStartDate());
						employeeTeamMap.setEmployeeRole(employeeRole.toString());
						employeeTeamMap.setIsShadow(employeedto.getIsShadowResource());
						employeeTeamMap.setPoId(employeedto.getPoId());
						employeeTeamMap.setRoleId(employeedto.getPoRoleId());
						employeeTeamMap.setEmpTeamDepartmentId(dept != null ? dept.getDeptId() : null);
						employeeTeamMap.setCreatedBy(updatedBy);
						employeeTeamMap.setUpdatedBy(updatedBy);
						employeeTeamMap.setUpdatedOn(LocalDateTime.now());
						EmployeeTeamMap dbEmployeeTeamMap = employeeTeamMapRepository.save(employeeTeamMap);

						Project project = projectRepository.findByProjectId(employeedto.getDefaultProjectId());
						if (project != null) {
							project.setIsDraftProject("true");
							project.setUpdatedBy(updatedBy);
							project.setUpdatedOn(LocalDateTime.now());
							project = projectRepository.save(project);
						}

						teamMembersService.updateEmployeeDefaultProjectIfUpdated(List.of(employee2.getEmpId()),
								List.of(employee2.getEmpId()), project, updatedBy, List.of(dbEmployeeTeamMap));
					}
				}

				Employee employee = employeeRepository.findByEmpId(employeedto.getEmpId());
				
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setEmployeementId(employeedto.getEmployeementId());
				employee.setName(employeedto.getName());
				//added by rahul for reffered in employee
				employee.setReferedType(employeedto.getReferedType());
				employee.setReferedName(employeedto.getReferedName());
				//end
				employee.setDateOfBirth(employeedto.getDateOfBirth() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd")
						: null);
				employee.setDateOfJoining(employeedto.getDateOfJoining() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd")
						: null);
				employee.setOnRollDate(employeedto.getOnRollDate()!=null
						? stringToDateTimeParser.getDate(employeedto.getOnRollDate(),"yyyy-MM-dd")
						: null);
				employee.setManagerId(employeedto.getManagerId());
				employee.setEmail(employeedto.getEmail());
				employee.setSecondaryEmail(employeedto.getSecondaryEmail());
				employee.setGender(employeedto.getGender());
				employee.setBloodGroup(employeedto.getBloodGroup());
				employee.setMaritalStatus(employeedto.getMaritalStatus());
				employee.setFatherName(employeedto.getFatherName());
				employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
				employee.setMotherTongue(employeedto.getMotherTongue());
				employee.setPassportNumber(employeedto.getPassportNumber());
				employee.setAadhar(employeedto.getAadhar());
				employee.setPanNumber(employeedto.getPanNumber());
				employee.setMobileNo(employeedto.getMobileNo());
				employee.setLandline(employeedto.getLandline());
				employee.setAddress(employeedto.getAddress());
				employee.setCity(employeedto.getCity());
				employee.setState(employeedto.getState());
				employee.setCountry(employeedto.getCountry());
				employee.setPincode(employeedto.getPincode());
				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
				employee.setPermanentAddress(employeedto.getPermanentAddress());
				employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
				employee.setRelation(employeedto.getRelation());
				employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
				employee.setNoticePeriod(employeedto.getNoticePeriod());
				employee.setEmploymentstatus(employeedto.getEmploymentstatus());
				
				employee.setBankName(employeedto.getBankName());
				employee.setBankAccountNo(employeedto.getBankAccountNo());
				employee.setBankIFSCCode(employeedto.getBankIFSCCode());
				employee.setPfAccountNumber(employeedto.getPfAccountNumber());
				employee.setPreviousPfAccountNumber(employeedto.getPreviousPfAccountNumber());
				employee.setUan(employeedto.getUan());
				employee.setEsicNumber(employeedto.getEsicNumber());
				employee.setGraduationType(employeedto.getGraduationType());
				employee.setPursuing(employeedto.getPursuing());
				employee.setYearOfPassing(employeedto.getYearOfPassing());
				employee.setPassingGrade(employeedto.getPassingGrade());
				employee.setAboutMe(employeedto.getAboutMe());
				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
				employee.setJobRoleId(employeedto.getJobRoleId());
				employee.setExperience(employeedto.getExperience());
				employee.setRole(employeedto.getRole());
				employee.setWorkLocation(employeedto.getWorkLocation());
				employee.setProbationPeriod(employeedto.getProbationPeriod());
				employee.setIsConsultant(employeedto.getIsConsultant());
				employee.setIsApprenticeship(employeedto.getIsApprenticeship());
				employee.setIsApmosysProduct(employeedto.getIsApmosysProduct());
				
				if ("No".equals(employeedto.getOnbenchDate())) {
				    // Keep the existing value (no need to set it again)
				} else {
				    employee.setOnbenchDate(employeedto.getOnbenchDate());
				}
				
//				System.out.println("Employee 1 : " + employee);
			 if(employeedto.getReportiesFlag().equals("Yes") && employeedto.getUpdateType().equals("automatic")){
					for(Employee emp : listOfReporties) {
						emp.setManagerId(employeedto.getNewManagerId());
						employeeRepository.save(emp);
						System.err.println(" Manager mapping done ");
						
					}
				}
			 
			 if(employeedto.getEmploymentstatus().equals("Confirmed"))
					 {	
				 employee.setEmployeeConfirmationDate(employeedto.getEmployeeConfirmationDate() != null
						? stringToDateTimeParser.getDate(employeedto.getEmployeeConfirmationDate(), "yyyy-MM-dd")
						: null)		; 
				 }
			 
			
			 if(employeedto.getEmploymentstatus().equals("Retain")) {
					
					employee.setDateOfRetain(employeedto.getDateOfRetain() != null
							? stringToDateTimeParser.getDate(employeedto.getDateOfRetain(), "yyyy-MM-dd")
							: null);
					employee.setIsRetain(employeedto.getIsRetain());
					
//					this.cronJobService.isRetain(employee);
					isRetain(employee);
					
					
		     } else {
					employee.setIsRetain(employeedto.getIsRetain());
			 }
			 

				if(employeedto.getEmploymentstatus().equals("Resigned") || employeedto.getEmploymentstatus().equals("InActive") || employeedto.getEmploymentstatus().equals("Retain") )  {
					System.out.println("Right method call for  setDateOfResign   ");
					employee.setDateOfResign(employeedto.getDateOfResign() != null
							? stringToDateTimeParser.getDate(employeedto.getDateOfResign(), "yyyy-MM-dd")
							: null);
					
//					if(employeedto.getUpdateType() != null) {
//						if(employeedto.getUpdateType().equals("automatic") && (!employeedto.getEmploymentstatus().equals("Resigned")) ) {
//							for(Employee emp : listOfReporties) {
//								emp.setManagerId(employeedto.getNewManagerId());
//								listOfEmp.add(emp);
//								System.err.println(" Manager mapping done ");
//								
//							}
//							
////							get compOff leaves 
//							
////							Optional<List<CompOffLeave>> findListOfCompOff = compOffLeaveRepository.findCompOffByEmpId(employeedto.getEmpId());
////							
////							if(findListOfCompOff.isPresent()) {
////								List<CompOffLeave> findCompOffs = findListOfCompOff.get();
////								
////								for (CompOffLeave compOff : findCompOffs) {
////									System.out.println(" compOff Id     ::   \n"+compOff.getCompOffLeaveId());
////									compOff.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////									
////									compOffLeaveRepository.save(compOff);	
////								}
////							}
////							
//////							get leaves 
////							
////							Optional<List<EmployeeLeave>> findListOfLeaves = employeeLeaveRepository.findLeaveByManagerId(employeedto.getEmpId());
////							System.err.println(" findListOfLeaves    "+findListOfLeaves);
////							if(findListOfLeaves.isPresent()) {
////								List<EmployeeLeave> findLeaves = findListOfLeaves.get();
////								
////							for (EmployeeLeave empLeaves : findLeaves) {
////								System.out.println( " leavesId   ::   \n"+empLeaves.getLeaveId());
////								empLeaves.setManagerId(Math.toIntExact(employeedto.getNewManagerId()));
////								employeeLeaveRepository.save(empLeaves);
////								
////							}
////						}						
//							
//							
//							employeeRepository.saveAll(listOfEmp);
//							}	
//					}
					
					if(employeedto.getEmploymentstatus().equals("InActive")) {
						
						
						List<Employee> reporites= employeeRepository.findByManagerId(employeedto.getEmpId());
						List<Object[]> reportees = employeeRepository.findReporteesOfManager(employeedto.getEmpId());
//						   cronJobService.notificationformanagerstatusInActive(employeedto.getEmpId());
							System.out.println("hbcsdh"+employeedto.getJobRoleId());
							JobRole job= jobRoleRepository.findByjobRoleId(employeedto.getJobRoleId());
							System.out.println("hbcsdh"+job.getEmployeeRole());
						 
						 if(job.getEmployeeRole().equals("Manager") || job.getEmployeeRole().equals("SuperAdmin") ) {
							 
							 
							 if (!reportees.isEmpty()) {
							 StringBuilder html = new StringBuilder();
							    html.append("<html>\n" +
							            "  <head>\n" +
							            "    <style>\n" +
							            "      table, th, td {\n" +
							            "        border: 1px solid black;\n" +
							            "        padding: 8px;\n" +
							            "        text-align: left;\n" +
							            "      }\n" +
							            "      table {\n" +
							            "        border-collapse: collapse;\n" +
							            "        width: 100%;\n" +
							            "      }\n" +
							            "      th {\n" +
							            "        background-color: #f2f2f2;\n" +
							            "      }\n" +
							            "    </style>\n" +
							            "  </head>\n" +
							            "  <body>\n" +
							            "    <p>Dear team,</p>\n" +
							            "    <p>Please find below the details of the reportees of the inactive manager:</p>\n" +
							            "    <table>\n" +
							            "      <tr>\n" +
							            "        <th>Emp ID</th>\n" +
							            "        <th>Name</th>\n" +
							            "        <th>Department Name</th>\n" +
							            "      </tr>\n");
							    
							    for (Object[] reportee : reportees) {
							       
							        BigInteger employmentIdBigInt = (BigInteger) reportee[0];
							        String employmentId = employmentIdBigInt.toString();

							       
							        String isApprenticeship = (String) reportee[3];
							        String isConsultant = (String) reportee[4];
							        String isApmosysProduct = (String) reportee[5];

							        
							        if ("true".equalsIgnoreCase(isConsultant)) {
							            employmentId = "CS-" + employmentId;
							        } else if ("true".equalsIgnoreCase(isApprenticeship)) {
							            employmentId = "A-" + employmentId;
							        } else if ("true".equalsIgnoreCase(isApmosysProduct)) {
							            employmentId = "AP-" + employmentId;
							        } else {
							            employmentId = "A-" + employmentId;
							        }

							        // Append data to the HTML table
							        html.append("      <tr>\n");
							        html.append("        <td>").append(employmentId).append("</td>\n");
							        html.append("        <td>").append(reportee[1]).append("</td>\n");
							        html.append("        <td>").append(reportee[2]).append("</td>\n");
							        html.append("      </tr>\n");
							    }

							    html.append("    </table>\n" +
							                "    <p>Kindly take the necessary action to update the reportees under another active manager.</p>\n" +
							                "  </body>\n" +
							                "</html>");
							    
							    String subject = "Reminder for Manager Update of Reportees of Inactive Manager: " + employeedto.getName();
							    String mailBody = html.toString();

							    boolean flag = mailService.sendMailWithCC(rmgMail,hrMailAddress, subject, mailBody);
							 }	
						 }
						 
						// create logic for remove resource from team and projects
						List<EmployeeTeamMap> findAllActiveTeams = employeeTeamMapRepository.findByEmpIdAndActiveStatus(employeedto.getEmpId());
						if(findAllActiveTeams != null) {
							
							findAllActiveTeams.forEach(obj ->{
								obj.setActive(0l);	
								obj.setEndDate(LocalDate.parse(employeedto.getDateOfRelieving()).atStartOfDay());	
								employeeTeamMapRepository.save(obj);
								});
						}
						
						// of project manager 
						List<ProjectManagerMapping> activeProjectManagerMappings = projectManagerMappingRepository
							    .findByProjectManagerIdAndActive(employeedto.getEmpId(), 1);
						
						List<Project> activeProjectsOfThatManager = projectRepository.findProjectsOfProjectManager(employeedto.getEmpId());

						if(activeProjectsOfThatManager != null) {
							
							StringBuilder html = new StringBuilder();
							html.append("<html>\n" +
						            "  <head>\n" +
						            "    <style>\n" +
						            "      table, th, td {\n" +
						            "        border: 1px solid black;\n" +
						            "        padding: 8px;\n" +
						            "        text-align: left;\n" +
						            "      }\n" +
						            "      table {\n" +
						            "        border-collapse: collapse;\n" +
						            "        width: 100%;\n" +
						            "      }\n" +
						            "      th {\n" +
						            "        background-color: #f2f2f2;\n" +
						            "      }\n" +
						            "    </style>\n" +
						            "  </head>\n" +
						            "  <body>\n" +
						            "    <p>Dear,</p>\n" +
						            "    <p>Please find below the details of the Projects of the inactive Project Manager:</p>\n" +
						            "    <table>\n" +
						            "      <tr>\n" +
						            "        <th>Client Name</th>\n" +
						            "        <th>Project Name</th>\n" +
						            "        <th>PoNo</th>\n" +
						            "      </tr>\n");	
							
							
							activeProjectsOfThatManager.forEach(obj ->{

//							obj.getClientName();
//							obj.getProjectName();
//							obj.getPoNo();
//							
//							
								html.append("      <tr>\n");
						        html.append("        <td>").append(obj.getClientName()).append("</td>\n");
						        html.append("        <td>").append(obj.getProjectName()).append("</td>\n");
						        html.append("        <td>").append(obj.getPoNo()).append("</td>\n");
						        html.append("      </tr>\n");
							});
						
							html.append("    </table>\n" +
					                "    <p>Kindly take the necessary action to update the Projects under another active project manager.</p>\n" +
					                "  </body>\n" +
					                "</html>");
					    
					    String subject = "Reminder for Project Manager Update of projects of Inactive Project Manager: " + employeedto.getName();
					    String mailBody = html.toString();

					    boolean flag = mailService.sendMailWithCC(rmgMail,hrMailAddress, subject, mailBody);
					    
					    
						}
						
						if (activeProjectManagerMappings != null && !activeProjectManagerMappings.isEmpty()) {
							activeProjectManagerMappings.forEach(mapping -> {
						        mapping.setActive(0);
						        mapping.setUpdatedBy(Long.valueOf(employeedto.getUpdatedBy().toString()));
						        mapping.setUpdatedOn(LocalDateTime.now());
						        projectManagerMappingRepository.save(mapping);
						    });
						    System.out.println("deactivated " + activeProjectManagerMappings.size() + " project manger mappings for employee: " + employeedto.getEmpId());
						}
							
							
						
						// of project overhead
							List<ProjectOverheadMapping> activeProjectOverheadMappings = projectOverheadMappingRepository
								    .findByProjectOverheadIdAndActive(employeedto.getEmpId(), 1);
							
							List<Project> activeProjectsOfProjectOverhead = projectRepository.findProjectOfProjectOverhead(employeedto.getEmpId());
							
							if(activeProjectsOfProjectOverhead != null){
								StringBuilder html = new StringBuilder();
								html.append("<html>\n" +
							            "  <head>\n" +
							            "    <style>\n" +
							            "      table, th, td {\n" +
							            "        border: 1px solid black;\n" +
							            "        padding: 8px;\n" +
							            "        text-align: left;\n" +
							            "      }\n" +
							            "      table {\n" +
							            "        border-collapse: collapse;\n" +
							            "        width: 100%;\n" +
							            "      }\n" +
							            "      th {\n" +
							            "        background-color: #f2f2f2;\n" +
							            "      }\n" +
							            "    </style>\n" +
							            "  </head>\n" +
							            "  <body>\n" +
							            "    <p>Dear,</p>\n" +
							            "    <p>Please find below the details of the Projects of the inactive Project Over Head:</p>\n" +
							            "    <table>\n" +
							            "      <tr>\n" +
							            "        <th>Client Name</th>\n" +
							            "        <th>Project Name</th>\n" +
							            "        <th>PoNo</th>\n" +
							            "      </tr>\n");	
								
								activeProjectsOfProjectOverhead.forEach(obj ->{
									
								
								
//								for(Project obj : activeProjectsOfProjectOverhead.get()) {
									 html.append("      <tr>\n");
								        html.append("        <td>").append(obj.getClientName()).append("</td>\n");
								        html.append("        <td>").append(obj.getProjectName()).append("</td>\n");
								        html.append("        <td>").append(obj.getPoNo()).append("</td>\n");
								        html.append("      </tr>\n");
								});
								html.append("    </table>\n" +
						                "    <p>Kindly take the necessary action to update the projects under another active project ober head.</p>\n" +
						                "  </body>\n" +
						                "</html>");
						    
						    String subject = "Reminder For Project Over Head Update of projects of Inactive Project Over Head: " + employeedto.getName();
						    String mailBody = html.toString();

						    boolean flag = mailService.sendMailWithCC(rmgMail,hrMailAddress, subject, mailBody);
								
								
							}

							if (activeProjectOverheadMappings != null && !activeProjectOverheadMappings.isEmpty()) {
								activeProjectOverheadMappings.forEach(mapping -> {
							        mapping.setActive(0);
							        mapping.setUpdatedBy(Long.valueOf(employeedto.getUpdatedBy().toString()));
							        mapping.setUpdatedOn(LocalDateTime.now());
							        projectOverheadMappingRepository.save(mapping);
							    });
							    System.out.println("deactivated " + activeProjectOverheadMappings.size() + " project overhead mappings for employee: " + employeedto.getEmpId());
							}
								
								
								
								
								// for teamLead 
								List<Team> activeTeamLead = teamRepository.findByTeamLeadIdAndIsActive(employeedto.getEmpId(), "Y");
								
								List<Project> activeProjectOfTeamlead = projectRepository.findProjectOfTeamLead(employeedto.getEmpId());
								
								if(activeProjectOfTeamlead != null) {
									StringBuilder html = new StringBuilder();
									html.append("<html>\n" +
								            "  <head>\n" +
								            "    <style>\n" +
								            "      table, th, td {\n" +
								            "        border: 1px solid black;\n" +
								            "        padding: 8px;\n" +
								            "        text-align: left;\n" +
								            "      }\n" +
								            "      table {\n" +
								            "        border-collapse: collapse;\n" +
								            "        width: 100%;\n" +
								            "      }\n" +
								            "      th {\n" +
								            "        background-color: #f2f2f2;\n" +
								            "      }\n" +
								            "    </style>\n" +
								            "  </head>\n" +
								            "  <body>\n" +
								            "    <p>Dear,</p>\n" +
								            "    <p>Please find below the details of the Projects of the inactive Team Lead  </p>\n" +
								            "    <table>\n" +
								            "      <tr>\n" +
								            "        <th>Client Name</th>\n" +
								            "        <th>Project Name</th>\n" +
								            "        <th>PoNo</th>\n" +
								            "      </tr>\n");
									
									activeProjectOfTeamlead.forEach(obj ->{
										
									
//									for(Project obj : activeProjectOfTeamlead.get()) {
										 html.append("      <tr>\n");
									        html.append("        <td>").append(obj.getClientName()).append("</td>\n");
									        html.append("        <td>").append(obj.getProjectName()).append("</td>\n");
									        html.append("        <td>").append(obj.getPoNo()).append("</td>\n");
									        html.append("      </tr>\n");
									});
									html.append("    </table>\n" +
							                "    <p>Kindly take the necessary action to update the projects under another active Team Lead.</p>\n" +
							                "  </body>\n" +
							                "</html>");
							    
							    String subject = "Reminder For Team Lead Update of projects of Inactive Team Lead: " + employeedto.getName();
							    String mailBody = html.toString();

							    boolean flag = mailService.sendMailWithCC(rmgMail,hrMailAddress, subject, mailBody);
								}

								if (activeTeamLead != null) {
									activeTeamLead.forEach(obj ->{
										
								
//								    for (Team obj : activeTeamLead.get()) {
								        Team teamDetails = teamRepository.findByTeamId(obj.getTeamId());
								        teamDetails.setTeamLeadId(null);
								        teamDetails.setTeamLeadName(null);
								        teamDetails.setUpdatedBy(employeedto.getUpdatedBy());
								        teamDetails.setUpdatedOn(LocalDateTime.now());
								        
								        Team dbResponse = teamRepository.save(teamDetails);
									});
								}
								
							// for spoc 
								List<Team> activeSpoc = teamRepository.findBySpocIdAndIsActive(employeedto.getEmpId() , "Y");
								
								List<Project> activeProjectOfSpoc = projectRepository.findProjectOfSpoc(employeedto.getEmpId());
								
								if(activeProjectOfSpoc != null ) {
									StringBuilder html = new StringBuilder();
									html.append("<html>\n" +
								            "  <head>\n" +
								            "    <style>\n" +
								            "      table, th, td {\n" +
								            "        border: 1px solid black;\n" +
								            "        padding: 8px;\n" +
								            "        text-align: left;\n" +
								            "      }\n" +
								            "      table {\n" +
								            "        border-collapse: collapse;\n" +
								            "        width: 100%;\n" +
								            "      }\n" +
								            "      th {\n" +
								            "        background-color: #f2f2f2;\n" +
								            "      }\n" +
								            "    </style>\n" +
								            "  </head>\n" +
								            "  <body>\n" +
								            "    <p>Dear,</p>\n" +
								            "    <p>Please find below the details of the Projects of the inactive SPOC  </p>\n" +
								            "    <table>\n" +
								            "      <tr>\n" +
								            "        <th>Client Name</th>\n" +
								            "        <th>Project Name</th>\n" +
								            "        <th>PoNo</th>\n" +
								            "      </tr>\n");
									
									activeProjectOfSpoc.forEach(obj ->{
										
									
//									for(Project obj : activeProjectOfSpoc.get()){
										html.append("      <tr>\n");
								        html.append("        <td>").append(obj.getClientName()).append("</td>\n");
								        html.append("        <td>").append(obj.getProjectName()).append("</td>\n");
								        html.append("        <td>").append(obj.getPoNo()).append("</td>\n");
								        html.append("      </tr>\n");
									});
									html.append("    </table>\n" +
							                "    <p>Kindly take the necessary action to update the projects under another active SPOC .</p>\n" +
							                "  </body>\n" +
							                "</html>");
							    
							    String subject = "Reminder For SPOC Update of projects of Inactive SPOC: " + employeedto.getName();
							    String mailBody = html.toString();

							    boolean flag = mailService.sendMailWithCC(rmgMail,hrMailAddress, subject, mailBody);
								}
								
								if(activeSpoc != null) {
									activeSpoc.forEach(obj ->{
										
									
//									for( Team obj: activeSpoc.get()) {
										Team spocDetails = teamRepository.findByTeamId(obj.getTeamId());
										spocDetails.setSpocId(null);
										spocDetails.setUpdatedBy(employeedto.getUpdatedBy());
										spocDetails.setUpdatedOn(LocalDateTime.now());
										
										Team dbResponse = teamRepository.save(spocDetails);
										
									});
								}

						// department HOD 
						Optional<List<Department>> findDept = Optional.ofNullable(departmentRepository.findByHodId(employeedto.getEmpId()));
						if(findDept.isPresent() && !findDept.isEmpty()) {
							System.err.println(" department update call ");
							List<Department> listOfDept = findDept.get();
							int count =0;
							for (Department department : listOfDept) {
								department.setHodId(employeedto.getNewManagerId());
								departmentRepository.save(department);
								count = count+1;
							}
							System.err.println(" count total "+count);
						}
					}
					
				}else if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed") || employeedto.getEmploymentstatus().equals("Retain") ) {
					employee.setDateOfResign(null);
				}
					if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed") || employee.getEmploymentstatus().equals("Resigned") || employeedto.getEmploymentstatus().equals("Retain") ) {
				
					employee.setEmploymentReleaseStatus(null);
				}else {
					employee.setEmploymentReleaseStatus(employeedto.getEmploymentReleaseStatus());
				}
				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
				employee.setBillable(employeedto.getBillable());
				employee.setBillableType(employeedto.getBillableType());
				employee.setChild1(employeedto.getChild1());
				employee.setChild2(employeedto.getChild2());
				employee.setChild3(employeedto.getChild3());
				employee.setMothersName(employeedto.getMothersName());
				employee.setSpouse(employeedto.getSpouse());
				employee.setTotalExperience(employeedto.getTotalExperience());
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setDesignationId(employeedto.getDesignationId());
				employee.setReportingManagerId(employeedto.getReportingManagerId());
				if(employeedto.getReportingManagerId() == null) {
					employee.setApprovalsTo(null);
				}else {
					employee.setApprovalsTo(employeedto.getApprovalsTo());
				}
				
				// Certification
				// Case 1 : Updating Existing certification
				if (employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
					employeedto.getCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() != null)
							.forEach((certificate) -> {

								EmployeeCertificate employeeCertificate = employeeCertificateRepository
										.findById(certificate.getEmployeeCertificateId()).get();

								employeeCertificate.setCertificationName(certificate.getCertificationName());
								employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
								employeeCertificate.setDateOfCompletion(stringToDateTimeParser
										.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
								employeeCertificate.setDuration(certificate.getDuration());
								employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
								employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());

								employeeCertificateRepository.save(employeeCertificate);
							});
				}

				if (employeedto.getUpdatedCertifications() != null
						&& !employeedto.getUpdatedCertifications().isEmpty()) {
					// Case 2 : Adding New certification
					newCertificationlist = employeedto.getUpdatedCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() == null)
							.collect(Collectors.toList());
					if (!newCertificationlist.isEmpty()) {
						newCertificationlist.forEach((certification) -> {
							certification.setEmpId(employeedto.getEmpId());

						});
						addCertifications(newCertificationlist, employeedto.getIsDraft());
					}

					// Case 3 : Deleting Removed certification
					employeedto.getUpdatedCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() != null)
							.forEach((certification) -> {
								employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
							});
				}

				// Previous Employer
				if (employeedto.getExperience().equals("Fresher")) {
					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
							.findByEmpId(employeedto.getEmpId());

					if (!previousEmploymentList.isEmpty()) {
						previousEmploymentList.stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((prevEmployer) -> {
									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
								});
					}
				} else {
					if (employeedto.getPreviousEmploymentList() != null
							&& !employeedto.getPreviousEmploymentList().isEmpty()) {
						employeedto.getPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((previousEmployeeDTO) -> {

									PreviousEmployment previousEmployment = previousEmploymentRepository
											.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();

									previousEmployment.setDateOfJoining(stringToDateTimeParser
											.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd"));
									previousEmployment.setDateOfRelieving(stringToDateTimeParser
											.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd"));
									previousEmployment.setDesignation(previousEmployeeDTO.getDesignation());
									previousEmployment.setHrContactNumber(previousEmployeeDTO.getHrContactNumber());
									previousEmployment.setHrName(previousEmployeeDTO.getHrName());
									previousEmployment.setManagerName(previousEmployeeDTO.getManagerName());
									previousEmployment
											.setManagerContactNumber(previousEmployeeDTO.getManagerContactNumber());
									previousEmployment.setEmployerName(previousEmployeeDTO.getEmployerName());
									previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());
//									System.out.println("previousEmployment : " + previousEmployment);
									previousEmploymentRepository.save(previousEmployment);
								});
					}

					if (employeedto.getUpdatedPreviousEmploymentList() != null
							&& !employeedto.getUpdatedPreviousEmploymentList().isEmpty()) {
						newPreviousEmploymentList = employeedto.getUpdatedPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null)
								.collect(Collectors.toList());
						if (!newPreviousEmploymentList.isEmpty()) {
							newPreviousEmploymentList.forEach((previousEmployer) -> {
								previousEmployer.setEmpId(employeedto.getEmpId());

							});
							addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
						}

						employeedto.getUpdatedPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((prevEmployer) -> {
									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
								});
					}
				}
				
				//Employee Specialization Mapping
				
				if(employeedto.getSpecializationList() != null && employeedto.getSpecializationList().length != 0) {
					List<EmployeeSpecializationMap> mappingObj = employeeSpecializationMapRepository.findByEmpId(employee.getEmpId());
					if(!mappingObj.isEmpty()) {
						mappingObj.forEach((object) -> {
							boolean contains = Arrays.stream(employeedto.getSpecializationList()).anyMatch(i -> i.equals(object.getSpecializationId()));
							
							//Delete Specialization
							if(!contains) {
								employeeSpecializationMapRepository.deleteById(object.getEmpSpecializationMapId());
							}
							
						});
					}
					for(Long specializationId: employeedto.getSpecializationList()) {
						EmployeeSpecializationMap empMapObj = employeeSpecializationMapRepository.findByEmpIdAndSpecializationId(employee.getEmpId(),specializationId);
						
						//Add new Specialization
						if(empMapObj == null) {
								EmployeeSpecializationMap empSpecObj = new EmployeeSpecializationMap();
								
								empSpecObj.setEmpId(employee.getEmpId());
								empSpecObj.setSpecializationId(specializationId);
								
								EmployeeSpecializationMap dbResponse = employeeSpecializationMapRepository.save(empSpecObj);
						}
					}
				}
				
				
				Employee dbResponse = employeeRepository.save(employee);
				Employee employeeObj;
				if (dbResponse != null) {
					
					//Update Draft
//					 if(employeedto.getOldEmployeeType().equalsIgnoreCase("true")) {
//						 employeeObj=employeeRepository.findByEmployeementIdForApmosysProduct(employeedto.getOldEmployeementId());					 
//						 }else {
//							employeeObj = employeeRepository.findByEmployeementIdForOthers(employeedto.getOldEmployeementId());                    
//							}
					 if(dbResponse !=null) {
					List<DraftEmployee> draftEmployees = draftEmployeeRepository.findByEmployeementIdForUpdate(employeedto.getOldEmployeementId(),employeedto.getOldEmployeeType());			
							System.out.println("draftEmployee : "+draftEmployees);
					if (draftEmployees != null && !draftEmployees.isEmpty()) {

					    // Update each draft employee using stream.map()
					    List<DraftEmployee> updatedDrafts = draftEmployees.stream().map(draft -> {

					        draft.setName(dbResponse.getName());
					        draft.setEmployeementId(dbResponse.getEmployeementId());

					        if ("true".equalsIgnoreCase(dbResponse.getIsApmosysProduct())) {
					            draft.setIsApmosysProduct("true");
					            draft.setIsApprenticeship("false");
					            draft.setIsConsultant("false");
					        } else if ("true".equalsIgnoreCase(dbResponse.getIsApprenticeship())) {
					            draft.setIsApprenticeship("true");
					            draft.setIsApmosysProduct("false");
					            draft.setIsConsultant("false");
					        } else if ("true".equalsIgnoreCase(dbResponse.getIsConsultant())) {
					            draft.setIsConsultant("true");
					            draft.setIsApmosysProduct("false");
					            draft.setIsApprenticeship("false");
					        } else {
					            draft.setIsApmosysProduct("false");
					            draft.setIsApprenticeship("false");
					            draft.setIsConsultant("false");
					        }

					        draft.setDateOfBirth(dbResponse.getDateOfBirth());
					        draft.setDateOfJoining(dbResponse.getDateOfJoining());
					        draft.setManagerId(dbResponse.getManagerId());
					        draft.setEmail(dbResponse.getEmail());
					        draft.setMobileNo(dbResponse.getMobileNo());
					        draft.setNoticePeriod(dbResponse.getNoticePeriod());
					        draft.setEmploymentstatus(dbResponse.getEmploymentstatus());
					        draft.setJobRoleId(dbResponse.getJobRoleId());
					        draft.setExperience(dbResponse.getExperience());
					        draft.setRole(dbResponse.getRole());
					        draft.setWorkLocation(dbResponse.getWorkLocation());
					        draft.setUpdatedBy(dbResponse.getUpdatedBy() != null ? Integer.parseInt(dbResponse.getUpdatedBy().toString()) : null);
					        draft.setBillable(dbResponse.getBillable());
					        draft.setTotalExperience(dbResponse.getTotalExperience());
					        draft.setUpdatedOn(dbResponse.getUpdatedOn());
					        draft.setDesignationId(dbResponse.getDesignationId());
					        draft.setDateOfResign(dbResponse.getDateOfResign());
					        draft.setDateOfRelieving(dbResponse.getDateOfRelieving());
					        draft.setReportingManagerId(dbResponse.getReportingManagerId());

					        if (dbResponse.getReportingManagerId() == null) {
					            draft.setApprovalsTo(null);
					        } else {
					            draft.setApprovalsTo(dbResponse.getApprovalsTo());
					        }

					        return draft;

					    }).collect(Collectors.toList());

					    // Save all updated drafts at once
					    List<DraftEmployee> savedDrafts = draftEmployeeRepository.saveAll(updatedDrafts);

					    // Send notifications
					    savedDrafts.forEach(mailNotify::sendDraftUpdateNotification);
					}
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Profile Updated.");
					
					apiLogInfo.setApiResponse("Employee Profile Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Failed.");
					
					apiLogInfo.setApiResponse("Employee Profile Updation Failed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not Found");
				
				apiLogInfo.setApiResponse("Employee Profile Not Found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	private String getCurrentEmployeeType(EmployeeDTO employeeDTO) {
	    if (Boolean.TRUE.equals(employeeDTO.getIsConsultant())) {
	        return "Consultant";
	    } else if (Boolean.TRUE.equals(employeeDTO.getIsApmosysProduct())) {
	        return "Apmosys Product";
	    } else {
	        return "Regular";
	    }
	}
	
	
	

	void isRetain(Employee employeeObj) {
		
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		      
		     if(!leaveType.isEmpty()) {
		    	 
		    	 for(LeaveTypeMaster ltm :leaveType) {
		    		 
//		        		  Employee employeeObj = new Employee();
		        		 
		        		  System.out.println(employeeObj.getEmploymentstatus() +"  "+ ltm.getLeaveTypeMasterId());
		        		  
		        		  Optional<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.
		        				  findByEmployentStatusAndLeaveTypeMasterId(employeeObj.getEmploymentstatus(),ltm.getLeaveTypeMasterId());
		        		  System.err.println("Red"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        		  if(!leavePolicy.isEmpty() ) {
		        			 
		        			  LeavePolicyMaster leavePolicyObj = leavePolicy.get();

		        			  if(leavePolicyObj.getIncrement().equals("Yes")){
		        				  EmployeeLeavesMap employeeLeaveMap = employeeLeavesMapRepository.
		        						  findByEmpIdAndLeaveTypeMasterId(employeeObj.getEmpId(),ltm.getLeaveTypeMasterId());
		        				  
		        				  System.err.println("Red"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				  float retainValue = 0.0F;
		        				        		  
		        				        		  LocalDate resignedDate = employeeObj.getDateOfResign();
		        				        		  LocalDate retainedDate = employeeObj.getDateOfRetain();
		        				        	      String  isRetain = employeeObj.getIsRetain();
		        				        	      System.out.println("Yesssss     "+employeeObj);
		        				        	      
		        				        	      System.out.println("1    "+resignedDate); 
		        				        	      System.out.println("2    "+retainedDate);
		        				        	      System.out.println("3   "+isRetain);
		        				        	      
		        				        	      System.out.println("1    "+employeeObj.getDateOfResign()); 
		        				        	      System.out.println("2    "+employeeObj.getDateOfRetain());
		        				        	      System.out.println("3   "+employeeObj.getIsRetain());
		        				        	      
		        				        	      if ("Yes".equals(isRetain)) { 
		        				        	    	  System.out.println("bsjhsdh"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				        	    	  
		        				        	    	    if (resignedDate != null && retainedDate != null) {
		        				        	    	    	System.err.println("jhbshj"+employeeObj.getEmpId());
		        				        	    	    	System.out.println("bsjhsdh"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				        	    	        LocalDate startDate = resignedDate;
		        				        	    	        LocalDate endDate = retainedDate;
		        				        	    	        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

		        				        	    	        long fullMonths = totalDays / 30;
		        				        	    	        
		        				        	    	        System.out.print("fullMonth  "+fullMonths);

		        				        	    	        for (long i = 0; i < fullMonths; i++) {
		        				        	    	            retainValue = retainValue + leavePolicyObj.getIncrementValue();
		        				        	    	        }

		        				        	    	        if (retainValue > 0.0F) {
//		        				        	    	            executeQueryForRetain(employeeObj);
		        				        	    	        	System.err.println("jhbshj"+employeeObj.getEmpId());
		        				        	    	        	employeeRepository.updateIsRetain(employeeObj.getEmpId());
		        				        	    	        }
		        				        	    	    } else {
		        				        	    	        // Handle the case where either date is null
		        				        	    	        System.out.println("Warning: resignedDate or retainedDate is null. Skipping retain calculation.");
		        				        	    	        // Proceed with further execution
		        				        	    	    }
		        				        	    	}

		        			
		        				        		  float newBalance = employeeLeaveMap.getBalance() + retainValue;
			        				        	  System.out.println(newBalance);
			        				        	  EmployeeLeavesMap dbResponse=null;
			        				        	  if(retainValue != 0) {
			        				        		  employeeLeaveMap.setBalance(newBalance);
				        				        	   dbResponse = employeeLeavesMapRepository.save(employeeLeaveMap);
			        				        	  }
			        				        	  
			        				        	  
			        				        	  if(dbResponse != null && retainValue != 0) {
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(newBalance);
														log.setEmpId(employeeObj.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														log.setMessage(LeaveLogMessage.autoAddLeave.replace("0.0",
																leavePolicyObj.getIncrementValue().toString()));
														log.setUpdateBalanceBy("+" + retainValue);

														leaveBalanceLogRepository.save(log);
			        				        	  }
		        				        		  
//		        				        	  }
//		        				          }
		        			  }
		        		  }else {
		        			  System.out.println("Leave Policy not found");
		        		  }
		        		  
		        		  
		        	//  }
		        	  
		        	  
		        	  
		            }
		     }
		   }catch(Exception e) {
			e.printStackTrace();
		   }

  

		
	}
	
	
	
	
	
	
	

	public ServiceResponse updateEmployeeByEmpIdByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateEmployeeByEmpIdByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
	
		logBuilder.append(employeedto.getEmployeementId() + " : employeementy id");
		
		try {

			System.out.println(employeedto.getEmployeementId() + " : employeementy id");
			Optional<Employee> employeeObject = Optional
					.ofNullable(employeeRepository.findByEmployeementId(employeedto.getEmployeementId()));
			if (employeeObject.isPresent()) {

//				Employee m = employeeRepository.findByEmployeementId(employeedto.getManagerId());
				System.out.println(employeedto.getManagerId() + " : manager id");

//				Long primaryEmpId = m.getEmpId();
//				System.out.println(primaryEmpId);

				Employee employee = employeeObject.get();

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//				employee.setManagerId(primaryEmpId);
//				employee.setExperience(employeedto.getExperience());
//				employee.setTotalExperience(employeedto.getTotalExperience());
//				employee.setFatherName(employeedto.getFatherName());
//				employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
//				
//				if(employeedto.getEmergencyContactMobile() != null) {
//					employee.setEmergencyContactPerson(employeedto.getFatherName());
//					employee.setRelation("Father");
//				}
//				
//				employee.setSpouse(employeedto.getSpouse());
//				employee.setSecondaryEmail(employeedto.getSecondaryEmail());
//				employee.setPassportNumber(employeedto.getPassportNumber());
//				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
//				employee.setReference(employeedto.getReference());
//				employee.setBackgroundVerificationStatus(employeedto.getBackgroundVerificationStatus());
				
//				employee.setBankName(employeedto.getBankName());
//				employee.setBankIFSCCode(employeedto.getBankIFSCCode());
//				employee.setBankAccountNo(employeedto.getBankAccountNo());
				
//				employee.setPfAccountNumber(employeedto.getPfAccountNumber());
//				employee.setUan(employeedto.getUan());
//				employee.setEsicNumber(employeedto.getEsicNumber());
				
				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Profile Updated.");
					apiLogInfo.setApiResponse("Employee Profile Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Failed.");
					apiLogInfo.setApiResponse("Employee Profile Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not found" + employeedto.getEmployeementId());
				apiLogInfo.setApiResponse("Employee Profile Not found" + employeedto.getEmployeementId());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			e.printStackTrace();
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	
	public ServiceResponse getAllEmployees() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("get_all_employee");
	    apiLogInfo.setApiUrl("/api/getAllEmployees");
	    apiLogInfo.setLogLevel("INFO");

	    final String cacheKey = "allEmployees";

	    long startTime = System.currentTimeMillis();

	    try {
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedList = employeeCache.get(cacheKey);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(cachedList);
	            apiLogInfo.setApiResponse("Data fetched from cache. Size: " + cachedList.size());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        List<EmployeeProjection> result = employeeRepository.getAllEmployees();
	        if (result == null || result.isEmpty()) {
	            throw new DataNotFoundException("No employees found in the repository.");
	        }

	        ExecutorService executor = Executors.newFixedThreadPool(
	            Math.max(4, Runtime.getRuntime().availableProcessors())
	        );

	        List<CompletableFuture<EmployeeDTO>> futures = result.stream()
	            .map(empProj -> CompletableFuture.supplyAsync(() -> {
	                try {
	                    EmployeeDTO empDTO = new EmployeeDTO(empProj);
	                    empDTO.setFailedAttempt(failedAttempt);
	                    if (empProj.getEmployeementId() != null) {
	                        empDTO.setEmploymentIdAcToET(
	                            "true".equalsIgnoreCase(empProj.getIsApmosysProduct())
	                                ? "AP-" + empProj.getEmployeementId()
	                                : "true".equalsIgnoreCase(empProj.getIsConsultant())
	                                    ? "CS-" + empProj.getEmployeementId()
	                                    : "A-" + empProj.getEmployeementId()
	                        );
	                    }

	                    if (empProj.getProjectIds() != null && empProj.getProjectName() != null) {
	                        String projectIdStr = empProj.getProjectIds().trim();
	                        String projectNameStr = empProj.getProjectName().trim();

	                        if (!projectIdStr.isEmpty() && !projectNameStr.isEmpty()) {
	                            String[] projectIds = projectIdStr.split(",");
	                            String[] projectNames = projectNameStr.split(",");

	                            List<ProjectDTO> projectList = new ArrayList<>();
	                            int length = Math.min(projectIds.length, projectNames.length);

	                            for (int i = 0; i < length; i++) {
	                                try {
	                                    ProjectDTO projectDTO = new ProjectDTO();
	                                    projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
	                                    projectDTO.setProjectName(projectNames[i].trim());
	                                    projectList.add(projectDTO);
	                                } catch (NumberFormatException e) {
	                                    System.err.println("Invalid projectId: " + projectIds[i]);
	                                }
	                            }
	                            empDTO.setProjectList(projectList);
	                        }
	                    }

	                    return empDTO;
	                } catch (Exception e) {
	                	logger.error("Error while processing employee: " + e.getMessage());
	                    return null; 
	                }
	            }, executor))
	            .collect(Collectors.toList());

	        List<EmployeeDTO> dtoList = futures.stream()
	            .map(CompletableFuture::join)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	        
	        List<Long> empIds = dtoList.stream()
	                .map(EmployeeDTO::getEmpId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());
	        
	        List<EmployeeSkillProficiencyDTO> allSkills =employeeSkillProficiencyMappingRepository.findEmployeeSkillsByEmpId(empIds);
	        
	        List<CertificateDTO>  allCertificate=employeeCertificatesRepository.getEmployeeCertficatesByEmpIds(empIds);
	        
	        
	        Map<Long, List<EmployeeSkillProficiencyDTO>> skillsByEmpId = allSkills.stream()
	                .collect(Collectors.groupingBy(EmployeeSkillProficiencyDTO::getEmpId));
	        
	        Map<Long, List<CertificateDTO>> allCertificateByEmpId = allCertificate.stream()
	                .collect(Collectors.groupingBy(CertificateDTO::getEmpId));
	        
	        
	        
	        dtoList.parallelStream().forEach(emp -> {
	            emp.setEmployeeSkills(skillsByEmpId.getOrDefault(emp.getEmpId(), Collections.emptyList()));
	            emp.setEmployeeCertificates(allCertificateByEmpId.getOrDefault(emp.getEmpId(), Collections.emptyList()));
	        });



	        executor.shutdown();

	        employeeCache.put(cacheKey, dtoList);

	        long duration = System.currentTimeMillis() - startTime;
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);

	        apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " employees in " + duration + " ms");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        logService.logMyInfo(httpRequest, apiLogInfo);

	    } catch (DataNotFoundException e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(e.getMessage());
	        apiLogInfo.setApiResponse("No employees found.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("WARN");
	        logService.logMyInfo(httpRequest, apiLogInfo);

	    } catch (RejectedExecutionException e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Thread pool execution rejected. Try again later.");
	        apiLogInfo.setApiResponse("Executor service overload: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        logService.logMyInfo(httpRequest, apiLogInfo);

	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Unexpected error occurred while fetching employees.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        logService.logMyInfo(httpRequest, apiLogInfo);

	    } finally {
	        long totalTime = System.currentTimeMillis() - startTime;
	        logger.info("getAllEmployees() completed in " + totalTime + " ms");
	    }

	    return response;
	}


	
	
	public List<Object> example(Long empId)
	{
		return employeeRepository.findexample(empId);
	}
	
	public ServiceResponse getAllEmployeesForPerformance(HrHodHrViewPerformance hrHodHrViewPerformance) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allEmployeeListForPerformance=new ArrayList<Object[]>();
			if(hrHodHrViewPerformance.getHrvalidate()) {
				allEmployeeListForPerformance=employeeRepository.getAllEmployeesForPerformanceForHr();
			}else {
				allEmployeeListForPerformance = employeeRepository.getAllEmployeesForPerformance(hrHodHrViewPerformance.getEmpId());
				
			}
			
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

			if (allEmployeeListForPerformance != null) {
				int totalEnabledQuarters = quarterCycleRepository.countByIsEnableAndIsActive();
				allEmployeeListForPerformance.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					
					empDTO.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setDateOfJoining(object[1] != null ? stringToDateTimeParser.formatDateToString(object[1].toString())
									: null);
					empDTO.setEmail(object[2] != null ? object[2].toString() : null);
					empDTO.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
					empDTO.setJobRoleId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					empDTO.setManagerId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					empDTO.setName(object[6] != null ? object[6].toString() : null);
					empDTO.setDepartmentId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					empDTO.setJobRoleName(object[8] != null ? object[8].toString() : null);
					empDTO.setDepartmentName(object[9] != null ? object[9].toString() : null);
					empDTO.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					empDTO.setManagerName(object[11] != null ? object[11].toString() : null);
					empDTO.setExperience(object[12] != null ? object[12].toString() : null);
					empDTO.setBillable(object[13] != null ? (object[13].toString()) : null);
					empDTO.setTotalExperience(object[14] != null ? Float.parseFloat(object[14].toString()) : null);
					empDTO.setJobRoleName(object[15] != null ? (object[15].toString()) : null);	
					empDTO.setBillableType(object[16] != null ? object[16].toString() : null );	
					empDTO.setDesignationName(object[17] != null ? object[17].toString() : null);
                    empDTO.setReportingManagerId(object[18] != null ? Long.parseLong(object[18].toString()) : null)	;
                    empDTO.setReportingManagerName(object[19] != null ? object[19].toString() : null);
                    empDTO.setEmployeeRole(object[20] != null ? object[20].toString() : null);                
					empDTO.setHodId(object[21] != null ? Long.parseLong(object[21].toString()) : null );
				    empDTO.setHodName(object[22] != null ? object[22].toString() : null);
				    empDTO.setHodDepartmentName(object[23] != null ? object[23].toString() : null);
				    empDTO.setIsApmosysProduct(object[24] != null ? object[24].toString() : null);
				    if (object.length > 25 && object[25] != null) {
				    	try {
				    		if (object[25] instanceof Number) {
				    			empDTO.setMobileNo(((Number) object[25]).longValue());
				    		} else {
				    			String ms = object[25].toString().trim();
				    			if (!ms.isEmpty()) {
				    				empDTO.setMobileNo(Long.parseLong(ms.replaceAll("[^0-9]", "")));
				    			}
				    		}
				    	} catch (Exception ignored) { }
				    }
				    if (object.length > 26 && object[26] != null) {
				    	empDTO.setWorkLocation(object[26].toString().trim());
				    }
				    
				    String employmentId = empDTO.getEmployeementId() != null ? empDTO.getEmployeementId().toString() : null;
//				    String isConsultant = timesheetDto.getIsConsultant();
				    String isApmosysProduct = empDTO.getIsApmosysProduct();

				    if (employmentId != null) {
				        if ("true".equalsIgnoreCase(isApmosysProduct)) {
				        	empDTO.setEmploymentIdAcToET("AP-" + employmentId);
				        }else {
				        	empDTO.setEmploymentIdAcToET("A-" + employmentId);
				        }
				    }

				    
				    
					 
//					 int completedQuarters = quarterCycleRepository.countByEmpIdAndCompletionStatusAndQuarterIdIn(empDTO.getEmpId(), quarterCycleRepository.findAllEnabledQuarterIds());
					 
					 int completedQuarters = quarterCycleRepository.countByEmpIdAndCompletionStatusAndQuarterIdIn1(empDTO.getEmpId()); 
					 
					 double performanceStatus = (totalEnabledQuarters > 0) 
							    ? ((double) completedQuarters / totalEnabledQuarters) * 100 
							    : 0.0;
					 performanceStatus = Double.parseDouble(String.format("%.2f", performanceStatus));
					 empDTO.setPerformanceStatusPercentage(performanceStatus);
					  //empDTO.setApprovalsTo(object[24] != null ? object[24].toString() : null);
						
					 dtoList.add(empDTO);
				});
				 

	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	
	public ServiceResponse getAllEmployeesFor360View(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_all_employee");
		apiLogInfo.setApiUrl("/api/getAllEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		//String cacheKey = "allEmployees360";
//		
//		if (employeeCache.containsKey(cacheKey)) {
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(employeeCache.get(cacheKey));
//            apiLogInfo.setApiResponse("Data fetched from cache. Size: " + employeeCache.get(cacheKey).size());
//            logService.logMyInfo(httpRequest, apiLogInfo);
//            return response;
//        }
		
		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees360(empId);
			logBuilder.append("getALLEmployees size : "+allEmployeeList.size());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

			if (allEmployeeList != null) {
				allEmployeeList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();

					empDTO.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
					empDTO.setCity(object[8] != null ? object[8].toString() : null);
					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
					empDTO.setCreatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
					empDTO.setCreatedOn(object[11] != null ? (object[11].toString()) : null);
					empDTO.setDateOfBirth(
							object[12] != null ? stringToDateTimeParser.formatDateToString(object[12].toString())
									: null);
					empDTO.setDateOfJoining(
							object[13] != null ? stringToDateTimeParser.formatDateToString(object[13].toString())
									: null);
					empDTO.setEmail(object[14] != null ? object[14].toString() : null);
					empDTO.setEmergencyContactMobile(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					empDTO.setEmergencyContactPerson(object[16] != null ? object[16].toString() : null);
					empDTO.setEmploymentstatus(object[17] != null ? object[17].toString() : null);
					empDTO.setEsicNumber(object[18] != null ? object[18].toString() : null);
					empDTO.setFatherName(object[19] != null ? object[19].toString() : null);
					empDTO.setGender(object[20] != null ? object[20].toString() : null);
					empDTO.setGraduationType(object[21] != null ? object[21].toString() : null);
					empDTO.setPursuing(object[22] != null ? object[22].toString() : null);
					empDTO.setJobRoleId(object[23] != null ? Long.parseLong(object[23].toString()) : null);
					empDTO.setLandline(object[24] != null ? Long.parseLong(object[24].toString()) : null);
					empDTO.setManagerId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
					empDTO.setMaritalStatus(object[26] != null ? object[26].toString() : null);
					empDTO.setMobileNo(object[27] != null ? Long.parseLong(object[27].toString()) : null);
					empDTO.setMotherTongue(object[28] != null ? object[28].toString() : null);
					empDTO.setName(object[29] != null ? object[29].toString() : null);
					empDTO.setNoticePeriod(object[30] != null ? Short.parseShort(object[30].toString()) : null);
					empDTO.setAlternateMobileNo(object[31] != null ? Long.parseLong(object[31].toString()) : null);
					empDTO.setPanNumber(object[32] != null ? object[32].toString() : null);
					empDTO.setPassportNumber(object[33] != null ? object[33].toString() : null);
					empDTO.setPermanentAddress(object[34] != null ? object[34].toString() : null);
					empDTO.setPfAccountNumber(object[35] != null ? object[35].toString() : null);
					empDTO.setPincode(object[36] != null ? Integer.parseInt(object[36].toString()) : null);
					empDTO.setPlaceOfBirth(object[37] != null ? object[37].toString() : null);
					empDTO.setPassingGrade(object[38] != null ? object[38].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[39] != null ? object[39].toString() : null);
					empDTO.setRelation(object[40] != null ? object[40].toString() : null);
					empDTO.setState(object[41] != null ? object[41].toString() : null);
					empDTO.setUan(object[42] != null ? object[42].toString() : null);
					empDTO.setViewsOnOrganisation(object[43] != null ? object[43].toString() : null);
					empDTO.setYearOfPassing(object[44] != null ? Short.parseShort(object[44].toString()) : null);
					empDTO.setDepartmentId(object[45] != null ? Long.parseLong(object[45].toString()) : null);
					empDTO.setJobRoleName(object[46] != null ? object[46].toString() : null);
					empDTO.setDepartmentName(object[47] != null ? object[47].toString() : null);
					empDTO.setWorkLocation(object[48] != null ? object[48].toString() : null);
					empDTO.setProbationPeriod(object[49] != null ? Short.parseShort(object[49].toString()) : null);
					empDTO.setEmpId(object[50] != null ? Long.parseLong(object[50].toString()) : null);
					empDTO.setManagerName(object[51] != null ? object[51].toString() : null);
					empDTO.setExperience(object[52] != null ? object[52].toString() : null);
					empDTO.setBillable(object[53] != null ? (object[53].toString()) : null);
					empDTO.setChild1(object[54] != null ? (object[54].toString()) : null);
					empDTO.setChild2(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild3(object[56] != null ? (object[56].toString()) : null);
					empDTO.setMothersName(object[57] != null ? (object[57].toString()) : null);
					empDTO.setSpouse(object[58] != null ? (object[58].toString()) : null);
					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
					empDTO.setDateOfResign(
							object[60] != null ? stringToDateTimeParser.formatDateToString(object[60].toString())
									: null);
					empDTO.setInvalidAccessAttempt(object[61] != null ? Integer.parseInt(object[61].toString()) : null);
					empDTO.setDateOfRelieving(
							object[62] != null ? stringToDateTimeParser.formatDateToString(object[62].toString())
									: null);
					empDTO.setJobRoleName(object[63] != null ? (object[63].toString()) : null);	
					empDTO.setUpdatedByName(object[64] != null ? (object[64].toString()) : null);	
					empDTO.setCreatedByName(object[65] != null ? (object[65].toString()) : null);	
					empDTO.setUpdatedOn(object[66] != null ? (object[66].toString()) : null);
					empDTO.setIsTimesheetLockCheckEnable(object[67] != null ? (object[67].toString()) : null);
					empDTO.setEmploymentReleaseStatus(object[68] != null ? (object[68].toString()) : null);
					empDTO.setFailedAttempt(failedAttempt);
					empDTO.setPipFlag(object[69] != null ? object[69].toString() : null);
					empDTO.setPipId(object[70] != null ? Long.parseLong(object[70].toString()) : null );	
					empDTO.setBillableType(object[71] != null ? object[71].toString() : null );	
					empDTO.setProjectName(object[72] != null ? object[72].toString() : null);
					empDTO.setClientName(object[73] != null ? object[73].toString() : null);
					empDTO.setTeamName(object[74] != null ? object[74].toString() : null);
					empDTO.setDesignationName(object[75] != null ? object[75].toString() : null);
					empDTO.setIsConsultant(object[76] != null ? object[76].toString() : null);
					
					empDTO.setIsApprenticeship(object[77] != null ? object[77].toString() : null);
                    empDTO.setReportingManagerId(object[78] != null ? Long.parseLong(object[78].toString()) : null)	;
                    empDTO.setReportingManagerName(object[79] != null ? object[79].toString() : null);
                    empDTO.setEmployeeRole(object[80] != null ? object[80].toString() : null);                
                    
					empDTO.setReferedType(object[81] != null ? object[81].toString() : null);
					empDTO.setReferedName(object[82] != null ? object[82].toString() : null);

					empDTO.setEmployeeConfirmationDate(object[83] != null ? object[83].toString() : null);
					empDTO.setHodId(object[84] != null ? Long.parseLong(object[84].toString()) : null );
				    empDTO.setHodName(object[85] != null ? object[85].toString() : null);
				    empDTO.setHodDepartmentName(object[86] != null ? object[86].toString() : null);
				    empDTO.setIsApmosysProduct(object[89] != null ? object[89].toString() : null);	
				    
				    String employmentId = empDTO.getEmployeementId() != null ? empDTO.getEmployeementId().toString() : null;
		            String isConsultant = empDTO.getIsConsultant();
		            String isApmosysProduct = empDTO.getIsApmosysProduct();

		            if (employmentId != null) {
		                  if ("true".equalsIgnoreCase(isApmosysProduct)) {
		                	  empDTO.setEmploymentIdAcToET("AP-" + employmentId);                	  
//		                    newDto.setEmployeementIdAccToET("AP-" + employmentId);
		                } else {
		                	empDTO.setEmploymentIdAcToET("A-" + employmentId); 
//		                    newDto.setEmployeementIdAccToET("A-" + employmentId);
		                }
		            }
				    
				    if (object[87] != null && object[72] != null) {
		                String projectIdStr = object[87].toString().trim();
		                String projectNameStr = object[72].toString().trim();

		                
		                if (!projectIdStr.isEmpty() && !projectNameStr.isEmpty()) {
		                    String[] projectIds = projectIdStr.split(",");
		                    String[] projectNames = projectNameStr.split(",");

		                    
		                    List<ProjectDTO> projectList = new ArrayList<>();
		                    int length = Math.min(projectIds.length, projectNames.length);
		                    
		                    for (int i = 0; i < length; i++) {
		                        try {
		                            ProjectDTO projectDTO = new ProjectDTO();
		                            projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
		                            projectDTO.setProjectName(projectNames[i].trim());
		                            projectList.add(projectDTO);
		                        } catch (NumberFormatException e) {
		                            System.err.println("Invalid projectId: " + projectIds[i]);
		                        }
		                    }
		                    empDTO.setProjectList(projectList);
		                }
		            }
                    
					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
					
					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);

					
					dtoList.add(empDTO);
				});
				
                //employeeCache.put(cacheKey, dtoList);

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("List fetched from DB and stored in cache. Size: " + dtoList.size());

//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is null.");
				apiLogInfo.setApiResponse("Employee List is null.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	
	public void clearEmployeeCache() {
        employeeCache.clear();
    }

//	public ServiceResponse getAllEmployees() {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("get_all_employee");
//		apiLogInfo.setApiUrl("/api/getAllEmployees");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("getALLEmployees size : "+employeeRepository.getAllEmployees().size());
//
//		try {
//			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
//			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//
//			if (allEmployeeList != null) {
//				allEmployeeList.forEach((object) -> {
//					EmployeeDTO empDTO = new EmployeeDTO();
//
//					empDTO.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
//					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
//					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
//					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
//					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
//					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
//					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
//					empDTO.setCity(object[8] != null ? object[8].toString() : null);
//					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
//					empDTO.setCreatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
//					empDTO.setCreatedOn(object[11] != null ? (object[11].toString()) : null);
//					empDTO.setDateOfBirth(
//							object[12] != null ? stringToDateTimeParser.formatDateToString(object[12].toString())
//									: null);
//					empDTO.setDateOfJoining(
//							object[13] != null ? stringToDateTimeParser.formatDateToString(object[13].toString())
//									: null);
//					empDTO.setEmail(object[14] != null ? object[14].toString() : null);
//					empDTO.setEmergencyContactMobile(object[15] != null ? Long.parseLong(object[15].toString()) : null);
//					empDTO.setEmergencyContactPerson(object[16] != null ? object[16].toString() : null);
//					empDTO.setEmploymentstatus(object[17] != null ? object[17].toString() : null);
//					empDTO.setEsicNumber(object[18] != null ? object[18].toString() : null);
//					empDTO.setFatherName(object[19] != null ? object[19].toString() : null);
//					empDTO.setGender(object[20] != null ? object[20].toString() : null);
//					empDTO.setGraduationType(object[21] != null ? object[21].toString() : null);
//					empDTO.setPursuing(object[22] != null ? object[22].toString() : null);
//					empDTO.setJobRoleId(object[23] != null ? Long.parseLong(object[23].toString()) : null);
//					empDTO.setLandline(object[24] != null ? Long.parseLong(object[24].toString()) : null);
//					empDTO.setManagerId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
//					empDTO.setMaritalStatus(object[26] != null ? object[26].toString() : null);
//					empDTO.setMobileNo(object[27] != null ? Long.parseLong(object[27].toString()) : null);
//					empDTO.setMotherTongue(object[28] != null ? object[28].toString() : null);
//					empDTO.setName(object[29] != null ? object[29].toString() : null);
//					empDTO.setNoticePeriod(object[30] != null ? Short.parseShort(object[30].toString()) : null);
//					empDTO.setAlternateMobileNo(object[31] != null ? Long.parseLong(object[31].toString()) : null);
//					empDTO.setPanNumber(object[32] != null ? object[32].toString() : null);
//					empDTO.setPassportNumber(object[33] != null ? object[33].toString() : null);
//					empDTO.setPermanentAddress(object[34] != null ? object[34].toString() : null);
//					empDTO.setPfAccountNumber(object[35] != null ? object[35].toString() : null);
//					empDTO.setPincode(object[36] != null ? Integer.parseInt(object[36].toString()) : null);
//					empDTO.setPlaceOfBirth(object[37] != null ? object[37].toString() : null);
//					empDTO.setPassingGrade(object[38] != null ? object[38].toString() : null);
//					empDTO.setPreviousPfAccountNumber(object[39] != null ? object[39].toString() : null);
//					empDTO.setRelation(object[40] != null ? object[40].toString() : null);
//					empDTO.setState(object[41] != null ? object[41].toString() : null);
//					empDTO.setUan(object[42] != null ? object[42].toString() : null);
//					empDTO.setViewsOnOrganisation(object[43] != null ? object[43].toString() : null);
//					empDTO.setYearOfPassing(object[44] != null ? Short.parseShort(object[44].toString()) : null);
//					empDTO.setDepartmentId(object[45] != null ? Long.parseLong(object[45].toString()) : null);
//					empDTO.setJobRoleName(object[46] != null ? object[46].toString() : null);
//					empDTO.setDepartmentName(object[47] != null ? object[47].toString() : null);
//					empDTO.setWorkLocation(object[48] != null ? object[48].toString() : null);
//					empDTO.setProbationPeriod(object[49] != null ? Short.parseShort(object[49].toString()) : null);
//					empDTO.setEmpId(object[50] != null ? Long.parseLong(object[50].toString()) : null);
//					empDTO.setManagerName(object[51] != null ? object[51].toString() : null);
//					empDTO.setExperience(object[52] != null ? object[52].toString() : null);
//					empDTO.setBillable(object[53] != null ? (object[53].toString()) : null);
//					empDTO.setChild1(object[54] != null ? (object[54].toString()) : null);
//					empDTO.setChild2(object[55] != null ? (object[55].toString()) : null);
//					empDTO.setChild3(object[56] != null ? (object[56].toString()) : null);
//					empDTO.setMothersName(object[57] != null ? (object[57].toString()) : null);
//					empDTO.setSpouse(object[58] != null ? (object[58].toString()) : null);
//					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
//					empDTO.setDateOfResign(
//							object[60] != null ? stringToDateTimeParser.formatDateToString(object[60].toString())
//									: null);
//					empDTO.setInvalidAccessAttempt(object[61] != null ? Integer.parseInt(object[61].toString()) : null);
//					empDTO.setDateOfRelieving(
//							object[62] != null ? stringToDateTimeParser.formatDateToString(object[62].toString())
//									: null);
//					empDTO.setJobRoleName(object[63] != null ? (object[63].toString()) : null);	
//					empDTO.setUpdatedByName(object[64] != null ? (object[64].toString()) : null);	
//					empDTO.setCreatedByName(object[65] != null ? (object[65].toString()) : null);	
//					empDTO.setUpdatedOn(object[66] != null ? (object[66].toString()) : null);
//					empDTO.setIsTimesheetLockCheckEnable(object[67] != null ? (object[67].toString()) : null);
//					empDTO.setEmploymentReleaseStatus(object[68] != null ? (object[68].toString()) : null);
//					empDTO.setFailedAttempt(failedAttempt);
//					empDTO.setPipFlag(object[69] != null ? object[69].toString() : null);
//					empDTO.setPipId(object[70] != null ? Long.parseLong(object[70].toString()) : null );	
//					empDTO.setBillableType(object[71] != null ? object[71].toString() : null );	
//					empDTO.setProjectName(object[72] != null ? object[72].toString() : null);
//					empDTO.setClientName(object[73] != null ? object[73].toString() : null);
//					empDTO.setTeamName(object[74] != null ? object[74].toString() : null);
//					empDTO.setDesignationName(object[75] != null ? object[75].toString() : null);
//					empDTO.setIsConsultant(object[76] != null ? object[76].toString() : null);
//					empDTO.setIsApprenticeship(object[77] != null ? object[77].toString() : null);
//
//					//added by rahul
//					empDTO.setReferedType(object[78] != null ? object[78].toString() : null);
//					empDTO.setReferedName(object[79] != null ? object[79].toString() : null);
//
//					
//				
//
//					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
//					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
//					
//					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
//					
//					
//					
//					dtoList.add(empDTO);
//				});
//				
//				
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee List is null.");
//				apiLogInfo.setApiResponse("Employee List is null.");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
//	public ServiceResponse getEmployeeByAppreciationName(String name) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("get_employee_by_name");
//		apiLogInfo.setApiUrl("/api/getEmployeeByName");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("getALLEmployees size : "+employeeRepository.findByAppreciationByName(name).size());
//		try {
//			List<Object[]> allEmployeeList = employeeRepository.findByAppreciationByName(name);
//			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//
//			if (allEmployeeList != null) {
//				allEmployeeList.forEach((object) -> {
//					EmployeeDTO empDTO = new EmployeeDTO();
//					if (object[0] instanceof Employee) {
//			            Employee employee = (Employee) object[0];
//			            empDTO.setEmployeementId(employee.getEmployeementId());
//			            empDTO.setAadhar(employee.getAadhar());
//			            empDTO.setAboutMe(employee.getAboutMe());
//			            empDTO.setAddress(employee.getAddress());
//			            empDTO.setBankAccountNo(employee.getBankAccountNo());
//			            empDTO.setBankIFSCCode(employee.getBankIFSCCode());
//			            empDTO.setBankName(employee.getBankName());
//			            empDTO.setBloodGroup(employee.getBloodGroup());
//			            empDTO.setCity(employee.getCity());
//			            empDTO.setCountry(employee.getCountry());
//			            empDTO.setCreatedBy(employee.getCreatedBy());
//			            empDTO.setCreatedOn(employee.getCreatedOn().toString());
//			            empDTO.setDateOfBirth(employee.getDateOfBirth().toString());
//			            empDTO.setDateOfJoining(employee.getDateOfJoining().toString());
//			            empDTO.setEmail(employee.getEmail());
//			            empDTO.setEmergencyContactMobile(employee.getEmergencyContactMobile());
//			            empDTO.setEmergencyContactPerson(employee.getEmergencyContactPerson());
//			            empDTO.setEmploymentstatus(employee.getEmploymentstatus());
//			            empDTO.setEsicNumber(employee.getEsicNumber());
//			            empDTO.setFatherName(employee.getFatherName());
//			            empDTO.setGender(employee.getGender());
//			            empDTO.setGraduationType(employee.getGraduationType());
//			            empDTO.setPursuing(employee.getPursuing());
//			            empDTO.setJobRoleId(employee.getJobRoleId());
//			            empDTO.setLandline(employee.getLandline());
//			            empDTO.setManagerId(employee.getManagerId());
//			            empDTO.setMaritalStatus(employee.getMaritalStatus());
//			            empDTO.setMobileNo(employee.getMobileNo());
//			            empDTO.setMotherTongue(employee.getMotherTongue());
//			            empDTO.setName(employee.getName());
//			            empDTO.setNoticePeriod(employee.getNoticePeriod());
//			            empDTO.setAlternateMobileNo(employee.getAlternateMobileNo());
//			            empDTO.setPanNumber(employee.getPanNumber());
//			            empDTO.setPassportNumber(employee.getPassportNumber());
//			            empDTO.setPermanentAddress(employee.getPermanentAddress());
//			            empDTO.setPfAccountNumber(employee.getPfAccountNumber());
//			            empDTO.setPincode(employee.getPincode());
//			            empDTO.setPlaceOfBirth(employee.getPlaceOfBirth());
//			            empDTO.setPassingGrade(employee.getPassingGrade());
//			            empDTO.setPreviousPfAccountNumber(employee.getPreviousPfAccountNumber());
//			            empDTO.setRelation(employee.getRelation());
//			            empDTO.setState(employee.getState());
//			            empDTO.setUan(employee.getUan());
//			            empDTO.setViewsOnOrganisation(employee.getViewsOnOrganisation());
//			            empDTO.setYearOfPassing(employee.getYearOfPassing());
//			            empDTO.setWorkLocation(employee.getWorkLocation());
//			            empDTO.setProbationPeriod(employee.getProbationPeriod());
//			            empDTO.setEmpId(employee.getEmpId());
//			            empDTO.setExperience(employee.getExperience());
//			            empDTO.setBillable(employee.getBillable());
//			            empDTO.setChild1(employee.getChild1());
//			            empDTO.setChild2(employee.getChild2());
//			            empDTO.setChild3(employee.getChild3());
//			            empDTO.setMothersName(employee.getMothersName());
//			            empDTO.setSpouse(employee.getSpouse());
//			            empDTO.setTotalExperience(employee.getTotalExperience());
//			            empDTO.setInvalidAccessAttempt(employee.getInvalidAccessAttempt());
//			            empDTO.setDateOfRelieving(employee.getDateOfRelieving());
//			            empDTO.setUpdatedOn(employee.getUpdatedOn().toString());
//			            empDTO.setIsTimesheetLockCheckEnable(employee.getIsTimesheetLockCheckEnable());
//			            empDTO.setEmploymentReleaseStatus(employee.getEmploymentReleaseStatus());
//			            empDTO.setPipId(employee.getPipId());
//			            empDTO.setBillableType(employee.getBillableType());
//			            empDTO.setIsConsultant(employee.getIsConsultant());
//					
//			        }
//				
//					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
//					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
//					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
//					dtoList.add(empDTO);
//				});
//				
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee List is null.");
//				apiLogInfo.setApiResponse("Employee List is null.");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//    }
	
	public ServiceResponse previewImage(MultipartFile image) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/previewImage");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {

			byte[] imageBytes = image.getBytes();
			logBuilder.append("imageBytes : "+image.getBytes());
			if (imageBytes != null) {
				response.setServiceResponse(imageBytes);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("preview image");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceResponse("Failed To Preview Image !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Failed To Preview Image !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			response.setServiceResponse("Something went wrong");
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			e.printStackTrace();
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse uploadImage(MultipartFile image, Long uploadedBy) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("upload_profile_image");
		apiLogInfo.setApiUrl("/api/uploadImage");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("uploadedBy : " + uploadedBy);
		try {

			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

			if (employeeObject.isPresent()) {
				System.out.println("Employee : " + employeeObject);
				Employee employeeObj = employeeObject.get();

				String extension = FilenameUtils.getExtension(image.getOriginalFilename());
				String newFileName = employeeObj.getName().replaceAll("\\s", "").toLowerCase() + employeeObj.getEmpId()
						+ "." + extension;

				System.err.println(" extenstion : " + extension);
				System.err.println(" New File Name : " + newFileName);

				File savedFile = new File(imageFileLocation + File.separator + newFileName);

				System.err.println(" New File Location : " + savedFile);

				if (!savedFile.exists()) {
					image.transferTo(savedFile);

					if (savedFile.exists()) {
						employeeObj.setProfileImageName(newFileName);
						Employee dbResponse = employeeRepository.save(employeeObj);

						if (dbResponse != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Employee Profile Picture Uploaded.");
							
							apiLogInfo.setApiResponse("Employee Profile Picture Uploaded.");	
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee Profile Picture Upload Failed.");
							
							apiLogInfo.setApiResponse("Employee Profile Picture Upload Failed.");	
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
						}

					} else {
						response.setServiceResponse("Failed To save Image !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						
						apiLogInfo.setApiResponse("Failed To save Image !!");	
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
					}
				} else {
					if (savedFile.delete()) {
						File savedUpdatedFile = new File(imageFileLocation + File.separator + newFileName);
						image.transferTo(savedUpdatedFile);

						if (savedFile.exists()) {
							employeeObj.setProfileImageName(newFileName);
							Employee dbResponse = employeeRepository.save(employeeObj);

							if (dbResponse != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Employee Profile Picture Uploaded.");
								
								apiLogInfo.setApiResponse("Employee Profile Picture Uploaded.");	
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
								
							} else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Employee Profile Picture Upload Failed.");
								
								apiLogInfo.setApiResponse("Employee Profile Picture Upload Failed..");	
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
							}

						} else {
							response.setServiceResponse("Failed To save Image !!");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							
							apiLogInfo.setApiResponse("Failed To save Image !!");	
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
						}
					} else {
						response.setServiceResponse("Failed To Delete Existing Profile Image !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						
						apiLogInfo.setApiResponse("Failed To Delete Existing Profile Image !!");	
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
					}
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				
				apiLogInfo.setApiResponse("User Not Found !!");	
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse updateEmployeeProfileByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_employee_profile_by_empId");
		apiLogInfo.setApiUrl("/api/updateEmployeeProfileByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();

		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employee = employeeObject.get();

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setName(employeedto.getName());
				employee.setDateOfBirth(employeedto.getDateOfBirth() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd")
						: null);
				employee.setDateOfJoining(employeedto.getDateOfJoining() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd")
						: null);
				employee.setEmail(employeedto.getEmail());
				employee.setGender(employeedto.getGender());
				employee.setBloodGroup(employeedto.getBloodGroup());
				employee.setMaritalStatus(employeedto.getMaritalStatus());
				employee.setFatherName(employeedto.getFatherName());
				employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
				employee.setMotherTongue(employeedto.getMotherTongue());
				employee.setPassportNumber(employeedto.getPassportNumber());
				employee.setAadhar(employeedto.getAadhar());
				employee.setPanNumber(employeedto.getPanNumber());
				employee.setMobileNo(employeedto.getMobileNo());
				employee.setLandline(employeedto.getLandline());
				employee.setAddress(employeedto.getAddress());
				employee.setCity(employeedto.getCity());
				employee.setState(employeedto.getState());
				employee.setCountry(employeedto.getCountry());
				employee.setPincode(employeedto.getPincode());
				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
				employee.setPermanentAddress(employeedto.getPermanentAddress());
				employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
				employee.setRelation(employeedto.getRelation());
				employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
				employee.setNoticePeriod(employeedto.getNoticePeriod());
				employee.setEmploymentstatus(employeedto.getEmploymentstatus());
				employee.setBankName(employeedto.getBankName());
				employee.setBankAccountNo(employeedto.getBankAccountNo());
				employee.setBankIFSCCode(employeedto.getBankIFSCCode());
				employee.setPfAccountNumber(employeedto.getPfAccountNumber());
				employee.setPreviousPfAccountNumber(employeedto.getPreviousPfAccountNumber());
				employee.setUan(employeedto.getUan());
				employee.setEsicNumber(employeedto.getEsicNumber());
				employee.setGraduationType(employeedto.getGraduationType());
				employee.setPursuing(employeedto.getPursuing());
				employee.setYearOfPassing(employeedto.getYearOfPassing());
				employee.setPassingGrade(employeedto.getPassingGrade());
				employee.setAboutMe(employeedto.getAboutMe());
				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
				employee.setWorkLocation(employeedto.getWorkLocation());
				employee.setExperience(employeedto.getExperience());
				employee.setBillable(employeedto.getBillable());
				employee.setChild1(employeedto.getChild1());
				employee.setChild2(employeedto.getChild2());
				employee.setChild3(employeedto.getChild3());
				employee.setMothersName(employeedto.getMothersName());
				employee.setSpouse(employeedto.getSpouse());
				employee.setTotalExperience(employeedto.getTotalExperience());
				// Certification
				// Case 1 : Updating Existing certification
				if (employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
					employeedto.getCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() != null)
							.forEach((certificate) -> {

								EmployeeCertificate employeeCertificate = employeeCertificateRepository
										.findById(certificate.getEmployeeCertificateId()).get();

								employeeCertificate.setCertificationName(certificate.getCertificationName());
								employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
								employeeCertificate.setDateOfCompletion(stringToDateTimeParser
										.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
								employeeCertificate.setDuration(certificate.getDuration());
								employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
								employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());

								employeeCertificateRepository.save(employeeCertificate);
							});
				}

				if (employeedto.getUpdatedCertifications() != null
						&& !employeedto.getUpdatedCertifications().isEmpty()) {
					// Case 2 : Adding New certification
					newCertificationlist = employeedto.getUpdatedCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() == null)
							.collect(Collectors.toList());
					if (!newCertificationlist.isEmpty()) {
						newCertificationlist.forEach((certification) -> {
							certification.setEmpId(employeedto.getEmpId());

						});
						addCertifications(newCertificationlist, employeedto.getIsDraft());
					}

					// Case 3 : Deleting Removed certification
					employeedto.getUpdatedCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() != null)
							.forEach((certification) -> {
								employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
							});
				}

				// Previous Employer
				if (employeedto.getExperience().equals("Fresher")) {
					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
							.findByEmpId(employeedto.getEmpId());

					if (!previousEmploymentList.isEmpty()) {
						previousEmploymentList.stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((prevEmployer) -> {
									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
								});
					}
				} else {
					if (employeedto.getPreviousEmploymentList() != null
							&& !employeedto.getPreviousEmploymentList().isEmpty()) {
						employeedto.getPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((previousEmployeeDTO) -> {

									PreviousEmployment previousEmployment = previousEmploymentRepository
											.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();

									previousEmployment.setDateOfJoining(stringToDateTimeParser
											.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd"));
									previousEmployment.setDateOfRelieving(stringToDateTimeParser
											.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd"));
									previousEmployment.setDesignation(previousEmployeeDTO.getDesignation());
									previousEmployment.setHrContactNumber(previousEmployeeDTO.getHrContactNumber());
									previousEmployment.setHrName(previousEmployeeDTO.getHrName());
									previousEmployment.setManagerName(previousEmployeeDTO.getManagerName());
									previousEmployment
											.setManagerContactNumber(previousEmployeeDTO.getManagerContactNumber());
									previousEmployment.setEmployerName(previousEmployeeDTO.getEmployerName());
									previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());

									previousEmploymentRepository.save(previousEmployment);
								});
					}

					if (employeedto.getUpdatedPreviousEmploymentList() != null
							&& !employeedto.getUpdatedPreviousEmploymentList().isEmpty()) {
						newPreviousEmploymentList = employeedto.getUpdatedPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null)
								.collect(Collectors.toList());
						if (!newPreviousEmploymentList.isEmpty()) {
							newPreviousEmploymentList.forEach((previousEmployer) -> {
								previousEmployer.setEmpId(employeedto.getEmpId());

							});
							addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
						}

						employeedto.getUpdatedPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((prevEmployer) -> {
									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
								});
					}
				}

				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Profile Updated.");
					apiLogInfo.setApiResponse("Employee Profile Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Failed.");
					apiLogInfo.setApiResponse("Employee Profile Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not Found");
				apiLogInfo.setApiResponse("Employee Profile Not Found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse getAllEmployeesByRole(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllEmployeesByRole");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("role : ").append(employeedto.getRole());

	    try {
	        List<JobRole> jobRoleObj;
	        List<String> jobRoles = new ArrayList<>(Arrays.asList("Employee", "HR"));

	        if ("Manager".equals(employeedto.getRole())) {
	            jobRoleObj = jobRoleRepository.findByEmployeeRoleNotIn(jobRoles);
	        } else if ("HOD".equals(employeedto.getRole())) {
	            jobRoles = new ArrayList<>(Arrays.asList("Employee", "HR", "Manager"));
	            jobRoleObj = jobRoleRepository.findByEmployeeRoleNotIn(jobRoles);
	        } else {
	            jobRoleObj = jobRoleRepository.findByEmployeeRole(employeedto.getRole());
	            System.err.println("In else part :: " + employeedto.getRole());
	        }

	        if (!jobRoleObj.isEmpty()) {
	            List<Long> jobRoleIds = jobRoleObj.stream()
	                    .map(JobRole::getJobRoleId)
	                    .collect(Collectors.toList());

	            System.out.println("jobRoleIds :: " + jobRoleIds);

	            List<EmployeeDTO> empList = employeeRepository.getEmployeesByRoleIds(jobRoleIds);

	            Optional.ofNullable(empList).ifPresentOrElse((list) -> {
	                List<EmployeeDTO> sortedList = list.stream()
	                        .sorted(Comparator.comparing(EmployeeDTO::getName, Comparator.nullsLast(String::compareTo)))
	                        .collect(Collectors.toList());

	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(sortedList);

	                apiLogInfo.setApiResponse(sortedList.size() + " employee(s) found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }, () -> {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No Employee Found");

	                apiLogInfo.setApiResponse("No Employee Found");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            });
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Job Roles Found");

	            apiLogInfo.setApiResponse("No Job Roles Found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);

	    return response;
	}

//	public ServiceResponse getAllEmployeesByDepartmentIds(EmployeeDTO employeedto) {
//	    ServiceResponse response = new ServiceResponse();
//
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getAllEmployeesByDepartmentIds");
//	    apiLogInfo.setLogLevel("INFO");
//
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("departmentList : ").append(employeedto.getDepartmentList());
//
//	    try {
//	        Optional.ofNullable(employeedto.getDepartmentList()).ifPresentOrElse(departmentList -> {
//
//	            if (departmentList.isEmpty()) {
//	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	                response.setServiceResponse("Department list is empty.");
//
//	                apiLogInfo.setApiResponse("Department list is empty.");
//	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            } else {
//	                List<Long> deptIds = departmentList.stream()
//	                    .map(department -> department.getDeptId())
//	                    .collect(Collectors.toList());
//
//	                List<EmployeeDTO> employeeList = employeeRepository.getAllEmployeesByDepartmentIds(deptIds);
//
//	                Optional.ofNullable(employeeList).ifPresent(list -> {
//	                    if (list.isEmpty()) {
//	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	                        response.setServiceResponse("Employee list is empty.");
//
//	                        apiLogInfo.setApiResponse("Employee list is empty");
//	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	                    } else {
//	                        List<EmployeeDTO> dtoList = new ArrayList<>();
//
//	                        list.forEach(emp -> {
//	                            EmployeeDTO dto = new EmployeeDTO();
//	                            dto.setEmpId(emp.getEmpId());
//	                            dto.setName(emp.getName());
//	                            dto.setJobRoleName(emp.getJobRoleName());
//	                            dto.setDepartmentId(emp.getDepartmentId());
//	                            dto.setDepartmentName(emp.getDepartmentName());
//	                            dto.setEmploymentId(emp.getEmploymentId());
//	                            dtoList.add(dto);
//	                        });
//
//	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	                        response.setServiceResponse(dtoList);
//
//	                        apiLogInfo.setApiResponse("dtoList : " + dtoList);
//	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	                    }
//	                });
//	            }
//
//	        }, () -> {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("Department list is null");
//
//	            apiLogInfo.setApiResponse("Department list is null");
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        });
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something Went Wrong.");
//	        response.setServiceError(e.getMessage());
//
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
//	    apiLogInfo.setApiRequest(logBuilder.toString());
//	    logService.logMyInfo(httpRequest, apiLogInfo);
//
//	    return response;
//	}



//	public ServiceResponse getAllEmployeesByDepartmentId(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		//apiLogInfo.setSubFeatureName("delete_holiday");
//		apiLogInfo.setApiUrl("/api/getAllEmployeesByDepartmentId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("departmentId : " + employeedto.getDepartmentId());
//		try {
//			
//			List<Object[]> objectArrayList = employeeRepository.getAllEmployeesByDepartmentId(employeedto.getDepartmentId());
//			
//			if(!objectArrayList.isEmpty()) {
//				
//				List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//
//				objectArrayList.forEach((object) -> {
//
//					EmployeeDTO dto = new EmployeeDTO();
//
//					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//					dto.setName(object[1] != null ? object[1].toString() : null);
//					dto.setJobRoleName(object[2] != null ? object[2].toString() : null);
//					dto.setDepartmentId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
//					dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
//
//					dtoList.add(dto);
//
//				});
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				
//				apiLogInfo.setApiResponse("dtoList : " +dtoList);			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Department list is null");
//				
//				apiLogInfo.setApiResponse("Department list is null");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	public ServiceResponse updateEmployeeForgotPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateEmployeeForgotPassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("email : " + employeedto.getEmail());

		try {
			response = updateEmployeePassword(employeedto);
			
			if(ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())){
				Employee employee = employeeRepository.findByEmail(employeedto.getEmail());
				UserSession existingUserSession = userSessionRepository.findByEmpId(employee.getEmpId());
				boolean isUserLoggedIn = (existingUserSession != null ) ? true : false; 
				
				if (isUserLoggedIn) {
					userSessionRepository.deleteById(existingUserSession.getUserSessionId());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	

	public ServiceResponse updateEmployeePassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		// apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/updateEmployeePassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("email : " + employeedto.getEmail());

		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null) {
				
				System.out.println(employee.getPassword());
				System.out.println(employeedto.getNewPassword());

				if (employee.getPassword().equalsIgnoreCase(employeedto.getNewPassword())) {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Old password and new password should not be same");
				}
				else {

				employee.setPassword(employeedto.getNewPassword());
				employee.setIsNew("false");
				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Password Updated.");

					apiLogInfo.setApiResponse("Password Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Password Updation Failed.");

					apiLogInfo.setApiResponse("Password Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Not Found");

				apiLogInfo.setApiResponse("Employee Not Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse checkEmployeeOldPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/checkEmployeeOldPassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		try {
			Optional<Employee> checkEmployeeOldPassword = employeeRepository.findById(employeedto.getEmpId());

			Employee employeepassword = checkEmployeeOldPassword.get();

			String dbPassword = employeepassword.getPassword();
			System.out.println(dbPassword);

			String oldPassword = employeedto.getPassword();
			System.out.println(oldPassword);

			if (dbPassword.equals(oldPassword)) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Password match");
				
				apiLogInfo.setApiResponse("Password match");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Incorrect Old Password");
				
				apiLogInfo.setApiResponse("Incorrect Old Password");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse checkEmployeeEmail(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setApiUrl("/api/checkEmployeeEmail");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("email : " + employeedto.getEmail());
//
//		try {
//			Employee checkEmployeeEmail = employeeRepository.findByEmail(employeedto.getEmail());
//			DraftEmployee checkDraftEmployeementEmail = draftEmployeeRepository.findByEmail(employeedto.getEmail());
//
//			if(checkEmployeeEmail != null) {
//					if(!checkEmployeeEmail.getEmployeementId().equals(employeedto.getEmployeementId())) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Email already exists !!");
//						apiLogInfo.setApiResponse("Email already exists !!");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					}
//			}
//			
//			if(checkDraftEmployeementEmail != null) {
//					if(!checkDraftEmployeementEmail.getEmployeementId().equals(employeedto.getEmployeementId())) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Email already exists !!");
//						apiLogInfo.setApiResponse("Email already exists !!");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					}else {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					}
//			}
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
//	 added by anurag
	public ServiceResponse checkEmployeeEmail(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkEmployeeEmail");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("email : " + employeedto.getEmail());

		try {
			Employee checkEmployeeEmail = employeeRepository.findByEmail(employeedto.getEmail());
			System.err.println(checkEmployeeEmail);
			System.err.println(employeedto);
			DraftEmployee checkDraftEmployeementEmail = draftEmployeeRepository.findByEmail(employeedto.getEmail());
			
			//System.out.println(" checkEmployeeEmail.getEmployeementId()  :  "+checkEmployeeEmail.getEmployeementId() +"  =  employeedto.getEmployeementId() "+employeedto.getEmployeementId());

			if(checkEmployeeEmail != null) {
				if (Objects.equals(employeedto.getEmployeementId(), checkEmployeeEmail.getEmployeementId())) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Email already exists !!");
						apiLogInfo.setApiResponse("Email already exists !!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}


		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}


//	public ServiceResponse checkEmployeementId(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setApiUrl("/api/checkEmployeementId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("email : " + employeedto.getEmail());
//
//		try {
//			Employee checkEmployeementId = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());
//			DraftEmployee checkDraftEmployeementId = draftEmployeeRepository
//					.findByEmployeementId(employeedto.getEmployeementId());
//
//			if (checkEmployeementId == null && checkDraftEmployeementId == null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				
//			} else {
//				if (checkEmployeementId != null && !employeedto.getEmpId().equals(checkEmployeementId.getEmpId())) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Employeement ID already exist!");
//					apiLogInfo.setApiResponse("Employeement ID already exist!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//				if (checkDraftEmployeementId != null && !employeedto.getEmail().equals(checkDraftEmployeementId.getEmail())) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Employeement ID already exist in Employee Draft!");
//					apiLogInfo.setApiResponse("Employeement ID already exist in Employee Draft!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}


//	 added by anurag
	public ServiceResponse checkEmployeementId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkEmployeementId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("email : " + employeedto.getEmail());

		try {
			String employeeType = employeedto.getEmployeeType();
			Employee checkEmployeementId;
			if ("Apmosys Product".equalsIgnoreCase(employeeType)) {
				checkEmployeementId = employeeRepository.findByEmployeementIdForApmosysProduct(employeedto.getEmployeementId());
			} else if("Consultant".equalsIgnoreCase(employeeType)) {
				checkEmployeementId = employeeRepository.findByEmployeementIdForConsultant(employeedto.getEmployeementId());
//			}
//			else if("Apprentice".equalsIgnoreCase(employeeType)) {
//				checkEmployeementId = employeeRepository.findByEmployeementIdForApprentice(employeedto.getEmployeementId());
//				
			}
			else {
				checkEmployeementId = employeeRepository.findByEmployeementIdForOthers(employeedto.getEmployeementId());
			}
//			Employee checkEmployeementId = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());
//			DraftEmployee checkDraftEmployeementId = draftEmployeeRepository
//					.findByEmployeementId(employeedto.getEmployeementId());
			
			if(employeedto.getEmpId()!=null){

			if(checkEmployeementId!=null && checkEmployeementId.getEmpId()!=null && checkEmployeementId.getEmpId().toString().equals(employeedto.getEmpId().toString())) {
				checkEmployeementId=null;
			}
		}

			
			if (checkEmployeementId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
//				if (checkEmployeementId != null && employeedto.getEmployeementId().equals(checkEmployeementId.getEmployeementId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employeement ID already exist!");
					apiLogInfo.setApiResponse("Employeement ID already exist!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	
//	public ServiceResponse checkEmployeeMobileNo(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setApiUrl("/api/checkEmployeeMobileNo");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append(employeedto.getMobileNo());
//		try {
//
////			if (employeedto.getEmpId() != null) {
////				checkEmployeeMobileNo = employeeRepository.findByMobileNoAndEmpId(employeedto.getMobileNo(),
////						employeedto.getEmpId());
////				checkDraftEmployeeMobileNo = draftEmployeeRepository
////						.findByMobileNoAndDraftEmpId(employeedto.getMobileNo(), employeedto.getEmpId());
////			} else {
////				checkEmployeeMobileNo = employeeRepository.findByMobileNo(employeedto.getMobileNo());
////				checkDraftEmployeeMobileNo = draftEmployeeRepository.findByMobileNo(employeedto.getMobileNo());
////			}
////
////			if (employeedto.getMobileNo() == null) {
////				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
////			} else {
////				if (!checkEmployeeMobileNo.isEmpty()) {
////					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////					response.setServiceResponse("Mobile Number already exist!");
////				}
////				if (!checkDraftEmployeeMobileNo.isEmpty()) {
////					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////					response.setServiceResponse("Mobile Number already exist in Employee Draft!");
////				}
////			}
//			
//			List<Employee> checkEmployeeMobileNo = checkEmployeeMobileNo = employeeRepository.findByMobileNo(employeedto.getMobileNo());
//			List<DraftEmployee> checkDraftEmployeeMobileNo = draftEmployeeRepository.findByMobileNo(employeedto.getMobileNo());
//
//			if(!checkEmployeeMobileNo.isEmpty()) {
//				checkEmployeeMobileNo.forEach((employee) -> {
//					if(!employee.getEmployeementId().equals(employeedto.getEmployeementId())) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Mobile Number already exist!");
//						apiLogInfo.setApiResponse("Mobile Number already exist!");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					}
//				});
//			}
//			
//			if(!checkDraftEmployeeMobileNo.isEmpty()) {
//				checkDraftEmployeeMobileNo.forEach((employee) -> {
//					if(!employee.getEmployeementId().equals(employeedto.getEmployeementId())) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Mobile Number already exist in Employee Draft!");
//						apiLogInfo.setApiResponse("Mobile Number already exist in Employee Draft!");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					}else {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					}
//				});
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	
//	added by anurag
	public ServiceResponse checkEmployeeMobileNo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkEmployeeMobileNo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeedto.getMobileNo());
		try {


			List<Employee> checkEmployeeMobileNo = employeeRepository.findByMobileNo(employeedto.getMobileNo());
			

			if(!checkEmployeeMobileNo.isEmpty()) {
				checkEmployeeMobileNo.forEach((employee) -> {
					System.err.println(employee.getEmployeementId() + " = V  employeedto.getEmployeementId()  ::  "+employeedto.getEmployeementId());
					if(!employee.getEmployeementId().equals(employeedto.getEmployeementId())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Mobile Number already exist!");
						apiLogInfo.setApiResponse("Mobile Number already exist!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Mobile Number is successfully added !");
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
//	 addded by anurag
	public ServiceResponse checkEmployeeAadharNumber(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkEmployeeAadharNumber");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("aadhar number : "+employeedto.getAadhar());
		try {
			
			List<Employee> existingEmployeeAadhar = employeeRepository.findByAadhar(employeedto.getAadhar());
			List<DraftEmployee> existingDraftEmployeeAadhar = draftEmployeeRepository.findByAadhar(employeedto.getAadhar());

			if(!existingEmployeeAadhar.isEmpty()) {
				existingEmployeeAadhar.forEach((employee) -> {
					if(!employee.getEmployeementId().equals(employeedto.getEmployeementId())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Aadhaar Number already exists !!");
						apiLogInfo.setApiResponse("Aadhaar Number already exists !!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				});
			}
			
			if(!existingDraftEmployeeAadhar.isEmpty()) {
				existingDraftEmployeeAadhar.forEach((employee) -> {
					if(!employee.getEmployeementId().equals(employeedto.getEmployeementId())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Aadhaar Number already exists !!");
						apiLogInfo.setApiResponse("Aadhaar Number already exists !!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse checkEmployeePanNumber(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkEmployeePanNumber");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("pan number : "+employeedto.getPanNumber());

		try {
			
			if(employeedto.getPanNumber() != null) {
				List<Employee> existingEmployeePan = employeeRepository.findByPanNumber(employeedto.getPanNumber());
				List<DraftEmployee> existingEmployeeDraftPan = draftEmployeeRepository.findByPanNumber(employeedto.getPanNumber());

				if(!existingEmployeePan.isEmpty()){
					for(Employee empObj :existingEmployeePan) {
						if(!employeedto.getEmployeementId().equals(empObj.getEmployeementId())) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("PAN Number already exist!");
						}
					}
				}
				
				if(!existingEmployeeDraftPan.isEmpty()){
					for(DraftEmployee draftEmpObj :existingEmployeeDraftPan) {
						if(!employeedto.getEmployeementId().equals(draftEmpObj.getEmployeementId())) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("PAN Number already exist!");
						}
					}
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Please enter Pan Number!");
				apiLogInfo.setApiResponse("Please enter Pan Number!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	public EmployeeDTO getEmployeeInfoOnLogin(String employeeEmail) {
		ServiceResponse response = new ServiceResponse();
		EmployeeDTO employee = new EmployeeDTO();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getEmployeeInfoOnLogin");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employee email : "+employeeEmail);		
		try {
			List<Object[]> objectArrayList = employeeRepository.getEmployeeInfoOnLogin(employeeEmail);

			if (objectArrayList.isEmpty()) {
				return employee;
			} else {
				objectArrayList.forEach((object) -> {

					EmployeeDTO dto = new EmployeeDTO();

					employee.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					employee.setName(object[1] != null ? object[1].toString() : null);
					employee.setDateOfJoining(object[2] != null ? object[2].toString() : null);
					employee.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
					employee.setManagerId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					employee.setGender(object[5] != null ? object[5].toString() : null);
					employee.setDepartmentId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					employee.setEmployeementId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					employee.setIsNew(object[8] != null ? object[8].toString() : null);
					employee.setIsUserInfoUpdated(object[9] != null ? object[9].toString() : null);
					employee.setIsAppreciationEnable(object[10] != null ? object[10].toString() : null);
					employee.setTimesheetLockDays(timesheetLockDays);
					employee.setDepartmentName(object[11] != null ? object[11].toString() : null);
					employee.setDateOfResign(object[12] != null ? object[12].toString() : null);
					employee.setEmployeeRole(object[13] != null ? object[13].toString() : null);
					employee.setManagerName(object[14] != null ? object[14].toString() : null);
					employee.setManagerEmail(object[15] != null ? object[15].toString() : null);
					employee.setHodId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
					employee.setHodName(object[17] != null ? object[17].toString() : null);
					employee.setHodEmail(object[18] != null ? object[18].toString() : null);
					employee.setIsTimesheetLockCheckEnable(object[19] != null ? object[19].toString() : null);
					employee.setReportingManagerId(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					employee.setApprovalsTo(object[21] != null ? object[21].toString() : null);
					employee.setReportingManagerName(object[22] != null ? object[22].toString() : null);
					employee.setReportingManagerEmail(object[23] != null ? object[23].toString() : null);
					employee.setProbationPeriod(object[24] != null ? Short.parseShort(object[24].toString()) : null);
					employee.setWorkLocation(object[25] != null ? object[25].toString() : null);
					employee.setMaritalStatus(object[26] != null ? object[26].toString() : null);
					employee.setJobRoleName(object[27] != null ? object[27].toString() : null);
					employee.setIsApmosysProduct(object[28] != null ? object[28].toString() : null)	;
					employee.setJobRoleId(object[29] != null ? Long.parseLong(object[29].toString()) : null);	
					});
				
				//Check if all Policy read.
				List<UploadPolicy> allPolicy = uploadPolicyRepository.findByReadEnabled("true");
				
				if(!allPolicy.isEmpty()) {
					for(UploadPolicy object: allPolicy) {
						PolicyReadResponse readResponse = policyReadResponseRepository
								.findByEmpIdAndPolicyID(employee.getEmpId(), object.getPolicyID());
						
						if(readResponse == null) {
							employee.setPolicyReadConsent(object);
							break;
						}
					}
				}
				
				//Check if all Notification consent given.
				List<Notification> allConsentNotification = notificationRepository
						.findByNotificationTypeAndIsActive("consentNotification", "true");
				
				if(!allConsentNotification.isEmpty()) {
					for(Notification object: allConsentNotification) {
						EmployeeNotificationConsent consentObj = employeeNotificationConsentRepository
								.findByEmpIdAndNotificationId(employee.getEmpId(), object.getNotificationId());
						
						if(consentObj == null) {
							employee.setNotificationConsent(object);
							break;
						}
					}
				}
				
				
				//Check if all Notification consent given.
				List<Notification> allReleaseNotes = notificationRepository
						.findByNotificationTypeAndIsActive("releaseNotes", "true");
				
				if(!allReleaseNotes.isEmpty()) {
					for(Notification object: allReleaseNotes) {
						EmployeeNotificationConsent releaseConsentObj = employeeNotificationConsentRepository
								.findByEmpIdAndNotificationId(employee.getEmpId(), object.getNotificationId());
						
						if(releaseConsentObj == null) {
							employee.setReleaseNoteNotification(object);
							break;
						}
					}
				}
				
				
				//Check if LinkedIn Page notification consent given.
				List<Notification> allLinkedInPageNotifications = notificationRepository
						.findByNotificationTypeAndIsActive("NewLinkedIn page", "true");
				
				if(!allLinkedInPageNotifications.isEmpty()) {
					for(Notification object: allLinkedInPageNotifications) {
						EmployeeNotificationConsent linkedInConsentObj = employeeNotificationConsentRepository
								.findByEmpIdAndNotificationId(employee.getEmpId(), object.getNotificationId());
						
						if(linkedInConsentObj == null) {
							employee.setLinkedinPageNotification(object);
							break;
						}
					}
				}
				
				
				//Check if all Newsletter is read
				List<Newsletter> allNewsletters = newsletterRepository.findAll();

					if (!allNewsletters.isEmpty()) {
						for (Newsletter object : allNewsletters) {
							try {
								NewsletterReadResponse readResponse = newsletterReadResponseRepository
										.findByEmpIdAndDocumentId(employee.getEmpId(), object.getDocumentId());
								if (readResponse == null) {
									employee.setNewsletterReadCheck(object);
									break;
								}
								}catch(Exception e) {
											List<NewsletterReadResponse> res= newsletterReadResponseRepository.findAllByEmpIdAndDocumentId(employee.getEmpId(), object.getDocumentId());
											 System.err.println("Error checking newsletter on login: " + e.getMessage());
											 if (res == null || res.isEmpty()) {
													employee.setNewsletterReadCheck(object);
													break;
												}
										}
							
							
							}
							
						}
				
				
				//Check training lock status and mandatory training requirements
				// This check is critical for routing decisions on login
				List<Long> jobRoleIds = Arrays.stream(trainingJobRoleExclude.split(","))
						.map(String::trim)
						.map(Long::parseLong)
						.collect(Collectors.toList());
						
				Long employeeJobRoleId = employee.getJobRoleId();
				if (employee.getEmpId() != null && employeeJobRoleId != null && !jobRoleIds.contains(employeeJobRoleId)) {
					try {

						ServiceResponse lockResponse = trainingUserService.getLockStatus(employee.getEmpId());
						System.out.println("lockResponse==>  "+lockResponse);
						if (lockResponse != null && lockResponse.getServiceStatus() != null && 
							lockResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) &&
							lockResponse.getServiceResponse() != null) {
							
							LockStatusDTO lockStatus = (LockStatusDTO) lockResponse.getServiceResponse();
							
							
							if (lockStatus.getIsLocked() == null) {
								lockStatus.setIsLocked(false);
							}
							if (lockStatus.getHasMandatoryTrainingPending() == null) {
								lockStatus.setHasMandatoryTrainingPending(false);
							}
							if (lockStatus.getIsHardLock() == null) {
								lockStatus.setIsHardLock(false);
							}
							if (lockStatus.getDeadlineCrossed() == null) {
								lockStatus.setDeadlineCrossed(false);
							}
							
							// Set training lock status for routing and navigation decisions
							// This includes:
							// - hasMandatoryTrainingPending: true if mandatory training exists (for routing)
							// - isLocked: true if lock enabled (for blocking navigation)
							// - isHardLock: true if lock enabled AND deadline crossed (hardest lock)
							// - deadlineCrossed: true if deadline has passed
							employee.setTrainingLockStatus(lockStatus);
							
							// Log lock status for debugging and monitoring
							if (lockStatus.getIsHardLock() != null && lockStatus.getIsHardLock()) {
								System.out.println("Training Lock Status - HARD LOCK: Employee " + employee.getEmpId() + 
									" has deadline-crossed mandatory training with lock enabled. Training: " + 
									lockStatus.getLockedTrainingName());
							} else if (lockStatus.getIsLocked() != null && lockStatus.getIsLocked()) {
								System.out.println("Training Lock Status - LOCKED: Employee " + employee.getEmpId() + 
									" has mandatory training with lock enabled. Training: " + 
									lockStatus.getLockedTrainingName() + 
									", Deadline Crossed: " + lockStatus.getDeadlineCrossed());
							} else if (lockStatus.getHasMandatoryTrainingPending() != null && lockStatus.getHasMandatoryTrainingPending()) {
								System.out.println("Training Lock Status - MANDATORY PENDING: Employee " + employee.getEmpId() + 
									" has mandatory training pending (no lock). Training: " + 
									lockStatus.getLockedTrainingName());
							}
						} else {
							// If lock check returns failure or null, initialize empty lock status
							LockStatusDTO emptyLockStatus = new LockStatusDTO();
							emptyLockStatus.setIsLocked(false);
							emptyLockStatus.setHasMandatoryTrainingPending(false);
							emptyLockStatus.setIsHardLock(false);
							emptyLockStatus.setDeadlineCrossed(false);
							employee.setTrainingLockStatus(emptyLockStatus);
							System.out.println("Training Lock Status - No lock status returned for employee " + employee.getEmpId());
						}
					} catch (Exception e) {
						// If lock check fails, initialize empty lock status to prevent NPE
						// Log error but don't fail login - training lock check should not block login
						LockStatusDTO emptyLockStatus = new LockStatusDTO();
						emptyLockStatus.setIsLocked(false);
						emptyLockStatus.setHasMandatoryTrainingPending(false);
						emptyLockStatus.setIsHardLock(false);
						emptyLockStatus.setDeadlineCrossed(false);
						employee.setTrainingLockStatus(emptyLockStatus);
						
						System.err.println("Error checking training lock on login for employee " + employee.getEmpId() + ": " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					// If empId is null, initialize empty lock status
					LockStatusDTO emptyLockStatus = new LockStatusDTO();
					emptyLockStatus.setIsLocked(false);
					emptyLockStatus.setHasMandatoryTrainingPending(false);
					emptyLockStatus.setIsHardLock(false);
					emptyLockStatus.setDeadlineCrossed(false);
					employee.setTrainingLockStatus(emptyLockStatus);
					System.err.println("Warning: Employee ID is null, cannot check training lock status");
				}
				
				response.setServiceResponse("Employee login info found.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Employee login info found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				System.out.println("Employee => "+employee);
				return employee;
			}

		} catch (Exception e) {
			response.setServiceResponse("Something went wrong.");
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			e.printStackTrace();

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return employee;
	}

	public ServiceResponse getAllEmployeesBirthDayToday() {
		ServiceResponse response = new ServiceResponse();
		EmployeeDTO employee = new EmployeeDTO();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllEmployeesBirthDayToday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();	

		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployeesBirthDayToday();
			logBuilder.append("getAllEmployeesBirthDayToday : "+allEmployeeList.size());	

			if (allEmployeeList != null) {
				List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
				allEmployeeList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					empDTO.setName(object[0] != null ? object[0].toString() : null);
					empDTO.setDepartmentName(object[1] != null ? object[1].toString() : null);
					empDTO.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()): null);
					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is null.");
				apiLogInfo.setApiResponse("Employee List is null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	/** Logged-in employee id from Spring Security (set in {@link com.apmosys.employeeportal.EmployeePortalInterceptor}). */
	private Long getLoggedInEmpIdFromSecurityContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}
		Object p = auth.getPrincipal();
		if (p instanceof Long) {
			return (Long) p;
		}
		if (p instanceof String) {
			try {
				return Long.parseLong((String) p);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	/** HOD, HR-ish departments, or SuperAdmin may use any anchor; others only own reporting subtree. */
	private boolean hasWideHierarchyAnchorAccess(Long empId) {
		if (empId == null) {
			return false;
		}
		List<Department> hodDepts = departmentRepository.findByHodId(empId);
		if (hodDepts != null && !hodDepts.isEmpty()) {
			return true;
		}
		try {
			String role = employeeRepository.getEmployeeRoleByEmpId(empId);
			if (role != null && "SuperAdmin".equalsIgnoreCase(role.trim())) {
				return true;
			}
		} catch (Exception ignored) {
			// ignore
		}
		try {
			String deptName = employeeRepository.getDepartment(empId);
			if (deptName != null) {
				String d = deptName.trim();
				if ("HR".equalsIgnoreCase(d) || "Accounts".equalsIgnoreCase(d)
						|| "Resource Management Group".equalsIgnoreCase(d)) {
					return true;
				}
			}
		} catch (Exception ignored) {
			// ignore
		}
		return false;
	}

	/**
	 * True if the caller may request direct reports for {@code anchorEmpId}:
	 * wide org roles (HOD/HR/SuperAdmin), or any anchor in the caller's manager subtree (self + descendants).
	 */
	private boolean isHierarchyAnchorAllowedForCaller(Long callerEmpId, Long anchorEmpId) {
		if (callerEmpId == null || anchorEmpId == null) {
			return false;
		}
		if (hasWideHierarchyAnchorAccess(callerEmpId)) {
			return true;
		}
		BigInteger cnt = employeeRepository.countEmpInManagerReportingSubtree(callerEmpId, anchorEmpId);
		return cnt != null && cnt.compareTo(BigInteger.ZERO) > 0;
	}
	
	public ServiceResponse getHierarchyByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_team");
		apiLogInfo.setApiUrl("/api/getHierarchyByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		try {

			Long callerEmpId = getLoggedInEmpIdFromSecurityContext();
			Long anchorEmpId = employeedto.getEmpId();
			if (anchorEmpId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee id is required");
				apiLogInfo.setApiResponse("Missing anchor empId");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			if (callerEmpId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unauthorized");
				apiLogInfo.setApiResponse("No logged-in employee");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}
			if (!isHierarchyAnchorAllowedForCaller(callerEmpId, anchorEmpId)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Access denied");
				apiLogInfo.setApiResponse("Hierarchy anchor not allowed for this user");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiRequest(logBuilder.toString());
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}

			List<Object[]> list = employeeRepository.getHierarchyByEmpId(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Hierarchy found");
				
				apiLogInfo.setApiResponse("No Hierarchy found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				
				// Date Range to Check Timesheet
				int currentYear = LocalDate.now().getYear();
				int currentMonth = LocalDate.now().getMonthValue();
				
				LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
				LocalDate end = LocalDate.now().minusDays(1);
				
				Long period = ChronoUnit.DAYS.between(firstOfMonth, end) + 1;
				
				// Get Filled EOD Count for Team Members
				List<Object[]> timesheetList = employeeTimesheetsNewRepository.getMyTeamsFilledEodCountByManagerId(firstOfMonth, end, employeedto.getEmpId());

				list.forEach((object) -> {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setName(object[1] != null ? object[1].toString() : null);
					dto.setEmail(object[2] != null ? object[2].toString() : null);
					dto.setJobRoleName(object[3] != null ? object[3].toString() : null);
					dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					dto.setManagerName(object[5] != null ? object[5].toString() : null);
					dto.setEmployeementId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					dto.setInvalidAccessAttempt(object[7] != null ? Integer.parseInt(object[7].toString()) : null);
					dto.setIsTimesheetLockCheckEnable(object[8] != null ? object[8].toString() : null);
					dto.setPipFlag(object[9] != null ? object[9].toString() : null);	
					dto.setIsConsultant(object[10] != null ? object[10].toString() : null);			
					dto.setIsApprenticeship(object[11] != null ? object[11].toString() : null);
					
					timesheetList.forEach((timesheet) -> {

						Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
						Long employeeEmpId = object[0] != null ? Long.parseLong(object[0].toString()) : null;

						if (timesheetEmpId.equals(employeeEmpId)) {
							Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;
							Long pendingEodCount = period - filledEodCount;

							if(pendingEodCount >= 3) {
								dto.setTimesheetStatus("Defaulter");
							}else if (pendingEodCount > 0 && pendingEodCount < 3) {
								dto.setTimesheetStatus("Pending Timesheet(s)");
							}else {
								dto.setTimesheetStatus("Timesheets upto date");
							}
						}
					});
					
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList : " +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	
	
	// Here we will get Manager, Co-worker, Reportee's
	public ServiceResponse getHierarchyChartByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_team");
		apiLogInfo.setApiUrl("/api/getHierarchyChartByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		try {

			List<Object[]> managerInfoList = employeeRepository.getMyManagerInfo(employeedto.getManagerId());
			List<Object[]> coWorkerList = employeeRepository.getMyReporteeInfo(employeedto.getManagerId());
			List<Object[]> reporteeList = employeeRepository.getMyReporteeInfo(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			
			if(!managerInfoList.isEmpty()) {
				managerInfoList.forEach((object) -> {
					EmployeeDTO managerObj = new EmployeeDTO();
					
					managerObj.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					managerObj.setName(object[1] != null ? object[1].toString() : null);
					managerObj.setEmail(object[2] != null ? object[2].toString() : null);
					managerObj.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					managerObj.setJobRoleName(object[4] != null ? object[4].toString() : null);
					managerObj.setDepartmentName(object[5] != null ? object[5].toString() : null);
					managerObj.setManagerName(object[6] != null ? object[6].toString() : null);
					managerObj.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					managerObj.setReporteeCount(object[8] != null ? Integer.parseInt(object[8].toString()) : null);
					managerObj.setIsConsultant(object[9] != null ? object[9].toString() : null);
					managerObj.setIsApprenticeship(object[10] != null ? object[10].toString() : null);
					managerObj.setHierarchyType("Manager");
					
					dtoList.add(managerObj);
				});
			}
			
			if(!coWorkerList.isEmpty()) {
				coWorkerList.forEach((object) -> {
					EmployeeDTO coWorker = new EmployeeDTO();
					
					coWorker.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					coWorker.setName(object[1] != null ? object[1].toString() : null);
					coWorker.setEmail(object[2] != null ? object[2].toString() : null);
					coWorker.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					coWorker.setJobRoleName(object[4] != null ? object[4].toString() : null);
					coWorker.setDepartmentName(object[5] != null ? object[5].toString() : null);
					coWorker.setManagerName(object[6] != null ? object[6].toString() : null);
					coWorker.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					coWorker.setReporteeCount(object[8] != null ? Integer.parseInt(object[8].toString()) : null);
					coWorker.setIsConsultant(object[9] != null ? object[9].toString() : null);
					coWorker.setIsApprenticeship(object[10] != null ? object[10].toString() : null);
					if(coWorker.getEmpId().equals(employeedto.getEmpId())) {
						coWorker.setHierarchyType("Self");
					}else {
						coWorker.setHierarchyType("Co-Worker");						
					}
					
					dtoList.add(coWorker);
				});
			}
			
			
			if(!reporteeList.isEmpty()) {
				reporteeList.forEach((object) -> {
					EmployeeDTO reportee = new EmployeeDTO();
					
					reportee.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					reportee.setName(object[1] != null ? object[1].toString() : null);
					reportee.setEmail(object[2] != null ? object[2].toString() : null);
					reportee.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					reportee.setJobRoleName(object[4] != null ? object[4].toString() : null);
					reportee.setDepartmentName(object[5] != null ? object[5].toString() : null);
					reportee.setManagerName(object[6] != null ? object[6].toString() : null);
					reportee.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					reportee.setReporteeCount(object[8] != null ? Integer.parseInt(object[8].toString()) : null);
					reportee.setIsConsultant(object[9] != null ? object[9].toString() : null);
					reportee.setIsApprenticeship(object[10] != null ? object[10].toString() : null);
					reportee.setHierarchyType("Reportee");
					
					dtoList.add(reportee);
				});
			}
			
			if (dtoList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Hierarchy found");
				
				apiLogInfo.setApiResponse("No Hierarchy found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList : " +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}


	public ServiceResponse revokeAccount(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/revokeAccount");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeedto.getEmpId());
		try {
			Employee employee = employeeRepository.getById(employeedto.getEmpId());
			employee.setInvalidAccessAttempt(0);
			employeeRepository.save(employee);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Account is Unblock !!");
			apiLogInfo.setApiResponse("Account is Unblock !!");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getEmployees() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_employees");
		apiLogInfo.setApiUrl("/api/getEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> allEmployees = employeeRepository.getEmployees();
			logBuilder.append("List fetched : "+allEmployees.size());
			List<EmployeeDTO> empDTO = new ArrayList<>();
			if (!allEmployees.isEmpty()) {
				for (Object[] obj : allEmployees) {
					EmployeeDTO employeeDTO = new EmployeeDTO();
					employeeDTO.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
					employeeDTO.setEmployeementId(obj[1] != null ? Long.parseLong(obj[1].toString()) : null);
					employeeDTO.setManagerName(obj[2] != null ? obj[2].toString() : null);
					employeeDTO.setName(obj[3] != null ? obj[3].toString() : null);
					employeeDTO.setEmail(obj[4] != null ? obj[4].toString() : null);
					employeeDTO.setManagerMail(obj[5] != null ? obj[5].toString() : null);
					employeeDTO.setEmploymentstatus(obj[6] != null ? obj[6].toString() : null);
					empDTO.add(employeeDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(empDTO);
				apiLogInfo.setApiResponse("Employee list fetched.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is empty.");
				apiLogInfo.setApiResponse("Employee List is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllManagers() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllManagers");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> allEmployees = employeeRepository.getAllManagers();	
		logBuilder.append("getAllManagers size : "+allEmployees.size());		

			Optional.ofNullable(allEmployees).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No employees found. List is empty.");
					apiLogInfo.setApiResponse("No employees found. List is empty.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<EmployeeDTO> empDTO = new ArrayList<>();

					list.forEach((object) -> {
						
						EmployeeDTO dto = new EmployeeDTO();
						dto.setManagerId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						empDTO.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(empDTO);
					apiLogInfo.setApiResponse("Manager list fetched.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No employees found. List is null.");
				apiLogInfo.setApiResponse("No employees found. List is null.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
//	employee working history by anurag
	
	public ServiceResponse findEmployeeWorkingHistory(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_all_employee");
		apiLogInfo.setApiUrl("/api/findEmployeeWorkingHistory");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeeDto.getEmpId());
		try {
			List<Object[]> workingHistory = employeeRepository.findEmployeeWorkingHistory(employeeDto.getEmpId());
			System.out.println(" employeeDto.getEmpId() : ------------" +employeeDto.getEmpId());
			List<EmployeeDTO> historyList = new ArrayList<EmployeeDTO>();
			if (workingHistory.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("list is empty !!");
			} else {
				workingHistory.forEach((object) -> {
					EmployeeDTO emplDto = new EmployeeDTO();
					emplDto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					emplDto.setName(object[1] != null ? object[1].toString() : null);
					emplDto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					emplDto.setTeamName(object[3] != null ? object[3].toString() : null);
					emplDto.setTeamLeadId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
					emplDto.setProjectId(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
					emplDto.setProjectName(object[6] != null ? object[6].toString() : null);
					emplDto.setStartDate(object[7] != null ? object[7].toString() : null);
					emplDto.setUpdatedOn(object[8] != null ? object[8].toString() : null);
					emplDto.setTeamLeadName(object[9] != null ? object[9].toString() : null);
					emplDto.setDateOfJoining(object[10] != null ? object[10].toString() : null);
//					emplDto.setDateOfResign(object[11] != null ? object[11].toString() : null);
//					emplDto.setRole(object[12] != null ? object[12].toString() : null);
					emplDto.setJobRoleName(object[11] != null ? object[11].toString() : null);
//					emplDto.setStartDate(object[14] != null ? object[14].toString() : null);
					emplDto.setClientLocation(object[12] != null ? object[12].toString() : null);
					emplDto.setClientName(object[13] != null ? object[13].toString() : null); 
					emplDto.setDateOfResign(object[14] != null ? object[14].toString() : null);
					historyList.add(emplDto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(historyList);
				System.out.println("\n "+ "history ------" +historyList);

				apiLogInfo.setApiResponse("historyList : " +historyList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;

	}
	
	public ServiceResponse getEmployeeProfileCompletion(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();
		DecimalFormat df = new DecimalFormat("0.00");
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Home_Page");
		apiLogInfo.setApiUrl("/api/getEmployeeProfileCompletion");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeeDto.getEmpId());
		
		Double proileCompleted = 0.00;
		Double totalFields = 0.00;
//		System.err.println("check details \n");
//		System.err.println(" \n"+employeeDto);
		try {
			List<Object[]> employeeProile = employeeRepository.getEmployeeProfileCompletion(employeeDto.getEmpId());
			
			
			if (employeeProile.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Profile not found !!");
			} else {
				Object[] employee = employeeProile.get(0);
				totalFields = (double) employee.length;
						
				for(int i = 0; i < employee.length; i++) {
					if(employee[i] != null) {
						proileCompleted++;
					}
				}
				
				Double profileCompletedPercent = (proileCompleted/totalFields)*100;
				
				employeeDto.setProfileCompletedPercent(Double.parseDouble(df.format(profileCompletedPercent)));
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(employeeDto);
				

				apiLogInfo.setApiResponse("historyList : ");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;

	}

	public ServiceResponse encryptPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/encryptPassword");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		
		try {
			
			System.out.println(employeedto.getEmployeementId() + " : employeementy id");
			Optional<Employee> employeeObject = Optional
					.ofNullable(employeeRepository.findByEmployeementId(employeedto.getEmployeementId()));
			
			if (employeeObject.isPresent()) {
				
				Employee employee = employeeObject.get();
				String encryptedPassword = EncryptDecrypt.encrypt(employeedto.getPassword());

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setPassword(encryptedPassword);
				
				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Password Updated.");
					apiLogInfo.setApiResponse("Employee Password Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Password Updation Failed.");
					apiLogInfo.setApiResponse("Employee Password Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not found");
				apiLogInfo.setApiResponse("Employee Profile Not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse sendMailByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/sendMailByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeedto.getEmployeementId() + " : employeement id");
		
		try {
			
			System.out.println(employeedto.getEmployeementId() + " : employeement id");
			Optional<Employee> employeeObject = Optional
					.ofNullable(employeeRepository.findByEmployeementId(employeedto.getEmployeementId()));
			
			if (employeeObject.isPresent()) {
				
				Employee employee = employeeObject.get();
				
				mailService.sendMail(employee.getEmail(), "Regarding new Employee Portal *IShine*",
						"Dear ApMoSysian,<br>"
						+ "<br>"
						+ "Welcome to iShine Portal, Please login using your old leave portal credentials.<br>"
						+ "Please complete updation of your profile before end of this week and start using this portal for leave and timesheet applications.<br>"
						+ "<br>"
						+ "links - <br>"
						+ "portal link : https://ishine.apmosys.com/ <br>"
						+ "tutorial : https://share.apmosys.com/index.php/s/WbgeWcvwzj0ZWxI");
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New Portal Credentials Mail sent successfully");
				apiLogInfo.setApiResponse("New Portal Credentials Mail sent successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				System.out.println(employeedto.getEmployeementId() + "  ===");
				response.setServiceResponse("Employee Profile Not found");
				apiLogInfo.setApiResponse("Employee Profile Not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse addDemographicsInfo(Map<String, Object> payload) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("add_demographic_info");
	    apiLogInfo.setApiUrl("/api/addDemographicsInfo");
	    apiLogInfo.setLogLevel("INFO");
	    apiLogInfo.setApiRequest("pincode: " + payload);

	    try {
	        // --- Validation ---
	    	 Integer pincode = (Integer) payload.get("pincode");
	         apiLogInfo.setApiRequest("pincode: " + pincode);

	         // --- Validation ---
	         if (pincode == null) {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("Pincode cannot be empty");
	             apiLogInfo.setApiResponse("Pincode cannot be empty");
	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             logService.logMyInfo(httpRequest, apiLogInfo);
	             return response;
	         }

	        // --- Setup RestTemplate with timeouts ---
	       
	        RestTemplate restTemplate = new RestTemplate();

	        String url = "https://api.postalpincode.in/pincode/" + pincode;

	        // --- Retry Logic (max 3 times) ---
	        int maxRetries = 3;
	        ResponseEntity<List<Map<String, Object>>> apiResponse = null;
	        for (int attempt = 1; attempt <= maxRetries; attempt++) {
	            try {
	                apiResponse = restTemplate.exchange(
	                    url,
	                    HttpMethod.GET,
	                    null,
	                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
	                );
	                break; // success → exit loop
	            } catch (ResourceAccessException e) {
	                if (attempt == maxRetries) throw e; // last try → rethrow
	                Thread.sleep(1000 * attempt); // backoff (1s, 2s, 3s)
	            }
	        }

	        // --- Parse the Response ---
	        if (apiResponse != null &&
	            apiResponse.getStatusCode() == HttpStatus.OK &&
	            apiResponse.getBody() != null &&
	            !apiResponse.getBody().isEmpty()) {

	            Map<String, Object> data = apiResponse.getBody().get(0);
	            String status = (String) data.get("Status");

	            if ("Success".equalsIgnoreCase(status)) {
	                List<Map<String, Object>> postOffices = (List<Map<String, Object>>) data.get("PostOffice");

	                if (postOffices != null && !postOffices.isEmpty()) {
	                    // take first entry (main city)
	                    Map<String, Object> details = postOffices.get(0);

	                    Map<String, Object> result = new HashMap<>();
	                    result.put("city", details.getOrDefault("District", ""));
	                    result.put("state", details.getOrDefault("State", ""));
	                    result.put("country", details.getOrDefault("Country", ""));

	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse(result);
	                    apiLogInfo.setApiResponse("Fetched demographic details successfully");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("No post office data found for this pincode");
	                    apiLogInfo.setApiResponse("No post office data found for this pincode");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                }
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Invalid Pincode");
	                apiLogInfo.setApiResponse("Invalid Pincode");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            }
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to fetch pincode details");
	            apiLogInfo.setApiResponse("Unable to fetch pincode details");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while fetching pincode details");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        e.printStackTrace();
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getAllEmployeeInfo() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = createGetAllEmployeeInfoLogDto();
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(
					poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					OPERATION_GET_ALL_EMPLOYEE_INFO,
					PO_PORTAL_LOG_SOURCE,
					null,
					httpRequest);

			List<PoPortalDTO> poPortalDTOList = Optional
					.ofNullable(employeeRepository.getAllEmployeeInfoForPoPortal())
					.orElseGet(Collections::emptyList);

			if (poPortalDTOList.isEmpty()) {
				applyGetAllEmployeeInfoNotFound(response, apiLogInfo);
				finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
				logger.info("[{}] completed: notFound, employeeCount=0, path={}",
						OPERATION_GET_ALL_EMPLOYEE_INFO, sourceSystem);
			} else {
				applyGetAllEmployeeInfoSuccess(response, apiLogInfo, poPortalDTOList);
				finalHttpStatusCode = HttpStatus.OK.value();
				logger.info("[{}] completed: success, employeeCount={}, path={}",
						OPERATION_GET_ALL_EMPLOYEE_INFO, poPortalDTOList.size(), sourceSystem);
			}
		} catch (Exception e) {
			logger.error("[{}] failed: path={}, error={}", OPERATION_GET_ALL_EMPLOYEE_INFO, sourceSystem,
					e.getMessage(), e);
			applyGetAllEmployeeInfoSystemError(response, apiLogInfo, e);
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
			apiLogInfo.setApiRequest(buildGetAllEmployeeInfoApiRequestSummary(sourceSystem));
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
	}

	private static LogDTO createGetAllEmployeeInfoLogDto() {
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName(SUBFEATURE_GET_ALL_EMPLOYEE_INFO);
		apiLogInfo.setApiUrl(API_GET_ALL_EMPLOYEE_INFO);
		apiLogInfo.setLogLevel(LOG_LEVEL_INFO);
		return apiLogInfo;
	}

	private static String buildRequestPathForLogging(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		return (query != null && !query.isEmpty()) ? uri + "?" + query : uri;
	}

	private static String buildGetAllEmployeeInfoApiRequestSummary(String path) {
		return OPERATION_GET_ALL_EMPLOYEE_INFO + "; path=" + path;
	}

	private static void applyGetAllEmployeeInfoSuccess(ServiceResponse response, LogDTO apiLogInfo,
			List<PoPortalDTO> poPortalDTOList) {
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(poPortalDTOList);
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		apiLogInfo.setApiResponse("List fetched of size: " + poPortalDTOList.size());
	}

	private static void applyGetAllEmployeeInfoNotFound(ServiceResponse response, LogDTO apiLogInfo) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(MSG_EMPLOYEE_INFO_NOT_FOUND);
		apiLogInfo.setApiResponse(MSG_EMPLOYEE_INFO_NOT_FOUND);
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	}

	private static void applyGetAllEmployeeInfoSystemError(ServiceResponse response, LogDTO apiLogInfo, Exception e) {
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse(MSG_SYSTEM_ERROR);
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel(LOG_LEVEL_ERROR);
		apiLogInfo.setApiResponse("Exception in " + OPERATION_GET_ALL_EMPLOYEE_INFO + ": " + e.getMessage());
		response.setServiceError(e.getMessage());
	}
	
	public ServiceResponse updateLeaveBalanceList(EmployeeDTO employeedto) {	
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_leave_balance_list");
		apiLogInfo.setApiUrl("/api/updateLeaveBalanceList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeedto.getEmpId());
		
		Float balance = (float) 14;	
		try {	
			Employee EmployeementId = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());	
				
			List<EmployeeLeavesMap> leaveBalance =employeeLeavesMapRepository.findAllByEmpId(EmployeementId.getEmpId());	
				
			if(leaveBalance.isEmpty()) {	
				System.out.println("in if block");				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);				
				response.setServiceResponse("No Leaves found.");	
					
			}else {	
				for(EmployeeLeavesMap l : leaveBalance) {	
						
					System.out.println(l.getLeaveTypeMasterId() + " leave type id");	
					if(l.getLeaveTypeMasterId() == 1) {	
						l.setBalance(balance);	
					}	
						
					EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(l);				
					System.out.println(dbResponse + " dp response");	
						
						
					if(dbResponse != null) {				
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);						
						response.setServiceResponse("leave balance Updated.");		
						apiLogInfo.setApiResponse("leave balance Updated.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {				
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);						
						response.setServiceResponse("leave balance Updation Failed.");	
						apiLogInfo.setApiResponse("leave balance Updation Failed.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);						
					}		
				}	
			}	
	
		} catch (Exception e) {	
			e.printStackTrace();	
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);	
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());	
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return null;
	}
		
	public ServiceResponse updateTimesheetLockCheck(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_timesheet_lock_check");
		apiLogInfo.setApiUrl("/api/getEmployeeProfileCompletion");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeeDto.getEmpId());

		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeeDto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employee = employeeObject.get();

				employee.setIsTimesheetLockCheckEnable(employeeDto.getIsTimesheetLockCheckEnable());
				employee.setUpdatedBy(Integer.parseInt(employeeDto.getUpdatedBy().toString()));
				employee.setTimesheetLockUpdatedOn(LocalDate.now());
				
				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					if(dbResponse.getIsTimesheetLockCheckEnable().equals("true")) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet Check Enabled.");
						
						apiLogInfo.setApiResponse("Timesheet Check Enabled.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Timesheet Check Disabled.");
						
						apiLogInfo.setApiResponse("Timesheet Check Disabled, It will enabled automatically in "+ timesheetReconcileDays +" day(s) if not enabled");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Timesheet Check Updation Failed.");

					apiLogInfo.setApiResponse("Timesheet Check Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Not Found");

				apiLogInfo.setApiResponse("Employee Not Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;

	}
	
	public ServiceResponse getEmployeeBasicInfo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getEmployeeBasicInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeedto.getEmpId());

		try {
			if(employeedto.getEmail() != null) {
				EmployeeDTO employeeInfo = getEmployeeInfoOnLogin(employeedto.getEmail());

				if (employeeInfo != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(employeeInfo);
					apiLogInfo.setApiResponse("employeeInfo fetched.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Info not found");
					apiLogInfo.setApiResponse("Employee Info not found");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);			}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee email not found");
				apiLogInfo.setApiResponse("Employee email not found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getEmployeeAuditInfo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getEmployeeAuditInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeedto.getEmpId());
		
		try {
			Map<String,Object> auditList = new HashMap<String,Object>();
			Map<String,Object> teamAuditHistory = new HashMap<String,Object>();
			
			List<EmployeeDTO> employeeDtoList = new ArrayList<>();
			List<TeamDTO> teamDtoList = new ArrayList<TeamDTO>();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			StringBuilder employeeInfoQuery = new StringBuilder("SELECT e.emp_id, e.aadhar , e.about_me,e.address,e.bank_account_no,e.bankifsccode,e.bank_name,\n"
					+ "e.blood_group,e.city,e.country,e.date_of_birth,e.date_of_joining,e.email,e.emergency_contact_mobile,\n"
					+ "e.employmentstatus,e.esic_number,e.father_name,e.gender,e.graduation_type,e.pursuing,e.landline,e.marital_status\n"
					+ ",e.mobile_no,e.mother_tongue,e.name,e.notice_period,e.alternate_mobile_no,e.pan_number,e.passport_number,\n"
					+ "e.permanent_address,e.pf_account_number,e.pincode,e.place_of_birth,e.passing_grade,\n"
					+ "e.previous_pf_account_number,e.relation,e.state,e.uan,e.views_on_organisation,e.year_of_passing,\n"
					+ "e.pincode as p,e.emergency_contact_person,e.profile_image_name,\n"
					+ "em.name as manager, j.name as jobrole , d.name as department , d.dept_id , j.job_role_id ,e.manager_id, e.work_location,\n"
					+ "e.experience, e.role, e.employeement_id, e.probation_period, e.date_of_resign,\n"
					+ "e.billable,e.child1,e.child2,e.child3,e.mothers_name,e.spouse,e.total_experience , e.secondary_email,e.date_of_relieving, e.reporting_manager_id,\n"
					+ "e.approvals_to,rm.name as reportingManager,e.designation_id,de.designation_name, e.created_on,e.updated_by, e.created_by, createdBy.name as createdByName, updatedBy.name as updateByName\n"
					+ "FROM employee_aud e\n"
					+ "INNER JOIN employee em ON e.manager_id = em.emp_id\n"
					+ "INNER JOIN job_role j ON j.job_role_id = e.job_role_id\n"
					+ "INNER JOIN department d ON d.dept_id = j.dept_id\n"
					+ "LEFT JOIN employee rm ON e.reporting_manager_id = rm.emp_id\n"
					+ "LEFT JOIN designation de ON de.designation_id = e.designation_id\n"
					+ "LEFT JOIN employee createdBy ON e.created_by = createdBy.emp_id\n"
					+ "LEFT JOIN employee updatedBy ON e.updated_by = updatedBy.emp_id\n"
					+ "WHERE e.emp_id ="+employeedto.getEmpId()+"\n"
					+ "order by e.created_on");
			
			List<Object[]> employeeAudit = auditCustomRepository.readAuditCustomNativeQuery(employeeInfoQuery.toString());
			
			if(!employeeAudit.isEmpty()) {
				for (Object[] object : employeeAudit) {
					EmployeeDTO empDTO = new EmployeeDTO();
					
					empDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
					empDTO.setCity(object[8] != null ? object[8].toString() : null);
					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
					empDTO.setDateOfBirth(
							object[10] != null ? format.format(format.parse(object[10].toString())) : null);
					empDTO.setDateOfJoining(
							object[11] != null ? format.format(format.parse(object[11].toString())) : null);
					empDTO.setEmail(object[12] != null ? object[12].toString() : null);
					empDTO.setEmergencyContactMobile(object[13] != null ? Long.parseLong(object[13].toString()) : null);
					empDTO.setEmploymentstatus(object[14] != null ? object[14].toString() : null);
					empDTO.setEsicNumber(object[15] != null ? object[15].toString() : null);
					empDTO.setFatherName(object[16] != null ? object[16].toString() : null);
					empDTO.setGender(object[17] != null ? object[17].toString() : null);
					empDTO.setGraduationType(object[18] != null ? object[18].toString() : null);
					empDTO.setPursuing(object[19] != null ? object[19].toString() : null);
					empDTO.setLandline(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					empDTO.setMaritalStatus(object[21] != null ? object[21].toString() : null);
					empDTO.setMobileNo(object[22] != null ? Long.parseLong(object[22].toString()) : null);
					empDTO.setMotherTongue(object[23] != null ? object[23].toString() : null);
					empDTO.setName(object[24] != null ? object[24].toString() : null);
					empDTO.setNoticePeriod(object[25] != null ? Short.parseShort(object[25].toString()) : null);
					empDTO.setAlternateMobileNo(object[26] != null ? Long.parseLong(object[26].toString()) : null);
					empDTO.setPanNumber(object[27] != null ? object[27].toString() : null);
					empDTO.setPassportNumber(object[28] != null ? object[28].toString() : null);
					empDTO.setPermanentAddress(object[29] != null ? object[29].toString() : null);
					empDTO.setPfAccountNumber(object[30] != null ? object[30].toString() : null);
					empDTO.setPincode(object[31] != null ? Integer.parseInt(object[31].toString()) : null);
					empDTO.setPlaceOfBirth(object[32] != null ? object[32].toString() : null);
					empDTO.setPassingGrade(object[33] != null ? object[33].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[34] != null ? object[34].toString() : null);
					empDTO.setRelation(object[35] != null ? object[35].toString() : null);
					empDTO.setState(object[36] != null ? object[36].toString() : null);
					empDTO.setUan(object[37] != null ? object[37].toString() : null);
					empDTO.setViewsOnOrganisation(object[38] != null ? object[38].toString() : null);
					empDTO.setYearOfPassing(object[39] != null ? Short.parseShort(object[39].toString()) : null);
					empDTO.setEmergencyContactPerson(object[41] != null ? object[41].toString() : null);
					empDTO.setManagerName(object[43] != null ? object[43].toString() : null);
					empDTO.setJobRoleName(object[44] != null ? object[44].toString() : null);
					empDTO.setDepartmentName(object[45] != null ? object[45].toString() : null);
					empDTO.setDepartmentId(object[46] != null ? Long.parseLong(object[46].toString()) : null);
					empDTO.setJobRoleId(object[47] != null ? Long.parseLong(object[47].toString()) : null);
					empDTO.setManagerId(object[48] != null ? Long.parseLong(object[48].toString()) : null);
					empDTO.setWorkLocation(object[49] != null ? (object[49].toString()) : null);
					empDTO.setExperience(object[50] != null ? (object[50].toString()) : null);
					empDTO.setRole(object[51] != null ? (object[51].toString()) : null);
					empDTO.setEmployeementId(object[52] != null ? Long.parseLong(object[52].toString()) : null);
					empDTO.setProbationPeriod(object[53] != null ? Short.parseShort(object[53].toString()) : null);
					empDTO.setDateOfResign(
							object[54] != null ? format.format(format.parse(object[54].toString())) : null);
					empDTO.setBillable(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild1(object[56] != null ? (object[56].toString()) : null);
					empDTO.setChild2(object[57] != null ? (object[57].toString()) : null);
					empDTO.setChild3(object[58] != null ? (object[58].toString()) : null);
					empDTO.setMothersName(object[59] != null ? (object[59].toString()) : null);
					empDTO.setSpouse(object[60] != null ? (object[60].toString()) : null);
					empDTO.setTotalExperience(object[61] != null ? Float.parseFloat(object[61].toString()) : null);
					empDTO.setSecondaryEmail(object[62] != null ? object[62].toString() : null);
					empDTO.setDateOfRelieving(object[63] != null ? format.format(format.parse(object[63].toString())) : null);
					empDTO.setReportingManagerId(object[64] != null ? Long.parseLong(object[64].toString()) : null);
					empDTO.setApprovalsTo(object[65] != null ? object[65].toString() : null);
					empDTO.setReportingManagerName(object[66] != null ? object[66].toString() : null);
					empDTO.setDesignationId(object[67] != null ? Long.parseLong(object[67].toString()) : null);
					empDTO.setDesignationName(object[68] != null ? object[68].toString() : null);
					empDTO.setCreatedOn(
							object[69] != null ? dateTimeFormat.format(dateTimeFormat.parse(object[69].toString())) : null);
					empDTO.setUpdatedBy(object[70] != null ? Long.parseLong(object[70].toString()) : null);
					empDTO.setCreatedBy(object[71] != null ? Integer.parseInt(object[71].toString()) : null);
					empDTO.setCreatedByName(object[72] != null ? object[72].toString() : null);
					empDTO.setUpdatedByName(object[73] != null ? object[73].toString() : null);
					
					employeeDtoList.add(empDTO);
			    }
			}
			
			for (int i = 0; i < employeeDtoList.size() - 1; i++) {
			    final int currentIndex = i;
			    if (auditList.isEmpty()) {
			    	employeeDtoList.get(0).setBucketName("Employment Info Changes");
			    	auditList.put(employeeDtoList.get(0).getCreatedOn(), employeeDtoList.get(0));
			    }
			        DiffNode diff = ObjectDifferBuilder.buildDefault().compare(employeeDtoList.get(currentIndex), employeeDtoList.get(currentIndex + 1));
			        if (diff.hasChanges()) {
			            EmployeeDTO empDTO = new EmployeeDTO();
			            diff.visit(new DiffNode.Visitor() {
			                public void node(DiffNode node, Visit visit) {
			                    if (!node.hasChildren()) {
			                        final Object oldValue = node.canonicalGet(employeeDtoList.get(currentIndex));
			                        final Object newValue = node.canonicalGet(employeeDtoList.get(currentIndex + 1));
			                        try {
			                            Field field = EmployeeDTO.class.getDeclaredField(node.getPropertyName());
			                            field.setAccessible(true);
			                            field.set(empDTO, newValue);
			                            
			                            empDTO.setUpdatedBy(employeeDtoList.get(currentIndex + 1).getUpdatedBy());
			                            empDTO.setUpdatedByName(employeeDtoList.get(currentIndex + 1).getUpdatedByName());
			                            
			                            if(node.getPropertyName().equals("name") || node.getPropertyName().equals("managerId")
			                            	    || node.getPropertyName().equals("email")
			                            		|| node.getPropertyName().equals("jobRoleId") || node.getPropertyName().equals("departmentId")
			                            		|| node.getPropertyName().equals("reportingManagerId") || node.getPropertyName().equals("designationId")
			                            		|| node.getPropertyName().equals("approvalsTo") || node.getPropertyName().equals("dateOfBirth")
			                            		|| node.getPropertyName().equals("experience") || node.getPropertyName().equals("mobileNo")
			                            		|| node.getPropertyName().equals("noticePeriod") || node.getPropertyName().equals("probationPeriod")
			                            		|| node.getPropertyName().equals("billable")
			                            		|| node.getPropertyName().equals("dateOfRelieving")) {
			                            	empDTO.setBucketName("Employment Info Changes");
			                            }else if(node.getPropertyName().equals("employmentstatus")) {
			                            	empDTO.setBucketName("Lifecycle Changes");
			                            }else {
			                            	empDTO.setBucketName("KYC Update");
			                            }
			                            
			                        } catch (NoSuchFieldException | IllegalAccessException e) {
			                            e.printStackTrace();
			                        }
			                    }
			                }
			            });
			            auditList.put(employeeDtoList.get(currentIndex + 1).getCreatedOn(), empDTO);
			        }
			}
			
			
			StringBuilder teamInfoQuery = new StringBuilder("SELECT etma.employee_team_map_id, etma.active, etma.emp_id, etma.employee_role, etma.start_date, etma.updated_on, etma.team_id, t.team_name, \n"
					+ " t.team_lead_id, p.project_name, createdBy.name as createdByName, updatedBy.name as updateByName, t.updated_by FROM employee_team_mapping_aud etma \n"
					+ "INNER JOIN teams t ON t.team_id = etma.team_id \n"
					+ "INNER JOIN projects p ON p.project_id = t.project_id \n"
					+ "LEFT JOIN employee createdBy ON t.created_by = createdBy.emp_id \n"
					+ "LEFT JOIN employee updatedBy ON t.updated_by = updatedBy.emp_id \n"
					+ "WHERE etma.emp_id ="+employeedto.getEmpId());
			
			List<Object[]> teamAudit = auditCustomRepository.readAuditCustomNativeQuery(teamInfoQuery.toString());
			
			if(!teamAudit.isEmpty()) {
				for(Object[] teamObject :teamAudit) {
					TeamDTO teamDTO = new TeamDTO();
					
					teamDTO.setEmployeeTeamMapId(teamObject[0] != null ? Long.parseLong(teamObject[0].toString()) : null);
					teamDTO.setActive(teamObject[1] != null ? teamObject[1].toString() : null);
					teamDTO.setEmpId(teamObject[2] != null ? Long.parseLong(teamObject[2].toString()) : null);
					teamDTO.setEmployeeTeamRole(teamObject[3] != null ? teamObject[3].toString() : null);
					teamDTO.setStartDate(teamObject[4] != null ? dateTimeFormat.format(dateTimeFormat.parse(teamObject[4].toString())) : null);
					teamDTO.setUpdatedOn(teamObject[5] != null ? dateTimeFormat.format(dateTimeFormat.parse(teamObject[5].toString())) : null);
					
					teamDTO.setTeamId(teamObject[6] != null ? Long.parseLong(teamObject[6].toString()) : null);
					teamDTO.setTeamName(teamObject[7] != null ? teamObject[7].toString() : null);
					teamDTO.setTeamLeadId(teamObject[8] != null ? Long.parseLong(teamObject[8].toString()) : null);
					teamDTO.setProjectName(teamObject[9] != null ? teamObject[9].toString() : null);
					
					teamDTO.setCreatedByName(teamObject[10] != null ? teamObject[10].toString() : null);
					teamDTO.setUpdatedByName(teamObject[11] != null ? teamObject[11].toString() : null);
					teamDTO.setUpdatedBy(teamObject[12] != null ? Long.parseLong(teamObject[12].toString()) : null);
					
					teamDtoList.add(teamDTO);
				}
			}
			
			if(!teamDtoList.isEmpty()) {
				for (int i = 0; i < teamDtoList.size() - 1; i++) {
				    final int currentIndex = i;
				    
				    if (teamAuditHistory.isEmpty()) {
				    	teamDtoList.get(currentIndex).setBucketName("Team/Project Changes");
				    	teamAuditHistory.put(teamDtoList.get(currentIndex).getStartDate(), teamDtoList.get(currentIndex));
				    }
				    	 DiffNode diff = ObjectDifferBuilder.buildDefault().compare(teamDtoList.get(currentIndex), teamDtoList.get(currentIndex + 1));
					        if (diff.hasChanges()) {
					        	TeamDTO teamDTO = new TeamDTO();
					        	
					            diff.visit(new DiffNode.Visitor() {
					                public void node(DiffNode node, Visit visit) {
					                    if (!node.hasChildren()) {
											final Object oldValue = node.canonicalGet(teamDtoList.get(currentIndex));
					                        final Object newValue = node.canonicalGet(teamDtoList.get(currentIndex + 1));
					                        try {
					                            Field field = TeamDTO.class.getDeclaredField(node.getPropertyName());
					                            field.setAccessible(true);
					                            field.set(teamDTO, newValue);
					                            teamDTO.setBucketName("Team/Project Changes");
					                            
					                            teamDTO.setTeamName(teamDtoList.get(currentIndex + 1).getTeamName());
				                            	teamDTO.setProjectName(teamDtoList.get(currentIndex + 1).getProjectName());
				                            	teamDTO.setUpdatedByName(teamDtoList.get(currentIndex + 1).getUpdatedByName());
				                            	
					                        } catch (NoSuchFieldException | IllegalAccessException e) {
					                            e.printStackTrace();
					                        }
					                    }
					                }
								});
					            teamAuditHistory.put(teamDtoList.get(currentIndex + 1).getStartDate(), teamDTO);
					        }
						
				}
			
			}
			
			auditList.putAll(teamAuditHistory);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(auditList);
			apiLogInfo.setApiResponse("auditList fetched.");			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}


	
	
	public ServiceResponse unlockAllTimesheet(EmployeeDTO employeeDto) {
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("unlockAllTimesheet");
		apiLogInfo.setApiUrl("/api/unlockAllTimesheet");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeeDto.getEmpId());
		
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> employeeObject = null;
			if(employeeDto.getUnlockTimesheetFor().equals("All")) {
				employeeObject = employeeRepository.getEmployeeDetailForCron();
			}else if(employeeDto.getUnlockTimesheetFor().equals("MyTeam")) {
				employeeObject = employeeRepository.getAllTeamView(employeeDto.getManagerId());
			}
			
			if(!employeeObject.isEmpty()) {
				employeeObject.forEach((object) -> {
					Long empId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
					
					if(empId != null) {
						
						Optional<Employee> employeeFound = employeeRepository.findById(empId);
						if (employeeFound.isPresent()) {
							Employee employee = employeeFound.get();

							employee.setIsTimesheetLockCheckEnable(employeeDto.getIsTimesheetLockCheckEnable());
							employee.setUpdatedBy(Integer.parseInt(employeeDto.getUpdatedBy().toString()));
							employee.setTimesheetLockUpdatedOn(LocalDate.now());
							
							Employee dbResponse = employeeRepository.save(employee);

							if (dbResponse != null) {
								if(dbResponse.getIsTimesheetLockCheckEnable().equals("true")) {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Timesheet Check Enabled.");
									
									apiLogInfo.setApiResponse("Timesheet Check Enabled.");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

								}else {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Timesheet Check Disabled.");
									
									apiLogInfo.setApiResponse("Timesheet Check Disabled, It will enabled automatically in "+ timesheetReconcileDays +" day(s) if not enabled");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								}
								
							} else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Timesheet Check Updation Failed.");

								apiLogInfo.setApiResponse("Timesheet Check Updation Failed.");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee Not Found");

							apiLogInfo.setApiResponse("Employee Not Found.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}
				});
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Not Found");

				apiLogInfo.setApiResponse("Employee Not Found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse getAllReporteesByEmpId(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		
//		List<Department> findDepartmentDetailsByHodId = departmentRepository.findByHodId(employeedto.getHodId());
//		if(!findDepartmentDetailsByHodId.isEmpty()) {
//			findDepartmentDetailsByHodId.forEach(department ->{
//				List<Project> findProjects = projectRepository.findProjectByDepartmentName(department.getName());
//				findProjects.forEach(projectObj ->{
//					List<Team> findTeamList = teamRepository.findTeamByProjectId(projectObj.getProjectId());
//					findTeamList.forEach(teamObj->{
//						List<EmployeeTeamMap>  teamMembersByTeamId = employeeTeamMapRepository.findByTeamId(teamObj.getTeamId());
//						System.err.println(" teamMembersByTeamId     @@@@@@@@@@@@@@@@@@####################    "+teamMembersByTeamId.size());
//					});
//				});
//				
//			});
//			
//		}
//		
//		
//		return response;
//	}

	
	
	
	
	public ServiceResponse getTotalNoOfreporties(String empId) {
		ServiceResponse response = new ServiceResponse();
		try {
			Long id = Long.parseLong(empId);
			Employee managerName = employeeRepository.findByEmpId(id);
			Long findManagerCount = employeeRepository.countReportiesByManagerId1(id);
			Long findReportingManagerCount = employeeRepository.countReportiesByReportingManagerId(id);
//			System.err.println("findManagerCount "+findManagerCount);
//			System.err.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% count  "+findManagerCount);
			EmployeeDTO dto = new EmployeeDTO();
			dto.setReporteeCountManager(findManagerCount);
			dto.setReporteeCountReportingManager(findReportingManagerCount);
			System.err.println(" dto    "+dto);
			
			if(dto != null ) {
				response.setServiceResponse(managerName.getName()+ " is the manager of " +dto.getReporteeCountManager()+" and reporting manager of " + dto.getReporteeCountReportingManager()+" reportees.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceResponse("No reportees found !! ");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());	
		}
		
		return response;
	}

	public ServiceResponse getDepartmentByHodId(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		/**
		 * Optional<List<Object>> findDepartments =
		 * departmentRepository.getDepartmentsByHodId(empId); Employee findEmployee =
		 * employeeRepository.findByEmpId(empId);
		 * 
		 * if(findDepartments.isPresent()) { List<Object> listOfDept =
		 * findDepartments.get();
		 * 
		 * if(listOfDept != null) { response.setServiceResponse(listOfDept);
		 * response.setServiceStatus(ServiceResponse.STATUS_SUCCESS); } }else {
		 * response.setServiceResponse(findEmployee.getName()
		 * +" is not HOD of any department ");
		 * response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		 } */
		
		// find list of reporties
		
		List<Object[]> findDepartment = employeeRepository.findDepartmentsByReporties(empId);
		
		List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
		
		findDepartment.forEach(obj ->{
			EmployeeDTO dto = new EmployeeDTO();
			
			dto.setDepartmentName(obj[3] != null ? obj[3].toString() : null);
			dtoList.add(dto);
		});
		
		if(dtoList != null) {
			response.setServiceResponse(dtoList);
			 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		}else {
			response.setServiceResponse("There is no reporties available !!");
	         response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		
		return response;
	}

//	public ServiceResponse getProjectsByDepartmentName(String departmentName) {
//		
//		ServiceResponse response = new ServiceResponse();
//		try {
//			Department findDepartment = departmentRepository.findByName(departmentName);
//			List<ProjectDepartmentMap> findProjectsByDeptId = projectDepartmentMapRepository.findByDeptId(findDepartment.getDeptId());
//			List<Project> allProjects = new ArrayList<Project>();
//			if(!findProjectsByDeptId.isEmpty()){
//				findProjectsByDeptId.forEach((projectObj)->{
//					Project project = projectRepository.findByProjectId(projectObj.getProjectId());
//					System.err.println(" project by department Id   "+project);
//					
//					allProjects.add(project);
//						
//				});	
//				response.setServiceResponse(allProjects);
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				}else {
//					response.setServiceResponse(" Projects are not present in "+ departmentName+" department ");
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Duplicate department name found.");
//			response.setServiceError(e.getMessage());
//		}
//			
//		return response;
//	}
	
public ServiceResponse getProjectsByDepartmentName(EmployeeDTO employeeDto) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			Department findDepartment = departmentRepository.findByName(employeeDto.getDepartmentName());
			List<ProjectDepartmentMap> findProjectsByDeptId = projectDepartmentMapRepository.findByDeptId(findDepartment.getDeptId());
			List<Project> allProjects = new ArrayList<Project>();
			if(!findProjectsByDeptId.isEmpty()){
				findProjectsByDeptId.forEach((projectObj)->{
					Project project = projectRepository.findByProjectIdAndProjectManagerId(projectObj.getProjectId(),employeeDto.getManagerId());
					System.err.println(" project by department Id   "+project);
					if(project != null) {
					allProjects.add(project);
					}
				});	
				response.setServiceResponse(allProjects);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceResponse(" Projects are not present in "+ employeeDto.getDepartmentName()+" department ");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Duplicate department name found.");
			response.setServiceError(e.getMessage());
		}
			
		return response;
	}

	public ServiceResponse getTeamByProjectName(String projectName) {
		ServiceResponse response = new ServiceResponse();
		try {
			Project project = projectRepository.findByProjectName(projectName);
			List<Team> listOfTeams = teamRepository.findTeamByProjectId(project.getProjectId());
			System.out.println(" size   listOfTeams      "+listOfTeams.size());
			if(listOfTeams.size() > 0) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listOfTeams);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team is not present in this project !!");
			}
		} catch (Exception e) {
			
		}
		return response;
	}

//	public ServiceResponse getTeamMemberByTeamName(String teamName) {
//		ServiceResponse response = new ServiceResponse();
//		
//		Team findTeam = teamRepository.findByTeamName(teamName);
//		System.out.println(" team found findTeam  "+findTeam);
//		List<EmployeeDTO> listOfMembers = new ArrayList<>();
//		List<Object[]> listOfEmployees = employeeTeamMapRepository.findTeammembersByTeamId(findTeam.getTeamId());
//
//        if (listOfEmployees != null) {
//            for (Object[] object : listOfEmployees) {
//                EmployeeDTO employeeDto = new EmployeeDTO();
//                employeeDto.setName(object[0] != null ? object[0].toString() : null);
//                employeeDto.setEmployeeRole(object[1] != null ? object[1].toString() : null);
//                employeeDto.setTeamName(object[2] != null ? object[2].toString() : null); 
//                employeeDto.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
//                employeeDto.setManagerName(object[4] != null ? object[4].toString() : null);
//                employeeDto.setManagerId(object[5] != null ? Long.parseLong(object[5].toString()) : null);             
//                
//                listOfMembers.add(employeeDto);
//            }
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse(listOfMembers);
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(listOfMembers);
//			}
//		
//		System.err.println("teamName    listOfEmployees      "+listOfMembers.size());
//		return response;
//	}
	
	public ServiceResponse getTeamMemberByTeamName(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		
		Team findTeam = teamRepository.findByTeamName(employeeDTO.getTeamName());
		System.out.println(" team found findTeam  "+findTeam);
		List<EmployeeDTO> listOfMembers = new ArrayList<>();
		List<Object[]> listOfEmployees = employeeTeamMapRepository.findTeammembersByTeamIdAndManagerId(findTeam.getTeamId(), employeeDTO.getManagerId());
        if (listOfEmployees != null) {
            for (Object[] object : listOfEmployees) {
                EmployeeDTO employeeDto = new EmployeeDTO();
                employeeDto.setName(object[0] != null ? object[0].toString() : null);
                employeeDto.setEmployeeRole(object[1] != null ? object[1].toString() : null);
                employeeDto.setTeamName(object[2] != null ? object[2].toString() : null); 
                employeeDto.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()) : null);      
                
                listOfMembers.add(employeeDto);
            }
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(listOfMembers);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listOfMembers);
			}
		
		System.err.println("teamName    listOfEmployees      "+listOfMembers.size());
		return response;
	}

	public ServiceResponse getManagerList() {
		
		ServiceResponse response = new ServiceResponse();
		List<Object[]> findManagersList = employeeRepository.findManagerListByRole();
		List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
		
		findManagersList.forEach(object ->{
			EmployeeDTO empDto = new EmployeeDTO();
			empDto.setManagerId(object[0] != null ? Long.parseLong(object[0].toString()) : null );
			empDto.setManagerName(object[1] != null ? object[1].toString() : null);
			dtoList.add(empDto);
			});
		
		if(dtoList != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			}
		
		
		return response;
	}

	public ServiceResponse setManagerToNewManager(EmployeeDTO employeeDto) {
		
		ServiceResponse response = new ServiceResponse();
		Long employeeId = employeeDto.getEmpId();
		Long managerId = employeeDto.getManagerId();
		
		Employee findEmployee = employeeRepository.findByEmpId(employeeId);
		
		if(findEmployee != null) {
			findEmployee.setManagerId(employeeDto.getManagerId());
			Employee dbResponse = employeeRepository.save(findEmployee);
			if(dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				
			}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Employee Not found !!  ");
				}
		
		System.err.println("findEmployee   "+findEmployee);
		
		return response;
	}
	
	public ServiceResponse mapLeavesAndCompOffToNewManager(EmployeeDTO employeeDto) {
		
		ServiceResponse response = new ServiceResponse();
		
		Employee findEmployee = employeeRepository.findByEmpId(employeeDto.getEmpId());
		System.out.println("findEmployee  manager  "+findEmployee.getManagerId());
		
		System.out.println(" manager id "+employeeDto.getManagerId());
		
		List<CompOffLeave> findPendingCompOff = compOffLeaveRepository.findPendingCompOffOffByEmpId(findEmployee.getEmpId());
		System.err.println(" findPendingCompOff "+findPendingCompOff.size());
		for (CompOffLeave compOffLeave : findPendingCompOff) {
			compOffLeave.setManagerId(Math.toIntExact(employeeDto.getManagerId()));
			compOffLeaveRepository.save(compOffLeave);
		}
		
		List<EmployeeLeave> findPendingLeaves = employeeLeaveRepository.findLeavesByEmpIdAndStatus(findEmployee.getEmpId());
		System.err.println(" findPendingLeaves "+findPendingLeaves.size());
		for (EmployeeLeave employeeLeave : findPendingLeaves) {
			employeeLeave.setManagerId(Math.toIntExact(employeeDto.getManagerId()));
			employeeLeaveRepository.save(employeeLeave);	
		}
		
		findEmployee.setManagerId(employeeDto.getManagerId());
		Employee dbResponse = employeeRepository.save(findEmployee);
		
		if(dbResponse != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Manager updated");
			
		}
		
	return response;
	}
	
	public ServiceResponse isEmployeeOnBench(EmployeeDTO employeeDto) {

		ServiceResponse response = new ServiceResponse();
		List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

		try {
			StringBuilder query = new StringBuilder("SELECT  e.name, CASE WHEN ea.billable_type = 'Bench' THEN DATEDIFF(CURRENT_DATE, e.onbench_date)\n"
					+ "ELSE NULL END AS number_of_days, ea.billable_type FROM employee e INNER JOIN employee_aud ea \n"
					+ "ON  e.emp_id = ea.emp_id WHERE  e.emp_id = "+ employeeDto.getEmpId()+" ORDER BY  ea.created_on DESC LIMIT 1;");
		
			
			List<Object[]> employeeAudit = auditCustomRepository.isEmployeeOnBenchNativeQuery(query.toString());

			if (!employeeAudit.isEmpty()) {
				for (Object[] object : employeeAudit) {
					
					EmployeeDTO empDTO = new EmployeeDTO();
					empDTO.setName(object[0] != null ? object[0].toString() : null);	
				    empDTO.setDayOnbench(object[1] != null ? object[1].toString() : null);	
					empDTO.setBillableType(object[2] != null ? object[2].toString() : null);
					
					dtoList.add(empDTO);
				}
			}

			
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
		}

		return response;

	}
	
	public ServiceResponse getReporteesListByManagerId(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			List<Object[]> reporteesList = employeeRepository.getReporteesListByManagerId(employeeDTO.getEmpId());
			List<EmployeeDTO> listOfMembers = new ArrayList<EmployeeDTO>();

	        if (reporteesList != null) {
	            for (Object[] object : reporteesList) {
	            	
	                EmployeeDTO employeeDetail = new EmployeeDTO();
	                
	                employeeDetail.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	                employeeDetail.setName(object[1] != null ? object[1].toString() : null);
	                employeeDetail.setDepartmentName(object[2] != null ? object[2].toString() : null);
	                employeeDetail.setManagerId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	                
	                listOfMembers.add(employeeDetail);
	            }
	        }

	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(listOfMembers);
	        
			System.err.println("teamName    listOfEmployees      "+listOfMembers.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
		}
		
		return response;
	}
	
	public ServiceResponse getReporteesListByReportingManagerId(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			List<Object[]> listOfReportees = employeeRepository.getReporteesListByReportingManagerId(employeeDTO.getEmpId());
			List<EmployeeDTO> listOfMembers = new ArrayList<EmployeeDTO>();

	        if (listOfReportees != null) {
	            for (Object[] object : listOfReportees) {

	                EmployeeDTO employeeDetail = new EmployeeDTO();
	                
	                if (employeeDetail != null) {
	                    employeeDetail = new EmployeeDTO();
	                    employeeDetail.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	                    employeeDetail.setName(object[1] != null ? object[1].toString() : null);
	                    employeeDetail.setDepartmentName(object[2] != null ? object[2].toString() : null);
	                    employeeDetail.setManagerId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	                    
	                    listOfMembers.add(employeeDetail);
	                    }
	            }
	        }
	        
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listOfMembers);
				
	        
			System.err.println("teamName    listOfEmployees      "+listOfMembers.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
		}
		
		return response;
	}
	
	public ServiceResponse removeStaleMappingOfInactiveEmployees() {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("One time use api to remove stale mapping of inactive employees");
		apiLogInfo.setApiUrl("/api/removeStaleMappingOfInactiveEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> inactiveEmployees = employeeRepository.removeStaleMappingOfInactiveEmployees();
			if (inactiveEmployees.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("list is empty !!");
			} else {
				inactiveEmployees.forEach((object) -> {
					Long key = object[0] != null ? Long.parseLong(object[0].toString()) : null;
					employeeTeamMapRepository.updateActiveFieldToZero(key);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Removed Stale Mapping of Inactive Employees");

				apiLogInfo.setApiResponse("Removed Stale Mapping of Inactive Employees");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;

	} 
	
	public ServiceResponse setReportingManagerToNewManager(EmployeeDTO employeeDto) {
	
	ServiceResponse response = new ServiceResponse();
	Long employeeId = employeeDto.getEmpId();
	
	Employee findEmployee = employeeRepository.findByEmpId(employeeId);
	
	if(findEmployee != null) {
		findEmployee.setReportingManagerId(employeeDto.getReportingManagerId());
		Employee dbResponse = employeeRepository.save(findEmployee);
		if(dbResponse != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			}
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(" Employee Not found !!  ");
			}
	
	System.err.println("findEmployee   "+findEmployee);
	
	return response;
}
//	public ServiceResponse getRewardsAndAppreciationCount(AppreciationAndRewardsCountDto employeeDto) {
//	      ServiceResponse response = new ServiceResponse();
//			
//			try {
//				
//				List<Object[]> EmployeeRewardsAndAppreciationCount = employeeRepository.getRewardsAndAppreciationCount(employeeDto.getEmpId());
//				List<AppreciationAndRewardsCountDto> listOfRewardsAndAppreciation = new ArrayList<AppreciationAndRewardsCountDto>();
//
//		        if (EmployeeRewardsAndAppreciationCount != null) {
//		            for (Object[] object : EmployeeRewardsAndAppreciationCount) {
//
//		            	AppreciationAndRewardsCountDto employeeDetail = new AppreciationAndRewardsCountDto();
//		            	    employeeDetail.setEmpId(employeeDto.getEmpId());	                    
//		            	    employeeDetail.setAppreciationCount(object[1] != null ? object[1].toString() : null);
//		                    employeeDetail.setRewardsCount(object[0] != null ? object[0].toString() : null);	          
//		                    listOfRewardsAndAppreciation.add(employeeDetail);
//		                    
//		            }
//		        }
//		        
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(listOfRewardsAndAppreciation);
//					
//		        
//			
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//				
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse(e.getMessage());
//			}
//			
//			return response;
//	}


	public ServiceResponse getAllEmployeesByProjectId(Integer projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllEmployeesByProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + projectId);
		try {
			Project projectObj = projectRepository.findByProjectId(projectId);
			if (projectObj == null) {
				throw new RuntimeException("Project Not Found!!");
			}
	
			List<Team> teamList = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
			List<Employee> employeeList = new ArrayList<Employee>();
			if (!teamList.isEmpty()) {
				List<Long> teamIdList = teamList.stream().map(Team::getTeamId).distinct().collect(Collectors.toList());
					List<Long> empIds = employeeTeamMapRepository.findByActiveAndTeamIdIn(teamIdList);
					if (!empIds.isEmpty()) {
						employeeList = employeeRepository.findByEmpIdIn(empIds);
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("No teamMember(s) found in the Team.");
						apiLogInfo.setApiResponse("No teamMember(s) Found in the Team");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				apiLogInfo.setApiResponse("teamListDto :" + employeeList.size());
				response.setServiceResponse(employeeList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No team(s) found in the project.");
				apiLogInfo.setApiResponse("No team(s) found in the project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
	
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse updateDefaultProject(Long empId, String projectId,Long updatedBy) {
		
		ServiceResponse response = new ServiceResponse();
		if (empId == null || projectId == null || projectId.isEmpty()) {
	        response.setServiceMessage("Please provide valid Employee Id and Project Id..!!");
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        return response;
	    }

	    try {
	        
	        DefaultProjectUpdateDTO defaultProjectUpdateDTO = new DefaultProjectUpdateDTO();
	        defaultProjectUpdateDTO.setUpdatedBy(updatedBy);
	        defaultProjectUpdateDTO.setProjectId(Integer.valueOf(projectId));
	        defaultProjectUpdateDTO.setEmpIds(Collections.singletonList(empId));

	       
	        response = resourceManagementService.setDefaultProjectUpdateBillable(defaultProjectUpdateDTO);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceMessage("Exception occurred while updating default project.");
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

	
	
	public ServiceResponse getExpiredPo() {
		ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	    	List<ProjectDTO> expiredPoList = new ArrayList<>();
		      
		        // List<Project> result = projectRepository.getExpiredPolist();
				List<ExpiredProjectDTOForNotification> result = projectRepository.getExpiredPolist();

		    if (result.isEmpty()) {
	            serviceResponse.setServiceResponse("No data found");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            serviceResponse.setServiceResponse(result);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        }
    } catch (Exception e) {
        e.printStackTrace(); 
        serviceResponse.setServiceResponse("Error occurred while fetching data");
        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
    }
    
    return serviceResponse;
	}


	
	
	public ServiceResponse getRewardsAndAppreciationCount(AppreciationAndRewardsCountDto employeeDto) {
      ServiceResponse response = new ServiceResponse();
		
		try {
			
			List<Object[]> EmployeeRewardsAndAppreciationCount = employeeRepository.getRewardsAndAppreciationCount(employeeDto.getEmpId());
			List<AppreciationAndRewardsCountDto> listOfRewardsAndAppreciation = new ArrayList<AppreciationAndRewardsCountDto>();

	        if (EmployeeRewardsAndAppreciationCount != null) {
	            for (Object[] object : EmployeeRewardsAndAppreciationCount) {

	            	AppreciationAndRewardsCountDto employeeDetail = new AppreciationAndRewardsCountDto();
	            	    employeeDetail.setEmpId(employeeDto.getEmpId());	                    
	            	    employeeDetail.setAppreciationCount(object[1] != null ? object[1].toString() : null);
	                    employeeDetail.setRewardsCount(object[0] != null ? object[0].toString() : null);	  
	                    employeeDetail.setAverageRating(object[2]!=null ? object[2].toString() : null);
	                    listOfRewardsAndAppreciation.add(employeeDetail);
	                    
	            }
	        }
	        
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listOfRewardsAndAppreciation);
				
	        
		
			
		} catch (Exception e) {
			e.printStackTrace();
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
		}
		
		return response;
}

	
	public ServiceResponse sendExpiredPoEmail(ExpiredPOMailSendDTO employeeDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
//		businessMail;vpMails;
		System.out.println(employeeDTO.getExpiredData());
		String subject = "PROVIDE INFORMATION REGARDING EXPIRED PROJECT/POs";
		String bodyText = generateEmailBody(employeeDTO.getExpiredData());
		try {
		boolean mailSent = mailService.sendMailWithCC(businessMail,vpMails,subject,bodyText);
		if(!mailSent) {
			serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Unable to send mail...!!");
		}
		else {
			serviceResponse.setServiceStatus(serviceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceMessage("Mail sent successfully...!!");
		}
		}
		catch(Exception e){
			serviceResponse.setErrorStackTrace(e.getMessage());
			serviceResponse.setServiceMessage(serviceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
		}
		
		return serviceResponse;
	}
	
//	public String generateEmailBody(PoObject expiredData) {
//		System.out.println(expiredData);
//	    StringBuilder emailBody = new StringBuilder();
////	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//	    emailBody.append("Hello Business Team,<br><br>");
//	    emailBody.append("The following Projects/POs seems to be expired:<br><br>");
//	    
////	    	String formattedEndDate = dateFormat.format(expiredData.getEndDate());
//	        emailBody.append("<b>PO Number:</b> ").append(expiredData.getPoNo())
//	                 .append(" | <b>Project Name:</b> ").append(expiredData.getProjectName())
//	                 .append(" | <b>PO/Project Type:</b> ").append(expiredData.getPoType())
//	                 .append(" | <b>End Date:</b> ").append(expiredData.getEndDate())
//	                 .append("<br>");
//	    
//	    return emailBody.toString();
//	}
	
	
//	public ServiceResponse getRewardsAndAppreciationCount(AppreciationAndRewardsCountDto employeeDto) {
//      ServiceResponse response = new ServiceResponse();
//		
//		try {
//			
//			List<Object[]> EmployeeRewardsAndAppreciationCount = employeeRepository.getRewardsAndAppreciationCount(employeeDto.getEmpId());
//			List<AppreciationAndRewardsCountDto> listOfRewardsAndAppreciation = new ArrayList<AppreciationAndRewardsCountDto>();
//
//	        if (EmployeeRewardsAndAppreciationCount != null) {
//	            for (Object[] object : EmployeeRewardsAndAppreciationCount) {
//
//	            	AppreciationAndRewardsCountDto employeeDetail = new AppreciationAndRewardsCountDto();
//	            	    employeeDetail.setEmpId(employeeDto.getEmpId());	                    
//	            	    employeeDetail.setAppreciationCount(object[1] != null ? object[1].toString() : null);
//	                    employeeDetail.setRewardsCount(object[0] != null ? object[0].toString() : null);	          
//	                    listOfRewardsAndAppreciation.add(employeeDetail);
//	                    
//	            }
//	        }
//	        
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(listOfRewardsAndAppreciation);
//				
//	        
//		
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse(e.getMessage());
//		}
//		
//		return response;
//}
	
//	public ServiceResponse sendExpiredPoEmail(ExpiredPOMailSendDTO employeeDTO) {
//		ServiceResponse serviceResponse = new ServiceResponse();
//		System.out.println(employeeDTO.getExpiredData());
//		String subject = "PROVIDE INFORMATION REGARDING EXPIRED PROJECT/POs";
//		String bodyText = generateEmailBody(employeeDTO.getExpiredData());
//		try {
//		boolean mailSent = mailService.sendMailWithCC(businessMail,"sakti.das@apmosys.com",subject,bodyText);
//		if(!mailSent) {
//			serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
//			serviceResponse.setServiceMessage("Unable to send mail...!!");
//		}
//		else {
//			serviceResponse.setServiceStatus(serviceResponse.STATUS_SUCCESS);
//			serviceResponse.setServiceMessage("Mail sent successfully...!!");
//		}
//		}
//		catch(Exception e){
//			serviceResponse.setErrorStackTrace(e.getMessage());
//			serviceResponse.setServiceMessage(serviceResponse.SOMETHING_WENT_WRONG);
//			serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
//		}
//		
//		return serviceResponse;
//	}
	
	public String generateEmailBody(PoObject expiredData) {
		System.out.println(expiredData);
	    StringBuilder emailBody = new StringBuilder();
//	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    emailBody.append("Hello Business Team,<br><br>");
	    emailBody.append("The following Projects/POs seems to be expired:<br><br>");
	    
//	    	String formattedEndDate = dateFormat.format(expiredData.getEndDate());
	        emailBody.append("<b>PO Number:</b> ").append(expiredData.getPoNo())
	                 .append(" | <b>Project Name:</b> ").append(expiredData.getProjectName())
	                 .append(" | <b>PO/Project Type:</b> ").append(expiredData.getPoType())
	                 .append(" | <b>End Date:</b> ").append(expiredData.getEndDate())
	                 .append("<br>");
	    
	    return emailBody.toString();
	}
	
	public ServiceResponse getAllEmployeesWorkAnniversaryToday() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllEmployeesWorkAnniversaryToday");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        Optional<List<Object[]>> optionalEmployeeList = employeeRepository.getAllEmployeesWorkAnniversaryToday();
	        List<Object[]> allEmployeeList = optionalEmployeeList.orElse(Collections.emptyList());
	        apiLogInfo.setApiRequest("getAllEmployeesWorkAnniversaryToday: " + allEmployeeList.size());

	        if (!allEmployeeList.isEmpty()) {
	            List<GetAllEmployeesWorkAnniversaryTodayDTO> dtoList = allEmployeeList.stream()
	                .map(object -> {
	                    GetAllEmployeesWorkAnniversaryTodayDTO empDTO = new GetAllEmployeesWorkAnniversaryTodayDTO();
	                    empDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	                    empDTO.setName(object[1] != null ? object[1].toString() : null);
	                    empDTO.setDateOfJoining(object[2] != null ? object[2].toString() : null);
	                    empDTO.setTotalYearsWorked(object[3] != null ? object[3].toString() : null);
	                    empDTO.setEmail(object[4] != null ? object[4].toString() : null);
	                    empDTO.setDepartmentName(object[5] != null ? object[5].toString() : null);
	                    return empDTO;
	                }).collect(Collectors.toList());

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            apiLogInfo.setApiResponse("List fetched of size: " + dtoList.size());
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee list is empty.");
	            apiLogInfo.setApiResponse("Employee list is empty.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An error occurred while processing the request.");
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	
	public ServiceResponse getInternalProjectsAccToDepartmentSelected(DefaultProjectEmployeeConfig defaultProjectEmployeeConfig) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getInternalProjectsAccToDepartment");
		apiLogInfo.setLogLevel("INFO");
		try {
			List<Object[]> getBenchOrOtherProjects = new ArrayList<>();
			Long departmentId = defaultProjectEmployeeConfig.getDepartmentId();
			if ("Bench".equalsIgnoreCase(defaultProjectEmployeeConfig.getDefaultProjectType())) {
				getBenchOrOtherProjects = employeeRepository.getAllInternalBenchprojectsAndTeamDetailsForDepartmenFilter();
			} else {
				getBenchOrOtherProjects = employeeRepository.getAllProjectsThatAreNotBench();
			}
			Map<Integer, ProjectDTO> projectMap = new HashMap<>();

			for (Object[] row : getBenchOrOtherProjects) {
				Integer projectId = TypeConversionUtil.safeParseInt(row[0]);
				String projectName = TypeConversionUtil.getSafeString(row[1]);
				Long teamId = TypeConversionUtil.safeParseLong(row[2]);
				String teamName = TypeConversionUtil.getSafeString(row[3]);
				String deptIds = TypeConversionUtil.getSafeString(row[4]);
				String projectType = TypeConversionUtil.getSafeString(row[5]);
				Date projectStartDate = row[6] != null ? (Date) row[6] : null;

				if (deptIds == null || !Arrays.asList(deptIds.split(",")).contains(departmentId.toString())) {
					continue;
				}

				TeamDTO teamDTO = new TeamDTO();
				teamDTO.setTeamId(teamId);
				teamDTO.setTeamName(teamName);
				teamDTO.setDepartmentList(deptIds.split(","));

				if (projectMap.containsKey(projectId)) {
					projectMap.get(projectId).getTeamList().add(teamDTO);
				} else {
					ProjectDTO projectDTO = new ProjectDTO();
					projectDTO.setProjectId(projectId);
					projectDTO.setProjectName(projectName);
					projectDTO.setProjectType(projectType);
					projectDTO.setProjectStartDate(projectStartDate);
					projectDTO.setTeamList(new ArrayList<>());
					projectDTO.getTeamList().add(teamDTO);
					projectMap.put(projectId, projectDTO);
				}
			}

			List<ProjectDTO> filteredProjects = new ArrayList<>(projectMap.values());
			response.setServiceResponse(filteredProjects);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("List fetched of size: " + filteredProjects.size());
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("An error occurred while processing the request.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	
	public ServiceResponse getAllEmployeesBasedOnUserLogined(List<DepartmentDTO> deapartment) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_all_employee");
		apiLogInfo.setApiUrl("/api/getAllEmployeesBasedOnUserLogined");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		String cacheKey = "allEmployees";
		
		if (employeeCache.containsKey(cacheKey)) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(employeeCache.get(cacheKey));
            apiLogInfo.setApiResponse("Data fetched from cache. Size: " + employeeCache.get(cacheKey).size());
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
        }
		
		try {
			String deptIdCsv = deapartment.stream()
				    .map(DepartmentDTO::getDeptId)     
				    .filter(Objects::nonNull)          
				    .map(String::valueOf)              
				    .collect(Collectors.joining(",")); 
   List<Integer> deptIds = Arrays.stream(deptIdCsv.split(","))
				    .map(String::trim)
				    .map(Integer::parseInt)
				    .collect(Collectors.toList());

			List<Object[]> allEmployeeList = employeeRepository.getAllEmployeesBasedOnUserLogined(deptIds);
			logBuilder.append("getALLEmployees size : "+allEmployeeList.size());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

			if (allEmployeeList != null) {
				allEmployeeList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();

					empDTO.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setAadhar(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					empDTO.setAboutMe(object[2] != null ? object[2].toString() : null);
					empDTO.setAddress(object[3] != null ? object[3].toString() : null);
					empDTO.setBankAccountNo(object[4] != null ? object[4].toString() : null);
					empDTO.setBankIFSCCode(object[5] != null ? object[5].toString() : null);
					empDTO.setBankName(object[6] != null ? object[6].toString() : null);
					empDTO.setBloodGroup(object[7] != null ? object[7].toString() : null);
					empDTO.setCity(object[8] != null ? object[8].toString() : null);
					empDTO.setCountry(object[9] != null ? object[9].toString() : null);
					empDTO.setCreatedBy(object[10] != null ? Integer.parseInt(object[10].toString()) : null);
					empDTO.setCreatedOn(object[11] != null ? (object[11].toString()) : null);
					empDTO.setDateOfBirth(
							object[12] != null ? stringToDateTimeParser.formatDateToString(object[12].toString())
									: null);
					empDTO.setDateOfJoining(
							object[13] != null ? stringToDateTimeParser.formatDateToString(object[13].toString())
									: null);
					empDTO.setEmail(object[14] != null ? object[14].toString() : null);
					empDTO.setEmergencyContactMobile(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					empDTO.setEmergencyContactPerson(object[16] != null ? object[16].toString() : null);
					empDTO.setEmploymentstatus(object[17] != null ? object[17].toString() : null);
					empDTO.setEsicNumber(object[18] != null ? object[18].toString() : null);
					empDTO.setFatherName(object[19] != null ? object[19].toString() : null);
					empDTO.setGender(object[20] != null ? object[20].toString() : null);
					empDTO.setGraduationType(object[21] != null ? object[21].toString() : null);
					empDTO.setPursuing(object[22] != null ? object[22].toString() : null);
					empDTO.setJobRoleId(object[23] != null ? Long.parseLong(object[23].toString()) : null);
					empDTO.setLandline(object[24] != null ? Long.parseLong(object[24].toString()) : null);
					empDTO.setManagerId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
					empDTO.setMaritalStatus(object[26] != null ? object[26].toString() : null);
					empDTO.setMobileNo(object[27] != null ? Long.parseLong(object[27].toString()) : null);
					empDTO.setMotherTongue(object[28] != null ? object[28].toString() : null);
					empDTO.setName(object[29] != null ? object[29].toString() : null);
					empDTO.setNoticePeriod(object[30] != null ? Short.parseShort(object[30].toString()) : null);
					empDTO.setAlternateMobileNo(object[31] != null ? Long.parseLong(object[31].toString()) : null);
					empDTO.setPanNumber(object[32] != null ? object[32].toString() : null);
					empDTO.setPassportNumber(object[33] != null ? object[33].toString() : null);
					empDTO.setPermanentAddress(object[34] != null ? object[34].toString() : null);
					empDTO.setPfAccountNumber(object[35] != null ? object[35].toString() : null);
					empDTO.setPincode(object[36] != null ? Integer.parseInt(object[36].toString()) : null);
					empDTO.setPlaceOfBirth(object[37] != null ? object[37].toString() : null);
					empDTO.setPassingGrade(object[38] != null ? object[38].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[39] != null ? object[39].toString() : null);
					empDTO.setRelation(object[40] != null ? object[40].toString() : null);
					empDTO.setState(object[41] != null ? object[41].toString() : null);
					empDTO.setUan(object[42] != null ? object[42].toString() : null);
					empDTO.setViewsOnOrganisation(object[43] != null ? object[43].toString() : null);
					empDTO.setYearOfPassing(object[44] != null ? Short.parseShort(object[44].toString()) : null);
					empDTO.setDepartmentId(object[45] != null ? Long.parseLong(object[45].toString()) : null);
					empDTO.setJobRoleName(object[46] != null ? object[46].toString() : null);
					empDTO.setDepartmentName(object[47] != null ? object[47].toString() : null);
					empDTO.setWorkLocation(object[48] != null ? object[48].toString() : null);
					empDTO.setProbationPeriod(object[49] != null ? Short.parseShort(object[49].toString()) : null);
					empDTO.setEmpId(object[50] != null ? Long.parseLong(object[50].toString()) : null);
					empDTO.setManagerName(object[51] != null ? object[51].toString() : null);
					empDTO.setExperience(object[52] != null ? object[52].toString() : null);
					empDTO.setBillable(object[53] != null ? (object[53].toString()) : null);
					empDTO.setChild1(object[54] != null ? (object[54].toString()) : null);
					empDTO.setChild2(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild3(object[56] != null ? (object[56].toString()) : null);
					empDTO.setMothersName(object[57] != null ? (object[57].toString()) : null);
					empDTO.setSpouse(object[58] != null ? (object[58].toString()) : null);
					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
					empDTO.setDateOfResign(
							object[60] != null ? stringToDateTimeParser.formatDateToString(object[60].toString())
									: null);
					empDTO.setInvalidAccessAttempt(object[61] != null ? Integer.parseInt(object[61].toString()) : null);
					empDTO.setDateOfRelieving(
							object[62] != null ? stringToDateTimeParser.formatDateToString(object[62].toString())
									: null);
					empDTO.setJobRoleName(object[63] != null ? (object[63].toString()) : null);	
					empDTO.setUpdatedByName(object[64] != null ? (object[64].toString()) : null);	
					empDTO.setCreatedByName(object[65] != null ? (object[65].toString()) : null);	
					empDTO.setUpdatedOn(object[66] != null ? (object[66].toString()) : null);
					empDTO.setIsTimesheetLockCheckEnable(object[67] != null ? (object[67].toString()) : null);
					empDTO.setEmploymentReleaseStatus(object[68] != null ? (object[68].toString()) : null);
					empDTO.setFailedAttempt(failedAttempt);
					empDTO.setPipFlag(object[69] != null ? object[69].toString() : null);
					empDTO.setPipId(object[70] != null ? Long.parseLong(object[70].toString()) : null );	
					empDTO.setBillableType(object[71] != null ? object[71].toString() : null );	
					empDTO.setProjectName(object[72] != null ? object[72].toString() : null);
					empDTO.setClientName(object[73] != null ? object[73].toString() : null);
					empDTO.setTeamName(object[74] != null ? object[74].toString() : null);
					empDTO.setDesignationName(object[75] != null ? object[75].toString() : null);
					empDTO.setIsConsultant(object[76] != null ? object[76].toString() : null);
					empDTO.setIsApprenticeship(object[77] != null ? object[77].toString() : null);
                    empDTO.setReportingManagerId(object[78] != null ? Long.parseLong(object[78].toString()) : null)	;
                    empDTO.setReportingManagerName(object[79] != null ? object[79].toString() : null);
                    empDTO.setEmployeeRole(object[80] != null ? object[80].toString() : null);                
                    
					empDTO.setReferedType(object[81] != null ? object[81].toString() : null);
					empDTO.setReferedName(object[82] != null ? object[82].toString() : null);
					empDTO.setEmployeeConfirmationDate(object[83] != null ? object[83].toString() : null);
					empDTO.setHodId(object[84] != null ? Long.parseLong(object[84].toString()) : null );
				    empDTO.setHodName(object[85] != null ? object[85].toString() : null);
				    empDTO.setHodDepartmentName(object[86] != null ? object[86].toString() : null);
					
					
					
					if (object[87] != null && object[72] != null) {
		                String projectIdStr = object[87].toString().trim();
		                String projectNameStr = object[72].toString().trim();

		                
		                if (!projectIdStr.isEmpty() && !projectNameStr.isEmpty()) {
		                    String[] projectIds = projectIdStr.split(",");
		                    String[] projectNames = projectNameStr.split(",");

		                    
		                    List<ProjectDTO> projectList = new ArrayList<>();
		                    int length = Math.min(projectIds.length, projectNames.length);
		                    
		                    for (int i = 0; i < length; i++) {
		                        try {
		                            ProjectDTO projectDTO = new ProjectDTO();
		                            projectDTO.setProjectId(Integer.parseInt(projectIds[i].trim()));
		                            projectDTO.setProjectName(projectNames[i].trim());
		                            projectList.add(projectDTO);
		                        } catch (NumberFormatException e) {
		                            System.err.println("Invalid projectId: " + projectIds[i]);
		                        }
		                    }
		                    empDTO.setProjectList(projectList);
		                }
		               

		            }
				
					empDTO.setUpdatedBy(object[88] != null ? Long.parseLong(object[88].toString()) : null);

					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
					
					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
					
//					 int totalEnabledQuarters = quarterCycleRepository.countByIsEnableAndIsActive();
//					 int completedQuarters = quarterCycleRepository.countByEmpIdAndCompletionStatusAndQuarterIdIn(
//				                empDTO.getEmpId(), quarterCycleRepository.findAllEnabledQuarterIds()
//				            );
//					 
//					 double performanceStatus = (totalEnabledQuarters > 0) 
//							    ? ((double) completedQuarters / totalEnabledQuarters) * 100 
//							    : 0.0;
//					 performanceStatus = Double.parseDouble(String.format("%.2f", performanceStatus));
//					 empDTO.setPerformanceStatusPercentage(performanceStatus);		 
					dtoList.add(empDTO);
				});
			 employeeCache.put(cacheKey, dtoList);

//	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	                response.setServiceResponse(dtoList);
//	                apiLogInfo.setApiResponse("List fetched from DB and stored in cache. Size: " + dtoList.size());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is null.");
				apiLogInfo.setApiResponse("Employee List is null.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	public ServiceResponse extendEmployeeProbation(EmployeeDTO employeeDto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/extendEmployeeProbation");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        String cacheKey = "allEmployees";
	        EmployeeDTO cachedEmployee = null;
	        Float noOfDays = employeeRepository.getNOOfDays(employeeDto.getEmpId());
	        
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            cachedEmployee = cachedEmployees.stream()
	                    .filter(emp -> emp.getEmpId().equals(employeeDto.getEmpId()))
	                    .findFirst()
	                    .orElse(null);
	                    
	            if (cachedEmployee != null) {
	                apiLogInfo.setApiResponse("Employee data retrieved from cache for empId: " + employeeDto.getEmpId());
	            }
	        }
	       
	        if (cachedEmployee == null) {
	            Employee employee = employeeRepository.findByEmpId(employeeDto.getEmpId());
	            if (employee == null) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " not found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	            
	            cachedEmployee = new EmployeeDTO();
	            cachedEmployee.setEmpId(employee.getEmpId());
	            cachedEmployee.setName(employee.getName());
	            cachedEmployee.setProbationPeriod(employee.getProbationPeriod());
	            
	            Object[] department = employeeRepository.getDepartmentRow(Long.parseLong(employee.getEmpId().toString()));
	            if (department != null && department.length > 0) {
	                cachedEmployee.setDepartmentId(Long.parseLong(department[0].toString()));
	            }
	            	            
	            apiLogInfo.setApiResponse("Employee data retrieved from database for empId: " + employeeDto.getEmpId());
	        }

	        Long actualHodIdOptional;
	        if (cachedEmployee.getHodId() != null) {
	            actualHodIdOptional = cachedEmployee.getHodId();
	        } else {
	            actualHodIdOptional = departmentRepository.findHodIdForEmployee(employeeDto.getEmpId());
	        }

	        if (actualHodIdOptional == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Could not determine the HOD for employee " + employeeDto.getEmploymentId() + ". The employee may not be assigned to a department.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (!actualHodIdOptional.equals(employeeDto.getHodId())) {
	            String hodName = employeeRepository.findEmployeeNameById(actualHodIdOptional);
	            String currenhod = employeeRepository.findEmployeeNameById(employeeDto.getHodId());

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("User " + currenhod + " is not the authorized HOD for this employee. The correct HOD is " + hodName + ".");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Employee employee = employeeRepository.findByEmpId(employeeDto.getEmpId());
	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " not found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
	        short newTotalProbation = (short) (employee.getProbationPeriod() + employeeDto.getExtendedPeriod()+noOfDays);
	        
	        employee.setProbationPeriod(newTotalProbation);
	        employee.setExtendedPeriod(employeeDto.getExtendedPeriod());
	        employee.setReasonOfExtension(employeeDto.getReasonOfExtension());
	        employee.setIsExtensionClicked(1L);
	        employee.setUpdatedOn(LocalDateTime.now());
	        employee.setUpdatedBy(employeeDto.getHodId().intValue());
	        employeeRepository.save(employee);
	        
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            for (EmployeeDTO emp : cachedEmployees) {
	                if (emp.getEmpId().equals(employeeDto.getEmpId())) {
	                    emp.setProbationPeriod(newTotalProbation);
	                    emp.setExtendedPeriod(employeeDto.getExtendedPeriod());
	                    emp.setIsExtensionClicked(1L);
	                    emp.setReasonOfExtension(employeeDto.getReasonOfExtension());
	                    emp.setUpdatedOn(LocalDateTime.now().toString());
	                    emp.setUpdatedBy(employeeDto.getHodId());
	                    break;
	                }
	            }
	            employeeCache.put(cacheKey, cachedEmployees);
	            apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Cache updated with new probation data");
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Probation for employee '" + cachedEmployee.getName() + "' has been successfully extended.");
	        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Successfully extended probation for empId: " + cachedEmployee.getEmpId());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An internal error occurred: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
  

	
	
	
	public ServiceResponse reduceEmployeeProbation(EmployeeDTO employeeDto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/reduceEmployeeProbation");
	    apiLogInfo.setLogLevel("INFO");
	    final int STANDARD_PROBATION_DAYS = 180;

	    try {
	        String cacheKey = "allEmployees";
	        EmployeeDTO cachedEmployee = null;

	       
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            cachedEmployee = cachedEmployees.stream()
	                    .filter(emp -> emp.getEmpId().equals(employeeDto.getEmpId()))
	                    .findFirst()
	                    .orElse(null);
	            if (cachedEmployee != null) {
	                apiLogInfo.setApiResponse("Employee data retrieved from cache for empId: " + employeeDto.getEmpId());
	            }
	        }

	        if (cachedEmployee == null) {
	            Employee employee = employeeRepository.findByEmpId(employeeDto.getEmpId());
	            if (employee == null) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " not found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	         
	            cachedEmployee = new EmployeeDTO();
	            apiLogInfo.setApiResponse("Employee data retrieved from database for empId: " + employeeDto.getEmpId());
	        }

	        Long actualHodId;
	        if (cachedEmployee.getHodId() != null) {
	            actualHodId = cachedEmployee.getHodId();
	        } else {
	            actualHodId = departmentRepository.findHodIdForEmployee(employeeDto.getEmpId());
	        }

	        if (actualHodId == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Could not determine the HOD for employee " + employeeDto.getEmpId() + ".");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (!actualHodId.equals(employeeDto.getHodId())) {
	            String hodName = employeeRepository.findEmployeeNameById(actualHodId);
	            String currentHod = employeeRepository.findEmployeeNameById(employeeDto.getHodId());
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("User " + currentHod + " is not the authorized HOD. The correct HOD is " + hodName + ".");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Employee employee = employeeRepository.findByEmpId(employeeDto.getEmpId());
	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " not found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	     
	        Float noOfDays = employeeRepository.getNOOfDays(employeeDto.getEmpId());
	        if ((employee.getProbationPeriod() + noOfDays) <= STANDARD_PROBATION_DAYS) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Cannot reduce probation. Employee is not on an extended probation period.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	      
	        short newTotalProbation = (short) (employee.getProbationPeriod() + noOfDays - employeeDto.getDaysToReduce());
	        if (newTotalProbation < STANDARD_PROBATION_DAYS) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Probation period cannot be reduced below the standard " + STANDARD_PROBATION_DAYS + " days.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        
	  
	        if (employee.getUpdatedOn() != null && employee.getExtendedPeriod() > 0) {
	            long daysSpentInExtension = ChronoUnit.DAYS.between(employee.getUpdatedOn().toLocalDate(), LocalDate.now());
	            long remainingExtensionDays = employee.getExtendedPeriod() - daysSpentInExtension;
	            remainingExtensionDays = Math.max(0, remainingExtensionDays);

	            if (employeeDto.getDaysToReduce() > remainingExtensionDays) {
	                 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                 response.setServiceResponse("Cannot reduce probation by " + employeeDto.getDaysToReduce() + " days. " +
	                                             "Only " + remainingExtensionDays + " days are left in the current extension period.");
	                 apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                 logService.logMyInfo(httpRequest, apiLogInfo);
	                 return response;
	            }
	        }

	        short newExtendedPeriod = (short) (employee.getExtendedPeriod() - employeeDto.getDaysToReduce());

	        employee.setProbationPeriod(newTotalProbation);
	        employee.setExtendedPeriod(newExtendedPeriod);
//	        employee.setReasonForReduction(employeeDto.getReasonForReduction()); 
	        
	        employee.setUpdatedOn(LocalDateTime.now());
	        employee.setUpdatedBy(employeeDto.getHodId().intValue());
	        employeeRepository.save(employee);

	   
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            for (EmployeeDTO emp : cachedEmployees) {
	                if (emp.getEmpId().equals(employeeDto.getEmpId())) {
	                    emp.setProbationPeriod(newTotalProbation);
	                    emp.setExtendedPeriod(newExtendedPeriod);
	                    emp.setUpdatedOn(LocalDateTime.now().toString());
	                    emp.setUpdatedBy(employeeDto.getHodId());
	                    break;
	                }
	            }
	            employeeCache.put(cacheKey, cachedEmployees);
	            apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Cache updated with new probation data");
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Probation for employee '" + cachedEmployee.getName() + "' has been successfully reduced.");
	        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Successfully reduced probation for empId: " + cachedEmployee.getEmpId());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An internal error occurred: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getEmployeeAndTimesheetDetails(EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getEmployeeAndTimesheetDetails");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"getEmployeeAndTimesheetDetails", PO_PORTAL_LOG_SOURCE, null, httpRequest);

			if (employeeTimesheetRequest == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request body is missing");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				response.setServiceError("Request body is missing");
				exceptionDetailsForLog = "employeeTimesheetRequest is null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}

			List<EmployeeTimesheetProjectResponse> employeeTimesheetProjectResponseList = new ArrayList<>();
			if (employeeTimesheetRequest.getStartDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid start date recieved!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				response.setServiceError("Invalid start date recieved!");
				exceptionDetailsForLog = "startDate null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			if (employeeTimesheetRequest.getEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid end date recieved!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				response.setServiceError("Invalid end date recieved!");
				exceptionDetailsForLog = "endDate null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			if (!employeeTimesheetRequest.getEndDate().isAfter(employeeTimesheetRequest.getStartDate())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Start date is greater than the recieved end date!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				response.setServiceError("Start date is greater than the recieved end date!");
				exceptionDetailsForLog = "date range invalid";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			if (employeeTimesheetRequest.getListType() == null
					|| employeeTimesheetRequest.getListType().trim().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Empty Billable type received!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				response.setServiceError("Empty Billable type received!");
				exceptionDetailsForLog = "listType empty";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			employeeTimesheetProjectResponseList = employeeRepository.findEmployeeAndTimesheetDetailsWithoutPagination(
					employeeTimesheetRequest.getStartDate().toLocalDate(),
					employeeTimesheetRequest.getEndDate().toLocalDate(), employeeTimesheetRequest.getListType());
			if (employeeTimesheetProjectResponseList != null && !employeeTimesheetProjectResponseList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(employeeTimesheetProjectResponseList);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("INFO");
				finalHttpStatusCode = HttpStatus.OK.value();
				return response;
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeTimesheetProjectResponseList != null
					? employeeTimesheetProjectResponseList
					: new ArrayList<>());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setLogLevel("INFO");
			finalHttpStatusCode = HttpStatus.OK.value();
		} catch (Exception e) {
			logger.error("getEmployeeAndTimesheetDetails failed", e);
			ExceptionLogContext.add(e);
			exceptionDetailsForLog = String.valueOf(e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode,
						exceptionDetailsForLog != null ? exceptionDetailsForLog : ExceptionLogContext.get(),
						httpRequest);
			}
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
	}

    public ServiceResponse getTeamAndTimeSheetDetails(GetTeamAndTimesheetDetailsDTO dto) {
    	ServiceResponse response = new ServiceResponse();
    	LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getTeamAndTimeSheetDetails");
	    apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = buildRequestPathForLogging(httpRequest);

		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"getTeamAndTimeSheetDetails", PO_PORTAL_LOG_SOURCE, null, httpRequest);

			if (dto == null) {
				buildFailureResponse(response, apiLogInfo, "Request body is missing.");
				exceptionDetailsForLog = "dto is null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}

			Long poId = dto.getPoId();
			Long poProjectId = dto.getPoProjectId();
			LocalDate startDate = dto.getStartDate();
			LocalDate endDate = dto.getEndDate();
			LocalDateTime startDateTime = null;
			LocalDateTime endDateTime = null;
			String projectName = dto.getProjectName();
			if(poId == null) {
				buildFailureResponse(response, apiLogInfo, "PO Id must not be null.");
				exceptionDetailsForLog = "poId null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			// if (poProjectId == null) {
			// 	return buildFailureResponse(response, apiLogInfo,
			// 			"PO Project Id must not be null.");
			// }

			if (startDate == null) {
				buildFailureResponse(response, apiLogInfo, "Start Date must not be null.");
				exceptionDetailsForLog = "startDate null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
	
			if (endDate == null) {
				buildFailureResponse(response, apiLogInfo, "End Date must not be null.");
				exceptionDetailsForLog = "endDate null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
	
			if (endDate.isBefore(startDate)) {
				buildFailureResponse(response, apiLogInfo,
						"End Date must be greater than or equal to Start Date.");
				exceptionDetailsForLog = "endDate before startDate";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			if(projectName == null){
				buildFailureResponse(response, apiLogInfo, "Projectname can not be null.");
				exceptionDetailsForLog = "projectName null";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				return response;
			}
			startDateTime = startDate.atStartOfDay();
			endDateTime = endDate.atTime(23, 59, 59, 999999999);
			// List<TeamTimesheetDetailsResponse> teamTimesheetDetailsResponseList = employeeTeamMapRepository.getTeamAndTimeSheetDetails(poId,poProjectId,startDateTime,endDateTime);
			List<TeamTimesheetDetailsResponse> teamTimesheetDetailsResponseList = employeeTeamMapRepository.getTeamAndTimeSheetDetails2(poId,projectName,startDateTime,endDateTime);

			if(teamTimesheetDetailsResponseList == null || teamTimesheetDetailsResponseList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse("No timesheet detail fetched for the employee of this project!");
		        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | No timesheet detail fetched for this employee in the given date range! Start Date:- " + poId );
		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		        finalHttpStatusCode = HttpStatus.OK.value();
		        return response;
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(teamTimesheetDetailsResponseList);
	        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " Length of teamTimesheet Response list :- " + teamTimesheetDetailsResponseList.size() );
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        finalHttpStatusCode = HttpStatus.OK.value();
		} catch (Exception e) {
			logger.error("getTeamAndTimeSheetDetails failed", e);
			ExceptionLogContext.add(e);
			exceptionDetailsForLog = String.valueOf(e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode,
						exceptionDetailsForLog != null ? exceptionDetailsForLog : ExceptionLogContext.get(),
						httpRequest);
			}
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
    }

//    public ServiceResponse getProjectDetailsByEmpIdAndDateRange(EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
//    	ServiceResponse response = new ServiceResponse();
//    	LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getProjectDetailsByEmpIdAndDateRange");
//	    apiLogInfo.setLogLevel("INFO");
//	    StringBuilder logBuilder = new StringBuilder();
//		try {
//			List<TeamTimesheetDetailsResponse> teamTimesheetDetailsResponseList = new ArrayList<>();
//			if(employeeTimesheetRequest.getEmpId() != null) {
//				logBuilder.append("EmpId:- " + employeeTimesheetRequest.getEmpId() + "\n");
//				logBuilder.append("StartDate:- " + employeeTimesheetRequest.getStartDate() + "\n");
//				logBuilder.append("EndDate:- " + employeeTimesheetRequest.getEndDate() + "\n");
//	            
//	            if (employeeTimesheetRequest.getStartDate() == null) {
//	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	                response.setServiceResponse("Null start date received");
//	                apiLogInfo.setApiResponse("Null start date date received for empId:- " + employeeTimesheetRequest.getEmpId());
//	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	                apiLogInfo.setApiRequest(logBuilder.toString());
//	                logService.logMyInfo(httpRequest, apiLogInfo);
//	                return response;
//	            }
//	            
//				Boolean flag = employeeRepository.isActiveEmployee(employeeTimesheetRequest.getEmpId());
//				
//				if(flag) {
//					teamTimesheetDetailsResponseList = employeeTeamMapRepository.getProjectDetailsByEmpIdAndDateRange(employeeTimesheetRequest.getEmpId(),employeeTimesheetRequest.getStartDate(),employeeTimesheetRequest.getEndDate());
//					if(teamTimesheetDetailsResponseList == null || teamTimesheetDetailsResponseList.isEmpty()) {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				        response.setServiceResponse("No timesheet detail fetched for this employee in the given date range!");
//				        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | No timesheet detail fetched for this employee in the given date range! Start Date:- " + employeeTimesheetRequest.getStartDate() + " End Date:- " + employeeTimesheetRequest.getEndDate());
//				        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				        return response;
//					}
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			        response.setServiceResponse(teamTimesheetDetailsResponseList);
//			        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " Length of teamTimesheet Response list :- " + teamTimesheetDetailsResponseList.size() );
//			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			        response.setServiceResponse("Employee has been made inactive in Ishine!");
//			        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Employee has been made inactive in Ishine! empId:- " + employeeTimesheetRequest.getEmpId());
//			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			throw e;
//		}
//		 apiLogInfo.setApiRequest(logBuilder.toString());
//	     logService.logMyInfo(httpRequest, apiLogInfo);
//	     return response;
//	}
    
    public ServiceResponse getProjectDetailsByEmpIdAndDateRange(EmployeeTimesheetProjectRequest employeeTimesheetRequest) {

        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getProjectDetailsByEmpIdAndDateRange");
        apiLogInfo.setLogLevel("INFO");

        StringBuilder logBuilder = new StringBuilder();
        ApiLog initialLog = null;
        String exceptionDetailsForLog = null;
        int finalHttpStatusCode = HttpStatus.OK.value();
        String sourceSystem = buildRequestPathForLogging(httpRequest);

        try {
            initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
                    "getProjectDetailsByEmpIdAndDateRange", PO_PORTAL_LOG_SOURCE, null, httpRequest);

            if (employeeTimesheetRequest == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Request body is missing");
                apiLogInfo.setApiResponse("employeeTimesheetRequest is null");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                exceptionDetailsForLog = "employeeTimesheetRequest is null";
                finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
                return response;
            }

            List<TeamTimesheetDetailsResponse> teamTimesheetDetailsResponseList = new ArrayList<>();

            if (employeeTimesheetRequest.getEmpId() != null) {

                logBuilder.append("EmpId:- ").append(employeeTimesheetRequest.getEmpId()).append("\n");
                logBuilder.append("StartDate:- ").append(employeeTimesheetRequest.getStartDate()).append("\n");
                logBuilder.append("EndDate:- ").append(employeeTimesheetRequest.getEndDate()).append("\n");

                if (employeeTimesheetRequest.getStartDate() == null) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Null start date received");

                    apiLogInfo.setApiResponse("Null start date received for empId:- " + employeeTimesheetRequest.getEmpId());
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                    exceptionDetailsForLog = "startDate null";
                    finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
                    return response;
                }

                Boolean flag = employeeRepository.isActiveEmployee(employeeTimesheetRequest.getEmpId());

                if (Boolean.TRUE.equals(flag)) {

                    List<Object[]> result =
                            employeeTeamMapRepository.getProjectDetailsByEmpIdAndDateRange(
                                    employeeTimesheetRequest.getEmpId(),
                                    employeeTimesheetRequest.getStartDate(),
                                    employeeTimesheetRequest.getEndDate()
                            );

                    if (result == null || result.isEmpty()) {

                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                        response.setServiceResponse("No timesheet detail fetched for this employee in the given date range!");

                        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse()
                                + " | No timesheet detail fetched for this employee in the given date range! "
                                + "Start Date:- " + employeeTimesheetRequest.getStartDate()
                                + " End Date:- " + employeeTimesheetRequest.getEndDate());

                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                        finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
                        return response;
                    }

                    for (Object[] obj : result) {

                        TeamTimesheetDetailsResponse dto = new TeamTimesheetDetailsResponse();

                        dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
                        dto.setEmployementId(obj[1] != null ? Long.parseLong(obj[1].toString()) : null);
                        dto.setEmpName(obj[2] != null ? obj[2].toString() : null);
                        dto.setRole(obj[3] != null ? obj[3].toString() : null);

                        dto.setPoId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
                        dto.setBillingRole(obj[5] != null ? obj[5].toString() : null);

                        dto.setTeamName(obj[6] != null ? obj[6].toString() : null);
                        dto.setTeamId(obj[7] != null ? Long.parseLong(obj[7].toString()) : null);

                        dto.setTeamLeadName(obj[8] != null ? obj[8].toString() : null);
                        dto.setManagerName(obj[9] != null ? obj[9].toString() : null);

                        dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
                        dto.setProjectName(obj[11] != null ? obj[11].toString() : null);

                        dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);

                        dto.setStartDate(
                                obj[13] != null
                                        ? convertToLocalDateTime(obj[13])
                                        : null
                        );

                        dto.setEndDate(
                                obj[14] != null
                                        ? convertToLocalDateTime(obj[14])
                                        : null
                        );
                        
                        dto.setDepartment(obj[15] != null ? obj[15].toString() : null);                        
                        teamTimesheetDetailsResponseList.add(dto);
                    }

                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(teamTimesheetDetailsResponseList);

                    apiLogInfo.setApiResponse(apiLogInfo.getApiResponse()
                            + " Length of teamTimesheet Response list :- "
                            + teamTimesheetDetailsResponseList.size());

                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                    finalHttpStatusCode = HttpStatus.OK.value();

                } else {

                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Employee has been made inactive in Ishine!");

                    apiLogInfo.setApiResponse(apiLogInfo.getApiResponse()
                            + " | Employee has been made inactive in Ishine! empId:- "
                            + employeeTimesheetRequest.getEmpId());

                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                    finalHttpStatusCode = HttpStatus.FORBIDDEN.value();
                }
            }

        } catch (Exception e) {
            logger.error("getProjectDetailsByEmpIdAndDateRange failed", e);
            ExceptionLogContext.add(e);
            exceptionDetailsForLog = String.valueOf(e);

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());

            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
            finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        } finally {
            if (initialLog != null) {
                apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode,
                        exceptionDetailsForLog != null ? exceptionDetailsForLog : ExceptionLogContext.get(),
                        httpRequest);
            }
            apiLogInfo.setApiRequest(logBuilder.toString());
            logService.logMyInfo(httpRequest, apiLogInfo);
        }

        return response;
    }
    
    
    private LocalDateTime convertToLocalDateTime(Object value) {

//        if (value instanceof Timestamp) {
//            return ((Timestamp) value).toLocalDateTime();
//        }

        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }

        if (value instanceof Date) {
            return ((Date) value).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        return null;
    }



    
    
//Raj Alpha Swain
    
    
    private Map<String, Integer> getActiveCountsByDateRanges(String tabName, String poProjectType, List<Long> deptIds, Boolean maternityleaveFilter) {
	    Map<String, Integer> activeCounts = new HashMap<>();
	    LocalDate currentDate = LocalDate.now();

	    LocalDate expiredWithin1Month = currentDate.plusMonths(1);
	    LocalDate expired1To2Month = currentDate.plusMonths(2);
	    LocalDate expired2To3Month = currentDate.plusMonths(3);
	    LocalDate expired3To6Month = currentDate.plusMonths(6);
	    LocalDate expired6To9Month = currentDate.plusMonths(9);
	    LocalDate expired9To12Month = currentDate.plusMonths(12);
	    LocalDate expiredAbove12Month = currentDate.plusYears(20); 

	    String  toDate1Month= expiredWithin1Month.plusDays(1).toString();
	    String  fromDate1Month= currentDate.toString();

	    String  toDate2Month= expired1To2Month.plusDays(1).toString();
	    String fromDate2Month = expiredWithin1Month.toString();
	    
	    String  toDate3Month= expired2To3Month.plusDays(1).toString();
	    String  fromDate3Month= expired1To2Month.toString();

	    String  toDate6Month= expired3To6Month.plusDays(1).toString();
	    String  fromDate6Month= expired2To3Month.toString();
	    
	    String  toDate9Month= expired6To9Month.plusDays(1).toString();
	    String  fromDate9Month= expired3To6Month.toString();
	    
	    String  toDate12Month= expired9To12Month.plusDays(1).toString();
	    String fromDate12Month = expired6To9Month.toString();

	    String fromDateAbove12Month = expired9To12Month.toString();
	    String toDateAbove12Month = expiredAbove12Month.toString();

	    Integer expiredWithin1MonthCount, expired1To2MonthsCount, expired2To3MonthsCount, expired3To6MonthsCount, 
	            expired6To9MonthsCount, expired9To12MonthsCount, expiredAbove12MonthsCount;

	    if ("Employee".equalsIgnoreCase(tabName)) {
	    	expiredWithin1MonthCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate1Month, toDate1Month, maternityleaveFilter));
	    	expired1To2MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate2Month, toDate2Month,maternityleaveFilter));
	    	expired2To3MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate3Month, toDate3Month,maternityleaveFilter));
	    	expired3To6MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate6Month, toDate6Month,maternityleaveFilter));
	    	expired6To9MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate9Month, toDate9Month,maternityleaveFilter));
	    	expired9To12MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDate12Month, toDate12Month,maternityleaveFilter));
	    	expiredAbove12MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsNew(poProjectType, deptIds, fromDateAbove12Month, toDateAbove12Month,maternityleaveFilter));
	    } else { 
	    	expiredWithin1MonthCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate1Month, toDate1Month));
	    	expired1To2MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate2Month, toDate2Month));
	    	expired2To3MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate3Month, toDate3Month));
	    	expired3To6MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate6Month, toDate6Month));
	    	expired6To9MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate9Month, toDate9Month));
	    	expired9To12MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDate12Month, toDate12Month));
	    	expiredAbove12MonthsCount = extractCountFromResult(employeeRepository.fetchactivePOCountsForProjectNew(poProjectType, deptIds, fromDateAbove12Month, toDateAbove12Month));
	    }

	    Integer totalExpiredCount = expiredWithin1MonthCount + expired1To2MonthsCount + expired2To3MonthsCount +
	    		expired3To6MonthsCount + expired6To9MonthsCount + expired9To12MonthsCount + expiredAbove12MonthsCount;

	    activeCounts.put("activeCountWithin1Month", expiredWithin1MonthCount);
	    activeCounts.put("activeCount1To2Months", expired1To2MonthsCount);
	    activeCounts.put("activeCount2To3Months", expired2To3MonthsCount);
	    activeCounts.put("activeCount3To6Months", expired3To6MonthsCount);
	    activeCounts.put("activeCount6To9Months", expired6To9MonthsCount);
	    activeCounts.put("activeCount9To12Months", expired9To12MonthsCount);
	    activeCounts.put("activeCountAbove12Months", expiredAbove12MonthsCount);
	    activeCounts.put("totalactivePOCount", totalExpiredCount);

	    return activeCounts;
	}
	
	private Map<String, Integer> getInactiveCountsByDateRanges(String poProjectType, List<Long> deptIds, Boolean maternityleaveFilter,String statusFlag) {
	    Map<String, Integer> inactiveCounts = new HashMap<>();
	    LocalDate currentDate = LocalDate.now();

	    LocalDate expiredWithin1Month = currentDate.minusMonths(1);
	    LocalDate expired1To2Month = currentDate.minusMonths(2);
	    LocalDate expired2To3Month = currentDate.minusMonths(3);
	    LocalDate expired3To6Month = currentDate.minusMonths(6);
	    LocalDate expired6To9Month = currentDate.minusMonths(9);
	    LocalDate expired9To12Month = currentDate.minusMonths(12);
	    LocalDate expiredAbove12Month = currentDate.minusYears(20); 

	    String fromDateExpiredWithin1Month = expiredWithin1Month.plusDays(1).toString();
	    String toDateExpiredWithin1Month = currentDate.toString();

	    String fromDateExpired1To2Month = expired1To2Month.plusDays(1).toString();
	    String toDateExpired1To2Month = expiredWithin1Month.toString();
	    
	    String fromDateExpired2To3Month = expired2To3Month.plusDays(1).toString();
	    String toDateExpired2To3Month = expired1To2Month.toString();

	    String fromDateExpired3To6Month = expired3To6Month.plusDays(1).toString();
	    String toDateExpired3To6Month = expired2To3Month.toString();
	    
	    String fromDateExpired6To9Month = expired6To9Month.plusDays(1).toString();
	    String toDateExpired6To9Month = expired3To6Month.toString();
	    
	    String fromDateExpired9To12Month = expired9To12Month.plusDays(1).toString();
	    String toDateExpired9To12Month = expired6To9Month.toString();

	    String fromDateExpiredAbove12Month = expiredAbove12Month.toString();
	    String toDateExpiredAbove12Month = expired9To12Month.toString();

	    Integer expiredWithin1MonthCount, expired1To2MonthsCount, expired2To3MonthsCount, expired3To6MonthsCount, 
	            expired6To9MonthsCount, expired9To12MonthsCount, expiredAbove12MonthsCount, totalExpiredCount;

	    expiredWithin1MonthCount  = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpiredWithin1Month, toDateExpiredWithin1Month, maternityleaveFilter)).orElse(0);
	    expired1To2MonthsCount    = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpired1To2Month, toDateExpired1To2Month, maternityleaveFilter)).orElse(0);
	    expired2To3MonthsCount    = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpired2To3Month, toDateExpired2To3Month, maternityleaveFilter)).orElse(0);
	    expired3To6MonthsCount    = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpired3To6Month, toDateExpired3To6Month, maternityleaveFilter)).orElse(0);
	    expired6To9MonthsCount    = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpired6To9Month, toDateExpired6To9Month, maternityleaveFilter)).orElse(0);
	    expired9To12MonthsCount   = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpired9To12Month, toDateExpired9To12Month, maternityleaveFilter)).orElse(0);
	    expiredAbove12MonthsCount = Optional.ofNullable(employeeRepository.fetchInactivePOCountsNew(poProjectType, deptIds, fromDateExpiredAbove12Month, toDateExpiredAbove12Month, maternityleaveFilter)).orElse(0);
	    totalExpiredCount         = Optional.ofNullable(employeeRepository.fetchInactiveTotalPOCount(poProjectType, deptIds, maternityleaveFilter,statusFlag)).orElse(0);

	    inactiveCounts.put("inactiveCountWithin1Month", expiredWithin1MonthCount);
	    inactiveCounts.put("inactiveCount1To2Months", expired1To2MonthsCount);
	    inactiveCounts.put("inactiveCount2To3Months", expired2To3MonthsCount);
	    inactiveCounts.put("inactiveCount3To6Months", expired3To6MonthsCount);
	    inactiveCounts.put("inactiveCount6To9Months", expired6To9MonthsCount);
	    inactiveCounts.put("inactiveCount9To12Months", expired9To12MonthsCount);
	    inactiveCounts.put("inactiveCountAbove12Months", expiredAbove12MonthsCount);
	    inactiveCounts.put("totalInactivePOCount", totalExpiredCount);

	    return inactiveCounts;
	}

	private Integer extractCountFromResult(List<Object[]> queryResult) {
	    if (queryResult != null && !queryResult.isEmpty()) {
	        Object[] firstRow = queryResult.get(0);
	       
	        if (firstRow != null && firstRow.length > 3 && firstRow[3] != null) {
	            return Integer.parseInt(firstRow[3].toString());
	        }
	    }
	    return 0;
	}
//	Raj ALpha Swain
	
	public ServiceResponse fetchActivePOCounts(GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/fetchActivePOCounts");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        
	        if (employeeDTO.getCategory() == null || employeeDTO.getPoProjectType() == null || employeeDTO.getDeptId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Required parameters (tabName, poProjectType, deptId) are missing.");
	            apiLogInfo.setApiResponse("Validation failed: Missing parameters.");
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Map<String, Integer> countsMap = getActiveCountsByDateRanges(
	            employeeDTO.getCategory(),
	            employeeDTO.getPoProjectType(),
	            employeeDTO.getDeptId(),
	            employeeDTO.getHideMaternityLeaveEmps()
	        );

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(countsMap);
	        apiLogInfo.setApiResponse("Active PO counts fetched successfully for tab: " + employeeDTO.getCategory());

	    } catch (Exception e) {
	        e.printStackTrace(); 
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An error occurred while fetching Active PO counts.");
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	
	
	public ServiceResponse fetchInactivePOCounts(GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/fetchInactivePOCounts");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        
	        if (employeeDTO.getCategory() == null || employeeDTO.getPoProjectType() == null || employeeDTO.getDeptId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Required parameters (tabName, poProjectType, deptId) are missing.");
	            apiLogInfo.setApiResponse("Validation failed: Missing parameters.");
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Map<String, Integer> countsMap = getInactiveCountsByDateRanges(
	            employeeDTO.getPoProjectType(),
	            employeeDTO.getDeptId(),
	            employeeDTO.getHideMaternityLeaveEmps(),
	            employeeDTO.getFlag()
	            );

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(countsMap);
	        apiLogInfo.setApiResponse("Inactive PO counts fetched successfully for tab: " + employeeDTO.getCategory());

	    } catch (Exception e) {
	        e.printStackTrace(); 
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An error occurred while fetching inactive PO counts.");
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}


//	public ServiceResponse fetchInactivePOCounts(EmployeeDTO employeeDTO) {
//		
//		 ServiceResponse response = new ServiceResponse();
//		    LogDTO apiLogInfo = new LogDTO();
//		    apiLogInfo.setApiUrl("/api/fetchInactivePOCounts");
//		    apiLogInfo.setLogLevel("INFO");
//
//		    try {
//		    	
//		    	if(employeeDTO.getTabName().equals("Employee")) {
//		    	
//		        List<Object[]> optionalEmployeeList = employeeRepository.fetchInactivePOCounts(employeeDTO.getPoProjectType(),employeeDTO.getDeptId());
//				List<InActivePoDTO> countOfInActive = new ArrayList<InActivePoDTO>();
//		        if (!optionalEmployeeList.isEmpty()) {
//		        	for(Object[] object : optionalEmployeeList) {
//		        		if ("TNM".equals(object[1])) {
//
//		                	InActivePoDTO inActivePoDTO = new InActivePoDTO();
//		                	inActivePoDTO.setTotalEmpPerProjectTypeLast7days(object[4] != null ? object[4].toString() : null);
//		                	inActivePoDTO.setTotalEmpPerProjectTypeLast30days(object[5] != null ? object[5].toString() : null);
//		                	inActivePoDTO.setTotalEmpPerProjectTypeLast90days(object[6] != null ? object[6].toString() : null);
//		                	inActivePoDTO.setTotalEmpPerProjectTypeLast180days(object[7] != null ? object[7].toString() : null);
//		                	inActivePoDTO.setTotalEmpPerProjectTypeLast1Year(object[8] != null ? object[8].toString() : null);
//		                	inActivePoDTO.setTotalEmpPerProjectTypeTotal(object[3] != null ? object[3].toString() : null);
//		                	
//		                	countOfInActive.add(inActivePoDTO);
//		                	}		                   
//		                };
//
//		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		            response.setServiceResponse(countOfInActive);
//		            apiLogInfo.setApiResponse(" fetched of size: " + countOfInActive.size());
//		        } else {
//		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		            response.setServiceResponse("countOfInActive  is empty.");
//		            apiLogInfo.setApiResponse("countOfInActive is empty.");
//		        }
//		    	}
//		    	else {
//		    		  List<Object[]> optionalEmployeeListForProjects = employeeRepository.fetchInactivePOCountsForProject(employeeDTO.getPoProjectType(),employeeDTO.getDeptId());
//						List<InActivePoDTO> countOfInActive = new ArrayList<InActivePoDTO>();
//				        if (!optionalEmployeeListForProjects.isEmpty()) {
//				        	for(Object[] object : optionalEmployeeListForProjects) {
//				        		if ("TNM".equals(object[1])) {
//
//				                	InActivePoDTO inActivePoDTO = new InActivePoDTO();
//				                	inActivePoDTO.setTotalEmpPerProjectTypeLast7days(object[4] != null ? object[4].toString() : null);
//				                	inActivePoDTO.setTotalEmpPerProjectTypeLast30days(object[5] != null ? object[5].toString() : null);
//				                	inActivePoDTO.setTotalEmpPerProjectTypeLast90days(object[6] != null ? object[6].toString() : null);
//				                	inActivePoDTO.setTotalEmpPerProjectTypeLast180days(object[7] != null ? object[7].toString() : null);
//				                	inActivePoDTO.setTotalEmpPerProjectTypeLast1Year(object[8] != null ? object[8].toString() : null);
//				                	inActivePoDTO.setTotalEmpPerProjectTypeTotal(object[3] != null ? object[3].toString() : null);
//				                	
//				                	countOfInActive.add(inActivePoDTO);
//				                	}		                   
//				                };
//
//				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				            response.setServiceResponse(countOfInActive);
//				            apiLogInfo.setApiResponse(" fetched of size: " + countOfInActive.size());
//				        } else {
//				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				            response.setServiceResponse("countOfInActive  is empty.");
//				            apiLogInfo.setApiResponse("countOfInActive is empty.");
//				        }
//		    	}
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        response.setServiceResponse("An error occurred while processing the request.");
//		        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        apiLogInfo.setLogLevel("ERROR");
//		        response.setServiceError(e.getMessage());
//		    }
//
//		    logService.logMyInfo(httpRequest, apiLogInfo);
//		    return response;
//	}
	
	
//Raj Alpha Swain
	public ServiceResponse fetchActivePOListOfEmployee(GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/fetchActivePOListOfEmployee");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (employeeDTO.getCategory() == null || employeeDTO.getPoProjectType() == null || 
	            employeeDTO.getDeptId() == null || employeeDTO.getDateRange() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Required parameters (tabName, poProjectType, deptId, dateRange) are missing.");
	            apiLogInfo.setApiResponse("Validation failed: Missing parameters.");
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Map<String, String> dateRange = getDateRangeForList1(employeeDTO.getDateRange());
	        String fromDate = dateRange.get("fromDate");
	        String toDate = dateRange.get("toDate");

	        if ("Employee".equalsIgnoreCase(employeeDTO.getCategory())) {
	           
	            List<Object[]> optionalEmployeeList = employeeRepository.fetchActivePOListOfEmployeeNew(
	                employeeDTO.getPoProjectType(),   
	                employeeDTO.getDeptId(),            
	                fromDate,                           
	                toDate,
	                employeeDTO.getHideMaternityLeaveEmps()
	            );
	            
	            List<EmployeeDTO> countOfActivePoEmployeeWise = new ArrayList<>();
	            
	            if (!optionalEmployeeList.isEmpty()) {
	                for (Object[] object : optionalEmployeeList) {
	                    EmployeeDTO employeeDetails = new EmployeeDTO();
	                    employeeDetails.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	                    employeeDetails.setName(object[1] != null ? object[1].toString() : null);
	                    employeeDetails.setProjectName(object[4] != null ? object[4].toString() : null);
	                    employeeDetails.setPoNo(object[7] != null ? object[7].toString() : null);
	                    employeeDetails.setProjectStartDate(object[5] != null ? object[5].toString() : null);
	                    employeeDetails.setProjectEndDate(object[6] != null ? object[6].toString() : null);
	                    employeeDetails.setClientName(object[8] != null ? object[8].toString() : null);
	                    employeeDetails.setClientLocation(object[9] != null ? object[9].toString() : null);
	                    employeeDetails.setDepartmentName(object[10] != null ? object[10].toString() : null);
	                    employeeDetails.setPoProjectType(object[11] != null ? object[11].toString() : null);
	                    countOfActivePoEmployeeWise.add(employeeDetails);
	                }

	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(countOfActivePoEmployeeWise);
	                apiLogInfo.setApiResponse("Employee list fetched of size: " + countOfActivePoEmployeeWise.size() + 
	                    " for date range: " + employeeDTO.getDateRange());
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No active PO employees found for the specified date range.");
	                apiLogInfo.setApiResponse("fetchactivePOListOfEmployee is empty for date range: " + employeeDTO.getDateRange());
	            }
	        } else {
	            List<Object[]> poProjectActiveListProject = employeeRepository.fetchActivePOListOfProjectNew(
	                employeeDTO.getPoProjectType(),    
	                employeeDTO.getDeptId(),           
	                fromDate,                          
	                toDate                             
	            );
	            
	            List<ProjectFetchDTO> countOfActivePoProjectWise = new ArrayList<>();
	            
	            if (!poProjectActiveListProject.isEmpty()) {
	                for (Object[] object : poProjectActiveListProject) {
	                    ProjectFetchDTO projectDetails = new ProjectFetchDTO();
	                    projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
	                    projectDetails.setPoNo(object[1] != null ? object[1].toString() : null);
	                    projectDetails.setPoProjectType(object[2] != null ? object[2].toString() : null);
	                    projectDetails.setProjectStartDate(object[3] != null ? object[3].toString() : null);
	                    projectDetails.setProjectEndDate(object[4] != null ? object[4].toString() : null);
	                    projectDetails.setClientRM(object[5] != null ? object[5].toString() : null);
	                    projectDetails.setApmosysRM(object[6] != null ? object[6].toString() : null);
	                    projectDetails.setClientName(object[7] != null ? object[7].toString() : null);
	                    projectDetails.setClientLocation(object[8] != null ? object[8].toString() : null);
	                    countOfActivePoProjectWise.add(projectDetails);
	                }

	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(countOfActivePoProjectWise);
	                apiLogInfo.setApiResponse("Project list fetched of size: " + countOfActivePoProjectWise.size() + 
	                    " for date range: " + employeeDTO.getDateRange());
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No active PO projects found for the specified date range.");
	                apiLogInfo.setApiResponse("countOfActive is empty for date range: " + employeeDTO.getDateRange());
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An error occurred while processing the request.");
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}



public ServiceResponse getAllProficiency() {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getAllProficiency");
	apiLogInfo.setApiUrl("/api/getAllProficiency");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		List<Proficiency> allProficiency = proficiencyRepository.findAll();
		if (allProficiency.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Proficiency List is Empty.");
			apiLogInfo.setApiResponse("Proficiency List is Empty.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} else {
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(allProficiency);
			apiLogInfo.setApiResponse("dtoList Size : "+allProficiency.size());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		}
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}



public ServiceResponse getAllPredefinedSkills() {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getAllPredefinedSkills");
	apiLogInfo.setApiUrl("/api/getAllPredefinedSkills");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		List<PredefinedSkills> predefinedSkills = predefinedSkillsRepository.findAll();
		if (predefinedSkills.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("PredefinedSkills List is Empty.");
			apiLogInfo.setApiResponse("PredefinedSkills List is Empty.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} else {
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(predefinedSkills);
			apiLogInfo.setApiResponse("dtoList Size : "+predefinedSkills.size());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		}
	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}


public ServiceResponse addSkill(EmployeeSkillProficiencyDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("addSkill");
	apiLogInfo.setApiUrl("/api/addSkill");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		   if ((dto.getSkillId() == null || dto.getSkillId() == 0) 
		             && (dto.getAdditionalSkill() == null || dto.getAdditionalSkill().trim().isEmpty())) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Either skillId or additionalSkill must be provided");
		            return response;
		        }

		       
		        EmployeeSkillProficiencyMapping entity = new EmployeeSkillProficiencyMapping();
		        entity.setEmpId(dto.getEmpId());
		        entity.setSkillId(dto.getSkillId()); 
		        entity.setAdditionalSkill(dto.getAdditionalSkill()); 
		        entity.setProficiencyId(dto.getProficiencyId());
		        entity.setActive(true);
		        entity.setCreatedBy(dto.getEmpId());    
		        employeeSkillProficiencyMappingRepository.save(entity);

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse("Skill added successfully");
		        logBuilder.append("Skill added for empId: ").append(dto.getEmpId());
	
		
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}

public ServiceResponse downloadCertificate(Long docId) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("downloadCertificate");
    apiLogInfo.setApiUrl("/api/downloadCertificate");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();

    try {
        Optional<CertificateDocumentMapping> optionalDoc = certificateDocumentRepository.findById(docId);

        if (optionalDoc.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Document not found with id: " + docId);
            return response;
        }

        CertificateDocumentMapping doc = optionalDoc.get();

        
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("docId", doc.getDocId());
        fileData.put("docName", doc.getDocName());
        fileData.put("docMimeType", doc.getDocMimeType());
        fileData.put("docData", Base64.getEncoder().encodeToString(doc.getDocData())); // encode for JSON

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(fileData);
        logBuilder.append("File downloaded successfully for docId: ").append(docId);

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something Went Wrong.");
        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        apiLogInfo.setLogLevel("ERROR");
        response.setServiceError(e.getMessage());
    }

    apiLogInfo.setApiRequest(logBuilder.toString());
    logService.logMyInfo(httpRequest, apiLogInfo);
    return response;
}



public ServiceResponse updateSkill(EmployeeSkillProficiencyDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("updateSkill");
	apiLogInfo.setApiUrl("/api/updateSkill");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		Optional<EmployeeSkillProficiencyMapping> toBeUpdated = employeeSkillProficiencyMappingRepository.findById(dto.getEmpSkillId());	
		if(toBeUpdated.isPresent()) {
		
			EmployeeSkillProficiencyMapping update = toBeUpdated.get();
			update.setProficiencyId(dto.getProficiencyId());
			update.setUpdatedOn(LocalDateTime.now());
			update.setUpdatedBy(dto.getEmpId());
			
			employeeSkillProficiencyMappingRepository.save(update);
			
			 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Skill updated successfully.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}
		else {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Skill not found for empSkillId: " + dto.getEmpSkillId());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        }
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}


public ServiceResponse deleteSkillsOfEmployee(EmployeeSkillProficiencyDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("deleteSkillsOfEmployee");
	apiLogInfo.setApiUrl("/api/deleteSkillsOfEmployee");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		Optional<EmployeeSkillProficiencyMapping> toBedeleted = employeeSkillProficiencyMappingRepository.findById(dto.getEmpSkillId());	
		if(toBedeleted.isPresent()) {
		
			EmployeeSkillProficiencyMapping update = toBedeleted.get();
			update.setActive(false);	

			
			employeeSkillProficiencyMappingRepository.save(update);
			
			 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Skill removed successfully.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}
		else {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Skill not found for empSkillId: " + dto.getEmpSkillId());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        }
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}

public ServiceResponse getAllSkillsByEmpId(EmployeeSkillProficiencyDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getAllSkillsByEmpId");
	apiLogInfo.setApiUrl("/api/getAllSkillsByEmpId");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		
		List<EmployeeSkillProficiencyDTO> getAllSkillsByEmpId = employeeSkillProficiencyMappingRepository.getAllSkillsByEmpId(dto.getEmpId());		
		if (getAllSkillsByEmpId.isEmpty()) {
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Skill List of employee  is Empty.");
			apiLogInfo.setApiResponse("Skill List of employee is Empty.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} else {

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(getAllSkillsByEmpId);
			apiLogInfo.setApiResponse("dtoList Size : "+getAllSkillsByEmpId.size());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		}

		
		
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}

public ServiceResponse getAllCertificatesByEmpId(CertificateDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setSubFeatureName("getAllCertificatesByEmpId");
	apiLogInfo.setApiUrl("/api/getAllCertificatesByEmpId");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();

	try {
		
		List<CertificateDTO> getAllCertificatesByEmpId = employeeCertificatesRepository.getAllCertificatesByEmpId(dto.getEmpId());		
		if (getAllCertificatesByEmpId.isEmpty()) {
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Certificates List of employee  is Empty.");
			apiLogInfo.setApiResponse("Certificates List of employee is Empty.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} else {

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(getAllCertificatesByEmpId);
			apiLogInfo.setApiResponse("dtoList Size : "+getAllCertificatesByEmpId.size());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		}

		
		
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}


public ServiceResponse addCertificate(CertificateDTO dto, MultipartFile doc1) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("addCertificate");
    apiLogInfo.setApiUrl("/api/addCertificate");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();

    try {
     
        if (dto == null) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Certificate details are missing.");
            return response;
        }

       
        CertificateDocumentMapping docEntity = new CertificateDocumentMapping();
        if (doc1 != null && !doc1.isEmpty()) {
            docEntity.setDocData(doc1.getBytes());
            docEntity.setDocMimeType(doc1.getContentType());
            docEntity.setDocName(doc1.getOriginalFilename());
            docEntity.setEmpId(dto.getEmpId()); 
            docEntity.setDActive(true);
            docEntity.setDoccreatedBy(dto.getEmpId());
            certificateDocumentRepository.save(docEntity);
            logBuilder.append("Document saved with docId: ").append(docEntity.getDocId()).append("; ");
        }

       
        EmployeeCertificates certEntity = new EmployeeCertificates();
        certEntity.setCertificateName(dto.getCertificationName());
        certEntity.setSpecialization(dto.getSpecialization());
        certEntity.setDeptId(dto.getDeptId());
        certEntity.setProficiencyId(dto.getProficiencyId());
        certEntity.setIssuingAuthority(dto.getIssuingAuthority());
        certEntity.setEmpId(dto.getEmpId()) ;     
        if (dto.getValidFrom() != null && !dto.getValidFrom().isEmpty()) {
            certEntity.setValidFrom(LocalDate.parse(dto.getValidFrom()));
        }
        if (dto.getExpiresOn() != null && !dto.getExpiresOn().isEmpty()) {
            certEntity.setExpiresOn(LocalDate.parse(dto.getExpiresOn()));
        }

        certEntity.setCActive(true);
        certEntity.setCertificateStatus("Active");
        certEntity.setCreatedBy(dto.getEmpId());

        if (docEntity.getDocId() != null) {
            certEntity.setDocId(docEntity.getDocId());
        }

        employeeCertificatesRepository.save(certEntity);
       

       
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            for (EmployeeSkillProficiencyDTO skillDto : dto.getSkills()) {
                CertificateSkillMapping skillEntity = new CertificateSkillMapping();
                skillEntity.setEmployeeCertificateId(certEntity.getEmployeeCertificateId());
                skillEntity.setSkillId(skillDto.getSkillId());
                skillEntity.setAdditionalSkill(skillDto.getAdditionalSkill());
                skillEntity.setProficiencyId(dto.getProficiencyId());
                skillEntity.setEmpId(dto.getEmpId());
                skillEntity.setScActive(true);
                skillEntity.setCscreatedBy(skillDto.getEmpId());
                certificateSkillMapRepository.save(skillEntity);
            }
            logBuilder.append("Skills saved for certificateId: ")
                      .append(certEntity.getEmployeeCertificateId()).append("; ");
            
            syncEmployeeSkills(dto.getEmpId(), dto.getProficiencyId(), dto.getEmpId(), dto.getSkills());
            
            logBuilder.append("Skills saved and synced with EmployeeSkillProficiencyMapping; ");
        }

        
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse("Certificate added successfully.");
        logBuilder.append("Certificate successfully added.");

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something Went Wrong.");
        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        apiLogInfo.setLogLevel("ERROR");
        response.setServiceError(e.getMessage());
    }

    apiLogInfo.setApiRequest(logBuilder.toString());
    logService.logMyInfo(httpRequest, apiLogInfo);
    return response;
}


public ServiceResponse deleteCertificateOfEmployee(CertificateDTO dto) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("deleteCertificateOfEmployee");
    apiLogInfo.setApiUrl("/api/deleteCertificate");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();

    try {
        if (dto.getEmployeeCertificateId() == null || dto.getEmployeeCertificateId() == 0) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Employee Certificate Id is required");
            return response;
        }

      
        Optional<EmployeeCertificates> certOpt = employeeCertificatesRepository.findById(dto.getEmployeeCertificateId());
        if (!certOpt.isPresent()) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Certificate not found");
            return response;
        }

        EmployeeCertificates certificate = certOpt.get();

       
        certificate.setCActive(false);
        employeeCertificatesRepository.save(certificate);

     
        List<CertificateSkillMapping> skills = certificateSkillMapRepository
                .findByEmployeeCertificateIdAndScActiveTrue(dto.getEmployeeCertificateId());

        for (CertificateSkillMapping skill : skills) {
            skill.setScActive(false);
        }
        certificateSkillMapRepository.saveAll(skills);

       
        if (certificate.getDocId() != null) {
            Optional<CertificateDocumentMapping> docOpt = certificateDocumentRepository.findById(certificate.getDocId());
            if (docOpt.isPresent()) {
                CertificateDocumentMapping doc = docOpt.get();
                doc.setDActive(false);
                certificateDocumentRepository.save(doc);
            }
        }

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse("Certificate deleted successfully");
        logBuilder.append("Certificate soft-deleted with employeeCertificateId: ")
                  .append(dto.getEmployeeCertificateId());

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something Went Wrong.");
        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        apiLogInfo.setLogLevel("ERROR");
        response.setServiceError(e.getMessage());
    }

    apiLogInfo.setApiRequest(logBuilder.toString());
    logService.logMyInfo(httpRequest, apiLogInfo);

    return response;
}



///////////////////////cron to update certficate status//////////////////////////
//@Scheduled(cron = "0 01 23 * * ?")
public void updateCertificateStatuses() {

    try {
        List<EmployeeCertificates> certificates = employeeCertificatesRepository.findAll();

        LocalDate today = LocalDate.now();

        for (EmployeeCertificates cert : certificates) {
            if (cert.getValidFrom() == null || cert.getExpiresOn() == null) {
                continue; 
            }

            LocalDate validFrom = cert.getValidFrom();
            LocalDate expiresOn = cert.getExpiresOn();

            long totalDays = ChronoUnit.DAYS.between(validFrom, expiresOn);
            long thresholdDays = (totalDays * EXPIRY_THRESHOLD_PERCENT) / 100;
            LocalDate thresholdDate = validFrom.plusDays(thresholdDays);

            String status;
            if (today.isAfter(expiresOn)) {
                status = "Expired";
            } else if ((today.isEqual(thresholdDate) || today.isAfter(thresholdDate)) && today.isBefore(expiresOn.plusDays(1))) {
                status = "Expiring";
            } else {
                status = "Active";
            }

         
            if (!status.equals(cert.getCertificateStatus())) {
                cert.setCertificateStatus(status);
                cert.setUpdatedOn(LocalDateTime.now());
            
                employeeCertificatesRepository.save(cert);
               
            }
        }

      

    } catch (Exception e) {
        e.printStackTrace();
    }
  
   
}


//method to add skills for employee through certification/////////////
private void syncEmployeeSkills(Long empId, Long proficiencyId, Long createdBy, List<EmployeeSkillProficiencyDTO> skills) {
  
    List<EmployeeSkillProficiencyMapping> existingSkills = employeeSkillProficiencyMappingRepository.findByEmpIdAndActiveTrue(empId);

    for (EmployeeSkillProficiencyDTO skillDto : skills) {
        boolean exists = false;

        for (EmployeeSkillProficiencyMapping existing : existingSkills) {
          
            boolean skillMatch = (skillDto.getSkillId() != null && skillDto.getSkillId().equals(existing.getSkillId())) ||
                                 (skillDto.getAdditionalSkill() != null &&   normalizeName(skillDto.getAdditionalSkill())
                                         .equals(normalizeName(existing.getAdditionalSkill())));

            if (skillMatch) {
                exists = true;
           
                if (!existing.getProficiencyId().equals(proficiencyId)) {
                    existing.setProficiencyId(proficiencyId);
                    existing.setUpdatedOn(LocalDateTime.now());
                    existing.setUpdatedBy(createdBy);
                    employeeSkillProficiencyMappingRepository.save(existing);
                }
                break;
            }
        }

   
        if (!exists) {
            EmployeeSkillProficiencyMapping newSkill = new EmployeeSkillProficiencyMapping();
            newSkill.setEmpId(empId);
            newSkill.setSkillId(skillDto.getSkillId());
            newSkill.setAdditionalSkill(skillDto.getAdditionalSkill());
            newSkill.setProficiencyId(proficiencyId);
            newSkill.setActive(true);
            newSkill.setCreatedBy(createdBy);
            employeeSkillProficiencyMappingRepository.save(newSkill);
        }
    }
}






	
public ServiceResponse fetchInactivePOListOfEmployee(GetEmployeeProjectReportPayloadDTO employeeDTO) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setApiUrl("/api/fetchInactivePOListOfEmployee");
    apiLogInfo.setLogLevel("INFO");

    try {
        if (employeeDTO.getCategory() == null || employeeDTO.getPoProjectType() == null || 
            employeeDTO.getDeptId() == null || employeeDTO.getDateRange() == null) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Required parameters (tabName, poProjectType, deptId, dateRange) are missing.");
            apiLogInfo.setApiResponse("Validation failed: Missing parameters.");
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
        }

        Map<String, String> dateRange = getDateRangeForList(employeeDTO.getDateRange());
        String fromDate = dateRange.get("fromDate");
        String toDate = dateRange.get("toDate");

        if ("Employee".equalsIgnoreCase(employeeDTO.getCategory())) {
           
            List<Object[]> optionalEmployeeList = employeeRepository.fetchInactivePOListOfEmployeeNew(
                employeeDTO.getPoProjectType(),   
                employeeDTO.getDeptId(),            
                fromDate,                           
                toDate,
                employeeDTO.getHideMaternityLeaveEmps()
            );
            
            List<EmployeeDTO> countOfInActivePoEmployeeWise = new ArrayList<>();
            
            if (!optionalEmployeeList.isEmpty()) {
                for (Object[] object : optionalEmployeeList) {
                    EmployeeDTO employeeDetails = new EmployeeDTO();
                    employeeDetails.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
                    employeeDetails.setName(object[1] != null ? object[1].toString() : null);
                    employeeDetails.setProjectName(object[2] != null ? object[2].toString() : null);
                    employeeDetails.setPoNo(object[5] != null ? object[5].toString() : null);
                    employeeDetails.setProjectStartDate(object[3] != null ? object[3].toString() : null);
                    employeeDetails.setProjectEndDate(object[4] != null ? object[4].toString() : null);
                    employeeDetails.setClientName(object[6] != null ? object[6].toString() : null);
                    employeeDetails.setClientLocation(object[7] != null ? object[7].toString() : null);
                    employeeDetails.setDepartmentName(object[8] != null ? object[8].toString() : null);
                    employeeDetails.setPoProjectType(object[9] != null ? object[9].toString() : null);
                    employeeDetails.setEmployeementIdAccToET(object[10] != null ? object[10].toString() : null);
                    countOfInActivePoEmployeeWise.add(employeeDetails);
                }

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(countOfInActivePoEmployeeWise);
                apiLogInfo.setApiResponse("Employee list fetched of size: " + countOfInActivePoEmployeeWise.size() + 
                    " for date range: " + employeeDTO.getDateRange());
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No inactive PO employees found for the specified date range.");
                apiLogInfo.setApiResponse("fetchInactivePOListOfEmployee is empty for date range: " + employeeDTO.getDateRange());
            }
        } else {
            List<Object[]> poProjectInActiveListProject = employeeRepository.fetchInActivePOListOfProjectNew(
                employeeDTO.getPoProjectType(),    
                employeeDTO.getDeptId(),           
                fromDate,                          
                toDate                             
            );
            
            List<ProjectFetchDTO> countOfInActivePoProjectWise = new ArrayList<>();
            
            if (!poProjectInActiveListProject.isEmpty()) {
                for (Object[] object : poProjectInActiveListProject) {
                    ProjectFetchDTO projectDetails = new ProjectFetchDTO();
                    projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
                    projectDetails.setPoNo(object[1] != null ? object[1].toString() : null);
                    projectDetails.setPoProjectType(object[2] != null ? object[2].toString() : null);
                    projectDetails.setProjectStartDate(object[3] != null ? object[3].toString() : null);
                    projectDetails.setProjectEndDate(object[4] != null ? object[4].toString() : null);
                    projectDetails.setClientRM(object[5] != null ? object[5].toString() : null);
                    projectDetails.setApmosysRM(object[6] != null ? object[6].toString() : null);
                    projectDetails.setClientName(object[7] != null ? object[7].toString() : null);
                    projectDetails.setClientLocation(object[8] != null ? object[8].toString() : null);
                    countOfInActivePoProjectWise.add(projectDetails);
                }

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(countOfInActivePoProjectWise);
                apiLogInfo.setApiResponse("Project list fetched of size: " + countOfInActivePoProjectWise.size() + 
                    " for date range: " + employeeDTO.getDateRange());
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No inactive PO projects found for the specified date range.");
                apiLogInfo.setApiResponse("countOfInActive is empty for date range: " + employeeDTO.getDateRange());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("An error occurred while processing the request.");
        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        apiLogInfo.setLogLevel("ERROR");
        response.setServiceError(e.getMessage());
    }

    logService.logMyInfo(httpRequest, apiLogInfo);
    return response;
}
    
    public ServiceResponse revokeConfirmation(EmployeeDTO employeeDto) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/revokeConfirmation");
        apiLogInfo.setLogLevel("INFO");

        try {
            String cacheKey = "allEmployees";
            Long employeeIdToRevoke = employeeDto.getEmpId();

            Optional<Employee> employeeOptional = employeeRepository.findById(employeeIdToRevoke);

            if (employeeOptional.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Employee not found with ID " + employeeIdToRevoke);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            Employee employeeToUpdate = employeeOptional.get();

            
            Long actualHodId = departmentRepository.findHodIdForEmployee(employeeIdToRevoke);

            if (actualHodId == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Could not determine the HOD for employee " + employeeDto.getEmploymentId() + ".");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }

      
            if (!actualHodId.equals(employeeDto.getHodId())) {
                String actualHodName = employeeRepository.findEmployeeNameById(actualHodId);
                String requestorHodName = employeeRepository.findEmployeeNameById(employeeDto.getHodId());
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("User " + requestorHodName + " is not the authorized HOD. The correct HOD is " + actualHodName + ".");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }

         
            if (employeeToUpdate.getIsConfirmedClicked() == 0) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Confirmation for employee " + employeeToUpdate.getName() + " has not been clicked yet. Nothing to revoke.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }

            
            employeeToUpdate.setIsConfirmedClicked(0L); 
            employeeToUpdate.setEmploymentstatus("Probation"); 
            employeeToUpdate.setUpdatedOn(LocalDateTime.now());
            employeeToUpdate.setUpdatedBy(employeeDto.getHodId().intValue());
            
            employeeRepository.save(employeeToUpdate);

           
            if (employeeCache.containsKey(cacheKey)) {
                List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
                for (EmployeeDTO empDTO : cachedEmployees) {
                    if (empDTO.getEmpId().equals(employeeIdToRevoke)) {
                        empDTO.setIsConfirmedClicked(0L); 
                        empDTO.setEmploymentstatus("Probation"); 
                        empDTO.setUpdatedOn(LocalDateTime.now().toString());
                        empDTO.setUpdatedBy(employeeDto.getHodId());
                        break;
                    }
                }
                employeeCache.put(cacheKey, cachedEmployees); 
                apiLogInfo.setApiResponse("Database and cache updated. EmpId: " + employeeIdToRevoke);
            }

            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Confirmation for employee '" + employeeToUpdate.getName() + "' has been successfully revoked.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("An internal error occurred while revoking confirmation: " + e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            apiLogInfo.setLogLevel("ERROR");
            response.setServiceError(e.getMessage());
        }

        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }
 

 public ServiceResponse getEmployeesNearingProbationEnd(EmployeeDTO employeeDto) {
     ServiceResponse response = new ServiceResponse();
    
     
     LogDTO apiLogInfo = new LogDTO();
     apiLogInfo.setApiUrl("/api/employees/probation-reminders/"); 
     apiLogInfo.setLogLevel("INFO");

     try {
    	 Long hodId =  employeeDto.getHodId();
         LocalDate today = LocalDate.now();
         List<Employee> employeesToCheck = employeeRepository.getEmployeeInProbation();
         employeesToCheck.addAll(employeeRepository.getEmployeeInProbationExtended());

         List<EmployeeDTO> employeesNearingEnd = new ArrayList<>();

         for (Employee employee : employeesToCheck) {
             if (employee.getProbationPeriod() == null || employee.getDateOfJoining() == null) {
                 continue;
             }

//             Float noOfDays = employeeRepository.getNOOfDays(employeeDto.getEmpId());
             Float noOfDays = employeeRepository.getNOOfDays(employeeDto.getEmpId());
             if (noOfDays == null) {
                 noOfDays = 0f;
             }
LocalDate confirmationDate = employee.getDateOfJoining()
        .plusDays(employee.getProbationPeriod())
        .plusDays(Math.round(noOfDays));
		             long daysLeft = ChronoUnit.DAYS.between(today, confirmationDate);

             if (daysLeft == 6 || daysLeft  == 3 || daysLeft == 1) {
           
                 Long actualHodId = departmentRepository.findHodIdForEmployee(employee.getEmpId());
                 
                 if (hodId.equals(actualHodId)) {
                     EmployeeDTO dto = new EmployeeDTO();
                     dto.setEmpId(employee.getEmpId());
                     dto.setName(employee.getName());

                     Object[] departmentInfo = employeeRepository.getDepartmentRow(employee.getEmpId());
                     if (departmentInfo != null && departmentInfo.length > 0) {
                         dto.setDepartmentName((String) departmentInfo[1]);
                     }
                     
              
                     dto.setHodName(employeeRepository.findEmployeeNameById(hodId));
                     
                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                     dto.setEmployeeConfirmationDate(confirmationDate.format(formatter));

                     employeesNearingEnd.add(dto);
                 }
             }
         }

         response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
         response.setServiceResponse(employeesNearingEnd);
         apiLogInfo.setApiResponse("Successfully retrieved " + employeesNearingEnd.size() + " employees for probation reminder for HOD ID: " + hodId);
         apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

     } catch (Exception e) {
         e.printStackTrace();
         response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
         response.setServiceResponse("An error occurred while fetching probation reminders: " + e.getMessage());
         apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
         apiLogInfo.setLogLevel("ERROR");
     }

     logService.logMyInfo(httpRequest, apiLogInfo);
     return response;
 }
    
 
 public ServiceResponse confirmEmployeeFromProbation(EmployeeDTO employeeDto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/confirmEmployeeFromProbation");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        String cacheKey = "allEmployees";
	        EmployeeDTO cachedEmployee = null;
	        
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            cachedEmployee = cachedEmployees.stream()
	                    .filter(emp -> emp.getEmpId().equals(employeeDto.getEmpId()))
	                    .findFirst()
	                    .orElse(null);
	                    
	            if (cachedEmployee != null) {
	                apiLogInfo.setApiResponse("Employee data retrieved from cache for empId: " + employeeDto.getEmpId());
	            }
	        }
	       
	        if (cachedEmployee == null) {
	            Optional<Employee> employeeOptional = employeeRepository.findByIdAndStatusNot(employeeDto.getEmpId(), "Confirmed");
	            if (employeeOptional.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Employee not found with ID " + employeeDto.getEmploymentId() + ", or is already in 'Confirmed' status.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	            
	            Employee employee = employeeOptional.get();
	            
	            cachedEmployee = new EmployeeDTO();
	            cachedEmployee.setEmpId(employee.getEmpId());
	            cachedEmployee.setName(employee.getName());
	            cachedEmployee.setProbationPeriod(employee.getProbationPeriod());
	            cachedEmployee.setDateOfJoining(employee.getDateOfJoining().toString());
	            cachedEmployee.setEmploymentstatus(employee.getEmploymentstatus());
	            
	            Object[] department = employeeRepository.getDepartmentRow(Long.parseLong(employee.getEmpId().toString()));
	            if (department != null && department.length > 0) {
	                cachedEmployee.setDepartmentId(Long.parseLong(department[0].toString()));
	            }
	            	            
	            apiLogInfo.setApiResponse("Employee data retrieved from database for empId: " + employeeDto.getEmpId());
	        }

	        Long actualHodIdOptional;
	        if (cachedEmployee.getHodId() != null) {
	            actualHodIdOptional = cachedEmployee.getHodId();
	        } else {
	            actualHodIdOptional = departmentRepository.findHodIdForEmployee(employeeDto.getEmpId());
	        }

	        if (actualHodIdOptional == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Could not determine the HOD for employee " + employeeDto.getEmploymentId() + ". The employee may not be assigned to a department.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (!actualHodIdOptional.equals(employeeDto.getHodId())) {
	            String hodName = employeeRepository.findEmployeeNameById(actualHodIdOptional);
	            String currentHod = employeeRepository.findEmployeeNameById(employeeDto.getHodId());

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("User " + currentHod + " is not the authorized HOD for this employee. The correct HOD is " + hodName + ".");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	     
	        if ("Confirmed".equals(cachedEmployee.getEmploymentstatus())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " is already in 'Confirmed' status.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }
	        Float noOfDays = employeeRepository.getNOOfDays(employeeDto.getEmpId());
LocalDate probationEndDate = LocalDate.parse(cachedEmployee.getDateOfJoining())
     .plusDays(cachedEmployee.getProbationPeriod())
     .plusDays(Math.round(noOfDays));
			        LocalDate today = LocalDate.now();

	        Employee employee = employeeRepository.findByEmpId(employeeDto.getEmpId());
	        if (employee == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Employee with ID " + employeeDto.getEmpId() + " not found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (today.isBefore(probationEndDate)) {
	            employee.setIsConfirmedClicked(1L);
	            LocalDate date = LocalDate.parse(probationEndDate.toString()); 
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	            String formattedDate = date.format(formatter);
	            response.setServiceResponse("Employee will be confirmed on " + formattedDate);
	            
	            employeeRepository.save(employee);
	            System.out.print("++++++++++++++++++++++++++++++++++++++++++++++"+employee.getIsConfirmedClicked());
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            if (employeeCache.containsKey(cacheKey)) {
		            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
		            for (EmployeeDTO emp : cachedEmployees) {
		                if (emp.getEmpId().equals(employeeDto.getEmpId())) {
		                    emp.setEmploymentstatus("Probation");
		                    emp.setIsConfirmedClicked(1L);
		                    emp.setUpdatedOn(LocalDateTime.now().toString());
		                    emp.setUpdatedBy(employeeDto.getHodId());
		                    if (employeeDto.getReasonOfExtension() != null) {
		                        emp.setReasonOfExtension(employeeDto.getReasonOfExtension());
		                    }
		                    break;
		                }
		            }
		            employeeCache.put(cacheKey, cachedEmployees);
		            apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Cache updated with confirmation data");
		        }
	            return response;

	            
	        } else if (today.isAfter(probationEndDate)) {
	            if (!StringUtils.hasText(employeeDto.getReasonOfExtension())) { 
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Confirmation is after the probation end date. An extension reason is required.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	            employee.setReasonOfExtension(employeeDto.getReasonOfExtension());
	        }

	        employee.setIsConfirmedClicked(1L);
	        employee.setEmploymentstatus("Confirmed");
	        employee.setUpdatedOn(LocalDateTime.now());
	        employee.setUpdatedBy(employeeDto.getHodId().intValue());
	        employeeRepository.save(employee);
	        
	        sendConfirmationSuccessEmail(employee);

	        
	        if (employeeCache.containsKey(cacheKey)) {
	            List<EmployeeDTO> cachedEmployees = employeeCache.get(cacheKey);
	            for (EmployeeDTO emp : cachedEmployees) {
	                if (emp.getEmpId().equals(employeeDto.getEmpId())) {
	                    emp.setEmploymentstatus("Confirmed");
	                    emp.setIsConfirmedClicked(1L);
	                    emp.setUpdatedOn(LocalDateTime.now().toString());
	                    emp.setUpdatedBy(employeeDto.getHodId());
	                    if (employeeDto.getReasonOfExtension() != null) {
	                        emp.setReasonOfExtension(employeeDto.getReasonOfExtension());
	                    }
	                    break;
	                }
	            }
	            employeeCache.put(cacheKey, cachedEmployees);
	            apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Cache updated with confirmation data");
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Status for employee '" + cachedEmployee.getName() + "' has been successfully changed to Confirmed.");
	        apiLogInfo.setApiResponse(apiLogInfo.getApiResponse() + " | Successfully confirmed empId: " + cachedEmployee.getEmpId());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("An internal error occurred: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        apiLogInfo.setLogLevel("ERROR");
	        response.setServiceError(e.getMessage());
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	
	public  ServiceResponse submitForDelay(EmployeeDTO employeeDto)
	{
		 ServiceResponse response = new ServiceResponse();
	     LogDTO apiLogInfo = new LogDTO();
	     apiLogInfo.setApiUrl("/api/submitForDelay");
      apiLogInfo.setLogLevel("INFO");
      try {
     	 
     	 String employeeRole = employeeRepository.getEmployeeRoleByEmpId(employeeDto.getHodId());

     	
     	 Optional<Employee> employeeOptional = employeeRepository.findById(employeeDto.getEmpId());
     	 
     	 Employee employee = employeeOptional.get();
     	 if(employeeDto.getReasonOfExtension()!=null)
     	 {
     		if("HR".equalsIgnoreCase(employeeRole)  || "HOD".equalsIgnoreCase(employeeRole) || "superAdmin".equalsIgnoreCase(employeeRole) ) {
     		employee.setReasonOfExtension(employeeDto.getReasonOfExtension());
             employee.setUpdatedOn(LocalDateTime.now());
             employee.setUpdatedBy(employeeDto.getHodId().intValue());
             employeeRepository.save(employee);	
             
             
             response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
             response.setServiceResponse("Delay for '" + employee.getName() + "' has been successfully sent.");
             apiLogInfo.setApiResponse("Reason has been submitted for: " + employee.getEmpId());
             apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
     		}
     		else
     		{
     			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                 response.setServiceResponse("Current User should be HOD or HR");
                 apiLogInfo.setApiResponse("Reason can not be submitted for: " + employee.getEmpId());
                 apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
     		}
     	 }
     	 else
     	 {
     		 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
              response.setServiceResponse("An delay reason is required.");
              apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
              logService.logMyInfo(httpRequest, apiLogInfo);
     	 }
     	 
     	 return response;
      }catch(Exception e) {
     	 e.printStackTrace();
          response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
          response.setServiceResponse("An internal error occurred: " + e.getMessage());
          apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
          apiLogInfo.setLogLevel("ERROR");
          response.setServiceError(e.getMessage());
      }
		 return null;
	}
	

 
 private void sendConfirmationSuccessEmail(Employee employee) {
	    if (employee == null || employee.getEmpId() == null) {
	        return;
	    }

	    try {
	        String employeeEmail = employeeRepository.getMailByEmpId(employee.getEmpId());
	        if (employeeEmail == null || employeeEmail.isEmpty()) {
	            return;
	        }

	        String departmentName = employeeRepository.getDepartment(employee.getEmpId());
	        Long hodId = departmentRepository.findHodIdForEmployee(employee.getEmpId());
	        String hodName = "Management";
	        if (hodId != null) {
	            hodName = employeeRepository.findEmployeeNameById(hodId);
	        }

	        String subject = "Congratulations on Your Confirmation";

	        String effectiveDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

	        StringBuilder body = new StringBuilder();
	        body.append("<html><body>");
	        body.append("<p>Dear ").append(employee.getName()).append(",</p>");
	        body.append("<p>We are delighted to inform you that following the successful completion of your probationary period, your employment status has been confirmed, effective ").append(effectiveDate).append(".</p>");
	        body.append("<p>Your performance and dedication have been highly valued, and we are excited to have you as a permanent member of the ").append(departmentName != null ? departmentName : "team").append(".</p>");
	        body.append("<p>We look forward to your continued contributions and a successful journey with us.</p>");
	        body.append("<p>Congratulations once again!</p><br/>");
	        body.append("<p>Best regards,</p>");
	        body.append("<b>").append(hodName).append("</b><br/>");
	        if(departmentName != null) {
	             body.append("Head of ").append(departmentName).append("<br/>");
	        }
	        body.append("<br/><br/><hr/>");
	        body.append("<p><i>This is an auto-generated email. Please do not reply.</i></p>");
	        body.append("<a href=\"https://ishine.apmosys.com/\">Visit iShine Portal</a>");
	        body.append("</body></html>");

	        mailService.sendMail(employeeEmail, subject, body.toString());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
private Map<String, String> getDateRangeForList(String dateRangeType) {
    Map<String, String> dateRange = new HashMap<>();
    LocalDate currentDate = LocalDate.now();
    
    switch (dateRangeType.toLowerCase()) {
        case "within1month":
            LocalDate expiredWithin1Month = currentDate.minusMonths(1);
            dateRange.put("fromDate", expiredWithin1Month.plusDays(1).toString());
            dateRange.put("toDate", currentDate.toString());
            break;
            
        case "1to2months":
            LocalDate expired1To2Month = currentDate.minusMonths(2);
            LocalDate expiredWithin1MonthEnd = currentDate.minusMonths(1);
            dateRange.put("fromDate", expired1To2Month.plusDays(1).toString());
            dateRange.put("toDate", expiredWithin1MonthEnd.toString());
            break;
            
        case "2to3months":
            LocalDate expired2To3Month = currentDate.minusMonths(3);
            LocalDate expired1To2MonthEnd = currentDate.minusMonths(2);
            dateRange.put("fromDate", expired2To3Month.plusDays(1).toString());
            dateRange.put("toDate", expired1To2MonthEnd.toString());
            break;
            
        case "3to6months":
            LocalDate expired3To6Month = currentDate.minusMonths(6);
            LocalDate expired2To3MonthEnd = currentDate.minusMonths(3);
            dateRange.put("fromDate", expired3To6Month.plusDays(1).toString());
            dateRange.put("toDate", expired2To3MonthEnd.toString());
            break;
            
        case "6to9months":
            LocalDate expired6To9Month = currentDate.minusMonths(9);
            LocalDate expired3To6MonthEnd = currentDate.minusMonths(6);
            dateRange.put("fromDate", expired6To9Month.plusDays(1).toString());
            dateRange.put("toDate", expired3To6MonthEnd.toString());
            break;
            
        case "9to12months":
            LocalDate expired9To12Month = currentDate.minusMonths(12);
            LocalDate expired6To9MonthEnd = currentDate.minusMonths(9);
            dateRange.put("fromDate", expired9To12Month.plusDays(1).toString());
            dateRange.put("toDate", expired6To9MonthEnd.toString());
            break;
            
        case "above12months":
            LocalDate expiredAbove12Month = currentDate.minusYears(20);
            LocalDate expired9To12MonthEnd = currentDate.minusMonths(12);
            dateRange.put("fromDate", expiredAbove12Month.toString());
            dateRange.put("toDate", expired9To12MonthEnd.toString());
            break;
            
        case "total":
            dateRange.put("fromDate", null);
            dateRange.put("toDate", null);
            break;
            
        default:
            LocalDate defaultExpired = currentDate.minusMonths(1);
            dateRange.put("fromDate", defaultExpired.plusDays(1).toString());
            dateRange.put("toDate", currentDate.toString());
            break;
    }
    
    return dateRange;
}

private Map<String, String> getDateRangeForList1(String dateRangeType) {
    Map<String, String> dateRange = new HashMap<>();
    LocalDate currentDate = LocalDate.now();
    
    // Pre-calculate all future dates
    LocalDate expiredWithin1Month = currentDate.plusMonths(1);
    LocalDate expired1To2Month = currentDate.plusMonths(2);
    LocalDate expired2To3Month = currentDate.plusMonths(3);
    LocalDate expired3To6Month = currentDate.plusMonths(6);
    LocalDate expired6To9Month = currentDate.plusMonths(9);
    LocalDate expired9To12Month = currentDate.plusMonths(12);
    LocalDate expiredAbove12Month = currentDate.plusYears(20);
    
    // Pre-calculate all date ranges
    String toDate1Month = expiredWithin1Month.plusDays(1).toString();
    String fromDate1Month = currentDate.toString();
    
    String toDate2Month = expired1To2Month.plusDays(1).toString();
    String fromDate2Month = expiredWithin1Month.toString();
    
    String toDate3Month = expired2To3Month.plusDays(1).toString();
    String fromDate3Month = expired1To2Month.toString();
    
    String toDate6Month = expired3To6Month.plusDays(1).toString();
    String fromDate6Month = expired2To3Month.toString();
    
    String toDate9Month = expired6To9Month.plusDays(1).toString();
    String fromDate9Month = expired3To6Month.toString();
    
    String toDate12Month = expired9To12Month.plusDays(1).toString();
    String fromDate12Month = expired6To9Month.toString();
    
    String fromDateAbove12Month = expired9To12Month.toString();
    String toDateAbove12Month = expiredAbove12Month.toString();
    
    switch (dateRangeType.toLowerCase()) {
        case "within1month":
            dateRange.put("fromDate", fromDate1Month);
            dateRange.put("toDate", toDate1Month);
            break;
            
        case "1to2months":
            dateRange.put("fromDate", fromDate2Month);
            dateRange.put("toDate", toDate2Month);
            break;
            
        case "2to3months":
            dateRange.put("fromDate", fromDate3Month);
            dateRange.put("toDate", toDate3Month);
            break;
            
        case "3to6months":
            dateRange.put("fromDate", fromDate6Month);
            dateRange.put("toDate", toDate6Month);
            break;
            
        case "6to9months":
            dateRange.put("fromDate", fromDate9Month);
            dateRange.put("toDate", toDate9Month);
            break;
            
        case "9to12months":
            dateRange.put("fromDate", fromDate12Month);
            dateRange.put("toDate", toDate12Month);
            break;
            
        case "above12months":
            dateRange.put("fromDate", fromDateAbove12Month);
            dateRange.put("toDate", toDateAbove12Month);
            break;
            
        default:
            // Default to within 1 month
            dateRange.put("fromDate", fromDate1Month);
            dateRange.put("toDate", toDate1Month);
            break;
    }
    
    return dateRange;
}

//	public ServiceResponse fetchInactivePOListOfEmployee(EmployeeDTO employeeDTO) {
//		
//		 ServiceResponse response = new ServiceResponse();
//		    LogDTO apiLogInfo = new LogDTO();
//		    apiLogInfo.setApiUrl("/api/fetchInactivePOListOfEmployee");
//		    apiLogInfo.setLogLevel("INFO");
//
//		    try {
//		    	
//		    	if(employeeDTO.getTabName().equals("Employee")) {
//		    	
//		        List<Object[]> optionalEmployeeList = employeeRepository.fetchInActivePOListOfEmployee(employeeDTO.getPoProjectType(),employeeDTO.getDays(),employeeDTO.getDeptId());
//				List<EmployeeDTO> countOfInActivePoEmployeeWise = new ArrayList<EmployeeDTO>();
//		        if (!optionalEmployeeList.isEmpty()) {
//		        	for(Object[] object : optionalEmployeeList) {
//
//		        		EmployeeDTO employeeDetails = new EmployeeDTO();
//		        		employeeDetails.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);	
//		        		employeeDetails.setName(object[1] != null ? object[1].toString() : null);
//		        		employeeDetails.setProjectName(object[4] != null ? object[4].toString() : null);
//		        		employeeDetails.setPoNo(object[7] != null ? object[7].toString() : null);
//		        		employeeDetails.setPoStartDate(object[5] != null ? object[5].toString() : null);
//		        		employeeDetails.setPoEndDate(object[6] != null ? object[6].toString() : null);
//		        		employeeDetails.setClientName(object[8] != null ? object[8].toString() : null);
//		        		employeeDetails.setClientLocation(object[9] != null ? object[9].toString() : null);	
//		        		employeeDetails.setDepartmentName(object[10] != null ? object[10].toString() : null);	
//		        		employeeDetails.setPoProjectType(object[11] != null ? object[11].toString() : null);        		
//		                countOfInActivePoEmployeeWise.add(employeeDetails);
//		                			                   
//		                };
//
//		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		            response.setServiceResponse(countOfInActivePoEmployeeWise);
//		            apiLogInfo.setApiResponse(" fetched of size: " + countOfInActivePoEmployeeWise.size());
//		        } else {
//		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		            response.setServiceResponse("fetchInactivePOListOfEmployee  is empty.");
//		            apiLogInfo.setApiResponse("fetchInactivePOListOfEmployee is empty.");
//		        }
//		    	}else {
//		    		  List<Object[]> poProjectInActiveListProject = employeeRepository.fetchInActivePOListOfProject(employeeDTO.getPoProjectType(),employeeDTO.getDays(),employeeDTO.getDeptId());
//		    		  List<ProjectFetchDTO> countOfInActivePoProjectWise = new ArrayList<ProjectFetchDTO>();
//				        if (!poProjectInActiveListProject.isEmpty()) {
//				        	for(Object[] object : poProjectInActiveListProject) {
//
//				        		ProjectFetchDTO projectDetails = new ProjectFetchDTO();
//				        		projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
//				        		projectDetails.setPoNo(object[1] != null ? object[1].toString() : null);
//				        		projectDetails.setPoProjectType(object[2] != null ? object[2].toString() : null); 
//				        		projectDetails.setPoStartDate(object[3] != null ? object[3].toString() : null);
//				        		projectDetails.setPoEndDate(object[4] != null ? object[4].toString() : null);	
//				        		projectDetails.setClientRM(object[5] != null ? object[5].toString() : null);
//				        		projectDetails.setApmosysRM(object[6] != null ? object[6].toString() : null);
//				        		projectDetails.setClientName(object[7] != null ? object[7].toString() : null);
//				        		projectDetails.setClientLocation(object[8] != null ? object[8].toString() : null);
//				        		countOfInActivePoProjectWise.add(projectDetails);      		                   
//				               };
//
//				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				            response.setServiceResponse(countOfInActivePoProjectWise);
//				            apiLogInfo.setApiResponse(" fetched of size: " + countOfInActivePoProjectWise.size());
//				        } else {
//				            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				            response.setServiceResponse("countOfInActive  is empty.");
//				            apiLogInfo.setApiResponse("countOfInActive is empty.");
//				        }
//		    	}
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        response.setServiceResponse("An error occurred while processing the request.");
//		        apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		        apiLogInfo.setLogLevel("ERROR");
//		        response.setServiceError(e.getMessage());
//		    }
//
//		    logService.logMyInfo(httpRequest, apiLogInfo);
//		    return response;
//	}
	
public ServiceResponse getAllEmployeesByDepartmentIds(EmployeeDTO employeedto) {
    ServiceResponse response = new ServiceResponse();
    
    
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setApiUrl("/api/getAllEmployeesByDepartmentIds");
    apiLogInfo.setLogLevel("INFO");

    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append("departmentList : ").append(employeedto.getDepartmentList());

    try {
        Optional.ofNullable(employeedto.getDepartmentList()).ifPresentOrElse(departmentList -> {

            if (departmentList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Department list is empty.");

                apiLogInfo.setApiResponse("Department list is empty.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                List<Long> deptIds = departmentList.stream()
                    .map(department -> department.getDeptId())
                    .collect(Collectors.toList());
                	
                List<EmployeeDTO> employeeList ;
                Map<Long, EmployeeDTO> uniqueEmployees = new LinkedHashMap<>();
                boolean hasMoreData = true;
                
                if ((employeedto.getIsEmpLeaveExclusion()!=null && employeedto.getIsEmpLeaveExclusion()) ||
                		(employeedto.getIsEmpLeaveInclusion() != null && employeedto.getIsEmpLeaveInclusion())) {
                	
                    int currentPage = employeedto.getPage();
                    int pageSize = employeedto.getSize();
                    String sortcolumn =mapSortColumn(employeedto.getSortColumn());
                    Map<String, String> filters=employeedto.getFilters();
                    
                    String empId = filters.getOrDefault("empId", null);
                    String name = filters.getOrDefault("name", null);
                    String jobRoleId = filters.getOrDefault("jobRoleId", null);
                    String deptName = filters.getOrDefault("deptId", null);
                    String projectName = filters.getOrDefault("projectName", null);
                    String billableType = filters.getOrDefault("billableType", null);

                    // Fetch pages and ensure distinct empId up to pageSize
                    while (uniqueEmployees.size() < pageSize && hasMoreData) {
                        Pageable pageable = PageRequest.of(currentPage, pageSize, 
                        		Sort.by(Sort.Direction.fromString(employeedto.getSortDirection()),
                        				sortcolumn));

                        Page<EmployeeDTO> pageResult;

                        if (employeedto.getIsEmpLeaveExclusion()) {
                            pageResult = employeeRepository.getAllEmployeesByDepartmentIdsForLeaveExclusion(
                            	    deptIds,empId,name,jobRoleId,deptName,projectName,billableType,pageable);
                        } else {
                            pageResult = employeeRepository.getAllEmployeesByDepartmentIdsForLeaveInclusion(
                            		deptIds,empId,name,jobRoleId,deptName,projectName,billableType,pageable);
                        }
                        response.setTotalEle(pageResult.getTotalElements());
                        List<EmployeeDTO> content = pageResult.getContent();
                        for (EmployeeDTO emp : content) {
                            uniqueEmployees.putIfAbsent(emp.getEmpId(), emp);
                            if (uniqueEmployees.size() >= pageSize) break;
                        }

                        hasMoreData = pageResult.hasNext();
                        currentPage++;
                    }
                    employeeList = new ArrayList<>(uniqueEmployees.values());

                } else {
                    employeeList = employeeRepository.getAllEmployeesByDepartmentIds(deptIds);
                }

                List<EmployeeDTO> finalList = employeeList;               
                Optional.ofNullable(finalList).ifPresent(list -> {
                    if (list.isEmpty()) {
                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                        response.setServiceResponse("Employee list is empty.");

                        apiLogInfo.setApiResponse("Employee list is empty");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                    } else {
                        List<EmployeeDTO> dtoList = new ArrayList<>();
                        
                        if ((employeedto.getIsEmpLeaveExclusion()!=null && employeedto.getIsEmpLeaveExclusion()) ||
                        		(employeedto.getIsEmpLeaveInclusion() != null && employeedto.getIsEmpLeaveInclusion())) {
                        	
                        	Map<Long, List<EmployeeDTO>> groupedByEmp = finalList.stream()
                        	        .collect(Collectors.groupingBy(EmployeeDTO::getEmpId));

                        	    groupedByEmp.values().forEach(empList -> {
                        	        EmployeeDTO first = empList.get(0);

                        	        String combinedProjects = empList.stream()
                        	            .map(EmployeeDTO::getProjectName)
                        	            .filter(Objects::nonNull)
                        	            .distinct()
                        	            .collect(Collectors.joining(", "));

                        	        EmployeeDTO dto = new EmployeeDTO();
                        	        dto.setEmpId(first.getEmpId());
                        	        dto.setName(first.getName());
                        	        dto.setJobRoleName(first.getJobRoleName());
                        	        dto.setDepartmentId(first.getDepartmentId());
                        	        dto.setDepartmentName(first.getDepartmentName());
                        	        dto.setEmploymentId(first.getEmploymentId());
                        	        dto.setCreatedOn(first.getCreatedOn());
                        	        dto.setUpdatedOn(first.getUpdatedOn());
                        	        dto.setCreatedByName(first.getCreatedByName());
                        	        dto.setProjectName(combinedProjects);  
                        	        dto.setBillableType(first.getBillableType());
                        	        dtoList.add(dto);
                        	    });
                       }else {

                        list.forEach(emp -> {
                            EmployeeDTO dto = new EmployeeDTO();
                            dto.setEmpId(emp.getEmpId());
                            dto.setName(emp.getName());
                            dto.setJobRoleName(emp.getJobRoleName());
                            dto.setDepartmentId(emp.getDepartmentId());
                            dto.setDepartmentName(emp.getDepartmentName());
                            dto.setEmploymentId(emp.getEmploymentId());
                            dtoList.add(dto);
                        });
                        }
                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                        response.setServiceResponse(dtoList);

                        apiLogInfo.setApiResponse("dtoList : " + dtoList);
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                    }
                });
            }

        }, () -> {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Department list is null");

            apiLogInfo.setApiResponse("Department list is null");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        });

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something Went Wrong.");
        response.setServiceError(e.getMessage());

        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        apiLogInfo.setLogLevel("ERROR");
    }

    apiLogInfo.setApiRequest(logBuilder.toString());
    logService.logMyInfo(httpRequest, apiLogInfo);

    return response;
}

public String mapSortColumn(String column) {
    switch (column) {
        case "name": return "name";
        case "empId": return "empId";
        case "jobRoleId": return "jr.jobRoleId";
        case "deptId": return "d.deptId";
        case "projectName": return "p.projectName";
        case "billableType": return "billableType";
        case "createdOn": return "ee.createdOn";
        case "updatedOn": return "ee.updatedOn";
        case "created_by_name": return "cb.name";
        default: return "name";
    }
}

@Transactional(rollbackFor = Exception.class)
public ServiceResponse bulkSkillCertficate(SkillCertConfigDTO dto,MultipartFile doc1) throws EncryptedDocumentException, InvalidFormatException {
	 ServiceResponse response = new ServiceResponse();
	 List<String> errorMessages = new ArrayList<>();
	    int rowNum = 1;
	
	try (Workbook workbook = WorkbookFactory.create(doc1.getInputStream())) {
		Sheet sheet = workbook.getSheetAt(0);
       Iterator<Row> rows = sheet.iterator();
       rows.next(); 

       while (rows.hasNext()) {
           Row row = rows.next();
           rowNum++;
           try {
               String empIdentifier = row.getCell(0).getStringCellValue().trim();
               String skillsRaw = row.getCell(1).getStringCellValue().trim();
               String proficiencyStr = row.getCell(2).getStringCellValue().trim();

            
               Long empId = resolveEmployeeId(empIdentifier);
               if (empId == null) {
                   errorMessages.add("Row " + rowNum + ": Employee " + empIdentifier + " not found.");
                   continue;
               }

               // Step 2: Proficiency mapping
               Optional<Proficiency> optProf = proficiencyRepository.findByProficiencyNameIgnoreCase(proficiencyStr);
               if (!optProf.isPresent()) {
                   errorMessages.add("Row " + rowNum + ": Invalid proficiency '" + proficiencyStr + "'");
                   continue;
               }

               Long proficiencyId = optProf.get().getProficiencyId();
               if (proficiencyId == null) {
                   errorMessages.add("Row " + rowNum + ": Invalid proficiency " + proficiencyStr);
                   continue;
               }

               // Step 3: Existing Skills of Employee
               List<EmployeeSkillProficiencyDTO> existingSkills =
                       employeeSkillProficiencyMappingRepository.getAllSkillsByEmpId(empId);

               // Step 4: Process each skill
               for (String rawSkill : skillsRaw.split(",")) {
                   String skillName = rawSkill.trim();

                   // check if already exists
                   Optional<EmployeeSkillProficiencyDTO> existingSkillOpt = existingSkills.stream()
                       .filter(s -> (s.getSkillName() != null && 
                                     s.getSkillName().equalsIgnoreCase(skillName)) ||
                                    (s.getAdditionalSkill() != null && 
                                     s.getAdditionalSkill().equalsIgnoreCase(skillName)))
                       .findFirst();

                   if (existingSkillOpt.isPresent()) {
                       EmployeeSkillProficiencyDTO existing = existingSkillOpt.get();
                       if (!existing.getProficiencyId().equals(proficiencyId)) {
                           // update proficiency
                           EmployeeSkillProficiencyMapping mapping =
                                   employeeSkillProficiencyMappingRepository.findById(existing.getEmpSkillId()).get();
                           mapping.setProficiencyId(proficiencyId);
                           mapping.setUpdatedBy(dto.getUploadedBy());
                           mapping.setUpdatedOn(LocalDateTime.now());
                           employeeSkillProficiencyMappingRepository.save(mapping);
                       }
                   } else {
                       // Check predefined
                	   PredefinedSkills predef = predefinedSkillsRepository.findAll().stream()
                		        .filter(s -> normalizeName(s.getSkillName()).equals(normalizeName(skillName)))
                		        .findFirst()
                		        .orElse(null);
                       

                       EmployeeSkillProficiencyMapping newMapping = new EmployeeSkillProficiencyMapping();
                       newMapping.setEmpId(empId);
                       newMapping.setProficiencyId(proficiencyId);
                       newMapping.setCreatedBy(dto.getUploadedBy());
                       newMapping.setActive(true);

                       if (predef != null) {
                           newMapping.setSkillId(predef.getSkillId());
                       } else {
                           newMapping.setAdditionalSkill(skillName);
                       }

                       employeeSkillProficiencyMappingRepository.save(newMapping);
                   }
               }

           } catch (Exception e) {
               errorMessages.add("Row " + rowNum + ": Error processing row. " + e.getMessage());
           }
       }

       if (!errorMessages.isEmpty()) {
           response.setServiceStatus(ServiceResponse.STATUS_FAIL);
           response.setServiceResponse(String.join(" | ", errorMessages));
       } else {
           response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
           response.setServiceResponse("Skill excel file processed successfully.");
       }
	
}catch (IOException e) {
   e.printStackTrace();
   response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
   response.setServiceResponse("Something went wrong during file processing.");
}
	 
return response;
}


@Transactional(rollbackFor = Exception.class)
public ServiceResponse bulkSkillCertficateallTotal(SkillCertConfigDTO dto, MultipartFile doc1) 
        throws EncryptedDocumentException, InvalidFormatException {

    ServiceResponse response = new ServiceResponse();
    List<String> errorMessages = new ArrayList<>();
    List<BulkSkillRowData> validRows = new ArrayList<>();
    int rowNum = 1;

    try (Workbook workbook = WorkbookFactory.create(doc1.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next();

        
        while (rows.hasNext()) {
            Row row = rows.next();
            rowNum++;
            try {
                String empIdentifier = getCellValue(row, 0);
                String skillsRaw = getCellValue(row, 1);
                String proficiencyStr = getCellValue(row,2);

                List<String> rowErrors = new ArrayList<>();
                
                Map<String, String> fieldMap = Map.of(
                        "Employee Id", empIdentifier,
                        "Skills", skillsRaw,
                        "Proficiency", proficiencyStr
                    );

                    boolean hasNullField = false;
                    for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                        if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                            errorMessages.add("Row " + rowNum + ": Field '" + entry.getKey() + "' is missing or empty.");
                            hasNullField = true;
                        }
                    }

                    if (hasNullField) continue;

                Long empId = resolveEmployeeId(empIdentifier);
                if (empId == null) {
                    rowErrors.add("Employee '" + empIdentifier + "' not found");
                }

                Optional<Proficiency> optProf = proficiencyRepository.findByProficiencyNameIgnoreCase(proficiencyStr);
                if (!optProf.isPresent()) {
                    rowErrors.add("Invalid proficiency '" + proficiencyStr + "'");
                }

                Long proficiencyId = optProf.map(Proficiency::getProficiencyId).orElse(null);

               

                if (!rowErrors.isEmpty()) {
                    errorMessages.add("Row " + rowNum + ": " + String.join(", ", rowErrors));
                } else {
                    BulkSkillRowData data = new BulkSkillRowData();
                    data.setEmpId(empId);
                    data.setProficiencyId(proficiencyId);
                    data.setSkillsRaw(skillsRaw);
                    validRows.add(data);
                }

            } catch (Exception e) {
                errorMessages.add("Row " + rowNum + ": Error processing row. " + e.getMessage());
            }
        }

     
        if (!errorMessages.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(errorMessages);
            return response;
        }

       
        for (BulkSkillRowData data : validRows) {
            List<EmployeeSkillProficiencyDTO> existingSkills =
                    employeeSkillProficiencyMappingRepository.getAllSkillsByEmpId(data.getEmpId());

           

            for (String rawSkill : data.getSkillsRaw().split(",")) {
                String skillName = rawSkill;
                if (skillName.isEmpty()) continue;

                Optional<EmployeeSkillProficiencyDTO> existingSkillOpt = existingSkills.stream()
                	    .filter(s -> 
                	        (s.getSkillName() != null && 
                	         normalizeName(s.getSkillName()).equals(normalizeName(skillName))) 
                	        ||
                	        (s.getAdditionalSkill() != null &&
                	         normalizeName(s.getAdditionalSkill()).equals(normalizeName(skillName)))
                	    )
                	    .findFirst();


                if (existingSkillOpt.isPresent()) {
                    EmployeeSkillProficiencyDTO existing = existingSkillOpt.get();
                    if (!existing.getProficiencyId().equals(data.getProficiencyId())) {
                        Optional<EmployeeSkillProficiencyMapping> mappingOpt =
                                employeeSkillProficiencyMappingRepository.findById(existing.getEmpSkillId());
                        
                        if(mappingOpt.isPresent()) {                
                        	  EmployeeSkillProficiencyMapping mapping = mappingOpt.get();		
                        		mapping.setProficiencyId(data.getProficiencyId());
                        
                        mapping.setUpdatedBy(dto.getUploadedBy());
                        mapping.setUpdatedOn(LocalDateTime.now());
                        employeeSkillProficiencyMappingRepository.save(mapping);
                        }else {
                        	 throw new EntityNotFoundException(
                                     "Mapping not found for empSkillId: " + existing.getEmpSkillId());
                        }
                    }
                } else {
                	  String normalizeSkillName = normalizeName(skillName);
                	  PredefinedSkills predef = predefinedSkillsRepository.findByNormalizedSkillName(normalizeSkillName);

                    EmployeeSkillProficiencyMapping newMapping = new EmployeeSkillProficiencyMapping();
                    newMapping.setEmpId(data.getEmpId());
                    newMapping.setProficiencyId(data.getProficiencyId());
                    newMapping.setCreatedBy(dto.getUploadedBy());
                    newMapping.setActive(true);

                   
                   

                    if (predef != null) {
                        newMapping.setSkillId(predef.getSkillId());
                        
                    } else {
                        newMapping.setAdditionalSkill(skillName);
                       
                    }

                    employeeSkillProficiencyMappingRepository.save(newMapping);
                   
                }
            }
        }

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse("Skill excel file processed successfully.");

    } catch (IOException e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something went wrong during file processing.");
    }

    return response;
}




@Transactional(rollbackFor = Exception.class)
public ServiceResponse uploadCertificateBulk(SkillCertConfigDTO dto,MultipartFile doc1) throws EncryptedDocumentException, InvalidFormatException, IOException {
	 ServiceResponse response = new ServiceResponse();
	 List<String> errorMessages = new ArrayList<>();
	
	
	    int rowNum = 1;
	
	try (Workbook workbook = WorkbookFactory.create(doc1.getInputStream())) {
		Sheet sheet = workbook.getSheetAt(0);
       Iterator<Row> rows = sheet.iterator();
       rows.next(); 

       while (rows.hasNext()) {
           Row row = rows.next();
           rowNum++;
           
        	   DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
               String empIdentifier = row.getCell(0).getStringCellValue().trim();
               String certificateName = row.getCell(1).getStringCellValue().trim();
               String specialization = row.getCell(2).getStringCellValue().trim();
               String deptName = row.getCell(3).getStringCellValue().trim();
               String proficiencyStr = row.getCell(4).getStringCellValue().trim();
               String issuingAuthority = row.getCell(5).getStringCellValue().trim();
               String validFromStr = row.getCell(6).getStringCellValue().trim();
               String expiresOnStr = row.getCell(7).getStringCellValue().trim();
               LocalDate validFrom = parseDate(validFromStr, rowNum, errorMessages);
               LocalDate expiresOn = parseDate(expiresOnStr, rowNum, errorMessages);
               if (validFrom == null || expiresOn == null) {
                  
                   throw new IllegalArgumentException("Row " + rowNum + ": Invalid date format.");
               }
               
               if (expiresOn.isBefore(LocalDate.now())) {
                   throw new IllegalArgumentException("Row " + rowNum + ": 'Expires On' date cannot be in the past");
               }
               String skillsRaw = row.getCell(8).getStringCellValue().trim();
               String driveLink = row.getCell(9).getStringCellValue().trim();
               
               
               if (empIdentifier == null || empIdentifier.isEmpty()) {
                   throw new IllegalArgumentException("Row " + rowNum + ": Employee Id missing.");
               }
               if (certificateName == null || certificateName.isEmpty()) {
                   throw new IllegalArgumentException("Row " + rowNum + ": Certificate Name missing.");
               }

               // 1) resolve employee
               Long empId = resolveEmployeeId(empIdentifier);
               if (empId == null) {
            	   throw new IllegalArgumentException("Row " + rowNum + ": Employee '" + empIdentifier + "' not found.");
               }
               
               Optional<Proficiency> optProf = proficiencyRepository.findByProficiencyNameIgnoreCase(proficiencyStr);
               if (!optProf.isPresent()) {
            	   throw new IllegalArgumentException("Row " + rowNum + ": Invalid proficiency '" + proficiencyStr + "'");
               }

               Long proficiencyId = optProf.get().getProficiencyId();
               if (proficiencyId == null) {
            	   throw new IllegalArgumentException("Row " + rowNum + ": Invalid proficiency " + proficiencyStr);
               }
               
               
               List<GetDeptIdByRoleDTO> departments = departmentRepository.findAllDepartmentsForSA();
               String normalizedExcelName = normalizeName(deptName);

               Long deptId = departments.stream()
                       .filter(d -> normalizeName(d.getName()).equals(normalizedExcelName))
                       .map(GetDeptIdByRoleDTO::getDeptId)
                       .findFirst()
                       .orElse(null); 
               if (deptId == null) {
            	   throw new IllegalArgumentException("Row " + rowNum + ": Department '" + deptName + "' not found.");
               }
               Long driveId = null;
               if (driveLink != null && !driveLink.isEmpty()) {
                   CertificateDriveLinkMapping drive = new CertificateDriveLinkMapping();
                   drive.setEmpId(empId);
                   drive.setDriveLink(driveLink);
                   drive.setDrcreatedBy(dto.getUploadedBy());
                   CertificateDriveLinkMapping savedDrive = certificateDriveLinkMappingRepository.save(drive);
                   driveId = savedDrive.getDriveId();
               }
               
             

             

              
               
               EmployeeCertificates cert = new EmployeeCertificates();
               cert.setEmpId(empId);
               cert.setCertificateName(certificateName);
               cert.setSpecialization(specialization);
               cert.setDeptId(deptId);
               cert.setProficiencyId(proficiencyId);
               cert.setIssuingAuthority(issuingAuthority);
               cert.setValidFrom(validFrom);
               cert.setExpiresOn(expiresOn);
               cert.setCActive(true);
               cert.setDriveId(driveId);
               cert.setCreatedBy(dto.getUploadedBy());
               cert.setCertificateStatus("Active");       
               EmployeeCertificates savedCert = employeeCertificatesRepository.save(cert);

               Long employeeCertificateId = savedCert.getEmployeeCertificateId();
               
               List<EmployeeSkillProficiencyDTO> skillsToSync = new ArrayList<>();
               if (skillsRaw != null && !skillsRaw.trim().isEmpty()) {
                   String[] skillParts = skillsRaw.split(",");
                   for (String s : skillParts) {
                       String skillName = s.trim();
                       if (skillName.isEmpty()) continue;

                       
                       PredefinedSkills predef = predefinedSkillsRepository.findAll().stream()
                               .filter(ps -> normalizeName(ps.getSkillName()).equals(normalizeName(skillName)))
                               .findFirst()
                               .orElse(null);

                       CertificateSkillMapping csm = new CertificateSkillMapping();
                       csm.setEmployeeCertificateId(employeeCertificateId);
                       csm.setProficiencyId(proficiencyId);
                       csm.setEmpId(empId);
                       csm.setScActive(true);
                       csm.setCscreatedBy(dto.getUploadedBy());

                       if (predef != null) {
                           csm.setSkillId(predef.getSkillId());
                       
                           EmployeeSkillProficiencyDTO skillDto = new EmployeeSkillProficiencyDTO();
                           skillDto.setEmpId(empId);
                           skillDto.setSkillId(predef.getSkillId());
                           skillDto.setAdditionalSkill(null);
                           skillsToSync.add(skillDto);
                       } else {
                           
                           csm.setAdditionalSkill(skillName);
                           EmployeeSkillProficiencyDTO skillDto = new EmployeeSkillProficiencyDTO();
                           skillDto.setEmpId(empId);
                           skillDto.setSkillId(null);
                           skillDto.setAdditionalSkill(skillName);
                           skillsToSync.add(skillDto);
                       }

                       certificateSkillMapRepository.save(csm);
                   } 
               }

               // 7) Sync employee skills table for these skills (missing skills should be added/updated)
               if (!skillsToSync.isEmpty()) {
                 
                   syncEmployeeSkills(empId, proficiencyId, dto.getUploadedBy(), skillsToSync);
               } 
               
               
          
       }
       response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
       response.setServiceResponse("Certificate excel file processed successfully.");

	
}catch (Exception e) {
	e.printStackTrace();
	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
     response.setServiceResponse("Error processing excel file: " + e.getMessage());
    
    
}
	return response;
           
	
}


//total valid then insertion certifiacte //
@Transactional(rollbackFor = Exception.class)
public ServiceResponse uploadCertificateBulkallTotal(SkillCertConfigDTO dto, MultipartFile doc1) 
        throws EncryptedDocumentException, InvalidFormatException, IOException {

    ServiceResponse response = new ServiceResponse();
    List<String> errorMessages = new ArrayList<>();
    List<RowData> validRows = new ArrayList<>();
    Map<String, List<Integer>> duplicatesMap = new HashMap<>();
    Map<String, String> empIdMap = new HashMap<>();

    int rowNum = 1;

    try (Workbook workbook = WorkbookFactory.create(doc1.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next(); 

        
        while (rows.hasNext()) {
            Row row = rows.next();
            rowNum++;

            String empIdentifier = getCellValue(row, 0);
            String certificateName = getCellValue(row, 1);
            String specialization = getCellValue(row, 2);
            String deptName = getCellValue(row, 3);
            String proficiencyStr = getCellValue(row, 4);
            String issuingAuthority = getCellValue(row, 5);
            String validFromStr = getCellValue(row, 6);
            String expiresOnStr = getCellValue(row, 7);
            String skillsRaw = getCellValue(row, 8);
            String driveLink = getCellValue(row, 9);

           
            
            Map<String, String> fieldMap = Map.of(
                    "Employee Id", empIdentifier,
                    "Certificate Name", certificateName,
                    "Specialization", specialization,
                    "Department Name", deptName,
                    "Proficiency", proficiencyStr,
                    "Issuing Authority", issuingAuthority,
                    "Valid From", validFromStr,
                    "Expires On", expiresOnStr
                );

                boolean hasNullField = false;
                for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                        errorMessages.add("Row " + rowNum + ": Field '" + entry.getKey() + "' is missing or empty.");
                        hasNullField = true;
                    }
                }

                if (hasNullField) continue;
                
                LocalDate validFrom = parseDate(validFromStr, rowNum, errorMessages);
               

                LocalDate expiresOn = null;
             

                if (expiresOnStr != null && !expiresOnStr.isEmpty()) {
                    expiresOn = parseDate(expiresOnStr, rowNum, errorMessages);

                    if (expiresOn != null && expiresOn.isBefore(LocalDate.now())) {
                        errorMessages.add("Row " + rowNum + ": 'Expires On' date cannot be in the past");
                        continue;
                    }
                }

            
            

            Long empId = resolveEmployeeId(empIdentifier);
            if (empId == null) {
                errorMessages.add("Row " + rowNum + ": Employee '" + empIdentifier + "' not found.");
                continue;
            }

            Optional<Proficiency> optProf = proficiencyRepository.findByProficiencyNameIgnoreCase(proficiencyStr);
            if (!optProf.isPresent()) {
                errorMessages.add("Row " + rowNum + ": Invalid proficiency '" + proficiencyStr + "'");
                continue;
            }
            Long proficiencyId = optProf.get().getProficiencyId();

//            List<GetDeptIdByRoleDTO> departments = departmentRepository.findAllDepartmentsForSA();
            String normalizedExcelName = normalizeName(deptName);
            Long deptId = departmentRepository.findByDepartmentnameIgnoreCase(normalizedExcelName);
//            Long deptId = departments.stream()
//                    .filter(d -> normalizeName(d.getName()).equals(normalizedExcelName))
//                    .map(GetDeptIdByRoleDTO::getDeptId)
//                    .findFirst()
//                    .orElse(null);

            if (deptId == null) {
                errorMessages.add("Row " + rowNum + ": Department '" + deptName + "' not found.");
                continue;
            }
            
            String key = (empIdentifier + "|" + certificateName + "|" + issuingAuthority)
                    .toLowerCase()
                    .replaceAll("\\s+", ""); 

            duplicatesMap.putIfAbsent(key, new ArrayList<>());
            duplicatesMap.get(key).add(rowNum);
            empIdMap.putIfAbsent(key, empIdentifier);
           
            validRows.add(new RowData(empId, certificateName, specialization, deptId, proficiencyId,
                    issuingAuthority, validFrom, expiresOn, skillsRaw, driveLink));
        }
        
        for (Map.Entry<String, List<Integer>> entry : duplicatesMap.entrySet()) {
            List<Integer> rowsList = entry.getValue();
            if (rowsList.size() > 1) {
                String empId = empIdMap.get(entry.getKey());
                errorMessages.add("Rows " + rowsList + " have the duplicate certificate entry for the employee id (" + empId + "), "
                        + "please keep only one valid record for this Employee-Certificate-Issuer combination.");
            }
        }

       
        if (!errorMessages.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(errorMessages);
            return response;
        }

      
        for (RowData data : validRows) {
        	
        	CertificateDTO existingCertificate = getExistingCertificateByNameAndAuthOfEmployee(data.getEmpId(),data.getCertificateName(),data.getIssuingAuthority());  

        	if(existingCertificate != null) {
             	
             	updateExistingCertificateIfNeeded(data,existingCertificate,dto);
        	}
        	else {
        	Long driveId = null;
            if (data.getDriveLink() != null && !data.getDriveLink().isEmpty()) {
                CertificateDriveLinkMapping drive = new CertificateDriveLinkMapping();
                drive.setEmpId(data.getEmpId());
                drive.setDriveLink(data.getDriveLink());
                drive.setDrcreatedBy(dto.getUploadedBy());
                drive.setDrActive(true);             
                driveId = certificateDriveLinkMappingRepository.save(drive).getDriveId();
            }

            
            EmployeeCertificates cert = new EmployeeCertificates();
            cert.setEmpId(data.getEmpId());
            cert.setCertificateName(data.getCertificateName());
            cert.setSpecialization(data.getSpecialization());
            cert.setDeptId(data.getDeptId());
            cert.setProficiencyId(data.getProficiencyId());
            cert.setIssuingAuthority(data.getIssuingAuthority());
            cert.setValidFrom(data.getValidFrom());
            cert.setExpiresOn(data.getExpiresOn());
            cert.setCActive(true);
            cert.setDriveId(driveId);
            cert.setCreatedBy(dto.getUploadedBy());
            cert.setCertificateStatus("Active");
            Long certificateId = employeeCertificatesRepository.save(cert).getEmployeeCertificateId();

            List<EmployeeSkillProficiencyDTO> skillsToSync = new ArrayList<>();
            if (data.getSkillsRaw() != null && !data.getSkillsRaw().trim().isEmpty()) {
                String[] skillParts = data.getSkillsRaw().split(",");
                for (String s : skillParts) {
                    String skillName = s;
                    if (skillName.isEmpty()) continue;
                    String normalizeSKillName = normalizeName(s);

                    CertificateSkillMapping csm = new CertificateSkillMapping();
                    csm.setEmployeeCertificateId(certificateId);
                    csm.setEmpId(data.getEmpId());
                    csm.setProficiencyId(data.getProficiencyId());
                    csm.setScActive(true);
                    csm.setCscreatedBy(dto.getUploadedBy());

                    PredefinedSkills predef = predefinedSkillsRepository.findByNormalizedSkillName(normalizeSKillName);

                    if (predef != null) {
                        csm.setSkillId(predef.getSkillId());
                        EmployeeSkillProficiencyDTO skillDto = new EmployeeSkillProficiencyDTO();
                        skillDto.setEmpId(data.getEmpId());
                        skillDto.setSkillId(predef.getSkillId());
                        skillDto.setAdditionalSkill(null);
                        skillsToSync.add(skillDto);
                    } else {
                        csm.setAdditionalSkill(skillName);
                        EmployeeSkillProficiencyDTO skillDto = new EmployeeSkillProficiencyDTO();
                        skillDto.setEmpId(data.getEmpId());
                        skillDto.setSkillId(null);
                        skillDto.setAdditionalSkill(skillName);
                        skillsToSync.add(skillDto);
                    }
                    certificateSkillMapRepository.save(csm);
                }
            }
            if (!skillsToSync.isEmpty()) {
                
                syncEmployeeSkills(data.getEmpId(), data.getProficiencyId(), dto.getUploadedBy(), skillsToSync);
            } 
        	}
            
        }

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse("Certificate excel file processed successfully.");

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Error processing excel file: " + e.getMessage());
    }

    return response;
}


private void updateExistingCertificateIfNeeded(RowData data, CertificateDTO existingCertificate,SkillCertConfigDTO dto) {
	 boolean certificateUpdated = false;
	 Long newDriveId = existingCertificate.getDriveId();
	    String existingDrive = existingCertificate.getDriveLink();
	    String newDriveLink = data.getDriveLink();
	    if (newDriveLink != null && !normalizeName(existingDrive).equals(normalizeName(newDriveLink))) {
	       
	        if (existingDrive != null) {
	            certificateDriveLinkMappingRepository.deactivateDriveLinkById(existingCertificate.getDriveId());
	        }

	       
	        CertificateDriveLinkMapping newDrive = new CertificateDriveLinkMapping();
	        newDrive.setEmpId(data.getEmpId());
	        newDrive.setDriveLink(newDriveLink);
	        newDrive.setDrcreatedBy(dto.getUploadedBy());
	        newDrive.setDrActive(true);
	        newDriveId = certificateDriveLinkMappingRepository.save(newDrive).getDriveId();
	        certificateUpdated = true;
	    }
	    
	    EmployeeCertificates cert = employeeCertificatesRepository.findById(existingCertificate.getEmployeeCertificateId()).orElse(null);
	    if (cert == null) return;

	    if ((data.getSpecialization() != null && !normalizeName(data.getSpecialization()).equals(normalizeName(cert.getSpecialization()))) ||
	        (data.getDeptId() != null && !data.getDeptId().equals(cert.getDeptId())) ||
	        (data.getProficiencyId() != null && !data.getProficiencyId().equals(cert.getProficiencyId())) ||
	        (data.getValidFrom() != null && !data.getValidFrom().equals(cert.getValidFrom())) ||
	        (data.getExpiresOn() != null && !data.getExpiresOn().equals(cert.getExpiresOn())) ||
	        (newDriveId != null && !newDriveId.equals(cert.getDriveId()))) {

	        cert.setSpecialization(data.getSpecialization());
	        cert.setDeptId(data.getDeptId());
	        cert.setProficiencyId(data.getProficiencyId());
	        cert.setValidFrom(data.getValidFrom());
	        cert.setExpiresOn(data.getExpiresOn());
	        cert.setDriveId(newDriveId);
	        cert.setUpdatedBy(dto.getUploadedBy());
	        employeeCertificatesRepository.save(cert);

	        certificateUpdated = true;
	    }
	    
	    List<EmployeeSkillProficiencyDTO> skillsFromcertificateToSkillTable = new ArrayList<>();
	    if (data.getSkillsRaw() != null && !data.getSkillsRaw().trim().isEmpty()) {
	        List<EmployeeSkillProficiencyDTO> existingSkills = existingCertificate.getSkills();
	        Set<Long> existingSkillIds = existingSkills.stream()
	                .filter(s -> s.getSkillId() != null)
	                .map(EmployeeSkillProficiencyDTO::getSkillId)
	                .collect(Collectors.toSet());
	        Set<String> existingAdditionalSkills = existingSkills.stream()
	                .filter(s -> s.getAdditionalSkill() != null)
	                .map(s -> normalizeName(s.getAdditionalSkill()))
	                .collect(Collectors.toSet());

	        String[] newSkillsArray = data.getSkillsRaw().split(",");
	        Set<Long> newSkillIds = new HashSet<>();
	        Set<String> newAdditionalSkills = new HashSet<>();

	        for (String s : newSkillsArray) {
	            String skillName = s;
	            if (skillName.isEmpty()) continue;
	            String normalizedInputSkill = normalizeName(s);
             
	            PredefinedSkills predef = predefinedSkillsRepository.findByNormalizedSkillName(normalizedInputSkill);
	            EmployeeSkillProficiencyDTO skillDto = new EmployeeSkillProficiencyDTO();
	            if (predef != null) {
	                newSkillIds.add(predef.getSkillId());
	                skillDto.setSkillId(predef.getSkillId());
	                skillDto.setAdditionalSkill(null);
	            } else {
	                newAdditionalSkills.add(normalizeName(skillName));
	                skillDto.setSkillId(null);
	                skillDto.setAdditionalSkill(skillName);
	            }
	            skillsFromcertificateToSkillTable.add(skillDto);
	        }
	        
	        for (Long skillId : newSkillIds) {
	            if (!existingSkillIds.contains(skillId)) {
	                CertificateSkillMapping csm = new CertificateSkillMapping();
	                csm.setEmployeeCertificateId(existingCertificate.getEmployeeCertificateId());
	                csm.setEmpId(data.getEmpId());
	                csm.setSkillId(skillId);
	                csm.setProficiencyId(data.getProficiencyId());
	                csm.setScActive(true);
	                csm.setCscreatedBy(dto.getUploadedBy());
	                certificateSkillMapRepository.save(csm);
	            }
	        }
	        
	        for (String addSkill : newAdditionalSkills) {
	            if (!existingAdditionalSkills.contains(addSkill)) {
	                CertificateSkillMapping csm = new CertificateSkillMapping();
	                csm.setEmployeeCertificateId(existingCertificate.getEmployeeCertificateId());
	                csm.setEmpId(data.getEmpId());
	                csm.setAdditionalSkill(addSkill);
	                csm.setProficiencyId(data.getProficiencyId());
	                csm.setScActive(true);
	                csm.setCscreatedBy(dto.getUploadedBy());
	                certificateSkillMapRepository.save(csm);
	            }
	        }
	        
	        
	        for (EmployeeSkillProficiencyDTO ex : existingSkills) {
	            boolean shouldDeactivate = false;

	            if (ex.getSkillId() != null && !newSkillIds.contains(ex.getSkillId())) {
	                shouldDeactivate = true;
	            }
	            if (ex.getAdditionalSkill() != null && !newAdditionalSkills.contains(normalizeName(ex.getAdditionalSkill()))) {
	                shouldDeactivate = true;
	            }

	            if (shouldDeactivate) {
	                certificateSkillMapRepository.deactivateSkillByCertificateSkillId(ex.getEmpSkillId(), dto.getUploadedBy());
	            } else {
	            
	                if (ex.getProficiencyId() != null && !ex.getProficiencyId().equals(data.getProficiencyId())) {
	                    certificateSkillMapRepository.updateProficiencyByCertificateSkillId(ex.getEmpSkillId(), data.getProficiencyId(), dto.getUploadedBy());
	                }
	            }
	        }
	        
	    }
	    
	    syncEmployeeSkills( data.getEmpId(),data.getProficiencyId(),dto.getUploadedBy(),skillsFromcertificateToSkillTable);
	       

	    if (certificateUpdated) {
	        System.out.println(" Certificate and mappings updated successfully.");
	    } else {
	        System.out.println("No changes detected for certificate.");
	    }
	}


 public CertificateDTO getExistingCertificateByNameAndAuthOfEmployee (Long empId,String certName,String issuingAuth) {
	
//	    List<EmployeeSkillProficiencyDTO> skillsList = new ArrayList<>();
	    List<Object[]> existingCert = employeeCertificatesRepository.findExistingCert(empId,certName,issuingAuth);
	    
	    
	    CertificateDTO certificateDTO = new CertificateDTO();
	    if (existingCert == null || existingCert.isEmpty()) {
	       
	        return null;
	    }

	    Map<Long, EmployeeSkillProficiencyDTO> skillMap = new HashMap<>();
	    boolean certSet = false;

	    for (Object[] object : existingCert) {
	        if (!certSet) {
	            certificateDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	            certificateDTO.setEmployeeCertificateId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
	            certificateDTO.setCertificationName(object[2] != null ? object[2].toString() : null);
	            certificateDTO.setSpecialization(object[3] != null ? object[3].toString() : null);
	            certificateDTO.setDeptId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
	            certificateDTO.setProficiencyId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
	            certificateDTO.setIssuingAuthority(object[6] != null ? object[6].toString() : null);
	            certificateDTO.setValidFrom(object[7] != null ? object[7].toString() : null);
	            certificateDTO.setExpiresOn(object[8] != null ? object[8].toString() : null);
	            certificateDTO.setDriveLink(object[13] != null ? object[13].toString() : null);
	            certificateDTO.setDriveId(object[14] != null ? Long.parseLong(object[14].toString()) : null);          
	            
	            certSet = true;
	        }

	      
	        Long skillId = object[10] != null ? Long.parseLong(object[10].toString()) : null;
	        if (skillId != null && !skillMap.containsKey(skillId)) {
	            EmployeeSkillProficiencyDTO skillDTO = new EmployeeSkillProficiencyDTO();
	            skillDTO.setEmpSkillId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
	            skillDTO.setEmpId(empId);
	            skillDTO.setSkillId(skillId);
	            skillDTO.setAdditionalSkill(object[11] != null ? object[11].toString() : null);
	            skillDTO.setProficiencyId(object[12] != null ? Long.parseLong(object[12].toString()) : null);

	            skillMap.put(skillId, skillDTO);
	        }
	    }

	    certificateDTO.setSkills(new ArrayList<>(skillMap.values()));
	    return certificateDTO;
	}


private LocalDate parseDate(String dateStr, int rowNum, List<String> errorMessages) {
    List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),     
            DateTimeFormatter.ofPattern("d MMMM yyyy"),    
            DateTimeFormatter.ofPattern("d MMM yy"),      
            DateTimeFormatter.ofPattern("d MMMM yy")      
    );

    for (DateTimeFormatter formatter : formatters) {
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();    }
    }
    errorMessages.add("Row " + rowNum + ": Invalid date format .Allowed formats are yyyy-mm-dd /dd-mm-yyyy '" + dateStr + "'");
    return null;
}







private Long resolveEmployeeId(String empIdentifier) {
    if (empIdentifier.startsWith("A-")) {
        String idNum = empIdentifier.substring(2);
        Employee emp = employeeRepository.findByEmployeementIdForOthers(Long.valueOf(idNum));
        return emp != null ? emp.getEmpId() : null;
    } else if (empIdentifier.startsWith("AP-")) {
        String idNum = empIdentifier.substring(3);
        Employee emp = employeeRepository.findByEmployeementIdForApmosysProduct(Long.valueOf(idNum));
        return emp != null ? emp.getEmpId() : null;
    }
    return null;
}

private String normalizeName(String name) {
    return name == null ? "" : name.trim().replaceAll("\\s+", "").toLowerCase();
}





public ServiceResponse searchEmployeesBySkillsAndCertificates(SearchEmpPayloadDTO payload) {
    ServiceResponse response = new ServiceResponse();
    try {
    	
    	 PageResponseDTO<SearchEmployeeDTO> result = fetchEmployeesSSV(payload);
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(result);
    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceResponse("Error: " + e.getMessage());
    }
    return response;
}





public ServiceResponse searchEmployeesNotInSearch(SearchEmpPayloadDTO payload) {
    ServiceResponse response = new ServiceResponse();
    try {
    	
        List<SearchEmployeeDTO> result = fetchEmployeesNotInSearch(payload);
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(result);
    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceResponse("Error: " + e.getMessage());
    }
    return response;
}


private List<SearchEmployeeDTO> fetchEmployees(SearchEmpPayloadDTO payload) {
    Map<String, Object> params = new HashMap<>();

    // Step 1: Get employee IDs matching the filter
    StringBuilder filterSql = new StringBuilder();
    filterSql.append("SELECT DISTINCT e.emp_id FROM employee e ")
             .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
             .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
             .append("WHERE 1=1 ");

    List<String> orConditions = new ArrayList<>();
    List<String> andConditions = new ArrayList<>();

    if (payload.getSkillIds() != null && !payload.getSkillIds().isEmpty()) {
    	andConditions.add("s.skill_id IN (:skillIds)");
        params.put("skillIds", payload.getSkillIds());
    }
    if (payload.getCertificateIds() != null && !payload.getCertificateIds().isEmpty()) {
    	andConditions.add("c.employee_certificate_id IN (:certificateIds)");
        params.put("certificateIds", payload.getCertificateIds());
    }
//    if (payload.getCertificationDeptIds() != null && !payload.getCertificationDeptIds().isEmpty()) {
//        orConditions.add("c.dept_id IN (:certificationDeptIds)");
//        params.put("certificationDeptIds", payload.getCertificationDeptIds());
//    }
    if (payload.getSpecialization() != null && !payload.getSpecialization().isEmpty()) {
        orConditions.add("c.specialization LIKE CONCAT('%', :specialization, '%')");
        params.put("specialization", payload.getSpecialization());
    }
    if (payload.getCertificateStatus() != null && !payload.getCertificateStatus().isEmpty()
            && !payload.getCertificateStatus().equalsIgnoreCase("All")) {
    	andConditions.add("c.certificate_status = :certificateStatus");
        params.put("certificateStatus", payload.getCertificateStatus());
    }

    
    filterSql.append(" AND (s.emp_skill_id IS NOT NULL OR c.employee_certificate_id IS NOT NULL)");


    if (!andConditions.isEmpty()) {
        filterSql.append(" AND ").append(String.join(" AND ", andConditions));
    }

   
    if (!orConditions.isEmpty()) {
        filterSql.append(" OR (").append(String.join(" OR ", orConditions)).append(") ");
    }
    

    List<Long> filteredEmpIds = namedParameterJdbcTemplate.queryForList(filterSql.toString(), params, Long.class);
    if (filteredEmpIds.isEmpty()) return Collections.emptyList();

    
    StringBuilder dataSql = new StringBuilder();
    dataSql.append("SELECT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) ELSE CONCAT('A-', e.employeement_id) END AS formatted_emp_id ,e.name AS employee_name, e.email, ")
           .append("jr.job_role_id, jr.name AS job_role, d.dept_id, d.name AS dept_name, ")
           .append("s.emp_skill_id, s.skill_id, ps.skill_name, s.additional_skill, ")
           .append("s.proficiency_id, p.proficiency_name, ")
           .append("c.employee_certificate_id, c.certificate_name, c.specialization, c.dept_id AS cert_dept_id, ")
           .append("c.valid_from, c.expires_on, c.certificate_status, c.doc_id, cdr.drive_link,e.profile_image_name ")
           .append("FROM employee e ")
           .append("JOIN job_role jr ON e.job_role_id = jr.job_role_id ")
           .append("JOIN department d ON jr.dept_id = d.dept_id ")
           .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
           .append("LEFT JOIN predefined_skills ps ON s.skill_id = ps.skill_id ")
           .append("LEFT JOIN proficiency p ON s.proficiency_id = p.proficiency_id ")
           .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
           .append("LEFT JOIN certificate_drive_link_mapping cdr ON c.drive_id = cdr.drive_id AND cdr.dr_active = true ")
           .append("LEFT JOIN certificate_document_mapping cd ON c.doc_id = cd.doc_id AND cd.d_active = true ")
           .append("WHERE e.emp_id IN (:empIds) and e.employmentstatus != 'InActive' and e.emp_id NOT BETWEEN 1 AND 6 ORDER BY COUNT(*) OVER (PARTITION BY e.emp_id) DESC,e.name");

    Map<String, Object> dataParams = new HashMap<>();
    dataParams.put("empIds", filteredEmpIds);
    
    if (payload.getDeptIds() != null && !payload.getDeptIds().isEmpty()) {
        dataSql.append(" AND d.dept_id IN (:deptIds) ");
        dataParams.put("deptIds", payload.getDeptIds());
    }

    List<Map<String, Object>> rawData = namedParameterJdbcTemplate.queryForList(dataSql.toString(), dataParams);

   
    Map<Long, SearchEmployeeDTO> employeeMap = new LinkedHashMap<>();
    Map<Long, Set<Long>> addedSkills = new HashMap<>();
    Map<Long, Set<Long>> addedCertificates = new HashMap<>();
    
    for (Map<String, Object> row : rawData) {
        Long empId = ((Number) row.get("emp_id")).longValue();
        String profileImageName = (String) row.get("profile_image_name");
        SearchEmployeeDTO employee = employeeMap.computeIfAbsent(empId, k -> {
            SearchEmployeeDTO dto = new SearchEmployeeDTO();
            dto.setEmpId(empId);
            dto.setEmployeementId((String) row.get("formatted_emp_id"));
            dto.setEmployeeName((String) row.get("employee_name"));
            dto.setEmail((String) row.get("email"));
            dto.setJobRoleId(((Number) row.get("job_role_id")).longValue());
            dto.setJobRole((String) row.get("job_role"));
            dto.setDeptId(((Number) row.get("dept_id")).longValue());
            dto.setDeptName((String) row.get("dept_name"));
            dto.setSkillsEmp(new ArrayList<>());
            dto.setCertificatesEmp(new ArrayList<>());
            addedSkills.put(empId, new HashSet<>());
            addedCertificates.put(empId, new HashSet<>());

            if (profileImageName != null) {

				File actualFile = new File(
						Paths.get(imageFileLocation + File.separator + profileImageName).toString());

				if (actualFile.exists()) {
					byte[] imageBytes;
					try {
						imageBytes = Files.readAllBytes(
								Paths.get(imageFileLocation + File.separator + profileImageName));

						dto.setImageBytes(imageBytes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
            return dto;
        });

       
        if (row.get("emp_skill_id") != null) {
            Long skillId = ((Number) row.get("emp_skill_id")).longValue();
            if (!addedSkills.get(empId).contains(skillId)) {
                addedSkills.get(empId).add(skillId);

                EmployeeSkillProficiencyDTO skill = new EmployeeSkillProficiencyDTO();
                skill.setEmpSkillId(skillId);
                skill.setEmpId(empId);
                skill.setSkillId(row.get("skill_id") != null ? ((Number) row.get("skill_id")).longValue() : null);
                skill.setSkillName((String) row.get("skill_name"));
                skill.setAdditionalSkill((String) row.get("additional_skill"));
                skill.setProficiencyId(row.get("proficiency_id") != null ? ((Number) row.get("proficiency_id")).longValue() : null);
                skill.setProficiencyLevel((String) row.get("proficiency_name"));
                employee.getSkillsEmp().add(skill);
            }
        }

     
        if (row.get("employee_certificate_id") != null) {
            Long certificateId = ((Number) row.get("employee_certificate_id")).longValue();
            if (!addedCertificates.get(empId).contains(certificateId)) {
                addedCertificates.get(empId).add(certificateId);

                CertificateDTO cert = new CertificateDTO();
                cert.setEmployeeCertificateId(certificateId);
                cert.setCertificationName((String) row.get("certificate_name"));
                cert.setDeptId(row.get("cert_dept_id") != null ? ((Number) row.get("cert_dept_id")).longValue() : null);
                cert.setValidFrom(row.get("valid_from") != null ? row.get("valid_from").toString() : null);
                cert.setExpiresOn(row.get("expires_on") != null ? row.get("expires_on").toString() : null);
                cert.setCertificateStatus((String) row.get("certificate_status"));
                cert.setDriveLink((String) row.get("drive_link"));
                cert.setDocId(row.get("doc_id") != null ? ((Number) row.get("doc_id")).longValue() : null);
                cert.setEmpId(empId);
                cert.setSpecialization((String) row.get("specialization"));
                employee.getCertificatesEmp().add(cert);
            }
        }
    }

    List<SearchEmployeeDTO> employees = new ArrayList<>(employeeMap.values());


    return employees;
}

private PageResponseDTO<SearchEmployeeDTO> fetchEmployeesSSV(SearchEmpPayloadDTO payload) {
    Map<String, Object> params = new HashMap<>();

   
    StringBuilder filterSql = new StringBuilder();
    filterSql.append("SELECT DISTINCT e.emp_id FROM employee e ")
             .append("JOIN job_role jr ON e.job_role_id = jr.job_role_id ")
             .append("JOIN department d ON jr.dept_id = d.dept_id ")
             .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
             .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
             .append("WHERE 1=1 and e.employmentstatus != 'InActive' and e.emp_id NOT BETWEEN 1 AND 6 ");

    List<String> orConditions = new ArrayList<>();
    List<String> andConditions = new ArrayList<>();

//    if (payload.getSkillIds() != null && !payload.getSkillIds().isEmpty()) {
//    	andConditions.add("s.skill_id IN (:skillIds)");
//        params.put("skillIds", payload.getSkillIds());
//    }
    
    
    if (payload.getSkillIds() != null && !payload.getSkillIds().isEmpty()) {
        List<Long> skillIds = payload.getSkillIds();
        params.put("skillIds", skillIds);

       
        StringBuilder skillCondition = new StringBuilder("s.skill_id IN (:skillIds)");

       
        if (payload.getSkillNames() != null && !payload.getSkillNames().isEmpty()) {
            List<String> skillNames = payload.getSkillNames();
            List<String> likeClauses = new ArrayList<>();

            for (int i = 0; i < skillNames.size(); i++) {
                String paramName = "skillName" + i;
                likeClauses.add("LOWER(s.additional_skill) LIKE CONCAT('%', :" + paramName + ", '%')");
                params.put(paramName, skillNames.get(i).toLowerCase());
            }

           
            skillCondition.append(" OR (").append(String.join(" OR ", likeClauses)).append(")");
        }

        andConditions.add("(" + skillCondition.toString() + ")");
    }

    if (payload.getCertificateIds() != null && !payload.getCertificateIds().isEmpty()) {
    	andConditions.add("c.employee_certificate_id IN (:certificateIds)");
        params.put("certificateIds", payload.getCertificateIds());
    }

    if (payload.getSpecialization() != null && !payload.getSpecialization().isEmpty()) {
        orConditions.add("c.specialization LIKE CONCAT('%', :specialization, '%')");
        params.put("specialization", payload.getSpecialization());
    }
    if (payload.getCertificateStatus() != null && !payload.getCertificateStatus().isEmpty()
            && !payload.getCertificateStatus().equalsIgnoreCase("All")) {
    	andConditions.add("c.certificate_status = :certificateStatus");
        params.put("certificateStatus", payload.getCertificateStatus());
    }
    if (payload.getDeptIds() != null && !payload.getDeptIds().isEmpty()) {
    	andConditions.add("d.dept_id IN (:deptIds) ");
        params.put("deptIds", payload.getDeptIds());
    }

    

    
    filterSql.append(" AND (s.emp_skill_id IS NOT NULL OR c.employee_certificate_id IS NOT NULL)");


    if (!andConditions.isEmpty()) {
        filterSql.append(" AND ").append(String.join(" AND ", andConditions));
    }

   
    if (!orConditions.isEmpty()) {
        filterSql.append(" OR (").append(String.join(" OR ", orConditions)).append(") ");
    }
    
    
    System.err.println(filterSql.toString());
    

    List<Long> filteredEmpIds = namedParameterJdbcTemplate.queryForList(filterSql.toString(), params, Long.class);
    if (filteredEmpIds.isEmpty()) {
        return new PageResponseDTO<>(Collections.emptyList(), payload.getPage(), payload.getSize(), 0, 0);
    }
    
    StringBuilder sortSql = new StringBuilder();
    sortSql.append("SELECT e.emp_id, COUNT(s.emp_skill_id) AS skill_count ")
           .append("FROM employee e ")
           .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
           .append("WHERE e.emp_id IN (:empIds) ")
           .append("GROUP BY e.emp_id ")
           .append("ORDER BY skill_count DESC ");
    
    Map<String, Object> sortParams = new LinkedHashMap<>();
    sortParams.put("empIds", filteredEmpIds);

    List<Long> sortedEmpIds = namedParameterJdbcTemplate.query(sortSql.toString(), sortParams,
            (rs, rowNum) -> rs.getLong("emp_id"));
    
    int totalElements = sortedEmpIds.size();
    int totalPages = (int) Math.ceil((double) totalElements / payload.getSize());
    List<Long> paginatedEmpIds;
    if(Boolean.TRUE.equals(payload.getExport())){
    	paginatedEmpIds = sortedEmpIds;
    }else {
    	 int fromIndex = Math.min((payload.getPage() - 1) * payload.getSize(), totalElements);
    	 int toIndex = Math.min(fromIndex + payload.getSize(), totalElements);
    	 paginatedEmpIds = sortedEmpIds.subList(fromIndex, toIndex);
    }
    
  
   

    
    StringBuilder dataSql = new StringBuilder();
    dataSql.append("SELECT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) ELSE CONCAT('A-', e.employeement_id) END AS formatted_emp_id ,e.name AS employee_name, e.email, ")
           .append("jr.job_role_id, jr.name AS job_role, d.dept_id, d.name AS dept_name, ")
           .append("s.emp_skill_id, s.skill_id, ps.skill_name, s.additional_skill, ")
           .append("s.proficiency_id, p.proficiency_name, ")
           .append("c.employee_certificate_id, c.certificate_name, c.specialization, c.dept_id AS cert_dept_id, ")
           .append("c.valid_from, c.expires_on, c.certificate_status, c.doc_id, cdr.drive_link,e.profile_image_name ")
           .append("FROM employee e ")
           .append("JOIN job_role jr ON e.job_role_id = jr.job_role_id ")
           .append("JOIN department d ON jr.dept_id = d.dept_id ")
           .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
           .append("LEFT JOIN predefined_skills ps ON s.skill_id = ps.skill_id ")
           .append("LEFT JOIN proficiency p ON s.proficiency_id = p.proficiency_id ")
           .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
           .append("LEFT JOIN certificate_drive_link_mapping cdr ON c.drive_id = cdr.drive_id AND cdr.dr_active = true ")
           .append("LEFT JOIN certificate_document_mapping cd ON c.doc_id = cd.doc_id AND cd.d_active = true ")
           .append("WHERE e.emp_id IN (:empIds) ORDER BY FIELD(e.emp_id,:empIds) ");
    
    
   

    Map<String, Object> dataParams = new HashMap<>();
    dataParams.put("empIds", paginatedEmpIds);

    List<Map<String, Object>> rawData = namedParameterJdbcTemplate.queryForList(dataSql.toString(), dataParams);

   
    Map<Long, SearchEmployeeDTO> employeeMap = new LinkedHashMap<>();
    Map<Long, Set<Long>> addedSkills = new HashMap<>();
    Map<Long, Set<Long>> addedCertificates = new HashMap<>();
    
    for (Map<String, Object> row : rawData) {
        Long empId = ((Number) row.get("emp_id")).longValue();
        String profileImageName = (String) row.get("profile_image_name");
        SearchEmployeeDTO employee = employeeMap.computeIfAbsent(empId, k -> {
            SearchEmployeeDTO dto = new SearchEmployeeDTO();
            dto.setEmpId(empId);
            dto.setEmployeementId((String) row.get("formatted_emp_id"));
            dto.setEmployeeName((String) row.get("employee_name"));
            dto.setEmail((String) row.get("email"));
            dto.setJobRoleId(((Number) row.get("job_role_id")).longValue());
            dto.setJobRole((String) row.get("job_role"));
            dto.setDeptId(((Number) row.get("dept_id")).longValue());
            dto.setDeptName((String) row.get("dept_name"));
            dto.setSkillsEmp(new ArrayList<>());
            dto.setCertificatesEmp(new ArrayList<>());
            addedSkills.put(empId, new HashSet<>());
            addedCertificates.put(empId, new HashSet<>());

            if (profileImageName != null) {

				File actualFile = new File(
						Paths.get(imageFileLocation + File.separator + profileImageName).toString());

				if (actualFile.exists()) {
					byte[] imageBytes;
					try {
						imageBytes = Files.readAllBytes(
								Paths.get(imageFileLocation + File.separator + profileImageName));

						dto.setImageBytes(imageBytes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
            return dto;
        });

       
        if (row.get("emp_skill_id") != null) {
            Long skillId = ((Number) row.get("emp_skill_id")).longValue();
            if (!addedSkills.get(empId).contains(skillId)) {
                addedSkills.get(empId).add(skillId);

                EmployeeSkillProficiencyDTO skill = new EmployeeSkillProficiencyDTO();
                skill.setEmpSkillId(skillId);
                skill.setEmpId(empId);
                skill.setSkillId(row.get("skill_id") != null ? ((Number) row.get("skill_id")).longValue() : null);
                skill.setSkillName((String) row.get("skill_name"));
                skill.setAdditionalSkill((String) row.get("additional_skill"));
                skill.setProficiencyId(row.get("proficiency_id") != null ? ((Number) row.get("proficiency_id")).longValue() : null);
                skill.setProficiencyLevel((String) row.get("proficiency_name"));
                employee.getSkillsEmp().add(skill);
            }
        }

     
        if (row.get("employee_certificate_id") != null) {
            Long certificateId = ((Number) row.get("employee_certificate_id")).longValue();
            if (!addedCertificates.get(empId).contains(certificateId)) {
                addedCertificates.get(empId).add(certificateId);

                CertificateDTO cert = new CertificateDTO();
                cert.setEmployeeCertificateId(certificateId);
                cert.setCertificationName((String) row.get("certificate_name"));
                cert.setDeptId(row.get("cert_dept_id") != null ? ((Number) row.get("cert_dept_id")).longValue() : null);
                cert.setValidFrom(row.get("valid_from") != null ? row.get("valid_from").toString() : null);
                cert.setExpiresOn(row.get("expires_on") != null ? row.get("expires_on").toString() : null);
                cert.setCertificateStatus((String) row.get("certificate_status"));
                cert.setDriveLink((String) row.get("drive_link"));
                cert.setDocId(row.get("doc_id") != null ? ((Number) row.get("doc_id")).longValue() : null);
                cert.setEmpId(empId);
                cert.setSpecialization((String) row.get("specialization"));
                employee.getCertificatesEmp().add(cert);
            }
        }
    }

    List<SearchEmployeeDTO> employees = new ArrayList<>(employeeMap.values());
    return new PageResponseDTO<>(employees,payload.getPage(),payload.getSize(),totalElements,totalPages);
}




public ServiceResponse duplicateCertificateCheckForEmployee(CertificateDTO dto) {
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
	apiLogInfo.setApiUrl("/api/duplicateCertificateCheck");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append("empId : " + dto.getEmpId());

	try {
		
		
		EmployeeCertificates duplicateCertificate =  employeeCertificatesRepository.findByCertificateNameAndAuthorityNative(dto.getEmpId(),dto.getCertificationName(),dto.getIssuingAuthority());
				if(duplicateCertificate != null ) {				
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("certificate exists !!");
					apiLogInfo.setApiResponse("certificate already exists !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}


	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
}


private String getCellValue(Row row, int cellIndex) {
    Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) return "";

    switch (cell.getCellTypeEnum()) {
        case STRING:
            return cell.getStringCellValue().trim();

        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                LocalDate localDate = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                return localDate.toString(); 
            } else {
                double val = cell.getNumericCellValue();
                return (val == Math.floor(val))
                        ? String.valueOf((long) val)
                        : String.valueOf(val);
            }

        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());

        case FORMULA:
            try {
                return cell.getStringCellValue().trim();
            } catch (IllegalStateException e) {
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception ex) {
                    return "";
                }
            }
        default:
            return "";
    }
}


private List<SearchEmployeeDTO> fetchEmployeesNotInSearch(SearchEmpPayloadDTO payload) {
    Map<String, Object> params = new HashMap<>();

    
    StringBuilder filterSql = new StringBuilder();
    filterSql.append("SELECT DISTINCT e.emp_id FROM employee e ")
             .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
             .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
             .append("WHERE 1=1 ");
             

    List<String> orConditions = new ArrayList<>();
    List<String> andConditions = new ArrayList<>();

//    if (payload.getSkillIds() != null && !payload.getSkillIds().isEmpty()) {
//    	andConditions.add("s.skill_id IN (:skillIds)");
//        params.put("skillIds", payload.getSkillIds());
//    }
    
    if (payload.getSkillIds() != null && !payload.getSkillIds().isEmpty()) {
        List<Long> skillIds = payload.getSkillIds();
        params.put("skillIds", skillIds);

       
        StringBuilder skillCondition = new StringBuilder("s.skill_id IN (:skillIds)");

       
        if (payload.getSkillNames() != null && !payload.getSkillNames().isEmpty()) {
            List<String> skillNames = payload.getSkillNames();
            List<String> likeClauses = new ArrayList<>();

            for (int i = 0; i < skillNames.size(); i++) {
                String paramName = "skillName" + i;
                likeClauses.add("LOWER(s.additional_skill) LIKE CONCAT('%', :" + paramName + ", '%')");
                params.put(paramName, skillNames.get(i).toLowerCase());
            }

           
            skillCondition.append(" OR (").append(String.join(" OR ", likeClauses)).append(")");
        }

        andConditions.add("(" + skillCondition.toString() + ")");
    }
    if (payload.getCertificateIds() != null && !payload.getCertificateIds().isEmpty()) {
    	andConditions.add("c.employee_certificate_id IN (:certificateIds)");
        params.put("certificateIds", payload.getCertificateIds());
    }
//    if (payload.getCertificationDeptIds() != null && !payload.getCertificationDeptIds().isEmpty()) {
//        orConditions.add("c.dept_id IN (:certificationDeptIds)");
//        params.put("certificationDeptIds", payload.getCertificationDeptIds());
//    }
    if (payload.getSpecialization() != null && !payload.getSpecialization().isEmpty()) {
        orConditions.add("c.specialization LIKE CONCAT('%', :specialization, '%')");
        params.put("specialization", payload.getSpecialization());
    }
    if (payload.getCertificateStatus() != null && !payload.getCertificateStatus().isEmpty()
            && !payload.getCertificateStatus().equalsIgnoreCase("All")) {
    	andConditions.add("c.certificate_status = :certificateStatus");
        params.put("certificateStatus", payload.getCertificateStatus());
    }

    
    filterSql.append(" AND (s.emp_skill_id IS NOT NULL OR c.employee_certificate_id IS NOT NULL)");


    if (!andConditions.isEmpty()) {
        filterSql.append(" AND ").append(String.join(" AND ", andConditions));
    }

   
    if (!orConditions.isEmpty()) {
        filterSql.append(" OR (").append(String.join(" OR ", orConditions)).append(") ");
    }
    
    
    StringBuilder outerQuery = new StringBuilder();
    outerQuery.append("SELECT DISTINCT e.emp_id FROM employee e ")
              .append("WHERE e.emp_id NOT IN (")
              .append(filterSql)
              .append(")");
    
    
    
    System.err.println(outerQuery.toString());
    
   

    List<Long> filteredEmpIds = namedParameterJdbcTemplate.queryForList(outerQuery.toString(), params, Long.class);
    if (filteredEmpIds.isEmpty()) return Collections.emptyList();

    
    StringBuilder dataSql = new StringBuilder();
    dataSql.append("SELECT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) ELSE CONCAT('A-', e.employeement_id) END AS formatted_emp_id ,e.name AS employee_name, e.email, ")
           .append("jr.job_role_id, jr.name AS job_role, d.dept_id, d.name AS dept_name, ")
           .append("s.emp_skill_id, s.skill_id, ps.skill_name, s.additional_skill, ")
           .append("s.proficiency_id, p.proficiency_name, ")
           .append("c.employee_certificate_id, c.certificate_name, c.specialization, c.dept_id AS cert_dept_id, ")
           .append("c.valid_from, c.expires_on, c.certificate_status, c.doc_id, cdr.drive_link ")
           .append("FROM employee e ")
           .append("JOIN job_role jr ON e.job_role_id = jr.job_role_id ")
           .append("JOIN department d ON jr.dept_id = d.dept_id ")
           .append("LEFT JOIN employee_skill_proficiency_mapping s ON e.emp_id = s.emp_id AND s.active = TRUE ")
           .append("LEFT JOIN predefined_skills ps ON s.skill_id = ps.skill_id ")
           .append("LEFT JOIN proficiency p ON s.proficiency_id = p.proficiency_id ")
           .append("LEFT JOIN employee_certificates c ON e.emp_id = c.emp_id AND c.c_active = TRUE ")
           .append("LEFT JOIN certificate_drive_link_mapping cdr ON c.drive_id = cdr.drive_id AND cdr.dr_active = true ")
           .append("LEFT JOIN certificate_document_mapping cd ON c.doc_id = cd.doc_id AND cd.d_active = true ")
           .append("WHERE e.emp_id IN (:empIds) and e.employmentstatus != 'InActive' and e.emp_id NOT BETWEEN 1 AND 6 ORDER BY COUNT(*) OVER (PARTITION BY e.emp_id) DESC,e.name ");

    Map<String, Object> dataParams = new HashMap<>();
    dataParams.put("empIds", filteredEmpIds);
    
    if (payload.getDeptIds() != null && !payload.getDeptIds().isEmpty()) {
        dataSql.append(" AND d.dept_id IN (:deptIds) ");
        dataParams.put("deptIds", payload.getDeptIds());
    }

    List<Map<String, Object>> rawData = namedParameterJdbcTemplate.queryForList(dataSql.toString(), dataParams);

   
    Map<Long, SearchEmployeeDTO> employeeMap = new LinkedHashMap<>();
    Map<Long, Set<Long>> addedSkills = new HashMap<>();
    Map<Long, Set<Long>> addedCertificates = new HashMap<>();
    
    for (Map<String, Object> row : rawData) {
        Long empId = ((Number) row.get("emp_id")).longValue();
       
        SearchEmployeeDTO employee = employeeMap.computeIfAbsent(empId, k -> {
            SearchEmployeeDTO dto = new SearchEmployeeDTO();
            dto.setEmpId(empId);
            dto.setEmployeementId((String) row.get("formatted_emp_id"));
            dto.setEmployeeName((String) row.get("employee_name"));
            dto.setEmail((String) row.get("email"));
            dto.setJobRoleId(((Number) row.get("job_role_id")).longValue());
            dto.setJobRole((String) row.get("job_role"));
            dto.setDeptId(((Number) row.get("dept_id")).longValue());
            dto.setDeptName((String) row.get("dept_name"));
            dto.setSkillsEmp(new ArrayList<>());
            dto.setCertificatesEmp(new ArrayList<>());
            addedSkills.put(empId, new HashSet<>());
            addedCertificates.put(empId, new HashSet<>());

         
            return dto;
        });

       
        if (row.get("emp_skill_id") != null) {
            Long skillId = ((Number) row.get("emp_skill_id")).longValue();
            if (!addedSkills.get(empId).contains(skillId)) {
                addedSkills.get(empId).add(skillId);

                EmployeeSkillProficiencyDTO skill = new EmployeeSkillProficiencyDTO();
                skill.setEmpSkillId(skillId);
                skill.setEmpId(empId);
                skill.setSkillId(row.get("skill_id") != null ? ((Number) row.get("skill_id")).longValue() : null);
                skill.setSkillName((String) row.get("skill_name"));
                skill.setAdditionalSkill((String) row.get("additional_skill"));
                skill.setProficiencyId(row.get("proficiency_id") != null ? ((Number) row.get("proficiency_id")).longValue() : null);
                skill.setProficiencyLevel((String) row.get("proficiency_name"));
                employee.getSkillsEmp().add(skill);
            }
        }

     
        if (row.get("employee_certificate_id") != null) {
            Long certificateId = ((Number) row.get("employee_certificate_id")).longValue();
            if (!addedCertificates.get(empId).contains(certificateId)) {
                addedCertificates.get(empId).add(certificateId);

                CertificateDTO cert = new CertificateDTO();
                cert.setEmployeeCertificateId(certificateId);
                cert.setCertificationName((String) row.get("certificate_name"));
                cert.setDeptId(row.get("cert_dept_id") != null ? ((Number) row.get("cert_dept_id")).longValue() : null);
                cert.setValidFrom(row.get("valid_from") != null ? row.get("valid_from").toString() : null);
                cert.setExpiresOn(row.get("expires_on") != null ? row.get("expires_on").toString() : null);
                cert.setCertificateStatus((String) row.get("certificate_status"));
                cert.setDriveLink((String) row.get("drive_link"));
                cert.setDocId(row.get("doc_id") != null ? ((Number) row.get("doc_id")).longValue() : null);
                cert.setEmpId(empId);
                cert.setSpecialization((String) row.get("specialization"));
                employee.getCertificatesEmp().add(cert);
            }
        }
    }

    List<SearchEmployeeDTO> employees = new ArrayList<>(employeeMap.values());


    return employees;
}

public ServiceResponse getPendingTimesheetProjects(Long empId, LocalDate relievingDate) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setApiUrl("/api/getPendingTimesheetProjects");
    apiLogInfo.setLogLevel("INFO");
    apiLogInfo.setApiRequest("empId: " + empId + ", relievingDate: " + relievingDate);
    try {
        LocalDate checkDate = relievingDate;
        
        if (checkDate == null) {
            Optional<Employee> employeeOpt = employeeRepository.findById(empId);
            if (employeeOpt.isPresent() && employeeOpt.get().getDateOfRelieving() != null) {
                checkDate = LocalDate.parse(employeeOpt.get().getDateOfRelieving());
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Date of relieving not found");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                apiLogInfo.setApiResponse("Date of relieving not found");
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
        }
        Long pendingCount = employeeRepository
                .countPendingTimesheetsByEmployeeAndDate(empId, checkDate);
        List<String> pendingProjects = Collections.emptyList();
        if (pendingCount != null && pendingCount > 0) {
            pendingProjects = employeeRepository
                    .findPendingTimesheetProjectNames(empId, checkDate);
        }
        PendingTimesheetDTO dto =
                new PendingTimesheetDTO(
                        pendingCount == null ? 0 : pendingCount,
                        pendingProjects,
                        checkDate
                );
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(dto);
        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
        apiLogInfo.setApiResponse("Pending timesheet status fetched for date: " + checkDate);
    } catch (Exception e) {
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceResponse("Error fetching pending timesheet status");
        apiLogInfo.setLogLevel("ERROR");
        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        apiLogInfo.setApiResponse(e.getMessage());
    }
    logService.logMyInfo(httpRequest, apiLogInfo);
    return response;
}

public ServiceResponse getPoRequirementDataByTeamAndPoId(Long teamId, Long poId) {

    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setApiUrl("/api/getPoRequirementDataByTeamAndPoId");
    try {
        if (teamId == null || poId == null) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Team ID and PO ID are required");
            return response;
        }

        List<PoRequirementDataDTO> data =
                poRequirementMappingRepository
                        .getPoRequirementDataByTeamAndPoId(teamId, poId);

        if (data != null && !data.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(data);
            response.setServiceResponse1("PO Requirement data retrieved successfully");
        } else {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(null);
            response.setServiceResponse1("No data found for given Team and PO");
        }

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Error while fetching PO Requirement data");
        response.setServiceError(e.getMessage());
    }

    return response;
}

	@Transactional(readOnly = true)
	public ServiceResponse getAllActiveEmployeeInformation() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getAllActiveEmployeeInformation");
		apiLogInfo.setApiUrl("/api/getAllActiveEmployeeInformation");
		apiLogInfo.setLogLevel("INFO");
		try {
			List<Object[]> results = employeeRepository.getAllActiveEmployeeInformation();
			List<EmployeeInformationDTO> employeeInfoList = new ArrayList<>();
			if (results != null && !results.isEmpty()) {
				for (Object[] obj : results) {
					Long empId = TypeConversionUtil.safeParseLong(obj[0]);
					EmployeeInformationDTO dto = new EmployeeInformationDTO();
					dto.setEmpId(empId);
					dto.setEmploymentId(TypeConversionUtil.getSafeString(obj[1]));
					dto.setName(TypeConversionUtil.getSafeString(obj[2]));
					dto.setBillableType(TypeConversionUtil.getSafeString(obj[3]));
					dto.setJobRole(TypeConversionUtil.getSafeString(obj[4]));
					dto.setDeptName(TypeConversionUtil.getSafeString(obj[5]));
					dto.setDeptId(TypeConversionUtil.safeParseLong(obj[6]));
					dto.setDefaultProjectId(TypeConversionUtil.safeParseLong(obj[7]));
					employeeInfoList.add(dto);
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeInfoList);
		} catch (Exception e) {
			logger.error("Error in getAllActiveEmployeeInformation", e);
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error : " + e.getMessage());
		}
		return response;
	}

private ServiceResponse buildFailureResponse(ServiceResponse response,
                                             LogDTO apiLogInfo,
                                             String message) {

    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    response.setServiceResponse(message);
    apiLogInfo.setApiResponse(message);
    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
    return response;
}
public ServiceResponse getInActiveableOrNot(Long empId) {

    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("getInActiveableOrNot");
    apiLogInfo.setApiUrl("/api/getInActiveableOrNot/{empId}");
    apiLogInfo.setLogLevel("INFO");

    try {
    	 boolean isInactiveAllowed = false;

         int currentYear = LocalDate.now().getYear();
         int currentMonth = LocalDate.now().getMonthValue();

         LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
         LocalDate end = LocalDate.now().minusDays(1);

         long totalDaysTillYesterday =
                 ChronoUnit.DAYS.between(firstOfMonth, end) + 1;

         Integer pendingFlag =
        	        employeeTimesheetsNewRepository
        	                .hasRealPendingTimesheet(empId, firstOfMonth, end);

        	boolean hasPendingTimesheets =
        	        pendingFlag != null && pendingFlag == 1;


         

        Integer TNMStatus = employeeRepository.checkActiveTNMProject(empId);
        boolean isUnderTNMProject = TNMStatus != null && TNMStatus == 1;

        if (hasPendingTimesheets || isUnderTNMProject) {
            isInactiveAllowed = true; 
        }

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(isInactiveAllowed);

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceResponse("Error : " + e.getMessage());
    }

    return response;
}

public ServiceResponse getEmployeeBillableType(Long empId){
	
	ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    // apiLogInfo.setSubFeatureName("getEmployeeBillableType");
    apiLogInfo.setApiUrl("/api/getEmployeeBillableType");
    apiLogInfo.setLogLevel("INFO");

	try{
		String billableType  = employeeRepository.getBillableType(empId);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(billableType);
	}
	catch(Exception e){
		e.printStackTrace();
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceResponse("Error : " + e.getMessage());
	}
	return response;
}

    public ServiceResponse getDefaulterStatus(Long empId) {

        ServiceResponse response = new ServiceResponse();

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getDefaulterStatus");
        apiLogInfo.setLogLevel("INFO");

        try {

            //  Validation
            if (empId == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Employee ID is missing");

                apiLogInfo.setApiResponse("Employee ID is missing");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }
            List<PortalConfig> configList = portalConfigRepository.findAll();

                    Map<String, String> configMap = configList.stream()
                    .collect(Collectors.toMap(
                            PortalConfig::getConfigName,
                            pc -> pc.getConfigValue() != null ? pc.getConfigValue() : ""));

                    
                    String heading = configMap.get("DEF_POPUP_HEADING");
                    String description = configMap.get("DEF_POPUP_DESCRIPTION");

                    int cutoffYear = configMap.get("DEF_CUTOFF_YEAR") != null
                            ? Integer.parseInt(configMap.get("DEF_CUTOFF_YEAR"))
                            : 2025;

                    int cutoffMonth = configMap.get("DEF_CUTOFF_MONTH") != null
                            ? Integer.parseInt(configMap.get("DEF_CUTOFF_MONTH"))
                            : 10;

            //  repository  Call
            List<Object[]> result = employeeDefaulterConsentRepository.findDefaulterMonths(empId, cutoffYear,
                    cutoffMonth);

            if (result != null && !result.isEmpty()) {

                List<Map<String, Integer>> months = new ArrayList<>();

                for (Object[] row : result) {
                    Map<String, Integer> m = new HashMap<>();
                    m.put("year", ((Number) row[0]).intValue());
                    m.put("month", ((Number) row[1]).intValue());
                    months.add(m);
                }

                DefaulterResponseDTO data = new DefaulterResponseDTO(
                        true,
                        months,
                        heading,
                        description
                );

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(data);

                apiLogInfo.setApiResponse(months.size() + " month(s) found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

            } else {

                Map<String, Object> data = new HashMap<>();
                data.put("isDefaulter", false);
                data.put("months", new ArrayList<>());

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(data);

                apiLogInfo.setApiResponse("No defaulter record found");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());

            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }

        apiLogInfo.setApiRequest("empId: " + empId);
        logService.logMyInfo(httpRequest, apiLogInfo);

        return response;
    }

    public ServiceResponse saveDefaulterConsent(Long empId) {

        ServiceResponse response = new ServiceResponse();

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/saveDefaulterConsent");
        apiLogInfo.setLogLevel("INFO");

        try {

            if (empId == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Employee ID is missing");

                apiLogInfo.setApiResponse("Employee ID is missing");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }

            Optional<EmployeeDefaulterConsent> existingOpt = employeeDefaulterConsentRepository.findByEmpId(empId);

            EmployeeDefaulterConsent entity;

            if (existingOpt.isPresent()) {
                employeeDefaulterConsentRepository.updateConsent(empId);
            } else {

                entity = new EmployeeDefaulterConsent();
                entity.setEmpId(empId);
                entity.setConsent(true); // first time consent

                employeeDefaulterConsentRepository.save(entity);
            }


            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Consent saved successfully");

            apiLogInfo.setApiResponse("Consent saved for empId: " + empId);
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong.");
            response.setServiceError(e.getMessage());

            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }

        apiLogInfo.setApiRequest("empId: " + empId);
        logService.logMyInfo(httpRequest, apiLogInfo);

        return response;
    }
}
	
