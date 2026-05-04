package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.enums.SchedulerExecutionStatus;
import com.apmosys.employeeportal.enums.SchedulerTriggerType;
import com.apmosys.employeeportal.model.SchedulerExecutionTracker;
import com.apmosys.employeeportal.repository.SchedulerExecutionTrackerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerExecutionTrackerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerExecutionTrackerService.class);

    private final SchedulerExecutionTrackerRepository repository;

    @Transactional
    public void logExecutionStart(String methodName, SchedulerTriggerType triggerType) {
        // Intentionally NO-OP.
        // We persist exactly one row per execution, and status is never NULL.
        if (!SchedulerTriggerType.INTERNAL.equals(triggerType)) {
            log.debug("Scheduler start methodName={} triggerType={}", methodName, triggerType);
        }
    }

    @Transactional
    public void logExecutionSuccess(String methodName, SchedulerTriggerType triggerType) {
        logTerminal(methodName, triggerType, SchedulerExecutionStatus.SUCCESS, null);
    }

    @Transactional
    public void logExecutionFailure(String methodName, SchedulerTriggerType triggerType, String errorMessage) {
        logTerminal(methodName, triggerType, SchedulerExecutionStatus.FAILED, errorMessage);
    }

    @Transactional
    public void logExecutionInterrupted(String methodName, SchedulerTriggerType triggerType, String errorMessage) {
        logTerminal(methodName, triggerType, SchedulerExecutionStatus.INTERRUPTED, errorMessage);
    }

    private void logTerminal(
            String methodName,
            SchedulerTriggerType triggerType,
            SchedulerExecutionStatus status,
            String errorMessage) {

        if (SchedulerTriggerType.INTERNAL.equals(triggerType)) {
            return;
        }

        try {
            SchedulerExecutionTracker row = new SchedulerExecutionTracker();
            row.setMethodName(methodName);
            row.setTriggerType(triggerType);
            row.setExecutionStatus(status);
            row.setCreatedOn(LocalDateTime.now());
            row.setErrorMessage(sanitize(errorMessage));
            repository.save(row);
        } catch (Exception e) {
            log.warn("Failed to log scheduler terminal status methodName={} triggerType={} status={}",
                    methodName, triggerType, status, e);
        }
    }

    public boolean shouldRunTodayForScheduler(String methodName) {
        try {
            SchedulerExecutionTracker latest = repository
                    .findTopByMethodNameAndTriggerTypeOrderByCreatedOnDesc(methodName, SchedulerTriggerType.SCHEDULER)
                    .orElse(null);

            if (latest == null || latest.getCreatedOn() == null) {
                return true;
            }

            if (!LocalDateTime.now().toLocalDate().equals(latest.getCreatedOn().toLocalDate())) {
                return true;
            }

            return latest.getExecutionStatus() != SchedulerExecutionStatus.SUCCESS;
        } catch (Exception e) {
            // Safety: never block cron if tracker read fails
            log.warn("Failed to evaluate scheduler shouldRunToday methodName={}", methodName, e);
            return true;
        }
    }

    private static String sanitize(String errorMessage) {
        if (errorMessage == null) return null;
        String msg = errorMessage.trim();
        if (msg.length() <= 2000) return msg;
        return msg.substring(0, 2000);
    }
}
