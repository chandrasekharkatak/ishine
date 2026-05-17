package com.apmosys.employeeportal.grievance.audit;

/**
 * High-level audit events for grievance tickets (stored as string in DB).
 */
public enum GrievanceAuditEventType {

	TICKET_CREATED,
	FIELD_UPDATED,
	STATUS_CHANGED,
	COMMENT_ADDED,
	ASSIGNED_USER_CHANGED,
	ATTACHMENT_ADDED,
	TICKET_CLOSED
}
