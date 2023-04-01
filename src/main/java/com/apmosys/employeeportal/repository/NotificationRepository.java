package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllNotications();

	public Notification findByNotificationId(Integer notificationId);

	public List<Notification> findByNotificationTypeAndIsActive(String string, String string2);

}
