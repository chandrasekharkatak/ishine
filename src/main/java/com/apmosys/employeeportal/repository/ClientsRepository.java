package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Client;

@Repository
public interface ClientsRepository extends JpaRepository<Client, Integer> {

	Optional<Client> findByClientName(String clientName);

}
