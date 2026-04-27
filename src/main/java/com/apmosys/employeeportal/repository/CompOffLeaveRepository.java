package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.PendingCompOffDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.EmployeeLeave;

public interface CompOffLeaveRepository extends JpaRepository<CompOffLeave, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByManagerId(Integer managerId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByEmpIdAndStatus(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllCompOffRequestsByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public String countPendingCompOffRequestsByManagerId(Integer managerId);

	public List<CompOffLeave> findByLeaveTypeMasterId(Short oldLeaveTypeMasterId);

	public List<CompOffLeave> findByEmpIdAndLeaveStatusIdAndCreatedOnAfterAndCompOffStatus(Long empId, short s,
			Timestamp perv45Day, String compOffStatus);

	public List<CompOffLeave> findByLeaveId(Long leaveId);

	@Query(nativeQuery = true)
	public CompOffLeave findOldestCompOffApplicationByEmpId(Long empId, String compOffStatus);

	@Query(nativeQuery = true)
	public List<CompOffLeave> findAllPendingApplicationByEmpId(Long empId, Timestamp perv45Day, String compOffStatus);

	public List<CompOffLeave> findByEmpId(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getCompOffBalanceDetailsByEmpIdAndFromDate(Long empId, String fromDate);

	public List<CompOffLeave> findByEmpIdAndFromDateLessThanEqual(Long empId, LocalDate date);

	public List<CompOffLeave> findByEmpIdAndFromDateGreaterThan(Long empId, LocalDate date);

	public List<CompOffLeave> findByEmpIdAndFromDateLessThanEqualAndCompOffStatusIs(Long empId, LocalDate date,
			String string);

	@Query(nativeQuery = true)
	public List<CompOffLeave> findByLeaveStatusIdAndManagerIdAndCreatedOnBefore(short leaveStatusId, Long managerId,
			Date sevenDaysAgoDate);

	public List<CompOffLeave> findByCompOffStatusAndCreatedOnBefore(String status, Timestamp sevenDaysAgoTimestamp);

	@Query(nativeQuery = true , value = "SELECT * FROM comp_off_leave cl WHERE cl.manager_id = :empId AND cl.comp_off_status='Pending'")
	public Optional<List<CompOffLeave>> findCompOffByEmpId(Long empId);
	
	@Query(nativeQuery = true , value = "select * from comp_off_leave cl where cl.current_approval_level is null and cl.level2approval_status is null and cl.comp_off_status='Pending' and cl.leave_status_id=1")
	public List<CompOffLeave> getAllHodsBucketPendingCompOff();

	@Query(nativeQuery = true , value ="select * from comp_off_leave cl where cl.comp_off_status='Pending' and cl.emp_id= :empId AND leave_status_id=1")
	public List<CompOffLeave> findPendingCompOffOffByEmpId(Long empId);

	public CompOffLeave findByCompOffLeaveId(Long compOffLeaveId);
	
	@Query(value = " FROM CompOffLeave WHERE current_date() BETWEEN  fromDate and toDate")
	public List<CompOffLeave> findEmployeeIsOnCompOffLeaveToday();

	@Query(nativeQuery = true)
	public List<Object[]> getPendingCompOffRequestsByManagerIdInHirarchy(List<Long> empIds);

	@Query(nativeQuery = true)
	public CompOffLeave findOldestCompOffApplicationByEmpIdIn15Days(Long empId, String compOffStatus, LocalDate date);
	
	@Query(nativeQuery = true)
    public Long countMonthlyActiveCompOffByEmpId(
        @Param("empId") Long empId,
        @Param("fromDate") LocalDate fromDate
);

	public boolean existsByEmpIdAndFromDateAndCompOffStatusIn(Long empId, LocalDate appliedForDate, List<String> statuses);

// 	@Query(value = "SELECT cl.* " +
//         "FROM comp_off_leave cl " +
//         "INNER JOIN leave_status s ON s.leave_status_id = cl.leave_status_id " +
//         "WHERE cl.comp_off_status NOT IN ('Approved','Rejected','Expired') " +
//         "AND s.status = 'Pending' " +
//         "AND (cl.manager_approval_status = 'Pending' " +
//         "OR cl.level2approval_status = 'Pending') " +
//         "ORDER BY cl.created_on DESC",
//         nativeQuery = true)
// List<CompOffLeave> getAllPendingCompOff();
@Query(value = "SELECT " +
        "el.comp_off_leave_id AS compOffLeaveId, " +
        "cm.comp_off_reasons AS compOffReasons, " +
        "el.description AS description, " +
        "el.created_on AS createdOn, " +
        "e.name AS createdByName, " +
        "s.status AS status, " +
        "el.from_date AS fromDate, " +
        "el.to_date AS toDate, " +
        "el.no_of_days AS noOfDays, " +
        "el.emp_id AS empId, " +
        "e.email AS email, " +
        "e.employeement_id AS employeementId, " +
        "em.emp_id AS managerId, " +
        "em.email AS managerEmail, " +
        "cl.final_approval_level AS finalApprovalLevel, " +
        "cl.level2approver_id AS level2ApproverId, " +
        "cl.manager_approval_status AS managerApprovalStatus, " +
        "cl.reporting_manager_id AS reportingManagerId, " +
        "cl.current_approval_level AS currentApprovalLevel, " +
        "cl.level2approval_status AS level2ApprovalStatus, " +
        "e2.name AS level2ApproverName, " +
        "em.name AS approverName " +
        "FROM comp_off_leave el " +
        "INNER JOIN employee e ON e.emp_id = el.emp_id " +
        "INNER JOIN leave_status s ON s.leave_status_id = el.leave_status_id " +
        "INNER JOIN comp_off_master cm ON cm.comp_off_id = el.reason " +
        "INNER JOIN employee em ON em.emp_id = e.manager_id " +
        "INNER JOIN comp_off_leave cl ON cl.comp_off_leave_id = el.comp_off_leave_id " +
        "LEFT JOIN employee e2 ON e2.emp_id = cl.level2approver_id " +
        "WHERE cl.comp_off_status NOT IN ('Approved','Rejected','Expired') " +
        "AND s.status = 'Pending' " +
        "ORDER BY el.created_on DESC",
        nativeQuery = true)
List<PendingCompOffDTO> getAllPendingCompOff();
}
