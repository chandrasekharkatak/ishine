package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SkillDomainMaster;

@Repository
public interface SkillDomainMasterRepository
		extends JpaRepository<SkillDomainMaster, Integer>, JpaSpecificationExecutor<SkillDomainMaster> {

	boolean existsByDomainNameIgnoreCase(String domainName);

	boolean existsByDomainNameIgnoreCaseAndDomainIdNot(String domainName, Integer domainId);

	List<SkillDomainMaster> findByDomainNameIgnoreCaseOrderByDomainIdAsc(String domainName);

	List<SkillDomainMaster> findAllByOrderByDomainNameAsc();
}
