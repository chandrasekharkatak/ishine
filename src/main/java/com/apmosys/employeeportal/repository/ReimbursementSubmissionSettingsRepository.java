package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementSubmissionSettings;

public interface ReimbursementSubmissionSettingsRepository
		extends JpaRepository<ReimbursementSubmissionSettings, Long> {
}
