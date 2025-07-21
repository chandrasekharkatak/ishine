package com.apmosys.employeeportal.repository;

import java.util.*;
import java.lang.Override;
import java.time.LocalDateTime;

import javax.transaction.Transactional;

import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightServiceModel;
import com.apmosys.employeeportal.model.ProjectInsightSubDomain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;

import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;

public interface ProjectInsightDomainRepository extends JpaRepository<ProjectInsightDomain, Long> {

        ProjectInsightDomain findByDomain(String domain);

        boolean existsByDomain(String domain);

        @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy(d.domain,d.domainId, d.createdOn, e.name, d.isActive, d.isApproved, e1.name) FROM ProjectInsightDomain d LEFT JOIN Employee e ON d.createdBy = e.empId LEFT JOIN Employee e1 ON d.approvedBy = e1.empId")
        Page<ProjectInsightDomainCreatedBy> findAllDomainAndCreatedBy(Pageable pageable);

        @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy(d.domain,d.domainId, d.createdOn, e.name, d.isActive, d.isApproved, e1.name) "
                        +
                        "FROM ProjectInsightDomain d " +
                        "LEFT JOIN Employee e ON d.createdBy = e.empId " +
                        "LEFT JOIN Employee e1 ON d.approvedBy = e1.empId "+
                        "WHERE (:domain IS NULL OR d.domain LIKE %:domain%) " +
                        "AND (:createdBy IS NULL OR e.name LIKE %:createdBy%) " +
                        "AND (:createdOn IS NULL OR FUNCTION('DATE', d.createdOn) = FUNCTION('DATE', :createdOn)) " +
                        "AND (:isApproved IS NULL OR d.isApproved = :isApproved) "+
                        "AND (:isActive IS NULL OR d.isActive = :isActive)")
        Page<ProjectInsightDomainCreatedBy> findAllDomainSearched(
                        @Param("domain") String domain,
                        @Param("createdBy") String createdBy,
                        @Param("createdOn") LocalDateTime createdOn,
                        @Param("isActive") Boolean isActive,
                        @Param("isApproved") ProjectInsightDomainApprovedStatus isApproved,
                        Pageable pageable);

        @Modifying
        @Transactional
        @Query("update ProjectInsightDomain d set d.domain = :domain where d.domainId = :domainId")
        void updateDomainName(Long domainId, String domain);

        @Modifying
        @Transactional
        @Query("update ProjectInsightDomain d set d.isApproved = :isApproved , d.approvedBy = :approvedBy, d.approvedOn = current_timestamp where d.domainId = :domainId")
        void updateIsApproved(@Param("domainId") Long domainId, @Param("isApproved") ProjectInsightDomainApprovedStatus isApproved, @Param("approvedBy") Long approvedBy);

        @Modifying
        @Transactional
        @Query("delete from ProjectInsightDomain d where d.domain = :domain")
        void deleteByDomainName(String domain);

        // setting isactive to isActive
        @Modifying
        @Transactional
        @Query("update ProjectInsightDomain d set d.isActive = :isActive where d.domainId = :domainId or d.domain = :domain")
        void updateIsActive(Long domainId, boolean isActive, String domain);

        // @Query("SELECT DISTINCT d FROM ProjectInsightDomain d " +
        // "LEFT JOIN on d.subDomains sd " +
        // "LEFT JOIN on sd.services s " +
        // "LEFT JOIN on s.subServices ss " +
        // "WHERE d.isActive = true " +
        // "AND (sd IS NULL OR sd.isActive = true) " +
        // "AND (s IS NULL OR s.isActive = true) " +
        // "AND (ss IS NULL OR ss.isActive = true)")
        // List<ProjectInsightDomain> findAllActiveWithHierarchy();

        Page<ProjectInsightDomain> findAllByIsActive(Boolean isActive, Pageable pageable);

        Page<ProjectInsightDomain> findAllByDomain(String domain, Pageable pageable);

        // when searcTearm='Domain' then domain:=searchValue
        // if

}
