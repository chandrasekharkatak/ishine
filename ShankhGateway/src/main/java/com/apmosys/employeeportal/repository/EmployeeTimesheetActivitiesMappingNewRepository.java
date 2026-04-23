package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface EmployeeTimesheetActivitiesMappingNewRepository extends JpaRepository<EmployeeTimesheetActivitiesMappingNew, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeTimesheetActivitiesMappingNew a WHERE a.timesheetId = :timesheetId")
    void deleteByTimesheetId(@Param("timesheetId") Long timesheetId);
}
