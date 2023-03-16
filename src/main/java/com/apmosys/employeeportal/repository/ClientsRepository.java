package com.apmosys.employeeportal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Client;

@Repository
public interface ClientsRepository extends JpaRepository<Client, Integer> {

	Optional<Client> findByClientName(String clientName);

	@Query(nativeQuery = true)
	public List<Object[]> getClientInfo();

	Client findByPoClientId(Integer poClientId);

	Client findByClientId(Integer clientId);

	Optional<Client> findFirstByClientNameLike(String internalClient);

	@Query(nativeQuery = true)
	Client findByClientNameList(String internalClient);
	
}
