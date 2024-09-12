package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.Timesheet;

@Repository
public interface TimesheetsRepository extends JpaRepository<Timesheet, Long> {

	public List<Timesheet> findAllByEmpIdAndDateBetweenOrderByDateDesc(Long empId,LocalDate start, LocalDate end);

//	@Query(nativeQuery = true)
//	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesTimesheetRequests(Long managerId,String status,LocalDate dateOfJoining);
	
//	@Query(nativeQuery = true)
//	public Long countMyReporteesTimesheetRequests(Long managerId);

	@Query(nativeQuery = true)
	public Long countMyReporteesTimesheetRequests(Long managerId, LocalDate dateOfJoining);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyReporteesApprovedTimesheets(Long managerId, LocalDate start, LocalDate end);
	
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

	@Query(nativeQuery = true)
	public List<Timesheet> findTimesheetOnLeaveDate(Long empId, String start, String end);
	
//	@Query(nativeQuery = true)
//	public Optional<Timesheet> findExistingTimesheetOnLeaveDate(Long empId, LocalDate fromDate, LocalDate toDate);

//	@Query(nativeQuery = true)
//	public List<Timesheet> findTimesheetOnLeaveDate(Long empId, LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllLeaveTimesheetsWithoutLeaveApplication(LocalDate start, LocalDate end);
	
	@Query(nativeQuery = true)
	public List<Object[]> getInactiveActivitiesByTimesheetId(Long timesheetId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getMyTeamsFilledEodCountByManagerId(LocalDate start, LocalDate end, Long managerId);

	@Query(nativeQuery = true)
	public List<Object[]> getTimesheetFilledByMember(Long empId, LocalDate date);
	
//	@Query(value = "SELECT et.emp_id, e.manager_id FROM employee e " +
//            "INNER JOIN employee_timesheets et ON e.emp_id = et.emp_id", nativeQuery = true)
//List<Object[]> findEmployeesAndTheirManagers();
//
//@Modifying
//@Transactional
//@Query(value = "UPDATE employee_timesheets SET current_manager_id = :managerId WHERE emp_id = :empId", nativeQuery = true)
//void updateCurrentManagerId(Long empId, Long managerId);
	
	@Query(value = "SELECT DISTINCT emp_id FROM employee_timesheets", nativeQuery = true)
    List<Long> findDistinctEmpIds();
	
	@Query(value = "SELECT * FROM employee_timesheets WHERE emp_id = :empId ORDER BY created_on DESC", nativeQuery = true)
	List<Timesheet> findTimesheetsByEmpIdOrderByCreatedOn(Long empId);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE employee_timesheets SET current_manager_id = :managerId WHERE timesheet_id = :timesheetId", nativeQuery = true)
	void updateCurrentManagerId(Long timesheetId, Long managerId);

}
