package com.apmosys.employeeportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Releases reimbursement tickets held after the monthly submission deadline once their
 * processing cycle month begins (from the 1st onward).
 * <p>
 * Runs automatically — no monthly backend restart and no manual API call required.
 * The target month is already stored on each ticket ({@code processing_cycle_year_month}).
 */
@Component
public class ReimbursementHeldTicketReleaseScheduler {

	private static final Logger log = LoggerFactory.getLogger(ReimbursementHeldTicketReleaseScheduler.class);

	@Autowired
	private ReimbursementTicketService reimbursementTicketService;

	/** Catch-up after deploy or if the server was down at 00:10 on the 1st. */
	@EventListener(ApplicationReadyEvent.class)
	public void releaseDueHeldTicketsOnStartup() {
		int released = reimbursementTicketService.releaseDueHeldTickets();
		if (released > 0) {
			log.info("Reimbursement held-ticket release on startup: released {} ticket(s).", released);
		}
	}

	/** Daily at 00:10 Asia/Kolkata — releases held tickets whose cycle month has opened. */
	@Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Kolkata")
	public void releaseDueHeldTicketsScheduled() {
		int released = reimbursementTicketService.releaseDueHeldTickets();
		if (released > 0) {
			log.info("Reimbursement held-ticket scheduled release: released {} ticket(s).", released);
		}
	}
}
