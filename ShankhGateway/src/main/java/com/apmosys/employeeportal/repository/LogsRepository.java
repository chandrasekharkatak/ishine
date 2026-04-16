package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Log;

public interface LogsRepository extends JpaRepository<Log, Long> {

}
