package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "grievance_audit_log_detail", indexes = {
		@Index(name = "idx_griev_audit_detail_audit", columnList = "audit_id"),
		@Index(name = "idx_griev_audit_detail_field", columnList = "field_name")
})
@Data
public class GrievanceAuditLogDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "detail_id")
	private Long detailId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "audit_id", nullable = false)
	private GrievanceAuditLog auditLog;

	@Column(name = "field_name", nullable = false, length = 128)
	private String fieldName;

	@Column(name = "old_value", columnDefinition = "LONGTEXT")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "LONGTEXT")
	private String newValue;
}
