package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitCertificationDTO {
	private String certName;
	private String issuingBody;
	/** yyyy-MM-dd */
	private String dateObtained;
	/** lifetime | 1_year | 2_year | 3_year (or raw label) */
	private String expiryType;
	/** yyyy-MM-dd (optional) */
	private String expiryDate;
	private String credentialId;
	private String credentialUrl;

	/** File metadata from upload endpoint (optional). */
	private String fileReferenceKey;
	private String originalFilename;
	private Integer fileSizeBytes;
	private String fileMimeType;
}

