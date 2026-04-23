package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Kpis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KpisRepository extends JpaRepository<Kpis, Long> {
    // Add any custom query methods if needed
}