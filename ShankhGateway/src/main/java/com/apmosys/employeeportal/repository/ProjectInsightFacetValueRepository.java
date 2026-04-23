package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectInsightFacetValueDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetValue;

@Repository
public interface ProjectInsightFacetValueRepository extends JpaRepository<ProjectInsightFacetValue, Long> {

    @Query(value = "Select new com.apmosys.employeeportal.dto.ProjectInsightFacetValueDTO(p.facetValueId, p.facetValue, p.facetCategoryId) From ProjectInsightFacetValue p where p.facetCategoryId=:id")
    List<ProjectInsightFacetValueDTO> findProjectInsightFacetValueByFacetCategoryId(Long id);

    @Query(value = "Select p From ProjectInsightFacetValue p where p.facetCategoryId=:id and LOWER(p.facetValue)=:facetValue")
    ProjectInsightFacetValue findByFacetCategoryIdAndFacetValue(Long id, String facetValue);

    @Query(value = "Select new com.apmosys.employeeportal.dto.ProjectInsightFacetValueDTO(p.facetValueId, p.facetValue, p.facetCategoryId) From ProjectInsightFacetValue p where p.facetCategoryId=:id and LOWER(p.facetValue) like %:keyword%")
    List<ProjectInsightFacetValueDTO> findProjectInsightFacetValueByFacetCategoryIdAndContainsFacetValue(Long id, String keyword);
    
}
