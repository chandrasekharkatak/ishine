package com.apmosys.employeeportal.service;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
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
	private ModelMapper mapper;

	@Value("${timesheet.lock.days}")
	private Integer timesheetLockDays;

	@Transactional
	public ServiceResponse createEmployee(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Employee employee = new Employee();

			employee.setEmployeementId(employeedto.getEmployeementId());
			employee.setName(employeedto.getName());
			employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));
			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd"));
			employee.setManagerId(employeedto.getManagerId());
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
			employee.setAboutMe("Add about yourself.");
			employee.setViewsOnOrganisation("Add your views.");
			employee.setJobRoleId(employeedto.getJobRoleId());
			employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
			employee.setCreatedBy(employeedto.getCreatedBy());
			employee.setExperience(employeedto.getExperience());
			employee.setRole(employeedto.getRole());
			employee.setWorkLocation(employeedto.getWorkLocation());
			employee.setInvalidAccessAttempt(0);
			Employee newEmployee = employeeRepository.save(employee);

			Optional.ofNullable(employeedto.getPreviousEmploymentList()).ifPresent((previousEmployerList) -> {

				if (!previousEmployerList.isEmpty()) {
					previousEmployerList.forEach((previousEmployer) -> {
						previousEmployer.setEmpId(newEmployee.getEmpId());

					});
					addPreviousEmployer(previousEmployerList, employeedto.getIsDraft());
				}

			});

			Optional.ofNullable(employeedto.getCertifications()).ifPresent((certificationList) -> {

				if (!certificationList.isEmpty()) {
					certificationList.forEach((certification) -> {
						certification.setEmpId(newEmployee.getEmpId());

					});
					addCertifications(certificationList, employeedto.getIsDraft());
				}

			});

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

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse createEmployeeByList(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Employee employee = new Employee();

			employee.setEmployeementId(employeedto.getEmployeementId());
			employee.setName(employeedto.getName());
			if (employeedto.getDateOfBirth() == null) {					
				employee.setDateOfBirth(stringToDateTimeParser.getDate("2001-01-01", "yyyy-MM-dd"));					
			} else {					
				employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth(), "yyyy-MM-dd"));					
			}
			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd"));
			employee.setManagerId(employeedto.getManagerId());
			employee.setEmail(employeedto.getEmail());
			employee.setGender(employeedto.getGender());
			employee.setBloodGroup(employeedto.getBloodGroup());
			employee.setMaritalStatus(employeedto.getMaritalStatus());
			employee.setFatherName(employeedto.getFatherName());
			employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
			employee.setMotherTongue(employeedto.getMotherTongue());
			employee.setPassportNumber(employeedto.getPassportNumber());
			if (employeedto.getAadhar() == null) {					
				employee.setAadhar(123456789012l);					
			} else {					
				employee.setAadhar(employeedto.getAadhar());					
			}
			employee.setPanNumber(employeedto.getPanNumber());
			employee.setMobileNo(employeedto.getMobileNo());
			employee.setLandline(employeedto.getLandline());
			employee.setAddress(employeedto.getAddress());
			employee.setCity(employeedto.getCity());
			employee.setState(employeedto.getState());
			employee.setCountry(employeedto.getCountry());
			employee.setPincode(employeedto.getPincode());
			if (employeedto.getAlternateMobileNo() == null) {					
				employee.setAlternateMobileNo(1111111111l);					
			} else {					
				employee.setAlternateMobileNo(employeedto.getAlternateMobileNo());					
			}
			employee.setPermanentAddress(employeedto.getPermanentAddress());
			employee.setEmergencyContactPerson(employeedto.getEmergencyContactPerson());
			employee.setRelation(employeedto.getRelation());
			employee.setEmergencyContactMobile(employeedto.getEmergencyContactMobile());
			employee.setNoticePeriod(employeedto.getNoticePeriod());
			
			LocalDate joinDate = stringToDateTimeParser.getDate(employeedto.getDateOfJoining(), "yyyy-MM-dd");		
			LocalDate todayDate = LocalDate.now();		
			LocalDate returnvalue = todayDate.minusMonths(9);		
			Integer result = returnvalue.compareTo(joinDate);		
					
			if(employeedto.getEmploymentstatus().equals("N")) {		
				employee.setEmploymentstatus("InActive");		
			}else if(result <= 0) {		
				employee.setEmploymentstatus("Probation");		
			}else if(result >= 0) {		
				employee.setEmploymentstatus("Confirmed");		
			}		
				
