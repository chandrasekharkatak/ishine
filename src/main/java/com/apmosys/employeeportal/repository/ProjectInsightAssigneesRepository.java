package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightAssignees;

public interface ProjectInsightAssigneesRepository extends JpaRepository<ProjectInsightAssignees, Long> {

	List<ProjectInsightAssignees> getByEntityIdAndEntityType(Long milestoneId, String string);

	@Query(value="Select distinct e.name from ProjectInsightAssignees pia inner join Employee e on e.empId=pia.assignedTo where pia.entityId=:entityId and pia.entityType=:entityType")
	List<String> getUserNameByEntityIdAndEntityType(Long entityId, String entityType);

}
