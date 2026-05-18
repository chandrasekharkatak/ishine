package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SubFeatureMaster;

@Repository
public interface SubFeatureMasterRepository extends JpaRepository<SubFeatureMaster, Long> {

	public List<SubFeatureMaster> findBySubFeatureType(Short subFeatureType);

	@Query(nativeQuery = true)
	public List<Object[]> getAllSubFeatures();

	public SubFeatureMaster findBySubFeatureName(String subFeatureName);

}
