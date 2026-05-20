package com.apmosys.employeeportal.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.service.EmployeeMonthlySnapshotCronService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Manual or scripted triggers for snapshot cron SQL (same logic as {@link com.apmosys.employeeportal.cron.EmployeeMonthlySnapshotScheduler}).
 * Requires an authenticated HR session with feature access (same as Repeated Offender dashboard).
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/employee-monthly-snapshot/cron")
public class EmployeeMonthlySnapshotCronController {

	@Autowired
	private EmployeeMonthlySnapshotCronService employeeMonthlySnapshotCronService;

	@JobRoleAccess(featureIds = { 15, 16 })
	@PostMapping("/live-refresh")
	public ServiceResponse runLiveRefresh() {
		ServiceResponse response = new ServiceResponse();
		try {
			Map<String, Integer> counts = employeeMonthlySnapshotCronService.runLiveRefreshJob();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(counts);
		} catch (Exception e) {
			log.error("employee-monthly-snapshot cron live-refresh failed", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
		}
		return response;
	}

	@JobRoleAccess(featureIds = { 15, 16 })
	@PostMapping("/close-month")
	public ServiceResponse runCloseMonth() {
		ServiceResponse response = new ServiceResponse();
		try {
			Map<String, Integer> counts = employeeMonthlySnapshotCronService.runCloseMonthJob();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(counts);
		} catch (Exception e) {
			log.error("employee-monthly-snapshot cron close-month failed", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
		}
		return response;
	}
}
