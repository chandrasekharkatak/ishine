package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.TagMaster;

public interface TagMasterRepository extends JpaRepository<TagMaster, Long>, JpaSpecificationExecutor<TagMaster> {

	List<TagMaster> findByProjectId(Long projectId);

	List<TagMaster> findByEntityIdAndEntityTypeAndType(Long userContributionId, String string, String string2);
	
	@Query(value = "select tm from TagMaster tm where tm.entityId in :entityIdList and tm.entityType=:entityType  and tm.type=:type ")
	List<TagMaster> findByEntityIdInAndEntityTypeAndType(List<Long> entityIdList, String entityType, String type);

	List<TagMaster> findByEntityIdAndEntityType(Object object, String projectText);

}
