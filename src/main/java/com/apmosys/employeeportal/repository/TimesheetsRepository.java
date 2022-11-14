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
	
	@Query(nativeQuery = true)
	public Long countMyReporteesTimesheetRequests(Long managerId);

	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesApprovedTimesheets(Long managerId,LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getLast7DaysTimesheetsByEmpId(Long empId,LocalDate date);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetsForHomePageByEmpId(Long empId, LocalDate start, LocalDate end);

	public Timesheet findByEmpIdAndDate(Long empId, LocalDate dateToday);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTimesheetData();

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysPendingTimesheetReport(LocalDate start, LocalDate end);

	@Query(nativeQuery = true)
	public List<Object[]> getLast9DaysFilledTimesheetReport(LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTeamTimesheets(Long createdBy, LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllMyTimesheets(Long empId, LocalDate start, LocalDate end);

}
