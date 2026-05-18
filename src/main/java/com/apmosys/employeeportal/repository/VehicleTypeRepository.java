package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.VehicleType;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {

	boolean existsByVehicleTypeNameIgnoreCaseAndIdNot(String vehicleTypeName, Long id);

	boolean existsByVehicleTypeNameIgnoreCase(String vehicleTypeName);
}

