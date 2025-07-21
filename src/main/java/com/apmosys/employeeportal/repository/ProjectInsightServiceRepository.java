package com.apmosys.employeeportal.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightServiceModel;

public interface ProjectInsightServiceRepository extends JpaRepository<ProjectInsightServiceModel, Long> {

    @Modifying
    @Transactional
    @Query("update ProjectInsightServiceModel s set s.service = :service where s.serviceId = :serviceId")
    void updateServiceName(Long serviceId, String service);
}