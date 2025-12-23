package com.apmosys.employeeportal.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ProjectInsightSubService;

public interface ProjectInsightSubServiceRepository extends JpaRepository<ProjectInsightSubService, Long> {

    // update subService name

    @Modifying
    @Transactional
    @Query("UPDATE ProjectInsightSubService s SET s.subService = :subService WHERE s.id = :subServiceId")
    void updateSubServiceName(@Param("subServiceId") Long subServiceId, @Param("subService") String subService);

}
