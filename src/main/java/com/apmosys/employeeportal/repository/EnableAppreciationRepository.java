package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.model.AppreciationEvent;

@Repository
public interface EnableAppreciationRepository extends JpaRepository<AppreciationEvent , Long>{

	@Query(nativeQuery = true)
	List<Object[]> getAppreciationEventInfo();

	AppreciationEvent findByAppreciationEventName(String appreciationEventName);



}
