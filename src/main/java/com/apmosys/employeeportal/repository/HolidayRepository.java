package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Short> {

	Holiday findByOccasion(String occasion);

	List<Holiday> findByDateOfHoliday(LocalDate dateToday);

	
//	public List<Holiday> findByCustomHoliday(String customHoliday);

}
