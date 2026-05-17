package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.apmosys.employeeportal.enums.SchedulerExecutionStatus;
import com.apmosys.employeeportal.enums.SchedulerTriggerType;

import lombok.Getter;
import lombok.Setter;

/**
 * Append-only execution log for schedulers / manual triggers.
 */
@Entity
@Table(name = "scheduler_execution_tracker")
@Getter
@Setter
public class SchedulerExecutionTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false)
    private SchedulerExecutionStatus executionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private SchedulerTriggerType triggerType;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
