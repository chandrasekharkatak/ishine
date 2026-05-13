package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ReimbursementTicket;

public interface ReimbursementTicketRepository extends JpaRepository<ReimbursementTicket, Long> {

	List<ReimbursementTicket> findByEmpIdAndIsActiveOrderBySubmittedOnDesc(BigInteger empId, Integer isActive);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t LEFT JOIN FETCH t.claims WHERE t.empId = :empId AND t.isActive = 1")
	List<ReimbursementTicket> findByEmpIdWithClaims(@Param("empId") BigInteger empId);

	@Query("SELECT t FROM ReimbursementTicket t LEFT JOIN FETCH t.claims WHERE t.ticketId = :id AND t.isActive = 1")
	Optional<ReimbursementTicket> findByIdWithClaims(@Param("id") Long id);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t JOIN FETCH t.claims c WHERE t.isActive = 1 AND t.workflowStage = :stage AND t.hodEmpId = :hodId")
	List<ReimbursementTicket> findTicketsWithClaimsByStageAndHod(@Param("stage") String stage,
			@Param("hodId") BigInteger hodId);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t JOIN FETCH t.claims c WHERE t.isActive = 1 AND t.workflowStage = :stage")
	List<ReimbursementTicket> findTicketsWithClaimsByStage(@Param("stage") String stage);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t JOIN FETCH t.claims c WHERE t.isActive = 1 AND t.hodEmpId = :hodId")
	List<ReimbursementTicket> findAllTicketsWithClaimsByHodEmpId(@Param("hodId") BigInteger hodId);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t JOIN FETCH t.claims c WHERE t.isActive = 1 AND LOWER(TRIM(t.hodEmail)) = LOWER(TRIM(:hodEmail))")
	List<ReimbursementTicket> findAllTicketsWithClaimsByHodEmail(@Param("hodEmail") String hodEmail);

	@Query("SELECT DISTINCT t FROM ReimbursementTicket t LEFT JOIN FETCH t.claims WHERE t.isActive = 1")
	List<ReimbursementTicket> findAllActiveWithClaims();
}
