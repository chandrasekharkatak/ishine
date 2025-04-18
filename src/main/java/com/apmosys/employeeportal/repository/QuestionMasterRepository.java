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

}
