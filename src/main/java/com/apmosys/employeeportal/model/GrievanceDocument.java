package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "grievance_document")
@Data
public class GrievanceDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "document_id")
	private Long documentId;

	@Column(name = "ticket_id", nullable = false)
	private Long ticketId;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "stored_file_path", nullable = false, length = 1000)
	private String storedFilePath;

	@Column(name = "file_size_bytes")
	private Long fileSizeBytes;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder = 0;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "is_active", nullable = false)
	private Integer isActive = 1;
}
