package com.apmosys.employeeportal.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.TravelDeskTicket;

public interface TravelDeskTicketRepository extends JpaRepository<TravelDeskTicket, Long> {

	@Query("SELECT DISTINCT t FROM TravelDeskTicket t LEFT JOIN FETCH t.lines WHERE t.empId = :empId AND t.isActive = 1")
	List<TravelDeskTicket> findByEmpIdWithLines(@Param("empId") BigInteger empId);

	@Query("SELECT t FROM TravelDeskTicket t LEFT JOIN FETCH t.lines WHERE t.ticketId = :id AND t.isActive = 1")
	Optional<TravelDeskTicket> findByIdWithLines(@Param("id") Long id);

	@Query("SELECT DISTINCT t FROM TravelDeskTicket t LEFT JOIN FETCH t.lines WHERE t.isActive = 1 AND t.workflowStage = :stage")
	List<TravelDeskTicket> findTicketsWithLinesByStage(@Param("stage") String stage);

	@Query("SELECT DISTINCT t FROM TravelDeskTicket t LEFT JOIN FETCH t.lines WHERE t.isActive = 1")
	List<TravelDeskTicket> findAllActiveWithLines();
}
