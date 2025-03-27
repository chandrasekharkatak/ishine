package com.apmosys.employeeportal.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.EmployeeDocument;
import com.apmosys.employeeportal.model.Log;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.EmployeeDocumentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LogsRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
import com.apmosys.employeeportal.utility.DbTable;
import com.apmosys.employeeportal.utility.LogEvents;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class DraftEmployeeService {

	@Autowired
	DraftEmployeeRepository draftEmployeeRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	EmployeeService employeeService;

	@Autowired
	PreviousEmploymentRepository previousEmploymentRepository;

	@Autowired
	EmployeeCertificateRepository employeeCertificateRepository;

	@Value("${file.location.image}")
	private String imageFileLocation;

	@Autowired
	private LogsRepository logsRepository;

	@Autowired
	private MailService mailService;

	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmployeeDocumentRepository employeeDocumentRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	
	public ServiceResponse createDraftEmployee(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("create_DraftEmployee");
		apiLogInfo.setApiUrl("/api/createDraftEmployee");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("draftEmployeeId : "+employeedto.getDraftEmpId());
		try {

			DraftEmployee employee = new DraftEmployee();

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
			employee.setUpdateApplicationStatus(employeedto.getUpdateApplicationStatus());
			employee.setJobRoleId(employeedto.getJobRoleId());
			employee.setExperience(employeedto.getExperience());
			// employee.setRole(employeedto.getRole());
			employee.setWorkLocation(employeedto.getWorkLocation());
			employee.setBillable(employeedto.getBillable());
			employee.setChild1(employeedto.getChild1());
			employee.setChild2(employeedto.getChild2());
			employee.setChild3(employeedto.getChild3());
			employee.setMothersName(employeedto.getMothersName());
			employee.setSpouse(employeedto.getSpouse());
			employee.setTotalExperience(employeedto.getTotalExperience());
			employee.setReportingManagerId(employeedto.getReportingManagerId());
			employee.setApprovalsTo(employeedto.getApprovalsTo());
			employee.setDesignationId(employeedto.getDesignationId());
			employee.setProbationPeriod(employeedto.getProbationPeriod());
			employee.setIsConsultant(employeedto.getIsConsultant());
			if(employee.getEmploymentstatus().equals("Resigned") || employee.getEmploymentstatus().equals("InActive") )  {
				employee.setDateOfResign(employeedto.getDateOfResign() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfResign(), "yyyy-MM-dd")
						: null);
				employee.setDateOfRelieving(employeedto.getDateOfRelieving());
			}
			//added by rahul for reffered type and reffered Name
			employee.setReferedType(employeedto.getReferedType());
			employee.setReferedName(employeedto.getReferedName());
			DraftEmployee dbResponse = draftEmployeeRepository.save(employee);

			Optional.ofNullable(employeedto.getPreviousEmploymentList()).ifPresent((previousEmployerList) -> {

				if (!previousEmployerList.isEmpty()) {
					previousEmployerList.forEach((previousEmployer) -> {
						
						previousEmployer.setEmpId(dbResponse.getDraftEmpId());

					});
					employeeService.addPreviousEmployer(previousEmployerList, employeedto.getIsDraft());
				}

			});
			
			
			Optional.ofNullable(employeedto.getCertifications()).ifPresent((certificationList) -> {
				
				if (!certificationList.isEmpty()) {
					
					certificationList.forEach((certification) -> {
						String dateOfCompletion;
						if(certification.getDateOfCompletion() != null) {
							dateOfCompletion = certification.getDateOfCompletion().toString();
						}else {
							dateOfCompletion = null;
						}
						certification.setEmpId(dbResponse.getDraftEmpId());

					});
					employeeService.addCertifications(certificationList, employeedto.getIsDraft());
				}

			});

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dbResponse);
				apiLogInfo.setApiResponse("dbResponse : " +dbResponse);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Creation Failed.");
				apiLogInfo.setApiResponse("Draft Employee Profile Creation Failed.");
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

//	public ServiceResponse getDraftEmployeeById(EmployeeDTO employeedto) {
//		ServiceResponse response = new ServiceResponse();
//		try {
//			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getEmpId());
//
//			if (employeeObject.isPresent()) {
//
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(employeeObject.get());
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Draft Employee Profile Not Found");
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

	public ServiceResponse getDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getDraftEmployeeById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("draftEmployeeId : "+employeedto.getDraftEmpId()+""+employeedto.getEmployeementId());
		
		EmployeeDTO empDTO = new EmployeeDTO();
		List<EmployeeCertificateDTO> certificationDTOlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> previousEmploymentDTOList = new ArrayList<PreviousEmploymentDTO>();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			List<Object[]> objectList = draftEmployeeRepository.getDraftEmployeeByEmpId(employeedto.getEmpId());
			List<EmployeeCertificate> certificationsList = employeeCertificateRepository
					.findByEmpIdAndIsDraft(employeedto.getEmpId(), employeedto.getIsDraft());
			List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
					.findByEmpIdAndIsDraft(employeedto.getEmpId(), employeedto.getIsDraft());

			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					empDTO.setDraftEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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

					// empDTO.setEmpId(object[53] != null ? Long.parseLong(object[53].toString()) :
					// null);

					empDTO.setBillable(object[53] != null ? (object[53].toString()) : null);
					empDTO.setChild1(object[54] != null ? (object[54].toString()) : null);
					empDTO.setChild2(object[55] != null ? (object[55].toString()) : null);
					empDTO.setChild3(object[56] != null ? (object[56].toString()) : null);
					empDTO.setMothersName(object[57] != null ? (object[57].toString()) : null);
					empDTO.setSpouse(object[58] != null ? (object[58].toString()) : null);
					empDTO.setTotalExperience(object[59] != null ? Float.parseFloat(object[59].toString()) : null);
					empDTO.setReportingManagerName(object[60] != null ? object[60].toString() : null);
					empDTO.setProbationPeriod(object[61] != null ? Short.parseShort(object[61].toString()) : null );
					empDTO.setDateOfResign(object[62] != null ? format.format(format.parse(object[62].toString())) : null);
                    empDTO.setDateOfRelieving(object[63] != null ? format.format(format.parse(object[63].toString())) : null);
                    
//					if (object[42] != null) {
//
//						File actualFile = new File(
//								Paths.get(imageFileLocation + File.separator + object[42].toString()).toString());
//
//						if (actualFile.exists()) {
//							byte[] imageBytes = Files.readAllBytes(
//									Paths.get(imageFileLocation + File.separator + object[42].toString()));
//							empDTO.setImageBytes(imageBytes);
//						}
//
//					}
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
				apiLogInfo.setApiResponse("Employee draft found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setApiResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/deleteDraftEmployeeById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("draftEmpId : "+employeedto.getDraftEmpId());
		List<EmployeeCertificate> certificationlist = new ArrayList<EmployeeCertificate>();
		List<PreviousEmployment> previousEmploymentList = new ArrayList<PreviousEmployment>();
		List<EmployeeDocument> documentList = new ArrayList<EmployeeDocument>();
		try {

			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());
			if (employeeObject.isPresent()) {
				DraftEmployee employeeToBeDeleted = employeeObject.get();

				previousEmploymentList = previousEmploymentRepository
						.findByEmpIdAndIsDraft(employeeToBeDeleted.getDraftEmpId(), "true");
				certificationlist = employeeCertificateRepository
						.findByEmpIdAndIsDraft(employeeToBeDeleted.getDraftEmpId(), "true");
				documentList = employeeDocumentRepository.findByEmpIdAndIsDraft(employeedto.getDraftEmpId(), "true");

				File directoryPath = new File(imageFileLocation + File.separator + "Documents" + File.separator
						+ "Draft" + File.separator + employeedto.getEmployeementId());
				if(directoryPath.exists()) {
					String[] contents = directoryPath.list();
					
					for (EmployeeDocument document : documentList) {

						for (int i = 0; i < contents.length; i++) {
							if (contents[i].equals(document.getDocumentName())) {
								File file = new File(directoryPath.getAbsolutePath() + File.separator + contents[i]);
								file.delete();
							}

						}
					}
				}

				if (previousEmploymentList != null && !previousEmploymentList.isEmpty()) {
					previousEmploymentList.forEach((prevEmployer) -> {
						previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
					});
				}

				if (certificationlist != null && !certificationlist.isEmpty()) {
					certificationlist.forEach((certification) -> {
						employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
					});
				}

				if (documentList != null && !documentList.isEmpty()) {
					documentList.forEach((document) -> {
						employeeDocumentRepository.deleteById(document.getEmployeeDocumentId());
					});
				}

				draftEmployeeRepository.deleteById(employeeToBeDeleted.getDraftEmpId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Draft Employee Profile Deleted");
				apiLogInfo.setApiResponse("Draft Employee Profile Deleted");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Not Found");
				apiLogInfo.setApiResponse("Draft Employee Profile Not Found");
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

	public ServiceResponse updateDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateDraftEmployeeById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("draftEmpId : "+employeedto.getDraftEmpId());
		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();

		try {
			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());
			if (employeeObject.isPresent()) {
				DraftEmployee employee = employeeObject.get();

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
				employee.setUpdateApplicationStatus(employeedto.getUpdateApplicationStatus());

				employee.setBillable(employeedto.getBillable());
				employee.setChild1(employeedto.getChild1());
				employee.setChild2(employeedto.getChild2());
				employee.setChild3(employeedto.getChild3());
				employee.setMothersName(employeedto.getMothersName());
				employee.setSpouse(employeedto.getSpouse());
				employee.setTotalExperience(employeedto.getTotalExperience());
				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
				employee.setAboutMe(employeedto.getAboutMe());
				employee.setReferedType(employeedto.getReferedType());
				employee.setReferedName(employeedto.getReferedName());
				// Certification
				// Case 1 : Updating Existing certification
				if (employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
					employeedto.getCertifications().stream()
							.filter((certification) -> certification.getEmployeeCertificateId() != null)
							.forEach((certificate) -> {
								
								if(certificate.getCertificationName() == null || certificate.getCertificationName() == "") {
									employeeCertificateRepository.deleteById(certificate.getEmployeeCertificateId());
								}else {
									LocalDate dateOfCompletion;
									if(certificate.getDateOfCompletion() != null) {
										dateOfCompletion = stringToDateTimeParser
												.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd");
									}else {
										dateOfCompletion = null;
									}

									EmployeeCertificate employeeCertificate = employeeCertificateRepository
											.findById(certificate.getEmployeeCertificateId()).get();

									employeeCertificate.setCertificationName(certificate.getCertificationName());
									employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
									employeeCertificate.setDateOfCompletion(dateOfCompletion);
									employeeCertificate.setDuration(certificate.getDuration());
									employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
									employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());

									employeeCertificateRepository.save(employeeCertificate);
								}
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
							certification.setEmpId(employeedto.getDraftEmpId());

						});
						employeeService.addCertifications(newCertificationlist, employeedto.getIsDraft());
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
							.findByEmpId(employeedto.getDraftEmpId());

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
								
									LocalDate dateOfJoining;
									LocalDate dateOfReleiving;
									if(previousEmployeeDTO.getDateOfJoining() != null) {
										dateOfJoining = stringToDateTimeParser
												.getDate(previousEmployeeDTO.getDateOfJoining(), "yyyy-MM-dd");
									}else {
										dateOfJoining = null;
									}
									if(previousEmployeeDTO.getDateOfRelieving() != null) {
										dateOfReleiving = stringToDateTimeParser
												.getDate(previousEmployeeDTO.getDateOfRelieving(), "yyyy-MM-dd");
									}else {
										dateOfReleiving = null;
									}
									

									PreviousEmployment previousEmployment = previousEmploymentRepository
											.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();

									previousEmployment.setDateOfJoining(dateOfJoining);
									previousEmployment.setDateOfRelieving(dateOfReleiving);
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
								previousEmployer.setEmpId(employeedto.getDraftEmpId());

							});
							employeeService.addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
						}

						employeedto.getUpdatedPreviousEmploymentList().stream()
								.filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
								.forEach((prevEmployer) -> {
									previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
								});
					}
				}

				DraftEmployee dbResponse = draftEmployeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dbResponse);
					apiLogInfo.setApiResponse("dbResponse");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Draft Employee Profile Updation Failed.");
					apiLogInfo.setApiResponse("Draft Employee Profile Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Not Found");
				apiLogInfo.setApiResponse("Draft Employee Profile Not Found");
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

