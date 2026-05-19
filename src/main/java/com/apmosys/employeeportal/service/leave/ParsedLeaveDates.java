package com.apmosys.employeeportal.service.leave;

import java.time.LocalDate;

/** Parsed from-date / to-date for leave apply (yyyy-MM-dd). */
public final class ParsedLeaveDates {

    public final LocalDate fromDate;
    public final LocalDate toDate;

    public ParsedLeaveDates(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
}
