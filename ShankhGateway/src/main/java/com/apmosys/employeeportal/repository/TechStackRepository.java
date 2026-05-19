package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.*;

import com.apmosys.employeeportal.model.TechStack;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

}