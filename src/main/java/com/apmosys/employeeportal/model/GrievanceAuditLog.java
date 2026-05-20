package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import lombok.Data;

@Entity
@Table(name = "grievance_audit_log", indexes = {
		@Index(name = "idx_griev_audit_ticket_time", columnList = "ticket_id,changed_at"),
		@Index(name = "idx_griev_audit_ticket_version", columnList = "ticket_id,version_no")
})
@Data
public class GrievanceAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id")
	private Long auditId;

	@Column(name = "ticket_id", nullable = false)
	private Long ticketId;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Column(name = "summary", length = 2000)
	private String summary;

	@Column(name = "version_no", nullable = false)
	private Integer versionNo;

	@Column(name = "changed_by")
	private Long changedBy;

	@Column(name = "changed_by_name", length = 200)
	private String changedByName;

	@Column(name = "changed_at", nullable = false)
	private Timestamp changedAt;

	@Column(name = "old_snapshot", columnDefinition = "LONGTEXT")
	private String oldSnapshot;

	@Column(name = "new_snapshot", nullable = false, columnDefinition = "LONGTEXT")
	private String newSnapshot;

	@Column(name = "reason", length = 1000)
	private String reason;

	@Column(name = "is_deleted", nullable = false)
	private Integer isDeleted = 0;

	@OneToMany(mappedBy = "auditLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("detailId ASC")
	@BatchSize(size = 50)
	private List<GrievanceAuditLogDetail> details = new ArrayList<>();
}
