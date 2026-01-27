package com.apmosys.employeeportal.controller;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Encrypted;
import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.AppreciationAndRewardsCountDto;
import com.apmosys.employeeportal.dto.AppreciationDetails;
import com.apmosys.employeeportal.dto.AppreciationRequest;
import com.apmosys.employeeportal.dto.CertificateDTO;
import com.apmosys.employeeportal.dto.DateRangeDTO;
import com.apmosys.employeeportal.dto.DefaultProjectEmployeeConfig;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeAppreciationRequest;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ExpiredPOMailSendDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.SearchEmpPayloadDTO;
import com.apmosys.employeeportal.dto.SkillCertConfigDTO;
import com.apmosys.employeeportal.request.EmployeeTimesheetProjectRequest;
import com.apmosys.employeeportal.response.EmployeeTimesheetProjectResponse;
import com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse;
import com.apmosys.employeeportal.dto.EmployeeRewardsRequest;
import com.apmosys.employeeportal.dto.EmployeeSkillProficiencyDTO;
import com.apmosys.employeeportal.dto.ExpiredPOMailSendDTO;
import com.apmosys.employeeportal.dto.HrHodHrViewPerformance;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@Autowired
	PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/createEmployee", method = RequestMethod.POST)
	public ServiceResponse createEmployee(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.createEmployee(employeedto);
		employeeService.clearEmployeeCache();
		return response;
	}
	@GetMapping("/example/{empId}")
	public List<Object> example(@PathVariable Long empId) {
		List<Object> response = employeeService.example(empId);
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
	@Encrypted
	@RequestMapping(value = "/getEmployeeAppreciationByEmpId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public AppreciationDetails getEmployeeAppreciationByEmpId(@RequestBody EmployeeAppreciationRequest request) {
	    return employeeService.getEmployeeAppreciationByEmpId(request);
	}
	
	@RequestMapping(value = "/getDateRangesForDropdown", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DateRangeDTO>> getDateRangesForDropdown(@RequestBody Long empId) {
		List<DateRangeDTO> dateRanges =  employeeService.getDateRangesForDropdown(empId);
        return ResponseEntity.ok(dateRanges);
    }
	@Encrypted
	@RequestMapping(value = "/getTeamAppreciationByEmpId", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public AppreciationDetails getTeamAppreciationByEmpId(@RequestBody EmployeeAppreciationRequest request) {
		return employeeService.getTeamAppreciationByEmpId(request);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,60,61,59,8,37,38,9,55})
	@RequestMapping(value = "/getEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse getEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getEmployeeByEmpId(employeedto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {3,25,27,52})
	@RequestMapping(value = "/getAllEmployees", method = RequestMethod.GET)
	public ServiceResponse getAllEmployees() {

		ServiceResponse response = employeeService.getAllEmployees();
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {52})
	@RequestMapping(value = "/getAllEmployeesForPerformance", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesForPerformance(@RequestBody HrHodHrViewPerformance hrHodHrViewPerformance) {

		ServiceResponse response = employeeService.getAllEmployeesForPerformance(hrHodHrViewPerformance);
		return response;
	}
	@JobRoleAccess(featureIds = {3,4,5,6,7,13,14,16,23,24,25,26,28,29,31,36,39,41,43,44,46,52,53,63})
	@RequestMapping(value = "/getAllEmployeesFor360View/{empId}", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeesFor360View(@PathVariable("empId") Long empId) {
		ServiceResponse response = employeeService.getAllEmployeesFor360View(empId);
		return response;
	}

//	@PostMapping("/getEmployeeByAppreciationName")
//    public ServiceResponse getEmployeeByAppreciationName(@RequestBody AppreciationRequest request) {
//        String appreciationByName = request.getEmpName();
//        ServiceResponse response = employeeService.getEmployeeByAppreciationName(appreciationByName);
//        return response;
//        
//	}
	@JobRoleAccess(featureIds = {3})
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
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/deleteEmployeeByEmpId", method = RequestMethod.POST)
	public ServiceResponse deleteEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.deleteEmployeeByEmpId(employeedto);
		employeeService.clearEmployeeCache();
		return response;
	}
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/changeManagerMapping", method = RequestMethod.POST)
	public ServiceResponse changeManagerMapping(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.changeManagerMapping(employeedto);
		return response;
	}
	
	@JobRoleAccess(featureIds = {3,8})
	@RequestMapping(value = "/previewImage", method = RequestMethod.POST)
	public ServiceResponse previewImage(HttpServletRequest request, @RequestParam("image") MultipartFile image) {

		ServiceResponse serviceResponse = employeeService.previewImage(image);
		return serviceResponse;
	}

	@JobRoleAccess(featureIds = {3,8})
	@RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
	public ServiceResponse uploadImage(HttpServletRequest request, @RequestParam("image") MultipartFile image,
			@RequestParam("uploadedBy") Long uploadedBy) {

		ServiceResponse serviceResponse = employeeService.uploadImage(image, uploadedBy);
		return serviceResponse;
	}
	@JobRoleAccess(featureIds = {8})
	@RequestMapping(value = "/updateEmployeeProfileByEmpId", method = RequestMethod.POST)
	public ServiceResponse updateEmployeeProfileByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeeProfileByEmpId(employeedto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34,33,4})
	@RequestMapping(value = "/getAllEmployeesByRole", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesByRole(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getAllEmployeesByRole(employeedto);
		return response;
	}

	@JobRoleAccess(featureIds = {7,34})
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
	
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value = "/updateEmployeePassword", method = RequestMethod.POST)
	public ServiceResponse updatePassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.updateEmployeePassword(employeedto);
		return response;
	}

	@JobRoleAccess(featureIds = {24})
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
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/checkEmployeementId", method = RequestMethod.POST)
	public ServiceResponse checkEmployeementId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeementId(employeedto);
		return response;
	}

	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/checkEmployeeMobileNo", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeMobileNo(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeMobileNo(employeedto);
		return response;
	}

	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/checkEmployeeAadharNumber", method = RequestMethod.POST)
	public ServiceResponse checkEmployeeAadharNumber(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeeAadharNumber(employeedto);
		return response;
	}

	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/checkEmployeePanNumber", method = RequestMethod.POST)
	public ServiceResponse checkEmployeePanNumber(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.checkEmployeePanNumber(employeedto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value = "/getAllEmployeesBirthDayToday", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeesBirthDayToday() {

		ServiceResponse response = employeeService.getAllEmployeesBirthDayToday();
		return response;
	}

	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value = "/getHierarchyByEmpId", method = RequestMethod.POST)
	public ServiceResponse getHierarchyByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getHierarchyByEmpId(employeedto);
		return response;
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value = "/getHierarchyChartByEmpId", method = RequestMethod.POST)
	public ServiceResponse getHierarchyChartByEmpId(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getHierarchyChartByEmpId(employeedto);
		return response;
	}
	@JobRoleAccess(featureIds = {3,7})
	@RequestMapping(value = "/revokeAccount", method = RequestMethod.POST)
	public ServiceResponse revokeAccount(@RequestBody EmployeeDTO employeedto) {
		ServiceResponse response = employeeService.revokeAccount(employeedto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {27})
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
	@Encrypted
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value = "/getAllManagers", method = RequestMethod.GET)
	public ServiceResponse getAllManagers() {
		ServiceResponse response = employeeService.getAllManagers();
		return response;

	}
	
