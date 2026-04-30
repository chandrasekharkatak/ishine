package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SchedulerExecutionTracker;

@Repository
public interface SchedulerExecutionTrackerRepository extends JpaRepository<SchedulerExecutionTracker, Long> {

    Optional<SchedulerExecutionTracker> findTopByMethodNameOrderByCreatedOnDesc(String methodName);

    Optional<SchedulerExecutionTracker> findTopByMethodNameAndTriggerTypeOrderByCreatedOnDesc(
            String methodName,
            com.apmosys.employeeportal.enums.SchedulerTriggerType triggerType);
}
