package com.apmosys.employeeportal.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeCertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.PreviousEmploymentDTO;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.EmployeeCertificate;
import com.apmosys.employeeportal.model.PreviousEmployment;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificateRepository;
import com.apmosys.employeeportal.repository.PreviousEmploymentRepository;
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
	
	public ServiceResponse createDraftEmployee(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			DraftEmployee employee = new DraftEmployee();			
			
			employee.setEmployeementId(employeedto.getEmployeementId());
			employee.setName(employeedto.getName());
			employee.setDateOfBirth(employeedto.getDateOfBirth() != null ? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(),"yyyy-MM-dd") : null);
			employee.setDateOfJoining(employeedto.getDateOfJoining() != null ? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(),"yyyy-MM-dd") : null);
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
			employee.setExperience(employeedto.getExperience());
			employee.setRole(employeedto.getRole());
			employee.setWorkLocation(employeedto.getWorkLocation());
		
			
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
						certification.setEmpId(dbResponse.getDraftEmpId());

					});
					employeeService.addCertifications(certificationList, employeedto.getIsDraft());
				}

			});
			
			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Draft Employee Profile Created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Creation Failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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
		EmployeeDTO empDTO = new EmployeeDTO();
		List<EmployeeCertificateDTO> certificationDTOlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> previousEmploymentDTOList = new ArrayList<PreviousEmploymentDTO>();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			List<Object[]> objectList = draftEmployeeRepository.getDraftEmployeeByEmpId(employeedto.getEmpId());
			List<EmployeeCertificate> certificationsList = employeeCertificateRepository
					.findByEmpIdAndIsDraft(employeedto.getEmpId(),employeedto.getIsDraft());
			List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository
					.findByEmpIdAndIsDraft(employeedto.getEmpId(),employeedto.getIsDraft());

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


	public ServiceResponse deleteDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		List<EmployeeCertificate> certificationlist = new ArrayList<EmployeeCertificate>();
		List<PreviousEmployment> previousEmploymentList = new ArrayList<PreviousEmployment>();
		
		try {

			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());
			if (employeeObject.isPresent()) {
				DraftEmployee employeeToBeDeleted = employeeObject.get();				
				
				previousEmploymentList = previousEmploymentRepository.findByEmpId(employeeToBeDeleted.getDraftEmpId());
				certificationlist = employeeCertificateRepository.findByEmpId(employeeToBeDeleted.getDraftEmpId());
				
				if(previousEmploymentList != null && !previousEmploymentList.isEmpty()) {
					previousEmploymentList.forEach((prevEmployer) -> {
						previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
					});
				}
				
				 if(certificationlist != null && !certificationlist.isEmpty()) {
					 certificationlist.forEach((certification) -> {
							employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
					 });
				 }
				 
				draftEmployeeRepository.deleteById(employeeToBeDeleted.getDraftEmpId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Draft Employee Profile Deleted");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Not Found");
			}			

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		List<EmployeeCertificateDTO> newCertificationlist = new ArrayList<EmployeeCertificateDTO>();
		List<PreviousEmploymentDTO> newPreviousEmploymentList = new ArrayList<PreviousEmploymentDTO>();
			
		try
		{
			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				DraftEmployee employee = employeeObject.get();
			
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setEmployeementId(employeedto.getEmployeementId());
				employee.setName(employeedto.getName());
				employee.setDateOfBirth(employeedto.getDateOfBirth() != null ? stringToDateTimeParser.getDate(employeedto.getDateOfBirth(),"yyyy-MM-dd") : null);
				employee.setDateOfJoining(employeedto.getDateOfJoining() != null ? stringToDateTimeParser.getDate(employeedto.getDateOfJoining(),"yyyy-MM-dd") : null);
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
				employee.setExperience(employeedto.getExperience());
				employee.setRole(employeedto.getRole());
				employee.setWorkLocation(employeedto.getWorkLocation());
				
				// Certification
				// Case 1 : Updating Existing certification 
				if(employeedto.getCertifications() != null && !employeedto.getCertifications().isEmpty()) {
					employeedto.getCertifications().stream().filter((certification) -> certification.getEmployeeCertificateId() != null)
					.forEach((certificate) -> {
						
						EmployeeCertificate employeeCertificate = employeeCertificateRepository.findById(certificate.getEmployeeCertificateId()).get();

						
						employeeCertificate.setCertificationName(certificate.getCertificationName());
						employeeCertificate.setCertificationNumber(certificate.getCertificationNumber());
						employeeCertificate.setDateOfCompletion(
								stringToDateTimeParser.getDate(certificate.getDateOfCompletion(), "yyyy-MM-dd"));
						employeeCertificate.setDuration(certificate.getDuration());
						employeeCertificate.setEmployeeCertificateId(certificate.getEmployeeCertificateId());
						employeeCertificate.setModeOfCourse(certificate.getModeOfCourse());
						
						employeeCertificateRepository.save(employeeCertificate);
					});
				}
				
				if(employeedto.getUpdatedCertifications() != null && !employeedto.getUpdatedCertifications().isEmpty()) {
					// Case 2 : Adding New certification
					newCertificationlist = employeedto.getUpdatedCertifications().stream().filter((certification) -> certification.getEmployeeCertificateId() == null).toList();
					if (!newCertificationlist.isEmpty()) {
						newCertificationlist.forEach((certification) -> {
							certification.setEmpId(employeedto.getEmpId());

						});
						employeeService.addCertifications(newCertificationlist, employeedto.getIsDraft());
					}
					
					// Case 3 : Deleting Removed certification
					employeedto.getUpdatedCertifications().stream().filter((certification) -> certification.getEmployeeCertificateId() != null)
					.forEach((certification) -> {
						employeeCertificateRepository.deleteById(certification.getEmployeeCertificateId());
					});
				}
				
				
				// Previous Employer
				if(employeedto.getExperience().equals("Fresher")) {
					List<PreviousEmployment> previousEmploymentList = previousEmploymentRepository.findByEmpId(employeedto.getEmpId());
					
					if(!previousEmploymentList.isEmpty()) {
						previousEmploymentList.stream().filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
						.forEach((prevEmployer) -> {
							previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
						});
					}
				}else {
					if(employeedto.getPreviousEmploymentList() != null && !employeedto.getPreviousEmploymentList().isEmpty()) {
						employeedto.getPreviousEmploymentList().stream().filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
						.forEach((previousEmployeeDTO) -> {
							
							PreviousEmployment previousEmployment = previousEmploymentRepository.findById(previousEmployeeDTO.getPreviousEmploymentId()).get();

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
							previousEmployment.setYearsOfExperience(previousEmployeeDTO.getYearsOfExperience());
							
							previousEmploymentRepository.save(previousEmployment);
						});
					}
					
					if(employeedto.getUpdatedPreviousEmploymentList() != null && !employeedto.getUpdatedPreviousEmploymentList().isEmpty()) {
						newPreviousEmploymentList = employeedto.getUpdatedPreviousEmploymentList().stream().filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() == null).toList();
						if (!newPreviousEmploymentList.isEmpty()) {
							newPreviousEmploymentList.forEach((previousEmployer) -> {
								previousEmployer.setEmpId(employeedto.getEmpId());

							});
							employeeService.addPreviousEmployer(newPreviousEmploymentList, employeedto.getIsDraft());
						}
						
						employeedto.getUpdatedPreviousEmploymentList().stream().filter((prevEmployer) -> prevEmployer.getPreviousEmploymentId() != null)
						.forEach((prevEmployer) -> {
							previousEmploymentRepository.deleteById(prevEmployer.getPreviousEmploymentId());
						});
					}
				}
				
				DraftEmployee dbResponse = draftEmployeeRepository.save(employee);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Draft Employee Profile Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Draft Employee Profile Updation Failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee Profile Not Found");
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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
	
	public ServiceResponse getAllDraftEmployees() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allEmployeeList = draftEmployeeRepository.getAllDraftEmployees();

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


}
