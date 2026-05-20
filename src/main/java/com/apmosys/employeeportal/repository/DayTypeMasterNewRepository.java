package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.DayTypeMasterNew;

public interface DayTypeMasterNewRepository extends JpaRepository<DayTypeMasterNew, Integer> {

    @Query("SELECT d FROM DayTypeMasterNew d WHERE LOWER(d.dayType) = LOWER(:dayType)")
    DayTypeMasterNew findByDayType(@Param("dayType") String dayType);

    @Query("SELECT d.dayTypeId FROM DayTypeMasterNew d WHERE LOWER(d.dayType) = LOWER(:dayType)")
    Integer findIdByDayType(@Param("dayType") String dayType);

}
