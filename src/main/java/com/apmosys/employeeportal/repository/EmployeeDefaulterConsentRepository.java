package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.EmployeeDefaulterConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Repository
public interface EmployeeDefaulterConsentRepository extends JpaRepository<EmployeeDefaulterConsent, Long> {

    Optional<EmployeeDefaulterConsent> findByEmpId(Long empId);

        @Query(value = "SELECT DISTINCT snapshot_year, snapshot_month " +
            "FROM employee_monthly_snapshot " +
            "WHERE emp_id = :empId " +
            "AND is_defaulter_client = 1 " +
            "AND is_month_closed = 1 " +
            "AND (snapshot_year > :cutoffYear OR (snapshot_year = :cutoffYear AND snapshot_month >= :cutoffMonth)) " +
            "ORDER BY snapshot_year DESC, snapshot_month DESC", nativeQuery = true)
    List<Object[]> findDefaulterMonths(  @Param("empId") Long empId,
         @Param("cutoffYear") int cutoffYear,
        @Param("cutoffMonth") int cutoffMonth);
    //in above query we have hardcoded for no months earlier than oct 2025

    @Modifying
    @Transactional
    @Query(value = "UPDATE employee_defaulter_consent SET updated_at = CURRENT_TIMESTAMP WHERE emp_id = :empId", nativeQuery = true)
    void updateConsent(@Param("empId") Long empId);
}