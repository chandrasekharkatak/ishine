package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ApiLog;

public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

}
