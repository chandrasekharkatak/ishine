package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

		
	List<TimesheetRejectionDetailsNew> findByTimesheetIdAndIsActive(Long timesheetId, Boolean isActive);

	List<TimesheetRejectionDetailsNew> findByTimesheetIdInAndIsActive(List<Long> timesheetIds, Boolean isActive);

	@Modifying
	@Transactional
	@Query(value = "UPDATE timesheet_rejection_details_new \n"
			+ "SET project_id = :primaryProjectId, \n"
			+ "    updated_by = :updatedBy, \n"
			+ "    updated_on = NOW() \n"
			+ "WHERE project_id IN (:deletedProjectIds)", nativeQuery = true)
	int bulkMoveRejectionDetailsProjectToPrimary(
			@Param("primaryProjectId") Integer primaryProjectId,
			@Param("deletedProjectIds") List<Integer> deletedProjectIds,
			@Param("updatedBy") Long updatedBy
	);
}
