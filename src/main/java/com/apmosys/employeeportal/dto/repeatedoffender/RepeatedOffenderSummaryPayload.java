package com.apmosys.employeeportal.dto.repeatedoffender;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * KPI payload for the four RO metric cards (no Resolved card).
 * Populated from {@link com.apmosys.employeeportal.repository.RepeatedOffenderRepository#fetchSummary} once SQL is integrated.
 */
@Getter
@Setter
public class RepeatedOffenderSummaryPayload {

	private long totalApplicable;
	private long repeatedOffenders;
	private String repeatedPct;
	private long allMonthsStreak;
	private long thresholdBucket;

	/** Echo of month column headers for the selected range, e.g. FEB 2026 — optional for UI. */
	private List<String> monthLabels = new ArrayList<>();

	public static RepeatedOffenderSummaryPayload emptyStub() {
		RepeatedOffenderSummaryPayload p = new RepeatedOffenderSummaryPayload();
		p.setTotalApplicable(0L);
		p.setRepeatedOffenders(0L);
		p.setRepeatedPct("0");
		p.setAllMonthsStreak(0L);
		p.setThresholdBucket(0L);
		return p;
	}
}
