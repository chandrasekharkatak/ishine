package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PoRequirementMapping;

@Repository
public interface PoRequirementMappingRepository extends JpaRepository<PoRequirementMapping, Long> {
	 
	 PoRequirementMapping findByPoIdAndDepartment(Long poId, String department);
	 
	 @Query("select distinct e.employeementId from EmployeeTeamMap etm " +
	           "inner join Employee e on etm.empId = e.empId " +
	           "where etm.active != 0 and etm.poRequirementMappingId in :poRequirementMappingId")
	    List<Long> checkActiveAndPendingEmployeeMappingWithPoRequirementId(@Param("poRequirementMappingId") List<Long> poRequirementMappingId);
}
