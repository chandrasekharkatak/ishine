package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.TagMaster;

public interface TagMasterRepository extends JpaRepository<TagMaster, Long>, JpaSpecificationExecutor<TagMaster> {

	List<TagMaster> findByProjectId(Long projectId);

}
