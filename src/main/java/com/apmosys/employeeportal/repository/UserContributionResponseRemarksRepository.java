package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.UserContributionResponseRemarks;

public interface UserContributionResponseRemarksRepository extends JpaRepository<UserContributionResponseRemarks, Long>{

	List<UserContributionResponseRemarks> findByUserContributionId(Long userContributionId);

}
