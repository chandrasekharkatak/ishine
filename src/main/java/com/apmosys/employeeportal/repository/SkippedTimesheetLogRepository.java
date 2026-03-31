package com.apmosys.employeeportal.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.SkippedTimesheetLog;

public interface SkippedTimesheetLogRepository 
        extends JpaRepository<SkippedTimesheetLog, Long> {
}
