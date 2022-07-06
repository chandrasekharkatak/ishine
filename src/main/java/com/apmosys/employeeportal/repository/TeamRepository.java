package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>{

	public List<Team> findByProjectId(Integer projectId);

}
