package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.SkillMatrixCertificateUploadResponseDTO;

@Service
public class SkillMatrixCertificateStorageService {

	private static final long MAX_BYTES = 5L * 1024L * 1024L; // 5 MB
	private static final Set<String> ALLOWED_MIME = Set.of("application/pdf", "image/jpeg", "image/png");
	private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");

	@Value("${skillmatrix.certificate.upload-dir}")
	private String uploadDir;

	public SkillMatrixCertificateUploadResponseDTO store(Long empId, MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File is required.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new IllegalArgumentException("Max file size is 5MB.");
		}
		String original = file.getOriginalFilename();
		String ext = extension(original);
		if (!ALLOWED_EXT.contains(ext)) {
			throw new IllegalArgumentException("Only PDF/JPG/PNG files are allowed.");
		}
		String mime = file.getContentType();
		if (!StringUtils.hasText(mime) || !ALLOWED_MIME.contains(mime)) {
			throw new IllegalArgumentException("Only PDF/JPG/PNG files are allowed.");
		}
		String base = StringUtils.hasText(uploadDir) ? uploadDir.trim() : "./uploads/skillmatrix/certificates/";
		Path basePath = Paths.get(base).toAbsolutePath().normalize();

		String ymd = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
		String empPart = empId != null ? String.valueOf(empId) : "0";
		String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		Path rel = Paths.get("emp-" + empPart, ymd, name);
		Path target = basePath.resolve(rel).normalize();
		Files.createDirectories(target.getParent());
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}
		// Reference key stored in DB; keep it relative to base upload dir.
		String referenceKey = rel.toString().replace('\\', '/');
		return new SkillMatrixCertificateUploadResponseDTO(referenceKey, safeOriginal(original), safeInt(file.getSize()), mime);
	}

	private static String safeOriginal(String original) {
		if (!StringUtils.hasText(original)) {
			return null;
		}
		// Avoid any path traversal remnants from old browsers
		String o = original.replace("\\", "/");
		int idx = o.lastIndexOf('/');
		return idx >= 0 ? o.substring(idx + 1) : o;
	}

	private static Integer safeInt(long v) {
		return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
	}

	private static String extension(String originalFilename) {
		if (!StringUtils.hasText(originalFilename)) {
			return "";
		}
		String o = originalFilename.trim();
		int dot = o.lastIndexOf('.');
		if (dot < 0 || dot == o.length() - 1) {
			return "";
		}
		return o.substring(dot + 1).toLowerCase(Locale.ROOT);
	}
}

