package com.apmosys.employeeportal.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelDesk;

public interface TravelDeskRepository extends JpaRepository<TravelDesk, BigInteger>{

}
