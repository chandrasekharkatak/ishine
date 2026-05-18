package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.RoleFeatureMap;

@Repository
public interface RoleFeatureMapRepository extends JpaRepository<RoleFeatureMap, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getTabsByRoleId(Long roleId);

	public List<RoleFeatureMap> findByJobRoleId(Long roleId);

	public Integer deleteByJobRoleId(Long roleId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getRolesToBeMappedWithNewSubFeature(List<String> employeeRolelist);

	public RoleFeatureMap findByJobRoleIdAndSubFeatureMasterId(Long jobRoleId, Long subFeatureId);

	/**
	 * Remove {@code role_subfeature_mapping} rows that contradict {@code employee_role_master}
	 * default ACL (permission N) for the job role's persona.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(nativeQuery = true, value = "DELETE rsm FROM role_subfeature_mapping rsm "
			+ "INNER JOIN job_role jr ON jr.job_role_id = rsm.job_role_id "
			+ "INNER JOIN employee_role_master erm ON erm.employee_role = jr.employee_role "
			+ "AND erm.sub_feature_master_id = rsm.sub_feature_master_id "
			+ "WHERE UPPER(TRIM(COALESCE(erm.permission, ''))) = 'N'")
	int deleteMappingsDeniedInEmployeeRoleMaster();

	/**
	 * Ensure every job role has {@code role_subfeature_mapping} rows for all sub-features its
	 * persona is allowed in {@code employee_role_master} (permission Y). Keeps login tab list
	 * aligned with "View Default Role Access" after bulk or legacy data drift.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(nativeQuery = true, value = "INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id) "
			+ "SELECT jr.job_role_id, erm.sub_feature_master_id "
			+ "FROM employee_role_master erm "
			+ "INNER JOIN job_role jr ON jr.employee_role = erm.employee_role "
			+ "WHERE UPPER(TRIM(COALESCE(erm.permission, ''))) = 'Y' "
			+ "AND NOT EXISTS (SELECT 1 FROM role_subfeature_mapping r "
			+ "WHERE r.job_role_id = jr.job_role_id AND r.sub_feature_master_id = erm.sub_feature_master_id)")
	int insertMappingsAllowedInEmployeeRoleMaster();

}
