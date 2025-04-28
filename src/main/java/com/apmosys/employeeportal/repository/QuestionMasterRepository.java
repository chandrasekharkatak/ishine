package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.QuestionMaster;

public interface QuestionMasterRepository extends JpaRepository<QuestionMaster, Long>{

	List<QuestionMaster> findByEntityIdAndEntityType(Long milestoneId, String string);

	@Query(value="Select qm from QuestionMaster qm "
			+ "inner join ProjectInsightAssignees pia on qm.entityId = pia.entityId and qm.entityType = pia.entityType "
			+ "where pia.assignedTo=:employeeId and qm.entityId=:entityId and qm.entityType=:entityType")
	List<QuestionMaster> findByEntityIdAndEntityTypeAndAssignedTo(Long entityId, String entityType,Long employeeId);
	
	QuestionMaster findByQuestionMasterId(Long questionId);

//	List<QuestionMaster> findByMilestoneId(Long milestoneId);
	
	@Transactional
	@Modifying
	@Query(value="delete from QuestionMaster qm where qm.questionMasterId=:questionMasterId")
	void deleteAllQuestionsByEntityIdAndEntityType(Long questionMasterId);
	
	@Query(value="SELECT p.project_id,p.project_name,p.project_manager_id,pme.name as 'projectManagerName',CAST(NULL AS char) as created_by,CAST(NULL AS char) as  created_by_name ,CAST(NULL AS char) as  created_on \n"
			+ "FROM question_master qm \n"
			+ "INNER JOIN projects p ON p.project_id=qm.entity_id and qm.entity_type = 'Project' \n"
			+ "INNER JOIN employee pme ON pme.emp_id=p.project_manager_id \n"
			+ "where p.project_id not in(:projectIds)\n"
			+ "GROUP BY p.project_id,p.project_name,p.project_manager_id,pme.name\n"
			+ "",nativeQuery = true)
	List<Object[]> getAllProjectInsight(List<Long> projectIds);
}
