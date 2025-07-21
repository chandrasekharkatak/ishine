package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param; 

import com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO;
import com.apmosys.employeeportal.model.ProjectManagerMapping;

public interface ProjectManagerMappingRepository extends JpaRepository<ProjectManagerMapping, Long> {

    @Query("SELECT new com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO(e.empId , e.name) from Employee e, ProjectManagerMapping pmm where e.empId = pmm.projectManagerId and pmm.projectId = :projectId ")
    List<ProjectManagerIdAndNameDTO> findProjectManagerIdAndName(@Param("projectId") Long projectId);

}