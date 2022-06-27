package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.DepartmentHolidayMap;
import com.apmosys.employeeportal.repository.DepartmentHolidayMapRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	HolidayRepository holidayRepository;
	
	@Autowired
	DepartmentHolidayMapRepository departmentHolidayMapRepository;

	@Transactional
	public ServiceResponse createDepartment(DepartmentDTO departmentDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		try {
			Department newDepartment = new Department();
			// Employee hod = new Employee();
			newDepartment.setName(departmentDTO.getName());
			// hod.setEmpId(departmentDTO.getHodId());
			newDepartment.setHodId(departmentDTO.getHodId());
			newDepartment.setCreatedBy(departmentDTO.getCreatedBy());

			Department newDepartmentCreated = departmentRepository.save(newDepartment);

			if (newDepartmentCreated != null) {
				
				message = "New Department Created.";				
				
				List<DepartmentHolidayMap> departmentHolidayMapList = new ArrayList<DepartmentHolidayMap>();
				
				holidayRepository.findByCustomHoliday("No").forEach((defaultholiday)->{
					
					DepartmentHolidayMap departmentHolidayMap = new DepartmentHolidayMap();
					departmentHolidayMap.setDeptId(newDepartmentCreated.getDeptId());
					departmentHolidayMap.setHolidayId(defaultholiday.getHolidayId());					
					departmentHolidayMapList.add(departmentHolidayMap);
				});
				
				List<DepartmentHolidayMap> deptHolidayMapped =	departmentHolidayMapRepository.saveAll(departmentHolidayMapList);
				
				if(deptHolidayMapped.isEmpty())
				{
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(message+"Default holidays not mapped to new department.");
				}
				else
				{
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(message+"Default holidays mapped to new department.");
				}				
				
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
					departmentDTO.setHodId(Long.parseLong(object[6].toString()));
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

	/*
	 * By Suraj: 27/06/22
	 * This List will contain custom as well as default holidays mapped to department.
	 */
	public ServiceResponse getAllHolidayListByDeptId(DepartmentDTO departmentDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allHolidayList = departmentHolidayMapRepository.getAllHolidayListByDeptId(departmentDTO.getDeptId());
			
			if (allHolidayList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department wise holiday list is empty.");
			} else {
				List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();
				for (Object[] object : allHolidayList) {
					
					HolidayDTO holidayDTO = new HolidayDTO();
					
					holidayDTO.setDepartmentHolidayMapId(object[0] != null?Long.parseLong(object[0].toString()):null);
					holidayDTO.setDeptId(object[1] != null?Long.parseLong(object[1].toString()):null);
					holidayDTO.setHolidayId(object[2] != null?Short.parseShort(object[2].toString()):null);
					holidayDTO.setOccasion(object[3] != null?object[3].toString():null);
					holidayDTO.setCustomHoliday(object[4] != null?object[4].toString():null);
					
					dtoList.add(holidayDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
