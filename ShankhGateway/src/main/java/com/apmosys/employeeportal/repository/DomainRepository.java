package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Domain;

public interface DomainRepository extends JpaRepository<Domain, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAllDomain();

	Domain findByDomainId(Long domainId);

	Domain findByDomainName(String domainName);

	List<Domain> findByDomainIdIn(Set<Long> domainIds);

	List<Domain> findByIsActive(String isActive);
	
	@Query(nativeQuery= true,value="select * from domain where domain_name =:domainName")
	List<Domain>findByDomainnName(String domainName);

}
