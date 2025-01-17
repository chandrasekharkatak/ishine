package com.apmosys.employeeportal.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ReleaseNotesVideos;

public interface ReleaseNotesVideosRepository extends JpaRepository<ReleaseNotesVideos, Integer>{	

    @Query("SELECT r.notificationId FROM ReleaseNotesVideos r")
    ArrayList<Integer> getAllNotificationIds();
    
    @Query(value = "SELECT * FROM release_notes_videos WHERE notification_id = :id", nativeQuery = true)
    ReleaseNotesVideos getReleaseNotesVideosByNotificationId(@Param("id") Integer id);
}