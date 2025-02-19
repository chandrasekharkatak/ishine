package com.apmosys.employeeportal.service;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.naming.factory.webservices.ServiceRefFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationDetailsDTO;
import com.apmosys.employeeportal.dto.DateRangeDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeAssetMap;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeNotificationConsent;
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
import com.apmosys.employeeportal.model.PIP;
import com.apmosys.employeeportal.model.PolicyReadResponse;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.UploadPolicy;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.AppreciationRepository;
import com.apmosys.employeeportal.repository.AuditCustomRepository;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeNotificationConsentRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeSpecializationMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.LogsRepository;
import com.apmosys.employeeportal.repository.NewsletterReadResponseRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.NotificationRepository;
import com.apmosys.employeeportal.repository.PIPRepository;
import com.apmosys.employeeportal.repository.PolicyReadResponseRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.repository.UploadPolicyRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.DbTable;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.LogEvents;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.sun.mail.iap.Response;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;

@Service
public class EmployeeService {

	@Value("${valid.attempt}")
	private Integer failedAttempt;
	
	

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	DraftEmployeeRepository draftEmployeeRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	JobRoleRepository jobRoleRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

	@Value("${default.password}")
	String defaultPaswword;

	@Value("${file.location.image}")
	private String imageFileLocation;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${rmg.mail}")
	private String rmgMail;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
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
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	private  UploadPolicyRepository uploadPolicyRepository;

	@Autowired
	PolicyReadResponseRepository policyReadResponseRepository;
	
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
	
	private final Map<String, List<EmployeeDTO>> employeeCache = new ConcurrentHashMap<>();


//	@Transactional
//	public ServiceResponse createEmployee(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//
//		try {
//
//			Employee employee = new Employee();
//
//			employee.setEmployeementId(employeedto.getEmployeementId());
//			employee.setName(employeedto.getName());
//			employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));
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
//			employee.setAadhar(employeedto.getAadhar());
//			employee.setPanNumber(employeedto.getPanNumber());
//			employee.setMobileNo(employeedto.getMobileNo());
//			employee.setLandline(employeedto.getLandline());
//			employee.setAddress(employeedto.getAddress());
//			employee.setCity(employeedto.getCity());
//			employee.setState(employeedto.getState());
//			employee.setCountry(employeedto.getCountry());
//			employee.setPincode(employeedto.getPincode());
//			employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());
//			employee.setPermanentAddress(employeedto.getPermanentAddress());
//			employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
//			employee.setRelation(employeedto.getRelation());
//			employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
//			employee.setNoticePeriod(employeedto.getNoticePeriod());
//			employee.setEmploymentstatus(employeedto.getEmploymentstatus());
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
//			employee.setJobRoleId(employeedto.getJobRoleId());
//			employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
//			employee.setCreatedBy(employeedto.getCreatedBy());
//			employee.setExperience(employeedto.getExperience());
//			employee.setRole(employeedto.getRole());
//			employee.setWorkLocation(employeedto.getWorkLocation());
//			employee.setInvalidAccessAttempt(0);
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
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

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
			employee.setBillable(employeedto.getBillable());
			employee.setBillableType(employeedto.getBillableType());
			employee.setIsTimesheetLockCheckEnable("true");
			employee.setDesignationId(employeedto.getDesignationId());
			employee.setIsConsultant(employeedto.getIsConsultant());
			employee.setIsApprenticeship(employeedto.getIsApprenticeship());
			
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

