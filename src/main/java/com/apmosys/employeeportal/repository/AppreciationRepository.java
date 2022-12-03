package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Appreciation;

@Repository
public interface AppreciationRepository extends JpaRepository<Appreciation, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAppreciationByCategories(Long appreciationEventId, String appreciateType);

	@Query(nativeQuery = true)
	List<Object[]> getAppreciateEmployeeByCurrentUser(Long appreciationBy, Long appreciationEventId );
	


}
