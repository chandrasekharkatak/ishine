package com.apmosys.employeeportal.utility;

import java.util.UUID;

public class FileNameGenerator {
    
     public static String generate(Integer projectId, String extension, String docType) {
        String uuid = UUID.randomUUID().toString();

        // optional: make docType lowercase
        String safeDocType = docType != null ? docType.toLowerCase() : "doc";
        
        if(projectId == null) throw new IllegalArgumentException("ProjectId must not be null");
        return projectId + "_" + safeDocType + "_" + uuid + extension;
    }
}
