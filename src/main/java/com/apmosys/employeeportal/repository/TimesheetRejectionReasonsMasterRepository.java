package com.apmosys.employeeportal.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO;
import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;

public interface TimesheetRejectionReasonsMasterRepository extends JpaRepository<TimesheetRejectionReasonsMaster, Long> {

	public Optional<TimesheetRejectionReasonsMaster> findByRejectionId(Long rejectionId);
	
    @Query("SELECT new com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO(" +
            "r.rejectionId, r.rejectionReason, r.active, r.createdBy, " +
            "r.createdOn, r.updatedBy, r.updatedOn, c.name as createdByName, u.name as updatedByName) " +
            "FROM TimesheetRejectionReasonsMaster r "
            + "LEFT JOIN Employee c on c.empId = r.createdBy \n"
            + "LEFT JOIN Employee u on u.empId = r.updatedBy ")
    public Optional<List<TimesheetRejectionReasonsMasterDTO>> getAllRejectionReason();

}
