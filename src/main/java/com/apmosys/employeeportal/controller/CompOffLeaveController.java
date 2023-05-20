package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.CompOffLeaveService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class CompOffLeaveController {
	
	@Autowired
	CompOffLeaveService compOffLeaveService;
	
	@RequestMapping(value = "/getAllCompOffReasons" ,method = RequestMethod.GET)
	public ServiceResponse getAllCompOffReasons() {
		
		ServiceResponse response = compOffLeaveService.getAllCompOffReasons();
		return response;
	}
	
	@RequestMapping(value = "/applyForCompOff" ,method = RequestMethod.POST)
	public ServiceResponse applyForCompOff(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.applyForCompOff(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateCompOffById" ,method = RequestMethod.POST)
	public ServiceResponse updateCompOffById(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.updateCompOffById(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getPendingCompOffRequestsByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse getPendingCompOffRequestsByManagerId (@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.getPendingCompOffRequestsByManagerId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/countPendingCompOffRequestsByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse countPendingCompOffRequestsByManagerId (@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.countPendingCompOffRequestsByManagerId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllCompOffRequestsByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllCompOffRequestsByEmpId (@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.getAllCompOffRequestsByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateCompOff" ,method = RequestMethod.POST)
	public ServiceResponse updateCompOff(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.updateCompOff(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/deleteCompOff" ,method = RequestMethod.POST)
	public ServiceResponse deleteCompOff(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.deleteCompOff(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getCompOffBalanceDetailsByEmpIdAndFromDate" ,method = RequestMethod.POST)
	public ServiceResponse getCompOffBalanceDetailsByEmpIdAndFromDate (@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
		return response;
	}
	
	/* CompOff reconsilation */
	
	@RequestMapping(value = "/getCompOffBalanceMigratedFromOldLeavePortal" ,method = RequestMethod.GET)
	public ServiceResponse getCompOffBalanceMigratedFromOldLeavePortal() {
		
		ServiceResponse response = compOffLeaveService.getCompOffBalanceMigratedFromOldLeavePortal();
		return response;
	}

	@RequestMapping(value = "/setCompOffStatusAndLeaveId" ,method = RequestMethod.GET)
	public ServiceResponse setCompOffStatusAndLeaveId() {
		
		ServiceResponse response = compOffLeaveService.setCompOffStatusAndLeaveId();
		return response;
	}
	
	@RequestMapping(value = "/convertSingleCompOffApplicationToken" ,method = RequestMethod.GET)
	public ServiceResponse convertSingleCompOffApplicationToken() {
		
		ServiceResponse response = compOffLeaveService.convertSingleCompOffApplicationToken();
		return response;
	}
	
	@RequestMapping(value = "/lapseAndReconcileCompOffBalance" ,method = RequestMethod.GET)
	public ServiceResponse lapseAndReconcileCompOffBalance() {
		
		ServiceResponse response = compOffLeaveService.lapseAndReconcileCompOffBalance();
		return response;
	}

}
