package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PredefinedSkills;

@Repository
public interface PredefinedSkillsRepository extends JpaRepository<PredefinedSkills,Long> {

	
	 PredefinedSkills findBySkillNameIgnoreCase(String skillName);
}
