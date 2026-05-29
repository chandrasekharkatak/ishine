package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

/**
 * Represents a closed date range [from, to].
 * A null {@code to} means the range is open-ended (employee still active).
 */
public class DateRange {

    private final LocalDate from;
    private final LocalDate to;   // null = still active / no end date

    public DateRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to   = to;
    }

    public LocalDate getFrom() { return from; }
    public LocalDate getTo()   { return to;   }

    /** Returns true if {@code date} falls within [from, to] (to=null means open-ended). */
    public boolean contains(LocalDate date) {
        if (date == null) return false;
        if (date.isBefore(from)) return false;
        return to == null || !date.isAfter(to);
    }
}
