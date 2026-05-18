package com.apmosys.employeeportal.repository;
import com.apmosys.employeeportal.model.LeaveRejectionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
@Repository
public interface LeaveRejectionDetailRepository
        extends JpaRepository<LeaveRejectionDetail, Long> {

    List<LeaveRejectionDetail> findByLeaveIdAndIsActiveTrue(Long leaveId);

    void deleteByLeaveId(Long leaveId);

    @Modifying
@Query("update LeaveRejectionDetail l " +
       "set l.isActive = false " +
       "where l.leaveId = :leaveId and l.isActive = true")
void deactivateByLeaveId(@Param("leaveId") Long leaveId);
}
