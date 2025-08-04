package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.model.ProjectInsighProjectMapping;

public interface ProjectInsighProjectMappingRepository extends JpaRepository<ProjectInsighProjectMapping, Long> {

	ProjectInsighProjectMapping findByProjectId(Integer projectId);
	 
	@Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO( " +
		       "   pm.projectId, " +
		       "   pm.projectInsightId, " +
		       "   pm.isDraft, " +
		       "   pm.createdBy, " +
		       "   p.projectName, " +
		       "   e.name, " +
		       "   c.clientName," +
		       "   manager.name, " +
		       "   pm.projectInsightDetailsId" +
		       ") " +
		       "FROM ProjectInsighProjectMapping pm " +
		       "JOIN Project p ON p.id = pm.projectId " +
		       "JOIN Employee e ON e.id = pm.createdBy " +
		       "JOIN Client c ON c.id = p.clientId " +
		       "LEFT JOIN Employee manager ON manager.id = p.projectManagerId")
		List<ProjectInsighProjectMappingDTO> fetchAllProjectMappings();

	@Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO( " +
       "   pm.projectId, " +
       "   pm.projectInsightId, " +
       "   pm.isDraft, " +
       "   pm.createdBy, " +
       "   p.projectName, " +
       "   e.name, " +
       "   c.clientName, " +
       "   manager.name, " +
       "   pm.projectInsightDetailsId" +
       ") " +
       "FROM ProjectInsighProjectMapping pm " +
       "JOIN Project p ON p.id = pm.projectId " +
       "JOIN Employee e ON e.id = pm.createdBy " +
       "JOIN Client c ON c.id = p.clientId " +
       "LEFT JOIN Employee manager ON manager.id = p.projectManagerId " +
       "WHERE pm.projectId IN :ids")
List<ProjectInsighProjectMappingDTO> fetchAllProjectMappingsByInsightIds(@Param("ids") List<Integer> ids);


	ProjectInsighProjectMapping findByProjectInsightId(String id);

	@Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO( " +
		       "   pm.projectId, " +
		       "   pm.projectInsightId, " +
		       "   pm.isDraft, " +
		       "   pm.createdBy, " +
		       "   p.projectName, " +
		       "   e.name, " +
		       "   c.clientName, " +
		       "   manager.name, " +
		       "   pm.projectInsightDetailsId" +
		       ") " +
		       "FROM ProjectInsighProjectMapping pm " +
		       "JOIN Project p ON p.id = pm.projectId " +
		       "LEFT JOIN Employee e ON e.id = pm.createdBy " +
		       "LEFT JOIN Client c ON c.id = p.clientId " +
		       "LEFT JOIN Employee manager ON manager.id = p.projectManagerId " +
		       "WHERE pm.projectInsightId IN :insightIds")
		List<ProjectInsighProjectMappingDTO> fetchAllProjectMappingsByProjectInsightId(@Param("insightIds") List<String> insightIds);




}
