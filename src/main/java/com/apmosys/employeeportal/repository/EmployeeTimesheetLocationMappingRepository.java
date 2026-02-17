package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;

import javax.transaction.Transactional;

public interface EmployeeTimesheetLocationMappingRepository
        extends JpaRepository<EmployeeTimesheetLocationMapping, Long> {

    List<EmployeeTimesheetLocationMapping> findByTimesheetId(Long timesheetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeTimesheetLocationMapping l WHERE l.timesheetId = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);
    
    @Modifying
    @Query(
          "DELETE FROM EmployeeTimesheetActivitiesMappingNew a "
        + "WHERE a.timesheetId = :timesheetId "
        + "AND a.locationMappingId = :locationMappingId "
        + "AND a.projectId = :projectId"
    )
    void deleteByTimesheetIdAndLocationMappingIdAndProjectId(
            @Param("timesheetId") Long timesheetId,
            @Param("locationMappingId") Long locationMappingId,
            @Param("projectId") Integer projectId
    );

}

