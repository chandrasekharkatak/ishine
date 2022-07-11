package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Timesheet;

@Repository
public interface TimesheetsRepository extends JpaRepository<Timesheet, Long> {

	public List<Timesheet> findAllByEmpIdAndDateBetweenOrderByDateDesc(Long empId,LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status);

	

}
