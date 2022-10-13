package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Appreciation;

@Repository
public interface AppreciationRepository extends JpaRepository<Appreciation, Long> {
	


}
