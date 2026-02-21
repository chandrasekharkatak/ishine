package com.apmosys.employeeportal.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.TimesheetRejectionDetailsId;
import com.apmosys.employeeportal.model.TimesheetRejectionDetailsNew;

public interface TimesheetRejectionDetailsNewRepository extends JpaRepository<TimesheetRejectionDetailsNew, TimesheetRejectionDetailsId> {

	@Transactional
    @Modifying
    @Query(
            "delete from TimesheetRejectionDetailsNew t " +
            "where t.timesheetId = :timesheetId " +
            "and t.locationMappingId = :locationMappingId " +
            "and t.projectId = :projectId"
    )
    void deleteRow(Long timesheetId, Long locationMappingId, Integer projectId);
	
}
