package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeNotificationConsent;

public interface EmployeeNotificationConsentRepository extends JpaRepository<EmployeeNotificationConsent, Long> {

	EmployeeNotificationConsent findByEmpIdAndNotificationId(Long empId, Integer notificationId);

	@Query(nativeQuery = true)
	List<Object[]> getNotificationResponse(Integer notificationId);

	List<EmployeeNotificationConsent> findByNotificationId(Integer notificationId);

}
