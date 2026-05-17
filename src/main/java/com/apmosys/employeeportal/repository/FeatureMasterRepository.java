package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.FeatureMaster;

@Repository
public interface FeatureMasterRepository extends JpaRepository<FeatureMaster, Long> {

	List<FeatureMaster> findByTabId(Long tabId);

	@Query("SELECT distinct fm.featureId FROM FeatureMaster fm "
			+ "INNER JOIN SubFeatureMaster sfm on sfm.featureId = fm.featureId "
			+ "INNER JOIN RoleFeatureMap rfm on rfm.subFeatureMasterId = sfm.subFeatureMasterId "
			+ " WHERE rfm.jobRoleId =:jobRoleId")
	List<Long> getAllFeatureIdsByJobRoleIds(@Param("jobRoleId") Long jobRoleId);
}
