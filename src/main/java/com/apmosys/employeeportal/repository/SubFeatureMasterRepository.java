package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SubFeatureMaster;

@Repository
public interface SubFeatureMasterRepository extends JpaRepository<SubFeatureMaster, Long> {

	List<SubFeatureMaster> findByFeatureId(Long featureId);

	@Query("SELECT s FROM SubFeatureMaster s WHERE s.subFeatureName = :subFeatureName AND s.featureId IN "
			+ "(SELECT f.featureId FROM FeatureMaster f WHERE f.featureName = :featureName)")
	Optional<SubFeatureMaster> findBySubFeatureNameUnderFeature(@Param("subFeatureName") String subFeatureName,
			@Param("featureName") String featureName);

	public List<SubFeatureMaster> findBySubFeatureType(Short subFeatureType);

	@Query(nativeQuery = true)
	public List<Object[]> getAllSubFeatures();

	public SubFeatureMaster findBySubFeatureName(String subFeatureName);

}
