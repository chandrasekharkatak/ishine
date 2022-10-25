package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.ClientLocation;

@Repository
public interface ClientLocationRepository extends JpaRepository<ClientLocation, Integer> {

	ClientLocation findByClientIdAndClientLocation(Integer clientId, String clientLocation);

}
