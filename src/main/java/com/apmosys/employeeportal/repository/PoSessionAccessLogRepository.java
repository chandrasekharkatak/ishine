package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.PoSessionAccessLog;

public interface PoSessionAccessLogRepository extends JpaRepository<PoSessionAccessLog, Long> {
}
