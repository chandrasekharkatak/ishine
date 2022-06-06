package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	public ServiceResponse createDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Department newDepartment = new Department();
			// Employee hod = new Employee();
			newDepartment.setName(departmentDTO.getName());
			// hod.setEmpId(departmentDTO.getHodId());
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());

			Department dbResponse = departmentRepository.save(newDepartment);

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New Department Created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Department Creation Failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllDepartments() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allDepartmentList = departmentRepository.getAllDepartments();
			if (allDepartmentList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department List is Empty.");
			} else {
				List<DepartmentDTO> dtoList = new ArrayList<DepartmentDTO>();
				for (Object[] object : allDepartmentList) {
					DepartmentDTO departmentDTO = new DepartmentDTO();
					departmentDTO.setDeptId(Long.parseLong(object[0].toString()));
					departmentDTO.setCreatedBy(Integer.parseInt(object[1].toString()));
					departmentDTO.setCreatedOn(object[2].toString());
					departmentDTO.setName(object[3].toString());
					departmentDTO.setCreatedByName(object[4].toString());
					departmentDTO.setHodName(object[5].toString());
					dtoList.add(departmentDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isPresent()) {
				Department departmentToBeUpdated = departmentObject.get();
				// Employee hod = new Employee();
				// hod.setEmpId(departmentDTO.getHodId());
				departmentToBeUpdated.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				departmentToBeUpdated.setName(departmentDTO.getName());
				departmentToBeUpdated.setHodId(departmentDTO.getHodId());

				Department dbResponse = departmentRepository.save(departmentToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Department Updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department Updation Failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse deleteDepartment(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<Department> departmentObject = departmentRepository.findById(departmentDTO.getDeptId());
			if (departmentObject.isPresent()) {
				Department departmentToBeDeleted = departmentObject.get();
				departmentRepository.deleteById(departmentToBeDeleted.getDeptId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Department Deleted.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department Not Found.");
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
