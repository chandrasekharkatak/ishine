package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Outcomes;

public interface OutcomesRespository extends JpaRepository<Outcomes, Long> {

    
}