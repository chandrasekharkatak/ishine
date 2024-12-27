package com.apmosys.employeeportal.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ReleaseNotesVideos;

public interface ReleaseNotesVideosRepository extends JpaRepository<ReleaseNotesVideos, Integer>{	

    
    ArrayList<Integer> getAllNotificationIds();
    
    @Query( nativeQuery = true)
    ReleaseNotesVideos getReleaseNotesVideosByNotificationId(@Param("id") Integer id);
}