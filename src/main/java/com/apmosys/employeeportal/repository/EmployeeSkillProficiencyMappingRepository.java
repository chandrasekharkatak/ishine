package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeSkillProficiencyDTO;
import com.apmosys.employeeportal.model.EmployeeSkillProficiencyMapping;

@Repository
public interface EmployeeSkillProficiencyMappingRepository extends JpaRepository<EmployeeSkillProficiencyMapping,Long>{

	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeSkillProficiencyDTO(esp.empSkillId,\n"
			+ "esp.empId,esp.skillId,ps.skillName,esp.additionalSkill,esp.proficiencyId,p.proficiencyName,\n"
			+ "p.imageUrl,esp.createdOn,esp.updatedOn)from EmployeeSkillProficiencyMapping esp\n"
			+ "left join PredefinedSkills ps on ps.skillId = esp.skillId\n"
			+ "inner join Proficiency p on p.proficiencyId= esp.proficiencyId\n"
			+ "where esp.empId = :empId and esp.active = true order by esp.empSkillId desc")
	public List<EmployeeSkillProficiencyDTO> getAllSkillsByEmpId(Long empId);

	public List<EmployeeSkillProficiencyMapping> findByEmpIdAndActiveTrue(Long empId);
}
