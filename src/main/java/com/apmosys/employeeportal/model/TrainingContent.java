package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "training_content")
public class TrainingContent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer contentId;
	
	@ManyToOne
	@JoinColumn(name = "training_id", nullable = false)
	private TrainingMaster trainingMaster;
	
	@Column(name = "content_type", nullable = false)
	private String contentType; // PPT, PDF, VIDEO, AUDIO, LINK
	
	@Column(name = "content_name", nullable = false)
	private String contentName;
	
	@Column(name = "content_path", length = 1000)
	private String contentPath;
	
	@Column(name = "external_link_url", length = 1000)
	private String externalLinkUrl;
	
	@Column(name = "effective_from", nullable = false)
	private LocalDate effectiveFrom;
	
	@Column(name = "effective_to")
	private LocalDate effectiveTo;
	
	@Column(name = "file_size_bytes")
	private Long fileSizeBytes;
	
	@Column(name = "mime_type")
	private String mimeType;
	
	@Column(name = "active_status", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'true'")
	private String activeStatus;
	
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
	
	@Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
	
	@Column(name = "updated_by")
	private Long updatedBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@Column(name = "updated_on")
	private Timestamp updatedOn;
}
