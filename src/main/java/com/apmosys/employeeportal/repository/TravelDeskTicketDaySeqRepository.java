package com.apmosys.employeeportal.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.TravelDeskTicketDaySeq;

public interface TravelDeskTicketDaySeqRepository extends JpaRepository<TravelDeskTicketDaySeq, String> {

	@Modifying(flushAutomatically = true, clearAutomatically = false)
	@Query(value = "INSERT IGNORE INTO travel_desk_ticket_day_seq (day_key, last_seq) VALUES (:dayKey, 0)", nativeQuery = true)
	void ensureDayRow(@Param("dayKey") String dayKey);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM TravelDeskTicketDaySeq s WHERE s.dayKey = :dayKey")
	Optional<TravelDeskTicketDaySeq> findByDayKeyForUpdate(@Param("dayKey") String dayKey);
}
