package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Kpi;
//import com.apmosys.employeeportal.model.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {
//    List<Kpi> findByStatus(GoalStatus status);
   
    List<Kpi> findByDepartmentIgnoreCase(String department);
	List<Kpi> findByQuarterId(Long quarterId);
	List<Kpi> findByQuarterIdAndDepartmentId(Long quarterId, Long departmentId);
	List<Kpi> findByDepartmentId(Long departmentId);
	List<Kpi> findByQuarterIdAndDepartmentIgnoreCase(Long quarterId, String department);
	
	
//	Kpi findById(Long id);
	
	@Query(nativeQuery = true,value = "select id from kpi_kra where id = :id")
	public Long findbyId(@Param ("id") Long id) ;
	
	@Query(nativeQuery = true, value = "select * from kpi_kra where id= :id")
	public Kpi findByID(Long id);
	


}

