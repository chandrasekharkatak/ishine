package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.KpiResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiResponseRepository extends JpaRepository<KpiResponse, Long> {
    List<KpiResponse> findByEmpId(Long empId);
    List<KpiResponse> findByKpiId(Long kpiId);
    List<KpiResponse> findByQuarterId(Long quarterId);
    List<KpiResponse> findByDepartmentId(Long departmentId);
}