//	 added by anurag
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value ="/findEmployeeWorkingHistory" , method = RequestMethod.POST)
	public ServiceResponse findEmployeeWorkingHistory(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.findEmployeeWorkingHistory(employeeDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value ="/getEmployeeProfileCompletion" , method = RequestMethod.POST)
	public ServiceResponse getEmployeeProfileCompletion(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.getEmployeeProfileCompletion(employeeDto);
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {3,14})
	@RequestMapping(value ="/updateTimesheetLockCheck" , method = RequestMethod.POST)
	public ServiceResponse updateTimesheetLockCheck(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response=employeeService.updateTimesheetLockCheck(employeeDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {15,16})
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
	public ServiceResponse addDemographicsInfo(@RequestBody Map<String, Object> payload) {
		
		ServiceResponse response = employeeService.addDemographicsInfo(payload);
		return response;
	}
	
	/*
	 API for PoPortal
	 */

	
	@GetMapping(value = "/getAllEmployeeInfo")
	public ServiceResponse employeeInfo(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return employeeService.getAllEmployeeInfo();
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
	@JobRoleAccess(featureIds = {3,13})
	@RequestMapping(value = "/getEmployeeAuditInfo", method = RequestMethod.POST)
	public ServiceResponse getEmployeeAuditInfo(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = employeeService.getEmployeeAuditInfo(employeedto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,14})
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
	@JobRoleAccess(featureIds = {3})
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
	@Encrypted
	@JobRoleAccess(featureIds = {3})
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
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/getTeamMemberByTeamName", method = RequestMethod.POST)
	public ServiceResponse getTeamMemberByTeamName(@RequestBody EmployeeDTO employeeDto) {

//		System.out.println(" projectName  getTeamByProjectName "+employeeDto.getTeamName());
		ServiceResponse response = employeeService.getTeamMemberByTeamName(employeeDto);
		return response;
	}
	
	
	// getManagerList by anurag
	@Encrypted
	@JobRoleAccess(featureIds = {3,4,5,6,7,13,14,16,23,24,25,26,28,29,31,36,39,41,43,44,46,52,53,63})
	@RequestMapping(value = "/getManagerList", method = RequestMethod.GET)
	public ServiceResponse getManagerList() {

		System.out.println(" projectName  getManagerList ");
		ServiceResponse response = employeeService.getManagerList();
		return response;
	}
	
//	setManagerToNewManager   this API helps to modify manager mapping by anurag
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/setManagerToNewManager", method = RequestMethod.POST)
	public ServiceResponse setManagerToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  setManagerToNewManaager ");
		ServiceResponse response = employeeService.setManagerToNewManager(employeeDto);
		return response;
	}
//	mapLeavesAndCompOffToNewManager
	@Encrypted
	@RequestMapping(value = "/mapLeavesAndCompOffToNewManager", method = RequestMethod.POST)
	public ServiceResponse mapLeavesAndCompOffToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  mapLeavesAndCompOffToNewManager ");
		ServiceResponse response = employeeService.mapLeavesAndCompOffToNewManager(employeeDto);
		return response;
	}
	
	
	@Encrypted
	@JobRoleAccess(featureIds = {24})
	@RequestMapping(value = "/isEmployeeOnBench", method = RequestMethod.POST)
	public ServiceResponse isEmployeeOnBench(@RequestBody EmployeeDTO employeeDto) {
		ServiceResponse response = employeeService.isEmployeeOnBench(employeeDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/getReporteesListByManagerId", method = RequestMethod.POST)
	public ServiceResponse getReporteesListByManagerId(@RequestBody EmployeeDTO employeeDto) {

		ServiceResponse response = employeeService.getReporteesListByManagerId(employeeDto);
		return response;
	}
	
	
	@Encrypted
	@JobRoleAccess(featureIds = {3})
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
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/setReportingManagerToNewManager", method = RequestMethod.POST)
	public ServiceResponse setReportingManagerToNewManager(@RequestBody EmployeeDTO employeeDto) {

		System.out.println(" projectName  setManagerToNewManaager ");
		ServiceResponse response = employeeService.setReportingManagerToNewManager(employeeDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@PostMapping("/updateDefaultProject")
	public ServiceResponse updateDeafultProject(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeService.updateDefaultProject(employeeDTO.getEmpId(),employeeDTO.getSelectedProjectId(),employeeDTO.getUpdatedBy());
		
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {24})
	@GetMapping("/getExpiredPo")
	public ServiceResponse getEmployeeRewardByEmpId() { 
		
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse = employeeService.getExpiredPo();
		return serviceResponse;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,4,5,6,7,13,14,16,23,24,25,26,28,29,31,36,39,41,43,44,46,52,53,63})
	@RequestMapping(value = "/getRewardsAndAppreciationCount", method = RequestMethod.POST)
	public ServiceResponse getRewardsAndAppreciationCount(@RequestBody AppreciationAndRewardsCountDto employeeDto) {
		
		ServiceResponse response = employeeService.getRewardsAndAppreciationCount(employeeDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {24})
	@PostMapping("/sendExpiredPoEmail")
	public ServiceResponse sendExpiredPoEmail(@RequestBody ExpiredPOMailSendDTO employeeDTO) {
		ServiceResponse response = employeeService.sendExpiredPoEmail(employeeDTO);
		
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,24})
	@GetMapping("/getAllEmployeesWorkAnniversaryToday")
	public ServiceResponse getAllEmployeesWorkAnniversaryToday() {
	    return employeeService.getAllEmployeesWorkAnniversaryToday();
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@PostMapping("/getProjectsAccToDepartmentAndProjectType")
	public ServiceResponse getInternalProjectsAccToDepartmentSelected(@RequestBody DefaultProjectEmployeeConfig defaultProjectEmployeeConfig) {
		return employeeService.getInternalProjectsAccToDepartmentSelected(defaultProjectEmployeeConfig);
	}
	@Encrypted
	@PostMapping("/getAllEmployeesBasedOnUserLogined")
	public ServiceResponse getAllEmployeesBasedOnUserLogined(@RequestBody List<DepartmentDTO> department) {
	    return employeeService.getAllEmployeesBasedOnUserLogined(department);
	}

	@JobRoleAccess(featureIds = {3})
	@PostMapping("/extendEmployeeProbation")
	public ServiceResponse extendEmployeeProbation(@RequestBody EmployeeDTO employee) {
	    return employeeService.extendEmployeeProbation(employee);
	}

	@JobRoleAccess(featureIds = {3})
	@PostMapping("/confirmEmployeeFromProbation")
	public ServiceResponse confirmEmployeeFromProbation(@RequestBody EmployeeDTO employee) {
	    return employeeService.confirmEmployeeFromProbation(employee);
	}

	@JobRoleAccess(featureIds = {3})
	@PostMapping("/submitReasonForDelay")
	public ServiceResponse submitForDelay(@RequestBody EmployeeDTO employee)
	{
		return employeeService.submitForDelay(employee);
	}

	@JobRoleAccess(featureIds = {3})
	@PostMapping("/reduceEmployeeExtension")
	public ServiceResponse reduceEmployeeProbation(@RequestBody EmployeeDTO employee)
	{
		return employeeService.reduceEmployeeProbation(employee);
	}
	
	 @PostMapping("/getEmployeeAndTimesheetDetails")
	 public ServiceResponse getEmployeeAndTimesheetDetails(HttpServletRequest request,@RequestBody EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		ServiceResponse response = new ServiceResponse();
		response = employeeService.getEmployeeAndTimesheetDetails(employeeTimesheetRequest);
		return response;
	 }

	 @PostMapping("/getTeamAndTimeSheetDetails")
	 public ServiceResponse getTeamAndTimeSheetDetails(HttpServletRequest request,@RequestBody Long id) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		return employeeService.getTeamAndTimeSheetDetails(id);
	 }
	 
	 @PostMapping("/getProjectDetailsByEmpIdAndDateRange")
	 public ServiceResponse getProjectDetailsByEmpIdAndDateRange(HttpServletRequest request,@RequestBody EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		 return employeeService.getProjectDetailsByEmpIdAndDateRange(employeeTimesheetRequest);
	 }
	 
	
	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/fetchInactivePOCounts",method=RequestMethod.POST)
	public ServiceResponse fetchInactivePOCounts(@RequestBody GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    return employeeService.fetchInactivePOCounts(employeeDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/fetchInactivePOListOfEmployee",method=RequestMethod.POST)  
	public ServiceResponse fetchInactivePOListOfEmployee(@RequestBody GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    return employeeService.fetchInactivePOListOfEmployee(employeeDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/fetchactivePOCounts",method=RequestMethod.POST)
	public ServiceResponse fetchActivePOCounts(@RequestBody GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    return employeeService.fetchActivePOCounts(employeeDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/fetchActivePOListOfEmployee",method=RequestMethod.POST)  
	public ServiceResponse fetchActivePOListOfEmployee(@RequestBody GetEmployeeProjectReportPayloadDTO employeeDTO) {
	    return employeeService.fetchActivePOListOfEmployee(employeeDTO);
	}
	
	

	@JobRoleAccess(featureIds = {3,7})
	@PutMapping("/revoke")
	public ServiceResponse revokeConfirmantion(@RequestBody EmployeeDTO employeeDto)
	{
		return employeeService.revokeConfirmation(employeeDto);
	}
	
	@JobRoleAccess(featureIds = {3,7,24})
	@PostMapping("/probation-reminders")
	public ServiceResponse getProbationRemindersForHod(@RequestBody EmployeeDTO employeeDto) {
	    return employeeService.getEmployeesNearingProbationEnd(employeeDto);
	}
	
	
	
	@JobRoleAccess(featureIds = {8})
	@GetMapping("/getAllProficiency")
	public ServiceResponse getAllProficiency() {
		return employeeService.getAllProficiency();
	}
	
	@JobRoleAccess(featureIds = {34,8})
	@GetMapping("/getAllPredefinedSkills")
		public ServiceResponse getAllPredefinedSkills() {
			return employeeService.getAllPredefinedSkills();
		}
	
	@JobRoleAccess(featureIds = {8})
	@PostMapping("/addSkillOfEmployee")
	public ServiceResponse addSkill(@RequestBody EmployeeSkillProficiencyDTO employeeSkillProficiencyDTO) {
		return employeeService.addSkill(employeeSkillProficiencyDTO);
	}
	
	@JobRoleAccess(featureIds = {8})
	@PostMapping("/updateSkillOfEmployee")
	public ServiceResponse updateSkill(@RequestBody EmployeeSkillProficiencyDTO employeeSkillProficiencyDTO) {
		return employeeService.updateSkill(employeeSkillProficiencyDTO);
	}
	
	@JobRoleAccess(featureIds = {3,8})
	@PostMapping("/getAllSkillsByEmpId")
	public ServiceResponse getAllSkillsByEmpId(@RequestBody EmployeeSkillProficiencyDTO employeeSkillProficiencyDTO) {
		return employeeService.getAllSkillsByEmpId(employeeSkillProficiencyDTO);
	}
	
	@JobRoleAccess(featureIds = {3,8})
	@PostMapping("/getAllCertificatesByEmpId")
	public ServiceResponse getAllCertificatesByEmpId(@RequestBody CertificateDTO certificateDTO) {
		return employeeService.getAllCertificatesByEmpId(certificateDTO);
	}
	

	@JobRoleAccess(featureIds = {8})
	@PostMapping("/deleteSkillsOfEmployee")
	public ServiceResponse deleteSkillsOfEmployee(@RequestBody EmployeeSkillProficiencyDTO employeeSkillProficiencyDTO) {
		return employeeService.deleteSkillsOfEmployee(employeeSkillProficiencyDTO);
	}
	
	@JobRoleAccess(featureIds = {8})
	@PostMapping("/addCertificate")
	public ServiceResponse addTimesheetWithClient(@RequestPart("dto") CertificateDTO dto,
			@RequestPart(value = "doc1",required = false) MultipartFile doc1) throws Exception 

	{
		
		return employeeService.addCertificate(dto,doc1);
	}
	@JobRoleAccess(featureIds = {8})
	@PostMapping("/duplicateCertificateCheck")
	public ServiceResponse duplicateCertificateCheckForEmployee(@RequestBody CertificateDTO dto) {
		
		return employeeService.duplicateCertificateCheckForEmployee(dto);
		
	}
	

	@JobRoleAccess(featureIds = {3,8})
	@GetMapping("/downloadCertificate/{docId}")
	public ServiceResponse downloadCertificate(@PathVariable Long docId) {
	    return employeeService.downloadCertificate(docId);
	}
	

	@JobRoleAccess(featureIds = {8})
	@PostMapping("/deleteCertificate")
	public ServiceResponse deleteCertificateOfEmployee(@RequestBody CertificateDTO certificateDTO)
	{
		return employeeService.deleteCertificateOfEmployee(certificateDTO);
	}
	
	@Scheduled(cron = "0 0 14 * * ?")
	public void updateCertificateStatusesAfterNoon() {
		
	     employeeService.updateCertificateStatuses();
	}
	 
	
	@Scheduled(cron = "0 59 23 * * ?") 
	public void updateCertificateStatusesNight() {
	    employeeService.updateCertificateStatuses();
	}
	 
	 
	 
	 
	 
	@JobRoleAccess(featureIds = {64})
	@PostMapping("/uploadSkillBulk")
		public ServiceResponse bulkSkillCertficate(@RequestPart("dto") SkillCertConfigDTO dto,
				@RequestPart(value = "doc1",required = false) MultipartFile doc1) throws Exception {
	    	ServiceResponse response = new ServiceResponse();
	        if (doc1.isEmpty()) {
	            response.setServiceResponse("Please upload a file.");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        response = employeeService.bulkSkillCertficateallTotal(dto,doc1);
	        return response;
	    }
	    
		@JobRoleAccess(featureIds = {64})
	    @PostMapping("/uploadCertificateBulk")
		public ServiceResponse uploadCertificateBulk(@RequestPart("dto") SkillCertConfigDTO dto,
				@RequestPart(value = "doc1",required = false) MultipartFile doc1) throws Exception {
	    	ServiceResponse response = new ServiceResponse();
	        if (doc1.isEmpty()) {
	            response.setServiceResponse("Please upload a file.");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return response;
	        }

	        response = employeeService.uploadCertificateBulkallTotal(dto,doc1);
	        return response;
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
		@JobRoleAccess(featureIds = {34})
	    @PostMapping("/searchEmployeesBySkillsAndCertificates")
	    public ServiceResponse searchEmployeesBySkillsAndCertificates(@RequestBody SearchEmpPayloadDTO payload) {
	    	
	        return employeeService.searchEmployeesBySkillsAndCertificates(payload);
	        
	    }
	    
	    @PostMapping("/searchEmployeesNotInSearch")
       public ServiceResponse searchEmployeesNotInSearch(@RequestBody SearchEmpPayloadDTO payload) {
	    	
	        return employeeService.searchEmployeesNotInSearch(payload);
	        
	    }
	    
	    
//		@Encrypted
	    @JobRoleAccess(featureIds = {3})
	    @RequestMapping(value = "/getPendingTimesheetProjects", method = RequestMethod.POST)
	    public ServiceResponse getPendingTimesheetProjects(@RequestBody Map<String, Object> request) {

	        Long empId = Long.valueOf(request.get("empId").toString());
	        LocalDate relievingDate = null;

	        if (request.containsKey("relievingDate") && request.get("relievingDate") != null) {
	            relievingDate = LocalDate.parse(request.get("relievingDate").toString());
	        }

	        ServiceResponse response = employeeService.getPendingTimesheetProjects(empId, relievingDate);
	        return response;
	    }
	    
	    
	    
	    
	@RequestMapping(value = "/getAllEmployeesByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllEmployeesByProjectId( @RequestParam("projectId") Integer projectId) {
		ServiceResponse serviceResponse = employeeService.getAllEmployeesByProjectId(projectId);
		return serviceResponse;
	}
	
	@PostMapping("/api/getPoRequirementDataByTeamAndPoId")
	public ServiceResponse getPoRequirementDataByTeamAndPoId(@RequestBody Map<String, Long> requestBody) {
	    Long teamId = requestBody.get("teamId");
	    Long poId = requestBody.get("poId");
	    return employeeService.getPoRequirementDataByTeamAndPoId(teamId, poId);
	}
	
	// @Encrypted
	@GetMapping("/getAllActiveEmployeeInformation")
	public ServiceResponse getAllActiveEmployeeInformation() {
		return employeeService.getAllActiveEmployeeInformation();
	}

}
