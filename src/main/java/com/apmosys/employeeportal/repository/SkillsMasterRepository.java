package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SkillsMaster;

@Repository
public interface SkillsMasterRepository extends JpaRepository<SkillsMaster, Integer>, JpaSpecificationExecutor<SkillsMaster> {

	List<SkillsMaster> findBySkillNameIgnoreCaseOrderBySkillIdAsc(String skillName);

	List<SkillsMaster> findByDepartmentIdOrderBySkillNameAsc(Long departmentId);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SkillsMaster s "
			+ "WHERE s.departmentId = :departmentId AND LOWER(s.skillName) = LOWER(:skillName)")
	boolean existsInDepartmentIgnoreCaseSkillName(@Param("departmentId") Long departmentId,
			@Param("skillName") String skillName);
}
