package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.ProjectInsightResponseMetadata;

public interface ProjectInsightResponseRepository extends JpaRepository<ProjectInsightResponse, Long> {

	ProjectInsightResponse findByQuestionMasterId(Long questionMasterId);

	@Query(value="Select  new com.apmosys.employeeportal.model.ProjectInsightResponse( pir.projectInsightResponseId,pir.projectInsightResponseMetadataId,pir.questionMasterId,pir.response,pir.documentPath,pir.documentFileName,pir.isApprovedForKnowledgeHub,pirm.responseBy, pirm.isFinalSubmitted) from ProjectInsightResponse pir \n"
			+ " inner join ProjectInsightResponseMetadata pirm on pirm.projectInsightResponseMetadataId = pir.projectInsightResponseMetadataId  \n"
			+ " where pirm.responseBy =:employeeId and pir.questionMasterId =:questionMasterId  ")
	List<ProjectInsightResponse> findByQuestionMasterIdAndEmpId(Long questionMasterId, Long employeeId);

	@Query(value="Select  new com.apmosys.employeeportal.model.ProjectInsightResponse( pir.projectInsightResponseId,pir.projectInsightResponseMetadataId,pir.questionMasterId,pir.response,pir.documentPath,pir.documentFileName,pir.isApprovedForKnowledgeHub,pirm.responseBy, pirm.isFinalSubmitted) from ProjectInsightResponse pir \n"
			+ " inner join ProjectInsightResponseMetadata pirm on pirm.projectInsightResponseMetadataId = pir.projectInsightResponseMetadataId  \n"
			+ " where pir.questionMasterId =:questionMasterId  ")
	List<ProjectInsightResponse> findAllByQuestionMasterId(Long questionMasterId);

	@Query(value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',pim.created_by,e2.name as 'createdByName',pim.created_on \n"
			+ "FROM question_master qm \n"
			+ "inner JOIN project_insight_assignees pia ON qm.entity_id = pia.entity_id AND qm.entity_type = pia.entity_type \n"
			+ "left JOIN project_insight_milestone pim ON pim.milestone_id = qm.entity_id and qm.entity_type = 'Milestone'  \n"
			+ "left JOIN project_insight_module pimo ON pimo.module_id = qm.entity_id and qm.entity_type = 'Module'  \n"
			+ "left JOIN project_insight_submodule pismo ON pismo.submodule_id = qm.entity_id and qm.entity_type = 'SubModule'  \n"
			+ "INNER JOIN projects p ON p.project_id=pim.project_id \n"
			+ "INNER JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "INNER JOIN employee e2 ON e2.emp_id=pim.created_by \n"
			+ "WHERE pia.assigned_to =:empId \n"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name ,pim.created_by,e2.name,pim.created_on "
			,nativeQuery = true)
	List<Object[]> getAllProjectInsightByUser(Long empId);
	
	@Query(value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',pim.created_by,e2.name as 'createdByName',pim.created_on,COUNT(*)  \n"
			+ "FROM project_insight_response_metadata pirm  \n"
			+ "INNER JOIN projects p ON p.project_id=pirm.project_id \n"
			+ "LEFT JOIN project_insight_milestone pim ON pim.project_id=p.project_id \n"
			+ "INNER JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "INNER JOIN employee e2 ON e2.emp_id=pim.created_by \n"
			+ "Where pirm.response_by =:empId \n"
			+ "AND pirm.is_final_submitted = 'Y' \n"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name,pim.created_by,e2.name,pim.created_on  \n"
			,nativeQuery = true)
	List<Object[]> getAllProjectInsightForReviewByUser(Long empId);

	@Query(value="Select  new com.apmosys.employeeportal.model.ProjectInsightResponse( pir.projectInsightResponseId,pir.projectInsightResponseMetadataId,pir.questionMasterId,pir.response,pir.documentPath,pir.documentFileName,pir.isApprovedForKnowledgeHub,pirm.responseBy, pirm.isFinalSubmitted) from ProjectInsightResponse pir \n"
			+ " inner join ProjectInsightResponseMetadata pirm on pirm.projectInsightResponseMetadataId = pir.projectInsightResponseMetadataId  \n"
			+ " where pirm.responseBy =:empId and pir.questionMasterId =:questionId  ")
	ProjectInsightResponse findByEmpIdAndQuestionMasterId(Long empId, Long questionId);
	
	@Query(value="Select  new com.apmosys.employeeportal.model.ProjectInsightResponse( pir.projectInsightResponseId,pir.projectInsightResponseMetadataId,pir.questionMasterId,pir.response,pir.documentPath,pir.documentFileName,pir.isApprovedForKnowledgeHub,pirm.responseBy, pirm.isFinalSubmitted) from ProjectInsightResponse pir \n"
			+ " inner join ProjectInsightResponseMetadata pirm on pirm.projectInsightResponseMetadataId = pir.projectInsightResponseMetadataId  \n"
			+ "where pir.questionMasterId=:questionMasterId and  pirm.responseBy not in  :empIdList  \n ")
	List<ProjectInsightResponse> findAllByQuestionMasterIdAndEmpIdListNotIn(Long questionMasterId,List<Long> empIdList);

	@Transactional
	@Modifying
	@Query(value="delete from ProjectInsightResponse pir where pir.questionMasterId=:questionId ")
	void deleteAllProjectInsightResponseByQuestionMasterId(Long questionId);

}
