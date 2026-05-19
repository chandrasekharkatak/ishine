package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.GrievanceIssueScenario;

@Repository
public interface GrievanceIssueScenarioRepository extends JpaRepository<GrievanceIssueScenario, Long> {

	List<GrievanceIssueScenario> findAllByOrderByTabKeyAscSortOrderAscScenarioLabelAsc();

	Page<GrievanceIssueScenario> findAll(Pageable pageable);

	@Query(value = "SELECT scenario_label FROM grievance_issue_scenario WHERE is_active = 1 "
			+ "AND LOWER(TRIM(tab_key)) = LOWER(TRIM(:tabKey)) "
			+ "AND (feature_name IS NULL OR TRIM(feature_name) = '' "
			+ "     OR LOWER(TRIM(feature_name)) = LOWER(TRIM(:feature))) "
			+ "AND (sub_feature_name IS NULL OR TRIM(sub_feature_name) = '' "
			+ "     OR LOWER(TRIM(sub_feature_name)) = LOWER(TRIM(:sub))) "
			+ "ORDER BY sort_order ASC, scenario_label ASC", nativeQuery = true)
	List<String> findScenarioLabels(@Param("tabKey") String tabKey, @Param("feature") String feature,
			@Param("sub") String sub);
}
