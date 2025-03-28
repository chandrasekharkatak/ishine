package com.apmosys.employeeportal.controller;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.AppreciationAndRewardsCountDto;
import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationRequest;
import com.apmosys.employeeportal.dto.DateRangeDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeRewardsRequest;
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
		employeeService.clearEmployeeCache();
		return response;
	}

	@RequestMapping(value = "/createEmployeeByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse createEmployeeByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.createEmployeeByList(employee);
			employeeService.clearEmployeeCache();
		}
		return response;
	}
	@RequestMapping(value = "/getEmployeeAppreciationByEmpId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public AppreciationDetails getEmployeeAppreciationByEmpId(@RequestBody EmployeeAppreciationRequest request) {
	    return employeeService.getEmployeeAppreciationByEmpId(request);
	}
	
	@RequestMapping(value = "/getDateRangesForDropdown", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DateRangeDTO>> getDateRangesForDropdown(@RequestBody Long empId) {
		List<DateRangeDTO> dateRanges =  employeeService.getDateRangesForDropdown(empId);
        return ResponseEntity.ok(dateRanges);
    }
	
	@RequestMapping(value = "/getTeamAppreciationByEmpId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public AppreciationDetails getTeamAppreciationByEmpId(@RequestBody EmployeeAppreciationRequest request) {
		return employeeService.getTeamAppreciationByEmpId(request);
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
	
	@RequestMapping(value = "/getAllEmployeesForPerformance", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeesForPerformance() {

		ServiceResponse response = employeeService.getAllEmployeesForPerformance();
		return response;
	}
	
	@RequestMapping(value = "/getAllEmployeesFor360View", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeesFor360View() {

		ServiceResponse response = employeeService.getAllEmployeesFor360View();
		return response;
	}

//	@PostMapping("/getEmployeeByAppreciationName")
//    public ServiceResponse getEmployeeByAppreciationName(@RequestBody AppreciationRequest request) {
//        String appreciationByName = request.getEmpName();
//        ServiceResponse response = employeeService.getEmployeeByAppreciationName(appreciationByName);
//        return response;
//        
//	}
	
	@RequestMapping(value = "/updateEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse updateEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {
		
//		System.out.println("updateEmployeeByEmpId =========================================================================");

		ServiceResponse response = employeeService.updateEmployeeByEmpId(employeedto);
		employeeService.clearEmployeeCache();
		return response;
	}

	@RequestMapping(value = "/updateEmployeeByEmpIdByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse updateEmployeeByEmpIdByList(@RequestBody EmployeeDTO[] employeedto) {

		ServiceResponse response = null;

		for (EmployeeDTO employee : employeedto) {
			response = employeeService.updateEmployeeByEmpIdByList(employee);
			employeeService.clearEmployeeCache();

		}
		return response;
	}

	@RequestMapping(value = "/deleteEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse deleteEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.deleteEmployeeByEmpId(employeedto);
		employeeService.clearEmployeeCache();
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

	@RequestMapping(value = "/getAllEmployeesByDepartmentIds", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesByDepartmentIds(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getAllEmployeesByDepartmentIds(employeedto);
		return response;
	}
	
//	@RequestMapping(value = "/getAllEmployeesByDepartmentId", method = RequestMethod.POST)
//	public ServiceResponse getAllEmployeesByDepartmentId(@RequestBody EmployeeDTO employeedto) {
//
//		ServiceResponse response = employeeService.getAllEmployeesByDepartmentId(employeedto);
//		return response;
//	}

	@RequestMapping(value = "/updateEmployeeForgotPassword", method = RequestMethod.POST)
	public ServiceResponse updateEmployeeForgotPassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeeForgotPassword(employeedto);
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
	
//	 added by anurag
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
	
	@RequestMapping(value ="/updateTimesheetLockCheck" , method = RequestMethod.POST)
	public ServiceResponse updateTimesheetLockCheck(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.updateTimesheetLockCheck(employeeDto);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeBasicInfo", method = RequestMethod.POST)
	public ServiceResponse getEmployeeBasicInfo(@RequestBody EmployeeDTO employeedto) {
		ServiceResponse response= employeeService.getEmployeeBasicInfo(employeedto);
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
	
	/*
	 API for PoPortal
	 */
	
	@RequestMapping(value = "/getAllEmployeeInfo", method = RequestMethod.GET)
	public ServiceResponse employeeInfo() {

		ServiceResponse response = employeeService.getAllEmployeeInfo();
		return response;
	}
	
	@RequestMapping(value = "/updateLeaveBalanceList", method = RequestMethod.POST, consumes = "application/json")	
	public ServiceResponse updateLeaveBalanceList(@RequestBody EmployeeDTO[] employeeDTO) {	
		ServiceResponse response = null;	
		for (EmployeeDTO employeedto : employeeDTO) {	
			response = employeeService.updateLeaveBalanceList(employeedto);	
		}	
		return response;	
	}
	
	/*Audit APIs*/
	
	@RequestMapping(value = "/getEmployeeAuditInfo", method = RequestMethod.POST)
	public ServiceResponse getEmployeeAuditInfo(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getEmployeeAuditInfo(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/unlockAllTimesheet", method = RequestMethod.POST)
	public ServiceResponse unlockAllTimesheet(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.unlockAllTimesheet(employeedto);
		return response;
	}
	
	
//	@RequestMapping(value = "/getAllReporteesByEmpId", method = RequestMethod.POST)
//	public ServiceResponse getAllReporteesByEmpId(@RequestBody EmployeeDTO employeedto) {
//
//		ServiceResponse response = employeeService.getAllReporteesByEmpId(employeedto);
//		return response;
//	}
//	
	@RequestMapping(value = "/getDepartmentByHodId/{empId}", method = RequestMethod.POST)
	public ServiceResponse getDepartmentByHodId(@PathVariable("empId") Long empId) {
		ServiceResponse response = employeeService.getDepartmentByHodId(empId);
		return response;
	}
	
	
//	getTotalNoOfreporties by anurag
	@RequestMapping(value = "/getTotalNoOfreporties/{empId}", method = RequestMethod.POST)
	public ServiceResponse getTotalNoOfreporties(@PathVariable("empId") String empId) {

		System.out.println(" controller call "+empId);
		ServiceResponse response = employeeService.getTotalNoOfreporties(empId);
		return response;
	}
	
	// getProjectsByDepartmentName by anurag below method is only for entire projects by Department not manager wise
	
//	@RequestMapping(value = "/getProjectsByDepartmentName/{departmentName}", method = RequestMethod.POST)
//	public ServiceResponse getProjectsByDepartmentName(@PathVariable("departmentName") String departmentName) {
//
//		System.out.println(" DepartmentName  getProjectsByDepartmentName "+departmentName);
//		ServiceResponse response = employeeService.getProjectsByDepartmentName(departmentName);
//		return response;
//	}
	
	@RequestMapping(value = "/getProjectsByDepartmentName", method = RequestMethod.POST)
	public ServiceResponse getProjectsByDepartmentName(@RequestBody EmployeeDTO employeedto) {

//		System.out.println(" DepartmentName  getProjectsByDepartmentName "+employeedto.getDepartmentName());
		ServiceResponse response = employeeService.getProjectsByDepartmentName(employeedto);
		return response;
	}
	
//	getTeamByProjectName by anurag
	@RequestMapping(value = "/getTeamByProjectName/{projectName}", method = RequestMethod.POST)
	public ServiceResponse getTeamByProjectName(@PathVariable("projectName") String projectName) {

		System.out.println(" projectName  getTeamByProjectName "+projectName);
		ServiceResponse response = employeeService.getTeamByProjectName(projectName);
		return response;
	}
	
//	getTeamMemberByTeamName
//	@RequestMapping(value = "/getTeamMemberByTeamName/{teamName}", method = RequestMethod.POST)
//	public ServiceResponse getTeamMemberByTeamName(@PathVariable("teamName") String teamName) {
//
//		System.out.println(" projectName  getTeamByProjectName "+teamName);
//		ServiceResponse response = employeeService.getTeamMemberByTeamName(teamName);
//		return response;
//	}
	
	@RequestMapping(value = "/getTeamMemberByTeamName", method = RequestMethod.POST)
	public ServiceResponse getTeamMemberByTeamName(@RequestBody EmployeeDTO employeeDto) {

//		System.out.println(" projectName  getTeamByProjectName "+employeeDto.getTeamName());
		ServiceResponse response = employeeService.getTeamMemberByTeamName(employeeDto);
		return response;
	}
	
	
	// getManagerList by anurag
	@RequestMapping(value = "/getManagerList", method = RequestMethod.GET)
	public ServiceResponse getManagerList() {

		System.out.println(" projectName  getManagerList ");
		ServiceResponse response = employeeService.getManagerList();
		return response;
	}
	
//	setManagerToNewManager   this API helps to modify manager mapping by anurag
	@RequestMapping(value = "/setManagerToNewManager", method = RequestMethod.POST)
	public ServiceResponse setManagerToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  setManagerToNewManaager ");
		ServiceResponse response = employeeService.setManagerToNewManager(employeeDto);
		return response;
	}
//	mapLeavesAndCompOffToNewManager
	
	@RequestMapping(value = "/mapLeavesAndCompOffToNewManager", method = RequestMethod.POST)
	public ServiceResponse mapLeavesAndCompOffToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  mapLeavesAndCompOffToNewManager ");
		ServiceResponse response = employeeService.mapLeavesAndCompOffToNewManager(employeeDto);
		return response;
	}
	
	
	
	@RequestMapping(value = "/isEmployeeOnBench", method = RequestMethod.POST)
	public ServiceResponse isEmployeeOnBench(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response = employeeService.isEmployeeOnBench(employeeDto);
		return response;
	}
	
	@RequestMapping(value = "/getReporteesListByManagerId", method = RequestMethod.POST)
	public ServiceResponse getReporteesListByManagerId(@RequestBody EmployeeDTO employeeDto) {

		ServiceResponse response = employeeService.getReporteesListByManagerId(employeeDto);
		return response;
	}
	
	@RequestMapping(value = "/getReporteesListByReportingManagerId", method = RequestMethod.POST)
	public ServiceResponse getReporteesListByReportingManagerId(@RequestBody EmployeeDTO employeeDto) {

		ServiceResponse response = employeeService.getReporteesListByReportingManagerId(employeeDto);
		return response;
	}
	
	@RequestMapping(value = "/removeStaleMappingOfInactiveEmployees", method = RequestMethod.POST)
	public ServiceResponse removeStaleMappingOfInactiveEmployees() {

		ServiceResponse response = employeeService.removeStaleMappingOfInactiveEmployees();
		return response;
	}
	
	@RequestMapping(value = "/setReportingManagerToNewManager", method = RequestMethod.POST)
	public ServiceResponse setReportingManagerToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  setManagerToNewManaager ");
		ServiceResponse response = employeeService.setReportingManagerToNewManager(employeeDto);
		return response;
	}
	
	@PostMapping("/updateDefaultProject")
	public ServiceResponse updateDeafultProject(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeService.updateDefaultProject(employeeDTO.getEmpId(),employeeDTO.getSelectedProjectId());
		
		return response;
	}
	
	@GetMapping("/getExpiredPo")
	public ServiceResponse getEmployeeRewardByEmpId() { 
		
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = employeeService.getExpiredPo();
		return serviceResponse;
	}
	
	@RequestMapping(value = "/getRewardsAndAppreciationCount", method = RequestMethod.POST)
	public ServiceResponse getRewardsAndAppreciationCount(@RequestBody AppreciationAndRewardsCountDto employeeDto) {
		
		ServiceResponse response = employeeService.getRewardsAndAppreciationCount(employeeDto);
		return response;
	}
	
	@PostMapping("/sendExpiredPoEmail")
	public ServiceResponse sendExpiredPoEmail(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeService.updateDefaultProject(employeeDTO.getEmpId(),employeeDTO.getSelectedProjectId());
		
		return response;
	}
	

}
