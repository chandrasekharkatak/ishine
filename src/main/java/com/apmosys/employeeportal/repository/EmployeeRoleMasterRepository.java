package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.dto.CertificateDTO;
import com.apmosys.employeeportal.dto.EmployeeRoleDTO;
import com.apmosys.employeeportal.model.EmployeeRole;

public interface EmployeeRoleMasterRepository extends JpaRepository<EmployeeRole, Integer> {
	
	public List<EmployeeRole> findByEmployeeRoleAndPermission(String employeeRole,String permission);
	
	public List<EmployeeRole> findBySubFeatureMasterIdAndPermission(Long subFeatureMasterId,String permission);

	@Query(nativeQuery = true)
	public List<Object[]> getAllSubFeatureList();
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.EmployeeRoleDTO("
			+ "sfm.subFeatureMasterId, sfm.subFeatureName, fm.featureId, "
			+ "fm.featureName, tm.tabId, tm.tabName) FROM SubFeatureMaster sfm "
			+ "LEFT JOIN FeatureMaster fm on sfm.featureId = fm.featureId "
			+ "LEFT JOIN TabMaster tm ON tm.tabId = fm.tabId "
			+ "WHERE "
		     + "(COALESCE(:tabNames, NULL) IS NULL OR tm.tabName IN :tabNames) "
		     + "AND (COALESCE(:featureNames, NULL) IS NULL OR fm.featureName IN :featureNames) "
		     + "AND (COALESCE(:subFeatureNames, NULL) IS NULL OR sfm.subFeatureName IN :subFeatureNames)")
	List<EmployeeRoleDTO> getAllSubFeatureList(List<String> tabNames,List<String> featureNames,List<String> subFeatureNames);


	@Query(nativeQuery = true)
	public List<Object[]> getMappedSubFeatureByJobRoleId(Long jobRoleId);
	
	@Query(nativeQuery = true)
	public List<EmployeeRole> findBySubFeatureMasterId(Long subFeatureMasterId);

	public EmployeeRole findBySubFeatureMasterIdAndEmployeeRole(Long subFeatureId, String employeeRole);

}
