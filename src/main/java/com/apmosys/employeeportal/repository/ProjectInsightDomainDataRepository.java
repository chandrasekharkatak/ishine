package com.apmosys.employeeportal.repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;

@Repository
public interface ProjectInsightDomainDataRepository extends  JpaRepository<ProjectInsightDomainData, Long> {

    Boolean existsByName(String name);

    @Query("SELECT d FROM ProjectInsightDomainData d WHERE d.type = 'domain'")
    List<ProjectInsightDomainData> findAllDomain();

    @Query("SELECT d FROM ProjectInsightDomainData d WHERE d.type = 'domain' AND d.isActive = true AND d.isApproved != 'rejected'")
    List<ProjectInsightDomainData> findAllActiveAndApprovedDomain();

    @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy(d.name,d.id, d.createdOn, e.name, d.isActive, d.isApproved, e1.name) FROM ProjectInsightDomainData d LEFT JOIN Employee e ON d.createdBy = e.empId LEFT JOIN Employee e1 ON d.approvedBy = e1.empId")
        Page<ProjectInsightDomainCreatedBy> findAllDomainAndCreatedBy(Pageable pageable);

   @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy(d.name, d.id, d.createdOn, e.name, d.isActive, d.isApproved, e1.name) " +
       "FROM ProjectInsightDomainData d " +
       "LEFT JOIN Employee e ON d.createdBy = e.empId " +
       "LEFT JOIN Employee e1 ON d.approvedBy = e1.empId " +
       "WHERE d.type = 'domain' " +
       "AND (:domain IS NULL OR d.name LIKE CONCAT('%', :domain, '%')) " +
       "AND (:createdBy IS NULL OR e.name LIKE CONCAT('%', :createdBy, '%')) " +
       "AND (:createdOn IS NULL OR FUNCTION('DATE', d.createdOn) = FUNCTION('DATE', :createdOn)) " +
       "AND (:isApproved IS NULL OR d.isApproved = :isApproved) " +
       "AND (:isActive IS NULL OR d.isActive = :isActive)")
Page<ProjectInsightDomainCreatedBy> findAllDomainSearched(
       @Param("domain") String domain,
       @Param("createdBy") String createdBy,
       @Param("createdOn") LocalDateTime createdOn,
       @Param("isActive") Boolean isActive,
       @Param("isApproved") String isApproved,
       Pageable pageable);


    @Query("SELECT d FROM ProjectInsightDomainData d WHERE d.type = 'domain' AND LOWER(d.name) = LOWER(:name)")
    ProjectInsightDomainData findByDomain(@Param("name") String name);

    ProjectInsightDomainData findByName(String name);

    

    @PersistenceContext
    EntityManager entityManager = null;

    default void bulkUpdateDomainNames(List<Map<Long, String>> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder(
            "UPDATE project_insight_domain_data d " +
            "SET d.name = CASE d.id "
        );

        List<Long> ids = new ArrayList<>();

        for (Map<Long, String> update : updates) {
            Map.Entry<Long, String> entry = update.entrySet().iterator().next();
            sql.append("WHEN ").append(entry.getKey()).append(" THEN '").append(entry.getValue()).append("' ");
            ids.add(entry.getKey());
        }

        sql.append("ELSE d.name END, ")
           .append("d.updated_at = CURRENT_TIMESTAMP ")
           .append("WHERE d.id IN (")
           .append(ids.stream().map(String::valueOf).collect(Collectors.joining(",")))
           .append(")");

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }
    
    @Query("SELECT d FROM ProjectInsightDomainData d WHERE d.id = :id AND LOWER(d.type) = LOWER(:type)")
    ProjectInsightDomainData findByIdAndType(@Param("id") Long id, @Param("type") String type);

    // List<ProjectInsightDomainData> findByParentId(Long id);

    @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto(" +
       "d.id, d.name, d.type, " +
       "CASE WHEN (SELECT COUNT(c) FROM com.apmosys.employeeportal.model.ProjectInsightDomainData c WHERE c.parent.id = d.id) > 0 THEN TRUE ELSE FALSE END) " +
       "FROM com.apmosys.employeeportal.model.ProjectInsightDomainData d " +
       "WHERE (:id IS NULL OR d.parent.id = :id) AND d.type IN :type")
    List<ProjectInsightDomainDataDto> findDomainsByTypeAndParentId(
        @Param("type") List<String> type,
        @Param("id") Long parentId);

    @Query("SELECT d FROM ProjectInsightDomainData d WHERE d.type = 'domain' AND d.isActive = true AND d.isApproved != 'rejected' and d.parent is null")
    List<ProjectInsightDomainData> findAllActiveAndApprovedDomainAndParentIsNull();
}