package com.apmosys.employeeportal.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
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

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;

	@Transactional
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
			employee.setUserTypeId(employeedto.getUserTypeId());

			Employee newEmployee = employeeRepository.save(employee);

			List<LeaveTypeMaster> leaveTypeMasterList = leaveTypeMasterRepository.findAll();

			List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();

			leaveTypeMasterList.forEach((leaveType) -> {

				EmployeeLeavesMap map = new EmployeeLeavesMap();

				map.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
				map.setEmpId(newEmployee.getEmpId());
				map.setBalance((float) 0);
				map.setPendingForApproval((float) 0);
				mapList.add(map);

			});

			List<EmployeeLeavesMap> list = employeeLeavesMapRepository.saveAll(mapList);

			if (list != null) {
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
			List<Object[]> objectList = employeeRepository.getEmployeeByEmpId(employeedto.getEmpId());

			if (objectList != null) {

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
					empDTO.setGraduation(object[18] != null ? object[18].toString() : null);
					empDTO.setHobbies(object[19] != null ? object[19].toString() : null);
					empDTO.setLandline(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					empDTO.setMaritalStatus(object[21] != null ? object[21].toString() : null);
					empDTO.setMobileNo(object[22] != null ? Long.parseLong(object[22].toString()) : null);
					empDTO.setMotherTongue(object[23] != null ? object[23].toString() : null);
					empDTO.setName(object[24] != null ? object[24].toString() : null);
					empDTO.setNoticePeriod(object[25] != null ? Short.parseShort(object[25].toString()) : null);
					empDTO.setOfficialMobileNo(object[26] != null ? Long.parseLong(object[26].toString()) : null);
					empDTO.setPanNumber(object[27] != null ? object[27].toString() : null);
					empDTO.setPassportNumber(object[28] != null ? object[28].toString() : null);
					empDTO.setPermanentAddress(object[29] != null ? object[29].toString() : null);
					empDTO.setPfAccountNumber(object[30] != null ? object[30].toString() : null);
					empDTO.setPincode(object[31] != null ? Integer.parseInt(object[31].toString()) : null);
					empDTO.setPlaceOfBirth(object[32] != null ? object[32].toString() : null);
					empDTO.setPostGraduation(object[33] != null ? object[33].toString() : null);
					empDTO.setPreviousPfAccountNumber(object[34] != null ? object[34].toString() : null);
					empDTO.setRelation(object[35] != null ? object[35].toString() : null);
					empDTO.setState(object[36] != null ? object[36].toString() : null);
					empDTO.setUan(object[37] != null ? object[37].toString() : null);
					empDTO.setViewsOnOrganisation(object[38] != null ? object[38].toString() : null);
					empDTO.setYearOfGrad(object[39] != null ? Short.parseShort(object[39].toString()) : null);
					empDTO.setYearOfPostGrad(object[40] != null ? Short.parseShort(object[40].toString()) : null);
					empDTO.setEmergencyContactPerson(object[41] != null ? object[41].toString() : null);
					empDTO.setManagerName(object[43] != null ? object[43].toString() : null);
					empDTO.setJobRoleName(object[44] != null ? object[44].toString() : null);
					empDTO.setDepartmentName(object[45] != null ? object[45].toString() : null);

					if (object[42] != null) {
						byte[] imageBytes = Files
								.readAllBytes(Paths.get(imageFileLocation + File.separator + object[42].toString()));
						empDTO.setImageBytes(imageBytes);
					}
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

		try {
			Optional<Employee> employeeObject = employeeRepository.findById(employeedto.getEmpId());
			if (employeeObject.isPresent()) {
				Employee employee = employeeObject.get();

				employee.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				employee.setName(employeedto.getName());
				employee.setDateOfBirth(employeedto.getDateOfBirth() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfBirth())
						: null);
				employee.setDateOfJoining(employeedto.getDateOfJoining() != null
						? stringToDateTimeParser.getDate(employeedto.getDateOfJoining())
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
				employee.setUserTypeId(employeedto.getUserTypeId());

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

	public ServiceResponse getAllEmployees() {

		ServiceResponse response = new ServiceResponse();
		try {
			List<Employee> allEmployeeList = employeeRepository.findAll();

			if (allEmployeeList != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(allEmployeeList);
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

}
