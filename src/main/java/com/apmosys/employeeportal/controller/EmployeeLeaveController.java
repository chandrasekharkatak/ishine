package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.EmployeeLeaveService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeLeaveController {
	
	@Autowired
	EmployeeLeaveService employeeLeaveService;
	
	
	
	@RequestMapping(value = "/applyLeave" ,method = RequestMethod.POST)
	public ServiceResponse applyLeave(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.applyLeave(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/deletePendingLeave" ,method = RequestMethod.POST)
	public ServiceResponse deletePendingLeave(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.deletePendingLeave(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/updatePendingLeave" ,method = RequestMethod.POST)
	public ServiceResponse updatePendingLeave(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updatePendingLeave(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateLeaveStatus" ,method = RequestMethod.POST)
	public ServiceResponse updateLeaveStatus(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updateLeaveStatus(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyLeaveApplicationsByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyLeaveApplicationsByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyLeaveApplicationsByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyTeamsPendingLeaveApplicationsByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamsPendingLeaveApplicationsByManagerId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/countAllMyTeamsPendingLeaveApplicationsByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse countAllMyTeamsPendingLeaveApplicationsByManagerId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getMyLeaveBalancesByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getMyLeaveBalancesByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getMyLeaveBalancesByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeLeaveApplicationwithHolidays" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeLeaveApplicationwithHolidays(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getEmployeeLeaveApplicationwithHolidays(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getOverlappedTeamMemberLeave" ,method = RequestMethod.POST)
	public ServiceResponse getOverlappedTeamMemberLeave(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getOverlappedTeamMemberLeave(leaveDTO);
		return response;
	}
	
	/*		
	 *	Data migration - updateleavebalance, oldleaveApplication 		
	 */		
			
	@RequestMapping(value="/updateLeaveBalanceByEmployeementId" , method = RequestMethod.POST, consumes="application/json")		
	public ServiceResponse updateLeaveBalanceByEmployeementId(@RequestBody LeaveDTO[] leaveDTO) {			
				
		ServiceResponse response = null;		
				
		 for (LeaveDTO leave: leaveDTO) {		
			 response = employeeLeaveService.updateLeaveBalanceByEmployeementId(leave);		
		    }			
		return response;		
	}		
			
	@RequestMapping(value="/addOldLeaveApplicationByList" , method = RequestMethod.POST, consumes="application/json")		
	public ServiceResponse addOldLeaveApplicationByList(@RequestBody LeaveDTO[] leaveDTO) {			
				
		ServiceResponse response = null;		
				
		 for (LeaveDTO leave: leaveDTO) {		
			 response = employeeLeaveService.addOldLeaveApplicationByList(leave);		
		    }			
		return response;		
	}
	
	/*
	 * To update leave bucket of an employee eg: Add Compoff , monthly leave updation ,etc.
	 */
	@RequestMapping(value = "/updateLeavesByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse updateLeavesByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updateLeavesByEmpId(leaveDTO);
		return response;
	}
	
	
	
	@RequestMapping(value = "/getLeaveLogsByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getLeaveLogsByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getLeaveLogsByEmpId(leaveDTO);
		return response;
	}
	
	/*
	 *	to get Approved Past Leave Applications to check lock-in limit in days 
	 */
	@RequestMapping(value = "/getAppliedLeaveApplicationsByEmpIdAndDateRange" ,method = RequestMethod.POST)
	public ServiceResponse getAppliedLeaveApplicationsByEmpIdAndDateRange(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAppliedLeaveApplicationsByEmpIdAndDateRange(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/countMyPendingLeaveApplicationsByLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse countMyPendingLeaveApplicationsByLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.countMyPendingLeaveApplicationsByLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/countMyRejectedLeaveApplicationsByLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse countMyRejectedLeaveApplicationsByLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/countMyApprovedLeaveApplicationsByLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse countMyApprovedLeaveApplicationsByLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveDTO);
		return response;
	}
	
	// To Get All Applications Applied by Me for Team Members
	@RequestMapping(value = "/getAllMyTeamApplicationsByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamApplicationsByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyTeamApplicationsByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/bulkApproveLeaveRequest", method = RequestMethod.POST)
	public ServiceResponse bulkApproveLeaveRequest(@RequestBody LeaveDTO leaveDTO) {

		ServiceResponse response = employeeLeaveService.bulkApproveLeaveRequest(leaveDTO);
		return response;
	}

	@RequestMapping(value = "/bulkRejectLeaveRequest", method = RequestMethod.POST)
	public ServiceResponse bulkRejectLeaveRequest(@RequestBody LeaveDTO leaveDTO) {

		ServiceResponse response = employeeLeaveService.bulkRejectLeaveRequest(leaveDTO);
		return response;
	}
	
	/*
	  revoke leave Application methods start --
	  */
	
	@RequestMapping(value="/revokeApprovedLeaveApplication" , method = RequestMethod.POST)
	public ServiceResponse revokeApprovedLeaveApplication(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	employeeLeaveService.revokeApprovedLeaveApplication(leaveDTO);		
		return response;
	}
	
	@RequestMapping(value="/getRevokeLeaveApplicationByEmpId" , method = RequestMethod.POST)
	public ServiceResponse getRevokeLeaveApplicationByEmpId(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	employeeLeaveService.getRevokeLeaveApplicationByEmpId(leaveDTO);		
		return response;
	}
	
	@RequestMapping(value="/getAllMyTeamLeaveRevokeApplicationsByEmpId" , method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamLeaveRevokeApplicationsByEmpId(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	employeeLeaveService.getAllMyTeamLeaveRevokeApplicationsByEmpId(leaveDTO);		
		return response;
	}
	
	@RequestMapping(value="/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId" , method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	employeeLeaveService.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(leaveDTO);		
		return response;
	}
	
	@RequestMapping(value="/updateRevokeLeaveStatus" , method = RequestMethod.POST)
	public ServiceResponse updateRevokeLeaveStatus(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	employeeLeaveService.updateRevokeLeaveStatus(leaveDTO);		
		return response;
	}
	
	@RequestMapping(value = "/getAllLeaveBalanceByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllLeaveBalanceByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllLeaveBalanceByEmpId(leaveDTO);
		return response;
	}
	
	
	/* Employee Leave Balance reconciliation */
	@RequestMapping(value = "/setEmployeeLeaveEntitlement" ,method = RequestMethod.POST)
	public ServiceResponse setEmployeeLeaveEntitlement(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.setEmployeeLeaveEntitlement(leaveDTO);
		return response;
	}
	
	/* Timesheet reconsilation */
	@RequestMapping(value = "/fillTimesheetForOldLeaves" ,method = RequestMethod.GET)
	public ServiceResponse fillTimesheetForOldLeaves() {
		
		ServiceResponse response = employeeLeaveService.fillTimesheetForOldLeaves();
		return response;
	}
	
	@RequestMapping(value = "/pendingForApprovalReconsilation" ,method = RequestMethod.GET)
	public ServiceResponse pendingForApprovalReconsilation() {
		
		ServiceResponse response = employeeLeaveService.pendingForApprovalReconsilation();
		return response;
	}
	
	@RequestMapping(value = "/reconsileCasualBalance" ,method = RequestMethod.GET)
	public ServiceResponse reconsileCasualBalance() {
		
		ServiceResponse response = employeeLeaveService.reconsileCasualBalance();
		return response;
	}

}
