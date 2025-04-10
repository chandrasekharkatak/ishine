package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TagMaster;

public interface TagMasterRepository extends JpaRepository<TagMaster, Long> {

	List<TagMaster> findByProjectId(Long projectId);

}
