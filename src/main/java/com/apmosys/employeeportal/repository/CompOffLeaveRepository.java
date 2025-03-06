package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
	public Long countPendingCompOffRequestsByManagerId(Integer managerId);

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

	@Query(nativeQuery = true )
	public Optional<List<CompOffLeave>> findCompOffByEmpId(Long empId);
	
	@Query(nativeQuery = true )
	public List<CompOffLeave> getAllHodsBucketPendingCompOff();

	@Query(nativeQuery = true )
	public List<CompOffLeave> findPendingCompOffOffByEmpId(Long empId);
	@Query(value = " FROM CompOffLeave WHERE fromDate = CURRENT_DATE()")
	public List<CompOffLeave> findEmployeeIsOnCompOffLeaveToday();
}
