package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.FeatureMaster;

@Repository
public interface FeatureMasterRepository extends JpaRepository<FeatureMaster, Long> {

}
