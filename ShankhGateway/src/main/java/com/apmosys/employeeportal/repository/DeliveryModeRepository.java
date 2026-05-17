package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.*;

import com.apmosys.employeeportal.model.DeliveryMode;

public interface DeliveryModeRepository extends JpaRepository<DeliveryMode, Long> {

}