//	public ServiceResponse getAllDraftEmployees() {
//		
//		ServiceResponse response = new ServiceResponse();
//		try
//		{
//			List<DraftEmployee> allEmployeeList = draftEmployeeRepository.findAll();
//			
//			if (allEmployeeList != null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(allEmployeeList);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Draft Employee List is null.");
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

	public ServiceResponse getAllDraftEmployees(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllDraftEmployees");
		apiLogInfo.setApiUrl("/api/getAllDraftEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("List fetched of size : "+draftEmployeeRepository.findAll().size());

		try {
			List<Object[]> allEmployeeList = draftEmployeeRepository
					.getAllDraftEmployees(employeedto.getUpdateApplicationStatus());
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
					empDTO.setUpdateApplicationStatus(object[6] != null ? object[6].toString() : null);
					empDTO.setManagerName(object[7] != null ? object[7].toString() : null);
					empDTO.setManagerId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
					empDTO.setDepartmentName(object[9] != null ? object[9].toString() : null);
					empDTO.setIsConsultant(object[10] != null ? object[10].toString() : null);
					empDTO.setIsApprenticeship(object[11] != null ? object[11].toString() : null);
					
					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
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

	public ServiceResponse getDraftEmployeeByEmploymentId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getDraftEmployeeByEmploymentId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId"+employeedto.getEmployeementId());
		EmployeeDTO empDTO = new EmployeeDTO();
		List<EmployeeCertificateDTO> certificationDTOlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> previousEmploymentDTOList = new ArrayList<PreviousEmploymentDTO>();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			List<Object[]> objectList = draftEmployeeRepository
					.getDraftEmployeeByEmployeementId(employeedto.getEmployeementId());

			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					empDTO.setDraftEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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
					empDTO.setUpdateApplicationStatus(object[53] != null ? (object[53].toString()) : null);
					empDTO.setMothersName(object[54] != null ? object[54].toString() : null);
					empDTO.setSpouse(object[55] != null ? object[55].toString() : null);
					empDTO.setChild1(object[56] != null ? object[56].toString() : null);
					empDTO.setChild2(object[57] != null ? object[57].toString() : null);
					empDTO.setChild3(object[58] != null ? object[58].toString() : null);
					empDTO.setReportingManagerName(object[59] != null ? object[59].toString() : null);
					empDTO.setDesignationId(object[60] != null ? Long.parseLong(object[60].toString()) : null);
					empDTO.setDesignationName(object[61] != null ? object[61].toString() : null);
					empDTO.setCreatedOn(object[62] != null ? object[62].toString() : null);
                    empDTO.setProbationPeriod(object[63] != null ? Short.parseShort(object[63].toString()): null);
                    empDTO.setTotalExperience(object[64] !=null ? Float.parseFloat(object[64].toString()): null);
                    empDTO.setDateOfResign(object[65] != null ? format.format(format.parse(object[65].toString())) : null);
                    empDTO.setDateOfRelieving(object[66] != null ? format.format(format.parse(object[66].toString())) : null);
                    empDTO.setIsConsultant(object[67] != null ? object[67].toString() : null);
                    empDTO.setIsApprenticeship(object[68] != null ? object[68].toString() : null);
                    empDTO.setReferedType(object[69] != null ? object[68].toString() : null);
                    empDTO.setReferedName(object[70] != null ? object[68].toString() : null);
                    
//					if (object[42] != null) {
//
//						File actualFile = new File(
//								Paths.get(imageFileLocation + File.separator + object[42].toString()).toString());
//
//						if (actualFile.exists()) {
//							byte[] imageBytes = Files.readAllBytes(
//									Paths.get(imageFileLocation + File.separator + object[42].toString()));
//							empDTO.setImageBytes(imageBytes);
//						}
//
//					}
				}

				List<EmployeeCertificate> certificationsList = employeeCertificateRepository
						.findByEmpIdAndIsDraft(empDTO.getDraftEmpId(), employeedto.getIsDraft());
				List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
						.findByEmpIdAndIsDraft(empDTO.getDraftEmpId(), employeedto.getIsDraft());

				if (!certificationsList.isEmpty()) {

					for (EmployeeCertificate empCert : certificationsList) {
						EmployeeCertificateDTO dto = new EmployeeCertificateDTO();
						
						String dateOfCompletion;
						if(empCert.getDateOfCompletion() != null) {
							dateOfCompletion = empCert.getDateOfCompletion().toString();
						}else {
							dateOfCompletion = null;
						}

						dto.setEmployeeCertificateId(empCert.getEmployeeCertificateId());
						dto.setCertificationName(empCert.getCertificationName());
						dto.setDuration(empCert.getDuration());
						dto.setModeOfCourse(empCert.getModeOfCourse());
						dto.setDateOfCompletion(dateOfCompletion);
						dto.setCertificationNumber(empCert.getCertificationNumber());

						certificationDTOlist.add(dto);
					}
					empDTO.setCertifications(certificationDTOlist);
				}

				if (!previousEmploymentList.isEmpty()) {
					for (PreviousEmployment pervEmploy : previousEmploymentList) {
						PreviousEmploymentDTO dto = new PreviousEmploymentDTO();
						
						String dateOfJoining;
						String dateOfReleiving;
						if(pervEmploy.getDateOfJoining() != null) {
							dateOfJoining = pervEmploy.getDateOfJoining().toString();
						}else {
							dateOfJoining = null;
						}
						if(pervEmploy.getDateOfRelieving() != null) {
							dateOfReleiving = pervEmploy.getDateOfRelieving().toString();
						}else {
							dateOfReleiving = null;
						}

						dto.setPreviousEmploymentId(pervEmploy.getPreviousEmploymentId());
						dto.setEmployerName(pervEmploy.getEmployerName());
						dto.setDateOfJoining(dateOfJoining);
						dto.setDateOfRelieving(dateOfReleiving);
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
				apiLogInfo.setApiResponse("empDto object fetched.");
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
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse rejectDraftEmployeeApplication(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/rejectDraftEmployeeApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId"+employeedto.getEmployeementId()+", draftEmpId : "+employeedto.getDraftEmpId());

		try {

			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());

			if (employeeObject.isPresent()) {
				DraftEmployee draftEmployee = employeeObject.get();

				// change status from 'Pending For Approval' to 'In-progress'
				draftEmployee.setUpdateApplicationStatus(employeedto.getUpdateApplicationStatus());
				draftEmployeeRepository.save(draftEmployee);

				// log rejected message
				Log log = new Log();
				log.setEmpId(employeedto.getUpdatedBy());
				log.setEvent(LogEvents.UPDATE);
				log.setTableName(DbTable.DRAFT_EMPLOYEE);
				log.setTableEntryId(draftEmployee.getDraftEmpId());
				log.setPayload(employeedto.getRemarks());
				logsRepository.save(log);

				// trigger mail to employee
				mailService.sendMail(employeedto.getEmail()+","+hrMailAddress, "Regarding Employee KYC Updation Request Rejection", 
						"Dear"+" "+employeedto.getName()+","
						+"<br>"+" &nbsp;"+" &nbsp;"+"Your profile verification has been rejected, Kindly re-submit your details on iShine Portal. "
								+"<br>"+"<br>"
						+"<b>"+"Rejection reason : "+ employeedto.getRemarks());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Draft Employee Application Rejected");
				apiLogInfo.setApiResponse("Draft Employee Application Rejected");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Application Not Found");
				apiLogInfo.setApiResponse("Draft Employee Application Not Found");
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

	public ServiceResponse revokeDraftEmployeeApplication(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/revokeDraftEmployeeApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId"+employeedto.getEmployeementId()+", draftEmpId : "+employeedto.getDraftEmpId());

		try {

			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());

			if (employeeObject.isPresent()) {
				DraftEmployee draftEmployee = employeeObject.get();

				// change status from 'Pending For Approval' to 'In-progress'
				draftEmployee.setUpdateApplicationStatus(employeedto.getUpdateApplicationStatus());
				draftEmployeeRepository.save(draftEmployee);

				// log revoked message
				Log log = new Log();
				log.setEmpId(employeedto.getUpdatedBy());
				log.setEvent(LogEvents.UPDATE);
				log.setTableName(DbTable.DRAFT_EMPLOYEE);
				log.setTableEntryId(draftEmployee.getDraftEmpId());
				log.setPayload("Application revoked by employee.");
				logsRepository.save(log);

				// trigger mail to employee
				mailService.sendMail(draftEmployee.getEmail(), "Regarding employee profile creation",
						"Your application has been revoked.Kindly resubmit details on IShine portal. <br>");

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Draft employee application revoked");
				apiLogInfo.setApiResponse("Draft employee application revoked");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Application Not Found");
				apiLogInfo.setApiResponse("Draft Employee Application Not Found");
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

	@Transactional
	public ServiceResponse approveDraftEmployeeApplication(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/approveDraftEmployeeApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId"+employeedto.getEmployeementId()+", draftEmpId : "+employeedto.getDraftEmpId());

		try {
			// System.out.println(employeedto);
			Employee employee = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());
			if (employee != null) {
				System.out.println("Employee Object :- " + employee);

				employee.setGender(employeedto.getGender());
				employee.setBloodGroup(employeedto.getBloodGroup());
				employee.setMaritalStatus(employeedto.getMaritalStatus());
				employee.setFatherName(employeedto.getFatherName());
				employee.setPlaceOfBirth(employeedto.getPlaceOfBirth());
				employee.setMotherTongue(employeedto.getMotherTongue());
				employee.setPassportNumber(employeedto.getPassportNumber());
				employee.setAadhar(employeedto.getAadhar());
				employee.setPanNumber(employeedto.getPanNumber());
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
				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
				employee.setSpouse(employeedto.getSpouse());
				employee.setChild1(employeedto.getChild1());
				employee.setChild2(employeedto.getChild2());
				employee.setChild3(employeedto.getChild3());
				employee.setMothersName(employeedto.getMothersName());
				employee.setBillable(employeedto.getBillable());
				employee.setAboutMe(employeedto.getAboutMe());
				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
				employee.setIsConsultant(employeedto.getIsConsultant());
				// Update employee
				Employee updatedEmployee = employeeRepository.save(employee);

				if (updatedEmployee != null) {

					/*
					 * DELETE PREVIOUSLY APPROVED IMAGES,CERTIFICATES AND PREVIOUS_EMPLOYMENT -
					 * START
					 */

					List<EmployeeDocument> approvedDocumentList = employeeDocumentRepository
							.findByEmpId(employee.getEmpId());
					List<EmployeeCertificate> approvedCertificationsList = employeeCertificateRepository
							.findByEmpId(employee.getEmpId());
					List<PreviousEmployment> approvedPreviousEmploymentList = previousEmploymentRepository
							.findByEmpId(employee.getEmpId());

					approvedDocumentList.forEach((list) -> {
						employeeDocumentRepository.deleteById(list.getEmployeeDocumentId());
					});
					approvedCertificationsList.forEach((list) -> {
						employeeCertificateRepository.deleteById(list.getEmployeeCertificateId());
					});
					approvedPreviousEmploymentList.forEach((list) -> {
						previousEmploymentRepository.deleteById(list.getPreviousEmploymentId());
					});

					/*
					 * DELETE PREVIOUSLY APPROVED EXISTING IMAGES,CERTIFICATES AND
					 * PREVIOUS_EMPLOYMENT - END
					 */

					List<EmployeeDocument> documentList = employeeDocumentRepository
							.findByEmpIdAndIsDraft(employeedto.getDraftEmpId(), "true");
					List<EmployeeCertificate> certificationsList = employeeCertificateRepository
							.findByEmpIdAndIsDraft(employeedto.getDraftEmpId(),"true");
					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
							.findByEmpIdAndIsDraft(employeedto.getDraftEmpId(),"true");

					if (documentList != null) {

						List<EmployeeDocument> list = new ArrayList<>();

						File directoryPath = new File(imageFileLocation + File.separator + "Documents" + File.separator
								+ "Draft" + File.separator + employeedto.getEmployeementId());
						String[] contents = directoryPath.list();

//						by priyadarshini
//						List<String> filesInFolder = Arrays.asList(contents);
						List<String> filesInFolder = contents != null ? Arrays.asList(contents) : new ArrayList<>();

						List<String> finalDocFileNames = new ArrayList<String>();

						for (EmployeeDocument document : documentList) {
							document.setIsDraft("false");
							document.setEmpId(employee.getEmpId());
							
							list.add(document);
							finalDocFileNames.add(document.getDocumentName());
						}
						employeeDocumentRepository.saveAll(list);

						
						/* Delete old and junk documents from Employee Document Folder */
						for(String fileName : filesInFolder) {
							if (!finalDocFileNames.contains(fileName)) {
								File junkFile = new File(imageFileLocation + File.separator + "Documents" + File.separator
										+ "Draft" + File.separator + employeedto.getEmployeementId() + File.separator + fileName);
								
								if(junkFile.exists()) {
									if(junkFile.delete()) {
										System.out.println(fileName + " deleted !!");
									}else {
										System.out.println("Failed to delete file !!");
									}
								}
							}
						}
					}

					if (certificationsList != null) {

						List<EmployeeCertificate> list = new ArrayList<>();

						for (EmployeeCertificate certificate : certificationsList) {
							certificate.setIsDraft("false");
							certificate.setEmpId(employee.getEmpId());
							list.add(certificate);
						}
						employeeCertificateRepository.saveAll(list);
					}

					if (previousEmploymentList != null) {

						List<PreviousEmployment> list = new ArrayList<>();

						for (PreviousEmployment previousEmployment : previousEmploymentList) {
							previousEmployment.setIsDraft("false");
							previousEmployment.setEmpId(employee.getEmpId());
							list.add(previousEmployment);
						}
						previousEmploymentRepository.saveAll(list);
					}

					draftEmployeeRepository.deleteById(employeedto.getDraftEmpId());

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee profile approved.");
					apiLogInfo.setApiResponse("Employee profile approved.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
					mailService.sendMail(employeedto.getEmail()+","+hrMailAddress, "Regarding Employee KYC Updation Request Approval", 
							"Dear"+" "+employeedto.getName()+","
							+"<br>"+" &nbsp;"+" &nbsp;"+"Your profile verification has been approved, Thanks for sharing your details with iShine Portal. ");
						
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee profile updation failed.");
					apiLogInfo.setApiResponse("Employee profile updation failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee profile not found.");
				apiLogInfo.setApiResponse("Employee profile not found.");
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
	
	public ServiceResponse updateDraftStatusById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateDraftStatusById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId"+employeedto.getEmployeementId()+", draftEmpId : "+employeedto.getDraftEmpId()+", isDraft : "+employeedto.getIsDraft());

		try {
			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());
			if (employeeObject.isPresent()) {
				DraftEmployee employee = employeeObject.get();

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setUpdatedBy(Integer.parseInt(employeedto.getUpdatedBy().toString()));
				employee.setUpdateApplicationStatus(employeedto.getUpdateApplicationStatus());
				
				DraftEmployee dbResponse = draftEmployeeRepository.save(employee);

				if (dbResponse != null) {
					if (dbResponse.getUpdateApplicationStatus().equals("Pending For Approval")) {
						Employee employeeObj = employeeRepository.findByEmployeementId(employeedto.getEmployeementId());

						if (employeeObj != null) {
							employeeObj.setIsUserInfoUpdated("true");
							employeeRepository.save(employeeObj);
						}
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dbResponse);
					apiLogInfo.setApiResponse("dbResponse");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Profile Updation Submit Failed.");
					apiLogInfo.setApiResponse("Employee Profile Updation Submit Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Not Found");
				apiLogInfo.setApiResponse("Draft Employee Profile Not Found");
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
}
