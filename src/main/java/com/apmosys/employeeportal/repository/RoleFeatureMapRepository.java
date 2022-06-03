package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.RoleFeatureMap;

@Repository
public interface RoleFeatureMapRepository extends JpaRepository<RoleFeatureMap,Long> {
	
	
	@Query(nativeQuery = true)
	public List<Object[]> getTabsByRoleId(Long roleId);

}
