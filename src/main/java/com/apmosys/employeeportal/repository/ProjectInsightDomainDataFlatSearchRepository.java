package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.SqlResultSetMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.model.ProjectInsightDomainDataFlatSearch;

public interface ProjectInsightDomainDataFlatSearchRepository extends  JpaRepository<ProjectInsightDomainDataFlatSearch, Long> {

    @Query(value = "SELECT d.* " +
               "FROM project_insight_domain_data_flat_search p " +
               "JOIN project_insight_domain_data d ON p.domain_id = d.id " +
               "WHERE p.domain_id IN (:domainIds) " +
               "AND d.type = 'domain'",
       nativeQuery = true)
    List<ProjectInsightDomainData> findByDomainIdIn(List<Long> domainIds);

//     @Query(value = "SELECT d.* " +
//                "FROM project_insight_domain_data_flat_search p " +
//                "JOIN project_insight_domain_data d ON p.domain_id = d.id " +
//                "WHERE LOWER(p.flat_search) LIKE LOWER(CONCAT('%', :flatSearch, '%')) " +
//                "AND d.type = 'domain'",
//        nativeQuery = true)
//     List<ProjectInsightDomainData> findAllByFlatSearch(String flatSearch);

        @Query(value = "SELECT d.id, d.approved_by, d.created_by, d.created_on, d.is_active, " +
               "       d.is_approved, d.name, d.type, d.updated_at, d.parent_id, d.domaincolor_code " +
               "FROM project_insight_domain_data d " +
               "JOIN project_insight_domain_data_flat_search f ON d.id = f.domain_id " +
               "WHERE MATCH(f.flat_search) AGAINST (:flatSearch IN NATURAL LANGUAGE MODE) " +
               "AND d.type = 'domain'",
                nativeQuery = true)
        List<Object[]> findAllByFlatSearch(@Param("flatSearch") String flatSearch);

        @Query("SELECT p FROM ProjectInsightDomainDataFlatSearch p WHERE p.flatSearch LIKE CONCAT('%', :flatSearch, '%')")
        List<ProjectInsightDomainDataFlatSearch> findByFlatSearch(@Param("flatSearch") String flatSearch);


    @Query(value = "SELECT d.* " +
               "FROM project_insight_domain_data_flat_search p " +
               "JOIN project_insight_domain_data d ON p.domain_id = d.id " +
               "WHERE LOWER(p.flat_search) LIKE LOWER(CONCAT('%', :flatSearch, '%')) " +
               "AND d.type = 'domain' AND d.isApproved != 'rejected'",
       nativeQuery = true)
    List<ProjectInsightDomainData> findAllByFlatSearchNotRejected(String flatSearch);

    @Query(value = "SELECT d.* " +
               "FROM project_insight_domain_data_flat_search p " +
               "JOIN project_insight_domain_data d ON p.domain_id = d.id " +
               "WHERE p.domain_id IN (:domainIds) " +
               "AND d.type = 'domain' AND d.isApproved != 'rejected'",
       nativeQuery = true)
    List<ProjectInsightDomainData> findByDomainIdInNotRejected(List<Long> domainIds);

    Optional<ProjectInsightDomainDataFlatSearch> findByDomainId(Long domainId);

    
}