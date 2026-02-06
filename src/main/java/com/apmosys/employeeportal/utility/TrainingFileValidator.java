package com.apmosys.employeeportal.utility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.FileValidationException;

public class TrainingFileValidator {

    private static final Tika tika = new Tika();

    /* ===============================
       Allowed MIME Types
     =============================== */
    private static final Map<String, List<String>> ALLOWED_MIME_TYPES = new HashMap<>();

    /* ===============================
       Allowed Extensions
     =============================== */
    private static final Map<String, List<String>> ALLOWED_EXTENSIONS = new HashMap<>();

    static {

        ALLOWED_MIME_TYPES.put("PPT", Arrays.asList(
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        ));

        ALLOWED_MIME_TYPES.put("PDF", Collections.singletonList("application/pdf"));

        ALLOWED_MIME_TYPES.put("VIDEO", Arrays.asList(
                "video/mp4", "video/webm", "video/ogg",
                "video/quicktime", "video/x-msvideo"
        ));

        ALLOWED_MIME_TYPES.put("AUDIO", Arrays.asList(
                "audio/mpeg", "audio/mp3", "audio/wav",
                "audio/ogg", "audio/webm"
        ));

        ALLOWED_EXTENSIONS.put("PPT", Arrays.asList(".ppt", ".pptx"));
        ALLOWED_EXTENSIONS.put("PDF", Collections.singletonList(".pdf"));
        ALLOWED_EXTENSIONS.put("VIDEO", Arrays.asList(".mp4", ".webm", ".ogg", ".mov", ".avi"));
        ALLOWED_EXTENSIONS.put("AUDIO", Arrays.asList(".mp3", ".wav", ".ogg", ".webm"));
    }

    /* =========================================================
       MAIN VALIDATION METHOD
     ========================================================= */
    public static void validateFile(
            MultipartFile file,
            String contentType,
            String maxFileSizeProperty
    ) {

        /* ========= 1. NULL / EMPTY CHECK ========= */
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is required.");
        }

        /* ========= 2. CONTENT TYPE CHECK ========= */
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new FileValidationException("Content type is required.");
        }

        contentType = contentType.toUpperCase();

        /* ========= 3. FILE SIZE VALIDATION ========= */
        long maxSizeBytes = DataSize.parse(maxFileSizeProperty).toBytes();

        if (file.getSize() > maxSizeBytes) {
            throw new FileValidationException(
                    "File size exceeds allowed limit of " + maxFileSizeProperty
            );
        }

        /* ========= 4. FILENAME VALIDATION ========= */
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new FileValidationException("Invalid file name.");
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf("."))
                .toLowerCase();

        List<String> allowedExtensions = ALLOWED_EXTENSIONS.get(contentType);

        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new FileValidationException(
                    "Invalid file extension for " + contentType
            );
        }

        /* ========= 5. MIME TYPE VALIDATION ========= */
        String mimeType = file.getContentType();

        List<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(contentType);

        if (mimeType == null || allowedMimeTypes == null
                || !allowedMimeTypes.contains(mimeType)) {

            throw new FileValidationException(
                    "Invalid MIME type for " + contentType
            );
        }

        /* ========= 6. ACTUAL FILE CONTENT VALIDATION (TIKA) ========= */
        try {
            String detectedMime = tika.detect(file.getInputStream());

            if (!allowedMimeTypes.contains(detectedMime)) {
                throw new FileValidationException(
                        "File content does not match expected type. Detected: "
                                + detectedMime
                );
            }

        } catch (IOException e) {
            throw new FileValidationException("Error validating file content."+e.getMessage());
        }
    }
}