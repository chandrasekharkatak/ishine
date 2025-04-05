package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.BioMaxRequestIssue;

@Repository
public interface BioMaxRequestIssueRepository extends JpaRepository<BioMaxRequestIssue,Long>{

}
