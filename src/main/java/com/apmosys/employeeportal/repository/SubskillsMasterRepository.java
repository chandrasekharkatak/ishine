package com.apmosys.employeeportal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.SubskillsMaster;

@Repository
public interface SubskillsMasterRepository
		extends JpaRepository<SubskillsMaster, Integer>, JpaSpecificationExecutor<SubskillsMaster> {

	List<SubskillsMaster> findBySkillIdInOrderBySubskillNameAsc(Collection<Integer> skillIds);
}
