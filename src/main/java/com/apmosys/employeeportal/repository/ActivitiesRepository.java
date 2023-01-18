package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Activity;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activity, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllActivitiesByProjectIdAndTeamId(Integer projectId, Long teamId);

	public List<Activity> findByTeamId(Long teamId);

	public List<Activity> findByTeamIdAndEmployeeRoleIn(Long teamId, String[] employeeRole);

}
