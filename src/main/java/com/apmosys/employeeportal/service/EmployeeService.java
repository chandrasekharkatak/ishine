package com.apmosys.employeeportal.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoPortalDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeAssetMap;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeSpecializationMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Log;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeSpecializationMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.LogsRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
import com.apmosys.employeeportal.utility.DbTable;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.LogEvents;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

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

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;

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
	private HttpServletRequest httpRequest;
	
	@Value("${timesheet.reconcile.days}")
	private Long timesheetReconcileDays;

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
			employee.setIsTimesheetLockCheckEnable("true");
			employee.setReportingManagerId(employeedto.getReportingManagerId());
			employee.setApprovalsTo(employeedto.getApprovalsTo());
			employee.setDesignationId(employeedto.getDesignationId());

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
		//apiLogInfo.setSubFeatureName("add_holidays");
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

		List<PreviousEmployment> list = new ArrayList<PreviousEmployment>();

		previousEmployeeDTOList.forEach((previousEmployeeDTO) -> {

			PreviousEmployment previousEmployment = new PreviousEmployment();
			LocalDate dateOfJoining;
			LocalDate dateOfRelieving;
			if(previousEmployeeDTO.getDateOfJoining() != null) {
				dateOfJoining = stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd");
			}else {
				dateOfJoining = null;
			}
			if(previousEmployeeDTO.getDateOfRelieving() != null){
				dateOfRelieving = stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd");
			}else {
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
	
	public ServiceResponse updateEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_employee");
		apiLogInfo.setApiUrl("/api/updateEmployeeByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId()+ "certification :" +employeedto.getCertifications()+ "updatedCertification :"+employeedto.getUpdatedCertifications()+ "experience : "+employeedto.getExperience());
		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();

		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employee = employeeObject.get();

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setEmployeementId(employeedto.getEmployeementId());
				employee.setName(employeedto.getName());
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
				if(employee.getEmploymentstatus().equals("Resigned") || employee.getEmploymentstatus().equals("InActive") )  {
					employee.setDateOfResign(employeedto.getDateOfResign() != null
							? stringToDateTimeParser.getDate(employeedto.getDateOfResign(), "yyyy-MM-dd")
							: null);
				}else if(employee.getEmploymentstatus().equals("Probation") || employee.getEmploymentstatus().equals("Confirmed")) {
					employee.setDateOfResign(null);
				}
				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
				employee.setBillable(employeedto.getBillable());
				employee.setChild1(employeedto.getChild1());
				employee.setChild2(employeedto.getChild2());
				employee.setChild3(employeedto.getChild3());
				employee.setMothersName(employeedto.getMothersName());
				employee.setSpouse(employeedto.getSpouse());
				employee.setTotalExperience(employeedto.getTotalExperience());
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setReportingManagerId(employeedto.getReportingManagerId());
				employee.setApprovalsTo(employeedto.getApprovalsTo());
				employee.setDesignationId(employeedto.getDesignationId());
				
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
						draftEmployee.setReportingManagerId(dbResponse.getReportingManagerId());
						draftEmployee.setApprovalsTo(dbResponse.getApprovalsTo());
						draftEmployee.setDesignationId(dbResponse.getDesignationId());
						
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

	public ServiceResponse updateEmployeeByEmpIdByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
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
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not found" + employeedto.getEmployeementId());
			}

		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong");
			e.printStackTrace();
		}
		return response;
	}

	public ServiceResponse getAllEmployees() {
		ServiceResponse response = new ServiceResponse();
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
					empDTO.setFailedAttempt(failedAttempt);
				
					ServiceResponse completionResponse = getEmployeeProfileCompletion(empDTO);
					EmployeeDTO emp = (EmployeeDTO) completionResponse.getServiceResponse();
					
					empDTO.setProfileCompletedPercent(emp != null ? emp.getProfileCompletedPercent() : 0.00);
					
					
					
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

	public ServiceResponse previewImage(MultipartFile image) {
		ServiceResponse response = new ServiceResponse();

		try {

			byte[] imageBytes = image.getBytes();

			if (imageBytes != null) {

				response.setServiceResponse(imageBytes);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceResponse("Failed To Preview Image !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse getAllEmployeesByRole(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_employee");
		apiLogInfo.setApiUrl("/api/getAllEmployeesByRole");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("role : " + employeedto.getRole());
		try {

			List<JobRole> jobRoleObj;
			List<String> jobRoles = new ArrayList<String>(Arrays.asList("Employee", "HR"));
			if (employeedto.getRole().equals("Manager")) {
				jobRoleObj = jobRoleRepository.findByEmployeeRoleNotIn(jobRoles);
			} else {
				jobRoleObj = jobRoleRepository.findByEmployeeRole(employeedto.getRole());
			}
			List<EmployeeDTO> employeeList = new ArrayList<EmployeeDTO>();
			
			if (!jobRoleObj.isEmpty()) {
				
				List<Long> jobRoleIds = 
						jobRoleObj.stream().map(JobRole::getJobRoleId).collect(Collectors.toList());
				
					List<Object[]> empList = employeeRepository.getEmployeesByRoleIds(jobRoleIds);

					Optional.ofNullable(empList).ifPresentOrElse((list) -> {

						list.forEach((object) -> {
							EmployeeDTO dto = new EmployeeDTO();
							dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
							dto.setName(object[1] != null ? object[1].toString() : null);
							dto.setJobRoleName(object[2] != null ? object[2].toString() : null);
							dto.setDepartmentId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
							dto.setEmployeementId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
							employeeList.add(dto);
						});
						
						// sorted alphabetically
						
						final List<EmployeeDTO> employeeDTOList = 
						employeeList
						  .stream()
						  .sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
						  
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(employeeDTOList);
						
						apiLogInfo.setApiResponse("employeeDTOList : " +employeeDTOList);			
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

	public ServiceResponse checkEmployeeEmail(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Employee checkEmployeeEmail = employeeRepository.findByEmail(employeedto.getEmail());
			DraftEmployee checkDraftEmployeementEmail = draftEmployeeRepository.findByEmail(employeedto.getEmail());

			if (employeedto.getEmail() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				if ((employeedto.getEmpId() != null) && !checkEmployeeEmail.getEmpId().equals(employeedto.getEmpId()) && checkEmployeeEmail != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Email already exist!");
				}else if((employeedto.getEmpId() == null) && checkEmployeeEmail != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Email already exist!");
				}
				if (checkDraftEmployeementEmail != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Email already exist in Employee Draft!");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmployeementId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {
			Employee checkEmployeementId = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());
			DraftEmployee checkDraftEmployeementId = draftEmployeeRepository
					.findByEmployeementId(employeedto.getEmployeementId());

			if (checkEmployeementId == null && checkDraftEmployeementId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				if (checkEmployeementId != null && !employeedto.getEmpId().equals(checkEmployeementId.getEmpId())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employeement ID already exist!");
				}
				if (checkDraftEmployeementId != null && !employeedto.getEmail().equals(checkDraftEmployeementId.getEmail())) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employeement ID already exist in Employee Draft!");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmployeeMobileNo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		List<Employee> checkEmployeeMobileNo = null;
		List<DraftEmployee> checkDraftEmployeeMobileNo = null;
		try {

			if (employeedto.getEmpId() != null) {
				checkEmployeeMobileNo = employeeRepository.findByMobileNoAndEmpId(employeedto.getMobileNo(),
						employeedto.getEmpId());
				checkDraftEmployeeMobileNo = draftEmployeeRepository
						.findByMobileNoAndDraftEmpId(employeedto.getMobileNo(), employeedto.getEmpId());
			} else {
				checkEmployeeMobileNo = employeeRepository.findByMobileNo(employeedto.getMobileNo());
				checkDraftEmployeeMobileNo = draftEmployeeRepository.findByMobileNo(employeedto.getMobileNo());
			}

			if (employeedto.getMobileNo() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				if (!checkEmployeeMobileNo.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Mobile Number already exist!");
				}
				if (!checkDraftEmployeeMobileNo.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Mobile Number already exist in Employee Draft!");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmployeeAadharNumber(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Employee> existingEmployeeAadhar = employeeRepository.findByAadhar(employeedto.getAadhar());
			List<DraftEmployee> existingDraftEmployeeAadhar = draftEmployeeRepository.findByAadhar(employeedto.getAadhar());

			if(!existingEmployeeAadhar.isEmpty()) {
				existingEmployeeAadhar.forEach((employee) -> {
					if(!employee.getEmpId().equals(employeedto.getEmpId())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Aadhaar Number already exists !!");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					}
				});
			}
			
			if(!existingDraftEmployeeAadhar.isEmpty()) {
				existingDraftEmployeeAadhar.forEach((employee) -> {
					if(!employee.getDraftEmpId().equals(employeedto.getEmpId())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Aadhaar Number already exists !!");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmployeePanNumber(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		List<Employee> existingEmployeePan = null;
		List<DraftEmployee> existingEmployeeDraftPan = null;

		try {

			if (employeedto.getEmpId() != null) {
				existingEmployeePan = employeeRepository.findByPanNumberAndEmpId(employeedto.getPanNumber(),
						employeedto.getEmpId());
				existingEmployeeDraftPan = draftEmployeeRepository
						.findByPanNumberAndDraftEmpId(employeedto.getPanNumber(), employeedto.getEmpId());
			} else {
				existingEmployeePan = employeeRepository.findByPanNumber(employeedto.getPanNumber());
				existingEmployeeDraftPan = draftEmployeeRepository.findByPanNumber(employeedto.getPanNumber());
			}

			if (employeedto.getPanNumber() != null && !existingEmployeePan.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PAN Number already exist!");
			} else if (employeedto.getPanNumber() != null && !existingEmployeeDraftPan.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PAN Number already exist in Employee Draft!");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public EmployeeDTO getEmployeeInfoOnLogin(String employeeEmail) {
		EmployeeDTO employee = new EmployeeDTO();
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
				});
				return employee;
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return employee;
	}

	public ServiceResponse getAllEmployeesBirthDayToday() {
		ServiceResponse response = new ServiceResponse();
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
		try {
			Employee employee = employeeRepository.getById(employeedto.getEmpId());
			employee.setInvalidAccessAttempt(0);
			employeeRepository.save(employee);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Account is Unblock !!");

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;

	}

	public ServiceResponse getEmployees() {
		ServiceResponse response = new ServiceResponse();

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
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is empty.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllManagers() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allEmployees = employeeRepository.getAllManagers();			

			Optional.ofNullable(allEmployees).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No employees found. List is empty.");
				} else {
					List<EmployeeDTO> empDTO = new ArrayList<>();

					list.forEach((object) -> {
						
						EmployeeDTO dto = new EmployeeDTO();
						dto.setManagerId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						empDTO.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(empDTO);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No employees found. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
//	employee working history
	
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
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Password Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not found");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse sendMailByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			System.out.println(employeedto.getEmployeementId() + " : employeementy id");
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

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				System.out.println(employeedto.getEmployeementId() + "  ===");
				response.setServiceResponse("Employee Profile Not found");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse addDemographicsInfo(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee updation failed");
				}
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Not found");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllEmployeeInfo() {
		ServiceResponse response = new ServiceResponse();
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
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Info not found.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse updateLeaveBalanceList(EmployeeDTO employeedto) {	
		ServiceResponse response = new ServiceResponse();	
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
							
							
					}else {				
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);						
						response.setServiceResponse("leave balance Updation Failed.");	
							
							
					}		
				}	
			}	
	
		} catch (Exception e) {	
			e.printStackTrace();	
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);	
			response.setServiceResponse("Something Went Wrong.");	
			response.setServiceError(e.getMessage());	
		}	
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

		try {
			if(employeedto.getEmail() != null) {
				EmployeeDTO employeeInfo = getEmployeeInfoOnLogin(employeedto.getEmail());

				if (employeeInfo != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(employeeInfo);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Info not found");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee email not found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
}
