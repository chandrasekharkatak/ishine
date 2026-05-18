package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SkillDomainFeatureMaster;

@Repository
public interface SkillDomainFeatureMasterRepository
		extends JpaRepository<SkillDomainFeatureMaster, Integer>, JpaSpecificationExecutor<SkillDomainFeatureMaster> {

	boolean existsByDomainId(Integer domainId);

	boolean existsBySubdomainId(Integer subdomainId);

	List<SkillDomainFeatureMaster> findByDomainIdAndSubdomainIdIsNullOrderByFeatureNameAsc(Integer domainId);

	List<SkillDomainFeatureMaster> findByDomainIdAndSubdomainIdOrderByFeatureNameAsc(Integer domainId,
			Integer subdomainId);
}
