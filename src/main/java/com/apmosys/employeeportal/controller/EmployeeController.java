package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@RequestMapping(value = "/createEmployee", method = RequestMethod.POST)
	public ServiceResponse createEmployee(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.createEmployee(employeedto);
		return response;
	}

	@RequestMapping(value = "/createEmployeeByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse createEmployeeByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.createEmployeeByList(employee);
		}
		return response;
	}

	@RequestMapping(value = "/getEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse getEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getEmployeeByEmpId(employeedto);
		return response;
	}

	@RequestMapping(value = "/getAllEmployees", method = RequestMethod.GET)
	public ServiceResponse getAllEmployees() {

		ServiceResponse response = employeeService.getAllEmployees();
		return response;
	}

	@RequestMapping(value = "/updateEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse updateEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeeByEmpId(employeedto);
		return response;
	}

	@RequestMapping(value = "/updateEmployeeByEmpIdByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse updateEmployeeByEmpIdByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.updateEmployeeByEmpIdByList(employee);
		}
		return response;
	}

	@RequestMapping(value = "/deleteEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse deleteEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.deleteEmployeeByEmpId(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/changeManagerMapping", method = RequestMethod.POST)
	public ServiceResponse changeManagerMapping(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.changeManagerMapping(employeedto);
		return response;
	}

	@RequestMapping(value = "/previewImage", method = RequestMethod.POST)
	public ServiceResponse previewImage(HttpServletRequest request, @RequestParam("image") MultipartFile image) {

		ServiceResponse serviceResponse = employeeService.previewImage(image);
		return serviceResponse;
	}

	@RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
	public ServiceResponse uploadImage(HttpServletRequest request, @RequestParam("image") MultipartFile image,
			@RequestParam("uploadedBy") Long uploadedBy) {

		ServiceResponse serviceResponse = employeeService.uploadImage(image, uploadedBy);
		return serviceResponse;
	}

	@RequestMapping(value = "/updateEmployeeProfileByEmpId", method = RequestMethod.POST)
	public ServiceResponse updateEmployeeProfileByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeeProfileByEmpId(employeedto);
		return response;
	}

	@RequestMapping(value = "/getAllEmployeesByRole", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesByRole(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getAllEmployeesByRole(employeedto);
		return response;
	}

//	@RequestMapping(value = "/getAllEmployeesByDepartmentIds", method = RequestMethod.POST)
//	public ServiceResponse getAllEmployeesByDepartmentIds(@RequestBody EmployeeDTO employeedto) {
//
//		ServiceResponse response = employeeService.getAllEmployeesByDepartmentIds(employeedto);
//		return response;
//	}
	
	@RequestMapping(value = "/getAllEmployeesByDepartmentId", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesByDepartmentId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getAllEmployeesByDepartmentId(employeedto);
		return response;
	}

	@RequestMapping(value = "/updateEmployeePassword", method = RequestMethod.POST)
	public ServiceResponse updatePassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeePassword(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeeOldPassword", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeOldPassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeOldPassword(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeeEmail", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeEmail(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeEmail(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeementId", method = RequestMethod.POST)
	public ServiceResponse checkEmployeementId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeementId(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeeMobileNo", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeMobileNo(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeMobileNo(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeeAadharNumber", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeAadharNumber(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeAadharNumber(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkEmployeePanNumber", method = RequestMethod.POST)
	public ServiceResponse checkEmployeePanNumber(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeePanNumber(employeedto);
		return response;
	}

	@RequestMapping(value = "/getAllEmployeesBirthDayToday", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeesBirthDayToday() {

		ServiceResponse response = employeeService.getAllEmployeesBirthDayToday();
		return response;
	}

	@RequestMapping(value = "/getHierarchyByEmpId", method = RequestMethod.POST)
	public ServiceResponse getHierarchyByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getHierarchyByEmpId(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/getHierarchyChartByEmpId", method = RequestMethod.POST)
	public ServiceResponse getHierarchyChartByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getHierarchyChartByEmpId(employeedto);
		return response;
	}

	@RequestMapping(value = "/revokeAccount", method = RequestMethod.POST)
	public ServiceResponse revokeAccount(@RequestBody EmployeeDTO employeedto) {
		ServiceResponse response = employeeService.revokeAccount(employeedto);
		return response;
	}

	@RequestMapping(value = "/empdetails", method = RequestMethod.GET)
	public ServiceResponse getEmployees() {
		ServiceResponse response = employeeService.getEmployees();
		return response;

	}
	

	/*
	 * getAllManagers gets all employees whose empId occurs in manager_id column in
	 * employee table. Not to be confused with employee whose designation is MANAGER
	 * persona
	 */
	@RequestMapping(value = "/getAllManagers", method = RequestMethod.GET)
	public ServiceResponse getAllManagers() {
		ServiceResponse response = employeeService.getAllManagers();
		return response;

	}
	
	@RequestMapping(value ="/findEmployeeWorkingHistory" , method = RequestMethod.POST)
	public ServiceResponse findEmployeeWorkingHistory(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.findEmployeeWorkingHistory(employeeDto);
		return response;
	}
	
	@RequestMapping(value ="/getEmployeeProfileCompletion" , method = RequestMethod.POST)
	public ServiceResponse getEmployeeProfileCompletion(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.getEmployeeProfileCompletion(employeeDto);
		return response;
	}

	/*
	 Old Employee Portal Password Encryption - part of data migration.
	 */
	
	@RequestMapping(value = "/encryptPassword", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse encryptPassword(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.encryptPassword(employee);
		}
		return response;
	}
	
	/*
	 send Mail to employee - part of data migration.
	 */
	
	@RequestMapping(value = "/sendMailByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse sendMailByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.sendMailByList(employee);
		}
		return response;
	}
	
	/*
	 Save demographics info using postal code
	 */
	
	@RequestMapping(value = "/addDemographicsInfo", method = RequestMethod.POST)
	public ServiceResponse addDemographicsInfo(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.addDemographicsInfo(employeedto);
		return response;
	}

}
