package com.apmosys.employeeportal.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightSubDomain;

public interface ProjectInsightSubDomainRepository extends JpaRepository<ProjectInsightSubDomain, Long> {

    @Modifying
    @Transactional
    @Query("update ProjectInsightSubDomain s set s.subDomain = :subDomain where s.subDomainId = :subDomainId")
    void updateSubDomainName(Long subDomainId, String subDomain);
}
