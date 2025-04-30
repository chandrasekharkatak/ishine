package com.apmosys.employeeportal.controller;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api")
public class QrCodeDownloadController {
	
	@Autowired
	private FilePathRepo filePathRepo;
	
	@Value("${qr.storage.path}")
	private String qrStoragePath;
	
	@GetMapping("/downloadFileFromQrCode/{id}")
	public ResponseEntity<?> getQRCodeDataById(@PathVariable Long id, HttpServletResponse response) throws IOException {Optional<FilePath> optionalFilePath = filePathRepo.findById(id);

    if (optionalFilePath.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" Error: QR Code record not found.");
    }

    FilePath filePathEntity = optionalFilePath.get();

    
    if (filePathEntity.getFileData() != null && filePathEntity.getFileData().length > 0) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(filePathEntity.getFileData());
    }

    
    String storedPath = filePathEntity.getQrCodePath();

    if (storedPath != null && !storedPath.isEmpty()) {
        if (storedPath.startsWith("http://") || storedPath.startsWith("https://")) {
            response.sendRedirect(storedPath);
            return ResponseEntity.ok().build();  
        } else {
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Stored Path: " + storedPath);
        }
    }
   
    String responseText = "Name: " + filePathEntity.getName() + "\n" +
                          "Info: " + filePathEntity.getYourInfo();

    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(responseText);}

	
	@GetMapping("/downloadFileFromQrCode/pdf/{id}")
	public ResponseEntity<?> getPdfById(@PathVariable Long id) {
		 Optional<FilePath> optionalFilePath = filePathRepo.findById(id);

		    if (optionalFilePath.isEmpty()) {
		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" Error: PDF record not found.");
		    }

		    FilePath filePathEntity = optionalFilePath.get();
		    byte[] pdfData = filePathEntity.getFileData();

		    if (pdfData == null || pdfData.length == 0) {
		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" No PDF file found for this ID.");
		    }

		    return ResponseEntity.ok()
		            .contentType(MediaType.APPLICATION_PDF)
		            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"document.pdf\"")
		            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdfData.length))
		            .body(pdfData);

	}
	
	@GetMapping("/downloadFileFromQrCode")
	public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName) {
	    try {
	        Path filePath = Paths.get(qrStoragePath + fileName);
	        Resource resource = new UrlResource(filePath.toUri());

	        if (!resource.exists() || !resource.isReadable()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }

	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(resource);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

}
