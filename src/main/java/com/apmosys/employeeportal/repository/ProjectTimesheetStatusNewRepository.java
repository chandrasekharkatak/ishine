package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTimesheetStatusNewRepository extends JpaRepository<ProjectTimesheetStatusNew, ProjectTimesheetStatusId> {
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    List<ProjectTimesheetStatusNew> findByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.projectId = :projectId")
    Optional<ProjectTimesheetStatusNew> findByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    Boolean existsByLocationMappingIdAndStatus(
    		Long locationMappingId,
    		Integer status
    );
    
    List<ProjectTimesheetStatusNew> findByTimesheetIdAndLocationTypeId( Long timesheetId,
            Integer locationTypeId);
    
    List<ProjectTimesheetStatusNew> findByLocationMappingId(Long locationMappingId);
    
    
    @Modifying
    @Query(
          "DELETE FROM ProjectTimesheetStatusNew p "
        + "WHERE p.timesheetId = :timesheetId "
        + "AND p.locationMappingId = :locationMappingId "
        + "AND p.projectId = :projectId"
    )
    void deleteByTimesheetIdAndLocationMappingIdAndProjectId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId,
            @Param("projectId") Integer projectId
    );
    
    
    
    List<ProjectTimesheetStatusNew> findByTimesheetIdAndStatus(
            Long timesheetId,
            Integer status
    );
    
    
    List<ProjectTimesheetStatusNew>
    findByTimesheetIdAndLocationMappingId(
        Long timesheetId,
        Long locationMappingId
    );
    
    
    Optional<ProjectTimesheetStatusNew>
    findByTimesheetIdAndLocationMappingIdAndProjectId(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId
    );



}

