package com.apmosys.employeeportal.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.QuarterModel;

public interface QuarterRepository extends JpaRepository<QuarterModel, Long>{
}
