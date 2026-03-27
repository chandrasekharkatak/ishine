package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TempFailedDoc;

public interface TempFailedDocRepository extends JpaRepository<TempFailedDoc, Long>{
    
}
