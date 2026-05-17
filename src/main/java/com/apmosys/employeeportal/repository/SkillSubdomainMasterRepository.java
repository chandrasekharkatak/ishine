package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SkillSubdomainMaster;

@Repository
public interface SkillSubdomainMasterRepository
		extends JpaRepository<SkillSubdomainMaster, Integer>, JpaSpecificationExecutor<SkillSubdomainMaster> {

	boolean existsByDomainId(Integer domainId);

	List<SkillSubdomainMaster> findByDomainIdAndSubdomainNameIgnoreCaseOrderBySubdomainIdAsc(Integer domainId,
			String subdomainName);

	List<SkillSubdomainMaster> findByDomainIdOrderBySubdomainNameAsc(Integer domainId);
}
