package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Short> {

	Holiday findByOccasion(String occasion);

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
    List<Holiday> findWeekOffCountByFromAndToDate(LocalDate fromDate);

}
