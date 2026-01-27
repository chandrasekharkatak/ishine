package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;

@Repository
public interface ClientLocationRepository extends JpaRepository<ClientLocation, Integer> {

	public ClientLocation findByClientIdAndClientLocation(Integer clientId, String clientLocation);

	public List<ClientLocation> findByClientId(Integer clientId);
	
	
	@Query(value = "select * from client_locations where client_id =:clientId",nativeQuery = true)
	ClientLocation findByClientIdd(@Param("clientId") Integer clientId);

	public Optional<ClientLocation> findByClientIdAndClientLocationAndClientState(Integer clientId, String clientLocation, String state );
	
	public Optional<ClientLocation> findByClientIdAndClientLocationAndClientStateIsNull(Integer clientId, String clientLocation);

}
