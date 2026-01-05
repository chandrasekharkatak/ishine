package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;

@Repository
public interface ProjectInsightFacetCategoryRepository extends JpaRepository<ProjectInsightFacetCategory, Long> {

    @Query(value = "Select new com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO(p.facetCategoryId, p.categoryName,p.description) From ProjectInsightFacetCategory p where p.facetCategoryId=:id")
    public Optional<ProjectInsightFacetCategoryDTO> getProjectInsightFacetCategoryById(Long id);

    boolean existsByCategoryNameIgnoreCase(String name);

    @Query("SELECT c.categoryName FROM ProjectInsightFacetCategory c WHERE LOWER(c.categoryName) IN :names")
    List<String> findCategoryNameByCategoryNameInIgnoreCase(List<String> names);

    @Query("SELECT c FROM ProjectInsightFacetCategory c WHERE LOWER(c.categoryName) IN :names")
    List<ProjectInsightFacetCategory> findAllByCategoryNameInIgnoreCase(List<String> names);

    @Query("SELECT c FROM ProjectInsightFacetCategory c WHERE c.facetCategoryId IN :ids")
    List<ProjectInsightFacetCategory> findAllByFacetCategoryIdIn(List<Long> ids);

    @Query("SELECT new com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO(p.facetCategoryId, p.categoryName,p.description) FROM ProjectInsightFacetCategory p WHERE p.facetCategoryId IN :ids")
    List<ProjectInsightFacetCategoryDTO> findFacetCategoryByFacetCategoryIdIn(List<Long> ids);
}
