package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.FeatureMaster;
import com.apmosys.employeeportal.model.FieldAlteration;

public interface FieldAlterationRepository extends JpaRepository<FieldAlteration, Long> {

}
