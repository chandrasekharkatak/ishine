package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.GrievanceAuditLog;

public interface GrievanceAuditLogRepository extends JpaRepository<GrievanceAuditLog, Long> {

	@EntityGraph(attributePaths = { "details" })
	Page<GrievanceAuditLog> findByTicketIdAndIsDeletedOrderByVersionNoDesc(Long ticketId, int isDeleted, Pageable pageable);

	@EntityGraph(attributePaths = { "details" })
	@Query("SELECT a FROM GrievanceAuditLog a WHERE a.ticketId = :tid AND a.isDeleted = 0 AND EXISTS (SELECT 1 FROM GrievanceAuditLogDetail d WHERE d.auditLog = a AND LOWER(d.fieldName) = LOWER(:field)) ORDER BY a.versionNo DESC")
	Page<GrievanceAuditLog> findByTicketIdAndField(@Param("tid") Long ticketId, @Param("field") String field, Pageable pageable);

	@EntityGraph(attributePaths = { "details" })
	Optional<GrievanceAuditLog> findByTicketIdAndVersionNoAndIsDeleted(Long ticketId, Integer versionNo, int isDeleted);

	@Query("SELECT a.versionNo FROM GrievanceAuditLog a WHERE a.ticketId = :ticketId AND a.isDeleted = 0 ORDER BY a.versionNo ASC")
	List<Integer> findVersionNumbersByTicketId(@Param("ticketId") Long ticketId);

	List<GrievanceAuditLog> findByTicketIdAndIsDeletedOrderByVersionNoAsc(Long ticketId, int isDeleted);

	@Query("SELECT MAX(a.versionNo) FROM GrievanceAuditLog a WHERE a.ticketId = :ticketId AND a.isDeleted = 0")
	Integer findMaxVersionNoByTicketId(@Param("ticketId") Long ticketId);
}
