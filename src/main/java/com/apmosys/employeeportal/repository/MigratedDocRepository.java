package com.apmosys.employeeportal.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.MigratedDoc;

public interface MigratedDocRepository extends JpaRepository<MigratedDoc, Long> {
    @Query("SELECT m.docId FROM MigratedDoc m")
    Set<Long> findAllMigratedDocIds();
}
