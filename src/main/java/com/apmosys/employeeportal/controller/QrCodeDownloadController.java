package com.apmosys.employeeportal.controller;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.model.FilePath;
import com.apmosys.employeeportal.repository.FilePathRepo;

import kotlin.io.FilePathComponents;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api")
public class QrCodeDownloadController {
	
	@Autowired
	private FilePathRepo filePathRepo;
	
	@Value("${qr.storage.path}")
	private String qrStoragePath;
	
	@GetMapping("/downloadFileFromQrCode")
	public ResponseEntity<Resource> downloadFile(@RequestParam("id") long id) {
	    try {
	    	
	        FilePath path = filePathRepo.findById(id)
	        .orElseThrow(() -> new RuntimeException("File not found with id " + id));
	        
	         String fullPath = path.getPath();
	        
	        Path filePath = Paths.get(fullPath);
	        
	        Resource resource = new UrlResource(filePath.toUri());
	        

	        if (!resource.exists() || !resource.isReadable()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }
	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	        // Return file as an attachment
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+timeStamp +"\"")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(resource);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}


}