//			employee.setEmploymentstatus(employeedto.getEmploymentstatus());
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
			employee.setAboutMe("Add about yourself.");
			employee.setViewsOnOrganisation("Add your views.");
			
			if(jobRoleRepository.findById(employeedto.getJobRoleId()).isEmpty())					
			{					
				employee.setJobRoleId(135L);					
			}					
			else					
			{					
				employee.setJobRoleId(employeedto.getJobRoleId());					
			}					
										
			//employee.setJobRoleId(employeedto.getJobRoleId());				
							
			employee.setPassword(employeedto.getPassword());		
			//employee.setPassword(EncryptDecrypt.encrypt(defaultPaswword));
			employee.setCreatedBy(employeedto.getCreatedBy());
			employee.setExperience(employeedto.getExperience());
			employee.setRole(employeedto.getRole());
			employee.setWorkLocation(employeedto.getWorkLocation());

			Employee newEmployee = employeeRepository.save(employee);

			Optional.ofNullable(employeedto.getPreviousEmploymentList()).ifPresent((previousEmployerList) -> {

				if (!previousEmployerList.isEmpty()) {
					previousEmployerList.forEach((previousEmployer) -> {
						previousEmployer.setEmpId(newEmployee.getEmpId());

					});
					addPreviousEmployer(previousEmployerList, employeedto.getIsDraft());
				}

			});

			Optional.ofNullable(employeedto.getCertifications()).ifPresent((certificationList) -> {

				if (!certificationList.isEmpty()) {
					certificationList.forEach((certification) -> {
						certification.setEmpId(newEmployee.getEmpId());

					});
					addCertifications(certificationList, employeedto.getIsDraft());
				}

			});

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

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Profile Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public boolean addPreviousEmployer(List<PreviousEmploymentDTO> previousEmployeeDTOList, String isDraft) {

		List<PreviousEmployment> list = new ArrayList<PreviousEmployment>();

		previousEmployeeDTOList.forEach((previousEmployeeDTO) -> {

			PreviousEmployment previousEmployment = new PreviousEmployment();

			previousEmployment.setDateOfJoining(
					stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd"));
			previousEmployment.setDateOfRelieving(
					stringToDateTimeParser.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd"));
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

			EmployeeCertificate employeeCertificate = new EmployeeCertificate();

			employeeCertificate.setCertificationName(certificate.getCertificationName());
			employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
			employeeCertificate.setDateOfCompletion(
					stringToDateTimeParser.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
			employeeCertificate.setDuration(certificate.getDuration());
			employeeCertificate.setEmpId(certificate.getEmpId());
			employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
			employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());
			employeeCertificate.setIsDraft(isDraft);
			list.add(employeeCertificate);

		});

		return (employeeCertificateRepository.saveAll(list).isEmpty()) ? false : true;
	}

	public ServiceResponse getEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		EmployeeDTO empDTO = new EmployeeDTO();
		List<EmployeeCertificateDTO> certificationDTOlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> previousEmploymentDTOList = new ArrayList<PreviousEmploymentDTO>();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			List<Object[]> objectList = employeeRepository.getEmployeeByEmpId(employeedto.getEmpId());
			List<EmployeeCertificate> certificationsList = employeeCertificateRepository
					.findByEmpId(employeedto.getEmpId());
			List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
					.findByEmpId(employeedto.getEmpId());

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

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(empDTO);
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

	public ServiceResponse deleteEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employeeToBeDeleted = employeeObject.get();
				Long count = employeeRepository.countByEmpId(employeeToBeDeleted.getEmpId());
				
				if (count == 0) {	
					
					employeeRepository.deleteById(employeeToBeDeleted.getEmpId());	
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
					response.setServiceResponse("Employee Profile Deleted");
					
				}else {	
						
					List<EmployeeTeamMap> deleteEmpFromTeam = employeeTeamMapRepository.findByEmpId(employeeToBeDeleted.getEmpId());	
						
					for(EmployeeTeamMap empToBeDeletedFromTeam :deleteEmpFromTeam) {	
						// 1: Active   0: InActive	
						empToBeDeletedFromTeam.setActive((long) 0);	
						employeeTeamMapRepository.save(empToBeDeletedFromTeam);	
					}	
					employeeToBeDeleted.setEmploymentstatus("InActive");	
					employeeRepository.save(employeeToBeDeleted);	
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
					response.setServiceResponse("Employment Status changed to InActive");	
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

	public ServiceResponse updateEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
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
							.filter((certification) -> certification.getEmployeeCertificateId() == null).collect(Collectors.toList());
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
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null).collect(Collectors.toList());
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
	
	public ServiceResponse updateEmployeeByEmpIdByList(EmployeeDTO employeedto) {					
		ServiceResponse response = new ServiceResponse();					
		try {					
								
			System.out.println(employeedto.getEmployeementId() + " : employeementy id");					
			Optional<Employee> employeeObject = Optional.ofNullable(employeeRepository.findByEmployeementId(employeedto.getEmployeementId()));					
			if (employeeObject.isPresent()) {					
									
								
				Employee m = employeeRepository.findByEmployeementId(employeedto.getManagerId());					
				System.out.println(employeedto.getManagerId() + " : manager id");					
									
				Long primaryEmpId = m.getEmpId(); 					
				System.out.println(primaryEmpId);					
									
				Employee employee = employeeObject.get();					
									
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());					
									
				employee.setManagerId(primaryEmpId);	
									
				Employee dbResponse = employeeRepository.save(employee);					
									
				if (dbResponse != null) {					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);					
					response.setServiceResponse("Employee Profile Updated.");					
				} else {					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
					response.setServiceResponse("Employee Profile Updation Failed.");					
				}					
									
			}else {					
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Employee Profile Not found" + employeedto.getEmployeementId());					
			}					
								
		}catch(Exception e) {					
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
			response.setServiceResponse("Something went wrong");					
			e.printStackTrace();					
		}					
		return response;					
	}

