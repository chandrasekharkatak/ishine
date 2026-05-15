package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ReimbursementSubmissionSettingsDTO;
import com.apmosys.employeeportal.model.ReimbursementSubmissionSettings;
import com.apmosys.employeeportal.repository.ReimbursementSubmissionSettingsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementSubmissionSettingsService {

	private static final int DEFAULT_DEADLINE_DAY = 10;

	@Autowired
	private ReimbursementSubmissionSettingsRepository settingsRepository;

	@Value("${reimbursement.ticket-id.zone:Asia/Kolkata}")
	private String reimbursementZone;

	@Transactional(readOnly = true)
	public ReimbursementSubmissionSettings getOrCreateSettings() {
		return settingsRepository.findById(ReimbursementSubmissionSettings.SINGLETON_ID)
				.orElseGet(this::defaultSettings);
	}

	private ReimbursementSubmissionSettings defaultSettings() {
		ReimbursementSubmissionSettings s = new ReimbursementSubmissionSettings();
		s.setSettingsId(ReimbursementSubmissionSettings.SINGLETON_ID);
		s.setMonthlyDeadlineDay(DEFAULT_DEADLINE_DAY);
		s.setEnabled("Y");
		return s;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchSettings() {
		ServiceResponse resp = new ServiceResponse();
		try {
			ReimbursementSubmissionSettings s = getOrCreateSettings();
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toDto(s));
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse saveSettings(ReimbursementSubmissionSettingsDTO dto) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (dto == null || dto.getMonthlyDeadlineDay() == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Monthly deadline day is required (1–31).");
				return resp;
			}
			int day = dto.getMonthlyDeadlineDay();
			if (day < 1 || day > 31) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Monthly deadline day must be between 1 and 31.");
				return resp;
			}
			ReimbursementSubmissionSettings s = settingsRepository
					.findById(ReimbursementSubmissionSettings.SINGLETON_ID).orElseGet(this::defaultSettings);
			s.setSettingsId(ReimbursementSubmissionSettings.SINGLETON_ID);
			s.setMonthlyDeadlineDay(day);
			s.setEnabled(Boolean.FALSE.equals(dto.getEnabled()) ? "N" : "Y");
			s.setUpdatedBy(dto.getUpdatedBy());
			s.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
			settingsRepository.save(s);
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(toDto(s));
			resp.setServiceMessage("Submission window settings saved.");
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchSubmissionWindowStatus() {
		ServiceResponse resp = new ServiceResponse();
		try {
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(buildWindowStatusMap());
		} catch (Exception e) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(e.getMessage());
		}
		return resp;
	}

	public void assertSubmissionAllowed() {
		Map<String, Object> status = buildWindowStatusMap();
		if (!Boolean.TRUE.equals(status.get("allowed"))) {
			throw new IllegalArgumentException(String.valueOf(status.get("message")));
		}
	}

	private Map<String, Object> buildWindowStatusMap() {
		ReimbursementSubmissionSettings s = getOrCreateSettings();
		boolean ruleEnabled = "Y".equalsIgnoreCase(s.getEnabled());
		int deadlineDay = s.getMonthlyDeadlineDay() != null ? s.getMonthlyDeadlineDay() : DEFAULT_DEADLINE_DAY;
		ZoneId zone = ZoneId.of(reimbursementZone != null ? reimbursementZone : "Asia/Kolkata");
		LocalDate today = LocalDate.now(zone);
		int currentDay = today.getDayOfMonth();
		boolean allowed = !ruleEnabled || currentDay <= deadlineDay;

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("allowed", allowed);
		out.put("ruleEnabled", ruleEnabled);
		out.put("monthlyDeadlineDay", deadlineDay);
		out.put("currentDayOfMonth", currentDay);
		out.put("currentMonthLabel", YearMonth.from(today).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
		out.put("zoneId", zone.getId());
		if (!allowed) {
			YearMonth next = YearMonth.from(today).plusMonths(1);
			out.put("nextOpenLabel", "1 " + next.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
			out.put("message",
					"Reimbursement applications are closed for "
							+ YearMonth.from(today).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
							+ " after day " + deadlineDay
							+ ". You can apply again from the 1st of next month.");
		} else if (ruleEnabled) {
			out.put("message", "You may submit reimbursement requests until day " + deadlineDay + " of this month (inclusive).");
		} else {
			out.put("message", "Submission window restriction is not enabled.");
		}
		return out;
	}

	private Map<String, Object> toDto(ReimbursementSubmissionSettings s) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("monthlyDeadlineDay", s.getMonthlyDeadlineDay());
		m.put("enabled", "Y".equalsIgnoreCase(s.getEnabled()));
		m.put("updatedBy", s.getUpdatedBy());
		m.put("updatedOn", s.getUpdatedOn());
		return m;
	}
}
