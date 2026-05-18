package com.apmosys.employeeportal.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO;
import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;

public interface TimesheetRejectionReasonsMasterRepository extends JpaRepository<TimesheetRejectionReasonsMaster, Long> {

	public Optional<TimesheetRejectionReasonsMaster> findByRejectionId(Long rejectionId);

	/**
	 * Find rejection reason by exact text (ignore case). Used e.g. when rejecting timesheet
	 * due to "Half-day leave applied — only half-day timesheet allowed."
	 */
	@Query("SELECT r FROM TimesheetRejectionReasonsMaster r WHERE UPPER(TRIM(r.rejectionReason)) = UPPER(TRIM(:rejectionReason)) AND (r.active IS NULL OR r.active = true)")
	Optional<TimesheetRejectionReasonsMaster> findFirstByRejectionReasonIgnoreCaseAndActive(@Param("rejectionReason") String rejectionReason);
	
    @Query("SELECT new com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO(" +
            "r.rejectionId, r.rejectionReason, r.active, r.createdBy, " +
            "r.createdOn, r.updatedBy, r.updatedOn, c.name as createdByName, u.name as updatedByName) " +
            "FROM TimesheetRejectionReasonsMaster r "
            + "LEFT JOIN Employee c on c.empId = r.createdBy \n"
            + "LEFT JOIN Employee u on u.empId = r.updatedBy ")
    public Optional<List<TimesheetRejectionReasonsMasterDTO>> getAllRejectionReason();

}