//	public ServiceResponse getAllEmployees() {
//
//		ServiceResponse response = new ServiceResponse();
//		try {
//			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();
//			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//
//			if (allEmployeeList != null) {
//				allEmployeeList.forEach((object) -> {
//					EmployeeDTO empDTO = new EmployeeDTO();
//
//					empDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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
//					dtoList.add(empDTO);
//				});
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee List is null.");
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

	public ServiceResponse getAllEmployees() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allEmployeeList = employeeRepository.getAllEmployees();

			if (allEmployeeList != null) {
				List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
				allEmployeeList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					empDTO.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					empDTO.setName(object[1] != null ? object[1].toString() : null);
					empDTO.setEmail(object[2] != null ? object[2].toString() : null);
					empDTO.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
					empDTO.setDateOfJoining(
							object[4] != null ? stringToDateTimeParser.formatDateToString(object[4].toString()) : null);
					empDTO.setEmployeementId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					empDTO.setInvalidAccessAttempt(object[6] != null ?Integer.parseInt(object[6].toString()):null);
					empDTO.setFailedAttempt(failedAttempt);
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
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee Profile Picture Upload Failed.");
						}

					} else {
						response.setServiceResponse("Failed To save Image !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
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
							} else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Employee Profile Picture Upload Failed.");
							}

						} else {
							response.setServiceResponse("Failed To save Image !!");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						}
					} else {
						response.setServiceResponse("Failed To Delete Existing Profile Image !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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
							.filter((certification) -> certification.getEmployeeCertificateId() == null).collect(Collectors.toList());
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
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null).collect(Collectors.toList());
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

//	public ServiceResponse getAllEmployeesByRole() {
//		ServiceResponse response = new ServiceResponse();
//
//		try {
//
//			List<Object[]> objectlist = employeeRepository.getEmployeesByRole();
//
//			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
//
//			Optional.ofNullable(objectlist).ifPresentOrElse((list) -> {
//
//				if (!list.isEmpty()) {
//					list.forEach((object) -> {
//						EmployeeDTO dto = new EmployeeDTO();
//						dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//						dto.setName(object[1] != null ? object[1].toString() : null);
//						dto.setRole(object[2] != null ? object[2].toString() : null);
//						dtoList.add(dto);
//					});
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(dtoList);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Employee list is empty.");
//				}
//
//			}, () -> {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Employee list is null");
//			});
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}
	
	public ServiceResponse getAllEmployeesByRole(EmployeeDTO employeedto) {	
		ServiceResponse response = new ServiceResponse();	
		try {
			List<JobRole> jobRoleObj = jobRoleRepository.findAll();
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();	
				
			for(JobRole jobRole : jobRoleObj) {
				String jobRoleName = jobRole.getName();
				
				if (jobRoleName.contains("-")) {
					String jobname = jobRoleName.split("-")[1];	
						
					if(jobname.equals(employeedto.getRole())) {
							
						List<Object[]> objectlist = employeeRepository.getEmployeesByRole(jobRoleName);	
							
									Optional.ofNullable(objectlist).ifPresentOrElse((list) -> {	
										
											list.forEach((object) -> {	
												EmployeeDTO dto = new EmployeeDTO();	
												dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);	
												dto.setName(object[1] != null ? object[1].toString() : null);	
												dto.setJobRoleName(object[2] != null ? object[2].toString() : null);	
												dtoList.add(dto);	
											});	
											response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
											response.setServiceResponse(dtoList);
											
									}, () -> {	
										response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
										response.setServiceResponse("Employee list is null");	
									});	
							
					}	
						
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

	public ServiceResponse getAllEmployeesByDepartmentIds(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Optional.ofNullable(employeedto.getDepartmentList()).ifPresentOrElse((departmentList) -> {

				if (departmentList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department list is empty.");
				} else {
					List<Long> deptIds = departmentList.stream().map((department) -> {
						return department.getDeptId();
					}).collect(Collectors.toList());

					List<Object[]> objectArrayList = employeeRepository.getAllEmployeesByDepartmentIds(deptIds);

					Optional.ofNullable(objectArrayList).ifPresent((list) -> {

						if (list.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee list is empty.");
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
						}
					});

				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateEmployeePassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

		try {

			Employee employee = employeeRepository.findByEmail(employeedto.getEmail());

			if (employee != null) {

				employee.setPassword(employeedto.getNewPassword());
				Employee dbResponse = employeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Password Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Password Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Not Found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkEmployeeOldPassword(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();

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
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Incorrect Old Password");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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
				if (checkEmployeeEmail != null) {
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

			if (employeedto.getEmployeementId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				if (checkEmployeementId != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employeement ID already exist!");
				}
				if (checkDraftEmployeementId != null) {
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
		List<Employee> existingEmployeeAadhar = null;
		List<DraftEmployee> existingDraftEmployeeAadhar = null;

		try {
			if (employeedto.getEmpId() != null) {
				existingEmployeeAadhar = employeeRepository.findByAadharAndEmpId(employeedto.getAadhar(),
						employeedto.getEmpId());
				existingDraftEmployeeAadhar = draftEmployeeRepository.findByAadharAndDraftEmpId(employeedto.getAadhar(),
						employeedto.getEmpId());
			} else {
				existingEmployeeAadhar = employeeRepository.findByAadhar(employeedto.getAadhar());
				existingDraftEmployeeAadhar = draftEmployeeRepository.findByAadhar(employeedto.getAadhar());
			}

			if (employeedto.getAadhar() != null && !existingEmployeeAadhar.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Aadhar Number already exist!");
			} else if (employeedto.getAadhar() != null && !existingDraftEmployeeAadhar.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Aadhar Number already exist in Employee Draft!");
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
					employee.setTimesheetLockDays(timesheetLockDays);

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
		try {
			
			List<Object[]> list = employeeRepository.getHierarchyByEmpId(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Hierarchy found");
			} else {

				list.forEach((object) -> {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()): null);
					dto.setName(object[1] != null ? object[1].toString(): null);
					dto.setEmail(object[2] != null ? object[2].toString(): null);
					dto.setJobRoleName(object[3] != null ? object[3].toString(): null);
					dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()): null);
					dto.setManagerName(object[5] != null ? object[5].toString(): null);
					dto.setEmployeementId(object[6] != null ? Long.parseLong(object[6].toString()): null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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

}
