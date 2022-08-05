package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	public ServiceResponse getDraftEmployeeById(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getEmpId());

			if (employeeObject.isPresent()) {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(employeeObject.get());
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
			Optional<DraftEmployee> employeeObject = draftEmployeeRepository.findById(employeedto.getDraftEmpId());
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

	public ServiceResponse getAllDraftEmployees() {
		
		ServiceResponse response = new ServiceResponse();
		try
		{
			List<DraftEmployee> allEmployeeList = draftEmployeeRepository.findAll();
			
			if (allEmployeeList != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(allEmployeeList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Draft Employee List is null.");
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


}
