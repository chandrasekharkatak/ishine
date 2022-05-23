package com.apmosys.employeeportal.service;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.DraftEmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeService {

	@Autowired
	EmployeeRepository employeeRepository;	
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Value("${default.password}")
	String defaultPaswword;
	
	@Value("${file.location.image}")
	private String imageFileLocation;

	public ServiceResponse createEmployee(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Employee employee = new Employee();
			
			employee.setName(employeedto.getName());
			employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth()));
			employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining()));
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
			employee.setOfficialMobileNo(employeedto.getOfficialMobileNo());
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
			employee.setGraduation(employeedto.getGraduation());
			employee.setYearOfGrad(employeedto.getYearOfGrad());
			employee.setPostGraduation(employeedto.getPostGraduation());
			employee.setYearOfPostGrad(employeedto.getYearOfPostGrad());
			employee.setHobbies(employeedto.getHobbies());
			employee.setAboutMe("Add about yourself.");
			employee.setViewsOnOrganisation("Add your views.");
			employee.setJobRoleId(employeedto.getJobRoleId());
			employee.setPassword(defaultPaswword);
			
			Employee dbResponse = employeeRepository.save(employee);
			
			if (dbResponse != null) {
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

	public ServiceResponse getEmployeeByEmpId(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		EmployeeDTO empDTO = new EmployeeDTO();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());

			if (employeeObject.isPresent()) {
				Employee emp = employeeObject.get();
				
				byte[] imageBytes = Files
						.readAllBytes(Paths.get(imageFileLocation + File.separator + emp.getProfileImageName()));
				
				empDTO.setName(emp.getName());
				empDTO.setDateOfBirth(format.format(format.parse(emp.getDateOfBirth().toString())));
				empDTO.setDateOfJoining(format.format(format.parse(emp.getDateOfJoining().toString())));
				empDTO.setManagerId(emp.getManagerId());
				empDTO.setEmail(emp.getEmail());
				empDTO.setGender(emp.getGender());
				empDTO.setBloodGroup(emp.getBloodGroup());
				empDTO.setMaritalStatus(emp.getMaritalStatus());
				empDTO.setFatherName(emp.getFatherName());
				empDTO.setPlaceOfBirth(emp.getPlaceOfBirth());
				empDTO.setMotherTongue(emp.getMotherTongue());
				empDTO.setPassportNumber(emp.getPassportNumber());
				empDTO.setAadhar(emp.getAadhar());
				empDTO.setPanNumber(emp.getPanNumber());
				empDTO.setMobileNo(emp.getMobileNo());
				empDTO.setLandline(emp.getLandline());
				empDTO.setAddress(emp.getAddress());
				empDTO.setCity(emp.getCity());
				empDTO.setState(emp.getState());
				empDTO.setCountry(emp.getCountry());
				empDTO.setPincode(emp.getPincode());
				empDTO.setOfficialMobileNo(emp.getOfficialMobileNo());
				empDTO.setPermanentAddress(emp.getPermanentAddress());
				empDTO.setEmergencyContactPerson(emp.getEmergencyContactPerson());
				empDTO.setRelation(emp.getRelation());
				empDTO.setEmergencyContactMobile(emp.getEmergencyContactMobile());
				empDTO.setNoticePeriod(emp.getNoticePeriod());
				empDTO.setEmploymentstatus(emp.getEmploymentstatus());
				empDTO.setBankName(emp.getBankName());
				empDTO.setBankAccountNo(emp.getBankAccountNo());
				empDTO.setBankIFSCCode(emp.getBankIFSCCode());
				empDTO.setPfAccountNumber(emp.getPfAccountNumber());
				empDTO.setPreviousPfAccountNumber(emp.getPreviousPfAccountNumber());
				empDTO.setUan(emp.getUan());
				empDTO.setEsicNumber(emp.getEsicNumber());
				empDTO.setGraduation(emp.getGraduation());
				empDTO.setYearOfGrad(emp.getYearOfGrad());
				empDTO.setPostGraduation(emp.getPostGraduation());
				empDTO.setYearOfPostGrad(emp.getYearOfPostGrad());
				empDTO.setHobbies(emp.getHobbies());
				empDTO.setAboutMe(emp.getAboutMe());
				empDTO.setViewsOnOrganisation(emp.getViewsOnOrganisation());
				empDTO.setJobRoleId(emp.getJobRoleId());
				empDTO.setImageBytes(imageBytes);
				
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
				employeeRepository.deleteById(employeeToBeDeleted.getEmpId());				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee Profile Deleted");
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
		
			
		try
		{
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employee = employeeObject.get();
			
				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setName(employeedto.getName());
				employee.setDateOfBirth(stringToDateTimeParser.getDate(employeedto.getDateOfBirth()));
				employee.setDateOfJoining(stringToDateTimeParser.getDate(employeedto.getDateOfJoining()));
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
				employee.setOfficialMobileNo(employeedto.getOfficialMobileNo());
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
				employee.setGraduation(employeedto.getGraduation());
				employee.setYearOfGrad(employeedto.getYearOfGrad());
				employee.setPostGraduation(employeedto.getPostGraduation());
				employee.setYearOfPostGrad(employeedto.getYearOfPostGrad());
				employee.setHobbies(employeedto.getHobbies());
				employee.setAboutMe(employeedto.getAboutMe());
				employee.setViewsOnOrganisation(employeedto.getViewsOnOrganisation());
				employee.setJobRoleId(employeedto.getJobRoleId());
				
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

	public ServiceResponse getAllEmployees() {
		
		ServiceResponse response = new ServiceResponse();
		try
		{
			List<Employee> allEmployeeList = employeeRepository.findAll();
			
			if (allEmployeeList != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(allEmployeeList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee List is null.");
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
			
			System.out.println("uploadedBy : "+ uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);
			
			if(employeeObject.isPresent()) {
				System.out.println("Employee : "+ employeeObject);
				Employee employeeObj = employeeObject.get();
				
				String extension = FilenameUtils.getExtension(image.getOriginalFilename());
				String newFileName = employeeObj.getName().replaceAll("\\s", "").toLowerCase() + "." + extension;

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
					if(savedFile.delete()) {
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
					}else {
						response.setServiceResponse("Failed To Delete Existing Profile Image !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				}
				
			}else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
}
