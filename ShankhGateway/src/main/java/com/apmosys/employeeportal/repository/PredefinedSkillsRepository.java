package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PredefinedSkills;

@Repository
public interface PredefinedSkillsRepository extends JpaRepository<PredefinedSkills,Long> {

	
	 PredefinedSkills findBySkillNameIgnoreCase(String skillName);
	 
	 @Query(nativeQuery = true,value = "SELECT * FROM predefined_skills \n"
	 		+ "WHERE REPLACE(LOWER(skill_name), ' ', '') = REPLACE(LOWER(:skillName), ' ', '')")
	 PredefinedSkills findByNormalizedSkillName(String skillName);
}
