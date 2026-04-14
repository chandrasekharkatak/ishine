package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.UserContributionDocument;

public interface UserContributionDocumentRepository extends JpaRepository<UserContributionDocument, Long> {

	List<UserContributionDocument> findByUserContributionId(Long userContributionId);

}
