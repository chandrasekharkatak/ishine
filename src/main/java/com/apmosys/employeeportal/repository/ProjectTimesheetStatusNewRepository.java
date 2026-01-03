package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTimesheetStatusNewRepository extends JpaRepository<ProjectTimesheetStatusNew, ProjectTimesheetStatusId> {
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    List<ProjectTimesheetStatusNew> findByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    @Query("SELECT p FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId AND p.id.projectId = :projectId")
    Optional<ProjectTimesheetStatusNew> findByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Long projectId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectTimesheetStatusNew p WHERE p.id.timesheetId = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);
}

