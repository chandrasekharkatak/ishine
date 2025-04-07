package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Short> {
    
	@Query(value = "SELECT * FROM holiday h WHERE h.occasion = :occasion", nativeQuery = true)
	List<Holiday> findByOccasion(@Param("occasion") String occasion);

	List<Holiday> findByDateOfHoliday(LocalDate dateToday);

	List<Holiday> findByOccasionAndDateOfHoliday(String occasion, LocalDate secondSaturday);

	@Query(nativeQuery = true)
	List<Object[]> getHolidayWeekOffSize(String fromDate, String toDate, String state);

	@Query(nativeQuery = true)
	List<Object[]> getAllHolidaysList(String state);

	Holiday findFirstByDateOfHolidayAndState(LocalDate holidayDate, String state);
	
	@Query(nativeQuery = true)
	List<Object[]> getAllHolidays();

	
//	public List<Holiday> findByCustomHoliday(String customHoliday);
	
	@Query(nativeQuery = true)
    List<Holiday> findWeekOffCountByFromAndToDate(LocalDate fromDate, String state);
	
	@Query(nativeQuery = true,value="SELECT YEAR(date_of_holiday) from holiday where occasion = :occasion")
	Integer findYearOfOccassion(@Param("occasion") String occasion);

	@Query(value="SELECT * FROM holiday where date_of_holiday=current_date()", nativeQuery=true)
	List<Holiday> currentDayHoliday();
}
