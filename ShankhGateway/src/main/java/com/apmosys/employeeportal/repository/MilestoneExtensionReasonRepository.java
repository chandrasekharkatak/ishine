package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.apmosys.employeeportal.model.MilestoneExtensionReason;

@Repository
public interface MilestoneExtensionReasonRepository extends JpaRepository<MilestoneExtensionReason, Long> {
    List<MilestoneExtensionReason> findAllByOrderByMilestoneExtensionReasonAsc();
}
