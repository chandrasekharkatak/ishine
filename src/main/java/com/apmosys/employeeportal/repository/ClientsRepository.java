package com.apmosys.employeeportal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ClientsDTO;
import com.apmosys.employeeportal.dto.ClientIdAndName;
import com.apmosys.employeeportal.model.Client;

@Repository
public interface ClientsRepository extends JpaRepository<Client, Integer> {

	Optional<Client> findByClientName(String clientName);

	@Query(value="SELECT new com.apmosys.employeeportal.dto.ClientsDTO(c.clientId, c.clientName, cl.clientLocation, cl.clientLocationId) \n"+
			"from Client c \n" +
			"INNER JOIN ClientLocation cl on cl.clientId = c.clientId ")
	public List<ClientsDTO> getClientInfo();

	Optional<Client> findByPoClientId(Long poClientId);

	Client findByClientId(Integer clientId);

	Optional<Client> findFirstByClientNameLike(String internalClient);

	@Query(value="SELECT c FROM Client c where c.clientName LIKE :internalClient")
	Client findByClientNameList(String internalClient);

	@Query("select new com.apmosys.employeeportal.dto.ClientIdAndName(c.clientId, c.clientName) from Client c")
	List<ClientIdAndName> findAllClientIdAndName();
	
	@Query(value="SELECT c FROM Client c WHERE LOWER(TRIM(c.clientName)) IN :clientNames")
	List<Client> findByTrimmedClientNameIn(List<String> clientNames);

	@Query(value="SELECT c FROM Client c WHERE poClientId IN :poIds")
	List<Client> findByPoClientIdIn(List<Long> poIds);


}
