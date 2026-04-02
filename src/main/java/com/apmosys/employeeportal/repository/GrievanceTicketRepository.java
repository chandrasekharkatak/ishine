package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.GrievanceTicket;

@Repository
public interface GrievanceTicketRepository extends JpaRepository<GrievanceTicket, Long> {

	List<GrievanceTicket> findByCreatedByEmpIdAndIsActiveOrderByCreatedOnDesc(Long createdByEmpId, Integer isActive);

	Page<GrievanceTicket> findByCreatedByEmpIdAndIsActive(Long createdByEmpId, Integer isActive, Pageable pageable);

	List<GrievanceTicket> findByIsActiveOrderByCreatedOnDesc(Integer isActive);

	Page<GrievanceTicket> findByIsActive(Integer isActive, Pageable pageable);

	Page<GrievanceTicket> findByAssignedToEmpIdAndIsActive(Long assignedToEmpId, Integer isActive, Pageable pageable);

	List<GrievanceTicket> findByAssignedToEmpIdAndIsActiveOrderByCreatedOnDesc(Long assignedToEmpId, Integer isActive);

	Optional<GrievanceTicket> findByTicketIdAndIsActive(Long ticketId, Integer isActive);

	boolean existsByTicketNumber(String ticketNumber);

	@Query("SELECT t FROM GrievanceTicket t WHERE t.status = 'RESOLVED' AND t.isActive = 1 "
			+ "AND t.resolutionDate IS NOT NULL AND t.resolutionDate <= :cutoff")
	List<GrievanceTicket> findResolvedEligibleForAutoClose(@Param("cutoff") Timestamp cutoff);
}
