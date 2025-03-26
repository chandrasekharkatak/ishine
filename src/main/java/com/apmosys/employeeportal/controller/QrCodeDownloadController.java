package com.apmosys.employeeportal.controller;

import java.nio.file.Paths;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api")
public class QrCodeDownloadController {
	
	@Value("${qr.storage.path}")
	private String qrStoragePath;
	
	@GetMapping("/downloadFileFromQrCode")
	public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName) {
	    try {
	        // Locate the file on the server
	        Path filePath = Paths.get(qrStoragePath + fileName);
	        Resource resource = new UrlResource(filePath.toUri());

	        if (!resource.exists() || !resource.isReadable()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }

	        // Return file as an attachment
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(resource);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}


}
