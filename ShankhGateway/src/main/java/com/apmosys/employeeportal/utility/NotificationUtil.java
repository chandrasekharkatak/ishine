package com.apmosys.employeeportal.utility;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.service.MailService;

@Component
public class NotificationUtil {
	
	
	 @Autowired
	 private MailService mailService;
	 
	 @Autowired
	 EmployeeRepository employeeRepository;
	 
		private static final Logger logger = LoggerFactory.getLogger(NotificationUtil.class);
	 
	 

		
		public void sendDraftUpdateNotification(DraftEmployee savedDraft) {
		    try {
		        logger.info("Attempting to send email notification for employee: {}", savedDraft.getName());
		        Map<String, String> emailMap = new HashMap<>();
		        emailMap.put("Employee", savedDraft.getEmail());
//		        String managerEmail = savedDraft.getReportingManagerId();
//		        emailMap.put("Manager", managerEmail);
		        emailMap.put("HR", "hr@apmosys.com");
		        String subject = "Employee Information Updated (Draft): " + savedDraft.getName();
		        LocalDateTime combinedDateTime=savedDraft.getUpdatedOn();
		        
//		        SimpleDateFormat  formatter= new SimpleDateFormat("yyyy/MM/dd");
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		        String updatedOn=combinedDateTime.format(formatter);
		        Integer emp_id=savedDraft.getUpdatedBy();
		        Long updatedBy_email=emp_id.longValue();
		        String updatedBy=employeeRepository.findEmailByEmpId(updatedBy_email);
		        
		        String body = "<html><body>"
		                + "<p>Dear Team,</p>"
		                + "<p>The  information for the following employee has been updated in ishine.</p>"
		                + "<table border='1' style='border-collapse: collapse; padding: 8px;'>"
		                + "<tr><th align='left'>Employee Name</th><td>" + savedDraft.getName() + "</td></tr>"
		                + "<tr><th align='left'>Employee ID</th><td>" + savedDraft.getEmployeementId() + "</td></tr>"
		                + "<tr><th align='left'>Email</th><td>" + savedDraft.getEmail() + "</td></tr>"
		                + "<tr><th align='left'>updated by</th><td>" + updatedBy + "</td></tr>"
		                + "<tr><th align='left'>Employmentstatus</th><td>" + savedDraft.getEmploymentstatus() + "</td></tr>"
		                + "<tr><th align='left'>Updated On</th><td>" + updatedOn + "</td></tr>"
		                + "</table>"
		                + "<p>Regards,<br>ApMoSys Technologies</p>"
		                + "</body></html>";
		        String employeeEmail = emailMap.get("Employee");
//		        String managerEmail = emailMap.get("Manager");
		        String adminEmail = emailMap.get("HR");
		        if (employeeEmail != null && adminEmail != null) {
		            mailService.sendMail(adminEmail, subject, body);
		            logger.info("Email notification sent successfully for employee draft update: {}", savedDraft.getName());
		        } else {
		            logger.warn("Could not send email because recipient address was null. Employee: {}, Manager: {}", employeeEmail);
		        }
		    } catch (Exception mailEx) {
		        logger.error("CRITICAL: Failed to send draft update email for employee {}: {}", savedDraft.getName(), mailEx.getMessage());
		        mailEx.printStackTrace();
		    }
		}

		

}
