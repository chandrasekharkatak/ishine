package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Proficiency;

@Repository
public interface ProficiencyRepository extends JpaRepository<Proficiency,Long> {
	
	 Optional<Proficiency> findByProficiencyNameIgnoreCase(String proficiencyName);

}
