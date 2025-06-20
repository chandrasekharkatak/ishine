package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ResourceRequirementTemp;

public interface ResourceRequirementTempRepo extends JpaRepository<ResourceRequirementTemp, Long>{
	
//	@Query(nativeQuery=true,value="select distinct department from resource_requirement_temp where po_project_id=:poProjectId")
//	List<String> getAllDepartmentsFromPoProjectId(@Param("poProjectId") Long poProjectId);
	
	 @Query(value="select distinct rrt.department from ResourceRequirementTemp rrt where rrt.poProjectId=:poProjectId")
		List<String> getAllDepartmentsFromPoProjectId(@Param("poProjectId") Long poProjectId);

	@Query(nativeQuery=true, value="select  role, count, experience, department,resource_overview_id, po_project_id from resource_requirement_temp where po_project_id=:poProjectId")
	List<Object[]> findByPoProjectId(@Param("poProjectId")Long poProjectId);
}
