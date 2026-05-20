package com.apmosys.employeeportal.utility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.Exception.FileValidationException;

@Component
public class TrainingFileValidator {

    private final Tika tika = new Tika();

    private final Map<String, List<String>> ALLOWED_MIME_TYPES = new HashMap<>();
    private final Map<String, List<String>> ALLOWED_EXTENSIONS = new HashMap<>();

    public TrainingFileValidator() {

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

    public void validateFile(
            MultipartFile file,
            String contentType,
            String maxFileSizeProperty) {

        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is required.");
        }

        if (contentType == null || contentType.trim().isEmpty()) {
            throw new FileValidationException("Content type is required.");
        }

        contentType = contentType.toUpperCase();

        long maxSizeBytes = DataSize.parse(maxFileSizeProperty).toBytes();

        if (file.getSize() > maxSizeBytes) {
            throw new FileValidationException(
                    "File size exceeds allowed limit of " + maxFileSizeProperty
            );
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new FileValidationException("Invalid file name.");
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf("."))
                .toLowerCase();

        List<String> allowedExtensions = ALLOWED_EXTENSIONS.get(contentType);
        List<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(contentType);

        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new FileValidationException(
                    "Invalid file extension for " + contentType
            );
        }

        String mimeType = file.getContentType();

        if (mimeType == null || allowedMimeTypes == null
                || !allowedMimeTypes.contains(mimeType)) {

            throw new FileValidationException(
                    "Invalid MIME type for " + contentType
            );
        }

        /*try {
            String detectedMime = tika.detect(file.getInputStream());

            if (!allowedMimeTypes.contains(detectedMime)) {
                throw new FileValidationException(
                        "File content does not match expected type. Detected: "
                                + detectedMime
                );
            }

        } catch (IOException e) {
            throw new FileValidationException(
                    "Error validating file content. " + e.getMessage()
            );
        }*/
    }
}