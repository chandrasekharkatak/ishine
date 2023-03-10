package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

	List<Specialization> findByDomainId(Long domainId);

	Specialization findBySpecializationId(Long specializationId);

	List<Specialization> findBySpecializationIdNotInAndDomainId(List<Long> specializationIds, Long domainId);

	List<Specialization> findByDomainIdAndIsActive(Long domainId, String isActive);

	List<Specialization> findByDomainIdInAndIsActive(List<Long> domainIds, String string);

	List<Specialization> findByDomainIdAndSpecializationIdInAndIsActive(Long domainId, Set<Long> specializationIds,
			String string);

	List<Specialization> findByIsActive(String isActive);

}
