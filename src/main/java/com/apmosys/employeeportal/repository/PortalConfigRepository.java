package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.CustomQueryDetails;
import com.apmosys.employeeportal.model.PortalConfig;

public interface PortalConfigRepository extends JpaRepository<PortalConfig, Short>{
	
	@Query(value="SELECT * FROM portal_config where portal_config_id=:portalConfigId", nativeQuery=true)
	Optional<PortalConfig> findByportalConfigById(@Param("portalConfigId") short portalConfigId);

	

}
