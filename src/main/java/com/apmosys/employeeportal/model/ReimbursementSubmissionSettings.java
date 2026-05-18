package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "reimbursement_submission_settings")
public class ReimbursementSubmissionSettings {

	public static final long SINGLETON_ID = 1L;

	@Id
	@Column(name = "settings_id")
	private Long settingsId = SINGLETON_ID;

	/** Last calendar day of the month (inclusive) when employees may submit new tickets. */
	@Column(name = "monthly_deadline_day", nullable = false)
	private Integer monthlyDeadlineDay = 10;

	/** Y = enforce deadline; N = submissions always allowed. */
	@Column(name = "enabled", nullable = false, length = 1)
	private String enabled = "Y";

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "updated_on")
	private Timestamp updatedOn;
}
