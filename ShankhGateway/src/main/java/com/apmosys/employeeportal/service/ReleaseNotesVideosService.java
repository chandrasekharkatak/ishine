package com.apmosys.employeeportal.service;

import java.io.File;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.model.ReleaseNotesVideos;
import com.apmosys.employeeportal.repository.ReleaseNotesVideosRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;



import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Paths;

@Service

public class ReleaseNotesVideosService {
	
	@Value("${file.location.releasenotesvideos}")
	private String releaseNotesVideosFileLocation;

	@Value("${file.location.releasenotevideos}")
	private String releaseNotesVideosFileLocationOut;
	
	@Autowired
	private ReleaseNotesVideosRepository releaseNotesVideosRepository;

	
	public ServiceResponse getAllNotificationIds() {
		ServiceResponse response = new ServiceResponse();
		try {
	        ArrayList<Integer> notificationIds = releaseNotesVideosRepository.getAllNotificationIds();
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceMessage("Notification IDs retrieved successfully.");
	        response.setServiceResponse(notificationIds);
	    } catch (Exception e) {
	       
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError(e.getMessage());
	        response.setErrorStackTrace(getStackTrace(e));
	    }
	    
	    return response;
	}
	
	
	
	//added code by vishal pandey..........
	public ResponseEntity<?> getReleaseNotesVideoName(Integer id) {
	    try {
	        // Fetch video metadata from repository
	        ReleaseNotesVideos releaseNotesVideos = releaseNotesVideosRepository.getReleaseNotesVideosByNotificationId(id);
	        
	        if (releaseNotesVideos != null) {
	            String fileName = releaseNotesVideos.getName();
	            File videoFile = new File(releaseNotesVideosFileLocationOut + fileName);
	            
	            if (!videoFile.exists()) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                        .body("Video file not found.");
	            }

	            Resource resource = new UrlResource(videoFile.toURI());

	            return ResponseEntity.ok()
	                    .contentType(MediaType.valueOf("video/mp4")) // Adjust if needed
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	                    .body(resource);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("ReleaseNotesVideos with id " + id + " not found.");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Something went wrong: " + e.getMessage());
	    }
	}
	
	private String generateUniqueFileName(String originalFileName) {
		return UUID.randomUUID().toString() + "_" + originalFileName;
	}

	public ServiceResponse saveVideo(MultipartFile file, Integer id) {

		ServiceResponse response = new ServiceResponse();

		try {

			String fileName = generateUniqueFileName(file.getOriginalFilename());

			Path filePath = Paths.get(releaseNotesVideosFileLocation + File.separator + fileName);

			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			System.err.print("done");

			ReleaseNotesVideos release = new ReleaseNotesVideos();
			release.setName(fileName);
			release.setNotificationId(id);
			releaseNotesVideosRepository.save(release);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceMessage("File saved successfully.");
			response.setServiceResponse(fileName);

		} catch (IOException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
	        response.setErrorStackTrace(getStackTrace(e));

		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL_1);
			response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
	        response.setErrorStackTrace(getStackTrace(e));

		}

		return response;
	}
	
	private String getStackTrace(Exception e) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    return sw.toString();
	}





}
