package com.apmosys.employeeportal.service;

import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.Employee;

/**
 * Sends comp-off notification emails (apply, approval, rejection) with retries.
 * {@link MailService#sendMailWithCC} swallows exceptions and returns false, so a false result is
 * converted to {@link IllegalStateException} to drive Spring Retry.
 */
@Component
public class CompOffNotificationMailSender {

	private static final Logger log = LogManager.getLogger(CompOffNotificationMailSender.class);

	private static final int MAX_MAIL_ATTEMPTS = 3;
	private static final long BACKOFF_MS = 2000L;

	@Autowired
	private MailService mailService;

	@Value("${hr.mail}")
	private String hrMailAddress;

	@Retryable(
			value = IllegalStateException.class,
			maxAttempts = MAX_MAIL_ATTEMPTS,
			backoff = @Backoff(delay = BACKOFF_MS),
			recover = "recoverApprovedNotification",
			listeners = { "compOffMailRetryListener" })
	public void sendApprovedNotification(LeaveDTO leaveDTO, Employee findHOD, Employee updatedBy,
			Employee findRequestor) {
		Objects.requireNonNull(leaveDTO, "leaveDTO");
		Objects.requireNonNull(findHOD, "findHOD");
		Objects.requireNonNull(updatedBy, "updatedBy");
		Objects.requireNonNull(findRequestor, "findRequestor");

		boolean sent;
		try {
			sent = mailService.sendMailWithCC(leaveDTO.getEmail(),
					findHOD.getEmail() + "," + leaveDTO.getManagerEmail() + "," + hrMailAddress,
					"Regarding Compensatory off Request Approval", buildApprovedBody(leaveDTO, updatedBy, findRequestor));
		} catch (MessagingException e) {
			log.warn(
					"Comp-off approval mail threw checked exception (will retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId(), e);
			throw new IllegalStateException("Comp-off approval notification mail failed", e);
		}
		if (!sent) {
			log.warn(
					"Comp-off approval mail send returned false (retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId());
			throw new IllegalStateException("Comp-off approval notification mail failed");
		}
	}

	@Retryable(
			value = IllegalStateException.class,
			maxAttempts = MAX_MAIL_ATTEMPTS,
			backoff = @Backoff(delay = BACKOFF_MS),
			recover = "recoverRejectedNotification",
			listeners = { "compOffMailRetryListener" })
	public void sendRejectedNotification(LeaveDTO leaveDTO, Employee findHOD, Employee updatedBy,
			Employee findRequestor) {
		Objects.requireNonNull(leaveDTO, "leaveDTO");
		Objects.requireNonNull(findHOD, "findHOD");
		Objects.requireNonNull(updatedBy, "updatedBy");
		Objects.requireNonNull(findRequestor, "findRequestor");

		boolean sent;
		try {
			sent = mailService.sendMailWithCC(leaveDTO.getEmail(),
					findHOD.getEmail() + "," + leaveDTO.getManagerEmail() + "," + hrMailAddress,
					"Regarding Compensatory off Request Rejection", buildRejectedBody(leaveDTO, updatedBy, findRequestor));
		} catch (MessagingException e) {
			log.warn(
					"Comp-off rejection mail threw checked exception (will retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId(), e);
			throw new IllegalStateException("Comp-off rejection notification mail failed", e);
		}
		if (!sent) {
			log.warn(
					"Comp-off rejection mail send returned false (retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId());
			throw new IllegalStateException("Comp-off rejection notification mail failed");
		}
	}

	@Retryable(
			value = IllegalStateException.class,
			maxAttempts = MAX_MAIL_ATTEMPTS,
			backoff = @Backoff(delay = BACKOFF_MS),
			recover = "recoverAppliedNotification",
			listeners = { "compOffMailRetryListener" })
	public void sendAppliedNotification(LeaveDTO leaveDTO, String compOffReasonText) {
		Objects.requireNonNull(leaveDTO, "leaveDTO");

		boolean sent;
		try {
			sent = mailService.sendMailWithCC(leaveDTO.getEmail(),
					leaveDTO.getHodEmail() + "," + leaveDTO.getManagerEmail() + "," + hrMailAddress,
					"Regarding Comp-Off Request", buildAppliedBody(leaveDTO, compOffReasonText));
		} catch (MessagingException e) {
			log.warn(
					"Comp-off apply mail threw checked exception (will retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId(), e);
			throw new IllegalStateException("Comp-off apply notification mail failed", e);
		}
		if (!sent) {
			log.warn(
					"Comp-off apply mail send returned false (retry if attempts remain) empId={} compOffLeaveId={}",
					leaveDTO.getEmpId(), leaveDTO.getCompOffLeaveId());
			throw new IllegalStateException("Comp-off apply notification mail failed");
		}
	}

	@Recover
	public void recoverAppliedNotification(IllegalStateException ex, LeaveDTO leaveDTO, String compOffReasonText) {
		log.error(
				"Comp-off apply mail exhausted retries (operation=apply empId={} compOffLeaveId={})",
				leaveDTO != null ? leaveDTO.getEmpId() : null,
				leaveDTO != null ? leaveDTO.getCompOffLeaveId() : null, ex);
	}

	@Recover
	public void recoverApprovedNotification(IllegalStateException ex, LeaveDTO leaveDTO, Employee findHOD,
			Employee updatedBy, Employee findRequestor) {
		log.error(
				"Comp-off approval mail exhausted retries (operation=approve empId={} compOffLeaveId={})",
				leaveDTO != null ? leaveDTO.getEmpId() : null,
				leaveDTO != null ? leaveDTO.getCompOffLeaveId() : null, ex);
	}

	@Recover
	public void recoverRejectedNotification(IllegalStateException ex, LeaveDTO leaveDTO, Employee findHOD,
			Employee updatedBy, Employee findRequestor) {
		log.error(
				"Comp-off rejection mail exhausted retries (operation=reject empId={} compOffLeaveId={})",
				leaveDTO != null ? leaveDTO.getEmpId() : null,
				leaveDTO != null ? leaveDTO.getCompOffLeaveId() : null, ex);
	}

	private static String buildApprovedBody(LeaveDTO leaveDTO, Employee updatedBy, Employee findRequestor) {
		return "Dear " + findRequestor.getName() + "," + "<br> " + " &nbsp;" + " &nbsp;" + " "
				+ "Your Compensatory off application has been approved by " + updatedBy.getName() + "." + "<br>" + "<br>"
				+ "<b>" + "Comp-Off Details :" + "<b>" + "<br>" + "EmpID :" + "A- " + leaveDTO.getEmployeementId()
				+ "<br>" + "Name :" + " " + findRequestor.getName() + "<br>" + " Date " + " " + leaveDTO.getFromDate()
				+ "<br>" + "No. Of Days :" + " 1 " + "day(s)" + "<br>" + "Comp-off Description :" + " "
				+ leaveDTO.getDescription() + "." + "<br>" + "Comp-off reason :" + " " + leaveDTO.getCompOffReasons()
				+ ".";
	}

	private static String buildRejectedBody(LeaveDTO leaveDTO, Employee updatedBy, Employee findRequestor) {
		return "Dear " + findRequestor.getName() + "," + "<br> " + " &nbsp;" + " &nbsp;" + " "
				+ "Your Compensatory off application has been rejected by " + updatedBy.getName() + "." + "<br>" + "<br>"
				+ "<b>" + "Comp-Off Details :" + "<b>" + "<br>" + "EmpID :" + "A- " + leaveDTO.getEmployeementId()
				+ "<br>" + "Name :" + " " + findRequestor.getName() + "<br>" + " Date " + " " + leaveDTO.getFromDate()
				+ "<br>" + "No. Of Days :" + " 1 " + "day(s)" + "<br>" + "Comp-off Description :" + " "
				+ leaveDTO.getDescription() + "." + "<br>" + "Comp-off reason :" + " " + leaveDTO.getCompOffReasons()
				+ ".";
	}

	private static String buildAppliedBody(LeaveDTO leaveDTO, String compOffReasonText) {
		String reason = compOffReasonText == null ? "null" : compOffReasonText;
		return "Dear " + leaveDTO.getHodName() + "," + "<br> " + " &nbsp;" + " &nbsp;" + " "
				+ "Comp-Off Request has been applied by " + leaveDTO.getEmployeeName() + " for 1" + " day(s)"
				+ ", Please take necessary action." + "<br>" + "<br>" + "<b>" + "Comp-Off Details :" + "<b>" + "<br>"
				+ "EmpID :" + "A- " + leaveDTO.getEmployeementId() + "<br>" + "Name :" + " " + leaveDTO.getEmployeeName()
				+ "<br>" + " Date " + " " + leaveDTO.getFromDate() + "<br>" + "No. Of Days :" + " 1 " + "day(s)"
				+ "<br>" + "comp-Off Description :" + " " + leaveDTO.getDescription() + "." + "<br>" + "comp-Off Reason : "
				+ reason;
	}
}
