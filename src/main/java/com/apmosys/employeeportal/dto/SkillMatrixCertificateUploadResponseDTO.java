package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixCertificateUploadResponseDTO {
	/** Opaque storage key to persist in DB (file_reference_key). */
	private String referenceKey;
	private String originalFilename;
	private Integer fileSizeBytes;
	private String fileMimeType;
}

