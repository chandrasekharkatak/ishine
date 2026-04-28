package com.apmosys.employeeportal.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.service.EmployeeMonthlySnapshotCronService;

import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled execution for {@code employee_monthly_snapshot} maintenance.
 * Cron expressions are overridable via {@code snapshot.cron.live-refresh} and {@code snapshot.cron.close-month}.
 */
@Slf4j
@Component
public class EmployeeMonthlySnapshotScheduler {

	@Autowired
	private EmployeeMonthlySnapshotCronService employeeMonthlySnapshotCronService;

	/** Default: every day at 00:05 (12:05 AM). */
	@Scheduled(cron = "${snapshot.cron.live-refresh:0 5 0 * * *}")
	public void scheduledLiveRefresh() {
		try {
			employeeMonthlySnapshotCronService.runLiveRefreshJob();
		} catch (Exception ex) {
			log.error("Scheduled employee_monthly_snapshot live refresh failed", ex);
		}
	}

	/** Default: 1st of month at 00:30 (12:30 AM). */
	@Scheduled(cron = "${snapshot.cron.close-month:0 30 0 1 * *}")
	public void scheduledCloseMonth() {
		try {
			employeeMonthlySnapshotCronService.runCloseMonthJob();
		} catch (Exception ex) {
			log.error("Scheduled employee_monthly_snapshot close-month failed", ex);
		}
	}
}
