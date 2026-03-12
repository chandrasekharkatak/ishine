package com.apmosys.employeeportal.cron;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.repository.TrainingConsentRepository;
import com.apmosys.employeeportal.service.MailService;
import com.apmosys.employeeportal.service.TrainingUserServiceImpl;

import lombok.Data;

@Component
public class TrainingCron {

	@Value("${training.job.role.exclude}")
	private String trainingJobRoleExclude;

	@Value("${training.dry.run.empids.to.include}")
	private String trainingDryRunEmpIdsToInclude;

	@Autowired
	private TrainingConsentRepository trainingConsentRepository;

	@Autowired
	private MailService mailService;

	@Scheduled(cron = "${trainingReminder.time}")
	public void sendTrainingReminders() {

			// Dry run empIds
			List<Long> empIdsToInclude = Arrays.stream(trainingDryRunEmpIdsToInclude.split(","))
					.map(String::trim)
					.map(Long::parseLong)
					.collect(Collectors.toList());

		
		try {

			List<Object[]> pendingTrainings = trainingConsentRepository.findEmpForUnattendedQuiz(empIdsToInclude);
			System.out.println("Pending Trainings : " + pendingTrainings);
			
			if (pendingTrainings == null || pendingTrainings.isEmpty()) {
				return;
			}
			
			List<EmpEmailTrainingDTO> employeeNotAttendedTraining = getEmployeeNameMap(pendingTrainings);

			for (EmpEmailTrainingDTO entry : employeeNotAttendedTraining) {
				String empEmail = entry.getEmpEmail();
				List<String> trainingNames = entry.getTrainings();
				String empName = entry.getEmpName();
				
				triggerTrainingReminderMail(empEmail, trainingNames, empName);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void triggerTrainingReminderMail(String empEmail, List<String> trainingNames, String empName) {
		try {

			StringBuilder html = new StringBuilder();
			html.append("<html><body>");
			html.append("<p>Dear <b>").append(empName).append("</b>,</p>");
			html.append("<p>This is a reminder that you have not yet completed the following training:</p>");

			html.append("<div style='overflow-x:auto;'>");
			html.append("<table border='1' style='border-collapse: collapse; width: 100%; table-layout: auto;'>");
			html.append("<tr>");
			html.append(
					"<th style='white-space: nowrap; padding: 8px; background-color: #f5f5f5; text-align: left;'>Training Name</th>");
			// html.append(
					// "<th style='white-space: nowrap; padding: 8px; background-color: #f5f5f5; text-align: left;'>Deadline</th>");
			html.append("</tr>");

			for (String trainingName : trainingNames) {
				html.append("<tr>");
				html.append("<td style='white-space: nowrap; padding: 8px;'>").append(trainingName).append("</td>");
				// html.append("<td style='white-space: nowrap; padding: 8px; color: #d63031;
				// font-weight: bold;'>").append(formattedDeadline).append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			html.append("</div>");

			html.append("<p style='color: #d63031; font-weight: bold;'>");
			html.append(" Please complete this training as early as possible , avoid any delays.");
			html.append("</p>");

			html.append(
					"<p>If you have any questions or need assistance, please contact the training coordinator.</p>");

			html.append("<p>Regards,<br/>HR Team</p>");
			html.append("</body></html>");

			String subject = "Reminder: Complete Your Pending Training";

			mailService.sendMailToMultipleRecipients(List.of(empEmail), null, subject, html.toString());
			System.out.println("Mail sent to : " + empEmail);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private List<EmpEmailTrainingDTO> getEmployeeNameMap(List<Object[]> datas) {
		Map<String, EmpEmailTrainingDTO> empDataMap = new LinkedHashMap<>();
		for (Object[] data : datas) {
			String empEmail = data[0] != null ? data[0].toString() : "";
			String trainingName = data[1] != null ? data[1].toString() : "";
			String empName = data[2] != null ? data[2].toString() : "";

			EmpEmailTrainingDTO dto = empDataMap.get(empEmail);
			if (dto == null) {
				dto = new EmpEmailTrainingDTO(empEmail, empName);
				empDataMap.put(empEmail, dto);
			}

			if (!trainingName.isEmpty()) {
				dto.addTraining(trainingName);
			}

		}
		return new ArrayList<>(empDataMap.values());
	}

}

@Data
class EmpEmailTrainingDTO {

	private String empEmail;
	private String empName;
	private List<String> trainings;

	public EmpEmailTrainingDTO(String empEmail, String empName) {
		this.empEmail = empEmail;
		this.empName = empName;
		this.trainings = new ArrayList<>();
	}

	public void addTraining(String training) {
		this.trainings.add(training);
	}

}