					mailService.sendMail(newEmployee.getSecondaryEmail(), "Regarding employee profile creation",
							"Your account has been created. <br>Username: " + newEmployee.getEmail()
									+ "<br>Password: " + defaultPaswword);
					mailService.sendMail(hod.getEmail(), "Regarding new employee",
							newEmployee.getName() + " has been inducted in " + hod.getDepartmentName()
									+ " department as " + hod.getJobRoleName());

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
				Employee employee = employeeObject.get();
//				System.out.println("Employee 1 : " + employee);
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

							        
							        if ("true".equalsIgnoreCase(isConsultant)) {
							            employmentId = "A-" + employmentId;
							        } else if ("true".equalsIgnoreCase(isApprenticeship)) {
							            employmentId = "A-" + employmentId;
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
						
						List<EmployeeTeamMap> findAllActiveTeams = employeeTeamMapRepository.findByEmpId(employeedto.getEmpId());
						if(findAllActiveTeams != null) {
							
							findAllActiveTeams.forEach(obj ->{
								obj.setActive(0l);				
								employeeTeamMapRepository.save(obj);
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
					
				
				if (dbResponse != null) {
					
					//Update Draft
					DraftEmployee draftEmployee = draftEmployeeRepository.findByEmployeementId(dbResponse.getEmployeementId());
					System.out.println("draftEmployee : "+draftEmployee);
					if(draftEmployee != null) {
						draftEmployee.setName(dbResponse.getName());
						draftEmployee.setDateOfBirth(dbResponse.getDateOfBirth());
						draftEmployee.setDateOfJoining(dbResponse.getDateOfJoining());
						draftEmployee.setManagerId(dbResponse.getManagerId());
						draftEmployee.setEmail(dbResponse.getEmail());
						draftEmployee.setMobileNo(dbResponse.getMobileNo());
						draftEmployee.setNoticePeriod(dbResponse.getNoticePeriod());
						draftEmployee.setEmploymentstatus(dbResponse.getEmploymentstatus());
						draftEmployee.setJobRoleId(dbResponse.getJobRoleId());
						draftEmployee.setExperience(dbResponse.getExperience());
						draftEmployee.setRole(dbResponse.getRole());
						draftEmployee.setWorkLocation(dbResponse.getWorkLocation());
						draftEmployee.setUpdatedBy(Integer.parseInt(dbResponse.getUpdatedBy().toString()));
						draftEmployee.setBillable(dbResponse.getBillable());
						draftEmployee.setTotalExperience(dbResponse.getTotalExperience());
						draftEmployee.setUpdatedOn(dbResponse.getUpdatedOn());
						draftEmployee.setDesignationId(dbResponse.getDesignationId());
						draftEmployee.setDateOfResign(dbResponse.getDateOfResign());
						draftEmployee.setDateOfRelieving(dbResponse.getDateOfRelieving());
						
						draftEmployee.setReportingManagerId(dbResponse.getReportingManagerId());
						if(dbResponse.getReportingManagerId() == null){
							draftEmployee.setApprovalsTo(null);
						}else {							
							draftEmployee.setApprovalsTo(dbResponse.getApprovalsTo());
						}
						System.out.println("draftEmployee : "+draftEmployee);
						draftEmployeeRepository.save(draftEmployee);
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
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getALLEmployees size : "+employeeRepository.getAllEmployees().size());
		
		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
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
	
	public ServiceResponse getAllEmployeesFor360View() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_all_employee");
		apiLogInfo.setApiUrl("/api/getAllEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getALLEmployees size : "+employeeRepository.getAllEmployees().size());
		
		String cacheKey = "allEmployees";
		
		if (employeeCache.containsKey(cacheKey)) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(employeeCache.get(cacheKey));
            apiLogInfo.setApiResponse("Data fetched from cache. Size: " + employeeCache.get(cacheKey).size());
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
        }
		
		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
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
                    
					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
					
					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);

					
					dtoList.add(empDTO);
				});
				
                employeeCache.put(cacheKey, dtoList);

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
		logBuilder.append("role : " + employeedto.getRole());
		try {

			List<JobRole> jobRoleObj;
			List<String> jobRoles = new ArrayList<String>(Arrays.asList("Employee", "HR"));
			if (employeedto.getRole().equals("Manager")) {
				jobRoleObj = jobRoleRepository.findByEmployeeRoleNotIn(jobRoles);
			} 
			else if(employeedto.getRole().equals("HOD")){
				// HoD should include directors (SuperAdmins)
				jobRoles = new ArrayList<String>(Arrays.asList("Employee", "HR", "Manager"));
				jobRoleObj = jobRoleRepository.findByEmployeeRoleNotIn(jobRoles);
			}
			else {
				jobRoleObj = jobRoleRepository.findByEmployeeRole(employeedto.getRole());
				System.err.println(" in else  part  ::   "+employeedto.getRole());
			}
			List<EmployeeDTO> employeeList = new ArrayList<EmployeeDTO>();
			
			if (!jobRoleObj.isEmpty()) {
				
				List<Long> jobRoleIds = 
						jobRoleObj.stream().map(JobRole::getJobRoleId).collect(Collectors.toList());
				System.out.println(" jobRoleIds    ::   "+jobRoleIds);
				
					List<Object[]> empList = employeeRepository.getEmployeesByRoleIds(jobRoleIds);

					Optional.ofNullable(empList).ifPresentOrElse((list) -> {

						list.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
							dto.setName(object[1] != null ? object[1].toString() : null);
							dto.setJobRoleName(object[2] != null ? object[2].toString() : null);
							dto.setDepartmentId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
							dto.setEmployeementId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
							dto.setBillableType(object[5] != null ? object[5].toString() : null);
							employeeList.add(dto);
						});
						
						// sorted alphabetically
						
						final List<EmployeeDTO> employeeDTOList = 
						employeeList
						  .stream()
						  .sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
						  
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(employeeDTOList);
						
						apiLogInfo.setApiResponse(employeeDTOList.size() + " employee's found.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					}, () -> {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("No Employee Found");
						
						
						apiLogInfo.setApiResponse("No Employee Found");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					});
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

	public ServiceResponse getAllEmployeesByDepartmentIds(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/getAllEmployeesByDepartmentIds");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("departmentList : " + employeedto.getDepartmentList());
		try {

			Optional.ofNullable(employeedto.getDepartmentList()).ifPresentOrElse((departmentList) -> {

				if (departmentList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department list is empty.");
					
					apiLogInfo.setApiResponse("Department list is empty.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<Long> deptIds = departmentList.stream().map((department) -> {
						return department.getDeptId();
					}).collect(Collectors.toList());

					List<Object[]> objectArrayList = employeeRepository.getAllEmployeesByDepartmentIds(deptIds);

					Optional.ofNullable(objectArrayList).ifPresent((list) -> {

						if (list.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee list is empty.");
							
							apiLogInfo.setApiResponse("Employee list is empty");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						} else {

							List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();

							list.forEach((object) -> {

								EmployeeDTO dto = new EmployeeDTO();

								dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
								dto.setName(object[1] != null ? object[1].toString() : null);
								dto.setJobRoleName(object[2] != null ? object[2].toString() : null);
								dto.setDepartmentId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
								dto.setDepartmentName(object[4] != null ? object[4].toString() : null);

								dtoList.add(dto);

							});

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(dtoList);
							
							apiLogInfo.setApiResponse("dtoList : " +dtoList);			
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
			Employee checkEmployeementId = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());
//			DraftEmployee checkDraftEmployeementId = draftEmployeeRepository
//					.findByEmployeementId(employeedto.getEmployeementId());

			if (checkEmployeementId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
				if (checkEmployeementId != null && employeedto.getEmployeementId().equals(checkEmployeementId.getEmployeementId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employeement ID already exist!");
					apiLogInfo.setApiResponse("Employeement ID already exist!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
				
				
				//Check if all Newsletter is read
				List<Newsletter> allNewsletters = newsletterRepository.findAll();

				if (!allNewsletters.isEmpty()) {
					for (Newsletter object : allNewsletters) {
						NewsletterReadResponse readResponse = newsletterReadResponseRepository
									.findByEmpIdAndDocumentId(employee.getEmpId(), object.getDocumentId());
						
						if (readResponse == null) {
							employee.setNewsletterReadCheck(object);
							break;
						}
					}
				}
				
				
				
				response.setServiceResponse("Employee login info found.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Employee login info found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
		logBuilder.append("getAllEmployeesBirthDayToday : "+employeeRepository.getAllEmployeesBirthDayToday().size());		

		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployeesBirthDayToday();

			if (allEmployeeList != null) {
				List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
				allEmployeeList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					empDTO.setName(object[0] != null ? object[0].toString() : null);
					empDTO.setDepartmentName(object[1] != null ? object[1].toString() : null);
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
	
	public ServiceResponse getHierarchyByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_my_team");
		apiLogInfo.setApiUrl("/api/getHierarchyByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		try {

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
				List<Object[]> timesheetList = timesheetsRepository.getMyTeamsFilledEodCountByManagerId(firstOfMonth, end, employeedto.getEmpId());

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
		logBuilder.append("List fetched : "+employeeRepository.findAll().size());
		try {
			List<Object[]> allEmployees = employeeRepository.getEmployees();
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
		logBuilder.append("getAllManagers size : "+employeeRepository.getAllManagers().size());
		try {
			List<Object[]> allEmployees = employeeRepository.getAllManagers();			

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
		System.err.println("check details \n");
		System.err.println(" \n"+employeeDto);
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

	public ServiceResponse addDemographicsInfo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_demographic_info");
		apiLogInfo.setApiUrl("/api/addDemographicsInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeedto.getEmpId());
		
		try {
			
//			String url = "https://api.postalpincode.in/pincode/"+employee.getPincode();
//			RestTemplate restTemplate = new RestTemplate();
//			
//			Object[] demograpicsInfo = restTemplate.getForObject(url, Object[].class);
//			System.out.println(Arrays.asList(demograpicsInfo));
//			
//			for(Object info : demograpicsInfo) {
//				System.out.println();
//			}
			
			System.out.println(employeedto.getEmployeementId() + " : employeementy id");
			Optional<Employee> employeeObject = Optional
					.ofNullable(employeeRepository.findByEmployeementId(employeedto.getEmployeementId()));
			
			if (employeeObject.isPresent()) {
				
				Employee employee = employeeObject.get();
				
				employee.setPincode(employeedto.getPincode());
				employee.setState(employeedto.getState());
				employee.setCity(employeedto.getCity());
				employee.setCountry(employeedto.getCountry());
				
				Employee empSaved = employeeRepository.save(employee);
				
				if(empSaved!=null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Demographics details added successfully");
					apiLogInfo.setApiResponse("Employee Demographics details added successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee updation failed");
					apiLogInfo.setApiResponse("Employee updation failed");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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

	public ServiceResponse getAllEmployeeInfo() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_all_employee_info");
		apiLogInfo.setApiUrl("/api/getAllEmployeeInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(employeeRepository.getAllEmployeeInfoForPoPortal());
		
		try {
			List<Object[]> employeeObj = employeeRepository.getAllEmployeeInfoForPoPortal();
			List<PoPortalDTO> dtoList = new ArrayList<PoPortalDTO>();
			
			if(!employeeObj.isEmpty()) {
				employeeObj.forEach((object) -> {
					PoPortalDTO dto = new PoPortalDTO();
					String employeementStatus = object[3] != null ? object[3].toString() : null;
					String status = null;
					if(employeementStatus != null) {
						status = !employeementStatus.equals("InActive") ? "Y" : "N";
					}
					Long empId = object[8] != null ? Long.parseLong(object[8].toString()): null;
					
					dto.setEmpId(object[0] != null ? object[0].toString() : null);
					dto.setEmpName(object[1] != null ? object[1].toString() : null);
					dto.setDeptId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					dto.setIsActive(status);
					dto.setIsHead(validationService.validateHodId(empId) != false ? "Y" : "N");
					dto.setMailId(object[4] != null ? object[4].toString() : null);
					dto.setMobile(object[5] != null ? object[5].toString() : null);
					dto.setRoleId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Info not found.");
				apiLogInfo.setApiResponse("Employee Info not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e){
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
					+ " t.team_lead_id, p.project_name, createdBy.name as createdByName, updatedBy.name as updateByName FROM employee_team_mapping_aud etma \n"
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
			Long findManagerCount = employeeRepository.countReportiesByManagerId(id);
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
					+ "ELSE NULL END AS number_of_days, ea.billable_type FROM employee e INNER JOIN db_emp_portal.employee_aud ea \n"
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
	
}
