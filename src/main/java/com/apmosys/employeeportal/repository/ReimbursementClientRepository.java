package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementClient;

public interface ReimbursementClientRepository extends JpaRepository<ReimbursementClient, Long> {

	List<ReimbursementClient> findByIsActiveOrderByClientNameAsc(Integer isActive);

	Optional<ReimbursementClient> findByIsActiveAndClientNameIgnoreCase(Integer isActive, String clientName);
}
